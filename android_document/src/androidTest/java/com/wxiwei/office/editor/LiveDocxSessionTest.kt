/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
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
        val leaves = (0 until map.size).map { map.leaf(it) }.filter { it.start < com.wxiwei.office.constant.wp.WPModelConstant.HEADER } // the body only
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

    private suspend fun shot(reader: com.wxiwei.office.reader.OfficeReader, name: String, page: Int = 2) {
        reader.thumbnails?.invalidateAll()
        delay(500)
        val bitmap = reader.thumbnails!!.render(page, 1240) ?: return
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

    /** sample.docx with its run "MidiConverter parse..." split: "MidiConverter" hidden (w:vanish), " parse" raised 6pt, the rest in small caps. */
    private fun hiddenAndRaised(name: String): File {
        val source = OpenDocument.copySample("sample.docx", "$name.src.docx")
        val out = OpenDocument.output("$name.docx")
        java.util.zip.ZipFile(source).use { zip ->
            java.util.zip.ZipOutputStream(out.outputStream()).use { zos ->
                for (entry in zip.entries()) {
                    var bytes = zip.getInputStream(entry).readBytes()
                    if (entry.name == "word/document.xml") {
                        val xml = bytes.toString(Charsets.UTF_8)
                        val rPr = "<w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:eastAsia=\"Arial\" w:cs=\"Arial\"/><w:color w:val=\"222222\"/><w:sz w:val=\"22\"/><w:szCs w:val=\"22\"/></w:rPr>"
                        val old = "<w:r>$rPr<w:t>MidiConverter parse"
                        check(xml.contains(old))
                        val hidden = rPr.replace("<w:sz ", "<w:vanish/><w:sz ")
                        val raised = rPr.replace("<w:sz ", "<w:position w:val=\"12\"/><w:sz ")
                        val small = rPr.replace("<w:sz ", "<w:smallCaps/><w:sz ")
                        bytes = xml.replace(old, "<w:r>$hidden<w:t>MidiConverter</w:t></w:r><w:r>$raised<w:t xml:space=\"preserve\"> parse</w:t></w:r><w:r>$small<w:t>").toByteArray()
                    }
                    zos.putNextEntry(java.util.zip.ZipEntry(entry.name))
                    zos.write(bytes)
                    zos.closeEntry()
                }
            }
        }
        return out
    }

    /** doc_test.docx: "School Name" is in a text box; tap it, type, bold, undo/redo, save, reopen. */
    @Test
    fun editTextBox() {
        val textbox = com.wxiwei.office.constant.wp.WPModelConstant.TEXTBOX
        val source = OpenDocument.copySample("doc_test.docx", "live_docx_textbox.docx")
        val saved = OpenDocument.output("live_docx_textbox_saved.docx")
        fun find(path: String, needle: String): Long {
            val map = DocxSourceMap.get(path)!!
            for (i in 0 until map.size) {
                val l = map.leaf(i)
                val k = l.text.indexOf(needle)
                if (k >= 0 && (l.start and com.wxiwei.office.constant.wp.WPModelConstant.AREA_MASK) == textbox) return l.start + k
            }
            return -1
        }
        OpenDocument.open(source, { it.layout != null }) { reader ->
            delay(1500)
            val at = find(source.absolutePath, "School Name")
            assertTrue("text box text mapped", at > 0)
            // the caret spot of "S" and a tap on its middle give that offset back
            val tapped = onMain {
                val sel = com.wxiwei.office.editor.word.WordSelection(reader.control!!).apply { storyPage = 0 }
                val r = sel.rectsFor(at, at + 1).first()
                sel.offsetAt(r.left + r.width() * 0.25f, r.exactCenterY())
            }
            assertEquals(at, tapped)
            val session = onMain { LiveDocxSession(reader.control!!, source) }
            assertTrue(session.lastError?.toString(), onMain { session.insertText(at, "Tên ") })
            assertEquals("Tên School Name", modelText(reader, at, 15))
            assertTrue(session.lastError?.toString(), onMain { session.setBold(at, at + 3, true) })
            assertTrue("bold live", bold(reader, at + 1))
            assertTrue(onMain { session.undo() })
            assertFalse("undo bold", bold(reader, at + 1))
            assertTrue(onMain { session.redo() })
            assertTrue("redo bold", bold(reader, at + 1))
            delay(1000)
            shot(reader, "docx_textbox_edit", 1)
            assertTrue(onMain { session.save(saved) } is EditResult.Ok)
        }
        OpenDocument.open(saved, { it.layout != null }) { reader ->
            delay(1000)
            // "Tên" is a bold run of its own now, right before "School Name"
            val name = find(saved.absolutePath, "School Name")
            assertTrue("text box still mapped", name > 0)
            assertEquals("Tên School Name", modelText(reader, name - 4, 15))
            assertTrue("saved bold", bold(reader, name - 3))
            assertFalse("rest not bold", bold(reader, name + 1))
        }
    }

    /** Copy "MidiConverter parse" with "Midi" bold and "parse" red, paste it elsewhere: formatting kept, one undo, saved. */
    @Test
    fun pasteKeepsFormatting() {
        val source = OpenDocument.copySample("sample.docx", "live_docx_paste_fmt.docx")
        val saved = OpenDocument.output("live_docx_paste_fmt_saved.docx")
        OpenDocument.open(source, { it.layout != null }) { reader ->
            val from = offsetOf(source.absolutePath, "MidiConverter parse")
            val to = offsetOf(source.absolutePath, "Map mỗi MIDI note")
            assertTrue(from >= 0 && to > from)
            val session = onMain { LiveDocxSession(reader.control!!, source) }
            assertTrue(onMain { session.setBold(from, from + 4, true) })
            assertTrue(onMain { session.setTextColor(from + 14, from + 19, "C00000") })
            val clip = onMain { session.copyFormatted(from, from + 19) }
            assertEquals("MidiConverter parse", clip.text)
            // "Map mỗi" moved by nothing: the formatting above changed no text
            assertTrue(session.lastError?.toString(), onMain { session.pasteFormatted(to, to, clip) })
            assertEquals("MidiConverter parseMap", modelText(reader, to, 22))
            assertTrue("pasted bold", bold(reader, to + 1))
            assertFalse("pasted plain after bold", bold(reader, to + 6))
            assertEquals(0xC00000, onMain {
                val doc = (reader.control!!.getView() as Word).getDocument()
                AttrManage.instance().getFontColor(doc.getParagraph(to + 15)!!.getAttribute(), doc.getLeaf(to + 15)!!.getAttribute()) and 0xFFFFFF
            })
            assertFalse("text after the paste keeps its look", bold(reader, to + 20))
            // one undo takes the whole paste away
            assertTrue(onMain { session.undo() })
            assertEquals("Map mỗi MIDI", modelText(reader, to, 12))
            assertTrue(onMain { session.redo() })
            assertTrue("redo", bold(reader, to + 1))
            assertTrue(onMain { session.save(saved) } is EditResult.Ok)
        }
        OpenDocument.open(saved, { it.layout != null }) { reader ->
            val at = offsetOf(saved.absolutePath, "MidiConverter parseMap")
            assertTrue("saved", at >= 0)
            assertTrue("saved bold", bold(reader, at + 1))
            assertFalse("saved plain", bold(reader, at + 6))
        }
    }

    /** Cell widths in percent (tcW type="pct", fiftieths of a percent) of a table without a grid. */
    @Test
    fun percentCellWidthsWithoutGrid() {
        val source = OpenDocument.copySample("sample.docx", "docx_pct_cells.src.docx")
        val file = OpenDocument.output("docx_pct_cells.docx")
        java.util.zip.ZipFile(source).use { zip ->
            java.util.zip.ZipOutputStream(file.outputStream()).use { zos ->
                for (entry in zip.entries()) {
                    var bytes = zip.getInputStream(entry).readBytes()
                    if (entry.name == "word/document.xml") {
                        val xml = bytes.toString(Charsets.UTF_8)
                        val a = xml.indexOf("<w:tbl>"); val b = xml.indexOf("</w:tbl>", a)
                        // first table: no grid, 20% / 80% of its 9200 twips
                        val table = xml.substring(a, b)
                            .replace("<w:tblGrid><w:gridCol w:w=\"3000\"/><w:gridCol w:w=\"6200\"/></w:tblGrid>", "")
                            .replace("<w:tcW w:w=\"3000\" w:type=\"dxa\"/>", "<w:tcW w:w=\"1000\" w:type=\"pct\"/>")
                            .replace("<w:tcW w:w=\"6200\" w:type=\"dxa\"/>", "<w:tcW w:w=\"80%\" w:type=\"pct\"/>")
                        check(!table.contains("tblGrid") && table.contains("w:w=\"1000\" w:type=\"pct\""))
                        bytes = (xml.substring(0, a) + table + xml.substring(b)).toByteArray()
                    }
                    zos.putNextEntry(java.util.zip.ZipEntry(entry.name))
                    zos.write(bytes)
                    zos.closeEntry()
                }
            }
        }
        OpenDocument.open(file, { it.layout != null }) { reader ->
            val first = offsetOf(file.absolutePath, "Hạng mục")
            val second = offsetOf(file.absolutePath, "Thông tin")
            assertTrue(first >= 0 && second > first)
            delay(500)
            val (dx, zoom) = onMain {
                val sel = com.wxiwei.office.editor.word.WordSelection(reader.control!!)
                (sel.caretRect(second)!!.left - sel.caretRect(first)!!.left) to (reader.control!!.getView() as Word).getZoom()
            }
            // both cells have the same left margin: the text starts one column (20% of 9200 twips) apart
            val expected = 1840 * com.wxiwei.office.constant.MainConstant.TWIPS_TO_PIXEL
            assertEquals(expected, dx / zoom, 3f)
        }
    }

    /** Hidden text takes no room (the text after it starts where it would); raised text keeps its place in the line. */
    @Test
    fun hiddenAndRaisedText() {
        val file = hiddenAndRaised("docx_hidden")
        OpenDocument.open(file, { it.layout != null }) { reader ->
            val at = offsetOf(file.absolutePath, "MidiConverter parse")
            assertTrue(at >= 0)
            delay(500)
            val (hidden, next) = onMain {
                val sel = com.wxiwei.office.editor.word.WordSelection(reader.control!!)
                sel.rectsFor(at, at + 13).first() to sel.caretRect(at + 13)!!
            }
            assertTrue("hidden text width ${hidden.width()}", hidden.width() <= 2)
            assertTrue("text after hidden text starts where it did", Math.abs(next.left - hidden.left) <= 2)
            shot(reader, "docx_hidden_raised")
        }
    }

    /** Offset of [needle] in the header or footer ([area]) from the source map. */
    private fun storyOffsetOf(path: String, area: Long, needle: String): Long {
        val map = DocxSourceMap.get(path)!!
        val leaves = (0 until map.size).map { map.leaf(it) }.filter { (it.start and com.wxiwei.office.constant.wp.WPModelConstant.AREA_MASK) == area }
        val base = leaves.minOf { it.start }
        val chars = CharArray((leaves.maxOf { it.end } - base).toInt()) { ' ' }
        for (l in leaves) l.text.forEachIndexed { i, c -> chars[(l.start - base + i).toInt()] = c }
        return String(chars).indexOf(needle).let { if (it < 0) -1 else base + it }
    }

    /** Typing in the footer shows at once, does not move body offsets (nor body typing footer ones), and saves. */
    @Test
    fun editFooterLive() {
        val footer = com.wxiwei.office.constant.wp.WPModelConstant.FOOTER
        val source = OpenDocument.copySample("sample.docx", "live_docx_footer.docx")
        val saved = OpenDocument.output("live_docx_footer_saved.docx")
        OpenDocument.open(source, { it.layout != null }) { reader ->
            val f = storyOffsetOf(source.absolutePath, footer, "Internal Dev Doc")
            val body = offsetOf(source.absolutePath, "MidiConverter parse")
            assertTrue(f >= 0 && body >= 0)
            val session = onMain { LiveDocxSession(reader.control!!, source) }
            assertTrue(session.lastError?.toString(), onMain { session.insertText(body, "Bộ ") })
            assertTrue(session.lastError?.toString(), onMain { session.insertText(f, "Mật — ") })
            assertEquals("Mật — Internal", modelText(reader, f, 14))
            // bold the typed footer text, delete "Dev " after it
            assertTrue(session.lastError?.toString(), onMain { session.setBold(f, f + 3, true) })
            val dev = f + "Mật — Internal ".length
            assertTrue(session.lastError?.toString(), onMain { session.deleteText(dev, dev + 4) })
            assertEquals("Mật — Internal Doc", modelText(reader, f, 18))
            assertEquals("Bộ MidiConverter", modelText(reader, body, 16))
            assertTrue("footer bold live", bold(reader, f + 1))
            // undo the delete, redo it
            assertTrue(onMain { session.undo() })
            assertEquals("Mật — Internal Dev Doc", modelText(reader, f, 22))
            assertTrue(onMain { session.redo() })
            delay(1500)
            shot(reader, "docx_live_footer")
            val result = onMain { session.save(saved) }
            assertTrue(result.toString(), result is EditResult.Ok)
        }
        OpenDocument.open(saved, { it.layout != null }) { reader ->
            val f = storyOffsetOf(saved.absolutePath, footer, "Mật — Internal Doc")
            assertTrue("footer saved", f >= 0)
            assertTrue("footer bold saved", bold(reader, f + 1))
            assertTrue("body saved", offsetOf(saved.absolutePath, "Bộ MidiConverter") >= 0)
        }
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
            // typed text can be formatted too
            assertTrue(session.lastError?.toString(), onMain { session.setBold(a, a + 3, true) })
            // undo both bolds and the replace, redo them
            repeat(3) { assertTrue(onMain { session.undo() }) }
            assertEquals("Map mỗi", modelText(reader, cNow, 7))
            repeat(3) { assertTrue(onMain { session.redo() }) }
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
    fun typeInTableCell() {
        val source = OpenDocument.copySample("sample.docx", "live_docx_cell.docx")
        val saved = OpenDocument.output("live_docx_cell_saved.docx")
        OpenDocument.open(source, { it.layout != null }) { reader ->
            val at = offsetOf(source.absolutePath, "Nạp file .mid")
            assertTrue(at >= 0)
            val session = onMain { LiveDocxSession(reader.control!!, source) }
            assertTrue(session.lastError?.toString(), onMain { session.insertText(at, "[ô] ") })
            assertTrue(paragraphText(reader, at).startsWith("[ô] Nạp file .mid"))
            assertTrue(session.lastError?.toString(), onMain { session.setBold(at + 4, at + 8, true) })
            val result = onMain { session.save(saved) }
            assertTrue(result.toString(), result is EditResult.Ok)
        }
        OpenDocument.open(saved, { it.layout != null }) { reader ->
            val at = offsetOf(saved.absolutePath, "[ô] Nạp file .mid")
            assertTrue("typed cell text saved", at >= 0)
            assertTrue("bold in cell saved", bold(reader, at + 5))
        }
    }

    /** (start, end) of every laid out page, once the background layout has finished. */
    private suspend fun pages(reader: com.wxiwei.office.reader.OfficeReader): List<Pair<Long, Long>> {
        repeat(100) {
            val done = onMain { (reader.control!!.getView() as Word).getRoot(com.wxiwei.office.constant.wp.WPViewConstant.PAGE_ROOT.toInt()).let { (it as com.wxiwei.office.wp.view.PageRoot).isFinishLayout() } }
            if (done) return@repeat
            delay(100)
        }
        delay(300)
        return onMain {
            val root = (reader.control!!.getView() as Word).getRoot(com.wxiwei.office.constant.wp.WPViewConstant.PAGE_ROOT.toInt()) as com.wxiwei.office.wp.view.PageRoot
            (0 until root.getPageCount()).map { root.getPageView(it)!!.let { p -> p.getStartOffset(null) to p.getEndOffset(null) } }
        }
    }

    @Test
    fun typingSpeedAndIncrementalLayout() {
        for (name in listOf("sample.docx", "doc_test.docx")) {
            val source = OpenDocument.copySample(name, "live_speed_$name")
            OpenDocument.open(source, { it.layout != null }) { reader ->
                val before = pages(reader)
                val session = onMain { LiveDocxSession(reader.control!!, source) }
                val doc = { (reader.control!!.getView() as Word).getDocument() }
                // a paragraph with text on the middle page, where typing lays out only the pages from there
                val page = before[before.size / 2]
                val at = onMain {
                    var o = page.first
                    while (o < page.second) {
                        val p = doc().getParagraph(o) ?: break
                        if (p.getEndOffset() - p.getStartOffset() > 20 && p.getStartOffset() >= page.first) { o = p.getStartOffset() + 5; break }
                        o = p.getEndOffset()
                    }
                    o
                }
                // show the page being edited, as when typing on it
                onMain {
                    val w = reader.control!!.getView() as Word
                    val rect = w.modelToView(at, com.wxiwei.office.java.awt.Rectangle(), false)
                    w.scrollTo(0, ((rect.y - 200) * w.getZoom()).toInt())
                }
                delay(300)
                val times = ArrayList<Long>()
                for (i in 0 until 30) {
                    val t = onMain { val s0 = System.nanoTime(); assertTrue(session.lastError?.toString(), session.insertText(at + i, if (i % 6 == 5) " " else "a")); System.nanoTime() - s0 }
                    times.add(t / 1000)
                    delay(30)
                }
                // Enter and Backspace too
                assertTrue(onMain { session.insertText(at + 30, "\n") })
                val incremental = pages(reader)
                onMain { (reader.control!!.getView() as Word).relayoutContent() }
                val full = pages(reader)
                val sorted = times.sorted()
                android.util.Log.i("TypingSpeed", "$name pages=${before.size} at=$at reopen=${session.needsReopen} median=${sorted[sorted.size / 2]}us max=${sorted.last()}us all=$times")
                assertEquals("incremental layout paginates like a full one", full, incremental)
            }
        }
    }

    /** Not a check: lays sample.docx out again and again for a profiler (simpleperf) to watch. */
    @Test
    fun layoutProfileLoop() {
        if (InstrumentationRegistry.getArguments().getString("profileLoop") == null) return
        val source = OpenDocument.copySample("sample.docx", "live_profile.docx")
        OpenDocument.open(source, { it.layout != null }) { reader ->
            pages(reader)
            val end = System.currentTimeMillis() + 20_000
            while (System.currentTimeMillis() < end) {
                onMain {
                    val w = reader.control!!.getView() as Word
                    synchronized(w.getDocument()) { w.relayoutContent() }
                }
                pages(reader)
            }
        }
    }

    @Test
    fun bulletsLive() {
        val source = OpenDocument.copySample("sample.docx", "live_docx_bullets.docx")
        val saved = OpenDocument.output("live_docx_bullets_saved.docx")
        OpenDocument.open(source, { it.layout != null }) { reader ->
            val at = offsetOf(source.absolutePath, "Tính năng Play-along cho phép")
            assertTrue(at >= 0)
            val session = onMain { LiveDocxSession(reader.control!!, source) }
            assertFalse(onMain { session.hasBullet(at) })
            assertTrue(session.lastError?.toString(), onMain { session.setBullets(at, at + 1, true) })
            assertTrue(onMain { session.hasBullet(at) })
            // the page shows a bullet view for that paragraph
            val drawn = onMain {
                val w = reader.control!!.getView() as Word
                val root = w.getRoot(com.wxiwei.office.constant.wp.WPViewConstant.PAGE_ROOT.toInt())!!
                val para = root.getView(at, com.wxiwei.office.constant.wp.WPViewConstant.PARAGRAPH_VIEW.toInt(), false) as? com.wxiwei.office.wp.view.ParagraphView
                para?.getBNView() != null
            }
            assertTrue("bullet drawn", drawn)
            assertTrue(onMain { session.undo() })
            assertFalse(onMain { session.hasBullet(at) })
            assertTrue(onMain { session.redo() })
            shot(reader, "docx_bullets_live")
            val result = onMain { session.save(saved) }
            assertTrue(result.toString(), result is EditResult.Ok)
        }
        val z = java.util.zip.ZipFile(saved)
        val numbering = z.getEntry("word/numbering.xml")?.let { z.getInputStream(it).readBytes().toString(Charsets.UTF_8) }
        z.close()
        assertTrue("numbering part", numbering != null && numbering.contains("bullet"))
        OpenDocument.open(saved, { it.layout != null }) { reader ->
            val at = offsetOf(saved.absolutePath, "Tính năng Play-along cho phép")
            val session = onMain { LiveDocxSession(reader.control!!, saved) }
            assertTrue("bullet saved", onMain { session.hasBullet(at) })
        }
    }

    @Test
    fun newListsInDocumentWithoutNumbering() {
        val source = OpenDocument.copySample("shape_in_table.docx", "live_docx_lists.docx")
        val saved = OpenDocument.output("live_docx_lists_saved.docx")
        var bulletId = -1
        var numberId = -1
        OpenDocument.open(source, { it.layout != null }) { reader ->
            val p2 = offsetOf(source.absolutePath, "Paragraph 2")
            val p3 = offsetOf(source.absolutePath, "Paragraph 3")
            val p4 = offsetOf(source.absolutePath, "Paragraph 4")
            val session = onMain { LiveDocxSession(reader.control!!, source) }
            // numbering first: its id must still match what save writes
            assertTrue(session.lastError?.toString(), onMain { session.setNumbering(p3, p4 + 1, true) })
            assertTrue(session.lastError?.toString(), onMain { session.setBullets(p2, p2 + 1, true) })
            numberId = onMain { session.listAt(p3) }
            bulletId = onMain { session.listAt(p2) }
            assertTrue(numberId >= 0 && bulletId >= 0 && numberId != bulletId)
            assertEquals(numberId, onMain { session.listAt(p4) })
            assertTrue(session.lastError?.toString(), onMain { session.setListLevel(p4, p4 + 1, 1) })
            assertEquals(1, onMain { session.listLevelAt(p4) })
            val result = onMain { session.save(saved) }
            assertTrue(result.toString(), result is EditResult.Ok)
        }
        OpenDocument.open(saved, { it.layout != null }) { reader ->
            val session = onMain { LiveDocxSession(reader.control!!, saved) }
            assertEquals("numbered list id saved", numberId, onMain { session.listAt(offsetOf(saved.absolutePath, "Paragraph 3")) })
            assertEquals("bullet list id saved", bulletId, onMain { session.listAt(offsetOf(saved.absolutePath, "Paragraph 2")) })
            assertTrue(onMain { session.hasNumbering(offsetOf(saved.absolutePath, "Paragraph 4")) })
            assertEquals("level saved", 1, onMain { session.listLevelAt(offsetOf(saved.absolutePath, "Paragraph 4")) })
        }
    }

    @Test
    fun formatTypedText() {
        val source = OpenDocument.copySample("sample.docx", "live_docx_fmt_typed.docx")
        val saved = OpenDocument.output("live_docx_fmt_typed_saved.docx")
        var liveItalicBan = false
        OpenDocument.open(source, { it.layout != null }) { reader ->
            val at = offsetOf(source.absolutePath, "Luồng từ file .mid")
            val session = onMain { LiveDocxSession(reader.control!!, source) }
            assertTrue(onMain { session.insertText(at, "Xin chào ") })
            // bold "chào" (typed only)
            assertTrue(session.lastError?.toString(), onMain { session.setBold(at + 4, at + 8, true) })
            assertTrue(bold(reader, at + 5))
            assertFalse(bold(reader, at + 1))
            assertTrue(onMain { session.isBold(at + 5) && !session.isBold(at + 1) })
            // italic "ào Luồng": typed + original text in one step
            assertTrue(session.lastError?.toString(), onMain { session.setItalic(at + 6, at + 14, true) })
            assertTrue(onMain { session.undo() })
            assertTrue(onMain { session.redo() })
            // more typing after formatting starts a new insert
            assertTrue(onMain { session.insertText(at + 9, "bạn ") })
            liveItalicBan = onMain {
                val doc = (reader.control!!.getView() as Word).getDocument()
                AttrManage.instance().getFontItalic(doc.getParagraph(at + 10)!!.getAttribute(), doc.getLeaf(at + 10)!!.getAttribute())
            }
            val result = onMain { session.save(saved) }
            assertTrue(result.toString(), result is EditResult.Ok)
        }
        OpenDocument.open(saved, { it.layout != null }) { reader ->
            val at = offsetOf(saved.absolutePath, "Xin chào bạn Luồng từ file .mid")
            assertTrue("text saved", at >= 0)
            assertFalse("Xin not bold", bold(reader, at + 1))
            assertTrue("chào bold", bold(reader, at + 5))
            val italic = { o: Long -> onMain {
                val doc = (reader.control!!.getView() as Word).getDocument()
                AttrManage.instance().getFontItalic(doc.getParagraph(o)!!.getAttribute(), doc.getLeaf(o)!!.getAttribute())
            } }
            assertTrue("ào italic", italic(at + 7))
            assertEquals("bạn saved as shown", liveItalicBan, italic(at + 10))
            assertTrue("Luồng italic", italic(at + 13 + 1))
            assertFalse("từ not italic", italic(at + 13 + 7))
        }
    }

    /** The whole main text of the open document. */
    private fun mainText(reader: com.wxiwei.office.reader.OfficeReader): String = onMain {
        val doc = (reader.control!!.getView() as Word).getDocument()
        doc.getText(0, doc.getAreaEnd(0))
    }

    /** Random typing, deleting, Enter and Backspace; the saved file must read back as shown. */
    @Test
    fun randomEditsSaveAsShown() {
        for ((name, seed) in listOf("sample.docx" to 7L, "sample.docx" to 11L, "sample.docx" to 23L, "doc_test.docx" to 3L, "doc_test.docx" to 5L, "shape_in_table.docx" to 2L).let { all ->
            InstrumentationRegistry.getArguments().getString("fuzzOnly")?.toInt()?.let { listOf(all[it]) } ?: all
        }) {
            val source = OpenDocument.copySample(name, "fuzz_${seed}_$name")
            val saved = OpenDocument.output("fuzz_${seed}_saved_$name")
            var shown = ""
            var log = ""
            OpenDocument.open(source, { it.layout != null }) { reader ->
                pages(reader)
                val session = onMain { LiveDocxSession(reader.control!!, source) }
                val rnd = java.util.Random(seed)
                val end = onMain { (reader.control!!.getView() as Word).getDocument().getAreaEnd(0) }
                var caret = end / 3 + rnd.nextInt(200)
                val ops = StringBuilder()
                var kind = ""
                repeat(InstrumentationRegistry.getArguments().getString("fuzzOps")?.toInt() ?: 120) { step ->
                    val wasReopen = session.needsReopen
                    val docEnd = onMain { (reader.control!!.getView() as Word).getDocument().getAreaEnd(0) }
                    if (rnd.nextInt(6) == 0) caret = rnd.nextInt(docEnd.toInt() - 2).toLong() // jump elsewhere
                    caret = caret.coerceIn(1, docEnd - 2)
                    val k = rnd.nextInt(14)
                    kind = listOf("ins", "ins", "ins", "ins", "ins", "bs", "bs", "enter", "del", "bold", "repl", "paste", "undo", "redo")[k]
                    val ok = when (k) {
                        in 0..4 -> { val t = listOf("a", "ễ", " ", "xin ", "Đ").let { it[rnd.nextInt(it.size)] }; onMain { session.insertText(caret, t) }.also { if (it) caret += t.length } }
                        5, 6 -> onMain { session.deleteText(caret - 1, caret) }.also { if (it) caret -= 1 }
                        7 -> onMain { session.insertText(caret, "\n") }.also { if (it) caret += 1 }
                        8 -> { val n = 1 + rnd.nextInt(5); onMain { session.deleteText(caret, caret + n) } }
                        9 -> onMain { session.setBold(caret - 1, caret + 2, rnd.nextBoolean()) }
                        10 -> { val n = 1 + rnd.nextInt(4); onMain { session.replaceText(caret - n, caret, "zz") }.also { if (it) caret += 2 - n } }
                        12 -> onMain { session.undo() }
                        13 -> onMain { session.redo() }
                        else -> onMain { session.insertText(caret, "dòng một\ndòng hai ") }.also { if (it) caret += 18 }
                    }
                    ops.append("$step@$caret:$kind:${if (ok) "ok" else session.lastError?.message}${if (!wasReopen && session.needsReopen) " REOPEN" else ""} ")
                    if (InstrumentationRegistry.getArguments().getString("saveEachStep") != null) {
                        val r = onMain { session.save(OpenDocument.output("fuzz_step.docx")) }
                        if (r !is EditResult.Ok) {
                            val d = onMain { (reader.control!!.getView() as Word).getDocument() }
                            val around = onMain { d.getText(maxOf(0, caret - 40), minOf(d.getAreaEnd(0), caret + 40)) }
                            throw AssertionError("save breaks at $ops :: $r :: around='$around'")
                        }
                    }
                }
                log = ops.toString()
                assertFalse("all edits shown live: $log", session.needsReopen)
                shown = mainText(reader)
                val result = onMain { session.save(saved) }
                assertTrue(result.toString() + " " + log, result is EditResult.Ok)
            }
            OpenDocument.open(saved, { it.layout != null }) { reader ->
                val reread = mainText(reader)
                if (reread != shown) {
                    val i = reread.zip(shown).indexOfFirst { (a, b) -> a != b }.let { if (it < 0) minOf(reread.length, shown.length) else it }
                    throw AssertionError("$name/$seed differs at $i: shown='${shown.substring(maxOf(0, i - 30), minOf(shown.length, i + 30))}' saved='${reread.substring(maxOf(0, i - 30), minOf(reread.length, i + 30))}' ops=$log")
                }
            }
        }
    }

    /** A document made by "New Word document": one empty paragraph. */
    @Test
    fun typeInNewDocument() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val source = OpenDocument.output("new_blank.docx")
        ctx.assets.open("document_templates/blank.docx").use { i -> source.outputStream().use { i.copyTo(it) } }
        val saved = OpenDocument.output("new_blank_saved.docx")
        OpenDocument.open(source, { it.layout != null }) { reader ->
            val session = onMain { LiveDocxSession(reader.control!!, source) }
            // a tap below the text: the caret the view gives
            val tapped = onMain {
                val w = reader.control!!.getView() as Word
                com.wxiwei.office.editor.word.WordSelection(w).offsetAt(w.width / 2f, w.height * 0.8f)
            }
            android.util.Log.i("NewDoc", "tap offset=$tapped end=${onMain { (reader.control!!.getView() as Word).getDocument().getAreaEnd(0) }}")
            assertEquals("caret stays before the last paragraph mark", 0L, tapped)
            assertTrue(session.lastError?.toString(), onMain { session.insertText(tapped, "Xin chào") })
            assertTrue(session.lastError?.toString(), onMain { session.insertText(8, "\n") })
            assertTrue(session.lastError?.toString(), onMain { session.insertText(9, "Dòng hai") })
            assertTrue(session.lastError?.toString(), onMain { session.setBold(0, 3, true) })
            assertTrue(onMain { session.setBullets(9, 10, true) })
            val result = onMain { session.save(saved) }
            assertTrue(result.toString(), result is EditResult.Ok)
        }
        OpenDocument.open(saved, { it.layout != null }) { reader ->
            assertEquals("Xin chào\nDòng hai\n", mainText(reader))
            assertTrue(bold(reader, 1))
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

    /** Paragraphs and their leaves of the main text tile the offsets with no gap or overlap; problems as text. */
    private fun modelProblems(doc: com.wxiwei.office.wp.model.WPDocument): List<String> {
        val bad = ArrayList<String>()
        val end = doc.getAreaEnd(0)
        var o = 0L
        var prevEnd = 0L
        while (o < end) {
            val p = doc.getParagraph(o) as? com.wxiwei.office.simpletext.model.ParagraphElement ?: run { bad.add("no paragraph at $o"); return bad }
            if (p.getStartOffset() != prevEnd) bad.add("paragraph [${p.getStartOffset()},${p.getEndOffset()}) after $prevEnd")
            var at = p.getStartOffset()
            for (i in 0 until p.leafCount()) {
                val l = p.getElementForIndex(i)!!
                if (l.getStartOffset() != at) bad.add("leaf $i of [${p.getStartOffset()},${p.getEndOffset()}) at ${l.getStartOffset()}, expected $at")
                at = l.getEndOffset()
            }
            if (p !is com.wxiwei.office.wp.model.TableElement && p.leafCount() > 0 && at != p.getEndOffset()) bad.add("leaves of [${p.getStartOffset()},${p.getEndOffset()}) end at $at")
            prevEnd = p.getEndOffset()
            o = maxOf(o + 1, p.getEndOffset())
            if (bad.size > 5) return bad
        }
        return bad
    }

    private fun shapeAtModel(doc: com.wxiwei.office.simpletext.model.IDocument, offset: Long): Boolean {
        val leaf = doc.getLeaf(offset) ?: return false
        return leaf.getStartOffset() == offset && leaf.getEndOffset() == offset + 1 && AttrManage.instance().getShapeID(leaf.getAttribute()) >= 0
    }

    /** sample.docx's in-line picture moved live (no reopen): up into the text above it, then undone; saved. */
    @Test
    fun movePictureLive() {
        val source = OpenDocument.copySample("sample.docx", "docx_live_move_source.docx")
        val saved = OpenDocument.output("docx_live_move_saved.docx")
        OpenDocument.open(source, { it.layout != null }) { reader ->
            val map = DocxSourceMap.get(source.absolutePath)!!
            val pic = (0 until map.size).map { map.leaf(it) }.first { it.kind == DocxSourceMap.Kind.OBJECT && it.start < com.wxiwei.office.constant.wp.WPModelConstant.HEADER }.start
            val target = offsetOf(source.absolutePath, "Hết tài liệu")
            assertTrue(target in 1 until pic)
            val session = onMain { LiveDocxSession(reader.control!!, source) }
            val doc = onMain { (reader.control!!.getView() as Word).getDocument() as com.wxiwei.office.wp.model.WPDocument }
            assertEquals(emptyList<String>(), onMain { modelProblems(doc) })
            val textBefore = onMain { doc.getText(target, target + 12) }
            assertTrue(session.lastError?.toString(), onMain { session.moveObject(pic, target) })
            assertFalse("shown live", onMain { session.needsReopen })
            assertEquals(emptyList<String>(), onMain { modelProblems(doc) })
            assertTrue("picture at the target", onMain { shapeAtModel(doc, target) })
            assertEquals("text after it", textBefore, onMain { doc.getText(target + 1, target + 13) })
            // the pages after the edited one are laid out in the background
            var laidOut = false
            repeat(50) {
                if (!laidOut) {
                    laidOut = onMain {
                        val w = reader.control!!.getView() as Word
                        w.getRoot(w.getCurrentRootType())!!.getView(target, com.wxiwei.office.constant.wp.WPViewConstant.OBJ_VIEW.toInt(), false) is com.wxiwei.office.wp.view.ObjView
                    }
                    if (!laidOut) delay(200)
                }
            }
            assertTrue("laid out as a picture there", laidOut)
            // undo puts it back
            assertTrue(onMain { session.undo() })
            assertEquals(emptyList<String>(), onMain { modelProblems(doc) })
            assertTrue("picture back", onMain { shapeAtModel(doc, pic) })
            assertTrue(onMain { session.redo() })
            assertTrue(onMain { shapeAtModel(doc, target) })
            assertTrue(onMain { session.save(saved) } is EditResult.Ok)
        }
        val xml = java.util.zip.ZipFile(saved).use { z -> z.getInputStream(z.getEntry("word/document.xml")).readBytes().toString(Charsets.UTF_8) }
        val drawing = xml.indexOf("<w:drawing>")
        assertTrue("saved before 'Hết tài liệu'", drawing in 0 until xml.indexOf("Hết tài liệu"))
    }

    /** sample.docx's second table moved live to the top; text typed in one of its cells after that is saved in that cell. */
    @Test
    fun moveTableLive() {
        val source = OpenDocument.copySample("sample.docx", "docx_live_table_source.docx")
        val saved = OpenDocument.output("docx_live_table_saved.docx")
        OpenDocument.open(source, { it.layout != null }) { reader ->
            val session = onMain { LiveDocxSession(reader.control!!, source) }
            val doc = onMain { (reader.control!!.getView() as Word).getDocument() as com.wxiwei.office.wp.model.WPDocument }
            val (start, end) = onMain { doc.getTableCollection(0)!!.getElementForIndex(1)!!.let { it.getStartOffset() to it.getEndOffset() } }
            val tableText = onMain { doc.getText(start, end) }
            val firstCell = tableText.substringBefore('\n')
            android.util.Log.i("MoveDbg", "table [$start,$end) first cell '$firstCell'")
            assertTrue(onMain { session.moveTable(start + 2, 0, after = false) })
            assertFalse("shown live", onMain { session.needsReopen })
            assertEquals(emptyList<String>(), onMain { modelProblems(doc) })
            assertEquals("table text now first", tableText, onMain { doc.getText(0, end - start) })
            assertEquals(0L until (end - start), onMain { session.movedTable })
            delay(800)
            assertTrue("laid out as a table", onMain {
                val w = reader.control!!.getView() as Word
                w.getRoot(w.getCurrentRootType())!!.getView(0, com.wxiwei.office.constant.wp.WPViewConstant.TABLE_VIEW.toInt(), false) != null
            })
            // undo and redo
            assertTrue(onMain { session.undo() })
            assertEquals(emptyList<String>(), onMain { modelProblems(doc) })
            assertEquals(tableText, onMain { doc.getText(start, end) })
            assertTrue(onMain { session.redo() })
            assertEquals(tableText, onMain { doc.getText(0, end - start) })
            // type at the start of its first cell
            assertTrue(session.lastError?.toString(), onMain { session.insertText(0, "MỚI ") })
            assertTrue(onMain { session.save(saved) } is EditResult.Ok)
        }
        val xml = java.util.zip.ZipFile(saved).use { z -> z.getInputStream(z.getEntry("word/document.xml")).readBytes().toString(Charsets.UTF_8) }
        val body = xml.indexOf("<w:body>") + "<w:body>".length
        assertTrue("table first in the body", xml.startsWith("<w:tbl>", body))
        val plain = Regex("<w:t(?: [^>]*)?>([^<]*)</w:t>").findAll(xml.substring(body, xml.indexOf("</w:tbl>", body))).joinToString("") { it.groupValues[1] }
        assertTrue("typed text in the moved table's first cell: '${plain.take(40)}'", plain.startsWith("MỚI "))
    }

    /** The first table of sample.docx: its first column border moved right, its second row made taller; shown live, undone, saved. */
    @Test
    fun resizeTableColumnAndRow() {
        val source = OpenDocument.copySample("sample.docx", "docx_table_size_source.docx")
        val saved = OpenDocument.output("docx_table_size_saved.docx")
        val xml0 = java.util.zip.ZipFile(source).use { z -> z.getInputStream(z.getEntry("word/document.xml")).readBytes().toString(Charsets.UTF_8) }
        val grid0 = Regex("<w:gridCol w:w=\"(\\d+)\"").findAll(xml0.substring(xml0.indexOf("<w:tbl>"))).take(2).map { it.groupValues[1].toInt() }.toList()
        OpenDocument.open(source, { it.layout != null }) { reader ->
            val session = onMain { LiveDocxSession(reader.control!!, source) }
            val doc = onMain { (reader.control!!.getView() as Word).getDocument() as com.wxiwei.office.wp.model.WPDocument }
            val table = onMain { doc.getTableCollection(0)!!.getElementForIndex(0) as com.wxiwei.office.wp.model.TableElement }
            fun width(row: Int, cell: Int) = onMain { AttrManage.instance().getTableCellWidth((table.getElementForIndex(row) as com.wxiwei.office.wp.model.RowElement).getElementForIndex(cell)!!.getAttribute()) }
            val w0 = width(0, 0); val w1 = width(0, 1)
            android.util.Log.i("MoveDbg", "grid $grid0 cells $w0 $w1")
            assertEquals(grid0[0], w0)
            // column border 1 by +600 twips
            assertEquals(600, onMain { session.resizeTableColumn(table.getStartOffset(), 1, 600) })
            assertFalse("shown live", onMain { session.needsReopen })
            assertEquals(w0 + 600, width(0, 0)); assertEquals(w1 - 600, width(0, 1))
            assertEquals(w0 + 600, width(2, 0))
            // row 2 at least 1200 twips
            val row = onMain { table.getElementForIndex(1)!! }
            assertTrue(onMain { session.setTableRowHeight(row.getStartOffset(), 1200) })
            assertEquals(1200, onMain { AttrManage.instance().getTableRowHeight(row.getAttribute()) })
            delay(500)
            val rowPx = onMain {
                val w = reader.control!!.getView() as Word
                val g = com.wxiwei.office.editor.word.WordSelection(w).tableGuides(table.getStartOffset(), table.getEndOffset())
                g?.rows?.getOrNull(1)?.let { (it.bottom - it.top) / w.getZoom() }
            }
            android.util.Log.i("MoveDbg", "row 2 laid out $rowPx px")
            assertTrue("row laid out taller: $rowPx", rowPx != null && rowPx >= 1200 / 15f - 2)
            // undo both, redo both
            assertTrue(onMain { session.undo() }); assertTrue(onMain { session.undo() })
            // sample.docx gives the row no height of its own
            assertEquals(w0, width(0, 0)); assertEquals(0, onMain { AttrManage.instance().getTableRowHeight(row.getAttribute()) })
            assertTrue(onMain { session.redo() }); assertTrue(onMain { session.redo() })
            assertEquals(w0 + 600, width(0, 0))
            assertTrue(onMain { session.save(saved) } is EditResult.Ok)
        }
        val xml = java.util.zip.ZipFile(saved).use { z -> z.getInputStream(z.getEntry("word/document.xml")).readBytes().toString(Charsets.UTF_8) }
        val tbl = xml.substring(xml.indexOf("<w:tbl>"), xml.indexOf("</w:tbl>"))
        val grid = Regex("<w:gridCol w:w=\"(\\d+)\"").findAll(tbl).take(2).map { it.groupValues[1].toInt() }.toList()
        assertEquals(listOf(grid0[0] + 600, grid0[1] - 600), grid)
        assertTrue("tcW of the first cell", Regex("<w:tcW w:w=\"${grid0[0] + 600}\" w:type=\"dxa\"").containsMatchIn(tbl) || Regex("<w:tcW w:type=\"dxa\" w:w=\"${grid0[0] + 600}\"").containsMatchIn(tbl))
        val rows = tbl.split("<w:tr>", "<w:tr ").drop(1)
        assertTrue("trHeight on row 2: ${rows[1].take(200)}", rows[1].contains("w:val=\"1200\""))
    }

    /** A touch in the middle of a table cell's text gives an offset in that cell (not the row below). */
    @Test
    fun tapInTableCell() {
        val source = OpenDocument.copySample("sample.docx", "docx_cell_tap.docx")
        OpenDocument.open(source, { it.layout != null }) { reader ->
            delay(1500)
            val bad = onMain {
                val w = reader.control!!.getView() as Word
                val doc = w.getDocument()
                val text = doc.getText(0, (doc as com.wxiwei.office.wp.model.WPDocument).getAreaEnd(0))
                val sel = com.wxiwei.office.editor.word.WordSelection(w)
                val out = ArrayList<String>()
                for (needle in listOf("WATCH", "PLAY_ALONG", "CHALLENGE", "Toàn bộ nốt", "Thử thách")) {
                    val at = text.indexOf(needle).toLong()
                    val r = sel.rectsFor(at, at + 1).first()
                    val got = sel.offsetAt(r.exactCenterX(), r.exactCenterY())
                    if (got !in at..(at + needle.length)) out.add("$needle: $at -> $got")
                }
                out
            }
            assertEquals(emptyList<String>(), bad)
        }
    }

    /** sample.docx's second table (3 columns, 4 rows): a row added below "WATCH" and a column right of it; saved, read again. */
    @Test
    fun insertTableRowAndColumn() {
        val source = OpenDocument.copySample("sample.docx", "docx_table_insert_source.docx")
        val saved = OpenDocument.output("docx_table_insert_saved.docx")
        OpenDocument.open(source, { it.layout != null }) { reader ->
            val session = onMain { LiveDocxSession(reader.control!!, source) }
            val at = offsetOf(source.absolutePath, "WATCH").toLong()
            val place = onMain { session.cellAt(at) }!!
            assertEquals(listOf(1, 1, 0, 1), listOf(place.table, place.row, place.cell, place.span))
            assertTrue(session.lastError?.toString(), onMain { session.insertTableRow(at, below = true) })
            assertTrue(session.lastError?.toString(), onMain { session.insertTableColumn(at, right = true) })
            assertFalse("shown live (no merged cells)", onMain { session.needsReopen })
            assertTrue(onMain { session.save(saved) } is EditResult.Ok)
        }
        val xml = java.util.zip.ZipFile(saved).use { z -> z.getInputStream(z.getEntry("word/document.xml")).readBytes().toString(Charsets.UTF_8) }
        val first = xml.indexOf("<w:tbl>")
        val start = xml.indexOf("<w:tbl>", first + 1)
        val tbl = xml.substring(start, xml.indexOf("</w:tbl>", start))
        assertEquals("4 grid columns", 4, Regex("<w:gridCol ").findAll(tbl).count())
        val xml0 = java.util.zip.ZipFile(source).use { z -> z.getInputStream(z.getEntry("word/document.xml")).readBytes().toString(Charsets.UTF_8) }
        val tbl0 = xml0.substring(xml0.indexOf("<w:tbl>", xml0.indexOf("<w:tbl>") + 1))
        fun gridSum(t: String) = Regex("<w:gridCol w:w=\"(\\d+)\"").findAll(t.substring(0, t.indexOf("</w:tblGrid>"))).sumOf { it.groupValues[1].toInt() }
        assertEquals("the table keeps its width", gridSum(tbl0), gridSum(tbl))
        val rows = tbl.split(Regex("<w:tr[ >]")).drop(1)
        assertEquals("5 rows", 5, rows.size)
        rows.forEachIndexed { i, r -> assertEquals("cells in row $i", 4, Regex("<w:tc>").findAll(r).count()) }
        fun texts(r: String) = Regex("<w:tc>.*?</w:tc>", RegexOption.DOT_MATCHES_ALL).findAll(r).map { c -> Regex("<w:t(?: [^>]*)?>([^<]*)</w:t>").findAll(c.value).joinToString("") { it.groupValues[1] } }.toList()
        assertEquals("row of WATCH: new empty cell after it", listOf("WATCH", ""), texts(rows[1]).take(2))
        assertTrue("new row empty: ${texts(rows[2])}", texts(rows[2]).all { it.isEmpty() })
        // the view reads it back: 5 rows of 4 cells
        OpenDocument.open(saved, { it.layout != null }) { reader ->
            val shape = onMain {
                val t = ((reader.control!!.getView() as Word).getDocument() as com.wxiwei.office.wp.model.WPDocument).getTableCollection(0)!!.getElementForIndex(1) as com.wxiwei.office.wp.model.TableElement
                (0 until t.rowCount()).map { (t.getElementForIndex(it) as com.wxiwei.office.wp.model.RowElement).getCellNumber() }
            }
            assertEquals(listOf(4, 4, 4, 4, 4), shape)
        }
    }

    /** sample.docx's second table (3 columns, 4 rows): the "PLAY_ALONG" row and the "Mô tả" column removed; saved, read again. */
    @Test
    fun deleteTableRowAndColumn() {
        val source = OpenDocument.copySample("sample.docx", "docx_table_delete_source.docx")
        val saved = OpenDocument.output("docx_table_delete_saved.docx")
        OpenDocument.open(source, { it.layout != null }) { reader ->
            val session = onMain { LiveDocxSession(reader.control!!, source) }
            assertTrue(session.lastError?.toString(), onMain { session.deleteTableRow(offsetOf(source.absolutePath, "PLAY_ALONG")) })
            assertTrue(session.lastError?.toString(), onMain { session.deleteTableColumn(offsetOf(source.absolutePath, "Mô tả")) })
            assertTrue(onMain { session.save(saved) } is EditResult.Ok)
        }
        val xml0 = java.util.zip.ZipFile(source).use { z -> z.getInputStream(z.getEntry("word/document.xml")).readBytes().toString(Charsets.UTF_8) }
        val xml = java.util.zip.ZipFile(saved).use { z -> z.getInputStream(z.getEntry("word/document.xml")).readBytes().toString(Charsets.UTF_8) }
        fun second(x: String): String { val a = x.indexOf("<w:tbl>", x.indexOf("<w:tbl>") + 1); return x.substring(a, x.indexOf("</w:tbl>", a)) }
        fun gridSum(t: String) = Regex("<w:gridCol w:w=\"(\\d+)\"").findAll(t).sumOf { it.groupValues[1].toInt() }
        val tbl = second(xml)
        assertEquals("2 grid columns", 2, Regex("<w:gridCol ").findAll(tbl).count())
        assertEquals("the table keeps its width", gridSum(second(xml0)), gridSum(tbl))
        fun texts(r: String) = Regex("<w:tc>.*?</w:tc>", RegexOption.DOT_MATCHES_ALL).findAll(r).map { c -> Regex("<w:t(?: [^>]*)?>([^<]*)</w:t>").findAll(c.value).joinToString("") { it.groupValues[1] } }.toList()
        val rows = tbl.split(Regex("<w:tr[ >]")).drop(1).map { texts(it) }
        assertEquals(listOf(listOf("Chế độ", "Dùng khi"), listOf("WATCH", "Demo bài, học giai điệu lần đầu"), listOf("CHALLENGE", "Thử thách, thi đấu điểm")), rows)
        OpenDocument.open(saved, { it.layout != null }) { reader ->
            val shape = onMain {
                val t = ((reader.control!!.getView() as Word).getDocument() as com.wxiwei.office.wp.model.WPDocument).getTableCollection(0)!!.getElementForIndex(1) as com.wxiwei.office.wp.model.TableElement
                (0 until t.rowCount()).map { (t.getElementForIndex(it) as com.wxiwei.office.wp.model.RowElement).getCellNumber() }
            }
            assertEquals(listOf(2, 2, 2), shape)
        }
    }

    /** D10: a word shaded 92D050 (written as w:shd, not a named highlight) keeps its color when read back. */
    @Test
    fun shadingReadBack() {
        val source = OpenDocument.copySample("sample.docx", "docx_shd_source.docx")
        val saved = OpenDocument.output("docx_shd_saved.docx")
        var at = 0L
        OpenDocument.open(source, { it.layout != null }) { reader ->
            at = offsetOf(source.absolutePath, "PianoLearn").toLong()
            val session = onMain { LiveDocxSession(reader.control!!, source) }
            assertTrue(session.lastError?.toString(), onMain { session.highlight(at, at + 10, "92D050") })
            assertTrue(onMain { session.save(saved) } is EditResult.Ok)
        }
        val xml = java.util.zip.ZipFile(saved).use { z -> z.getInputStream(z.getEntry("word/document.xml")).readBytes().toString(Charsets.UTF_8) }
        assertTrue("written as shading", xml.contains("w:fill=\"92D050\""))
        OpenDocument.open(saved, { it.layout != null }) { reader ->
            val color = onMain {
                val doc = (reader.control!!.getView() as Word).getDocument()
                AttrManage.instance().getFontHighLight(doc.getParagraph(at + 1)!!.getAttribute(), doc.getLeaf(at + 1)!!.getAttribute())
            }
            assertEquals("read back", 0xFF92D050.toInt(), color)
        }
    }
}
