package com.reader.pdfviewer.source

import android.content.Context
import android.os.ParcelFileDescriptor
import com.reader.pdfviewer.util.FileUtils
import com.reader.pdfviewer.pdfium.PdfDocument
import com.reader.pdfviewer.pdfium.PdfiumCore
import java.io.IOException

class AssetSource(private val assetName: String) : DocumentSource {
    @Throws(IOException::class)
    override fun createDocument(
        context: Context?,
        core: PdfiumCore?,
        password: String?
    ): PdfDocument? {
        if (context == null || core == null) {
            return null
        }
        val f = FileUtils.fileFromAsset(context, assetName)
        val pfd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY)
        return core.newDocument(pfd, password)
    }
}
