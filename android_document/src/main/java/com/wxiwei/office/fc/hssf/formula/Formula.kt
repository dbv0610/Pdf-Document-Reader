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
package com.wxiwei.office.fc.hssf.formula

import com.wxiwei.office.fc.hssf.formula.ptg.ExpPtg
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg.Companion.readTokens
import com.wxiwei.office.fc.hssf.formula.ptg.TblPtg
import com.wxiwei.office.fc.ss.util.CellReference
import com.wxiwei.office.fc.util.LittleEndian.getUShort
import com.wxiwei.office.fc.util.LittleEndianByteArrayInputStream
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Encapsulates an encoded formula token array.
 * 
 * @author Josh Micich
 */
class Formula private constructor(
    /** immutable  */
    private val _byteEncoding: ByteArray,
    /**
     * This method is often used when the formula length does not appear immediately before
     * the encoded token data.
     * 
     * @return the encoded length of the plain formula tokens.  This does *not* include
     * the leading ushort field, nor any trailing array constant data.
     */
    val encodedTokenSize: Int
) {
    init {
        if (false) { // set to true to eagerly check Ptg decoding
            val `in` = LittleEndianByteArrayInputStream(
                _byteEncoding
            )
            readTokens(encodedTokenSize, `in`)
            val nUnusedBytes = _byteEncoding.size - `in`.getReadIndex()
            if (nUnusedBytes > 0) {
                // TODO - this seems to occur when IntersectionPtg is present
                // This example file "IntersectionPtg.xls"
                // used by test: TestIntersectionPtg.testReading()
                // has 10 bytes unused at the end of the formula
                // 10 extra bytes are just 0x01 and 0x00
                println(nUnusedBytes.toString() + " unused bytes at end of formula")
            }
        }
    }

    val tokens: Array<Ptg?>
        get() {
            val `in`: LittleEndianInput = LittleEndianByteArrayInputStream(_byteEncoding)
            return readTokens(this.encodedTokenSize, `in`)
        }

    /**
     * Writes  The formula encoding is includes:
     * 
     *  * ushort tokenDataLen
     *  * tokenData
     *  * arrayConstantData (if present)
     * 
     */
    fun serialize(out: LittleEndianOutput) {
        out.writeShort(this.encodedTokenSize)
        out.write(_byteEncoding)
    }

    fun serializeTokens(out: LittleEndianOutput) {
        out.write(_byteEncoding, 0, this.encodedTokenSize)
    }

    fun serializeArrayConstantData(out: LittleEndianOutput) {
        val len = _byteEncoding.size - this.encodedTokenSize
        out.write(_byteEncoding, this.encodedTokenSize, len)
    }


    val encodedSize: Int
        /**
         * @return total formula encoding length.  The formula encoding includes:
         * 
         *  * ushort tokenDataLen
         *  * tokenData
         *  * arrayConstantData (optional)
         * 
         * Note - this value is different to <tt>tokenDataLength</tt>
         */
        get() = 2 + _byteEncoding.size

    fun copy(): Formula {
        // OK to return this because immutable
        return this
    }

    val expReference: CellReference?
        /**
         * Gets the locator for the corresponding [com.wxiwei.office.fc.hssf.record.SharedFormulaRecord],
         * [com.wxiwei.office.fc.hssf.record.ArrayRecord] or [com.wxiwei.office.fc.hssf.record.TableRecord]
         * if this formula belongs to such a grouping.  The [CellReference]
         * returned by this method will  match the top left corner of the range of that grouping.
         * The return value is usually not the same as the location of the cell containing this formula.
         * 
         * @return the firstRow & firstColumn of an array formula or shared formula that this formula
         * belongs to.  `null` if this formula is not part of an array or shared formula.
         */
        get() {
            val data = _byteEncoding
            if (data.size != 5) {
                // tExp and tTbl are always 5 bytes long, and the only ptg in the formula
                return null
            }
            when (data[0].toInt()) {
                ExpPtg.sid.toInt() -> {}
                TblPtg.sid.toInt() -> {}
                else -> return null
            }
            val firstRow = getUShort(data, 1)
            val firstColumn = getUShort(data, 3)
            return CellReference(firstRow, firstColumn)
        }

    fun isSame(other: Formula): Boolean {
        return _byteEncoding.contentEquals(other._byteEncoding)
    }

    companion object {
        private val EMPTY = Formula(ByteArray(0), 0)

        /**
         * When there are no array constants present, <tt>encodedTokenLen</tt>==<tt>totalEncodedLen</tt>
         * @param encodedTokenLen number of bytes in the stream taken by the plain formula tokens
         * @param totalEncodedLen the total number of bytes in the formula (includes trailing encoding
         * for array constants, but does not include 2 bytes for initial <tt>ushort encodedTokenLen</tt> field.
         * @return A new formula object as read from the stream.  Possibly empty, never `null`.
         */
        /**
         * Convenience method for [.read]
         */
        @JvmStatic
        @JvmOverloads
        fun read(
            encodedTokenLen: Int,
            `in`: LittleEndianInput,
            totalEncodedLen: Int = encodedTokenLen
        ): Formula {
            val byteEncoding = ByteArray(totalEncodedLen)
            `in`.readFully(byteEncoding)
            return Formula(byteEncoding, encodedTokenLen)
        }

        /**
         * Creates a [Formula] object from a supplied [Ptg] array.
         * Handles `null`s OK.
         * @param ptgs may be `null`
         * @return Never `null` (Possibly empty if the supplied <tt>ptgs</tt> is `null`)
         */
        fun create(ptgs: Array<Ptg?>?): Formula? {
            if (ptgs == null || ptgs.size < 1) {
                return EMPTY
            }
            val totalSize = Ptg.getEncodedSize(ptgs)
            val encodedData = ByteArray(totalSize)
            Ptg.serializePtgs(ptgs, encodedData, 0)
            val encodedTokenLen = Ptg.getEncodedSizeWithoutArrayData(ptgs)
            return Formula(encodedData, encodedTokenLen)
        }

        /**
         * Gets the [Ptg] array from the supplied [Formula].
         * Handles `null`s OK.
         * 
         * @param formula may be `null`
         * @return possibly `null` (if the supplied <tt>formula</tt> is `null`)
         */
        @JvmStatic
        fun getTokens(formula: Formula?): Array<Ptg?>? {
            if (formula == null) {
                return null
            }
            return formula.tokens
        }
    }
}
