package com.reader.pdfviewer.source

import android.content.Context
import com.reader.pdfviewer.pdfium.PdfDocument
import com.reader.pdfviewer.pdfium.PdfiumCore
import java.io.IOException

interface DocumentSource {
    @Throws(IOException::class)
    fun createDocument(context: Context?, core: PdfiumCore?, password: String?): PdfDocument?
}
