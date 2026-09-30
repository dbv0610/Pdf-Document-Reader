/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
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
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * Describes a chart sheet properties record. SHTPROPS (0x1044) 
 *
 *
 * 
 * (As with all chart related records, documentation is lacking.
 * See [ChartRecord] for more details)
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class SheetPropertiesRecord : StandardRecord {
    /**
     * Get the flags field for the SheetProperties record.
     */
    var flags: Int = 0
        private set

    /**
     * Get the empty field for the SheetProperties record.
     * 
     * @return  One of
     * EMPTY_NOT_PLOTTED
     * EMPTY_ZERO
     * EMPTY_INTERPOLATED
     */
    var empty: Int = 0
        private set

    constructor()

    constructor(`in`: RecordInputStream) {
        this.flags = `in`.readUShort()
        this.empty = `in`.readUShort()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[SHTPROPS]\n")
        buffer.append("    .flags                = ").append(
            shortToHex(
                this.flags
            )
        ).append('\n')
        buffer.append("         .chartTypeManuallyFormatted= ")
            .append(this.isChartTypeManuallyFormatted).append('\n')
        buffer.append("         .plotVisibleOnly           = ").append(this.isPlotVisibleOnly)
            .append('\n')
        buffer.append("         .doNotSizeWithWindow       = ").append(this.isDoNotSizeWithWindow)
            .append('\n')
        buffer.append("         .defaultPlotDimensions     = ").append(this.isDefaultPlotDimensions)
            .append('\n')
        buffer.append("         .autoPlotArea              = ").append(this.isAutoPlotArea)
            .append('\n')
        buffer.append("    .empty                = ").append(
            shortToHex(
                this.empty
            )
        ).append('\n')

        buffer.append("[/SHTPROPS]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(this.flags)
        out.writeShort(this.empty)
    }

    override fun getDataSize(): Int {
        return 2 + 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = SheetPropertiesRecord()

        rec.flags = this.flags
        rec.empty = this.empty
        return rec
    }

    /**
     * Set the empty field for the SheetProperties record.
     * 
     * @param empty
     * One of
     * EMPTY_NOT_PLOTTED
     * EMPTY_ZERO
     * EMPTY_INTERPOLATED
     */
    fun setEmpty(empty: Byte) {
        this.empty = empty.toInt()
    }

    var isChartTypeManuallyFormatted: Boolean
        /**
         * Has the chart type been manually formatted?
         * @return  the chart type manually formatted field value.
         */
        get() = chartTypeManuallyFormatted.isSet(this.flags)
        /**
         * Sets the chart type manually formatted field value.
         * Has the chart type been manually formatted?
         */
        set(value) {
            this.flags = chartTypeManuallyFormatted.setBoolean(
                this.flags,
                value
            )
        }

    var isPlotVisibleOnly: Boolean
        /**
         * Only show visible cells on the chart.
         * @return  the plot visible only field value.
         */
        get() = plotVisibleOnly.isSet(this.flags)
        /**
         * Sets the plot visible only field value.
         * Only show visible cells on the chart.
         */
        set(value) {
            this.flags =
                plotVisibleOnly.setBoolean(this.flags, value)
        }

    var isDoNotSizeWithWindow: Boolean
        /**
         * Do not size the chart when the window changes size
         * @return  the do not size with window field value.
         */
        get() = doNotSizeWithWindow.isSet(this.flags)
        /**
         * Sets the do not size with window field value.
         * Do not size the chart when the window changes size
         */
        set(value) {
            this.flags =
                doNotSizeWithWindow.setBoolean(this.flags, value)
        }

    var isDefaultPlotDimensions: Boolean
        /**
         * Indicates that the default area dimensions should be used.
         * @return  the default plot dimensions field value.
         */
        get() = defaultPlotDimensions.isSet(this.flags)
        /**
         * Sets the default plot dimensions field value.
         * Indicates that the default area dimensions should be used.
         */
        set(value) {
            this.flags =
                defaultPlotDimensions.setBoolean(this.flags, value)
        }

    var isAutoPlotArea: Boolean
        /**
         * ??
         * @return  the auto plot area field value.
         */
        get() = autoPlotArea.isSet(this.flags)
        /**
         * Sets the auto plot area field value.
         * ??
         */
        set(value) {
            this.flags = autoPlotArea.setBoolean(this.flags, value)
        }

    companion object {
        const val sid: Short = 0x1044

        private val chartTypeManuallyFormatted = getInstance(0x01)
        private val plotVisibleOnly = getInstance(0x02)
        private val doNotSizeWithWindow = getInstance(0x04)
        private val defaultPlotDimensions = getInstance(0x08)
        private val autoPlotArea = getInstance(0x10)

        const val EMPTY_NOT_PLOTTED: Byte = 0
        const val EMPTY_ZERO: Byte = 1
        const val EMPTY_INTERPOLATED: Byte = 2
    }
}
