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

import com.wxiwei.office.fc.hssf.formula.eval.AreaEval
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.EvaluationException
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver.getSingleValue
import com.wxiwei.office.fc.hssf.formula.eval.RefEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval

/**
 * Implementation for Excel function OFFSET()
 *
 *
 * 
 * OFFSET returns an area reference that is a specified number of rows and columns from a
 * reference cell or area.
 *
 *
 * 
 * **Syntax**:<br></br>
 * **OFFSET**(**reference**, **rows**, **cols**, height, width)
 *
 *
 * **reference** is the base reference.<br></br>
 * **rows** is the number of rows up or down from the base reference.<br></br>
 * **cols** is the number of columns left or right from the base reference.<br></br>
 * **height** (default same height as base reference) is the row count for the returned area reference.<br></br>
 * **width** (default same width as base reference) is the column count for the returned area reference.<br></br>
 * 
 * @author Josh Micich
 */
class Offset : Function {
    /**
     * A one dimensional base + offset.  Represents either a row range or a column range.
     * Two instances of this class together specify an area range.
     */
    /* package */
    internal class LinearOffsetRange(offset: Int, length: Int) {
        private val _offset: Int
        private val _length: Int

        init {
            if (length == 0) {
                // handled that condition much earlier
                throw RuntimeException("length may not be zero")
            }
            _offset = offset
            _length = length
        }

        val firstIndex: Short
            get() = _offset.toShort()
        val lastIndex: Short
            get() = (_offset + _length - 1).toShort()

        /**
         * Moves the range by the specified translation amount.
         *
         *
         * 
         * This method also 'normalises' the range: Excel specifies that the width and height
         * parameters (length field here) cannot be negative.  However, OFFSET() does produce
         * sensible results in these cases.  That behavior is replicated here. 
         *
         *
         * 
         * @param translationAmount may be zero negative or positive
         * 
         * @return the equivalent <tt>LinearOffsetRange</tt> with a positive length, moved by the
         * specified translationAmount.
         */
        fun normaliseAndTranslate(translationAmount: Int): LinearOffsetRange {
            if (_length > 0) {
                if (translationAmount == 0) {
                    return this
                }
                return LinearOffsetRange(translationAmount + _offset, _length)
            }
            return LinearOffsetRange(translationAmount + _offset + _length + 1, -_length)
        }

        fun isOutOfBounds(lowValidIx: Int, highValidIx: Int): Boolean {
            if (_offset < lowValidIx) {
                return true
            }
            if (this.lastIndex > highValidIx) {
                return true
            }
            return false
        }

        override fun toString(): String {
            val sb = StringBuffer(64)
            sb.append(javaClass.getName()).append(" [")
            sb.append(_offset).append("...").append(this.lastIndex.toInt())
            sb.append("]")
            return sb.toString()
        }
    }

    /**
     * Encapsulates either an area or cell reference which may be 2d or 3d.
     */
    private class BaseRef {
        val firstRowIndex: Int
        val firstColumnIndex: Int
        val width: Int
        val height: Int
        private val _refEval: RefEval?
        private val _areaEval: AreaEval?

        constructor(re: RefEval) {
            _refEval = re
            _areaEval = null
            this.firstRowIndex = re.row
            this.firstColumnIndex = re.column
            this.height = 1
            this.width = 1
        }

        constructor(ae: AreaEval) {
            _refEval = null
            _areaEval = ae
            this.firstRowIndex = ae.firstRow
            this.firstColumnIndex = ae.firstColumn
            this.height = ae.lastRow - ae.firstRow + 1
            this.width = ae.lastColumn - ae.firstColumn + 1
        }

        fun offset(
            relFirstRowIx: Int, relLastRowIx: Int,
            relFirstColIx: Int, relLastColIx: Int
        ): AreaEval? {
            if (_refEval == null) {
                return _areaEval!!.offset(relFirstRowIx, relLastRowIx, relFirstColIx, relLastColIx)
            }
            return _refEval.offset(relFirstRowIx, relLastRowIx, relFirstColIx, relLastColIx)
        }
    }

    override fun evaluate(args: Array<ValueEval?>, srcCellRow: Int, srcCellCol: Int): ValueEval? {
        if (args.size < 3 || args.size > 5) {
            return ErrorEval.VALUE_INVALID
        }

        try {
            val baseRef: BaseRef = evaluateBaseRef(args[0])
            val rowOffset: Int = evaluateIntArg(args[1], srcCellRow, srcCellCol)
            val columnOffset: Int = evaluateIntArg(args[2], srcCellRow, srcCellCol)
            var height = baseRef.height
            var width = baseRef.width
            when (args.size) {
                5 -> {
                    width = evaluateIntArg(args[4], srcCellRow, srcCellCol)
                    height = evaluateIntArg(args[3], srcCellRow, srcCellCol)
                }

                4 -> height = evaluateIntArg(args[3], srcCellRow, srcCellCol)
            }
            // Zero height or width raises #REF! error
            if (height == 0 || width == 0) {
                return ErrorEval.REF_INVALID
            }
            val rowOffsetRange = LinearOffsetRange(rowOffset, height)
            val colOffsetRange = LinearOffsetRange(columnOffset, width)
            return createOffset(baseRef, rowOffsetRange, colOffsetRange)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
    }

    companion object {
        // These values are specific to BIFF8
        private const val LAST_VALID_ROW_INDEX = 0xFFFF
        private const val LAST_VALID_COLUMN_INDEX = 0xFF


        @Throws(EvaluationException::class)
        private fun createOffset(
            baseRef: BaseRef,
            orRow: LinearOffsetRange, orCol: LinearOffsetRange
        ): AreaEval? {
            val absRows = orRow.normaliseAndTranslate(baseRef.firstRowIndex)
            val absCols = orCol.normaliseAndTranslate(baseRef.firstColumnIndex)

            if (absRows.isOutOfBounds(0, LAST_VALID_ROW_INDEX)) {
                throw EvaluationException(ErrorEval.REF_INVALID)
            }
            if (absCols.isOutOfBounds(0, LAST_VALID_COLUMN_INDEX)) {
                throw EvaluationException(ErrorEval.REF_INVALID)
            }
            return baseRef.offset(
                orRow.firstIndex.toInt(),
                orRow.lastIndex.toInt(),
                orCol.firstIndex.toInt(),
                orCol.lastIndex.toInt()
            )
        }

        @Throws(EvaluationException::class)
        private fun evaluateBaseRef(eval: ValueEval?): BaseRef {
            if (eval is RefEval) {
                return BaseRef(eval)
            }
            if (eval is AreaEval) {
                return BaseRef(eval)
            }
            if (eval is ErrorEval) {
                throw EvaluationException(eval)
            }
            throw EvaluationException(ErrorEval.VALUE_INVALID)
        }

        /**
         * OFFSET's numeric arguments (2..5) have similar processing rules
         */
        @Throws(EvaluationException::class)
        fun evaluateIntArg(eval: ValueEval?, srcCellRow: Int, srcCellCol: Int): Int {
            val ve = getSingleValue(eval, srcCellRow, srcCellCol)
            return OperandResolver.coerceValueToInt(ve!!)
        }
    }
}
