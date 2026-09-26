/*
 * 文件名称:          ModelUtil.java
 *
 * 编译器:            android2.2
 * 时间:              下午3:23:24
 */
package com.wxiwei.office.ss.util

import android.graphics.Rect
import android.graphics.RectF
import com.wxiwei.office.constant.SSConstant
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.ss.model.CellRangeAddress
import com.wxiwei.office.ss.model.baseModel.Cell
import com.wxiwei.office.ss.model.baseModel.Sheet
import com.wxiwei.office.ss.model.baseModel.Workbook
import com.wxiwei.office.ss.model.drawing.CellAnchor
import com.wxiwei.office.ss.util.format.NumericFormatter
import com.wxiwei.office.ss.view.SheetView
import java.util.Locale
import java.util.LinkedHashMap

/**
 * excel用到工具类
 *
 * Read版本:        Read V1.0
 * 作者:            ljj8494
 * 日期:            2011-11-10
 * 负责人:          ljj8494
 */
class ModelUtil {
    private val area = RectF()
    private data class NumericFormatKey(
        val format: String,
        val number: Double,
        val numericType: Short,
        val date1904: Boolean
    )

    private val numericFormatCache = object : LinkedHashMap<NumericFormatKey, String>(256, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<NumericFormatKey, String>?): Boolean {
            return size > 512
        }
    }

    /**
     * 得到合交单元的CellRangeAddress的Index
     * @param sheet
     * @param row
     * @param column
     * @return
     */
    fun getCellRangeAddressIndex(sheet: Sheet, row: Int, column: Int): Int {
        val len = sheet.getMergeRangeCount()
        for (i in 0 until len) {
            val cra = sheet.getMergeRange(i)
            if (containsCell(cra!!, row, column)) {
                return i
            }
        }
        return -1
    }

    /**
     * 得到合交单元的CellRangeAddress
     * @param sheet
     * @param row
     * @param column
     * @return
     */
    fun getCellRangeAddress(sheet: Sheet, row: Int, column: Int): CellRangeAddress? {
        val len = sheet.getMergeRangeCount()
        for (i in 0 until len) {
            val cra = sheet.getMergeRange(i)
            if (containsCell(cra!!, row, column)) {
                return cra
            }
        }
        return null
    }

    /**
     *
     * @param cr
     * @param row
     * @param col
     * @return
     */
    fun containsCell(cr: CellRangeAddress, row: Int, col: Int): Boolean {
        return row >= cr.getFirstRow() && row <= cr.getLastRow()
                && col >= cr.getFirstColumn() && col <= cr.getLastColumn()
    }

    /**
     *
     * @param sheetview
     * @param cellRangeAddress
     * @return
     */
    fun getCellRangeAddressAnchor(sheetview: SheetView, cellRangeAddress: CellRangeAddress): RectF {
        //left and right of rect
        area.left = getValueX(sheetview, cellRangeAddress.getFirstColumn(), 0f)
        area.top = getValueY(sheetview, cellRangeAddress.getFirstRow(), 0f)

        area.right = getValueX(sheetview, cellRangeAddress.getLastColumn() + 1, 0f)
        area.bottom = getValueY(sheetview, cellRangeAddress.getLastRow() + 1, 0f)

        return area
    }

    /**
     *
     * @param sheet
     * @param cellRangeAddress
     * @return
     */
    private fun getCellRangeAddressAnchor(sheet: Sheet, cellRangeAddress: CellRangeAddress): Rect {
        val area = Rect()
        //left and right of rect
        area.left = Math.round(getValueX(sheet, cellRangeAddress.getFirstColumn(), 0))
        area.top = Math.round(getValueY(sheet, cellRangeAddress.getFirstRow(), 0))

        area.right = Math.round(getValueX(sheet, cellRangeAddress.getLastColumn() + 1, 0))
        area.bottom = Math.round(getValueY(sheet, cellRangeAddress.getLastRow() + 1, 0))

        return area
    }

    /**
     * area based on the 0 column of the 0 row
     * @param sheet
     * @param cellAnchor
     * @return
     */
    fun getCellAnchor(sheet: Sheet, cellAnchor: CellAnchor?): Rectangle? {
        val area = Rectangle()
        if (cellAnchor == null) {
            return null
        }

        //left and right of rect
        area.x = Math.round(getValueX(sheet, cellAnchor.getStart()!!.getColumn().toInt(), cellAnchor.getStart()!!.getDX()))
        area.y = Math.round(getValueY(sheet, cellAnchor.getStart()!!.getRow(), cellAnchor.getStart()!!.getDY()))
        if (cellAnchor.getType() == CellAnchor.TWOCELLANCHOR) {
            area.width = Math.round(getValueX(sheet, cellAnchor.getEnd()!!.getColumn().toInt(), cellAnchor.getEnd()!!.getDX()) - area.x)
            area.height = Math.round(getValueY(sheet, cellAnchor.getEnd()!!.getRow(), cellAnchor.getEnd()!!.getDY()) - area.y)
        } else if (cellAnchor.getType() == CellAnchor.ONECELLANCHOR) {
            area.width = cellAnchor.getWidth()
            area.height = cellAnchor.getHeight()
        }

        return area
    }

    /**
     *
     * @param sheetview
     * @param cell
     * @return
     */
    fun getCellAnchor(sheetview: SheetView, cell: Cell?): RectF? {
        if (cell == null) {
            return null
        }

        if (cell.getRangeAddressIndex() >= 0) {
            return getCellRangeAddressAnchor(sheetview, sheetview.getCurrentSheet()!!.getMergeRange(cell.getRangeAddressIndex())!!)
        } else {
            //left and right of rect
            area.left = getValueX(sheetview, cell.getColNumber(), 0f)
            area.top = getValueY(sheetview, cell.getRowNumber(), 0f)

            area.right = getValueX(sheetview, cell.getColNumber() + 1, 0f)
            area.bottom = getValueY(sheetview, cell.getRowNumber() + 1, 0f)

            return area
        }
    }

    /**
     * a relatively rect area based on view left-top
     * @param sheetview
     * @param row
     * @param column
     * @return
     */
    fun getCellAnchor(sheetview: SheetView, row: Int, column: Int): RectF {
        val sheet = sheetview.getCurrentSheet()!!
        if (sheet.getRow(row) != null && sheet.getRow(row)!!.getCell(column) != null) {
            val cell = sheet.getRow(row)!!.getCell(column)!!
            if (cell.getRangeAddressIndex() >= 0) {
                return getCellRangeAddressAnchor(sheetview, sheet.getMergeRange(cell.getRangeAddressIndex())!!)
            }
        }

        //left and right of rect
        area.left = getValueX(sheetview, column, 0f)
        area.top = getValueY(sheetview, row, 0f)

        area.right = getValueX(sheetview, column + 1, 0f)
        area.bottom = getValueY(sheetview, row + 1, 0f)

        return area
    }

    /**
     * a relatively rect area based on view left-top
     * @param sheetview
     * @param row
     * @param column1
     * @param column2
     * @return
     */
    fun getCellAnchor(sheetview: SheetView, row: Int, column1: Int, column2: Int): RectF {
        var column2 = column2
        val sheet = sheetview.getCurrentSheet()!!
        if (sheet.getRow(row) != null && sheet.getRow(row)!!.getCell(column1) != null && sheet.getRow(row)!!.getCell(column2) != null) {
            val cell = sheet.getRow(row)!!.getCell(column2)!!
            if (cell.getRangeAddressIndex() >= 0) {
                column2 = sheet.getMergeRange(cell.getRangeAddressIndex())!!.getLastColumn()
            }
        }

        //left and right of rect
        area.left = getValueX(sheetview, column1, 0f)
        area.top = getValueY(sheetview, row, 0f)

        area.right = getValueX(sheetview, column2 + 1, 0f)
        area.bottom = getValueY(sheetview, row + 1, 0f)

        return area
    }

    /**
     * an absolute rect area based on body left-top
     * @param sheet
     * @param row
     * @param column
     * @return
     */
    fun getCellAnchor(sheet: Sheet, row: Int, column: Int): Rect {
        if (sheet.getRow(row) != null && sheet.getRow(row)!!.getCell(column) != null) {
            val cell = sheet.getRow(row)!!.getCell(column)!!
            if (cell.getRangeAddressIndex() >= 0) {
                return getCellRangeAddressAnchor(sheet, sheet.getMergeRange(cell.getRangeAddressIndex())!!)
            }
        }

        val area = Rect()
        //left and right of rect
        area.left = Math.round(getValueX(sheet, column, 0))
        area.top = Math.round(getValueY(sheet, row, 0))

        area.right = Math.round(getValueX(sheet, column + 1, 0))
        area.bottom = Math.round(getValueY(sheet, row + 1, 0))

        return area
    }

    /**
     *
     * @param sheet
     * @param row
     * @param column
     * @param ignoreMergerCell
     * @return
     */
    fun getCellAnchor(sheet: Sheet, row: Int, column: Int, ignoreMergerCell: Boolean): Rect? {
        if (!ignoreMergerCell && sheet.getRow(row) != null && sheet.getRow(row)!!.getCell(column) != null) {
            val cell = sheet.getRow(row)!!.getCell(column)!!
            if (cell.getRangeAddressIndex() >= 0) {
                return getCellRangeAddressAnchor(sheet, sheet.getMergeRange(cell.getRangeAddressIndex())!!)
            }

            return null
        } else {
            val area = Rect()
            //left and right of rect
            area.left = Math.round(getValueX(sheet, column, 0))
            area.top = Math.round(getValueY(sheet, row, 0))

            area.right = Math.round(getValueX(sheet, column + 1, 0))
            area.bottom = Math.round(getValueY(sheet, row + 1, 0))

            return area
        }
    }

    /**
     *
     * @param columnIndex
     * @param dx
     * @return
     */
    private fun getValueX(sheet: Sheet, columnIndex: Int, dx: Int): Float {
        var x = 0f
        for (i in 0 until columnIndex) {
            if (sheet.isColumnHidden(i)) {
                continue
            }

            x += sheet.getColumnPixelWidth(i)
        }

        return dx + x
    }

    private fun getValueY(sheet: Sheet, rowIndex: Int, dy: Int): Float {
        var y = 0f
        var h = 0f
        for (i in 0 until rowIndex) {
            val row = sheet.getRow(i)
            if (row != null && row.isZeroHeight()) {
                continue
            }

            h = if (row == null) sheet.getDefaultRowHeight().toFloat() else row.getRowPixelHeight()
            y += h
        }

        return y + dy
    }

    fun getValueX(sheetview: SheetView, columnIndex: Int, dx: Float): Float {
        var x = sheetview.getRowHeaderWidth().toFloat()
        var w = 0f
        val sheet = sheetview.getCurrentSheet()!!

        val minRowAndColumnInformation = sheetview.getMinRowAndColumnInformation()!!
        var colStart = if (minRowAndColumnInformation.getMinColumnIndex() > 0) minRowAndColumnInformation.getMinColumnIndex() else 0
        if (colStart < columnIndex && !minRowAndColumnInformation.isColumnAllVisible()) {
            colStart += 1
            x += (minRowAndColumnInformation.getVisibleColumnWidth() * sheetview.getZoom()).toFloat()
        }

        val maxColumns = if (sheet.getWorkbook()!!.isBefore07Version()) Workbook.MAXCOLUMN_03 else Workbook.MAXCOLUMN_07
        while (colStart < columnIndex && colStart <= maxColumns) {
            if (sheet.isColumnHidden(colStart)) {
                colStart++
                continue
            }

            w = (sheet.getColumnPixelWidth(colStart) * sheetview.getZoom())
            x += w
            colStart++
        }

        return dx + x
    }

    /**
     *
     * @param rowIndex
     * @param dy
     * @return
     */
    fun getValueY(sheetview: SheetView, rowIndex: Int, dy: Float): Float {
        var y = (SSConstant.DEFAULT_COLUMN_HEADER_HEIGHT * sheetview.getZoom())
        var h = 0f
        val sheet = sheetview.getCurrentSheet()!!
        var row: com.wxiwei.office.ss.model.baseModel.Row?

        val minRowAndColumnInformation = sheetview.getMinRowAndColumnInformation()!!
        var rowStart = if (minRowAndColumnInformation.getMinRowIndex() > 0) minRowAndColumnInformation.getMinRowIndex() else 0
        if (rowStart < rowIndex && !minRowAndColumnInformation.isRowAllVisible()) {
            rowStart += 1
            y += (minRowAndColumnInformation.getVisibleRowHeight() * sheetview.getZoom()).toFloat()
        }

        val maxRows = if (sheet.getWorkbook()!!.isBefore07Version()) Workbook.MAXROW_03 else Workbook.MAXROW_07
        while (rowStart < rowIndex && rowStart <= maxRows) {
            row = sheet.getRow(rowStart)
            if (row != null && row.isZeroHeight()) {
                rowStart++
                continue
            }

            h = if (row == null) sheetview.getCurrentSheet()!!.getDefaultRowHeight().toFloat() else row.getRowPixelHeight()
            h = (h * sheetview.getZoom())
            y += h
            rowStart++
        }

        return y + dy
    }

    /**
     *
     * @param cell
     * @return
     */
    fun getFormatContents(book: Workbook, cell: Cell): String? {
        //
        if (!cell.hasValidValue()) {
            return null
        }

        val style = cell.getCellStyle()
        var value: String? = ""
        val numericType: Short
        when (cell.getCellType()) {
            Cell.CELL_TYPE_BOOLEAN -> value = cell.getBooleanValue().toString().uppercase(Locale.getDefault())

            Cell.CELL_TYPE_NUMERIC -> {
                var key = style!!.getFormatCode()
                if (key == null) {
                    key = "General"
                    numericType = Cell.CELL_TYPE_NUMERIC_GENERAL
                } else {
                    if (cell.getCellNumericType() > 0) {
                        numericType = cell.getCellNumericType()
                    } else {
                        numericType = NumericFormatter.instance().getNumericCellType(key)
                        cell.setCellNumericType(numericType)
                    }
                }

                try {
                    val number = cell.getNumberValue()
                    val date1904 = book.isUsing1904DateWindowing()
                    val cacheKey = NumericFormatKey(key, number, numericType, date1904)
                    val cached = synchronized(numericFormatCache) { numericFormatCache[cacheKey] }
                    value = cached ?: if (numericType == Cell.CELL_TYPE_NUMERIC_SIMPLEDATE) {
                        NumericFormatter.instance().getFormatContents(key, cell.getDateCellValue(date1904))
                    } else {
                        NumericFormatter.instance().getFormatContents(key, number, numericType)
                    }
                    if (cached == null && value != null) {
                        synchronized(numericFormatCache) { numericFormatCache[cacheKey] = value!! }
                    }
                    if (numericType == Cell.CELL_TYPE_NUMERIC_SIMPLEDATE) {
                        //store string content, so no need to convert any more
                        cell.setCellType(Cell.CELL_TYPE_STRING)
                        cell.setCellValue(book.addSharedString(value))
                    }
                } catch (ex: Exception) {
                    value = cell.getNumberValue().toString()
                }
            }

            Cell.CELL_TYPE_STRING -> if (cell.getStringCellValueIndex() >= 0) {
                value = book.getSharedString(cell.getStringCellValueIndex())
            }

            Cell.CELL_TYPE_FORMULA -> {
            }

            Cell.CELL_TYPE_ERROR -> value = ErrorEval.getText(cell.getErrorValue())

            else -> {
            }
        }

        return value
    }

    companion object {
        //
        private val mu = ModelUtil()

        //
        @JvmStatic
        fun instance(): ModelUtil {
            return mu
        }

        @JvmStatic
        fun processRect(rect: Rectangle, angle: Float): Rectangle {
            var angle = angle
            angle = angle % 360
            if ((angle > 45 && angle <= 135) || (angle > 225 && angle < 315)) {
                val centerX = rect.getCenterX()
                val centerY = rect.getCenterY()

                rect.x = Math.round(centerX - rect.height / 2).toInt()
                rect.y = Math.round(centerY - rect.width / 2).toInt()
                val temp = rect.width
                rect.width = rect.height
                rect.height = temp
            }
            return rect
        }
    }
}
