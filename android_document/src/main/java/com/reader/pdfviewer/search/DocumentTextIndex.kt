package com.reader.pdfviewer.search

import android.graphics.Bitmap
import android.util.Log
import com.reader.pdfviewer.PdfFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.ConcurrentHashMap

/**
 * Text of every page of a document, read once and kept in a temporary file so repeated searches
 * don't reload pages through pdfium. Pages without a usable text layer (scans, fonts without
 * unicode mapping) are recognized with OCR when [pageText] is asked to; with an [ocrCache] the
 * results are kept across openings of the file and used even when OCR is not asked for.
 */
internal class DocumentTextIndex(
    private val file: PdfFile,
    private val source: PageSource,
    cacheDir: File,
    private val ocrCache: OcrCache? = null
) {
    private val pageCount = file.pagesCount
    private val states = IntArray(pageCount) // STATE_*
    private val offsets = LongArray(pageCount)
    private val lengths = IntArray(pageCount)
    private val ocrLines = ConcurrentHashMap<Int, List<OcrLine>>()
    private val mutex = Mutex()
    private val ocr = PageOcr()
    private val storageFile: File
    private val storage: RandomAccessFile

    @Volatile
    var isClosed = false
        private set

    init {
        val dir = File(cacheDir, CACHE_DIR_NAME)
        dir.mkdirs()
        // Files of reader sessions that were killed before closing their index
        val staleTime = System.currentTimeMillis() - STALE_FILE_AGE_MS
        dir.listFiles()?.filter { it.lastModified() < staleTime }?.forEach { it.delete() }
        storageFile = File.createTempFile("text", ".idx", dir)
        storage = RandomAccessFile(storageFile, "rw")
    }

    /** Whether [page] has been read, its text layer only when OCR was not allowed */
    fun isIndexed(page: Int, allowOcr: Boolean): Boolean {
        val state = states[page]
        return state == STATE_TEXT || (!allowOcr && state == STATE_NEEDS_OCR)
    }

    /** OCR result of [page], null when the page text came from its text layer */
    fun ocrLines(page: Int): List<OcrLine>? = ocrLines[page]

    /**
     * Text of [page], read on first use. With [allowOcr] false, a page needing OCR returns an empty text.
     */
    suspend fun pageText(page: Int, allowOcr: Boolean): String {
        if (!isIndexed(page, allowOcr)) {
            mutex.withLock { index(page, allowOcr) }
        }
        return if (states[page] == STATE_TEXT) read(page) else ""
    }

    private suspend fun index(page: Int, allowOcr: Boolean) {
        if (isClosed || isIndexed(page, allowOcr)) {
            return
        }
        if (states[page] == STATE_NONE) {
            val text = source.pageText(page).orEmpty()
            if (!needsOcr(text)) {
                write(page, text)
                return
            }
            states[page] = STATE_NEEDS_OCR
        }
        val docPage = file.documentPage(page)
        val cached = if (docPage >= 0) ocrCache?.get(docPage) else null
        val lines = cached ?: if (allowOcr) recognize(page) else null
        if (lines == null) {
            if (allowOcr) {
                // Failed: searchable as empty for now, not cached so a later opening tries again.
                ocrLines[page] = emptyList()
                write(page, "")
            }
            return
        }
        if (cached == null && docPage >= 0) ocrCache?.put(docPage, lines)
        ocrLines[page] = lines
        write(page, lines.joinToString("\n") { it.text })
    }

    /** OCR of [page], null when it could not be rendered or recognized (unlike a page without text). */
    private suspend fun recognize(page: Int): List<OcrLine>? {
        val bitmap = renderForOcr(page) ?: return null
        return try {
            ocr.recognize(bitmap)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "OCR failed on page $page", e)
            null
        } finally {
            bitmap.recycle()
        }
    }

    private fun renderForOcr(page: Int): Bitmap? {
        val size = file.getPageSize(page)
        if (size.width <= 0f || size.height <= 0f) {
            return null
        }
        var width = OCR_RENDER_WIDTH
        var height = (width * size.height / size.width).toInt()
        if (height > OCR_RENDER_MAX_HEIGHT) {
            height = OCR_RENDER_MAX_HEIGHT
            width = (height * size.width / size.height).toInt()
        }
        if (width <= 0 || height <= 0) {
            return null
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        if (!source.renderPage(page, bitmap)) {
            bitmap.recycle()
            return null
        }
        return bitmap
    }

    private fun write(page: Int, text: String) {
        val bytes = text.toByteArray()
        synchronized(storage) {
            if (isClosed) {
                return
            }
            val offset = storage.length()
            storage.seek(offset)
            storage.write(bytes)
            offsets[page] = offset
            lengths[page] = bytes.size
        }
        states[page] = STATE_TEXT
    }

    private fun read(page: Int): String {
        val bytes = ByteArray(lengths[page])
        synchronized(storage) {
            if (isClosed) {
                return ""
            }
            storage.seek(offsets[page])
            storage.readFully(bytes)
        }
        return String(bytes)
    }

    /** Free the memory pdfium holds for the pages read so far, the text stays indexed */
    fun releaseSource() {
        source.release()
    }

    fun close() {
        source.close()
        synchronized(storage) {
            if (isClosed) {
                return
            }
            isClosed = true
            runCatching { storage.close() }
            storageFile.delete()
        }
        ocr.close()
    }

    companion object {
        private const val TAG = "DocumentTextIndex"
        private const val CACHE_DIR_NAME = "pdf_text_index"
        private const val STALE_FILE_AGE_MS = 24 * 60 * 60 * 1000L
        private const val STATE_NONE = 0
        private const val STATE_TEXT = 1
        private const val STATE_NEEDS_OCR = 2
        private const val OCR_RENDER_WIDTH = 1600
        private const val OCR_RENDER_MAX_HEIGHT = 2800

        /** Pages whose text has fewer letters/digits than this ratio are glyph codes without unicode mapping */
        private const val MIN_ALPHANUMERIC_RATIO = 0.6f

        /**
         * A page without text may be a scan, a page mostly made of symbols comes from fonts without
         * unicode mapping (e.g. `!"#$%&''$()`), both only have searchable text through OCR.
         */
        fun needsOcr(text: String): Boolean {
            var total = 0
            var alphanumeric = 0
            for (c in text) {
                if (c.isWhitespace()) {
                    continue
                }
                total++
                if (c.isLetterOrDigit()) {
                    alphanumeric++
                }
            }
            return total == 0 || alphanumeric < total * MIN_ALPHANUMERIC_RATIO
        }
    }
}
