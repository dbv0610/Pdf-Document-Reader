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
import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * The plot growth record specifies the scaling factors used when a font is scaled.
 *
 *
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class PlotGrowthRecord : StandardRecord {
    /**
     * Get the horizontalScale field for the PlotGrowth record.
     */
    /**
     * Set the horizontalScale field for the PlotGrowth record.
     */
    var horizontalScale: Int = 0
    /**
     * Get the verticalScale field for the PlotGrowth record.
     */
    /**
     * Set the verticalScale field for the PlotGrowth record.
     */
    var verticalScale: Int = 0


    constructor()

    constructor(`in`: RecordInputStream) {
        this.horizontalScale = `in`.readInt()
        this.verticalScale = `in`.readInt()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[PLOTGROWTH]\n")
        buffer.append("    .horizontalScale      = ")
            .append("0x").append(toHex(this.horizontalScale))
            .append(" (").append(this.horizontalScale).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .verticalScale        = ")
            .append("0x").append(toHex(this.verticalScale))
            .append(" (").append(this.verticalScale).append(" )")
        buffer.append(System.getProperty("line.separator"))

        buffer.append("[/PLOTGROWTH]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeInt(this.horizontalScale)
        out.writeInt(this.verticalScale)
    }

    override fun getDataSize(): Int {
        return 4 + 4
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = PlotGrowthRecord()

        rec.horizontalScale = this.horizontalScale
        rec.verticalScale = this.verticalScale
        return rec
    }


    companion object {
        const val sid: Short = 0x1064
    }
}
