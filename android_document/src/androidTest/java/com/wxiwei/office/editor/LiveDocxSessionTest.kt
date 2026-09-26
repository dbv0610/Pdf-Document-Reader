package com.wxiwei.office.editor

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wxiwei.office.editor.OpenDocument.onMain
import com.wxiwei.office.editor.docx.DocxSourceMap
import com.wxiwei.office.editor.docx.LiveDocxSession
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.wp.control.Word
import kotlinx.coroutines.delay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Word formatting shows at once (model + relayout), undoes, and is saved. */
@RunWith(AndroidJUnit4::class)
class LiveDocxSessionTest {
    private val out = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), "render").apply { mkdirs() }

    /** Model offset of [needle] in the main text rebuilt from the source map (across runs). */
    private fun offsetOf(path: String, needle: String): Long {
        val map = DocxSourceMap.get(path)!!
        val leaves = (0 until map.size).map { map.leaf(it) }
        val end = leaves.maxOfOrNull { it.end } ?: 0L
        val chars = CharArray(end.toInt()) { ' ' }
        for (l in leaves) l.text.forEachIndexed { i, c -> chars[(l.start + i).toInt()] = c }
        return String(chars).indexOf(needle).toLong()
    }

    private fun bold(reader: com.wxiwei.office.reader.OfficeReader, offset: Long): Boolean = onMain {
        val doc = (reader.control!!.getView() as Word).getDocument()
        val para = doc.getParagraph(offset)!!
        val leaf = doc.getLeaf(offset)!!
        AttrManage.instance().getFontBold(para.getAttribute(), leaf.getAttribute())
    }

    private suspend fun shot(reader: com.wxiwei.office.reader.OfficeReader, name: String) {
        reader.thumbnails?.invalidateAll()
        delay(500)
        val bitmap = reader.thumbnails!!.render(2, 1240) ?: return
        File(out, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 90, it) }
    }

    @Test
    fun formatLiveUndoSave() {
        val source = OpenDocument.copySample("sample.docx", "live_docx.docx")
        val saved = OpenDocument.output("live_docx_saved.docx")
        OpenDocument.open(source, { it.layout != null }) { reader ->
            val start = offsetOf(source.absolutePath, "MidiConverter parse")
            assertTrue(start >= 0)
            val end = start + "MidiConverter parse".length
            assertFalse(bold(reader, start + 2))
            val session = onMain { LiveDocxSession(reader.control!!, source) }
            assertTrue(session.lastError?.toString(), onMain { session.setBold(start, end, true) })
            assertTrue(onMain { session.setTextColor(start, end, "C00000") })
            assertTrue(onMain { session.setFontSize(start, end, 16f) })
            delay(1500)
            assertTrue("bold in the model at once", bold(reader, start + 2))
            assertFalse("text after the range untouched", bold(reader, end + 3))
            assertTrue("pages laid out again", reader.state.value.pageCount > 0 || onMain { (reader.control!!.getView() as Word).getPageCount() } > 0)
            shot(reader, "docx_live_format")
            assertTrue(onMain { session.undo() }) // size
            assertTrue(onMain { session.undo() }) // color
            assertTrue(onMain { session.undo() }) // bold
            assertFalse("undo", bold(reader, start + 2))
            assertTrue(onMain { session.redo() })
            assertTrue("redo", bold(reader, start + 2))
            val result = onMain { session.save(saved) }
            assertTrue(result.toString(), result is EditResult.Ok)
        }
        OpenDocument.open(saved, { it.layout != null }) { reader ->
            val start = offsetOf(saved.absolutePath, "MidiConverter parse")
            assertTrue("saved bold", bold(reader, start + 2))
            assertEquals(false, bold(reader, start + "MidiConverter parse".length + 3))
        }
    }

    private fun modelText(reader: com.wxiwei.office.reader.OfficeReader, from: Long, len: Int): String = onMain {
        val doc = (reader.control!!.getView() as Word).getDocument()
        val sb = StringBuilder()
        var o = from
        while (sb.length < len) {
            val leaf = doc.getLeaf(o) ?: break
            val t = leaf.getText(null) ?: break
            val k = (o - leaf.getStartOffset()).toInt()
            sb.append(t.substring(k))
            o = leaf.getEndOffset()
        }
        sb.take(len).toString()
    }

    @Test
    fun typeDeleteReplaceLive() {
        val source = OpenDocument.copySample("sample.docx", "live_docx_text.docx")
        val saved = OpenDocument.output("live_docx_text_saved.docx")
        OpenDocument.open(source, { it.layout != null }) { reader ->
            val a = offsetOf(source.absolutePath, "MidiConverter parse")
            val b = offsetOf(source.absolutePath, "tách NoteOn/NoteOff")
            val c = offsetOf(source.absolutePath, "Map mỗi MIDI note")
            val session = onMain { LiveDocxSession(reader.control!!, source) }
            // type "Bộ " then "phân tích " at the same place: one queued insert
            assertTrue(session.lastError?.toString(), onMain { session.insertText(a, "Bộ ") })
            assertTrue(onMain { session.insertText(a + 3, "phân tích ") })
            assertEquals("Bộ phân tích MidiConverter", modelText(reader, a, 26))
            // delete "tách " (after the typed text: current offset moved by 13)
            val bNow = b + 13
            assertEquals("tách ", modelText(reader, bNow, 5))
            assertTrue(session.lastError?.toString(), onMain { session.deleteText(bNow, bNow + 5) })
            assertEquals("NoteOn/NoteOff", modelText(reader, bNow, 14))
            // replace "Map" in the next paragraph (moved by +13 -5)
            val cNow = c + 8
            assertEquals("Map mỗi", modelText(reader, cNow, 7))
            assertTrue(session.lastError?.toString(), onMain { session.replaceText(cNow, cNow + 3, "Ánh xạ") })
            assertEquals("Ánh xạ mỗi", modelText(reader, cNow, 10))
            // format original text after the edits: offsets are mapped for the file
            val mNow = cNow + "Ánh xạ ".length
            assertTrue(session.lastError?.toString(), onMain { session.setBold(mNow, mNow + 3, true) })
            // formatting typed text is refused until saved
            assertTrue(!onMain { session.setBold(a, a + 3, true) })
            // undo the bold and the replace, redo them
            assertTrue(onMain { session.undo() }); assertTrue(onMain { session.undo() })
            assertEquals("Map mỗi", modelText(reader, cNow, 7))
            assertTrue(onMain { session.redo() }); assertTrue(onMain { session.redo() })
            assertEquals("Ánh xạ mỗi", modelText(reader, cNow, 10))
            assertTrue(!session.needsReopen)
            val result = onMain { session.save(saved) }
            assertTrue(result.toString(), result is EditResult.Ok)
        }
        OpenDocument.open(saved, { it.layout != null }) { reader ->
            val a = offsetOf(saved.absolutePath, "Bộ phân tích MidiConverter parse")
            assertTrue("typed text saved", a >= 0)
            assertTrue("deleted text saved", offsetOf(saved.absolutePath, "tách NoteOn") < 0)
            val m = offsetOf(saved.absolutePath, "Ánh xạ mỗi MIDI")
            assertTrue("replace saved", m >= 0)
            assertTrue("bold saved", bold(reader, m + "Ánh xạ ".length + 1))
        }
    }

    private fun paragraphText(reader: com.wxiwei.office.reader.OfficeReader, offset: Long): String = onMain {
        val doc = (reader.control!!.getView() as Word).getDocument()
        val p = doc.getParagraph(offset)!!
        doc.getText(p.getStartOffset(), p.getEndOffset())
    }

    @Test
    fun enterSplitsParagraphLive() {
        val source = OpenDocument.copySample("sample.docx", "live_docx_enter.docx")
        val saved = OpenDocument.output("live_docx_enter_saved.docx")
        OpenDocument.open(source, { it.layout != null }) { reader ->
            val at = offsetOf(source.absolutePath, "người dùng nạp một file MIDI")
            assertTrue(at >= 0)
            val count = onMain { (reader.control!!.getView() as Word).getDocument().getParaCount(0) }
            val session = onMain { LiveDocxSession(reader.control!!, source) }
            assertTrue(session.lastError?.toString(), onMain { session.insertText(at, "\n") })
            assertEquals(count + 1, onMain { (reader.control!!.getView() as Word).getDocument().getParaCount(0) })
            assertTrue("second part starts a paragraph", paragraphText(reader, at + 1).startsWith("người dùng nạp"))
            assertTrue("first part ends with the mark", paragraphText(reader, at - 1).endsWith("cho phép \n"))
            assertTrue(onMain { session.undo() })
            assertEquals(count, onMain { (reader.control!!.getView() as Word).getDocument().getParaCount(0) })
            assertTrue(paragraphText(reader, at).contains("cho phép người dùng nạp"))
            assertTrue(onMain { session.redo() })
            assertEquals(count + 1, onMain { (reader.control!!.getView() as Word).getDocument().getParaCount(0) })
            // Backspace right after the Enter takes it back; redo splits again
            assertTrue(session.lastError?.toString(), onMain { session.deleteText(at, at + 1) })
            assertEquals(count, onMain { (reader.control!!.getView() as Word).getDocument().getParaCount(0) })
            assertTrue(onMain { session.redo() })
            assertEquals(count + 1, onMain { (reader.control!!.getView() as Word).getDocument().getParaCount(0) })
            // Backspace joining two paragraphs of the original document
            val second = offsetOf(source.absolutePath, "Luồng từ file .mid")
            val secondNow = second + 1
            assertEquals("Luồng", onMain { (reader.control!!.getView() as Word).getDocument().getText(secondNow, secondNow + 5) })
            val paraCount = onMain { (reader.control!!.getView() as Word).getDocument().getParaCount(0) }
            assertTrue(session.lastError?.toString(), onMain { session.deleteText(secondNow - 1, secondNow) })
            assertEquals(paraCount - 1, onMain { (reader.control!!.getView() as Word).getDocument().getParaCount(0) })
            assertTrue(onMain { session.undo() })
            assertEquals(paraCount, onMain { (reader.control!!.getView() as Word).getDocument().getParaCount(0) })
            // typing in the new paragraph still maps to the file correctly
            assertTrue(session.lastError?.toString(), onMain { session.insertText(at + 1, "→ ") })
            val result = onMain { session.save(saved) }
            assertTrue(result.toString(), result is EditResult.Ok)
        }
        OpenDocument.open(saved, { it.layout != null }) { reader ->
            val at = offsetOf(saved.absolutePath, "→ người dùng nạp")
            assertTrue("split + typed text saved", at >= 0)
            assertTrue(paragraphText(reader, at).startsWith("→ người dùng nạp"))
        }
    }

    @Test
    fun pasteLinesAndEditTyping() {
        val source = OpenDocument.copySample("sample.docx", "live_docx_paste.docx")
        val saved = OpenDocument.output("live_docx_paste_saved.docx")
        OpenDocument.open(source, { it.layout != null }) { reader ->
            val doc = { (reader.control!!.getView() as Word).getDocument() }
            val at = offsetOf(source.absolutePath, "người dùng nạp một file MIDI")
            val count = onMain { doc().getParaCount(0) }
            val session = onMain { LiveDocxSession(reader.control!!, source) }
            // pasting two lines splits the paragraph once
            assertTrue(session.lastError?.toString(), onMain { session.insertText(at, "Dòng một\r\nDòng hai ") })
            assertEquals(count + 1, onMain { doc().getParaCount(0) })
            assertTrue(paragraphText(reader, at).endsWith("Dòng một\n"))
            assertTrue(paragraphText(reader, at + 9).startsWith("Dòng hai người dùng nạp"))
            // an IME rewriting the word being typed ("hai " -> "hai! ") and a Backspace inside it
            val typed = at + 9 + "Dòng hai".length
            assertTrue(session.lastError?.toString(), onMain { session.replaceText(typed, typed + 1, "! ") })
            assertTrue(session.lastError?.toString(), onMain { session.deleteText(typed + 1, typed + 2) })
            assertTrue(paragraphText(reader, at + 9).startsWith("Dòng hai!người dùng nạp"))
            // replacing original text with two lines
            val other = offsetOf(source.absolutePath, "Luồng từ file .mid") + "Dòng một\nDòng hai!".length
            assertEquals("Luồng", onMain { doc().getText(other, other + 5) })
            assertTrue(session.lastError?.toString(), onMain { session.replaceText(other, other + 5, "Dòng A\nDòng B") })
            assertTrue(paragraphText(reader, other).endsWith("Dòng A\n"))
            assertTrue(paragraphText(reader, other + 7).startsWith("Dòng B từ file .mid"))
            val result = onMain { session.save(saved) }
            assertTrue(result.toString(), result is EditResult.Ok)
        }
        OpenDocument.open(saved, { it.layout != null }) { reader ->
            val a = offsetOf(saved.absolutePath, "Dòng hai!người dùng nạp")
            assertTrue("pasted lines saved", a >= 0)
            assertTrue(paragraphText(reader, a).startsWith("Dòng hai!người"))
            assertTrue(paragraphText(reader, a - 1).endsWith("Dòng một\n"))
            val b = offsetOf(saved.absolutePath, "Dòng B từ file .mid")
            assertTrue("multi-line replace saved", b >= 0)
            assertTrue(paragraphText(reader, b - 1).endsWith("Dòng A\n"))
        }
    }

    @Test
    fun paragraphFormattingLive() {
        val source = OpenDocument.copySample("sample.docx", "live_docx_para.docx")
        val saved = OpenDocument.output("live_docx_para_saved.docx")
        val am = AttrManage.instance()
        OpenDocument.open(source, { it.layout != null }) { reader ->
            val at = offsetOf(source.absolutePath, "Tính năng Play-along cho phép")
            val session = onMain { LiveDocxSession(reader.control!!, source) }
            assertTrue(session.lastError?.toString(), onMain { session.setAlignment(at, at + 5, "center") })
            assertTrue(session.lastError?.toString(), onMain { session.setIndentLeft(at, at + 5, 1440) })
            assertTrue(session.lastError?.toString(), onMain { session.setLineSpacing(at, at + 5, 2f) })
            onMain {
                val p = (reader.control!!.getView() as Word).getDocument().getParagraph(at)!!
                assertEquals(com.wxiwei.office.constant.wp.WPAttrConstant.PARA_HOR_ALIGN_CENTER.toInt(), am.getParaHorizontalAlign(p.getAttribute()))
                assertEquals(1440, am.getParaIndentLeft(p.getAttribute()))
            }
            assertTrue(onMain { session.undo() }) // spacing
            val result = onMain { session.save(saved) }
            assertTrue(result.toString(), result is EditResult.Ok)
        }
        val xml = java.util.zip.ZipFile(saved).use { z -> z.getInputStream(z.getEntry("word/document.xml")).readBytes().toString(Charsets.UTF_8) }
        val i = xml.indexOf("Tính năng Play-along cho phép")
        val pPr = xml.substring(xml.lastIndexOf("<w:pPr>", i), xml.indexOf("</w:pPr>", xml.lastIndexOf("<w:pPr>", i)))
        assertTrue(pPr, pPr.contains("<w:ind w:left=\"1440\"/>") && pPr.contains("<w:jc w:val=\"center\"/>"))
        assertTrue("schema order: spacing, ind, jc: $pPr", pPr.indexOf("w:spacing") < pPr.indexOf("w:ind") && pPr.indexOf("w:ind") < pPr.indexOf("w:jc"))
        assertTrue("undone spacing not saved", !pPr.contains("w:line=\"480\""))
        OpenDocument.open(saved, { it.layout != null }) { reader ->
            val at = offsetOf(saved.absolutePath, "Tính năng Play-along cho phép")
            onMain {
                val p = (reader.control!!.getView() as Word).getDocument().getParagraph(at)!!
                assertEquals(com.wxiwei.office.constant.wp.WPAttrConstant.PARA_HOR_ALIGN_CENTER.toInt(), am.getParaHorizontalAlign(p.getAttribute()))
            }
        }
    }
}
