package com.reader.pdfviewer.tools

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.reader.pdfviewer.pdfium.PdfiumCore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class PdfToolsTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val tools = PdfTools(context)
    private val pdfium = PdfiumCore(context)
    private lateinit var dir: File

    @Before
    fun setUp() {
        dir = File(context.cacheDir, "pdf_tools_test").apply {
            deleteRecursively()
            mkdirs()
        }
    }

    @Test
    fun splitKeepsRequestedPagesInOrder() = runBlocking {
        val source = textPdf("a.pdf", pages = 3)
        val output = File(dir, "split.pdf")

        tools.split(PdfSource(source), intArrayOf(2, 0), output)

        assertEquals(2, pageCount(output))
        val first = tools.pageText(PdfSource(output), 0)!!
        assertTrue(first.text, first.text.contains("Page 3"))
        assertTrue(tools.pageText(PdfSource(output), 1)!!.text.contains("Page 1"))
        assertNoTempFiles()
    }

    @Test
    fun mergeAppendsAllPagesAndReportsProgress() = runBlocking {
        val a = textPdf("a.pdf", pages = 3)
        val b = textPdf("b.pdf", pages = 2)
        val output = File(dir, "merged.pdf")
        val progress = mutableListOf<Pair<Int, Int>>()

        tools.merge(listOf(PdfSource(a), PdfSource(b)), output) { done, total -> progress += done to total }

        assertEquals(5, pageCount(output))
        assertEquals(listOf(1 to 2, 2 to 2), progress)
        assertTrue(tools.pageText(PdfSource(output), 3)!!.text.contains("Page 1"))
        assertNoTempFiles()
    }

    @Test
    fun createFromImagesAddsOnePagePerImageWithImageAspect() = runBlocking {
        val output = File(dir, "images.pdf")
        val opaque = createBitmap(400, 800, Bitmap.Config.RGB_565).apply { eraseColor(Color.RED) }
        // Transparent image: must come out flattened on white, not black.
        val transparent = createBitmap(300, 300).apply { eraseColor(Color.TRANSPARENT) }

        tools.createFromImages(output) {
            addImage(opaque)
            addImage(transparent)
        }

        assertEquals(2, pageCount(output))
        withDocument(output) { doc ->
            val size = pdfium.getPageSize(doc, 0)!!
            assertEquals(2f, size.height / size.width.toFloat(), 0.02f)
            val bitmap = createBitmap(100, 100)
            assertTrue(pdfium.renderPageBitmapOnce(doc, bitmap, 1))
            val pixel = bitmap.getPixel(50, 50)
            assertTrue("expected white, was ${Integer.toHexString(pixel)}", Color.red(pixel) > 240 && Color.blue(pixel) > 240)
        }
        assertNoTempFiles()
    }

    @Test
    fun pageTextHasWordAndLinePositions() = runBlocking {
        val source = textPdf("a.pdf", pages = 1)

        val page = tools.pageText(PdfSource(source), 0)!!

        assertEquals(PAGE_WIDTH.toFloat(), page.pageWidth, 0.5f)
        assertEquals(PAGE_HEIGHT.toFloat(), page.pageHeight, 0.5f)
        val word = page.words.first { it.text == "hello" }
        // "Page 1 hello world" is drawn with its baseline at (TEXT_X, TEXT_Y), origin top left.
        assertTrue("word box ${word.bounds}", word.bounds.left > TEXT_X && word.bounds.left < TEXT_X + 120)
        assertTrue("word box ${word.bounds}", word.bounds.top < TEXT_Y && word.bounds.bottom > TEXT_Y - 4)
        assertTrue(abs(word.bounds.bottom - TEXT_Y) < TEXT_SIZE)
        val lines = page.lines.map { it.text }
        assertEquals(listOf("Page 1 hello world", "second line"), lines)
        val second = page.lines[1].bounds
        assertTrue("line boxes ${page.lines}", second.top > page.lines[0].bounds.bottom - 1)
        // A box maps onto a rendered bitmap by scale.
        val onBitmap = page.toBitmapRect(word.bounds, PAGE_WIDTH * 2, PAGE_HEIGHT * 2)
        assertEquals(word.bounds.left * 2, onBitmap.left, 0.01f)
    }

    @Test
    fun blankPageHasNoText() = runBlocking {
        val output = File(dir, "blank.pdf")
        tools.createFromImages(output) { addImage(createBitmap(10, 10).apply { eraseColor(Color.WHITE) }) }

        val page = tools.pageText(PdfSource(output), 0)!!

        assertEquals(0, page.charCount)
        assertEquals("", page.text)
        assertTrue(page.words.isEmpty() && page.lines.isEmpty())
    }

    @Test
    fun scannedPageIsRecognizedWithWordPositions() = runBlocking {
        // A "scan": the page is only an image of text, there is no text layer.
        val image = createBitmap(1200, 1600, Bitmap.Config.RGB_565).apply { eraseColor(Color.WHITE) }
        val paint = Paint().apply { textSize = 80f; color = Color.BLACK; isAntiAlias = true }
        Canvas(image).drawText("INVOICE 2026", 100f, 400f, paint)
        val output = File(dir, "scan.pdf")
        tools.createFromImages(output) { addImage(image) }

        assertEquals("", tools.pageText(PdfSource(output), 0, allowOcr = false)!!.text)
        val page = tools.pageText(PdfSource(output), 0)!!

        assertTrue(page.isOcr)
        val word = page.words.firstOrNull { it.text == "INVOICE" }
        assertNotNull("recognized: ${page.text}", word)
        // Drawn at x=100, baseline y=400 of a 1200px wide image on a 595pt wide page.
        val scale = page.pageWidth / 1200f
        assertEquals(100f * scale, word!!.bounds.left, 10f)
        assertTrue("word box ${word.bounds}", word.bounds.bottom > 340f * scale && word.bounds.top < 400f * scale)
        assertTrue(page.words.any { it.text == "2026" })
    }

    /** Not an assertion: logs what ML Kit's Latin model reads from Vietnamese (adb logcat -s PdfToolsTest). */
    @Test
    fun logVietnameseRecognition(): Unit = runBlocking {
        val output = File(dir, "vietnamese.pdf")
        tools.createFromImages(output) { addImage(textImage("Hóa đơn tiền điện", "Người mua hàng")) }
        val text = tools.pageText(PdfSource(output), 0)!!.text
        android.util.Log.i("PdfToolsTest", "Vietnamese OCR: ${text.replace("\r\n", " | ")}")
    }

    @Test
    fun readTextVisitsEveryPage() = runBlocking {
        val source = textPdf("a.pdf", pages = 4)
        val pages = mutableListOf<Int>()

        tools.readText(PdfSource(source)) { pages += it.pageIndex }

        assertEquals(listOf(0, 1, 2, 3), pages)
    }

    @Test
    fun missingSourceFailsWithItsSource() = runBlocking {
        val missing = PdfSource(File(dir, "missing.pdf"))
        try {
            tools.merge(listOf(PdfSource(textPdf("a.pdf", 1)), missing), File(dir, "out.pdf"))
            fail("merge should fail")
        } catch (e: PdfSourceException) {
            assertEquals(missing, e.source)
        }
        assertFalse(File(dir, "out.pdf").exists())
        assertNoTempFiles()
    }

    @Test
    fun cancelledMergeLeavesNoFile() = runBlocking {
        val sources = List(20) { PdfSource(textPdf("s$it.pdf", pages = 20)) }
        val output = File(dir, "cancelled.pdf")
        val job = async {
            tools.merge(sources, output) { done, _ -> if (done == 2) throw CancellationException("stop") }
        }
        try {
            job.await()
            fail("merge should be cancelled")
        } catch (_: CancellationException) {
        }
        assertFalse(output.exists())
        assertNoTempFiles()
    }

    /** Pages with two lines of real (extractable) text: "Page N hello world" and "second line". */
    private fun textPdf(name: String, pages: Int): File {
        val file = File(dir, name)
        val pdf = PdfDocument()
        val paint = Paint().apply { textSize = TEXT_SIZE; color = Color.BLACK }
        repeat(pages) { index ->
            val page = pdf.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, index + 1).create())
            page.canvas.drawText("Page ${index + 1} hello world", TEXT_X, TEXT_Y, paint)
            page.canvas.drawText("second line", TEXT_X, TEXT_Y + 40, paint)
            pdf.finishPage(page)
        }
        file.outputStream().use(pdf::writeTo)
        pdf.close()
        return file
    }

    private fun textImage(first: String, second: String): Bitmap {
        val image = createBitmap(1200, 1600, Bitmap.Config.RGB_565).apply { eraseColor(Color.WHITE) }
        val paint = Paint().apply { textSize = 80f; color = Color.BLACK; isAntiAlias = true }
        Canvas(image).apply {
            drawText(first, 100f, 400f, paint)
            drawText(second, 100f, 560f, paint)
        }
        return image
    }

    private fun pageCount(file: File): Int = withDocument(file) { pdfium.getPageCount(it) }

    private fun <T> withDocument(file: File, block: (com.reader.pdfviewer.pdfium.PdfDocument) -> T): T {
        val doc = pdfium.newDocument(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY))
        try {
            return block(doc)
        } finally {
            pdfium.closeDocument(doc)
        }
    }

    private fun assertNoTempFiles() {
        val temps = dir.listFiles().orEmpty().filter { it.name.endsWith(".tmp") }
        assertTrue("temp files left: $temps", temps.isEmpty())
    }

    private companion object {
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842
        const val TEXT_X = 72f
        const val TEXT_Y = 100f
        const val TEXT_SIZE = 18f
    }
}
