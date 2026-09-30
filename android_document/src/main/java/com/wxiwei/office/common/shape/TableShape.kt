/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:           Table.java
 *  
 * 编译器:             android2.2
 * 时间:               下午5:11:07
 */
package com.wxiwei.office.common.shape

/**
 * Represents a table
 * 
 * 
 * 
 * 
 * Read版本:       Read V1.0
 * 
 * 
 * 作者:           jhy1790
 * 
 * 
 * 日期:           2012-4-11
 * 
 * 
 * 负责人:         jhy1790
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
class TableShape(numrows: Int, numcols: Int) : AbstractShape() {
    /**
     * get type of this shape
     */
    override val type: Short
        get() = AbstractShape.Companion.SHAPE_TABLE

    /**
     * get a table cell
     * @param row
     * @param column
     * @return
     */
    fun getCell(index: Int): TableCell? {
        if (index >= cells!!.size) {
            return null
        }
        return cells!![index]
    }

    /**
     * set a table cell
     * @param row
     * @param column
     * @param tableCell
     */
    fun addCell(index: Int, tableCell: TableCell?) {
        cells!![index] = tableCell
    }

    val cellCount: Int
        /**
         * 
         * @return
         */
        get() = cells!!.size

    /**
     * 
     */
    override fun dispose() {
        if (cells != null) {
            for (i in cells.orEmpty().indices) {
                val cell = cells!![i]
                if (cell != null) {
                    cell.dispose()
                }
            }
            cells = null
        }
    }

    //
    private var cells: Array<TableCell?>?


    var rowCount: Int
    var columnCount: Int
    /**
     * 
     * @return
     */
    /**
     * 
     * @param shape07
     */
    // autoShape is 07 or 03
    var isTable07: Boolean = true

    //table property
    var isFirstRow: Boolean = false
    var isLastRow: Boolean = false
    var isFirstCol: Boolean = false
    var isLastCol: Boolean = false
    var isBandRow: Boolean = false
    var isBandCol: Boolean = false

    /**
     * 
     * @param numrows
     * @param numcols
     */
    init {
        require(numrows >= 1) { "The number of rows must be greater than 1" }
        require(numcols >= 1) { "The number of columns must be greater than 1" }

        this.rowCount = numrows
        this.columnCount = numcols

        cells = arrayOfNulls<TableCell>(numrows * numcols)
    }
}
