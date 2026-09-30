/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.reader.pdfviewer.convert

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** One line of text of a PDF page, in points from the top left of the page as it is shown. */
class TextLine(val runs: List<Run>, val left: Float, val top: Float, val right: Float, val bottom: Float) {
    constructor(text: String, left: Float, top: Float, right: Float, bottom: Float) : this(listOf(Run(text, null)), left, top, right, bottom)

    val text: String = runs.joinToString("") { it.text }
    val height: Float get() = bottom - top

    /** The size most of its characters have, 0 when unknown. */
    val fontSize: Float = runs.filter { (it.style?.size ?: 0f) > 0f }
        .groupBy { it.style!!.size }.maxByOrNull { (_, r) -> r.sumOf { it.text.length } }?.key ?: 0f
}

/** A picture of a PDF page, [key] naming its pixels for the writer; same coordinates as [TextLine]. */
class PictureBox(val key: Int, val left: Float, val top: Float, val right: Float, val bottom: Float)

/** An area of a page, same coordinates as [TextLine]. */
data class Box(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width get() = right - left
    val height get() = bottom - top
    fun contains(x: Float, y: Float) = x in left..right && y in top..bottom
    fun intersects(o: Box) = left < o.right && o.left < right && top < o.bottom && o.top < bottom
    fun union(o: Box) = Box(min(left, o.left), min(top, o.top), max(right, o.right), max(bottom, o.bottom))
    fun grow(d: Float) = Box(left - d, top - d, right + d, bottom + d)
    fun distance(o: Box): Float {
        val dx = max(0f, max(o.left - right, left - o.right))
        val dy = max(0f, max(o.top - bottom, top - o.bottom))
        return max(dx, dy)
    }
}

enum class Align { LEFT, CENTER, RIGHT }

/** A paragraph or a picture of a converted page, in reading order. Sizes are in points. */
sealed interface PageBlock {
    val spaceBefore: Float
    val align: Align
}

data class ParagraphBlock(
    val runs: List<Run>,
    /** Size of its text where a run does not know its own. */
    val fontSize: Float,
    override val align: Align,
    /** Left indent of the paragraph from the page's left margin. */
    val indent: Float,
    /** Extra indent of the first line (negative: hanging). */
    val firstLine: Float,
    override val spaceBefore: Float,
) : PageBlock {
    constructor(text: String, fontSize: Float, align: Align, indent: Float, firstLine: Float, spaceBefore: Float) :
        this(listOf(Run(text, null)), fontSize, align, indent, firstLine, spaceBefore)

    val text: String get() = runs.joinToString("") { it.text }
}

data class PictureBlock(
    val key: Int,
    val width: Float,
    val height: Float,
    override val align: Align,
    override val spaceBefore: Float,
) : PageBlock

/** A page rebuilt as flowing paragraphs: its size, the margins of its text and its blocks. */
data class LayoutPage(
    val width: Float,
    val height: Float,
    val marginLeft: Float,
    val marginRight: Float,
    val marginTop: Float,
    val blocks: List<PageBlock>,
)

/**
 * Turns the lines and pictures of one PDF page into paragraphs for a word processor: lines are put
 * in reading order (top to bottom, the left column before the right one on a two-column page),
 * pieces of one row are joined, lines are grouped into paragraphs (by the gap between them, a short
 * last line, a change of size or indent, a bullet or number), and each paragraph gets its size,
 * alignment and indents. Pictures go where they sit between the paragraphs.
 */
object PageLayout {

