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

import com.wxiwei.office.fc.ss.util.WorkbookUtil
import com.wxiwei.office.fc.util.BitFieldFactory.Companion.getInstance
import com.wxiwei.office.fc.util.HexDump.byteToHex
import com.wxiwei.office.fc.util.HexDump.intToHex
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.fc.util.StringUtil.hasMultibyte
import com.wxiwei.office.fc.util.StringUtil.putCompressedUnicode
import com.wxiwei.office.fc.util.StringUtil.putUnicodeLE
import java.util.Arrays

/**
 * Title:        Bound Sheet Record (aka BundleSheet) (0x0085)<P>
 * Description:  Defines a sheet within a workbook.  Basically stores the sheet name
 * and tells where the Beginning of file record is within the HSSF
 * file. </P><P>
 * REFERENCE:  PG 291 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Sergei Kozello (sergeikozello at mail.ru)
</P> */
class BoundSheetRecord : StandardRecord {
    /**
     * get the offset in bytes of the Beginning of File Marker within the HSSF Stream part of the POIFS file
     * 
     * @return offset in bytes
     */
    /**
     * set the offset in bytes of the Beginning of File Marker within the HSSF
     * Stream part of the POIFS file
     * 
     * @param pos offset in bytes
     */
    var positionOfBof: Int = 0
    private var field_2_option_flags: Int
    private var field_4_isMultibyteUnicode = 0
    private var field_5_sheetname: String? = null

    constructor(sheetname: String) {
        field_2_option_flags = 0
        this.sheetname = sheetname
    }

    /**
     * UTF8: sid + len + bof + flags + len(str) + unicode + str 2 + 2 + 4 + 2 +
     * 1 + 1 + len(str)
     * 
     * UNICODE: sid + len + bof + flags + len(str) + unicode + str 2 + 2 + 4 + 2 +
     * 1 + 1 + 2 * len(str)
     */
    constructor(`in`: RecordInputStream) {
        this.positionOfBof = `in`.readInt()
        field_2_option_flags = `in`.readUShort()
        val field_3_sheetname_length = `in`.readUByte()
        field_4_isMultibyteUnicode = `in`.readByte().toInt()

        if (this.isMultibyte) {
            field_5_sheetname = `in`.readUnicodeLEString(field_3_sheetname_length)
        } else {
            field_5_sheetname = `in`.readCompressedUnicode(field_3_sheetname_length)
        }
    }

    private val isMultibyte: Boolean
        get() = (field_4_isMultibyteUnicode and 0x01) != 0

    var sheetname: String
        /**
         * get the sheetname for this sheet.  (this appears in the tabs at the bottom)
         * @return sheetname the name of the sheet
         */
        get() = field_5_sheetname!!
        /**
         * Set the sheetname for this sheet.  (this appears in the tabs at the bottom)
         * @param sheetName the name of the sheet
         * @see WorkbookUtil.createSafeSheetName
         * @throws IllegalArgumentException if sheet name will cause excel to crash.
         */
        set(sheetName) {
            WorkbookUtil.validateSheetName(sheetName)
            field_5_sheetname = sheetName
            field_4_isMultibyteUnicode =
                if (hasMultibyte(sheetName)) 1 else 0
        }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[BOUNDSHEET]\n")
        buffer.append("    .bof        = ").append(intToHex(this.positionOfBof)).append("\n")
        buffer.append("    .options    = ").append(shortToHex(field_2_option_flags)).append("\n")
        buffer.append("    .unicodeflag= ").append(byteToHex(field_4_isMultibyteUnicode))
            .append("\n")
        buffer.append("    .sheetname  = ").append(field_5_sheetname).append("\n")
        buffer.append("[/BOUNDSHEET]\n")
        return buffer.toString()
    }

    override fun getDataSize(): Int {
        return 8 + field_5_sheetname!!.length * (if (this.isMultibyte) 2 else 1)
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeInt(this.positionOfBof)
        out.writeShort(field_2_option_flags)

        val name = field_5_sheetname!!
        out.writeByte(name.length)
        out.writeByte(field_4_isMultibyteUnicode)

        if (this.isMultibyte) {
            putUnicodeLE(name, out)
        } else {
            putCompressedUnicode(name, out)
        }
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    var isHidden: Boolean
        /**
         * Is the sheet hidden? Different from very hidden
         */
        get() = hiddenFlag.isSet(field_2_option_flags)
        /**
         * Is the sheet hidden? Different from very hidden
         */
        set(hidden) {
            field_2_option_flags =
                hiddenFlag.setBoolean(field_2_option_flags, hidden)
        }

    var isVeryHidden: Boolean
        /**
         * Is the sheet very hidden? Different from (normal) hidden
         */
        get() = veryHiddenFlag.isSet(field_2_option_flags)
        /**
         * Is the sheet very hidden? Different from (normal) hidden
         */
        set(veryHidden) {
            field_2_option_flags = veryHiddenFlag.setBoolean(
                field_2_option_flags,
                veryHidden
            )
        }

    companion object {
        const val sid: Short = 0x0085

        private val hiddenFlag = getInstance(0x01)
        private val veryHiddenFlag = getInstance(0x02)

        /**
         * Converts a List of [BoundSheetRecord]s to an array and sorts by the position of their
         * BOFs.
         */
        fun orderByBofPosition(boundSheetRecords: MutableList<BoundSheetRecord>): Array<BoundSheetRecord> {
            val bsrs = boundSheetRecords.toTypedArray()
            Arrays.sort(bsrs, BOFComparator)
            return bsrs
        }

        private val BOFComparator: Comparator<BoundSheetRecord> =
            object : Comparator<BoundSheetRecord> {
                override fun compare(bsr1: BoundSheetRecord, bsr2: BoundSheetRecord): Int {
                    return bsr1.positionOfBof - bsr2.positionOfBof
                }
            }
    }
}
