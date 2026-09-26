package com.wxiwei.office.editor

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wxiwei.office.editor.OpenDocument.onMain
import com.wxiwei.office.editor.xlsx.SheetEditSession
import com.wxiwei.office.ss.control.ExcelView
import com.wxiwei.office.ss.model.baseModel.Workbook
import kotlinx.coroutines.delay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * sample.xlsx: "Tổng quan"!D6 = SUM('Dữ liệu chi tiết'!G2:G74) = 279. Editing G2 must update D6
 * at once, and the saved file must reopen with both values.
 */
@RunWith(AndroidJUnit4::class)
class SheetEditSessionTest {
    private fun book(reader: com.wxiwei.office.reader.OfficeReader): Workbook =
        (reader.control!!.getView() as ExcelView).getSpreadsheet()!!.getWorkbook()!!

    private fun num(book: Workbook, sheet: Int, row: Int, col: Int): Double {
        val s = book.getSheet(sheet) ?: error("no sheet $sheet")
        val r = s.getRow(row) ?: error("sheet $sheet '${s.getSheetName()}' state=${s.getState()} rows=${s.getFirstRowNum()}..${s.getLastRowNum()}: no row $row")
        val c = r.getCell(col) ?: error("no cell $row,$col")
        return c.getNumberValue()
    }

    /** Sheets load when first shown: show each once so its rows exist. */
    private suspend fun loadAll(reader: com.wxiwei.office.reader.OfficeReader) {
        val excel = reader.control!!.getView() as ExcelView
        val n = onMain { excel.getSpreadsheet()!!.getSheetCount() }
        for (i in 0 until n) {
            onMain { excel.showSheet(i) }
            delay(1500)
        }
        onMain { excel.showSheet(0) }
    }

    @Test
    fun editRecalculatesAndSaves() {
        val source = OpenDocument.copySample("sample.xlsx", "edit_source.xlsx")
        val saved = OpenDocument.output("edit_saved.xlsx")
        var g2Before = 0.0
        OpenDocument.open(source) { reader ->
            loadAll(reader)
            val book = book(reader)
            g2Before = onMain { num(book, 1, 1, 6) }
            assertEquals(279.0, onMain { num(book, 0, 5, 3) }, 0.0)
            val session = onMain { SheetEditSession(reader.control!!, source) }
            val ok = onMain { session.setCellInput(1, 1, 6, (g2Before + 10).toLong().toString()) }
            assertTrue(session.lastError?.toString(), ok)
            assertEquals("SUM follows the edit", 289.0, onMain { num(book, 0, 5, 3) }, 0.0)
            // undo / redo
            assertTrue(onMain { session.undo() })
            assertEquals(279.0, onMain { num(book, 0, 5, 3) }, 0.0)
            assertTrue(onMain { session.redo() })
            assertEquals(289.0, onMain { num(book, 0, 5, 3) }, 0.0)
            // a formula typed into a cell
            assertTrue(onMain { session.setCellInput(3, 20, 1, "=SUM(1,2,3)") })
            assertEquals(6.0, onMain { num(book, 3, 20, 1) }, 0.0)
            // format: sheet 4 ("Ghi chú dữ liệu") C6 shares its style with the rest of the table body
            val otherStyleBefore = onMain { book.getSheet(3)!!.getRow(6)!!.getCell(2)!!.getCellStyleIndex() }
            val fmt = com.wxiwei.office.editor.xlsx.CellFormat(bold = true, fillColor = "FFFF00", horizontal = "center", numberFormat = "0.00")
            assertTrue(onMain { session.setCellFormat(3, 20, 1, fmt) })
            onMain {
                val st = book.getSheet(3)!!.getRow(20)!!.getCell(1)!!.getCellStyle()!!
                assertTrue("bold", book.getFont(st.getFontIndex().toInt())!!.isBold())
                assertEquals(0xFFFFFF00.toInt(), st.getFgColor())
                assertEquals(com.wxiwei.office.ss.model.style.CellStyle.ALIGN_CENTER, st.getHorizontalAlign())
                assertEquals("0.00", st.getFormatCode())
            }
            assertTrue(onMain { session.setRangeFormat(3, 5, 1, 6, 2, com.wxiwei.office.editor.xlsx.CellFormat(italic = true)) })
            assertTrue(onMain { session.undo() })
            assertEquals("undo restores the style", otherStyleBefore, onMain { book.getSheet(3)!!.getRow(6)!!.getCell(2)!!.getCellStyleIndex() })
            val result = onMain { session.save(saved) }
            assertTrue(result.toString(), result is EditResult.Ok)
        }
        OpenDocument.open(saved) { reader ->
            loadAll(reader)
            val book = book(reader)
            assertEquals(g2Before + 10, onMain { num(book, 1, 1, 6) }, 0.0)
            assertEquals(289.0, onMain { num(book, 0, 5, 3) }, 0.0)
            assertEquals(6.0, onMain { num(book, 3, 20, 1) }, 0.0)
            assertEquals("SUM(1,2,3)", onMain { book.getSheet(3)!!.getRow(20)!!.getCell(1)!!.formula })
            onMain {
                val st = book.getSheet(3)!!.getRow(20)!!.getCell(1)!!.getCellStyle()!!
                assertTrue("bold saved", book.getFont(st.getFontIndex().toInt())!!.isBold())
                assertEquals(0xFFFFFF00.toInt(), st.getFgColor())
                assertEquals(com.wxiwei.office.ss.model.style.CellStyle.ALIGN_CENTER, st.getHorizontalAlign())
                assertEquals("0.00", st.getFormatCode())
                val other = book.getSheet(3)!!.getRow(6)!!.getCell(2)!!.getCellStyle()!!
                assertTrue("untouched cell keeps its font", !book.getFont(other.getFontIndex().toInt())!!.isItalic())
            }
        }
    }
}
