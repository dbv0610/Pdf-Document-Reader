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

import com.wxiwei.office.fc.util.HexDump.byteToHex
import com.wxiwei.office.fc.util.HexDump.shortToHex

/**
 * Label Record (0x0204) - read only support for strings stored directly in the cell..  Don't
 * use this (except to read), use LabelSST instead <P>
 * REFERENCE:  PG 325 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 * @see LabelSSTRecord
</P> */
class LabelRecord : Record, CellValueRecordInterface {
    private var field_1_row = 0
    private var field_2_column: Short = 0
    private var field_3_xf_index: Short = 0
    private var field_4_string_len: Short = 0
    private var field_5_unicode_flag: Byte = 0
    private var field_6_value: String? = null

    /** Creates new LabelRecord  */
    constructor()

    /**
     * @param in the RecordInputstream to read the record from
     */
    constructor(`in`: RecordInputStream) {
        field_1_row = `in`.readUShort()
        field_2_column = `in`.readShort()
        field_3_xf_index = `in`.readShort()
        field_4_string_len = `in`.readShort()
        field_5_unicode_flag = `in`.readByte()
        if (field_4_string_len > 0) {
            if (isUnCompressedUnicode()) {
                field_6_value = `in`.readUnicodeLEString(field_4_string_len.toInt())
            } else {
                field_6_value = `in`.readCompressedUnicode(field_4_string_len.toInt())
            }
        } else {
            field_6_value = ""
        }
    }

    /*
 * READ ONLY ACCESS... THIS IS FOR COMPATIBILITY ONLY...USE LABELSST! public
 */

    /*
     * READ ONLY ACCESS... THIS IS FOR COMPATIBILITY ONLY...USE LABELSST! setters are NO-OP!
     */
    override var row: Int
        get() = field_1_row
        set(row) {
        }

    override var column: Short
        get() = field_2_column
        set(col) {
        }

    override var xFIndex: Short
        get() = field_3_xf_index
        set(xf) {
        }

    /**
     * get the number of characters this string contains
     * @return number of characters
     */
    fun getStringLength(): Short {
        return field_4_string_len
    }

    /**
     * is this uncompressed unicode (16bit)?  Or just 8-bit compressed?
     * @return isUnicode - True for 16bit- false for 8bit
     */
    fun isUnCompressedUnicode(): Boolean {
        return (field_5_unicode_flag.toInt() == 1)
    }

    /**
     * get the value
     * 
     * @return the text string
     * @see .getStringLength
     */
    fun getValue(): String? {
        return field_6_value
    }

    /**
     * THROWS A RUNTIME EXCEPTION..  USE LABELSSTRecords.  YOU HAVE NO REASON to use LABELRecord!!
     */
    override fun serialize(offset: Int, data: ByteArray): Int {
        throw RecordFormatException("Label Records are supported READ ONLY...convert to LabelSST")
    }

    override fun getRecordSize(): Int {
        throw RecordFormatException("Label Records are supported READ ONLY...convert to LabelSST")
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun toString(): String {
        val sb = StringBuffer()
        sb.append("[LABEL]\n")
        sb.append("    .row       = ").append(shortToHex(row)).append("\n")
        sb.append("    .column    = ").append(shortToHex(column.toInt())).append("\n")
        sb.append("    .xfindex   = ").append(shortToHex(xFIndex.toInt())).append("\n")
        sb.append("    .string_len= ").append(shortToHex(field_4_string_len.toInt())).append("\n")
        sb.append("    .unicode_flag= ").append(byteToHex(field_5_unicode_flag.toInt()))
            .append("\n")
        sb.append("    .value       = ").append(getValue()).append("\n")
        sb.append("[/LABEL]\n")
        return sb.toString()
    }

    companion object {
        const val sid: Short = 0x0204
    }
}
