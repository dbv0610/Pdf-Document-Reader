/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.wxiwei.office.editor.xlsx

import com.wxiwei.office.editor.ooxml.OoxmlPackage
import com.wxiwei.office.editor.ooxml.SS
import com.wxiwei.office.editor.ooxml.childrenNamed
import com.wxiwei.office.editor.ooxml.firstChild
import com.wxiwei.office.editor.ooxml.newElement
import com.wxiwei.office.fc.dom4j.Element

/**
 * Replays row/column inserts and deletes on the XML of an .xlsx: cells and rows move (deleted ones
 * go), every formula of the workbook is rewritten ([RefShifter]), and so are merged cells, the
 * dimension, hyperlinks, conditional formats, validations, filters, column widths, tables (with
 * their columns), drawing anchors, chart ranges, defined names and pivot tables (their source
 * range and where they sit).
 */
internal class XlsxStructure(private val pkg: OoxmlPackage, private val sheetParts: List<String>) {
    private val workbook = "xl/workbook.xml"
    private val sheetNames: List<String> =
        pkg.xml(workbook).rootElement!!.firstChild(SS, "sheets")?.childrenNamed(SS, "sheet")?.map { it.attributeValue("name") ?: "" }.orEmpty()

    fun apply(changes: List<StructureWrite>) {
        // shared formulas become plain ones: a master row may be deleted, and each copy moves on its own
        sheetParts.forEach { if (it.isNotEmpty()) expandSharedFormulas(it) }
        for (w in changes) {
            val part = sheetParts.getOrNull(w.sheetIndex)?.takeIf { it.isNotEmpty() } ?: continue
            val change = RefShifter.Change(sheetNames.getOrElse(w.sheetIndex) { "" }, w.rows, w.at, w.count)
            moveCells(part, change)
            sheetRanges(part, change)
            tables(part, change)
            drawings(part, change)
            sheetParts.forEachIndexed { i, p -> if (p.isNotEmpty()) formulas(p, sheetNames.getOrElse(i) { "" }, change) }
            charts(change)
            definedNames(change)
            pivots(part, change)
        }
    }

    // ---- helpers ---------------------------------------------------------------------------

    private fun colOf(ref: String): Int = ref.takeWhile { it.isLetter() }.uppercase().fold(0) { n, c -> n * 26 + (c - 'A' + 1) } - 1
    private fun rowOf(ref: String): Int = (ref.dropWhile { it.isLetter() }.toIntOrNull() ?: 0) - 1

    private fun move(v: Int, c: RefShifter.Change): Int? = when {
        c.count > 0 -> if (v >= c.at) v + c.count else v
        v < c.at -> v
        v >= c.at - c.count -> v + c.count
        else -> null
    }

    private fun descendants(e: Element): Sequence<Element> = sequence {
        for (child in e.elements()!!.filterIsInstance<Element>()) { yield(child); yieldAll(descendants(child)) }
    }

    private fun related(part: String, typeSuffix: String): List<String> =
        pkg.relationships(part).filter { it.type.endsWith(typeSuffix) && it.targetMode != "External" }.map { pkg.resolveTarget(part, it.target) }

    // ---- steps -----------------------------------------------------------------------------

    private fun expandSharedFormulas(part: String) {
        val data = pkg.xml(part).rootElement!!.firstChild(SS, "sheetData") ?: return
        val masters = HashMap<String, Triple<String, Int, Int>>()
        for (row in data.childrenNamed(SS, "row")) for (c in row.childrenNamed(SS, "c")) {
            val f = c.firstChild(SS, "f") ?: continue
            if (f.attributeValue("t") == "shared" && !f.text.isNullOrEmpty()) {
                val ref = c.attributeValue("r") ?: continue
                masters[f.attributeValue("si") ?: continue] = Triple(f.text!!, rowOf(ref), colOf(ref))
            }
        }
        if (masters.isEmpty()) return
        for (row in data.childrenNamed(SS, "row")) for (c in row.childrenNamed(SS, "c")) {
            val f = c.firstChild(SS, "f") ?: continue
            if (f.attributeValue("t") != "shared") continue
            val (text, r0, c0) = masters[f.attributeValue("si")] ?: continue
            val ref = c.attributeValue("r") ?: continue
            f.text = if (f.text.isNullOrEmpty()) A1FormulaShifter.shift(text, rowOf(ref) - r0, colOf(ref) - c0) else f.text
            listOf("t", "ref", "si").forEach { n -> f.attribute(n)?.let { f.remove(it) } }
        }
    }

