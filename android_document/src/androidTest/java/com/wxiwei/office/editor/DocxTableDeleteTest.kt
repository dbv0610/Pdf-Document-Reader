/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.wxiwei.office.editor

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wxiwei.office.editor.OpenDocument.onMain
import com.wxiwei.office.editor.docx.LiveDocxSession
import com.wxiwei.office.wp.control.Word
import com.wxiwei.office.wp.model.RowElement
import com.wxiwei.office.wp.model.TableElement
import com.wxiwei.office.wp.model.WPDocument
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Rows and columns of a Word table removed live in several orders, then saved and read again.
 * sample.docx's second table: header "Chế độ | Mô tả | Dùng khi", then rows WATCH, PLAY_ALONG, CHALLENGE.
 */
@RunWith(AndroidJUnit4::class)
class DocxTableDeleteTest {
    private val T = 1 // the second body table

    private fun ok(session: LiveDocxSession, done: Boolean) = assertTrue(session.lastError?.toString(), done)
    private fun LiveDocxSession.row(r: Int) = ok(this, onMain { deleteTableRow(cellStart(T, r, 0)) })
    private fun LiveDocxSession.col(r: Int, c: Int) = ok(this, onMain { deleteTableColumn(cellStart(T, r, c)) })

    /** First-cell text of each row and the cell count of each row, from the saved file read again. */
    private fun edit(name: String, table: Int = T, source: File = OpenDocument.copySample("sample.docx", "docx_tdel_${name}_source.docx"),
                     block: LiveDocxSession.() -> Unit): Pair<List<String>, List<Int>> {
        val saved = OpenDocument.output("docx_tdel_${name}_saved.docx")
        OpenDocument.open(source, { it.layout != null }) { reader ->
            val session = onMain { LiveDocxSession(reader.control!!, source) }
            session.block()
            val result = onMain { session.save(saved) }
            assertTrue(result.toString(), result is EditResult.Ok)
        }
        var out: Pair<List<String>, List<Int>>? = null
        OpenDocument.open(saved, { it.layout != null }) { reader ->
            out = onMain {
                val doc = (reader.control!!.getView() as Word).getDocument() as WPDocument
                val t = doc.getTableCollection(0)!!.getElementForIndex(table) as TableElement
                val rows = (0 until t.rowCount()).map { t.getElementForIndex(it) as RowElement }
                rows.map { r -> r.getElementForIndex(0)!!.let { c -> doc.getText(c.getStartOffset(), c.getEndOffset() - 1) } } to rows.map { it.getCellNumber() }
            }
        }
        return out!!
    }

    @Test fun firstRowTwice() {
        val (first, cells) = edit("first2") { row(0); row(0) }
        assertEquals(listOf("PLAY_ALONG", "CHALLENGE"), first)
        assertEquals(listOf(3, 3), cells)
    }

    @Test fun lastRowTwice() {
        val (first, _) = edit("last2") { row(3); row(2) }
        assertEquals(listOf("Chế độ", "WATCH"), first)
    }

    @Test fun rowsAboveThenBelow() {
        val (first, _) = edit("updown") { row(1); row(2) }
        assertEquals(listOf("Chế độ", "PLAY_ALONG"), first)
    }

    @Test fun rowThenColumnInPlace() {
        val (first, cells) = edit("rowcol") { row(1); col(1, 0) }
        assertEquals(listOf("Mô tả", "Một tay (autoHand) máy tự chơi, tay kia người dùng đánh theo & chấm điểm.", "Người dùng đánh TẤT CẢ nốt, chấm điểm đầy đủ."), first)
        assertEquals(listOf(2, 2, 2), cells)
    }

    @Test fun columnThenRowInPlace() {
        val (first, cells) = edit("colrow") { col(1, 0); row(1) }
        assertEquals(3, first.size)
        assertEquals("Mô tả", first[0])
        assertEquals(listOf(2, 2, 2), cells)
    }

    @Test fun lastColumnTwice() {
        val (first, cells) = edit("lastcol2") { col(0, 2); col(0, 1) }
        assertEquals(listOf("Chế độ", "WATCH", "PLAY_ALONG", "CHALLENGE"), first)
        assertEquals(listOf(1, 1, 1, 1), cells)
    }

