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
package com.wxiwei.office.fc.hssf.record.pivottable

import com.wxiwei.office.fc.hssf.record.RecordInputStream
import com.wxiwei.office.fc.hssf.record.StandardRecord
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.fc.util.StringUtil.getEncodedSize
import com.wxiwei.office.fc.util.StringUtil.writeUnicodeString


/**
 * SXDI - Data Item (0x00C5)<br></br>
 * 
 * @author Patrick Cheng
 */
class DataItemRecord(`in`: RecordInputStream) : StandardRecord() {
    private val isxvdData: Int
    private val iiftab: Int
    private val df: Int
    private val isxvd: Int
    private val isxvi: Int
    private val ifmt: Int
    private val name: String

    init {
        isxvdData = `in`.readUShort()
        iiftab = `in`.readUShort()
        df = `in`.readUShort()
        isxvd = `in`.readUShort()
        isxvi = `in`.readUShort()
        ifmt = `in`.readUShort()

        name = `in`.readString()
    }

    override fun serialize(out: LittleEndianOutput) {
        out.writeShort(isxvdData)
        out.writeShort(iiftab)
        out.writeShort(df)
        out.writeShort(isxvd)
        out.writeShort(isxvi)
        out.writeShort(ifmt)

        writeUnicodeString(out, name)
    }

    override fun getDataSize(): Int {
        return 2 + 2 + 2 + 2 + 2 + 2 + getEncodedSize(name)
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[SXDI]\n")
        buffer.append("  .isxvdData = ").append(shortToHex(isxvdData)).append("\n")
        buffer.append("  .iiftab = ").append(shortToHex(iiftab)).append("\n")
        buffer.append("  .df = ").append(shortToHex(df)).append("\n")
        buffer.append("  .isxvd = ").append(shortToHex(isxvd)).append("\n")
        buffer.append("  .isxvi = ").append(shortToHex(isxvi)).append("\n")
        buffer.append("  .ifmt = ").append(shortToHex(ifmt)).append("\n")
        buffer.append("[/SXDI]\n")
        return buffer.toString()
    }

    companion object {
        const val sid: Short = 0x00C5
    }
}
