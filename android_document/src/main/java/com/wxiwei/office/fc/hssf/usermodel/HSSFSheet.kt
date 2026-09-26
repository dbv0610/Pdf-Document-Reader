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

import com.wxiwei.office.fc.ddf.EscherRecord
import com.wxiwei.office.fc.hssf.formula.FormulaShifter.Companion.createForRowShift
import com.wxiwei.office.fc.hssf.formula.ptg.Area3DPtg
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.model.InternalSheet
import com.wxiwei.office.fc.hssf.model.InternalSheet.Companion.createSheet
import com.wxiwei.office.fc.hssf.model.InternalWorkbook
import com.wxiwei.office.fc.hssf.record.AutoFilterInfoRecord
import com.wxiwei.office.fc.hssf.record.CellValueRecordInterface
import com.wxiwei.office.fc.hssf.record.DimensionsRecord
import com.wxiwei.office.fc.hssf.record.EscherAggregate
import com.wxiwei.office.fc.hssf.record.NameRecord
import com.wxiwei.office.fc.hssf.record.NoteRecord
import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.hssf.record.RowRecord
import com.wxiwei.office.fc.hssf.record.SCLRecord
import com.wxiwei.office.fc.hssf.record.WSBoolRecord
import com.wxiwei.office.fc.hssf.record.WindowTwoRecord
import com.wxiwei.office.fc.hssf.record.aggregates.FormulaRecordAggregate
import com.wxiwei.office.fc.hssf.record.aggregates.WorksheetProtectionBlock
import com.wxiwei.office.fc.hssf.util.ColumnInfo
import com.wxiwei.office.fc.hssf.util.HSSFPaneInformation
import com.wxiwei.office.fc.ss.SpreadsheetVersion
import com.wxiwei.office.fc.ss.usermodel.CellRange
import com.wxiwei.office.fc.ss.usermodel.DataValidation
import com.wxiwei.office.fc.ss.usermodel.DataValidationHelper
import com.wxiwei.office.fc.ss.usermodel.ICell
import com.wxiwei.office.fc.ss.usermodel.ICellStyle
import com.wxiwei.office.fc.ss.usermodel.IRow
import com.wxiwei.office.fc.ss.usermodel.Sheet
import com.wxiwei.office.fc.ss.util.CellReference
import com.wxiwei.office.fc.ss.util.HSSFCellRangeAddress
import com.wxiwei.office.fc.ss.util.Region
import com.wxiwei.office.fc.ss.util.SSCellRange
import com.wxiwei.office.fc.ss.util.SheetUtil
import com.wxiwei.office.fc.util.POILogFactory.Companion.getLogger
import com.wxiwei.office.fc.util.POILogger
import java.io.PrintWriter
import java.util.TreeMap
import kotlin.math.max
import kotlin.math.min

/**
 * High level representation of a worksheet.
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author  Glen Stampoultzis (glens at apache.org)
 * @author  Libin Roman (romal at vistaportal.com)
 * @author  Shawn Laubach (slaubach at apache dot org) (Just a little)
 * @author  Jean-Pierre Paris (jean-pierre.paris at m4x dot org) (Just a little, too)
 * @author  Yegor Kozlov (yegor at apache.org) (Autosizing columns)
 * @author  Josh Micich
 * @author  Petr Udalau(Petr.Udalau at exigenservices.com) - set/remove array formulas
 */
class HSSFSheet : Sheet {
    /**
     * used internally in the API to get the low level Sheet record represented by this
     * Object.
     * @return Sheet - low level representation of this HSSFSheet.
     */
    /**
     * reference to the low level [InternalSheet] object
     */
    val sheet: InternalSheet

    /** stores rows by zero-based row number  */
    private val _rows: TreeMap<Int?, HSSFRow?>
    protected val _book: InternalWorkbook

    /**
     * Return the parent workbook
     * 
     * @return the parent workbook
     */
    val workbook: HSSFWorkbook
    private val _patriarch: HSSFPatriarch? = null

    /**
     * Gets the first row on the sheet
     * @return the number of the first logical row on the sheet, zero based
     */
    var firstRowNum: Int = 0
        private set

    /**
     * Gets the number last row on the sheet.
     * Owing to idiosyncrasies in the excel file
     * format, if the result of calling this method
     * is zero, you can't tell if that means there
     * are zero rows on the sheet, or one at
     * position zero. For that case, additionally
     * call [.getPhysicalNumberOfRows] to
     * tell if there is a row at position zero
     * or not.
     * @return the number of the last row contained in this sheet, zero based.
     */
    var lastRowNum: Int = 0
        private set

    /**
     * @return Returns the isInitForDraw.
     */
    /**
     * @param isInitForDraw The isInitForDraw to set.
     */
    // 是否已初始化过绘制值
    var isInitForDraw: Boolean = false

    //zoom
    var zoom: Float = 1f

    //pan
    var scrollX: Int = 0
        private set
    var scrollY: Int = 0
        private set

    //active cell
    /**
     * get active cell row index
     * @return
     */
    var activeCellRow: Int = -1
        private set

    /**
     * get active cell column index
     * @return
     */
    var activeCellColumn: Int = -1
        private set

    private var _paneInformation: HSSFPaneInformation? = null

    /**
     * Creates new HSSFSheet   - called by HSSFWorkbook to create a sheet from
     * scratch.  You should not be calling this from application code (its protected anyhow).
     * 
     * @param workbook - The HSSF Workbook object associated with the sheet.
     * @see HSSFWorkbook.createSheet
     */
    constructor(workbook: HSSFWorkbook) {
        this.sheet = createSheet()
        _rows = TreeMap<Int?, HSSFRow?>()
        this.workbook = workbook
        this._book = workbook.workbook
    }

    /**
     * Creates an HSSFSheet representing the given Sheet object.  Should only be
     * called by HSSFWorkbook when reading in an exisiting file.
     * 
     * @param workbook - The HSSF Workbook object associated with the sheet.
     * @param sheet - lowlevel Sheet object this sheet will represent
     * @see HSSFWorkbook.createSheet
     */
    constructor(workbook: HSSFWorkbook, sheet: InternalSheet) {
        this.sheet = sheet
        _rows = TreeMap<Int?, HSSFRow?>()
        this.workbook = workbook
        this._book = workbook.workbook
        setPropertiesFromSheet(sheet)
    }

    fun cloneSheet(workbook: HSSFWorkbook): HSSFSheet {
        return HSSFSheet(workbook, sheet.cloneSheet())
    }

    /**
     * used internally to set the properties given a Sheet object
     */
    private fun setPropertiesFromSheet(sheet: InternalSheet) {
        var row = sheet.nextRow
        val rowRecordsAlreadyPresent = row != null

        while (row != null) {
            createRowFromRecord(row)

            row = sheet.nextRow
        }

        val iter: MutableIterator<CellValueRecordInterface?> = sheet.cellValueIterator
        val timestart = System.currentTimeMillis()

        if (log.check(POILogger.DEBUG)) log.log(
            DEBUG, "Time at start of cell creating in HSSF sheet = ",
            timestart
        )
        var lastrow: HSSFRow? = null

        // Add every cell to its row
        while (iter.hasNext()) {
            val cval = iter.next() ?: continue

            val cellstart = System.currentTimeMillis()
            var hrow = lastrow

            if (hrow == null || hrow.rowNum != cval.row) {
                hrow = getRow(cval.row)
                lastrow = hrow
                if (hrow == null) {
                    // Some tools (like Perl module Spreadsheet::WriteExcel - bug 41187) skip the RowRecords
                    // Excel, OpenOffice.org and GoogleDocs are all OK with this, so POI should be too.
                    if (rowRecordsAlreadyPresent) {
                        // if at least one row record is present, all should be present.
                        throw RuntimeException(
                            "Unexpected missing row when some rows already present"
                        )
                    }
                    // create the row record on the fly now.
                    val rowRec = RowRecord(cval.row)
                    sheet.addRow(rowRec)
                    hrow = createRowFromRecord(rowRec)
                }
            }
            if (log.check(POILogger.DEBUG)) log.log(
                DEBUG,
                "record id = " + Integer.toHexString((cval as Record).getSid().toInt())
            )
            hrow.createCellFromRecord(cval)
            if (log.check(POILogger.DEBUG)) log.log(
                DEBUG,
                "record took ",
                System.currentTimeMillis() - cellstart
            )
        }
        if (log.check(POILogger.DEBUG)) log.log(
            DEBUG, "total sheet cell creation took ",
            System.currentTimeMillis() - timestart
        )
    }

