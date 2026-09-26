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
package com.wxiwei.office.fc.hssf.formula.eval

import com.wxiwei.office.fc.hssf.formula.EvaluationCell
import com.wxiwei.office.fc.hssf.formula.EvaluationSheet
import com.wxiwei.office.fc.hssf.formula.EvaluationWorkbook
import com.wxiwei.office.fc.ss.usermodel.Sheet
import com.wxiwei.office.fc.ss.util.CellReference
import java.util.Arrays

/**
 * Represents a sheet being used for forked evaluation.  Initially, objects of this class contain
 * only the cells from the master workbook. By calling [.getOrCreateUpdatableCell],
 * the master cell object is logically replaced with a [ForkedEvaluationCell] instance, which
 * will be used in all subsequent evaluations.
 * 
 * @author Josh Micich
 */
internal class ForkedEvaluationSheet(private val _masterSheet: EvaluationSheet) : EvaluationSheet {
    /**
     * Only cells which have been split are put in this map.  (This has been done to conserve memory).
     */
    private val _sharedCellsByRowCol: MutableMap<RowColKey, ForkedEvaluationCell>

    init {
        _sharedCellsByRowCol = HashMap<RowColKey, ForkedEvaluationCell>()
    }

    override fun getCell(rowIndex: Int, columnIndex: Int): EvaluationCell? {
        val key = RowColKey(rowIndex, columnIndex)

        val result = _sharedCellsByRowCol.get(key)
        if (result == null) {
            return _masterSheet.getCell(rowIndex, columnIndex)
        }
        return result
    }

    fun getOrCreateUpdatableCell(rowIndex: Int, columnIndex: Int): ForkedEvaluationCell {
        val key = RowColKey(rowIndex, columnIndex)

        var result = _sharedCellsByRowCol.get(key)
        if (result == null) {
            val mcell = _masterSheet.getCell(rowIndex, columnIndex)
            if (mcell == null) {
                val cr = CellReference(rowIndex, columnIndex)
                throw UnsupportedOperationException(
                    ("Underlying cell '"
                            + cr.formatAsString() + "' is missing in master sheet.")
                )
            }
            result = ForkedEvaluationCell(this, mcell)
            _sharedCellsByRowCol.put(key, result)
        }
        return result
    }

    fun copyUpdatedCells(sheet: Sheet?) {
        val keys: Array<RowColKey> = _sharedCellsByRowCol.keys.toTypedArray()
        Arrays.sort(keys)
        //		for (int i = 0; i < keys.length; i++) {
//			RowColKey key = keys[i];
//			IRow row = sheet.getRow(key.getRowIndex());
//			if (row == null) {
//				row = sheet.createRow(key.getRowIndex());
//			}
//			ICell destCell = row.getCell(key.getColumnIndex());
//			if (destCell == null) {
//				destCell = row.createCell(key.getColumnIndex());
//			}
//
//			ForkedEvaluationCell srcCell = _sharedCellsByRowCol.get(key);
//			srcCell.copyValue(destCell);
//		}
    }

    fun getSheetIndex(mewb: EvaluationWorkbook): Int {
        return mewb.getSheetIndex(_masterSheet)
    }

    private class RowColKey(val rowIndex: Int, val columnIndex: Int) : Comparable<RowColKey> {
        override fun equals(obj: Any?): Boolean {
            assert(obj is RowColKey) { "these private cache key instances are only compared to themselves" }
            val other: RowColKey = obj as RowColKey
            return this.rowIndex == other.rowIndex && this.columnIndex == other.columnIndex
        }

        override fun hashCode(): Int {
            return this.rowIndex xor this.columnIndex
        }

        override fun compareTo(o: RowColKey): Int {
            val cmp = this.rowIndex - o.rowIndex
            if (cmp != 0) {
                return cmp
            }
            return this.columnIndex - o.columnIndex
        }
    }
}
