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
import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * The font index record indexes into the font table for the text record.
 *
 *
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class FontIndexRecord : StandardRecord {
    /**
     * Get the font index field for the FontIndex record.
     */
    /**
     * Set the font index field for the FontIndex record.
     */
    var fontIndex: Short = 0


    constructor()

    constructor(`in`: RecordInputStream) {
        this.fontIndex = `in`.readShort()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[FONTX]\n")
        buffer.append("    .fontIndex            = ")
            .append("0x").append(toHex(this.fontIndex))
            .append(" (").append(this.fontIndex.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))

        buffer.append("[/FONTX]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(fontIndex.toInt())
    }

    override fun getDataSize(): Int {
        return 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = FontIndexRecord()

        rec.fontIndex = this.fontIndex
        return rec
    }


    companion object {
        const val sid: Short = 0x1026
    }
}
