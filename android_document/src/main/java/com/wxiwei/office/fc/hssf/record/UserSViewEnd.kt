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

import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndianOutput
import java.util.Locale


/**
 * The UserSViewEnd record marks the end of the settings for a custom view associated with the sheet
 * 
 * @author Yegor Kozlov
 */
class UserSViewEnd : StandardRecord {
    private val _rawData: ByteArray

    constructor(data: ByteArray) {
        _rawData = data
    }

    /**
     * construct an UserSViewEnd record.  No fields are interpreted and the record will
     * be serialized in its original form more or less
     * @param in the RecordInputstream to read the record from
     */
    constructor(`in`: RecordInputStream) {
        _rawData = `in`.readRemainder()
    }

    /**
     * spit the record out AS IS. no interpretation or identification
     */
    public override fun serialize(out: LittleEndianOutput) {
        out.write(_rawData)
    }

    override fun getDataSize(): Int {
        return _rawData.size
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun toString(): String {
        val sb = StringBuffer()

        sb.append("[").append("USERSVIEWEND").append("] (0x")
        sb.append(Integer.toHexString(Companion.sid.toInt()).uppercase(Locale.getDefault()) + ")\n")
        sb.append("  rawData=").append(toHex(_rawData)).append("\n")
        sb.append("[/").append("USERSVIEWEND").append("]\n")
        return sb.toString()
    }

    //HACK: do a "cheat" clone, see Record.java for more information
    override fun clone(): Any {
        return cloneViaReserialise()
    }


    companion object {
        const val sid: Short = 0x01AB
    }
}
