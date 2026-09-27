package com.wxiwei.office.editor.pptx

/**
 * Replaces the text of formatted paragraphs and keeps the formatting of the text that stays.
 * The common start and end of the old and new text keep their runs; only the changed middle is
 * new, and it takes the format of the text it replaces, else of the text before it, else after it.
 * Typing Enter splits a paragraph (both halves keep its properties); deleting a paragraph mark
 * joins two. Used for both the slide XML ([PptxEditor]) and the live model ([LiveSlideModel]).
 */
internal object Retext {
    /** Text with formatting [tag]; a [lineBreak] run is one line break inside its paragraph. */
    class Run<T>(val text: String, val tag: T, val lineBreak: Boolean = false)
    /** [end]: formatting of the paragraph mark, used by new text that has no run to borrow from. */
    class Para<P, T>(val tag: P, val runs: List<Run<T>>, val end: T?)
    /** A paragraph of the new text: [source] is the old paragraph whose properties it keeps; a run
     *  with a null tag is new text with no run to borrow from (use [Para.end] of [source]). */
    class Out<P, T>(val source: Para<P, T>, val runs: List<Run<T?>>)

    /** One character: its old paragraph, its old run (index into all runs, -1 for none). */
    private class Cell(val ch: Char, val para: Int, val run: Int, val sep: Boolean = false, val brk: Boolean = false) {
        val isText get() = !sep && !brk
    }

    /** Paragraphs are separated by '\n'; '\r\n', '\r' and '\u000b' count as '\n' too. */
    fun normalize(text: String) = text.replace("\r\n", "\n").replace('\r', '\n').replace('\u000b', '\n')

    fun <P, T> apply(old: List<Para<P, T>>, text: String): List<Out<P, T>> {
        require(old.isNotEmpty()) { "No paragraph to keep formatting from" }
        val runs = ArrayList<Run<T>>()
        val cells = ArrayList<Cell>()
        old.forEachIndexed { i, para ->
            if (i > 0) cells.add(Cell('\n', i - 1, -1, sep = true))
            for (run in para.runs) {
                val index = runs.size
                runs.add(run)
                if (run.lineBreak) cells.add(Cell('\n', i, index, brk = true))
                else for (ch in normalize(run.text).replace('\n', ' ')) cells.add(Cell(ch, i, index))
            }
        }
        val target = normalize(text)
        val oldLength = cells.size
        var prefix = 0
        while (prefix < oldLength && prefix < target.length && cells[prefix].ch == target[prefix]) prefix++
        var suffix = 0
        while (suffix < oldLength - prefix && suffix < target.length - prefix &&
            cells[oldLength - 1 - suffix].ch == target[target.length - 1 - suffix]) suffix++

        // the replaced text, else the text before, else the text after gives the new text its format
        val anchor = cells.getOrNull(prefix)?.takeIf { prefix < oldLength - suffix && it.isText }
            ?: cells.getOrNull(prefix - 1)?.takeIf { it.isText }
            ?: cells.getOrNull(oldLength - suffix)?.takeIf { suffix > 0 && it.isText }
        val anchorPara = anchor?.para
            ?: cells.getOrNull(prefix - 1)?.let { if (it.sep) it.para + 1 else it.para }
            ?: cells.getOrNull(prefix)?.para ?: 0
        val next = ArrayList<Cell>(target.length)
        next.addAll(cells.subList(0, prefix))
        for (ch in target.substring(prefix, target.length - suffix)) {
            next.add(if (ch == '\n') Cell(ch, anchorPara, -1, sep = true) else Cell(ch, anchorPara, anchor?.run ?: -1))
        }
        next.addAll(cells.subList(oldLength - suffix, oldLength))

        val out = ArrayList<Out<P, T>>()
        var current = ArrayList<Cell>()
        var previousSep = -1
        fun close(sepPara: Int?) {
            val source = current.firstOrNull { it.run >= 0 }?.para ?: current.firstOrNull()?.para
                ?: sepPara ?: (previousSep + 1).coerceAtMost(old.lastIndex)
            val built = ArrayList<Run<T?>>()
            var i = 0
            while (i < current.size) {
                val cell = current[i]
                val tag = if (cell.run >= 0) runs[cell.run].tag else null
                if (cell.brk) { built.add(Run("\n", tag, true)); i++; continue }
                val sb = StringBuilder()
                while (i < current.size && current[i].isText && current[i].run == cell.run) sb.append(current[i++].ch)
                built.add(Run(sb.toString(), tag))
            }
            out.add(Out(old[source], built))
            current = ArrayList()
        }
        for (cell in next) {
            if (cell.sep) { close(cell.para); previousSep = cell.para } else current.add(cell)
        }
        close(null)
        return out
    }
}
