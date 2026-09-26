package com.wxiwei.office.editor.xlsx

import com.wxiwei.office.fc.hssf.formula.WorkbookEvaluator
import com.wxiwei.office.fc.hssf.formula.eval.BlankEval
import com.wxiwei.office.fc.hssf.formula.eval.BoolEval
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.NumberEval
import com.wxiwei.office.fc.hssf.formula.eval.StringEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.fc.hssf.formula.udf.UDFFinder
import com.wxiwei.office.ss.model.baseModel.Cell
import com.wxiwei.office.ss.model.baseModel.Sheet
import com.wxiwei.office.ss.model.baseModel.Workbook

/**
 * Recalculates XLSX formulas in the in-memory model. Only fully loaded sheets are recalculated;
 * formulas that fail (unsupported function, sheet still loading) keep their cached value and add
 * a warning. Not thread safe: call on the thread that owns the model (the UI thread).
 */
class XlsxFormulaEngine(val book: Workbook) {
    val adapter = XlsxEvaluationWorkbook(book)
    private val evaluator = WorkbookEvaluator(adapter, null, null)
    /** Formula cells per sheet index, found on first use and kept up to date by [formulaChanged]. */
    private val formulaCells = HashMap<Int, LinkedHashSet<Cell>>()

    data class Changed(val sheetIndex: Int, val cell: Cell)

    companion object {
        private const val WIDE = 64
        /** Functions whose result does not follow from the cells they reference. */
        private val VOLATILE = setOf("INDIRECT", "OFFSET", "NOW", "TODAY", "RAND", "RANDBETWEEN", "CELL", "INFO")
    }

    private fun loaded(sheet: Sheet?) = sheet != null && sheet.getState() == Sheet.State_Accomplished

    private fun formulas(sheetIndex: Int): LinkedHashSet<Cell> = formulaCells.getOrPut(sheetIndex) {
        val set = LinkedHashSet<Cell>()
        val sheet = book.getSheet(sheetIndex)
        if (sheet != null) for (r in sheet.getFirstRowNum()..sheet.getLastRowNum()) {
            val row = sheet.getRow(r) ?: continue
            for (cell in row.cellCollection()) if (cell.formula != null) set.add(cell)
        }
        set
    }

    /** A plain value changed (or a cell was created/cleared) at [cell]. */
    fun valueChanged(sheetIndex: Int, cell: Cell) {
        adapter.cellAdapter(sheetIndex, cell)?.let { evaluator.notifyUpdateCell(it) }
    }

    /** [cell]'s formula text was set, changed or removed. */
    fun formulaChanged(sheetIndex: Int, cell: Cell) {
        adapter.forget(cell)
        val set = formulas(sheetIndex)
        if (cell.formula != null) set.add(cell) else set.remove(cell)
        dropIndex()
        // Dependency links of the old formula are stale: start the cache over
        evaluator.clearAllCachedResultValues()
    }

    /** Evaluate one formula cell without storing the result. */
    fun evaluate(sheetIndex: Int, cell: Cell): ValueEval = evaluator.evaluate(adapter.cellAdapter(sheetIndex, cell)!!)

    // ---- Dependency index: which formulas read a given cell -------------------------------
    private class Dep(val r1: Int, val r2: Int, val c1: Int, val c2: Int, val sheet: Int, val cell: Cell)
    private var index: HashMap<Long, MutableList<Dep>>? = null
    private val wide = ArrayList<Dep>()          // areas wider than WIDE columns
    private val always = LinkedHashSet<Pair<Int, Cell>>() // INDIRECT, OFFSET, NOW... or unparsable
    private fun bucket(sheet: Int, col: Int) = (sheet.toLong() shl 20) or col.toLong()

