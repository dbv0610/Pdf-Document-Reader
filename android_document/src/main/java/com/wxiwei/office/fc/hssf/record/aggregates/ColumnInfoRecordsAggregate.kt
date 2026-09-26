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
import com.wxiwei.office.fc.hssf.record.ColumnInfoRecord
import java.util.Collections
import kotlin.Any
import kotlin.Cloneable
import kotlin.Comparator
import kotlin.Int
import kotlin.RuntimeException
import kotlin.Short
import kotlin.math.max
import kotlin.math.min
import kotlin.require

/**
 * @author Glen Stampoultzis
 */
class ColumnInfoRecordsAggregate() : RecordAggregate(), Cloneable {
    /**
     * List of [ColumnInfoRecord]s assumed to be in order
     */
    private val records: MutableList<ColumnInfoRecord?>


    private class CIRComparator : Comparator<ColumnInfoRecord?> {
        override fun compare(a: ColumnInfoRecord?, b: ColumnInfoRecord?): Int {
            return Companion.compareColInfos(a!!, b!!)
        }

        companion object {
            val instance: Comparator<ColumnInfoRecord?> = CIRComparator()
            fun compareColInfos(a: ColumnInfoRecord, b: ColumnInfoRecord): Int {
                return a.firstColumn - b.firstColumn
            }
        }
    }

    /**
     * Creates an empty aggregate
     */
    init {
        records = ArrayList<ColumnInfoRecord?>()
    }

    constructor(rs: RecordStream) : this() {
        var isInOrder = true
        var cirPrev: ColumnInfoRecord? = null
        while (rs.peekNextClass() == ColumnInfoRecord::class.java) {
            val cir = rs.next as ColumnInfoRecord
            records.add(cir)
            if (cirPrev != null && CIRComparator.Companion.compareColInfos(cirPrev, cir) > 0) {
                isInOrder = false
            }
            cirPrev = cir
        }
        if (records.size < 1) {
            throw RuntimeException("No column info records found")
        }
        if (!isInOrder) {
            Collections.sort<ColumnInfoRecord?>(records, CIRComparator.instance)
        }
    }

    /**
     * Performs a deep clone of the record
     */
    public override fun clone(): Any {
        val rec = ColumnInfoRecordsAggregate()
        for (k in records.indices) {
            val ci = records.get(k) as ColumnInfoRecord
            rec.records.add(ci.clone() as ColumnInfoRecord)
        }
        return rec
    }

    /**
     * Inserts a column into the aggregate (at the end of the list).
     */
    fun insertColumn(col: ColumnInfoRecord?) {
        records.add(col)
        Collections.sort<ColumnInfoRecord?>(records, CIRComparator.instance)
    }

    /**
     * Inserts a column into the aggregate (at the position specified by
     * `idx`.
     */
    private fun insertColumn(idx: Int, col: ColumnInfoRecord?) {
        records.add(idx, col)
    }

    val numColumns: Int
        get() = records.size

    override fun visitContainedRecords(rv: RecordVisitor) {
        val nItems = records.size
        if (nItems < 1) {
            return
        }
        var cirPrev: ColumnInfoRecord? = null
        for (i in 0..<nItems) {
            val cir = records.get(i) as ColumnInfoRecord
            rv.visitRecord(cir)
            if (cirPrev != null && CIRComparator.compareColInfos(cirPrev, cir) > 0) {
                // Excel probably wouldn't mind, but there is much logic in this class
                // that assumes the column info records are kept in order
                throw RuntimeException("Column info records are out of order")
            }
            cirPrev = cir
        }
    }

    private fun findStartOfColumnOutlineGroup(pIdx: Int): Int {
        // Find the start of the group.
        var columnInfo = records.get(pIdx) as ColumnInfoRecord
        val level = columnInfo.outlineLevel
        var idx = pIdx
        while (idx != 0) {
            val prevColumnInfo = records.get(idx - 1) as ColumnInfoRecord
            if (!prevColumnInfo.isAdjacentBefore(columnInfo)) {
                break
            }
            if (prevColumnInfo.outlineLevel < level) {
                break
            }
            idx--
            columnInfo = prevColumnInfo
        }

        return idx
    }

