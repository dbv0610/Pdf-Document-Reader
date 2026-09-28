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
package com.wxiwei.office.fc.hssf.record.pivottable

import com.wxiwei.office.fc.hssf.record.RecordFormatException
import com.wxiwei.office.fc.hssf.record.RecordInputStream
import com.wxiwei.office.fc.hssf.record.StandardRecord
import com.wxiwei.office.fc.util.HexDump.byteToHex
import com.wxiwei.office.fc.util.HexDump.intToHex
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.fc.util.StringUtil


/**
 * SXVDEX - Extended PivotTable View Fields (0x0100)<br></br>
 * 
 * @author Patrick Cheng
 */
class ExtendedPivotTableViewFieldsRecord(`in`: RecordInputStream) : StandardRecord() {
    private val _grbit1: Int
    private val _grbit2: Int
    private val _citmShow: Int
    private val _isxdiSort: Int
    private val _isxdiShow: Int
    private val _reserved1: Int
    private val _reserved2: Int

    /** custom sub-total name  */
    private var _subtotalName: String? = null

    init {
        _grbit1 = `in`.readInt()
        _grbit2 = `in`.readUByte()
        _citmShow = `in`.readUByte()
        _isxdiSort = `in`.readUShort()
        _isxdiShow = `in`.readUShort()
        // This record seems to have different valid encodings
        when (`in`.remaining()) {
            0 -> {
                // as per "Microsoft Excel Developer's Kit" book
                // older version of SXVDEX - doesn't seem to have a sub-total name
                _reserved1 = 0
                _reserved2 = 0
                _subtotalName = null
            }

            10 -> {
                val cchSubName = `in`.readUShort()
                _reserved1 = `in`.readInt()
                _reserved2 = `in`.readInt()
                if (cchSubName != STRING_NOT_PRESENT_LEN) {
                    _subtotalName = `in`.readUnicodeLEString(cchSubName)
                } else {
                    _subtotalName = null
                }
            }
            else -> throw RecordFormatException("Unexpected remaining size (" + `in`.remaining() + ")")
        }
    }

    override fun serialize(out: LittleEndianOutput) {
        out.writeInt(_grbit1)
        out.writeByte(_grbit2)
        out.writeByte(_citmShow)
        out.writeShort(_isxdiSort)
        out.writeShort(_isxdiShow)

        if (_subtotalName == null) {
            out.writeShort(STRING_NOT_PRESENT_LEN)
        } else {
            out.writeShort(_subtotalName!!.length)
        }

        out.writeInt(_reserved1)
        out.writeInt(_reserved2)
        if (_subtotalName != null) {
            StringUtil.putUnicodeLE(_subtotalName!!, out)
        }
    }

    override fun getDataSize(): Int {
        return 4 + 1 + 1 + 2 + 2 + 2 + 4 + 4 +
                (if (_subtotalName == null) 0 else (2 * _subtotalName!!.length)) // in unicode
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[SXVDEX]\n")

        buffer.append("    .grbit1 =").append(intToHex(_grbit1)).append("\n")
        buffer.append("    .grbit2 =").append(byteToHex(_grbit2)).append("\n")
        buffer.append("    .citmShow =").append(byteToHex(_citmShow)).append("\n")
        buffer.append("    .isxdiSort =").append(shortToHex(_isxdiSort)).append("\n")
        buffer.append("    .isxdiShow =").append(shortToHex(_isxdiShow)).append("\n")
        buffer.append("    .subtotalName =").append(_subtotalName).append("\n")
        buffer.append("[/SXVDEX]\n")
        return buffer.toString()
    }

    companion object {
        const val sid: Short = 0x0100

        /** the value of the subname length when the [._subtotalName] is not present  */
        private const val STRING_NOT_PRESENT_LEN = 0xFFFF
    }
}
