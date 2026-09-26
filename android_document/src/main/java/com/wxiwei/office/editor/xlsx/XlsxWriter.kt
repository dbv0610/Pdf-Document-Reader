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

/** Rows ([rows]) or columns inserted ([count] > 0) or deleted at [at] of a sheet. */
data class StructureWrite(val sheetIndex: Int, val rows: Boolean, val at: Int, val count: Int)

/** A format change for one cell, relative to the cell's format in the original file. */
data class StyleWrite(val sheetIndex: Int, val row: Int, val col: Int, val format: CellFormat)

/**
 * Patches the ORIGINAL .xlsx: only the written cells change, everything else (styles, drawings,
 * charts...) is kept byte for byte. Removes calcChain.xml and sets fullCalcOnLoad so Excel
 * recalculates on open. [formulaOf] gives the model formula of any cell, used to expand shared
 * formulas whose master cell is overwritten.
 */
class XlsxWriter(private val source: File, private val formulaOf: (sheetIndex: Int, row: Int, col: Int) -> String? = { _, _, _ -> null }) {

    fun save(target: File, writes: Collection<CellWrite>, styles: Collection<StyleWrite> = emptyList(),
             structure: List<StructureWrite> = emptyList()): EditResult {
        if (!source.extension.equals("xlsx", true) && !source.extension.equals("xlsm", true))
            return EditResult.Error(Reason.UNSUPPORTED_FORMAT, "Only .xlsx/.xlsm can be saved")
        if (source.canonicalFile == target.canonicalFile)
            return EditResult.Error(Reason.INVALID_ARGUMENT, "Save to a new file")
        return runEdit {
            val pkg = OoxmlPackage.open(source)
            val parts = sheetParts(pkg)
            // rows/columns first: the cell writes use the coordinates after them
            if (structure.isNotEmpty()) XlsxStructure(pkg, parts).apply(structure)
            for ((sheetIndex, cells) in writes.groupBy { it.sheetIndex }) {
                val part = parts.getOrNull(sheetIndex) ?: return@runEdit EditResult.Error(Reason.NOT_FOUND, "Sheet $sheetIndex not found")
                writeSheet(pkg, part, sheetIndex, cells)
            }
            if (styles.isNotEmpty()) {
                val stylesPart = stylesPart(pkg) ?: return@runEdit EditResult.Error(Reason.NOT_FOUND, "No styles.xml")
                val book = StyleBook(pkg.xml(stylesPart).rootElement!!)
                for ((sheetIndex, cells) in styles.groupBy { it.sheetIndex }) {
                    val part = parts.getOrNull(sheetIndex) ?: return@runEdit EditResult.Error(Reason.NOT_FOUND, "Sheet $sheetIndex not found")
                    val data = pkg.xml(part).rootElement!!.firstChild(SS, "sheetData") ?: error("Missing sheetData in $part")
                    for (w in cells.sortedWith(compareBy({ it.row }, { it.col }))) {
                        val row = rowElement(data, w.row)
                        val c = cellElement(row, w.row, w.col)
                        val base = c.attributeValue("s")?.toIntOrNull() ?: 0
                        c.addAttribute("s", book.xfFor(base, w.format).toString())
                    }
                }
            }
            dropCalcChain(pkg)
            pkg.saveTo(target)
        }
    }

    /** Worksheet part names in workbook order (same order as the viewer's sheet index). */
    private fun sheetParts(pkg: OoxmlPackage): List<String> {
        val workbook = "xl/workbook.xml"
        val rels = pkg.relationships(workbook).associateBy { it.id }
        val sheets = pkg.xml(workbook).rootElement!!.firstChild(SS, "sheets") ?: return emptyList()
        return sheets.childrenNamed(SS, "sheet").map { s ->
            val id = s.attributeValue(QName("id", R)) ?: ""
            rels[id]?.let { pkg.resolveTarget(workbook, it.target) } ?: ""
        }
    }

    private fun stylesPart(pkg: OoxmlPackage): String? {
        val workbook = "xl/workbook.xml"
        val rel = pkg.relationships(workbook).firstOrNull { it.type.endsWith("/styles") } ?: return null
        return pkg.resolveTarget(workbook, rel.target)
    }

