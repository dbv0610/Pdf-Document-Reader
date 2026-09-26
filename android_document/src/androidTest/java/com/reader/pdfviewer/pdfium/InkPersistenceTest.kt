package com.reader.pdfviewer.pdfium

import android.graphics.Bitmap
import android.graphics.Color
import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class InkPersistenceTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val core = PdfiumCore(context)

    @Test
    fun savedInkHasAppearanceAndSurvivesReopenAndRemoval() {
        val source = File(context.cacheDir, "ink-source.pdf")
        val saved = File(context.cacheDir, "ink-saved.pdf")
        val removed = File(context.cacheDir, "ink-removed.pdf")
        val pdf = android.graphics.pdf.PdfDocument()
        try {
            val page = pdf.startPage(android.graphics.pdf.PdfDocument.PageInfo.Builder(200, 200, 1).create())
            page.canvas.drawColor(Color.WHITE)
            pdf.finishPage(page)
            source.outputStream().use(pdf::writeTo)
        } finally {
            pdf.close()
        }
        open(source) { doc ->
            assertTrue(core.addInkAnnot(doc, 0, floatArrayOf(30f, 100f, 100f, 100f, 150f, 150f), 8f, Color.RED, "stroke"))
            assertTrue(core.addInkAnnot(doc, 0, floatArrayOf(50f, 50f, 50f, 50f), 10f, Color.BLUE, "dot"))
            assertTrue(core.saveAsCopy(doc, saved.absolutePath))
        }
        // Rendering with PDFium alone can mask a missing AP by synthesizing it.
        // Check the persisted dictionaries before any reader opens the output.
        val bytes = saved.readText(Charsets.ISO_8859_1)
        assertEquals(2, Regex("/Subtype\\s*/Ink\\b").findAll(bytes).count())
        assertEquals("Every stroke needs a persisted appearance", 2,
            Regex("/AP\\s*<<").findAll(bytes).count())
        assertTrue(bytes.contains("/InkList"))
        open(saved) { doc ->
            val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
            try {
                core.renderPageBitmap(doc, bitmap, 0, 0, 0, 200, 200, true)
                val line = bitmap.getPixel(70, 100)
                assertTrue("Saved line should be red", Color.red(line) > 200 && Color.green(line) < 60)
                val dot = bitmap.getPixel(50, 150)
                assertTrue("Single tap should remain visible", Color.blue(dot) > 200 && Color.red(dot) < 60)
                assertTrue(core.removeAnnotByName(doc, 0, "stroke"))
                assertTrue(core.removeAnnotByName(doc, 0, "dot"))
                assertTrue(core.saveAsCopy(doc, removed.absolutePath))
            } finally {
                bitmap.recycle()
            }
        }
        open(removed) { doc ->
            assertTrue(core.getAnnotations(doc, 0).isEmpty())
            val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
            try {
                core.renderPageBitmap(doc, bitmap, 0, 0, 0, 200, 200, true)
                assertEquals(Color.WHITE, bitmap.getPixel(70, 100))
                assertEquals(Color.WHITE, bitmap.getPixel(50, 150))
            } finally {
                bitmap.recycle()
            }
        }
    }

    private fun open(file: File, action: (PdfDocument) -> Unit) {
        val doc = core.newDocument(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY))
        try {
            core.openPage(doc, 0)
            action(doc)
        } finally {
            core.closeDocument(doc)
        }
    }
}
