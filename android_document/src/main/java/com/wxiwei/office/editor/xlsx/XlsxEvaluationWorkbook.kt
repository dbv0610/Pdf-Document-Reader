/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
package com.wxiwei.office.editor.xlsx

import com.wxiwei.office.fc.hssf.formula.EvaluationCell
import com.wxiwei.office.fc.hssf.formula.EvaluationName
import com.wxiwei.office.fc.hssf.formula.EvaluationSheet
import com.wxiwei.office.fc.hssf.formula.EvaluationWorkbook
import com.wxiwei.office.fc.hssf.formula.FormulaParser
import com.wxiwei.office.fc.hssf.formula.FormulaParsingWorkbook
import com.wxiwei.office.fc.hssf.formula.FormulaType
import com.wxiwei.office.fc.hssf.formula.ptg.NamePtg
import com.wxiwei.office.fc.hssf.formula.ptg.NameXPtg
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.formula.udf.UDFFinder
import com.wxiwei.office.fc.ss.SpreadsheetVersion
import com.wxiwei.office.fc.ss.usermodel.ICell
import com.wxiwei.office.ss.model.baseModel.Cell
import com.wxiwei.office.ss.model.baseModel.Sheet
import com.wxiwei.office.ss.model.baseModel.Workbook
import java.util.IdentityHashMap

/**
 * Lets the bundled POI formula engine (fc/hssf/formula) read the XLSX model (ss/model/baseModel),
 * like HSSFEvaluationWorkbook does for .xls. External sheet indexes are plain sheet indexes.
 * Defined names and links to other workbooks are not supported (they evaluate to #NAME? / #REF!).
 */
class XlsxEvaluationWorkbook(val book: Workbook) : EvaluationWorkbook, FormulaParsingWorkbook {
    private val sheets = HashMap<Int, SheetAdapter>()
    private val tokens = IdentityHashMap<Cell, Pair<String, Array<Ptg?>>>()

    inner class SheetAdapter(val index: Int, val sheet: Sheet) : EvaluationSheet {
        override fun getCell(rowIndex: Int, columnIndex: Int): EvaluationCell? =
            sheet.getRow(rowIndex)?.getCell(columnIndex)?.let { CellAdapter(this, it) }
    }

    inner class CellAdapter(private val owner: SheetAdapter, val cell: Cell) : EvaluationCell {
        @Suppress("INAPPLICABLE_JVM_NAME")
        @get:JvmName("getIdentityKeyProperty")
        override val identityKey: Any get() = cell
        @Suppress("INAPPLICABLE_JVM_NAME")
        @get:JvmName("getSheetProperty")
        override val sheet: EvaluationSheet get() = owner
        @Suppress("INAPPLICABLE_JVM_NAME")
        @get:JvmName("getRowIndexProperty")
        override val rowIndex: Int get() = cell.getRowNumber()
        @Suppress("INAPPLICABLE_JVM_NAME")
        @get:JvmName("getColumnIndexProperty")
        override val columnIndex: Int get() = cell.getColNumber()
        @Suppress("INAPPLICABLE_JVM_NAME")
        @get:JvmName("getCellTypeProperty")
        override val cellType: Int get() = if (cell.formula != null) ICell.CELL_TYPE_FORMULA else when (cell.getCellType()) {
            Cell.CELL_TYPE_NUMERIC -> ICell.CELL_TYPE_NUMERIC
            Cell.CELL_TYPE_STRING -> ICell.CELL_TYPE_STRING
            Cell.CELL_TYPE_BOOLEAN -> ICell.CELL_TYPE_BOOLEAN
            Cell.CELL_TYPE_ERROR -> ICell.CELL_TYPE_ERROR
            else -> ICell.CELL_TYPE_BLANK
        }
        @Suppress("INAPPLICABLE_JVM_NAME")
        @get:JvmName("getNumericCellValueProperty")
        override val numericCellValue: Double get() = cell.getNumberValue().let { if (it.isNaN()) 0.0 else it }
        @Suppress("INAPPLICABLE_JVM_NAME")
        @get:JvmName("getStringCellValueProperty")
        override val stringCellValue: String get() = stringOf(cell)
        @Suppress("INAPPLICABLE_JVM_NAME")
        @get:JvmName("getBooleanCellValueProperty")
        override val booleanCellValue: Boolean get() = cell.getBooleanValue()
        @Suppress("INAPPLICABLE_JVM_NAME")
        @get:JvmName("getErrorCellValueProperty")
        override val errorCellValue: Int get() = cell.getErrorValue()
    }

    fun stringOf(cell: Cell): String {
        val index = cell.getStringCellValueIndex()
        return if (index >= 0) book.getSharedString(index).orEmpty() else ""
    }

    fun sheetAdapter(index: Int): SheetAdapter? {
        val sheet = book.getSheet(index) ?: return null
        return sheets.getOrPut(index) { SheetAdapter(index, sheet) }.takeIf { it.sheet === sheet }
            ?: SheetAdapter(index, sheet).also { sheets[index] = it }
    }