    /**
     * Adds formats to styles.xml: every (original xf, format) pair becomes one new xf, built from
     * a copy of the original font/fill with the changes, so other cells keep their look.
     */
    private class StyleBook(private val root: Element) {
        private val made = HashMap<Pair<Int, CellFormat>, Int>()
        private fun list(name: String): Element = root.firstChild(SS, name) ?: com.wxiwei.office.editor.ooxml.newElement(SS, name).also {
            // CT_Stylesheet order: numFmts, fonts, fills, borders, cellStyleXfs, cellXfs, ...
            val order = listOf("numFmts", "fonts", "fills", "borders", "cellStyleXfs", "cellXfs")
            val content = root.content() as MutableList<Any?>
            val next = order.drop(order.indexOf(name) + 1).firstNotNullOfOrNull { n -> root.firstChild(SS, n) }
            if (next == null) content.add(it) else content.add(content.indexOf(next), it)
        }
        private fun items(list: Element, name: String) = list.childrenNamed(SS, name)
        private fun append(list: Element, name: String, e: Element): Int {
            list.add(e)
            val n = items(list, name).size
            list.addAttribute("count", n.toString())
            return n - 1
        }

        fun xfFor(base: Int, f: CellFormat): Int = made.getOrPut(base to f) {
            val xfs = list("cellXfs")
            val baseXf = items(xfs, "xf").getOrNull(base) ?: items(xfs, "xf").firstOrNull()
            val xf = baseXf?.createCopy() ?: com.wxiwei.office.editor.ooxml.newElement(SS, "xf").apply {
                addAttribute("numFmtId", "0"); addAttribute("fontId", "0"); addAttribute("fillId", "0"); addAttribute("borderId", "0"); addAttribute("xfId", "0")
            }
            if (f.changesFont) {
                xf.addAttribute("fontId", font(xf.attributeValue("fontId")?.toIntOrNull() ?: 0, f).toString())
                xf.addAttribute("applyFont", "1")
            }
            f.fillColor?.let {
                xf.addAttribute("fillId", fill(it).toString())
                xf.addAttribute("applyFill", "1")
            }
            f.numberFormat?.let {
                xf.addAttribute("numFmtId", numFmt(it).toString())
                xf.addAttribute("applyNumberFormat", "1")
            }
            if (f.changesAlignment) {
                val a = xf.firstChild(SS, "alignment") ?: com.wxiwei.office.editor.ooxml.newElement(SS, "alignment").also {
                    (xf.content() as MutableList<Any?>).add(0, it)
                }
                f.horizontal?.let { if (it == "general") a.attribute("horizontal")?.let { at -> a.remove(at) } else a.addAttribute("horizontal", it) }
                f.vertical?.let { a.addAttribute("vertical", it) }
                f.wrap?.let { a.addAttribute("wrapText", if (it) "1" else "0") }
                xf.addAttribute("applyAlignment", "1")
            }
            append(xfs, "xf", xf)
        }

        private fun font(base: Int, f: CellFormat): Int {
            val fonts = list("fonts")
            val font = items(fonts, "font").getOrNull(base)?.createCopy() ?: com.wxiwei.office.editor.ooxml.newElement(SS, "font")
            fun set(name: String, on: Boolean?) {
                if (on == null) return
                font.childrenNamed(SS, name).forEach { font.remove(it) }
                if (on) font.add(com.wxiwei.office.editor.ooxml.newElement(SS, name))
            }
            set("b", f.bold); set("i", f.italic); set("strike", f.strike); set("u", f.underline)
            f.fontSize?.let { sz ->
                val e = font.firstChild(SS, "sz") ?: com.wxiwei.office.editor.ooxml.newElement(SS, "sz").also { font.add(it) }
                e.addAttribute("val", if (sz == Math.rint(sz)) sz.toLong().toString() else sz.toString())
            }
            f.fontColor?.let { rgb ->
                font.childrenNamed(SS, "color").forEach { font.remove(it) }
                font.add(com.wxiwei.office.editor.ooxml.newElement(SS, "color").apply { addAttribute("rgb", "FF" + rgb.removePrefix("#").uppercase()) })
            }
            return append(fonts, "font", font)
        }

        private fun fill(color: String): Int {
            if (color == "none") return 0
            val fills = list("fills")
            val fill = com.wxiwei.office.editor.ooxml.newElement(SS, "fill")
            val pattern = com.wxiwei.office.editor.ooxml.newElement(SS, "patternFill").apply { addAttribute("patternType", "solid") }
            pattern.add(com.wxiwei.office.editor.ooxml.newElement(SS, "fgColor").apply { addAttribute("rgb", "FF" + color.removePrefix("#").uppercase()) })
            pattern.add(com.wxiwei.office.editor.ooxml.newElement(SS, "bgColor").apply { addAttribute("indexed", "64") })
            fill.add(pattern)
            return append(fills, "fill", fill)
        }

