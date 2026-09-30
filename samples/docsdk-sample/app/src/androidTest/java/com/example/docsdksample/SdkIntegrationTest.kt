package com.example.docsdksample

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.editor.docsdk.DocumentException
import com.editor.docsdk.DocumentTools
import com.editor.docsdk.DocumentType
import com.editor.docsdk.DocumentViewer
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.zip.ZipFile

/**
 * DocSDK used the way an integrator does: only through its public API, from the published
 * (obfuscated) AAR, in an app that is itself minified.
 */
@RunWith(AndroidJUnit4::class)
class SdkIntegrationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val tools = DocumentTools(context)
    private val dir = File(context.cacheDir, "sdk-test").apply { deleteRecursively(); mkdirs() }

    private fun asset(name: String): File =
        File(dir, name).also { f -> instrumentation.context.assets.open(name).use { i -> f.outputStream().use { i.copyTo(it) } } }

    /** Two pictures with text, as PNG files. */
    private fun pictures(): List<File> = (1..2).map { n ->
        val bitmap = Bitmap.createBitmap(1240, 1754, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).apply {
            drawColor(Color.WHITE)
            drawText("PAGE $n OF THE SDK TEST", 120f, 300f, Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 70f; color = Color.BLACK })
        }
        File(dir, "picture$n.png").also { f -> f.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) } }
    }

    private fun reason(block: suspend () -> Unit): DocumentException.Reason? = try {
        runBlocking { block() }
        null
    } catch (e: DocumentException) {
        e.reason
    }

    @Test
    fun toolsWorkThroughThePublicApi() = runBlocking {
        // new blank documents
        for ((type, ext) in listOf(DocumentType.WORD to "docx", DocumentType.EXCEL to "xlsx", DocumentType.POWERPOINT to "pptx")) {
            val file = tools.createDocument(type, File(dir, "new/blank.$ext"))
            assertTrue("$type created", file.length() > 0)
        }
        // pictures to PDF, page count, page picture
        val pdf = File(dir, "pictures.pdf")
        assertEquals(2, tools.imagesToPdf(pictures(), pdf))
        assertEquals(2, tools.pageCount(pdf))
        val page = tools.renderPage(pdf, 1, 400)
        assertEquals(400, page.width)
        // merge and split
        val merged = File(dir, "merged.pdf")
        tools.mergePdfs(listOf(pdf, pdf), merged)
        assertEquals(4, tools.pageCount(merged))
        val split = File(dir, "split.pdf")
        tools.splitPdf(merged, intArrayOf(3, 0), split)
        assertEquals(2, tools.pageCount(split))
        // passwords
        val locked = File(dir, "locked.pdf")
        tools.protectPdf(pdf, locked, "1234")
        assertEquals(DocumentException.Reason.PASSWORD_REQUIRED, reason { tools.pageCount(locked) })
        assertEquals(DocumentException.Reason.PASSWORD_INCORRECT, reason { tools.pageCount(locked, "0000") })
        assertEquals(2, tools.pageCount(locked, "1234"))
        val unlocked = File(dir, "unlocked.pdf")
        tools.unprotectPdf(locked, "1234", unlocked)
        assertEquals(2, tools.pageCount(unlocked))
        // compress, OCR, PDF to Word
        assertTrue(tools.compressPdf(pdf, File(dir, "small.pdf"), DocumentTools.Compression.HIGH) >= 0)
        assertEquals(2, tools.recognizeText(pdf, File(dir, "ocr.pdf")))
        val word = File(dir, "word.docx")
        var progressCalls = 0
        assertEquals(2, tools.pdfToWord(pdf, word) { _, _ -> progressCalls++ })
        assertEquals(2, progressCalls)
        val text = ZipFile(word).use { z -> String(z.getInputStream(z.getEntry("word/document.xml")).readBytes()) }
        assertTrue("OCR text in the Word file", text.contains("PAGE") && text.contains("SDK"))
        // failures are DocumentExceptions
        assertEquals(DocumentException.Reason.NOT_FOUND, reason { tools.pageCount(File(dir, "missing.pdf")) })
        assertEquals(DocumentException.Reason.UNSUPPORTED, reason { tools.createDocument(DocumentType.PDF, File(dir, "x.pdf")) })
    }

    @Test
    fun documentViewOpensEveryType() {
        val pdf = File(dir, "view.pdf")
        runBlocking { tools.imagesToPdf(pictures(), pdf) }
        val pptx = runBlocking { tools.createDocument(DocumentType.POWERPOINT, File(dir, "view.pptx")) }
        val files = listOf(pdf, asset("sample.docx"), asset("sample.xlsx"), pptx)
        for (file in files) {
            ActivityScenario.launch<EmbeddedViewerActivity>(EmbeddedViewerActivity.intent(context, Uri.fromFile(file))).use { scenario ->
                var pages = 0
                var type: DocumentType? = null
                val end = System.currentTimeMillis() + 60_000
                while (pages == 0 && System.currentTimeMillis() < end) {
                    Thread.sleep(300)
                    scenario.onActivity { pages = it.documentView.pageCount; type = it.documentView.documentType }
                }
                assertTrue("${file.name} shows its pages", pages > 0)
                assertEquals(DocumentType.fromFileName(file.name), type)
            }
        }
    }

    @Test
    fun documentViewReportsUnsupportedFiles() {
        val file = File(dir, "notes.xyz").apply { writeText("not a document") }
        ActivityScenario.launch<EmbeddedViewerActivity>(EmbeddedViewerActivity.intent(context, Uri.fromFile(file))).use { scenario ->
            Thread.sleep(1000)
            scenario.onActivity { assertEquals(null, it.documentView.documentType) }
        }
    }

    @Test
    fun viewerScreenOpensAProtectedPdf() {
        val pdf = File(dir, "screen.pdf")
        val locked = File(dir, "screen-locked.pdf")
        runBlocking {
            tools.imagesToPdf(pictures(), pdf)
            tools.protectPdf(pdf, locked, "1234")
        }
        val intent = DocumentViewer.intent(context, Uri.fromFile(locked), DocumentViewer.Options(title = "Test", password = "1234"))
        ActivityScenario.launch<android.app.Activity>(intent).use { scenario ->
            Thread.sleep(4000)
            scenario.onActivity { activity ->
                if (activity.isFinishing) fail("viewer closed itself")
            }
        }
    }
}
