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
import com.wxiwei.office.fc.hssf.formula.WorkbookEvaluator
import com.wxiwei.office.fc.hssf.formula.eval.AreaEval
import com.wxiwei.office.fc.hssf.formula.eval.BlankEval
import com.wxiwei.office.fc.hssf.formula.eval.BoolEval
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.EvaluationException
import com.wxiwei.office.fc.hssf.formula.eval.NumberEval
import com.wxiwei.office.fc.hssf.formula.eval.NumericValueEval
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver.getSingleValue
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver.parseDouble
import com.wxiwei.office.fc.hssf.formula.eval.RefEval
import com.wxiwei.office.fc.hssf.formula.eval.StringEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import kotlin.Boolean
import kotlin.IllegalArgumentException
import kotlin.Int
import kotlin.RuntimeException
import kotlin.String
import kotlin.Throws
import kotlin.require
import kotlin.requireNotNull


/**
 * Common functionality used by VLOOKUP, HLOOKUP, LOOKUP and MATCH
 * 
 * @author Josh Micich
 */
internal object LookupUtils {
    fun createRowVector(tableArray: TwoDEval, relativeRowIndex: Int): ValueVector {
        return RowVector(tableArray, relativeRowIndex)
    }

    fun createColumnVector(tableArray: TwoDEval, relativeColumnIndex: Int): ValueVector {
        return ColumnVector(tableArray, relativeColumnIndex)
    }

    /**
     * @return `null` if the supplied area is neither a single row nor a single colum
     */
    fun createVector(ae: TwoDEval): ValueVector? {
        if (ae.isColumn) {
            return createColumnVector(ae, 0)
        }
        if (ae.isRow) {
            return createRowVector(ae, 0)
        }
        return null
    }

    /**
     * Processes the third argument to VLOOKUP, or HLOOKUP (**col_index_num**
     * or **row_index_num** respectively).<br></br>
     * Sample behaviour:
     * <table border="0" cellpadding="1" cellspacing="2" summary="Sample behaviour">
     * <tr><th>Input&nbsp;&nbsp;&nbsp;Return</th><th>Value&nbsp;&nbsp;</th><th>Thrown Error</th></tr>
     * <tr><td>5</td><td>4</td><td>&nbsp;</td></tr>
     * <tr><td>2.9</td><td>2</td><td>&nbsp;</td></tr>
     * <tr><td>"5"</td><td>4</td><td>&nbsp;</td></tr>
     * <tr><td>"2.18e1"</td><td>21</td><td>&nbsp;</td></tr>
     * <tr><td>"-$2"</td><td>-3</td><td>*</td></tr>
     * <tr><td>FALSE</td><td>-1</td><td>*</td></tr>
     * <tr><td>TRUE</td><td>0</td><td>&nbsp;</td></tr>
     * <tr><td>"TRUE"</td><td>&nbsp;</td><td>#REF!</td></tr>
     * <tr><td>"abc"</td><td>&nbsp;</td><td>#REF!</td></tr>
     * <tr><td>""</td><td>&nbsp;</td><td>#REF!</td></tr>
     * <tr><td>&lt;blank&gt;</td><td>&nbsp;</td><td>#VALUE!</td></tr>
    </table> * <br></br>
     * 
     * Note - out of range errors (result index too high) are handled by the caller.
     * @return column or row index as a zero-based value, never negative.
     * @throws EvaluationException when the specified arg cannot be coerced to a non-negative integer
     */
    @Throws(EvaluationException::class)
    fun resolveRowOrColIndexArg(rowColIndexArg: ValueEval?, srcCellRow: Int, srcCellCol: Int): Int {
        requireNotNull(rowColIndexArg) { "argument must not be null" }

        val veRowColIndexArg: ValueEval?
        try {
            veRowColIndexArg =
                getSingleValue(rowColIndexArg, srcCellRow, srcCellCol.toShort().toInt())
        } catch (e: EvaluationException) {
            // All errors get translated to #REF!
            throw EvaluationException.invalidRef()
        }
        val oneBasedIndex: Int
        if (veRowColIndexArg is StringEval) {
            val se = veRowColIndexArg
            val strVal = se.stringValue
            val dVal: Double? = parseDouble(strVal)
            if (dVal == null) {
                // String does not resolve to a number. Raise #REF! error.
                throw EvaluationException.invalidRef()
                // This includes text booleans "TRUE" and "FALSE".  They are not valid.
            }
            // else - numeric value parses OK
        }
        // actual BoolEval values get interpreted as FALSE->0 and TRUE->1
        oneBasedIndex = OperandResolver.coerceValueToInt(veRowColIndexArg!!)
        if (oneBasedIndex < 1) {
            // note this is asymmetric with the errors when the index is too large (#REF!)
            throw EvaluationException.invalidValue()
        }
        return oneBasedIndex - 1 // convert to zero based
    }


