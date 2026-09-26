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

import java.util.Arrays
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * 
 * Library for common statistics functions
 */
internal object StatsLib {
    /**
     * returns the mean of deviations from mean.
     * @param v
     */
    fun avedev(v: DoubleArray): Double {
        var r = 0.0
        var m = 0.0
        var s = 0.0
        run {
            var i = 0
            val iSize = v.size
            while (i < iSize) {
                s += v[i]
                i++
            }
        }
        m = s / v.size
        s = 0.0
        var i = 0
        val iSize = v.size
        while (i < iSize) {
            s += abs(v[i] - m)
            i++
        }
        r = s / v.size
        return r
    }

    fun stdev(v: DoubleArray?): Double {
        var r = Double.NaN
        if (v != null && v.size > 1) {
            r = sqrt(devsq(v) / (v.size - 1))
        }
        return r
    }

    fun `var`(v: DoubleArray?): Double {
        var r = Double.NaN
        if (v != null && v.size > 1) {
            r = devsq(v) / (v.size - 1)
        }
        return r
    }

    fun varp(v: DoubleArray?): Double {
        var r = Double.NaN
        if (v != null && v.size > 1) {
            r = devsq(v) / v.size
        }
        return r
    }

    fun median(v: DoubleArray?): Double {
        var r = Double.NaN

        if (v != null && v.size >= 1) {
            val n = v.size
            Arrays.sort(v)
            r = if (n % 2 == 0)
                (v[n / 2] + v[n / 2 - 1]) / 2
            else
                v[n / 2]
        }

        return r
    }


    fun devsq(v: DoubleArray?): Double {
        var r = Double.NaN
        if (v != null && v.size >= 1) {
            var m = 0.0
            var s = 0.0
            val n = v.size
            for (i in 0..<n) {
                s += v[i]
            }
            m = s / n
            s = 0.0
            for (i in 0..<n) {
                s += (v[i] - m) * (v[i] - m)
            }

            r = if (n == 1)
                0.0
            else
                s
        }
        return r
    }

    /**
     * returns the kth largest element in the array. Duplicates
     * are considered as distinct values. Hence, eg.
     * for array {1,2,4,3,3} & k=2, returned value is 3.
     * <br></br>
     * k <= 0 & k >= v.length and null or empty arrays
     * will result in return value Double.NaN
     */
    fun kthLargest(v: DoubleArray?, k: Int): Double {
        var r = Double.NaN
        val index = k - 1 // since arrays are 0-based
        if (v != null && v.size > index && index >= 0) {
            Arrays.sort(v)
            r = v[v.size - index - 1]
        }
        return r
    }

    /**
     * returns the kth smallest element in the array. Duplicates
     * are considered as distinct values. Hence, eg.
     * for array {1,1,2,4,3,3} & k=2, returned value is 1.
     * <br></br>
     * k <= 0 & k >= v.length or null array or empty array
     * will result in return value Double.NaN
     * @param v
     * @param k
     */
    fun kthSmallest(v: DoubleArray?, k: Int): Double {
        var r = Double.NaN
        val index = k - 1 // since arrays are 0-based
        if (v != null && v.size > index && index >= 0) {
            Arrays.sort(v)
            r = v[index]
        }
        return r
    }
}