    /**
     * Create a new row within the sheet and return the high level representation
     * 
     * @param rownum  row number
     * @return High level HSSFRow object representing a row in the sheet
     * @see HSSFRow
     * 
     * @see .removeRow
     */
    fun createRow(rownum: Int): HSSFRow {
        val row = HSSFRow(this.workbook, this, rownum)

        addRow(row, true)
        return row
    }

    /**
     * Used internally to create a high level Row object from a low level row object.
     * USed when reading an existing file
     * @param row  low level record to represent as a high level Row and add to sheet
     * @return HSSFRow high level representation
     */
    private fun createRowFromRecord(row: RowRecord): HSSFRow {
        val hrow = HSSFRow(this.workbook, this, row)

        addRow(hrow, false)
        return hrow
    }

    /**
     * Remove a row from this sheet.  All cells contained in the row are removed as well
     * 
     * @param row   representing a row to remove.
     */
    fun removeRow(row: IRow) {
        val hrow = row as HSSFRow?
        require(row.sheet === this) { "Specified row does not belong to this sheet" }
        for (cell in row) {
            val xcell = cell as HSSFCell
            if (xcell.isPartOfArrayFormulaGroup()) {
                val msg = ("Row[rownum="
                        + row.rowNum
                        + "] contains cell(s) included in a multi-cell array formula. You cannot change part of an array.")
                xcell.notifyArrayFormulaChanging(msg)
            }
        }

        if (_rows.size > 0) {
            val key = row.rowNum
            val removedRow = _rows.remove(key)
            require(removedRow === row) { "Specified row does not belong to this sheet" }
            if (hrow!!.rowNum == this.lastRowNum) {
                this.lastRowNum = findLastRow(this.lastRowNum)
            }
            if (hrow.rowNum == this.firstRowNum) {
                this.firstRowNum = findFirstRow(this.firstRowNum)
            }
            sheet.removeRow(hrow.rowRecord)
        }
    }

    /**
     * used internally to refresh the "last row" when the last row is removed.
     */
    private fun findLastRow(lastrow: Int): Int {
        if (lastrow < 1) {
            return 0
        }
        var rownum = lastrow - 1
        var r = getRow(rownum)

        while (r == null && rownum > 0) {
            r = getRow(--rownum)
        }
        if (r == null) {
            return 0
        }
        return rownum
    }

    /**
     * used internally to refresh the "first row" when the first row is removed.
     */
    private fun findFirstRow(firstrow: Int): Int {
        var rownum = firstrow + 1
        var r = getRow(rownum)

        while (r == null && rownum <= this.lastRowNum) {
            r = getRow(++rownum)
        }

        if (rownum > this.lastRowNum) return 0

        return rownum
    }

    /**
     * add a row to the sheet
     * 
     * @param addLow whether to add the row to the low level model - false if its already there
     */
    private fun addRow(row: HSSFRow, addLow: Boolean) {
        _rows.put(row.rowNum, row)
        if (addLow) {
            sheet.addRow(row.rowRecord)
        }
        val firstRow = _rows.size == 1
        if (row.rowNum > this.lastRowNum || firstRow) {
            this.lastRowNum = row.rowNum
        }
        if (row.rowNum < this.firstRowNum || firstRow) {
            this.firstRowNum = row.rowNum
        }
    }

    /**
     * Returns the logical row (not physical) 0-based.  If you ask for a row that is not
     * defined you get a null.  This is to say row 4 represents the fifth row on a sheet.
     * @param rowIndex  row to get
     * @return HSSFRow representing the row number or null if its not defined on the sheet
     */
    fun getRow(rowIndex: Int): HSSFRow? {
        return _rows.get(rowIndex)
    }

    val physicalNumberOfRows: Int
        /**
         * Returns the number of physically defined rows (NOT the number of rows in the sheet)
         */
        get() = _rows.size

    /**
     * Creates a data validation object
     * @param dataValidation The Data validation object settings
     */
    fun addValidationData(dataValidation: DataValidation) {
        requireNotNull(dataValidation) { "objValidation must not be null" }
        val hssfDataValidation = dataValidation as HSSFDataValidation
        val dvt = sheet.orCreateDataValidityTable

        val dvRecord = hssfDataValidation.createDVRecord(this)
        dvt.addDataValidation(dvRecord)
    }

    @Deprecated("(Sep 2008) use {@link #setColumnHidden(int, boolean)}")
    fun setColumnHidden(columnIndex: Short, hidden: Boolean) {
        setColumnHidden(columnIndex.toInt() and 0xFFFF, hidden)
    }

    @Deprecated("(Sep 2008) use {@link #isColumnHidden(int)}")
    fun isColumnHidden(columnIndex: Short): Boolean {
        return isColumnHidden(columnIndex.toInt() and 0xFFFF)
    }

    @Deprecated("(Sep 2008) use {@link #setColumnWidth(int, int)}")
    fun setColumnWidth(columnIndex: Short, width: Short) {
        setColumnWidth(columnIndex.toInt() and 0xFFFF, width.toInt() and 0xFFFF)
    }

    @Deprecated("(Sep 2008) use {@link #getColumnWidth(int)}")
    fun getColumnWidth(columnIndex: Short): Short {
        return getColumnWidth(columnIndex.toInt() and 0xFFFF).toShort()
    }

    @Deprecated("(Sep 2008) use {@link #setDefaultColumnWidth(int)}")
    fun setDefaultColumnWidth(width: Short) {
        setDefaultColumnWidth(width.toInt() and 0xFFFF)
    }

    /**
     * Get the visibility state for a given column.
     * @param columnIndex - the column to get (0-based)
     * @param hidden - the visiblity state of the column
     */
    fun setColumnHidden(columnIndex: Int, hidden: Boolean) {
        sheet.setColumnHidden(columnIndex, hidden)
    }

    /**
     * Get the hidden state for a given column.
     * @param columnIndex - the column to set (0-based)
     * @return hidden - `false` if the column is visible
     */
    fun isColumnHidden(columnIndex: Int): Boolean {
        return sheet.isColumnHidden(columnIndex)
    }

    val columnInfo: MutableList<ColumnInfo?>?
        /**
         * get the column information
         * @return
         */
        get() = sheet.columnInfo

    /**
     * Set the width (in units of 1/256th of a character width)
     * 
     * 
     * 
     * The maximum column width for an individual cell is 255 characters.
     * This value represents the number of characters that can be displayed
     * in a cell that is formatted with the standard font (first font in the workbook).
     * 
     * 
     * 
     * 
     * Character width is defined as the maximum digit width
     * of the numbers `0, 1, 2, ... 9` as rendered
     * using the default font (first font in the workbook).
     * <br></br>
     * Unless you are using a very special font, the default character is '0' (zero),
     * this is true for Arial (default font font in HSSF) and Calibri (default font in XSSF)
     * 
     * 
     * 
     * 
     * Please note, that the width set by this method includes 4 pixels of margin padding (two on each side),
     * plus 1 pixel padding for the gridlines (Section 3.3.1.12 of the OOXML spec).
     * This results is a slightly less value of visible characters than passed to this method (approx. 1/2 of a character).
     * 
     * 
     * 
     * To compute the actual number of visible characters,
     * Excel uses the following formula (Section 3.3.1.12 of the OOXML spec):
     * 
     * `
     * width = Truncate([{Number of Visible Characters} *
     * {Maximum Digit Width} + {5 pixel padding}]/{Maximum Digit Width}*256)/256
    ` * 
     * 
     * Using the Calibri font as an example, the maximum digit width of 11 point font size is 7 pixels (at 96 dpi).
     * If you set a column width to be eight characters wide, e.g. `setColumnWidth(columnIndex, 8*256)`,
     * then the actual value of visible characters (the value shown in Excel) is derived from the following equation:
     * `
     * Truncate([numChars*7+5]/7*256)/256 = 8;
    ` * 
     * 
     * which gives `7.29`.
     * 
     * @param columnIndex - the column to set (0-based)
     * @param width - the width in units of 1/256th of a character width
     * @throws IllegalArgumentException if width > 255*256 (the maximum column width in Excel is 255 characters)
     */
    fun setColumnWidth(columnIndex: Int, width: Int) {
        sheet.setColumnWidth(columnIndex, width)
    }

