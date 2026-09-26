package com.wxiwei.office.editor.xlsx

import com.wxiwei.office.editor.EditResult
import com.wxiwei.office.editor.Reason
import com.wxiwei.office.ss.control.ExcelView
import com.wxiwei.office.ss.control.Spreadsheet
import com.wxiwei.office.ss.model.baseModel.Cell
import com.wxiwei.office.ss.model.baseModel.Row
import com.wxiwei.office.ss.model.baseModel.Sheet
import com.wxiwei.office.ss.model.baseModel.Workbook
import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.simpletext.font.Font
import com.wxiwei.office.ss.model.style.BuiltinFormats
import com.wxiwei.office.ss.model.style.CellStyle
import com.wxiwei.office.ss.model.style.NumberFormat
import com.wxiwei.office.ss.model.CellRangeAddress
import com.wxiwei.office.system.IControl
import java.io.File
import java.util.Locale

/**
 * Realtime cell editing for an open .xlsx: the new value shows immediately, formulas that depend
 * on it are recalculated in memory, and [save] patches the original file (see [XlsxWriter]).
 *
 * Input rules for [setCellInput] (like typing into Excel): "=..." is a formula, a US-format number
 * ("12", "-1.5", "3e4") is a number, TRUE/FALSE is a boolean, "" clears the cell, anything else is
 * text. UI thread only.
 */
