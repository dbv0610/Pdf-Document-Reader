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
package com.wxiwei.office.fc.hssf.record.cf

import com.wxiwei.office.fc.ss.util.HSSFCellRangeAddress

/**
 * 
 * @author Dmitriy Kumshayev
 */
object CellRangeUtil {
    const val NO_INTERSECTION: Int = 1
    const val OVERLAP: Int = 2

    /** first range is within the second range  */
    const val INSIDE: Int = 3

    /** first range encloses or is equal to the second  */
    const val ENCLOSES: Int = 4

    /**
     * Intersect this range with the specified range.
     * 
     * @param crB - the specified range
     * @return code which reflects how the specified range is related to this range.<br></br>
     * Possible return codes are:
     * NO_INTERSECTION - the specified range is outside of this range;<br></br>
     * OVERLAP - both ranges partially overlap;<br></br>
     * INSIDE - the specified range is inside of this one<br></br>
     * ENCLOSES - the specified range encloses (possibly exactly the same as) this range<br></br>
     */
    fun intersect(crA: HSSFCellRangeAddress, crB: HSSFCellRangeAddress): Int {
        val firstRow = crB.getFirstRow()
        val lastRow = crB.getLastRow()
        val firstCol = crB.getFirstColumn()
        val lastCol = crB.getLastColumn()

        if (gt(crA.getFirstRow(), lastRow) ||
            lt(crA.getLastRow(), firstRow) ||
            gt(crA.getFirstColumn(), lastCol) ||
            lt(crA.getLastColumn(), firstCol)
        ) {
            return NO_INTERSECTION
        } else if (contains(crA, crB)) {
            return INSIDE
        } else if (contains(crB, crA)) {
            return ENCLOSES
        } else {
            return OVERLAP
        }
    }

    /**
     * Do all possible cell merges between cells of the list so that:<br></br>
     *  * if a cell range is completely inside of another cell range, it gets removed from the list
     *  * if two cells have a shared border, merge them into one bigger cell range
     * @param cellRanges
     * @return updated List of cell ranges
     */
    fun mergeCellRanges(cellRanges: Array<HSSFCellRangeAddress>): Array<HSSFCellRangeAddress> {
        if (cellRanges.size < 1) {
            return cellRanges
        }

        val lst: MutableList<HSSFCellRangeAddress> = ArrayList<HSSFCellRangeAddress>()
        for (cr in cellRanges) lst.add(cr)
        val temp = mergeCellRanges(lst)
        return toArray(temp)
    }

    private fun mergeCellRanges(cellRangeList: MutableList<HSSFCellRangeAddress>): MutableList<HSSFCellRangeAddress> {
        while (cellRangeList.size > 1) {
            var somethingGotMerged = false

            for (i in cellRangeList.indices) {
                val range1 = cellRangeList.get(i) as HSSFCellRangeAddress
                var j = i + 1
                while (j < cellRangeList.size) {
                    val range2 = cellRangeList.get(j) as HSSFCellRangeAddress

                    val mergeResult = mergeRanges(range1, range2)
                    if (mergeResult == null) {
                        j++
                        continue
                    }
                    somethingGotMerged = true
                    // overwrite range1 with first result 
                    cellRangeList.set(i, mergeResult[0])
                    // remove range2
                    cellRangeList.removeAt(j--)
                    // add any extra results beyond the first
                    for (k in 1..<mergeResult.size) {
                        j++
                        cellRangeList.add(j, mergeResult[k])
                    }
                    j++
                }
            }
            if (!somethingGotMerged) {
                break
            }
        }


        return cellRangeList
    }

