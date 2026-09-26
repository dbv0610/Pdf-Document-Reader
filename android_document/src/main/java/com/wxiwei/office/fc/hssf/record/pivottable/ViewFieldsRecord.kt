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

import com.wxiwei.office.fc.hssf.record.RecordInputStream
import com.wxiwei.office.fc.hssf.record.StandardRecord
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.fc.util.StringUtil
import com.wxiwei.office.fc.util.StringUtil.hasMultibyte


/**
 * SXVD - View Fields (0x00B1)<br></br>
 * 
 * @author Patrick Cheng
 */
class ViewFieldsRecord(`in`: RecordInputStream) : StandardRecord() {
    private val _sxaxis: Int
    private val _cSub: Int
    private val _grbitSub: Int
    private val _cItm: Int

    private var _name: String? = null

    /**
     * values for the [_sxaxis] field
     */
    private object Axis {
        const val NO_AXIS: Int = 0
        const val ROW: Int = 1
        const val COLUMN: Int = 2
        const val PAGE: Int = 4
        const val DATA: Int = 8
    }

    init {
        _sxaxis = `in`.readShort().toInt()
        _cSub = `in`.readShort().toInt()
        _grbitSub = `in`.readShort().toInt()
        _cItm = `in`.readShort().toInt()

        val cchName = `in`.readUShort()
        if (cchName != STRING_NOT_PRESENT_LEN) {
            val flag = `in`.readByte().toInt()
            if ((flag and 0x01) != 0) {
                _name = `in`.readUnicodeLEString(cchName)
            } else {
                _name = `in`.readCompressedUnicode(cchName)
            }
        }
    }

    override fun serialize(out: LittleEndianOutput) {
        out.writeShort(_sxaxis)
        out.writeShort(_cSub)
        out.writeShort(_grbitSub)
        out.writeShort(_cItm)

        if (_name != null) {
            StringUtil.writeUnicodeString(out, _name!!)
        } else {
            out.writeShort(STRING_NOT_PRESENT_LEN)
        }
    }

    override fun getDataSize(): Int {
        if (_name == null) {
            return BASE_SIZE
        }
        return (BASE_SIZE
                + 1 // unicode flag 
                + _name!!.length * (if (hasMultibyte(_name)) 2 else 1))
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun toString(): String {
        val buffer = StringBuffer()
        buffer.append("[SXVD]\n")
        buffer.append("    .sxaxis    = ").append(shortToHex(_sxaxis)).append('\n')
        buffer.append("    .cSub      = ").append(shortToHex(_cSub)).append('\n')
        buffer.append("    .grbitSub  = ").append(shortToHex(_grbitSub)).append('\n')
        buffer.append("    .cItm      = ").append(shortToHex(_cItm)).append('\n')
        buffer.append("    .name      = ").append(_name).append('\n')

        buffer.append("[/SXVD]\n")
        return buffer.toString()
    }

    companion object {
        const val sid: Short = 0x00B1

        /** the value of the <tt>cchName</tt> field when the [._name] is not present  */
        private const val STRING_NOT_PRESENT_LEN = 0xFFFF

        /** 5 shorts  */
        private const val BASE_SIZE = 10
    }
}
