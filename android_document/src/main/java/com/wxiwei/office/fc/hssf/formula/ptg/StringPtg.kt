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

import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.fc.util.StringUtil.hasMultibyte
import com.wxiwei.office.fc.util.StringUtil.putCompressedUnicode
import com.wxiwei.office.fc.util.StringUtil.putUnicodeLE
import com.wxiwei.office.fc.util.StringUtil.readCompressedUnicode
import com.wxiwei.office.fc.util.StringUtil.readUnicodeLE

/**
 * String Stores a String value in a formula value stored in the format
 * &lt;length 2 bytes&gt;char[]
 * 
 * @author Werner Froidevaux
 * @author Jason Height (jheight at chariot dot net dot au)
 * @author Bernard Chesnoy
 */
class StringPtg : ScalarConstantPtg {
    private val _is16bitUnicode: Boolean

    /**
     * NOTE: OO doc says 16bit length, but BiffViewer says 8 Book says something
     * totally different, so don't look there!
     */
    val value: String

    /** Create a StringPtg from a stream  */
    constructor(`in`: LittleEndianInput) {
        val nChars = `in`.readUByte() // Note - nChars is 8-bit
        _is16bitUnicode = (`in`.readByte().toInt() and 0x01) != 0
        if (_is16bitUnicode) {
            this.value = readUnicodeLE(`in`, nChars)
        } else {
            this.value = readCompressedUnicode(`in`, nChars)
        }
    }

    /**
     * Create a StringPtg from a string representation of the number Number
     * format is not checked, it is expected to be validated in the parser that
     * calls this method.
     * 
     * @param value :
     * String representation of a floating point number
     */
    constructor(value: String) {
        require(value.length <= 255) { "String literals in formulas can't be bigger than 255 characters ASCII" }
        _is16bitUnicode = hasMultibyte(value)
        this.value = value
    }

    override fun write(out: LittleEndianOutput) {
        out.writeByte(sid + ptgClass)
        out.writeByte(value.length) // Note - nChars is 8-bit
        out.writeByte(if (_is16bitUnicode) 0x01 else 0x00)
        if (_is16bitUnicode) {
            putUnicodeLE(this.value, out)
        } else {
            putCompressedUnicode(this.value, out)
        }
    }

    override val size: Int get() {
        return 3 + value.length * (if (_is16bitUnicode) 2 else 1)
    }

    override fun toFormulaString(): String {
        val value = this.value
        val len = value.length
        val sb = StringBuffer(len + 4)
        sb.append(FORMULA_DELIMITER)

        for (i in 0..<len) {
            val c = value.get(i)
            if (c == FORMULA_DELIMITER) {
                sb.append(FORMULA_DELIMITER)
            }
            sb.append(c)
        }

        sb.append(FORMULA_DELIMITER)
        return sb.toString()
    }

    companion object {
        const val sid: Byte = 0x17

        /** the character (") used in formulas to delimit string literals  */
        private const val FORMULA_DELIMITER = '"'
    }
}
