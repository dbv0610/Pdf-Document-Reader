/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.wxiwei.office.editor.pptx

import org.junit.Assert.assertEquals
import org.junit.Test

class RetextTest {
    private fun para(tag: String, vararg runs: Pair<String, String>, end: String = "$tag.end") =
        Retext.Para(tag, runs.map { (text, fmt) -> if (text == "\n") Retext.Run(text, fmt, true) else Retext.Run(text, fmt) }, end)

    /** "P[text/fmt|text/fmt]" per paragraph; a run without its own format shows the paragraph end format. */
    private fun show(out: List<Retext.Out<String, String>>) = out.joinToString(" ") { p ->
        p.source.tag + "[" + p.runs.joinToString("|") { (if (it.lineBreak) "BR" else it.text) + "/" + (it.tag ?: "~" + p.source.end) } + "]"
    }

    private val title = listOf(para("P0", "Lộ trình " to "plain", "KHÁM PHÁ" to "bold", " Việt" to "red"))

    @Test fun unchangedTextKeepsEveryRun() =
        assertEquals("P0[Lộ trình /plain|KHÁM PHÁ/bold| Việt/red]", show(Retext.apply(title, "Lộ trình KHÁM PHÁ Việt")))

    @Test fun replacedWordTakesItsFormat() =
        assertEquals("P0[Lộ trình /plain|XUYÊN/bold| Việt/red]", show(Retext.apply(title, "Lộ trình XUYÊN Việt")))

    @Test fun typedAtRunEndTakesTheRunBefore() =
        assertEquals("P0[Lộ trình /plain|KHÁM PHÁ!/bold| Việt/red]", show(Retext.apply(title, "Lộ trình KHÁM PHÁ! Việt")))

    @Test fun typedAtStartTakesTheFirstRun() =
        assertEquals("P0[>> Lộ trình /plain|KHÁM PHÁ/bold| Việt/red]", show(Retext.apply(title, ">> Lộ trình KHÁM PHÁ Việt")))

    @Test fun deletingARunDropsIt() =
        assertEquals("P0[Lộ trình /plain| Việt/red]", show(Retext.apply(title, "Lộ trình  Việt")))

    @Test fun enterSplitsAndKeepsParagraphProperties() =
        assertEquals("P0[Lộ trình /plain|KHÁM/bold] P0[ PHÁ/bold| Việt/red]", show(Retext.apply(title, "Lộ trình KHÁM\n PHÁ Việt")))

    @Test fun joiningParagraphsKeepsBothFormats() {
        val two = listOf(para("P0", "một" to "a"), para("P1", "hai" to "b"))
        assertEquals("P0[một/a|hai/b]", show(Retext.apply(two, "mộthai")))
        assertEquals("P0[một/a] P1[hai/b]", show(Retext.apply(two, "một\nhai")))
    }

    @Test fun emptyParagraphUsesItsEndFormat() {
        val paras = listOf(para("P0", "một" to "a"), para("P1"), para("P2", "ba" to "c"))
        assertEquals("P0[một/a] P1[] P2[ba/c]", show(Retext.apply(paras, "một\n\nba")))
        assertEquals("P0[một/a] P1[x/~P1.end] P2[ba/c]", show(Retext.apply(paras, "một\nx\nba")))
    }

    @Test fun lineBreaksStayLineBreaks() {
        val paras = listOf(para("P0", "a" to "f", "\n" to "br", "b" to "g"))
        assertEquals("P0[a/f|BR/br|bc/g]", show(Retext.apply(paras, "a\nbc")))
        assertEquals("P0[a/f|BR/br|b/g] P0[c/g]", show(Retext.apply(paras, "a\nb\nc")))
    }

    @Test fun clearingAndRetypingTakesTheOldFirstRun() =
        assertEquals("P0[mới/plain]", show(Retext.apply(title, "mới")))
}
