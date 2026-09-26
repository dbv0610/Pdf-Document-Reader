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
 * Defines a legend for a chart.
 *
 *
 * 
 * @author Andrew C. Oliver (acoliver at apache.org)
 */
class LegendRecord : StandardRecord {
    /**
     * Get the x axis upper left field for the Legend record.
     */
    /**
     * Set the x axis upper left field for the Legend record.
     */
    var xAxisUpperLeft: Int = 0
    /**
     * Get the y axis upper left field for the Legend record.
     */
    /**
     * Set the y axis upper left field for the Legend record.
     */
    var yAxisUpperLeft: Int = 0
    /**
     * Get the x size field for the Legend record.
     */
    /**
     * Set the x size field for the Legend record.
     */
    var xSize: Int = 0
    /**
     * Get the y size field for the Legend record.
     */
    /**
     * Set the y size field for the Legend record.
     */
    var ySize: Int = 0
    /**
     * Get the type field for the Legend record.
     * 
     * @return  One of
     * TYPE_BOTTOM
     * TYPE_CORNER
     * TYPE_TOP
     * TYPE_RIGHT
     * TYPE_LEFT
     * TYPE_UNDOCKED
     */
    /**
     * Set the type field for the Legend record.
     * 
     * @param field_5_type
     * One of
     * TYPE_BOTTOM
     * TYPE_CORNER
     * TYPE_TOP
     * TYPE_RIGHT
     * TYPE_LEFT
     * TYPE_UNDOCKED
     */
    var type: Byte = 0
    /**
     * Get the spacing field for the Legend record.
     * 
     * @return  One of
     * SPACING_CLOSE
     * SPACING_MEDIUM
     * SPACING_OPEN
     */
    /**
     * Set the spacing field for the Legend record.
     * 
     * @param field_6_spacing
     * One of
     * SPACING_CLOSE
     * SPACING_MEDIUM
     * SPACING_OPEN
     */
    var spacing: Byte = 0
    /**
     * Get the options field for the Legend record.
     */
    /**
     * Set the options field for the Legend record.
     */
    var options: Short = 0


    constructor()