    /**
     * [heightToSize] is the height of a line box for a 1 pt font, used when a line does not know
     * its size: about 1.17 for pdfium's loose boxes (ascent to descent), 1 for OCR boxes.
     */
    fun build(
        pageWidth: Float,
        pageHeight: Float,
        lines: List<TextLine>,
        pictures: List<PictureBox>,
        heightToSize: Float = PDF_LINE_HEIGHT,
    ): LayoutPage {
        val rows = joinRows(readingOrder(lines.filter { it.text.isNotBlank() && it.height > 0f }, pageWidth), pageWidth)
        // a picture behind the text of the whole page is the scan the text was recognized from
        val shown = pictures.filter { p ->
            val area = (p.right - p.left) * (p.bottom - p.top)
            p.right > p.left && p.bottom > p.top && !(rows.isNotEmpty() && area > BACKGROUND_SHARE * pageWidth * pageHeight)
        }
        val textLeft = rows.minOfOrNull { it.left } ?: shown.minOfOrNull { it.left } ?: DEFAULT_MARGIN
        val textRight = rows.maxOfOrNull { it.right } ?: shown.maxOfOrNull { it.right } ?: (pageWidth - DEFAULT_MARGIN)
        val marginLeft = textLeft.coerceIn(MIN_MARGIN, MAX_MARGIN)
        val marginRight = (pageWidth - textRight).coerceIn(MIN_MARGIN, MAX_MARGIN)
        val top = min(rows.minOfOrNull { it.top } ?: Float.MAX_VALUE, shown.minOfOrNull { it.top } ?: Float.MAX_VALUE)
        val marginTop = if (top == Float.MAX_VALUE) DEFAULT_MARGIN else top.coerceIn(MIN_MARGIN, MAX_MARGIN)
        val area = Area(textLeft, max(textRight, textLeft + 1f), marginLeft, pageWidth - marginLeft - marginRight, pageWidth)

        val paragraphs = group(rows, area, heightToSize)
        val body = bodySize(rows, heightToSize)
        val built = paragraphs.mapIndexed { i, p -> p.block(area, 0f, body, listLine(paragraphs, i)) }
        val blocks = ArrayList<PageBlock>()
        var bottom = Float.NaN // bottom of the block before, NaN at the top of the page
        var next = 0
        val sortedPictures = shown.sortedBy { it.top }
        fun gap(top: Float, leading: Float) = if (bottom.isNaN()) 0f else (top - bottom - leading).coerceIn(0f, MAX_SPACE)
        for ((i, paragraph) in paragraphs.withIndex()) {
            while (next < sortedPictures.size && sortedPictures[next].top < paragraph.top) {
                val p = sortedPictures[next++]
                blocks += picture(p, area, gap(p.top, 0f))
                bottom = p.bottom
            }
            blocks += built[i].copy(spaceBefore = gap(paragraph.top, paragraph.leading))
            bottom = paragraph.bottom
        }
        while (next < sortedPictures.size) {
            val p = sortedPictures[next++]
            blocks += picture(p, area, gap(p.top, 0f))
            bottom = p.bottom
        }
        return LayoutPage(pageWidth, pageHeight, marginLeft, marginRight, marginTop, blocks)
    }

    /**
     * Areas that read better as one picture than as their parts: drawings with small labels (a
     * screenshot or a diagram drawn in the PDF: three or more lines under [TINY_TEXT] pt close
     * together, grown over the pictures they touch), and pictures made of several pictures that
     * touch or overlap (tiles, a picture with its frame or shadow), which a flowing document would
     * put one under the other.
     */
    fun figureAreas(pageWidth: Float, pageHeight: Float, lines: List<TextLine>, pictures: List<PictureBox>,
                    heightToSize: Float = PDF_LINE_HEIGHT): List<Box> {
        val tiny = lines.filter { it.text.isNotBlank() && it.height > 0f && sizeOf(it, heightToSize) < TINY_TEXT }
            .map { Box(it.left, it.top, it.right, it.bottom) }
        val clusters = ArrayList<Pair<Box, Int>>()
        for (box in tiny.sortedBy { it.top }) {
            val i = clusters.indexOfFirst { it.first.distance(box) < FIGURE_JOIN }
            if (i >= 0) clusters[i] = clusters[i].first.union(box) to clusters[i].second + 1 else clusters += box to 1
        }
        var areas = clusters.filter { it.second >= FIGURE_MIN_LINES }.map { it.first.grow(FIGURE_PADDING) }
        val page = Box(0f, 0f, pageWidth, pageHeight)
        val touching = pictures.map { Box(it.left, it.top, it.right, it.bottom) }
            .filter { it.width * it.height < 0.5f * pageWidth * pageHeight }
        val groups = ArrayList<Pair<Box, Int>>()
        for (p in touching) {
            val near = groups.filter { it.first.distance(p) < PICTURE_TOUCH }
            groups.removeAll(near.toSet())
            groups += near.fold(p to 1) { acc, g -> acc.first.union(g.first) to acc.second + g.second }
        }
        areas = areas + groups.filter { it.second >= 2 }.map { it.first }
        areas = areas.map { a -> touching.filter { it.intersects(a) }.fold(a) { acc, p -> acc.union(p) } }
        // labels a little bigger than the tiny ones, next to the figure, are part of it
        val small = lines.filter { it.text.isNotBlank() && it.height > 0f && sizeOf(it, heightToSize) < SMALL_LABEL }
            .map { Box(it.left, it.top, it.right, it.bottom) }
        repeat(3) {
            areas = areas.map { a -> small.filter { it.distance(a) < FIGURE_JOIN }.fold(a) { acc, b -> acc.union(b.grow(FIGURE_PADDING)) } }
        }
        // areas that now overlap are one figure
        var merged = true
        while (merged) {
            merged = false
            val out = ArrayList<Box>()
            for (a in areas) {
                val i = out.indexOfFirst { it.intersects(a) }
                if (i >= 0) { out[i] = out[i].union(a); merged = true } else out += a
            }
            areas = out
        }
        return areas.map { Box(max(page.left, it.left), max(page.top, it.top), min(page.right, it.right), min(page.bottom, it.bottom)) }
    }