    private fun buildIndex(warnings: MutableList<String>): HashMap<Long, MutableList<Dep>> {
        index?.let { return it }
        val map = HashMap<Long, MutableList<Dep>>(); wide.clear(); always.clear()
        fun add(d: Dep) {
            if (d.c2 - d.c1 >= WIDE) { wide.add(d); return }
            for (c in d.c1..d.c2) map.getOrPut(bucket(d.sheet, c)) { ArrayList(2) }.add(d)
        }
        for (s in 0 until book.getSheetCount()) {
            if (!loaded(book.getSheet(s))) continue
            for (cell in formulas(s)) {
                val ptgs = try { adapter.tokensOf(s, cell) } catch (e: Exception) { null }
                // Unparsable (unsupported function such as IFERROR/XLOOKUP): it keeps its saved value,
                // so trying it again on every edit would only cost time
                if (ptgs == null) continue
                for (p in ptgs) when (p) {
                    is com.wxiwei.office.fc.hssf.formula.ptg.Area3DPtg -> add(Dep(p.getFirstRow(), p.getLastRow(), p.getFirstColumn(), p.getLastColumn(), p.getExternSheetIndex(), cell))
                    is com.wxiwei.office.fc.hssf.formula.ptg.AreaPtgBase -> add(Dep(p.getFirstRow(), p.getLastRow(), p.getFirstColumn(), p.getLastColumn(), s, cell))
                    is com.wxiwei.office.fc.hssf.formula.ptg.Ref3DPtg -> add(Dep(p.getRow(), p.getRow(), p.getColumn(), p.getColumn(), p.getExternSheetIndex(), cell))
                    is com.wxiwei.office.fc.hssf.formula.ptg.RefPtgBase -> add(Dep(p.getRow(), p.getRow(), p.getColumn(), p.getColumn(), s, cell))
                    is com.wxiwei.office.fc.hssf.formula.ptg.AbstractFunctionPtg -> if (p.getName().uppercase() in VOLATILE) always.add(s to cell)
                    is com.wxiwei.office.fc.hssf.formula.ptg.NamePtg, is com.wxiwei.office.fc.hssf.formula.ptg.NameXPtg -> always.add(s to cell)
                    else -> Unit
                }
            }
        }
        index = map
        return map
    }

    /** Forget the dependency index (a formula was added, changed or removed). */
    private fun dropIndex() { index = null }

    /** Formula cells that read (sheet,row,col), directly or through other formulas. */
    private fun dependents(sheetIndex: Int, row: Int, col: Int, warnings: MutableList<String>): LinkedHashSet<Pair<Int, Cell>> {
        val map = buildIndex(warnings)
        val found = LinkedHashSet<Pair<Int, Cell>>()
        val queue = ArrayDeque<Triple<Int, Int, Int>>(); queue.add(Triple(sheetIndex, row, col))
        while (queue.isNotEmpty()) {
            val (s, r, c) = queue.removeFirst()
            val hits = (map[bucket(s, c)].orEmpty().asSequence() + wide.asSequence())
                .filter { it.sheet == s && r in it.r1..it.r2 && c in it.c1..it.c2 }
            for (d in hits) {
                val key = formulaSheetOf(d) to d.cell
                if (found.add(key)) queue.add(Triple(key.first, d.cell.getRowNumber(), d.cell.getColNumber()))
            }
        }
        return found
    }

    private val sheetOfCell = java.util.IdentityHashMap<Sheet, Int>()
    private fun formulaSheetOf(d: Dep): Int {
        val sheet = d.cell.getSheet() ?: return d.sheet
        return sheetOfCell.getOrPut(sheet) { book.getSheetIndex(sheet) }
    }

    /**
     * Recalculate only what depends on the edited cell (plus volatile formulas) and store the
     * results. Returns the cells whose displayed value changed.
     */
    fun recalcAfter(sheetIndex: Int, cell: Cell, warnings: MutableList<String> = ArrayList()): List<Changed> {
        if (adapter.rowTouched(sheetIndex, cell.getRowNumber())) { dropIndex(); evaluator.clearAllCachedResultValues() }
        val targets = dependents(sheetIndex, cell.getRowNumber(), cell.getColNumber(), warnings)
        targets.addAll(always)
        if (cell.formula != null) targets.add(sheetIndex to cell)
        val changed = ArrayList<Changed>()
        for ((s, f) in targets) {
            if (!loaded(book.getSheet(s))) continue
            val result = try { evaluate(s, f) } catch (e: Exception) {
                warnings.add("${book.getSheet(s)?.getSheetName()}!${A1FormulaShifter.address(f.getRowNumber(), f.getColNumber())}: ${e.cause?.javaClass?.simpleName ?: e.javaClass.simpleName} ${(e.cause?.message ?: e.message).orEmpty()}".trim())
                continue
            } catch (e: StackOverflowError) { warnings.add("Formula chain too deep"); continue }
            if (store(f, result)) changed.add(Changed(s, f))
        }
        return changed
    }

