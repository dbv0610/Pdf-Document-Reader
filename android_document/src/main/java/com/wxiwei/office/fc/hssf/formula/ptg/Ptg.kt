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

import com.wxiwei.office.fc.util.LittleEndianByteArrayOutputStream
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * <tt>Ptg</tt> represents a syntactic token in a formula.  'PTG' is an acronym for
 * '**p**arse **t**hin**g**'.  Originally, the name referred to the single
 * byte identifier at the start of the token, but in POI, <tt>Ptg</tt> encapsulates
 * the whole formula token (initial byte + value data).
 * 
 * 
 * 
 * <tt>Ptg</tt>s are logically arranged in a tree representing the structure of the
 * parsed formula.  However, in BIFF files <tt>Ptg</tt>s are written/read in
 * *Reverse-Polish Notation* order. The RPN ordering also simplifies formula
 * evaluation logic, so POI mostly accesses <tt>Ptg</tt>s in the same way.
 * 
 * @author  andy
 * @author avik
 * @author Jason Height (jheight at chariot dot net dot au)
 */
abstract class Ptg {
    /**
     * @return the encoded length of this Ptg, including the initial Ptg type identifier byte.
     */
    abstract val size: Int

    abstract fun write(out: LittleEndianOutput)

    /**
     * return a string representation of this token alone
     */
    abstract fun toFormulaString(): String?

    /** Overridden toString method to ensure object hash is not printed.
     * This helps get rid of gratuitous diffs when comparing two dumps
     * Subclasses may output more relevant information by overriding this method
     */
    override fun toString(): String {
        return this.javaClass.toString()
    }

    /**
     * @return the 'operand class' (REF/VALUE/ARRAY) for this Ptg
     */
    var ptgClass: Byte = CLASS_REF //base ptg
        private set

    fun setClass(thePtgClass: Byte) {
        if (this.isBaseToken) {
            throw RuntimeException("setClass should not be called on a base token")
        }
        ptgClass = thePtgClass
    }

    val rVAType: Char
        /**
         * Debug / diagnostic method to get this token's 'operand class' type.
         * @return 'R' for 'reference', 'V' for 'value', 'A' for 'array' and '.' for base tokens
         */
        get() {
            if (this.isBaseToken) {
                return '.'
            }
            when (ptgClass) {
                CLASS_REF -> return 'R'
                CLASS_VALUE -> return 'V'
                CLASS_ARRAY -> return 'A'
            }
            throw RuntimeException("Unknown operand class (" + ptgClass + ")")
        }

    abstract val defaultOperandClass: Byte

    /**
     * @return `false` if this token is classified as 'reference', 'value', or 'array'
     */
    abstract val isBaseToken: Boolean

