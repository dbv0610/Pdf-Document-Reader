/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.reader.pdfviewer.convert

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PageLayoutTest {

    /** A line of 12 pt text (pdfium box: 14 pt high) starting at [top]. */
    private fun line(text: String, left: Float, top: Float, right: Float = 520f, height: Float = 14f) =
        TextLine(text, left, top, right, top + height)

    private fun paragraphs(page: LayoutPage) = page.blocks.filterIsInstance<ParagraphBlock>()

    @Test
    fun linesOfOneParagraphAreJoined() {
        val page = PageLayout.build(595f, 842f, listOf(
            line("The first line of a paragraph that runs", 72f, 100f),
            line("to the right edge and goes on in the", 72f, 117f),
            line("third line.", 72f, 134f, right = 150f),
        ), emptyList())
        val p = paragraphs(page).single()
        assertEquals("The first line of a paragraph that runs to the right edge and goes on in the third line.", p.text)
        assertEquals(12f, p.fontSize)
        assertEquals(Align.LEFT, p.align)
        assertEquals(72f, page.marginLeft)
    }

    @Test
    fun shortLineGapSizeAndBulletStartNewParagraphs() {
        val page = PageLayout.build(595f, 842f, listOf(
            line("Title", 72f, 60f, right = 200f, height = 28f),     // bigger
            line("Body line one that is long enough", 72f, 110f),
            line("ends short.", 72f, 127f, right = 160f),             // short last line
            line("Next paragraph after a short line", 72f, 144f),
            line("• a bullet item", 72f, 161f),                        // bullet
            line("After a gap", 72f, 220f),                            // gap
        ), emptyList())
        assertEquals(listOf("Title", "Body line one that is long enough ends short.", "Next paragraph after a short line",
            "• a bullet item", "After a gap"), paragraphs(page).map { it.text })
        assertEquals(24f, paragraphs(page)[0].fontSize)
        assertTrue(paragraphs(page).last().spaceBefore > 30f)
    }

    @Test
    fun hyphenAtLineEndJoinsTheWord() {
        val page = PageLayout.build(595f, 842f, listOf(
            line("a word cut at the end of the con-", 72f, 100f),
            line("tent goes on", 72f, 117f, right = 200f),
        ), emptyList())
        assertEquals("a word cut at the end of the content goes on", paragraphs(page).single().text)
    }

    @Test
    fun centeredAndRightAligned() {
        val page = PageLayout.build(595f, 842f, listOf(
            line("Left text spanning the whole width here", 72f, 100f, right = 520f),
            line("Centered", 250f, 160f, right = 342f),
            line("Right", 460f, 220f, right = 520f),
        ), emptyList())
        assertEquals(listOf(Align.LEFT, Align.CENTER, Align.RIGHT), paragraphs(page).map { it.align })
    }

    @Test
    fun firstLineIndent() {
        val page = PageLayout.build(595f, 842f, listOf(
            line("Indented first line of the paragraph", 108f, 100f),
            line("second line back at the margin and", 72f, 117f),
            line("third.", 72f, 134f, right = 120f),
        ), emptyList())
        val p = paragraphs(page).single()
        assertEquals(0f, p.indent)
        assertEquals(36f, p.firstLine)
    }

    @Test
    fun piecesOfOneRowAreOneLine() {
        val page = PageLayout.build(595f, 842f, listOf(
            line("Name:", 72f, 100f, right = 110f),
            line("Nguyễn Văn A", 120f, 100f, right = 200f),
        ), emptyList())
        assertEquals("Name: Nguyễn Văn A", paragraphs(page).single().text)
    }

    @Test
    fun picturesGoBetweenParagraphsAndScansAreDropped() {
        val page = PageLayout.build(595f, 842f, listOf(
            line("Before the picture", 72f, 100f, right = 200f),
            line("After the picture", 72f, 400f, right = 200f),
        ), listOf(
            PictureBox(1, 150f, 150f, 450f, 350f),
            PictureBox(2, 0f, 0f, 595f, 842f), // the scan under the text
        ))
        val kinds = page.blocks.map { if (it is PictureBlock) "picture ${it.key}" else (it as ParagraphBlock).text }
        assertEquals(listOf("Before the picture", "picture 1", "After the picture"), kinds)
        val picture = page.blocks[1] as PictureBlock
        assertEquals(300f, picture.width)
        assertEquals(200f, picture.height)
    }

    @Test
    fun pageWithOnlyAPictureKeepsIt() {
        val page = PageLayout.build(595f, 842f, emptyList(), listOf(PictureBox(0, 0f, 0f, 595f, 842f)))
        val picture = page.blocks.single() as PictureBlock
        // made as wide as the text column, keeping its shape
        assertTrue(picture.width <= 595f - page.marginLeft - page.marginRight + 0.01f)
        assertEquals(picture.width * 842f / 595f, picture.height, 0.5f)
    }

    @Test
    fun figureAreasJoinTinyLabelsAndTouchingPictures() {
        val labels = (0 until 4).map { line("label $it", 100f, 300f + it * 6f, right = 140f, height = 4f) }
        val areas = PageLayout.figureAreas(595f, 842f, labels + line("Body text", 72f, 100f), listOf(
            PictureBox(0, 100f, 500f, 200f, 600f), PictureBox(1, 200f, 520f, 300f, 580f), // two tiles side by side
            PictureBox(2, 100f, 700f, 200f, 780f),                                         // alone
        ))
        assertEquals(2, areas.size)
        assertTrue(areas.any { it.contains(120f, 305f) && !it.contains(80f, 105f) })     // the labels, not the body
        assertTrue(areas.any { it.left <= 100f && it.right >= 300f && it.top <= 500f })   // the tiles as one
        assertTrue(areas.none { it.contains(150f, 740f) })                                // a lone picture stays a picture
    }
}
