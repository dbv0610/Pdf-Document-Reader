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
import com.wxiwei.office.fc.hssf.formula.ptg.FuncVarPtg
import com.wxiwei.office.fc.ss.usermodel.ICell


/**
 * 
 * 
 * @author Josh Micich
 */
internal class SheetRefEvaluator(
    bookEvaluator: WorkbookEvaluator,
    tracker: EvaluationTracker,
    sheetIndex: Int
) {
    private val _bookEvaluator: WorkbookEvaluator
    private val _tracker: EvaluationTracker
    private val _sheetIndex: Int
    private var _sheet: EvaluationSheet? = null

    init {
        require(sheetIndex >= 0) { "Invalid sheetIndex: " + sheetIndex + "." }
        _bookEvaluator = bookEvaluator
        _tracker = tracker
        _sheetIndex = sheetIndex
    }

    val sheetName: String?
        get() = _bookEvaluator.getSheetName(_sheetIndex)

    fun getEvalForCell(rowIndex: Int, columnIndex: Int): ValueEval? {
        return _bookEvaluator.evaluateReference(
            this.sheet!!,
            _sheetIndex,
            rowIndex,
            columnIndex,
            _tracker
        )
    }

    private val sheet: EvaluationSheet?
        get() {
            if (_sheet == null) {
                _sheet = _bookEvaluator.getSheet(_sheetIndex)
            }
            return _sheet
        }

    /**
     * @return  whether cell at rowIndex and columnIndex is a subtotal
     * @see com.wxiwei.office.fc.hssf.formula.function.Subtotal
     */
    fun isSubTotal(rowIndex: Int, columnIndex: Int): Boolean {
        var subtotal = false
        val cell = this.sheet!!.getCell(rowIndex, columnIndex)
        if (cell != null && cell.cellType == ICell.CELL_TYPE_FORMULA) {
            val wb = _bookEvaluator.workbook
            for (ptg in wb.getFormulaTokens(cell)!!) {
                if (ptg is FuncVarPtg) {
                    val f = ptg
                    if ("SUBTOTAL" == f.name) {
                        subtotal = true
                        break
                    }
                }
            }
        }
        return subtotal
    }
}
