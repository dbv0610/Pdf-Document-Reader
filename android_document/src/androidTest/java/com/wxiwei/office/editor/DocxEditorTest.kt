package com.wxiwei.office.editor

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wxiwei.office.editor.OpenDocument.onMain
import com.wxiwei.office.editor.docx.DocxEditor
import com.wxiwei.office.editor.docx.DocxSourceMap
import com.wxiwei.office.wp.control.Word
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** sample.docx: bold a word, replace another, insert and delete text; the saved file reopens with them. */
@RunWith(AndroidJUnit4::class)
class DocxEditorTest {
    /** Main text by model offset, rebuilt from the source map the editor uses. */
    private fun text(path: String): String {
        val map = DocxSourceMap.get(path)!!
        val leaves = (0 until map.size).map { map.leaf(it) }.filter { it.start < com.wxiwei.office.constant.wp.WPModelConstant.HEADER } // the body only
        val end = leaves.maxOfOrNull { it.end } ?: 0L
        val chars = CharArray(end.toInt()) { ' ' }
        for (l in leaves) l.text.forEachIndexed { i, c -> chars[(l.start + i).toInt()] = c }
        return String(chars)
    }

    /** Text of [area] (WPModelConstant.HEADER/FOOTER) from the source map, with the offset where it starts. */
    private fun story(path: String, area: Long): Pair<Long, String> {
        val map = DocxSourceMap.get(path)!!
        val leaves = (0 until map.size).map { map.leaf(it) }.filter { (it.start and com.wxiwei.office.constant.wp.WPModelConstant.AREA_MASK) == area }
        val start = leaves.minOf { it.start }
        val chars = CharArray((leaves.maxOf { it.end } - start).toInt()) { ' ' }
        for (l in leaves) l.text.forEachIndexed { i, c -> chars[(l.start - start + i).toInt()] = c }
        return start to String(chars)
    }

    /** The footer ("Trang N • Internal Dev Doc") and the body are edited and saved in one go. */
    @Test
    fun editFooterAndBody() {
        val footer = com.wxiwei.office.constant.wp.WPModelConstant.FOOTER
        val source = OpenDocument.copySample("sample.docx", "docx_footer_source.docx")
        val saved = OpenDocument.output("docx_footer_saved.docx")
        OpenDocument.open(source, { it.layout != null }) { _ ->
            val (base, text) = story(source.absolutePath, footer)
            val at = text.indexOf("Internal Dev Doc")
            assertTrue(text, at >= 0)
            val editor = DocxEditor(source, DocxSourceMap.get(source.absolutePath)!!)
            val s = base + at
            assertTrue(editor.lastError?.toString(), editor.replaceText(s, s + "Internal Dev Doc".length, "Tài liệu nội bộ"))
            val trang = base + text.indexOf("Trang")
            assertTrue(editor.lastError?.toString(), editor.setBold(trang, trang + "Trang".length, true))
            assertTrue(editor.lastError?.toString(), editor.insertText(0, "BẢN NHÁP "))
            // an edit may not run from one story into another
            assertTrue(!editor.deleteText(0, s))
            val result = editor.save(saved)
            assertTrue(result.toString(), result is EditResult.Ok)
        }
        OpenDocument.open(saved, { it.layout != null }) { _ ->
            val (_, text) = story(saved.absolutePath, footer)
            assertTrue(text, text.contains("Tài liệu nội bộ") && !text.contains("Internal Dev Doc"))
            assertTrue("body", text(saved.absolutePath).startsWith("BẢN NHÁP "))
        }
        val xml = java.util.zip.ZipFile(saved).use { z ->
            z.entries().toList().filter { it.name.startsWith("word/footer") }.joinToString { e -> z.getInputStream(e).readBytes().toString(Charsets.UTF_8) }
        }
        // the run holding "Trang" is bold
        val at = xml.indexOf("Trang")
        val runStart = Regex("<w:r[ >]").findAll(xml.substring(0, at)).last().range.first
        assertTrue(xml.substring(runStart, at), xml.substring(runStart, at).contains("<w:b w:val=\"1\""))
    }

