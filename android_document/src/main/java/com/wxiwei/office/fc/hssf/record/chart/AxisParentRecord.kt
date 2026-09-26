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
import com.wxiwei.office.fc.hssf.record.UnknownRecord
import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndian.getInt
import com.wxiwei.office.fc.util.LittleEndian.getShort
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * The axis size and location
 *
 *
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class AxisParentRecord : StandardRecord {
    /**
     * Get the axis type field for the AxisParent record.
     * 
     * @return  One of
     * AXIS_TYPE_MAIN
     * AXIS_TYPE_SECONDARY
     */
    /**
     * Set the axis type field for the AxisParent record.
     * 
     * @param field_1_axisType
     * One of
     * AXIS_TYPE_MAIN
     * AXIS_TYPE_SECONDARY
     */
    var axisType: Short = 0
    /**
     * Get the x field for the AxisParent record.
     */
    /**
     * Set the x field for the AxisParent record.
     */
    var x: Int = 0
    /**
     * Get the y field for the AxisParent record.
     */
    /**
     * Set the y field for the AxisParent record.
     */
    var y: Int = 0
    /**
     * Get the width field for the AxisParent record.
     */
    /**
     * Set the width field for the AxisParent record.
     */
    var width: Int = 0
    /**
     * Get the height field for the AxisParent record.
     */
    /**
     * Set the height field for the AxisParent record.
     */
    var height: Int = 0


    constructor()

    constructor(`in`: RecordInputStream) {
        this.axisType = `in`.readShort()
        this.x = `in`.readInt()
        this.y = `in`.readInt()
        this.width = `in`.readInt()
        this.height = `in`.readInt()
    }

    constructor(unknownRecord: UnknownRecord) {
        if (unknownRecord.getSid() == Companion.sid && unknownRecord.data.size == getDataSize()) {
            val data = unknownRecord.data

            this.axisType = getShort(data, 0)
            this.x = getInt(data, 2)
            this.y = getInt(data, 6)
            this.width = getInt(data, 10)
            this.height = getInt(data, 14)
        }
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[AXISPARENT]\n")
        buffer.append("    .axisType             = ")
            .append("0x").append(toHex(this.axisType))
            .append(" (").append(this.axisType.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .x                    = ")
            .append("0x").append(toHex(this.x))
            .append(" (").append(this.x).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .y                    = ")
            .append("0x").append(toHex(this.y))
            .append(" (").append(this.y).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .width                = ")
            .append("0x").append(toHex(this.width))
            .append(" (").append(this.width).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .height               = ")
            .append("0x").append(toHex(this.height))
            .append(" (").append(this.height).append(" )")
        buffer.append(System.getProperty("line.separator"))

        buffer.append("[/AXISPARENT]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(axisType.toInt())
        out.writeInt(this.x)
        out.writeInt(this.y)
        out.writeInt(this.width)
        out.writeInt(this.height)
    }

    override fun getDataSize(): Int {
        return 2 + 4 + 4 + 4 + 4
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = AxisParentRecord()

        rec.axisType = this.axisType
        rec.x = this.x
        rec.y = this.y
        rec.width = this.width
        rec.height = this.height
        return rec
    }


    companion object {
        const val sid: Short = 0x1041
        const val AXIS_TYPE_MAIN: Short = 0
        const val AXIS_TYPE_SECONDARY: Short = 1
    }
}
