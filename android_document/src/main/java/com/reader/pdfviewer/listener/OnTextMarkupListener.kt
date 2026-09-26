package com.reader.pdfviewer.listener

import com.reader.pdfviewer.TextMarkupType

fun interface OnTextMarkupListener {
    /**
     * Called on the main thread after a markup was added to the document in memory,
     * the host app decides when to save it with [com.reader.pdfviewer.PDFView.saveDocument]
     */
    fun onTextMarkupAdded(page: Int, type: TextMarkupType)
}
