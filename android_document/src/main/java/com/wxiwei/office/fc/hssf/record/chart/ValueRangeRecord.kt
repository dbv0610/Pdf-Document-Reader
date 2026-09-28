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
 * The value range record defines the range of the value axis.
 *
 *
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class ValueRangeRecord : StandardRecord {
    /**
     * Get the minimum axis value field for the ValueRange record.
     */
    /**
     * Set the minimum axis value field for the ValueRange record.
     */
    var minimumAxisValue: Double = 0.0
    /**
     * Get the maximum axis value field for the ValueRange record.
     */
    /**
     * Set the maximum axis value field for the ValueRange record.
     */
    var maximumAxisValue: Double = 0.0
    /**
     * Get the major increment field for the ValueRange record.
     */
    /**
     * Set the major increment field for the ValueRange record.
     */
    var majorIncrement: Double = 0.0
    /**
     * Get the minor increment field for the ValueRange record.
     */
    /**
     * Set the minor increment field for the ValueRange record.
     */
    var minorIncrement: Double = 0.0
    /**
     * Get the category axis cross field for the ValueRange record.
     */
    /**
     * Set the category axis cross field for the ValueRange record.
     */
    var categoryAxisCross: Double = 0.0
    /**
     * Get the options field for the ValueRange record.
     */
    /**
     * Set the options field for the ValueRange record.
     */
    var options: Short = 0


    constructor()

    constructor(`in`: RecordInputStream) {
        this.minimumAxisValue = `in`.readDouble()
        this.maximumAxisValue = `in`.readDouble()
        this.majorIncrement = `in`.readDouble()
        this.minorIncrement = `in`.readDouble()
        this.categoryAxisCross = `in`.readDouble()
        this.options = `in`.readShort()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[VALUERANGE]\n")
        buffer.append("    .minimumAxisValue     = ")
            .append(" (").append(this.minimumAxisValue).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .maximumAxisValue     = ")
            .append(" (").append(this.maximumAxisValue).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .majorIncrement       = ")
            .append(" (").append(this.majorIncrement).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .minorIncrement       = ")
            .append(" (").append(this.minorIncrement).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .categoryAxisCross    = ")
            .append(" (").append(this.categoryAxisCross).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .options              = ")
            .append("0x").append(toHex(this.options))
            .append(" (").append(this.options.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("         .automaticMinimum         = ").append(this.isAutomaticMinimum)
            .append('\n')
        buffer.append("         .automaticMaximum         = ").append(this.isAutomaticMaximum)
            .append('\n')
        buffer.append("         .automaticMajor           = ").append(this.isAutomaticMajor)
            .append('\n')
        buffer.append("         .automaticMinor           = ").append(this.isAutomaticMinor)
            .append('\n')
        buffer.append("         .automaticCategoryCrossing     = ")
            .append(this.isAutomaticCategoryCrossing).append('\n')
        buffer.append("         .logarithmicScale         = ").append(this.isLogarithmicScale)
            .append('\n')
        buffer.append("         .valuesInReverse          = ").append(this.isValuesInReverse)
            .append('\n')
        buffer.append("         .crossCategoryAxisAtMaximum     = ")
            .append(this.isCrossCategoryAxisAtMaximum).append('\n')
        buffer.append("         .reserved                 = ").append(this.isReserved).append('\n')

        buffer.append("[/VALUERANGE]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeDouble(this.minimumAxisValue)
        out.writeDouble(this.maximumAxisValue)
        out.writeDouble(this.majorIncrement)
        out.writeDouble(this.minorIncrement)
        out.writeDouble(this.categoryAxisCross)
        out.writeShort(options.toInt())
    }

    override fun getDataSize(): Int {
        return 8 + 8 + 8 + 8 + 8 + 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = ValueRangeRecord()

        rec.minimumAxisValue = this.minimumAxisValue
        rec.maximumAxisValue = this.maximumAxisValue
        rec.majorIncrement = this.majorIncrement
        rec.minorIncrement = this.minorIncrement
        rec.categoryAxisCross = this.categoryAxisCross
        rec.options = this.options
        return rec
    }


    var isAutomaticMinimum: Boolean
        /**
         * automatic minimum value selected
         * @return  the automatic minimum field value.
         */
        get() = automaticMinimum.isSet(options.toInt())
        /**
         * Sets the automatic minimum field value.
         * automatic minimum value selected
         */
        set(value) {
            this.options =
                automaticMinimum.setShortBoolean(this.options, value)
        }

    var isAutomaticMaximum: Boolean
        /**
         * automatic maximum value selected
         * @return  the automatic maximum field value.
         */
        get() = automaticMaximum.isSet(options.toInt())
        /**
         * Sets the automatic maximum field value.
         * automatic maximum value selected
         */
        set(value) {
            this.options =
                automaticMaximum.setShortBoolean(this.options, value)
        }

    var isAutomaticMajor: Boolean
        /**
         * automatic major unit selected
         * @return  the automatic major field value.
         */
        get() = automaticMajor.isSet(options.toInt())
        /**
         * Sets the automatic major field value.
         * automatic major unit selected
         */
        set(value) {
            this.options =
                automaticMajor.setShortBoolean(this.options, value)
        }

    var isAutomaticMinor: Boolean
        /**
         * automatic minor unit selected
         * @return  the automatic minor field value.
         */
        get() = automaticMinor.isSet(options.toInt())
        /**
         * Sets the automatic minor field value.
         * automatic minor unit selected
         */
        set(value) {
            this.options =
                automaticMinor.setShortBoolean(this.options, value)
        }

    var isAutomaticCategoryCrossing: Boolean
        /**
         * category crossing point is automatically selected
         * @return  the automatic category crossing field value.
         */
        get() = automaticCategoryCrossing.isSet(options.toInt())
        /**
         * Sets the automatic category crossing field value.
         * category crossing point is automatically selected
         */
        set(value) {
            this.options = automaticCategoryCrossing.setShortBoolean(
                this.options,
                value
            )
        }

    var isLogarithmicScale: Boolean
        /**
         * use logarithmic scale
         * @return  the logarithmic scale field value.
         */
        get() = logarithmicScale.isSet(options.toInt())
        /**
         * Sets the logarithmic scale field value.
         * use logarithmic scale
         */
        set(value) {
            this.options =
                logarithmicScale.setShortBoolean(this.options, value)
        }

    var isValuesInReverse: Boolean
        /**
         * values are reverses in graph
         * @return  the values in reverse field value.
         */
        get() = valuesInReverse.isSet(options.toInt())
        /**
         * Sets the values in reverse field value.
         * values are reverses in graph
         */
        set(value) {
            this.options =
                valuesInReverse.setShortBoolean(this.options, value)
        }

    var isCrossCategoryAxisAtMaximum: Boolean
        /**
         * category axis to cross at maximum value
         * @return  the cross category axis at maximum field value.
         */
        get() = crossCategoryAxisAtMaximum.isSet(options.toInt())
        /**
         * Sets the cross category axis at maximum field value.
         * category axis to cross at maximum value
         */
        set(value) {
            this.options = crossCategoryAxisAtMaximum.setShortBoolean(
                this.options,
                value
            )
        }

    var isReserved: Boolean
        /**
         * reserved, must equal 1 (excel dev. guide says otherwise)
         * @return  the reserved field value.
         */
        get() = reserved.isSet(options.toInt())
        /**
         * Sets the reserved field value.
         * reserved, must equal 1 (excel dev. guide says otherwise)
         */
        set(value) {
            this.options = reserved.setShortBoolean(this.options, value)
        }

    companion object {
        const val sid: Short = 0x101f

        private val automaticMinimum = getInstance(0x0001)
        private val automaticMaximum = getInstance(0x0002)
        private val automaticMajor = getInstance(0x0004)
        private val automaticMinor = getInstance(0x0008)
        private val automaticCategoryCrossing = getInstance(0x0010)
        private val logarithmicScale = getInstance(0x0020)
        private val valuesInReverse = getInstance(0x0040)
        private val crossCategoryAxisAtMaximum = getInstance(0x0080)
        private val reserved = getInstance(0x0100)
    }
}
