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
import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * The font basis record stores various font metrics.
 *
 *
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class FontBasisRecord : StandardRecord {
    /**
     * Get the x Basis field for the FontBasis record.
     */
    /**
     * Set the x Basis field for the FontBasis record.
     */
    var xBasis: Short = 0
    /**
     * Get the y Basis field for the FontBasis record.
     */
    /**
     * Set the y Basis field for the FontBasis record.
     */
    var yBasis: Short = 0
    /**
     * Get the height basis field for the FontBasis record.
     */
    /**
     * Set the height basis field for the FontBasis record.
     */
    var heightBasis: Short = 0
    /**
     * Get the scale field for the FontBasis record.
     */
    /**
     * Set the scale field for the FontBasis record.
     */
    var scale: Short = 0
    /**
     * Get the index to font table field for the FontBasis record.
     */
    /**
     * Set the index to font table field for the FontBasis record.
     */
    var indexToFontTable: Short = 0


    constructor()

    constructor(`in`: RecordInputStream) {
        this.xBasis = `in`.readShort()
        this.yBasis = `in`.readShort()
        this.heightBasis = `in`.readShort()
        this.scale = `in`.readShort()
        this.indexToFontTable = `in`.readShort()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[FBI]\n")
        buffer.append("    .xBasis               = ")
            .append("0x").append(toHex(this.xBasis))
            .append(" (").append(this.xBasis.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .yBasis               = ")
            .append("0x").append(toHex(this.yBasis))
            .append(" (").append(this.yBasis.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .heightBasis          = ")
            .append("0x").append(toHex(this.heightBasis))
            .append(" (").append(this.heightBasis.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .scale                = ")
            .append("0x").append(toHex(this.scale))
            .append(" (").append(this.scale.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .indexToFontTable     = ")
            .append("0x").append(toHex(this.indexToFontTable))
            .append(" (").append(this.indexToFontTable.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))

        buffer.append("[/FBI]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(xBasis.toInt())
        out.writeShort(yBasis.toInt())
        out.writeShort(heightBasis.toInt())
        out.writeShort(scale.toInt())
        out.writeShort(indexToFontTable.toInt())
    }

    override fun getDataSize(): Int {
        return 2 + 2 + 2 + 2 + 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = FontBasisRecord()

        rec.xBasis = this.xBasis
        rec.yBasis = this.yBasis
        rec.heightBasis = this.heightBasis
        rec.scale = this.scale
        rec.indexToFontTable = this.indexToFontTable
        return rec
    }


    companion object {
        const val sid: Short = 0x1060
    }
}
