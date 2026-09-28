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
 * ENDOBJECT - Chart Future Record Type End Object (0x0855)<br></br>
 * 
 * @author Patrick Cheng
 */
class ChartEndObjectRecord(`in`: RecordInputStream) : StandardRecord() {
    private val rt: Short
    private val grbitFrt: Short
    private val iObjectKind: Short
    private var reserved: ByteArray

    init {
        rt = `in`.readShort()
        grbitFrt = `in`.readShort()
        iObjectKind = `in`.readShort()

        // The spec says that there should be 6 bytes at the
        //  end, which must be there and must be zero
        // However, sometimes Excel forgets them...
        reserved = ByteArray(6)
        if (`in`.available() == 0) {
            // They've gone missing...
        } else {
            // Read the reserved bytes 
            `in`.readFully(reserved)
        }
    }

    override fun getDataSize(): Int {
        return 2 + 2 + 2 + 6
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(rt.toInt())
        out.writeShort(grbitFrt.toInt())
        out.writeShort(iObjectKind.toInt())
        // 6 bytes unused
        out.write(reserved)
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[ENDOBJECT]\n")
        buffer.append("    .rt         =").append(shortToHex(rt.toInt())).append('\n')
        buffer.append("    .grbitFrt   =").append(shortToHex(grbitFrt.toInt())).append('\n')
        buffer.append("    .iObjectKind=").append(shortToHex(iObjectKind.toInt())).append('\n')
        buffer.append("    .reserved   =").append(toHex(reserved)).append('\n')
        buffer.append("[/ENDOBJECT]\n")
        return buffer.toString()
    }

    companion object {
        const val sid: Short = 0x0855
    }
}
