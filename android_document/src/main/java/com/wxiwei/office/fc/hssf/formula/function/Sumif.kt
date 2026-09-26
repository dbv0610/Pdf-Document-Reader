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

import com.wxiwei.office.fc.hssf.formula.eval.AreaEval
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.EvaluationException
import com.wxiwei.office.fc.hssf.formula.eval.NumberEval
import com.wxiwei.office.fc.hssf.formula.eval.RefEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.fc.hssf.formula.function.CountUtils.I_MatchPredicate


/**
 * Implementation for the Excel function SUMIF
 *
 *
 * 
 * Syntax : <br></br>
 * SUMIF ( **range**, **criteria**, sum_range ) <br></br>
 * <table border="0" cellpadding="1" cellspacing="0" summary="Parameter descriptions">
 * <tr><th>range</th><td>The range over which criteria is applied.  Also used for addend values when the third parameter is not present</td></tr>
 * <tr><th>criteria</th><td>The value or expression used to filter rows from **range**</td></tr>
 * <tr><th>sum_range</th><td>Locates the top-left corner of the corresponding range of addends - values to be added (after being selected by the criteria)</td></tr>
</table> * <br></br>
 * 
 * @author Josh Micich
 */
class Sumif : Var2or3ArgFunction() {
    override fun evaluate(
        srcRowIndex: Int,
        srcColumnIndex: Int,
        arg0: ValueEval?,
        arg1: ValueEval?
    ): ValueEval? {
        val aeRange: AreaEval?
        try {
            aeRange = convertRangeArg(arg0)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
        return Companion.eval(srcRowIndex, srcColumnIndex, arg1, aeRange!!, aeRange)
    }

    override fun evaluate(
        srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?, arg1: ValueEval?,
        arg2: ValueEval?
    ): ValueEval? {
        val aeRange: AreaEval?
        val aeSum: AreaEval?
        try {
            aeRange = convertRangeArg(arg0)
            aeSum = Companion.createSumRange(arg2, aeRange!!)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
        return Companion.eval(srcRowIndex, srcColumnIndex, arg1, aeRange, aeSum!!)
    }

    companion object {
        private fun eval(
            srcRowIndex: Int, srcColumnIndex: Int, arg1: ValueEval?, aeRange: AreaEval,
            aeSum: AreaEval
        ): ValueEval {
            // TODO - junit to prove last arg must be srcColumnIndex and not srcRowIndex
            val mp: I_MatchPredicate? =
                Countif.Companion.createCriteriaPredicate(arg1, srcRowIndex, srcColumnIndex)
            val result: Double = Companion.sumMatchingCells(aeRange, mp!!, aeSum)
            return NumberEval(result)
        }

        private fun sumMatchingCells(
            aeRange: AreaEval,
            mp: I_MatchPredicate,
            aeSum: AreaEval
        ): Double {
            val height = aeRange.height
            val width = aeRange.width

            var result = 0.0
            for (r in 0..<height) {
                for (c in 0..<width) {
                    result += accumulate(aeRange, mp, aeSum, r, c)
                }
            }
            return result
        }

        private fun accumulate(
            aeRange: AreaEval, mp: I_MatchPredicate, aeSum: AreaEval, relRowIndex: Int,
            relColIndex: Int
        ): Double {
            if (!mp.matches(aeRange.getRelativeValue(relRowIndex, relColIndex))) {
                return 0.0
            }
            val addend = aeSum.getRelativeValue(relRowIndex, relColIndex)
            if (addend is NumberEval) {
                return addend.numberValue
            }
            // everything else (including string and boolean values) counts as zero
            return 0.0
        }

        /**
         * @return a range of the same dimensions as aeRange using eval to define the top left corner.
         * @throws EvaluationException if eval is not a reference
         */
        @Throws(EvaluationException::class)
        private fun createSumRange(eval: ValueEval?, aeRange: AreaEval): AreaEval? {
            if (eval is AreaEval) {
                return eval.offset(0, aeRange.height - 1, 0, aeRange.width - 1)
            }
            if (eval is RefEval) {
                return eval.offset(0, aeRange.height - 1, 0, aeRange.width - 1)
            }
            throw EvaluationException(ErrorEval.VALUE_INVALID)
        }

        @Throws(EvaluationException::class)
        private fun convertRangeArg(eval: ValueEval?): AreaEval? {
            if (eval is AreaEval) {
                return eval
            }
            if (eval is RefEval) {
                return eval.offset(0, 0, 0, 0)
            }
            throw EvaluationException(ErrorEval.VALUE_INVALID)
        }
    }
}
