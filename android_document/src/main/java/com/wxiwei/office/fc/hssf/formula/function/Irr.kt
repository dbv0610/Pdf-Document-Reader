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
import kotlin.math.abs
import kotlin.math.pow

/**
 * Calculates the internal rate of return.
 * 
 * Syntax is IRR(values) or IRR(values,guess)
 * 
 * @author Marcel May
 * @author Yegor Kozlov
 * 
 * @see [Wikipedia on IRR](http://en.wikipedia.org/wiki/Internal_rate_of_return.Numerical_solution)
 * 
 * @see [Excel IRR](http://office.microsoft.com/en-us/excel-help/irr-HP005209146.aspx)
 */
class Irr : Function {
    override fun evaluate(
        args: Array<ValueEval?>, srcRowIndex: Int,
        srcColumnIndex: Int
    ): ValueEval? {
        if (args.size == 0 || args.size > 2) {
            // Wrong number of arguments
            return ErrorEval.VALUE_INVALID
        }

        try {
            val values: DoubleArray =
                AggregateFunction.ValueCollector.Companion.collectValues(args[0])
            val guess: Double
            if (args.size == 2) {
                guess = NumericFunction.Companion.singleOperandEvaluate(
                    args[1],
                    srcRowIndex,
                    srcColumnIndex
                )
            } else {
                guess = 0.1
            }
            val result: Double = irr(values, guess)
            NumericFunction.Companion.checkValue(result)
            return NumberEval(result)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
    }

    companion object {
        /**
         * Computes the internal rate of return using an estimated irr of 10 percent.
         * 
         * @param income the income values.
         * @return the irr.
         */
        fun irr(income: DoubleArray): Double {
            return irr(income, 0.1)
        }

        /**
         * Calculates IRR using the Newton-Raphson Method.
         * 
         * 
         * Starting with the guess, the method cycles through the calculation until the result
         * is accurate within 0.00001 percent. If IRR can't find a result that works
         * after 20 tries, the Double.NaN<> is returned.
         * 
         * 
         * 
         * The implementation is inspired by the NewtonSolver from the Apache Commons-Math library,
         * @see [http://commons.apache.org](http://commons.apache.org)
         * 
         * 
         * 
         * @param values        the income values.
         * @param guess         the initial guess of irr.
         * @return the irr value. The method returns `Double.NaN`
         * if the maximum iteration count is exceeded
         * 
         * @see [
         * http://en.wikipedia.org/wiki/Internal_rate_of_return.Numerical_solution](http://en.wikipedia.org/wiki/Internal_rate_of_return.Numerical_solution)
         * 
         * @see [
         * http://en.wikipedia.org/wiki/Newton%27s_method](http://en.wikipedia.org/wiki/Newton%27s_method)
         */
        fun irr(values: DoubleArray, guess: Double): Double {
            val maxIterationCount = 20
            val absoluteAccuracy = 1E-7

            var x0 = guess
            var x1: Double

            var i = 0
            while (i < maxIterationCount) {
                // the value of the function (NPV) and its derivate can be calculated in the same loop

                var fValue = 0.0
                var fDerivative = 0.0
                for (k in values.indices) {
                    fValue += values[k] / (1.0 + x0).pow(k.toDouble())
                    fDerivative += -k * values[k] / (1.0 + x0).pow((k + 1).toDouble())
                }

                // the essense of the Newton-Raphson Method
                x1 = x0 - fValue / fDerivative

                if (abs(x1 - x0) <= absoluteAccuracy) {
                    return x1
                }

                x0 = x1
                ++i
            }
            // maximum number of iterations is exceeded
            return Double.NaN
        }
    }
}
