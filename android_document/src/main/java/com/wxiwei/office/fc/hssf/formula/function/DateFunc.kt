/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
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
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.ss.util.DateUtil.Companion.getExcelDate
import java.util.Calendar
import java.util.GregorianCalendar

/**
 * Implementation for the Excel function DATE
 * 
 * @author Pavel Krupets (pkrupets at palmtreebusiness dot com)
 */
class DateFunc private constructor() : Fixed3ArgFunction() {
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
            val d2: Double =
                NumericFunction.Companion.singleOperandEvaluate(arg2, srcRowIndex, srcColumnIndex)
            result = evaluate(getYear(d0), (d1 - 1).toInt(), d2.toInt())
            NumericFunction.Companion.checkValue(result)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
        return NumberEval(result)
    }

    companion object {
        val instance: Function = DateFunc()

        @Throws(EvaluationException::class)
        private fun evaluate(year: Int, month: Int, pDay: Int): Double {
            if (year < 0 || month < 0 || pDay < 0) {
                throw EvaluationException(ErrorEval.VALUE_INVALID)
            }

            if (year == 1900 && month == Calendar.FEBRUARY && pDay == 29) {
                return 60.0
            }

            var day = pDay
            if (year == 1900) {
                if ((month == Calendar.JANUARY && day >= 60) ||
                    (month == Calendar.FEBRUARY && day >= 30)
                ) {
                    day--
                }
            }

            val c: Calendar = GregorianCalendar()

            c.set(year, month, day, 0, 0, 0)
            c.set(Calendar.MILLISECOND, 0)

            return getExcelDate(c.getTime(), false) // TODO - fix 1900/1904 problem
        }

        private fun getYear(d: Double): Int {
            val year = d.toInt()

            if (year < 0) {
                return -1
            }

            return if (year < 1900) 1900 + year else year
        }
    }
}