    /**
     * get the width (in units of 1/256th of a character width )
     * @param columnIndex - the column to set (0-based)
     * @return width - the width in units of 1/256th of a character width
     */
    fun getColumnWidth(columnIndex: Int): Int {
        return sheet.getColumnWidth(columnIndex)
    }

    /**
     * 得到像素单位列宽
     */
    fun getColumnPixelWidth(columnIndex: Int): Int {
        return sheet.getColumnPixelWidth(columnIndex)
    }

    /**
     * 设置像素单位列宽
     */
    fun setColumnPixelWidth(columnIndex: Int, width: Int) {
        sheet.setColumnPixelWidth(columnIndex, width)
    }

    val defaultColumnWidth: Int
        /**
         * get the default column width for the sheet (if the columns do not define their own width) in
         * characters
         * @return default column width
         */
        get() = sheet.defaultColumnWidth

    /**
     * set the default column width for the sheet (if the columns do not define their own width) in
     * characters
     * @param width default column width
     */
    fun setDefaultColumnWidth(width: Int) {
        sheet.defaultColumnWidth = width
    }

    var defaultRowHeight: Short
        /**
         * get the default row height for the sheet (if the rows do not define their own height) in
         * twips (1/20 of  a point)
         * @return  default row height
         */
        get() = sheet.defaultRowHeight
        /**
         * set the default row height for the sheet (if the rows do not define their own height) in
         * twips (1/20 of  a point)
         * @param  height default row height
         */
        set(height) {
            sheet.defaultRowHeight = height
        }

    var defaultRowHeightInPoints: Float
        /**
         * get the default row height for the sheet (if the rows do not define their own height) in
         * points.
         * @return  default row height in points
         */
        get() = (sheet.defaultRowHeight.toFloat() / 20)
        /**
         * set the default row height for the sheet (if the rows do not define their own height) in
         * points
         * @param height default row height
         */
        set(height) {
            sheet.defaultRowHeight = (height * 20).toInt().toShort()
        }

    /**
     * Returns the HSSFCellStyle that applies to the given
     * (0 based) column, or null if no style has been
     * set for that column
     */
    fun getColumnStyle(column: Int): HSSFCellStyle? {
        val styleIndex = sheet.getXFIndexForColAt(column.toShort())

        if (styleIndex.toInt() == 0xf) {
            // None set
            return null
        }

        val xf = _book.getExFormatAt(styleIndex.toInt())
        return HSSFCellStyle(styleIndex, xf, _book)
    }

    var isGridsPrinted: Boolean
        /**
         * get whether gridlines are printed.
         * @return true if printed
         */
        get() = sheet.isGridsPrinted
        /**
         * set whether gridlines printed.
         * @param value  false if not printed.
         */
        set(value) {
            sheet.isGridsPrinted = value
        }

    @Deprecated("(Aug-2008) use <tt>CellRangeAddress</tt> instead of <tt>Region</tt>")
    fun addMergedRegion(region: Region): Int {
        return sheet.addMergedRegion(
            region.rowFrom, region.columnFrom.toInt(),  //(short) region.getRowTo(),
            region.rowTo, region.columnTo.toInt()
        )
    }

    /**
     * adds a merged region of cells (hence those cells form one)
     * @param region (rowfrom/colfrom-rowto/colto) to merge
     * @return index of this region
     */
    fun addMergedRegion(region: HSSFCellRangeAddress): Int {
        region.validate(SpreadsheetVersion.EXCEL97)

        // throw IllegalStateException if the argument CellRangeAddress intersects with
        // a multi-cell array formula defined in this sheet
        validateArrayFormulas(region)

        return sheet.addMergedRegion(
            region.firstRow, region.firstColumn,
            region.lastRow, region.lastColumn
        )
    }

    private fun validateArrayFormulas(region: HSSFCellRangeAddress) {
        val firstRow = region.firstRow
        val firstColumn = region.firstColumn
        val lastRow = region.lastRow
        val lastColumn = region.lastColumn
        for (rowIn in firstRow..lastRow) {
            for (colIn in firstColumn..lastColumn) {
                val row = getRow(rowIn) ?: continue

                val cell = row.getCell(colIn) ?: continue

                if (cell.isPartOfArrayFormulaGroup()) {
                    val arrayRange = cell.getArrayFormulaRange()
                    if (arrayRange.numberOfCells > 1
                        && (arrayRange.isInRange(
                            region.firstRow,
                            region.firstColumn
                        ) || arrayRange
                            .isInRange(region.firstRow, region.firstColumn))
                    ) {
                        val msg = ("The range " + region.formatAsString()
                                + " intersects with a multi-cell array formula. "
                                + "You cannot merge cells of an array.")
                        throw IllegalStateException(msg)
                    }
                }
            }
        }
    }

    var forceFormulaRecalculation: Boolean
        /**
         * Whether a record must be inserted or not at generation to indicate that
         * formula must be recalculated when workbook is opened.
         * @return true if an uncalced record must be inserted or not at generation
         */
        get() = sheet.uncalced
        /**
         * Control if Excel should be asked to recalculate all formulas on this sheet
         * when the workbook is opened.
         * 
         * 
         * 
         * Calculating the formula values with [com.wxiwei.office.fc.ss.usermodel.com.wxiwei.office.fc.ss.usermodel.FormulaEvaluator] is the
         * recommended solution, but this may be used for certain cases where
         * evaluation in POI is not possible.
         * 
         * 
         * 
         * 
         * It is recommended to force recalcuation of formulas on workbook level using
         * [com.wxiwei.office.ss.model.baseModel.com.wxiwei.office.fc.ss.usermodel.Workbook.setForceFormulaRecalculation]
         * to ensure that all cross-worksheet formuals and external dependencies are updated.
         * 
         * @param value true if the application will perform a full recalculation of
         * this worksheet values when the workbook is opened
         * 
         * @see com.wxiwei.office.ss.model.baseModel.com.wxiwei.office.fc.ss.usermodel.Workbook.setForceFormulaRecalculation
         */
        set(value) {
            sheet.uncalced = value
        }

    /**
     * TODO: Boolean not needed, remove after next release
     */
    @Deprecated("(Mar-2008) use getVerticallyCenter() instead")
    fun getVerticallyCenter(value: Boolean): Boolean {
        return this.verticallyCenter
    }

    var verticallyCenter: Boolean
        /**
         * Determine whether printed output for this sheet will be vertically centered.
         */
        get() = sheet.pageSettings!!.vCenter!!.getVCenter()
        /**
         * determines whether the output is vertically centered on the page.
         * @param value true to vertically center, false otherwise.
         */
        set(value) {
            sheet.pageSettings!!.vCenter!!.setVCenter(value)
        }

    var horizontallyCenter: Boolean
        /**
         * Determine whether printed output for this sheet will be horizontally centered.
         */
        get() = sheet.pageSettings.hCenter!!.getHCenter()
        /**
         * determines whether the output is horizontally centered on the page.
         * @param value true to horizontally center, false otherwise.
         */
        set(value) {
            sheet.pageSettings.hCenter!!.setHCenter(value)
        }

    var isRightToLeft: Boolean
        /**
         * Whether the text is displayed in right-to-left mode in the window
         * 
         * @return whether the text is displayed in right-to-left mode in the window
         */
        get() = sheet.windowTwo!!.getArabic()
        /**
         * Sets whether the worksheet is displayed from right to left instead of from left to right.
         * 
         * @param value true for right to left, false otherwise.
         */
        set(value) {
            sheet.windowTwo!!.setArabic(value)
        }

    /**
     * removes a merged region of cells (hence letting them free)
     * @param index of the region to unmerge
     */
    fun removeMergedRegion(index: Int) {
        sheet.removeMergedRegion(index)
    }

