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
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver.getSingleValue
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver.parseDouble
import com.wxiwei.office.fc.hssf.formula.eval.RefEval
import com.wxiwei.office.fc.hssf.formula.eval.StringEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.fc.hssf.formula.function.CountUtils.I_MatchPredicate
import com.wxiwei.office.fc.ss.usermodel.ErrorConstants
import java.util.regex.Pattern
import kotlin.IllegalArgumentException
import kotlin.Int
import kotlin.RuntimeException
import kotlin.String

/**
 * Implementation for the function COUNTIF
 * 
 * 
 * Syntax: COUNTIF ( range, criteria )
 * <table border="0" cellpadding="1" cellspacing="0" summary="Parameter descriptions">
 * <tr><th>range&nbsp;&nbsp;&nbsp;</th><td>is the range of cells to be counted based on the criteria</td></tr>
 * <tr><th>criteria</th><td>is used to determine which cells to count</td></tr>
</table> * 
 * 
 * 
 * @author Josh Micich
 */
class Countif : Fixed2ArgFunction() {
    private class CmpOp(val representation: String, val code: Int) {
        val length: Int
            /**
             * @return number of characters used to represent this operator
             */
            get() = representation.length

        fun evaluate(cmpResult: Boolean): Boolean {
            when (this.code) {
                NONE, EQ -> return cmpResult
                NE -> return !cmpResult
            }
            throw RuntimeException(
                ("Cannot call boolean evaluate on non-equality operator '"
                        + this.representation + "'")
            )
        }

        fun evaluate(cmpResult: Int): Boolean {
            when (this.code) {
                NONE, EQ -> return cmpResult == 0
                NE -> return cmpResult != 0
                LT -> return cmpResult < 0
                LE -> return cmpResult <= 0
                GT -> return cmpResult > 0
                GE -> return cmpResult <= 0
            }
            throw RuntimeException(
                ("Cannot call boolean evaluate on non-equality operator '"
                        + this.representation + "'")
            )
        }

        override fun toString(): String {
            val sb = StringBuffer(64)
            sb.append(javaClass.getName())
            sb.append(" [").append(this.representation).append("]")
            return sb.toString()
        }

        companion object {
            const val NONE: Int = 0
            const val EQ: Int = 1
            const val NE: Int = 2
            const val LE: Int = 3
            const val LT: Int = 4
            const val GT: Int = 5
            const val GE: Int = 6

            val OP_NONE: CmpOp = op("", NONE)
            val OP_EQ: CmpOp = op("=", EQ)
            val OP_NE: CmpOp = op("<>", NE)
            val OP_LE: CmpOp = op("<=", LE)
            val OP_LT: CmpOp = op("<", LT)
            val OP_GT: CmpOp = op(">", GT)
            val OP_GE: CmpOp = op(">=", GE)
            private fun op(rep: String, code: Int): CmpOp {
                return CmpOp(rep, code)
            }

            fun getOperator(value: String): CmpOp {
                val len = value.length
                if (len < 1) {
                    return OP_NONE
                }

                val firstChar = value.get(0)

                when (firstChar) {
                    '=' -> return OP_EQ
                    '>' -> {
                        if (len > 1) {
                            when (value.get(1)) {
                                '=' -> return OP_GE
                            }
                        }
                        return OP_GT
                    }

                    '<' -> {
                        if (len > 1) {
                            when (value.get(1)) {
                                '=' -> return OP_LE
                                '>' -> return OP_NE
                            }
                        }
                        return OP_LT
                    }
                }
                return OP_NONE
            }
        }
    }

    private abstract class MatcherBase(private val _operator: CmpOp) : I_MatchPredicate {
        protected val code: Int
            get() = _operator.code

        protected fun evaluate(cmpResult: Int): Boolean {
            return _operator.evaluate(cmpResult)
        }

        protected fun evaluate(cmpResult: Boolean): Boolean {
            return _operator.evaluate(cmpResult)
        }

        override fun toString(): String {
            val sb = StringBuffer(64)
            sb.append(javaClass.getName()).append(" [")
            sb.append(_operator.representation)
            sb.append(this.valueText)
            sb.append("]")
            return sb.toString()
        }

        protected abstract val valueText: String?
    }

    private class NumberMatcher(private val _value: Double, operator: CmpOp) :
        MatcherBase(operator) {
        override val valueText: String
            get() {
            return _value.toString()
        }

        override fun matches(x: ValueEval?): Boolean {
            val testValue: Double
            if (x is StringEval) {
                // if the target(x) is a string, but parses as a number
                // it may still count as a match, only for the equality operator
                when (this.code) {
                    CmpOp.EQ, CmpOp.NONE -> {}
                    CmpOp.NE ->                        // Always matches (inconsistent with above two cases).
                        // for example '<>123' matches '123', '4', 'abc', etc
                        return true

                    else ->                        // never matches (also inconsistent with above three cases).
                        // for example '>5' does not match '6',
                        return false
                }
                val se = x
                val `val`: Double? = parseDouble(se.stringValue)
                if (`val` == null) {
                    // x is text that is not a number
                    return false
                }
                return _value == `val`
            } else if ((x is NumberEval)) {
                val ne = x
                testValue = ne.numberValue
            } else {
                return false
            }
            return evaluate(testValue.compareTo(_value))
        }
    }