    /** sample.docx with a first-page header (titlePg) and an even-page header (evenAndOddHeaders). */
    private fun firstAndEvenHeaders(name: String): java.io.File {
        val source = OpenDocument.copySample("sample.docx", "$name.src.docx")
        val out = OpenDocument.output("$name.docx")
        val ns = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
        fun header(text: String) = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><w:hdr $ns><w:p><w:r><w:t>$text</w:t></w:r></w:p></w:hdr>"
        java.util.zip.ZipFile(source).use { zip ->
            java.util.zip.ZipOutputStream(out.outputStream()).use { zos ->
                fun put(name: String, bytes: ByteArray) { zos.putNextEntry(java.util.zip.ZipEntry(name)); zos.write(bytes); zos.closeEntry() }
                for (entry in zip.entries()) {
                    var text = zip.getInputStream(entry).readBytes().toString(Charsets.UTF_8)
                    when (entry.name) {
                        "word/document.xml" -> text = text
                            .replace("<w:headerReference r:id=\"rId3\" w:type=\"default\"/>",
                                "<w:headerReference r:id=\"rId3\" w:type=\"default\"/><w:headerReference r:id=\"rId90\" w:type=\"first\"/><w:headerReference r:id=\"rId91\" w:type=\"even\"/>")
                            .replace("<w:docGrid ", "<w:titlePg/><w:docGrid ")
                        "word/_rels/document.xml.rels" -> text = text.replace("</Relationships>",
                            "<Relationship Id=\"rId90\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/header\" Target=\"header2.xml\"/>" +
                            "<Relationship Id=\"rId91\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/header\" Target=\"header3.xml\"/></Relationships>")
                        "[Content_Types].xml" -> text = text.replace("</Types>",
                            "<Override PartName=\"/word/header2.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.header+xml\"/>" +
                            "<Override PartName=\"/word/header3.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.header+xml\"/></Types>")
                        "word/settings.xml" -> text = Regex("<w:settings[^>]*>").replace(text) { it.value + "<w:evenAndOddHeaders/>" }
                    }
                    put(entry.name, text.toByteArray())
                }
                put("word/header2.xml", header("TRANG ĐẦU").toByteArray())
                put("word/header3.xml", header("TRANG CHẴN").toByteArray())
            }
        }
        return out
    }

    /** Page 1 shows the first-page header, page 2 the even one, page 3 the default; the first-page one is edited and saved to header2.xml. */
    @Test
    fun firstAndEvenPageHeaders() {
        val file = firstAndEvenHeaders("docx_first_even")
        val saved = OpenDocument.output("docx_first_even_saved.docx")
        OpenDocument.open(file, { it.layout != null && it.pageCount >= 3 }) { reader ->
            kotlinx.coroutines.delay(1000)
            val (headers, footers) = onMain {
                val word = reader.control!!.getView() as Word
                val root = if (word.getCurrentRootType() == com.wxiwei.office.constant.wp.WPViewConstant.PRINT_ROOT.toInt())
                    word.getPrintWord().getListView().model as com.wxiwei.office.wp.view.PageRoot
                else word.getRoot(com.wxiwei.office.constant.wp.WPViewConstant.PAGE_ROOT.toInt()) as com.wxiwei.office.wp.view.PageRoot
                val pages = (0 until 3).map { root.getPageView(it)!! }
                pages.map { p -> p.getHeader()?.getElement()?.let { e -> word.getDocument().getText(e.getStartOffset(), e.getEndOffset()).trim() } } to pages.map { it.getFooter() != null }
            }
            assertEquals(listOf("TRANG ĐẦU", "TRANG CHẴN"), headers.take(2))
            assertTrue(headers[2].toString(), headers[2]!!.startsWith("PianoLearn"))
            // no first-page or even footer in the file: only page 3 has one
            assertEquals(listOf(false, false, true), footers)
            val (base, text) = story(file.absolutePath, com.wxiwei.office.constant.wp.WPModelConstant.HEADER).let { (b, t) -> b to t }
            val at = base + text.indexOf("TRANG ĐẦU")
            val editor = DocxEditor(file, DocxSourceMap.get(file.absolutePath)!!)
            assertTrue(editor.lastError?.toString(), editor.replaceText(at, at + "TRANG ĐẦU".length, "BÌA"))
            assertTrue(editor.save(saved) is EditResult.Ok)
        }
        java.util.zip.ZipFile(saved).use { z ->
            fun part(n: String) = z.getInputStream(z.getEntry(n)).readBytes().toString(Charsets.UTF_8)
            assertTrue(part("word/header2.xml").contains("BÌA"))
            assertTrue(part("word/header3.xml").contains("TRANG CHẴN"))
            assertTrue(part("word/header1.xml").contains("PianoLearn"))
        }
    }

    /** A 3 x 2 table after the paragraph "1. Tổng quan tính năng": saved as a bordered table, reopened as one. */
    @Test
    fun insertTable() {
        val source = OpenDocument.copySample("sample.docx", "docx_table_source.docx")
        val saved = OpenDocument.output("docx_table_saved.docx")
        OpenDocument.open(source, { it.layout != null }) { _ ->
            val at = text(source.absolutePath).indexOf("Tổng quan tính năng").toLong()
            assertTrue(at > 0)
            val editor = DocxEditor(source, DocxSourceMap.get(source.absolutePath)!!)
            assertTrue(editor.lastError?.toString(), editor.insertTable(at, 3, 2))
            assertTrue(editor.save(saved) is EditResult.Ok)
        }
        val xml = java.util.zip.ZipFile(saved).use { z -> z.getInputStream(z.getEntry("word/document.xml")).readBytes().toString(Charsets.UTF_8) }
        val heading = xml.indexOf("Tổng quan tính năng")
        val table = xml.indexOf("<w:tbl>", heading)
        assertTrue("table after the heading", table > heading && xml.indexOf("<w:p", heading) > 0)
        val tbl = xml.substring(table, xml.indexOf("</w:tbl>", table))
        assertEquals(3, Regex("<w:tr>").findAll(tbl).count())
        assertEquals(6, Regex("<w:tc>").findAll(tbl).count())
        assertTrue(tbl.contains("<w:insideH "))
        OpenDocument.open(saved, { it.layout != null }) { reader ->
            val tables = onMain {
                val doc = (reader.control!!.getView() as Word).getDocument() as com.wxiwei.office.wp.model.WPDocument
                doc.getTableCollection(0)?.size() ?: 0
            }
            // sample.docx had 5 tables
            assertEquals(6, tables)
        }
    }

