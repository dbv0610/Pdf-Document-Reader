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
 * The frame record indicates whether there is a border around the displayed text of a chart.
 *
 *
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class FrameRecord : StandardRecord {
    /**
     * Get the border type field for the Frame record.
     * 
     * @return  One of
     * BORDER_TYPE_REGULAR
     * BORDER_TYPE_SHADOW
     */
    /**
     * Set the border type field for the Frame record.
     * 
     * @param field_1_borderType
     * One of
     * BORDER_TYPE_REGULAR
     * BORDER_TYPE_SHADOW
     */
    var borderType: Short = 0
    /**
     * Get the options field for the Frame record.
     */
    /**
     * Set the options field for the Frame record.
     */
    var options: Short = 0


    constructor()

    constructor(`in`: RecordInputStream) {
        this.borderType = `in`.readShort()
        this.options = `in`.readShort()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[FRAME]\n")
        buffer.append("    .borderType           = ")
            .append("0x").append(toHex(this.borderType))
            .append(" (").append(this.borderType.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .options              = ")
            .append("0x").append(toHex(this.options))
            .append(" (").append(this.options.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("         .autoSize                 = ").append(this.isAutoSize).append('\n')
        buffer.append("         .autoPosition             = ").append(this.isAutoPosition)
            .append('\n')

        buffer.append("[/FRAME]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(borderType.toInt())
        out.writeShort(options.toInt())
    }

    override fun getDataSize(): Int {
        return 2 + 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = FrameRecord()

        rec.borderType = this.borderType
        rec.options = this.options
        return rec
    }


    var isAutoSize: Boolean
        /**
         * excel calculates the size automatically if true
         * @return  the auto size field value.
         */
        get() = autoSize.isSet(options.toInt())
        /**
         * Sets the auto size field value.
         * excel calculates the size automatically if true
         */
        set(value) {
            this.options = autoSize.setShortBoolean(this.options, value)
        }

    var isAutoPosition: Boolean
        /**
         * excel calculates the position automatically
         * @return  the auto position field value.
         */
        get() = autoPosition.isSet(options.toInt())
        /**
         * Sets the auto position field value.
         * excel calculates the position automatically
         */
        set(value) {
            this.options = autoPosition.setShortBoolean(this.options, value)
        }

    companion object {
        const val sid: Short = 0x1032

        private val autoSize = getInstance(0x1)
        private val autoPosition = getInstance(0x2)

        const val BORDER_TYPE_REGULAR: Short = 0
        const val BORDER_TYPE_SHADOW: Short = 1
    }
}
