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
 * Describes a line format record.  The line format record controls how a line on a chart appears.
 *
 *
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class LineFormatRecord : StandardRecord {
    /**
     * Get the line color field for the LineFormat record.
     */
    /**
     * Set the line color field for the LineFormat record.
     */
    var lineColor: Int = 0
    /**
     * Get the line pattern field for the LineFormat record.
     * 
     * @return  One of
     * LINE_PATTERN_SOLID
     * LINE_PATTERN_DASH
     * LINE_PATTERN_DOT
     * LINE_PATTERN_DASH_DOT
     * LINE_PATTERN_DASH_DOT_DOT
     * LINE_PATTERN_NONE
     * LINE_PATTERN_DARK_GRAY_PATTERN
     * LINE_PATTERN_MEDIUM_GRAY_PATTERN
     * LINE_PATTERN_LIGHT_GRAY_PATTERN
     */
    /**
     * Set the line pattern field for the LineFormat record.
     * 
     * @param field_2_linePattern
     * One of
     * LINE_PATTERN_SOLID
     * LINE_PATTERN_DASH
     * LINE_PATTERN_DOT
     * LINE_PATTERN_DASH_DOT
     * LINE_PATTERN_DASH_DOT_DOT
     * LINE_PATTERN_NONE
     * LINE_PATTERN_DARK_GRAY_PATTERN
     * LINE_PATTERN_MEDIUM_GRAY_PATTERN
     * LINE_PATTERN_LIGHT_GRAY_PATTERN
     */
    var linePattern: Short = 0
    /**
     * Get the weight field for the LineFormat record.
     * 
     * @return  One of
     * WEIGHT_HAIRLINE
     * WEIGHT_NARROW
     * WEIGHT_MEDIUM
     * WEIGHT_WIDE
     */
    /**
     * Set the weight field for the LineFormat record.
     * 
     * @param field_3_weight
     * One of
     * WEIGHT_HAIRLINE
     * WEIGHT_NARROW
     * WEIGHT_MEDIUM
     * WEIGHT_WIDE
     */
    var weight: Short = 0
    /**
     * Get the format field for the LineFormat record.
     */
    /**
     * Set the format field for the LineFormat record.
     */
    var format: Short = 0
    /**
     * Get the colour palette index field for the LineFormat record.
     */
    /**
     * Set the colour palette index field for the LineFormat record.
     */
    var colourPaletteIndex: Short = 0


    constructor()

    constructor(`in`: RecordInputStream) {
        this.lineColor = `in`.readInt()
        this.linePattern = `in`.readShort()
        this.weight = `in`.readShort()
        this.format = `in`.readShort()
        this.colourPaletteIndex = `in`.readShort()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[LINEFORMAT]\n")
        buffer.append("    .lineColor            = ")
            .append("0x").append(toHex(this.lineColor))
            .append(" (").append(this.lineColor).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .linePattern          = ")
            .append("0x").append(toHex(this.linePattern))
            .append(" (").append(this.linePattern.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .weight               = ")
            .append("0x").append(toHex(this.weight))
            .append(" (").append(this.weight.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .format               = ")
            .append("0x").append(toHex(this.format))
            .append(" (").append(this.format.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("         .auto                     = ").append(this.isAuto).append('\n')
        buffer.append("         .drawTicks                = ").append(this.isDrawTicks).append('\n')
        buffer.append("         .unknown                  = ").append(this.isUnknown).append('\n')
        buffer.append("    .colourPaletteIndex   = ")
            .append("0x").append(toHex(this.colourPaletteIndex))
            .append(" (").append(this.colourPaletteIndex.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))

        buffer.append("[/LINEFORMAT]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeInt(this.lineColor)
        out.writeShort(linePattern.toInt())
        out.writeShort(weight.toInt())
        out.writeShort(format.toInt())
        out.writeShort(colourPaletteIndex.toInt())
    }

    override fun getDataSize(): Int {
        return 4 + 2 + 2 + 2 + 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = LineFormatRecord()

        rec.lineColor = this.lineColor
        rec.linePattern = this.linePattern
        rec.weight = this.weight
        rec.format = this.format
        rec.colourPaletteIndex = this.colourPaletteIndex
        return rec
    }


    var isAuto: Boolean
        /**
         * automatic format
         * @return  the auto field value.
         */
        get() = auto.isSet(format.toInt())
        /**
         * Sets the auto field value.
         * automatic format
         */
        set(value) {
            this.format = auto.setShortBoolean(this.format, value)
        }

    var isDrawTicks: Boolean
        /**
         * draw tick marks
         * @return  the draw ticks field value.
         */
        get() = drawTicks.isSet(format.toInt())
        /**
         * Sets the draw ticks field value.
         * draw tick marks
         */
        set(value) {
            this.format = drawTicks.setShortBoolean(this.format, value)
        }

    var isUnknown: Boolean
        /**
         * book marks this as reserved = 0 but it seems to do something
         * @return  the unknown field value.
         */
        get() = unknown.isSet(format.toInt())
        /**
         * Sets the unknown field value.
         * book marks this as reserved = 0 but it seems to do something
         */
        set(value) {
            this.format = unknown.setShortBoolean(this.format, value)
        }

    companion object {
        const val sid: Short = 0x1007

        private val auto = getInstance(0x1)
        private val drawTicks = getInstance(0x4)
        private val unknown = getInstance(0x4)

        const val LINE_PATTERN_SOLID: Short = 0
        const val LINE_PATTERN_DASH: Short = 1
        const val LINE_PATTERN_DOT: Short = 2
        const val LINE_PATTERN_DASH_DOT: Short = 3
        const val LINE_PATTERN_DASH_DOT_DOT: Short = 4
        const val LINE_PATTERN_NONE: Short = 5
        const val LINE_PATTERN_DARK_GRAY_PATTERN: Short = 6
        const val LINE_PATTERN_MEDIUM_GRAY_PATTERN: Short = 7
        const val LINE_PATTERN_LIGHT_GRAY_PATTERN: Short = 8
        val WEIGHT_HAIRLINE: Short = -1
        const val WEIGHT_NARROW: Short = 0
        const val WEIGHT_MEDIUM: Short = 1
        const val WEIGHT_WIDE: Short = 2
    }
}
