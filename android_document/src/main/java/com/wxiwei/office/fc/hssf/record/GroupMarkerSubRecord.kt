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
package com.wxiwei.office.fc.hssf.record

import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * ftGmo (0x0006)
 *
 *
 * The group marker record is used as a position holder for groups.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class GroupMarkerSubRecord : SubRecord {
    private var reserved: ByteArray // would really love to know what goes in here.

    constructor() {
        reserved = EMPTY_BYTE_ARRAY
    }

    constructor(`in`: LittleEndianInput, size: Int) {
        val buf = ByteArray(size)
        `in`.readFully(buf)
        reserved = buf
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        val nl = System.getProperty("line.separator")
        buffer.append("[ftGmo]" + nl)
        buffer.append("  reserved = ").append(toHex(reserved)).append(nl)
        buffer.append("[/ftGmo]" + nl)
        return buffer.toString()
    }

    override fun serialize(out: LittleEndianOutput) {
        out.writeShort(sid.toInt())
        out.writeShort(reserved.size)
        out.write(reserved)
    }

    override fun getDataSize(): Int {
        return reserved.size
    }

    fun getSid(): Short {
        return sid
    }

    override fun clone(): Any {
        val rec = GroupMarkerSubRecord()
        rec.reserved = ByteArray(reserved.size)
        for (i in reserved.indices) rec.reserved[i] = reserved[i]
        return rec
    }

    companion object {
        const val sid: Short = 0x0006

        private val EMPTY_BYTE_ARRAY = byteArrayOf()
    }
}
