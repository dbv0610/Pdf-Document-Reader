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
package com.wxiwei.office.fc.hssf.formula.atp

import com.wxiwei.office.fc.hssf.formula.OperationEvaluationContext
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.EvaluationException
import com.wxiwei.office.fc.hssf.formula.eval.NumberEval
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver
import com.wxiwei.office.fc.hssf.formula.eval.StringEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.fc.hssf.formula.function.FreeRefFunction
import com.wxiwei.office.ss.util.DateUtil.Companion.getExcelDate
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.regex.Pattern

/**
 * Implementation of Excel 'Analysis ToolPak' function YEARFRAC()<br></br>
 * 
 * Returns the fraction of the year spanned by two dates.
 *
 *
 * 
 * **Syntax**<br></br>
 * **YEARFRAC**(**startDate**, **endDate**, basis)
 *
 *
 * 
 * The **basis** optionally specifies the behaviour of YEARFRAC as follows:
 * 
 * <table border="0" cellpadding="1" cellspacing="0" summary="basis parameter description">
 * <tr><th>Value</th><th>Days per Month</th><th>Days per Year</th></tr>
 * <tr align='center'><td>0 (default)</td><td>30</td><td>360</td></tr>
 * <tr align='center'><td>1</td><td>actual</td><td>actual</td></tr>
 * <tr align='center'><td>2</td><td>actual</td><td>360</td></tr>
 * <tr align='center'><td>3</td><td>actual</td><td>365</td></tr>
 * <tr align='center'><td>4</td><td>30</td><td>360</td></tr>
</table> * 
 * 
 */
internal class YearFrac private constructor() : FreeRefFunction {
    override fun evaluate(args: Array<ValueEval?>, ec: OperationEvaluationContext): ValueEval? {
        val srcCellRow = ec.rowIndex
        val srcCellCol = ec.columnIndex
        val result: Double
        try {
            var basis = 0 // default
            when (args.size) {
                3 -> basis = evaluateIntArg(args[2], srcCellRow, srcCellCol)
                2 -> {}
                else -> return ErrorEval.VALUE_INVALID
            }
            val startDateVal: Double = evaluateDateArg(args[0], srcCellRow, srcCellCol)
            val endDateVal: Double = evaluateDateArg(args[1], srcCellRow, srcCellCol)
            result = YearFracCalculator.calculate(startDateVal, endDateVal, basis)
        } catch (e: EvaluationException) {
            return e.errorEval
        }

        return NumberEval(result)
    }

    companion object {
        val instance: FreeRefFunction = YearFrac()

        @Throws(EvaluationException::class)
        private fun evaluateDateArg(arg: ValueEval?, srcCellRow: Int, srcCellCol: Int): Double {
            val ve = OperandResolver.getSingleValue(arg, srcCellRow, srcCellCol.toShort().toInt())

            if (ve is StringEval) {
                val strVal = ve.stringValue
                val dVal = OperandResolver.parseDouble(strVal)
                if (dVal != null) {
                    return dVal
                }
                val date: Calendar = parseDate(strVal)
                return getExcelDate(date, false)
            }
            return OperandResolver.coerceValueToDouble(ve)
        }

        @Throws(EvaluationException::class)
        private fun parseDate(strVal: String): Calendar {
            val parts = Pattern.compile("/").split(strVal)
            if (parts.size != 3) {
                throw EvaluationException(ErrorEval.VALUE_INVALID)
            }
            var part2 = parts[2]
            val spacePos = part2.indexOf(' ')
            if (spacePos > 0) {
                // drop time portion if present
                part2 = part2.substring(0, spacePos)
            }
            val f0: Int
            val f1: Int
            val f2: Int
            try {
                f0 = parts[0].toInt()
                f1 = parts[1].toInt()
                f2 = part2.toInt()
            } catch (e: NumberFormatException) {
                throw EvaluationException(ErrorEval.VALUE_INVALID)
            }
            if (f0 < 0 || f1 < 0 || f2 < 0 || (f0 > 12 && f1 > 12 && f2 > 12)) {
                // easy to see this cannot be a valid date
                throw EvaluationException(ErrorEval.VALUE_INVALID)
            }

            if (f0 >= 1900 && f0 < 9999) {
                // when 4 digit value appears first, the format is YYYY/MM/DD, regardless of OS settings
                return makeDate(f0, f1, f2)
            }
            // otherwise the format seems to depend on OS settings (default date format)
            if (false) {
                // MM/DD/YYYY is probably a good guess, if the in the US
                return makeDate(f2, f0, f1)
            }
            // TODO - find a way to choose the correct date format
            throw RuntimeException("Unable to determine date format for text '" + strVal + "'")
        }

        /**
         * @param month 1-based
         */
        @Throws(EvaluationException::class)
        private fun makeDate(year: Int, month: Int, day: Int): Calendar {
            if (month < 1 || month > 12) {
                throw EvaluationException(ErrorEval.VALUE_INVALID)
            }
            val cal: Calendar = GregorianCalendar(year, month - 1, 1, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            if (day < 1 || day > cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                throw EvaluationException(ErrorEval.VALUE_INVALID)
            }
            cal.set(Calendar.DAY_OF_MONTH, day)
            return cal
        }

        @Throws(EvaluationException::class)
        private fun evaluateIntArg(arg: ValueEval?, srcCellRow: Int, srcCellCol: Int): Int {
            val ve = OperandResolver.getSingleValue(arg, srcCellRow, srcCellCol.toShort().toInt())
            return OperandResolver.coerceValueToInt(ve)
        }
    }
}
