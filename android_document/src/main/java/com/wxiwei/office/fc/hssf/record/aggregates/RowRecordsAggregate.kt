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
package com.wxiwei.office.fc.hssf.record.aggregates

import com.wxiwei.office.fc.hssf.formula.FormulaShifter
import com.wxiwei.office.fc.hssf.model.RecordStream
import com.wxiwei.office.fc.hssf.record.ArrayRecord
import com.wxiwei.office.fc.hssf.record.CellValueRecordInterface
import com.wxiwei.office.fc.hssf.record.ContinueRecord
import com.wxiwei.office.fc.hssf.record.DBCellRecord
import com.wxiwei.office.fc.hssf.record.DimensionsRecord
import com.wxiwei.office.fc.hssf.record.FormulaRecord
import com.wxiwei.office.fc.hssf.record.IndexRecord
import com.wxiwei.office.fc.hssf.record.MergeCellsRecord
import com.wxiwei.office.fc.hssf.record.MulBlankRecord
import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.hssf.record.RowRecord
import com.wxiwei.office.fc.hssf.record.SharedFormulaRecord
import com.wxiwei.office.fc.hssf.record.TableRecord
import com.wxiwei.office.fc.hssf.record.UnknownRecord
import com.wxiwei.office.fc.ss.SpreadsheetVersion
import java.util.TreeMap

/**
 * 
 * @author  andy
 * @author Jason Height (jheight at chariot dot net dot au)
 */
class RowRecordsAggregate private constructor(svm: SharedValueManager) : RecordAggregate() {
    var firstRowNum: Int = -1
        private set
    var lastRowNum: Int = -1
        private set
    private val _rowRecords: MutableMap<Int?, RowRecord?>
    private val _valuesAgg: ValueRecordsAggregate
    private val _unknownRecords: MutableList<Record?>
    private val _sharedValueManager: SharedValueManager

    // Cache values to speed up performance of
    // getStartRowNumberForBlock / getEndRowNumberForBlock, see Bugzilla 47405
    private var _rowRecordValues: Array<RowRecord?>? = null

    /** Creates a new instance of ValueRecordsAggregate  */
    constructor() : this(SharedValueManager.Companion.createEmpty())

    init {
        requireNotNull(svm) { "SharedValueManager must be provided." }
        _rowRecords = TreeMap<Int?, RowRecord?>()
        _valuesAgg = ValueRecordsAggregate()
        _unknownRecords = ArrayList<Record?>()
        _sharedValueManager = svm
    }

    /**
     * @param rs record stream with all [SharedFormulaRecord]
     * [ArrayRecord], [TableRecord] [MergeCellsRecord] Records removed
     * @param svm an initialised [SharedValueManager] (from the shared formula, array
     * and table records of the current sheet).  Never `null`.
     */
    constructor(rs: RecordStream, svm: SharedValueManager) : this(svm) {
        while (rs.hasNext()) {
            val rec: Record? = rs.next
            when (rec!!.getSid()) {
                RowRecord.sid -> {
                    insertRow(rec as RowRecord)
                    continue
                }

                DBCellRecord.sid ->                    // end of 'Row Block'.  Should only occur after cell records
                    // ignore DBCELL records because POI generates them upon re-serialization
                    continue
            }
            if (rec is UnknownRecord) {
                // might need to keep track of where exactly these belong
                addUnknownRecord(rec)
                while (rs.peekNextSid() == ContinueRecord.sid.toInt()) {
                    addUnknownRecord(rs.next)
                }
                continue
            }
            if (rec is MulBlankRecord) {
                _valuesAgg.addMultipleBlanks(rec)
                continue
            }
            if (rec !is CellValueRecordInterface) {
                throw RuntimeException("Unexpected record type (" + rec.javaClass.getName() + ")")
            }
            _valuesAgg.construct(rec as CellValueRecordInterface, rs, svm)
        }
    }

    /**
     * Handles UnknownRecords which appear within the row/cell records
     */
    private fun addUnknownRecord(rec: Record?) {
        // ony a few distinct record IDs are encountered by the existing POI test cases:
        // 0x1065 // many
        // 0x01C2 // several
        // 0x0034 // few
        // No documentation could be found for these

        // keep the unknown records for re-serialization

        _unknownRecords.add(rec)
    }

    fun insertRow(row: RowRecord) {
        // Integer integer = Integer.valueOf(row.getRowNumber());
        _rowRecords.put(row.getRowNumber(), row)
        // Clear the cached values
        _rowRecordValues = null
        if ((row.getRowNumber() < this.firstRowNum) || (this.firstRowNum == -1)) {
            this.firstRowNum = row.getRowNumber()
        }
        if ((row.getRowNumber() > this.lastRowNum) || (this.lastRowNum == -1)) {
            this.lastRowNum = row.getRowNumber()
        }
    }

