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
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver.getSingleValue
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.pow

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 */
abstract class AggregateFunction protected constructor() :
    MultiOperandNumericFunction(false, false) {
    private class LargeSmall(private val _isLarge: Boolean) : Fixed2ArgFunction() {
        override fun evaluate(
            srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?,
            arg1: ValueEval?
        ): ValueEval? {
            val dn: Double
            try {
                val ve1 = getSingleValue(arg1, srcRowIndex, srcColumnIndex)
                dn = OperandResolver.coerceValueToDouble(ve1!!)
            } catch (e1: EvaluationException) {
                // all errors in the second arg translate to #VALUE!
                return ErrorEval.VALUE_INVALID
            }
            // weird Excel behaviour on second arg
            if (dn < 1.0) {
                // values between 0.0 and 1.0 result in #NUM!
                return ErrorEval.NUM_ERROR
            }
            // all other values are rounded up to the next integer
            val k = ceil(dn).toInt()

            val result: Double
            try {
                val ds: DoubleArray = ValueCollector.Companion.collectValues(arg0)
                if (k > ds.size) {
                    return ErrorEval.NUM_ERROR
                }
                result = if (_isLarge) StatsLib.kthLargest(ds, k) else StatsLib.kthSmallest(ds, k)
                NumericFunction.Companion.checkValue(result)
            } catch (e: EvaluationException) {
                return e.errorEval
            }

            return NumberEval(result)
        }
    }

    internal class ValueCollector : MultiOperandNumericFunction(false, false) {
        override fun evaluate(values: DoubleArray): Double {
            throw IllegalStateException("should not be called")
        }

        companion object {
            private val instance = ValueCollector()

            @Throws(EvaluationException::class)
            fun collectValues(vararg operands: ValueEval?): DoubleArray {
                return instance.getNumberArray(operands)
            }
        }
    }

    companion object {
        /**
         * Create an instance to use in the [Subtotal] function.
         * 
         * 
         * 
         * If there are other subtotals within argument refs (or nested subtotals),
         * these nested subtotals are ignored to avoid double counting.
         * 
         * 
         * @param   func  the function to wrap
         * @return  wrapped instance. The actual math is delegated to the argument function.
         */
        /*package*/
        fun subtotalInstance(func: Function?): Function {
            val arg = func as AggregateFunction
            return object : AggregateFunction() {
                @Throws(EvaluationException::class)
                override fun evaluate(values: DoubleArray): Double {
                    return arg.evaluate(values)
                }

                /**
                 * ignore nested subtotals.
                 */
                override val isSubtotalCounted: Boolean
                    get() {
                    return false
                }
            }
        }

        val AVEDEV: Function = object : AggregateFunction() {
            override fun evaluate(values: DoubleArray): Double {
                return StatsLib.avedev(values)
            }
        }
        val AVERAGE: Function = object : AggregateFunction() {
            @Throws(EvaluationException::class)
            override fun evaluate(values: DoubleArray): Double {
                if (values.size < 1) {
                    throw EvaluationException(ErrorEval.DIV_ZERO)
                }
                return MathX.average(values)
            }
        }
        val DEVSQ: Function = object : AggregateFunction() {
            override fun evaluate(values: DoubleArray): Double {
                return StatsLib.devsq(values)
            }
        }
        val LARGE: Function = LargeSmall(true)
        val MAX: Function = object : AggregateFunction() {
            override fun evaluate(values: DoubleArray): Double {
                return if (values.size > 0) MathX.max(values) else 0.0
            }
        }
        val MEDIAN: Function = object : AggregateFunction() {
            override fun evaluate(values: DoubleArray): Double {
                return StatsLib.median(values)
            }
        }
        val MIN: Function = object : AggregateFunction() {
            override fun evaluate(values: DoubleArray): Double {
                return if (values.size > 0) MathX.min(values) else 0.0
            }
        }
        val PRODUCT: Function = object : AggregateFunction() {
            override fun evaluate(values: DoubleArray): Double {
                return MathX.product(values)
            }
        }
        val SMALL: Function = LargeSmall(false)
        val STDEV: Function = object : AggregateFunction() {
            @Throws(EvaluationException::class)
            override fun evaluate(values: DoubleArray): Double {
                if (values.size < 1) {
                    throw EvaluationException(ErrorEval.DIV_ZERO)
                }
                return StatsLib.stdev(values)
            }
        }
        val SUM: Function = object : AggregateFunction() {
            override fun evaluate(values: DoubleArray): Double {
                return MathX.sum(values)
            }
        }
        val SUMSQ: Function = object : AggregateFunction() {
            override fun evaluate(values: DoubleArray): Double {
                return MathX.sumsq(values)
            }
        }
        val VAR: Function = object : AggregateFunction() {
            @Throws(EvaluationException::class)
            override fun evaluate(values: DoubleArray): Double {
                if (values.size < 1) {
                    throw EvaluationException(ErrorEval.DIV_ZERO)
                }
                return StatsLib.`var`(values)
            }
        }
        val VARP: Function = object : AggregateFunction() {
            @Throws(EvaluationException::class)
            override fun evaluate(values: DoubleArray): Double {
                if (values.size < 1) {
                    throw EvaluationException(ErrorEval.DIV_ZERO)
                }
                return StatsLib.varp(values)
            }
        }

        val DB: Function = object : AggregateFunction() {
            @Throws(EvaluationException::class)
            override fun evaluate(values: DoubleArray): Double {
                checkParas(values)

                val length = values.size

                if (length == 4) {
                    return db(values[0], values[1], values[2], values[3], 12.0)
                } else if (length == 5) {
                    return db(values[0], values[1], values[2], values[3], values[4])
                } else {
                    throw EvaluationException(ErrorEval.NA)
                }
            }

            /**
             * 
             * @param values
             * @throws EvaluationException
             */
            @Throws(EvaluationException::class)
            private fun checkParas(values: DoubleArray) {
                val length = values.size

                if (length == 4 || length == 5) {
                    if (values[2] <= 0 || values[3] <= 0 || values[3] - values[2] > 1) {
                        throw EvaluationException(ErrorEval.NA)
                    }

                    if (length == 5 && (values[4] > 12 || values[4] <= 0)) {
                        throw EvaluationException(ErrorEval.NA)
                    }
                } else {
                    throw EvaluationException(ErrorEval.NA)
                }
            }

            /**
             * 
             * @param cost
             * @param salvage
             * @param life
             * @param period need to be integer numeric now, float numeric maybe later.
             * @param month
             * @return
             * @throws EvaluationException
             */
            @Throws(EvaluationException::class)
            private fun db(
                cost: Double,
                salvage: Double,
                life: Double,
                period: Double,
                month: Double
            ): Double {
                var cost = cost
                if (abs(period - period.toInt()) > 0.001) {
                    throw EvaluationException(ErrorEval.NA)
                }


                //rate = 1 - ((salvage / cost) ^ (1 / life))，保留 3 位小数
                var rate = 1 - (salvage / cost).pow(1 / life)
                rate = Math.round(rate.toFloat() * 1000) / 1000.0

                if (abs(period - 1) < 0.001) {
                    //cost * rate * month / 12
                    return cost * rate * month / 12
                } else {
                    /**
                     * (cost - 前期折旧总值 ) * rate
                     */
                    var d = cost * rate * month / 12
                    cost -= d

                    if (period <= life) {
                        var i = 2
                        while (i <= period) {
                            d = cost * rate
                            cost -= d
                            i++
                        }
                        return d
                    } else if (period - life <= 1) {
                        //last
                        if (abs(month - 12) < 0.001) {
                            return 0.0
                        } else {
                            var i = 2
                            while (i <= life) {
                                d = cost * rate
                                cost -= d
                                i++
                            }
                            return (cost * rate * (12 - month)) / 12
                        }
                    }
                }

                throw EvaluationException(ErrorEval.NA)
            }
        }

        val DDB: Function = object : AggregateFunction() {
            @Throws(EvaluationException::class)
            override fun evaluate(values: DoubleArray): Double {
                checkParas(values)

                val length = values.size

                if (length == 4) {
                    return ddb(values[0], values[1], values[2], values[3], 2.0)
                } else if (length == 5) {
                    return ddb(values[0], values[1], values[2], values[3], values[4])
                } else {
                    throw EvaluationException(ErrorEval.NA)
                }
            }

            /**
             * 
             * @param values
             * @throws EvaluationException
             */
            @Throws(EvaluationException::class)
            private fun checkParas(values: DoubleArray) {
                val length = values.size

                if (length == 4 || length == 5) {
                    if (values[2] <= 0 || values[3] <= 0 || values[3] > values[2]) {
                        throw EvaluationException(ErrorEval.NA)
                    }
                } else {
                    throw EvaluationException(ErrorEval.NA)
                }
            }

            /**
             * 
             * @param cost
             * @param salvage
             * @param life
             * @param period need to be integer numeric now, float numeric maybe later.
             * @param month
             * @return
             * @throws EvaluationException
             */
            @Throws(EvaluationException::class)
            private fun ddb(
                cost: Double,
                salvage: Double,
                life: Double,
                period: Double,
                factor: Double
            ): Double {
                var cost = cost
                if (abs(period - period.toInt()) > 0.001) {
                    throw EvaluationException(ErrorEval.NA)
                }

                /**
                 * Min( (cost - total depreciation from prior periods) * (factor/life),
                 * (cost - salvage - total depreciation from prior periods) )
                 */
                var rate = factor / life
                rate = Math.round(rate.toFloat() * 1000) / 1000.0

                var i = 2
                var d = min(cost * rate, cost - salvage)

                while (i <= period) {
                    d = min(cost * rate, cost - salvage)
                    cost -= d
                    i++
                }
                return d
            }
        }
    }
}