    private fun findEndOfColumnOutlineGroup(colInfoIndex: Int): Int {
        // Find the end of the group.
        var columnInfo = records.get(colInfoIndex) as ColumnInfoRecord
        val level = columnInfo.outlineLevel
        var idx = colInfoIndex
        while (idx < records.size - 1) {
            val nextColumnInfo = records.get(idx + 1) as ColumnInfoRecord
            if (!columnInfo.isAdjacentBefore(nextColumnInfo)) {
                break
            }
            if (nextColumnInfo.outlineLevel < level) {
                break
            }
            idx++
            columnInfo = nextColumnInfo
        }
        return idx
    }

    fun getColInfo(idx: Int): ColumnInfoRecord {
        return records.get(idx) as ColumnInfoRecord
    }

    /**
     * 'Collapsed' state is stored in a single column col info record immediately after the outline group
     * @param idx
     * @return
     */
    private fun isColumnGroupCollapsed(idx: Int): Boolean {
        val endOfOutlineGroupIdx = findEndOfColumnOutlineGroup(idx)
        val nextColInfoIx = endOfOutlineGroupIdx + 1
        if (nextColInfoIx >= records.size) {
            return false
        }
        val nextColInfo = getColInfo(nextColInfoIx)
        if (!getColInfo(endOfOutlineGroupIdx).isAdjacentBefore(nextColInfo)) {
            return false
        }
        return nextColInfo.collapsed
    }


    private fun isColumnGroupHiddenByParent(idx: Int): Boolean {
        // Look out outline details of end
        var endLevel = 0
        var endHidden = false
        val endOfOutlineGroupIdx = findEndOfColumnOutlineGroup(idx)
        if (endOfOutlineGroupIdx < records.size) {
            val nextInfo = getColInfo(endOfOutlineGroupIdx + 1)
            if (getColInfo(endOfOutlineGroupIdx).isAdjacentBefore(nextInfo)) {
                endLevel = nextInfo.outlineLevel
                endHidden = nextInfo.hidden
            }
        }
        // Look out outline details of start
        var startLevel = 0
        var startHidden = false
        val startOfOutlineGroupIdx = findStartOfColumnOutlineGroup(idx)
        if (startOfOutlineGroupIdx > 0) {
            val prevInfo = getColInfo(startOfOutlineGroupIdx - 1)
            if (prevInfo.isAdjacentBefore(getColInfo(startOfOutlineGroupIdx))) {
                startLevel = prevInfo.outlineLevel
                startHidden = prevInfo.hidden
            }
        }
        if (endLevel > startLevel) {
            return endHidden
        }
        return startHidden
    }

    fun collapseColumn(columnIndex: Int) {
        val colInfoIx = findColInfoIdx(columnIndex, 0)
        if (colInfoIx == -1) {
            return
        }

        // Find the start of the group.
        val groupStartColInfoIx = findStartOfColumnOutlineGroup(colInfoIx)
        val columnInfo = getColInfo(groupStartColInfoIx)

        // Hide all the columns until the end of the group
        val lastColIx = setGroupHidden(groupStartColInfoIx, columnInfo.outlineLevel, true)

        // Write collapse field
        setColumn(lastColIx + 1, null, null, null, null, true)
    }

    /**
     * Sets all adjacent columns of the same outline level to the specified hidden status.
     * @param pIdx the col info index of the start of the outline group
     * @return the column index of the last column in the outline group
     */
    private fun setGroupHidden(pIdx: Int, level: Int, hidden: Boolean): Int {
        var idx = pIdx
        var columnInfo = getColInfo(idx)
        while (idx < records.size) {
            columnInfo.hidden = hidden
            if (idx + 1 < records.size) {
                val nextColumnInfo = getColInfo(idx + 1)
                if (!columnInfo.isAdjacentBefore(nextColumnInfo)) {
                    break
                }
                if (nextColumnInfo.outlineLevel < level) {
                    break
                }
                columnInfo = nextColumnInfo
            }
            idx++
        }
        return columnInfo.lastColumn
    }


