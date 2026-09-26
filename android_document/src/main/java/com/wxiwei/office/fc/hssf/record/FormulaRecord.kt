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
package com.wxiwei.office.fc.hssf.record

import com.wxiwei.office.fc.hssf.formula.Formula
import com.wxiwei.office.fc.hssf.formula.Formula.Companion.read
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.util.BitFieldFactory.Companion.getInstance
import com.wxiwei.office.fc.util.HexDump.intToHex
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.ss.model.baseModel.Cell
import kotlin.Any
import kotlin.Array
import kotlin.Boolean
import kotlin.ByteArray
import kotlin.IllegalStateException
import kotlin.Int
import kotlin.Long
import kotlin.Short
import kotlin.String
import kotlin.byteArrayOf
import kotlin.check
import kotlin.plus


/**
 * Formula Record (0x0006).
 * REFERENCE:  PG 317/444 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
</P> */
class FormulaRecord : CellRecord {
    /**
     * Manages the cached formula result values of other types besides numeric.
     * Excel encodes the same 8 bytes that would be field_4_value with various NaN
     * values that are decoded/encoded by this class.
     */
    private class SpecialCachedValue(data: ByteArray) {
        private val _variableData: ByteArray

        init {
            _variableData = data
        }

        fun getTypeCode(): Int {
            return _variableData[0].toInt()
        }

        fun serialize(out: LittleEndianOutput) {
            out.write(_variableData)
            out.writeShort(0xFFFF)
        }

        fun formatDebugString(): String {
            return formatValue() + ' ' + toHex(_variableData)
        }

        fun formatValue(): String? {
            val typeCode = getTypeCode()
            when (typeCode) {
                STRING -> return "<string>"
                BOOLEAN -> return if (getDataValue() == 0) "FALSE" else "TRUE"
                ERROR_CODE -> return ErrorEval.getText(getDataValue())
                EMPTY -> return "<empty>"
            }
            return "#error(type=" + typeCode + ")#"
        }

        fun getDataValue(): Int {
            return _variableData[DATA_INDEX].toInt()
        }

        override fun toString(): String {
            val sb = StringBuffer(64)
            sb.append(javaClass.getName())
            sb.append('[').append(formatValue()).append(']')
            return sb.toString()
        }

        fun getValueType(): Int {
            val typeCode = getTypeCode()
            when (typeCode) {
                STRING -> return Cell.CELL_TYPE_STRING.toInt()
                BOOLEAN -> return Cell.CELL_TYPE_BOOLEAN.toInt()
                ERROR_CODE -> return Cell.CELL_TYPE_ERROR.toInt()
                EMPTY -> return Cell.CELL_TYPE_STRING.toInt() // is this correct?
            }
            throw IllegalStateException("Unexpected type id (" + typeCode + ")")
        }

        fun getBooleanValue(): Boolean {
            check(getTypeCode() == BOOLEAN) { "Not a boolean cached value - " + formatValue() }
            return getDataValue() != 0
        }

        fun getErrorValue(): Int {
            check(getTypeCode() == ERROR_CODE) { "Not an error cached value - " + formatValue() }
            return getDataValue()
        }