    private fun moveCells(part: String, change: RefShifter.Change) {
        val root = pkg.xml(part).rootElement!!
        val data = root.firstChild(SS, "sheetData") ?: return
        for (row in data.childrenNamed(SS, "row")) {
            row.attribute("spans")?.let { row.remove(it) }
            if (change.rows) {
                val moved = move(rowOf("A" + (row.attributeValue("r") ?: continue)), change)
                if (moved == null) { data.remove(row); continue }
                row.addAttribute("r", (moved + 1).toString())
                for (c in row.childrenNamed(SS, "c")) {
                    val ref = c.attributeValue("r") ?: continue
                    c.addAttribute("r", A1FormulaShifter.column(colOf(ref)) + (moved + 1))
                }
            } else {
                for (c in row.childrenNamed(SS, "c")) {
                    val ref = c.attributeValue("r") ?: continue
                    val col = move(colOf(ref), change)
                    if (col == null) row.remove(c) else c.addAttribute("r", A1FormulaShifter.column(col) + (rowOf(ref) + 1))
                }
            }
        }
        if (!change.rows) root.firstChild(SS, "cols")?.let { cols ->
            for (col in cols.childrenNamed(SS, "col")) {
                val min = (col.attributeValue("min")?.toIntOrNull() ?: continue) - 1
                val max = (col.attributeValue("max")?.toIntOrNull() ?: continue) - 1
                val range = RefShifter.shiftRange(A1FormulaShifter.column(min) + "1:" + A1FormulaShifter.column(max) + "1", change)
                if (range == null) { cols.remove(col); continue }
                val (a, b) = range.split(":")
                col.addAttribute("min", (colOf(a) + 1).toString())
                col.addAttribute("max", (colOf(b) + 1).toString())
            }
            if (cols.childrenNamed(SS, "col").isEmpty()) root.remove(cols)
        }
    }

    /** sqref lists ("A1 B2:C3"), single refs and ranges inside the sheet part. */
    private fun sheetRanges(part: String, change: RefShifter.Change) {
        val root = pkg.xml(part).rootElement!!
        fun list(value: String): String? = value.split(' ').filter { it.isNotEmpty() }
            .mapNotNull { RefShifter.shiftRange(it, change) }.takeIf { it.isNotEmpty() }?.joinToString(" ")
        root.firstChild(SS, "dimension")?.let { d -> d.attributeValue("ref")?.let { r -> list(r)?.let { d.addAttribute("ref", it) } } }
        root.firstChild(SS, "mergeCells")?.let { mc ->
            for (m in mc.childrenNamed(SS, "mergeCell")) {
                val r = m.attributeValue("ref")?.let { RefShifter.shiftRange(it, change) }
                if (r == null) mc.remove(m) else m.addAttribute("ref", r)
            }
            val n = mc.childrenNamed(SS, "mergeCell").size
            if (n == 0) root.remove(mc) else mc.addAttribute("count", n.toString())
        }
        root.firstChild(SS, "hyperlinks")?.let { hl ->
            for (h in hl.childrenNamed(SS, "hyperlink")) {
                val r = h.attributeValue("ref")?.let { RefShifter.shiftRange(it, change) }
                if (r == null) hl.remove(h) else h.addAttribute("ref", r)
            }
            if (hl.childrenNamed(SS, "hyperlink").isEmpty()) root.remove(hl)
        }
        for (cf in root.childrenNamed(SS, "conditionalFormatting")) {
            val r = cf.attributeValue("sqref")?.let { list(it) }
            if (r == null) root.remove(cf) else {
                cf.addAttribute("sqref", r)
                val first = r.split(' ').first().substringBefore(':')
                // relative references in the rules follow the range's first cell
                cf.childrenNamed(SS, "cfRule").forEach { rule ->
                    rule.childrenNamed(SS, "formula").forEach { f -> f.text = RefShifter.shift(f.text ?: "", change.sheet, change) }
                }
            }
        }
        root.firstChild(SS, "dataValidations")?.let { dvs ->
            for (dv in dvs.childrenNamed(SS, "dataValidation")) {
                val r = dv.attributeValue("sqref")?.let { list(it) }
                if (r == null) dvs.remove(dv) else dv.addAttribute("sqref", r)
            }
            val n = dvs.childrenNamed(SS, "dataValidation").size
            if (n == 0) root.remove(dvs) else dvs.addAttribute("count", n.toString())
        }
        root.firstChild(SS, "autoFilter")?.let { af ->
            val r = af.attributeValue("ref")?.let { RefShifter.shiftRange(it, change) }
            if (r == null) root.remove(af) else af.addAttribute("ref", r)
        }
        // selections point at cells that may be gone: reset them to A1
        root.firstChild(SS, "sheetViews")?.childrenNamed(SS, "sheetView")?.forEach { v ->
            v.childrenNamed(SS, "selection").forEach { v.remove(it) }
        }
    }

