/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package com.wxiwei.office.fc.hssf.usermodel

import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.ptg.ExpPtg
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.model.InternalSheet
import com.wxiwei.office.fc.hssf.model.InternalWorkbook
import com.wxiwei.office.fc.hssf.record.BlankRecord
import com.wxiwei.office.fc.hssf.record.BoolErrRecord
import com.wxiwei.office.fc.hssf.record.CellValueRecordInterface
import com.wxiwei.office.fc.hssf.record.CommonObjectDataSubRecord
import com.wxiwei.office.fc.hssf.record.DrawingRecord
import com.wxiwei.office.fc.hssf.record.ExtendedFormatRecord
import com.wxiwei.office.fc.hssf.record.FormulaRecord
import com.wxiwei.office.fc.hssf.record.HyperlinkRecord
import com.wxiwei.office.fc.hssf.record.LabelSSTRecord
import com.wxiwei.office.fc.hssf.record.NoteRecord
import com.wxiwei.office.fc.hssf.record.NumberRecord
import com.wxiwei.office.fc.hssf.record.ObjRecord
import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.hssf.record.RecordBase
import com.wxiwei.office.fc.hssf.record.SubRecord
import com.wxiwei.office.fc.hssf.record.TextObjectRecord
import com.wxiwei.office.fc.hssf.record.aggregates.FormulaRecordAggregate
import com.wxiwei.office.fc.hssf.record.common.UnicodeString
import com.wxiwei.office.fc.ss.SpreadsheetVersion
import com.wxiwei.office.fc.ss.usermodel.Comment
import com.wxiwei.office.fc.ss.usermodel.ErrorConstants
import com.wxiwei.office.fc.ss.usermodel.FormulaError
import com.wxiwei.office.fc.ss.usermodel.ICell
import com.wxiwei.office.fc.ss.usermodel.ICellStyle
import com.wxiwei.office.fc.ss.usermodel.IHyperlink
import com.wxiwei.office.fc.ss.usermodel.RichTextString
import com.wxiwei.office.fc.ss.util.CellReference
import com.wxiwei.office.fc.ss.util.HSSFCellRangeAddress
import com.wxiwei.office.fc.ss.util.NumberToTextConverter
import com.wxiwei.office.fc.util.POILogFactory.Companion.getLogger
import com.wxiwei.office.fc.util.POILogger
import com.wxiwei.office.ss.util.DateUtil.Companion.getExcelDate
import com.wxiwei.office.ss.util.DateUtil.Companion.getJavaDate
import java.util.Calendar
import java.util.Date
import kotlin.Array
import kotlin.Boolean
import kotlin.Byte
import kotlin.Deprecated
import kotlin.IllegalStateException
import kotlin.Int
import kotlin.RuntimeException
import kotlin.Short
import kotlin.String
import kotlin.arrayOf
import kotlin.check
import kotlin.require
import kotlin.requireNotNull

/**
 * High level representation of a cell in a row of a spreadsheet.
 * Cells can be numeric, formula-based or string-based (text).  The cell type
 * specifies this.  String cells cannot conatin numbers and numeric cells cannot
 * contain strings (at least according to our model).  Client apps should do the
 * conversions themselves.  Formula cells have the formula string, as well as
 * the formula result, which can be numeric or string.
 * 
 * 
 * Cells should have their number (0 based) before being added to a row.  Only
 * cells that have values should be added.
 * 
 * 
 * 
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author  Dan Sherman (dsherman at isisph.com)
 * @author  Brian Sanders (kestrel at burdell dot org) Active Cell support
 * @author  Yegor Kozlov cell comments support
 */
class HSSFCell : ICell {
    private val _book: HSSFWorkbook
    private val _sheet: HSSFSheet
    private var _cellType = 0
    private var _stringValue: HSSFRichTextString?
    private var _record: CellValueRecordInterface? = null
    private var _comment: HSSFComment? = null

    /**
     * @return Returns the rangeAddressIndex.
     */
    /**
     * @param rangeAddressIndex The rangeAddressIndex to set.
     */
    // 合并单元的RangeAddress的Index
    var rangeAddressIndex: Int = -1


    /**
     * Creates new Cell - Should only be called by HSSFRow.  This creates a cell
     * from scratch.
     * 
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
     * @see HSSFRow.createCell
     */
    constructor(book: HSSFWorkbook, sheet: HSSFSheet, row: Int, col: Short) {
        checkBounds(col.toInt())
        _stringValue = null
        _book = book
        _sheet = sheet

        // Relying on the fact that by default the cellType is set to 0 which
        // is different to CELL_TYPE_BLANK hence the following method call correctly
        // creates a new blank cell.
        val xfindex = sheet.sheet.getXFIndexForColAt(col)
        setCellType(ICell.CELL_TYPE_BLANK, false, row, col, xfindex)
    }

    /**
     * Returns the HSSFSheet this cell belongs to
     * 
     * @return the HSSFSheet that owns this cell
     */
    override fun getSheet(): HSSFSheet {
        return _sheet
    }

    /**
     * Returns the HSSFRow this cell belongs to
     * 
     * @return the HSSFRow that owns this cell
     */
    override fun getRow(): HSSFRow? {
        val rowIndex = getRowIndex()
        return _sheet.getRow(rowIndex)
    }

    /**
     * Creates new Cell - Should only be called by HSSFRow.  This creates a cell
     * from scratch.
     * 
     * @param book - Workbook record of the workbook containing this cell
     * @param sheet - Sheet record of the sheet containing this cell
     * @param row   - the row of this cell
     * @param col   - the column for this cell
     * @param type  - CELL_TYPE_NUMERIC, CELL_TYPE_STRING, CELL_TYPE_FORMULA, CELL_TYPE_BLANK,
     * CELL_TYPE_BOOLEAN, CELL_TYPE_ERROR
     * Type of cell
     * @see HSSFRow.createCell
     */
    constructor(
        book: HSSFWorkbook, sheet: HSSFSheet, row: Int, col: Short,
        type: Int
    ) {
        checkBounds(col.toInt())
        _cellType = -1 // Force 'setCellType' to create a first Record
        _stringValue = null
        _book = book
        _sheet = sheet

        val xfindex = sheet.sheet.getXFIndexForColAt(col)
        setCellType(type, false, row, col, xfindex)
    }