    fun removeRow(row: RowRecord) {
        val rowIndex = row.getRowNumber()
        _valuesAgg.removeAllCellsValuesForRow(rowIndex)
        val key = rowIndex
        val rr = _rowRecords.remove(key)
        if (rr == null) {
            throw RuntimeException("Invalid row index (" + key + ")")
        }
        if (row != rr) {
            _rowRecords.put(key, rr)
            throw RuntimeException("Attempt to remove row that does not belong to this sheet")
        }


        // Clear the cached values
        _rowRecordValues = null
    }

    fun getRow(rowIndex: Int): RowRecord? {
        val maxrow = SpreadsheetVersion.EXCEL97.getLastRowIndex()
        require(!(rowIndex < 0 || rowIndex > maxrow)) { "The row number must be between 0 and " + maxrow }
        return _rowRecords.get(rowIndex)
    }

    val physicalNumberOfRows: Int
        get() = _rowRecords.size

    val rowBlockCount: Int
        /** Returns the number of row blocks.
         * 
         * The row blocks are goupings of rows that contain the DBCell record
         * after them
         */
        get() {
            var size = _rowRecords.size / DBCellRecord.BLOCK_SIZE
            if ((_rowRecords.size % DBCellRecord.BLOCK_SIZE) != 0) size++
            return size
        }

    private fun getRowBlockSize(block: Int): Int {
        return RowRecord.ENCODED_SIZE * getRowCountForBlock(block)
    }

    /** Returns the number of physical rows within a block */
    fun getRowCountForBlock(block: Int): Int {
        val startIndex = block * DBCellRecord.BLOCK_SIZE
        var endIndex = startIndex + DBCellRecord.BLOCK_SIZE - 1
        if (endIndex >= _rowRecords.size) endIndex = _rowRecords.size - 1

        return endIndex - startIndex + 1
    }

    /** Returns the physical row number of the first row in a block */
    private fun getStartRowNumberForBlock(block: Int): Int {
        val startIndex = block * DBCellRecord.BLOCK_SIZE

        if (_rowRecordValues == null) {
            _rowRecordValues = _rowRecords.values.toTypedArray<RowRecord?>()
        }

        try {
            return _rowRecordValues!![startIndex]!!.getRowNumber()
        } catch (e: ArrayIndexOutOfBoundsException) {
            throw RuntimeException("Did not find start row for block " + block)
        }
    }

    /** Returns the physical row number of the end row in a block */
    private fun getEndRowNumberForBlock(block: Int): Int {
        var endIndex = ((block + 1) * DBCellRecord.BLOCK_SIZE) - 1
        if (endIndex >= _rowRecords.size) endIndex = _rowRecords.size - 1

        if (_rowRecordValues == null) {
            _rowRecordValues = _rowRecords.values.toTypedArray<RowRecord?>()
        }

        try {
            return _rowRecordValues!![endIndex]!!.getRowNumber()
        } catch (e: ArrayIndexOutOfBoundsException) {
            throw RuntimeException("Did not find end row for block " + block)
        }
    }

    private fun visitRowRecordsForBlock(blockIndex: Int, rv: RecordVisitor): Int {
        val startIndex = blockIndex * DBCellRecord.BLOCK_SIZE
        val endIndex = startIndex + DBCellRecord.BLOCK_SIZE

        val rowIterator = _rowRecords.values.iterator()

        //Given that we basically iterate through the rows in order,
        //For a performance improvement, it would be better to return an instance of
        //an iterator and use that instance throughout, rather than recreating one and
        //having to move it to the right position.
        var i = 0
        while (i < startIndex) {
            rowIterator.next()
            i++
        }
        var result = 0
        while (rowIterator.hasNext() && (i++ < endIndex)) {
            val rec = rowIterator.next() as Record
            result += rec.getRecordSize()
            rv.visitRecord(rec)
        }
        return result
    }

