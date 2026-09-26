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

import com.wxiwei.office.fc.hssf.record.cont.ContinuableRecord
import com.wxiwei.office.fc.hssf.record.cont.ContinuableRecordOutput
import com.wxiwei.office.fc.util.StringUtil.hasMultibyte


/**
 * STRING (0x0207)
 *
 *
 * 
 * Stores the cached result of a text formula
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class StringRecord : ContinuableRecord {
    private var _is16bitUnicode = false
    private var _text: String? = null


    constructor()

    /**
     * @param in the RecordInputstream to read the record from
     */
    constructor(`in`: RecordInputStream) {
        val field_1_string_length = `in`.readUShort()
        _is16bitUnicode = `in`.readByte().toInt() != 0x00

        if (_is16bitUnicode) {
            _text = `in`.readUnicodeLEString(field_1_string_length)
        } else {
            _text = `in`.readCompressedUnicode(field_1_string_length)
        }
    }


    override fun serialize(out: ContinuableRecordOutput) {
        out.writeShort(_text!!.length)
        out.writeStringData(_text!!)
    }


    override fun getSid(): Short {
        return Companion.sid
    }

    /**
     * @return The string represented by this record.
     */
    fun getString(): String {
        return _text!!
    }


    /**
     * Sets the string represented by this record.
     */
    fun setString(string: String) {
        _text = string
        _is16bitUnicode = hasMultibyte(string)
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[STRING]\n")
        buffer.append("    .string            = ")
            .append(_text).append("\n")
        buffer.append("[/STRING]\n")
        return buffer.toString()
    }

    override fun clone(): Any {
        val rec = StringRecord()
        rec._is16bitUnicode = _is16bitUnicode
        rec._text = _text
        return rec
    }

    companion object {
        const val sid: Short = 0x0207
    }
}