    /**
     * Creates an HSSFCell from a CellValueRecordInterface.  HSSFSheet uses this when
     * reading in cells from an existing sheet.
     * 
     * @param book - Workbook record of the workbook containing this cell
     * @param sheet - Sheet record of the sheet containing this cell
     * @param cval - the Cell Value Record we wish to represent
     */
    constructor(book: HSSFWorkbook, sheet: HSSFSheet, cval: CellValueRecordInterface) {
        _record = cval
        _cellType = determineType(cval)
        _stringValue = null
        _book = book
        _sheet = sheet
        when (_cellType) {
            ICell.CELL_TYPE_STRING -> _stringValue =
                HSSFRichTextString(book.workbook, cval as LabelSSTRecord)

            ICell.CELL_TYPE_BLANK -> {}
            ICell.CELL_TYPE_FORMULA -> _stringValue =
                HSSFRichTextString((cval as FormulaRecordAggregate).stringValue)
        }
    }


    protected val boundWorkbook: InternalWorkbook
        /**
         * Returns the Workbook that this Cell is bound to
         */
        get() = _book.workbook

    /**
     * @return the (zero based) index of the row containing this cell
     */
    override fun getRowIndex(): Int {
        return _record!!.row
    }

    /**
     * Updates the cell record's idea of what
     * column it belongs in (0 based)
     * @param num the new cell number
     */
    fun updateCellNum(num: Short) {
        _record!!.column = num
    }

    @get:Deprecated("(Oct 2008) use {@link #getColumnIndex()}")
    @set:Deprecated("(Jan 2008) Doesn't update the row's idea of what cell this is, use {@link HSSFRow#moveCell(HSSFCell, short)} instead")
    var cellNum: Short
        get() = getColumnIndex().toShort()
        /**
         * Set the cell's number within the row (0 based).
         * @param num  short the cell number
         */
        set(num) {
            _record!!.column = num
        }

    override fun getColumnIndex(): Int {
        return _record!!.column.toInt() and 0xFFFF
    }

    /**
     * set the cells type (numeric, formula or string)
     * @see .CELL_TYPE_NUMERIC
     * 
     * @see .CELL_TYPE_STRING
     * 
     * @see .CELL_TYPE_FORMULA
     * 
     * @see .CELL_TYPE_BLANK
     * 
     * @see .CELL_TYPE_BOOLEAN
     * 
     * @see .CELL_TYPE_ERROR
     */
    override fun setCellType(cellType: Int) {
        notifyFormulaChanging()
        if (isPartOfArrayFormulaGroup()) {
            notifyArrayFormulaChanging()
        }
        val row = _record!!.row
        val col = _record!!.column
        val styleIndex = _record!!.xFIndex
        setCellType(cellType, true, row, col, styleIndex)
    }

    /**
     * set the cells type (numeric, formula or string)
     * @see .CELL_TYPE_NUMERIC
     * 
     * @see .CELL_TYPE_STRING
     * 
     * @see .CELL_TYPE_FORMULA
     * 
     * @see .CELL_TYPE_BLANK
     * 
     * @see .CELL_TYPE_BOOLEAN
     * 
     * @see .CELL_TYPE_ERROR
     */
    fun setCellType(cellType: Int, setValue: Boolean) {
        notifyFormulaChanging()
        if (isPartOfArrayFormulaGroup()) {
            notifyArrayFormulaChanging()
        }
        val row = _record!!.row
        val col = _record!!.column
        val styleIndex = _record!!.xFIndex
        setCellType(cellType, setValue, row, col, styleIndex)
    }

    /**
     * sets the cell type. The setValue flag indicates whether to bother about
     * trying to preserve the current value in the new record if one is created.
     * 
     * 
     * The @see #setCellValue method will call this method with false in setValue
     * since it will overwrite the cell value later
     * 
     */
    private fun setCellType(
        cellType: Int,
        setValue: Boolean,
        row: Int,
        col: Short,
        styleIndex: Short
    ) {
        if (cellType > ICell.CELL_TYPE_ERROR) {
            throw RuntimeException("I have no idea what type that is!")
        }
        when (cellType) {
            ICell.CELL_TYPE_FORMULA -> {
                val frec: FormulaRecordAggregate

                if (cellType != _cellType) {
                    frec = _sheet.sheet.rowsAggregate!!.createFormula(row, col.toInt())
                } else {
                    frec = _record as FormulaRecordAggregate
                    frec.row = row
                    frec.column = col
                }
                if (setValue) {
                    frec.formulaRecord.setValue(getNumericCellValue())
                }
                frec.xFIndex = styleIndex
                _record = frec
            }

            ICell.CELL_TYPE_NUMERIC -> {
                val nrec: NumberRecord

                if (cellType != _cellType) {
                    nrec = NumberRecord()
                } else {
                    nrec = _record as NumberRecord
                }
                nrec.column = col
                if (setValue) {
                    nrec.setValue(getNumericCellValue())
                }
                nrec.xFIndex = styleIndex
                nrec.row = row
                _record = nrec
            }

            ICell.CELL_TYPE_STRING -> {
                val lrec: LabelSSTRecord

                if (cellType == _cellType) {
                    lrec = _record as LabelSSTRecord
                } else {
                    lrec = LabelSSTRecord()
                    lrec.column = col
                    lrec.row = row
                    lrec.xFIndex = styleIndex
                }
                if (setValue) {
                    val str = convertCellValueToString()
                    val sstIndex = _book.workbook.addSSTString(UnicodeString(str))
                    lrec.setSSTIndex(sstIndex)
                    val us = _book.workbook.getSSTString(sstIndex)
                    _stringValue = HSSFRichTextString()
                    _stringValue!!.unicodeString = us
                }
                _record = lrec
            }

            ICell.CELL_TYPE_BLANK -> {
                val brec: BlankRecord

                if (cellType != _cellType) {
                    brec = BlankRecord()
                } else {
                    brec = _record as BlankRecord
                }
                brec.column = col

                // During construction the cellStyle may be null for a Blank cell.
                brec.xFIndex = styleIndex
                brec.row = row
                _record = brec
            }

            ICell.CELL_TYPE_BOOLEAN -> {
                val boolRec: BoolErrRecord

                if (cellType != _cellType) {
                    boolRec = BoolErrRecord()
                } else {
                    boolRec = _record as BoolErrRecord
                }
                boolRec.column = col
                if (setValue) {
                    boolRec.setValue(convertCellValueToBoolean())
                }
                boolRec.xFIndex = styleIndex
                boolRec.row = row
                _record = boolRec
            }

            ICell.CELL_TYPE_ERROR -> {
                val errRec: BoolErrRecord

                if (cellType != _cellType) {
                    errRec = BoolErrRecord()
                } else {
                    errRec = _record as BoolErrRecord
                }
                errRec.column = col
                if (setValue) {
                    errRec.setValue(ErrorConstants.ERROR_VALUE.toByte())
                }
                errRec.xFIndex = styleIndex
                errRec.row = row
                _record = errRec
            }
        }
        if (cellType != _cellType &&
            _cellType != -1
        )  // Special Value to indicate an uninitialized Cell
        {
            _sheet.sheet.replaceValueRecord(_record)
        }
        _cellType = cellType
    }

