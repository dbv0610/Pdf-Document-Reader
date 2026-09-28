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
 * The area format record is used to define the colours and patterns for an area.
 *
 *
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class AreaFormatRecord : StandardRecord {
    /**
     * Get the foreground color field for the AreaFormat record.
     */
    /**
     * Set the foreground color field for the AreaFormat record.
     */
    var foregroundColor: Int = 0
    /**
     * Get the background color field for the AreaFormat record.
     */
    /**
     * Set the background color field for the AreaFormat record.
     */
    var backgroundColor: Int = 0
    /**
     * Get the pattern field for the AreaFormat record.
     */
    /**
     * Set the pattern field for the AreaFormat record.
     */
    var pattern: Short = 0
    /**
     * Get the format flags field for the AreaFormat record.
     */
    /**
     * Set the format flags field for the AreaFormat record.
     */
    var formatFlags: Short = 0
    /**
     * Get the forecolor index field for the AreaFormat record.
     */
    /**
     * Set the forecolor index field for the AreaFormat record.
     */
    var forecolorIndex: Short = 0
    /**
     * Get the backcolor index field for the AreaFormat record.
     */
    /**
     * Set the backcolor index field for the AreaFormat record.
     */
    var backcolorIndex: Short = 0


    constructor()

    constructor(`in`: RecordInputStream) {
        this.foregroundColor = `in`.readInt()
        this.backgroundColor = `in`.readInt()
        this.pattern = `in`.readShort()
        this.formatFlags = `in`.readShort()
        this.forecolorIndex = `in`.readShort()
        this.backcolorIndex = `in`.readShort()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[AREAFORMAT]\n")
        buffer.append("    .foregroundColor      = ")
            .append("0x").append(toHex(this.foregroundColor))
            .append(" (").append(this.foregroundColor).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .backgroundColor      = ")
            .append("0x").append(toHex(this.backgroundColor))
            .append(" (").append(this.backgroundColor).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .pattern              = ")
            .append("0x").append(toHex(this.pattern))
            .append(" (").append(this.pattern.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .formatFlags          = ")
            .append("0x").append(toHex(this.formatFlags))
            .append(" (").append(this.formatFlags.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("         .automatic                = ").append(this.isAutomatic).append('\n')
        buffer.append("         .invert                   = ").append(this.isInvert).append('\n')
        buffer.append("    .forecolorIndex       = ")
            .append("0x").append(toHex(this.forecolorIndex))
            .append(" (").append(this.forecolorIndex.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .backcolorIndex       = ")
            .append("0x").append(toHex(this.backcolorIndex))
            .append(" (").append(this.backcolorIndex.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))

        buffer.append("[/AREAFORMAT]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeInt(this.foregroundColor)
        out.writeInt(this.backgroundColor)
        out.writeShort(pattern.toInt())
        out.writeShort(formatFlags.toInt())
        out.writeShort(forecolorIndex.toInt())
        out.writeShort(backcolorIndex.toInt())
    }

    override fun getDataSize(): Int {
        return 4 + 4 + 2 + 2 + 2 + 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = AreaFormatRecord()

        rec.foregroundColor = this.foregroundColor
        rec.backgroundColor = this.backgroundColor
        rec.pattern = this.pattern
        rec.formatFlags = this.formatFlags
        rec.forecolorIndex = this.forecolorIndex
        rec.backcolorIndex = this.backcolorIndex
        return rec
    }


    var isAutomatic: Boolean
        /**
         * automatic formatting
         * @return  the automatic field value.
         */
        get() = automatic.isSet(formatFlags.toInt())
        /**
         * Sets the automatic field value.
         * automatic formatting
         */
        set(value) {
            this.formatFlags =
                automatic.setShortBoolean(this.formatFlags, value)
        }

    var isInvert: Boolean
        /**
         * swap foreground and background colours when data is negative
         * @return  the invert field value.
         */
        get() = invert.isSet(formatFlags.toInt())
        /**
         * Sets the invert field value.
         * swap foreground and background colours when data is negative
         */
        set(value) {
            this.formatFlags =
                invert.setShortBoolean(this.formatFlags, value)
        }

    companion object {
        const val sid: Short = 0x100A

        private val automatic = getInstance(0x1)
        private val invert = getInstance(0x2)
    }
}