    override fun visitContainedRecords(rv: RecordVisitor) {
        val stv = PositionTrackingVisitor(rv, 0)
        //DBCells are serialized before row records.
        val blockCount = this.rowBlockCount
        for (blockIndex in 0..<blockCount) {
            // Serialize a block of rows.
            // Hold onto the position of the first row in the block
            var pos = 0
            // Hold onto the size of this block that was serialized
            val rowBlockSize = visitRowRecordsForBlock(blockIndex, rv)
            pos += rowBlockSize
            // Serialize a block of cells for those rows
            val startRowNumber = getStartRowNumberForBlock(blockIndex)
            val endRowNumber = getEndRowNumberForBlock(blockIndex)
            val dbcrBuilder = DBCellRecord.Builder()
            // Note: Cell references start from the second row...
            var cellRefOffset = (rowBlockSize - RowRecord.ENCODED_SIZE)
            for (row in startRowNumber..endRowNumber) {
                if (_valuesAgg.rowHasCells(row)) {
                    stv.position = 0
                    _valuesAgg.visitCellsForRow(row, stv)
                    val rowCellSize = stv.position
                    pos += rowCellSize
                    // Add the offset to the first cell for the row into the
                    // DBCellRecord.
                    dbcrBuilder.addCellOffset(cellRefOffset)
                    cellRefOffset = rowCellSize
                }
            }
            // Calculate Offset from the start of a DBCellRecord to the first Row
            rv.visitRecord(dbcrBuilder.build(pos))
        }
        for (i in _unknownRecords.indices) {
            // Potentially breaking the file here since we don't know exactly where to write these records
            rv.visitRecord(_unknownRecords.get(i)!!)
        }
    }

    val iterator: MutableIterator<RowRecord?>
        get() = _rowRecords.values.iterator()

    fun findStartOfRowOutlineGroup(row: Int): Int {
        // Find the start of the group.
        var rowRecord = this.getRow(row)
        val level = rowRecord!!.getOutlineLevel().toInt()
        var currentRow = row
        while (this.getRow(currentRow) != null) {
            rowRecord = this.getRow(currentRow)
            if (rowRecord!!.getOutlineLevel() < level) {
                return currentRow + 1
            }
            currentRow--
        }

        return currentRow + 1
    }

    fun findEndOfRowOutlineGroup(row: Int): Int {
        val level = getRow(row)!!.getOutlineLevel().toInt()
        var currentRow: Int
        currentRow = row
        while (currentRow < this.lastRowNum) {
            if (getRow(currentRow) == null || getRow(currentRow)!!.getOutlineLevel() < level) {
                break
            }
            currentRow++
        }

        return currentRow - 1
    }

    /**
     * Hide all rows at or below the current outline level
     * @return index of the *next* row after the last row that gets hidden
     ** */
    private fun writeHidden(pRowRecord: RowRecord?, row: Int): Int {
        var rowIx = row
        var rowRecord = pRowRecord
        val level = rowRecord!!.getOutlineLevel().toInt()
        while (rowRecord != null && getRow(rowIx)!!.getOutlineLevel() >= level) {
            rowRecord.setZeroHeight(true)
            rowIx++
            rowRecord = getRow(rowIx)
        }
        return rowIx
    }

    fun collapseRow(rowNumber: Int) {
        // Find the start of the group.

        val startRow = findStartOfRowOutlineGroup(rowNumber)
        val rowRecord = getRow(startRow)

        // Hide all the columns until the end of the group
        val nextRowIx = writeHidden(rowRecord, startRow)

        var row = getRow(nextRowIx)
        if (row == null) {
            row = createRow(nextRowIx)
            insertRow(row)
        }
        // Write collapse field
        row.setColapsed(true)
    }

    fun isRowGroupCollapsed(row: Int): Boolean {
        val collapseRow = findEndOfRowOutlineGroup(row) + 1

        if (getRow(collapseRow) == null) {
            return false
        }
        return getRow(collapseRow)!!.getColapsed()
    }

    fun expandRow(rowNumber: Int) {
        val idx = rowNumber
        if (idx == -1) return

        // If it is already expanded do nothing.
        if (!isRowGroupCollapsed(idx)) {
            return
        }

        // Find the start of the group.
        val startIdx = findStartOfRowOutlineGroup(idx)
        val row = getRow(startIdx)

        // Find the end of the group.
        val endIdx = findEndOfRowOutlineGroup(idx)

        // expand:
        // collapsed bit must be unset
        // hidden bit gets unset _if_ surrounding groups are expanded you can determine
        //   this by looking at the hidden bit of the enclosing group.  You will have
        //   to look at the start and the end of the current group to determine which
        //   is the enclosing group
        // hidden bit only is altered for this outline level.  ie.  don't un-collapse contained groups
        if (!isRowGroupHiddenByParent(idx)) {
            for (i in startIdx..endIdx) {
                val otherRow = getRow(i)
                if (row!!.getOutlineLevel() == otherRow!!.getOutlineLevel() || !isRowGroupCollapsed(
                        i
                    )
                ) {
                    otherRow.setZeroHeight(false)
                }
            }
        }

        // Write collapse field
        getRow(endIdx + 1)!!.setColapsed(false)
    }

