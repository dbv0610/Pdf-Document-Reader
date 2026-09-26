/*
 * 文件名称:          ExtendedCellRangeAddress.java
 *
 * 编译器:            android2.2
 * 时间:              下午4:34:27
 */
package com.wxiwei.office.ss.other

import com.wxiwei.office.ss.model.CellRangeAddress
import com.wxiwei.office.ss.model.baseModel.Cell

/**
 * TODO: 文件注释
 *
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2012-6-4
 * 负责人:           jqin
 */
class ExpandedCellRangeAddress(expandedCell: Cell?, firstRow: Int, firstCol: Int, lastRow: Int, lastCol: Int) {
    private var rangeAddr: CellRangeAddress? = CellRangeAddress(firstRow, firstCol, lastRow, lastCol)

    private var expandedCell: Cell? = expandedCell

    fun getRangedAddress(): CellRangeAddress? = rangeAddr

    fun getExpandedCell(): Cell? = expandedCell

    fun dispose() {
        rangeAddr = null

        expandedCell = null
    }
}
