package com.wxiwei.office.editor.xlsx

import com.wxiwei.office.editor.EditResult
import com.wxiwei.office.editor.ooxml.OoxmlPackage
import com.wxiwei.office.editor.ooxml.SS
import com.wxiwei.office.editor.ooxml.childrenNamed
import com.wxiwei.office.editor.ooxml.firstChild
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.ss.model.baseModel.Cell
import com.wxiwei.office.ss.model.baseModel.Row
import com.wxiwei.office.ss.model.baseModel.Sheet
import com.wxiwei.office.ss.model.baseModel.Workbook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class XlsxEditTest {
    @get:Rule val temp = TemporaryFolder()

    private fun book(): Workbook {
        val book = Workbook(false)
        for ((i, name) in listOf("S1", "S2").withIndex()) {
            val sheet = Sheet(); sheet.setWorkbook(book); sheet.setSheetName(name); sheet.setState(Sheet.State_Accomplished)
            book.addSheet(i, sheet)
        }
        return book
    }

    private fun session(book: Workbook, source: File = temp.newFile("x.xlsx")) = SheetEditSession(book, source) {}
    private fun num(book: Workbook, s: Int, ref: String): Double {
        val (r, c) = rc(ref); return book.getSheet(s)!!.getRow(r)!!.getCell(c)!!.getNumberValue()
    }
    private fun rc(ref: String) = (ref.dropWhile { it.isLetter() }.toInt() - 1) to (ref.takeWhile { it.isLetter() }.fold(0) { n, ch -> n * 26 + (ch - 'A' + 1) } - 1)
    private fun set(s: SheetEditSession, sheet: Int, ref: String, input: String) { val (r, c) = rc(ref); val ok = s.setCellInput(sheet, r, c, input); assertTrue("$ref=$input: ${s.lastError}", ok) }

    @Test fun shifterMovesRelativeRefsOnly() {
        assertEquals("SUM(B2:B4)+\$A\$1+C\$1+S2!B2+\"A1\"", A1FormulaShifter.shift("SUM(A1:A3)+\$A\$1+B\$1+S2!A1+\"A1\"", 1, 1))
        assertEquals("#REF!+1", A1FormulaShifter.shift("A1+1", -1, 0))
        assertEquals("LOG10(A2)", A1FormulaShifter.shift("LOG10(A1)", 1, 0))
    }

    @Test fun formulasCalculateAndDependentsFollowEdits() {
        val book = book(); val s = session(book)
        set(s, 0, "A1", "1"); set(s, 0, "A2", "2"); set(s, 0, "A3", "3")
        set(s, 0, "A4", "=SUM(A1:A3)"); assertEquals(6.0, num(book, 0, "A4"), 0.0)
        set(s, 0, "B1", "=AVERAGE(A1:A3)*2"); assertEquals(4.0, num(book, 0, "B1"), 0.0)
        set(s, 1, "A1", "10"); set(s, 0, "C1", "=S2!A1+A4")
        assertEquals(16.0, num(book, 0, "C1"), 0.0)
        set(s, 0, "D1", "=IF(A1>0,\"pos\",\"neg\")&\"!\"")
        assertEquals("pos!", s.engine.adapter.stringOf(book.getSheet(0)!!.getRow(0)!!.getCell(3)!!))
        set(s, 0, "E1", "=A1/0"); assertEquals(Cell.CELL_TYPE_ERROR, book.getSheet(0)!!.getRow(0)!!.getCell(4)!!.getCellType())
        // Change an input: every dependent follows, across sheets
        set(s, 0, "A1", "4")
        assertEquals(9.0, num(book, 0, "A4"), 0.0); assertEquals(6.0, num(book, 0, "B1"), 0.0); assertEquals(19.0, num(book, 0, "C1"), 0.0)
        set(s, 1, "A1", "20"); assertEquals(29.0, num(book, 0, "C1"), 0.0)
        assertEquals("=SUM(A1:A3)", s.getInput(0, 3, 0))
    }

    @Test fun badFormulaIsRejectedAndUndoRedoRestoreValues() {
        val book = book(); val s = session(book)
        set(s, 0, "A1", "5"); set(s, 0, "B1", "=A1*2")
        assertFalse(s.setCellInput(0, 0, 2, "=SUM(A1:"))
        assertTrue(s.lastError!!.message.startsWith("Formula error"))
        set(s, 0, "A1", "7"); assertEquals(14.0, num(book, 0, "B1"), 0.0)
        assertTrue(s.undo()); assertEquals(10.0, num(book, 0, "B1"), 0.0)
        assertTrue(s.redo()); assertEquals(14.0, num(book, 0, "B1"), 0.0)
        set(s, 0, "B1", "text"); assertNull(book.getSheet(0)!!.getRow(0)!!.getCell(1)!!.formula)
        assertTrue(s.undo()); assertEquals("=A1*2", s.getInput(0, 0, 1))
    }

    @Test fun textFunctionFormatsDates() {
        val book = book(); val s = session(book)
        set(s, 0, "A1", "46174") // 2026-06-01
        for ((fmt, want) in listOf("yyyy-mm" to "2026-06", "dd/mm/yyyy" to "01/06/2026", "0.00" to "46174.00", "#,##0" to "46,174")) {
            set(s, 0, "B1", "=TEXT(A1,\"$fmt\")")
            println("TEXT $fmt -> '${s.engine.adapter.stringOf(book.getSheet(0)!!.getRow(0)!!.getCell(1)!!)}'")
            assertEquals(fmt, want, s.engine.adapter.stringOf(book.getSheet(0)!!.getRow(0)!!.getCell(1)!!))
        }
    }

    private fun xlsx(): File {
        val ns = SS.uri
        val rel = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"
        val entries = linkedMapOf(
            "[Content_Types].xml" to """<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/calcChain.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.calcChain+xml"/></Types>""",
            "xl/workbook.xml" to """<workbook xmlns="$ns" xmlns:r="$rel"><sheets><sheet name="S1" sheetId="1" r:id="rId1"/></sheets></workbook>""",
            "xl/_rels/workbook.xml.rels" to """<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="$rel/worksheet" Target="worksheets/sheet1.xml"/><Relationship Id="rId9" Type="$rel/calcChain" Target="calcChain.xml"/></Relationships>""",
            "xl/calcChain.xml" to """<calcChain xmlns="$ns"><c r="B1" i="1"/></calcChain>""",
            "xl/worksheets/sheet1.xml" to """<worksheet xmlns="$ns"><dimension ref="A1:C2"/><sheetData>""" +
                """<row r="1" spans="1:3"><c r="A1"><v>1</v></c><c r="B1"><f t="shared" ref="B1:B2" si="0">A1*2</f><v>2</v></c><c r="C1" s="3" t="s"><v>0</v></c></row>""" +
                """<row r="2"><c r="A2"><v>5</v></c><c r="B2"><f t="shared" si="0"/><v>10</v></c></row></sheetData></worksheet>""")
        val f = temp.newFile("book-${System.nanoTime()}.xlsx")
        ZipOutputStream(f.outputStream()).use { z -> entries.forEach { (n, x) -> z.putNextEntry(ZipEntry(n)); z.write(x.toByteArray()); z.closeEntry() } }
        return f
    }

    private fun cells(pkg: OoxmlPackage): Map<String, Element> = pkg.xml("xl/worksheets/sheet1.xml").rootElement.firstChild(SS, "sheetData")!!
        .childrenNamed(SS, "row").flatMap { it.childrenNamed(SS, "c") }.associateBy { it.attributeValue("r") }

    @Test fun writerPatchesCellsKeepsStylesAndDropsCalcChain() {
        val src = xlsx(); val out = File(temp.root, "out.xlsx")
        val result = XlsxWriter(src) { _, r, c -> if (r == 1 && c == 1) "A2*2" else null }.save(out, listOf(
            CellWrite.Text(0, 0, 2, " hi "),                    // replace a shared-string cell, keep s=3
            CellWrite.Number(0, 0, 1, 3.0, "A1*3"),             // overwrite the shared formula master
            CellWrite.Number(0, 4, 3, 42.0, "SUM(A1:A2)")))     // new row 5 + new cell D5
        assertTrue(result.toString(), result is EditResult.Ok)
        val pkg = OoxmlPackage.open(out); val c = cells(pkg)
        assertEquals("3", c["C1"]!!.attributeValue("s")); assertEquals("inlineStr", c["C1"]!!.attributeValue("t"))
        assertEquals(" hi ", c["C1"]!!.firstChild(SS, "is")!!.firstChild(SS, "t")!!.text)
        assertEquals("A1*3", c["B1"]!!.firstChild(SS, "f")!!.text); assertNull(c["B1"]!!.firstChild(SS, "f")!!.attributeValue("t"))
        assertEquals("A2*2", c["B2"]!!.firstChild(SS, "f")!!.text) // dependent expanded to an explicit formula
        assertEquals("SUM(A1:A2)", c["D5"]!!.firstChild(SS, "f")!!.text); assertEquals("42", c["D5"]!!.firstChild(SS, "v")!!.text)
        val sheet = pkg.xml("xl/worksheets/sheet1.xml").rootElement
        assertEquals(listOf("1", "2", "5"), sheet.firstChild(SS, "sheetData")!!.childrenNamed(SS, "row").map { it.attributeValue("r") })
        assertEquals("A1:D5", sheet.firstChild(SS, "dimension")!!.attributeValue("ref"))
        assertFalse(pkg.has("xl/calcChain.xml"))
        assertTrue(pkg.relationships("xl/workbook.xml").none { it.type.endsWith("/calcChain") })
        assertEquals("1", pkg.xml("xl/workbook.xml").rootElement.firstChild(SS, "calcPr")!!.attributeValue("fullCalcOnLoad"))
        assertFalse(String(pkg.bytes("[Content_Types].xml")!!).contains("calcChain"))
    }
}
