package com.reader.pdfviewer

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import androidx.core.graphics.createBitmap
import com.reader.pdfviewer.exception.PageRenderingException
import com.reader.pdfviewer.model.PagePart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Renders queued [RenderingTask]s one at a time on a background dispatcher
 * and alerts [PDFView.onBitmapRendered] on the main thread when a portion of the PDF is ready.
 *
 * Tasks are consumed by a single coroutine because pdfium and the reusable bounds below are not thread-safe.
 */
internal class PageRenderer(
    private val pdfView: PDFView,
    private val scope: CoroutineScope,
) {
    private val renderBounds = RectF()
    private val roundedRenderBounds = Rect()
    private val renderMatrix = Matrix()

    private val tasks = Channel<RenderingTask>(Channel.UNLIMITED)
    private var renderingJob: Job? = null

    // Only read and written on the main thread
    private var running = false

    @Volatile
    private var renderingGeneration = 0L

    fun addRenderingTask(
        page: Int,
        renderingSize: RenderingSize,
        thumbnail: Boolean,
        cacheOrder: Int,
        renderingZoom: Float,
        bestQuality: Boolean,
        annotationRendering: Boolean,
        isForPrinting: Boolean,
    ) {
        val task = RenderingTask(
            renderingSize,
            page,
            thumbnail,
            cacheOrder,
            renderingZoom,
            bestQuality,
            annotationRendering,
            isForPrinting,
            renderingGeneration,
        )
        tasks.trySend(task)
    }

    fun start() {
        running = true
        renderingJob = scope.launch(Dispatchers.Default) {
            for (task in tasks) render(task)
        }
    }

    fun stop() {
        running = false
        cancelAllTasks()
        tasks.close()
        renderingJob?.cancel()
        renderingJob = null
    }

    fun cancelAllTasks() {
        renderingGeneration++
        cancelPendingTasks()
    }

    /**
     * Keeps an in-progress task eligible for display when only the viewport moved.
     */
    fun cancelPendingTasks() {
        while (tasks.tryReceive().isSuccess) Unit
    }

    private fun CoroutineScope.render(task: RenderingTask) {
        val pagePart = try {
            proceed(task)
        } catch (exception: PageRenderingException) {
            scope.launch(Dispatchers.Main) { pdfView.onPageError(exception) }
            null
        } catch (exception: Exception) {
            Log.e(TAG, "Cannot render page ${task.page}", exception)
            null
        } ?: return

        // Rendering is blocking, the renderer may have been stopped meanwhile
        if (!isActive) {
            pagePart.renderedBitmap?.recycle()
            return
        }

        scope.launch(Dispatchers.Main) {
            if (running &&
                task.renderingGeneration == renderingGeneration &&
                (task.isForPrinting || task.renderingZoom == pdfView.zoom)
            ) {
                pdfView.onBitmapRendered(pagePart, task.isForPrinting)
            } else {
                pagePart.renderedBitmap?.recycle()
            }
        }
    }

    @Throws(PageRenderingException::class)
    private fun proceed(renderingTask: RenderingTask): PagePart? {
        val pdfFile = pdfView.pdfFile
        pdfFile?.openPage(renderingTask.page)

        val w = renderingTask.renderingSize.width.roundToInt()
        val h = renderingTask.renderingSize.height.roundToInt()

        if (w == 0 || h == 0 || pdfFile?.pageHasError(renderingTask.page) == true) {
            return null
        }

        val render = runCatching {
            createBitmap(w, h, if (renderingTask.bestQuality) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565)
        }.onFailure {
            Log.e(TAG, "Cannot create bitmap", it)
        }.getOrNull()

        calculateBounds(w, h, renderingTask.renderingSize.bounds)

        pdfFile?.renderPageBitmap(
            render, renderingTask.page, roundedRenderBounds, renderingTask.annotationRendering
        )

        return PagePart(
            renderingTask.page,
            render,
            renderingTask.renderingSize.bounds,
            renderingTask.thumbnail,
            renderingTask.cacheOrder,
            renderingTask.renderingZoom,
        )
    }

    private fun calculateBounds(width: Int, height: Int, pageSliceBounds: RectF) {
        renderMatrix.reset()
        renderMatrix.postTranslate(-pageSliceBounds.left * width, -pageSliceBounds.top * height)
        renderMatrix.postScale(1 / pageSliceBounds.width(), 1 / pageSliceBounds.height())

        renderBounds[0f, 0f, width.toFloat()] = height.toFloat()
        renderMatrix.mapRect(renderBounds)
        renderBounds.round(roundedRenderBounds)
    }

    data class RenderingSize(
        var width: Float,
        var height: Float,
        var bounds: RectF,
    )

    private data class RenderingTask(
        var renderingSize: RenderingSize,
        var page: Int,
        var thumbnail: Boolean,
        var cacheOrder: Int,
        var renderingZoom: Float,
        var bestQuality: Boolean,
        var annotationRendering: Boolean,
        var isForPrinting: Boolean,
        val renderingGeneration: Long,
    )

    companion object {
        private val TAG: String = PageRenderer::class.java.name
    }
}
