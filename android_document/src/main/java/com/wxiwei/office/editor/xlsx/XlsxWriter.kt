package com.wxiwei.office.editor.xlsx

import com.wxiwei.office.editor.EditResult
import com.wxiwei.office.editor.Reason
import com.wxiwei.office.editor.ooxml.OoxmlPackage
import com.wxiwei.office.editor.ooxml.R
import com.wxiwei.office.editor.ooxml.SS
import com.wxiwei.office.editor.ooxml.childrenNamed
import com.wxiwei.office.editor.ooxml.firstChild
import com.wxiwei.office.editor.runEdit
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.QName
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import java.io.File

/** What to write into one cell. A formula also carries its cached result so viewers show it without recalculating. */
sealed class CellWrite {
    abstract val sheetIndex: Int; abstract val row: Int; abstract val col: Int
    data class Number(override val sheetIndex: Int, override val row: Int, override val col: Int, val value: Double, val formula: String? = null) : CellWrite()
    data class Text(override val sheetIndex: Int, override val row: Int, override val col: Int, val value: String, val formula: String? = null) : CellWrite()
    data class Bool(override val sheetIndex: Int, override val row: Int, override val col: Int, val value: Boolean, val formula: String? = null) : CellWrite()
    data class Error(override val sheetIndex: Int, override val row: Int, override val col: Int, val code: Int, val formula: String? = null) : CellWrite()
    data class Blank(override val sheetIndex: Int, override val row: Int, override val col: Int) : CellWrite()
    val formulaText: String? get() = when (this) { is Number -> formula; is Text -> formula; is Bool -> formula; is Error -> formula; is Blank -> null }
}

/**
 * Patches the ORIGINAL .xlsx: only the written cells change, everything else (styles, drawings,
 * charts...) is kept byte for byte. Removes calcChain.xml and sets fullCalcOnLoad so Excel
 * recalculates on open. [formulaOf] gives the model formula of any cell, used to expand shared
 * formulas whose master cell is overwritten.
 */
class XlsxWriter(private val source: File, private val formulaOf: (sheetIndex: Int, row: Int, col: Int) -> String? = { _, _, _ -> null }) {

    fun save(target: File, writes: Collection<CellWrite>): EditResult {
        if (!source.extension.equals("xlsx", true) && !source.extension.equals("xlsm", true))
            return EditResult.Error(Reason.UNSUPPORTED_FORMAT, "Only .xlsx/.xlsm can be saved")
        if (source.canonicalFile == target.canonicalFile)
            return EditResult.Error(Reason.INVALID_ARGUMENT, "Save to a new file")
        return runEdit {
            val pkg = OoxmlPackage.open(source)
            val parts = sheetParts(pkg)
            for ((sheetIndex, cells) in writes.groupBy { it.sheetIndex }) {
                val part = parts.getOrNull(sheetIndex) ?: return@runEdit EditResult.Error(Reason.NOT_FOUND, "Sheet $sheetIndex not found")
                writeSheet(pkg, part, sheetIndex, cells)
            }
            dropCalcChain(pkg)
            pkg.saveTo(target)
        }
    }

    /** Worksheet part names in workbook order (same order as the viewer's sheet index). */
    private fun sheetParts(pkg: OoxmlPackage): List<String> {
        val workbook = "xl/workbook.xml"
        val rels = pkg.relationships(workbook).associateBy { it.id }
        val sheets = pkg.xml(workbook).rootElement.firstChild(SS, "sheets") ?: return emptyList()
        return sheets.childrenNamed(SS, "sheet").map { s ->
            val id = s.attributeValue(QName("id", R)) ?: ""
            rels[id]?.let { pkg.resolveTarget(workbook, it.target) } ?: ""
        }
    }

    private fun ref(row: Int, col: Int) = A1FormulaShifter.address(row, col)
    private fun rowOf(e: Element) = e.attributeValue("r")?.toIntOrNull()?.minus(1) ?: -1
    private fun colOf(ref: String?): Int {
        val letters = ref?.takeWhile { it.isLetter() }?.uppercase() ?: return -1
        return letters.fold(0) { n, c -> n * 26 + (c - 'A' + 1) } - 1
    }

    private fun writeSheet(pkg: OoxmlPackage, part: String, sheetIndex: Int, cells: List<CellWrite>) {
        val root = pkg.xml(part).rootElement
        val data = root.firstChild(SS, "sheetData") ?: error("Missing sheetData in $part")
        for (w in cells.sortedWith(compareBy({ it.row }, { it.col }))) {
            val row = rowElement(data, w.row)
            val c = cellElement(row, w.row, w.col)
            expandSharedMaster(data, sheetIndex, c)
            fill(c, w)
        }
        updateDimension(root, cells)
    }

    private fun rowElement(data: Element, row: Int): Element {
        val rows = data.childrenNamed(SS, "row")
        rows.firstOrNull { rowOf(it) == row }?.let { return it }
        val e = com.wxiwei.office.editor.ooxml.newElement(SS, "row").apply { addAttribute("r", (row + 1).toString()) }
        val next = rows.firstOrNull { rowOf(it) > row }
        val content = data.content()
        if (next == null) content.add(e) else content.add(content.indexOf(next), e)
        return e
    }

