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

import com.wxiwei.office.fc.hssf.formula.ptg.ExpPtg
import com.wxiwei.office.fc.hssf.record.ArrayRecord
import com.wxiwei.office.fc.hssf.record.FormulaRecord
import com.wxiwei.office.fc.hssf.record.SharedFormulaRecord
import com.wxiwei.office.fc.hssf.record.SharedValueRecordBase
import com.wxiwei.office.fc.hssf.record.TableRecord
import com.wxiwei.office.fc.hssf.util.CellRangeAddress8Bit
import com.wxiwei.office.fc.ss.util.CellReference

/**
 * Manages various auxiliary records while constructing a
 * [RowRecordsAggregate]:
 * 
 *  * [SharedFormulaRecord]s
 *  * [ArrayRecord]s
 *  * [TableRecord]s
 * 
 * 
 * @author Josh Micich
 * @author Vladimirs Abramovs(Vladimirs.Abramovs at exigenservices.com) - handling of ArrayRecords
 */
class SharedValueManager private constructor(
    sharedFormulaRecords: Array<SharedFormulaRecord>,
    firstCells: Array<CellReference?>,
    arrayRecords: Array<ArrayRecord?>,
    tableRecords: Array<TableRecord>
) {
    private class SharedFormulaGroup(sfr: SharedFormulaRecord, firstCell: CellReference) {
        val sFR: SharedFormulaRecord
        private val _frAggs: Array<FormulaRecordAggregate?>
        private var _numberOfFormulas: Int

        /**
         * Coordinates of the first cell having a formula that uses this shared formula.
         * This is often *but not always* the top left cell in the range covered by
         * [._sfr]
         */
        val _firstCell: CellReference

        init {
            require(sfr.isInRange(firstCell.getRow(), firstCell.getCol().toInt())) {
                ("First formula cell " + firstCell.formatAsString()
                        + " is not shared formula range " + sfr.getRange().toString() + ".")
            }
            this.sFR = sfr
            _firstCell = firstCell
            val width = sfr.getLastColumn() - sfr.getFirstColumn() + 1
            val height = sfr.getLastRow() - sfr.getFirstRow() + 1
            _frAggs = arrayOfNulls<FormulaRecordAggregate>(width * height)
            _numberOfFormulas = 0
        }

        fun add(agg: FormulaRecordAggregate) {
            if (_numberOfFormulas == 0) {
                check(!(_firstCell.getRow() != agg.row || _firstCell.getCol() != agg.column)) { "shared formula coding error: " + _firstCell.getCol() + '/' + _firstCell.getRow() + " != " + agg.column + '/' + agg.row }
            }
            if (_numberOfFormulas >= _frAggs.size) {
                throw RuntimeException("Too many formula records for shared formula group")
            }
            _frAggs[_numberOfFormulas++] = agg
        }

        fun unlinkSharedFormulas() {
            for (i in 0..<_numberOfFormulas) {
                _frAggs[i]!!.unlinkSharedFormula()
            }
        }

        override fun toString(): String {
            val sb = StringBuffer(64)
            sb.append(javaClass.getName()).append(" [")
            sb.append(sFR.getRange().toString())
            sb.append("]")
            return sb.toString()
        }
    }

    private val _arrayRecords: MutableList<ArrayRecord>
    private val _tableRecords: Array<TableRecord>
    private val _groupsBySharedFormulaRecord: MutableMap<SharedFormulaRecord?, SharedFormulaGroup>

    /** cached for optimization purposes  */
    private var _groupsCache: MutableMap<Int?, SharedFormulaGroup>? = null

    init {
        val nShF = sharedFormulaRecords.size
        require(nShF == firstCells.size) { "array sizes don't match: " + nShF + "!=" + firstCells.size + "." }
        _arrayRecords = toList(arrayRecords)
        _tableRecords = tableRecords
        val m: MutableMap<SharedFormulaRecord?, SharedFormulaGroup> =
            HashMap<SharedFormulaRecord?, SharedFormulaGroup>(nShF * 3 / 2)
        for (i in 0..<nShF) {
            val sfr = sharedFormulaRecords[i]
            m.put(sfr, SharedFormulaGroup(sfr, firstCells[i]!!))
        }
        _groupsBySharedFormulaRecord = m
    }

    /**
     * @param firstCell as extracted from the [ExpPtg] from the cell's formula.
     * @return never `null`
     */
    fun linkSharedFormulaRecord(
        firstCell: CellReference,
        agg: FormulaRecordAggregate
    ): SharedFormulaRecord {
        val result = findFormulaGroupForCell(firstCell)
        result.add(agg)
        return result.sFR
    }

    private fun findFormulaGroupForCell(cellRef: CellReference): SharedFormulaGroup {
        if (null == _groupsCache) {
            _groupsCache = HashMap<Int?, SharedFormulaGroup>(_groupsBySharedFormulaRecord.size)
            for (group in _groupsBySharedFormulaRecord.values) {
                _groupsCache!!.put(getKeyForCache(group._firstCell), group)
            }
        }
        val sfg: SharedFormulaGroup? = _groupsCache!!.get(getKeyForCache(cellRef))
        if (null == sfg) {
            // TODO - fix file "15228.xls" so it opens in Excel after rewriting with POI
            throw RuntimeException("Failed to find a matching shared formula record")
        }
        return sfg
    }

    private fun getKeyForCache(cellRef: CellReference): Int {
        // The HSSF has a max of 2^16 rows and 2^8 cols
        return (cellRef.getCol() + 1) shl 16 or cellRef.getRow()
    }

    /**
     * Gets the [SharedValueRecordBase] record if it should be encoded immediately after the
     * formula record contained in the specified [FormulaRecordAggregate] agg.  Note - the
     * shared value record always appears after the first formula record in the group.  For arrays
     * and tables the first formula is always the in the top left cell.  However, since shared
     * formula groups can be sparse and/or overlap, the first formula may not actually be in the
     * top left cell.
     * 
     * @return the SHRFMLA, TABLE or ARRAY record for the formula cell, if it is the first cell of
     * a table or array region. `null` if the formula cell is not shared/array/table,
     * or if the specified formula is not the the first in the group.
     */
    fun getRecordForFirstCell(agg: FormulaRecordAggregate): SharedValueRecordBase? {
        val firstCell = agg.formulaRecord.getFormula().expReference
        // perhaps this could be optimised by consulting the (somewhat unreliable) isShared flag
        // and/or distinguishing between tExp and tTbl.
        if (firstCell == null) {
            // not a shared/array/table formula
            return null
        }


        val row = firstCell.getRow()
        val column = firstCell.getCol().toInt()
        if (agg.row != row || agg.column.toInt() != column) {
            // not the first formula cell in the group
            return null
        }

        if (!_groupsBySharedFormulaRecord.isEmpty()) {
            val sfg = findFormulaGroupForCell(firstCell)
            if (null != sfg) {
                return sfg.sFR
            }
        }

        // Since arrays and tables cannot be sparse (all cells in range participate)
        // The first cell will be the top left in the range.  So we can match the
        // ARRAY/TABLE record directly.
        for (tr in _tableRecords) {
            if (tr.isFirstCell(row, column)) {
                return tr
            }
        }
        for (ar in _arrayRecords) {
            if (ar.isFirstCell(row, column)) {
                return ar
            }
        }
        return null
    }

    /**
     * Converts all [FormulaRecord]s handled by <tt>sharedFormulaRecord</tt>
     * to plain unshared formulas
     */
    fun unlink(sharedFormulaRecord: SharedFormulaRecord?) {
        val svg = _groupsBySharedFormulaRecord.remove(sharedFormulaRecord)
        checkNotNull(svg) { "Failed to find formulas for shared formula" }
        _groupsCache = null // be sure to reset cached value
        svg.unlinkSharedFormulas()
    }

    /**
     * Add specified Array Record.
     */
    fun addArrayRecord(ar: ArrayRecord?) {
        // could do a check here to make sure none of the ranges overlap
        _arrayRecords.add(ar!!)
    }

    /**
     * Removes the [ArrayRecord] for the cell group containing the specified cell.
     * The caller should clear (set blank) all cells in the returned range.
     * @return the range of the array formula which was just removed. Never `null`.
     */
    fun removeArrayFormula(rowIndex: Int, columnIndex: Int): CellRangeAddress8Bit? {
        for (ar in _arrayRecords) {
            if (ar.isInRange(rowIndex, columnIndex)) {
                _arrayRecords.remove(ar)
                return ar.getRange()
            }
        }
        val ref = CellReference(rowIndex, columnIndex, false, false).formatAsString()
        throw IllegalArgumentException(
            ("Specified cell " + ref
                    + " is not part of an array formula.")
        )
    }

    /**
     * @return the shared ArrayRecord identified by (firstRow, firstColumn). never `null`.
     */
    fun getArrayRecord(firstRow: Int, firstColumn: Int): ArrayRecord? {
        for (ar in _arrayRecords) {
            if (ar.isFirstCell(firstRow, firstColumn)) {
                return ar
            }
        }
        return null
    }

    companion object {
        /**
         * @return a new empty [SharedValueManager].
         */
        fun createEmpty(): SharedValueManager {
            // Note - must create distinct instances because they are assumed to be mutable.
            return SharedValueManager(
                arrayOf<SharedFormulaRecord>(),
                arrayOfNulls<CellReference>(0),
                arrayOfNulls<ArrayRecord>(0),
                arrayOf<TableRecord>()
            )
        }

        /**
         * @return a modifiable list, independent of the supplied array
         */
        private fun <Z : Any> toList(zz: Array<Z?>): MutableList<Z> {
            val result: MutableList<Z> = ArrayList<Z>(zz.size)
            for (i in zz.indices) {
                result.add(zz[i]!!)
            }
            return result
        }

        /**
         */
        fun create(
            sharedFormulaRecords: Array<SharedFormulaRecord>,
            firstCells: Array<CellReference?>,
            arrayRecords: Array<ArrayRecord?>,
            tableRecords: Array<TableRecord>
        ): SharedValueManager {
            if (sharedFormulaRecords.size + firstCells.size + arrayRecords.size + tableRecords.size < 1) {
                return createEmpty()
            }
            return SharedValueManager(sharedFormulaRecords, firstCells, arrayRecords, tableRecords)
        }
    }
}