    fun isRowGroupHiddenByParent(row: Int): Boolean {
        // Look out outline details of end
        val endLevel: Int
        val endHidden: Boolean
        val endOfOutlineGroupIdx = findEndOfRowOutlineGroup(row)
        if (getRow(endOfOutlineGroupIdx + 1) == null) {
            endLevel = 0
            endHidden = false
        } else {
            endLevel = getRow(endOfOutlineGroupIdx + 1)!!.getOutlineLevel().toInt()
            endHidden = getRow(endOfOutlineGroupIdx + 1)!!.getZeroHeight()
        }

        // Look out outline details of start
        val startLevel: Int
        val startHidden: Boolean
        val startOfOutlineGroupIdx = findStartOfRowOutlineGroup(row)
        if (startOfOutlineGroupIdx - 1 < 0 || getRow(startOfOutlineGroupIdx - 1) == null) {
            startLevel = 0
            startHidden = false
        } else {
            startLevel = getRow(startOfOutlineGroupIdx - 1)!!.getOutlineLevel().toInt()
            startHidden = getRow(startOfOutlineGroupIdx - 1)!!.getZeroHeight()
        }

        if (endLevel > startLevel) {
            return endHidden
        }

        return startHidden
    }

    val cellValueIterator: MutableIterator<CellValueRecordInterface?>
        /**
         * Returns an iterator for the cell values
         */
        get() = _valuesAgg.iterator()

    @get:Deprecated("use {@link #getCellValueIterator()} instead")
    val valueRecords: Array<CellValueRecordInterface?>
        get() = _valuesAgg.valueRecords

    fun createIndexRecord(indexRecordOffset: Int, sizeOfInitialSheetRecords: Int): IndexRecord {
        val result = IndexRecord()
        result.setFirstRow(this.firstRowNum)
        result.setLastRowAdd1(this.lastRowNum + 1)

        // Calculate the size of the records from the end of the BOF
        // and up to the RowRecordsAggregate...

        // Add the references to the DBCells in the IndexRecord (one for each block)
        // Note: The offsets are relative to the Workbook BOF. Assume that this is
        // 0 for now.....
        val blockCount = this.rowBlockCount
        // Calculate the size of this IndexRecord
        val indexRecSize = IndexRecord.getRecordSizeForBlockCount(blockCount)

        var currentOffset = indexRecordOffset + indexRecSize + sizeOfInitialSheetRecords

        for (block in 0..<blockCount) {
            // each row-block has a DBCELL record.
            // The offset of each DBCELL record needs to be updated in the INDEX record

            // account for row records in this row-block

            currentOffset += getRowBlockSize(block)
            // account for cell value records after those
            currentOffset += _valuesAgg.getRowCellBlockSize(
                getStartRowNumberForBlock(block), getEndRowNumberForBlock(block)
            )

            // currentOffset is now the location of the DBCELL record for this row-block
            result.addDbcell(currentOffset)
            // Add space required to write the DBCELL record (whose reference was just added).
            currentOffset += (8 + (getRowCountForBlock(block) * 2))
        }
        return result
    }

    fun insertCell(cvRec: CellValueRecordInterface) {
        _valuesAgg.insertCell(cvRec)
    }

    fun removeCell(cvRec: CellValueRecordInterface?) {
        if (cvRec is FormulaRecordAggregate) {
            cvRec.notifyFormulaChanging()
        }
        _valuesAgg.removeCell(cvRec!!)
    }

    fun createFormula(row: Int, col: Int): FormulaRecordAggregate {
        val fr = FormulaRecord()
        fr.row = row
        fr.column = col.toShort()
        return FormulaRecordAggregate(fr, null, _sharedValueManager)
    }

    fun updateFormulasAfterRowShift(formulaShifter: FormulaShifter?, currentExternSheetIndex: Int) {
        _valuesAgg.updateFormulasAfterRowShift(formulaShifter!!, currentExternSheetIndex)
    }

    fun createDimensions(): DimensionsRecord {
        val result = DimensionsRecord()
        result.setFirstRow(this.firstRowNum)
        result.setLastRow(this.lastRowNum)
        result.setFirstCol(_valuesAgg.firstCellNum.toShort())
        result.setLastCol(_valuesAgg.lastCellNum.toShort())
        return result
    }

    fun dispose() {
        _rowRecords.clear()

        _valuesAgg.dispose()

        _unknownRecords.clear()

        _rowRecordValues = null
    }

    companion object {
        /**
         * Create a row record.
         * 
         * @param rowNumber row number
         * @return RowRecord created for the passed in row number
         * @see RowRecord
         */
        fun createRow(rowNumber: Int): RowRecord {
            return RowRecord(rowNumber)
        }
    }
}
