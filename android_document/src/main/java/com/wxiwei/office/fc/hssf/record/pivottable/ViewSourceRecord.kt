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


/**
 * SXVS - View Source (0x00E3)<br></br>
 * 
 * @author Patrick Cheng
 */
class ViewSourceRecord(`in`: RecordInputStream) : StandardRecord() {
    private val vs: Int

    init {
        vs = `in`.readShort().toInt()
    }

    override fun serialize(out: LittleEndianOutput) {
        out.writeShort(vs)
    }

    override fun getDataSize(): Int {
        return 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[SXVS]\n")
        buffer.append("    .vs      =").append(shortToHex(vs)).append('\n')

        buffer.append("[/SXVS]\n")
        return buffer.toString()
    }

    companion object {
        const val sid: Short = 0x00E3
    }
}
