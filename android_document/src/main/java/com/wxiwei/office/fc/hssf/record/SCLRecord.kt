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
package com.wxiwei.office.fc.hssf.record

import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Specifies the window's zoom magnification.  
 *
 *
 * If this record isn't present then the windows zoom is 100%. see p384 Excel Dev Kit
 * 
 * @author Andrew C. Oliver (acoliver at apache.org)
 */
class SCLRecord : StandardRecord {
    private var field_1_numerator: Short = 0
    private var field_2_denominator: Short = 0


    constructor()

    constructor(`in`: RecordInputStream) {
        field_1_numerator = `in`.readShort()
        field_2_denominator = `in`.readShort()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[SCL]\n")
        buffer.append("    .numerator            = ")
            .append("0x").append(toHex(getNumerator()))
            .append(" (").append(getNumerator().toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .denominator          = ")
            .append("0x").append(toHex(getDenominator()))
            .append(" (").append(getDenominator().toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))

        buffer.append("[/SCL]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(field_1_numerator.toInt())
        out.writeShort(field_2_denominator.toInt())
    }

    override fun getDataSize(): Int {
        return 2 + 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = SCLRecord()

        rec.field_1_numerator = field_1_numerator
        rec.field_2_denominator = field_2_denominator
        return rec
    }


    /**
     * Get the numerator field for the SCL record.
     */
    fun getNumerator(): Short {
        return field_1_numerator
    }

    /**
     * Set the numerator field for the SCL record.
     */
    fun setNumerator(field_1_numerator: Short) {
        this.field_1_numerator = field_1_numerator
    }

    /**
     * Get the denominator field for the SCL record.
     */
    fun getDenominator(): Short {
        return field_2_denominator
    }

    /**
     * Set the denominator field for the SCL record.
     */
    fun setDenominator(field_2_denominator: Short) {
        this.field_2_denominator = field_2_denominator
    }

    companion object {
        const val sid: Short = 0x00A0
    }
}