        companion object {
            /** deliberately chosen by Excel in order to encode other values within Double NaNs  */
            private const val BIT_MARKER = -0x1000000000000L
            private const val VARIABLE_DATA_LENGTH = 6
            private const val DATA_INDEX = 2

            const val STRING: Int = 0
            const val BOOLEAN: Int = 1
            const val ERROR_CODE: Int = 2
            const val EMPTY: Int = 3

            /**
             * @return `null` if the double value encoded by <tt>valueLongBits</tt>
             * is a normal (non NaN) double value.
             */
            fun create(valueLongBits: Long): SpecialCachedValue? {
                if ((BIT_MARKER and valueLongBits) != BIT_MARKER) {
                    return null
                }

                val result = ByteArray(VARIABLE_DATA_LENGTH)
                var x = valueLongBits
                for (i in 0..<VARIABLE_DATA_LENGTH) {
                    result[i] = x.toByte()
                    x = x shr 8
                }
                when (result[0].toInt()) {
                    STRING, BOOLEAN, ERROR_CODE, EMPTY -> {}
                    else -> throw RecordFormatException("Bad special value code (" + result[0] + ")")
                }
                return SpecialCachedValue(result)
            }

            fun createCachedEmptyValue(): SpecialCachedValue {
                return create(EMPTY, 0)
            }

            fun createForString(): SpecialCachedValue {
                return create(STRING, 0)
            }

            fun createCachedBoolean(b: Boolean): SpecialCachedValue {
                return create(BOOLEAN, if (b) 1 else 0)
            }

            fun createCachedErrorCode(errorCode: Int): SpecialCachedValue {
                return create(ERROR_CODE, errorCode)
            }

            private fun create(code: Int, data: Int): SpecialCachedValue {
                val vd = byteArrayOf(
                    code.toByte(),
                    0,
                    data.toByte(),
                    0,
                    0,
                    0,
                )
                return SpecialCachedValue(vd)
            }
        }
    }

    private var field_4_value = 0.0
    private var field_5_options: Short = 0

    /**
     * Unused field.  As it turns out this field is often not zero..
     * According to Microsoft Excel Developer's Kit Page 318:
     * when writing the chn field (offset 20), it's supposed to be 0 but ignored on read
     */
    private var field_6_zero = 0
    private var field_8_parsed_expr: Formula

    /**
     * Since the NaN support seems sketchy (different constants) we'll store and spit it out directly
     */
    private var specialCachedValue: SpecialCachedValue? = null

    /** Creates new FormulaRecord  */
    constructor() {
        field_8_parsed_expr = Formula.create(Ptg.EMPTY_PTG_ARRAY)!!
    }

    constructor(ris: RecordInputStream) : super(ris) {
        val `in`: LittleEndianInput = ris
        val valueLongBits = `in`.readLong()
        field_5_options = `in`.readShort()
        specialCachedValue = SpecialCachedValue.create(valueLongBits)
        if (specialCachedValue == null) {
            field_4_value = java.lang.Double.longBitsToDouble(valueLongBits)
        }

        field_6_zero = `in`.readInt()

        val field_7_expression_len =
            `in`.readShort().toInt() // this length does not include any extra array data
        val nBytesAvailable = `in`.available()
        field_8_parsed_expr = read(field_7_expression_len, `in`, nBytesAvailable)
    }

    /**
     * set the calculated value of the formula
     * 
     * @param value  calculated value
     */
    fun setValue(value: Double) {
        field_4_value = value
        specialCachedValue = null
    }

    fun setCachedResultTypeEmptyString() {
        specialCachedValue = SpecialCachedValue.createCachedEmptyValue()
    }

    fun setCachedResultTypeString() {
        specialCachedValue = SpecialCachedValue.createForString()
    }

    fun setCachedResultErrorCode(errorCode: Int) {
        specialCachedValue = SpecialCachedValue.createCachedErrorCode(errorCode)
    }

    fun setCachedResultBoolean(value: Boolean) {
        specialCachedValue = SpecialCachedValue.createCachedBoolean(value)
    }

    /**
     * @return `true` if this [FormulaRecord] is followed by a
     * [StringRecord] representing the cached text result of the formula
     * evaluation.
     */
    fun hasCachedResultString(): Boolean {
        if (specialCachedValue == null) {
            return false
        }
        return specialCachedValue!!.getTypeCode() == SpecialCachedValue.STRING
    }

    fun getCachedResultType(): Int {
        if (specialCachedValue == null) {
            return Cell.CELL_TYPE_NUMERIC.toInt()
        }
        return specialCachedValue!!.getValueType()
    }

    fun getCachedBooleanValue(): Boolean {
        return specialCachedValue!!.getBooleanValue()
    }

    fun getCachedErrorValue(): Int {
        return specialCachedValue!!.getErrorValue()
    }


