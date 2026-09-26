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
package com.wxiwei.office.fc.hssf.formula

import com.wxiwei.office.fc.hssf.formula.FormulaUsedBlankCellSet.BookSheetKey
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval

/**
 * Stores the cached result of a formula evaluation, along with the set of sensitive input cells
 * 
 * @author Josh Micich
 */
internal class FormulaCellCacheEntry : CellCacheEntry() {
    /**
     * Cells 'used' in the current evaluation of the formula corresponding to this cache entry
     * 
     * If any of the following cells change, this cache entry needs to be cleared
     */
    private var _sensitiveInputCells: Array<CellCacheEntry>? = null

    private var _usedBlankCellGroup: FormulaUsedBlankCellSet? = null

    val isInputSensitive: Boolean
        get() {
            if (_sensitiveInputCells != null) {
                if (_sensitiveInputCells!!.size > 0) {
                    return true
                }
            }
            return if (_usedBlankCellGroup == null) false else !_usedBlankCellGroup!!.isEmpty
        }

    fun setSensitiveInputCells(sensitiveInputCells: Array<CellCacheEntry>?) {
        // need to tell all cells that were previously used, but no longer are, 
        // that they are not consumed by this cell any more
        changeConsumingCells(if (sensitiveInputCells == null) CellCacheEntry.Companion.EMPTY_ARRAY else sensitiveInputCells)
        _sensitiveInputCells = sensitiveInputCells
    }

    fun clearFormulaEntry() {
        val usedCells = _sensitiveInputCells
        if (usedCells != null) {
            for (i in usedCells.indices.reversed()) {
                usedCells[i].clearConsumingCell(this)
            }
        }
        _sensitiveInputCells = null
        clearValue()
    }

    private fun changeConsumingCells(usedCells: Array<CellCacheEntry>) {
        val prevUsedCells = _sensitiveInputCells
        val nUsed = usedCells.size
        for (i in 0..<nUsed) {
            usedCells[i].addConsumingCell(this)
        }
        if (prevUsedCells == null) {
            return
        }
        val nPrevUsed = prevUsedCells.size
        if (nPrevUsed < 1) {
            return
        }
        val usedSet: MutableSet<CellCacheEntry?>?
        if (nUsed < 1) {
            usedSet = mutableSetOf<CellCacheEntry?>()
        } else {
            usedSet = HashSet<CellCacheEntry?>(nUsed * 3 / 2)
            for (i in 0..<nUsed) {
                usedSet.add(usedCells[i])
            }
        }
        for (i in 0..<nPrevUsed) {
            val prevUsed = prevUsedCells[i]
            if (!usedSet.contains(prevUsed)) {
                // previously was used by cellLoc, but not anymore
                prevUsed.clearConsumingCell(this)
            }
        }
    }

    fun updateFormulaResult(
        result: ValueEval?,
        sensitiveInputCells: Array<CellCacheEntry>?,
        usedBlankAreas: FormulaUsedBlankCellSet?
    ) {
        updateValue(result)
        setSensitiveInputCells(sensitiveInputCells)
        _usedBlankCellGroup = usedBlankAreas
    }

    fun notifyUpdatedBlankCell(
        bsk: BookSheetKey?,
        rowIndex: Int,
        columnIndex: Int,
        evaluationListener: IEvaluationListener?
    ) {
        if (_usedBlankCellGroup != null) {
            if (_usedBlankCellGroup!!.containsCell(bsk, rowIndex, columnIndex)) {
                clearFormulaEntry()
                recurseClearCachedFormulaResults(evaluationListener)
            }
        }
    }
}