    /**
     * @return the new range(s) to replace the supplied ones.  `null` if no merge is possible
     */
    private fun mergeRanges(
        range1: HSSFCellRangeAddress,
        range2: HSSFCellRangeAddress
    ): Array<HSSFCellRangeAddress>? {
        val x = intersect(range1, range2)
        when (x) {
            NO_INTERSECTION -> {
                if (hasExactSharedBorder(range1, range2)) {
                    return arrayOf<HSSFCellRangeAddress>(
                        createEnclosingCellRange(
                            range1,
                            range2
                        )!!,
                    )
                }
                // else - No intersection and no shared border: do nothing 
                return null
            }

            OVERLAP -> return resolveRangeOverlap(range1, range2)
            INSIDE ->                // Remove range2, since it is completely inside of range1
                return arrayOf<HSSFCellRangeAddress>(range1)

            ENCLOSES ->                // range2 encloses range1, so replace it with the enclosing one
                return arrayOf<HSSFCellRangeAddress>(range2)
        }
        throw RuntimeException("unexpected intersection result (" + x + ")")
    }

    // TODO - write junit test for this
    fun resolveRangeOverlap(
        rangeA: HSSFCellRangeAddress,
        rangeB: HSSFCellRangeAddress
    ): Array<HSSFCellRangeAddress>? {
        if (rangeA.isFullColumnRange()) {
            if (rangeA.isFullRowRange()) {
                // Excel seems to leave these unresolved
                return null
            }
            return sliceUp(rangeA, rangeB)
        }
        if (rangeA.isFullRowRange()) {
            if (rangeB.isFullColumnRange()) {
                // Excel seems to leave these unresolved
                return null
            }
            return sliceUp(rangeA, rangeB)
        }
        if (rangeB.isFullColumnRange()) {
            return sliceUp(rangeB, rangeA)
        }
        if (rangeB.isFullRowRange()) {
            return sliceUp(rangeB, rangeA)
        }
        return sliceUp(rangeA, rangeB)
    }

    /**
     * @param crB never a full row or full column range
     * @return an array including **this** <tt>CellRange</tt> and all parts of <tt>range</tt>
     * outside of this range
     */
    private fun sliceUp(
        crA: HSSFCellRangeAddress,
        crB: HSSFCellRangeAddress
    ): Array<HSSFCellRangeAddress> {
        var temp: MutableList<HSSFCellRangeAddress> = ArrayList<HSSFCellRangeAddress>()


        // Chop up range horizontally and vertically
        temp.add(crB)
        if (!crA.isFullColumnRange()) {
            temp = cutHorizontally(crA.getFirstRow(), temp)
            temp = cutHorizontally(crA.getLastRow() + 1, temp)
        }
        if (!crA.isFullRowRange()) {
            temp = cutVertically(crA.getFirstColumn(), temp)
            temp = cutVertically(crA.getLastColumn() + 1, temp)
        }
        val crParts = toArray(temp)

        // form result array
        temp.clear()
        temp.add(crA)

        for (i in crParts.indices) {
            val crPart = crParts[i]
            // only include parts that are not enclosed by this
            if (intersect(crA, crPart) != ENCLOSES) {
                temp.add(crPart)
            }
        }
        return toArray(temp)
    }

    private fun cutHorizontally(cutRow: Int, input: MutableList<HSSFCellRangeAddress>): MutableList<HSSFCellRangeAddress> {
        val result: MutableList<HSSFCellRangeAddress> = ArrayList<HSSFCellRangeAddress>()
        val crs = toArray(input)
        for (i in crs.indices) {
            val cr = crs[i]
            if (cr.getFirstRow() < cutRow && cutRow < cr.getLastRow()) {
                result.add(
                    HSSFCellRangeAddress(
                        cr.getFirstRow(),
                        cutRow,
                        cr.getFirstColumn(),
                        cr.getLastColumn()
                    )
                )
                result.add(
                    HSSFCellRangeAddress(
                        cutRow + 1,
                        cr.getLastRow(),
                        cr.getFirstColumn(),
                        cr.getLastColumn()
                    )
                )
            } else {
                result.add(cr)
            }
        }
        return result
    }

    private fun cutVertically(cutColumn: Int, input: MutableList<HSSFCellRangeAddress>): MutableList<HSSFCellRangeAddress> {
        val result: MutableList<HSSFCellRangeAddress> = ArrayList<HSSFCellRangeAddress>()
        val crs = toArray(input)
        for (i in crs.indices) {
            val cr = crs[i]
            if (cr.getFirstColumn() < cutColumn && cutColumn < cr.getLastColumn()) {
                result.add(
                    HSSFCellRangeAddress(
                        cr.getFirstRow(),
                        cr.getLastRow(),
                        cr.getFirstColumn(),
                        cutColumn
                    )
                )
                result.add(
                    HSSFCellRangeAddress(
                        cr.getFirstRow(),
                        cr.getLastRow(),
                        cutColumn + 1,
                        cr.getLastColumn()
                    )
                )
            } else {
                result.add(cr)
            }
        }
        return result
    }