    val numMergedRegions: Int
        /**
         * returns the number of merged regions
         * @return number of merged regions
         */
        get() = sheet.numMergedRegions

    @Deprecated("(Aug-2008) use {@link HSSFSheet#getMergedRegion(int)}")
    fun getMergedRegionAt(index: Int): com.wxiwei.office.fc.hssf.util.Region {
        val cra = getMergedRegion(index)

        return com.wxiwei.office.fc.hssf.util.Region(
            cra.firstRow, cra.firstColumn.toShort(), cra.lastRow,
            cra.lastColumn.toShort()
        )
    }

    /**
     * @return the merged region at the specified index
     */
    fun getMergedRegion(index: Int): HSSFCellRangeAddress {
        return sheet.getMergedRegionAt(index)!!
    }

    /**
     * @return an iterator of the PHYSICAL rows.  Meaning the 3rd element may not
     * be the third row if say for instance the second row is undefined.
     * Call getRowNum() on each row if you care which one it is.
     */
    fun rowIterator(): MutableIterator<IRow?> {
        val result// can this clumsy generic syntax be improved?
                = _rows.values.iterator() as MutableIterator<out IRow?>
        return result
    }

    /**
     * Alias for [.rowIterator] to allow
     * foreach loops
     */
    fun iterator(): MutableIterator<IRow?> {
        return rowIterator()
    }

    /**
     * whether alternate expression evaluation is on
     * @param b  alternative expression evaluation or not
     */
    fun setAlternativeExpression(b: Boolean) {
        val record = sheet.findFirstRecordBySid(WSBoolRecord.sid) as WSBoolRecord?

        record!!.setAlternateExpression(b)
    }

    /**
     * whether alternative formula entry is on
     * @param b  alternative formulas or not
     */
    fun setAlternativeFormula(b: Boolean) {
        val record = sheet.findFirstRecordBySid(WSBoolRecord.sid) as WSBoolRecord?

        record!!.setAlternateFormula(b)
    }

    val alternateExpression: Boolean
        /**
         * whether alternate expression evaluation is on
         * @return alternative expression evaluation or not
         */
        get() = (sheet.findFirstRecordBySid(WSBoolRecord.sid) as WSBoolRecord)
            .getAlternateExpression()

    val alternateFormula: Boolean
        /**
         * whether alternative formula entry is on
         * @return alternative formulas or not
         */
        get() = (sheet.findFirstRecordBySid(WSBoolRecord.sid) as WSBoolRecord).getAlternateFormula()

    var autobreaks: Boolean
        /**
         * show automatic page breaks or not
         * @return whether to show auto page breaks
         */
        get() = (sheet.findFirstRecordBySid(WSBoolRecord.sid) as WSBoolRecord).getAutobreaks()
        /**
         * show automatic page breaks or not
         * @param b  whether to show auto page breaks
         */
        set(b) {
            val record =
                sheet.findFirstRecordBySid(WSBoolRecord.sid) as WSBoolRecord?

            record!!.setAutobreaks(b)
        }

    var dialog: Boolean
        /**
         * get whether sheet is a dialog sheet or not
         * @return isDialog or not
         */
        get() = (sheet.findFirstRecordBySid(WSBoolRecord.sid) as WSBoolRecord).getDialog()
        /**
         * set whether sheet is a dialog sheet or not
         * @param b  isDialog or not
         */
        set(b) {
            val record =
                sheet.findFirstRecordBySid(WSBoolRecord.sid) as WSBoolRecord?

            record!!.setDialog(b)
        }

    var displayGuts: Boolean
        /**
         * get whether to display the guts or not
         * 
         * @return guts or no guts (or glory)
         */
        get() = (sheet.findFirstRecordBySid(WSBoolRecord.sid) as WSBoolRecord).getDisplayGuts()
        /**
         * set whether to display the guts or not
         * 
         * @param b  guts or no guts (or glory)
         */
        set(b) {
            val record =
                sheet.findFirstRecordBySid(WSBoolRecord.sid) as WSBoolRecord?

            record!!.setDisplayGuts(b)
        }

    var isDisplayZeros: Boolean
        /**
         * Gets the flag indicating whether the window should show 0 (zero) in cells containing zero value.
         * When false, cells with zero value appear blank instead of showing the number zero.
         * 
         * 
         * In Excel 2003 this option can be changed in the Options dialog on the View tab.
         * 
         * @return whether all zero values on the worksheet are displayed
         */
        get() = sheet.windowTwo!!.getDisplayZeros()
        /**
         * Set whether the window should show 0 (zero) in cells containing zero value.
         * When false, cells with zero value appear blank instead of showing the number zero.
         * 
         * 
         * In Excel 2003 this option can be set in the Options dialog on the View tab.
         * 
         * @param value whether to display or hide all zero values on the worksheet
         */
        set(value) {
            sheet.windowTwo!!.setDisplayZeros(value)
        }

    var fitToPage: Boolean
        /**
         * fit to page option is on
         * @return fit or not
         */
        get() = (sheet.findFirstRecordBySid(WSBoolRecord.sid) as WSBoolRecord).getFitToPage()
        /**
         * fit to page option is on
         * @param b  fit or not
         */
        set(b) {
            val record =
                sheet.findFirstRecordBySid(WSBoolRecord.sid) as WSBoolRecord?

            record!!.setFitToPage(b)
        }

    var rowSumsBelow: Boolean
        /**
         * get if row summaries appear below detail in the outline
         * @return below or not
         */
        get() = (sheet.findFirstRecordBySid(WSBoolRecord.sid) as WSBoolRecord).getRowSumsBelow()
        /**
         * set if row summaries appear below detail in the outline
         * @param b  below or not
         */
        set(b) {
            val record =
                sheet.findFirstRecordBySid(WSBoolRecord.sid) as WSBoolRecord?

            record!!.setRowSumsBelow(b)
            //setAlternateExpression must be set in conjuction with setRowSumsBelow
            record.setAlternateExpression(b)
        }

    var rowSumsRight: Boolean
        /**
         * get if col summaries appear right of the detail in the outline
         * @return right or not
         */
        get() = (sheet.findFirstRecordBySid(WSBoolRecord.sid) as WSBoolRecord).getRowSumsRight()
        /**
         * set if col summaries appear right of the detail in the outline
         * @param b  right or not
         */
        set(b) {
            val record =
                sheet.findFirstRecordBySid(WSBoolRecord.sid) as WSBoolRecord?

            record!!.setRowSumsRight(b)
        }

    var isPrintGridlines: Boolean
        /**
         * Returns whether gridlines are printed.
         * @return Gridlines are printed
         */
        get() = this.sheet.printGridlines!!.getPrintGridlines()
        /**
         * Turns on or off the printing of gridlines.
         * @param newPrintGridlines boolean to turn on or off the printing of
         * gridlines
         */
        set(newPrintGridlines) {
            this.sheet.printGridlines!!.setPrintGridlines(newPrintGridlines)
        }

    val printSetup: HSSFPrintSetup
        /**
         * Gets the print setup object.
         * @return The user model for the print setup object.
         */
        get() = HSSFPrintSetup(sheet.pageSettings!!.printSetup!!)

    val header: HSSFHeader
        get() = HSSFHeader(sheet.pageSettings)

    val footer: HSSFFooter
        get() = HSSFFooter(sheet.pageSettings)

    var isSelected: Boolean
        /**
         * Note - this is not the same as whether the sheet is focused (isActive)
         * @return `true` if this sheet is currently selected
         */
        get() = this.sheet.windowTwo!!.getSelected()
        /**
         * Sets whether sheet is selected.
         * @param sel Whether to select the sheet or deselect the sheet.
         */
        set(sel) {
            this.sheet.windowTwo!!.setSelected(sel)
        }

    var isActive: Boolean
        /**
         * @return `true` if this sheet is currently focused
         */
        get() = this.sheet.windowTwo!!.isActive()
        /**
         * Sets whether sheet is selected.
         * @param sel Whether to select the sheet or deselect the sheet.
         */
        set(sel) {
            this.sheet.windowTwo!!.setActive(sel)
        }

