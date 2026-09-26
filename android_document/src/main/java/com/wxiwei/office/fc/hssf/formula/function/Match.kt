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
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.EvaluationException
import com.wxiwei.office.fc.hssf.formula.eval.NumberEval
import com.wxiwei.office.fc.hssf.formula.eval.NumericValueEval
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver.getSingleValue
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver.parseDouble
import com.wxiwei.office.fc.hssf.formula.eval.RefEval
import com.wxiwei.office.fc.hssf.formula.eval.StringEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.fc.hssf.formula.function.LookupUtils.LookupValueComparer
import com.wxiwei.office.fc.hssf.formula.function.LookupUtils.ValueVector


/**
 * Implementation for the MATCH() Excel function.
 *
 *
 * 
 * **Syntax:**<br></br>
 * **MATCH**(**lookup_value**, **lookup_array**, match_type)
 *
 *
 * 
 * Returns a 1-based index specifying at what position in the **lookup_array** the specified
 * **lookup_value** is found.
 *
 *
 * 
 * Specific matching behaviour can be modified with the optional **match_type** parameter.
 * 
 * <table border="0" cellpadding="1" cellspacing="0" summary="match_type parameter description">
 * <tr><th>Value</th><th>Matching Behaviour</th></tr>
 * <tr><td>1</td><td>(default) find the largest value that is less than or equal to lookup_value.
 * The lookup_array must be in ascending *order**.</td></tr>
 * <tr><td>0</td><td>find the first value that is exactly equal to lookup_value.
 * The lookup_array can be in any order.</td></tr>
 * <tr><td>-1</td><td>find the smallest value that is greater than or equal to lookup_value.
 * The lookup_array must be in descending *order**.</td></tr>
</table> * 
 * 
 * * Note regarding *order* - For the **match_type** cases that require the lookup_array to
 * be ordered, MATCH() can produce incorrect results if this requirement is not met.  Observed
 * behaviour in Excel is to return the lowest index value for which every item after that index
 * breaks the match rule.<br></br>
 * The (ascending) sort order expected by MATCH() is:<br></br>
 * numbers (low to high), strings (A to Z), boolean (FALSE to TRUE)<br></br>
 * MATCH() ignores all elements in the lookup_array with a different type to the lookup_value.
 * Type conversion of the lookup_array elements is never performed.
 * 
 * 
 * @author Josh Micich
 */
class Match : Var2or3ArgFunction() {
    override fun evaluate(
        srcRowIndex: Int,
        srcColumnIndex: Int,
        arg0: ValueEval?,
        arg1: ValueEval?
    ): ValueEval? {
        // default match_type is 1.0
        return eval(srcRowIndex, srcColumnIndex, arg0, arg1, 1.0)
    }


    override fun evaluate(
        srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?, arg1: ValueEval?,
        arg2: ValueEval?
    ): ValueEval? {
        val match_type: Double

        try {
            match_type = evaluateMatchTypeArg(arg2, srcRowIndex, srcColumnIndex)
        } catch (e: EvaluationException) {
            // Excel/MATCH() seems to have slightly abnormal handling of errors with
            // the last parameter.  Errors do not propagate up.  Every error gets
            // translated into #REF!
            return ErrorEval.REF_INVALID
        }

        return eval(srcRowIndex, srcColumnIndex, arg0, arg1, match_type)
    }

    private class SingleValueVector(private val _value: ValueEval?) : ValueVector {
        override fun getItem(index: Int): ValueEval? {
            if (index != 0) {
                throw RuntimeException(
                    ("Invalid index ("
                            + index + ") only zero is allowed")
                )
            }
            return _value
        }

        override val size: Int
            get() {
            return 1
        }
    }