        private fun numFmt(code: String): Int {
            val builtin = com.wxiwei.office.ss.model.style.BuiltinFormats.getBuiltinFormat(code)
            if (builtin in 0..49 && !code.startsWith("reserved")) return builtin
            val list = list("numFmts")
            items(list, "numFmt").firstOrNull { it.attributeValue("formatCode") == code }?.let { return it.attributeValue("numFmtId")!!.toInt() }
            val id = maxOf(163, items(list, "numFmt").maxOfOrNull { it.attributeValue("numFmtId")?.toIntOrNull() ?: 0 } ?: 0) + 1
            append(list, "numFmt", com.wxiwei.office.editor.ooxml.newElement(SS, "numFmt").apply {
                addAttribute("numFmtId", id.toString()); addAttribute("formatCode", code)
            })
            return id
        }
    }

    private fun ref(row: Int, col: Int) = A1FormulaShifter.address(row, col)
    private fun rowOf(e: Element) = e.attributeValue("r")?.toIntOrNull()?.minus(1) ?: -1
    private fun colOf(ref: String?): Int {
        val letters = ref?.takeWhile { it.isLetter() }?.uppercase() ?: return -1
        return letters.fold(0) { n, c -> n * 26 + (c - 'A' + 1) } - 1
    }

    private fun writeSheet(pkg: OoxmlPackage, part: String, sheetIndex: Int, cells: List<CellWrite>) {
        val root = pkg.xml(part).rootElement!!
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
        val content = data.content() as MutableList<Any?>
        if (next == null) content.add(e) else content.add(content.indexOf(next), e)
        return e
    }

    private fun cellElement(row: Element, r: Int, col: Int): Element {
        val cells = row.childrenNamed(SS, "c")
        cells.firstOrNull { colOf(it.attributeValue("r")) == col }?.let { return it }
        val e = com.wxiwei.office.editor.ooxml.newElement(SS, "c").apply { addAttribute("r", ref(r, col)) }
        row.attributeValue("s")?.takeIf { row.attributeValue("customFormat") == "1" }?.let { e.addAttribute("s", it) }
        val next = cells.firstOrNull { colOf(it.attributeValue("r")) > col }
        val content = row.content() as MutableList<Any?>
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
            (other.content() as MutableList<Any?>).add(0, com.wxiwei.office.editor.ooxml.newElement(SS, "f").apply {
                this.text = text
            })
        }
    }

    private fun fill(c: Element, w: CellWrite) {
        listOf("f", "v", "is").forEach { name -> c.childrenNamed(SS, name).forEach { c.remove(it) } }
        c.attribute("t")?.let { c.remove(it) }
        c.attribute("cm")?.let { c.remove(it) }
        fun add(name: String, text: String): Element = com.wxiwei.office.editor.ooxml.newElement(SS, name).apply {
            this.text = text
        }.also { c.add(it) }
        w.formulaText?.let { add("f", it) }
        when (w) {
            is CellWrite.Number -> add("v", number(w.value))
            is CellWrite.Bool -> { c.addAttribute("t", "b"); add("v", if (w.value) "1" else "0") }
            is CellWrite.Error -> { c.addAttribute("t", "e"); add("v", ErrorEval.getText(w.code)) }
            is CellWrite.Text -> if (w.formula != null) { c.addAttribute("t", "str"); add("v", w.value) } else {
                c.addAttribute("t", "inlineStr")
                val t = com.wxiwei.office.editor.ooxml.newElement(SS, "t").apply { text = w.value }
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
            rels!!.elements()!!.filterIsInstance<Element>().filter { it.attributeValue("Id") == chainRel.id }.forEach { rels!!.remove(it) }
            if (pkg.has("[Content_Types].xml")) {
                val types = pkg.xml("[Content_Types].xml").rootElement
                types!!.elements()!!.filterIsInstance<Element>().filter { it.attributeValue("PartName")?.trimStart('/') == chain }.forEach { types!!.remove(it) }
            }
        }
        val book = pkg.xml(workbook).rootElement!!
        val calcPr = book.firstChild(SS, "calcPr") ?: com.wxiwei.office.editor.ooxml.newElement(SS, "calcPr").also { e ->
            // CT_Workbook order: ... sheets, functionGroups, externalReferences, definedNames, calcPr ...
            val anchor = listOf("definedNames", "externalReferences", "functionGroups", "sheets").firstNotNullOfOrNull { book.firstChild(SS, it) }
            val content = book.content() as MutableList<Any?>
            if (anchor == null) content.add(e) else content.add(content.indexOf(anchor) + 1, e)
        }
        calcPr.addAttribute("fullCalcOnLoad", "1")
    }
}
