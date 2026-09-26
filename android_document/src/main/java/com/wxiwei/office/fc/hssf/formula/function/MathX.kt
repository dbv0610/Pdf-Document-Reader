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

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.DoubleArray
import kotlin.Int
import kotlin.Short
import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.text.toDouble


/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * This class is an extension to the standard math library
 * provided by java.lang.Math class. It follows the Math class
 * in that it has a private constructor and all static methods.
 */
internal object MathX {
    /**
     * Returns a value rounded to p digits after decimal.
     * If p is negative, then the number is rounded to
     * places to the left of the decimal point. eg.
     * 10.23 rounded to -1 will give: 10. If p is zero,
     * the returned value is rounded to the nearest integral
     * value.
     * 
     * If n is negative, the resulting value is obtained
     * as the round value of absolute value of n multiplied
     * by the sign value of n (@see MathX.sign(double d)).
     * Thus, -0.6666666 rounded to p=0 will give -1 not 0.
     * 
     * If n is NaN, returned value is NaN.
     * @param n
     * @param p
     */
    fun round(n: Double, p: Int): Double {
        val retval: Double

        if (n.isNaN() || n.isInfinite()) {
            retval = kotlin.Double.NaN
        } else {
            retval = BigDecimal.valueOf(n).setScale(p, RoundingMode.HALF_UP).toDouble()
        }

        return retval
    }

    /**
     * Returns a value rounded-up to p digits after decimal.
     * If p is negative, then the number is rounded to
     * places to the left of the decimal point. eg.
     * 10.23 rounded to -1 will give: 20. If p is zero,
     * the returned value is rounded to the nearest integral
     * value.
     * 
     * If n is negative, the resulting value is obtained
     * as the round-up value of absolute value of n multiplied
     * by the sign value of n (@see MathX.sign(double d)).
     * Thus, -0.2 rounded-up to p=0 will give -1 not 0.
     * 
     * If n is NaN, returned value is NaN.
     * @param n
     * @param p
     */
    fun roundUp(n: kotlin.Double, p: Int): kotlin.Double {
        val retval: kotlin.Double

        if (n.isNaN() || n.isInfinite()) {
            retval = kotlin.Double.NaN
        } else {
            retval = BigDecimal.valueOf(n).setScale(p, RoundingMode.UP).toDouble()
        }

        return retval
    }

    /**
     * Returns a value rounded to p digits after decimal.
     * If p is negative, then the number is rounded to
     * places to the left of the decimal point. eg.
     * 10.23 rounded to -1 will give: 10. If p is zero,
     * the returned value is rounded to the nearest integral
     * value.
     * 
     * If n is negative, the resulting value is obtained
     * as the round-up value of absolute value of n multiplied
     * by the sign value of n (@see MathX.sign(double d)).
     * Thus, -0.8 rounded-down to p=0 will give 0 not -1.
     * 
     * If n is NaN, returned value is NaN.
     * @param n
     * @param p
     */
    fun roundDown(n: kotlin.Double, p: Int): kotlin.Double {
        val retval: kotlin.Double

        if (n.isNaN() || n.isInfinite()) {
            retval = kotlin.Double.NaN
        } else {
            retval = BigDecimal.valueOf(n).setScale(p, RoundingMode.DOWN).toDouble()
        }

        return retval
    }


    /**
     * If d < 0, returns short -1
     * <br></br>
     * If d > 0, returns short 1
     * <br></br>
     * If d == 0, returns short 0
     * 
     *  If d is NaN, then 1 will be returned. It is the responsibility
     * of caller to check for d isNaN if some other value is desired.
     * @param d
     */
    fun sign(d: kotlin.Double): Short {
        return (if (d == 0.0)
            0
        else
            if (d < 0)
                -1
            else
                1).toShort()
    }

    /**
     * average of all values
     * @param values
     */
    fun average(values: DoubleArray): kotlin.Double {
        var ave = 0.0
        var sum = 0.0
        var i = 0
        val iSize = values.size
        while (i < iSize) {
            sum += values[i]
            i++
        }
        ave = sum / values.size
        return ave
    }


    /**
     * sum of all values
     * @param values
     */
    fun sum(values: DoubleArray): kotlin.Double {
        var sum = 0.0
        var i = 0
        val iSize = values.size
        while (i < iSize) {
            sum += values[i]
            i++
        }
        return sum
    }

    /**
     * sum of squares of all values
     * @param values
     */
    fun sumsq(values: DoubleArray): kotlin.Double {
        var sumsq = 0.0
        var i = 0
        val iSize = values.size
        while (i < iSize) {
            sumsq += values[i] * values[i]
            i++
        }
        return sumsq
    }


    /**
     * product of all values
     * @param values
     */
    fun product(values: DoubleArray?): kotlin.Double {
        var product = 0.0
        if (values != null && values.size > 0) {
            product = 1.0
            var i = 0
            val iSize = values.size
            while (i < iSize) {
                product *= values[i]
                i++
            }
        }
        return product
    }

    /**
     * min of all values. If supplied array is zero length,
     * Double.POSITIVE_INFINITY is returned.
     * @param values
     */
    fun min(values: DoubleArray): kotlin.Double {
        var min = kotlin.Double.POSITIVE_INFINITY
        var i = 0
        val iSize = values.size
        while (i < iSize) {
            min = kotlin.math.min(min, values[i])
            i++
        }
        return min
    }

    /**
     * min of all values. If supplied array is zero length,
     * Double.NEGATIVE_INFINITY is returned.
     * @param values
     */
    fun max(values: DoubleArray): kotlin.Double {
        var max = kotlin.Double.NEGATIVE_INFINITY
        var i = 0
        val iSize = values.size
        while (i < iSize) {
            max = kotlin.math.max(max, values[i])
            i++
        }
        return max
    }