    companion object {
        private fun eval(
            srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?, arg1: ValueEval?,
            match_type: Double
        ): ValueEval? {
            val matchExact = match_type == 0.0
            // Note - Excel does not strictly require -1 and +1
            val findLargestLessThanOrEqual = match_type > 0

            try {
                val lookupValue = getSingleValue(arg0, srcRowIndex, srcColumnIndex)
                val lookupRange: ValueVector = evaluateLookupRange(arg1)
                val index: Int = findIndexOfValue(
                    lookupValue,
                    lookupRange,
                    matchExact,
                    findLargestLessThanOrEqual
                )
                return NumberEval((index + 1).toDouble()) // +1 to convert to 1-based
            } catch (e: EvaluationException) {
                return e.errorEval
            }
        }

        @Throws(EvaluationException::class)
        private fun evaluateLookupRange(eval: ValueEval?): ValueVector {
            if (eval is RefEval) {
                val re = eval
                return SingleValueVector(re.innerValueEval)
            }
            if (eval is TwoDEval) {
                val result = LookupUtils.createVector(eval)
                if (result == null) {
                    throw EvaluationException(ErrorEval.NA)
                }
                return result
            }

            // Error handling for lookup_range arg is also unusual
            if (eval is NumericValueEval) {
                throw EvaluationException(ErrorEval.NA)
            }
            if (eval is StringEval) {
                val se = eval
                val d: Double? = parseDouble(se.stringValue)
                if (d == null) {
                    // plain string
                    throw EvaluationException(ErrorEval.VALUE_INVALID)
                }
                // else looks like a number
                throw EvaluationException(ErrorEval.NA)
            }
            throw RuntimeException("Unexpected eval type (" + eval!!.javaClass.getName() + ")")
        }


        @Throws(EvaluationException::class)
        private fun evaluateMatchTypeArg(
            arg: ValueEval?,
            srcCellRow: Int,
            srcCellCol: Int
        ): Double {
            val match_type = getSingleValue(arg, srcCellRow, srcCellCol)

            if (match_type is ErrorEval) {
                throw EvaluationException(match_type)
            }
            if (match_type is NumericValueEval) {
                val ne = match_type
                return ne.numberValue
            }
            if (match_type is StringEval) {
                val se = match_type
                val d: Double? = parseDouble(se.stringValue)
                if (d == null) {
                    // plain string
                    throw EvaluationException(ErrorEval.VALUE_INVALID)
                }
                // if the string parses as a number, it is OK
                return d
            }
            throw RuntimeException("Unexpected match_type type (" + match_type!!.javaClass.getName() + ")")
        }

        /**
         * @return zero based index
         */
        @Throws(EvaluationException::class)
        private fun findIndexOfValue(
            lookupValue: ValueEval, lookupRange: ValueVector,
            matchExact: Boolean, findLargestLessThanOrEqual: Boolean
        ): Int {
            val lookupComparer: LookupValueComparer = createLookupComparer(lookupValue, matchExact)

            val size = lookupRange.size
            if (matchExact) {
                for (i in 0..<size) {
                    if (lookupComparer.compareTo(lookupRange.getItem(i)).isEqual) {
                        return i
                    }
                }
                throw EvaluationException(ErrorEval.NA)
            }

            if (findLargestLessThanOrEqual) {
                // Note - backward iteration
                for (i in size - 1 downTo 0) {
                    val cmp = lookupComparer.compareTo(lookupRange.getItem(i))
                    if (cmp.isTypeMismatch) {
                        continue
                    }
                    if (!cmp.isLessThan) {
                        return i
                    }
                }
                throw EvaluationException(ErrorEval.NA)
            }

            // else - find smallest greater than or equal to
            // TODO - is binary search used for (match_type==+1) ?
            for (i in 0..<size) {
                val cmp = lookupComparer.compareTo(lookupRange.getItem(i))
                if (cmp.isEqual) {
                    return i
                }
                if (cmp.isGreaterThan) {
                    if (i < 1) {
                        throw EvaluationException(ErrorEval.NA)
                    }
                    return i - 1
                }
            }

            throw EvaluationException(ErrorEval.NA)
        }

        private fun createLookupComparer(
            lookupValue: ValueEval,
            matchExact: Boolean
        ): LookupValueComparer {
            if (matchExact && lookupValue is StringEval) {
                val stringValue = lookupValue.stringValue
                if (isLookupValueWild(stringValue)) {
                    throw RuntimeException("Wildcard lookup values '" + stringValue + "' not supported yet")
                }
            }
            return LookupUtils.createLookupComparer(lookupValue)
        }

        private fun isLookupValueWild(stringValue: String): Boolean {
            if (stringValue.indexOf('?') >= 0 || stringValue.indexOf('*') >= 0) {
                return true
            }
            return false
        }
    }
}
