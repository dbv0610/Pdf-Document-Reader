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
 * CATLAB - Category Labels (0x0856)<br></br>
 * 
 * @author Patrick Cheng
 */
class CatLabRecord(`in`: RecordInputStream) : StandardRecord() {
    private val rt: Short
    private val grbitFrt: Short
    private val wOffset: Short
    private val at: Short
    private val grbit: Short
    private var unused: Short? = null

    init {
        rt = `in`.readShort()
        grbitFrt = `in`.readShort()
        wOffset = `in`.readShort()
        at = `in`.readShort()
        grbit = `in`.readShort()


        // Often, but not always has an unused short at the end
        if (`in`.available() == 0) {
            unused = null
        } else {
            unused = `in`.readShort()
        }
    }

    override fun getDataSize(): Int {
        return 2 + 2 + 2 + 2 + 2 + (if (unused == null) 0 else 2)
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(rt.toInt())
        out.writeShort(grbitFrt.toInt())
        out.writeShort(wOffset.toInt())
        out.writeShort(at.toInt())
        out.writeShort(grbit.toInt())
        if (unused != null) out.writeShort(unused!!.toInt())
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[CATLAB]\n")
        buffer.append("    .rt      =").append(shortToHex(rt.toInt())).append('\n')
        buffer.append("    .grbitFrt=").append(shortToHex(grbitFrt.toInt())).append('\n')
        buffer.append("    .wOffset =").append(shortToHex(wOffset.toInt())).append('\n')
        buffer.append("    .at      =").append(shortToHex(at.toInt())).append('\n')
        buffer.append("    .grbit   =").append(shortToHex(grbit.toInt())).append('\n')
        buffer.append("    .unused  =").append(shortToHex(unused!!.toInt())).append('\n')

        buffer.append("[/CATLAB]\n")
        return buffer.toString()
    }

    companion object {
        const val sid: Short = 0x0856
    }
}
