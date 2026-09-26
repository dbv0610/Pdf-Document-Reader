package com.reader.pdfviewer.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.graphics.createBitmap
import com.reader.pdfviewer.pdfium.PdfDocument
import com.reader.pdfviewer.pdfium.PdfPageText
import com.reader.pdfviewer.pdfium.PdfiumCore
import com.reader.pdfviewer.search.DocumentTextIndex
import com.reader.pdfviewer.search.PageOcr
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

/** A PDF on disk, with its password when it is encrypted. */
data class PdfSource(val file: File, val password: String? = null)

/** Thrown when [source] cannot be opened; [cause] is a PdfPasswordException when it needs a password. */
class PdfSourceException(val source: PdfSource, cause: Throwable) :
    IOException("Cannot open ${source.file.name}", cause)

/**
 * Split, merge, image to PDF and text extraction (with OCR for scans) on top of pdfium.
 *
 * Threading: every call runs on one background thread shared by all instances ([dispatcher]),
 * never on the caller's thread. pdfium is not thread safe, so each native step takes the global
 * PdfiumCore lock; the steps are kept short (open one file, import one file, add one page, save)
 * so the reader can still render between them. Calls are cancellable between steps, and outputs
 * are written to a temp file first, so a failed or cancelled call never leaves a broken file.
 */
class PdfTools(context: Context) {

    private val pdfium = PdfiumCore(context.applicationContext)

    /** Only used on [dispatcher]; the ML Kit client is created on first OCR and kept for the app's life. */
    private val ocr = PageOcr()

    /** Pages [pages] (zero based, in that order) of [source] into a new PDF at [output]. */
    suspend fun split(source: PdfSource, pages: IntArray, output: File) = withContext(dispatcher) {
        require(pages.isNotEmpty()) { "No pages to split" }
        val src = open(source)
        try {
            extract(src, pages, output)
        } finally {
            pdfium.closeDocument(src)
        }
    }

    /**
     * All pages of [sources], in order, into one PDF at [output].
     * [onProgress] is called on the tools thread after each source with (done, total).
     */
    suspend fun merge(
        sources: List<PdfSource>,
        output: File,
        onProgress: (done: Int, total: Int) -> Unit = { _, _ -> },
    ) = withContext(dispatcher) {
        require(sources.isNotEmpty()) { "No files to merge" }
        val dest = pdfium.newEmptyDocument()
        try {
            sources.forEachIndexed { index, source ->
                currentCoroutineContext().ensureActive()
                val src = open(source)
                try {
                    if (!pdfium.importPages(dest, src)) throw IOException("Cannot copy pages of ${source.file.name}")
                } finally {
                    // Imported pages are copies, so each source is released right away.
                    pdfium.closeDocument(src)
                }
                onProgress(index + 1, sources.size)
            }
            save(dest, output)
        } finally {
            pdfium.closeDocument(dest)
        }
    }

    /**
     * A new PDF at [output] with one page per image added by [block], which runs on the tools
     * thread, so it may decode bitmaps synchronously. At least one page must be added.
     * No OCR is done here: pages stay plain images, the reader recognizes them when searching.
     */
    suspend fun createFromImages(output: File, block: suspend ImagePageWriter.() -> Unit) = withContext(dispatcher) {
        val doc = pdfium.newEmptyDocument()
        try {
            val writer = ImagePageWriter(pdfium, doc)
            writer.block()
            if (writer.pageCount == 0) throw IOException("No pages added")
            save(doc, output)
        } finally {
            pdfium.closeDocument(doc)
        }
    }

    /**
     * Text with character boxes of one page of [source]; null when the page cannot be read.
     * With [allowOcr], a page without a usable text layer (a scan) is recognized with OCR instead.
     */
    suspend fun pageText(source: PdfSource, pageIndex: Int, allowOcr: Boolean = true): PdfPageText? =
        withContext(dispatcher) {
            val src = open(source)
            try {
                textOf(src, pageIndex, allowOcr)
            } finally {
                pdfium.closeDocument(src)
            }
        }

    /**
     * Text with character boxes of every page of [source], handed to [onPage] one page at a time
     * so large documents are never held in memory at once. Stops early when cancelled.
     * With [allowOcr], pages without a usable text layer are recognized with OCR.
     */
    suspend fun readText(
        source: PdfSource,
        allowOcr: Boolean = true,
        onPage: suspend (PdfPageText) -> Unit,
    ) = withContext(dispatcher) {
        val src = open(source)
        try {
            for (index in 0 until pdfium.getPageCount(src)) {
                currentCoroutineContext().ensureActive()
                textOf(src, index, allowOcr)?.let { onPage(it) }
            }
        } finally {
            pdfium.closeDocument(src)
        }
    }

