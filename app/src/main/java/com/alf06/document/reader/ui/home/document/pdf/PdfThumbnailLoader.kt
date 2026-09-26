package com.alf06.document.reader.ui.home.document.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.ParcelFileDescriptor
import android.util.LruCache
import androidx.core.graphics.createBitmap
import com.reader.pdfviewer.pdfium.PdfDocument
import com.reader.pdfviewer.pdfium.PdfiumCore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt

/**
 * Page thumbnails of [file], rendered with pdfium including annotations, so ink and markups
 * the reader saved show up (the system PdfRenderer skips annotations and cannot open protected files).
 */
class PdfThumbnailLoader(
    context: Context,
    private val file: File,
    private val password: String? = null,
    private val thumbnailWidth: Int = 200,
) {
    private val pdfium = PdfiumCore(context.applicationContext)
    private val dispatcher = Dispatchers.IO.limitedParallelism(1)
    private val closed = AtomicBoolean(false)
    private val cache = object : LruCache<Int, Bitmap>(
        minOf(Runtime.getRuntime().maxMemory() / 8, 32L * 1024 * 1024).toInt()
    ) {
        override fun sizeOf(key: Int, value: Bitmap): Int = value.allocationByteCount
    }
    private var document: PdfDocument? = null
    private var unavailable = false

    suspend fun pageCount(): Int = withContext(dispatcher) {
        currentCoroutineContext().ensureActive()
        val doc = if (closed.get()) null else openDocument()
        doc?.let(pdfium::getPageCount) ?: 0
    }

    suspend fun thumbnail(index: Int): Bitmap? = withContext(dispatcher) {
        currentCoroutineContext().ensureActive()
        if (closed.get()) return@withContext null
        cache.get(index)?.let { return@withContext it }
        val doc = openDocument() ?: return@withContext null
        if (index !in 0 until pdfium.getPageCount(doc)) return@withContext null
        try {
            val size = pdfium.getPageSize(doc, index) ?: return@withContext null
            val width = thumbnailWidth.coerceAtLeast(1)
            val height = (width * size.height / size.width.toFloat()).roundToInt().coerceAtLeast(1)
            val bitmap = try {
                createBitmap(width, height)
            } catch (e: OutOfMemoryError) {
                createBitmap((width / 2).coerceAtLeast(1), (height / 2).coerceAtLeast(1), Bitmap.Config.RGB_565)
            }
            bitmap.eraseColor(Color.WHITE)
            currentCoroutineContext().ensureActive()
            if (!pdfium.renderPageBitmapOnce(doc, bitmap, index, renderAnnot = true)) return@withContext null
            cache.put(index, bitmap)
            bitmap
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        } catch (e: OutOfMemoryError) {
            null
        }
    }

    /**
     * Drops the cached thumbnails and the open file. A save replaces the file, and the open
     * document would otherwise keep reading the old one.
     */
    suspend fun invalidate() = withContext(dispatcher) {
        cache.evictAll()
        closeDocument()
        unavailable = false
    }

    private fun openDocument(): PdfDocument? {
        if (unavailable) return null
        document?.let { return it }
        var fd: ParcelFileDescriptor? = null
        return try {
            fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfium.newDocument(fd, password).also { document = it }
        } catch (e: Exception) {
            fd?.close()
            unavailable = true
            null
        }
    }

    private fun closeDocument() {
        document?.let(pdfium::closeDocument)
        document = null
    }

    fun close() {
        if (!closed.compareAndSet(false, true)) return

        CoroutineScope(dispatcher).launch {
            try {
                closeDocument()
            } finally {
                cache.evictAll()
            }
        }
    }
}
