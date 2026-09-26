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
import kotlin.math.max
import kotlin.math.min

/**
 * 
 * @author Josh Micich
 */
class RangeEval private constructor() : Fixed2ArgFunction() {
    override fun evaluate(
        srcRowIndex: Int,
        srcColumnIndex: Int,
        arg0: ValueEval?,
        arg1: ValueEval?
    ): ValueEval? {
        try {
            val reA: AreaEval = evaluateRef(arg0)
            val reB: AreaEval = evaluateRef(arg1)
            return resolveRange(reA, reB)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
    }

    companion object {
        val instance: Function = RangeEval()

        /**
         * @return simple rectangular [AreaEval] which fully encloses both areas
         * <tt>aeA</tt> and <tt>aeB</tt>
         */
        private fun resolveRange(aeA: AreaEval, aeB: AreaEval): AreaEval? {
            val aeAfr = aeA.firstRow
            val aeAfc = aeA.firstColumn

            val top = min(aeAfr, aeB.firstRow)
            val bottom = max(aeA.lastRow, aeB.lastRow)
            val left = min(aeAfc, aeB.firstColumn)
            val right = max(aeA.lastColumn, aeB.lastColumn)

            return aeA.offset(top - aeAfr, bottom - aeAfr, left - aeAfc, right - aeAfc)
        }

        @Throws(EvaluationException::class)
        private fun evaluateRef(arg: ValueEval?): AreaEval {
            if (arg is AreaEval) {
                return arg
            }
            if (arg is RefEval) {
                return arg.offset(0, 0, 0, 0)
            }
            if (arg is ErrorEval) {
                throw EvaluationException(arg)
            }
            throw IllegalArgumentException("Unexpected ref arg class (" + arg!!.javaClass.getName() + ")")
        }
    }
}