    /**
     * Gets the size of the margin in inches.
     * @param margin which margin to get
     * @return the size of the margin
     */
    fun getMargin(margin: Short): Double {
        return sheet.pageSettings!!.getMargin(margin)
    }

    /**
     * Sets the size of the margin in inches.
     * @param margin which margin to get
     * @param size the size of the margin
     */
    fun setMargin(margin: Short, size: Double) {
        sheet.pageSettings!!.setMargin(margin, size)
    }

    private val protectionBlock: WorksheetProtectionBlock
        get() = sheet.protectionBlock

    val protect: Boolean
        /**
         * Answer whether protection is enabled or disabled
         * @return true => protection enabled; false => protection disabled
         */
        get() = this.protectionBlock!!.isSheetProtected

    val password: Short
        /**
         * @return hashed password
         */
        get() = this.protectionBlock!!.passwordHash.toShort()

    val objectProtect: Boolean
        /**
         * Answer whether object protection is enabled or disabled
         * @return true => protection enabled; false => protection disabled
         */
        get() = this.protectionBlock!!.isObjectProtected

    val scenarioProtect: Boolean
        /**
         * Answer whether scenario protection is enabled or disabled
         * @return true => protection enabled; false => protection disabled
         */
        get() = this.protectionBlock!!.isScenarioProtected

    /**
     * Sets the protection enabled as well as the password
     * @param password to set for protection. Pass `null` to remove protection
     */
    fun protectSheet(password: String?) {
        this.protectionBlock!!.protectSheet(password, true, true) //protect objs&scenarios(normal)
    }

    /**
     * Sets the zoom magnification for the sheet.  The zoom is expressed as a
     * fraction.  For example to express a zoom of 75% use 3 for the numerator
     * and 4 for the denominator.
     * 
     * @param numerator     The numerator for the zoom magnification.
     * @param denominator   The denominator for the zoom magnification.
     */
    fun setZoom(numerator: Int, denominator: Int) {
        require(!(numerator < 1 || numerator > 65535)) { "Numerator must be greater than 1 and less than 65536" }
        require(!(denominator < 1 || denominator > 65535)) { "Denominator must be greater than 1 and less than 65536" }

        val sclRecord = SCLRecord()
        sclRecord.setNumerator(numerator.toShort())
        sclRecord.setDenominator(denominator.toShort())
        this.sheet.setSCLRecord(sclRecord)
    }

    val topRow: Short
        /**
         * The top row in the visible view when the sheet is
         * first viewed after opening it in a viewer
         * @return short indicating the rownum (0 based) of the top row
         */
        get() = sheet.topRow

    val leftCol: Short
        /**
         * The left col in the visible view when the sheet is
         * first viewed after opening it in a viewer
         * @return short indicating the rownum (0 based) of the top row
         */
        get() = sheet.leftCol

    /**
     * Sets desktop window pane display area, when the
     * file is first opened in a viewer.
     * @param toprow the top row to show in desktop window pane
     * @param leftcol the left column to show in desktop window pane
     */
    fun showInPane(toprow: Short, leftcol: Short) {
        sheet.topRow = toprow
        sheet.leftCol = leftcol
    }

    /**
     * Shifts the merged regions left or right depending on mode
     * 
     * 
     * TODO: MODE , this is only row specific
     * @param startRow
     * @param endRow
     * @param n
     * @param isRow
     */
    protected fun shiftMerged(startRow: Int, endRow: Int, n: Int, isRow: Boolean) {
        val shiftedRegions: MutableList<HSSFCellRangeAddress?> = ArrayList<HSSFCellRangeAddress?>()
        //move merged regions completely if they fall within the new region boundaries when they are shifted
        var i = 0
        while (i < this.numMergedRegions) {
            val merged = getMergedRegion(i)

            val inStart = (merged.firstRow >= startRow || merged.lastRow >= startRow)
            val inEnd = (merged.firstRow <= endRow || merged.lastRow <= endRow)

            //don't check if it's not within the shifted area
            if (!inStart || !inEnd) {
                i++
                continue
            }

            //only shift if the region outside the shifted rows is not merged too
            if (!SheetUtil.containsCell(merged, startRow - 1, 0)
                && !SheetUtil.containsCell(merged, endRow + 1, 0)
            ) {
                merged.firstRow = merged.firstRow + n
                merged.lastRow = merged.lastRow + n
                //have to remove/add it back
                shiftedRegions.add(merged)
                removeMergedRegion(i)
                i = i - 1 // we have to back up now since we removed one
            }
            i++
        }

        //read so it doesn't get shifted again
        val iterator: MutableIterator<HSSFCellRangeAddress?> = shiftedRegions.iterator()
        while (iterator.hasNext()) {
            val region = iterator.next()
            if (region != null) {
                this.addMergedRegion(region)
            }
        }
    }

    /**
     * Shifts rows between startRow and endRow n number of rows.
     * If you use a negative number, it will shift rows up.
     * Code ensures that rows don't wrap around
     * 
     * 
     * 
     * Additionally shifts merged regions that are completely defined in these
     * rows (ie. merged 2 cells on a row to be shifted).
     * 
     * 
     * TODO Might want to add bounds checking here
     * @param startRow the row to start shifting
     * @param endRow the row to end shifting
     * @param n the number of rows to shift
     * @param copyRowHeight whether to copy the row height during the shift
     * @param resetOriginalRowHeight whether to set the original row's height to the default
     * @param moveComments whether to move comments at the same time as the cells they are attached to
     */
    /**
     * Shifts rows between startRow and endRow n number of rows.
     * If you use a negative number, it will shift rows up.
     * Code ensures that rows don't wrap around
     * 
     * 
     * 
     * Additionally shifts merged regions that are completely defined in these
     * rows (ie. merged 2 cells on a row to be shifted).
     * 
     * 
     * TODO Might want to add bounds checking here
     * @param startRow the row to start shifting
     * @param endRow the row to end shifting
     * @param n the number of rows to shift
     * @param copyRowHeight whether to copy the row height during the shift
     * @param resetOriginalRowHeight whether to set the original row's height to the default
     */
    /**
     * Shifts rows between startRow and endRow n number of rows.
     * If you use a negative number, it will shift rows up.
     * Code ensures that rows don't wrap around.
     * 
     * Calls shiftRows(startRow, endRow, n, false, false);
     * 
     * 
     * 
     * Additionally shifts merged regions that are completely defined in these
     * rows (ie. merged 2 cells on a row to be shifted).
     * @param startRow the row to start shifting
     * @param endRow the row to end shifting
     * @param n the number of rows to shift
     */
    @JvmOverloads
    fun shiftRows(
        startRow: Int, endRow: Int, n: Int, copyRowHeight: Boolean = false,
        resetOriginalRowHeight: Boolean = false, moveComments: Boolean = true
    ) {
        val s: Int
        val inc: Int
        if (n < 0) {
            s = startRow
            inc = 1
        } else if (n > 0) {
            s = endRow
            inc = -1
        } else {
            // Nothing to do
            return
        }

        val noteRecs: Array<NoteRecord?>
        if (moveComments) {
            noteRecs = sheet.noteRecords
        } else {
            noteRecs = NoteRecord.EMPTY_ARRAY
        }

        shiftMerged(startRow, endRow, n, true)
        sheet.pageSettings!!.shiftRowBreaks(startRow, endRow, n)

        var rowNum = s
        while (rowNum >= startRow && rowNum <= endRow && rowNum >= 0 && rowNum < 65536) {
            val row = getRow(rowNum)
            // notify all cells in this row that we are going to shift them,
            // it can throw IllegalStateException if the operation is not allowed, for example,
            // if the row contains cells included in a multi-cell array formula
            if (row != null) notifyRowShifting(row)

            var row2Replace = getRow(rowNum + n)
            if (row2Replace == null) row2Replace = createRow(rowNum + n)

            // Remove all the old cells from the row we'll
            //  be writing too, before we start overwriting
            //  any cells. This avoids issues with cells
            //  changing type, and records not being correctly
            //  overwritten
            row2Replace.removeAllCells()

            // If this row doesn't exist, nothing needs to
            //  be done for the now empty destination row
            if (row == null) {
                rowNum += inc
                continue  // Nothing to do for this row
            }

            // Fix up row heights if required
            if (copyRowHeight) {
                row2Replace.height = row.getHeight()
            }
            if (resetOriginalRowHeight) {
                row.height = 0xff.toShort()
            }

            // Copy each cell from the source row to
            //  the destination row
            val cells = row.cellIterator()
            while (cells.hasNext()) {
                val cell = cells.next() as HSSFCell
                row.removeCell(cell)
                val cellRecord = cell.cellValueRecord
                cellRecord.row = rowNum + n
                row2Replace.createCellFromRecord(cellRecord)
                sheet.addValueRecord(rowNum + n, cellRecord)

                val link = cell.getHyperlink()
                if (link != null) {
                    link.firstRow = link.firstRow + n
                    link.lastRow = link.lastRow + n
                }
            }
            // Now zap all the cells in the source row
            row.removeAllCells()

            // Move comments from the source row to the
            //  destination row. Note that comments can
            //  exist for cells which are null
            if (moveComments) {
                // This code would get simpler if NoteRecords could be organised by HSSFRow.
                for (i in noteRecs.indices.reversed()) {
                    val nr = noteRecs[i]!!
                    if (nr.getRow() != rowNum) {
                        continue
                    }
                    val comment = getCellComment(rowNum, nr.getColumn())
                    if (comment != null) {
                        comment.setRow(rowNum + n)
                    }
                }
            }
            rowNum += inc
        }

        // Re-compute the first and last rows of the sheet as needed
        if (n > 0) {
            // Rows are moving down
            if (startRow == this.firstRowNum) {
                // Need to walk forward to find the first non-blank row
                this.firstRowNum = max(startRow + n, 0)
                for (i in startRow + 1..<startRow + n) {
                    if (getRow(i) != null) {
                        this.firstRowNum = i
                        break
                    }
                }
            }
            if (endRow + n > this.lastRowNum) {
                this.lastRowNum = min(endRow + n, SpreadsheetVersion.EXCEL97.lastRowIndex)
            }
        } else {
            // Rows are moving up
            if (startRow + n < this.firstRowNum) {
                this.firstRowNum = max(startRow + n, 0)
            }
            if (endRow == this.lastRowNum) {
                // Need to walk backward to find the last non-blank row
                this.lastRowNum = min(endRow + n, SpreadsheetVersion.EXCEL97.lastRowIndex)
                var i = endRow - 1
                while (i > endRow + n) {
                    if (getRow(i) != null) {
                        this.lastRowNum = i
                        break
                    }
                    i++
                }
            }
        }

        // Update any formulas on this sheet that point to
        //  rows which have been moved
        val sheetIndex = workbook.getSheetIndex(this)
        val externSheetIndex = _book.checkExternSheet(sheetIndex)
        val shifter = createForRowShift(
            externSheetIndex.toInt(), startRow,
            endRow, n
        )
        sheet.updateFormulasAfterCellShift(shifter, externSheetIndex.toInt())

        val nSheets = workbook.numberOfSheets
        for (i in 0..<nSheets) {
            val otherSheet = workbook.getSheetAt(i).sheet
            if (otherSheet == this.sheet) {
                continue
            }
            val otherExtSheetIx = _book.checkExternSheet(i)
            otherSheet.updateFormulasAfterCellShift(shifter, otherExtSheetIx.toInt())
        }
        workbook.workbook.updateNamesAfterCellShift(shifter)
    }

