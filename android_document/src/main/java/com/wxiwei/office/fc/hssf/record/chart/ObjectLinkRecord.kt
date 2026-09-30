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
import com.wxiwei.office.fc.hssf.record.UnknownRecord
import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndian.getShort
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * Links text to an object on the chart or identifies it as the title.
 *
 *
 * 
 * @author Andrew C. Oliver (acoliver at apache.org)
 */
class ObjectLinkRecord : StandardRecord {
    /**
     * Get the anchor id field for the ObjectLink record.
     * 
     * @return  One of
     * ANCHOR_ID_CHART_TITLE
     * ANCHOR_ID_Y_AXIS
     * ANCHOR_ID_X_AXIS
     * ANCHOR_ID_SERIES_OR_POINT
     * ANCHOR_ID_Z_AXIS
     */
    /**
     * Set the anchor id field for the ObjectLink record.
     * 
     * @param field_1_anchorId
     * One of
     * ANCHOR_ID_CHART_TITLE
     * ANCHOR_ID_Y_AXIS
     * ANCHOR_ID_X_AXIS
     * ANCHOR_ID_SERIES_OR_POINT
     * ANCHOR_ID_Z_AXIS
     */
    var anchorId: Short = 0
    /**
     * Get the link 1 field for the ObjectLink record.
     */
    /**
     * Set the link 1 field for the ObjectLink record.
     */
    var link1: Short = 0
    /**
     * Get the link 2 field for the ObjectLink record.
     */
    /**
     * Set the link 2 field for the ObjectLink record.
     */
    var link2: Short = 0


    constructor()

    constructor(`in`: RecordInputStream) {
        this.anchorId = `in`.readShort()
        this.link1 = `in`.readShort()
        this.link2 = `in`.readShort()
    }


    constructor(unknownRecord: UnknownRecord) {
        if (unknownRecord.getSid() == Companion.sid && unknownRecord.data.size == getDataSize()) {
            val data = unknownRecord.data
            this.anchorId = getShort(data, 0)
            this.link1 = getShort(data, 2)
            this.link2 = getShort(data, 4)
        }
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[OBJECTLINK]\n")
        buffer.append("    .anchorId             = ")
            .append("0x").append(toHex(this.anchorId))
            .append(" (").append(this.anchorId.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .link1                = ")
            .append("0x").append(toHex(this.link1))
            .append(" (").append(this.link1.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .link2                = ")
            .append("0x").append(toHex(this.link2))
            .append(" (").append(this.link2.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))

        buffer.append("[/OBJECTLINK]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(anchorId.toInt())
        out.writeShort(link1.toInt())
        out.writeShort(link2.toInt())
    }

    override fun getDataSize(): Int {
        return 2 + 2 + 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = ObjectLinkRecord()

        rec.anchorId = this.anchorId
        rec.link1 = this.link1
        rec.link2 = this.link2
        return rec
    }


    companion object {
        const val sid: Short = 0x1027
        const val ANCHOR_ID_CHART_TITLE: Short = 1
        const val ANCHOR_ID_Y_AXIS: Short = 2
        const val ANCHOR_ID_X_AXIS: Short = 3
        const val ANCHOR_ID_SERIES_OR_POINT: Short = 4
        const val ANCHOR_ID_Z_AXIS: Short = 7
    }
}
