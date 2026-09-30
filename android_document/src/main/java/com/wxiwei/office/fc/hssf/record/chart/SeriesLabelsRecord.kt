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
import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * The series label record defines the type of label associated with the data format record.
 *
 *
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class SeriesLabelsRecord : StandardRecord {
    /**
     * Get the format flags field for the SeriesLabels record.
     */
    /**
     * Set the format flags field for the SeriesLabels record.
     */
    var formatFlags: Short = 0

    constructor()

    constructor(`in`: RecordInputStream) {
        this.formatFlags = `in`.readShort()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[ATTACHEDLABEL]\n")
        buffer.append("    .formatFlags          = ")
            .append("0x").append(toHex(this.formatFlags))
            .append(" (").append(this.formatFlags.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("         .showActual               = ").append(this.isShowActual)
            .append('\n')
        buffer.append("         .showPercent              = ").append(this.isShowPercent)
            .append('\n')
        buffer.append("         .labelAsPercentage        = ").append(this.isLabelAsPercentage)
            .append('\n')
        buffer.append("         .smoothedLine             = ").append(this.isSmoothedLine)
            .append('\n')
        buffer.append("         .showLabel                = ").append(this.isShowLabel).append('\n')
        buffer.append("         .showBubbleSizes          = ").append(this.isShowBubbleSizes)
            .append('\n')

        buffer.append("[/ATTACHEDLABEL]\n")
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
        val rec = SeriesLabelsRecord()

        rec.formatFlags = this.formatFlags
        return rec
    }


    var isShowActual: Boolean
        /**
         * show actual value of the data point
         * @return  the show actual field value.
         */
        get() = showActual.isSet(formatFlags.toInt())
        /**
         * Sets the show actual field value.
         * show actual value of the data point
         */
        set(value) {
            this.formatFlags =
                showActual.setShortBoolean(this.formatFlags, value)
        }

    var isShowPercent: Boolean
        /**
         * show value as percentage of total (pie charts only)
         * @return  the show percent field value.
         */
        get() = showPercent.isSet(formatFlags.toInt())
        /**
         * Sets the show percent field value.
         * show value as percentage of total (pie charts only)
         */
        set(value) {
            this.formatFlags =
                showPercent.setShortBoolean(this.formatFlags, value)
        }

    var isLabelAsPercentage: Boolean
        /**
         * show category label/value as percentage (pie charts only)
         * @return  the label as percentage field value.
         */
        get() = labelAsPercentage.isSet(formatFlags.toInt())
        /**
         * Sets the label as percentage field value.
         * show category label/value as percentage (pie charts only)
         */
        set(value) {
            this.formatFlags = labelAsPercentage.setShortBoolean(
                this.formatFlags,
                value
            )
        }

    var isSmoothedLine: Boolean
        /**
         * show smooth line
         * @return  the smoothed line field value.
         */
        get() = smoothedLine.isSet(formatFlags.toInt())
        /**
         * Sets the smoothed line field value.
         * show smooth line
         */
        set(value) {
            this.formatFlags =
                smoothedLine.setShortBoolean(this.formatFlags, value)
        }

    var isShowLabel: Boolean
        /**
         * display category label
         * @return  the show label field value.
         */
        get() = showLabel.isSet(formatFlags.toInt())
        /**
         * Sets the show label field value.
         * display category label
         */
        set(value) {
            this.formatFlags =
                showLabel.setShortBoolean(this.formatFlags, value)
        }

    var isShowBubbleSizes: Boolean
        /**
         * ??
         * @return  the show bubble sizes field value.
         */
        get() = showBubbleSizes.isSet(formatFlags.toInt())
        /**
         * Sets the show bubble sizes field value.
         * ??
         */
        set(value) {
            this.formatFlags = showBubbleSizes.setShortBoolean(
                this.formatFlags,
                value
            )
        }

    companion object {
        const val sid: Short = 0x100c

        private val showActual = getInstance(0x01)
        private val showPercent = getInstance(0x02)
        private val labelAsPercentage = getInstance(0x04)
        private val smoothedLine = getInstance(0x08)
        private val showLabel = getInstance(0x10)
        private val showBubbleSizes = getInstance(0x20)
    }
}
