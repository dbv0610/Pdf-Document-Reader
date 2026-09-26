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
import com.wxiwei.office.ss.util.DateUtil.Companion.getJavaDate
import java.util.Calendar
import java.util.GregorianCalendar

/**
 * Implementation of Excel functions Date parsing functions:
 * Date - DAY, MONTH and YEAR
 * Time - HOUR, MINUTE and SECOND
 */
class CalendarFieldFunction private constructor(private val _dateFieldId: Int) :
    Fixed1ArgFunction() {
    override fun evaluate(srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?): ValueEval? {
        val `val`: Double
        try {
            val ve = getSingleValue(arg0, srcRowIndex, srcColumnIndex)
            `val` = OperandResolver.coerceValueToDouble(ve!!)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
        if (`val` < 0) {
            return ErrorEval.NUM_ERROR
        }
        return NumberEval(getCalField(`val`).toDouble())
    }

    private fun getCalField(serialDate: Double): Int {
        // For some reason, a date of 0 in Excel gets shown
        //  as the non existant 1900-01-00
        if ((serialDate.toInt()) == 0) {
            when (_dateFieldId) {
                Calendar.YEAR -> return 1900
                Calendar.MONTH -> return 1
                Calendar.DAY_OF_MONTH -> return 0
            }
            // They want time, that's normal
        }

        // TODO Figure out if we're in 1900 or 1904
        val d = getJavaDate(serialDate, false)

        val c: Calendar = GregorianCalendar()
        c.setTime(d)
        var result = c.get(_dateFieldId)


        // Month is a special case due to C semantics
        if (_dateFieldId == Calendar.MONTH) {
            result++
        }

        return result
    }

    companion object {
        val YEAR: Function = CalendarFieldFunction(Calendar.YEAR)
        val MONTH: Function = CalendarFieldFunction(Calendar.MONTH)
        val WEEKDAY: Function = CalendarFieldFunction(Calendar.DAY_OF_WEEK)
        val DAY: Function = CalendarFieldFunction(Calendar.DAY_OF_MONTH)
        val HOUR: Function = CalendarFieldFunction(Calendar.HOUR_OF_DAY)
        val MINUTE: Function = CalendarFieldFunction(Calendar.MINUTE)
        val SECOND: Function = CalendarFieldFunction(Calendar.SECOND)
    }
}