    fun cellAdapter(sheetIndex: Int, cell: Cell): CellAdapter? = sheetAdapter(sheetIndex)?.let { CellAdapter(it, cell) }

    /** Parses [formula] (without "=") in the context of [sheetIndex]; throws FormulaParseException on bad syntax. */
    fun parse(formula: String, sheetIndex: Int): Array<Ptg?> = FormulaParser.parse(formula, this, FormulaType.CELL, sheetIndex)!!

    /** Forget the parsed tokens of a cell whose formula changed. */
    fun forget(cell: Cell) { tokens.remove(cell) }

    override fun getSheetName(sheetIndex: Int): String = book.getSheet(sheetIndex)?.getSheetName().orEmpty()
    override fun getSheetIndex(sheet: EvaluationSheet?): Int = (sheet as SheetAdapter).index
    override fun getSheetIndex(sheetName: String?): Int {
        for (i in 0 until book.getSheetCount()) if (book.getSheet(i)?.getSheetName().equals(sheetName, ignoreCase = true)) return i
        return -1
    }
    override fun getSheet(sheetIndex: Int): EvaluationSheet? = sheetAdapter(sheetIndex)
    override fun getExternalSheet(externSheetIndex: Int): EvaluationWorkbook.ExternalSheet? = null
    override fun convertFromExternSheetIndex(externSheetIndex: Int) = externSheetIndex
    override fun getExternalName(externSheetIndex: Int, externNameIndex: Int): EvaluationWorkbook.ExternalName? = null
    override fun getName(namePtg: NamePtg?): EvaluationName? = null
    override fun getName(name: String?, sheetIndex: Int): EvaluationName? = null
    override fun resolveNameXText(ptg: NameXPtg?): String? = null
    override fun getFormulaTokens(cell: EvaluationCell?): Array<Ptg?> {
        val model = (cell as CellAdapter).cell
        val formula = model.formula ?: return emptyArray()
        tokens[model]?.let { (text, ptgs) -> if (text == formula) return ptgs }
        return clampWholeColumns(parse(formula, (cell.sheet as SheetAdapter).index), (cell.sheet as SheetAdapter).index)
            .also { tokens[model] = formula to it }
    }

    /** Last row a whole-column clamp assumed, per sheet: a cell written below it invalidates the tokens. */
    private val clampRow = HashMap<Int, Int>()

    /**
     * "$E:$E" means rows 1..1048576, but a sheet only has data up to its last row. Lookups like
     * MATCH(x, $E:$E, 0) would otherwise walk a million empty rows for every formula.
     */
    private fun clampWholeColumns(ptgs: Array<Ptg?>, sheetIndex: Int): Array<Ptg?> {
        for (ptg in ptgs) {
            if (ptg !is com.wxiwei.office.fc.hssf.formula.ptg.AreaPtgBase) continue
            if (ptg.firstRow != 0 || ptg.lastRow < SpreadsheetVersion.EXCEL2007.getLastRowIndex()) continue
            val target = if (ptg is com.wxiwei.office.fc.hssf.formula.ptg.Area3DPtg) ptg.externSheetIndex else sheetIndex
            val sheet = book.getSheet(target) ?: continue
            if (sheet.getState() != Sheet.State_Accomplished) continue // its last row is not final yet
            val last = maxOf(0, sheet.getLastRowNum())
            ptg.setLastRow(last)
            clampRow[target] = maxOf(clampRow[target] ?: 0, last)
        }
        return ptgs
    }

    /** A cell was written at [row]: re-parse formulas if a whole-column clamp no longer covers it. */
    fun rowTouched(sheetIndex: Int, row: Int): Boolean {
        val limit = clampRow[sheetIndex] ?: return false
        if (row <= limit) return false
        tokens.clear(); clampRow.clear()
        return true
    }

    /** Parsed tokens of a formula cell (cached), for dependency analysis. */
    fun tokensOf(sheetIndex: Int, cell: Cell): Array<Ptg?>? = cellAdapter(sheetIndex, cell)?.let { getFormulaTokens(it) }
    // Own instance: WorkbookEvaluator adds finders into it, and doing that to the shared
    // UDFFinder.DEFAULT made it contain itself (endless lookup for unknown functions)
    private val udf = com.wxiwei.office.fc.hssf.formula.udf.AggregatingUDFFinder(com.wxiwei.office.fc.hssf.formula.atp.AnalysisToolPak.instance)
    override val uDFFinder: UDFFinder get() = udf
    override fun getNameXPtg(name: String?): NameXPtg? = null
    override fun getExternalSheetIndex(sheetName: String?): Int = getSheetIndex(sheetName)
    override fun getExternalSheetIndex(workbookName: String?, sheetName: String?): Int = -1
    override val spreadsheetVersion: SpreadsheetVersion get() = SpreadsheetVersion.EXCEL2007
}
