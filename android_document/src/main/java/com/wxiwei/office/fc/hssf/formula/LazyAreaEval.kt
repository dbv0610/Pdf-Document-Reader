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

import com.wxiwei.office.fc.hssf.formula.eval.AreaEval
import com.wxiwei.office.fc.hssf.formula.eval.AreaEvalBase
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.fc.hssf.formula.ptg.AreaI
import com.wxiwei.office.fc.hssf.formula.ptg.AreaI.OffsetArea
import com.wxiwei.office.fc.ss.util.CellReference


/**
 * 
 * @author Josh Micich
 */
internal class LazyAreaEval : AreaEvalBase {
    private val _evaluator: SheetRefEvaluator

    constructor(ptg: AreaI, evaluator: SheetRefEvaluator) : super(ptg) {
        _evaluator = evaluator
    }

    constructor(
        firstRowIndex: Int, firstColumnIndex: Int, lastRowIndex: Int,
        lastColumnIndex: Int, evaluator: SheetRefEvaluator
    ) : super(firstRowIndex, firstColumnIndex, lastRowIndex, lastColumnIndex) {
        _evaluator = evaluator
    }

    public override fun getRelativeValue(
        relativeRowIndex: Int,
        relativeColumnIndex: Int
    ): ValueEval? {
        val rowIx: Int = (relativeRowIndex + firstRow) and 0xFFFF
        val colIx: Int = (relativeColumnIndex + firstColumn) and 0x00FF

        return _evaluator.getEvalForCell(rowIx, colIx)
    }

    override fun offset(
        relFirstRowIx: Int,
        relLastRowIx: Int,
        relFirstColIx: Int,
        relLastColIx: Int
    ): AreaEval {
        val area: AreaI = OffsetArea(
            firstRow, firstColumn,
            relFirstRowIx, relLastRowIx, relFirstColIx, relLastColIx
        )

        return LazyAreaEval(area, _evaluator)
    }

    override fun getRow(rowIndex: Int): LazyAreaEval {
        require(rowIndex < height) {
            ("Invalid rowIndex " + rowIndex
                    + ".  Allowable range is (0.." + height + ").")
        }
        val absRowIx: Int = firstRow + rowIndex
        return LazyAreaEval(absRowIx, firstColumn, absRowIx, lastColumn, _evaluator)
    }

    override fun getColumn(columnIndex: Int): LazyAreaEval {
        require(columnIndex < width) {
            ("Invalid columnIndex " + columnIndex
                    + ".  Allowable range is (0.." + width + ").")
        }
        val absColIx: Int = firstColumn + columnIndex
        return LazyAreaEval(firstRow, absColIx, lastRow, absColIx, _evaluator)
    }

    override fun toString(): String {
        val crA = CellReference(firstRow, firstColumn)
        val crB = CellReference(lastRow, lastColumn)
        val sb = StringBuffer()
        sb.append(javaClass.getName()).append("[")
        sb.append(_evaluator.sheetName)
        sb.append('!')
        sb.append(crA.formatAsString())
        sb.append(':')
        sb.append(crB.formatAsString())
        sb.append("]")
        return sb.toString()
    }

    /**
     * @return  whether cell at rowIndex and columnIndex is a subtotal
     */
    public override fun isSubTotal(rowIndex: Int, columnIndex: Int): Boolean {
        // delegate the query to the sheet evaluator which has access to internal ptgs
        return _evaluator.isSubTotal(rowIndex, columnIndex)
    }
}
