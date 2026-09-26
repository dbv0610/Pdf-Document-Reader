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
 * The axis options record provides unit information and other various tidbits about the axis.
 *
 *
 * 
 * @author Andrew C. Oliver(acoliver at apache.org)
 */
class AxisOptionsRecord : StandardRecord {
    /**
     * Get the minimum category field for the AxisOptions record.
     */
    /**
     * Set the minimum category field for the AxisOptions record.
     */
    var minimumCategory: Short = 0
    /**
     * Get the maximum category field for the AxisOptions record.
     */
    /**
     * Set the maximum category field for the AxisOptions record.
     */
    var maximumCategory: Short = 0
    /**
     * Get the major unit value field for the AxisOptions record.
     */
    /**
     * Set the major unit value field for the AxisOptions record.
     */
    var majorUnitValue: Short = 0
    /**
     * Get the major unit field for the AxisOptions record.
     */
    /**
     * Set the major unit field for the AxisOptions record.
     */
    var majorUnit: Short = 0
    /**
     * Get the minor unit value field for the AxisOptions record.
     */
    /**
     * Set the minor unit value field for the AxisOptions record.
     */
    var minorUnitValue: Short = 0
    /**
     * Get the minor unit field for the AxisOptions record.
     */
    /**
     * Set the minor unit field for the AxisOptions record.
     */
    var minorUnit: Short = 0
    /**
     * Get the base unit field for the AxisOptions record.
     */
    /**
     * Set the base unit field for the AxisOptions record.
     */
    var baseUnit: Short = 0
    /**
     * Get the crossing point field for the AxisOptions record.
     */
    /**
     * Set the crossing point field for the AxisOptions record.
     */
    var crossingPoint: Short = 0
    /**
     * Get the options field for the AxisOptions record.
     */
    /**
     * Set the options field for the AxisOptions record.
     */
    var options: Short = 0


    constructor()