    /**
     * The second argument (table_array) should be an area ref, but can actually be a cell ref, in
     * which case it is interpreted as a 1x1 area ref.  Other scalar values cause #VALUE! error.
     */
    @Throws(EvaluationException::class)
    fun resolveTableArrayArg(eval: ValueEval?): TwoDEval {
        if (eval is TwoDEval) {
            return eval
        }

        if (eval is RefEval) {
            val refEval = eval

            // Make this cell ref look like a 1x1 area ref.

            // It doesn't matter if eval is a 2D or 3D ref, because that detail is never asked of AreaEval.
            return refEval.offset(0, 0, 0, 0)
        }
        throw EvaluationException.invalidValue()
    }


    /**
     * Resolves the last (optional) parameter (**range_lookup**) to the VLOOKUP and HLOOKUP functions.
     * @param rangeLookupArg must not be `null`
     */
    @Throws(EvaluationException::class)
    fun resolveRangeLookupArg(
        rangeLookupArg: ValueEval?,
        srcCellRow: Int,
        srcCellCol: Int
    ): Boolean {
        val valEval = getSingleValue(rangeLookupArg, srcCellRow, srcCellCol)
        if (valEval is BlankEval) {
            // Tricky:
            // fourth arg supplied but evaluates to blank
            // this does not get the default value
            return false
        }
        if (valEval is BoolEval) {
            // Happy day flow
            val boolEval = valEval
            return boolEval.booleanValue
        }

        if (valEval is StringEval) {
            val stringValue = valEval.stringValue
            if (stringValue.length < 1) {
                // More trickiness:
                // Empty string is not the same as BlankEval.  It causes #VALUE! error
                throw EvaluationException.invalidValue()
            }
            // TODO move parseBoolean to OperandResolver
            val b: Boolean? = Countif.Companion.parseBoolean(stringValue)
            if (b != null) {
                // string converted to boolean OK
                return b
            }
            // Even more trickiness:
            // Note - even if the StringEval represents a number value (for example "1"),
            // Excel does not resolve it to a boolean.
            throw EvaluationException.invalidValue()
            // This is in contrast to the code below,, where NumberEvals values (for
            // example 0.01) *do* resolve to equivalent boolean values.
        }
        if (valEval is NumericValueEval) {
            val nve = valEval
            // zero is FALSE, everything else is TRUE
            return 0.0 != nve.numberValue
        }
        throw RuntimeException("Unexpected eval type (" + valEval!!.javaClass.getName() + ")")
    }

    @Throws(EvaluationException::class)
    fun lookupIndexOfValue(
        lookupValue: ValueEval,
        vector: ValueVector,
        isRangeLookup: Boolean
    ): Int {
        val lookupComparer = createLookupComparer(lookupValue)
        val result: Int
        if (isRangeLookup) {
            result = performBinarySearch(vector, lookupComparer)
        } else {
            result = lookupIndexOfExactValue(lookupComparer, vector)
        }
        if (result < 0) {
            throw EvaluationException(ErrorEval.NA)
        }
        return result
    }


    @Throws(EvaluationException::class)
    fun lookupIndexOfValue(
        srcRowIndex: Int,
        srcColumnIndex: Int,
        lookupValue: ValueEval,
        vector: ValueVector,
        isRangeLookup: Boolean
    ): Int {
        val lookupComparer = createLookupComparer(srcRowIndex, srcColumnIndex, lookupValue)
        val result: Int
        if (isRangeLookup) {
            result = performBinarySearch(vector, lookupComparer)
        } else {
            result = lookupIndexOfExactValue(lookupComparer, vector)
        }
        if (result < 0) {
            throw EvaluationException(ErrorEval.NA)
        }
        return result
    }