    /** The first table of sample.docx, moved before the first paragraph of the document. */
    @Test
    fun moveTable() {
        val source = OpenDocument.copySample("sample.docx", "docx_table_move_source.docx")
        val saved = OpenDocument.output("docx_table_move_saved.docx")
        OpenDocument.open(source, { it.layout != null }) { reader ->
            val session = onMain { com.wxiwei.office.editor.docx.LiveDocxSession(reader.control!!, source) }
            val table = onMain {
                val doc = (reader.control!!.getView() as Word).getDocument() as com.wxiwei.office.wp.model.WPDocument
                doc.getTableCollection(0)!!.getElementForIndex(0)!!.getStartOffset()
            }
            assertTrue("table found in the model", onMain { session.tableAt(table) } != null)
            assertTrue(session.lastError?.toString(), onMain { session.moveTable(table, 0, after = false) })
            assertTrue("shown live", onMain { !session.needsReopen })
            assertTrue(onMain { session.save(saved) } is EditResult.Ok)
        }
        val xml = java.util.zip.ZipFile(saved).use { z -> z.getInputStream(z.getEntry("word/document.xml")).readBytes().toString(Charsets.UTF_8) }
        val body = xml.indexOf("<w:body>") + "<w:body>".length
        assertTrue("table first in the body", xml.startsWith("<w:tbl>", body))
    }

    /** The picture of sample.docx: resized to 200 x 100 px and moved to the start of the document. */
    @Test
    fun resizeAndMovePicture() {
        val source = OpenDocument.copySample("sample.docx", "docx_picture_source.docx")
        val saved = OpenDocument.output("docx_picture_saved.docx")
        OpenDocument.open(source, { it.layout != null }) { reader ->
            val map = DocxSourceMap.get(source.absolutePath)!!
            val at = (0 until map.size).map { map.leaf(it) }.first { it.kind == DocxSourceMap.Kind.OBJECT && it.start < com.wxiwei.office.constant.wp.WPModelConstant.HEADER }.start
            val session = onMain { com.wxiwei.office.editor.docx.LiveDocxSession(reader.control!!, source) }
            assertTrue("picture found in the model", onMain { session.shapeAt(at) } != null)
            assertTrue(session.lastError?.toString(), onMain { session.resizeObject(at, 200, 100) })
            assertTrue(session.lastError?.toString(), onMain { session.moveObject(at, 0) })
            // both shown at once now
            assertTrue(onMain { !session.needsReopen })
            assertTrue(onMain { session.save(saved) } is EditResult.Ok)
        }
        val xml = java.util.zip.ZipFile(saved).use { z -> z.getInputStream(z.getEntry("word/document.xml")).readBytes().toString(Charsets.UTF_8) }
        val drawing = xml.indexOf("<w:drawing>")
        assertTrue("picture before the first text", drawing in 0 until xml.indexOf("<w:t"))
        assertTrue(xml.substring(drawing).contains("cx=\"1905000\" cy=\"952500\""))
    }

    @Test
    fun editSaveReopen() {
        val source = OpenDocument.copySample("sample.docx", "docx_edit_source.docx")
        val saved = OpenDocument.output("docx_edit_saved.docx")
        OpenDocument.open(source, { it.layout != null }) { reader ->
            val all = text(source.absolutePath)
            val map = DocxSourceMap.get(source.absolutePath)!!
            val editor = DocxEditor(source, map)
            val play = all.indexOf("Play-along").toLong()
            val midi = all.indexOf("MidiConverter.kt").toLong()
            val del = all.indexOf("(thêm 3 GameMode)").toLong()
            assertTrue(play >= 0 && midi >= 0 && del >= 0)
            assertTrue(editor.lastError?.toString(), editor.setBold(play, play + 10, true))
            assertTrue(editor.lastError?.toString(), editor.replaceText(midi, midi + "MidiConverter".length, "MidiParser"))
            assertTrue(editor.lastError?.toString(), editor.insertText(0, "BẢN NHÁP "))
            assertTrue(editor.lastError?.toString(), editor.deleteText(del, del + "(thêm 3 GameMode)".length))
            val result = editor.save(saved)
            assertTrue(result.toString(), result is EditResult.Ok)
        }
        OpenDocument.open(saved, { it.layout != null }) { _ ->
            val all = text(saved.absolutePath)
            assertTrue("inserted", all.startsWith("BẢN NHÁP "))
            assertTrue("replaced", all.contains("MidiParser.kt") && !all.contains("MidiConverter.kt, "))
            assertTrue("deleted", !all.contains("(thêm 3 GameMode)"))
        }
    }
}
