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
package com.wxiwei.office.fc.hssf.record.chart

import com.wxiwei.office.fc.hssf.record.RecordInputStream
import com.wxiwei.office.fc.hssf.record.StandardRecord
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.fc.util.StringUtil
import com.wxiwei.office.fc.util.StringUtil.hasMultibyte


/**
 * SERIESTEXT (0x100D)
 * Defines a series name
 * 
 * @author Andrew C. Oliver (acoliver at apache.org)
 */
class SeriesTextRecord : StandardRecord {
    /**
     * Get the id field for the SeriesText record.
     */
    /**
     * Set the id field for the SeriesText record.
     */
    var id: Int = 0
    private var is16bit: Boolean
    private var field_4_text: String? = null

    constructor() {
        field_4_text = ""
        is16bit = false
    }

    constructor(`in`: RecordInputStream) {
        this.id = `in`.readUShort()
        val field_2_textLength = `in`.readUByte()
        is16bit = (`in`.readUByte() and 0x01) != 0
        if (is16bit) {
            field_4_text = `in`.readUnicodeLEString(field_2_textLength)
        } else {
            field_4_text = `in`.readCompressedUnicode(field_2_textLength)
        }
    }

    override fun toString(): String {
        val sb = StringBuffer()

        sb.append("[SERIESTEXT]\n")
        sb.append("  .id     =").append(shortToHex(this.id)).append('\n')
        sb.append("  .textLen=").append(field_4_text!!.length).append('\n')
        sb.append("  .is16bit=").append(is16bit).append('\n')
        sb.append("  .text   =").append(" (").append(this.text).append(" )").append('\n')
        sb.append("[/SERIESTEXT]\n")
        return sb.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(this.id)
        out.writeByte(field_4_text!!.length)
        if (is16bit) {
            // Excel (2007) seems to choose 16bit regardless of whether it is needed
            out.writeByte(0x01)
            StringUtil.putUnicodeLE(field_4_text!!, out)
        } else {
            // Excel can read this OK
            out.writeByte(0x00)
            StringUtil.putCompressedUnicode(field_4_text!!, out)
        }
    }

    override fun getDataSize(): Int {
        return 2 + 1 + 1 + field_4_text!!.length * (if (is16bit) 2 else 1)
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = SeriesTextRecord()

        rec.id = this.id
        rec.is16bit = is16bit
        rec.field_4_text = field_4_text
        return rec
    }

    var text: String
        /**
         * Get the text field for the SeriesText record.
         */
        get() = field_4_text!!
        /**
         * Set the text field for the SeriesText record.
         */
        set(text) {
            require(text.length <= MAX_LEN) {
                ("Text is too long ("
                        + text.length + ">" + MAX_LEN + ")")
            }
            field_4_text = text
            is16bit = hasMultibyte(text)
        }

    companion object {
        const val sid: Short = 0x100D

        /** the actual text cannot be longer than 255 characters  */
        private const val MAX_LEN = 0xFF
    }
}
