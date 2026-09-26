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
 * The axis record defines the type of an axis.
 *
 *
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class AxisRecord : StandardRecord {
    /**
     * Get the axis type field for the Axis record.
     * 
     * @return  One of
     * AXIS_TYPE_CATEGORY_OR_X_AXIS
     * AXIS_TYPE_VALUE_AXIS
     * AXIS_TYPE_SERIES_AXIS
     */
    /**
     * Set the axis type field for the Axis record.
     * 
     * @param field_1_axisType
     * One of
     * AXIS_TYPE_CATEGORY_OR_X_AXIS
     * AXIS_TYPE_VALUE_AXIS
     * AXIS_TYPE_SERIES_AXIS
     */
    var axisType: Short = 0
    /**
     * Get the reserved1 field for the Axis record.
     */
    /**
     * Set the reserved1 field for the Axis record.
     */
    var reserved1: Int = 0
    /**
     * Get the reserved2 field for the Axis record.
     */
    /**
     * Set the reserved2 field for the Axis record.
     */
    var reserved2: Int = 0
    /**
     * Get the reserved3 field for the Axis record.
     */
    /**
     * Set the reserved3 field for the Axis record.
     */
    var reserved3: Int = 0
    /**
     * Get the reserved4 field for the Axis record.
     */
    /**
     * Set the reserved4 field for the Axis record.
     */
    var reserved4: Int = 0


    constructor()

    constructor(`in`: RecordInputStream) {
        this.axisType = `in`.readShort()
        this.reserved1 = `in`.readInt()
        this.reserved2 = `in`.readInt()
        this.reserved3 = `in`.readInt()
        this.reserved4 = `in`.readInt()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[AXIS]\n")
        buffer.append("    .axisType             = ")
            .append("0x").append(toHex(this.axisType))
            .append(" (").append(this.axisType.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .reserved1            = ")
            .append("0x").append(toHex(this.reserved1))
            .append(" (").append(this.reserved1).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .reserved2            = ")
            .append("0x").append(toHex(this.reserved2))
            .append(" (").append(this.reserved2).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .reserved3            = ")
            .append("0x").append(toHex(this.reserved3))
            .append(" (").append(this.reserved3).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .reserved4            = ")
            .append("0x").append(toHex(this.reserved4))
            .append(" (").append(this.reserved4).append(" )")
        buffer.append(System.getProperty("line.separator"))

        buffer.append("[/AXIS]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(axisType.toInt())
        out.writeInt(this.reserved1)
        out.writeInt(this.reserved2)
        out.writeInt(this.reserved3)
        out.writeInt(this.reserved4)
    }

    override fun getDataSize(): Int {
        return 2 + 4 + 4 + 4 + 4
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = AxisRecord()

        rec.axisType = this.axisType
        rec.reserved1 = this.reserved1
        rec.reserved2 = this.reserved2
        rec.reserved3 = this.reserved3
        rec.reserved4 = this.reserved4
        return rec
    }


    companion object {
        const val sid: Short = 0x101d
        const val AXIS_TYPE_CATEGORY_OR_X_AXIS: Short = 0
        const val AXIS_TYPE_VALUE_AXIS: Short = 1
        const val AXIS_TYPE_SERIES_AXIS: Short = 2
    }
}
