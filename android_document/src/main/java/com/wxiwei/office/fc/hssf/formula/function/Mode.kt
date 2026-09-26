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

import com.wxiwei.office.fc.hssf.formula.TwoDEval
import com.wxiwei.office.fc.hssf.formula.eval.BlankEval
import com.wxiwei.office.fc.hssf.formula.eval.BoolEval
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.EvaluationException
import com.wxiwei.office.fc.hssf.formula.eval.NumberEval
import com.wxiwei.office.fc.hssf.formula.eval.RefEval
import com.wxiwei.office.fc.hssf.formula.eval.StringEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import java.util.Arrays

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 */
class Mode : Function {
    override fun evaluate(args: Array<ValueEval?>, srcCellRow: Int, srcCellCol: Int): ValueEval? {
        val result: Double
        try {
            val temp: MutableList<Double?> = ArrayList<Double?>()
            for (i in args.indices) {
                Companion.collectValues(args[i]!!, temp)
            }
            val values = DoubleArray(temp.size)
            for (i in values.indices) {
                values[i] = temp.get(i)!!
            }
            result = evaluate(values)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
        return NumberEval(result)
    }

    companion object {
        /**
         * if v is zero length or contains no duplicates, return value is
         * Double.NaN. Else returns the value that occurs most times and if there is
         * a tie, returns the first such value.
         * 
         * @param v
         */
        @Throws(EvaluationException::class)
        fun evaluate(v: DoubleArray): Double {
            if (v.size < 2) {
                throw EvaluationException(ErrorEval.NA)
            }

            // very naive impl, may need to be optimized
            val counts = IntArray(v.size)
            Arrays.fill(counts, 1)
            run {
                var i = 0
                val iSize = v.size
                while (i < iSize) {
                    var j = i + 1
                    val jSize = v.size
                    while (j < jSize) {
                        if (v[i] == v[j]) counts[i]++
                        j++
                    }
                    i++
                }
            }
            var maxv = 0.0
            var maxc = 0
            var i = 0
            val iSize = counts.size
            while (i < iSize) {
                if (counts[i] > maxc) {
                    maxv = v[i]
                    maxc = counts[i]
                }
                i++
            }
            if (maxc > 1) {
                return maxv
            }
            throw EvaluationException(ErrorEval.NA)
        }

        @Throws(EvaluationException::class)
        private fun collectValues(arg: ValueEval, temp: MutableList<Double?>) {
            if (arg is TwoDEval) {
                val ae = arg
                val width = ae.width
                val height = ae.height
                for (rrIx in 0..<height) {
                    for (rcIx in 0..<width) {
                        val ve1 = ae.getValue(rrIx, rcIx)
                        collectValue(ve1!!, temp, false)
                    }
                }
                return
            }
            if (arg is RefEval) {
                val re = arg
                Companion.collectValue(re.innerValueEval!!, temp, true)
                return
            }
            collectValue(arg, temp, true)
        }

        @Throws(EvaluationException::class)
        private fun collectValue(
            arg: ValueEval,
            temp: MutableList<Double?>,
            mustBeNumber: Boolean
        ) {
            if (arg is ErrorEval) {
                throw EvaluationException(arg)
            }
            if (arg === BlankEval.instance || arg is BoolEval || arg is StringEval) {
                if (mustBeNumber) {
                    throw EvaluationException.invalidValue()
                }
                return
            }
            if (arg is NumberEval) {
                temp.add(arg.numberValue)
                return
            }
            throw RuntimeException("Unexpected value type (" + arg.javaClass.getName() + ")")
        }
    }
}
