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
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver.parseDouble
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval

/**
 * Implementation for Excel VALUE() function.
 *
 *
 * 
 * **Syntax**:<br></br> **VALUE**(**text**)<br></br>
 * 
 * Converts the text argument to a number. Leading and/or trailing whitespace is
 * ignored. Currency symbols and thousands separators are stripped out.
 * Scientific notation is also supported. If the supplied text does not convert
 * properly the result is **#VALUE!** error. Blank string converts to zero.
 * 
 * @author Josh Micich
 */
class Value : Fixed1ArgFunction() {
    override fun evaluate(srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?): ValueEval? {
        val veText: ValueEval?
        try {
            veText = getSingleValue(arg0, srcRowIndex, srcColumnIndex)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
        val strText = OperandResolver.coerceValueToString(veText!!)
        val result: Double? = Companion.convertTextToNumber(strText!!)
        if (result == null) {
            return ErrorEval.VALUE_INVALID
        }
        return NumberEval(result)
    }

    companion object {
        /** "1,0000" is valid, "1,00" is not  */
        private const val MIN_DISTANCE_BETWEEN_THOUSANDS_SEPARATOR = 4
        private const val ZERO = 0.0

        /**
         * TODO see if the same functionality is needed in [parseDouble]
         * 
         * @return `null` if there is any problem converting the text
         */
        private fun convertTextToNumber(strText: String): Double? {
            var foundCurrency = false
            var foundUnaryPlus = false
            var foundUnaryMinus = false

            val len = strText.length
            var i: Int
            i = 0
            while (i < len) {
                val ch = strText.get(i)
                if (Character.isDigit(ch) || ch == '.') {
                    break
                }
                when (ch) {
                    ' ' -> {
                        i++
                        // intervening spaces between '$', '-', '+' are OK
                        continue
                    }

                    '$' -> {
                        if (foundCurrency) {
                            // only one currency symbols is allowed
                            return null
                        }
                        foundCurrency = true
                        i++
                        continue
                    }

                    '+' -> {
                        if (foundUnaryMinus || foundUnaryPlus) {
                            return null
                        }
                        foundUnaryPlus = true
                        i++
                        continue
                    }

                    '-' -> {
                        if (foundUnaryMinus || foundUnaryPlus) {
                            return null
                        }
                        foundUnaryMinus = true
                        i++
                        continue
                    }

                    else ->                    // all other characters are illegal
                        return null
                }
                i++
            }
            if (i >= len) {
                // didn't find digits or '.'
                if (foundCurrency || foundUnaryMinus || foundUnaryPlus) {
                    return null
                }
                return ZERO
            }

            // remove thousands separators
            var foundDecimalPoint = false
            var lastThousandsSeparatorIndex = Short.MIN_VALUE.toInt()

            val sb = StringBuffer(len)
            while (i < len) {
                val ch = strText.get(i)
                if (Character.isDigit(ch)) {
                    sb.append(ch)
                    i++
                    continue
                }
                when (ch) {
                    ' ' -> {
                        val remainingText = strText.substring(i)
                        if (remainingText.trim { it <= ' ' }.length > 0) {
                            // intervening spaces not allowed once the digits start
                            return null
                        }
                    }

                    '.' -> {
                        if (foundDecimalPoint) {
                            return null
                        }
                        if (i - lastThousandsSeparatorIndex < MIN_DISTANCE_BETWEEN_THOUSANDS_SEPARATOR) {
                            return null
                        }
                        foundDecimalPoint = true
                        sb.append('.')
                        i++
                        continue
                    }

                    ',' -> {
                        if (foundDecimalPoint) {
                            // thousands separators not allowed after '.' or 'E'
                            return null
                        }
                        val distanceBetweenThousandsSeparators = i - lastThousandsSeparatorIndex
                        // as long as there are 3 or more digits between
                        if (distanceBetweenThousandsSeparators < MIN_DISTANCE_BETWEEN_THOUSANDS_SEPARATOR) {
                            return null
                        }
                        lastThousandsSeparatorIndex = i
                        i++
                        // don't append ','
                        continue
                    }

                    'E', 'e' -> {
                        if (i - lastThousandsSeparatorIndex < MIN_DISTANCE_BETWEEN_THOUSANDS_SEPARATOR) {
                            return null
                        }
                        // append rest of strText and skip to end of loop
                        sb.append(strText.substring(i))
                        i = len
                    }

                    else ->                    // all other characters are illegal
                        return null
                }
                i++
            }
            if (!foundDecimalPoint) {
                if (i - lastThousandsSeparatorIndex < MIN_DISTANCE_BETWEEN_THOUSANDS_SEPARATOR) {
                    return null
                }
            }
            val d: Double
            try {
                d = sb.toString().toDouble()
            } catch (e: NumberFormatException) {
                // still a problem parsing the number - probably out of range
                return null
            }
            return if (foundUnaryMinus) -d else d
        }
    }
}
