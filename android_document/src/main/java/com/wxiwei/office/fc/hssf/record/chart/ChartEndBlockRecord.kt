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
import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * ENDBLOCK - Chart Future Record Type End Block (0x0853)<br></br>
 * 
 * @author Patrick Cheng
 */
class ChartEndBlockRecord : StandardRecord {
    private var rt: Short = 0
    private var grbitFrt: Short = 0
    private var iObjectKind: Short = 0
    private var unused: ByteArray = byteArrayOf()

    constructor()

    constructor(`in`: RecordInputStream) {
        rt = `in`.readShort()
        grbitFrt = `in`.readShort()
        iObjectKind = `in`.readShort()

        // Often, but not always has 6 unused bytes at the end
        if (`in`.available() == 0) {
            unused = ByteArray(0)
        } else {
            unused = ByteArray(6)
            `in`.readFully(unused)
        }
    }

    override fun getDataSize(): Int {
        return 2 + 2 + 2 + unused.size
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(rt.toInt())
        out.writeShort(grbitFrt.toInt())
        out.writeShort(iObjectKind.toInt())
        // 6 bytes unused
        out.write(unused)
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[ENDBLOCK]\n")
        buffer.append("    .rt         =").append(shortToHex(rt.toInt())).append('\n')
        buffer.append("    .grbitFrt   =").append(shortToHex(grbitFrt.toInt())).append('\n')
        buffer.append("    .iObjectKind=").append(shortToHex(iObjectKind.toInt())).append('\n')
        buffer.append("    .unused     =").append(toHex(unused)).append('\n')
        buffer.append("[/ENDBLOCK]\n")
        return buffer.toString()
    }

    override fun clone(): ChartEndBlockRecord {
        val record = ChartEndBlockRecord()

        record.rt = rt
        record.grbitFrt = grbitFrt
        record.iObjectKind = iObjectKind
        record.unused = unused.clone()

        return record
    }

    companion object {
        const val sid: Short = 0x0853
    }
}