    fun expandColumn(columnIndex: Int) {
        val idx = findColInfoIdx(columnIndex, 0)
        if (idx == -1) {
            return
        }

        // If it is already expanded do nothing.
        if (!isColumnGroupCollapsed(idx)) {
            return
        }

        // Find the start/end of the group.
        val startIdx = findStartOfColumnOutlineGroup(idx)
        val endIdx = findEndOfColumnOutlineGroup(idx)

        // expand:
        // colapsed bit must be unset
        // hidden bit gets unset _if_ surrounding groups are expanded you can determine
        //   this by looking at the hidden bit of the enclosing group.  You will have
        //   to look at the start and the end of the current group to determine which
        //   is the enclosing group
        // hidden bit only is altered for this outline level.  ie.  don't uncollapse contained groups
        val columnInfo = getColInfo(endIdx)
        if (!isColumnGroupHiddenByParent(idx)) {
            val outlineLevel = columnInfo.outlineLevel
            for (i in startIdx..endIdx) {
                val ci = getColInfo(i)
                if (outlineLevel == ci.outlineLevel) ci.hidden = false
            }
        }

        // Write collapse flag (stored in a single col info record after this outline group)
        setColumn(columnInfo.lastColumn + 1, null, null, null, null, false)
    }

    fun setColumn(
        targetColumnIx: Int, xfIndex: Short?, width: Int?,
        level: Int?, hidden: Boolean?, collapsed: Boolean?
    ) {
        var ci: ColumnInfoRecord? = null
        var k = 0

        k = 0
        while (k < records.size) {
            val tci = records.get(k) as ColumnInfoRecord
            if (tci.containsColumn(targetColumnIx)) {
                ci = tci
                break
            }
            if (tci.firstColumn > targetColumnIx) {
                // call column infos after k are for later columns
                break // exit now so k will be the correct insert pos
            }
            k++
        }

        if (ci == null) {
            // okay so there ISN'T a column info record that covers this column so lets create one!
            val nci = ColumnInfoRecord()

            nci.firstColumn = targetColumnIx
            nci.lastColumn = targetColumnIx
            setColumnInfoFields(nci, xfIndex, width, level, hidden, collapsed)
            insertColumn(k, nci)
            attemptMergeColInfoRecords(k)
            return
        }

        val styleChanged = xfIndex != null && ci.xFIndex != xfIndex.toInt()
        val widthChanged = width != null && ci.columnWidth != width.toShort().toInt()
        val levelChanged = level != null && ci.outlineLevel != level
        val hiddenChanged = hidden != null && ci.hidden != hidden
        val collapsedChanged = collapsed != null && ci.collapsed != collapsed

        val columnChanged =
            styleChanged || widthChanged || levelChanged || hiddenChanged || collapsedChanged
        if (!columnChanged) {
            // do nothing...nothing changed.
            return
        }

        if (ci.firstColumn == targetColumnIx && ci.lastColumn == targetColumnIx) {
            // ColumnInfo ci for a single column, the target column
            setColumnInfoFields(ci, xfIndex, width, level, hidden, collapsed)
            attemptMergeColInfoRecords(k)
            return
        }

        if (ci.firstColumn == targetColumnIx || ci.lastColumn == targetColumnIx) {
            // The target column is at either end of the multi-column ColumnInfo ci
            // we'll just divide the info and create a new one
            if (ci.firstColumn == targetColumnIx) {
                ci.firstColumn = targetColumnIx + 1
            } else {
                ci.lastColumn = targetColumnIx - 1
                k++ // adjust insert pos to insert after
            }
            val nci: ColumnInfoRecord = copyColInfo(ci)

            nci.firstColumn = targetColumnIx
            nci.lastColumn = targetColumnIx
            setColumnInfoFields(nci, xfIndex, width, level, hidden, collapsed)

            insertColumn(k, nci)
            attemptMergeColInfoRecords(k)
        } else {
            //split to 3 records
            val ciStart: ColumnInfoRecord? = ci
            val ciMid: ColumnInfoRecord = copyColInfo(ci)
            val ciEnd: ColumnInfoRecord = copyColInfo(ci)
            val lastcolumn = ci.lastColumn

            ciStart!!.lastColumn = targetColumnIx - 1

            ciMid.firstColumn = targetColumnIx
            ciMid.lastColumn = targetColumnIx
            setColumnInfoFields(ciMid, xfIndex, width, level, hidden, collapsed)
            insertColumn(++k, ciMid)

            ciEnd.firstColumn = targetColumnIx + 1
            ciEnd.lastColumn = lastcolumn
            insertColumn(++k, ciEnd)
            // no need to attemptMergeColInfoRecords because we
            // know both on each side are different
        }
    }

