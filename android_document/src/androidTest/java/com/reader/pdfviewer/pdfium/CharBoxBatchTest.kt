package com.reader.pdfviewer.pdfium

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** The batched char box reads and mappings must return exactly what the one by one calls return. */
@RunWith(AndroidJUnit4::class)
class CharBoxBatchTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val pdfium = PdfiumCore(context)
    private lateinit var doc: PdfDocument

    @Before
    fun setUp() {
        val file = File(context.cacheDir, "char_boxes.pdf")
        denseTextPdf(file)
        doc = pdfium.newDocument(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY))
        pdfium.openPage(doc, 0)
    }

    @After
    fun tearDown() {
        pdfium.closeDocument(doc)
    }

    @Test
    fun charBoxesMatchOneByOneReads() {
        val boxes = pdfium.getCharBoxes(doc, 0)!!
        val count = pdfium.getPageTextCount(doc, 0)
        assertTrue("page should be dense, has $count chars", count > 2000)
        assertEquals(count * 4, boxes.size)
        for (i in 0 until count) {
            val direct = pdfium.getCharBoxDirect(doc, 0, i)!!
            val expected = floatArrayOf(direct.left, direct.top, direct.right, direct.bottom)
            assertArrayEquals("char $i", expected, boxes.copyOfRange(i * 4, i * 4 + 4), 0f)
            assertEquals("char $i", direct, pdfium.getCharBox(doc, 0, i))
        }
        assertNull(pdfium.getCharBox(doc, 0, count))
        assertNull(pdfium.getCharBox(doc, 0, -1))
    }

    @Test
    fun batchMappingMatchesOneByOneMapping() {
        val boxes = pdfium.getCharBoxes(doc, 0)!!
        // Odd offsets and sizes, like a zoomed and scrolled page.
        for ((x, y, w, h) in listOf(
            intArrayOf(0, 0, 595, 842), intArrayOf(-137, 2411, 1788, 2530), intArrayOf(33, -9, 301, 427)
        ).map { it.toList() }) {
            val mapped = pdfium.mapRectsToDevice(doc, 0, x, y, w, h, 0, boxes)!!
            for (i in 0 until boxes.size / 4) {
                val box = RectF(boxes[i * 4], boxes[i * 4 + 1], boxes[i * 4 + 2], boxes[i * 4 + 3])
                val expected = pdfium.mapRectToDevice(doc, 0, x, y, w, h, 0, box)
                assertEquals("char $i at $x,$y ${w}x$h", expected,
                    RectF(mapped[i * 4], mapped[i * 4 + 1], mapped[i * 4 + 2], mapped[i * 4 + 3]))
            }
        }
        // A slice maps the same rects as the whole page.
        val whole = pdfium.mapRectsToDevice(doc, 0, 5, 6, 700, 990, 0, boxes)!!
        val slice = pdfium.mapRectsToDevice(doc, 0, 5, 6, 700, 990, 0, boxes, 100, 250)!!
        assertArrayEquals(whole.copyOfRange(400, 1000), slice, 0f)
        assertNull(pdfium.mapRectsToDevice(doc, 0, 0, 0, 1, 1, 0, boxes, 10, boxes.size / 4 + 1))
    }

    @Test
    fun cacheIsDroppedWithTheDocument() {
        assertNotNull(pdfium.getCharBoxes(doc, 0))
        assertEquals(1, doc.mTextCharBoxes.size)
        pdfium.closeDocument(doc)
        assertTrue(doc.mTextCharBoxes.isEmpty())
        setUp() // tearDown closes again
    }

    /** Not an assertion, logs how much a tap hit test costs each way (adb logcat -s CharBoxBatchTest). */
    @Test
    fun logHitTestCost() {
        val count = pdfium.getPageTextCount(doc, 0)
        var start = SystemClock.elapsedRealtimeNanos()
        for (i in 0 until count) {
            val box = pdfium.getCharBoxDirect(doc, 0, i)!!
            pdfium.mapRectToDevice(doc, 0, 0, 0, 1080, 1528, 0, box)
        }
        val oneByOne = SystemClock.elapsedRealtimeNanos() - start
        doc.mTextCharBoxes.clear() // measure the first, uncached read too
        start = SystemClock.elapsedRealtimeNanos()
        val boxes = pdfium.getCharBoxes(doc, 0)!!
        pdfium.mapRectsToDevice(doc, 0, 0, 0, 1080, 1528, 0, boxes)
        val batched = SystemClock.elapsedRealtimeNanos() - start
        Log.i("CharBoxBatchTest", "$count chars: one by one ${oneByOne / 1000} us, batched ${batched / 1000} us")
    }

    private fun denseTextPdf(file: File) {
        val pdf = android.graphics.pdf.PdfDocument()
        val paint = Paint().apply { textSize = 9f; color = Color.BLACK }
        val page = pdf.startPage(android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create())
        for (line in 0 until 70) {
            page.canvas.drawText(
                "Line $line: the quick brown fox jumps over the lazy dog 0123456789", 36f, 40f + line * 11, paint
            )
        }
        pdf.finishPage(page)
        file.outputStream().use(pdf::writeTo)
        pdf.close()
    }
}
