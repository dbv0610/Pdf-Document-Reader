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
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * STARTBLOCK - Chart Future Record Type Start Block (0x0852)<br></br>
 * 
 * @author Patrick Cheng
 */
class ChartStartBlockRecord : StandardRecord {
    private var rt: Short = 0
    private var grbitFrt: Short = 0
    private var iObjectKind: Short = 0
    private var iObjectContext: Short = 0
    private var iObjectInstance1: Short = 0
    private var iObjectInstance2: Short = 0

    constructor()

    constructor(`in`: RecordInputStream) {
        rt = `in`.readShort()
        grbitFrt = `in`.readShort()
        iObjectKind = `in`.readShort()
        iObjectContext = `in`.readShort()
        iObjectInstance1 = `in`.readShort()
        iObjectInstance2 = `in`.readShort()
    }

    override fun getDataSize(): Int {
        return 2 + 2 + 2 + 2 + 2 + 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(rt.toInt())
        out.writeShort(grbitFrt.toInt())
        out.writeShort(iObjectKind.toInt())
        out.writeShort(iObjectContext.toInt())
        out.writeShort(iObjectInstance1.toInt())
        out.writeShort(iObjectInstance2.toInt())
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[STARTBLOCK]\n")
        buffer.append("    .rt              =").append(shortToHex(rt.toInt())).append('\n')
        buffer.append("    .grbitFrt        =").append(shortToHex(grbitFrt.toInt())).append('\n')
        buffer.append("    .iObjectKind     =").append(shortToHex(iObjectKind.toInt())).append('\n')
        buffer.append("    .iObjectContext  =").append(shortToHex(iObjectContext.toInt()))
            .append('\n')
        buffer.append("    .iObjectInstance1=").append(shortToHex(iObjectInstance1.toInt()))
            .append('\n')
        buffer.append("    .iObjectInstance2=").append(shortToHex(iObjectInstance2.toInt()))
            .append('\n')
        buffer.append("[/STARTBLOCK]\n")
        return buffer.toString()
    }

    override fun clone(): ChartStartBlockRecord {
        val record = ChartStartBlockRecord()

        record.rt = rt
        record.grbitFrt = grbitFrt
        record.iObjectKind = iObjectKind
        record.iObjectContext = iObjectContext
        record.iObjectInstance1 = iObjectInstance1
        record.iObjectInstance2 = iObjectInstance2

        return record
    }

    companion object {
        const val sid: Short = 0x0852
    }
}
