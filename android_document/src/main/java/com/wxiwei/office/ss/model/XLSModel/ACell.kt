/*
 * 文件名称:          ACell.java
 *
 * 编译器:            android2.2
 */
package com.wxiwei.office.ss.model.XLSModel

import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.record.BlankRecord
import com.wxiwei.office.fc.hssf.record.BoolErrRecord
import com.wxiwei.office.fc.hssf.record.CellValueRecordInterface
import com.wxiwei.office.fc.hssf.record.FormulaRecord
import com.wxiwei.office.fc.hssf.record.LabelRecord
import com.wxiwei.office.fc.hssf.record.LabelSSTRecord
import com.wxiwei.office.fc.hssf.record.NumberRecord
import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.hssf.record.aggregates.FormulaRecordAggregate
import com.wxiwei.office.fc.hssf.record.common.UnicodeString
import com.wxiwei.office.fc.hssf.usermodel.HSSFRichTextString
import com.wxiwei.office.fc.ss.SpreadsheetVersion
import com.wxiwei.office.ss.model.baseModel.Cell
import com.wxiwei.office.ss.model.baseModel.Sheet
import com.wxiwei.office.ss.util.SectionElementFactory

/**
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2012-7-23
 * 负责人:           jqin
 */
open class ACell : Cell {
    private var record: CellValueRecordInterface? = null

    /**
     * Creates an ACell from a CellValueRecordInterface.  ASheet uses this when
     * reading in cells from an existing sheet.
     *
     * @param sheet - Sheet record of the sheet containing this cell
     * @param cval - the Cell Value Record we wish to represent
     */
    constructor(sheet: Sheet?, cval: CellValueRecordInterface) : super(Cell.CELL_TYPE_BLANK) {
        record = cval
        cellType = determineType(cval).toShort()
        this.sheet = sheet
        this.rowNumber = cval.getRow()
        this.colNumber = cval.getColumn().toInt()
        this.styleIndex = cval.getXFIndex().toInt()
        when (cellType) {
            CELL_TYPE_NUMERIC -> value = getNumericCellValue()

            CELL_TYPE_STRING -> if (cval is LabelSSTRecord) {
                value = cval.getSSTIndex()
                processSST()
            } else if (cval is LabelRecord) {
                value = sheet!!.getWorkbook()!!.addSharedString(cval.getValue())
            }

            CELL_TYPE_FORMULA -> procellFormulaCellValue(cval as FormulaRecordAggregate)

            CELL_TYPE_BLANK -> {
            }

            CELL_TYPE_BOOLEAN -> value = getBooleanCellValue()

            CELL_TYPE_ERROR -> value = getErrorCellValue()
        }
    }

    /**
     * Creates new Cell - Should only be called by HSSFRow.  This creates a cell
     * from scratch.
     *
     * When the cell is initially created it is set to CELL_TYPE_BLANK. Cell types
     * can be changed/overwritten by calling setCellValue with the appropriate
     * type as a parameter although conversions from one type to another may be
     * prohibited.
     *
     * @param book - Workbook record of the workbook containing this cell
     * @param sheet - Sheet record of the sheet containing this cell
     * @param row   - the row of this cell
     * @param col   - the column for this cell
     *
     * @see com.wxiwei.office.fc.hssf.usermodel.HSSFRow.createCell
     */
    constructor(book: AWorkbook?, sheet: ASheet, row: Int, col: Short) : super(CELL_TYPE_ERROR) {
        this.sheet = sheet
        // Relying on the fact that by default the cellType is set to 0 which
        // is different to CELL_TYPE_BLANK hence the following method call correctly
        // creates a new blank cell.
        val xfindex = sheet.getInternalSheet()!!.getXFIndexForColAt(col)
        setCellType(CELL_TYPE_BLANK.toInt(), false, row, col, xfindex)
    }

    private fun processSST() {
        val book = sheet!!.getWorkbook()!!
        val obj = book.getSharedItem(value as Int)
        if (obj is UnicodeString) {
            val unicodeString = obj
//            if(unicodeString.getFormatRunCount() > 0)
            run {
                value = book.addSharedString(SectionElementFactory.getSectionElement(book, unicodeString, this))
            }
//            else
//            {
//                book.addSharedString((Integer)value, unicodeString.getString());
//            }
        }
    }