    /**
     * get the cells type (numeric, formula or string)
     * @see .CELL_TYPE_STRING
     * 
     * @see .CELL_TYPE_NUMERIC
     * 
     * @see .CELL_TYPE_FORMULA
     * 
     * @see .CELL_TYPE_BOOLEAN
     * 
     * @see .CELL_TYPE_ERROR
     */
    override fun getCellType(): Int {
        return _cellType
    }

    /**
     * set a numeric value for the cell
     * 
     * @param value  the numeric value to set this cell to.  For formulas we'll set the
     * precalculated value, for numerics we'll set its value. For other types we
     * will change the cell to a numeric cell and set its value.
     */
    override fun setCellValue(value: Double) {
        if (value.isInfinite()) {
            // Excel does not support positive/negative infinities,
            // rather, it gives a #DIV/0! error in these cases.
            setCellErrorValue(FormulaError.DIV0.getCode())
        } else if (value.isNaN()) {
            // Excel does not support Not-a-Number (NaN),
            // instead it immediately generates a #NUM! error.
            setCellErrorValue(FormulaError.NUM.getCode())
        } else {
            val row = _record!!.row
            val col = _record!!.column
            val styleIndex = _record!!.xFIndex

            when (_cellType) {
                ICell.CELL_TYPE_NUMERIC -> (_record as NumberRecord).setValue(value)
                ICell.CELL_TYPE_FORMULA -> (_record as FormulaRecordAggregate).setCachedDoubleResult(
                    value
                )

                else -> {
                    setCellType(ICell.CELL_TYPE_NUMERIC, false, row, col, styleIndex)
                    (_record as NumberRecord).setValue(value)
                }
            }
        }
    }

    /**
     * set a date value for the cell. Excel treats dates as numeric so you will need to format the cell as
     * a date.
     * 
     * @param value  the date value to set this cell to.  For formulas we'll set the
     * precalculated value, for numerics we'll set its value. For other types we
     * will change the cell to a numeric cell and set its value.
     */
    override fun setCellValue(value: Date?) {
        setCellValue(getExcelDate(value, _book.workbook.isUsing1904DateWindowing))
    }

    /**
     * set a date value for the cell. Excel treats dates as numeric so you will need to format the cell as
     * a date.
     * 
     * This will set the cell value based on the Calendar's timezone. As Excel
     * does not support timezones this means that both 20:00+03:00 and
     * 20:00-03:00 will be reported as the same value (20:00) even that there
     * are 6 hours difference between the two times. This difference can be
     * preserved by using `setCellValue(value.getTime())` which will
     * automatically shift the times to the default timezone.
     * 
     * @param value  the date value to set this cell to.  For formulas we'll set the
     * precalculated value, for numerics we'll set its value. For othertypes we
     * will change the cell to a numeric cell and set its value.
     */
    override fun setCellValue(value: Calendar) {
        setCellValue(getExcelDate(value, _book.workbook.isUsing1904DateWindowing))
    }

    /**
     * set a string value for the cell.
     * 
     * @param value value to set the cell to.  For formulas we'll set the formula
     * cached string result, for String cells we'll set its value. For other types we will
     * change the cell to a string cell and set its value.
     * If value is null then we will change the cell to a Blank cell.
     */
    override fun setCellValue(value: String?) {
        val str = if (value == null) null else HSSFRichTextString(value)
        setCellValue(str)
    }