    constructor(`in`: RecordInputStream) {
        this.xAxisUpperLeft = `in`.readInt()
        this.yAxisUpperLeft = `in`.readInt()
        this.xSize = `in`.readInt()
        this.ySize = `in`.readInt()
        this.type = `in`.readByte()
        this.spacing = `in`.readByte()
        this.options = `in`.readShort()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[LEGEND]\n")
        buffer.append("    .xAxisUpperLeft       = ")
            .append("0x").append(toHex(this.xAxisUpperLeft))
            .append(" (").append(this.xAxisUpperLeft).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .yAxisUpperLeft       = ")
            .append("0x").append(toHex(this.yAxisUpperLeft))
            .append(" (").append(this.yAxisUpperLeft).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .xSize                = ")
            .append("0x").append(toHex(this.xSize))
            .append(" (").append(this.xSize).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .ySize                = ")
            .append("0x").append(toHex(this.ySize))
            .append(" (").append(this.ySize).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .type                 = ")
            .append("0x").append(toHex(this.type))
            .append(" (").append(this.type.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .spacing              = ")
            .append("0x").append(toHex(this.spacing))
            .append(" (").append(this.spacing.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .options              = ")
            .append("0x").append(toHex(this.options))
            .append(" (").append(this.options.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("         .autoPosition             = ").append(this.isAutoPosition)
            .append('\n')
        buffer.append("         .autoSeries               = ").append(this.isAutoSeries)
            .append('\n')
        buffer.append("         .autoXPositioning         = ").append(this.isAutoXPositioning)
            .append('\n')
        buffer.append("         .autoYPositioning         = ").append(this.isAutoYPositioning)
            .append('\n')
        buffer.append("         .vertical                 = ").append(this.isVertical).append('\n')
        buffer.append("         .dataTable                = ").append(this.isDataTable).append('\n')

        buffer.append("[/LEGEND]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeInt(this.xAxisUpperLeft)
        out.writeInt(this.yAxisUpperLeft)
        out.writeInt(this.xSize)
        out.writeInt(this.ySize)
        out.writeByte(type.toInt())
        out.writeByte(spacing.toInt())
        out.writeShort(options.toInt())
    }

    override fun getDataSize(): Int {
        return 4 + 4 + 4 + 4 + 1 + 1 + 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = LegendRecord()

        rec.xAxisUpperLeft = this.xAxisUpperLeft
        rec.yAxisUpperLeft = this.yAxisUpperLeft
        rec.xSize = this.xSize
        rec.ySize = this.ySize
        rec.type = this.type
        rec.spacing = this.spacing
        rec.options = this.options
        return rec
    }


    var isAutoPosition: Boolean
        /**
         * automatic positioning (1=docked)
         * @return  the auto position field value.
         */
        get() = autoPosition.isSet(options.toInt())
        /**
         * Sets the auto position field value.
         * automatic positioning (1=docked)
         */
        set(value) {
            this.options = autoPosition.setShortBoolean(this.options, value)
        }

    var isAutoSeries: Boolean
        /**
         * excel 5 only (true)
         * @return  the auto series field value.
         */
        get() = autoSeries.isSet(options.toInt())
        /**
         * Sets the auto series field value.
         * excel 5 only (true)
         */
        set(value) {
            this.options = autoSeries.setShortBoolean(this.options, value)
        }

    var isAutoXPositioning: Boolean
        /**
         * position of legend on the x axis is automatic
         * @return  the auto x positioning field value.
         */
        get() = autoXPositioning.isSet(options.toInt())
        /**
         * Sets the auto x positioning field value.
         * position of legend on the x axis is automatic
         */
        set(value) {
            this.options =
                autoXPositioning.setShortBoolean(this.options, value)
        }

    var isAutoYPositioning: Boolean
        /**
         * position of legend on the y axis is automatic
         * @return  the auto y positioning field value.
         */
        get() = autoYPositioning.isSet(options.toInt())
        /**
         * Sets the auto y positioning field value.
         * position of legend on the y axis is automatic
         */
        set(value) {
            this.options =
                autoYPositioning.setShortBoolean(this.options, value)
        }

    var isVertical: Boolean
        /**
         * vertical or horizontal legend (1 or 0 respectively).  Always 0 if not automatic.
         * @return  the vertical field value.
         */
        get() = vertical.isSet(options.toInt())
        /**
         * Sets the vertical field value.
         * vertical or horizontal legend (1 or 0 respectively).  Always 0 if not automatic.
         */
        set(value) {
            this.options = vertical.setShortBoolean(this.options, value)
        }

    var isDataTable: Boolean
        /**
         * 1 if chart contains data table
         * @return  the data table field value.
         */
        get() = dataTable.isSet(options.toInt())
        /**
         * Sets the data table field value.
         * 1 if chart contains data table
         */
        set(value) {
            this.options = dataTable.setShortBoolean(this.options, value)
        }

    companion object {
        const val sid: Short = 0x1015

        private val autoPosition = getInstance(0x01)
        private val autoSeries = getInstance(0x02)
        private val autoXPositioning = getInstance(0x04)
        private val autoYPositioning = getInstance(0x08)
        private val vertical = getInstance(0x10)
        private val dataTable = getInstance(0x20)

        const val TYPE_BOTTOM: Byte = 0
        const val TYPE_CORNER: Byte = 1
        const val TYPE_TOP: Byte = 2
        const val TYPE_RIGHT: Byte = 3
        const val TYPE_LEFT: Byte = 4
        const val TYPE_UNDOCKED: Byte = 7
        const val SPACING_CLOSE: Byte = 0
        const val SPACING_MEDIUM: Byte = 1
        const val SPACING_OPEN: Byte = 2
    }
}
