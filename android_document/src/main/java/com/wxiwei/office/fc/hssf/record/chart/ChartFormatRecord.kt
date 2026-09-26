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
import com.wxiwei.office.fc.util.BitFieldFactory.Companion.getInstance
import com.wxiwei.office.fc.util.HexDump.intToHex
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * Class ChartFormatRecord (0x1014)
 *
 *
 * 
 * (As with all chart related records, documentation is lacking.
 * See [ChartRecord] for more details)
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class ChartFormatRecord : StandardRecord {
    // ignored?
    var xPosition: Int = 0 // lower left
    var yPosition: Int = 0 // lower left
    var width: Int = 0
    var height: Int = 0
    private var field5_grbit = 0
    private var field6_unknown = 0

    constructor()

    constructor(`in`: RecordInputStream) {
        this.xPosition = `in`.readInt()
        this.yPosition = `in`.readInt()
        this.width = `in`.readInt()
        this.height = `in`.readInt()
        field5_grbit = `in`.readUShort()
        field6_unknown = `in`.readUShort()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[CHARTFORMAT]\n")
        buffer.append("    .xPosition       = ").append(this.xPosition).append("\n")
        buffer.append("    .yPosition       = ").append(this.yPosition).append("\n")
        buffer.append("    .width           = ").append(this.width).append("\n")
        buffer.append("    .height          = ").append(this.height).append("\n")
        buffer.append("    .grBit           = ").append(intToHex(field5_grbit)).append("\n")
        buffer.append("[/CHARTFORMAT]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeInt(this.xPosition)
        out.writeInt(this.yPosition)
        out.writeInt(this.width)
        out.writeInt(this.height)
        out.writeShort(field5_grbit)
        out.writeShort(field6_unknown)
    }

    override fun getDataSize(): Int {
        return 20 // 4 ints and 2 shorts
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    var varyDisplayPattern: Boolean
        get() = Companion.varyDisplayPattern.isSet(field5_grbit)
        set(value) {
            field5_grbit =
                Companion.varyDisplayPattern.setBoolean(field5_grbit, value)
        }

    companion object {
        const val sid: Short = 0x1014

        private val varyDisplayPattern = getInstance(0x01)
    }
}
