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
 * This record refers to a category or series axis and is used to specify label/tickmark frequency.
 *
 *
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class CategorySeriesAxisRecord : StandardRecord {
    /**
     * Get the crossing point field for the CategorySeriesAxis record.
     */
    /**
     * Set the crossing point field for the CategorySeriesAxis record.
     */
    var crossingPoint: Short = 0
    /**
     * Get the label frequency field for the CategorySeriesAxis record.
     */
    /**
     * Set the label frequency field for the CategorySeriesAxis record.
     */
    var labelFrequency: Short = 0
    /**
     * Get the tick mark frequency field for the CategorySeriesAxis record.
     */
    /**
     * Set the tick mark frequency field for the CategorySeriesAxis record.
     */
    var tickMarkFrequency: Short = 0
    /**
     * Get the options field for the CategorySeriesAxis record.
     */
    /**
     * Set the options field for the CategorySeriesAxis record.
     */
    var options: Short = 0


    constructor()

    constructor(`in`: RecordInputStream) {
        this.crossingPoint = `in`.readShort()
        this.labelFrequency = `in`.readShort()
        this.tickMarkFrequency = `in`.readShort()
        this.options = `in`.readShort()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[CATSERRANGE]\n")
        buffer.append("    .crossingPoint        = ")
            .append("0x").append(toHex(this.crossingPoint))
            .append(" (").append(this.crossingPoint.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .labelFrequency       = ")
            .append("0x").append(toHex(this.labelFrequency))
            .append(" (").append(this.labelFrequency.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .tickMarkFrequency    = ")
            .append("0x").append(toHex(this.tickMarkFrequency))
            .append(" (").append(this.tickMarkFrequency.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .options              = ")
            .append("0x").append(toHex(this.options))
            .append(" (").append(this.options.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("         .valueAxisCrossing        = ").append(this.isValueAxisCrossing)
            .append('\n')
        buffer.append("         .crossesFarRight          = ").append(this.isCrossesFarRight)
            .append('\n')
        buffer.append("         .reversed                 = ").append(this.isReversed).append('\n')

        buffer.append("[/CATSERRANGE]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(crossingPoint.toInt())
        out.writeShort(labelFrequency.toInt())
        out.writeShort(tickMarkFrequency.toInt())
        out.writeShort(options.toInt())
    }

    override fun getDataSize(): Int {
        return 2 + 2 + 2 + 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = CategorySeriesAxisRecord()

        rec.crossingPoint = this.crossingPoint
        rec.labelFrequency = this.labelFrequency
        rec.tickMarkFrequency = this.tickMarkFrequency
        rec.options = this.options
        return rec
    }


    var isValueAxisCrossing: Boolean
        /**
         * set true to indicate axis crosses between categories and false to cross axis midway
         * @return  the value axis crossing field value.
         */
        get() = valueAxisCrossing.isSet(options.toInt())
        /**
         * Sets the value axis crossing field value.
         * set true to indicate axis crosses between categories and false to cross axis midway
         */
        set(value) {
            this.options = valueAxisCrossing.setShortBoolean(
                this.options,
                value
            )
        }

    var isCrossesFarRight: Boolean
        /**
         * axis crosses at the far right
         * @return  the crosses far right field value.
         */
        get() = crossesFarRight.isSet(options.toInt())
        /**
         * Sets the crosses far right field value.
         * axis crosses at the far right
         */
        set(value) {
            this.options = crossesFarRight.setShortBoolean(
                this.options,
                value
            )
        }

    var isReversed: Boolean
        /**
         * categories are displayed in reverse order
         * @return  the reversed field value.
         */
        get() = reversed.isSet(options.toInt())
        /**
         * Sets the reversed field value.
         * categories are displayed in reverse order
         */
        set(value) {
            this.options =
                reversed.setShortBoolean(this.options, value)
        }

    companion object {
        const val sid: Short = 0x1020

        private val valueAxisCrossing = getInstance(0x1)
        private val crossesFarRight = getInstance(0x2)
        private val reversed = getInstance(0x4)
    }
}