    constructor(`in`: RecordInputStream) {
        this.minimumCategory = `in`.readShort()
        this.maximumCategory = `in`.readShort()
        this.majorUnitValue = `in`.readShort()
        this.majorUnit = `in`.readShort()
        this.minorUnitValue = `in`.readShort()
        this.minorUnit = `in`.readShort()
        this.baseUnit = `in`.readShort()
        this.crossingPoint = `in`.readShort()
        this.options = `in`.readShort()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[AXCEXT]\n")
        buffer.append("    .minimumCategory      = ")
            .append("0x").append(toHex(this.minimumCategory))
            .append(" (").append(this.minimumCategory.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .maximumCategory      = ")
            .append("0x").append(toHex(this.maximumCategory))
            .append(" (").append(this.maximumCategory.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .majorUnitValue       = ")
            .append("0x").append(toHex(this.majorUnitValue))
            .append(" (").append(this.majorUnitValue.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .majorUnit            = ")
            .append("0x").append(toHex(this.majorUnit))
            .append(" (").append(this.majorUnit.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .minorUnitValue       = ")
            .append("0x").append(toHex(this.minorUnitValue))
            .append(" (").append(this.minorUnitValue.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .minorUnit            = ")
            .append("0x").append(toHex(this.minorUnit))
            .append(" (").append(this.minorUnit.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .baseUnit             = ")
            .append("0x").append(toHex(this.baseUnit))
            .append(" (").append(this.baseUnit.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .crossingPoint        = ")
            .append("0x").append(toHex(this.crossingPoint))
            .append(" (").append(this.crossingPoint.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .options              = ")
            .append("0x").append(toHex(this.options))
            .append(" (").append(this.options.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("         .defaultMinimum           = ").append(this.isDefaultMinimum)
            .append('\n')
        buffer.append("         .defaultMaximum           = ").append(this.isDefaultMaximum)
            .append('\n')
        buffer.append("         .defaultMajor             = ").append(this.isDefaultMajor)
            .append('\n')
        buffer.append("         .defaultMinorUnit         = ").append(this.isDefaultMinorUnit)
            .append('\n')
        buffer.append("         .isDate                   = ").append(this.isIsDate).append('\n')
        buffer.append("         .defaultBase              = ").append(this.isDefaultBase)
            .append('\n')
        buffer.append("         .defaultCross             = ").append(this.isDefaultCross)
            .append('\n')
        buffer.append("         .defaultDateSettings      = ").append(this.isDefaultDateSettings)
            .append('\n')

        buffer.append("[/AXCEXT]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(minimumCategory.toInt())
        out.writeShort(maximumCategory.toInt())
        out.writeShort(majorUnitValue.toInt())
        out.writeShort(majorUnit.toInt())
        out.writeShort(minorUnitValue.toInt())
        out.writeShort(minorUnit.toInt())
        out.writeShort(baseUnit.toInt())
        out.writeShort(crossingPoint.toInt())
        out.writeShort(options.toInt())
    }

    override fun getDataSize(): Int {
        return 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = AxisOptionsRecord()

        rec.minimumCategory = this.minimumCategory
        rec.maximumCategory = this.maximumCategory
        rec.majorUnitValue = this.majorUnitValue
        rec.majorUnit = this.majorUnit
        rec.minorUnitValue = this.minorUnitValue
        rec.minorUnit = this.minorUnit
        rec.baseUnit = this.baseUnit
        rec.crossingPoint = this.crossingPoint
        rec.options = this.options
        return rec
    }


    var isDefaultMinimum: Boolean
        /**
         * use the default minimum category
         * @return  the default minimum field value.
         */
        get() = defaultMinimum.isSet(options.toInt())
        /**
         * Sets the default minimum field value.
         * use the default minimum category
         */
        set(value) {
            this.options =
                defaultMinimum.setShortBoolean(this.options, value)
        }

    var isDefaultMaximum: Boolean
        /**
         * use the default maximum category
         * @return  the default maximum field value.
         */
        get() = defaultMaximum.isSet(options.toInt())
        /**
         * Sets the default maximum field value.
         * use the default maximum category
         */
        set(value) {
            this.options =
                defaultMaximum.setShortBoolean(this.options, value)
        }

    var isDefaultMajor: Boolean
        /**
         * use the default major unit
         * @return  the default major field value.
         */
        get() = defaultMajor.isSet(options.toInt())
        /**
         * Sets the default major field value.
         * use the default major unit
         */
        set(value) {
            this.options =
                defaultMajor.setShortBoolean(this.options, value)
        }

    var isDefaultMinorUnit: Boolean
        /**
         * use the default minor unit
         * @return  the default minor unit field value.
         */
        get() = defaultMinorUnit.isSet(options.toInt())
        /**
         * Sets the default minor unit field value.
         * use the default minor unit
         */
        set(value) {
            this.options =
                defaultMinorUnit.setShortBoolean(this.options, value)
        }

    /**
     * Sets the isDate field value.
     * this is a date axis
     */
    fun setIsDate(value: Boolean) {
        this.options = isDate.setShortBoolean(this.options, value)
    }

    val isIsDate: Boolean
        /**
         * this is a date axis
         * @return  the isDate field value.
         */
        get() = isDate.isSet(options.toInt())

    var isDefaultBase: Boolean
        /**
         * use the default base unit
         * @return  the default base field value.
         */
        get() = defaultBase.isSet(options.toInt())
        /**
         * Sets the default base field value.
         * use the default base unit
         */
        set(value) {
            this.options =
                defaultBase.setShortBoolean(this.options, value)
        }

    var isDefaultCross: Boolean
        /**
         * use the default crossing point
         * @return  the default cross field value.
         */
        get() = defaultCross.isSet(options.toInt())
        /**
         * Sets the default cross field value.
         * use the default crossing point
         */
        set(value) {
            this.options =
                defaultCross.setShortBoolean(this.options, value)
        }

    var isDefaultDateSettings: Boolean
        /**
         * use default date setttings for this axis
         * @return  the default date settings field value.
         */
        get() = defaultDateSettings.isSet(options.toInt())
        /**
         * Sets the default date settings field value.
         * use default date setttings for this axis
         */
        set(value) {
            this.options = defaultDateSettings.setShortBoolean(
                this.options,
                value
            )
        }

    companion object {
        const val sid: Short = 0x1062

        private val defaultMinimum = getInstance(0x01)
        private val defaultMaximum = getInstance(0x02)
        private val defaultMajor = getInstance(0x04)
        private val defaultMinorUnit = getInstance(0x08)
        private val isDate = getInstance(0x10)
        private val defaultBase = getInstance(0x20)
        private val defaultCross = getInstance(0x40)
        private val defaultDateSettings = getInstance(0x80)
    }
}
