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
 * The bar record is used to define a bar chart.
 *
 *
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class BarRecord : StandardRecord {
    /**
     * Get the bar space field for the Bar record.
     */
    /**
     * Set the bar space field for the Bar record.
     */
    var barSpace: Short = 0
    /**
     * Get the category space field for the Bar record.
     */
    /**
     * Set the category space field for the Bar record.
     */
    var categorySpace: Short = 0
    /**
     * Get the format flags field for the Bar record.
     */
    /**
     * Set the format flags field for the Bar record.
     */
    var formatFlags: Short = 0


    constructor()

    constructor(`in`: RecordInputStream) {
        this.barSpace = `in`.readShort()
        this.categorySpace = `in`.readShort()
        this.formatFlags = `in`.readShort()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[BAR]\n")
        buffer.append("    .barSpace             = ")
            .append("0x").append(toHex(this.barSpace))
            .append(" (").append(this.barSpace.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .categorySpace        = ")
            .append("0x").append(toHex(this.categorySpace))
            .append(" (").append(this.categorySpace.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .formatFlags          = ")
            .append("0x").append(toHex(this.formatFlags))
            .append(" (").append(this.formatFlags.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("         .horizontal               = ").append(this.isHorizontal)
            .append('\n')
        buffer.append("         .stacked                  = ").append(this.isStacked).append('\n')
        buffer.append("         .displayAsPercentage      = ").append(this.isDisplayAsPercentage)
            .append('\n')
        buffer.append("         .shadow                   = ").append(this.isShadow).append('\n')

        buffer.append("[/BAR]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(barSpace.toInt())
        out.writeShort(categorySpace.toInt())
        out.writeShort(formatFlags.toInt())
    }

    override fun getDataSize(): Int {
        return 2 + 2 + 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = BarRecord()

        rec.barSpace = this.barSpace
        rec.categorySpace = this.categorySpace
        rec.formatFlags = this.formatFlags
        return rec
    }


    var isHorizontal: Boolean
        /**
         * true to display horizontal bar charts, false for vertical
         * @return  the horizontal field value.
         */
        get() = horizontal.isSet(formatFlags.toInt())
        /**
         * Sets the horizontal field value.
         * true to display horizontal bar charts, false for vertical
         */
        set(value) {
            this.formatFlags =
                horizontal.setShortBoolean(this.formatFlags, value)
        }

    var isStacked: Boolean
        /**
         * stack displayed values
         * @return  the stacked field value.
         */
        get() = stacked.isSet(formatFlags.toInt())
        /**
         * Sets the stacked field value.
         * stack displayed values
         */
        set(value) {
            this.formatFlags = stacked.setShortBoolean(this.formatFlags, value)
        }

    var isDisplayAsPercentage: Boolean
        /**
         * display chart values as a percentage
         * @return  the display as percentage field value.
         */
        get() = displayAsPercentage.isSet(formatFlags.toInt())
        /**
         * Sets the display as percentage field value.
         * display chart values as a percentage
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
        const val sid: Short = 0x1017

        private val horizontal = getInstance(0x1)
        private val stacked = getInstance(0x2)
        private val displayAsPercentage = getInstance(0x4)
        private val shadow = getInstance(0x8)
    }
}