    @Test fun undoRedo() {
        val (first, _) = edit("undo") {
            row(1); row(1)
            ok(this, onMain { undo() }); ok(this, onMain { undo() }); ok(this, onMain { redo() })
            col(0, 0); ok(this, onMain { undo() })
        }
        assertEquals(listOf("Chế độ", "PLAY_ALONG", "CHALLENGE"), first)
    }

    @Test fun insertedRowThenDeleteBelow() {
        val (first, cells) = edit("insdel") {
            ok(this, onMain { insertTableRow(cellStart(T, 0, 0), below = true) })
            row(2); row(2)
        }
        assertEquals(listOf("Chế độ", "", "CHALLENGE"), first)
        assertEquals(listOf(3, 3, 3), cells)
    }

    @Test fun insertRowWhereOneWasDeleted() {
        val (first, _) = edit("insrow") { row(1); ok(this, onMain { insertTableRow(cellStart(T, 1, 0), below = false) }) }
        assertEquals(listOf("Chế độ", "", "PLAY_ALONG", "CHALLENGE"), first)
    }

    @Test fun insertColumnWhereOneWasDeleted() {
        val (first, cells) = edit("inscol") { col(0, 0); ok(this, onMain { insertTableColumn(cellStart(T, 0, 0), right = false) }) }
        assertEquals(listOf("", "", "", ""), first)
        assertEquals(listOf(3, 3, 3, 3), cells)
    }

    @Test fun rowHeightWhereOneWasDeleted() {
        val saved = OpenDocument.output("docx_tdel_height_saved.docx")
        val source = OpenDocument.copySample("sample.docx", "docx_tdel_height_source.docx")
        OpenDocument.open(source, { it.layout != null }) { reader ->
            val session = onMain { LiveDocxSession(reader.control!!, source) }
            session.row(1)
            ok(session, onMain { session.setTableRowHeight(session.cellStart(T, 1, 0), 1234) })
            assertTrue(onMain { session.save(saved) } is EditResult.Ok)
        }
        val xml = java.util.zip.ZipFile(saved).use { z -> z.getInputStream(z.getEntry("word/document.xml")).readBytes().toString(Charsets.UTF_8) }
        val a = xml.indexOf("<w:tbl>", xml.indexOf("<w:tbl>") + 1)
        val rows = xml.substring(a, xml.indexOf("</w:tbl>", a)).split(Regex("<w:tr[ >]")).drop(1)
        assertTrue("PLAY_ALONG row is 1234 high", rows[1].contains("PLAY_ALONG") && rows[1].contains("w:val=\"1234\""))
    }

    /** The reported case: a new table in a blank document, saved and read again, then rows and columns removed. */
    @Test fun newTableInBlankDocument() {
        val blank = OpenDocument.output("docx_tdel_blank.docx")
        assertTrue(DocumentCreator.create(InstrumentationRegistry.getInstrumentation().targetContext, DocumentCreator.Format.WORD, blank) is EditResult.Ok)
        val withTable = OpenDocument.output("docx_tdel_table.docx")
        OpenDocument.open(blank, { it.layout != null }) { reader ->
            val session = onMain { LiveDocxSession(reader.control!!, blank) }
            ok(session, onMain { session.insertTable(0, 3, 3) })
            assertTrue(onMain { session.save(withTable) } is EditResult.Ok)
        }
        val (_, cells) = edit("blank", table = 0, source = withTable) {
            ok(this, onMain { deleteTableRow(cellStart(0, 0, 0)) })
            ok(this, onMain { deleteTableRow(cellStart(0, 0, 0)) })
            ok(this, onMain { deleteTableColumn(cellStart(0, 0, 1)) })
            ok(this, onMain { deleteTableColumn(cellStart(0, 0, 0)) })
        }
        assertEquals(listOf(1), cells)
    }

