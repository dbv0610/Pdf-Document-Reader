package com.reader.pdfviewer.source

import android.content.Context
import android.net.Uri
import com.reader.pdfviewer.pdfium.PdfDocument
import com.reader.pdfviewer.pdfium.PdfiumCore
import java.io.IOException

class UriSource(val uri: Uri) : DocumentSource {
    @Throws(IOException::class)
    override fun createDocument(
        context: Context?,
        core: PdfiumCore?,
        password: String?
    ): PdfDocument? {
        val pfd = context?.contentResolver?.openFileDescriptor(uri, "r")
            ?: throw IOException("Cannot open file descriptor for $uri")
        return core?.newDocument(pfd, password)
    }
}
