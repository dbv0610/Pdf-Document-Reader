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
package com.wxiwei.office.fc.hssf.formula.ptg

import com.wxiwei.office.fc.util.BitFieldFactory.Companion.getInstance
import com.wxiwei.office.fc.util.LittleEndianConsts
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * "Special Attributes"
 * This seems to be a Misc Stuff and Junk record.  One function it serves is
 * in SUM functions (i.e. SUM(A1:A3) causes an area PTG then an ATTR with the SUM option set)
 * @author  andy
 * @author Jason Height (jheight at chariot dot net dot au)
 */
class AttrPtg : ControlPtg {
    private val _options: Byte
    val data: Short

    /** only used for tAttrChoose: table of offsets to starts of args  */
    private val _jumpTable: IntArray?

    /** only used for tAttrChoose: offset to the tFuncVar for CHOOSE()  */
    private val _chooseFuncOffset: Int

    object SpaceType {
        /** 00H = Spaces before the next token (not allowed before tParen token)  */
        const val SPACE_BEFORE: Int = 0x00

        /** 01H = Carriage returns before the next token (not allowed before tParen token)  */
        const val CR_BEFORE: Int = 0x01

        /** 02H = Spaces before opening parenthesis (only allowed before tParen token)  */
        const val SPACE_BEFORE_OPEN_PAREN: Int = 0x02

        /** 03H = Carriage returns before opening parenthesis (only allowed before tParen token)  */
        const val CR_BEFORE_OPEN_PAREN: Int = 0x03

        /** 04H = Spaces before closing parenthesis (only allowed before tParen, tFunc, and tFuncVar tokens)  */
        const val SPACE_BEFORE_CLOSE_PAREN: Int = 0x04

        /** 05H = Carriage returns before closing parenthesis (only allowed before tParen, tFunc, and tFuncVar tokens)  */
        const val CR_BEFORE_CLOSE_PAREN: Int = 0x05

        /** 06H = Spaces following the equality sign (only in macro sheets)  */
        const val SPACE_AFTER_EQUALITY: Int = 0x06
    }

    constructor(`in`: LittleEndianInput) {
        _options = `in`.readByte()
        this.data = `in`.readShort()
        if (this.isOptimizedChoose) {
            val nCases = data.toInt()
            val jumpTable = IntArray(nCases)
            for (i in jumpTable.indices) {
                jumpTable[i] = `in`.readUShort()
            }
            _jumpTable = jumpTable
            _chooseFuncOffset = `in`.readUShort()
        } else {
            _jumpTable = null
            _chooseFuncOffset = -1
        }
    }

    private constructor(options: Int, data: Int, jt: IntArray?, chooseFuncOffset: Int) {
        _options = options.toByte()
        this.data = data.toShort()
        _jumpTable = jt
        _chooseFuncOffset = chooseFuncOffset
    }

    val isSemiVolatile: Boolean
        get() = semiVolatile.isSet(_options.toInt())

    val isOptimizedIf: Boolean
        get() = optiIf.isSet(_options.toInt())

    val isOptimizedChoose: Boolean
        get() = optiChoose.isSet(_options.toInt())

    val isSum: Boolean
        get() = optiSum.isSet(_options.toInt())
    val isSkip: Boolean
        get() = optiSkip.isSet(_options.toInt())

    private val isBaxcel: Boolean
        // lets hope no one uses this anymore
        get() = baxcel.isSet(_options.toInt())

    val isSpace: Boolean
        get() = space.isSet(_options.toInt())

    val jumpTable: IntArray?
        get() = _jumpTable!!.clone()
    val chooseFuncOffset: Int
        get() {
            checkNotNull(_jumpTable) { "Not tAttrChoose" }
            return _chooseFuncOffset
        }

