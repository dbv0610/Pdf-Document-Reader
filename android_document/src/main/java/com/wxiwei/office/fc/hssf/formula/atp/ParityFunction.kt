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
package com.wxiwei.office.fc.hssf.formula.atp

import com.wxiwei.office.fc.hssf.formula.OperationEvaluationContext
import com.wxiwei.office.fc.hssf.formula.eval.BoolEval
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.EvaluationException
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.fc.hssf.formula.function.FreeRefFunction
import kotlin.math.floor


/**
 * Implementation of Excel 'Analysis ToolPak' function ISEVEN() ISODD()<br></br>
 * 
 * @author Josh Micich
 */
internal class ParityFunction private constructor(private val _desiredParity: Int) :
    FreeRefFunction {
    override fun evaluate(args: Array<ValueEval?>, ec: OperationEvaluationContext): ValueEval? {
        if (args.size != 1) {
            return ErrorEval.VALUE_INVALID
        }

        val `val`: Int
        try {
            `val` = evaluateArgParity(args[0], ec.rowIndex, ec.columnIndex)
        } catch (e: EvaluationException) {
            return e.errorEval
        }

        return BoolEval.valueOf(`val` == _desiredParity)
    }

    companion object {
        val IS_EVEN: FreeRefFunction = ParityFunction(0)
        val IS_ODD: FreeRefFunction = ParityFunction(1)

        @Throws(EvaluationException::class)
        private fun evaluateArgParity(arg: ValueEval?, srcCellRow: Int, srcCellCol: Int): Int {
            val ve = OperandResolver.getSingleValue(arg, srcCellRow, srcCellCol.toShort().toInt())

            var d = OperandResolver.coerceValueToDouble(ve)
            if (d < 0) {
                d = -d
            }
            val v = floor(d).toLong()
            return (v and 0x0001L).toInt()
        }
    }
}
