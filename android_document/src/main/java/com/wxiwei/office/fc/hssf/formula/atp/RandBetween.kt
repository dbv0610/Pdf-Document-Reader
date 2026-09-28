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
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.EvaluationException
import com.wxiwei.office.fc.hssf.formula.eval.NumberEval
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.fc.hssf.formula.function.FreeRefFunction
import kotlin.math.ceil
import kotlin.math.floor


/**
 * Implementation of Excel 'Analysis ToolPak' function RANDBETWEEN()<br></br>
 * 
 * Returns a random integer number between the numbers you specify.
 *
 *
 * 
 * **Syntax**<br></br>
 * **RANDBETWEEN**(**bottom**, **top**)
 *
 *
 * 
 * **bottom** is the smallest integer RANDBETWEEN will return.<br></br>
 * **top** is the largest integer RANDBETWEEN will return.<br></br>
 * 
 * @author Brendan Nolan
 */
internal class RandBetween private constructor() : FreeRefFunction {
    /**
     * Evaluate for RANDBETWEEN(). Must be given two arguments. Bottom must be greater than top.
     * Bottom is rounded up and top value is rounded down. After rounding top has to be set greater
     * than top.
     * 
     * @see FreeRefFunction.evaluate
     */
    override fun evaluate(args: Array<ValueEval?>, ec: OperationEvaluationContext): ValueEval {
        var bottom: Double
        var top: Double

        if (args.size != 2) {
            return ErrorEval.VALUE_INVALID
        }

        try {
            bottom = OperandResolver.coerceValueToDouble(
                OperandResolver.getSingleValue(
                    args[0],
                    ec.rowIndex,
                    ec.columnIndex
                )
            )
            top = OperandResolver.coerceValueToDouble(
                OperandResolver.getSingleValue(
                    args[1],
                    ec.rowIndex,
                    ec.columnIndex
                )
            )
            if (bottom > top) {
                return ErrorEval.NUM_ERROR
            }
        } catch (e: EvaluationException) {
            return ErrorEval.VALUE_INVALID
        }

        bottom = ceil(bottom)
        top = floor(top)

        if (bottom > top) {
            top = bottom
        }

        return NumberEval((bottom + (Math.random() * ((top - bottom) + 1)).toInt()))
    }

    companion object {
        val instance: FreeRefFunction = RandBetween()
    }
}
