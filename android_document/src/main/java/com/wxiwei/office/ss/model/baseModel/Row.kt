/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          Row.java
 *
 * 编译器:            android2.2
 * 时间:              下午3:17:14
 */
package com.wxiwei.office.ss.model.baseModel

import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.constant.SSConstant
import com.wxiwei.office.ss.other.ExpandedCellRangeAddress

/**
 * Row of this sheet
 *
 * Read版本:        Read V1.0
 * 作者:            ljj8494
 * 日期:            2012-2-16
 * 负责人:          ljj8494
 */
open class Row
/**
 * 构造器
 */
(capacity: Int) {
    @JvmField
    protected var sheet: Sheet? = null

    //
    @JvmField
    protected var firstCol: Int = 0

    //
    @JvmField
    protected var lastCol: Int = capacity

    //
    @JvmField
    protected var rowNumber: Int = 0

    //
    @JvmField
    protected var styleIndex: Int = 0

    // 像素值的行高，默认18个像素
    private var rowPixelHeight: Float = SSConstant.DEFAULT_ROW_HEIGHT.toFloat()

    //row propperty
    private var rowProp: RowProperty? = RowProperty()

    // 行中cell
    //cells table
    @JvmField
    protected var cells: CellMap? = CellMap()

    fun setSheet(sheet: Sheet?) {
        this.sheet = sheet
    }

    /**
     * Get the cell representing a given column (logical cell)
     * 0-based. If you ask for a cell that is not defined, then
     * you get a null.
     *
     * This is the basic call, with no policies applied
     *
     * @param cellIndex  0 based column number
     * @param style  whether create a cell by row&column style when called cell is null
     * @return Cell representing that column or null if undefined.
     */
    private fun retrieveCell(cellIndex: Int, style: Boolean): Cell? {
        try {
            if (cellIndex < 0) {
                return null
            }

            var cell = cells!![cellIndex]
            if (cell == null && style) {
                //first check row default style
                cell = createCellByStyle(styleIndex, cellIndex)
                if (cell == null) {
                    //then check column default style
                    cell = createCellByStyle(sheet!!.getColumnStyle(cellIndex), cellIndex)
                }
            }

            return cell
        } catch (e: Exception) {
            return null
        }
    }

    private fun createCellByStyle(styleIndex: Int, column: Int): Cell? {
        var cell: Cell? = null
        val cellStyle = sheet!!.getWorkbook()!!.getCellStyle(styleIndex)
        if (cellStyle != null
            && ((cellStyle.getFillPatternType() == BackgroundAndFill.FILL_SOLID && (cellStyle.getFgColor() and 0xFFFFFF) != 0xFFFFFF)
                    || cellStyle.getBorderLeft() > 0
                    || cellStyle.getBorderTop() > 0
                    || cellStyle.getBorderRight() > 0
                    || cellStyle.getBorderBottom() > 0)
        ) {
            cell = Cell(Cell.CELL_TYPE_NUMERIC)
            cell.setColNumber(column)
            cell.setRowNumber(rowNumber)
            cell.setCellStyle(styleIndex)
            cell.setSheet(sheet)
            cells!![column] = cell
        }

        return cell
    }

    /**
     *
     * @param cellnum
     * @return
     */
    fun getCell(cellnum: Int): Cell? {
        return retrieveCell(cellnum, true)
    }

    /**
     * Get the cell representing a given column (logical cell)
     *  0-based.  If you ask for a cell that is not defined, then
     *  your supplied policy says what to do
     *
     * @param cellnum  0 based column number
     */
    fun getCell(cellnum: Int, style: Boolean): Cell? {
        return retrieveCell(cellnum, style)
    }

    /**
     *
     * @return
     */
    fun cellCollection(): MutableCollection<Cell> {
        return cells!!.values
    }

    /**
     * add Cell
     */
    fun addCell(cell: Cell) {
        val column = cell.getColNumber()
        cells!![column] = cell

        // fix up firstCol and lastCol indexes
        firstCol = Math.min(firstCol, column)
        lastCol = Math.max(lastCol, column + 1)
    }

    /**
     * @return Returns the firstCol.
     */
    fun getFirstCol(): Int = firstCol

    /**
     * @param firstCol The firstCol to set.
     */
    fun setFirstCol(firstCol: Int) {
        this.firstCol = firstCol
    }

    /**
     * @return Returns the lastCol.
     */
    fun getLastCol(): Int = lastCol

    /**
     * @param lastCol The lastCol to set.
     */
    fun setLastCol(lastCol: Int) {
        this.lastCol = lastCol
    }

    /**
     * @return Returns the styleIndex.
     */
    fun getRowStyle(): Int = styleIndex

    /**
     * @param styleIndex The styleIndex to set.
     */
    fun setRowStyle(styleIndex: Int) {
        this.styleIndex = styleIndex
    }

    /**
     * @return Returns the rowPixelHeight.
     */
    fun getRowPixelHeight(): Float = rowPixelHeight

    /**
     * @param rowPixelHeight The rowPixelHeight to set.
     */
    fun setRowPixelHeight(rowPixelHeight: Float) {
        this.rowPixelHeight = rowPixelHeight
    }

    /**
     * @return Returns the rowNumber.
     */
    fun getRowNumber(): Int = rowNumber

    /**
     * @param rowNumber The rowNumber to set.
     */
    fun setRowNumber(rowNumber: Int) {
        this.rowNumber = rowNumber
    }

    /**
     * Moves the cells at or after column [at] by [delta] (insert when > 0). With a negative
     * [delta] the cells of columns at..at-delta-1 are taken out and returned by column.
     */
    fun shiftCells(at: Int, delta: Int): Map<Int, Cell> {
        val cells = cells ?: return emptyMap()
        val removed = HashMap<Int, Cell>()
        if (delta < 0) for (c in at until at - delta) cells.remove(c)?.let { removed[c] = it }
        val from = if (delta < 0) at - delta else at
        val moving = cells.keys.filter { it >= from }.sorted().let { if (delta > 0) it.reversed() else it }
        for (k in moving) {
            val cell = cells.remove(k) ?: continue
            cell.setColNumber(k + delta)
            cells[k + delta] = cell
        }
        return removed
    }

    /** Puts cells taken out by [shiftCells] back (after the columns were inserted again). */
    fun restoreCells(cells: Map<Int, Cell>) {
        for ((c, cell) in cells) {
            cell.setColNumber(c)
            this.cells!![c] = cell
        }
    }

    /**
     * @return Returns the isEmpty.
     */
    fun isEmpty(): Boolean {
        return cells!!.size == 0
    }

    /**
     * @return Returns the zeroHeight.
     */
    fun isZeroHeight(): Boolean {
        return rowProp!!.isZeroHeight()
    }

    /**
     * @param zeroHeight The zeroHeight to set.
     */
    fun setZeroHeight(zeroHeight: Boolean) {
        rowProp!!.setRowProperty(RowProperty.ROWPROPID_ZEROHEIGHT, zeroHeight)
    }

    /**
     * @return Returns the physicalNumberOfCells.
     */
    fun getPhysicalNumberOfCells(): Int {
        return cells!!.size
    }

    /**
     * clear all cells
     */
    fun removeAllCells() {
        val cellCollection: Collection<Cell> = cells!!.values
        for (cell in cellCollection) {
            cell.dispose()
        }
        cells!!.clear()
    }

    /**
     * remove all cells of hidden row, except one which is the first row&col of merged cell
     */
    fun removeCellsForHiddenRow() {
        if (!rowProp!!.isZeroHeight()) {
            return
        }

        val cellCollection: Collection<Cell> = cells!!.values
        for (cell in cellCollection) {
            if (cell.getRangeAddressIndex() >= 0) {
                continue
            }

            cell.dispose()
        }
    }

    /**
     *
     */
    fun completed() {
        rowProp!!.setRowProperty(RowProperty.ROWPROPID_COMPLETED, true)
    }

    /**
     *
     * @return
     */
    fun isCompleted(): Boolean {
        return rowProp!!.isCompleted()
    }

    /**
     * has checked cells whose widths extend cell itself width, so we need extend its width to layout cell contents
     * @param init
     */
    fun setInitExpandedRangeAddress(init: Boolean) {
        rowProp!!.setRowProperty(RowProperty.ROWPROPID_INITEXPANDEDRANGEADDR, init)
    }

    /**
     *
     * @return
     */
    fun isInitExpandedRangeAddress(): Boolean {
        return rowProp!!.isInitExpandedRangeAddr()
    }

    /**
     *
     * @param index
     * @param addr
     */
    fun addExpandedRangeAddress(index: Int, addr: ExpandedCellRangeAddress?) {
        rowProp!!.setRowProperty(RowProperty.ROWPROPID_EXPANDEDRANGEADDRLIST, addr)
    }

    /**
     *
     * @return
     */
    fun getExpandedCellCount(): Int {
        return rowProp!!.getExpandedCellCount()
    }

    /**
     *
     * @param index
     * @return
     */
    fun getExpandedRangeAddress(index: Int): ExpandedCellRangeAddress? {
        return rowProp!!.getExpandedCellRangeAddr(index)
    }

    /**
     *
     */
    open fun dispose() {
        removeAllCells()
        if (rowProp != null) {
            rowProp!!.dispose()
            rowProp = null
        }
        sheet = null
        cells = null
    }

    override fun toString(): String {
        return "Row{" +
                ", firstCol=" + firstCol +
                ", lastCol=" + lastCol +
                ", rowNumber=" + rowNumber +
                ", rowPixelHeight=" + rowPixelHeight +
                '}'
    }
}