    /**
     * Set a string value for the cell.
     * 
     * @param value  value to set the cell to.  For formulas we'll set the formula
     * string, for String cells we'll set its value.  For other types we will
     * change the cell to a string cell and set its value.
     * If value is `null` then we will change the cell to a Blank cell.
     */
    override fun setCellValue(value: RichTextString?) {
        val hvalue = value as HSSFRichTextString?
        val row = _record!!.row
        val col = _record!!.column
        val styleIndex = _record!!.xFIndex
        if (hvalue == null) {
            notifyFormulaChanging()
            setCellType(ICell.CELL_TYPE_BLANK, false, row, col, styleIndex)
            return
        }

        require(hvalue.length() <= SpreadsheetVersion.EXCEL97.getMaxTextLength()) { "The maximum length of cell contents (text) is 32,767 characters" }

        if (_cellType == ICell.CELL_TYPE_FORMULA) {
            // Set the 'pre-evaluated result' for the formula
            // note - formulas do not preserve text formatting.
            val fr = _record as FormulaRecordAggregate
            fr.setCachedStringResult(hvalue.getString())
            // Update our local cache to the un-formatted version
            _stringValue = HSSFRichTextString(value.getString())

            // All done
            return
        }

        // If we get here, we're not dealing with a formula,
        //  so handle things as a normal rich text cell
        if (_cellType != ICell.CELL_TYPE_STRING) {
            setCellType(ICell.CELL_TYPE_STRING, false, row, col, styleIndex)
        }
        var index = 0

        val str = hvalue.unicodeString
        index = _book.workbook.addSSTString(str)
        (_record as LabelSSTRecord).setSSTIndex(index)
        _stringValue = hvalue
        _stringValue!!.setWorkbookReferences(_book.workbook, (_record as LabelSSTRecord))
        _stringValue!!.unicodeString = _book.workbook.getSSTString(index)
    }

    override fun setCellFormula(formula: String?) {
        if (isPartOfArrayFormulaGroup()) {
            notifyArrayFormulaChanging()
        }

        val row = _record!!.row
        val col = _record!!.column
        val styleIndex = _record!!.xFIndex

        if (formula == null) {
            notifyFormulaChanging()
            setCellType(ICell.CELL_TYPE_BLANK, false, row, col, styleIndex)
            return
        }
        val sheetIndex = _book.getSheetIndex(_sheet)
        //        Ptg[] ptgs = HSSFFormulaParser.parse(formula, _book, FormulaType.CELL, sheetIndex);
//        setCellType(CELL_TYPE_FORMULA, false, row, col, styleIndex);
//        FormulaRecordAggregate agg = (FormulaRecordAggregate) _record;
//        FormulaRecord frec = agg.getFormulaRecord();
//        frec.setOptions((short) 2);
//        frec.setValue(0);
//
//        //only set to default if there is no extended format index already set
//        if (agg.getXFIndex() == (short)0) {
//            agg.setXFIndex((short) 0x0f);
//        }
//        agg.setParsedExpression(ptgs);
    }

    fun setCellFormula(ptgs: Array<Ptg?>?) {
        if (isPartOfArrayFormulaGroup()) {
            notifyArrayFormulaChanging()
        }

        val row = _record!!.row
        val col = _record!!.column
        val styleIndex = _record!!.xFIndex

        setCellType(ICell.CELL_TYPE_FORMULA, false, row, col, styleIndex)
        val agg = _record as FormulaRecordAggregate
        val frec = agg.formulaRecord
        frec.setOptions(2.toShort())
        frec.setValue(0.0)

        //only set to default if there is no extended format index already set
        if (agg.xFIndex == 0.toShort()) {
            agg.xFIndex = 0x0f.toShort()
        }
        agg.setParsedExpression(ptgs)
    }

    /**
     * Should be called any time that a formula could potentially be deleted.
     * Does nothing if this cell currently does not hold a formula
     */
    private fun notifyFormulaChanging() {
        if (_record is FormulaRecordAggregate) {
            (_record as FormulaRecordAggregate).notifyFormulaChanging()
        }
    }

    override fun getCellFormula(): String? {
        if (_record !is FormulaRecordAggregate) {
            throw typeMismatch(ICell.CELL_TYPE_FORMULA, _cellType, true)
        }
        //return HSSFFormulaParser.toFormulaString(_book, ((FormulaRecordAggregate)_record).getFormulaTokens());
        return null
    }

    val formulaCachedValueType: Int
        /**
         * get Formula Cached Value Type
         * @see .CELL_TYPE_STRING
         * 
         * @see .CELL_TYPE_NUMERIC
         * 
         * @see .CELL_TYPE_FORMULA
         * 
         * @see .CELL_TYPE_BOOLEAN
         * 
         * @see .CELL_TYPE_ERROR
         * 
         * @return
         */
        get() = (_record as FormulaRecordAggregate).formulaRecord.getCachedResultType()

    /**
     * Get the value of the cell as a number.
     * For strings we throw an exception.
     * For blank cells we return a 0.
     * See [HSSFDataFormatter] for turning this
     * number into a string similar to that which
     * Excel would render this number as.
     */
    override fun getNumericCellValue(): Double {
        when (_cellType) {
            ICell.CELL_TYPE_BLANK -> return 0.0
            ICell.CELL_TYPE_NUMERIC -> return (_record as NumberRecord).getValue()
            ICell.CELL_TYPE_FORMULA -> {}
            else -> throw typeMismatch(ICell.CELL_TYPE_NUMERIC, _cellType, false)
        }
        val fr = (_record as FormulaRecordAggregate).formulaRecord
        checkFormulaCachedValueType(ICell.CELL_TYPE_NUMERIC, fr)
        return fr.getValue()
    }

    /**
     * Get the value of the cell as a date.
     * For strings we throw an exception.
     * For blank cells we return a null.
     * See [HSSFDataFormatter] for formatting
     * this date into a string similar to how excel does.
     */
    override fun getDateCellValue(): Date? {
        if (_cellType == ICell.CELL_TYPE_BLANK) {
            return null
        }
        val value = getNumericCellValue()
        if (_book.workbook.isUsing1904DateWindowing) {
            return getJavaDate(value, true)
        }
        return getJavaDate(value, false)
    }

    val sSTStringIndex: Int
        get() {
            val str = getRichStringCellValue()
            return str.sSTIndex
        }

    /**
     * get the value of the cell as a string - for numeric cells we throw an exception.
     * For blank cells we return an empty string.
     * For formulaCells that are not string Formulas, we throw an exception
     */
    override fun getStringCellValue(): String {
        val str = getRichStringCellValue()
        return str.getString()
    }

