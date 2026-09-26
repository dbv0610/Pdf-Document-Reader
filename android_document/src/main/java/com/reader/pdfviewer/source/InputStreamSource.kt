package com.reader.pdfviewer.source

import android.content.Context
import com.reader.pdfviewer.util.Util
import com.reader.pdfviewer.pdfium.PdfDocument
import com.reader.pdfviewer.pdfium.PdfiumCore
import java.io.IOException
import java.io.InputStream

class InputStreamSource(private val inputStream: InputStream) : DocumentSource {
    @Throws(IOException::class)
    override fun createDocument(
        context: Context?,
        core: PdfiumCore?,
        password: String?
    ): PdfDocument? =  core?.newDocument(Util.toByteArray(inputStream), password)
}