    /** Text lines run from [left] to [right]; the page's text column starts at [margin] and is [width] wide. */
    private class Area(val left: Float, val right: Float, val margin: Float, val width: Float, val pageWidth: Float) {
        val span: Float get() = right - left

        /** Centered in the text lines or on the page (when no line reaches the right margin). */
        fun centered(left: Float, right: Float, slack: Float): Boolean =
            left - this.left > CENTER_MIN_GAP * span &&
                (abs((left - this.left) - (this.right - right)) < slack || abs(left - (pageWidth - right)) < slack)
    }

    /**
     * Top to bottom, left to right within a row. When a clear gutter splits most lines in two, the
     * page has two columns: between lines that cross the gutter (titles, full-width pictures'
     * captions), the left column is read before the right one.
     */
    internal fun readingOrder(lines: List<TextLine>, pageWidth: Float): List<TextLine> {
        val sorted = bands(lines)
        val gutter = gutter(sorted, pageWidth) ?: return sorted
        val result = ArrayList<TextLine>()
        val left = ArrayList<TextLine>()
        val right = ArrayList<TextLine>()
        fun flush() { result += left; result += right; left.clear(); right.clear() }
        for (line in sorted) {
            when {
                line.right <= gutter -> left += line
                line.left >= gutter -> right += line
                else -> { flush(); result += line }
            }
        }
        flush()
        return result
    }

    /** Lines sorted by their top; lines sharing a row are sorted left to right. */
    private fun bands(lines: List<TextLine>): List<TextLine> {
        val byTop = lines.sortedBy { it.top }
        val result = ArrayList<TextLine>()
        var band = ArrayList<TextLine>()
        for (line in byTop) {
            val first = band.firstOrNull()
            if (first != null && min(first.bottom, line.bottom) - max(first.top, line.top) <= ROW_OVERLAP * min(first.height, line.height)) {
                result += band.sortedBy { it.left }
                band = ArrayList()
            }
            band += line
        }
        result += band.sortedBy { it.left }
        return result
    }

    /** The x of a gutter between two columns, or null for a one-column page. */
    private fun gutter(lines: List<TextLine>, pageWidth: Float): Float? {
        if (lines.size < COLUMN_MIN_LINES) return null
        var best: Float? = null
        var bestCrossing = Int.MAX_VALUE
        var x = pageWidth * 0.3f
        while (x <= pageWidth * 0.7f) {
            val crossing = lines.count { it.left < x && it.right > x }
            if (crossing < bestCrossing) { bestCrossing = crossing; best = x }
            x += 2f
        }
        val g = best ?: return null
        val leftCount = lines.count { it.right <= g }
        val rightCount = lines.count { it.left >= g }
        val enough = COLUMN_SHARE * lines.size
        return if (bestCrossing <= COLUMN_CROSSING * lines.size && leftCount >= enough && rightCount >= enough) g else null
    }

