package com.reader.pdfviewer.search

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.reader.pdfviewer.DisplayOptions
import com.reader.pdfviewer.PDFSpacing
import com.reader.pdfviewer.PdfFile
import com.reader.pdfviewer.pdfium.PdfiumCore
import com.reader.pdfviewer.pdfium.util.Size
import com.reader.pdfviewer.tools.PdfTools
import com.reader.pdfviewer.util.FitPolicy
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * The reader's search index with the OCR cache, built the way PDFView builds it: reopening a
 * scanned file finds its text, with word positions, without running OCR again.
 */
@RunWith(AndroidJUnit4::class)
class OcrCacheReaderTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val pdfium = PdfiumCore(context)
    private val cacheDir = File(context.cacheDir, "ocr_cache_reader_test")

    @Test
    fun ocrResultsAreReusedWhenTheFileIsOpenedAgain(): Unit = runBlocking {
        cacheDir.deleteRecursively()
        val file = File(context.cacheDir, "reader_scan.pdf")
        PdfTools(context).createFromImages(file) { addImage(textImage()) }

        openIndex(file) { index ->
            assertEquals("an image page has no text before OCR", "", index.pageText(0, allowOcr = false))
            assertTrue(index.pageText(0, allowOcr = true).contains("INVOICE 2026"))
        }

        // Opened again: text and word boxes come from the cache, OCR is not allowed to run.
        openIndex(file) { index ->
            assertTrue(index.pageText(0, allowOcr = false).contains("INVOICE 2026"))
            val word = index.ocrLines(0)!!.flatMap { line -> line.words.map { line.text.substring(it.start, it.end) to it.box } }
                .first { it.first == "INVOICE" }.second
            // Drawn at x=100 of a 1200px wide image; boxes are relative to the page.
            assertEquals(100f / 1200f, word.left, 0.02f)
        }

        // A changed file (e.g. saved with annotations) does not reuse the old results.
        file.appendBytes("\n% changed\n".toByteArray())
        openIndex(file) { index ->
            assertEquals("", index.pageText(0, allowOcr = false))
            assertNull(index.ocrLines(0))
        }
    }

    private suspend fun openIndex(file: File, block: suspend (DocumentTextIndex) -> Unit) {
        val doc = pdfium.newDocument(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY))
        val options = DisplayOptions(true, PDFSpacing(), false, Size(1080, 1920), FitPolicy.WIDTH)
        val pdfFile = PdfFile(pdfium, doc, null, options)
        val index = DocumentTextIndex(pdfFile, DisplayedPageSource(pdfFile), context.cacheDir, OcrCache(cacheDir, file))
        try {
            assertNotNull(index)
            block(index)
        } finally {
            index.close()
            pdfium.closeDocument(doc)
        }
    }

    private fun textImage(): Bitmap {
        val image = createBitmap(1200, 1600, Bitmap.Config.RGB_565).apply { eraseColor(Color.WHITE) }
        val paint = Paint().apply { textSize = 80f; color = Color.BLACK; isAntiAlias = true }
        Canvas(image).drawText("INVOICE 2026", 100f, 400f, paint)
        return image
    }
}