    /**
     * Finds first (lowest index) exact occurrence of specified value.
     * @param lookupValue the value to be found in column or row vector
     * @param vector the values to be searched. For VLOOKUP this is the first column of the
     * tableArray. For HLOOKUP this is the first row of the tableArray.
     * @return zero based index into the vector, -1 if value cannot be found
     */
    private fun lookupIndexOfExactValue(
        lookupComparer: LookupValueComparer,
        vector: ValueVector
    ): Int {
        // find first occurrence of lookup value

        val size = vector.size
        for (i in 0..<size) {
            if (lookupComparer.compareTo(vector.getItem(i)).isEqual) {
                return i
            }
        }
        return -1
    }


    /**
     * Excel has funny behaviour when the some elements in the search vector are the wrong type.
     * 
     */
    private fun performBinarySearch(vector: ValueVector, lookupComparer: LookupValueComparer): Int {
        // both low and high indexes point to values assumed too low and too high.
        val bsi = BinarySearchIndexes(vector.size)

        while (true) {
            var midIx = bsi.midIx

            if (midIx < 0) {
                return bsi.lowIx
            }
            var cr = lookupComparer.compareTo(vector.getItem(midIx))
            if (cr.isTypeMismatch) {
                val newMidIx = handleMidValueTypeMismatch(lookupComparer, vector, bsi, midIx)
                if (newMidIx < 0) {
                    continue
                }
                midIx = newMidIx
                cr = lookupComparer.compareTo(vector.getItem(midIx))
            }
            if (cr.isEqual) {
                return findLastIndexInRunOfEqualValues(
                    lookupComparer, vector, midIx,
                    bsi.highIx
                )
            }
            bsi.narrowSearch(midIx, cr.isLessThan)
        }
    }

    /**
     * Excel seems to handle mismatched types initially by just stepping 'mid' ix forward to the
     * first compatible value.
     * @param midIx 'mid' index (value which has the wrong type)
     * @return usually -1, signifying that the BinarySearchIndex has been narrowed to the new mid
     * index.  Zero or greater signifies that an exact match for the lookup value was found
     */
    private fun handleMidValueTypeMismatch(
        lookupComparer: LookupValueComparer, vector: ValueVector,
        bsi: BinarySearchIndexes, midIx: Int
    ): Int {
        var newMid = midIx
        val highIx = bsi.highIx

        while (true) {
            newMid++
            if (newMid == highIx) {
                // every element from midIx to highIx was the wrong type
                // move highIx down to the low end of the mid values
                bsi.narrowSearch(midIx, true)
                return -1
            }
            val cr = lookupComparer.compareTo(vector.getItem(newMid))
            if (cr.isLessThan && newMid == highIx - 1) {
                // move highIx down to the low end of the mid values
                bsi.narrowSearch(midIx, true)
                return -1
                // but only when "newMid == highIx-1"? slightly weird.
                // It would seem more efficient to always do this.
            }
            if (cr.isTypeMismatch) {
                // keep stepping over values until the right type is found
                continue
            }
            if (cr.isEqual) {
                return newMid
            }
            // Note - if moving highIx down (due to lookup<vector[newMid]),
            // this execution path only moves highIx it down as far as newMid, not midIx,
            // which would be more efficient.
            bsi.narrowSearch(newMid, cr.isLessThan)
            return -1
        }
    }

    /**
     * Once the binary search has found a single match, (V/H)LOOKUP steps one by one over subsequent
     * values to choose the last matching item.
     */
    private fun findLastIndexInRunOfEqualValues(
        lookupComparer: LookupValueComparer, vector: ValueVector,
        firstFoundIndex: Int, maxIx: Int
    ): Int {
        for (i in firstFoundIndex + 1..<maxIx) {
            if (!lookupComparer.compareTo(vector.getItem(i)).isEqual) {
                return i - 1
            }
        }
        return maxIx - 1
    }

    fun createLookupComparer(lookupValue: ValueEval): LookupValueComparer {
        if (lookupValue === BlankEval.instance) {
            // blank eval translates to zero
            // Note - a blank eval in the lookup column/row never matches anything
            // empty string in the lookup column/row can only be matched by explicit empty string
            return NumberLookupComparer(NumberEval.ZERO)
        }
        if (lookupValue is StringEval) {
            return StringLookupComparer(lookupValue)
        }
        if (lookupValue is NumberEval) {
            return NumberLookupComparer(lookupValue)
        }
        if (lookupValue is BoolEval) {
            return BooleanLookupComparer(lookupValue)
        }
        throw IllegalArgumentException("Bad lookup value type (" + lookupValue.javaClass.getName() + ")")
    }

