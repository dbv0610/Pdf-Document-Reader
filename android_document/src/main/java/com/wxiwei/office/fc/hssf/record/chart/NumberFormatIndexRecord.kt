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
 * The number format index record indexes format table.  This applies to an axis.
 *
 *
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class NumberFormatIndexRecord : StandardRecord {
    /**
     * Get the format index field for the NumberFormatIndex record.
     */
    /**
     * Set the format index field for the NumberFormatIndex record.
     */
    var formatIndex: Short = 0


    constructor()

    constructor(`in`: RecordInputStream) {
        this.formatIndex = `in`.readShort()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[IFMT]\n")
        buffer.append("    .formatIndex          = ")
            .append("0x").append(toHex(this.formatIndex))
            .append(" (").append(this.formatIndex.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))

        buffer.append("[/IFMT]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(formatIndex.toInt())
    }

    override fun getDataSize(): Int {
        return 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = NumberFormatIndexRecord()

        rec.formatIndex = this.formatIndex
        return rec
    }


    companion object {
        const val sid: Short = 0x104E
    }
}