    private fun tables(part: String, change: RefShifter.Change) {
        for (tablePart in related(part, "/table")) {
            val root = pkg.xml(tablePart).rootElement!!
            val oldRef = root.attributeValue("ref") ?: continue
            val newRef = RefShifter.shiftRange(oldRef, change)
            if (newRef == null) continue // the whole table is gone; Excel keeps the part, so do we
            root.addAttribute("ref", newRef)
            root.firstChild(SS, "autoFilter")?.let { it.addAttribute("ref", newRef) }
            if (!change.rows) {
                // table columns follow inserted/deleted sheet columns inside the table
                val cols = root.firstChild(SS, "tableColumns") ?: continue
                val first = colOf(oldRef.substringBefore(':'))
                val last = colOf(oldRef.substringAfter(':'))
                val list = cols.childrenNamed(SS, "tableColumn")
                if (change.count > 0 && change.at > first && change.at <= last) {
                    val index = change.at - first
                    val nextId = (list.maxOfOrNull { it.attributeValue("id")?.toIntOrNull() ?: 0 } ?: 0) + 1
                    val content = cols.content() as MutableList<Any?>
                    for (k in 0 until change.count) {
                        val e = newElement(SS, "tableColumn").apply {
                            addAttribute("id", (nextId + k).toString())
                            addAttribute("name", "Column" + (nextId + k))
                        }
                        content.add(content.indexOf(list[index]) , e)
                    }
                } else if (change.count < 0) {
                    for (c in change.at until change.at - change.count) {
                        if (c in first..last) cols.remove(list[c - first])
                    }
                }
                cols.addAttribute("count", cols.childrenNamed(SS, "tableColumn").size.toString())
            }
        }
    }

    private fun drawings(part: String, change: RefShifter.Change) {
        val xdr = "http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing"
        for (drawing in related(part, "/drawing")) {
            val root = pkg.xml(drawing).rootElement!!
            for (anchor in root.elements()!!.filterIsInstance<Element>()) {
                for (end in listOf("from", "to")) {
                    val marker = anchor.elements()!!.filterIsInstance<Element>().firstOrNull { it.namespaceURI == xdr && it.name == end } ?: continue
                    val cell = marker.elements()!!.filterIsInstance<Element>().firstOrNull { it.namespaceURI == xdr && it.name == if (change.rows) "row" else "col" } ?: continue
                    val v = cell.text?.trim()?.toIntOrNull() ?: continue
                    // an anchor inside a deleted band sticks to the band's start
                    cell.text = (move(v, change) ?: change.at).toString()
                }
            }
        }
    }

    private fun formulas(part: String, sheetName: String, change: RefShifter.Change) {
        val data = pkg.xml(part).rootElement!!.firstChild(SS, "sheetData") ?: return
        for (row in data.childrenNamed(SS, "row")) for (c in row.childrenNamed(SS, "c")) {
            val f = c.firstChild(SS, "f") ?: continue
            f.text?.takeIf { it.isNotEmpty() }?.let { f.text = RefShifter.shift(it, sheetName, change) }
            // array formula ranges live on the changed sheet only
            if (sheetName.equals(change.sheet, true)) f.attributeValue("ref")?.let { r ->
                RefShifter.shiftRange(r, change)?.let { f.addAttribute("ref", it) }
            }
        }
    }

    private fun charts(change: RefShifter.Change) {
        val c = "http://schemas.openxmlformats.org/drawingml/2006/chart"
        for (name in pkg.partNames().filter { it.startsWith("xl/charts/chart") && it.endsWith(".xml") }) {
            val root = pkg.xml(name).rootElement!!
            for (f in descendants(root).filter { it.namespaceURI == c && it.name == "f" }.toList()) {
                f.text?.let { f.text = RefShifter.shift(it, "", change) }
            }
        }
    }

    /**
     * Pivot caches reading a range of the changed sheet follow it (worksheetSource ref); pivot
     * tables sitting on it move with their cells (location ref).
     */
    private fun pivots(part: String, change: RefShifter.Change) {
        for (name in pkg.partNames().filter { it.startsWith("xl/pivotCache/pivotCacheDefinition") && it.endsWith(".xml") }) {
            val src = descendants(pkg.xml(name).rootElement!!).firstOrNull { it.name == "worksheetSource" } ?: continue
            val ref = src.attributeValue("ref") ?: continue
            if (src.attributeValue("sheet") != change.sheet) continue
            val moved = RefShifter.shift(ref, change.sheet, change)
            // a source deleted whole keeps its old range (Excel would show #REF! in the pivot)
            if (!moved.contains("#REF!")) src.addAttribute("ref", moved)
        }
        for (table in related(part, "/pivotTable")) {
            val location = pkg.xml(table).rootElement!!.firstChild(SS, "location") ?: continue
            val ref = location.attributeValue("ref") ?: continue
            val moved = RefShifter.shift(ref, change.sheet, change)
            if (!moved.contains("#REF!")) location.addAttribute("ref", moved)
        }
    }

    private fun definedNames(change: RefShifter.Change) {
        val names = pkg.xml(workbook).rootElement!!.firstChild(SS, "definedNames") ?: return
        for (n in names.childrenNamed(SS, "definedName")) n.text?.let { n.text = RefShifter.shift(it, "", change) }
    }
}
