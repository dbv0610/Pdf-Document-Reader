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

import com.wxiwei.office.fc.hssf.formula.eval.AreaEval
import com.wxiwei.office.fc.hssf.formula.eval.RefEvalBase
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.fc.hssf.formula.ptg.AreaI
import com.wxiwei.office.fc.hssf.formula.ptg.AreaI.OffsetArea
import com.wxiwei.office.fc.ss.util.CellReference


/**
 * 
 * @author Josh Micich
 */
internal class LazyRefEval(rowIndex: Int, columnIndex: Int, sre: SheetRefEvaluator) :
    RefEvalBase(rowIndex, columnIndex) {
    private val _evaluator: SheetRefEvaluator

    init {
        requireNotNull(sre) { "sre must not be null" }
        _evaluator = sre
    }

    override val innerValueEval: ValueEval?
        get() = _evaluator.getEvalForCell(row, column)

    override fun offset(
        relFirstRowIx: Int,
        relLastRowIx: Int,
        relFirstColIx: Int,
        relLastColIx: Int
    ): AreaEval {
        val area: AreaI = OffsetArea(
            row, column,
            relFirstRowIx, relLastRowIx, relFirstColIx, relLastColIx
        )

        return LazyAreaEval(area, _evaluator)
    }

    override fun toString(): String {
        val cr = CellReference(row, column)
        val sb = StringBuffer()
        sb.append(javaClass.getName()).append("[")
        sb.append(_evaluator.sheetName)
        sb.append('!')
        sb.append(cr.formatAsString())
        sb.append("]")
        return sb.toString()
    }
}