    fun insertChartRecords(records: MutableList<Record?>) {
        val window2Loc = sheet.findFirstRecordLocBySid(WindowTwoRecord.sid)
        sheet.records.addAll(window2Loc, records.filterNotNull())
    }

    private fun notifyRowShifting(row: HSSFRow) {
        val msg = ("Row[rownum=" + row.rowNum
                + "] contains cell(s) included in a multi-cell array formula. "
                + "You cannot change part of an array.")
        for (cell in row) {
            val hcell = cell as HSSFCell
            if (hcell.isPartOfArrayFormulaGroup()) {
                hcell.notifyArrayFormulaChanging(msg)
            }
        }
    }

    /**
     * Creates a split (freezepane). Any existing freezepane or split pane is overwritten.
     * 
     * 
     * 
     * If both colSplit and rowSplit are zero then the existing freeze pane is removed
     * 
     * 
     * @param colSplit      Horizonatal position of split.
     * @param rowSplit      Vertical position of split.
     * @param leftmostColumn   Left column visible in right pane.
     * @param topRow        Top row visible in bottom pane
     */
    /**
     * Creates a split (freezepane). Any existing freezepane or split pane is overwritten.
     * 
     * 
     * 
     * If both colSplit and rowSplit are zero then the existing freeze pane is removed
     * 
     * 
     * @param colSplit      Horizonatal position of split.
     * @param rowSplit      Vertical position of split.
     */
    @JvmOverloads
    fun createFreezePane(
        colSplit: Int,
        rowSplit: Int,
        leftmostColumn: Int = colSplit,
        topRow: Int = rowSplit
    ) {
        validateColumn(colSplit)
        validateRow(rowSplit)
        require(leftmostColumn >= colSplit) { "leftmostColumn parameter must not be less than colSplit parameter" }
        require(topRow >= rowSplit) { "topRow parameter must not be less than leftmostColumn parameter" }
        this.sheet.createFreezePane(colSplit, rowSplit, topRow, leftmostColumn)
    }

    /**
     * Creates a split pane. Any existing freezepane or split pane is overwritten.
     * @param xSplitPos      Horizonatal position of split (in 1/20th of a point).
     * @param ySplitPos      Vertical position of split (in 1/20th of a point).
     * @param topRow        Top row visible in bottom pane
     * @param leftmostColumn   Left column visible in right pane.
     * @param activePane    Active pane.  One of: PANE_LOWER_RIGHT,
     * PANE_UPPER_RIGHT, PANE_LOWER_LEFT, PANE_UPPER_LEFT
     * @see .PANE_LOWER_LEFT
     * 
     * @see .PANE_LOWER_RIGHT
     * 
     * @see .PANE_UPPER_LEFT
     * 
     * @see .PANE_UPPER_RIGHT
     */
    fun createSplitPane(
        xSplitPos: Int, ySplitPos: Int, leftmostColumn: Int, topRow: Int,
        activePane: Int
    ) {
        this.sheet.createSplitPane(xSplitPos, ySplitPos, topRow, leftmostColumn, activePane)
    }

    /**
     * Returns the information regarding the currently configured pane (split or freeze).
     * @return null if no pane configured, or the pane information.
     */
    val paneInformation: HSSFPaneInformation?
        get() {
            if (_paneInformation == null) {
                _paneInformation = this.sheet.paneInformation
            }
            return _paneInformation
        }

    var isDisplayGridlines: Boolean
        /**
         * Returns if gridlines are displayed.
         * @return whether gridlines are displayed
         */
        get() = sheet.isDisplayGridlines
        /**
         * Sets whether the gridlines are shown in a viewer.
         * @param show whether to show gridlines or not
         */
        set(show) {
            sheet.isDisplayGridlines = show
        }

    var isDisplayFormulas: Boolean
        /**
         * Returns if formulas are displayed.
         * @return whether formulas are displayed
         */
        get() = sheet.isDisplayFormulas
        /**
         * Sets whether the formulas are shown in a viewer.
         * @param show whether to show formulas or not
         */
        set(show) {
            sheet.isDisplayFormulas = show
        }

    var isDisplayRowColHeadings: Boolean
        /**
         * Returns if RowColHeadings are displayed.
         * @return whether RowColHeadings are displayed
         */
        get() = sheet.isDisplayRowColHeadings
        /**
         * Sets whether the RowColHeadings are shown in a viewer.
         * @param show whether to show RowColHeadings or not
         */
        set(show) {
            sheet.isDisplayRowColHeadings = show
        }

    /**
     * Sets a page break at the indicated row
     * Breaks occur above the specified row and left of the specified column inclusive.
     * 
     * For example, `sheet.setColumnBreak(2);` breaks the sheet into two parts
     * with columns A,B,C in the first and D,E,... in the second. Simuilar, `sheet.setRowBreak(2);`
     * breaks the sheet into two parts with first three rows (rownum=1...3) in the first part
     * and rows starting with rownum=4 in the second.
     * 
     * @param row the row to break, inclusive
     */
    fun setRowBreak(row: Int) {
        validateRow(row)
        sheet.pageSettings!!.setRowBreak(row, 0.toShort(), 255.toShort())
    }

