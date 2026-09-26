package com.alf06.document.reader.ui.home.document

import android.content.Context
import android.content.Intent
import com.alf06.document.reader.model.DocumentType
import com.alf06.document.reader.model.RecentDocument
import com.alf06.document.reader.ui.home.document.office.ReadDocumentActivity
import com.alf06.document.reader.ui.home.document.pdf.ReadPdfActivity

fun Context.openDocument(document: RecentDocument): Boolean {
    val intent = when (document.type) {
        DocumentType.Pdf -> Intent(this, ReadPdfActivity::class.java)
            .putExtra(ReadPdfActivity.ARG_DOCUMENT, document)

        DocumentType.Doc, DocumentType.Excel, DocumentType.Ppt, DocumentType.Txt ->
            Intent(this, ReadDocumentActivity::class.java)
                .putExtra(ReadDocumentActivity.ARG_DOCUMENT, document)

        DocumentType.Image -> return false
    }
    startActivity(intent)
    return true
}