    /** The text layer, or OCR of the rendered page when the layer has no usable text. */
    private suspend fun textOf(doc: PdfDocument, pageIndex: Int, allowOcr: Boolean): PdfPageText? {
        val layer = pdfium.getPageTextLayout(doc, pageIndex) ?: return null
        if (!allowOcr || !DocumentTextIndex.needsOcr(layer.text)) return layer
        val bitmap = renderForOcr(doc, pageIndex, layer.pageWidth, layer.pageHeight) ?: return layer
        return try {
            // Recognition runs on ML Kit's own threads, without holding the pdfium lock.
            PdfPageText.fromOcr(pageIndex, layer.pageWidth, layer.pageHeight, ocr.recognize(bitmap))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "OCR failed on page $pageIndex", e)
            layer
        } finally {
            bitmap.recycle()
        }
    }

    /** Same size as the reader's search OCR, sharp enough for body text. */
    private fun renderForOcr(doc: PdfDocument, pageIndex: Int, pageWidth: Float, pageHeight: Float): Bitmap? {
        if (pageWidth <= 0f || pageHeight <= 0f) return null
        var width = OCR_RENDER_WIDTH
        var height = (width * pageHeight / pageWidth).toInt()
        if (height > OCR_RENDER_MAX_HEIGHT) {
            height = OCR_RENDER_MAX_HEIGHT
            width = (height * pageWidth / pageHeight).toInt()
        }
        if (width <= 0 || height <= 0) return null
        val bitmap = try {
            createBitmap(width, height)
        } catch (e: OutOfMemoryError) {
            return null
        }
        if (!pdfium.renderPageBitmapOnce(doc, bitmap, pageIndex)) {
            bitmap.recycle()
            return null
        }
        return bitmap
    }

    private fun open(source: PdfSource): PdfDocument {
        val fd = try {
            ParcelFileDescriptor.open(source.file, ParcelFileDescriptor.MODE_READ_ONLY)
        } catch (e: IOException) {
            throw PdfSourceException(source, e)
        }
        return try {
            pdfium.newDocument(fd, source.password)
        } catch (e: Throwable) {
            fd.close()
            throw PdfSourceException(source, e)
        }
    }

    private suspend fun extract(src: PdfDocument, pages: IntArray, output: File) {
        val dest = pdfium.newEmptyDocument()
        try {
            if (!pdfium.importPages(dest, src, pages)) throw IOException("Cannot copy pages")
            save(dest, output)
        } finally {
            pdfium.closeDocument(dest)
        }
    }

    private suspend fun save(doc: PdfDocument, output: File) {
        currentCoroutineContext().ensureActive()
        val temp = File(output.parentFile, "${output.name}.tmp")
        try {
            if (!pdfium.saveAsCopy(doc, temp.path)) throw IOException("Cannot write ${output.name}")
            if (!temp.renameTo(output)) throw IOException("Cannot move ${temp.name} to ${output.name}")
        } finally {
            temp.delete()
        }
    }

    /** Adds image pages to a PDF being created by [createFromImages]. */
    class ImagePageWriter internal constructor(private val pdfium: PdfiumCore, private val doc: PdfDocument) {
        var pageCount = 0
            private set

        /**
         * Adds [bitmap] as a page [pageWidth] points wide (A4 by default) whose height follows the
         * image's aspect ratio, so there are no blank bands. The caller still owns [bitmap].
         */
        fun addImage(bitmap: Bitmap, pageWidth: Float = A4_WIDTH, jpegQuality: Int = 90) {
            require(bitmap.width > 0 && bitmap.height > 0) { "Empty bitmap" }
            // Encoding happens outside the pdfium lock; only adding the page takes it.
            val jpeg = encodeJpeg(bitmap, jpegQuality)
            val pageHeight = pageWidth * bitmap.height / bitmap.width
            if (!pdfium.addJpegPage(doc, jpeg, pageWidth, pageHeight)) {
                throw IOException("Cannot add page ${pageCount + 1}")
            }
            pageCount++
        }

        private fun encodeJpeg(bitmap: Bitmap, quality: Int): ByteArray {
            // JPEG has no alpha; flatten on white as a PDF viewer would show it.
            val opaque = if (bitmap.hasAlpha()) {
                createBitmap(bitmap.width, bitmap.height).also {
                    Canvas(it).apply {
                        drawColor(Color.WHITE)
                        drawBitmap(bitmap, 0f, 0f, null)
                    }
                }
            } else bitmap
            try {
                return ByteArrayOutputStream().use { stream ->
                    if (!opaque.compress(Bitmap.CompressFormat.JPEG, quality, stream)) {
                        throw IOException("Cannot encode page ${pageCount + 1}")
                    }
                    stream.toByteArray()
                }
            } finally {
                if (opaque !== bitmap) opaque.recycle()
            }
        }
    }

    companion object {
        private const val TAG = "PdfTools"
        private const val OCR_RENDER_WIDTH = 1600
        private const val OCR_RENDER_MAX_HEIGHT = 2800

        const val A4_WIDTH = 595f

        /**
         * The one thread all tool work runs on, so tools never take more than one IO thread.
         * Calls only interleave where one suspends; each works on its own documents, so that is safe.
         */
        val dispatcher = Dispatchers.IO.limitedParallelism(1)
    }
}
