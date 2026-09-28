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
package com.wxiwei.office.fc.hssf.formula.eval

import com.wxiwei.office.fc.hssf.formula.function.Fixed2ArgFunction
import com.wxiwei.office.fc.hssf.formula.function.Function
import kotlin.Int
import kotlin.Throws
import kotlin.math.pow

/**
 * @author Josh Micich
 */
abstract class TwoOperandNumericOperation : Fixed2ArgFunction() {
    @Throws(EvaluationException::class)
    protected fun singleOperandEvaluate(arg: ValueEval?, srcCellRow: Int, srcCellCol: Int): Double {
        val ve = OperandResolver.getSingleValue(arg, srcCellRow, srcCellCol)
        return OperandResolver.coerceValueToDouble(ve)
    }

    override fun evaluate(
        srcRowIndex: Int,
        srcColumnIndex: Int,
        arg0: ValueEval?,
        arg1: ValueEval?
    ): ValueEval? {
        val result: Double
        try {
            val d0 = singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex)
            val d1 = singleOperandEvaluate(arg1, srcRowIndex, srcColumnIndex)
            result = evaluate(d0, d1)
            if (result == 0.0) { // this '==' matches +0.0 and -0.0
                // Excel converts -0.0 to +0.0 for '*', '/', '%', '+' and '^'
                if (this !is SubtractEvalClass) {
                    return NumberEval.Companion.ZERO
                }
            }
            if (result.isNaN() || result.isInfinite()) {
                return ErrorEval.Companion.NUM_ERROR
            }
        } catch (e: EvaluationException) {
            return e.errorEval
        }
        return NumberEval(result)
    }

    @Throws(EvaluationException::class)
    protected abstract fun evaluate(d0: kotlin.Double, d1: kotlin.Double): kotlin.Double

    private class SubtractEvalClass : TwoOperandNumericOperation() {
        override fun evaluate(d0: kotlin.Double, d1: kotlin.Double): kotlin.Double {
            return d0 - d1
        }
    }

    companion object {
        val AddEval: Function = object : TwoOperandNumericOperation() {
            override fun evaluate(d0: kotlin.Double, d1: kotlin.Double): kotlin.Double {
                return d0 + d1
            }
        }
        val DivideEval: Function = object : TwoOperandNumericOperation() {
            @Throws(EvaluationException::class)
            override fun evaluate(d0: kotlin.Double, d1: kotlin.Double): kotlin.Double {
                if (d1 == 0.0) {
                    throw EvaluationException(ErrorEval.Companion.DIV_ZERO)
                }
                return d0 / d1
            }
        }
        val MultiplyEval: Function = object : TwoOperandNumericOperation() {
            override fun evaluate(d0: kotlin.Double, d1: kotlin.Double): kotlin.Double {
                return d0 * d1
            }
        }
        val PowerEval: Function = object : TwoOperandNumericOperation() {
            override fun evaluate(d0: kotlin.Double, d1: kotlin.Double): kotlin.Double {
                return d0.pow(d1)
            }
        }
        val SubtractEval: Function = SubtractEvalClass()
    }
}
