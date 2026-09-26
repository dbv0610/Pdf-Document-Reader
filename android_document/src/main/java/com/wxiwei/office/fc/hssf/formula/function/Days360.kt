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

import com.wxiwei.office.fc.hssf.formula.eval.EvaluationException
import com.wxiwei.office.fc.hssf.formula.eval.NumberEval
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver.getSingleValue
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.ss.util.DateUtil.Companion.getJavaDate
import java.util.Calendar
import java.util.GregorianCalendar

/**
 * Calculates the number of days between two dates based on a 360-day year
 * (twelve 30-day months), which is used in some accounting calculations. Use
 * this function to help compute payments if your accounting system is based on
 * twelve 30-day months.
 * 
 * @author PUdalau
 */
class Days360 : Var2or3ArgFunction() {
    override fun evaluate(
        srcRowIndex: Int,
        srcColumnIndex: Int,
        arg0: ValueEval?,
        arg1: ValueEval?
    ): ValueEval? {
        val result: Double
        try {
            val d0: Double =
                NumericFunction.Companion.singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex)
            val d1: Double =
                NumericFunction.Companion.singleOperandEvaluate(arg1, srcRowIndex, srcColumnIndex)
            result = evaluate(d0, d1, false)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
        return NumberEval(result)
    }

    override fun evaluate(
        srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?, arg1: ValueEval?,
        arg2: ValueEval?
    ): ValueEval? {
        val result: Double
        try {
            val d0: Double =
                NumericFunction.Companion.singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex)
            val d1: Double =
                NumericFunction.Companion.singleOperandEvaluate(arg1, srcRowIndex, srcColumnIndex)
            val ve = getSingleValue(arg2, srcRowIndex, srcColumnIndex)
            val method: Boolean? = OperandResolver.coerceValueToBoolean(ve, false)
            result = evaluate(d0, d1, if (method == null) false else method)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
        return NumberEval(result)
    }

    companion object {
        private fun evaluate(d0: Double, d1: Double, method: Boolean): Double {
            val startingDate: Calendar = getStartingDate(d0)
            val endingDate: Calendar = getEndingDateAccordingToStartingDate(d1, startingDate)
            val startingDay =
                (startingDate.get(Calendar.MONTH) * 30 + startingDate.get(Calendar.DAY_OF_MONTH)).toLong()
            val endingDay =
                ((endingDate.get(Calendar.YEAR) - startingDate.get(Calendar.YEAR)) * 360 + endingDate.get(
                    Calendar.MONTH
                ) * 30 + endingDate.get(Calendar.DAY_OF_MONTH)).toLong()
            return (endingDay - startingDay).toDouble()
        }

        private fun getDate(date: Double): Calendar {
            val processedDate: Calendar = GregorianCalendar()
            processedDate.setTime(getJavaDate(date, false))
            return processedDate
        }

        private fun getStartingDate(date: Double): Calendar {
            val startingDate: Calendar = getDate(date)
            if (isLastDayOfMonth(startingDate)) {
                startingDate.set(Calendar.DAY_OF_MONTH, 30)
            }
            return startingDate
        }

        private fun getEndingDateAccordingToStartingDate(
            date: Double,
            startingDate: Calendar
        ): Calendar {
            var endingDate: Calendar = getDate(date)
            endingDate.setTime(getJavaDate(date, false))
            if (isLastDayOfMonth(endingDate)) {
                if (startingDate.get(Calendar.DATE) < 30) {
                    endingDate = getFirstDayOfNextMonth(endingDate)
                }
            }
            return endingDate
        }

        private fun isLastDayOfMonth(date: Calendar): Boolean {
            val clone = date.clone() as Calendar
            clone.add(Calendar.MONTH, 1)
            clone.add(Calendar.DAY_OF_MONTH, -1)
            val lastDayOfMonth = clone.get(Calendar.DAY_OF_MONTH)
            return date.get(Calendar.DAY_OF_MONTH) == lastDayOfMonth
        }

        private fun getFirstDayOfNextMonth(date: Calendar): Calendar {
            val newDate = date.clone() as Calendar
            if (date.get(Calendar.MONTH) < Calendar.DECEMBER) {
                newDate.set(Calendar.MONTH, date.get(Calendar.MONTH) + 1)
            } else {
                newDate.set(Calendar.MONTH, 1)
                newDate.set(Calendar.YEAR, date.get(Calendar.YEAR) + 1)
            }
            newDate.set(Calendar.DATE, 1)
            return newDate
        }
    }
}
