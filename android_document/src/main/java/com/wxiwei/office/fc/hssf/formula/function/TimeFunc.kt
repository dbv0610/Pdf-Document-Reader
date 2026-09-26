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
import com.wxiwei.office.fc.hssf.formula.eval.MissingArgEval
import com.wxiwei.office.fc.hssf.formula.eval.NumberEval
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver.getSingleValue
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval

/**
 * Implementation for the Excel function TIME
 * 
 * @author Steven Butler (sebutler @ gmail dot com)
 * 
 * Based on POI [DateFunc]
 */
class TimeFunc : Fixed3ArgFunction() {
    override fun evaluate(
        srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?, arg1: ValueEval?,
        arg2: ValueEval?
    ): ValueEval? {
        val result: Double
        try {
            result = evaluate(
                evalArg(arg0, srcRowIndex, srcColumnIndex),
                evalArg(arg1, srcRowIndex, srcColumnIndex),
                evalArg(arg2, srcRowIndex, srcColumnIndex)
            )
        } catch (e: EvaluationException) {
            return e.errorEval
        }
        return NumberEval(result)
    }

    companion object {
        private const val SECONDS_PER_MINUTE = 60
        private const val SECONDS_PER_HOUR = 3600
        private const val HOURS_PER_DAY = 24
        private val SECONDS_PER_DAY: Int = HOURS_PER_DAY * SECONDS_PER_HOUR


        @Throws(EvaluationException::class)
        private fun evalArg(arg: ValueEval?, srcRowIndex: Int, srcColumnIndex: Int): Int {
            if (arg === MissingArgEval.instance) {
                return 0
            }
            val ev = getSingleValue(arg, srcRowIndex, srcColumnIndex)
            // Excel silently truncates double values to integers
            return OperandResolver.coerceValueToInt(ev!!)
        }

        /**
         * Converts the supplied hours, minutes and seconds to an Excel time value.
         * 
         * 
         * @param ds array of 3 doubles containing hours, minutes and seconds.
         * Non-integer inputs are truncated to an integer before further calculation
         * of the time value.
         * @return An Excel representation of a time of day.
         * If the time value represents more than a day, the days are removed from
         * the result, leaving only the time of day component.
         * @throws EvaluationException
         * If any of the arguments are greater than 32767 or the hours
         * minutes and seconds when combined form a time value less than 0, the function
         * evaluates to an error.
         */
        @Throws(EvaluationException::class)
        private fun evaluate(hours: Int, minutes: Int, seconds: Int): Double {
            if (hours > 32767 || minutes > 32767 || seconds > 32767) {
                throw EvaluationException(ErrorEval.VALUE_INVALID)
            }
            val totalSeconds: Int =
                hours * SECONDS_PER_HOUR + minutes * SECONDS_PER_MINUTE + seconds

            if (totalSeconds < 0) {
                throw EvaluationException(ErrorEval.VALUE_INVALID)
            }
            return (totalSeconds % SECONDS_PER_DAY) / SECONDS_PER_DAY.toDouble()
        }
    }
}
