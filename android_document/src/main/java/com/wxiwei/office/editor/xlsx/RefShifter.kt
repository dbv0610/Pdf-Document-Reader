/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.wxiwei.office.editor.xlsx

/**
 * Rewrites A1 references after rows or columns are inserted into or deleted from one sheet, the
 * way Excel does: every reference to that sheet at or after the change moves ($ included), a
 * range grows or shrinks, and a reference into deleted cells becomes #REF!.
 *
 * Handles A1 cells, A1:B2 ranges, whole rows (2:5) and whole columns (A:C), with an optional
 * sheet prefix (Sheet1!, 'My sheet'!); text in quotes is left alone.
 */
object RefShifter {
    /** A structural change: [count] > 0 inserts before [at], < 0 deletes -count starting at [at]. */
    data class Change(val sheet: String, val rows: Boolean, val at: Int, val count: Int)

    private const val MAX_ROW = 1048575
    private const val MAX_COL = 16383

    // "text" | sheet!ref(:ref)? | ref(:ref)?   where ref is $A$1, A1, $A, A (columns only in
    // ranges) or $1, 1 (rows only in ranges)
    private val token = Regex(
        "\"(?:\"\"|[^\"])*\"" +
            "|(?<![A-Za-z0-9_.\$'!])((?:'(?:''|[^'])+'|[A-Za-z_][A-Za-z0-9_.]*)!)?" +
            "(\\$?[A-Za-z]{1,3}\\$?[0-9]+|\\$?[A-Za-z]{1,3}|\\$?[0-9]+)" +
            "(?::(\\$?[A-Za-z]{1,3}\\$?[0-9]+|\\$?[A-Za-z]{1,3}|\\$?[0-9]+))?" +
            "(?![A-Za-z0-9_(!])"
    )

    private data class Ref(val col: Int?, val colAbs: Boolean, val row: Int?, val rowAbs: Boolean)

    private fun parse(s: String): Ref? {
        val m = Regex("(\\$?)([A-Za-z]{0,3})(\\$?)([0-9]*)").matchEntire(s) ?: return null
        val letters = m.groupValues[2]
        val digits = m.groupValues[4]
        if (letters.isEmpty() && digits.isEmpty()) return null
        val col = if (letters.isEmpty()) null else letters.uppercase().fold(0) { n, c -> n * 26 + (c - 'A' + 1) } - 1
        val row = if (digits.isEmpty()) null else digits.toInt() - 1
        if (col != null && col > MAX_COL) return null
        if (row != null && (row < 0 || row > MAX_ROW)) return null
        // "A" alone or "1" alone only make sense inside a range
        return Ref(col, m.groupValues[1] == "$" && letters.isNotEmpty(), row,
            (if (letters.isEmpty()) m.groupValues[1] else m.groupValues[3]) == "$")
    }

    private fun format(r: Ref): String =
        (if (r.col != null) (if (r.colAbs) "$" else "") + A1FormulaShifter.column(r.col) else "") +
            (if (r.row != null) (if (r.rowAbs) "$" else "") + (r.row + 1) else "")

    private fun unquote(prefix: String): String {
        val name = prefix.removeSuffix("!")
        return if (name.startsWith("'")) name.substring(1, name.length - 1).replace("''", "'") else name
    }

    /** Position after the change; null when it was deleted. */
    private fun move(v: Int, c: Change): Int? = when {
        c.count > 0 -> if (v >= c.at) v + c.count else v
        v < c.at -> v
        v >= c.at - c.count -> v + c.count
        else -> null
    }

    /**
     * [formula] (without "=") of a cell on [formulaSheet], after [change]. References without a
     * sheet prefix belong to [formulaSheet].
     */
    fun shift(formula: String, formulaSheet: String, change: Change): String = token.replace(formula) { m ->
        if (m.value.startsWith('"')) return@replace m.value
        val prefix = m.groupValues[1]
        val sheet = if (prefix.isEmpty()) formulaSheet else unquote(prefix)
        if (!sheet.equals(change.sheet, ignoreCase = true)) return@replace m.value
        val a = parse(m.groupValues[2]) ?: return@replace m.value
        val second = m.groupValues[3]
        if (second.isEmpty()) {
            // a single cell (a lone column or row is a name, not a reference)
            if (a.col == null || a.row == null) return@replace m.value
            val moved = if (change.rows) move(a.row, change)?.let { a.copy(row = it) } else move(a.col, change)?.let { a.copy(col = it) }
            return@replace prefix + (moved?.let { format(it) } ?: "#REF!")
        }
        val b = parse(second) ?: return@replace m.value
        // whole columns are not touched by row changes and whole rows not by column changes
        val first = if (change.rows) a.row else a.col
        val last = if (change.rows) b.row else b.col
        if (first == null || last == null) return@replace m.value
        val lo = minOf(first, last)
        val hi = maxOf(first, last)
        val newLo: Int
        val newHi: Int
        if (change.count > 0) {
            newLo = if (lo >= change.at) lo + change.count else lo
            newHi = if (hi >= change.at) hi + change.count else hi
        } else {
            val end = change.at - change.count // first row/col after the deleted band
            newLo = when { lo < change.at -> lo; lo >= end -> lo + change.count; else -> change.at }
            newHi = when { hi < change.at -> hi; hi >= end -> hi + change.count; else -> change.at - 1 }
            if (newHi < newLo) return@replace prefix + "#REF!"
        }
        val (na, nb) = if (change.rows) a.copy(row = newLo) to b.copy(row = newHi) else a.copy(col = newLo) to b.copy(col = newHi)
        prefix + format(na) + ":" + format(nb)
    }

    /** A range like "B5:I116" or "A1" (sqref, table refs...) on the changed sheet, or null if deleted. */
    fun shiftRange(range: String, change: Change): String? {
        val shifted = shift(range, change.sheet, change)
        return if (shifted.contains("#REF!")) null else shifted
    }
}
