/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
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

import com.wxiwei.office.fc.hssf.record.CellValueRecordInterface
import com.wxiwei.office.fc.hssf.record.RowRecord
import com.wxiwei.office.fc.ss.SpreadsheetVersion
import com.wxiwei.office.fc.ss.usermodel.ICell
import com.wxiwei.office.fc.ss.usermodel.ICellStyle
import com.wxiwei.office.fc.ss.usermodel.IRow
import com.wxiwei.office.fc.ss.usermodel.IRow.MissingCellPolicy

/**
 * High level representation of a row of a spreadsheet.
 * 
 * Only rows that have cells should be added to a Sheet.
 * 
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author Glen Stampoultzis (glens at apache.org)
 */
class HSSFRow internal constructor(
    /**
     * reference to containing low level Workbook
     */
    private val book: HSSFWorkbook,
    /**
     * reference to containing Sheet
     */
    private val sheet: HSSFSheet, record: RowRecord
) : IRow {
    private var rowNum = 0
    private var cells: Array<HSSFCell?>

    /**
     * get the lowlevel RowRecord represented by this object - should only be called
     * by other parts of the high level API
     * 
     * @return RowRecord this row represents
     */
    /**
     * reference to low level representation
     */
    val rowRecord: RowRecord

    /**
     * @return Returns the rowPixelHeight.
     */
    /**
     * @param rowPixelHeight The rowPixelHeight to set.
     */
    // 像素值的行高，默认18个像素
    var rowPixelHeight: Int = 18

    /**
     * Creates new HSSFRow from scratch. Only HSSFSheet should do this.
     * 
     * @param book low-level Workbook object containing the sheet that contains this row
     * @param sheet low-level Sheet object that contains this Row
     * @param rowNum the row number of this row (0 based)
     * @see HSSFSheet.createRow
     */
    internal constructor(book: HSSFWorkbook, sheet: HSSFSheet, rowNum: Int) : this(
        book,
        sheet,
        RowRecord(rowNum)
    )

    /**
     * Creates an HSSFRow from a low level RowRecord object.  Only HSSFSheet should do
     * this.  HSSFSheet uses this when an existing file is read in.
     * 
     * @param book low-level Workbook object containing the sheet that contains this row
     * @param sheet low-level Sheet object that contains this Row
     * @param record the low level api object this row should represent
     * @see HSSFSheet.createRow
     */
    init {
        this.rowRecord = record
        setRowNum(record.getRowNumber())


        // Size the initial cell list such that a read only case won't waste
        //  lots of memory, and a create/read followed by adding new cells can
        //  add a bit without needing a resize
        cells = arrayOfNulls<HSSFCell>(record.getLastCol() + INITIAL_CAPACITY)


        // Don't trust colIx boundaries as read by other apps
        // set the RowRecord empty for the moment
        record.setEmpty()
        // subsequent calls to createCellFromRecord() will update the colIx boundaries properly
    }

    @Deprecated("(Aug 2008) use {@link HSSFRow#createCell(int) }")
    fun createCell(columnIndex: Short): HSSFCell {
        return createCell(columnIndex.toInt())
    }

    @Deprecated("(Aug 2008) use {@link HSSFRow#createCell(int, int) }")
    fun createCell(columnIndex: Short, type: Int): HSSFCell {
        return createCell(columnIndex.toInt(), type)
    }

    /**
     * Use this to create new cells within the row and return it.
     * 
     * 
     * The cell that is returned is a CELL_TYPE_BLANK. The type can be changed
     * either through calling `setCellValue` or `setCellType`.
     * 
     * @param column - the column number this cell represents
     * 
     * @return HSSFCell a high level representation of the created cell.
     * @throws IllegalArgumentException if columnIndex < 0 or greater than 255,
     * the maximum number of columns supported by the Excel binary format (.xls)
     */
    override fun createCell(column: Int): HSSFCell {
        return this.createCell(column, ICell.CELL_TYPE_BLANK)
    }

    /**
     * Use this to create new cells within the row and return it.
     * 
     * 
     * The cell that is returned is a CELL_TYPE_BLANK. The type can be changed
     * either through calling setCellValue or setCellType.
     * 
     * @param columnIndex - the column number this cell represents
     * 
     * @return HSSFCell a high level representation of the created cell.
     * @throws IllegalArgumentException if columnIndex < 0 or greater than 255,
     * the maximum number of columns supported by the Excel binary format (.xls)
     */
    override fun createCell(columnIndex: Int, type: Int): HSSFCell {
        var shortCellNum = columnIndex.toShort()
        if (columnIndex > 0x7FFF) {
            shortCellNum = (0xffff - columnIndex).toShort()
        }

        val cell = HSSFCell(book, sheet, getRowNum(), shortCellNum, type)
        addCell(cell)
        sheet.sheet.addValueRecord(getRowNum(), cell.cellValueRecord)
        return cell
    }

    /**
     * remove the HSSFCell from this row.
     * @param cell to remove
     */
    override fun removeCell(cell: ICell) {
        requireNotNull(cell) { "cell must not be null" }
        removeCell(cell as HSSFCell, true)
    }

    private fun removeCell(cell: HSSFCell, alsoRemoveRecords: Boolean) {
        val column = cell.getColumnIndex()
        if (column < 0) {
            throw RuntimeException("Negative cell indexes not allowed")
        }
        if (column >= cells.size || cell !== cells[column]) {
            throw RuntimeException("Specified cell is not from this row")
        }
        if (cell.isPartOfArrayFormulaGroup()) {
            cell.notifyArrayFormulaChanging()
        }

        cells[column] = null

        if (alsoRemoveRecords) {
            val cval = cell.cellValueRecord
            sheet.sheet.removeValueRecord(getRowNum(), cval)
        }
        if (cell.getColumnIndex() + 1 == rowRecord.getLastCol()) {
            rowRecord.setLastCol(calculateNewLastCellPlusOne(rowRecord.getLastCol()))
        }
        if (cell.getColumnIndex() == rowRecord.getFirstCol()) {
            rowRecord.setFirstCol(calculateNewFirstCell(rowRecord.getFirstCol()))
        }
    }

    /**
     * Removes all the cells from the row, and their
     * records too.
     */
    fun removeAllCells() {
        for (i in cells.indices) {
            if (cells[i] != null) {
                removeCell(cells[i]!!, true)
            }
        }
        cells = arrayOfNulls<HSSFCell>(INITIAL_CAPACITY)
    }

    /**
     * create a high level HSSFCell object from an existing low level record.  Should
     * only be called from HSSFSheet or HSSFRow itself.
     * @param cell low level cell to create the high level representation from
     * @return HSSFCell representing the low level record passed in
     */
    fun createCellFromRecord(cell: CellValueRecordInterface): HSSFCell {
        val hcell = HSSFCell(book, sheet, cell)

        addCell(hcell)
        val colIx: Int = cell.column.toInt()
        if (rowRecord.isEmpty()) {
            rowRecord.setFirstCol(colIx)
            rowRecord.setLastCol(colIx + 1)
        } else {
            if (colIx < rowRecord.getFirstCol()) {
                rowRecord.setFirstCol(colIx)
            } else if (colIx > rowRecord.getLastCol()) {
                rowRecord.setLastCol(colIx + 1)
            } else {
                // added cell is within first and last cells
            }
        }
        // TODO - RowRecord column boundaries need to be updated for cell comments too
        return hcell
    }

    /**
     * set the row number of this row.
     * @param rowIndex  the row number (0-based)
     * @throws IndexOutOfBoundsException if the row number is not within the range 0-65535.
     */
    override fun setRowNum(rowIndex: Int) {
        val maxrow = SpreadsheetVersion.EXCEL97.getLastRowIndex()
        require(!((rowIndex < 0) || (rowIndex > maxrow))) {
            ("Invalid row number (" + rowIndex
                    + ") outside allowable range (0.." + maxrow + ")")
        }
        rowNum = rowIndex
        if (this.rowRecord != null) {
            rowRecord.setRowNumber(rowIndex) // used only for KEY comparison (HSSFRow)
        }
    }

    /**
     * get row number this row represents
     * @return the row number (0 based)
     */
    override fun getRowNum(): Int {
        return rowNum
    }

    /**
     * Returns the HSSFSheet this row belongs to
     * 
     * @return the HSSFSheet that owns this row
     */
    override fun getSheet(): HSSFSheet {
        return sheet
    }

    protected val outlineLevel: Int
        /**
         * Returns the rows outline level. Increased as you
         * put it into more groups (outlines), reduced as
         * you take it out of them.
         * TODO - Should this really be public?
         */
        get() = rowRecord.getOutlineLevel().toInt()

    /**
     * Moves the supplied cell to a new column, which
     * must not already have a cell there!
     * @param cell The cell to move
     * @param newColumn The new column number (0 based)
     */
    fun moveCell(cell: HSSFCell, newColumn: Short) {
        // Ensure the destination is free
        require(!(cells.size > newColumn && cells[newColumn.toInt()] != null)) { "Asked to move cell to column " + newColumn + " but there's already a cell there" }

        // Check it's one of ours
        require(cells[cell.getColumnIndex()] == cell) { "Asked to move a cell, but it didn't belong to our row" }

        // Move the cell to the new position
        // (Don't remove the records though)
        removeCell(cell, false)
        cell.updateCellNum(newColumn)
        addCell(cell)
    }

    /**
     * used internally to add a cell.
     */
    private fun addCell(cell: HSSFCell) {
        val column = cell.getColumnIndex()
        // re-allocate cells array as required.
        if (column >= cells.size) {
            val oldCells = cells
            // New size based on the same logic as ArrayList
            var newSize = oldCells.size * 3 / 2 + 1
            if (newSize < column + 1) {
                newSize = column + INITIAL_CAPACITY
            }
            cells = arrayOfNulls<HSSFCell>(newSize)
            System.arraycopy(oldCells, 0, cells, 0, oldCells.size)
        }
        cells[column] = cell

        // fix up firstCol and lastCol indexes
        if (rowRecord.isEmpty() || column < rowRecord.getFirstCol()) {
            rowRecord.setFirstCol(column)
        }

        if (rowRecord.isEmpty() || column >= rowRecord.getLastCol()) {
            rowRecord.setLastCol(column + 1) // +1 -> for one past the last index
        }
    }

    /**
     * Get the hssfcell representing a given column (logical cell)
     * 0-based. If you ask for a cell that is not defined, then
     * you get a null.
     * This is the basic call, with no policies applied
     * 
     * @param cellIndex  0 based column number
     * @return HSSFCell representing that column or null if undefined.
     */
    private fun retrieveCell(cellIndex: Int): HSSFCell? {
        if (cellIndex < 0 || cellIndex >= cells.size) {
            return null
        }
        return cells[cellIndex]
    }

    @Deprecated("(Aug 2008) use {@link #getCell(int)}")
    fun getCell(cellnum: Short): HSSFCell? {
        val ushortCellNum = cellnum.toInt() and 0x0000FFFF // avoid sign extension
        return getCell(ushortCellNum)
    }

    /**
     * Get the hssfcell representing a given column (logical cell)
     * 0-based.  If you ask for a cell that is not defined then
     * you get a null, unless you have set a different
     * [com.com.wxiwei.office.fc.ss.usermodel.IRow.MissingCellPolicy] on the base workbook.
     * 
     * @param cellnum  0 based column number
     * @return HSSFCell representing that column or null if undefined.
     */
    override fun getCell(cellnum: Int): HSSFCell? {
        return getCell(cellnum, book.missingCellPolicy!!)
    }

    /**
     * Get the hssfcell representing a given column (logical cell)
     * 0-based.  If you ask for a cell that is not defined, then
     * your supplied policy says what to do
     * 
     * @param cellnum  0 based column number
     * @param policy Policy on blank / missing cells
     * @return representing that column or null if undefined + policy allows.
     */
    override fun getCell(cellnum: Int, policy: MissingCellPolicy): HSSFCell? {
        val cell = retrieveCell(cellnum)
        if (policy == IRow.RETURN_NULL_AND_BLANK) {
            return cell
        }
        if (policy == IRow.RETURN_BLANK_AS_NULL) {
            if (cell == null) return cell
            if (cell.getCellType() == ICell.CELL_TYPE_BLANK) {
                return null
            }
            return cell
        }
        if (policy == IRow.CREATE_NULL_AS_BLANK) {
            if (cell == null) {
                return createCell(cellnum, ICell.CELL_TYPE_BLANK)
            }
            return cell
        }
        throw IllegalArgumentException("Illegal policy " + policy + " (" + policy.id + ")")
    }

    /**
     * get the number of the first cell contained in this row.
     * @return short representing the first logical cell in the row, or -1 if the row does not contain any cells.
     */
    override fun getFirstCellNum(): Short {
        if (rowRecord.isEmpty()) {
            return -1
        }
        return rowRecord.getFirstCol().toShort()
    }

    /**
     * Gets the index of the last cell contained in this row **PLUS ONE**. The result also
     * happens to be the 1-based column number of the last cell.  This value can be used as a
     * standard upper bound when iterating over cells:
     * <pre>
     * short minColIx = row.getFirstCellNum();
     * short maxColIx = row.getLastCellNum();
     * for(short colIx=minColIx; colIx&lt;maxColIx; colIx++) {
     * HSSFCell cell = row.getCell(colIx);
     * if(cell == null) {
     * continue;
     * }
     * //... do something with cell
     * }
    </pre> * 
     * 
     * @return short representing the last logical cell in the row **PLUS ONE**, or -1 if the
     * row does not contain any cells.
     */
    override fun getLastCellNum(): Short {
        if (rowRecord.isEmpty()) {
            return -1
        }
        return rowRecord.getLastCol().toShort()
    }


    /**
     * gets the number of defined cells (NOT number of cells in the actual row!).
     * That is to say if only columns 0,4,5 have values then there would be 3.
     * @return int representing the number of defined cells in the row.
     */
    override fun getPhysicalNumberOfCells(): Int {
        var count = 0
        for (i in cells.indices) {
            if (cells[i] != null) count++
        }
        return count
    }

    /**
     * set the row's height or set to ff (-1) for undefined/default-height.  Set the height in "twips" or
     * 1/20th of a point.
     * @param height  rowheight or -1 for undefined (use sheet default)
     */
    override fun setHeight(height: Short) {
        if (height.toInt() == -1) {
            rowRecord.setHeight((0xFF or 0x8000).toShort())
        } else {
            rowRecord.setBadFontHeight(true)
            rowRecord.setHeight(height)
        }
    }

    /**
     * set whether or not to display this row with 0 height
     * @param zHeight  height is zero or not.
     */
    override fun setZeroHeight(zHeight: Boolean) {
        rowRecord.setZeroHeight(zHeight)
    }

    /**
     * get whether or not to display this row with 0 height
     * @return - zHeight height is zero or not.
     */
    override fun getZeroHeight(): Boolean {
        return rowRecord.getZeroHeight()
    }

    /**
     * set the row's height in points.
     * @param height  row height in points, `-1` means to use the default height
     */
    override fun setHeightInPoints(height: Float) {
        if (height == -1f) {
            rowRecord.setHeight((0xFF or 0x8000).toShort())
        } else {
            rowRecord.setBadFontHeight(true)
            rowRecord.setHeight((height * 20).toInt().toShort())
        }
    }

    /**
     * get the row's height or ff (-1) for undefined/default-height in twips (1/20th of a point)
     * @return rowheight or 0xff for undefined (use sheet default)
     */
    override fun getHeight(): Short {
        var height: Short = rowRecord.getHeight()

        //The low-order 15 bits contain the row height.
        //The 0x8000 bit indicates that the row is standard height (optional)
        if ((height.toInt() and 0x8000) != 0) height = sheet.sheet.defaultRowHeight
        else height = (height.toInt() and 0x7FFF).toShort()

        return height
    }

    /**
     * get the row's height or ff (-1) for undefined/default-height in points (20*getHeight())
     * @return rowheight or 0xff for undefined (use sheet default)
     */
    override fun getHeightInPoints(): Float {
        return (getHeight().toFloat() / 20)
    }

    /**
     * used internally to refresh the "last cell plus one" when the last cell is removed.
     * @return 0 when row contains no cells
     */
    private fun calculateNewLastCellPlusOne(lastcell: Int): Int {
        var cellIx = lastcell - 1
        var r = retrieveCell(cellIx)

        while (r == null) {
            if (cellIx < 0) {
                return 0
            }
            r = retrieveCell(--cellIx)
        }
        return cellIx + 1
    }

    /**
     * used internally to refresh the "first cell" when the first cell is removed.
     * @return 0 when row contains no cells (also when first cell is occupied)
     */
    private fun calculateNewFirstCell(firstcell: Int): Int {
        var cellIx = firstcell + 1
        var r = retrieveCell(cellIx)

        while (r == null) {
            if (cellIx <= cells.size) {
                return 0
            }
            r = retrieveCell(++cellIx)
        }
        return cellIx
    }

    /**
     * Is this row formatted? Most aren't, but some rows
     * do have whole-row styles. For those that do, you
     * can get the formatting from [.getRowStyle]
     */
    override fun isFormatted(): Boolean {
        return rowRecord.getFormatted()
    }

    /**
     * Returns the whole-row cell styles. Most rows won't
     * have one of these, so will return null. Call
     * [.isFormatted] to check first.
     */
    override fun getRowStyle(): HSSFCellStyle? {
        if (!isFormatted()) {
            return null
        }
        val styleIndex: Short = rowRecord.getXFIndex()
        val xf = book.workbook.getExFormatAt(styleIndex.toInt())
        return HSSFCellStyle(styleIndex, xf, book)
    }

    val rowStyleIndex: Int
        get() {
            if (!isFormatted()) {
                return 0
            }

            return rowRecord.getXFIndex().toInt()
        }

    /**
     * Applies a whole-row cell styling to the row.
     */
    fun setRowStyle(style: HSSFCellStyle) {
        rowRecord.setFormatted(true)
        rowRecord.setXFIndex(style.getIndex())
    }

    /**
     * Applies a whole-row cell styling to the row.
     */
    override fun setRowStyle(style: ICellStyle?) {
        setRowStyle(style as HSSFCellStyle)
    }

    /**
     * @return cell iterator of the physically defined cells.
     * Note that the 4th element might well not be cell 4, as the iterator
     * will not return un-defined (null) cells.
     * Call getCellNum() on the returned cells to know which cell they are.
     * As this only ever works on physically defined cells,
     * the [com.com.wxiwei.office.fc.ss.usermodel.IRow.MissingCellPolicy] has no effect.
     */
    override fun cellIterator(): MutableIterator<ICell?> {
        return CellIterator()
    }

    /**
     * Alias for [.cellIterator] to allow
     * foreach loops
     */
    override fun iterator(): MutableIterator<ICell?> {
        return cellIterator()
    }

    /**
     * An iterator over the (physical) cells in the row.
     */
    private inner class CellIterator : MutableIterator<ICell?> {
        var thisId: Int = -1
        var nextId: Int = -1

        init {
            findNext()
        }

        override fun hasNext(): Boolean {
            return nextId < cells.size
        }

        override fun next(): ICell? {
            if (!hasNext()) throw NoSuchElementException("At last element")
            val cell = cells[nextId]
            thisId = nextId
            findNext()
            return cell
        }

        override fun remove() {
            check(thisId != -1) { "remove() called before next()" }
            cells[thisId] = null
        }

        fun findNext() {
            var i = nextId + 1
            while (i < cells.size) {
                if (cells[i] != null) break
                i++
            }
            nextId = i
        }
    }

    fun compareTo(obj: Any?): Int {
        val loc = obj as HSSFRow

        if (this.getRowNum() == loc.getRowNum()) {
            return 0
        }
        if (this.getRowNum() < loc.getRowNum()) {
            return -1
        }
        if (this.getRowNum() > loc.getRowNum()) {
            return 1
        }
        return -1
    }

    override fun equals(obj: Any?): Boolean {
        if (obj !is HSSFRow) {
            return false
        }
        val loc = obj

        if (this.getRowNum() == loc.getRowNum()) {
            return true
        }
        return false
    }

    val isEmpty: Boolean
        /**
         * 
         */
        get() = rowRecord!!.isEmpty()

    companion object {
        // used for collections
        const val INITIAL_CAPACITY: Int = 5
    }
}
