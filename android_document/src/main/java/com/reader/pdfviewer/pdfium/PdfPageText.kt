package com.reader.pdfviewer.pdfium

import android.graphics.RectF
import com.reader.pdfviewer.search.OcrLine

/**
 * Text of one PDF page with the position of every character.
 *
 * Coordinates are PDF points with the origin at the top left of the page as it is rendered
 * (crop box and page rotation applied), on a [pageWidth] x [pageHeight] page. Use [toBitmapRect]
 * to place a box on a rendered bitmap of any size.
 *
 * Pages without a usable text layer (scans) come from OCR when [isOcr] is true: words and lines
 * are exact there, but a character box is estimated from its share of its word.
 */
class PdfPageText internal constructor(
    val pageIndex: Int,
    val pageWidth: Float,
    val pageHeight: Float,
    private val codePoints: IntArray,
    /** left, top, right, bottom per character; all 0 when the character has no box. */
    private val boxes: FloatArray,
    val isOcr: Boolean = false,
) {
    val charCount: Int get() = codePoints.size

    /** Whole page text; pdfium adds "\r\n" between lines and spaces between words. */
    val text: String by lazy {
        buildString(codePoints.size) { codePoints.forEach { if (isText(it)) appendCodePoint(it) } }
    }

    /** Words, split on whitespace, in reading order. */
    val words: List<PdfTextBlock> by lazy { group { Character.isWhitespace(it) } }

    /** Lines, split on the line breaks pdfium inserts. */
    val lines: List<PdfTextBlock> by lazy { group { it == '\n'.code || it == '\r'.code } }

    fun charCodePoint(index: Int): Int = codePoints[index]

    /** Box of the character at [index], or null when it has none (e.g. generated spaces). */
    fun charBox(index: Int): RectF? {
        val offset = index * 4
        if (boxes[offset + 2] <= boxes[offset] || boxes[offset + 3] <= boxes[offset + 1]) return null
        return RectF(boxes[offset], boxes[offset + 1], boxes[offset + 2], boxes[offset + 3])
    }

    /** Maps a box of this page onto a rendered bitmap of [bitmapWidth] x [bitmapHeight] pixels. */
    fun toBitmapRect(rect: RectF, bitmapWidth: Int, bitmapHeight: Int): RectF {
        val sx = bitmapWidth / pageWidth
        val sy = bitmapHeight / pageHeight
        return RectF(rect.left * sx, rect.top * sy, rect.right * sx, rect.bottom * sy)
    }

    private inline fun group(isSeparator: (Int) -> Boolean): List<PdfTextBlock> {
        val result = mutableListOf<PdfTextBlock>()
        var start = -1
        for (i in 0..codePoints.size) {
            val separator = i == codePoints.size || !isText(codePoints[i]) || isSeparator(codePoints[i])
            if (!separator && start < 0) start = i
            if (separator && start >= 0) {
                block(start, i)?.let(result::add)
                start = -1
            }
        }
        return result
    }

    private fun block(start: Int, end: Int): PdfTextBlock? {
        val bounds = RectF()
        val text = StringBuilder(end - start)
        for (i in start until end) {
            text.appendCodePoint(codePoints[i])
            charBox(i)?.let { if (bounds.isEmpty) bounds.set(it) else bounds.union(it) }
        }
        val value = text.toString().trim()
        if (value.isEmpty()) return null
        return PdfTextBlock(value, bounds, start, end)
    }

    internal companion object {
        /**
         * Page text from OCR [lines], whose word boxes are relative to the page (0..1).
         * Lines are joined by "\n" and words by the spaces in [OcrLine.text], both without a box.
         */
        fun fromOcr(pageIndex: Int, pageWidth: Float, pageHeight: Float, lines: List<OcrLine>): PdfPageText {
            val codePoints = ArrayList<Int>()
            val boxes = ArrayList<Float>()
            fun add(codePoint: Int, left: Float = 0f, top: Float = 0f, right: Float = 0f, bottom: Float = 0f) {
                codePoints += codePoint
                boxes += left; boxes += top; boxes += right; boxes += bottom
            }
            lines.forEachIndexed { lineIndex, line ->
                if (lineIndex > 0) add('\n'.code)
                var offset = 0
                while (offset < line.text.length) {
                    val codePoint = line.text.codePointAt(offset)
                    val next = offset + Character.charCount(codePoint)
                    val word = line.words.firstOrNull { offset >= it.start && offset < it.end }
                    if (word == null) {
                        add(codePoint)
                    } else {
                        val box = word.box
                        val length = (word.end - word.start).toFloat()
                        val left = box.left + box.width() * (offset - word.start) / length
                        val right = box.left + box.width() * (next - word.start) / length
                        add(codePoint, left * pageWidth, box.top * pageHeight, right * pageWidth, box.bottom * pageHeight)
                    }
                    offset = next
                }
            }
            return PdfPageText(pageIndex, pageWidth, pageHeight, codePoints.toIntArray(), boxes.toFloatArray(), isOcr = true)
        }
    }

    // Broken fonts can map glyphs to 0 or to values that are not code points at all.
    private fun isText(codePoint: Int) = codePoint > 0 && Character.isValidCodePoint(codePoint)
}

/**
 * A word or line of [PdfPageText]; [bounds] is the union of its character boxes (empty when
 * none of them has a box). Characters are [startChar] until [endChar].
 */
data class PdfTextBlock(val text: String, val bounds: RectF, val startChar: Int, val endChar: Int)
