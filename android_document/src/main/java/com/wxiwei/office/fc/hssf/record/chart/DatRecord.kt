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
 * The dat record is used to store options for the chart.
 *
 *
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class DatRecord : StandardRecord {
    /**
     * Get the options field for the Dat record.
     */
    /**
     * Set the options field for the Dat record.
     */
    var options: Short = 0


    constructor()

    constructor(`in`: RecordInputStream) {
        this.options = `in`.readShort()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[DAT]\n")
        buffer.append("    .options              = ")
            .append("0x").append(toHex(this.options))
            .append(" (").append(this.options.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("         .horizontalBorder         = ").append(this.isHorizontalBorder)
            .append('\n')
        buffer.append("         .verticalBorder           = ").append(this.isVerticalBorder)
            .append('\n')
        buffer.append("         .border                   = ").append(this.isBorder).append('\n')
        buffer.append("         .showSeriesKey            = ").append(this.isShowSeriesKey)
            .append('\n')

        buffer.append("[/DAT]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(options.toInt())
    }

    override fun getDataSize(): Int {
        return 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = DatRecord()

        rec.options = this.options
        return rec
    }


    var isHorizontalBorder: Boolean
        /**
         * has a horizontal border
         * @return  the horizontal border field value.
         */
        get() = horizontalBorder.isSet(options.toInt())
        /**
         * Sets the horizontal border field value.
         * has a horizontal border
         */
        set(value) {
            this.options =
                horizontalBorder.setShortBoolean(this.options, value)
        }

    var isVerticalBorder: Boolean
        /**
         * has vertical border
         * @return  the vertical border field value.
         */
        get() = verticalBorder.isSet(options.toInt())
        /**
         * Sets the vertical border field value.
         * has vertical border
         */
        set(value) {
            this.options = verticalBorder.setShortBoolean(this.options, value)
        }

    var isBorder: Boolean
        /**
         * data table has a border
         * @return  the border field value.
         */
        get() = border.isSet(options.toInt())
        /**
         * Sets the border field value.
         * data table has a border
         */
        set(value) {
            this.options = border.setShortBoolean(this.options, value)
        }

    var isShowSeriesKey: Boolean
        /**
         * shows the series key
         * @return  the show series key field value.
         */
        get() = showSeriesKey.isSet(options.toInt())
        /**
         * Sets the show series key field value.
         * shows the series key
         */
        set(value) {
            this.options = showSeriesKey.setShortBoolean(this.options, value)
        }

    companion object {
        const val sid: Short = 0x1063

        private val horizontalBorder = getInstance(0x1)
        private val verticalBorder = getInstance(0x2)
        private val border = getInstance(0x4)
        private val showSeriesKey = getInstance(0x8)
    }
}
