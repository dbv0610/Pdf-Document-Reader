package com.example.docsdksample

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.editor.docsdk.DocumentException
import com.editor.docsdk.DocumentTools
import com.editor.docsdk.DocumentViewer
import kotlinx.coroutines.launch
import java.io.File

/**
 * Three ways to use DocSDK: the ready-made viewer screen, a DocumentView in your own screen,
 * and DocumentTools (here PDF to Word).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var status: TextView
    private val tools by lazy { DocumentTools(this) }
    private var action: (Uri) -> Unit = {}

    private val pickDocument = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) action(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }
        fun button(text: String, onClick: () -> Unit) =
            root.addView(Button(this).apply { this.text = text; setOnClickListener { onClick() } })

        // 1. one line: the SDK's viewer screen
        button("Mở bằng màn xem của SDK") {
            pick { uri -> DocumentViewer.open(this, uri) }
        }
        // 2. DocumentView inside your own screen
        button("Mở trong màn của app (DocumentView)") {
            pick { uri -> startActivity(EmbeddedViewerActivity.intent(this, uri)) }
        }
        // 3. processing without a screen
        button("Chuyển PDF sang Word") {
            pick(arrayOf("application/pdf")) { uri -> convertToWord(uri) }
        }
        status = TextView(this)
        root.addView(status)
        setContentView(root)
    }

    private fun pick(types: Array<String> = DOCUMENT_TYPES, then: (Uri) -> Unit) {
        action = then
        pickDocument.launch(types)
    }

    private fun convertToWord(uri: Uri) {
        lifecycleScope.launch {
            status.text = "Đang chuyển…"
            try {
                // DocumentTools reads files: copy the picked document first
                val input = File(cacheDir, "input.pdf")
                contentResolver.openInputStream(uri)!!.use { i -> input.outputStream().use { i.copyTo(it) } }
                val output = File(getExternalFilesDir(null), "converted.docx")
                val pages = tools.pdfToWord(input, output) { done, total ->
                    runOnUiThread { status.text = "Đang chuyển $done/$total trang" }
                }
                status.text = "Đã chuyển $pages trang: ${output.path}"
                DocumentViewer.open(this@MainActivity, output)
            } catch (e: DocumentException) {
                status.text = "Lỗi: ${e.reason}"
                if (e.reason == DocumentException.Reason.PASSWORD_REQUIRED) {
                    Toast.makeText(this@MainActivity, "PDF có mật khẩu: gọi lại pdfToWord(input, output, password)", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        val DOCUMENT_TYPES = arrayOf(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
        )
    }
}