class SheetEditSession internal constructor(
    private val book: Workbook,
    private val source: File,
    private val repaint: () -> Unit,
) {
    constructor(control: IControl, source: File) : this(spreadsheetOf(control).getWorkbook()!!, source, {
        val ss = spreadsheetOf(control)
        ss.getSheetView()?.invalidateTiles()
        ss.postInvalidate()
    })

    /** Formula engine of the workbook; rebuilt after rows or columns are inserted or deleted. */
    var engine = XlsxFormulaEngine(book)
        private set
    /** Warnings of the last recalculation (unsupported function, sheet still loading...). */
    var warnings: List<String> = emptyList(); private set
    var lastError: EditResult.Error? = null; private set

    private data class Key(val sheet: Int, val row: Int, val col: Int)
    /** One undoable edit: a typed value, a format change, or several applied together (a range). */
    private sealed class Step {
        data class Input(val key: Key, val before: String, val after: String) : Step()
        data class Format(val key: Key, val beforeStyle: Int, val afterStyle: Int, val format: CellFormat) : Step()
        data class Group(val steps: List<Step>) : Step()
        class Structure(val apply: () -> Unit, val revert: () -> Unit) : Step()
    }
    /** Rows/columns inserted or deleted, in order; save replays them on the file first. */
    private val structure = ArrayList<StructureWrite>()
    private val undoStack = ArrayList<Step>()
    private val redoStack = ArrayList<Step>()
    /** Format changes applied to each cell, in order; merged relative to the file's style on save. */
    private val formats = HashMap<Key, MutableList<CellFormat>>()
    /** Cells typed by the user plus formula cells whose result changed: everything [save] writes. */
    private val dirty = LinkedHashSet<Key>()

    fun canUndo() = undoStack.isNotEmpty()
    fun canRedo() = redoStack.isNotEmpty()
    fun hasChanges() = undoStack.isNotEmpty() || dirty.isNotEmpty() || formats.values.any { it.isNotEmpty() } || structure.isNotEmpty()

    // ---- rows and columns ---------------------------------------------------------------

    fun insertRows(sheetIndex: Int, at: Int, count: Int = 1) = structural(sheetIndex, true, at, count)
    fun deleteRows(sheetIndex: Int, at: Int, count: Int = 1) = structural(sheetIndex, true, at, -count)
    fun insertColumns(sheetIndex: Int, at: Int, count: Int = 1) = structural(sheetIndex, false, at, count)
    fun deleteColumns(sheetIndex: Int, at: Int, count: Int = 1) = structural(sheetIndex, false, at, -count)

    /**
     * Inserts ([count] > 0) or deletes rows/columns at [at]: cells, merged ranges, tables and
     * filters move, every formula of the workbook referring to them is rewritten (#REF! for
     * deleted cells), and the change is replayed on the file by [save]. One undoable step.
     */
    private fun structural(sheetIndex: Int, rows: Boolean, at: Int, count: Int): Boolean {
        val sheet = book.getSheet(sheetIndex) ?: return fail(Reason.NOT_FOUND, "Sheet $sheetIndex not found")
        val limit = if (rows) 1048576 else 16384
        if (count == 0 || at < 0 || at >= limit || Math.abs(count) > limit - at) return fail(Reason.INVALID_ARGUMENT, "Bad row/column range")
        if (sheet.getState() != Sheet.State_Accomplished) return fail(Reason.INVALID_ARGUMENT, "Sheet is still loading")
        val change = RefShifter.Change(sheet.getSheetName() ?: "", rows, at, count)
        // snapshots for undo
        val formulasBefore = HashMap<Cell, String>()
        for (i in 0 until book.getSheetCount()) {
            val s = book.getSheet(i) ?: continue
            for (r in s.getFirstRowNum()..s.getLastRowNum()) {
                val row = s.getRow(r) ?: continue
                for (cell in row.cellCollection()) cell.formula?.let { formulasBefore[cell] = it }
            }
        }
        fun ranges(): List<CellRangeAddress> = ArrayList<CellRangeAddress>().apply {
            for (i in 0 until sheet.getMergeRangeCount()) sheet.getMergeRange(i)?.let { add(it) }
            sheet.getTables()?.forEach { t -> t.getTableReference()?.let { add(it) } }
            sheet.getAutoFilters().forEach { add(it.range) }
        }
        val rangesBefore = ranges().map { it to intArrayOf(it.getFirstRow(), it.getFirstColumn(), it.getLastRow(), it.getLastColumn()) }
        val dirtyBefore = LinkedHashSet(dirty)
        val formatsBefore = formats.mapValues { ArrayList(it.value) }
        var removedRows: List<Row> = emptyList()
        var removedCells: Map<Int, Map<Int, Cell>> = emptyMap()
        val write = StructureWrite(sheetIndex, rows, at, count)

        fun moveKey(k: Key): Key? {
            if (k.sheet != sheetIndex) return k
            val v = if (rows) k.row else k.col
            val moved = when {
                count > 0 -> if (v >= at) v + count else v
                v < at -> v
                v >= at - count -> v + count
                else -> return null
            }
            return if (rows) k.copy(row = moved) else k.copy(col = moved)
        }

        val apply = {
            if (rows) removedRows = sheet.shiftRows(at, count) else removedCells = sheet.shiftColumns(at, count)
            // merged ranges moved with the rows; tables and filters here
            sheet.getTables()?.forEach { t -> t.getTableReference()?.let { sheet.shiftRange(it, rows, at, count) } }
            sheet.getAutoFilters().forEach { sheet.shiftRange(it.range, rows, at, count) }
            for ((cell, f) in formulasBefore) {
                val owner = cell.getSheet()?.getSheetName() ?: continue
                cell.formula = RefShifter.shift(f, owner, change)
            }
            val keys = dirtyBefore.mapNotNull { moveKey(it) }
            dirty.clear(); dirty.addAll(keys)
            val fmts = formatsBefore.entries.mapNotNull { (k, v) -> moveKey(k)?.let { it to ArrayList(v) } }
            formats.clear(); fmts.forEach { (k, v) -> formats[k] = v }
            structure.add(write)
            refreshFormulas()
        }
        val revert = {
            structure.remove(write)
            if (rows) {
                sheet.shiftRows(at, -count) // the inverse change; deleted rows come back below
                if (count < 0) sheet.restoreRows(removedRows)
            } else {
                sheet.shiftColumns(at, -count)
                if (count < 0) for ((r, cells) in removedCells) sheet.getRow(r)?.restoreCells(cells)
            }
            for ((range, v) in rangesBefore) {
                range.setFirstRow(v[0]); range.setFirstColumn(v[1]); range.setLastRow(v[2]); range.setLastColumn(v[3])
            }
            for ((cell, f) in formulasBefore) cell.formula = f
            dirty.clear(); dirty.addAll(dirtyBefore)
            formats.clear(); formatsBefore.forEach { (k, v) -> formats[k] = ArrayList(v) }
            refreshFormulas()
        }
        apply()
        undoStack.add(Step.Structure(apply, revert)); redoStack.clear()
        lastError = null
        repaint()
        return true
    }

    /** New engine for the moved formulas; values that changed (#REF! after a delete) are saved. */
    private fun refreshFormulas() {
        engine = XlsxFormulaEngine(book)
        val w = ArrayList<String>()
        for (changed in engine.recalc(w)) dirty.add(Key(changed.sheetIndex, changed.cell.getRowNumber(), changed.cell.getColNumber()))
        warnings = w
        for (i in 0 until book.getSheetCount()) {
            val s = book.getSheet(i) ?: continue
            for (r in s.getFirstRowNum()..s.getLastRowNum()) s.getRow(r)?.cellCollection()?.forEach { it.removeSTRoot() }
        }
    }

    fun sheetIndexOf(sheet: Sheet): Int = book.getSheetIndex(sheet)

    private fun cell(k: Key): Cell? = book.getSheet(k.sheet)?.getRow(k.row)?.getCell(k.col)

    /** What an edit box should show for a cell: "=FORMULA" or the raw value. */
    fun getInput(sheetIndex: Int, row: Int, col: Int): String = inputOf(cell(Key(sheetIndex, row, col)))

    private fun inputOf(cell: Cell?): String {
        if (cell == null) return ""
        cell.formula?.let { return "=$it" }
        return when (cell.getCellType()) {
            Cell.CELL_TYPE_NUMERIC -> cell.getNumberValue().let { if (it == Math.rint(it) && Math.abs(it) < 1e15) it.toLong().toString() else it.toString() }
            Cell.CELL_TYPE_STRING -> engine.adapter.stringOf(cell)
            Cell.CELL_TYPE_BOOLEAN -> if (cell.getBooleanValue()) "TRUE" else "FALSE"
            Cell.CELL_TYPE_ERROR -> com.wxiwei.office.fc.hssf.formula.eval.ErrorEval.getText(cell.getErrorValue())
            else -> ""
        }
    }

    /** Returns false (see [lastError]) for a bad formula or a sheet that does not exist. */
    fun setCellInput(sheetIndex: Int, row: Int, col: Int, input: String): Boolean {
        val key = Key(sheetIndex, row, col)
        val before = inputOf(cell(key))
        if (!apply(key, input)) return false
        undoStack.add(Step.Input(key, before, input)); redoStack.clear()
        return true
    }

    /**
     * Formats one cell (see [CellFormat]); the view shows it at once and [save] writes a new cell
     * format to styles.xml. Returns false (see [lastError]) for an invalid format or cell.
     */
    fun setCellFormat(sheetIndex: Int, row: Int, col: Int, format: CellFormat): Boolean =
        setRangeFormat(sheetIndex, row, col, row, col, format)

    /** Formats every cell of a rectangle as one undoable step. */
    fun setRangeFormat(sheetIndex: Int, row1: Int, col1: Int, row2: Int, col2: Int, format: CellFormat): Boolean {
        format.validate()?.let { return fail(Reason.INVALID_ARGUMENT, it) }
        val sheet = book.getSheet(sheetIndex) ?: return fail(Reason.NOT_FOUND, "Sheet $sheetIndex not found")
        if (minOf(row1, col1, row2, col2) < 0 || maxOf(row1, row2) >= 1048576 || maxOf(col1, col2) >= 16384 ||
            (maxOf(row1, row2) - minOf(row1, row2) + 1L) * (maxOf(col1, col2) - minOf(col1, col2) + 1L) > MAX_FORMAT_CELLS
        ) return fail(Reason.INVALID_ARGUMENT, "Range out of bounds or too large")
        val steps = ArrayList<Step>()
        for (r in minOf(row1, row2)..maxOf(row1, row2)) for (c in minOf(col1, col2)..maxOf(col1, col2)) {
            val key = Key(sheetIndex, r, c)
            val cell = cell(key) ?: create(sheet, key)
            val before = cell.getCellStyleIndex()
            val after = styleFor(cell.getCellStyle(), format)
            val step = Step.Format(key, before, after, format)
            applyFormat(step, true)
            steps.add(step)
        }
        undoStack.add(if (steps.size == 1) steps[0] else Step.Group(steps)); redoStack.clear()
        lastError = null
        repaint()
        return true
    }

    private fun applyFormat(step: Step.Format, redo: Boolean) {
        val cell = cell(step.key) ?: return
        cell.setCellStyle(if (redo) step.afterStyle else step.beforeStyle)
        cell.removeSTRoot()
        val list = formats.getOrPut(step.key) { ArrayList() }
        if (redo) list.add(step.format) else if (list.isNotEmpty()) list.removeAt(list.lastIndex)
    }

    /** A new model style: [base] with [f] applied (fonts are copied, never changed in place). */
    private fun styleFor(base: CellStyle?, f: CellFormat): Int {
        val style = base?.copy() ?: CellStyle()
        if (f.changesFont) {
            val font = (book.getFont(style.getFontIndex().toInt()) ?: Font()).copy()
            f.bold?.let { font.setBold(it) }
            f.italic?.let { font.setItalic(it) }
            f.underline?.let { font.setUnderline(if (it) Font.U_SINGLE.toInt() else Font.U_NONE.toInt()) }
            f.strike?.let { font.setStrikeline(it) }
            f.fontSize?.let { font.setFontSize(it) }
            f.fontColor?.let { font.setColorIndex(book.addColor(argb(it))) }
            val fontIndex = freeIndex { book.getFont(it) != null }
            font.setIndex(fontIndex)
            book.addFont(fontIndex, font)
            style.setFontIndex(fontIndex.toShort())
        }
        f.fillColor?.let {
            style.setFillPattern(if (it == "none") null else BackgroundAndFill().apply {
                fillType = BackgroundAndFill.FILL_SOLID
                foregroundColor = argb(it)
            })
        }
        f.horizontal?.let { style.setHorizontalAlign(it) }
        f.vertical?.let { style.setVerticalAlign(it) }
        f.wrap?.let { style.setWrapText(it) }
        f.numberFormat?.let { code ->
            val builtin = BuiltinFormats.getBuiltinFormat(code)
            style.setNumberFormat(NumberFormat((if (builtin >= 0) builtin else CUSTOM_FORMAT_ID).toShort(), code))
        }
        val index = freeIndex { book.getCellStyle(it) != null }
        style.setIndex(index.toShort())
        book.addCellStyle(index, style)
        return index
    }

    private fun argb(rgb: String): Int = (0xFF shl 24) or rgb.removePrefix("#").toInt(16)

    /** An index the workbook does not use yet (model indexes; the file gets its own on save). */
    private fun freeIndex(used: (Int) -> Boolean): Int {
        var i = FIRST_EDIT_INDEX
        while (used(i)) i++
        return i
    }

    fun clearCell(sheetIndex: Int, row: Int, col: Int) = setCellInput(sheetIndex, row, col, "")

    fun undo(): Boolean {
        val c = undoStack.lastOrNull() ?: return false
        if (!replay(c, false)) return false
        undoStack.removeAt(undoStack.lastIndex); redoStack.add(c); repaint(); return true
    }

    fun redo(): Boolean {
        val c = redoStack.lastOrNull() ?: return false
        if (!replay(c, true)) return false
        redoStack.removeAt(redoStack.lastIndex); undoStack.add(c); repaint(); return true
    }

    private fun replay(step: Step, redo: Boolean): Boolean = when (step) {
        is Step.Input -> apply(step.key, if (redo) step.after else step.before)
        is Step.Format -> { applyFormat(step, redo); true }
        is Step.Group -> (if (redo) step.steps else step.steps.asReversed()).all { replay(it, redo) }
        is Step.Structure -> { if (redo) step.apply() else step.revert(); true }
    }

    private fun fail(reason: Reason, message: String): Boolean { lastError = EditResult.Error(reason, message); return false }

    private fun apply(key: Key, input: String): Boolean {
        val sheet = book.getSheet(key.sheet) ?: return fail(Reason.NOT_FOUND, "Sheet ${key.sheet} not found")
        if (key.row < 0 || key.col < 0 || key.row >= 1048576 || key.col >= 16384) return fail(Reason.INVALID_ARGUMENT, "Cell out of range")
        val text = input.trim()
        val formula = if (text.startsWith("=") && text.length > 1) text.substring(1) else null
        if (formula != null) {
            try { engine.adapter.parse(formula, key.sheet) } catch (e: Exception) {
                return fail(Reason.INVALID_ARGUMENT, "Formula error: ${e.message}")
            }
        }
        val cell = cell(key) ?: create(sheet, key)
        val hadFormula = cell.formula != null
        cell.formula = formula
        when {
            formula != null -> Unit // value comes from the recalculation below
            text.isEmpty() -> { cell.setCellType(Cell.CELL_TYPE_BLANK); cell.setCellValue(null) }
            text.equals("TRUE", true) || text.equals("FALSE", true) -> { cell.setCellType(Cell.CELL_TYPE_BOOLEAN); cell.setCellValue(text.equals("TRUE", true)) }
            number(text) != null -> { cell.setCellType(Cell.CELL_TYPE_NUMERIC); cell.setCellValue(number(text)) }
            else -> { cell.setCellType(Cell.CELL_TYPE_STRING); cell.setCellValue(book.addSharedString(input)) }
        }
        cell.removeSTRoot()
        if (formula != null || hadFormula) engine.formulaChanged(key.sheet, cell) else engine.valueChanged(key.sheet, cell)
        if (formula != null) {
            // Evaluate the edited cell first so a bad reference shows as an error value, not a stale one
            try { engine.store(cell, engine.evaluate(key.sheet, cell)) } catch (e: Exception) {
                cell.setCellType(Cell.CELL_TYPE_ERROR); cell.setCellValue(com.wxiwei.office.fc.ss.usermodel.ErrorConstants.ERROR_NAME.toByte())
            }
        }
        dirty.add(key)
        val w = ArrayList<String>()
        for (changed in engine.recalcAfter(key.sheet, cell, w)) dirty.add(Key(changed.sheetIndex, changed.cell.getRowNumber(), changed.cell.getColNumber()))
        warnings = w
        lastError = null
        repaint()
        return true
    }

    private fun number(text: String): Double? =
        if (text.matches(Regex("[+-]?(\\d+\\.?\\d*|\\.\\d+)([eE][+-]?\\d+)?"))) text.toDoubleOrNull() else null

    private fun create(sheet: Sheet, key: Key): Cell {
        val row = sheet.getRow(key.row) ?: Row(key.col + 1).also {
            it.setRowNumber(key.row); it.setSheet(sheet)
            it.setRowPixelHeight(sheet.getDefaultRowHeight().toFloat())
            it.completed()
            sheet.addRow(it)
        }
        val cell = Cell(Cell.CELL_TYPE_BLANK)
        cell.setSheet(sheet); cell.setRowNumber(key.row); cell.setColNumber(key.col)
        cell.setCellStyle(if (row.getRowStyle() > 0) row.getRowStyle() else sheet.getColumnStyle(key.col))
        row.addCell(cell)
        return cell
    }

    /** Patch the original file with every changed cell. The view already shows it: no reopen needed. */
    fun save(target: File): EditResult {
        val writes = dirty.mapNotNull { k ->
            val cell = cell(k)
            val f = cell?.formula
            when {
                cell == null -> CellWrite.Blank(k.sheet, k.row, k.col)
                cell.getCellType() == Cell.CELL_TYPE_NUMERIC -> CellWrite.Number(k.sheet, k.row, k.col, cell.getNumberValue().let { if (it.isNaN()) 0.0 else it }, f)
                cell.getCellType() == Cell.CELL_TYPE_STRING -> CellWrite.Text(k.sheet, k.row, k.col, engine.adapter.stringOf(cell), f)
                cell.getCellType() == Cell.CELL_TYPE_BOOLEAN -> CellWrite.Bool(k.sheet, k.row, k.col, cell.getBooleanValue(), f)
                cell.getCellType() == Cell.CELL_TYPE_ERROR -> CellWrite.Error(k.sheet, k.row, k.col, cell.getErrorValue(), f)
                f != null -> CellWrite.Number(k.sheet, k.row, k.col, 0.0, f)
                else -> CellWrite.Blank(k.sheet, k.row, k.col)
            }
        }
        val styles = formats.filterValues { it.isNotEmpty() }
            .map { (k, list) -> StyleWrite(k.sheet, k.row, k.col, list.reduce { a, b -> a + b }) }
        val result = XlsxWriter(source) { s, r, c -> book.getSheet(s)?.getRow(r)?.getCell(c)?.formula }.save(target, writes, styles, structure)
        return if (result is EditResult.Ok) result.copy(warnings = warnings) else result
    }

    companion object {
        private const val FIRST_EDIT_INDEX = 20_000 // font and style indexes are Shorts
        private const val CUSTOM_FORMAT_ID = 200
        private const val MAX_FORMAT_CELLS = 100_000L

        private fun spreadsheetOf(control: IControl): Spreadsheet =
            (control.getView() as? ExcelView)?.getSpreadsheet() ?: error("Open an .xlsx first")
    }
}