    /**
     *
     * @param cval
     */
    private fun procellFormulaCellValue(cval: FormulaRecordAggregate) {
        val strRec = cval.getStringRecord()
        if (strRec != null) {
            cellType = Cell.CELL_TYPE_STRING
            value = sheet!!.getWorkbook()!!.addSharedString(strRec.getString())
        } else {
            val formulaRec = cval.getFormulaRecord()
            cellType = formulaRec.getCachedResultType().toShort()
            when (cellType) {
                CELL_TYPE_NUMERIC -> value = formulaRec.getValue()

                CELL_TYPE_STRING -> {
                    //Log.w("formula CachedResultType is string", String.valueOf(cval.getRow())+""+String.valueOf(cval.getColumn()));
                }

                CELL_TYPE_BOOLEAN -> value = formulaRec.getCachedBooleanValue()

                CELL_TYPE_ERROR -> value = formulaRec.getCachedErrorValue().toByte()
            }
        }
    }

    /**
     *
     * @param ptgs
     */
    fun setCellFormula(ptgs: Array<Ptg>?) {
        val row = record!!.getRow()
        val col = record!!.getColumn()
        val styleIndex = record!!.getXFIndex()
        setCellType(CELL_TYPE_FORMULA.toInt(), false, row, col, styleIndex)
        val agg = record as FormulaRecordAggregate
        val frec = agg.getFormulaRecord()
        frec.setOptions(2.toShort())
        frec.setValue(0.0)

        //only set to default if there is no extended format index already set
        if (agg.getXFIndex() == 0.toShort()) {
            agg.setXFIndex(0x0f.toShort())
        }
        agg.setParsedExpression(ptgs)
    }

    /**
     * get the value of the cell as a string - for numeric cells we throw an exception.
     * For blank cells we return an empty string.
     * For formulaCells that are not string Formulas, we throw an exception
     */
    fun getStringCellValue(): String? {
        when (cellType) {
            CELL_TYPE_BLANK -> return ""

            CELL_TYPE_STRING -> if (record is LabelSSTRecord) {
                return sheet!!.getWorkbook()!!.getSharedString((record as LabelSSTRecord).getSSTIndex())
            }

            CELL_TYPE_FORMULA -> {
            }

            else -> throw typeMismatch(CELL_TYPE_STRING.toInt(), cellType.toInt(), false)
        }
        val fra = (record as FormulaRecordAggregate)
        checkFormulaCachedValueType(CELL_TYPE_STRING.toInt(), fra.getFormulaRecord())
        val strVal = fra.getStringValue()
        return strVal
    }

    /**
     * Get the value of the cell as a number.
     * For strings we throw an exception.
     * For blank cells we return a 0.
     * See HSSFDataFormatter for turning this
     *  number into a string similar to that which
     *  Excel would render this number as.
     */
    fun getNumericCellValue(): Double {
        when (cellType) {
            CELL_TYPE_BLANK -> return 0.0

            CELL_TYPE_NUMERIC -> return (record as NumberRecord).getValue()

            CELL_TYPE_FORMULA -> {
            }

            else -> throw typeMismatch(CELL_TYPE_NUMERIC.toInt(), cellType.toInt(), false)
        }
        val fr = (record as FormulaRecordAggregate).getFormulaRecord()
        checkFormulaCachedValueType(CELL_TYPE_NUMERIC.toInt(), fr)
        return fr.getValue()
    }

    /**
     * get the value of the cell as a boolean.  For strings, numbers, and errors, we throw an exception.
     * For blank cells we return a false.
     */
    fun getBooleanCellValue(): Boolean {
        when (cellType) {
            CELL_TYPE_BLANK -> return false

            CELL_TYPE_BOOLEAN -> return (record as BoolErrRecord).getBooleanValue()

            CELL_TYPE_FORMULA -> {
            }

            else -> throw typeMismatch(CELL_TYPE_BOOLEAN.toInt(), cellType.toInt(), false)
        }
        val fr = (record as FormulaRecordAggregate).getFormulaRecord()
        checkFormulaCachedValueType(CELL_TYPE_BOOLEAN.toInt(), fr)
        return fr.getCachedBooleanValue()
    }

    /**
     * get the value of the cell as an error code.  For strings, numbers, and booleans, we throw an exception.
     * For blank cells we return a 0.
     */
    fun getErrorCellValue(): Byte {
        when (cellType) {
            CELL_TYPE_ERROR -> return (record as BoolErrRecord).getErrorValue()

            CELL_TYPE_FORMULA -> {
            }

            else -> throw typeMismatch(CELL_TYPE_ERROR.toInt(), cellType.toInt(), false)
        }
        val fr = (record as FormulaRecordAggregate).getFormulaRecord()
        checkFormulaCachedValueType(CELL_TYPE_ERROR.toInt(), fr)
        return fr.getCachedErrorValue().toByte()
    }

    /**
     * get Formula Cached Value Type
     * @see CELL_TYPE_STRING
     * @see CELL_TYPE_NUMERIC
     * @see CELL_TYPE_FORMULA
     * @see CELL_TYPE_BOOLEAN
     * @see CELL_TYPE_ERROR
     * @return
     */
    fun getFormulaCachedValueType(): Int {
        return (record as FormulaRecordAggregate).getFormulaRecord().getCachedResultType()
    }