    override fun toString(): String {
        val sb = StringBuffer(64)
        sb.append(javaClass.getName()).append(" [")

        if (this.isSemiVolatile) {
            sb.append("volatile ")
        }
        if (this.isSpace) {
            sb.append("space count=").append((data.toInt() shr 8) and 0x00FF)
            sb.append(" type=").append(data.toInt() and 0x00FF).append(" ")
        }
        // the rest seem to be mutually exclusive
        if (this.isOptimizedIf) {
            sb.append("if dist=").append(data.toInt())
        } else if (this.isOptimizedChoose) {
            sb.append("choose nCases=").append(data.toInt())
        } else if (this.isSkip) {
            sb.append("skip dist=").append(data.toInt())
        } else if (this.isSum) {
            sb.append("sum ")
        } else if (this.isBaxcel) {
            sb.append("assign ")
        }
        sb.append("]")
        return sb.toString()
    }

    override fun write(out: LittleEndianOutput) {
        out.writeByte(sid + ptgClass)
        out.writeByte(_options.toInt())
        out.writeShort(data.toInt())
        val jt = _jumpTable
        if (jt != null) {
            for (i in jt.indices) {
                out.writeShort(jt[i])
            }
            out.writeShort(_chooseFuncOffset)
        }
    }

    override val size: Int get() {
        if (_jumpTable != null) {
            return SIZE + (_jumpTable.size + 1) * LittleEndianConsts.SHORT_SIZE
        }
        return SIZE
    }

    fun toFormulaString(operands: Array<String?>): String? {
        if (space.isSet(_options.toInt())) {
            return operands[0]
        } else if (optiIf.isSet(_options.toInt())) {
            return toFormulaString() + "(" + operands[0] + ")"
        } else if (optiSkip.isSet(_options.toInt())) {
            return toFormulaString() + operands[0] //goto isn't a real formula element should not show up
        } else {
            return toFormulaString() + "(" + operands[0] + ")"
        }
    }


    val numberOfOperands: Int
        get() = 1

    val type: Int
        get() = -1

    override fun toFormulaString(): String {
        if (semiVolatile.isSet(_options.toInt())) {
            return "ATTR(semiVolatile)"
        }
        if (optiIf.isSet(_options.toInt())) {
            return "IF"
        }
        if (optiChoose.isSet(_options.toInt())) {
            return "CHOOSE"
        }
        if (optiSkip.isSet(_options.toInt())) {
            return ""
        }
        if (optiSum.isSet(_options.toInt())) {
            return "SUM"
        }
        if (baxcel.isSet(_options.toInt())) {
            return "ATTR(baxcel)"
        }
        if (space.isSet(_options.toInt())) {
            return ""
        }
        return "UNKNOWN ATTRIBUTE"
    }

    companion object {
        const val sid: Byte = 0x19
        private const val SIZE = 4

        // flags 'volatile' and 'space', can be combined.
        // OOO spec says other combinations are theoretically possible but not likely to occur.
        private val semiVolatile = getInstance(0x01)
        private val optiIf = getInstance(0x02)
        private val optiChoose = getInstance(0x04)
        private val optiSkip = getInstance(0x08)
        private val optiSum = getInstance(0x10)
        private val baxcel = getInstance(0x20) // 'assignment-style formula in a macro sheet'
        private val space = getInstance(0x40)

        val SUM: AttrPtg = AttrPtg(0x0010, 0, null, -1)

        /**
         * @param type a constant from <tt>SpaceType</tt>
         * @param count the number of space characters
         */
        fun createSpace(type: Int, count: Int): AttrPtg {
            val data = type and 0x00FF or ((count shl 8) and 0x00FFFF)
            return AttrPtg(space.set(0), data, null, -1)
        }

        /**
         * @param dist distance (in bytes) to start of either  * false parameter
         *  * tFuncVar(IF) token (when false parameter is not present)
         */
        @JvmStatic
        fun createIf(dist: Int): AttrPtg {
            return AttrPtg(optiIf.set(0), dist, null, -1)
        }

        /**
         * @param dist distance (in bytes) to position behind tFuncVar(IF) token (minus 1)
         */
        @JvmStatic
        fun createSkip(dist: Int): AttrPtg {
            return AttrPtg(optiSkip.set(0), dist, null, -1)
        }

        @JvmStatic
        val sumSingle: AttrPtg
            get() = AttrPtg(optiSum.set(0), 0, null, -1)
    }
}
