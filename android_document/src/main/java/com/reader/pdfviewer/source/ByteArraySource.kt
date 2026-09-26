package com.reader.pdfviewer.source

import android.content.Context
import com.reader.pdfviewer.pdfium.PdfDocument
import com.reader.pdfviewer.pdfium.PdfiumCore
import java.io.IOException

class ByteArraySource(private val data: ByteArray?) : DocumentSource {
    @Throws(IOException::class)
    override fun createDocument(
        context: Context?,
        core: PdfiumCore?,
        password: String?
    ): PdfDocument? =core?.newDocument(data, password)
}