    /**
     * get the value of the cell as a string - for numeric cells we throw an exception.
     * For blank cells we return an empty string.
     * For formulaCells that are not string Formulas, we throw an exception
     */
    override fun getRichStringCellValue(): HSSFRichTextString {
        when (_cellType) {
            ICell.CELL_TYPE_BLANK -> return HSSFRichTextString("")
            ICell.CELL_TYPE_STRING -> return _stringValue!!
            ICell.CELL_TYPE_FORMULA -> {}
            else -> throw typeMismatch(ICell.CELL_TYPE_STRING, _cellType, false)
        }
        val fra = (_record as FormulaRecordAggregate)
        checkFormulaCachedValueType(ICell.CELL_TYPE_STRING, fra.formulaRecord)
        val strVal = fra.stringValue
        return HSSFRichTextString(if (strVal == null) "" else strVal)
    }

    /**
     * set a boolean value for the cell
     * 
     * @param value the boolean value to set this cell to.  For formulas we'll set the
     * precalculated value, for booleans we'll set its value. For other types we
     * will change the cell to a boolean cell and set its value.
     */
    override fun setCellValue(value: Boolean) {
        val row = _record!!.row
        val col = _record!!.column
        val styleIndex = _record!!.xFIndex

        when (_cellType) {
            ICell.CELL_TYPE_BOOLEAN -> (_record as BoolErrRecord).setValue(value)
            ICell.CELL_TYPE_FORMULA -> (_record as FormulaRecordAggregate).setCachedBooleanResult(
                value
            )

            else -> {
                setCellType(ICell.CELL_TYPE_BOOLEAN, false, row, col, styleIndex)
                (_record as BoolErrRecord).setValue(value)
            }
        }
    }

    /**
     * set a error value for the cell
     * 
     * @param errorCode the error value to set this cell to.  For formulas we'll set the
     * precalculated value , for errors we'll set
     * its value. For other types we will change the cell to an error
     * cell and set its value.
     */
    override fun setCellErrorValue(errorCode: Byte) {
        val row = _record!!.row
        val col = _record!!.column
        val styleIndex = _record!!.xFIndex
        when (_cellType) {
            ICell.CELL_TYPE_ERROR -> (_record as BoolErrRecord).setValue(errorCode)
            ICell.CELL_TYPE_FORMULA -> (_record as FormulaRecordAggregate).setCachedErrorResult(
                errorCode.toInt()
            )

            else -> {
                setCellType(ICell.CELL_TYPE_ERROR, false, row, col, styleIndex)
                (_record as BoolErrRecord).setValue(errorCode)
            }
        }
    }

    /**
     * Chooses a new boolean value for the cell when its type is changing.
     *
     *
     * 
     * Usually the caller is calling setCellType() with the intention of calling
     * setCellValue(boolean) straight afterwards.  This method only exists to give
     * the cell a somewhat reasonable value until the setCellValue() call (if at all).
     * TODO - perhaps a method like setCellTypeAndValue(int, Object) should be introduced to avoid this
     */
    private fun convertCellValueToBoolean(): Boolean {
        when (_cellType) {
            ICell.CELL_TYPE_BOOLEAN -> return (_record as BoolErrRecord).booleanValue
            ICell.CELL_TYPE_STRING -> {
                val sstIndex: Int = (_record as LabelSSTRecord).getSSTIndex()
                val text = _book.workbook.getSSTString(sstIndex)!!.string
                return text.toBoolean()
            }

            ICell.CELL_TYPE_NUMERIC -> return (_record as NumberRecord).getValue() != 0.0

            ICell.CELL_TYPE_FORMULA -> {
                // use cached formula result if it's the right type:
                val fr = (_record as FormulaRecordAggregate).formulaRecord
                checkFormulaCachedValueType(ICell.CELL_TYPE_BOOLEAN, fr)
                return fr.getCachedBooleanValue()
            }

            ICell.CELL_TYPE_ERROR, ICell.CELL_TYPE_BLANK -> return false
        }
        throw RuntimeException("Unexpected cell type (" + _cellType + ")")
    }

    private fun convertCellValueToString(): String {
        when (_cellType) {
            ICell.CELL_TYPE_BLANK -> return ""
            ICell.CELL_TYPE_BOOLEAN -> return if ((_record as BoolErrRecord).booleanValue) "TRUE" else "FALSE"
            ICell.CELL_TYPE_STRING -> {
                val sstIndex: Int = (_record as LabelSSTRecord).getSSTIndex()
                return _book.workbook.getSSTString(sstIndex)!!.string
            }

            ICell.CELL_TYPE_NUMERIC -> return NumberToTextConverter.toText((_record as NumberRecord).getValue())
            ICell.CELL_TYPE_ERROR -> return ErrorConstants.getText((_record as BoolErrRecord).errorValue.toInt())
            ICell.CELL_TYPE_FORMULA -> {}
            else -> throw IllegalStateException("Unexpected cell type (" + _cellType + ")")
        }
        val fra = (_record as FormulaRecordAggregate)
        val fr = fra.formulaRecord
        when (fr.getCachedResultType()) {
            ICell.CELL_TYPE_BOOLEAN -> return if (fr.getCachedBooleanValue()) "TRUE" else "FALSE"
            ICell.CELL_TYPE_STRING -> return fra.stringValue!!
            ICell.CELL_TYPE_NUMERIC -> return NumberToTextConverter.toText(fr.getValue())
            ICell.CELL_TYPE_ERROR -> return ErrorConstants.getText(fr.getCachedErrorValue())
        }
        throw IllegalStateException("Unexpected formula result type (" + _cellType + ")")
    }

    /**
     * get the value of the cell as a boolean.  For strings, numbers, and errors, we throw an exception.
     * For blank cells we return a false.
     */
    override fun getBooleanCellValue(): Boolean {
        when (_cellType) {
            ICell.CELL_TYPE_BLANK -> return false
            ICell.CELL_TYPE_BOOLEAN -> return (_record as BoolErrRecord).booleanValue
            ICell.CELL_TYPE_FORMULA -> {}
            else -> throw typeMismatch(ICell.CELL_TYPE_BOOLEAN, _cellType, false)
        }
        val fr = (_record as FormulaRecordAggregate).formulaRecord
        checkFormulaCachedValueType(ICell.CELL_TYPE_BOOLEAN, fr)
        return fr.getCachedBooleanValue()
    }