    /**
     * @return `true` if there is a page break at the indicated row
     */
    fun isRowBroken(row: Int): Boolean {
        return sheet.pageSettings!!.isRowBroken(row)
    }

    /**
     * Removes the page break at the indicated row
     */
    fun removeRowBreak(row: Int) {
        sheet.pageSettings!!.removeRowBreak(row)
    }

    val rowBreaks: IntArray?
        /**
         * @return row indexes of all the horizontal page breaks, never `null`
         */
        get() =//we can probably cache this information, but this should be a sparsely used function
            sheet.pageSettings!!.rowBreaks

    val columnBreaks: IntArray?
        /**
         * @return column indexes of all the vertical page breaks, never `null`
         */
        get() =//we can probably cache this information, but this should be a sparsely used function
            sheet.pageSettings!!.columnBreaks

    /**
     * Sets a page break at the indicated column.
     * Breaks occur above the specified row and left of the specified column inclusive.
     * 
     * For example, `sheet.setColumnBreak(2);` breaks the sheet into two parts
     * with columns A,B,C in the first and D,E,... in the second. Simuilar, `sheet.setRowBreak(2);`
     * breaks the sheet into two parts with first three rows (rownum=1...3) in the first part
     * and rows starting with rownum=4 in the second.
     * 
     * @param column the column to break, inclusive
     */
    fun setColumnBreak(column: Int) {
        validateColumn(column.toShort().toInt())
        sheet.pageSettings!!.setColumnBreak(
            column.toShort(), 0.toShort(),
            SpreadsheetVersion.EXCEL97.lastRowIndex.toShort()
        )
    }

    /**
     * Determines if there is a page break at the indicated column
     * @param column FIXME: Document this!
     * @return FIXME: Document this!
     */
    fun isColumnBroken(column: Int): Boolean {
        return sheet.pageSettings!!.isColumnBroken(column)
    }

    /**
     * Removes a page break at the indicated column
     * @param column
     */
    fun removeColumnBreak(column: Int) {
        sheet.pageSettings!!.removeColumnBreak(column)
    }

    /**
     * Runs a bounds check for row numbers
     * @param row
     */
    protected fun validateRow(row: Int) {
        val maxrow = SpreadsheetVersion.EXCEL97.lastRowIndex
        require(row <= maxrow) { "Maximum row number is " + maxrow }
        require(row >= 0) { "Minumum row number is 0" }
    }

    /**
     * Runs a bounds check for column numbers
     * @param column
     */
    protected fun validateColumn(column: Int) {
        val maxcol = SpreadsheetVersion.EXCEL97.lastColumnIndex
        require(column <= maxcol) { "Maximum column number is " + maxcol }
        require(column >= 0) { "Minimum column number is 0" }
    }

    /**
     * Aggregates the drawing records and dumps the escher record hierarchy
     * to the standard output.
     */
    fun dumpDrawingRecords(fat: Boolean) {
        sheet.aggregateDrawingRecords(_book.drawingManager!!, false)

        val r = this.sheet.findFirstRecordBySid(EscherAggregate.sid) as EscherAggregate?
        val escherRecords = r!!.escherRecords
        val w = PrintWriter(System.out)
        val iterator: MutableIterator<EscherRecord> = escherRecords.iterator()
        while (iterator.hasNext()) {
            val escherRecord = iterator.next()
            if (fat) {
                println(escherRecord.toString())
            } else {
                escherRecord.display(w, 0)
            }
        }
        w.flush()
    }

    /**
     * Creates the top-level drawing patriarch.  This will have
     * the effect of removing any existing drawings on this
     * sheet.
     * This may then be used to add graphics or charts
     * @return  The new patriarch.
     */
    fun createDrawingPatriarch(): HSSFPatriarch {
        if (_patriarch == null) {
            // Create the drawing group if it doesn't already exist.
            workbook.initDrawings()

            if (_patriarch == null) {
                sheet.aggregateDrawingRecords(_book.drawingManager!!, true)
                val agg = sheet
                    .findFirstRecordBySid(EscherAggregate.sid) as EscherAggregate?
                //_patriarch = new HSSFPatriarch(this, agg);
                agg!!.setPatriarch(_patriarch)
            }
        }
        return _patriarch!!
    }

    val drawingEscherAggregate: EscherAggregate?
        /**
         * Returns the agregate escher records for this sheet,
         * it there is one.
         * WARNING - calling this will trigger a parsing of the
         * associated escher records. Any that aren't supported
         * (such as charts and complex drawing types) will almost
         * certainly be lost or corrupted when written out.
         */
        get() {
            _book.findDrawingGroup()

            // If there's now no drawing manager, then there's
            //  no drawing escher records on the workbook
            val drawingManager = _book.drawingManager
            if (drawingManager == null) {
                return null
            }

            val found = sheet.aggregateDrawingRecords(drawingManager, false)
            if (found == -1) {
                // Workbook has drawing stuff, but this sheet doesn't
                return null
            }

            // Grab our aggregate record, and wire it up
            val agg =
                sheet.findFirstRecordBySid(EscherAggregate.sid) as EscherAggregate?
            return agg
        }

    val drawingPatriarch: HSSFPatriarch?
        /**
         * Returns the top-level drawing patriach, if there is
         * one.
         * This will hold any graphics or charts for the sheet.
         * WARNING - calling this will trigger a parsing of the
         * associated escher records. Any that aren't supported
         * (such as charts and complex drawing types) will almost
         * certainly be lost or corrupted when written out. Only
         * use this with simple drawings, otherwise call
         * [createDrawingPatriarch] and
         * start from scratch!
         */
        get() {
            if (_patriarch != null) return _patriarch

            val agg = this.drawingEscherAggregate
            if (agg == null) return null

            //_patriarch = new HSSFPatriarch(this, agg);
            agg.setPatriarch(_patriarch)

            // Have it process the records into high level objects
            //  as best it can do (this step may eat anything
            //  that isn't supported, you were warned...)
            agg.convertRecordsToUserModel(null)

            // Return what we could cope with
            return _patriarch
        }

    @Deprecated("(Sep 2008) use {@link #setColumnGroupCollapsed(int, boolean)}")
    fun setColumnGroupCollapsed(columnNumber: Short, collapsed: Boolean) {
        setColumnGroupCollapsed(columnNumber.toInt() and 0xFFFF, collapsed)
    }

    @Deprecated("(Sep 2008) use {@link #groupColumn(int, int)}")
    fun groupColumn(fromColumn: Short, toColumn: Short) {
        groupColumn(fromColumn.toInt() and 0xFFFF, toColumn.toInt() and 0xFFFF)
    }

    @Deprecated("(Sep 2008) use {@link #ungroupColumn(int, int)}")
    fun ungroupColumn(fromColumn: Short, toColumn: Short) {
        ungroupColumn(fromColumn.toInt() and 0xFFFF, toColumn.toInt() and 0xFFFF)
    }

    /**
     * Expands or collapses a column group.
     * 
     * @param columnNumber      One of the columns in the group.
     * @param collapsed         true = collapse group, false = expand group.
     */
    fun setColumnGroupCollapsed(columnNumber: Int, collapsed: Boolean) {
        sheet.setColumnGroupCollapsed(columnNumber, collapsed)
    }

    /**
     * Create an outline for the provided column range.
     * 
     * @param fromColumn        beginning of the column range.
     * @param toColumn          end of the column range.
     */
    fun groupColumn(fromColumn: Int, toColumn: Int) {
        sheet.groupColumnRange(fromColumn, toColumn, true)
    }

    fun ungroupColumn(fromColumn: Int, toColumn: Int) {
        sheet.groupColumnRange(fromColumn, toColumn, false)
    }

    /**
     * Tie a range of cell together so that they can be collapsed or expanded
     * 
     * @param fromRow   start row (0-based)
     * @param toRow     end row (0-based)
     */
    fun groupRow(fromRow: Int, toRow: Int) {
        sheet.groupRowRange(fromRow, toRow, true)
    }

