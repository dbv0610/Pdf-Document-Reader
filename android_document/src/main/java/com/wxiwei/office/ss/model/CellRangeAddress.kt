/*
 * 文件名称:          CellRangeAddress.java
 *
 * 编译器:            android2.2
 * 时间:              下午7:49:30
 */
package com.wxiwei.office.ss.model

/**
 * 合并单元
 *
 * Read版本:        Read V1.0
 * 作者:            ljj8494
 * 日期:            2012-2-21
 * 负责人:          ljj8494
 */
class CellRangeAddress
/**
 *
 * @param firstRow
 * @param firstCol
 * @param lastRow
 * @param lastCol
 */
(
    private var firstRow: Int,
    private var firstCol: Int,
    private var lastRow: Int,
    private var lastCol: Int
) {
    /**
     * @return Returns the firstRow.
     */
    fun getFirstRow(): Int = firstRow

    /**
     * @param firstRow The firstRow to set.
     */
    fun setFirstRow(firstRow: Int) {
        this.firstRow = firstRow
    }

    /**
     * @return Returns the firstCol.
     */
    fun getFirstColumn(): Int = firstCol

    /**
     * @param firstCol The firstCol to set.
     */
    fun setFirstColumn(firstCol: Int) {
        this.firstCol = firstCol
    }

    /**
     * @return Returns the lastRow.
     */
    fun getLastRow(): Int = lastRow

    /**
     * @param lastRow The lastRow to set.
     */
    fun setLastRow(lastRow: Int) {
        this.lastRow = lastRow
    }

    /**
     * @return Returns the lastCol.
     */
    fun getLastColumn(): Int = lastCol

    /**
     * @param lastCol The lastCol to set.
     */
    fun setLastColumn(lastCol: Int) {
        this.lastCol = lastCol
    }

    /**
     *
     * @param rowInd
     * @param colInd
     * @return
     */
    fun isInRange(rowInd: Int, colInd: Int): Boolean {
        return firstRow <= rowInd && rowInd <= lastRow &&
                firstCol <= colInd && colInd <= lastCol
    }

    fun dispose() {
    }
}
