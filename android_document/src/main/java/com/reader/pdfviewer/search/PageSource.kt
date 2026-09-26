package com.reader.pdfviewer.search

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.reader.pdfviewer.PdfFile
import com.reader.pdfviewer.pdfium.PdfDocument
import com.reader.pdfviewer.pdfium.PdfiumCore
import com.reader.pdfviewer.source.DocumentSource

/** Where [DocumentTextIndex] reads page text and renders pages for OCR */
internal interface PageSource {
    fun pageText(page: Int): String?

    fun renderPage(page: Int, bitmap: Bitmap): Boolean

    /** Free the memory held for the pages read so far, called when a pass over the pages ends */
    fun release() {}

    fun close() {}
}

/** Reads the pages of the displayed document, for sources that cannot be opened twice */
internal class DisplayedPageSource(private val file: PdfFile) : PageSource {
    override fun pageText(page: Int): String? = file.readPageText(page)

    override fun renderPage(page: Int, bitmap: Bitmap): Boolean = file.renderPageForOcr(page, bitmap)
}

/**
 * Reads the pages from a second instance of the document. Pdfium keeps the fonts and images of every
 * loaded page in document caches that are only freed with the document, so reading all the pages of a
 * large file through the displayed document grows memory by gigabytes. This instance is reopened every
 * [PAGES_PER_DOCUMENT] pages and closed by [release], so its caches never grow large.
 */
internal class DetachedPageSource(
    private val file: PdfFile,
    private val core: PdfiumCore,
    private val docSource: DocumentSource,
    context: Context,
    private val password: String?
) : PageSource {
    private val context = context.applicationContext
    private var document: PdfDocument? = null
    private var pagesRead = 0
    private var closed = false

    /** Set when the source cannot be opened again, pages are then read from the displayed document */
    private var fallback: DisplayedPageSource? = null

    override fun pageText(page: Int): String? {
        fallback?.let { return it.pageText(page) }
        val doc = document(page) ?: return fallback?.pageText(page)
        return core.readPageText(doc, file.documentPage(page))
    }

    override fun renderPage(page: Int, bitmap: Bitmap): Boolean {
        fallback?.let { return it.renderPage(page, bitmap) }
        val doc = document(page) ?: return fallback?.renderPage(page, bitmap) ?: false
        return core.renderPageBitmapOnce(doc, bitmap, file.documentPage(page))
    }

    @Synchronized
    private fun document(page: Int): PdfDocument? {
        if (closed) {
            return null
        }
        if (pagesRead >= PAGES_PER_DOCUMENT) {
            closeDocument()
        }
        pagesRead++
        document?.let { return it }
        return try {
            docSource.createDocument(context, core, password).also { document = it }
        } catch (e: Exception) {
            Log.w(TAG, "Cannot open a second instance of the document, reading the displayed one", e)
            fallback = DisplayedPageSource(file)
            null
        }
    }

    @Synchronized
    private fun closeDocument() {
        document?.let { core.closeDocument(it) }
        document = null
        pagesRead = 0
    }

    override fun release() = closeDocument()

    @Synchronized
    override fun close() {
        closed = true
        closeDocument()
    }

    companion object {
        private const val TAG = "DetachedPageSource"
        private const val PAGES_PER_DOCUMENT = 150
    }
}
