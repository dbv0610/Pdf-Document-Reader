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

import com.wxiwei.office.fc.hssf.formula.ptg.AreaPtg
import com.wxiwei.office.fc.hssf.formula.ptg.AreaPtgBase
import com.wxiwei.office.fc.hssf.formula.ptg.OperandPtg
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.formula.ptg.RefPtg
import com.wxiwei.office.fc.hssf.formula.ptg.RefPtgBase
import com.wxiwei.office.fc.ss.SpreadsheetVersion

/**
 * Encapsulates logic to convert shared formulaa into non shared equivalent
 */
class SharedFormula(ssVersion: SpreadsheetVersion) {
    private val _columnWrappingMask: Int
    private val _rowWrappingMask: Int

    init {
        _columnWrappingMask = ssVersion.getLastColumnIndex() //"IV" for .xls and  "XFD" for .xlsx
        _rowWrappingMask = ssVersion.getLastRowIndex()
    }

    /**
     * Creates a non shared formula from the shared formula counterpart, i.e.
     * Converts the shared formula into the equivalent [Ptg] array that it would have,
     * were it not shared.
     * 
     * @param ptgs parsed tokens of the shared formula
     * @param formulaRow
     * @param formulaColumn
     */
    fun convertSharedFormulas(ptgs: Array<Ptg?>, formulaRow: Int, formulaColumn: Int): Array<Ptg?> {
        val newPtgStack = arrayOfNulls<Ptg>(ptgs.size)

        for (k in ptgs.indices) {
            var ptg = ptgs[k] ?: continue
            var originalOperandClass: Byte = -1
            if (!ptg.isBaseToken) {
                originalOperandClass = ptg.ptgClass
            }
            if (ptg is RefPtgBase) {
                val refNPtg = ptg
                ptg = RefPtg(
                    fixupRelativeRow(formulaRow, refNPtg.row, refNPtg.isRowRelative),
                    fixupRelativeColumn(
                        formulaColumn,
                        refNPtg.column,
                        refNPtg.isColRelative
                    ),
                    refNPtg.isRowRelative,
                    refNPtg.isColRelative
                )
                ptg.setClass(originalOperandClass)
            } else if (ptg is AreaPtgBase) {
                val areaNPtg = ptg
                ptg = AreaPtg(
                    fixupRelativeRow(
                        formulaRow,
                        areaNPtg.firstRow,
                        areaNPtg.isFirstRowRelative
                    ),
                    fixupRelativeRow(formulaRow, areaNPtg.lastRow, areaNPtg.isLastRowRelative),
                    fixupRelativeColumn(
                        formulaColumn,
                        areaNPtg.firstColumn,
                        areaNPtg.isFirstColRelative
                    ),
                    fixupRelativeColumn(
                        formulaColumn,
                        areaNPtg.lastColumn,
                        areaNPtg.isLastColRelative
                    ),
                    areaNPtg.isFirstRowRelative,
                    areaNPtg.isLastRowRelative,
                    areaNPtg.isFirstColRelative,
                    areaNPtg.isLastColRelative
                )
                ptg.setClass(originalOperandClass)
            } else if (ptg is OperandPtg) {
                // Any subclass of OperandPtg is mutable, so it's safest to not share these instances.
                ptg = ptg.copy()
            } else {
                // all other Ptgs are immutable and can be shared
            }
            newPtgStack[k] = ptg
        }
        return newPtgStack
    }

    private fun fixupRelativeColumn(currentcolumn: Int, column: Int, relative: Boolean): Int {
        if (relative) {
            // mask out upper bits to produce 'wrapping' at the maximum column ("IV" for .xls and  "XFD" for .xlsx)
            return (column + currentcolumn) and _columnWrappingMask
        }
        return column
    }

    private fun fixupRelativeRow(currentrow: Int, row: Int, relative: Boolean): Int {
        if (relative) {
            return (row + currentrow) and _rowWrappingMask
        }
        return row
    }
}
