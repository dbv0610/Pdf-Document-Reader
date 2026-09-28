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
 * The data format record is used to index into a series.
 *
 *
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class DataFormatRecord : StandardRecord {
    /**
     * Get the point number field for the DataFormat record.
     */
    /**
     * Set the point number field for the DataFormat record.
     */
    var pointNumber: Short = 0
    /**
     * Get the series index field for the DataFormat record.
     */
    /**
     * Set the series index field for the DataFormat record.
     */
    var seriesIndex: Short = 0
    /**
     * Get the series number field for the DataFormat record.
     */
    /**
     * Set the series number field for the DataFormat record.
     */
    var seriesNumber: Short = 0
    /**
     * Get the format flags field for the DataFormat record.
     */
    /**
     * Set the format flags field for the DataFormat record.
     */
    var formatFlags: Short = 0


    constructor()

    constructor(`in`: RecordInputStream) {
        this.pointNumber = `in`.readShort()
        this.seriesIndex = `in`.readShort()
        this.seriesNumber = `in`.readShort()
        this.formatFlags = `in`.readShort()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[DATAFORMAT]\n")
        buffer.append("    .pointNumber          = ")
            .append("0x").append(toHex(this.pointNumber))
            .append(" (").append(this.pointNumber.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .seriesIndex          = ")
            .append("0x").append(toHex(this.seriesIndex))
            .append(" (").append(this.seriesIndex.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .seriesNumber         = ")
            .append("0x").append(toHex(this.seriesNumber))
            .append(" (").append(this.seriesNumber.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .formatFlags          = ")
            .append("0x").append(toHex(this.formatFlags))
            .append(" (").append(this.formatFlags.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("         .useExcel4Colors          = ").append(this.isUseExcel4Colors)
            .append('\n')

        buffer.append("[/DATAFORMAT]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(pointNumber.toInt())
        out.writeShort(seriesIndex.toInt())
        out.writeShort(seriesNumber.toInt())
        out.writeShort(formatFlags.toInt())
    }

    override fun getDataSize(): Int {
        return 2 + 2 + 2 + 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = DataFormatRecord()

        rec.pointNumber = this.pointNumber
        rec.seriesIndex = this.seriesIndex
        rec.seriesNumber = this.seriesNumber
        rec.formatFlags = this.formatFlags
        return rec
    }


    var isUseExcel4Colors: Boolean
        /**
         * set true to use excel 4 colors.
         * @return  the use excel 4 colors field value.
         */
        get() = useExcel4Colors.isSet(formatFlags.toInt())
        /**
         * Sets the use excel 4 colors field value.
         * set true to use excel 4 colors.
         */
        set(value) {
            this.formatFlags =
                useExcel4Colors.setShortBoolean(this.formatFlags, value)
        }

    companion object {
        const val sid: Short = 0x1006

        private val useExcel4Colors = getInstance(0x1)
    }
}