    /**
     * set the option flags
     * 
     * @param options  bitmask
     */
    fun setOptions(options: Short) {
        field_5_options = options
    }

    /**
     * get the calculated value of the formula
     * 
     * @return calculated value
     */
    fun getValue(): Double {
        return field_4_value
    }

    /**
     * get the option flags
     * 
     * @return bitmask
     */
    fun getOptions(): Short {
        return field_5_options
    }

    fun isSharedFormula(): Boolean {
        return sharedFormula.isSet(field_5_options.toInt())
    }

    fun setSharedFormula(flag: Boolean) {
        field_5_options =
            sharedFormula.setShortBoolean(field_5_options, flag)
    }

    fun isAlwaysCalc(): Boolean {
        return alwaysCalc.isSet(field_5_options.toInt())
    }

    fun setAlwaysCalc(flag: Boolean) {
        field_5_options =
            alwaysCalc.setShortBoolean(field_5_options, flag)
    }

    fun isCalcOnLoad(): Boolean {
        return calcOnLoad.isSet(field_5_options.toInt())
    }

    fun setCalcOnLoad(flag: Boolean) {
        field_5_options =
            calcOnLoad.setShortBoolean(field_5_options, flag)
    }

    /**
     * @return the formula tokens. never `null`
     */
    fun getParsedExpression(): Array<Ptg?> {
        return field_8_parsed_expr.tokens
    }

    fun getFormula(): Formula {
        return field_8_parsed_expr
    }

    fun setParsedExpression(ptgs: Array<Ptg?>?) {
        field_8_parsed_expr = Formula.create(ptgs)!!
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun getValueDataSize(): Int {
        return FIXED_SIZE + field_8_parsed_expr.encodedSize
    }

    override fun serializeValue(out: LittleEndianOutput) {
        if (specialCachedValue == null) {
            out.writeDouble(field_4_value)
        } else {
            specialCachedValue!!.serialize(out)
        }

        out.writeShort(getOptions().toInt())

        out.writeInt(field_6_zero) // may as well write original data back so as to minimise differences from original
        field_8_parsed_expr.serialize(out)
    }

    override fun getRecordName(): String {
        return "FORMULA"
    }

    override fun appendValueText(sb: StringBuilder) {
        sb.append("  .value	 = ")
        if (specialCachedValue == null) {
            sb.append(field_4_value).append("\n")
        } else {
            sb.append(specialCachedValue!!.formatDebugString()).append("\n")
        }
        sb.append("  .options   = ").append(shortToHex(getOptions().toInt())).append("\n")
        sb.append("    .alwaysCalc= ").append(isAlwaysCalc()).append("\n")
        sb.append("    .calcOnLoad= ").append(isCalcOnLoad()).append("\n")
        sb.append("    .shared    = ").append(isSharedFormula()).append("\n")
        sb.append("  .zero      = ").append(intToHex(field_6_zero)).append("\n")

        val ptgs: Array<Ptg?> = field_8_parsed_expr.tokens
        for (k in ptgs.indices) {
            if (k > 0) {
                sb.append("\n")
            }
            sb.append("    Ptg[").append(k).append("]=")
            val ptg = ptgs[k]
            sb.append(ptg.toString()).append(ptg!!.rVAType)
        }
    }

    override fun clone(): Any {
        val rec = FormulaRecord()
        copyBaseFields(rec)
        rec.field_4_value = field_4_value
        rec.field_5_options = field_5_options
        rec.field_6_zero = field_6_zero
        rec.field_8_parsed_expr = field_8_parsed_expr
        rec.specialCachedValue = specialCachedValue
        return rec
    }

    companion object {
        const val sid: Short =
            0x0006 // docs say 406...because of a bug Microsoft support site article #Q184647)
        private const val FIXED_SIZE = 14 // double + short + int

        private val alwaysCalc = getInstance(0x0001)
        private val calcOnLoad = getInstance(0x0002)
        private val sharedFormula = getInstance(0x0008)
    }
}