    /**
     * Should only be used by HSSFSheet and friends.  Returns the low level CellValueRecordInterface record
     *
     * @return CellValueRecordInterface representing the cell via the low level api.
     */
    fun getCellValueRecord(): CellValueRecordInterface? {
        return record
    }

    /**
     * set the cells type (numeric, formula or string)
     * @see CELL_TYPE_NUMERIC
     * @see CELL_TYPE_STRING
     * @see CELL_TYPE_FORMULA
     * @see CELL_TYPE_BLANK
     * @see CELL_TYPE_BOOLEAN
     * @see CELL_TYPE_ERROR
     */
    fun setCellType(cellType: Int, setValue: Boolean) {
        val row = record!!.getRow()
        val col = record!!.getColumn()
        val styleIndex = record!!.getXFIndex()
        setCellType(cellType, setValue, row, col, styleIndex)
    }

    /**
     * sets the cell type. The setValue flag indicates whether to bother about
     *  trying to preserve the current value in the new record if one is created.
     *
     *  The @see #setCellValue method will call this method with false in setValue
     *  since it will overwrite the cell value later
     */
    private fun setCellType(cellType: Int, setValue: Boolean, row: Int, col: Short, styleIndex: Short) {
        if (cellType > CELL_TYPE_ERROR) {
            throw RuntimeException("I have no idea what type that is!")
        }

        when (cellType) {
            CELL_TYPE_FORMULA.toInt() -> {
                val frec: FormulaRecordAggregate
                if (this.cellType.toInt() != cellType) {
                    frec = (sheet as ASheet).getInternalSheet()!!.getRowsAggregate().createFormula(row, col.toInt())
                } else {
                    frec = record as FormulaRecordAggregate
                    frec.setRow(row)
                    frec.setColumn(col)
                }
                frec.setXFIndex(styleIndex)
                record = frec
            }

            CELL_TYPE_NUMERIC.toInt() -> {
                var nrec: NumberRecord? = null
                if (cellType != this.cellType.toInt()) {
                    nrec = NumberRecord()
                } else {
                    nrec = record as NumberRecord
                }
                nrec.setColumn(col)
                nrec.setXFIndex(styleIndex)
                nrec.setRow(row)
                record = nrec
            }

            CELL_TYPE_STRING.toInt() -> {
                val lrec: LabelSSTRecord
                if (cellType == this.cellType.toInt()) {
                    lrec = this.record as LabelSSTRecord
                } else {
                    lrec = LabelSSTRecord()
                    lrec.setColumn(col)
                    lrec.setRow(row)
                    lrec.setXFIndex(styleIndex)
                }
                record = lrec
            }

            CELL_TYPE_BLANK.toInt() -> {
                var brec: BlankRecord? = null
                if (this.cellType.toInt() != cellType) {
                    brec = BlankRecord()
                } else {
                    brec = record as BlankRecord
                }
                brec.setColumn(col)

                // During construction the cellStyle may be null for a Blank cell.
                brec.setXFIndex(styleIndex)
                brec.setRow(row)
                record = brec
            }

            CELL_TYPE_BOOLEAN.toInt() -> {
                var boolRec: BoolErrRecord? = null
                if (cellType != this.cellType.toInt()) {
                    boolRec = BoolErrRecord()
                } else {
                    boolRec = record as BoolErrRecord
                }
                boolRec.setColumn(col)
                boolRec.setXFIndex(styleIndex)
                boolRec.setRow(row)
                record = boolRec
            }

            CELL_TYPE_ERROR.toInt() -> {
                var errRec: BoolErrRecord? = null
                if (cellType != this.cellType.toInt()) {
                    errRec = BoolErrRecord()
                } else {
                    errRec = record as BoolErrRecord
                }
                errRec.setColumn(col)
                errRec.setXFIndex(styleIndex)
                errRec.setRow(row)
                record = errRec
            }
        }
//        if (cellType != cellType &&
//            cellType!= -1 )  // Special Value to indicate an uninitialized Cell
//        {
//            ((ASheet)sheet).getInternalSheet().replaceValueRecord(record);
//        }
        this.cellType = cellType.toShort()
    }

    /**
     * set a numeric value for the cell
     *
     * @param value  the numeric value to set this cell to.  For formulas we'll set the
     *        precalculated value, for numerics we'll set its value. For other types we
     *        will change the cell to a numeric cell and set its value.
     */
    fun setCellValue(value: Double) {
        when (cellType) {
            CELL_TYPE_STRING -> this.value = Math.round(value.toFloat())

            CELL_TYPE_NUMERIC -> {
                (record as NumberRecord).setValue(value)
                this.value = value
            }

            CELL_TYPE_FORMULA -> {
            }
        }
    }

