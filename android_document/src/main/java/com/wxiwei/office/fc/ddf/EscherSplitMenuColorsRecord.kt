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
package com.wxiwei.office.fc.ddf

import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndian.getInt
import com.wxiwei.office.fc.util.LittleEndian.putInt
import com.wxiwei.office.fc.util.LittleEndian.putShort
import com.wxiwei.office.fc.util.RecordFormatException

/**
 * A list of the most recently used colours for the drawings contained in
 * this document.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class EscherSplitMenuColorsRecord

    : EscherRecord() {
    var color1: Int = 0
    var color2: Int = 0
    var color3: Int = 0
    var color4: Int = 0

    override fun fillFields(
        data: ByteArray?,
        offset: Int,
        recordFactory: EscherRecordFactory?
    ): Int {
        val data = data!!
        var bytesRemaining = readHeader(data, offset)
        val pos = offset + 8
        var size = 0
        this.color1 = getInt(data, pos + size)
        size += 4
        this.color2 = getInt(data, pos + size)
        size += 4
        this.color3 = getInt(data, pos + size)
        size += 4
        this.color4 = getInt(data, pos + size)
        size += 4
        bytesRemaining -= size
        if (bytesRemaining != 0) throw RecordFormatException("Expecting no remaining data but got " + bytesRemaining + " byte(s).")
        return 8 + size + bytesRemaining
    }

    override fun serialize(
        offset: Int,
        data: ByteArray?,
        listener: EscherSerializationListener?
    ): Int {
        val data = data!!
        val listener = listener!!
//        int field_2_numIdClusters = field_5_fileIdClusters.length + 1;
        listener.beforeRecordSerialize(offset, recordId, this)

        var pos = offset
        putShort(data, pos, options)
        pos += 2
        putShort(data, pos, recordId)
        pos += 2
        val remainingBytes = recordSize - 8

        putInt(data, pos, remainingBytes)
        pos += 4
        putInt(data, pos, this.color1)
        pos += 4
        putInt(data, pos, this.color2)
        pos += 4
        putInt(data, pos, this.color3)
        pos += 4
        putInt(data, pos, this.color4)
        pos += 4
        listener.afterRecordSerialize(pos, recordId, pos - offset, this)
        return recordSize
    }

    override val recordSize: Int
        get() {
        return 8 + 4 * 4
    }

    override var recordId: Short
        set(value) {
            super.recordId = value
        }
        get() {
        return RECORD_ID
    }

    override val recordName: String
        get() {
        return "SplitMenuColors"
    }

    /**
     * @return  a string representation of this record.
     */
    override fun toString(): String {
        return javaClass.getName() + ":" + '\n' +
                "  RecordId: 0x" + toHex(RECORD_ID) + '\n' +
                "  Options: 0x" + toHex(options) + '\n' +
                "  Color1: 0x" + toHex(this.color1) + '\n' +
                "  Color2: 0x" + toHex(this.color2) + '\n' +
                "  Color3: 0x" + toHex(this.color3) + '\n' +
                "  Color4: 0x" + toHex(this.color4) + '\n' +
                ""
    }

    /**
     * 
     * 
     */
    override fun dispose() {
    }

    companion object {
        @JvmField
        val RECORD_ID: Short = 0xF11E.toShort()
        const val RECORD_DESCRIPTION: String = "MsofbtSplitMenuColors"
    }
}