    fun ungroupRow(fromRow: Int, toRow: Int) {
        sheet.groupRowRange(fromRow, toRow, false)
    }

    fun setRowGroupCollapsed(rowIndex: Int, collapse: Boolean) {
        if (collapse) {
            sheet.rowsAggregate!!.collapseRow(rowIndex)
        } else {
            sheet.rowsAggregate!!.expandRow(rowIndex)
        }
    }

    /**
     * Sets the default column style for a given column.  POI will only apply this style to new cells added to the sheet.
     * 
     * @param column the column index
     * @param style the style to set
     */
    fun setDefaultColumnStyle(column: Int, style: ICellStyle) {
        sheet.setDefaultColumnStyle(column, (style as HSSFCellStyle).index.toInt())
    }

    /**
     * Adjusts the column width to fit the contents.
     * 
     * This process can be relatively slow on large sheets, so this should
     * normally only be called once per column, at the end of your
     * processing.
     * 
     * You can specify whether the content of merged cells should be considered or ignored.
     * Default is to ignore merged cells.
     * 
     * @param column the column index
     * @param useMergedCells whether to use the contents of merged cells when calculating the width of the column
     */
    /**
     * Adjusts the column width to fit the contents.
     * 
     * This process can be relatively slow on large sheets, so this should
     * normally only be called once per column, at the end of your
     * processing.
     * 
     * @param column the column index
     */
    @JvmOverloads
    fun autoSizeColumn(column: Int, useMergedCells: Boolean = false) {
        var width = SheetUtil.getColumnWidth(this, column, useMergedCells)

        if (width != -1.0) {
            width *= 256.0
            val maxColumnWidth =
                255 * 256 // The maximum column width for an individual cell is 255 characters
            if (width > maxColumnWidth) {
                width = maxColumnWidth.toDouble()
            }
            setColumnWidth(column, (width).toInt())
        }
    }

    /**
     * Returns cell comment for the specified row and column
     * 
     * @return cell comment or `null` if not found
     */
    fun getCellComment(row: Int, column: Int): HSSFComment? {
        // Don't call findCellComment directly, otherwise
        //  two calls to this method will result in two
        //  new HSSFComment instances, which is bad
        val r = getRow(row)
        if (r != null) {
            val c = r.getCell(column)
            if (c != null) {
                return c.getCellComment()
            }
            // No cell, so you will get new
            //  objects every time, sorry...
            return HSSFCell.findCellComment(this.sheet, row, column)
        }
        return null
    }

    val sheetConditionalFormatting: HSSFSheetConditionalFormatting
        get() = HSSFSheetConditionalFormatting(this)

    val sheetName: String
        /**
         * Returns the name of this sheet
         * 
         * @return the name of this sheet
         */
        get() {
            val wb = this.workbook
            val idx = wb.getSheetIndex(this)
            return wb.getSheetName(idx)
        }

    /**
     * Also creates cells if they don't exist
     */
    private fun getCellRange(range: HSSFCellRangeAddress): CellRange<HSSFCell> {
        val firstRow = range.firstRow
        val firstColumn = range.firstColumn
        val lastRow = range.lastRow
        val lastColumn = range.lastColumn
        val height = lastRow - firstRow + 1
        val width = lastColumn - firstColumn + 1
        val temp: MutableList<HSSFCell> = ArrayList<HSSFCell>(height * width)
        for (rowIn in firstRow..lastRow) {
            for (colIn in firstColumn..lastColumn) {
                var row = getRow(rowIn)
                if (row == null) {
                    row = createRow(rowIn)
                }
                var cell = row.getCell(colIn)
                if (cell == null) {
                    cell = row.createCell(colIn)
                }
                temp.add(cell)
            }
        }
        return SSCellRange.create<HSSFCell>(
            firstRow,
            firstColumn,
            height,
            width,
            temp,
            HSSFCell::class.java
        )
    }

    fun setArrayFormula(formula: String?, range: HSSFCellRangeAddress?): CellRange<HSSFCell?>? {
//        // make sure the formula parses OK first
//        int sheetIndex = _workbook.getSheetIndex(this);
//        Ptg[] ptgs = HSSFFormulaParser.parse(formula, _workbook, FormulaType.ARRAY, sheetIndex);
//        CellRange<HSSFCell> cells = getCellRange(range);
//
//        for (HSSFCell c : cells)
//        {
//            c.setCellArrayFormula(range);
//        }
//        HSSFCell mainArrayFormulaCell = cells.getTopLeftCell();
//        FormulaRecordAggregate agg = (FormulaRecordAggregate)mainArrayFormulaCell
//            .getCellValueRecord();
//        agg.setArrayFormula(range, ptgs);
//        return cells;
        return null
    }

    fun removeArrayFormula(cell: ICell): CellRange<HSSFCell> {
        require(cell.getSheet() === this) { "Specified cell does not belong to this sheet." }
        val rec = (cell as HSSFCell).cellValueRecord
        if (rec !is FormulaRecordAggregate) {
            val ref = CellReference(cell).formatAsString()
            throw IllegalArgumentException("Cell " + ref + " is not part of an array formula.")
        }
        val fra = rec
        val range = fra.removeArrayFormula(cell.rowIndex, cell.columnIndex)

        val result = getCellRange(range)
        // clear all cells in the range
        for (c in result) {
            c.setCellType(ICell.CELL_TYPE_BLANK)
        }
        return result
    }

    val dataValidationHelper: DataValidationHelper
        get() = HSSFDataValidationHelper(this)

    fun setAutoFilter(range: HSSFCellRangeAddress): HSSFAutoFilter {
        val hssfWorkbook = this.workbook
        val internalWorkbook = hssfWorkbook.workbook
        val sheetIndex = hssfWorkbook.getSheetIndex(this)

        var name = internalWorkbook.getSpecificBuiltinRecord(
            NameRecord.BUILTIN_FILTER_DB,
            sheetIndex + 1
        )

        if (name == null) {
            name = internalWorkbook.createBuiltInName(NameRecord.BUILTIN_FILTER_DB, sheetIndex + 1)
        }

        // The built-in name must consist of a single Area3d Ptg.
        val ptg = Area3DPtg(
            range.firstRow, range.lastRow,
            range.firstColumn, range.lastColumn, false, false, false, false, sheetIndex
        )
        name.setNameDefinition(arrayOf<Ptg?>(ptg))

        val r = AutoFilterInfoRecord()
        // the number of columns that have AutoFilter enabled.
        val numcols = 1 + range.lastColumn - range.firstColumn
        r.numEntries = numcols.toShort()
        val idx = sheet.findFirstRecordLocBySid(DimensionsRecord.sid)
        sheet.records.add(idx, r)

        //create a combobox control for each column
        val p = createDrawingPatriarch()
        for (col in range.firstColumn..range.lastColumn) {
            p.createComboBox(
                HSSFClientAnchor(
                    0, 0, 0, 0, col.toShort(), range.firstRow,
                    (col + 1).toShort(), range.firstRow + 1
                )
            )
        }

        return HSSFAutoFilter(this)
    }

    fun setScroll(scrollX: Int, scrollY: Int) {
        this.scrollX = scrollX
        this.scrollY = scrollY
    }

    /**
     * set active cell row and column index
     * @param row
     * @param column
     */
    fun setActiveCell(row: Int, column: Int) {
        this.activeCellRow = row
        this.activeCellColumn = column
    }

    val activeCell: HSSFCell?
        /**
         * get active cell
         * @return
         */
        get() {
            if (getRow(this.activeCellRow) != null) {
                return getRow(this.activeCellRow)!!.getCell(this.activeCellColumn)
            }
            return null
        }

    companion object {
        private val log = getLogger(HSSFSheet::class.java)
        private val DEBUG = POILogger.DEBUG

        /**
         * Used for compile-time optimization.  This is the initial size for the collection of
         * rows.  It is currently set to 20.  If you generate larger sheets you may benefit
         * by setting this to a higher number and recompiling a custom edition of HSSFSheet.
         */
        const val INITIAL_CAPACITY: Int = 20
    }
}