    /** Pieces that sit on one row, left to right and close together, are one line. */
    private fun joinRows(lines: List<TextLine>, pageWidth: Float): List<TextLine> {
        val result = ArrayList<TextLine>()
        for (line in lines) {
            val last = result.lastOrNull()
            val overlap = if (last == null) 0f else min(last.bottom, line.bottom) - max(last.top, line.top)
            if (last != null && overlap > ROW_OVERLAP * min(last.height, line.height) &&
                line.left >= last.right - 1f && line.left - last.right < ROW_GAP * pageWidth) {
                result[result.lastIndex] = TextLine(joinRuns(last.runs, line.runs, hyphen = false),
                    last.left, min(last.top, line.top), line.right, max(last.bottom, line.bottom))
            } else {
                result += line
            }
        }
        return result
    }

    /**
     * [a] then [b] with a space between, or, with [hyphen], without the hyphen of a word [a] cut at
     * its end when [b] goes on in lower case. Neighbouring runs of one style become one.
     */
    private fun joinRuns(a: List<Run>, b: List<Run>, hyphen: Boolean): List<Run> {
        val out = a.toMutableList()
        val lastText = out.lastOrNull()?.text.orEmpty()
        val next = b.firstOrNull()?.text.orEmpty()
        val cut = hyphen && lastText.length >= 2 && lastText.last() == '-' && lastText[lastText.length - 2].isLetter() &&
            next.firstOrNull()?.isLowerCase() == true
        if (out.isNotEmpty()) {
            val last = out.last()
            out[out.lastIndex] = last.copy(text = if (cut) last.text.dropLast(1) else last.text + " ")
        }
        for (run in b) {
            val last = out.lastOrNull()
            if (last != null && last.style == run.style) out[out.lastIndex] = last.copy(text = last.text + run.text) else out += run
        }
        return out
    }

    private class Paragraph(val lines: MutableList<TextLine>, val heightToSize: Float) {
        val top get() = lines.first().top
        val bottom get() = lines.last().bottom
        /** Space pdfium's line boxes leave between two lines of this paragraph. */
        val leading get() = lines.first().height * LEADING

        /** The paragraph as a block; a size guessed from box heights close to [body] is [body]. */
        fun block(area: Area, spaceBefore: Float, body: Float, listLine: Boolean = false): ParagraphBlock {
            var size = lines.map { sizeOf(it, heightToSize) }.sorted()[lines.size / 2]
            if (lines.all { it.fontSize <= 0f } && body > 0f && abs(size - body) < BODY_SNAP * body) size = body
            val listed = listLine || BULLET.containsMatchIn(lines[0].text.trimStart())
            val align = if (listed) Align.LEFT else alignOf(area)
            var indent = 0f
            var firstLine = 0f
            if (align == Align.LEFT) {
                val body = if (lines.size > 1) lines[1].left else lines[0].left
                indent = snap(max(0f, body - area.margin))
                firstLine = snap(lines[0].left - body)
            }
            val runs = lines.drop(1).fold(lines[0].runs) { acc, line -> joinRuns(acc, line.runs, hyphen = true) }
            return ParagraphBlock(runs, size, align, indent, firstLine, spaceBefore)
        }

        private fun alignOf(area: Area): Align {
            val slack = max(ALIGN_SLACK * area.span, 6f)
            if (lines.all { area.centered(it.left, it.right, slack) }) return Align.CENTER
            val right = lines.all { area.right - it.right < slack && it.left - area.left > RIGHT_MIN_GAP * area.span }
            return if (right) Align.RIGHT else Align.LEFT
        }
    }

    /** The size most of the page's text has. */
    private fun bodySize(rows: List<TextLine>, heightToSize: Float): Float =
        rows.groupBy { sizeOf(it, heightToSize) }.maxByOrNull { (_, l) -> l.sumOf { it.text.length } }?.key ?: 0f

    /**
     * Paragraph [i] is one line of a left-aligned list: a one-line paragraph next to another that
     * starts at the same x but ends elsewhere. Its ends may happen to look centered; it is not.
     */
    private fun listLine(paragraphs: List<Paragraph>, i: Int): Boolean {
        val line = paragraphs[i].lines.singleOrNull() ?: return false
        return listOfNotNull(paragraphs.getOrNull(i - 1), paragraphs.getOrNull(i + 1)).any { p ->
            val other = p.lines.singleOrNull()
            other != null && abs(other.left - line.left) < 4f && abs(other.right - line.right) > 6f
        }
    }

