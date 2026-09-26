/*
 * 文件名称:          SSTableStyle.java
 *
 * 编译器:            android2.2
 * 时间:              下午1:42:34
 */
package com.wxiwei.office.ss.model.table

/**
 * TODO: 文件注释
 *
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2013-4-18
 * 负责人:           jqin
 */
class SSTableStyle {
    private var band1H: SSTableCellStyle? = null
    private var band2H: SSTableCellStyle? = null
    private var band1V: SSTableCellStyle? = null
    private var band2V: SSTableCellStyle? = null
    private var firstCol: SSTableCellStyle? = null
    private var lastCol: SSTableCellStyle? = null
    private var firstRow: SSTableCellStyle? = null
    private var lastRow: SSTableCellStyle? = null

    fun getBand1H(): SSTableCellStyle? = band1H

    fun setBand1H(band1h: SSTableCellStyle?) {
        band1H = band1h
    }

    fun getBand2H(): SSTableCellStyle? = band2H

    fun setBand2H(band2h: SSTableCellStyle?) {
        band2H = band2h
    }

    fun getBand1V(): SSTableCellStyle? = band1V

    fun setBand1V(band1v: SSTableCellStyle?) {
        band1V = band1v
    }

    fun getBand2V(): SSTableCellStyle? = band2V

    fun setBand2V(band2v: SSTableCellStyle?) {
        band2V = band2v
    }

    fun getFirstCol(): SSTableCellStyle? = firstCol

    fun setFirstCol(firstCol: SSTableCellStyle?) {
        this.firstCol = firstCol
    }

    fun getLastCol(): SSTableCellStyle? = lastCol

    fun setLastCol(lastCol: SSTableCellStyle?) {
        this.lastCol = lastCol
    }

    fun getFirstRow(): SSTableCellStyle? = firstRow

    fun setFirstRow(firstRow: SSTableCellStyle?) {
        this.firstRow = firstRow
    }

    fun getLastRow(): SSTableCellStyle? = lastRow

    fun setLastRow(lastRow: SSTableCellStyle?) {
        this.lastRow = lastRow
    }
}
