package com.wxiwei.office.pg.model.tableStyle

class TableStyle {
    private var wholeTable: TableCellStyle? = null
    private var band1H: TableCellStyle? = null
    private var band2H: TableCellStyle? = null
    private var band1V: TableCellStyle? = null
    private var band2V: TableCellStyle? = null
    private var firstCol: TableCellStyle? = null
    private var lastCol: TableCellStyle? = null
    private var firstRow: TableCellStyle? = null
    private var lastRow: TableCellStyle? = null
    fun getWholeTable() = wholeTable
    fun setWholeTable(v: TableCellStyle?) { wholeTable = v }
    fun getBand1H() = band1H
    fun setBand1H(v: TableCellStyle?) { band1H = v }
    fun getBand2H() = band2H
    fun setBand2H(v: TableCellStyle?) { band2H = v }
    fun getBand1V() = band1V
    fun setBand1V(v: TableCellStyle?) { band1V = v }
    fun getBand2V() = band2V
    fun setBand2V(v: TableCellStyle?) { band2V = v }
    fun getFirstCol() = firstCol
    fun setFirstCol(v: TableCellStyle?) { firstCol = v }
    fun getLastCol() = lastCol
    fun setLastCol(v: TableCellStyle?) { lastCol = v }
    fun getFirstRow() = firstRow
    fun setFirstRow(v: TableCellStyle?) { firstRow = v }
    fun getLastRow() = lastRow
    fun setLastRow(v: TableCellStyle?) { lastRow = v }
    fun dispose() {
        wholeTable?.dispose(); wholeTable = null
        band1H?.dispose(); band1H = null
        band2H?.dispose(); band2H = null
        band1V?.dispose(); band1V = null
        band2V?.dispose(); band2V = null
        firstCol?.dispose(); firstCol = null
        lastCol?.dispose(); lastCol = null
        firstRow?.dispose(); firstRow = null
        lastRow?.dispose(); lastRow = null
    }
}
