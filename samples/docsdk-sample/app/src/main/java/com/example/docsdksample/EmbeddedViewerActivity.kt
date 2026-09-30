package com.example.docsdksample

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.editor.docsdk.DocumentException
import com.editor.docsdk.DocumentView

/** A screen of the app that shows a document with DocumentView, under its own page counter. */
class EmbeddedViewerActivity : AppCompatActivity() {

    lateinit var documentView: DocumentView
        private set
    private lateinit var info: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        info = TextView(this).apply { setPadding(32, 24, 32, 24) }
        documentView = DocumentView(this)
        root.addView(info)
        root.addView(documentView, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)

        documentView.listener = object : DocumentView.Listener {
            override fun onLoaded(pageCount: Int) {
                info.text = "${documentView.documentType}: $pageCount trang"
            }

            override fun onPageChanged(page: Int, pageCount: Int) {
                info.text = "Trang ${page + 1}/$pageCount"
            }

            override fun onError(error: DocumentException) {
                info.text = "Không mở được: ${error.reason}"
            }
        }
        intent.data?.let { documentView.open(it) }
    }

    override fun onDestroy() {
        documentView.close()
        super.onDestroy()
    }

    companion object {
        fun intent(context: Context, uri: Uri): Intent =
            Intent(context, EmbeddedViewerActivity::class.java).setData(uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
}
