package com.wxiwei.office.editor.xlsx

import com.wxiwei.office.editor.EditResult
import com.wxiwei.office.editor.Reason
import com.wxiwei.office.ss.control.ExcelView
import com.wxiwei.office.ss.control.Spreadsheet
import com.wxiwei.office.ss.model.baseModel.Cell
import com.wxiwei.office.ss.model.baseModel.Row
import com.wxiwei.office.ss.model.baseModel.Sheet
import com.wxiwei.office.ss.model.baseModel.Workbook
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

    val engine = XlsxFormulaEngine(book)
    /** Warnings of the last recalculation (unsupported function, sheet still loading...). */
    var warnings: List<String> = emptyList(); private set
    var lastError: EditResult.Error? = null; private set

    private data class Key(val sheet: Int, val row: Int, val col: Int)
    private data class Change(val key: Key, val before: String, val after: String)
    private val undoStack = ArrayList<Change>()
    private val redoStack = ArrayList<Change>()
    /** Cells typed by the user plus formula cells whose result changed: everything [save] writes. */
    private val dirty = LinkedHashSet<Key>()

    fun canUndo() = undoStack.isNotEmpty()
    fun canRedo() = redoStack.isNotEmpty()
    fun hasChanges() = undoStack.isNotEmpty() || dirty.isNotEmpty()

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
        undoStack.add(Change(key, before, input)); redoStack.clear()
        return true
    }

    fun clearCell(sheetIndex: Int, row: Int, col: Int) = setCellInput(sheetIndex, row, col, "")

    fun undo(): Boolean {
        val c = undoStack.lastOrNull() ?: return false
        if (!apply(c.key, c.before)) return false
        undoStack.removeAt(undoStack.lastIndex); redoStack.add(c); return true
    }

    fun redo(): Boolean {
        val c = redoStack.lastOrNull() ?: return false
        if (!apply(c.key, c.after)) return false
        redoStack.removeAt(redoStack.lastIndex); undoStack.add(c); return true
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
                cell.setCellType(Cell.CELL_TYPE_ERROR); cell.setCellValue(com.wxiwei.office.fc.hssf.usermodel.HSSFErrorConstants.ERROR_NAME.toByte())
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
        val result = XlsxWriter(source) { s, r, c -> book.getSheet(s)?.getRow(r)?.getCell(c)?.formula }.save(target, writes)
        return if (result is EditResult.Ok) result.copy(warnings = warnings) else result
    }

    companion object {
        private fun spreadsheetOf(control: IControl): Spreadsheet =
            (control.getView() as? ExcelView)?.getSpreadsheet() ?: error("Open an .xlsx first")
    }
}
