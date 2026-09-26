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
import com.wxiwei.office.fc.hssf.record.BlankRecord
import com.wxiwei.office.fc.hssf.record.CellValueRecordInterface
import com.wxiwei.office.fc.hssf.record.FormulaRecord
import com.wxiwei.office.fc.hssf.record.MulBlankRecord
import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.hssf.record.RecordBase
import com.wxiwei.office.fc.hssf.record.StringRecord
import com.wxiwei.office.fc.hssf.record.aggregates.RecordAggregate.RecordVisitor

/**
 * 
 * Aggregate value records together.  Things are easier to handle that way.
 * 
 * @author  andy
 * @author  Glen Stampoultzis (glens at apache.org)
 * @author Jason Height (jheight at chariot dot net dot au)
 */
class ValueRecordsAggregate private constructor(
    firstCellIx: Int,
    lastCellIx: Int,
    pRecords: Array<Array<CellValueRecordInterface?>?>?
) : Iterable<CellValueRecordInterface?>, Cloneable {
    var firstCellNum: Int = INDEX_NOT_SET
        private set
    var lastCellNum: Int = INDEX_NOT_SET
        private set
    private var records: Array<Array<CellValueRecordInterface?>?>?

    /** Creates a new instance of ValueRecordsAggregate  */
    constructor() : this(
        INDEX_NOT_SET,
        INDEX_NOT_SET,
        arrayOfNulls<Array<CellValueRecordInterface?>>(30)
    ) // We start with 30 Rows.

    init {
        this.firstCellNum = firstCellIx
        this.lastCellNum = lastCellIx
        records = pRecords
    }

    fun insertCell(cell: CellValueRecordInterface) {
        val column = cell.column
        val row = cell.row
        if (row >= records!!.size) {
            val oldRecords = records
            var newSize = oldRecords!!.size * 2
            if (newSize < row + 1) newSize = row + 1
            records = arrayOfNulls<Array<CellValueRecordInterface?>>(newSize)
            System.arraycopy(oldRecords, 0, records, 0, oldRecords.size)
        }
        var rowCells = records!![row]
        if (rowCells == null) {
            var newSize = column + 1
            if (newSize < 10) newSize = 10
            rowCells = arrayOfNulls<CellValueRecordInterface>(newSize)
            records!![row] = rowCells
        }
        if (column >= rowCells.size) {
            val oldRowCells: Array<CellValueRecordInterface?>? = rowCells
            var newSize = oldRowCells!!.size * 2
            if (newSize < column + 1) newSize = column + 1
            // if(newSize>257) newSize=257; // activate?
            rowCells = arrayOfNulls<CellValueRecordInterface>(newSize)
            System.arraycopy(oldRowCells, 0, rowCells, 0, oldRowCells.size)
            records!![row] = rowCells
        }
        rowCells[column.toInt()] = cell

        if (column < this.firstCellNum || this.firstCellNum == INDEX_NOT_SET) {
            this.firstCellNum = column.toInt()
        }
        if (column > this.lastCellNum || this.lastCellNum == INDEX_NOT_SET) {
            this.lastCellNum = column.toInt()
        }
    }

    fun removeCell(cell: CellValueRecordInterface) {
        requireNotNull(cell) { "cell must not be null" }
        val row = cell.row
        if (row >= records!!.size) {
            throw RuntimeException("cell row is out of range")
        }
        val rowCells = records!![row]!!
        if (rowCells == null) {
            throw RuntimeException("cell row is already empty")
        }
        val column = cell.column
        if (column >= rowCells.size) {
            throw RuntimeException("cell column is out of range")
        }
        rowCells[column.toInt()] = null
    }

    fun removeAllCellsValuesForRow(rowIndex: Int) {
        require(!(rowIndex < 0 || rowIndex > MAX_ROW_INDEX)) {
            ("Specified rowIndex " + rowIndex
                    + " is outside the allowable range (0.." + MAX_ROW_INDEX + ")")
        }
        if (rowIndex >= records!!.size) {
            // this can happen when the client code has created a row,
            // and then removes/replaces it before adding any cells. (see bug 46312)
            return
        }

        records!![rowIndex] = null
    }


    val physicalNumberOfCells: Int
        get() {
            var count = 0
            for (r in records!!.indices) {
                val rowCells = records!![r]
                if (rowCells != null) {
                    for (c in rowCells.indices) {
                        if (rowCells[c] != null) count++
                    }
                }
            }
            return count
        }

    fun addMultipleBlanks(mbr: MulBlankRecord) {
        for (j in 0..<mbr.getNumColumns()) {
            val br = BlankRecord()

            br.column = (j + mbr.getFirstColumn()).toShort()
            br.row = mbr.getRow()
            br.xFIndex = mbr.getXFAt(j)
            insertCell(br)
        }
    }

    /**
     * Processes a single cell value record
     * @param sfh used to resolve any shared-formulas/arrays/tables for the current sheet
     */
    fun construct(rec: CellValueRecordInterface, rs: RecordStream, sfh: SharedValueManager) {
        if (rec is FormulaRecord) {
            val formulaRec = rec
            // read optional cached text value
            val cachedText: StringRecord?
            val nextClass = rs.peekNextClass()
            if (nextClass == StringRecord::class.java) {
                cachedText = rs.next as StringRecord?
            } else {
                cachedText = null
            }
            insertCell(FormulaRecordAggregate(formulaRec, cachedText, sfh))
        } else {
            insertCell(rec)
        }
    }

    /** Tallies a count of the size of the cell records
     * that are attached to the rows in the range specified.
     */
    fun getRowCellBlockSize(startRow: Int, endRow: Int): Int {
        var result = 0
        var rowIx = startRow
        while (rowIx <= endRow && rowIx < records!!.size) {
            result += getRowSerializedSize(records!![rowIx])
            rowIx++
        }
        return result
    }

    /** Returns true if the row has cells attached to it  */
    fun rowHasCells(row: Int): Boolean {
        if (row >= records!!.size) {
            return false
        }
        val rowCells = records!![row]
        if (rowCells == null) return false
        for (col in rowCells.indices) {
            if (rowCells[col] != null) return true
        }
        return false
    }

    fun visitCellsForRow(rowIndex: Int, rv: RecordVisitor) {
        val rowCells = records!![rowIndex]!!
        requireNotNull(rowCells) { "Row [" + rowIndex + "] is empty" }


        var i = 0
        while (i < rowCells.size) {
            val cvr = rowCells[i] as RecordBase?
            if (cvr == null) {
                i++
                continue
            }
            val nBlank: Int = countBlanks(rowCells, i)
            if (nBlank > 1) {
                rv.visitRecord(createMBR(rowCells, i, nBlank))
                i += nBlank - 1
            } else if (cvr is RecordAggregate) {
                val agg = cvr
                agg.visitContainedRecords(rv)
            } else {
                rv.visitRecord(cvr as Record)
            }
            i++
        }
    }

    private fun createMBR(
        cellValues: Array<CellValueRecordInterface?>,
        startIx: Int,
        nBlank: Int
    ): MulBlankRecord {
        val xfs = ShortArray(nBlank)
        for (i in xfs.indices) {
            xfs[i] = (cellValues[startIx + i] as BlankRecord).xFIndex
        }
        val rowIx = cellValues[startIx]!!.row
        return MulBlankRecord(rowIx, startIx, xfs)
    }

    fun updateFormulasAfterRowShift(shifter: FormulaShifter, currentExternSheetIndex: Int) {
        for (i in records!!.indices) {
            val rowCells = records!![i]
            if (rowCells == null) {
                continue
            }
            for (j in rowCells.indices) {
                val cell = rowCells[j]
                if (cell is FormulaRecordAggregate) {
                    val fr = cell.formulaRecord
                    val ptgs = fr.getParsedExpression() // needs clone() inside this getter?
                    if (shifter.adjustFormula(ptgs, currentExternSheetIndex)) {
                        fr.setParsedExpression(ptgs)
                    }
                }
            }
        }
    }

    /**
     * iterator for CellValueRecordInterface
     */
    internal inner class ValueIterator : MutableIterator<CellValueRecordInterface?> {
        var curRowIndex: Int = 0
        var curColIndex: Int = -1
        var nextRowIndex: Int = 0
        var nextColIndex: Int = -1

        init {
            this.nextPos
        }

        val nextPos: Unit
            get() {
                if (nextRowIndex >= records!!.size) return  // no next already


                while (nextRowIndex < records!!.size) {
                    ++nextColIndex
                    if (records!![nextRowIndex] == null || nextColIndex >= records!![nextRowIndex]!!.size) {
                        ++nextRowIndex
                        nextColIndex = -1
                        continue
                    }

                    if (records!![nextRowIndex]!![nextColIndex] != null) return  // next cell found
                }
                // no next found
            }

        override fun hasNext(): Boolean {
            return nextRowIndex < records!!.size
        }

        override fun next(): CellValueRecordInterface? {
            if (!hasNext()) throw IndexOutOfBoundsException("iterator has no next")

            curRowIndex = nextRowIndex
            curColIndex = nextColIndex
            val ret = records!![curRowIndex]!![curColIndex]
            this.nextPos
            return ret
        }

        override fun remove() {
            records!![curRowIndex]!![curColIndex] = null
        }
    }

    /** value iterator  */
    override fun iterator(): MutableIterator<CellValueRecordInterface?> {
        return ValueIterator()
    }

    @get:Deprecated("use {@link #iterator()} instead")
    val valueRecords: Array<CellValueRecordInterface?>
        /**
         * Gets all the cell records contained in this aggregate.
         * Note [BlankRecord]s appear separate (not in [MulBlankRecord]s).
         */
        get() {
            val temp: MutableList<CellValueRecordInterface?> =
                ArrayList<CellValueRecordInterface?>()

            for (rowIx in records!!.indices) {
                val rowCells = records!![rowIx]
                if (rowCells == null) {
                    continue
                }
                for (colIx in rowCells.indices) {
                    val cell = rowCells[colIx]
                    if (cell != null) {
                        temp.add(cell)
                    }
                }
            }

            return temp.toTypedArray()
        }

    public override fun clone(): Any {
        throw RuntimeException("clone() should not be called.  ValueRecordsAggregate should be copied via Sheet.cloneSheet()")
    }

    fun dispose() {
        records = null
    }

    companion object {
        private const val MAX_ROW_INDEX = 0XFFFF
        private val INDEX_NOT_SET = -1
        private fun getRowSerializedSize(rowCells: Array<CellValueRecordInterface?>?): Int {
            if (rowCells == null) {
                return 0
            }
            var result = 0
            var i = 0
            while (i < rowCells.size) {
                val cvr = rowCells[i] as RecordBase?
                if (cvr == null) {
                    i++
                    continue
                }
                val nBlank: Int = countBlanks(rowCells, i)
                if (nBlank > 1) {
                    result += (10 + 2 * nBlank)
                    i += nBlank - 1
                } else {
                    result += cvr.getRecordSize()
                }
                i++
            }
            return result
        }

        /**
         * @return the number of *consecutive* [BlankRecord]s in the specified row
         * starting from startIx.
         */
        private fun countBlanks(
            rowCellValues: Array<CellValueRecordInterface?>,
            startIx: Int
        ): Int {
            var i = startIx
            while (i < rowCellValues.size) {
                val cvr = rowCellValues[i]
                if (cvr !is BlankRecord) {
                    break
                }
                i++
            }
            return i - startIx
        }
    }
}
