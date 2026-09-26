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
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver.getSingleValue
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import kotlin.Array
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.Throws
import kotlin.longArrayOf
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.require
import kotlin.requireNotNull

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * @author Josh Micich
 * @author Stephen Wolke (smwolke at geistig.com)
 */
abstract class NumericFunction : Function {
    override fun evaluate(args: Array<ValueEval?>, srcCellRow: Int, srcCellCol: Int): ValueEval? {
        val result: Double
        try {
            result = eval(args, srcCellRow, srcCellCol)
            checkValue(result)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
        return NumberEval(result)
    }

    @Throws(EvaluationException::class)
    protected abstract fun eval(args: Array<ValueEval?>, srcCellRow: Int, srcCellCol: Int): Double

    /* -------------------------------------------------------------------------- */ // intermediate sub-classes (one-arg, two-arg and multi-arg)
    abstract class OneArg protected constructor() : Fixed1ArgFunction() {
        override fun evaluate(srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?): ValueEval? {
            val result: Double
            try {
                val d: Double = singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex)
                result = evaluate(d)
                checkValue(result)
            } catch (e: EvaluationException) {
                return e.errorEval
            }
            return NumberEval(result)
        }

        @Throws(EvaluationException::class)
        protected fun eval(args: Array<ValueEval?>, srcCellRow: Int, srcCellCol: Int): Double {
            if (args.size != 1) {
                throw EvaluationException(ErrorEval.VALUE_INVALID)
            }
            val d: Double = Companion.singleOperandEvaluate(args[0]!!, srcCellRow, srcCellCol)
            return evaluate(d)
        }

        @Throws(EvaluationException::class)
        protected abstract fun evaluate(d: Double): Double
    }

    abstract class TwoArg protected constructor() : Fixed2ArgFunction() {
        override fun evaluate(
            srcRowIndex: Int,
            srcColumnIndex: Int,
            arg0: ValueEval?,
            arg1: ValueEval?
        ): ValueEval? {
            val result: Double
            try {
                val d0: Double = singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex)
                val d1: Double = singleOperandEvaluate(arg1, srcRowIndex, srcColumnIndex)
                result = evaluate(d0, d1)
                checkValue(result)
            } catch (e: EvaluationException) {
                return e.errorEval
            }
            return NumberEval(result)
        }

