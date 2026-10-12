/**
 * Copyright (C) 2009, 2010 SC 4ViewSoft SRL
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wxiwei.office.thirdpart.achartengine.util

import java.text.NumberFormat
import java.text.ParseException
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Utility class for math operations.
 */
object MathHelper {
    /** A value that is used a null value.  */
    @JvmField
    val NULL_VALUE: Double = Double.MAX_VALUE

    /**
     * A number formatter to be used to make sure we have a maximum number of
     * fraction digits in the labels.
     */
    private val FORMAT: NumberFormat = NumberFormat.getNumberInstance()

    /**
     * Calculate the minimum and maximum values out of a list of doubles.
     * 
     * @param values the input values
     * @return an array with the minimum and maximum values
     */
    fun minmax(values: MutableList<Double>): DoubleArray {
        if (values.size == 0) {
            return DoubleArray(2)
        }
        var min: Double = values.get(0)!!
        var max = min
        val length = values.size
        for (i in 1..<length) {
            val value: Double = values.get(i)!!
            min = min(min, value)
            max = max(max, value)
        }
        return doubleArrayOf(min, max)
    }

    /**
     * Computes a reasonable set of labels for a data interval and number of
     * labels.
     * 
     * @param start start value
     * @param end final value
     * @param approxNumLabels desired number of labels
     * @return collection containing {start value, end value, increment}
     */
    fun getLabels(
        start: Double, end: Double,
        approxNumLabels: Int
    ): MutableList<Double> {
        FORMAT.setMaximumFractionDigits(5)
        val labels: MutableList<Double> = ArrayList<Double>()
        val labelParams = computeLabels(start, end, approxNumLabels)
        // when the start > end the inc will be negative so it will still work    
        val numLabels = 1 + ((labelParams[1] - labelParams[0]) / labelParams[2]).toInt()
        // we want the range to be inclusive but we don't want to blow up when
        // looping for the case where the min and max are the same. So we loop
        // on
        // numLabels not on the values.
        for (i in 0..<numLabels) {
            var z = labelParams[0] + i * labelParams[2]
            try {
                // this way, we avoid a label value like 0.4000000000000000001 instead
                // of 0.4
                z = FORMAT.parse(FORMAT.format(z)).toDouble()
            } catch (e: ParseException) {
                // do nothing here
            }
            labels.add(z)
        }
        return labels
    }

    /**
     * Computes a reasonable number of labels for a data range.
     * 
     * @param start start value
     * @param end final value
     * @param approxNumLabels desired number of labels
     * @return double[] array containing {start value, end value, increment}
     */
    private fun computeLabels(
        start: Double, end: Double,
        approxNumLabels: Int
    ): DoubleArray {
        var s = start
        var e = end
        if (abs(start - end) < 0.0000001f) {
            val xStep = roundUp(s / approxNumLabels)
            // Compute x starting point so it is a multiple of xStep.
            val xEnd = xStep * ceil(e / xStep)

            return doubleArrayOf(xStep, xEnd, xStep)
        }

        var switched = false
        if (s > e) {
            switched = true
            val tmp = s
            s = e
            e = tmp
        }
        val xStep = roundUp(abs(s - e) / approxNumLabels)
        // Compute x starting point so it is a multiple of xStep.
        val xStart = xStep * floor(s / xStep)
        val xEnd = xStep * ceil(e / xStep)
        if (switched) {
            return doubleArrayOf(xEnd, xStart, -1.0 * xStep)
        }
        return doubleArrayOf(xStart, xEnd, xStep)
    }

    /**
     * Given a number, round up to the nearest power of ten times 1, 2, or 5. The
     * argument must be strictly positive.
     */
    private fun roundUp(`val`: Double): Double {
        val exponent = floor(log10(`val`)).toInt()
        var rval = `val` * 10.0.pow(-exponent.toDouble())
        if (rval > 5.0) {
            rval = 10.0
        } else if (rval > 2.0) {
            rval = 5.0
        } else if (rval > 1.0) {
            rval = 2.0
        }
        rval *= 10.0.pow(exponent.toDouble())
        return rval
    }

    /**
     * Transforms a list of Float values into an array of float.
     * 
     * @param values the list of Float
     * @return the array of floats
     */
    fun getFloats(values: MutableList<Float>): FloatArray {
        val length = values.size
        val result = FloatArray(length)
        for (i in 0..<length) {
            result[i] = values.get(i)!!
        }
        return result
    }
}
