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

import com.wxiwei.office.fc.hssf.formula.eval.BlankEval
import com.wxiwei.office.fc.hssf.formula.eval.BoolEval
import com.wxiwei.office.fc.hssf.formula.eval.EvaluationException
import com.wxiwei.office.fc.hssf.formula.eval.MissingArgEval
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver.getSingleValue
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval

/**
 * Implementation for the Excel function IF
 * 
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 */
class IfFunc : Var2or3ArgFunction() {
    override fun evaluate(
        srcRowIndex: Int,
        srcColumnIndex: Int,
        arg0: ValueEval?,
        arg1: ValueEval?
    ): ValueEval? {
        val b: Boolean
        try {
            b = evaluateFirstArg(arg0, srcRowIndex, srcColumnIndex)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
        if (b) {
            if (arg1 === MissingArgEval.instance) {
                return BlankEval.instance
            }
            return arg1
        }
        return BoolEval.FALSE
    }

    override fun evaluate(
        srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?, arg1: ValueEval?,
        arg2: ValueEval?
    ): ValueEval? {
        val b: Boolean
        try {
            b = evaluateFirstArg(arg0, srcRowIndex, srcColumnIndex)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
        if (b) {
            if (arg1 === MissingArgEval.instance) {
                return BlankEval.instance
            }
            return arg1
        }
        if (arg2 === MissingArgEval.instance) {
            return BlankEval.instance
        }
        return arg2
    }

    companion object {
        @JvmStatic
        @Throws(EvaluationException::class)
        fun evaluateFirstArg(arg: ValueEval?, srcCellRow: Int, srcCellCol: Int): Boolean {
            val ve = getSingleValue(arg, srcCellRow, srcCellCol)
            val b: Boolean? = OperandResolver.coerceValueToBoolean(ve, false)
            if (b == null) {
                return false
            }
            return b
        }
    }
}