    fun createLookupComparer(
        srcRowIndex: Int,
        srcColumnIndex: Int,
        lookupValue: ValueEval
    ): LookupValueComparer {
        var lookupValue = lookupValue
        if (lookupValue is AreaEval) {
            lookupValue =
                WorkbookEvaluator.dereferenceResult(lookupValue, srcRowIndex, srcColumnIndex)!!

            return createLookupComparer(srcRowIndex, srcColumnIndex, lookupValue)
        } else {
            return createLookupComparer(lookupValue)
        }
    }

    /**
     * Represents a single row or column within an <tt>AreaEval</tt>.
     */
    interface ValueVector {
        fun getItem(index: Int): ValueEval?
        val size: Int
    }


    private class RowVector(tableArray: TwoDEval, private val _rowIndex: Int) : ValueVector {
        private val _tableArray: TwoDEval
        private val _size: Int

        init {
            val lastRowIx = tableArray.height - 1
            require(!(_rowIndex < 0 || _rowIndex > lastRowIx)) {
                ("Specified row index (" + _rowIndex
                        + ") is outside the allowed range (0.." + lastRowIx + ")")
            }
            _tableArray = tableArray
            _size = tableArray.width
        }

        override fun getItem(index: Int): ValueEval? {
            if (index > _size) {
                throw ArrayIndexOutOfBoundsException(
                    ("Specified index (" + index
                            + ") is outside the allowed range (0.." + (_size - 1) + ")")
                )
            }
            var eval = _tableArray.getValue(_rowIndex, index)

            try {
                while (eval is RefEval) {
                    eval = getSingleValue(eval, 0, 0)
                }

                return eval
            } catch (e: EvaluationException) {
                return e.errorEval
            }
        }

        override val size: Int
            get() {
            return _size
        }
    }

    private class ColumnVector(tableArray: TwoDEval, private val _columnIndex: Int) : ValueVector {
        private val _tableArray: TwoDEval
        private val _size: Int

        init {
            val lastColIx = tableArray.width - 1
            require(!(_columnIndex < 0 || _columnIndex > lastColIx)) {
                ("Specified column index (" + _columnIndex
                        + ") is outside the allowed range (0.." + lastColIx + ")")
            }
            _tableArray = tableArray
            _size = _tableArray.height
        }

        override fun getItem(index: Int): ValueEval? {
            if (index > _size) {
                throw ArrayIndexOutOfBoundsException(
                    ("Specified index (" + index
                            + ") is outside the allowed range (0.." + (_size - 1) + ")")
                )
            }
            return _tableArray.getValue(index, _columnIndex)
        }

        override val size: Int
            get() {
            return _size
        }
    }

    /**
     * Enumeration to support **4** valued comparison results.
     *
     *
     * Excel lookup functions have complex behaviour in the case where the lookup array has mixed
     * types, and/or is unordered.  Contrary to suggestions in some Excel documentation, there
     * does not appear to be a universal ordering across types.  The binary search algorithm used
     * changes behaviour when the evaluated 'mid' value has a different type to the lookup value.
     *
     *
     * 
     * A simple int might have done the same job, but there is risk in confusion with the well
     * known <tt>Comparable.compareTo()</tt> and <tt>Comparator.compare()</tt> which both use
     * a ubiquitous 3 value result encoding.
     */
    class CompareResult private constructor(isTypeMismatch: Boolean, simpleCompareResult: Int) {
        val isTypeMismatch: Boolean
        val isLessThan: Boolean
        val isEqual: Boolean
        val isGreaterThan: Boolean

        init {
            if (isTypeMismatch) {
                this.isTypeMismatch = true
                this.isLessThan = false
                this.isEqual = false
                this.isGreaterThan = false
            } else {
                this.isTypeMismatch = false
                this.isLessThan = simpleCompareResult < 0
                this.isEqual = simpleCompareResult == 0
                this.isGreaterThan = simpleCompareResult > 0
            }
        }

        override fun toString(): String {
            val sb = StringBuffer(64)
            sb.append(javaClass.getName()).append(" [")
            sb.append(formatAsString())
            sb.append("]")
            return sb.toString()
        }

