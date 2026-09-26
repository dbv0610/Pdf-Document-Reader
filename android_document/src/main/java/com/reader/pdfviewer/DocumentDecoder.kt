package com.reader.pdfviewer

import android.content.Context
import com.reader.pdfviewer.source.DocumentSource
import com.reader.pdfviewer.util.FitPolicy
import com.reader.pdfviewer.pdfium.PdfiumCore
import com.reader.pdfviewer.pdfium.util.Size
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

/**
 * Decodes a PDF document on a background dispatcher and delivers the result to [PDFView] on the main thread.
 */
internal class DocumentDecoder(
    private val docSource: DocumentSource,
    private val password: String?,
    private val userPages: IntArray?,
    pdfView: PDFView,
    private val pdfiumCore: PdfiumCore?,
    private val scope: CoroutineScope,
) {
    private val pdfViewReference = WeakReference(pdfView)
    private val contextReference = WeakReference<Context>(pdfView.context)

    private var job: Job? = null

    // Snapshot view state on the main thread, the view must not be touched from the background dispatcher
    private val displayOptions = DisplayOptions(
        isVertical = pdfView.isSwipeVertical,
        pdfSpacing = PDFSpacing(
            pdfView.pageSeparatorSpacing,
            pdfView.startSpacing,
            pdfView.endSpacing,
            pdfView.isAutoSpacingEnabled
        ),
        fitEachPage = pdfView.isFitEachPage,
        viewSize = Size(pdfView.width, pdfView.height),
        pageFitPolicy = pdfView.pageFitPolicy ?: FitPolicy.WIDTH
    )

    fun execute() {
        job = scope.launch {
            val result = try {
                Result.success(withContext(Dispatchers.IO) { decode() })
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                Result.failure(t)
            }

            val pdfView = pdfViewReference.get() ?: return@launch
            result
                .onSuccess { pdfView.loadComplete(it) }
                .onFailure { pdfView.loadError(it) }
        }
    }

    fun cancel() {
        job?.cancel()
        job = null
    }

    private fun decode(): PdfFile {
        if (pdfViewReference.get() == null) throw NullPointerException("pdfView == null")
        val pdfDocument = docSource.createDocument(contextReference.get(), pdfiumCore, password)
            ?: throw IllegalStateException("Cannot create document")
        return PdfFile(pdfiumCore, pdfDocument, userPages, displayOptions)
    }
}