    private fun group(rows: List<TextLine>, area: Area, heightToSize: Float): List<Paragraph> {
        val paragraphs = ArrayList<Paragraph>()
        for (line in rows) {
            val current = paragraphs.lastOrNull()
            if (current == null || startsNew(current, line, area, heightToSize)) {
                paragraphs += Paragraph(mutableListOf(line), heightToSize)
            } else {
                current.lines += line
            }
        }
        return paragraphs
    }

    private fun startsNew(paragraph: Paragraph, line: TextLine, area: Area, heightToSize: Float): Boolean {
        val prev = paragraph.lines.last()
        val size = sizeOf(line, heightToSize)
        if (abs(size - sizeOf(prev, heightToSize)) > SIZE_CHANGE) return true
        if (line.top < prev.top - 1f) return true // back up the page: another column or block
        if (line.top - prev.bottom > PARAGRAPH_GAP * min(prev.height, line.height)) return true
        if (area.right - prev.right > SHORT_LINE * area.span) return true
        if (line.right < prev.left || line.left > prev.right) return true // side by side: another column
        if (BULLET.containsMatchIn(line.text.trimStart())) return true
        val charWidth = size * 0.5f
        val bodyLeft = if (paragraph.lines.size > 1) paragraph.lines[1].left else null
        return bodyLeft != null && abs(line.left - bodyLeft) > 2 * charWidth
    }

    private fun picture(p: PictureBox, area: Area, spaceBefore: Float): PictureBlock {
        var width = p.right - p.left
        var height = p.bottom - p.top
        if (width > area.width) {
            height *= area.width / width
            width = area.width
        }
        val align = if (area.centered(p.left, p.right, max(ALIGN_SLACK * area.span, 12f))) Align.CENTER else Align.LEFT
        return PictureBlock(p.key, width, height, align, spaceBefore)
    }

    /** The size of [line]'s text: what the PDF says, or else from the height of its box. */
    private fun sizeOf(line: TextLine, heightToSize: Float): Float {
        val size = if (line.fontSize > 0f) line.fontSize else line.height / heightToSize
        return (size * 2).roundToInt().coerceIn(MIN_SIZE * 2, MAX_SIZE * 2) / 2f
    }

    /** Offsets under 2 pt are noise of the boxes. */
    private fun snap(value: Float) = if (abs(value) < 2f) 0f else value

    const val PDF_LINE_HEIGHT = 1.17f
    const val OCR_LINE_HEIGHT = 1f
    private const val BACKGROUND_SHARE = 0.8f
    private const val ROW_OVERLAP = 0.5f
    private const val ROW_GAP = 0.25f
    private const val LEADING = 0.2f
    private const val PARAGRAPH_GAP = 0.6f
    private const val SHORT_LINE = 0.2f
    private const val SIZE_CHANGE = 1f
    private const val ALIGN_SLACK = 0.04f
    private const val CENTER_MIN_GAP = 0.08f
    private const val RIGHT_MIN_GAP = 0.25f
    private const val MIN_SIZE = 5
    private const val MAX_SIZE = 96
    private const val MAX_SPACE = 48f
    private const val MIN_MARGIN = 18f
    private const val MAX_MARGIN = 144f
    private const val DEFAULT_MARGIN = 72f
    private const val COLUMN_MIN_LINES = 8
    private const val COLUMN_SHARE = 0.25f
    private const val COLUMN_CROSSING = 0.1f
    private const val TINY_TEXT = 5.5f
    private const val BODY_SNAP = 0.2f
    private const val SMALL_LABEL = 9f
    private const val PICTURE_TOUCH = 2f
    private const val FIGURE_JOIN = 24f
    private const val FIGURE_MIN_LINES = 3
    private const val FIGURE_PADDING = 8f
    private val BULLET = Regex("""^([•●○◦▪■□◆◇–—·*+\-]\s|(\d{1,3}|[a-zA-Z]|[ivxIVX]{1,5})[.)]\s)""")
}