        private fun formatAsString(): String {
            if (this.isTypeMismatch) {
                return "TYPE_MISMATCH"
            }
            if (this.isLessThan) {
                return "LESS_THAN"
            }
            if (this.isEqual) {
                return "EQUAL"
            }
            if (this.isGreaterThan) {
                return "GREATER_THAN"
            }
            // toString must be reliable
            return "??error??"
        }

        companion object {
            val TYPE_MISMATCH: CompareResult = CompareResult(true, 0)
            val LESS_THAN: CompareResult = CompareResult(false, -1)
            val EQUAL: CompareResult = CompareResult(false, 0)
            val GREATER_THAN: CompareResult = CompareResult(false, +1)

            fun valueOf(simpleCompareResult: Int): CompareResult {
                if (simpleCompareResult < 0) {
                    return LESS_THAN
                }
                if (simpleCompareResult > 0) {
                    return GREATER_THAN
                }
                return EQUAL
            }
        }
    }

    interface LookupValueComparer {
        /**
         * @return one of 4 instances or <tt>CompareResult</tt>: <tt>LESS_THAN</tt>, <tt>EQUAL</tt>,
         * <tt>GREATER_THAN</tt> or <tt>TYPE_MISMATCH</tt>
         */
        fun compareTo(other: ValueEval?): CompareResult
    }

    private abstract class LookupValueComparerBase protected constructor(targetValue: ValueEval) :
        LookupValueComparer {
        private val _targetClass: Class<out ValueEval?>

        init {
            if (targetValue == null) {
                throw RuntimeException("targetValue cannot be null")
            }
            _targetClass = targetValue.javaClass
        }

        override fun compareTo(other: ValueEval?): CompareResult {
            if (other == null) {
                throw RuntimeException("compare to value cannot be null")
            }
            if (_targetClass != other.javaClass) {
                return CompareResult.TYPE_MISMATCH
            }
            return compareSameType(other)
        }

        override fun toString(): String {
            val sb = StringBuffer(64)
            sb.append(javaClass.getName()).append(" [")
            sb.append(this.valueAsString)
            sb.append("]")
            return sb.toString()
        }

        protected abstract fun compareSameType(other: ValueEval?): CompareResult

        /** used only for debug purposes  */
        protected abstract val valueAsString: String?
    }

    private class StringLookupComparer(se: StringEval) : LookupValueComparerBase(se) {
        private val _value: String

        init {
            _value = se.stringValue
        }

        override fun compareSameType(other: ValueEval?): CompareResult {
            val se = other as StringEval
            return CompareResult.valueOf(_value.compareTo(se.stringValue, ignoreCase = true))
        }

        override val valueAsString: String
            get() {
            return _value
        }
    }

    private class NumberLookupComparer(ne: NumberEval) : LookupValueComparerBase(ne) {
        private val _value: Double

        init {
            _value = ne.numberValue
        }

        override fun compareSameType(other: ValueEval?): CompareResult {
            val ne = other as NumberEval
            return CompareResult.valueOf(_value.compareTo(ne.numberValue))
        }

        override val valueAsString: String
            get() {
            return _value.toString()
        }
    }

    private class BooleanLookupComparer(be: BoolEval) : LookupValueComparerBase(be) {
        private val _value: Boolean

        init {
            _value = be.booleanValue
        }

        override fun compareSameType(other: ValueEval?): CompareResult {
            val be = other as BoolEval
            val otherVal = be.booleanValue
            if (_value == otherVal) {
                return CompareResult.EQUAL
            }
            // TRUE > FALSE
            if (_value) {
                return CompareResult.GREATER_THAN
            }
            return CompareResult.LESS_THAN
        }

        override val valueAsString: String
            get() {
            return _value.toString()
        }
    }

    /**
     * Encapsulates some standard binary search functionality so the unusual Excel behaviour can
     * be clearly distinguished.
     */
    private class BinarySearchIndexes(var highIx: Int) {
        var lowIx: Int
            private set

        init {
            this.lowIx = -1
        }

        val midIx: Int
            /**
             * @return -1 if the search range is empty
             */
            get() {
                val ixDiff = this.highIx - this.lowIx
                if (ixDiff < 2) {
                    return -1
                }
                return this.lowIx + (ixDiff / 2)
            }

        fun narrowSearch(midIx: Int, isLessThan: Boolean) {
            if (isLessThan) {
                this.highIx = midIx
            } else {
                this.lowIx = midIx
            }
        }
    }
}


