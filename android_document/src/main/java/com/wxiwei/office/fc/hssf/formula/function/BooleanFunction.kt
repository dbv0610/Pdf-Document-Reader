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

import com.wxiwei.office.fc.hssf.formula.TwoDEval
import com.wxiwei.office.fc.hssf.formula.eval.BoolEval
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.EvaluationException
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver.getSingleValue
import com.wxiwei.office.fc.hssf.formula.eval.RefEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval


/**
 * Here are the general rules concerning Boolean functions:
 * 
 *  1.  Blanks are ignored (not either true or false) 
 *  1.  Strings are ignored if part of an area ref or cell ref, otherwise they must be 'true' or 'false'
 *  1.  Numbers: 0 is false. Any other number is TRUE 
 *  1.  Areas: *all* cells in area are evaluated according to the above rules
 * 
 * 
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 */
abstract class BooleanFunction : Function {
    override fun evaluate(args: Array<ValueEval?>, srcRow: Int, srcCol: Int): ValueEval? {
        if (args.size < 1) {
            return ErrorEval.VALUE_INVALID
        }
        val boolResult: Boolean
        try {
            boolResult = calculate(args)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
        return BoolEval.valueOf(boolResult)
    }

    @Throws(EvaluationException::class)
    private fun calculate(args: Array<ValueEval?>): Boolean {
        var result = this.initialResultValue
        var atleastOneNonBlank = false

        /*
		 * Note: no short-circuit boolean loop exit because any ErrorEvals will override the result
		 */
        var i = 0
        val iSize = args.size
        while (i < iSize) {
            val arg = args[i]
            if (arg is TwoDEval) {
                val ae = arg
                val height = ae.height
                val width = ae.width
                for (rrIx in 0..<height) {
                    for (rcIx in 0..<width) {
                        val ve = ae.getValue(rrIx, rcIx)
                        val tempVe: Boolean? = OperandResolver.coerceValueToBoolean(ve, true)
                        if (tempVe != null) {
                            result = partialEvaluate(result, tempVe)
                            atleastOneNonBlank = true
                        }
                    }
                }
                i++
                continue
            }
            val tempVe: Boolean?
            if (arg is RefEval) {
                val ve = arg.innerValueEval
                tempVe = OperandResolver.coerceValueToBoolean(ve, true)
            } else {
                tempVe = OperandResolver.coerceValueToBoolean(arg, false)
            }


            if (tempVe != null) {
                result = partialEvaluate(result, tempVe)
                atleastOneNonBlank = true
            }
            i++
        }

        if (!atleastOneNonBlank) {
            throw EvaluationException(ErrorEval.VALUE_INVALID)
        }
        return result
    }


    protected abstract val initialResultValue: Boolean
    protected abstract fun partialEvaluate(
        cumulativeResult: Boolean,
        currentValue: Boolean
    ): Boolean


    companion object {
        val AND: Function = object : BooleanFunction() {
            override val initialResultValue: Boolean
                get() {
                return true
            }

            override fun partialEvaluate(
                cumulativeResult: Boolean,
                currentValue: Boolean
            ): Boolean {
                return cumulativeResult && currentValue
            }
        }
        val OR: Function = object : BooleanFunction() {
            override val initialResultValue: Boolean
                get() {
                return false
            }

            override fun partialEvaluate(
                cumulativeResult: Boolean,
                currentValue: Boolean
            ): Boolean {
                return cumulativeResult || currentValue
            }
        }
        val FALSE: Function = object : Fixed0ArgFunction() {
            override fun evaluate(srcRowIndex: Int, srcColumnIndex: Int): ValueEval {
                return BoolEval.FALSE
            }
        }
        val TRUE: Function = object : Fixed0ArgFunction() {
            override fun evaluate(srcRowIndex: Int, srcColumnIndex: Int): ValueEval {
                return BoolEval.TRUE
            }
        }
        val NOT: Function = object : Fixed1ArgFunction() {
            override fun evaluate(
                srcRowIndex: Int,
                srcColumnIndex: Int,
                arg0: ValueEval?
            ): ValueEval? {
                val boolArgVal: Boolean
                try {
                    val ve = getSingleValue(arg0, srcRowIndex, srcColumnIndex)
                    val b: Boolean? = OperandResolver.coerceValueToBoolean(ve, false)
                    boolArgVal = if (b == null) false else b
                } catch (e: EvaluationException) {
                    return e.errorEval
                }

                return BoolEval.valueOf(!boolArgVal)
            }
        }
    }
}