    /**
     * get the value of the cell as an error code.  For strings, numbers, and booleans, we throw an exception.
     * For blank cells we return a 0.
     */
    override fun getErrorCellValue(): Byte {
        when (_cellType) {
            ICell.CELL_TYPE_ERROR -> return (_record as BoolErrRecord).errorValue
            ICell.CELL_TYPE_FORMULA -> {}
            else -> throw typeMismatch(ICell.CELL_TYPE_ERROR, _cellType, false)
        }
        val fr = (_record as FormulaRecordAggregate).formulaRecord
        checkFormulaCachedValueType(ICell.CELL_TYPE_ERROR, fr)
        return fr.getCachedErrorValue().toByte()
    }

    /**
     * set the style for the cell.  The style should be an HSSFCellStyle created/retreived from
     * the HSSFWorkbook.
     * 
     * @param style  reference contained in the workbook
     * @see HSSFWorkbook.createCellStyle
     * @see HSSFWorkbook.getCellStyleAt
     */
    override fun setCellStyle(style: ICellStyle?) {
        setCellStyle(style as HSSFCellStyle)
    }

    fun setCellStyle(style: HSSFCellStyle) {
        // Verify it really does belong to our workbook
        style.verifyBelongsToWorkbook(_book)

        val styleIndex: Short
        if (style.userStyleName != null) {
            styleIndex = applyUserCellStyle(style)
        } else {
            styleIndex = style.getIndex()
        }

        // Change our cell record to use this style
        _record!!.xFIndex = styleIndex
    }

    /**
     * get the style for the cell.  This is a reference to a cell style contained in the workbook
     * object.
     * @see HSSFWorkbook.getCellStyleAt
     */
    override fun getCellStyle(): HSSFCellStyle {
        val styleIndex = _record!!.xFIndex
        val xf = _book.workbook.getExFormatAt(styleIndex.toInt())
        return HSSFCellStyle(styleIndex, xf, _book)
    }

    val cellStyleIndex: Int
        /**
         * 
         * @return
         */
        get() = _record!!.xFIndex.toInt()

    val cellValueRecord: CellValueRecordInterface
        /**
         * Should only be used by HSSFSheet and friends.  Returns the low level CellValueRecordInterface record
         * 
         * @return CellValueRecordInterface representing the cell via the low level api.
         */
        get() = _record!!

    /**
     * Sets this cell as the active cell for the worksheet
     */
    override fun setAsActiveCell() {
        val row = _record!!.row
        val col = _record!!.column
        _sheet.sheet.activeCellRow = row
        _sheet.sheet.activeCellCol = col
    }

    /**
     * Returns a string representation of the cell
     * 
     * This method returns a simple representation,
     * anthing more complex should be in user code, with
     * knowledge of the semantics of the sheet being processed.
     * 
     * Formula cells return the formula string,
     * rather than the formula result.
     * Dates are displayed in dd-MMM-yyyy format
     * Errors are displayed as #ERR&lt;errIdx&gt;
     */
    override fun toString(): String {
        when (getCellType()) {
            ICell.CELL_TYPE_BLANK -> return ""
            ICell.CELL_TYPE_BOOLEAN -> return if (getBooleanCellValue()) "TRUE" else "FALSE"
            ICell.CELL_TYPE_ERROR -> return ErrorEval.getText((_record as BoolErrRecord).errorValue.toInt())
            ICell.CELL_TYPE_FORMULA -> return getCellFormula().toString()
            ICell.CELL_TYPE_NUMERIC ->                 //TODO apply the dataformat for this cell
                /*if (HSSFDateUtil.isCellDateFormatted(this)) {
                    DateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
                    return sdf.format(getDateCellValue());
                }*/
                return getNumericCellValue().toString()

            ICell.CELL_TYPE_STRING -> return getStringCellValue()
            else -> return "Unknown Cell Type: " + getCellType()
        }
    }

    /**
     * Assign a comment to this cell. If the supplied
     * comment is null, the comment for this cell
     * will be removed.
     * 
     * @param comment comment associated with this cell
     */
    override fun setCellComment(comment: Comment?) {
        if (comment == null) {
            removeCellComment()
            return
        }

        comment.setRow(_record!!.row)
        comment.setColumn(_record!!.column.toInt())
        _comment = comment as HSSFComment
    }

    /**
     * Returns comment associated with this cell
     * 
     * @return comment associated with this cell
     */
    override fun getCellComment(): HSSFComment? {
        if (_comment == null) {
            _comment = Companion.findCellComment(_sheet.sheet, _record!!.row, _record!!.column.toInt())
        }
        return _comment
    }

    /**
     * Removes the comment for this cell, if
     * there is one.
     * WARNING - some versions of excel will loose
     * all comments after performing this action!
     */
    override fun removeCellComment() {
        val comment: HSSFComment? =
            Companion.findCellComment(_sheet.sheet, _record!!.row, _record!!.column.toInt())
        _comment = null

        if (comment == null) {
            // Nothing to do
            return
        }

        // Zap the underlying NoteRecord
        val sheetRecords: MutableList<RecordBase> = _sheet.sheet.records
        comment.noteRecord?.let { sheetRecords.remove(it) }

        // If we have a TextObjectRecord, is should
        //  be proceeed by:
        // MSODRAWING with container
        // OBJ
        // MSODRAWING with EscherTextboxRecord
        val txo = comment.textObjectRecord
        if (txo != null) {
            val txoAt = sheetRecords.indexOf(txo)

            if (sheetRecords.get(txoAt - 3) is DrawingRecord &&
                sheetRecords.get(txoAt - 2) is ObjRecord &&
                sheetRecords.get(txoAt - 1) is DrawingRecord
            ) {
                // Zap these, in reverse order
                sheetRecords.removeAt(txoAt - 1)
                sheetRecords.removeAt(txoAt - 2)
                sheetRecords.removeAt(txoAt - 3)
            } else {
                throw IllegalStateException("Found the wrong records before the TextObjectRecord, can't remove comment")
            }

            // Now remove the text record
            sheetRecords.remove(txo)
        }
    }