    private class BooleanMatcher(value: Boolean, operator: CmpOp) : MatcherBase(operator) {
        private val _value: Int

        init {
            _value = boolToInt(value)
        }

        override val valueText: String
            get() {
            return if (_value == 1) "TRUE" else "FALSE"
        }

        override fun matches(x: ValueEval?): Boolean {
            val testValue: Int
            if (x is StringEval) {
                if (true) { // change to false to observe more intuitive behaviour
                    // Note - Unlike with numbers, it seems that COUNTIF never matches
                    // boolean values when the target(x) is a string
                    return false
                }
                val se = x
                val `val`: Boolean? = parseBoolean(se.stringValue)
                if (`val` == null) {
                    // x is text that is not a boolean
                    return false
                }
                testValue = boolToInt(`val`)
            } else if ((x is BoolEval)) {
                val be = x
                testValue = boolToInt(be.booleanValue)
            } else {
                return false
            }
            return evaluate(testValue - _value)
        }

        companion object {
            private fun boolToInt(value: Boolean): Int {
                return if (value) 1 else 0
            }
        }
    }

    private class ErrorMatcher(private val _value: Int, operator: CmpOp) : MatcherBase(operator) {
        override val valueText: String
            get() {
            return ErrorConstants.getText(_value)
        }

        override fun matches(x: ValueEval?): Boolean {
            if (x is ErrorEval) {
                val testValue = x.errorCode
                return evaluate(testValue - _value)
            }
            return false
        }
    }

    private class StringMatcher(private val _value: String, operator: CmpOp) :
        MatcherBase(operator) {
        private val _pattern: Pattern?

        init {
            when (operator.code) {
                CmpOp.NONE, CmpOp.EQ, CmpOp.NE -> _pattern = getWildCardPattern(
                    _value
                )

                else ->                    // pattern matching is never used for < > <= =>
                    _pattern = null
            }
        }

        override val valueText: String?
            get() {
            if (_pattern == null) {
                return _value
            }
            return _pattern.pattern()
        }

        override fun matches(x: ValueEval?): Boolean {
            if (x is BlankEval) {
                when (this.code) {
                    CmpOp.NONE, CmpOp.EQ -> return _value.length == 0
                }
                // no other criteria matches a blank cell
                return false
            }
            if (x !is StringEval) {
                // must always be string
                // even if match str is wild, but contains only digits
                // e.g. '4*7', NumberEval(4567) does not match
                return false
            }
            val testedValue = x.stringValue
            if (testedValue.length < 1 && _value.length < 1) {
                // odd case: criteria '=' behaves differently to criteria ''

                when (this.code) {
                    CmpOp.NONE -> return true
                    CmpOp.EQ -> return false
                    CmpOp.NE -> return true
                }
                return false
            }
            if (_pattern != null) {
                return evaluate(_pattern.matcher(testedValue).matches())
            }
            return evaluate(testedValue.compareTo(_value))
        }

        companion object {
            /**
             * Translates Excel countif wildcard strings into java regex strings
             * @return `null` if the specified value contains no special wildcard characters.
             */
            private fun getWildCardPattern(value: String): Pattern? {
                val len = value.length
                val sb = StringBuffer(len)
                var hasWildCard = false
                var i = 0
                while (i < len) {
                    var ch = value.get(i)
                    when (ch) {
                        '?' -> {
                            hasWildCard = true
                            // match exactly one character
                            sb.append('.')
                            i++
                            continue
                        }

                        '*' -> {
                            hasWildCard = true
                            // match one or more occurrences of any character
                            sb.append(".*")
                            i++
                            continue
                        }

                        '~' -> {
                            if (i + 1 < len) {
                                ch = value.get(i + 1)
                                when (ch) {
                                    '?', '*' -> {
                                        hasWildCard = true
                                        sb.append('[').append(ch).append(']')
                                        i++ // Note - incrementing loop variable here
                                        i++
                                        continue
                                    }
                                }
                            }
                            // else not '~?' or '~*'
                            sb.append('~') // just plain '~'
                            i++
                            continue
                        }

                        '.', '$', '^', '[', ']', '(', ')' -> {
                            // escape literal characters that would have special meaning in regex
                            sb.append("\\").append(ch)
                            i++
                            continue
                        }
                    }
                    sb.append(ch)
                    i++
                }
                if (hasWildCard) {
                    return Pattern.compile(sb.toString())
                }
                return null
            }
        }
    }