    /**
     * set a boolean value for the cell
     *
     * @param value the boolean value to set this cell to.  For formulas we'll set the
     *        precalculated value, for booleans we'll set its value. For other types we
     *        will change the cell to a boolean cell and set its value.
     */
    fun setCellValue(value: Boolean) {
        when (cellType) {
            CELL_TYPE_BOOLEAN -> {
                (record as BoolErrRecord).setValue(value)
                this.value = value
            }

            CELL_TYPE_FORMULA -> {
            }
        }
    }

    /**
     * set a string value for the cell.
     *
     * @param value value to set the cell to.  For formulas we'll set the formula
     * cached string result, for String cells we'll set its value. For other types we will
     * change the cell to a string cell and set its value.
     * If value is null then we will change the cell to a Blank cell.
     */
    fun setCellValue(value: String?) {
        val richString = if (value == null) null else HSSFRichTextString(value)
        val row = record!!.getRow()
        val col = record!!.getColumn()
        val styleIndex = record!!.getXFIndex()
        if (richString == null) {
            setCellType(CELL_TYPE_BLANK.toInt(), false, row, col, styleIndex)
            return
        }

        if (richString.length() > SpreadsheetVersion.EXCEL97.getMaxTextLength()) {
            throw IllegalArgumentException("The maximum length of cell contents (text) is 32,767 characters")
        }

        var index = 0

        val str = richString.getUnicodeString()
        index = (sheet!!.getWorkbook() as AWorkbook).getInternalWorkbook()!!.addSSTString(str)
        (record as LabelSSTRecord).setSSTIndex(index)
        this.value = index
    }

    /**
     * set a error value for the cell
     *
     * @param errorCode the error value to set this cell to.  For formulas we'll set the
     *        precalculated value , for errors we'll set
     *        its value. For other types we will change the cell to an error
     *        cell and set its value.
     */
    fun setCellErrorValue(errorCode: Byte) {
        when (cellType) {
            CELL_TYPE_ERROR -> {
                (record as BoolErrRecord).setValue(errorCode)
                value = errorCode
            }

            CELL_TYPE_FORMULA -> {
            }
        }
    }

    override fun dispose() {
        super.dispose()
        record = null
    }

    companion object {
        /**
         * Used to help format error messages
         */
        private fun getCellTypeName(cellTypeCode: Int): String {
            when (cellTypeCode) {
                CELL_TYPE_BLANK.toInt() -> return "blank"
                CELL_TYPE_STRING.toInt() -> return "text"
                CELL_TYPE_BOOLEAN.toInt() -> return "boolean"
                CELL_TYPE_ERROR.toInt() -> return "error"
                CELL_TYPE_NUMERIC.toInt() -> return "numeric"
                CELL_TYPE_FORMULA.toInt() -> return "formula"
            }
            return "#unknown cell type ($cellTypeCode)#"
        }

        private fun typeMismatch(expectedTypeCode: Int, actualTypeCode: Int, isFormulaCell: Boolean): RuntimeException {
            val msg = ("Cannot get a "
                    + getCellTypeName(expectedTypeCode) + " value from a "
                    + getCellTypeName(actualTypeCode) + " " + (if (isFormulaCell) "formula " else "") + "cell")
            return IllegalStateException(msg)
        }

        private fun checkFormulaCachedValueType(expectedTypeCode: Int, fr: FormulaRecord) {
            val cachedValueType = fr.getCachedResultType()
            if (cachedValueType != expectedTypeCode) {
                throw typeMismatch(expectedTypeCode, cachedValueType, true)
            }
        }

        /**
         * used internally -- given a cell value record, figure out its type
         */
        @JvmStatic
        fun determineType(cval: CellValueRecordInterface): Int {
            if (cval is FormulaRecordAggregate) {
                return CELL_TYPE_FORMULA.toInt()
            }
            // all others are plain BIFF records
            val record = cval as Record
            when (record.getSid()) {
                NumberRecord.sid -> return CELL_TYPE_NUMERIC.toInt()
                BlankRecord.sid -> return CELL_TYPE_BLANK.toInt()
                LabelSSTRecord.sid, LabelRecord.sid -> return CELL_TYPE_STRING.toInt()
                BoolErrRecord.sid -> {
                    val boolErrRecord = record as BoolErrRecord
                    return if (boolErrRecord.isBoolean()) CELL_TYPE_BOOLEAN.toInt() else CELL_TYPE_ERROR.toInt()
                }
            }
            throw RuntimeException("Bad cell value rec (" + cval.javaClass.name + ")")
        }
    }
}