    private fun toArray(temp: MutableList<HSSFCellRangeAddress>): Array<HSSFCellRangeAddress> {
        return temp.toTypedArray()
    }


    /**
     * Check if the specified range is located inside of this cell range.
     * 
     * @param crB
     * @return true if this cell range contains the argument range inside if it's area
     */
    fun contains(crA: HSSFCellRangeAddress, crB: HSSFCellRangeAddress): Boolean {
        val firstRow = crB.getFirstRow()
        val lastRow = crB.getLastRow()
        val firstCol = crB.getFirstColumn()
        val lastCol = crB.getLastColumn()
        return le(crA.getFirstRow(), firstRow) && ge(crA.getLastRow(), lastRow)
                && le(crA.getFirstColumn(), firstCol) && ge(crA.getLastColumn(), lastCol)
    }

    /**
     * Check if the specified cell range has a shared border with the current range.
     * 
     * @return `true` if the ranges have a complete shared border (i.e.
     * the two ranges together make a simple rectangular region.
     */
    fun hasExactSharedBorder(crA: HSSFCellRangeAddress, crB: HSSFCellRangeAddress): Boolean {
        val oFirstRow = crB.getFirstRow()
        val oLastRow = crB.getLastRow()
        val oFirstCol = crB.getFirstColumn()
        val oLastCol = crB.getLastColumn()

        if (crA.getFirstRow() > 0 && crA.getFirstRow() - 1 == oLastRow ||
            oFirstRow > 0 && oFirstRow - 1 == crA.getLastRow()
        ) {
            // ranges have a horizontal border in common
            // make sure columns are identical:
            return crA.getFirstColumn() == oFirstCol && crA.getLastColumn() == oLastCol
        }

        if (crA.getFirstColumn() > 0 && crA.getFirstColumn() - 1 == oLastCol ||
            oFirstCol > 0 && crA.getLastColumn() == oFirstCol - 1
        ) {
            // ranges have a vertical border in common
            // make sure rows are identical:
            return crA.getFirstRow() == oFirstRow && crA.getLastRow() == oLastRow
        }
        return false
    }

    /**
     * Create an enclosing CellRange for the two cell ranges.
     * 
     * @return enclosing CellRange
     */
    @JvmStatic
    fun createEnclosingCellRange(
        crA: HSSFCellRangeAddress,
        crB: HSSFCellRangeAddress?
    ): HSSFCellRangeAddress? {
        if (crB == null) {
            return crA.copy()
        }

        return HSSFCellRangeAddress(
            if (lt(crB.getFirstRow(), crA.getFirstRow())) crB.getFirstRow() else crA.getFirstRow(),
            if (gt(crB.getLastRow(), crA.getLastRow())) crB.getLastRow() else crA.getLastRow(),
            if (lt(
                    crB.getFirstColumn(),
                    crA.getFirstColumn()
                )
            ) crB.getFirstColumn() else crA.getFirstColumn(),
            if (gt(
                    crB.getLastColumn(),
                    crA.getLastColumn()
                )
            ) crB.getLastColumn() else crA.getLastColumn()
        )
    }

    /**
     * @return true if a < b
     */
    private fun lt(a: Int, b: Int): Boolean {
        return if (a == -1) false else (if (b == -1) true else a < b)
    }

    /**
     * @return true if a <= b
     */
    private fun le(a: Int, b: Int): Boolean {
        return a == b || lt(a, b)
    }

    /**
     * @return true if a > b
     */
    private fun gt(a: Int, b: Int): Boolean {
        return lt(b, a)
    }

    /**
     * @return true if a >= b
     */
    private fun ge(a: Int, b: Int): Boolean {
        return !lt(a, b)
    }
}