    /**
     * Note: this function is different from java.lang.Math.floor(..).
     * 
     * 
     * When n and s are "valid" arguments, the returned value is: Math.floor(n/s) * s;
     * <br></br>
     * n and s are invalid if any of following conditions are true:
     * 
     *  * s is zero
     *  * n is negative and s is positive
     *  * n is positive and s is negative
     * 
     * In all such cases, Double.NaN is returned.
     * @param n
     * @param s
     */
    fun floor(n: kotlin.Double, s: kotlin.Double): kotlin.Double {
        val f: kotlin.Double

        if ((n < 0 && s > 0) || (n > 0 && s < 0) || (s == 0.0 && n != 0.0)) {
            f = kotlin.Double.NaN
        } else {
            f = if (n == 0.0 || s == 0.0) 0.0 else kotlin.math.floor(n / s) * s
        }

        return f
    }

    /**
     * Note: this function is different from java.lang.Math.ceil(..).
     * 
     * 
     * When n and s are "valid" arguments, the returned value is: Math.ceiling(n/s) * s;
     * <br></br>
     * n and s are invalid if any of following conditions are true:
     * 
     *  * s is zero
     *  * n is negative and s is positive
     *  * n is positive and s is negative
     * 
     * In all such cases, Double.NaN is returned.
     * @param n
     * @param s
     */
    fun ceiling(n: kotlin.Double, s: kotlin.Double): kotlin.Double {
        val c: kotlin.Double

        if ((n < 0 && s > 0) || (n > 0 && s < 0)) {
            c = kotlin.Double.NaN
        } else {
            c = if (n == 0.0 || s == 0.0) 0.0 else ceil(n / s) * s
        }

        return c
    }

    /**
     * <br></br> for all n >= 1; factorial n = n * (n-1) * (n-2) * ... * 1
     * <br></br> else if n == 0; factorial n = 1
     * <br></br> else if n < 0; factorial n = Double.NaN
     * <br></br> Loss of precision can occur if n is large enough.
     * If n is large so that the resulting value would be greater
     * than Double.MAX_VALUE; Double.POSITIVE_INFINITY is returned.
     * If n < 0, Double.NaN is returned.
     * @param n
     */
    fun factorial(n: Int): kotlin.Double {
        var d = 1.0

        if (n >= 0) {
            if (n <= 170) {
                for (i in 1..n) {
                    d *= i.toDouble()
                }
            } else {
                d = kotlin.Double.POSITIVE_INFINITY
            }
        } else {
            d = kotlin.Double.NaN
        }
        return d
    }


    /**
     * returns the remainder resulting from operation:
     * n / d.
     * <br></br> The result has the sign of the divisor.
     * <br></br> Examples:
     * 
     *  * mod(3.4, 2) = 1.4
     *  * mod(-3.4, 2) = 0.6
     *  * mod(-3.4, -2) = -1.4
     *  * mod(3.4, -2) = -0.6
     * 
     * If d == 0, result is NaN
     * @param n
     * @param d
     */
    fun mod(n: kotlin.Double, d: kotlin.Double): kotlin.Double {
        var result = 0.0

        if (d == 0.0) {
            result = kotlin.Double.NaN
        } else if (sign(n) == sign(d)) {
            result = n % d
        } else {
            result = ((n % d) + d) % d
        }

        return result
    }

    /**
     * inverse hyperbolic cosine
     * @param d
     */
    fun acosh(d: kotlin.Double): kotlin.Double {
        return ln(sqrt(d.pow(2.0) - 1) + d)
    }

    /**
     * inverse hyperbolic sine
     * @param d
     */
    fun asinh(d: kotlin.Double): kotlin.Double {
        return ln(sqrt(d * d + 1) + d)
    }

    /**
     * inverse hyperbolic tangent
     * @param d
     */
    fun atanh(d: kotlin.Double): kotlin.Double {
        return ln((1 + d) / (1 - d)) / 2
    }

    /**
     * hyperbolic cosine
     * @param d
     */
    fun cosh(d: kotlin.Double): kotlin.Double {
        val ePowX = Math.E.pow(d)
        val ePowNegX = Math.E.pow(-d)
        return (ePowX + ePowNegX) / 2
    }

    /**
     * hyperbolic sine
     * @param d
     */
    fun sinh(d: kotlin.Double): kotlin.Double {
        val ePowX = Math.E.pow(d)
        val ePowNegX = Math.E.pow(-d)
        return (ePowX - ePowNegX) / 2
    }

    /**
     * hyperbolic tangent
     * @param d
     */
    fun tanh(d: kotlin.Double): kotlin.Double {
        val ePowX = Math.E.pow(d)
        val ePowNegX = Math.E.pow(-d)
        return (ePowX - ePowNegX) / (ePowX + ePowNegX)
    }


    /**
     * returns the total number of combinations possible when
     * k items are chosen out of total of n items. If the number
     * is too large, loss of precision may occur (since returned
     * value is double). If the returned value is larger than
     * Double.MAX_VALUE, Double.POSITIVE_INFINITY is returned.
     * If either of the parameters is negative, Double.NaN is returned.
     * @param n
     * @param k
     */
    fun nChooseK(n: Int, k: Int): kotlin.Double {
        var d = 1.0
        if (n < 0 || k < 0 || n < k) {
            d = kotlin.Double.NaN
        } else {
            val minnk = kotlin.math.min(n - k, k)
            val maxnk = kotlin.math.max(n - k, k)
            for (i in maxnk..<n) {
                d *= (i + 1).toDouble()
            }
            d /= factorial(minnk)
        }

        return d
    }
}