    companion object {
        @JvmField
        val EMPTY_PTG_ARRAY: Array<Ptg?> = arrayOf<Ptg?>()

        /**
         * Reads <tt>size</tt> bytes of the input stream, to create an array of <tt>Ptg</tt>s.
         * Extra data (beyond <tt>size</tt>) may be read if and <tt>ArrayPtg</tt>s are present.
         */
        @JvmStatic
        fun readTokens(size: Int, `in`: LittleEndianInput): Array<Ptg?> {
            val temp: MutableList<Ptg?> = ArrayList<Ptg?>(4 + size / 2)
            var pos = 0
            var hasArrayPtgs = false
            while (pos < size) {
                val ptg: Ptg = createPtg(`in`)
                if (ptg is ArrayPtg.Initial) {
                    hasArrayPtgs = true
                }
                pos += ptg.size
                temp.add(ptg)
            }
            if (pos != size) {
                throw RuntimeException("Ptg array size mismatch")
            }
            if (hasArrayPtgs) {
                val result: Array<Ptg?> = toPtgArray(temp)
                for (i in result.indices) {
                    if (result[i] is ArrayPtg.Initial) {
                        result[i] = (result[i] as ArrayPtg.Initial).finishReading(`in`)
                    }
                }
                return result
            }
            return toPtgArray(temp)
        }

        fun createPtg(`in`: LittleEndianInput): Ptg {
            val id = `in`.readByte()

            if (id < 0x20) {
                return createBasePtg(id, `in`)
            }

            val retval: Ptg = createClassifiedPtg(id, `in`)

            if (id >= 0x60) {
                retval.setClass(CLASS_ARRAY)
            } else if (id >= 0x40) {
                retval.setClass(CLASS_VALUE)
            } else {
                retval.setClass(CLASS_REF)
            }
            return retval
        }

        private fun createClassifiedPtg(id: Byte, `in`: LittleEndianInput): Ptg {
            val baseId = id.toInt() and 0x1F or 0x20

            when (baseId) {
                ArrayPtg.Companion.sid.toInt() -> return ArrayPtg.Initial(`in`) //0x20, 0x40, 0x60
                FuncPtg.Companion.sid.toInt() -> return FuncPtg.Companion.create(`in`) // 0x21, 0x41, 0x61
                FuncVarPtg.Companion.sid.toInt() -> return FuncVarPtg.Companion.create(`in`) //0x22, 0x42, 0x62
                NamePtg.Companion.sid.toInt() -> return NamePtg(`in`) // 0x23, 0x43, 0x63
                RefPtg.Companion.sid.toInt() -> return RefPtg(`in`) // 0x24, 0x44, 0x64
                AreaPtg.Companion.sid.toInt() -> return AreaPtg(`in`) // 0x25, 0x45, 0x65
                MemAreaPtg.Companion.sid.toInt() -> return MemAreaPtg(`in`) // 0x26, 0x46, 0x66
                MemErrPtg.Companion.sid.toInt() -> return MemErrPtg(`in`) // 0x27, 0x47, 0x67
                MemFuncPtg.Companion.sid.toInt() -> return MemFuncPtg(`in`) // 0x29, 0x49, 0x69
                RefErrorPtg.Companion.sid.toInt() -> return RefErrorPtg(`in`) // 0x2a, 0x4a, 0x6a
                AreaErrPtg.Companion.sid.toInt() -> return AreaErrPtg(`in`) // 0x2b, 0x4b, 0x6b
                RefNPtg.Companion.sid.toInt() -> return RefNPtg(`in`) // 0x2c, 0x4c, 0x6c
                AreaNPtg.Companion.sid.toInt() -> return AreaNPtg(`in`) // 0x2d, 0x4d, 0x6d

                NameXPtg.Companion.sid.toInt() -> return NameXPtg(`in`) // 0x39, 0x49, 0x79
                Ref3DPtg.Companion.sid.toInt() -> return Ref3DPtg(`in`) // 0x3a, 0x5a, 0x7a
                Area3DPtg.Companion.sid.toInt() -> return Area3DPtg(`in`) // 0x3b, 0x5b, 0x7b
                DeletedRef3DPtg.Companion.sid.toInt() -> return DeletedRef3DPtg(`in`) // 0x3c, 0x5c, 0x7c
                DeletedArea3DPtg.Companion.sid.toInt() -> return DeletedArea3DPtg(`in`) // 0x3d, 0x5d, 0x7d
            }
            throw UnsupportedOperationException(
                (" Unknown Ptg in Formula: 0x"
                        + Integer.toHexString(id.toInt()) + " (" + id.toInt() + ")")
            )
        }

        private fun createBasePtg(id: Byte, `in`: LittleEndianInput): Ptg {
            when (id.toInt()) {
                0x00 -> return UnknownPtg(id.toInt()) // TODO - not a real Ptg
                ExpPtg.Companion.sid.toInt() -> return ExpPtg(`in`) // 0x01
                TblPtg.Companion.sid.toInt() -> return TblPtg(`in`) // 0x02
                AddPtg.Companion.sid.toInt() -> return AddPtg.Companion.instance // 0x03
                SubtractPtg.Companion.sid.toInt() -> return SubtractPtg.Companion.instance // 0x04
                MultiplyPtg.Companion.sid.toInt() -> return MultiplyPtg.Companion.instance // 0x05
                DividePtg.Companion.sid.toInt() -> return DividePtg.Companion.instance // 0x06
                PowerPtg.Companion.sid.toInt() -> return PowerPtg.Companion.instance // 0x07
                ConcatPtg.Companion.sid.toInt() -> return ConcatPtg.Companion.instance // 0x08
                LessThanPtg.Companion.sid.toInt() -> return LessThanPtg.Companion.instance // 0x09
                LessEqualPtg.Companion.sid.toInt() -> return LessEqualPtg.Companion.instance // 0x0a
                EqualPtg.Companion.sid.toInt() -> return EqualPtg.Companion.instance // 0x0b
                GreaterEqualPtg.Companion.sid.toInt() -> return GreaterEqualPtg.Companion.instance // 0x0c
                GreaterThanPtg.Companion.sid.toInt() -> return GreaterThanPtg.Companion.instance // 0x0d
                NotEqualPtg.Companion.sid.toInt() -> return NotEqualPtg.Companion.instance // 0x0e
                IntersectionPtg.Companion.sid.toInt() -> return IntersectionPtg.Companion.instance // 0x0f
                UnionPtg.Companion.sid.toInt() -> return UnionPtg.Companion.instance // 0x10
                RangePtg.Companion.sid.toInt() -> return RangePtg.Companion.instance // 0x11
                UnaryPlusPtg.Companion.sid.toInt() -> return UnaryPlusPtg.Companion.instance // 0x12
                UnaryMinusPtg.Companion.sid.toInt() -> return UnaryMinusPtg.Companion.instance // 0x13
                PercentPtg.Companion.sid.toInt() -> return PercentPtg.Companion.instance // 0x14
                ParenthesisPtg.Companion.sid.toInt() -> return ParenthesisPtg.Companion.instance // 0x15
                MissingArgPtg.Companion.sid.toInt() -> return MissingArgPtg.Companion.instance // 0x16

                StringPtg.Companion.sid.toInt() -> return StringPtg(`in`) // 0x17
                AttrPtg.Companion.sid.toInt() -> return AttrPtg(`in`) // 0x19
                ErrPtg.Companion.sid.toInt() -> return ErrPtg.Companion.read(`in`) // 0x1c
                BoolPtg.Companion.sid.toInt() -> return BoolPtg.Companion.read(`in`) // 0x1d
                IntPtg.Companion.sid.toInt() -> return IntPtg(`in`) // 0x1e
                NumberPtg.Companion.sid.toInt() -> return NumberPtg(`in`) // 0x1f
            }
            throw RuntimeException("Unexpected base token id (" + id + ")")
        }

        private fun toPtgArray(l: MutableList<Ptg?>): Array<Ptg?> {
            if (l.isEmpty()) {
                return EMPTY_PTG_ARRAY
            }
            return l.toTypedArray()
        }

        /**
         * This method will return the same result as [.getEncodedSizeWithoutArrayData]
         * if there are no array tokens present.
         * @return the full size taken to encode the specified <tt>Ptg</tt>s
         */
        fun getEncodedSize(ptgs: Array<out Ptg?>): Int {
            var result = 0
            for (i in ptgs.indices) {
                result += ptgs[i]!!.size
            }
            return result
        }

        /**
         * Used to calculate value that should be encoded at the start of the encoded Ptg token array;
         * @return the size of the encoded Ptg tokens not including any trailing array data.
         */
        fun getEncodedSizeWithoutArrayData(ptgs: Array<out Ptg?>): Int {
            var result = 0
            for (i in ptgs.indices) {
                val ptg = ptgs[i]
                if (ptg is ArrayPtg) {
                    result += ArrayPtg.Companion.PLAIN_TOKEN_SIZE
                } else {
                    result += ptg!!.size
                }
            }
            return result
        }

        /**
         * Writes the ptgs to the data buffer, starting at the specified offset.
         * 
         * <br></br>
         * The 2 byte encode length field is **not** written by this method.
         * @return number of bytes written
         */
        fun serializePtgs(ptgs: Array<out Ptg?>, array: ByteArray, offset: Int): Int {
            val nTokens = ptgs.size

            val out = LittleEndianByteArrayOutputStream(array, offset)

            var arrayPtgs: MutableList<Ptg?>? = null

            for (k in 0..<nTokens) {
                val ptg = ptgs[k]!!

                ptg.write(out)
                if (ptg is ArrayPtg) {
                    if (arrayPtgs == null) {
                        arrayPtgs = ArrayList<Ptg?>(5)
                    }
                    arrayPtgs.add(ptg)
                }
            }
            if (arrayPtgs != null) {
                for (i in arrayPtgs.indices) {
                    val p = arrayPtgs.get(i) as ArrayPtg
                    p.writeTokenValueBytes(out)
                }
            }
            return out.getWriteIndex() - offset
        }

        const val CLASS_REF: Byte = 0x00
        const val CLASS_VALUE: Byte = 0x20
        const val CLASS_ARRAY: Byte = 0x40

        fun doesFormulaReferToDeletedCell(ptgs: Array<out Ptg?>): Boolean {
            for (i in ptgs.indices) {
                if (isDeletedCellRef(ptgs[i])) {
                    return true
                }
            }
            return false
        }

        private fun isDeletedCellRef(ptg: Ptg?): Boolean {
            if (ptg === ErrPtg.Companion.REF_INVALID) {
                return true
            }
            if (ptg is DeletedArea3DPtg) {
                return true
            }
            if (ptg is DeletedRef3DPtg) {
                return true
            }
            if (ptg is AreaErrPtg) {
                return true
            }
            if (ptg is RefErrorPtg) {
                return true
            }
            return false
        }
    }
}
