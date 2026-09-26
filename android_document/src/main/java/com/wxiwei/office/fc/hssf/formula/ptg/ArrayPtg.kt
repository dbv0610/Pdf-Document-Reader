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
package com.wxiwei.office.fc.hssf.formula.ptg

import com.wxiwei.office.constant.fc.ConstantValueParser
import com.wxiwei.office.constant.fc.ConstantValueParser.parse
import com.wxiwei.office.constant.fc.ErrorConstant
import com.wxiwei.office.fc.ss.util.NumberToTextConverter
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * ArrayPtg - handles arrays
 * 
 * The ArrayPtg is a little weird, the size of the Ptg when parsing initially only
 * includes the Ptg sid and the reserved bytes. The next Ptg in the expression then follows.
 * It is only after the "size" of all the Ptgs is met, that the ArrayPtg data is actually
 * held after this. So Ptg.createParsedExpression keeps track of the number of
 * ArrayPtg elements and need to parse the data upto the FORMULA record size.
 * 
 * @author Jason Height (jheight at chariot dot net dot au)
 */
class ArrayPtg : Ptg {
    // 7 bytes of data (stored as an int, short and byte here)
    private val _reserved0Int: Int
    private val _reserved1Short: Int
    private val _reserved2Byte: Int

    // data from these fields comes after the Ptg data of all tokens in current formula
    val columnCount: Int
    val rowCount: Int
    private val _arrayValues: Array<Any?>?

    internal constructor(
        reserved0: Int,
        reserved1: Int,
        reserved2: Int,
        nColumns: Int,
        nRows: Int,
        arrayValues: Array<Any?>?
    ) {
        _reserved0Int = reserved0
        _reserved1Short = reserved1
        _reserved2Byte = reserved2
        this.columnCount = nColumns
        this.rowCount = nRows
        _arrayValues = arrayValues
    }

    /**
     * @param values2d array values arranged in rows
     */
    constructor(values2d: Array<Array<Any?>?>) {
        val nColumns = values2d[0]!!.size
        val nRows = values2d.size
        // convert 2-d to 1-d array (row by row according to getValueIndex())
        this.columnCount = nColumns.toShort().toInt()
        this.rowCount = nRows.toShort().toInt()

        val vv: Array<Any?> = arrayOfNulls<Any>(this.columnCount * this.rowCount)
        for (r in 0..<nRows) {
            val rowData: Array<Any?> = values2d[r]!!
            for (c in 0..<nColumns) {
                vv[getValueIndex(c, r)] = rowData[c]!!
            }
        }

        _arrayValues = vv
        _reserved0Int = 0
        _reserved1Short = 0
        _reserved2Byte = 0
    }

    val tokenArrayValues: Array<Array<Any?>?>
        /**
         * @return 2-d array (inner index is rowIx, outer index is colIx)
         */
        get() {
            checkNotNull(_arrayValues) { "array values not read yet" }
            val result =
                Array<Array<Any?>?>(this.rowCount) {
                    arrayOfNulls<Any>(this.columnCount)
                }
            for (r in 0..<this.rowCount) {
                val rowData: Array<Any?> = result[r]!!
                for (c in 0..<this.columnCount) {
                    rowData[c] = _arrayValues[getValueIndex(c, r)]
                }
            }
            return result
        }

    override val isBaseToken: Boolean get() {
        return false
    }

    override fun toString(): String {
        val sb = StringBuffer("[ArrayPtg]\n")

        sb.append("nRows = ").append(this.rowCount).append("\n")
        sb.append("nCols = ").append(this.columnCount).append("\n")
        if (_arrayValues == null) {
            sb.append("  #values#uninitialised#\n")
        } else {
            sb.append("  ").append(toFormulaString())
        }
        return sb.toString()
    }

    /**
     * Note - (2D) array elements are stored row by row
     * @return the index into the internal 1D array for the specified column and row
     */
    /* package */
    fun getValueIndex(colIx: Int, rowIx: Int): Int {
        require(!(colIx < 0 || colIx >= this.columnCount)) {
            ("Specified colIx (" + colIx
                    + ") is outside the allowed range (0.." + (this.columnCount - 1) + ")")
        }
        require(!(rowIx < 0 || rowIx >= this.rowCount)) {
            ("Specified rowIx (" + rowIx
                    + ") is outside the allowed range (0.." + (this.rowCount - 1) + ")")
        }
        return rowIx * this.columnCount + colIx
    }

