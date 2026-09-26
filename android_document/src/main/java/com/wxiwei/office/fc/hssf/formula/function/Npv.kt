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

import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.EvaluationException
import com.wxiwei.office.fc.hssf.formula.eval.NumberEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval


/**
 * Calculates the net present value of an investment by using a discount rate
 * and a series of future payments (negative values) and income (positive
 * values). Minimum 2 arguments, first arg is the rate of discount over the
 * length of one period others up to 254 arguments representing the payments and
 * income.
 * 
 * @author SPetrakovsky
 * @author Marcel May
 */
class Npv : Function {
    override fun evaluate(
        args: Array<ValueEval?>,
        srcRowIndex: Int,
        srcColumnIndex: Int
    ): ValueEval? {
        val nArgs = args.size
        if (nArgs < 2) {
            return ErrorEval.VALUE_INVALID
        }

        try {
            val rate: Double = NumericFunction.Companion.singleOperandEvaluate(
                args[0],
                srcRowIndex,
                srcColumnIndex
            )
            // convert tail arguments into an array of doubles
            val vargs = arrayOfNulls<ValueEval>(args.size - 1)
            System.arraycopy(args, 1, vargs, 0, vargs.size)
            val values: DoubleArray =
                AggregateFunction.ValueCollector.Companion.collectValues(*vargs)

            val result = FinanceLib.npv(rate, values)
            NumericFunction.Companion.checkValue(result)
            return NumberEval(result)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
    }
}
