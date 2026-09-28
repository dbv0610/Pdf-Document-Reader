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
package com.wxiwei.office.fc.hssf.record

import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.fc.util.StringUtil.hasMultibyte
import com.wxiwei.office.fc.util.StringUtil.putCompressedUnicode
import com.wxiwei.office.fc.util.StringUtil.putUnicodeLE

/**
 * Title:        Format Record (0x041E) 
 *
 *
 * Description:  describes a number format -- those goofy strings like $(#,###)
 *
 *
 * 
 * REFERENCE:  PG 317 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)
 *
 *
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Shawn M. Laubach (slaubach at apache dot org)
 */
class FormatRecord : StandardRecord {
    private val field_1_index_code: Int
    private val field_3_hasMultibyte: Boolean
    private val field_4_formatstring: String

    constructor(indexCode: Int, fs: String) {
        field_1_index_code = indexCode
        field_4_formatstring = fs
        field_3_hasMultibyte = hasMultibyte(fs)
    }

    constructor(`in`: RecordInputStream) {
        field_1_index_code = `in`.readShort().toInt()
        val field_3_unicode_len = `in`.readUShort()
        field_3_hasMultibyte = (`in`.readByte().toInt() and 0x01) != 0

        if (field_3_hasMultibyte) {
            field_4_formatstring = `in`.readUnicodeLEString(field_3_unicode_len)
        } else {
            field_4_formatstring = `in`.readCompressedUnicode(field_3_unicode_len)
        }
    }

    /**
     * get the format index code (for built in formats)
     * 
     * @return the format index code
     * @see com.wxiwei.office.fc.hssf.model.InternalWorkbook
     */
    fun getIndexCode(): Int {
        return field_1_index_code
    }

    /**
     * get the format string
     * 
     * @return the format string
     */
    fun getFormatString(): String {
        return field_4_formatstring
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[FORMAT]\n")
        buffer.append("    .indexcode       = ").append(shortToHex(getIndexCode())).append("\n")
        buffer.append("    .isUnicode       = ").append(field_3_hasMultibyte).append("\n")
        buffer.append("    .formatstring    = ").append(getFormatString()).append("\n")
        buffer.append("[/FORMAT]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        val formatString = getFormatString()
        out.writeShort(getIndexCode())
        out.writeShort(formatString.length)
        out.writeByte(if (field_3_hasMultibyte) 0x01 else 0x00)

        if (field_3_hasMultibyte) {
            putUnicodeLE(formatString, out)
        } else {
            putCompressedUnicode(formatString, out)
        }
    }

    override fun getDataSize(): Int {
        return (5 // 2 shorts + 1 byte
                + getFormatString().length * (if (field_3_hasMultibyte) 2 else 1))
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        // immutable
        return this
    }

    companion object {
        const val sid: Short = 0x041E
    }
}
