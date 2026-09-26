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
 * The Tick record defines how tick marks and label positioning/formatting
 *
 *
 * 
 * @author Andrew C. Oliver(acoliver at apache.org)
 */
class TickRecord : StandardRecord {
    /**
     * Get the major tick type field for the Tick record.
     */
    /**
     * Set the major tick type field for the Tick record.
     */
    var majorTickType: Byte = 0
    /**
     * Get the minor tick type field for the Tick record.
     */
    /**
     * Set the minor tick type field for the Tick record.
     */
    var minorTickType: Byte = 0
    /**
     * Get the label position field for the Tick record.
     */
    /**
     * Set the label position field for the Tick record.
     */
    var labelPosition: Byte = 0
    /**
     * Get the background field for the Tick record.
     */
    /**
     * Set the background field for the Tick record.
     */
    var background: Byte = 0
    /**
     * Get the label color rgb field for the Tick record.
     */
    /**
     * Set the label color rgb field for the Tick record.
     */
    var labelColorRgb: Int = 0
    /**
     * Get the zero 1 field for the Tick record.
     */
    /**
     * Set the zero 1 field for the Tick record.
     */
    var zero1: Int = 0
    /**
     * Get the zero 2 field for the Tick record.
     */
    /**
     * Set the zero 2 field for the Tick record.
     */
    var zero2: Int = 0
    private var field_8_zero3 = 0
    private var field_9_zero4 = 0
    /**
     * Get the options field for the Tick record.
     */
    /**
     * Set the options field for the Tick record.
     */
    var options: Short = 0
    /**
     * Get the tick color field for the Tick record.
     */
    /**
     * Set the tick color field for the Tick record.
     */
    var tickColor: Short = 0
    /**
     * Get the zero 3 field for the Tick record.
     */
    /**
     * Set the zero 3 field for the Tick record.
     */
    var zero3: Short = 0


    constructor()

    constructor(`in`: RecordInputStream) {
        this.majorTickType = `in`.readByte()
        this.minorTickType = `in`.readByte()
        this.labelPosition = `in`.readByte()
        this.background = `in`.readByte()
        this.labelColorRgb = `in`.readInt()
        this.zero1 = `in`.readInt()
        this.zero2 = `in`.readInt()
        field_8_zero3 = `in`.readInt()
        field_9_zero4 = `in`.readInt()

        this.options = `in`.readShort()
        this.tickColor = `in`.readShort()
        this.zero3 = `in`.readShort()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[TICK]\n")
        buffer.append("    .majorTickType        = ")
            .append("0x").append(toHex(this.majorTickType))
            .append(" (").append(this.majorTickType.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .minorTickType        = ")
            .append("0x").append(toHex(this.minorTickType))
            .append(" (").append(this.minorTickType.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .labelPosition        = ")
            .append("0x").append(toHex(this.labelPosition))
            .append(" (").append(this.labelPosition.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .background           = ")
            .append("0x").append(toHex(this.background))
            .append(" (").append(this.background.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .labelColorRgb        = ")
            .append("0x").append(toHex(this.labelColorRgb))
            .append(" (").append(this.labelColorRgb).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .zero1                = ")
            .append("0x").append(toHex(this.zero1))
            .append(" (").append(this.zero1).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .zero2                = ")
            .append("0x").append(toHex(this.zero2))
            .append(" (").append(this.zero2).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .options              = ")
            .append("0x").append(toHex(this.options))
            .append(" (").append(this.options.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("         .autoTextColor            = ").append(this.isAutoTextColor)
            .append('\n')
        buffer.append("         .autoTextBackground       = ").append(this.isAutoTextBackground)
            .append('\n')
        buffer.append("         .rotation                 = ").append(this.rotation.toInt())
            .append('\n')
        buffer.append("         .autorotate               = ").append(this.isAutorotate)
            .append('\n')
        buffer.append("    .tickColor            = ")
            .append("0x").append(toHex(this.tickColor))
            .append(" (").append(this.tickColor.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .zero3                = ")
            .append("0x").append(toHex(this.zero3))
            .append(" (").append(this.zero3.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))

        buffer.append("[/TICK]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeByte(majorTickType.toInt())
        out.writeByte(minorTickType.toInt())
        out.writeByte(labelPosition.toInt())
        out.writeByte(background.toInt())
        out.writeInt(this.labelColorRgb)
        out.writeInt(this.zero1)
        out.writeInt(this.zero2)
        out.writeInt(field_8_zero3)
        out.writeInt(field_9_zero4)
        out.writeShort(options.toInt())
        out.writeShort(tickColor.toInt())
        out.writeShort(zero3.toInt())
    }

    override fun getDataSize(): Int {
        return 1 + 1 + 1 + 1 + 4 + 8 + 8 + 2 + 2 + 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = TickRecord()

        rec.majorTickType = this.majorTickType
        rec.minorTickType = this.minorTickType
        rec.labelPosition = this.labelPosition
        rec.background = this.background
        rec.labelColorRgb = this.labelColorRgb
        rec.zero1 = this.zero1
        rec.zero2 = this.zero2
        rec.field_8_zero3 = field_8_zero3
        rec.field_9_zero4 = field_9_zero4
        rec.options = this.options
        rec.tickColor = this.tickColor
        rec.zero3 = this.zero3
        return rec
    }


    var isAutoTextColor: Boolean
        /**
         * use the quote unquote automatic color for text
         * @return  the auto text color field value.
         */
        get() = autoTextColor.isSet(options.toInt())
        /**
         * Sets the auto text color field value.
         * use the quote unquote automatic color for text
         */
        set(value) {
            this.options = autoTextColor.setShortBoolean(this.options, value)
        }

    var isAutoTextBackground: Boolean
        /**
         * use the quote unquote automatic color for text background
         * @return  the auto text background field value.
         */
        get() = autoTextBackground.isSet(options.toInt())
        /**
         * Sets the auto text background field value.
         * use the quote unquote automatic color for text background
         */
        set(value) {
            this.options =
                autoTextBackground.setShortBoolean(this.options, value)
        }

    var rotation: Short
        /**
         * rotate text (0=none, 1=normal, 2=90 degrees counterclockwise, 3=90 degrees clockwise)
         * @return  the rotation field value.
         */
        get() = Companion.rotation.getShortValue(this.options)
        /**
         * Sets the rotation field value.
         * rotate text (0=none, 1=normal, 2=90 degrees counterclockwise, 3=90 degrees clockwise)
         */
        set(value) {
            this.options = Companion.rotation.setShortValue(this.options, value)
        }

    var isAutorotate: Boolean
        /**
         * automatically rotate the text
         * @return  the autorotate field value.
         */
        get() = autorotate.isSet(options.toInt())
        /**
         * Sets the autorotate field value.
         * automatically rotate the text
         */
        set(value) {
            this.options = autorotate.setShortBoolean(this.options, value)
        }

    companion object {
        const val sid: Short = 0x101E

        private val autoTextColor = getInstance(0x1)
        private val autoTextBackground = getInstance(0x2)
        private val rotation = getInstance(0x1c)
        private val autorotate = getInstance(0x20)
    }
}
