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

import com.wxiwei.office.fc.hssf.formula.eval.BoolEval
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.EvaluationException
import com.wxiwei.office.fc.hssf.formula.eval.NumberEval
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver.getSingleValue
import com.wxiwei.office.fc.hssf.formula.eval.StringEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.fc.ss.usermodel.DataFormatter
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * @author Josh Micich
 * @author Stephen Wolke (smwolke at geistig.com)
 */
abstract class TextFunction : Function {
    override fun evaluate(args: Array<ValueEval?>, srcCellRow: Int, srcCellCol: Int): ValueEval? {
        try {
            return evaluateFunc(args, srcCellRow, srcCellCol)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
    }

    @Throws(EvaluationException::class)
    protected abstract fun evaluateFunc(
        args: Array<ValueEval?>,
        srcCellRow: Int,
        srcCellCol: Int
    ): ValueEval?

    /* ---------------------------------------------------------------------- */
    private abstract class SingleArgTextFunc protected constructor() : Fixed1ArgFunction() {
        override fun evaluate(srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?): ValueEval? {
            val arg: String
            try {
                arg = evaluateStringArg(arg0, srcRowIndex, srcColumnIndex)
            } catch (e: EvaluationException) {
                return e.errorEval
            }
            return evaluate(arg)
        }

        protected abstract fun evaluate(arg: String): ValueEval?
    }

    private class LeftRight(private val _isLeft: Boolean) : Var1or2ArgFunction() {
        override fun evaluate(srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?): ValueEval? {
            return evaluate(srcRowIndex, srcColumnIndex, arg0, DEFAULT_ARG1)
        }

        override fun evaluate(
            srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?,
            arg1: ValueEval?
        ): ValueEval? {
            val arg: String?
            val index: Int
            try {
                arg = evaluateStringArg(arg0, srcRowIndex, srcColumnIndex)
                index = evaluateIntArg(arg1, srcRowIndex, srcColumnIndex)
            } catch (e: EvaluationException) {
                return e.errorEval
            }

            if (index < 0) {
                return ErrorEval.VALUE_INVALID
            }

            val result: String?
            if (_isLeft) {
                result = arg!!.substring(0, min(arg.length, index))
            } else {
                result = arg!!.substring(max(0, arg.length - index))
            }
            return StringEval(result)
        }

        companion object {
            private val DEFAULT_ARG1: ValueEval = NumberEval(1.0)
        }
    }

    private class SearchFind(private val _isCaseSensitive: Boolean) : Var2or3ArgFunction() {
        override fun evaluate(
            srcRowIndex: Int,
            srcColumnIndex: Int,
            arg0: ValueEval?,
            arg1: ValueEval?
        ): ValueEval? {
            try {
                val needle: String? = evaluateStringArg(arg0, srcRowIndex, srcColumnIndex)
                val haystack: String? = evaluateStringArg(arg1, srcRowIndex, srcColumnIndex)
                return eval(haystack!!, needle!!, 0)
            } catch (e: EvaluationException) {
                return e.errorEval
            }
        }

        override fun evaluate(
            srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?, arg1: ValueEval?,
            arg2: ValueEval?
        ): ValueEval? {
            try {
                val needle: String? = evaluateStringArg(arg0, srcRowIndex, srcColumnIndex)
                val haystack: String? = evaluateStringArg(arg1, srcRowIndex, srcColumnIndex)
                // evaluate third arg and convert from 1-based to 0-based index
                val startpos: Int = evaluateIntArg(arg2, srcRowIndex, srcColumnIndex) - 1
                if (startpos < 0) {
                    return ErrorEval.VALUE_INVALID
                }
                return eval(haystack!!, needle!!, startpos)
            } catch (e: EvaluationException) {
                return e.errorEval
            }
        }

        fun eval(haystack: String, needle: String, startIndex: Int): ValueEval {
            val result: Int
            if (_isCaseSensitive) {
                result = haystack.indexOf(needle, startIndex)
            } else {
                result = haystack.uppercase(Locale.getDefault())
                    .indexOf(needle.uppercase(Locale.getDefault()), startIndex)
            }
            if (result == -1) {
                return ErrorEval.VALUE_INVALID
            }
            return NumberEval((result + 1).toDouble())
        }
    }

    companion object {
        protected const val EMPTY_STRING: String = ""

        @Throws(EvaluationException::class)
        fun evaluateStringArg(eval: ValueEval?, srcRow: Int, srcCol: Int): String {
            val ve = getSingleValue(eval, srcRow, srcCol)
            return OperandResolver.coerceValueToString(ve!!)
        }

        @Throws(EvaluationException::class)
        fun evaluateIntArg(arg: ValueEval?, srcCellRow: Int, srcCellCol: Int): Int {
            val ve = getSingleValue(arg, srcCellRow, srcCellCol)
            return OperandResolver.coerceValueToInt(ve!!)
        }

        @Throws(EvaluationException::class)
        protected fun evaluateDoubleArg(arg: ValueEval?, srcCellRow: Int, srcCellCol: Int): Double {
            val ve = getSingleValue(arg, srcCellRow, srcCellCol)
            return OperandResolver.coerceValueToDouble(ve!!)
        }

        /**
         * Returns the character specified by a number.
         */
        val CHAR: Function = object : Fixed1ArgFunction() {
            override fun evaluate(
                srcRowIndex: Int,
                srcColumnIndex: Int,
                arg0: ValueEval?
            ): ValueEval? {
                val arg: Int
                try {
                    arg = evaluateIntArg(arg0, srcRowIndex, srcColumnIndex)
                    if (arg < 0 || arg >= 256) {
                        throw EvaluationException(ErrorEval.VALUE_INVALID)
                    }
                } catch (e: EvaluationException) {
                    return e.errorEval
                }
                return StringEval(arg.toChar().toString())
            }
        }

        val CODE: Function = object : SingleArgTextFunc() {
            override fun evaluate(arg: String): ValueEval {
                return NumberEval(arg.codePointAt(0).toDouble())
            }
        }

        val LEN: Function = object : SingleArgTextFunc() {
            override fun evaluate(arg: String): ValueEval {
                return NumberEval(arg.length.toDouble())
            }
        }
        val LOWER: Function = object : SingleArgTextFunc() {
            override fun evaluate(arg: String): ValueEval {
                return StringEval(arg.lowercase(Locale.getDefault()))
            }
        }
        val UPPER: Function = object : SingleArgTextFunc() {
            override fun evaluate(arg: String): ValueEval {
                return StringEval(arg.uppercase(Locale.getDefault()))
            }
        }

        /**
         * An implementation of the TRIM function:
         * Removes leading and trailing spaces from value if evaluated operand
         * value is string.
         * Author: Manda Wilson &lt; wilson at c bio dot msk cc dot org &gt;
         */
        val TRIM: Function = object : SingleArgTextFunc() {
            override fun evaluate(arg: String): ValueEval {
                return StringEval(arg.trim { it <= ' ' })
            }
        }

        /**
         * An implementation of the CLEAN function:
         * In Excel, the Clean function removes all non-printable characters from a string.
         * 
         * Author: Aniket Banerjee(banerjee@google.com)
         */
        val CLEAN: Function = object : SingleArgTextFunc() {
            override fun evaluate(arg: String): ValueEval {
                val result = StringBuilder()
                for (i in 0..<arg.length) {
                    val c = arg.get(i)
                    if (isPrintable(c)) {
                        result.append(c)
                    }
                }
                return StringEval(result.toString())
            }

            /**
             * From Excel docs: The CLEAN function was designed to remove the first 32 nonprinting characters
             * in the 7-bit ASCII code (values 0 through 31) from text. In the Unicode character set,
             * there are additional nonprinting characters (values 127, 129, 141, 143, 144, and 157). By itself,
             * the CLEAN function does not remove these additional  nonprinting characters. To do this task,
             * use the SUBSTITUTE function to replace the higher value Unicode characters with the 7-bit ASCII
             * characters for which the TRIM and CLEAN functions were designed.
             * 
             * @param c the character to test
             * @return  whether the character is printable
             */
            private fun isPrintable(c: Char): Boolean {
                val charCode = c.code
                return charCode >= 32
            }
        }

        /**
         * An implementation of the MID function<br></br>
         * MID returns a specific number of
         * characters from a text string, starting at the specified position.
         *
         *
         * 
         * **Syntax**:<br></br> **MID**(**text**, **start_num**,
         * **num_chars**)<br></br>
         * 
         * Author: Manda Wilson &lt; wilson at c bio dot msk cc dot org &gt;
         **** */
        val MID: Function = object : Fixed3ArgFunction() {
            override fun evaluate(
                srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?,
                arg1: ValueEval?, arg2: ValueEval?
            ): ValueEval? {
                val text: String?
                val startCharNum: Int
                val numChars: Int
                try {
                    text = evaluateStringArg(arg0, srcRowIndex, srcColumnIndex)
                    startCharNum = evaluateIntArg(arg1, srcRowIndex, srcColumnIndex)
                    numChars = evaluateIntArg(arg2, srcRowIndex, srcColumnIndex)
                } catch (e: EvaluationException) {
                    return e.errorEval
                }
                val startIx = startCharNum - 1 // convert to zero-based

                // Note - for start_num arg, blank/zero causes error(#VALUE!),
                // but for num_chars causes empty string to be returned.
                if (startIx < 0) {
                    return ErrorEval.VALUE_INVALID
                }
                if (numChars < 0) {
                    return ErrorEval.VALUE_INVALID
                }
                val len = text!!.length
                if (numChars < 0 || startIx > len) {
                    return StringEval("")
                }
                val endIx = min(startIx + numChars, len)
                val result = text.substring(startIx, endIx)
                return StringEval(result)
            }
        }

        val LEFT: Function = LeftRight(true)
        val RIGHT: Function = LeftRight(false)

        val CONCATENATE: Function = object : Function {
            override fun evaluate(
                args: Array<ValueEval?>,
                srcRowIndex: Int,
                srcColumnIndex: Int
            ): ValueEval? {
                val sb = StringBuilder()
                var i = 0
                val iSize = args.size
                while (i < iSize) {
                    try {
                        sb.append(evaluateStringArg(args[i], srcRowIndex, srcColumnIndex))
                    } catch (e: EvaluationException) {
                        return e.errorEval
                    }
                    i++
                }
                return StringEval(sb.toString())
            }
        }

        val EXACT: Function = object : Fixed2ArgFunction() {
            override fun evaluate(
                srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?,
                arg1: ValueEval?
            ): ValueEval? {
                val s0: String?
                val s1: String?
                try {
                    s0 = evaluateStringArg(arg0, srcRowIndex, srcColumnIndex)
                    s1 = evaluateStringArg(arg1, srcRowIndex, srcColumnIndex)
                } catch (e: EvaluationException) {
                    return e.errorEval
                }
                return BoolEval.valueOf(s0 == s1)
            }
        }

        /**
         * An implementation of the TEXT function<br></br>
         * TEXT returns a number value formatted with the given
         * number formatting string. This function is not a complete implementation of
         * the Excel function.  This function implements decimal formatting
         * with the Java class DecimalFormat.  For date formatting this function uses
         * the SimpleDateFormat class.
         *
         *
         * 
         * **Syntax**:<br></br> **TEXT**(**value**, **format_text**)<br></br>
         * 
         **** */
        private val EXCEL_FORMATTER = DataFormatter(Locale.US)

        val TEXT: Function = object : Fixed2ArgFunction() {
            override fun evaluate(
                srcRowIndex: Int,
                srcColumnIndex: Int,
                arg0: ValueEval?,
                arg1: ValueEval?
            ): ValueEval? {
                val s0: Double
                val s1: String?
                try {
                    s0 = evaluateDoubleArg(arg0, srcRowIndex, srcColumnIndex)
                    s1 = evaluateStringArg(arg1, srcRowIndex, srcColumnIndex)
                } catch (e: EvaluationException) {
                    return e.errorEval
                }
                // Excel format rules (like newer POI): "mm" after "yyyy" is a month, not minutes,
                // and number formats follow Excel, not DecimalFormat. Old code below is the fallback.
                try {
                    return StringEval(EXCEL_FORMATTER.formatRawCellContents(s0, -1, s1))
                } catch (ignored: Exception) {
                }
                if (s1!!.matches("[\\d,\\#,\\.,\\$,\\,]+".toRegex())) {
                    val formatter: NumberFormat = DecimalFormat(s1)
                    return StringEval(formatter.format(s0))
                } else if (s1.indexOf("/") == s1.lastIndexOf("/") && s1.indexOf("/") >= 0 && !s1.contains(
                        "-"
                    )
                ) {
                    val wholePart = floor(s0)
                    val decPart = s0 - wholePart
                    if (wholePart * decPart == 0.0) {
                        return StringEval("0")
                    }
                    val parts: Array<String?> =
                        s1.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val fractParts: Array<String?>?
                    if (parts.size == 2) {
                        fractParts = parts[1]!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                    } else {
                        fractParts =
                            s1.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    }

                    if (fractParts.size == 2) {
                        var minVal = 1.0
                        var currDenom = 10.0.pow(fractParts[1]!!.length.toDouble()) - 1.0
                        var currNeum = 0.0
                        for (i in (10.0.pow(fractParts[1]!!.length.toDouble()) - 1.0).toInt() downTo 1) {
                            for (i2 in (10.0.pow(fractParts[1]!!.length.toDouble()) - 1.0).toInt() downTo 1) {
                                if (minVal >= abs(i2.toDouble() / i.toDouble() - decPart)) {
                                    currDenom = i.toDouble()
                                    currNeum = i2.toDouble()
                                    minVal = abs(i2.toDouble() / i.toDouble() - decPart)
                                }
                            }
                        }
                        val neumFormatter: NumberFormat = DecimalFormat(fractParts[0])
                        val denomFormatter: NumberFormat = DecimalFormat(fractParts[1])
                        if (parts.size == 2) {
                            val wholeFormatter: NumberFormat = DecimalFormat(parts[0])
                            val result =
                                wholeFormatter.format(wholePart) + " " + neumFormatter.format(
                                    currNeum
                                ) + "/" + denomFormatter.format(currDenom)
                            return StringEval(result)
                        } else {
                            val result =
                                neumFormatter.format(currNeum + (currDenom * wholePart)) + "/" + denomFormatter.format(
                                    currDenom
                                )
                            return StringEval(result)
                        }
                    } else {
                        return ErrorEval.VALUE_INVALID
                    }
                } else {
                    try {
                        val dateFormatter: DateFormat = SimpleDateFormat(s1)
                        val cal: Calendar = GregorianCalendar(1899, 11, 30, 0, 0, 0)
                        cal.add(Calendar.DATE, floor(s0).toInt())
                        val dayFraction = s0 - floor(s0)
                        cal.add(
                            Calendar.MILLISECOND,
                            Math.round(dayFraction * 24 * 60 * 60 * 1000).toInt()
                        )
                        return StringEval(dateFormatter.format(cal.getTime()))
                    } catch (e: Exception) {
                        return ErrorEval.VALUE_INVALID
                    }
                }
            }
        }

        /**
         * Implementation of the FIND() function.
         *
         *
         * 
         * **Syntax**:<br></br>
         * **FIND**(**find_text**, **within_text**, start_num)
         *
         *
         * 
         * FIND returns the character position of the first (case sensitive) occurrence of
         * <tt>find_text</tt> inside <tt>within_text</tt>.  The third parameter,
         * <tt>start_num</tt>, is optional (default=1) and specifies where to start searching
         * from.  Character positions are 1-based.
         *
         *
         * 
         * Author: Torstein Tauno Svendsen (torstei@officenet.no)
         */
        val FIND: Function = SearchFind(true)

        /**
         * Implementation of the FIND() function.
         *
         *
         * 
         * **Syntax**:<br></br>
         * **SEARCH**(**find_text**, **within_text**, start_num)
         *
         *
         * 
         * SEARCH is a case-insensitive version of FIND()
         */
        val SEARCH: Function = SearchFind(false)
    }
}
