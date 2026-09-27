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
}
