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

import kotlin.math.ln
import kotlin.math.pow

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * 
 * 
 * This class is a functon library for common fiscal functions.
 * **Glossary of terms/abbreviations:**
 * <br></br>
 * 
 *  * *FV:* Future Value
 *  * *PV:* Present Value
 *  * *NPV:* Net Present Value
 *  * *PMT:* (Periodic) Payment
 * 
 * 
 * For more info on the terms/abbreviations please use the references below
 * (hyperlinks are subject to change):
 * Online References:
 * 
 *  1. GNU Emacs Calc 2.02 Manual: http://theory.uwinnipeg.ca/gnu/calc/calc_203.html
 *  1. Yahoo Financial Glossary: http://biz.yahoo.com/f/g/nn.html#y
 *  1. MS Excel function reference: http://office.microsoft.com/en-us/assistance/CH062528251033.aspx
 * 
 * <h3>Implementation Notes:</h3>
 * Symbols used in the formulae that follow:<br></br>
 * 
 *  * p: present value
 *  * f: future value
 *  * n: number of periods
 *  * y: payment (in each period)
 *  * r: rate
 *  * ^: the power operator (NOT the java bitwise XOR operator!)
 * 
 * [From MS Excel function reference] Following are some of the key formulas
 * that are used in this implementation:
 * <pre>
 * p(1+r)^n + y(1+rt)((1+r)^n-1)/r + f=0   ...{when r!=0}
 * ny + p + f=0                            ...{when r=0}
</pre> * 
 */
object FinanceLib {
    /**
     * Future value of an amount given the number of payments, rate, amount
     * of individual payment, present value and boolean value indicating whether
     * payments are due at the beginning of period
     * (false => payments are due at end of period)
     * @param r rate
     * @param n num of periods
     * @param y pmt per period
     * @param p future value
     * @param t type (true=pmt at end of period, false=pmt at begining of period)
     */
    fun fv(r: Double, n: Double, y: Double, p: Double, t: Boolean): Double {
        var retval = 0.0
        if (r == 0.0) {
            retval = -1 * (p + (n * y))
        } else {
            val r1 = r + 1
            retval = (((1 - r1.pow(n)) * (if (t) r1 else 1.0) * y) / r
                    -
                    p * r1.pow(n))
        }
        return retval
    }

    /**
     * Present value of an amount given the number of future payments, rate, amount
     * of individual payment, future value and boolean value indicating whether
     * payments are due at the beginning of period
     * (false => payments are due at end of period)
     * @param r
     * @param n
     * @param y
     * @param f
     * @param t
     */
    fun pv(r: Double, n: Double, y: Double, f: Double, t: Boolean): Double {
        var retval = 0.0
        if (r == 0.0) {
            retval = -1 * ((n * y) + f)
        } else {
            val r1 = r + 1
            retval = ((((1 - r1.pow(n)) / r) * (if (t) r1 else 1.0) * y - f)
                    / r1.pow(n))
        }
        return retval
    }

    /**
     * calculates the Net Present Value of a principal amount
     * given the discount rate and a sequence of cash flows
     * (supplied as an array). If the amounts are income the value should
     * be positive, else if they are payments and not income, the
     * value should be negative.
     * @param r
     * @param cfs cashflow amounts
     */
    fun npv(r: Double, cfs: DoubleArray): Double {
        var npv = 0.0
        val r1 = r + 1
        var trate = r1
        var i = 0
        val iSize = cfs.size
        while (i < iSize) {
            npv += cfs[i] / trate
            trate *= r1
            i++
        }
        return npv
    }

    /**
     * 
     * @param r
     * @param n
     * @param p
     * @param f
     * @param t
     */
    fun pmt(r: Double, n: Double, p: Double, f: Double, t: Boolean): Double {
        var retval = 0.0
        if (r == 0.0) {
            retval = -1 * (f + p) / n
        } else {
            val r1 = r + 1
            retval = ((f + p * r1.pow(n)) * r
                    /
                    ((if (t) r1 else 1.0) * (1 - r1.pow(n))))
        }
        return retval
    }

    /**
     * 
     * @param r
     * @param y
     * @param p
     * @param f
     * @param t
     */
    fun nper(r: Double, y: Double, p: Double, f: Double, t: Boolean): Double {
        var retval = 0.0
        if (r == 0.0) {
            retval = -1 * (f + p) / y
        } else {
            val r1 = r + 1
            val ryr = (if (t) r1 else 1.0) * y / r
            val a1 = if ((ryr - f) < 0) ln(f - ryr) else ln(ryr - f)
            val a2 = if ((ryr - f) < 0) ln(-p - ryr) else ln(p + ryr)
            val a3 = ln(r1)
            retval = (a1 - a2) / a3
        }
        return retval
    }
}