    /** Rows added below the last one as the panel does (a save and reopen before each one next to a new cell) until the table runs onto the next page. */
    @Test fun addRowsPastThePageEnd() {
        var file = OpenDocument.output("docx_tdel_grow_0.docx")
        assertTrue(DocumentCreator.create(InstrumentationRegistry.getInstrumentation().targetContext, DocumentCreator.Format.WORD, file) is EditResult.Ok)
        var pages = 0
        for (i in 0..60) {
            val next = OpenDocument.output("docx_tdel_grow_${i + 1}.docx")
            OpenDocument.open(file, { it.layout != null }) { reader ->
                val session = onMain { LiveDocxSession(reader.control!!, file) }
                if (i == 0) ok(session, onMain { session.insertTable(0, 2, 2) })
                else {
                    val rows = onMain { ((reader.control!!.getView() as Word).getDocument() as WPDocument).let { (it.getTableCollection(0)!!.getElementForIndex(0) as TableElement).rowCount() } }
                    ok(session, onMain { session.insertTableRow(session.cellStart(0, rows - 1, 0), below = true) })
                    // the second one is next to a new cell: refused until saved
                    val again = onMain { session.insertTableRow(session.cellStart(0, rows, 0), below = true) }
                    assertTrue(session.lastError?.toString(), again || session.needsFlush)
                    pages = reader.state.value.pageCount
                }
                val result = onMain { session.save(next) }
                assertTrue("step $i: $result", result is EditResult.Ok)
            }
            file = next
        }
        android.util.Log.i("TDEL", "pages $pages")
        assertTrue("the table runs past the first page", pages > 1)
    }

    /** Rows and columns added, typed in, removed and sized next to cells added just before, all without a save between. */
    @Test fun editNextToNewCellsWithoutSaving() {
        val source = OpenDocument.copySample("sample.docx", "docx_tdel_newcells_source.docx")
        val saved = OpenDocument.output("docx_tdel_newcells_saved.docx")
        var texts: List<List<String>>? = null
        OpenDocument.open(source, { it.layout != null }) { reader ->
            val s = onMain { LiveDocxSession(reader.control!!, source) }
            fun type(r: Int, c: Int, t: String) = ok(s, onMain { s.insertText(s.cellStart(T, r, c), t) })
            ok(s, onMain { s.insertTableRow(s.cellStart(T, 3, 0), below = true) })  // row 4
            ok(s, onMain { s.insertTableRow(s.cellStart(T, 4, 0), below = true) })  // row 5, next to a new one
            ok(s, onMain { s.insertTableRow(s.cellStart(T, 5, 1), below = false) }) // a new row 5, the other one is 6
            type(4, 0, "A"); type(5, 1, "B"); type(6, 2, "C")
            ok(s, onMain { s.deleteTableRow(s.cellStart(T, 5, 0)) })                // "B" goes
            ok(s, onMain { s.insertTableColumn(s.cellStart(T, 4, 0), right = true) }) // column 1, from a new cell
            type(5, 1, "D")
            ok(s, onMain { s.deleteTableColumn(s.cellStart(T, 4, 3)) })             // "Dùng khi", from a new cell: "C" goes
            val rowStart = s.cellStart(T, 4, 0)
            ok(s, onMain { s.setTableRowHeight(rowStart, 900) })
            val result = onMain { s.save(saved) }
            assertTrue(result.toString(), result is EditResult.Ok)
        }
        OpenDocument.open(saved, { it.layout != null }) { reader ->
            texts = onMain {
                val doc = (reader.control!!.getView() as Word).getDocument() as WPDocument
                val t = doc.getTableCollection(0)!!.getElementForIndex(T) as TableElement
                (0 until t.rowCount()).map { i -> (t.getElementForIndex(i) as RowElement).let { r ->
                    (0 until r.getCellNumber()).map { k -> r.getElementForIndex(k)!!.let { c -> doc.getText(c.getStartOffset(), c.getEndOffset() - 1) } } } }
            }
        }
        val rows = texts!!
        assertEquals(listOf("Chế độ", "WATCH", "PLAY_ALONG", "CHALLENGE", "A", ""), rows.map { it[0] })
        assertEquals(listOf(3, 3, 3, 3, 3, 3), rows.map { it.size })
        assertEquals(listOf("Chế độ", "", "Mô tả"), rows[0])
        assertEquals(listOf("A", "", ""), rows[4])
        assertEquals(listOf("", "D", ""), rows[5])
        val xml = java.util.zip.ZipFile(saved).use { z -> z.getInputStream(z.getEntry("word/document.xml")).readBytes().toString(Charsets.UTF_8) }
        val a = xml.indexOf("<w:tbl>", xml.indexOf("<w:tbl>") + 1)
        val trs = xml.substring(a, xml.indexOf("</w:tbl>", a)).split(Regex("<w:tr[ >]")).drop(1)
        assertTrue("the new row 4 is 900 high", trs[4].contains("w:trHeight w:val=\"900\""))
    }
}
