package com.reader.pdfviewer.search

import android.graphics.RectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.RandomAccessFile

@RunWith(AndroidJUnit4::class)
class OcrCacheTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var root: File
    private lateinit var cacheDir: File
    private lateinit var pdf: File

    @Before
    fun setUp() {
        root = File(context.cacheDir, "ocr_cache_test").apply { deleteRecursively(); mkdirs() }
        cacheDir = File(root, "cache")
        pdf = File(root, "scan.pdf").apply { writeBytes(ByteArray(100_000) { (it % 251).toByte() }) }
    }

    @Test
    fun resultsOutliveTheInstance() {
        OcrCache(cacheDir, pdf).apply {
            put(0, lines("INVOICE 2026"))
            put(3, lines("Hóa đơn tiền điện"))
            put(5, emptyList()) // a blank page is a result too, it is not recognized again
        }

        val reopened = OcrCache(cacheDir, pdf)

        assertLines(lines("INVOICE 2026"), reopened.get(0))
        assertLines(lines("Hóa đơn tiền điện"), reopened.get(3))
        assertEquals(emptyList<OcrLine>(), reopened.get(5))
        assertNull(reopened.get(1))
    }

    @Test
    fun renamedFileKeepsItsResultsChangedFileStartsOver() {
        OcrCache(cacheDir, pdf).put(0, lines("INVOICE 2026"))

        val renamed = File(root, "renamed.pdf")
        assertTrue(pdf.renameTo(renamed))
        assertLines(lines("INVOICE 2026"), OcrCache(cacheDir, renamed).get(0))

        // Saved again (e.g. with annotations): other content, other time.
        renamed.appendBytes(byteArrayOf(1, 2, 3))
        assertNull(OcrCache(cacheDir, renamed).get(0))
    }

    @Test
    fun halfWrittenRecordIsDroppedAndLaterPagesStillRead() {
        OcrCache(cacheDir, pdf).apply {
            put(0, lines("first page"))
            put(1, lines("second page"))
        }
        val stored = cacheDir.listFiles()!!.single()
        RandomAccessFile(stored, "rw").use { it.setLength(it.length() - 5) } // killed while writing page 1

        OcrCache(cacheDir, pdf).apply {
            assertLines(lines("first page"), get(0))
            assertNull(get(1))
            put(2, lines("third page"))
        }

        val reopened = OcrCache(cacheDir, pdf)
        assertLines(lines("first page"), reopened.get(0))
        assertNull(reopened.get(1))
        assertLines(lines("third page"), reopened.get(2))
    }

    @Test
    fun unreadableCacheStartsOver() {
        OcrCache(cacheDir, pdf).put(0, lines("INVOICE"))
        cacheDir.listFiles()!!.single().writeBytes(byteArrayOf(9, 9, 9, 9, 9, 9, 9, 9, 9))

        val cache = OcrCache(cacheDir, pdf)
        assertNull(cache.get(0))
        cache.put(1, lines("again"))
        assertLines(lines("again"), OcrCache(cacheDir, pdf).get(1))
    }

    @Test
    fun leastRecentlyUsedFilesAreDroppedOverTheLimit() {
        val page = List(40) { lines("word ".repeat(20).trim()).first() }
        val files = List(4) { n ->
            File(root, "doc$n.pdf").apply { writeBytes(ByteArray(1000) { (it * (n + 3)).toByte() }) }
        }
        OcrCache(cacheDir, files[0]).put(0, page)
        val oneFile = cacheDir.listFiles()!!.single()
        val limit = oneFile.length() * 5 / 2 // room for two files, not three
        oneFile.setLastModified(1_000_000L)

        for (n in 1 until files.size) {
            val before = cacheDir.listFiles()!!.map { it.name }.toSet()
            OcrCache(cacheDir, files[n], maxBytes = limit).put(0, page)
            // Distinct use times whatever the file system time resolution is.
            cacheDir.listFiles()!!.single { it.name !in before }.setLastModified(1_000_000L * (n + 1))
        }

        assertEquals(2, cacheDir.listFiles()!!.size)
        assertNull(OcrCache(cacheDir, files[0], limit).get(0))
        assertNull(OcrCache(cacheDir, files[1], limit).get(0))
        assertLines(page, OcrCache(cacheDir, files[2], limit).get(0))
        assertLines(page, OcrCache(cacheDir, files[3], limit).get(0))
    }

    private fun lines(vararg texts: String): List<OcrLine> = texts.map { text ->
        var start = 0
        val words = text.split(' ').filter { it.isNotEmpty() }.mapIndexed { i, word ->
            val wordStart = text.indexOf(word, start).also { start = it + word.length }
            OcrWord(wordStart, wordStart + word.length, RectF(0.1f * i, 0.2f, 0.1f * i + 0.08f, 0.25f))
        }
        OcrLine(text, words)
    }

    private fun assertLines(expected: List<OcrLine>, actual: List<OcrLine>?) {
        assertEquals(expected.map { it.text }, actual?.map { it.text })
        expected.zip(actual!!).forEach { (e, a) ->
            assertEquals(e.words.map { Triple(it.start, it.end, it.box) }, a.words.map { Triple(it.start, it.end, it.box) })
        }
    }
}