    private fun cellElement(row: Element, r: Int, col: Int): Element {
        val cells = row.childrenNamed(SS, "c")
        cells.firstOrNull { colOf(it.attributeValue("r")) == col }?.let { return it }
        val e = com.wxiwei.office.editor.ooxml.newElement(SS, "c").apply { addAttribute("r", ref(r, col)) }
        row.attributeValue("s")?.takeIf { row.attributeValue("customFormat") == "1" }?.let { e.addAttribute("s", it) }
        val next = cells.firstOrNull { colOf(it.attributeValue("r")) > col }
        val content = row.content()
        if (next == null) content.add(e) else content.add(content.indexOf(next), e)
        // A row's spans hint is optional; drop it rather than leave it wrong
        row.attribute("spans")?.let { row.remove(it) }
        return e
    }

    /** Overwriting the master of a shared formula would break its dependents: give them explicit formulas. */
    private fun expandSharedMaster(data: Element, sheetIndex: Int, c: Element) {
        val f = c.firstChild(SS, "f") ?: return
        if (f.attributeValue("t") != "shared" || f.text.isNullOrEmpty()) return
        val si = f.attributeValue("si") ?: return
        for (row in data.childrenNamed(SS, "row")) for (other in row.childrenNamed(SS, "c")) {
            if (other === c) continue
            val of = other.firstChild(SS, "f") ?: continue
            if (of.attributeValue("t") != "shared" || of.attributeValue("si") != si) continue
            val text = formulaOf(sheetIndex, rowOf(row), colOf(other.attributeValue("r"))) ?: continue
            other.remove(of)
            other.content().add(0, com.wxiwei.office.editor.ooxml.newElement(SS, "f").apply { setText(text) })
        }
    }

    private fun fill(c: Element, w: CellWrite) {
        listOf("f", "v", "is").forEach { name -> c.childrenNamed(SS, name).forEach { c.remove(it) } }
        c.attribute("t")?.let { c.remove(it) }
        c.attribute("cm")?.let { c.remove(it) }
        fun add(name: String, text: String): Element = com.wxiwei.office.editor.ooxml.newElement(SS, name).apply { setText(text) }.also { c.add(it) }
        w.formulaText?.let { add("f", it) }
        when (w) {
            is CellWrite.Number -> add("v", number(w.value))
            is CellWrite.Bool -> { c.addAttribute("t", "b"); add("v", if (w.value) "1" else "0") }
            is CellWrite.Error -> { c.addAttribute("t", "e"); add("v", ErrorEval.getText(w.code)) }
            is CellWrite.Text -> if (w.formula != null) { c.addAttribute("t", "str"); add("v", w.value) } else {
                c.addAttribute("t", "inlineStr")
                val t = com.wxiwei.office.editor.ooxml.newElement(SS, "t").apply { setText(w.value) }
                if (w.value.isNotEmpty() && (w.value.first().isWhitespace() || w.value.last().isWhitespace() || '\n' in w.value))
                    t.addAttribute(QName("space", com.wxiwei.office.fc.dom4j.Namespace.XML_NAMESPACE), "preserve")
                c.add(com.wxiwei.office.editor.ooxml.newElement(SS, "is").apply { add(t) })
            }
            is CellWrite.Blank -> Unit
        }
    }

    private fun number(v: Double): String = if (v == Math.rint(v) && Math.abs(v) < 1e15) v.toLong().toString() else v.toString()

    private fun updateDimension(root: Element, cells: List<CellWrite>) {
        val dim = root.firstChild(SS, "dimension") ?: return
        val parts = dim.attributeValue("ref")?.split(':') ?: return
        fun rc(s: String) = (s.dropWhile { it.isLetter() }.toIntOrNull()?.minus(1) ?: 0) to colOf(s)
        val (r1, c1) = rc(parts[0]); val (r2, c2) = rc(parts.getOrElse(1) { parts[0] })
        val top = minOf(r1, cells.minOf { it.row }); val left = minOf(c1, cells.minOf { it.col })
        val bottom = maxOf(r2, cells.maxOf { it.row }); val right = maxOf(c2, cells.maxOf { it.col })
        dim.addAttribute("ref", if (top == bottom && left == right) ref(top, left) else ref(top, left) + ":" + ref(bottom, right))
    }

    /** calcChain lists formula cells; a stale one makes Excel report a damaged file. */
    private fun dropCalcChain(pkg: OoxmlPackage) {
        val workbook = "xl/workbook.xml"
        val chainRel = pkg.relationships(workbook).firstOrNull { it.type.endsWith("/calcChain") }
        if (chainRel != null) {
            val chain = pkg.resolveTarget(workbook, chainRel.target)
            pkg.remove(chain)
            val rels = pkg.xml(pkg.relsPartOf(workbook)).rootElement
            rels.elements().filterIsInstance<Element>().filter { it.attributeValue("Id") == chainRel.id }.forEach { rels.remove(it) }
            if (pkg.has("[Content_Types].xml")) {
                val types = pkg.xml("[Content_Types].xml").rootElement
                types.elements().filterIsInstance<Element>().filter { it.attributeValue("PartName")?.trimStart('/') == chain }.forEach { types.remove(it) }
            }
        }
        val book = pkg.xml(workbook).rootElement
        val calcPr = book.firstChild(SS, "calcPr") ?: com.wxiwei.office.editor.ooxml.newElement(SS, "calcPr").also { e ->
            // CT_Workbook order: ... sheets, functionGroups, externalReferences, definedNames, calcPr ...
            val anchor = listOf("definedNames", "externalReferences", "functionGroups", "sheets").firstNotNullOfOrNull { book.firstChild(SS, it) }
            val content = book.content()
            if (anchor == null) content.add(e) else content.add(content.indexOf(anchor) + 1, e)
        }
        calcPr.addAttribute("fullCalcOnLoad", "1")
    }
}
