package com.wxiwei.office.reader

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Process
import android.util.LruCache
import android.view.View
import com.wxiwei.office.common.picture.PictureKit
import com.wxiwei.office.pg.control.Presentation
import com.wxiwei.office.system.OpenTrace
import com.wxiwei.office.wp.control.Word
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.roundToInt

/**
 * Page/slide thumbnails rendered on demand, one at a time at background priority, so only what
 * the screen asks for gets drawn. Each render is a child coroutine of [scope] (under the
 * document's scope), cancelled when its caller is, or all together by dispose() or when the
 * document goes. The most recent ones stay in an LRU cache bounded by bytes. Evicted bitmaps are
 * left to the GC rather than recycled, as a view may still show them.
 */
class PageThumbnails internal constructor(
    private val view: View,
    private val scale: Float,
    maxBytes: Int,
    private val scope: CoroutineScope,
    /** A page handed out without some of its pictures can now be drawn complete; any thread. */
    private val onRedraw: (pageNumber: Int) -> Unit,
) {
    /** One render at a time: pages share view state and drawing kits with each other. */
    private val renderDispatcher = Dispatchers.Default.limitedParallelism(1, "office-thumbnails")

    private val cache = object : LruCache<Int, Bitmap>(maxBytes) {
        override fun sizeOf(key: Int, value: Bitmap) = value.allocationByteCount
    }

    /** Bumped on invalidation so a render started before it is not cached. */
    private val generation = AtomicInteger()

    /** Pages drawn while one of their pictures was still converting. */
    private val pendingPages: MutableSet<Int> = ConcurrentHashMap.newKeySet()

    /** Bumped each time a picture finishes converting. */
    private val conversions = AtomicInteger()

    @Volatile
    private var disposed = false

    /**
     * Thumbnail of [pageNumber] (1-based), or null when the page is not laid out yet or cannot
     * be drawn. Cancelling the caller drops the request if it has not started drawing.
     */
    suspend fun get(pageNumber: Int): Bitmap? {
        cache.get(pageNumber)?.let { return it }
        return onRenderThread {
            cache.get(pageNumber) ?: run {
                val started = generation.get()
                val (bitmap, complete) = renderTracked(pageNumber) { scale }
                // Incomplete pages are not cached: the redraw after the conversion must draw again.
                if (bitmap != null && complete && started == generation.get()) cache.put(pageNumber, bitmap)
                bitmap
            }
        }
    }

    /**
     * [pageNumber] (1-based) drawn [width] px wide, e.g. full screen for a slide show. Not cached;
     * shares the render thread with the thumbnails.
     */
    suspend fun render(pageNumber: Int, width: Int): Bitmap? {
        if (width <= 0) return null
        return onRenderThread {
            renderTracked(pageNumber) { pageWidth -> width.toFloat() / pageWidth }.first
        }
    }

    /**
     * Runs [block] as a child of [scope] on the render dispatcher at background priority.
     * Cancelling the caller cancels it (dropped if not started); disposing returns null.
     */
    private suspend fun <T> onRenderThread(block: () -> T?): T? {
        if (disposed) return null
        val render = scope.async(renderDispatcher) {
            val thread = Process.myTid()
            val priority = Process.getThreadPriority(thread)
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
            try {
                block()
            } finally {
                // Default's threads are shared: give this one its priority back.
                Process.setThreadPriority(priority)
            }
        }
        return try {
            render.await()
        } catch (e: CancellationException) {
            render.cancel()
            currentCoroutineContext().ensureActive() // the caller was cancelled: keep propagating
            null // the thumbnails were disposed
        }
    }

    /**
     * A picture finished converting (EventConstant.TEST_REPAINT_ID); pages drawn without it
     * are handed to onRedraw. Any thread.
     */
    internal fun onPictureConverted() {
        conversions.incrementAndGet()
        drainPendingPages()
    }

    private fun drainPendingPages() {
        for (page in pendingPages.toList()) {
            if (pendingPages.remove(page)) onRedraw(page)
        }
    }

    /** Renders and tells whether every picture made it onto the page. */
    private fun renderTracked(pageNumber: Int, scaleFor: (pageWidth: Int) -> Float): Pair<Bitmap?, Boolean> {
        val conversionsBefore = conversions.get()
        val kit = PictureKit.instance()
        kit.startTrackingPendingPictures()
        val bitmap: Bitmap?
        val pending: Boolean
        try {
            bitmap = render(pageNumber, scaleFor)
        } finally {
            pending = kit.stopTrackingPendingPictures()
        }
        if (bitmap != null && pending) {
            pendingPages += pageNumber
            // A conversion that ended while this page was drawing fired before it was marked.
            if (conversions.get() != conversionsBefore) drainPendingPages()
        }
        return bitmap to !pending
    }

    /** Drops [pageNumber] so the next [get] draws it again (e.g. a Word page that was still being laid out). */
    fun invalidate(pageNumber: Int) {
        generation.incrementAndGet()
        cache.remove(pageNumber)
    }

    fun invalidateAll() {
        generation.incrementAndGet()
        cache.evictAll()
    }

    /** [scaleFor] maps the page width at zoom 1 to the scale to draw at. */
    private fun render(pageNumber: Int, scaleFor: (pageWidth: Int) -> Float): Bitmap? {
        if (disposed) return null
        return try {
            when (view) {
                is Word -> view.getPageBounds(pageNumber)?.let { bounds ->
                    draw(bounds.width(), bounds.height(), scaleFor) { view.drawPage(pageNumber, it) }
                }

                is Presentation -> view.getPageSize()?.let { size ->
                    draw(size.getWidth().toInt(), size.getHeight().toInt(), scaleFor) { view.drawSlide(pageNumber, it) }
                }

                else -> null
            }
        } catch (e: Exception) {
            OpenTrace.e("thumbnail render failed page=$pageNumber", e)
            null
        } catch (e: OutOfMemoryError) {
            OpenTrace.e("thumbnail render out of memory page=$pageNumber", e)
            cache.evictAll()
            null
        }
    }

    private inline fun draw(
        width: Int,
        height: Int,
        scaleFor: (pageWidth: Int) -> Float,
        block: (Canvas) -> Boolean,
    ): Bitmap? {
        if (width <= 0) return null
        val scale = scaleFor(width)
        val w = (width * scale).roundToInt()
        val h = (height * scale).roundToInt()
        if (w <= 0 || h <= 0) return null
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        canvas.scale(scale, scale)
        if (block(canvas)) return bitmap
        bitmap.recycle()
        return null
    }

    internal fun dispose() {
        disposed = true
        scope.cancel()
        pendingPages.clear()
        cache.evictAll()
    }
}