    private fun findColInfoIdx(columnIx: Int, fromColInfoIdx: Int): Int {
        require(columnIx >= 0) { "column parameter out of range: " + columnIx }
        require(fromColInfoIdx >= 0) { "fromIdx parameter out of range: " + fromColInfoIdx }

        for (k in fromColInfoIdx..<records.size) {
            val ci = getColInfo(k)
            if (ci.containsColumn(columnIx)) {
                return k
            }
            if (ci.firstColumn > columnIx) {
                break
            }
        }
        return -1
    }

    /**
     * Attempts to merge the col info record at the specified index
     * with either or both of its neighbours
     */
    private fun attemptMergeColInfoRecords(colInfoIx: Int) {
        val nRecords = records.size
        require(!(colInfoIx < 0 || colInfoIx >= nRecords)) {
            ("colInfoIx " + colInfoIx
                    + " is out of range (0.." + (nRecords - 1) + ")")
        }
        val currentCol = getColInfo(colInfoIx)
        val nextIx = colInfoIx + 1
        if (nextIx < nRecords) {
            if (mergeColInfoRecords(currentCol, getColInfo(nextIx))) {
                records.removeAt(nextIx)
            }
        }
        if (colInfoIx > 0) {
            if (mergeColInfoRecords(getColInfo(colInfoIx - 1), currentCol)) {
                records.removeAt(colInfoIx)
            }
        }
    }

    /**
     * Creates an outline group for the specified columns, by setting the level
     * field for each col info record in the range. [ColumnInfoRecord]s
     * may be created, split or merged as a result of this operation.
     * 
     * @param fromColumnIx
     * group from this column (inclusive)
     * @param toColumnIx
     * group to this column (inclusive)
     * @param indent
     * if `true` the group will be indented by one
     * level, if `false` indenting will be decreased by
     * one level.
     */
    fun groupColumnRange(fromColumnIx: Int, toColumnIx: Int, indent: Boolean) {
        var colInfoSearchStartIdx = 0 // optimization to speed up the search for col infos
        for (i in fromColumnIx..toColumnIx) {
            var level = 1
            val colInfoIdx = findColInfoIdx(i, colInfoSearchStartIdx)
            if (colInfoIdx != -1) {
                level = getColInfo(colInfoIdx).outlineLevel
                if (indent) {
                    level++
                } else {
                    level--
                }
                level = max(0, level)
                level = min(7, level)
                colInfoSearchStartIdx =
                    max(0, colInfoIdx - 1) // -1 just in case this column is collapsed later.
            }
            setColumn(i, null, null, level, null, null)
        }
    }

    /**
     * Finds the <tt>ColumnInfoRecord</tt> which contains the specified columnIndex
     * @param columnIndex index of the column (not the index of the ColumnInfoRecord)
     * @return `null` if no column info found for the specified column
     */
    fun findColumnInfo(columnIndex: Int): ColumnInfoRecord? {
        val nInfos = records.size
        for (i in 0..<nInfos) {
            val ci = getColInfo(i)
            if (ci.containsColumn(columnIndex)) {
                return ci
            }
        }
        return null
    }

    val maxOutlineLevel: Int
        get() {
            var result = 0
            val count = records.size
            for (i in 0..<count) {
                val columnInfoRecord = getColInfo(i)
                result = max(columnInfoRecord.outlineLevel, result)
            }
            return result
        }

    companion object {
        private fun copyColInfo(ci: ColumnInfoRecord): ColumnInfoRecord {
            return ci.clone() as ColumnInfoRecord
        }


        /**
         * Sets all non null fields into the `ci` parameter.
         */
        private fun setColumnInfoFields(
            ci: ColumnInfoRecord, xfStyle: Short?, width: Int?,
            level: Int?, hidden: Boolean?, collapsed: Boolean?
        ) {
            if (xfStyle != null) {
                ci.xFIndex = xfStyle.toInt()
            }
            if (width != null) {
                ci.columnWidth = width
            }
            if (level != null) {
                ci.outlineLevel = level
            }
            if (hidden != null) {
                ci.hidden = hidden
            }
            if (collapsed != null) {
                ci.collapsed = collapsed
            }
        }

        /**
         * merges two column info records (if they are adjacent and have the same formatting, etc)
         * @return `false` if the two column records could not be merged
         */
        private fun mergeColInfoRecords(
            ciA: ColumnInfoRecord,
            ciB: ColumnInfoRecord
        ): Boolean {
            if (ciA.isAdjacentBefore(ciB) && ciA.formatMatches(ciB)) {
                ciA.lastColumn = ciB.lastColumn
                return true
            }
            return false
        }
    }
}
