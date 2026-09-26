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

import com.wxiwei.office.fc.util.HexDump.intToHex
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.fc.util.StringUtil.putUnicodeLE

/**
 * TABLESTYLES (0x088E)<br></br>
 * 
 * @author Patrick Cheng
 */
class TableStylesRecord(`in`: RecordInputStream) : StandardRecord() {
    private val rt: Int
    private val grbitFrt: Int
    private val unused = ByteArray(8)
    private val cts: Int

    private val rgchDefListStyle: String
    private val rgchDefPivotStyle: String


    init {
        rt = `in`.readUShort()
        grbitFrt = `in`.readUShort()
        `in`.readFully(unused)
        cts = `in`.readInt()
        val cchDefListStyle = `in`.readUShort()
        val cchDefPivotStyle = `in`.readUShort()

        rgchDefListStyle = `in`.readUnicodeLEString(cchDefListStyle)
        rgchDefPivotStyle = `in`.readUnicodeLEString(cchDefPivotStyle)
    }

    override fun serialize(out: LittleEndianOutput) {
        out.writeShort(rt)
        out.writeShort(grbitFrt)
        out.write(unused)
        out.writeInt(cts)

        out.writeShort(rgchDefListStyle.length)
        out.writeShort(rgchDefPivotStyle.length)

        putUnicodeLE(rgchDefListStyle, out)
        putUnicodeLE(rgchDefPivotStyle, out)
    }

    override fun getDataSize(): Int {
        return (2 + 2 + 8 + 4 + 2 + 2
                + (2 * rgchDefListStyle.length) + (2 * rgchDefPivotStyle.length))
    }

    override fun getSid(): Short {
        return Companion.sid
    }


    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[TABLESTYLES]\n")
        buffer.append("    .rt      =").append(shortToHex(rt)).append('\n')
        buffer.append("    .grbitFrt=").append(shortToHex(grbitFrt)).append('\n')
        buffer.append("    .unused  =").append(toHex(unused)).append('\n')
        buffer.append("    .cts=").append(intToHex(cts)).append('\n')
        buffer.append("    .rgchDefListStyle=").append(rgchDefListStyle).append('\n')
        buffer.append("    .rgchDefPivotStyle=").append(rgchDefPivotStyle).append('\n')

        buffer.append("[/TABLESTYLES]\n")
        return buffer.toString()
    }

    companion object {
        const val sid: Short = 0x088E
    }
}
