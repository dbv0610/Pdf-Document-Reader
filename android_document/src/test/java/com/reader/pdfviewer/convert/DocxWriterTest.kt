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
import java.io.File
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory

class DocxWriterTest {

    private fun parts(file: File): Map<String, ByteArray> = ZipFile(file).use { zip ->
        zip.entries().toList().associate { it.name to zip.getInputStream(it).readBytes() }
    }

    private fun parse(bytes: ByteArray) = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
        .newDocumentBuilder().parse(bytes.inputStream())

    @Test
    fun writesParagraphsPicturesAndPageBreaks() {
        val writer = DocxWriter(File(System.getProperty("java.io.tmpdir")!!))
        writer.addPage(LayoutPage(595f, 842f, 72f, 72f, 60f, listOf(
            ParagraphBlock("Tiêu đề <A & B>", 24f, Align.CENTER, 0f, 0f, 0f),
            PictureBlock(7, 300f, 200f, Align.CENTER, 6f),
            ParagraphBlock("Body\u0001 text", 11f, Align.LEFT, 36f, -18f, 4f),
        )), mapOf(7 to DocxWriter.Image(byteArrayOf(1, 2, 3), "jpeg")))
        writer.addPage(LayoutPage(595f, 842f, 72f, 72f, 60f, listOf(
            ParagraphBlock("Second page", 11f, Align.RIGHT, 0f, 0f, 0f),
        )), emptyMap())
        writer.addPage(LayoutPage(595f, 842f, 72f, 72f, 60f, emptyList()), emptyMap())

        val out = File.createTempFile("convert", ".docx")
        try {
            writer.save(out)
            writer.close()
            val parts = parts(out)
            assertTrue(parts.keys.containsAll(listOf("[Content_Types].xml", "_rels/.rels", "word/document.xml",
                "word/styles.xml", "word/_rels/document.xml.rels", "word/media/image1.jpeg")))
            assertEquals(listOf<Byte>(1, 2, 3), parts.getValue("word/media/image1.jpeg").toList())
            // every XML part is well formed
            parts.filterKeys { it.endsWith(".xml") || it.endsWith(".rels") }.values.forEach { parse(it) }

            val doc = parse(parts.getValue("word/document.xml"))
            val w = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
            val texts = (0 until doc.getElementsByTagNameNS(w, "t").length).map { doc.getElementsByTagNameNS(w, "t").item(it).textContent }
            assertEquals(listOf("Tiêu đề <A & B>", "Body text", "Second page"), texts)
            assertEquals(2, doc.getElementsByTagNameNS(w, "pageBreakBefore").length) // pages 2 and 3
            assertEquals(1, doc.getElementsByTagNameNS("http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing", "inline").length)

            val xml = String(parts.getValue("word/document.xml"))
            assertTrue(xml.contains("<w:jc w:val=\"center\"/>"))
            assertTrue(xml.contains("<w:jc w:val=\"right\"/>"))
            assertTrue(xml.contains("<w:sz w:val=\"48\"/>"))
            assertTrue(xml.contains("<w:ind w:left=\"720\" w:hanging=\"360\"/>"))
            assertTrue(xml.contains("<wp:extent cx=\"3810000\" cy=\"2540000\"/>"))
            assertTrue(xml.contains("<w:pgSz w:w=\"11900\" w:h=\"16840\"/>"))
            val rels = String(parts.getValue("word/_rels/document.xml.rels"))
            assertTrue(rels.contains("Target=\"media/image1.jpeg\""))
            assertTrue(String(parts.getValue("[Content_Types].xml")).contains("Extension=\"jpeg\" ContentType=\"image/jpeg\""))
        } finally {
            out.delete()
        }
    }

    @Test
    fun emptyDocumentIsStillValid() {
        val out = File.createTempFile("empty", ".docx")
        try {
            DocxWriter(File(System.getProperty("java.io.tmpdir")!!)).use { it.save(out) }
            parse(parts(out).getValue("word/document.xml"))
        } finally {
            out.delete()
        }
    }
}
