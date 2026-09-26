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
import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * The area record is used to define a area chart.
 *
 *
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class AreaRecord : StandardRecord {
    /**
     * Get the format flags field for the Area record.
     */
    /**
     * Set the format flags field for the Area record.
     */
    var formatFlags: Short = 0

    constructor()

    constructor(`in`: RecordInputStream) {
        this.formatFlags = `in`.readShort()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[AREA]\n")
        buffer.append("    .formatFlags          = ")
            .append("0x").append(toHex(this.formatFlags))
            .append(" (").append(this.formatFlags.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("         .stacked                  = ").append(this.isStacked).append('\n')
        buffer.append("         .displayAsPercentage      = ").append(this.isDisplayAsPercentage)
            .append('\n')
        buffer.append("         .shadow                   = ").append(this.isShadow).append('\n')

        buffer.append("[/AREA]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(formatFlags.toInt())
    }

    override fun getDataSize(): Int {
        return 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = AreaRecord()

        rec.formatFlags = this.formatFlags
        return rec
    }


    var isStacked: Boolean
        /**
         * series is stacked
         * @return  the stacked field value.
         */
        get() = stacked.isSet(formatFlags.toInt())
        /**
         * Sets the stacked field value.
         * series is stacked
         */
        set(value) {
            this.formatFlags =
                stacked.setShortBoolean(this.formatFlags, value)
        }

    var isDisplayAsPercentage: Boolean
        /**
         * results displayed as percentages
         * @return  the display as percentage field value.
         */
        get() = displayAsPercentage.isSet(formatFlags.toInt())
        /**
         * Sets the display as percentage field value.
         * results displayed as percentages
         */
        set(value) {
            this.formatFlags =
                displayAsPercentage.setShortBoolean(this.formatFlags, value)
        }

    var isShadow: Boolean
        /**
         * display a shadow for the chart
         * @return  the shadow field value.
         */
        get() = shadow.isSet(formatFlags.toInt())
        /**
         * Sets the shadow field value.
         * display a shadow for the chart
         */
        set(value) {
            this.formatFlags = shadow.setShortBoolean(this.formatFlags, value)
        }

    companion object {
        const val sid: Short = 0x101A
        private val stacked = getInstance(0x1)
        private val displayAsPercentage = getInstance(0x2)
        private val shadow = getInstance(0x4)
    }
}