    override fun write(out: LittleEndianOutput) {
        out.writeByte(sid + ptgClass)
        out.writeInt(_reserved0Int)
        out.writeShort(_reserved1Short)
        out.writeByte(_reserved2Byte)
    }

    fun writeTokenValueBytes(out: LittleEndianOutput): Int {
        out.writeByte(this.columnCount - 1)
        out.writeShort(this.rowCount - 1)
        ConstantValueParser.encode(out, _arrayValues!!)
        return 3 + ConstantValueParser.getEncodedSize(_arrayValues!!)
    }

    /** This size includes the size of the array Ptg plus the Array Ptg Token value size */
    override val size: Int get() {
        return (PLAIN_TOKEN_SIZE // data written after the all tokens:
                + 1 + 2 // column, row
                + ConstantValueParser.getEncodedSize(_arrayValues!!))
    }

    override fun toFormulaString(): String {
        val b = StringBuffer()
        b.append("{")
        for (y in 0..<this.rowCount) {
            if (y > 0) {
                b.append(";")
            }
            for (x in 0..<this.columnCount) {
                if (x > 0) {
                    b.append(",")
                }
                val o = _arrayValues!![getValueIndex(x, y)]
                b.append(getConstantText(o))
            }
        }
        b.append("}")
        return b.toString()
    }

    override val defaultOperandClass: Byte get() {
        return Ptg.Companion.CLASS_ARRAY
    }

    /**
     * Represents the initial plain tArray token (without the constant data that trails the whole
     * formula).  Objects of this class are only temporary and cannot be used as [Ptg]s.
     * These temporary objects get converted to [ArrayPtg] by the
     * [.finishReading] method.
     */
    internal class Initial(`in`: LittleEndianInput) : Ptg() {
        private val _reserved0: Int
        private val _reserved1: Int
        private val _reserved2: Int

        init {
            _reserved0 = `in`.readInt()
            _reserved1 = `in`.readUShort()
            _reserved2 = `in`.readUByte()
        }

        override val defaultOperandClass: Byte get() {
            throw invalid()
        }

        override val size: Int get() {
            return PLAIN_TOKEN_SIZE
        }

        override val isBaseToken: Boolean get() {
            return false
        }

        override fun toFormulaString(): String? {
            throw invalid()
        }

        override fun write(out: LittleEndianOutput) {
            throw invalid()
        }

        /**
         * Read in the actual token (array) values. This occurs
         * AFTER the last Ptg in the expression.
         * See page 304-305 of Excel97-2007BinaryFileFormat(xls)Specification.pdf
         */
        fun finishReading(`in`: LittleEndianInput): ArrayPtg {
            var nColumns = `in`.readUByte()
            var nRows = `in`.readShort()
            //The token_1_columns and token_2_rows do not follow the documentation.
            //The number of physical rows and columns is actually +1 of these values.
            //Which is not explicitly documented.
            nColumns++
            nRows++

            val totalCount = nRows * nColumns
            val arrayValues: Array<Any?> = parse(`in`, totalCount)

            val result =
                ArrayPtg(_reserved0, _reserved1, _reserved2, nColumns, nRows.toInt(), arrayValues)
            result.setClass(ptgClass)
            return result
        }

        companion object {
            private fun invalid(): RuntimeException {
                throw IllegalStateException("This object is a partially initialised tArray, and cannot be used as a Ptg")
            }
        }
    }

    companion object {
        const val sid: Byte = 0x20

        private const val RESERVED_FIELD_LEN = 7

        /**
         * The size of the plain tArray token written within the standard formula tokens
         * (not including the data which comes after all formula tokens)
         */
        @JvmField
        val PLAIN_TOKEN_SIZE: Int = 1 + RESERVED_FIELD_LEN

        private fun getConstantText(o: Any?): String {
            if (o == null) {
                throw RuntimeException("Array item cannot be null")
            }
            if (o is String) {
                return "\"" + o + "\""
            }
            if (o is Double) {
                return NumberToTextConverter.toText(o)
            }
            if (o is Boolean) {
                return if (o) "TRUE" else "FALSE"
            }
            if (o is ErrorConstant) {
                return o.text
            }
            throw IllegalArgumentException("Unexpected constant class (" + o.javaClass.getName() + ")")
        }
    }
}
