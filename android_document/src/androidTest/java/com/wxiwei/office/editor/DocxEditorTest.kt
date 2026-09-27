package com.wxiwei.office.editor

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wxiwei.office.editor.OpenDocument.onMain
import com.wxiwei.office.editor.docx.DocxEditor
import com.wxiwei.office.editor.docx.DocxSourceMap
import com.wxiwei.office.wp.control.Word
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
