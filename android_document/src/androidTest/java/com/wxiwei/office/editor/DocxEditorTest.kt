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
        val leaves = (0 until map.size).map { map.leaf(it) }
        val end = leaves.maxOfOrNull { it.end } ?: 0L
        val chars = CharArray(end.toInt()) { ' ' }
        for (l in leaves) l.text.forEachIndexed { i, c -> chars[(l.start + i).toInt()] = c }
        return String(chars)
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
