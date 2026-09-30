/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.reader.pdfviewer.convert

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** How a piece of text is drawn: size in points (0 unknown), [color] as RGB (-1 unknown), [font] a family name. */
data class TextStyle(val size: Float, val bold: Boolean, val italic: Boolean, val color: Int, val font: String?) {
    companion object {
        /** Sizes differing by less than a quarter point are the same size. */
        fun roundSize(size: Float) = (size * 2).roundToInt() / 2f
    }
}

/** A piece of text in one style; [style] null when nothing is known about it. */
data class Run(val text: String, val style: TextStyle?)

/**
 * Builds the lines of a page from its characters: a line ends where pdfium put a line break, where
 * the next character is on another row, or where it jumps far to the right (another cell or
 * column). Pieces of text keep their style as runs.
 */
object TextRows {

    /**
     * [codePoints] of the characters, [boxes] 4 floats each (left, top, right, bottom; all 0 when a
     * character has no box), [styles] per character or null.
     */
    fun build(codePoints: IntArray, boxes: FloatArray, styles: Array<TextStyle?>?): List<TextLine> {
        val lines = ArrayList<TextLine>()
        val row = RowBuilder()
        var pendingSpace = false
        for (i in codePoints.indices) {
            val cp = codePoints[i]
            if (cp <= 0 || !Character.isValidCodePoint(cp)) continue
            if (cp == '\n'.code || cp == '\r'.code) {
                row.flush(lines)
                pendingSpace = false
                continue
            }
            val o = i * 4
            val hasBox = boxes[o + 2] > boxes[o] && boxes[o + 3] > boxes[o + 1]
            val style = styles?.getOrNull(i)
            if (Character.isWhitespace(cp) || Character.isSpaceChar(cp)) {
                pendingSpace = true
                continue
            }
            if (!hasBox) {
                // a character pdfium could not place belongs to the line it is read in
                if (!row.isEmpty) row.append(cp, style)
                continue
            }
            val left = boxes[o]; val top = boxes[o + 1]; val right = boxes[o + 2]; val bottom = boxes[o + 3]
            if (!row.isEmpty && !row.continuesWith(left, top, right, bottom)) {
                row.flush(lines)
                pendingSpace = false
            }
            if (!row.isEmpty && (pendingSpace || left - row.lastRight > WORD_GAP * row.lastHeight)) row.append(' '.code, row.lastStyle)
            row.append(cp, style)
            row.place(left, top, right, bottom)
            pendingSpace = false
        }
        row.flush(lines)
        return lines
    }

    private class RowBuilder {
        private val runs = ArrayList<Pair<StringBuilder, TextStyle?>>()
        private var left = 0f; private var top = 0f; private var right = 0f; private var bottom = 0f
        var lastRight = 0f; private set
        var lastHeight = 0f; private set
        private var lastLeft = 0f; private var lastTop = 0f; private var lastBottom = 0f
        private var placed = false
        var lastStyle: TextStyle? = null; private set

        val isEmpty get() = !placed

        fun append(cp: Int, style: TextStyle?) {
            val last = runs.lastOrNull()
            if (last != null && last.second == style) last.first.appendCodePoint(cp)
            else runs += StringBuilder().appendCodePoint(cp) to style
            lastStyle = style
        }

        fun place(l: Float, t: Float, r: Float, b: Float) {
            if (!placed) { left = l; top = t; right = r; bottom = b; placed = true }
            else { left = min(left, l); top = min(top, t); right = max(right, r); bottom = max(bottom, b) }
            lastLeft = l; lastTop = t; lastRight = r; lastBottom = b; lastHeight = b - t
        }

        /** Same row as the character before, and not a jump back or far ahead. */
        fun continuesWith(l: Float, t: Float, r: Float, b: Float): Boolean {
            val overlap = min(lastBottom, b) - max(lastTop, t)
            if (overlap <= ROW_OVERLAP * min(lastHeight, b - t)) return false
            if (l < lastLeft - lastHeight) return false
            return l - lastRight <= CELL_GAP * max(lastHeight, b - t)
        }

        fun flush(into: MutableList<TextLine>) {
            if (placed) {
                val pieces = runs.map { Run(it.first.toString(), it.second) }.let(::trim)
                if (pieces.isNotEmpty()) into += TextLine(pieces, left, top, right, bottom)
            }
            runs.clear(); placed = false; lastStyle = null
        }

        private fun trim(pieces: List<Run>): List<Run> {
            val result = pieces.toMutableList()
            if (result.isNotEmpty()) result[0] = result[0].copy(text = result[0].text.trimStart())
            if (result.isNotEmpty()) result[result.lastIndex] = result.last().copy(text = result.last().text.trimEnd())
            return result.filter { it.text.isNotEmpty() }
        }
    }

    private const val ROW_OVERLAP = 0.5f
    /** A gap wider than this share of the line height is a space even without a space character. */
    private const val WORD_GAP = 0.3f
    /** A gap wider than this many line heights starts another line (a table cell, a column). */
    private const val CELL_GAP = 3f
}

/** Font family from a PDF font name: "ABCDEF+TimesNewRomanPS-BoldMT" → "Times New Roman". */
object FontNames {
    private val known = mapOf(
        "arial" to "Arial", "arialmt" to "Arial", "helvetica" to "Arial", "timesnewroman" to "Times New Roman",
        "times" to "Times New Roman", "timesroman" to "Times New Roman", "couriernew" to "Courier New", "courier" to "Courier New",
        "calibri" to "Calibri", "cambria" to "Cambria", "verdana" to "Verdana", "tahoma" to "Tahoma", "georgia" to "Georgia",
        "segoeui" to "Segoe UI", "roboto" to "Roboto", "opensans" to "Open Sans", "notosans" to "Noto Sans", "notoserif" to "Noto Serif",
    )

    fun family(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        var name = raw.substringAfter('+')
        name = name.substringBefore('-').substringBefore(',')
        name = name.removeSuffix("PSMT").removeSuffix("MT").removeSuffix("PS")
        if (name.length < 3 || name.startsWith("CIDFont") || Regex("^[TF]\\d+$").matches(name)) return null
        return known[name.lowercase()] ?: name.takeIf { n -> n.all { it.isLetterOrDigit() || it == ' ' } }
    }

    fun isBold(raw: String?) = raw != null && Regex("(?i)bold|black|heavy|semibold|demi").containsMatchIn(raw.substringAfter('+'))
    fun isItalic(raw: String?) = raw != null && Regex("(?i)italic|oblique").containsMatchIn(raw.substringAfter('+'))
}
