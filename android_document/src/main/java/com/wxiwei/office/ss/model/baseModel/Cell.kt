/*
 * 文件名称:          Cell.java
 *
 * 编译器:            android2.2
 * 时间:              下午3:19:10
 */
package com.wxiwei.office.ss.model.baseModel

import com.wxiwei.office.common.hyperlink.Hyperlink
import com.wxiwei.office.simpletext.view.STRoot
import com.wxiwei.office.ss.model.style.CellStyle
import com.wxiwei.office.ss.model.table.SSTable
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

/**
 * Cell of this Row
 *
 * Read版本:        Read V1.0
 * 作者:            ljj8494
 * 日期:            2012-2-16
 * 负责人:          ljj8494
 */
open class Cell
/**
 * 构造器
 * @param cellType  type of this cell
 * @see CELL_TYPE_BLANK
 * @see CELL_TYPE_BOOLEAN
 * @see CELL_TYPE_ERROR
 * @see CELL_TYPE_FORMULA
 * @see CELL_TYPE_STRING
 * @see CELL_TYPE_NUMERIC
 */
(cellType: Short) {
    @JvmField
    protected var sheet: Sheet? = null

    // cell type
    @JvmField
    protected var cellType: Short = cellType

    //
    @JvmField
    protected var rowNumber: Int = 0

    //
    @JvmField
    protected var colNumber: Int = 0

    //
    @JvmField
    protected var styleIndex: Int = 0

    // value of this cell
    @JvmField
    protected var value: Any? = null

    /** OOXML formula without =; cell type/value remain the cached display result. */
    var formula: String? = null

    private var prop: CellProperty? = CellProperty()

    /**
     *
     * @param sheet
     */
    fun setSheet(sheet: Sheet?) {
        this.sheet = sheet
    }

    /**
     *
     * @return
     */
    fun getSheet(): Sheet? = sheet

    /**
     *
     * @param cellType  type of this cell
     * @see CELL_TYPE_BLANK
     * @see CELL_TYPE_BOOLEAN
     * @see CELL_TYPE_ERROR
     * @see CELL_TYPE_FORMULA
     * @see CELL_TYPE_STRING
     * @see CELL_TYPE_NUMERIC
     */
    open fun setCellType(cellType: Short) {
        this.cellType = cellType
    }

    /**
     *
     */
    fun getCellType(): Short = this.cellType

    fun setCellNumericType(numericType: Short) {
        if (cellType == CELL_TYPE_NUMERIC) {
            prop!!.setCellProp(CellProperty.CELLPROPID_NUMERICTYPE, numericType)
        }
    }

    fun getCellNumericType(): Short {
        return prop!!.getCellNumericType()
    }

    /**
     * set value of this cell
     * @param value
     */
    open fun setCellValue(value: Any?) {
        this.value = value
    }

    /**
     * get string value of this cell
     * @return
     */
    fun getStringCellValueIndex(): Int {
        if (cellType == CELL_TYPE_STRING && value != null) {
            return value as Int
        }
        return -1
    }

    /**
     * get number value of this cell
     * @return Returns the numberValue.
     */
    fun getNumberValue(): Double {
        if (cellType == CELL_TYPE_NUMERIC && value != null) {
            return (value as Double).toDouble()
        }
        return Double.NaN
    }

    fun getErrorValue(): Int {
        if (cellType == CELL_TYPE_ERROR && value != null) {
            return (value as Byte).toInt()
        }
        return -1
    }

    /**
     * get error code of this cell
     */
    fun getErrorCodeValue(): Byte {
        if (cellType == CELL_TYPE_ERROR && value != null) {
            return (value as Byte).toByte()
        }
        return Byte.MIN_VALUE
    }

    /**
     * get formula of this cell
     * @return
     */
    fun getCellFormulaValue(): String? {
        if (cellType == CELL_TYPE_FORMULA && value != null) {
            return (value as String)
        }
        return null
    }

    /**
     * get boolean value of this cell
     */
    fun getBooleanValue(): Boolean {
        if (cellType == CELL_TYPE_BOOLEAN && value != null) {
            return (value as Boolean)
        }
        return false
    }

    /**
     * get data value of this cell
     * @return
     */
    fun getDateCellValue(use1904windowing: Boolean): Date? {
        if (cellType == CELL_TYPE_NUMERIC && value != null) {
            val date = (value as Double).toDouble()
            val wholeDays = Math.floor(date).toInt()
            val millisecondsInDay = ((date - wholeDays) * DAY_MILLISECONDS + 0.5).toInt()

            val startYear = if (use1904windowing) 1904 else 1900
            // 1904 date windowing uses 1/2/1904 as the first day
            // Date is prior to 3/1/1900, so adjust because Excel thinks 2/29/1900 exists
            // If Excel date == 2/29/1900, will become 3/1/1900 in Java representation
            val dayAdjust = if (use1904windowing) 1 else (if (wholeDays < 61) 0 else -1)
            CALENDAR.clear()
            CALENDAR.set(startYear, 0, wholeDays + dayAdjust, 0, 0, 0)
            CALENDAR.set(GregorianCalendar.MILLISECOND, millisecondsInDay)
            return CALENDAR.time
        }
        return null
    }

    /**
     * @return Returns the rangeAddressIndex.
     */
    fun getRangeAddressIndex(): Int {
        return prop!!.getCellMergeRangeAddressIndex()
    }

    /**
     * @param rangeAddressIndex The rangeAddressIndex to set.
     */
    fun setRangeAddressIndex(rangeAddressIndex: Int) {
        prop!!.setCellProp(CellProperty.CELLPROPID_MERGEDRANGADDRESS, rangeAddressIndex)
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
     * @return Returns the colNumber.
     */
    fun getColNumber(): Int = colNumber

    /**
     * @param colNumber The colNumber to set.
     */
    fun setColNumber(colNumber: Int) {
        this.colNumber = colNumber
    }

    /**
     * @return Returns the link.
     */
    fun getHyperLink(): Hyperlink? {
        return prop!!.getCellHyperlink()
    }

    /**
     * @param link The link to set.
     */
    fun setHyperLink(link: Hyperlink?) {
        prop!!.setCellProp(CellProperty.CELLPROPID_HYPERLINK, link)
    }

    /**
     * @return Returns the cellStyle.
     */
    fun getCellStyle(): CellStyle? {
        return sheet!!.getWorkbook()!!.getCellStyle(styleIndex)
    }

    /**
     * @param styleIndex The cellStyle to set.
     */
    fun setCellStyle(styleIndex: Int) {
        this.styleIndex = styleIndex
    }

    fun hasValidValue(): Boolean {
        return value != null
    }

    fun setSTRoot(root: STRoot?) {
        if (sheet!!.getState() == Sheet.State_Accomplished) {
            prop!!.setCellProp(CellProperty.CELLPROPID_STROOT, sheet!!.addSTRoot(root))
        }
    }

    fun getSTRoot(): STRoot? {
        return sheet!!.getSTRoot(prop!!.getCellSTRoot())
    }

    /**
     *
     */
    fun removeSTRoot() {
        prop!!.removeCellSTRoot()
    }

    /**
     *
     * @param index
     */
    fun setExpandedRangeAddressIndex(index: Int) {
        prop!!.setCellProp(CellProperty.CELLPROPID_EXPANDRANGADDRESS, index)
    }

    /**
     *
     * @return
     */
    fun getExpandedRangeAddressIndex(): Int {
        return prop!!.getExpandCellRangeAddressIndex()
    }

    /**
     * table infomation
     */
    fun setTableInfo(table: SSTable?) {
        prop!!.setCellProp(CellProperty.CELLPROPID_TABLEINFO, table)
    }

    /**
     *
     * @return
     */
    fun getTableInfo(): SSTable? {
        return prop!!.getTableInfo()
    }

    /**
     * dispose
     */
    open fun dispose() {
        sheet = null
        value = null
        if (prop != null) {
            prop!!.dispose()
            prop = null
        }
    }

    companion object {
        private const val SECONDS_PER_MINUTE = 60
        private const val MINUTES_PER_HOUR = 60
        private const val HOURS_PER_DAY = 24
        private const val SECONDS_PER_DAY = (HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE)
        private const val DAY_MILLISECONDS = SECONDS_PER_DAY * 1000L

        // Numeric Cell type (0)
        const val CELL_TYPE_NUMERIC: Short = 0
        // String Cell type (1)
        const val CELL_TYPE_STRING: Short = (CELL_TYPE_NUMERIC + 1).toShort()
        // Formula Cell type (2)
        const val CELL_TYPE_FORMULA: Short = (CELL_TYPE_STRING + 1).toShort()
        // Blank Cell type (3)
        const val CELL_TYPE_BLANK: Short = (CELL_TYPE_FORMULA + 1).toShort()
        // Boolean Cell type (4)
        const val CELL_TYPE_BOOLEAN: Short = (CELL_TYPE_BLANK + 1).toShort()
        // Error Cell type (5)
        const val CELL_TYPE_ERROR: Short = (CELL_TYPE_BOOLEAN + 1).toShort()

        /**
         * GENERAL Cell type (6)
         */
        const val CELL_TYPE_NUMERIC_GENERAL: Short = (CELL_TYPE_ERROR + 1).toShort()

        /**
         * DecimalFormat Cell type (7)
         */
        const val CELL_TYPE_NUMERIC_DECIMAL: Short = (CELL_TYPE_NUMERIC_GENERAL + 1).toShort()

        /**
         * accounting Cell type (8)
         */
        const val CELL_TYPE_NUMERIC_ACCOUNTING: Short = (CELL_TYPE_NUMERIC_DECIMAL + 1).toShort()

        /**
         * FractionalFormat Cell type (9)
         */
        const val CELL_TYPE_NUMERIC_FRACTIONAL: Short = (CELL_TYPE_NUMERIC_ACCOUNTING + 1).toShort()

        /**
         * SimpleDateFormat Cell type (10)
         */
        const val CELL_TYPE_NUMERIC_SIMPLEDATE: Short = (CELL_TYPE_NUMERIC_FRACTIONAL + 1).toShort()

        /**
         * StringFormat Cell type (11)
         */
        const val CELL_TYPE_NUMERIC_STRING: Short = (CELL_TYPE_NUMERIC_SIMPLEDATE + 1).toShort()

        //
        private val CALENDAR: Calendar = GregorianCalendar()
    }
}
