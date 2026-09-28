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

import com.wxiwei.office.fc.util.HexDump.dump
import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndian.putInt
import com.wxiwei.office.fc.util.LittleEndian.putShort
import java.io.ByteArrayOutputStream


/**
 * @author Glen Stampoultzis
 */
class EscherBitmapBlip : EscherBlipRecord() {
    var uID: ByteArray? = null
    var marker: Byte = 0xFF.toByte()

    override fun fillFields(
        data: ByteArray?,
        offset: Int,
        recordFactory: EscherRecordFactory?
    ): Int {
        val data = data!!
        val bytesAfterHeader = readHeader(data, offset)
        var pos: Int = offset + HEADER_SIZE

        this.uID = ByteArray(16)
        System.arraycopy(data, pos, this.uID, 0, 16)
        pos += 16
        this.marker = data[pos]
        pos++

        picturedata = ByteArray(bytesAfterHeader - 17)
        System.arraycopy(data, pos, picturedata!!, 0, picturedata!!.size)

        return bytesAfterHeader + HEADER_SIZE
    }

    override fun serialize(
        offset: Int,
        data: ByteArray?,
        listener: EscherSerializationListener?
    ): Int {
        val data = data!!
        val listener = listener!!
        listener.beforeRecordSerialize(offset, recordId, this)

        putShort(data, offset, options)
        putShort(data, offset + 2, recordId)
        putInt(data, offset + 4, recordSize - HEADER_SIZE)
        val pos: Int = offset + HEADER_SIZE

        System.arraycopy(this.uID, 0, data, pos, 16)
        data[pos + 16] = this.marker
        System.arraycopy(picturedata!!, 0, data, pos + 17, picturedata!!.size)

        listener.afterRecordSerialize(
            offset + recordSize,
            recordId,
            recordSize,
            this
        )
        return HEADER_SIZE + 16 + 1 + picturedata!!.size
    }

    override val recordSize: Int
        get() {
        return 8 + 16 + 1 + picturedata!!.size
    }

    override fun toString(): String {
        val nl = System.getProperty("line.separator")

        var extraData: String?
        val b = ByteArrayOutputStream()
        try {
            dump(picturedata!!, 0, b, 0)
            extraData = b.toString()
        } catch (e: Exception) {
            extraData = e.toString()
        }
        return javaClass.getName() + ":" + nl +
                "  RecordId: 0x" + toHex(recordId) + nl +
                "  Options: 0x" + toHex(options) + nl +
                "  UID: 0x" + toHex(this.uID!!) + nl +
                "  Marker: 0x" + toHex(this.marker) + nl +
                "  Extra Data:" + nl + extraData
    }

    companion object {
        @JvmField
        val RECORD_ID_JPEG: Short = (0xF018.toShort() + 5).toShort()
        @JvmField
        val RECORD_ID_PNG: Short = (0xF018.toShort() + 6).toShort()
        @JvmField
        val RECORD_ID_DIB: Short = (0xF018.toShort() + 7).toShort()

        private const val HEADER_SIZE = 8
    }
}
