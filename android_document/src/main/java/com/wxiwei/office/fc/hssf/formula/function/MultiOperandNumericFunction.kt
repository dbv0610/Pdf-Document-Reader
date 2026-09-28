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
import kotlin.Array
import kotlin.Boolean
import kotlin.DoubleArray
import kotlin.Int
import kotlin.RuntimeException
import kotlin.Throws
import kotlin.doubleArrayOf
import kotlin.requireNotNull


/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * This is the super class for all excel function evaluator
 * classes that take variable number of operands, and
 * where the order of operands does not matter
 */
abstract class MultiOperandNumericFunction protected constructor(
    private val _isReferenceBoolCounted: Boolean,
    private val _isBlankCounted: Boolean
) : Function {
    private class DoubleList {
        private var _array: DoubleArray
        private var _count = 0

        init {
            _array = DoubleArray(8)
        }

        fun toArray(): DoubleArray {
            if (_count < 1) {
                return EMPTY_DOUBLE_ARRAY
            }
            val result = DoubleArray(_count)
            System.arraycopy(_array, 0, result, 0, _count)
            return result
        }

        fun ensureCapacity(reqSize: Int) {
            if (reqSize > _array.size) {
                val newSize = reqSize * 3 / 2 // grow with 50% extra
                val newArr = DoubleArray(newSize)
                System.arraycopy(_array, 0, newArr, 0, _count)
                _array = newArr
            }
        }

        fun add(value: Double) {
            ensureCapacity(_count + 1)
            _array[_count] = value
            _count++
        }
    }

    override fun evaluate(args: Array<ValueEval?>, srcCellRow: Int, srcCellCol: Int): ValueEval? {
        val d: Double
        try {
            val values = getNumberArray(args)
            d = evaluate(values)
        } catch (e: EvaluationException) {
            return e.errorEval
        }

        if (d.isNaN() || d.isInfinite()) return ErrorEval.NUM_ERROR

        return NumberEval(d)
    }

    @Throws(EvaluationException::class)
    protected abstract fun evaluate(values: DoubleArray): kotlin.Double

    /**
     * Returns a double array that contains values for the numeric cells
     * from among the list of operands. Blanks and Blank equivalent cells
     * are ignored. Error operands or cells containing operands of type
     * that are considered invalid and would result in #VALUE! error in
     * excel cause this function to return `null`.
     * 
     * @return never `null`
     */
    @Throws(EvaluationException::class)
    protected fun getNumberArray(operands: Array<out ValueEval?>): DoubleArray {
        if (operands.size > this.maxNumOperands) {
            throw EvaluationException.invalidValue()
        }
        val retval = DoubleList()

        var i = 0
        val iSize = operands.size
        while (i < iSize) {
            collectValues(operands[i]!!, retval)
            i++
        }
        return retval.toArray()
    }

    /**
     * Maximum number of operands accepted by this function.
     * Subclasses may override to change default value.
     */
    protected open val maxNumOperands: Int
        get() = DEFAULT_MAX_NUM_OPERANDS

    open val isSubtotalCounted: Boolean
        /**
         * Whether to count nested subtotals.
         */
        get() = true

    /**
     * Collects values from a single argument
     */
    @Throws(EvaluationException::class)
    private fun collectValues(operand: ValueEval, temp: DoubleList) {
        if (operand is TwoDEval) {
            val ae = operand
            val width = ae.width
            val height = ae.height
            for (rrIx in 0..<height) {
                for (rcIx in 0..<width) {
                    var ve = ae.getValue(rrIx, rcIx)
                    if (!this.isSubtotalCounted && ae.isSubTotal(rrIx, rcIx)) continue
                    while (ve is RefEval) {
                        ve = getSingleValue(ve, 0, 0)
                    }

                    collectValue(ve!!, true, temp)
                }
            }
            return
        }
        if (operand is RefEval) {
            val re = operand
            collectValue(re.innerValueEval!!, true, temp)
            return
        }
        collectValue(operand, false, temp)
    }

    @Throws(EvaluationException::class)
    private fun collectValue(ve: ValueEval?, isViaReference: Boolean, temp: DoubleList) {
        requireNotNull(ve) { "ve must not be null" }
        if (ve is NumberEval) {
            val ne = ve
            temp.add(ne.numberValue)
            return
        }
        if (ve is ErrorEval) {
            throw EvaluationException(ve)
        }
        if (ve is StringEval) {
            if (isViaReference) {
                // ignore all ref strings
                return
            }
            val s = ve.stringValue
            val d: kotlin.Double? = parseDouble(s)
            if (d == null) {
                throw EvaluationException(ErrorEval.VALUE_INVALID)
            }
            temp.add(d)
            return
        }
        if (ve is BoolEval) {
            if (!isViaReference || _isReferenceBoolCounted) {
                val boolEval = ve
                temp.add(boolEval.numberValue)
            }
            return
        }
        if (ve === BlankEval.instance) {
            if (_isBlankCounted) {
                temp.add(0.0)
            }
            return
        }
        throw RuntimeException(
            ("Invalid ValueEval type passed for conversion: ("
                    + ve.javaClass + ")")
        )
    }

    companion object {
        val EMPTY_DOUBLE_ARRAY: DoubleArray = doubleArrayOf()

        private const val DEFAULT_MAX_NUM_OPERANDS: Int = 30
    }
}
