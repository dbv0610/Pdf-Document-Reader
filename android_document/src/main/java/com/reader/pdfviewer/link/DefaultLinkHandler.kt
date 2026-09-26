package com.reader.pdfviewer.link

import android.content.Intent
import android.net.Uri
import android.util.Log
import com.reader.pdfviewer.PDFView
import com.reader.pdfviewer.model.LinkTapEvent

class DefaultLinkHandler(private val pdfView: PDFView) : LinkHandler {
    private fun handleUri(uri: String?) {
        val parsedUri = Uri.parse(uri)
        val intent = Intent(Intent.ACTION_VIEW, parsedUri)
        val context = pdfView.getContext()
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent)
        } else {
            Log.w(TAG, "No activity found for URI: " + uri)
        }
    }

    private fun handlePage(page: Int) {
        pdfView.jumpTo(page)
    }

    override fun handleLinkEvent(event: LinkTapEvent?) {
        val uri = event?.link?.uri
        val page = event?.link?.destPageIdx
        if (!uri.isNullOrEmpty()) {
            handleUri(uri)
        } else if (page != null) {
            handlePage(page)
        }
    }

    companion object {
        private val TAG: String = DefaultLinkHandler::class.java.getSimpleName()
    }
}