    override fun evaluate(
        srcRowIndex: Int,
        srcColumnIndex: Int,
        arg0: ValueEval?,
        arg1: ValueEval?
    ): ValueEval {
        val mp: I_MatchPredicate? = createCriteriaPredicate(arg1, srcRowIndex, srcColumnIndex)
        if (mp == null) {
            // If the criteria arg is a reference to a blank cell, countif always returns zero.
            return NumberEval.ZERO
        }
        val result = countMatchingCellsInArea(arg0, mp)
        return NumberEval(result)
    }

    /**
     * @return the number of evaluated cells in the range that match the specified criteria
     */
    private fun countMatchingCellsInArea(
        rangeArg: ValueEval?,
        criteriaPredicate: I_MatchPredicate
    ): kotlin.Double {
        if (rangeArg is RefEval) {
            return CountUtils.countMatchingCell(rangeArg, criteriaPredicate).toDouble()
        } else if (rangeArg is TwoDEval) {
            return CountUtils.countMatchingCellsInArea(rangeArg, criteriaPredicate).toDouble()
        } else {
            throw IllegalArgumentException("Bad range arg type (" + rangeArg!!.javaClass.getName() + ")")
        }
    }

    companion object {
        /**
         * Creates a criteria predicate object for the supplied criteria arg
         * @return `null` if the arg evaluates to blank.
         */
        /* package */
        fun createCriteriaPredicate(
            arg: ValueEval?,
            srcRowIndex: Int,
            srcColumnIndex: Int
        ): I_MatchPredicate? {
            val evaluatedCriteriaArg: ValueEval =
                evaluateCriteriaArg(arg, srcRowIndex, srcColumnIndex)

            if (evaluatedCriteriaArg is NumberEval) {
                return NumberMatcher(evaluatedCriteriaArg.numberValue, CmpOp.OP_NONE)
            }
            if (evaluatedCriteriaArg is BoolEval) {
                return BooleanMatcher(evaluatedCriteriaArg.booleanValue, CmpOp.OP_NONE)
            }

            if (evaluatedCriteriaArg is StringEval) {
                return createGeneralMatchPredicate(evaluatedCriteriaArg)
            }
            if (evaluatedCriteriaArg is ErrorEval) {
                return ErrorMatcher(evaluatedCriteriaArg.errorCode, CmpOp.OP_NONE)
            }
            if (evaluatedCriteriaArg === BlankEval.instance) {
                return null
            }
            throw RuntimeException(
                ("Unexpected type for criteria ("
                        + evaluatedCriteriaArg.javaClass.getName() + ")")
            )
        }

        /**
         * 
         * @return the de-referenced criteria arg (possibly [ErrorEval])
         */
        private fun evaluateCriteriaArg(
            arg: ValueEval?,
            srcRowIndex: Int,
            srcColumnIndex: Int
        ): ValueEval {
            try {
                return getSingleValue(arg, srcRowIndex, srcColumnIndex.toShort().toInt())!!
            } catch (e: EvaluationException) {
                return e.errorEval!!
            }
        }

        /**
         * When the second argument is a string, many things are possible
         */
        private fun createGeneralMatchPredicate(stringEval: StringEval): I_MatchPredicate {
            var value = stringEval.stringValue
            val operator = CmpOp.getOperator(value)
            value = value.substring(operator.length)

            val booleanVal: Boolean? = parseBoolean(value)
            if (booleanVal != null) {
                return BooleanMatcher(booleanVal, operator)
            }

            val doubleVal: kotlin.Double? = parseDouble(value)
            if (doubleVal != null) {
                return NumberMatcher(doubleVal, operator)
            }
            val ee: ErrorEval? = parseError(value)
            if (ee != null) {
                return ErrorMatcher(ee.errorCode, operator)
            }

            //else - just a plain string with no interpretation.
            return StringMatcher(value, operator)
        }

        private fun parseError(value: String): ErrorEval? {
            if (value.length < 4 || value.get(0) != '#') {
                return null
            }
            if (value == "#NULL!") return ErrorEval.NULL_INTERSECTION
            if (value == "#DIV/0!") return ErrorEval.DIV_ZERO
            if (value == "#VALUE!") return ErrorEval.VALUE_INVALID
            if (value == "#REF!") return ErrorEval.REF_INVALID
            if (value == "#NAME?") return ErrorEval.NAME_INVALID
            if (value == "#NUM!") return ErrorEval.NUM_ERROR
            if (value == "#N/A") return ErrorEval.NA

            return null
        }

        /**
         * Boolean literals ('TRUE', 'FALSE') treated similarly but NOT same as numbers.
         */
        /* package */
        fun parseBoolean(strRep: String): Boolean? {
            if (strRep.length < 1) {
                return null
            }
            when (strRep.get(0)) {
                't', 'T' -> if ("TRUE".equals(strRep, ignoreCase = true)) {
                    return true
                }

                'f', 'F' -> if ("FALSE".equals(strRep, ignoreCase = true)) {
                    return false
                }
            }
            return null
        }
    }
}