        @Throws(EvaluationException::class)
        protected abstract fun evaluate(d0: Double, d1: Double): Double
    }

    /* -------------------------------------------------------------------------- */
    private class Log : Var1or2ArgFunction() {
        override fun evaluate(srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?): ValueEval? {
            val result: Double
            try {
                val d0: Double = singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex)
                result = ln(d0) / LOG_10_TO_BASE_e
                checkValue(result)
            } catch (e: EvaluationException) {
                return e.errorEval
            }
            return NumberEval(result)
        }

        override fun evaluate(
            srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?,
            arg1: ValueEval?
        ): ValueEval? {
            val result: Double
            try {
                val d0: Double = singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex)
                val d1: Double = singleOperandEvaluate(arg1, srcRowIndex, srcColumnIndex)
                val logE = ln(d0)
                val base = d1
                if (base == Math.E) {
                    result = logE
                } else {
                    result = logE / ln(base)
                }
                checkValue(result)
            } catch (e: EvaluationException) {
                return e.errorEval
            }
            return NumberEval(result)
        }
    }

    companion object {
        const val ZERO: Double = 0.0
        const val TEN: Double = 10.0
        val LOG_10_TO_BASE_e: Double = ln(TEN)

        @Throws(EvaluationException::class)
        fun singleOperandEvaluate(arg: ValueEval?, srcRowIndex: Int, srcColumnIndex: Int): Double {
            requireNotNull(arg) { "arg must not be null" }
            val ve = getSingleValue(arg, srcRowIndex, srcColumnIndex)
            val result: Double = OperandResolver.coerceValueToDouble(ve!!)
            checkValue(result)
            return result
        }

        /**
         * @throws EvaluationException (#NUM!) if <tt>result</tt> is <tt>NaN</tt>> or <tt>Infinity</tt>
         */
        @Throws(EvaluationException::class)
        fun checkValue(result: Double) {
            if (result.isNaN() || result.isInfinite()) {
                throw EvaluationException(ErrorEval.NUM_ERROR)
            }
        }

        /* -------------------------------------------------------------------------- */
        val ABS: Function = object : OneArg() {
            override fun evaluate(d: kotlin.Double): kotlin.Double {
                return abs(d)
            }
        }
        val ACOS: Function = object : OneArg() {
            override fun evaluate(d: kotlin.Double): kotlin.Double {
                return acos(d)
            }
        }
        val ACOSH: Function = object : OneArg() {
            override fun evaluate(d: kotlin.Double): kotlin.Double {
                return MathX.acosh(d)
            }
        }
        val ASIN: Function = object : OneArg() {
            override fun evaluate(d: kotlin.Double): kotlin.Double {
                return asin(d)
            }
        }
        val ASINH: Function = object : OneArg() {
            override fun evaluate(d: kotlin.Double): kotlin.Double {
                return MathX.asinh(d)
            }
        }
        val ATAN: Function = object : OneArg() {
            override fun evaluate(d: kotlin.Double): kotlin.Double {
                return atan(d)
            }
        }
        val ATANH: Function = object : OneArg() {
            override fun evaluate(d: kotlin.Double): kotlin.Double {
                return MathX.atanh(d)
            }
        }
        val COS: Function = object : OneArg() {
            override fun evaluate(d: kotlin.Double): kotlin.Double {
                return cos(d)
            }
        }
        val COSH: Function = object : OneArg() {
            override fun evaluate(d: kotlin.Double): kotlin.Double {
                return MathX.cosh(d)
            }
        }
        val DEGREES: Function = object : OneArg() {
            override fun evaluate(d: kotlin.Double): kotlin.Double {
                return Math.toDegrees(d)
            }
        }
        val DOLLAR_ARG2_DEFAULT: NumberEval = NumberEval(2.0)
        val DOLLAR: Function = object : Var1or2ArgFunction() {
            override fun evaluate(
                srcRowIndex: Int,
                srcColumnIndex: Int,
                arg0: ValueEval?
            ): ValueEval? {
                return evaluate(srcRowIndex, srcColumnIndex, arg0, DOLLAR_ARG2_DEFAULT)
            }

            override fun evaluate(
                srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?,
                arg1: ValueEval?
            ): ValueEval? {
                val `val`: kotlin.Double
                val d1: kotlin.Double
                try {
                    `val` = singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex)
                    d1 = singleOperandEvaluate(arg1, srcRowIndex, srcColumnIndex)
                } catch (e: EvaluationException) {
                    return e.errorEval
                }
                // second arg converts to int by truncating toward zero
                val nPlaces = d1.toInt()

                if (nPlaces > 127) {
                    return ErrorEval.VALUE_INVALID
                }


                // TODO - DOLLAR() function impl is NQR
                // result should be StringEval, with leading '$' and thousands separators
                // current junits are asserting incorrect behaviour
                return NumberEval(`val`)
            }
        }
        val EXP: Function = object : OneArg() {
            override fun evaluate(d: kotlin.Double): kotlin.Double {
                return Math.E.pow(d)
            }
        }
        val FACT: Function = object : OneArg() {
            override fun evaluate(d: kotlin.Double): kotlin.Double {
                return MathX.factorial(d.toInt())
            }
        }
        val INT: Function = object : OneArg() {
            override fun evaluate(d: kotlin.Double): kotlin.Double {
                return Math.round(d - 0.5).toDouble()
            }
        }
        val LN: Function = object : OneArg() {
            override fun evaluate(d: kotlin.Double): kotlin.Double {
                return ln(d)
            }
        }
        val LOG10: Function = object : OneArg() {
            override fun evaluate(d: kotlin.Double): kotlin.Double {
                return ln(d) / LOG_10_TO_BASE_e
            }
        }
        val RADIANS: Function = object : OneArg() {
            override fun evaluate(d: kotlin.Double): kotlin.Double {
                return Math.toRadians(d)
            }
        }
        val SIGN: Function = object : OneArg() {
            override fun evaluate(d: kotlin.Double): kotlin.Double {
                return MathX.sign(d).toDouble()
            }
        }
        val SIN: Function = object : OneArg() {
            override fun evaluate(d: kotlin.Double): kotlin.Double {
                return sin(d)
            }
        }
        val SINH: Function = object : OneArg() {
            override fun evaluate(d: kotlin.Double): kotlin.Double {
                return MathX.sinh(d)
            }
        }
        val SQRT: Function = object : OneArg() {
            override fun evaluate(d: kotlin.Double): kotlin.Double {
                return sqrt(d)
            }
        }

        val TAN: Function = object : OneArg() {
            override fun evaluate(d: kotlin.Double): kotlin.Double {
                return tan(d)
            }
        }
        val TANH: Function = object : OneArg() {
            override fun evaluate(d: kotlin.Double): kotlin.Double {
                return MathX.tanh(d)
            }
        }

        /* -------------------------------------------------------------------------- */
        val ATAN2: Function = object : TwoArg() {
            @Throws(EvaluationException::class)
            override fun evaluate(d0: kotlin.Double, d1: kotlin.Double): kotlin.Double {
                if (d0 == ZERO && d1 == ZERO) {
                    throw EvaluationException(ErrorEval.DIV_ZERO)
                }
                return atan2(d1, d0)
            }
        }
        val CEILING: Function = object : TwoArg() {
            override fun evaluate(d0: kotlin.Double, d1: kotlin.Double): kotlin.Double {
                return MathX.ceiling(d0, d1)
            }
        }
        val COMBIN: Function = object : TwoArg() {
            @Throws(EvaluationException::class)
            override fun evaluate(d0: kotlin.Double, d1: kotlin.Double): kotlin.Double {
                if (d0 > Int.MAX_VALUE || d1 > Int.MAX_VALUE) {
                    throw EvaluationException(ErrorEval.NUM_ERROR)
                }
                return MathX.nChooseK(d0.toInt(), d1.toInt())
            }
        }
        val FLOOR: Function = object : TwoArg() {
            @Throws(EvaluationException::class)
            override fun evaluate(d0: kotlin.Double, d1: kotlin.Double): kotlin.Double {
                if (d1 == ZERO) {
                    if (d0 == ZERO) {
                        return ZERO
                    }
                    throw EvaluationException(ErrorEval.DIV_ZERO)
                }
                return MathX.floor(d0, d1)
            }
        }
        val MOD: Function = object : TwoArg() {
            @Throws(EvaluationException::class)
            override fun evaluate(d0: kotlin.Double, d1: kotlin.Double): kotlin.Double {
                if (d1 == ZERO) {
                    throw EvaluationException(ErrorEval.DIV_ZERO)
                }
                return MathX.mod(d0, d1)
            }
        }
        val POWER: Function = object : TwoArg() {
            override fun evaluate(d0: kotlin.Double, d1: kotlin.Double): kotlin.Double {
                return d0.pow(d1)
            }
        }
        val ROUND: Function = object : TwoArg() {
            override fun evaluate(d0: kotlin.Double, d1: kotlin.Double): kotlin.Double {
                return MathX.round(d0, d1.toInt())
            }
        }
        val ROUNDDOWN: Function = object : TwoArg() {
            override fun evaluate(d0: kotlin.Double, d1: kotlin.Double): kotlin.Double {
                return MathX.roundDown(d0, d1.toInt())
            }
        }
        val ROUNDUP: Function = object : TwoArg() {
            override fun evaluate(d0: kotlin.Double, d1: kotlin.Double): kotlin.Double {
                return MathX.roundUp(d0, d1.toInt())
            }
        }
        val TRUNC_ARG2_DEFAULT: NumberEval = NumberEval(0.0)
        val TRUNC: Function = object : Var1or2ArgFunction() {
            override fun evaluate(
                srcRowIndex: Int,
                srcColumnIndex: Int,
                arg0: ValueEval?
            ): ValueEval? {
                return evaluate(srcRowIndex, srcColumnIndex, arg0, TRUNC_ARG2_DEFAULT)
            }

            override fun evaluate(
                srcRowIndex: Int,
                srcColumnIndex: Int,
                arg0: ValueEval?,
                arg1: ValueEval?
            ): ValueEval? {
                val result: kotlin.Double
                try {
                    val d0: kotlin.Double = singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex)
                    val d1: kotlin.Double = singleOperandEvaluate(arg1, srcRowIndex, srcColumnIndex)
                    val multi = 10.0.pow(d1)
                    result = floor(d0 * multi) / multi
                    checkValue(result)
                } catch (e: EvaluationException) {
                    return e.errorEval
                }
                return NumberEval(result)
            }
        }

        val LOG: Function = Log()

        val PI_EVAL: NumberEval = NumberEval(Math.PI)
        val PI: Function = object : Fixed0ArgFunction() {
            override fun evaluate(srcRowIndex: Int, srcColumnIndex: Int): ValueEval {
                return PI_EVAL
            }
        }
        val RAND: Function = object : Fixed0ArgFunction() {
            override fun evaluate(srcRowIndex: Int, srcColumnIndex: Int): ValueEval {
                return NumberEval(Math.random())
            }
        }
        val POISSON: Function = object : Fixed3ArgFunction() {
            private val DEFAULT_RETURN_RESULT = 1.0

            /**
             * This checks is x = 0 and the mean = 0.
             * Excel currently returns the value 1 where as the
             * maths common implementation will error.
             * @param x  The number.
             * @param mean The mean.
             * @return If a default value should be returned.
             */
            private fun isDefaultResult(x: kotlin.Double, mean: kotlin.Double): Boolean {
                if (x == 0.0 && mean == 0.0) {
                    return true
                }
                return false
            }

            @Throws(EvaluationException::class)
            private fun checkArgument(aDouble: kotlin.Double): Boolean {
                checkValue(aDouble)

                // make sure that the number is positive
                if (aDouble < 0) {
                    throw EvaluationException(ErrorEval.NUM_ERROR)
                }

                return true
            }

            private fun probability(k: Int, lambda: kotlin.Double): kotlin.Double {
                return lambda.pow(k.toDouble()) * exp(-lambda) / factorial(k)
            }

            private fun cumulativeProbability(x: Int, lambda: kotlin.Double): kotlin.Double {
                var result = 0.0
                for (k in 0..x) {
                    result += probability(k, lambda)
                }
                return result
            }

            /** All long-representable factorials  */
            private val FACTORIALS = longArrayOf(
                1L, 1L, 2L,
                6L, 24L, 120L,
                720L, 5040L, 40320L,
                362880L, 3628800L, 39916800L,
                479001600L, 6227020800L, 87178291200L,
                1307674368000L, 20922789888000L, 355687428096000L,
                6402373705728000L, 121645100408832000L, 2432902008176640000L
            )


            fun factorial(n: Int): Long {
                require(!(n < 0 || n > 20)) { "Valid argument should be in the range [0..20]" }
                return FACTORIALS[n]
            }

            override fun evaluate(
                srcRowIndex: Int,
                srcColumnIndex: Int,
                arg0: ValueEval?,
                arg1: ValueEval?,
                arg2: ValueEval?
            ): ValueEval? {
                // arguments/result for this function

                var mean = 0.0
                var x = 0.0
                val cumulative = (arg2 as BoolEval).booleanValue
                var result = 0.0

                try {
                    x = singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex)
                    mean = singleOperandEvaluate(arg1, srcRowIndex, srcColumnIndex)

                    // check for default result : excel implementation for 0,0
                    // is different to Math Common.
                    if (isDefaultResult(x, mean)) {
                        return NumberEval(DEFAULT_RETURN_RESULT)
                    }
                    // check the arguments : as per excel function def
                    checkArgument(x)
                    checkArgument(mean)

                    // truncate x : as per excel function def
                    if (cumulative) {
                        result = cumulativeProbability(x.toInt(), mean)
                    } else {
                        result = probability(x.toInt(), mean)
                    }

                    // check the result
                    checkValue(result)
                } catch (e: EvaluationException) {
                    return e.errorEval
                }

                return NumberEval(result)
            }
        }
    }
}
