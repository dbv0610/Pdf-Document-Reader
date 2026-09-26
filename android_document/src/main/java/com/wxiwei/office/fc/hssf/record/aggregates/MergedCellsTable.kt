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

import com.wxiwei.office.fc.hssf.model.RecordStream
import com.wxiwei.office.fc.hssf.record.MergeCellsRecord
import com.wxiwei.office.fc.ss.util.CellRangeAddressList
import com.wxiwei.office.fc.ss.util.HSSFCellRangeAddress

/**
 * 
 * @author Josh Micich
 */
class MergedCellsTable : RecordAggregate() {
    private val _mergedRegions: MutableList<HSSFCellRangeAddress>

    /**
     * Creates an empty aggregate
     */
    init {
        _mergedRegions = ArrayList<HSSFCellRangeAddress>()
    }

    /**
     * reads zero or more consecutive [MergeCellsRecord]s
     * @param rs
     */
    fun read(rs: RecordStream) {
        val temp = _mergedRegions
        while (rs.peekNextClass() == MergeCellsRecord::class.java) {
            val mcr = rs.next as MergeCellsRecord
            val nRegions = mcr.getNumAreas().toInt()
            for (i in 0..<nRegions) {
                val cra = mcr.getAreaAt(i)
                temp.add(cra)
            }
        }
    }

    override fun getRecordSize(): Int {
        // a bit cheaper than the default impl
        val nRegions = _mergedRegions.size
        if (nRegions < 1) {
            // no need to write a single empty MergeCellsRecord
            return 0
        }
        val nMergedCellsRecords: Int = nRegions / MAX_MERGED_REGIONS
        val nLeftoverMergedRegions: Int = nRegions % MAX_MERGED_REGIONS

        val result = ((nMergedCellsRecords
                * (4 + CellRangeAddressList.getEncodedSize(MAX_MERGED_REGIONS))) + 4
                + CellRangeAddressList.getEncodedSize(nLeftoverMergedRegions))
        return result
    }

    override fun visitContainedRecords(rv: RecordVisitor) {
        val nRegions = _mergedRegions.size
        if (nRegions < 1) {
            // no need to write a single empty MergeCellsRecord
            return
        }

        val nFullMergedCellsRecords: Int = nRegions / MAX_MERGED_REGIONS
        val nLeftoverMergedRegions: Int = nRegions % MAX_MERGED_REGIONS
        val cras = _mergedRegions.toTypedArray()

        for (i in 0..<nFullMergedCellsRecords) {
            val startIx: Int = i * MAX_MERGED_REGIONS
            rv.visitRecord(MergeCellsRecord(cras, startIx, MAX_MERGED_REGIONS))
        }
        if (nLeftoverMergedRegions > 0) {
            val startIx: Int = nFullMergedCellsRecords * MAX_MERGED_REGIONS
            rv.visitRecord(MergeCellsRecord(cras, startIx, nLeftoverMergedRegions))
        }
    }

    fun addRecords(mcrs: Array<MergeCellsRecord?>) {
        for (i in mcrs.indices) {
            addMergeCellsRecord(mcrs[i]!!)
        }
    }

    private fun addMergeCellsRecord(mcr: MergeCellsRecord) {
        val nRegions = mcr.getNumAreas().toInt()
        for (i in 0..<nRegions) {
            val cra = mcr.getAreaAt(i)
            _mergedRegions.add(cra)
        }
    }

    fun get(index: Int): HSSFCellRangeAddress? {
        checkIndex(index)
        return _mergedRegions.get(index)
    }

    fun remove(index: Int) {
        checkIndex(index)
        _mergedRegions.removeAt(index)
    }

    private fun checkIndex(index: Int) {
        require(!(index < 0 || index >= _mergedRegions.size)) {
            ("Specified CF index " + index
                    + " is outside the allowable range (0.." + (_mergedRegions.size - 1) + ")")
        }
    }

    fun addArea(rowFrom: Int, colFrom: Int, rowTo: Int, colTo: Int) {
        _mergedRegions.add(HSSFCellRangeAddress(rowFrom, rowTo, colFrom, colTo))
    }

    val numberOfMergedRegions: Int
        get() = _mergedRegions.size

    companion object {
        private const val MAX_MERGED_REGIONS = 1027 // enforced by the 8224 byte limit
    }
}
