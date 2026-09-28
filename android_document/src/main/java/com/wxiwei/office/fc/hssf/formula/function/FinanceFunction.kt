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
package com.wxiwei.office.fc.hssf.formula.function

import com.wxiwei.office.fc.hssf.formula.eval.BoolEval
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.EvaluationException
import com.wxiwei.office.fc.hssf.formula.eval.NumberEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 */
abstract class FinanceFunction protected constructor() : Function3Arg, Function4Arg {
    override fun evaluate(
        srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?, arg1: ValueEval?,
        arg2: ValueEval?
    ): ValueEval? {
        return evaluate(srcRowIndex, srcColumnIndex, arg0, arg1, arg2, DEFAULT_ARG3)
    }

    override fun evaluate(
        srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?, arg1: ValueEval?,
        arg2: ValueEval?, arg3: ValueEval?
    ): ValueEval? {
        return evaluate(srcRowIndex, srcColumnIndex, arg0, arg1, arg2, arg3, DEFAULT_ARG4)
    }

    fun evaluate(
        srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?, arg1: ValueEval?,
        arg2: ValueEval?, arg3: ValueEval?, arg4: ValueEval?
    ): ValueEval? {
        val result: Double
        try {
            val d0: Double =
                NumericFunction.Companion.singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex)
            val d1: Double =
                NumericFunction.Companion.singleOperandEvaluate(arg1, srcRowIndex, srcColumnIndex)
            val d2: Double =
                NumericFunction.Companion.singleOperandEvaluate(arg2, srcRowIndex, srcColumnIndex)
            val d3: Double =
                NumericFunction.Companion.singleOperandEvaluate(arg3, srcRowIndex, srcColumnIndex)
            val d4: Double =
                NumericFunction.Companion.singleOperandEvaluate(arg4, srcRowIndex, srcColumnIndex)
            result = evaluate(d0, d1, d2, d3, d4 != 0.0)
            NumericFunction.Companion.checkValue(result)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
        return NumberEval(result)
    }

    override fun evaluate(
        args: Array<ValueEval?>,
        srcRowIndex: Int,
        srcColumnIndex: Int
    ): ValueEval? {
        when (args.size) {
            3 -> return evaluate(
                srcRowIndex,
                srcColumnIndex,
                args[0],
                args[1],
                args[2],
                DEFAULT_ARG3,
                DEFAULT_ARG4
            )

            4 -> return evaluate(
                srcRowIndex,
                srcColumnIndex,
                args[0],
                args[1],
                args[2],
                args[3],
                DEFAULT_ARG4
            )

            5 -> return evaluate(
                srcRowIndex,
                srcColumnIndex,
                args[0],
                args[1],
                args[2],
                args[3],
                args[4]
            )
        }
        return ErrorEval.VALUE_INVALID
    }

    @Throws(EvaluationException::class)
    protected fun evaluate(ds: DoubleArray): Double {
        // All finance functions have 3 to 5 args, first 4 are numbers, last is boolean
        // default for last 2 args are 0.0 and false
        // Text boolean literals are not valid for the last arg

        var arg3 = 0.0
        var arg4 = 0.0

        when (ds.size) {
            5 -> {
                arg4 = ds[4]
                arg3 = ds[3]
            }

            4 -> arg3 = ds[3]
            3 -> {}
            else -> throw IllegalStateException("Wrong number of arguments")
        }
        return evaluate(ds[0], ds[1], ds[2], arg3, arg4 != 0.0)
    }

    @Throws(EvaluationException::class)
    protected abstract fun evaluate(
        rate: Double,
        arg1: Double,
        arg2: Double,
        arg3: Double,
        type: Boolean
    ): Double


    companion object {
        private val DEFAULT_ARG3: ValueEval = NumberEval.ZERO
        private val DEFAULT_ARG4: ValueEval = BoolEval.FALSE


        val FV: Function = object : FinanceFunction() {
            override fun evaluate(
                rate: Double,
                arg1: Double,
                arg2: Double,
                arg3: Double,
                type: Boolean
            ): Double {
                return FinanceLib.fv(rate, arg1, arg2, arg3, type)
            }
        }
        val NPER: Function = object : FinanceFunction() {
            override fun evaluate(
                rate: Double,
                arg1: Double,
                arg2: Double,
                arg3: Double,
                type: Boolean
            ): Double {
                return FinanceLib.nper(rate, arg1, arg2, arg3, type)
            }
        }
        val PMT: Function = object : FinanceFunction() {
            override fun evaluate(
                rate: Double,
                arg1: Double,
                arg2: Double,
                arg3: Double,
                type: Boolean
            ): Double {
                return FinanceLib.pmt(rate, arg1, arg2, arg3, type)
            }
        }
        val PV: Function = object : FinanceFunction() {
            override fun evaluate(
                rate: Double,
                arg1: Double,
                arg2: Double,
                arg3: Double,
                type: Boolean
            ): Double {
                return FinanceLib.pv(rate, arg1, arg2, arg3, type)
            }
        }
    }
}