    /**
     * @return hyperlink associated with this cell or `null` if not found
     */
    override fun getHyperlink(): HSSFHyperlink? {
        val it: MutableIterator<RecordBase> = _sheet.sheet.records.iterator()
        while (it.hasNext()) {
            val rec = it.next()
            if (rec is HyperlinkRecord) {
                val link = rec
                if (link.getFirstColumn() == _record!!.column.toInt() && link.getFirstRow() == _record!!.row) {
                    return HSSFHyperlink(link)
                }
            }
        }
        return null
    }

    /**
     * Assign a hyperlink to this cell
     * 
     * @param hyperlink hyperlink associated with this cell
     */
    override fun setHyperlink(hyperlink: IHyperlink?) {
        val link = hyperlink as HSSFHyperlink

        link.setFirstRow(_record!!.row)
        link.setLastRow(_record!!.row)
        link.setFirstColumn(_record!!.column.toInt())
        link.setLastColumn(_record!!.column.toInt())

        when (link.getType()) {
            HSSFHyperlink.Companion.LINK_EMAIL, HSSFHyperlink.Companion.LINK_URL -> link.setLabel("url")
            HSSFHyperlink.Companion.LINK_FILE -> link.setLabel("file")
            HSSFHyperlink.Companion.LINK_DOCUMENT -> link.setLabel("place")
        }

        val records: MutableList<RecordBase> = _sheet.sheet.records
        val eofLoc = records.size - 1
        if (link.record != null) {
            records.add(eofLoc, link.record!!)
        }
    }

    /**
     * Only valid for formula cells
     * @return one of ([.CELL_TYPE_NUMERIC], [.CELL_TYPE_STRING],
     * [.CELL_TYPE_BOOLEAN], [.CELL_TYPE_ERROR]) depending
     * on the cached value of the formula
     */
    override fun getCachedFormulaResultType(): Int {
        check(_cellType == ICell.CELL_TYPE_FORMULA) { "Only formula cells have cached results" }
        return (_record as FormulaRecordAggregate).formulaRecord.getCachedResultType()
    }

    fun setCellArrayFormula(range: HSSFCellRangeAddress) {
        val row = _record!!.row
        val col = _record!!.column
        val styleIndex = _record!!.xFIndex
        setCellType(ICell.CELL_TYPE_FORMULA, false, row, col, styleIndex)

        // Billet for formula in rec
        val ptgsForCell = arrayOf<Ptg?>(ExpPtg(range.getFirstRow(), range.getFirstColumn()))
        val agg = _record as FormulaRecordAggregate
        agg.setParsedExpression(ptgsForCell)
    }

    override fun getArrayFormulaRange(): HSSFCellRangeAddress {
        if (_cellType != ICell.CELL_TYPE_FORMULA) {
            val ref = CellReference(this).formatAsString()
            throw IllegalStateException(
                ("Cell " + ref
                        + " is not part of an array formula.")
            )
        }
        return (_record as FormulaRecordAggregate).arrayFormulaRange
    }

    override fun isPartOfArrayFormulaGroup(): Boolean {
        if (_cellType != ICell.CELL_TYPE_FORMULA) {
            return false
        }
        return (_record as FormulaRecordAggregate).isPartOfArrayFormula
    }

    /**
     * The purpose of this method is to validate the cell state prior to modification
     * 
     * @see .notifyArrayFormulaChanging
     */
    fun notifyArrayFormulaChanging(msg: String?) {
        val cra = getArrayFormulaRange()
        check(cra.getNumberOfCells() <= 1) { msg!! }
        //un-register the single-cell array formula from the parent XSSFSheet
        getRow()!!.getSheet().removeArrayFormula(this)
    }

    /**
     * Called when this cell is modified.
     * 
     * 
     * The purpose of this method is to validate the cell state prior to modification.
     * 
     * 
     * @see .setCellType
     * @see .setCellFormula
     * @see HSSFRow.removeCell
     * @see HSSFSheet.removeRow
     * @see HSSFSheet.shiftRows
     * @see HSSFSheet.addMergedRegion
     * @throws IllegalStateException if modification is not allowed
     */
    fun notifyArrayFormulaChanging() {
        val ref = CellReference(this)
        val msg = "Cell " + ref.formatAsString() + " is part of a multi-cell array formula. " +
                "You cannot change part of an array."
        notifyArrayFormulaChanging(msg)
    }

    /**
     * Applying a user-defined style (UDS) is special. Excel does not directly reference user-defined styles, but
     * instead create a 'proxy' ExtendedFormatRecord referencing the UDS as parent.
     * 
     * The proceudre to apply a UDS is as follows:
     * 
     * 1. search for a ExtendedFormatRecord with parentIndex == style.getIndex()
     * and xfType ==  ExtendedFormatRecord.XF_CELL.
     * 2. if not found then create a new ExtendedFormatRecord and copy all attributes from the user-defined style
     * and set the parentIndex to be style.getIndex()
     * 3. return the index of the ExtendedFormatRecord, this will be assigned to the parent cell record
     * 
     * @param style  the user style to apply
     * 
     * @return  the index of a ExtendedFormatRecord record that will be referenced by the cell
     */
    private fun applyUserCellStyle(style: HSSFCellStyle): Short {
        requireNotNull(style.userStyleName) { "Expected user-defined style" }

        val iwb = _book.workbook
        var userXf: Short = -1
        val numfmt = iwb.numExFormats
        for (i in 0..<numfmt) {
            val xf = iwb.getExFormatAt(i.toInt())
            if (xf!!.getXFType() == ExtendedFormatRecord.XF_CELL && xf.getParentIndex() == style.getIndex()) {
                userXf = i.toShort()
                break
            }
        }
        val styleIndex: Short
        if (userXf.toInt() == -1) {
            val xfr = iwb.createCellXF()
            xfr.cloneStyleFrom(iwb.getExFormatAt(style.getIndex().toInt())!!)
            xfr.setIndentionOptions(0.toShort())
            xfr.setXFType(ExtendedFormatRecord.XF_CELL)
            xfr.setParentIndex(style.getIndex())
            styleIndex = numfmt.toShort()
        } else {
            styleIndex = userXf
        }

        return styleIndex
    }

