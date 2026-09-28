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
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.EvaluationException
import com.wxiwei.office.fc.hssf.formula.eval.NumberEval
import com.wxiwei.office.fc.hssf.formula.eval.RefEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.fc.hssf.formula.function.LookupUtils.ValueVector
import kotlin.Int
import kotlin.Throws
import kotlin.require


/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 */
abstract class XYNumericFunction : Fixed2ArgFunction() {
    private abstract class ValueArray protected constructor(private val _size: Int) : ValueVector {
        override fun getItem(index: Int): ValueEval? {
            require(!(index < 0 || index > _size)) {
                ("Specified index " + index
                        + " is outside range (0.." + (_size - 1) + ")")
            }
            return getItemInternal(index)
        }

        protected abstract fun getItemInternal(index: Int): ValueEval?
        override val size: Int
            get() {
            return _size
        }
    }

    private class SingleCellValueArray(private val _value: ValueEval?) : ValueArray(1) {
        override fun getItemInternal(index: Int): ValueEval? {
            return _value
        }
    }

    private class RefValueArray(private val _ref: RefEval) : ValueArray(1) {
        override fun getItemInternal(index: Int): ValueEval? {
            return _ref.innerValueEval
        }
    }

    private class AreaValueArray(private val _ae: TwoDEval) : ValueArray(
        _ae.width * _ae.height
    ) {
        private val _width: Int

        init {
            _width = _ae.width
        }

        override fun getItemInternal(index: Int): ValueEval? {
            val rowIx = index / _width
            val colIx = index % _width
            return _ae.getValue(rowIx, colIx)
        }
    }

    protected interface Accumulator {
        fun accumulate(x: Double, y: Double): Double
    }

    /**
     * Constructs a new instance of the Accumulator used to calculated this function
     */
    protected abstract fun createAccumulator(): Accumulator

    override fun evaluate(
        srcRowIndex: Int,
        srcColumnIndex: Int,
        arg0: ValueEval?,
        arg1: ValueEval?
    ): ValueEval? {
        val result: Double
        try {
            val vvX: ValueVector = createValueVector(arg0)
            val vvY: ValueVector = createValueVector(arg1)
            val size = vvX.size
            if (size == 0 || vvY.size != size) {
                return ErrorEval.NA
            }
            result = evaluateInternal(vvX, vvY, size)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
        if (result.isNaN() || result.isInfinite()) {
            return ErrorEval.NUM_ERROR
        }
        return NumberEval(result)
    }

    @Throws(EvaluationException::class)
    private fun evaluateInternal(x: ValueVector, y: ValueVector, size: Int): kotlin.Double {
        val acc = createAccumulator()

        // error handling is as if the x is fully evaluated before y
        var firstXerr: ErrorEval? = null
        var firstYerr: ErrorEval? = null
        var accumlatedSome = false
        var result = 0.0

        for (i in 0..<size) {
            val vx = x.getItem(i)
            val vy = y.getItem(i)
            if (vx is ErrorEval) {
                if (firstXerr == null) {
                    firstXerr = vx
                    continue
                }
            }
            if (vy is ErrorEval) {
                if (firstYerr == null) {
                    firstYerr = vy
                    continue
                }
            }
            // only count pairs if both elements are numbers
            if (vx is NumberEval && vy is NumberEval) {
                accumlatedSome = true
                val nx = vx
                val ny = vy
                result += acc.accumulate(nx.numberValue, ny.numberValue)
            } else {
                // all other combinations of value types are silently ignored
            }
        }
        if (firstXerr != null) {
            throw EvaluationException(firstXerr)
        }
        if (firstYerr != null) {
            throw EvaluationException(firstYerr)
        }
        if (!accumlatedSome) {
            throw EvaluationException(ErrorEval.DIV_ZERO)
        }
        return result
    }

    companion object {
        @Throws(EvaluationException::class)
        private fun createValueVector(arg: ValueEval?): ValueVector {
            if (arg is ErrorEval) {
                throw EvaluationException(arg)
            }
            if (arg is TwoDEval) {
                return AreaValueArray(arg)
            }
            if (arg is RefEval) {
                return RefValueArray(arg)
            }
            return SingleCellValueArray(arg)
        }
    }
}
