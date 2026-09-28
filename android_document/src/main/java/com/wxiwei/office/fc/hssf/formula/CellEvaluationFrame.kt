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
package com.wxiwei.office.fc.hssf.formula

import com.wxiwei.office.fc.hssf.formula.eval.ValueEval

/**
 * Stores details about the current evaluation of a cell.<br></br>
 */
internal class CellEvaluationFrame(private val _cce: FormulaCellCacheEntry) {
    private val _sensitiveInputCells: MutableSet<CellCacheEntry>
    private var _usedBlankCellGroup: FormulaUsedBlankCellSet? = null

    init {
        _sensitiveInputCells = HashSet<CellCacheEntry>()
    }

    val cCE: CellCacheEntry
        get() = _cce

    override fun toString(): String {
        val sb = StringBuffer(64)
        sb.append(javaClass.getName()).append(" [")
        sb.append("]")
        return sb.toString()
    }

    /**
     * @param inputCell a cell directly used by the formula of this evaluation frame
     */
    fun addSensitiveInputCell(inputCell: CellCacheEntry) {
        _sensitiveInputCells.add(inputCell)
    }

    private val sensitiveInputCells: Array<CellCacheEntry>
        /**
         * @return never `null`, (possibly empty) array of all cells directly used while
         * evaluating the formula of this frame.
         */
        get() {
            val nItems = _sensitiveInputCells.size
            if (nItems < 1) {
                return CellCacheEntry.Companion.EMPTY_ARRAY
            }
            return _sensitiveInputCells.toTypedArray()
        }

    fun addUsedBlankCell(bookIndex: Int, sheetIndex: Int, rowIndex: Int, columnIndex: Int) {
        if (_usedBlankCellGroup == null) {
            _usedBlankCellGroup = FormulaUsedBlankCellSet()
        }
        _usedBlankCellGroup!!.addCell(bookIndex, sheetIndex, rowIndex, columnIndex)
    }

    fun updateFormulaResult(result: ValueEval?) {
        _cce.updateFormulaResult(result, this.sensitiveInputCells, _usedBlankCellGroup)
    }
}