    companion object {
        private val log = getLogger(HSSFCell::class.java)

        private const val FILE_FORMAT_NAME = "BIFF8"

        /**
         * The maximum  number of columns in BIFF8
         */
        val LAST_COLUMN_NUMBER: Int = SpreadsheetVersion.EXCEL97.getLastColumnIndex() // 2^8 - 1
        private val LAST_COLUMN_NAME: String? = SpreadsheetVersion.EXCEL97.getLastColumnName()

        val ENCODING_UNCHANGED: Short = -1
        const val ENCODING_COMPRESSED_UNICODE: Short = 0
        const val ENCODING_UTF_16: Short = 1

        /**
         * used internally -- given a cell value record, figure out its type
         */
        private fun determineType(cval: CellValueRecordInterface?): Int {
            if (cval is FormulaRecordAggregate) {
                return ICell.CELL_TYPE_FORMULA
            }
            // all others are plain BIFF records
            val record = cval as Record
            when (record.getSid()) {
                NumberRecord.sid -> return ICell.CELL_TYPE_NUMERIC
                BlankRecord.sid -> return ICell.CELL_TYPE_BLANK
                LabelSSTRecord.sid -> return ICell.CELL_TYPE_STRING
                BoolErrRecord.sid -> {
                    val boolErrRecord = record as BoolErrRecord

                    return if (boolErrRecord.isBoolean)
                        ICell.CELL_TYPE_BOOLEAN
                    else
                        ICell.CELL_TYPE_ERROR
                }
            }
            throw RuntimeException("Bad cell value rec (" + cval.javaClass.getName() + ")")
        }

        /**
         * Used to help format error messages
         */
        private fun getCellTypeName(cellTypeCode: Int): String {
            when (cellTypeCode) {
                ICell.CELL_TYPE_BLANK -> return "blank"
                ICell.CELL_TYPE_STRING -> return "text"
                ICell.CELL_TYPE_BOOLEAN -> return "boolean"
                ICell.CELL_TYPE_ERROR -> return "error"
                ICell.CELL_TYPE_NUMERIC -> return "numeric"
                ICell.CELL_TYPE_FORMULA -> return "formula"
            }
            return "#unknown cell type (" + cellTypeCode + ")#"
        }

        private fun typeMismatch(
            expectedTypeCode: Int,
            actualTypeCode: Int,
            isFormulaCell: Boolean
        ): RuntimeException {
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
         * @throws RuntimeException if the bounds are exceeded.
         */
        private fun checkBounds(cellIndex: Int) {
            require(!(cellIndex < 0 || cellIndex > LAST_COLUMN_NUMBER)) {
                ("Invalid column index (" + cellIndex
                        + ").  Allowable column range for " + FILE_FORMAT_NAME + " is (0.."
                        + LAST_COLUMN_NUMBER + ") or ('A'..'" + LAST_COLUMN_NAME + "')")
            }
        }

        /**
         * Cell comment finder.
         * Returns cell comment for the specified sheet, row and column.
         * 
         * @return cell comment or `null` if not found
         */
        fun findCellComment(sheet: InternalSheet, row: Int, column: Int): HSSFComment? {
            // TODO - optimise this code by searching backwards, find NoteRecord first, quit if not found. Find one TXO by id
            var comment: HSSFComment? = null
            val noteTxo: MutableMap<Int?, TextObjectRecord?> =
                HashMap<Int?, TextObjectRecord?>()
            var i = 0
            val it: MutableIterator<RecordBase> = sheet.records.iterator()
            while (it.hasNext()) {
                var rec = it.next()
                if (rec is NoteRecord) {
                    val note = rec
                    if (note.getRow() == row && note.getColumn() == column) {
                        if (i < noteTxo.size) {
                            val txo = noteTxo.get(note.getShapeId())
                            if (txo != null) {
                                comment = HSSFComment(note, txo)
                                comment.setRow(note.getRow())
                                comment.setColumn(note.getColumn())
                                comment.setAuthor(note.getAuthor())
                                comment.setVisible(note.getFlags() == NoteRecord.NOTE_VISIBLE)
                                comment.setString(txo.getStr())
                            } else {
                                log.log(
                                    POILogger.WARN,
                                    "Failed to match NoteRecord and TextObjectRecord, row: " + row + ", column: " + column
                                )
                            }
                        } else {
                            log.log(
                                POILogger.WARN,
                                "Failed to match NoteRecord and TextObjectRecord, row: " + row + ", column: " + column
                            )
                        }
                        break
                    }
                    i++
                } else if (rec is ObjRecord) {
                    val obj = rec
                    val sub: SubRecord? = obj.getSubRecords()!!.get(0)
                    if (sub is CommonObjectDataSubRecord) {
                        val cmo = sub
                        if (cmo.objectType == CommonObjectDataSubRecord.OBJECT_TYPE_COMMENT) {
                            //map ObjectId and corresponding TextObjectRecord,
                            //it will be used to match NoteRecord and TextObjectRecord
                            while (it.hasNext()) {
                                rec = it.next()
                                if (rec is TextObjectRecord) {
                                    noteTxo.put(cmo.objectId, rec)
                                    break
                                }
                            }
                        }
                    }
                }
            }
            return comment
        }
    }
}