    /**
     * Recalculate every formula of the loaded sheets and store new results in the model.
     * Returns the cells whose displayed value changed; [warnings] lists what could not be computed.
     */
    fun recalc(warnings: MutableList<String> = ArrayList()): List<Changed> {
        val changed = ArrayList<Changed>()
        for (index in 0 until book.getSheetCount()) {
            val sheet = book.getSheet(index)
            if (!loaded(sheet)) {
                if (formulaCells.containsKey(index)) warnings.add("Sheet ${sheet?.getSheetName()} is still loading; its formulas keep their saved values")
                continue
            }
            for (cell in formulas(index)) {
                val result = try { evaluate(index, cell) } catch (e: Exception) {
                    warnings.add("${sheet!!.getSheetName()}!${A1FormulaShifter.address(cell.getRowNumber(), cell.getColNumber())}: ${e.javaClass.simpleName} ${e.message.orEmpty()}".trim())
                    continue
                } catch (e: StackOverflowError) { warnings.add("Formula chain too deep"); continue }
                if (store(cell, result)) changed.add(Changed(index, cell))
            }
        }
        return changed
    }

    /** Put an evaluation result into the model cell; returns true if the value changed. */
    fun store(cell: Cell, result: ValueEval): Boolean {
        val before = snapshot(cell)
        when (result) {
            is NumberEval -> {
                // Excel saves 15 significant digits: a result equal to that is not a change
                val old = if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) cell.getNumberValue() else Double.NaN
                if (!sameNumber(old, result.numberValue)) { cell.setCellType(Cell.CELL_TYPE_NUMERIC); cell.setCellValue(result.numberValue) }
            }
            is StringEval -> {
                if (!(cell.getCellType() == Cell.CELL_TYPE_STRING && adapter.stringOf(cell) == result.stringValue)) {
                    cell.setCellType(Cell.CELL_TYPE_STRING); cell.setCellValue(book.addSharedString(result.stringValue))
                }
            }
            is BoolEval -> { cell.setCellType(Cell.CELL_TYPE_BOOLEAN); cell.setCellValue(result.booleanValue) }
            is ErrorEval -> { cell.setCellType(Cell.CELL_TYPE_ERROR); cell.setCellValue(result.errorCode.toByte()) }
            is BlankEval -> { cell.setCellType(Cell.CELL_TYPE_NUMERIC); cell.setCellValue(0.0) }
            else -> return false
        }
        val after = snapshot(cell)
        if (before != after) { cell.removeSTRoot(); return true }
        return false
    }

    private fun sameNumber(a: Double, b: Double) = a == b || (!a.isNaN() && Math.abs(a - b) <= 1e-13 * maxOf(1.0, Math.abs(a), Math.abs(b)))

    private fun snapshot(cell: Cell): Any? = when (cell.getCellType()) {
        Cell.CELL_TYPE_NUMERIC -> cell.getNumberValue()
        // A formula returning "" is saved as an empty <v/>, which reads back as a blank cell
        Cell.CELL_TYPE_STRING -> adapter.stringOf(cell).takeIf { it.isNotEmpty() }?.let { "s:$it" }
        Cell.CELL_TYPE_BOOLEAN -> cell.getBooleanValue()
        // The reader may keep a saved error as the text "#N/A": treat both forms as the same value
        Cell.CELL_TYPE_ERROR -> "s:" + ErrorEval.getText(cell.getErrorValue())
        else -> null
    }
}
