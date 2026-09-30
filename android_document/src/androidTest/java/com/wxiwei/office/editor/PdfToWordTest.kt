/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.wxiwei.office.editor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.reader.pdfviewer.tools.PdfSource
import com.reader.pdfviewer.tools.PdfTools
import com.wxiwei.office.editor.OpenDocument.onMain
import com.wxiwei.office.wp.control.Word
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.zip.ZipFile

/** PDF → Word: a page of text with a picture, a scan and a page of drawings, opened again in the Word reader. */
@RunWith(AndroidJUnit4::class)
class PdfToWordTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val tools = PdfTools(context)

    private fun file(name: String) = File(context.cacheDir, name).also { it.delete() }

    /** Page 1: a centered title, a paragraph of three lines and a picture; page 2: only drawings. */
    private fun textAndDrawings(): File {
        val out = file("to_word_text.pdf")
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val title = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 28f; color = Color.BLACK; textAlign = Paint.Align.CENTER }
        page.canvas.drawText("Báo cáo tháng Chín", 297.5f, 90f, title)
        val body = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 12f; color = Color.BLACK }
        listOf(
            "Đây là đoạn văn đầu tiên của tài liệu, được viết đủ dài để chạy tới",
            "gần lề phải của trang và tiếp tục ở dòng thứ hai, rồi kết thúc ở",
            "dòng thứ ba.",
        ).forEachIndexed { i, line -> page.canvas.drawText(line, 72f, 150f + i * 16f, body) }
        // an opaque picture (no alpha, so pdfium does not see a mask)
        val picture = Bitmap.createBitmap(300, 150, Bitmap.Config.RGB_565).apply { eraseColor(Color.rgb(30, 120, 200)) }
        page.canvas.drawBitmap(picture, 147f, 230f, null)
        page.canvas.drawText("Đoạn sau ảnh.", 72f, 420f, body)
        doc.finishPage(page)
        val drawings = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 2).create())
        drawings.canvas.drawCircle(297f, 300f, 120f, Paint().apply { color = Color.RED })
        drawings.canvas.drawRect(100f, 500f, 495f, 700f, Paint().apply { color = Color.GREEN })
        doc.finishPage(drawings)
        out.outputStream().use { doc.writeTo(it) }
        doc.close()
        return out
    }

    private fun scan(): File {
        val out = file("to_word_scan.pdf")
        val bitmap = Bitmap.createBitmap(1240, 1754, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).apply {
            drawColor(Color.WHITE)
            drawText("HELLO SCANNED WORLD", 120f, 300f, Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 70f; color = Color.BLACK })
        }
        runBlocking { tools.createFromImages(out) { addImage(bitmap) } }
        bitmap.recycle()
        return out
    }

    @Test
    fun convertsTextPicturesScansAndDrawings() {
        val merged = file("to_word_src.pdf")
        val word = file("to_word_out.docx")
        val pages = runBlocking {
            val text = textAndDrawings()
            tools.merge(listOf(PdfSource(text), PdfSource(scan())), merged)
            tools.toWord(PdfSource(merged), word)
        }
        assertEquals(3, pages)

        val xml = ZipFile(word).use { zip ->
            val media = zip.entries().toList().count { it.name.startsWith("word/media/") }
            assertEquals("picture of page 1 + drawing page; the scan under its text is left out", 2, media)
            String(zip.getInputStream(zip.getEntry("word/document.xml")).readBytes())
        }
        assertEquals("pages 2 and 3 start new pages", 2, Regex("<w:pageBreakBefore/>").findAll(xml).count())
        assertTrue("centered title: $xml", xml.contains("<w:jc w:val=\"center\"/>"))

        OpenDocument.open(word, { it.layout != null }) { reader ->
            val shown = onMain {
                val doc = (reader.control!!.getView() as Word).getDocument()
                doc.getText(0, doc.getAreaEnd(0))
            }
            assertTrue("title: $shown", shown.contains("Báo cáo tháng Chín"))
            assertTrue("paragraph joined: $shown", shown.contains("tới gần lề phải của trang và tiếp tục ở dòng thứ hai, rồi kết thúc ở dòng thứ ba."))
            assertTrue("after picture: $shown", shown.contains("Đoạn sau ảnh."))
            assertTrue("OCR text: $shown", shown.contains("HELLO") && shown.contains("WORLD"))
        }
    }
}
