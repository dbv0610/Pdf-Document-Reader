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

import com.wxiwei.office.fc.util.HexDump
import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndian.getInt
import com.wxiwei.office.fc.util.LittleEndian.getShort
import com.wxiwei.office.fc.util.LittleEndian.putInt
import com.wxiwei.office.fc.util.LittleEndian.putShort

/**
 * The BSE record is related closely to the `EscherBlipRecord` and stores
 * extra information about the blip.  A blip record is actually stored inside
 * the BSE record even though the BSE record isn't actually a container record.
 * 
 * @author Glen Stampoultzis
 * @see EscherBlipRecord
 */
class EscherBSERecord : EscherRecord() {
    /**
     * The expected blip type under windows (failure to match this blip type will result in
     * Excel converting to this format).
     */
    /**
     * Set the expected win32 blip type
     */
    var blipTypeWin32: Byte = 0
    /**
     * The expected blip type under MacOS (failure to match this blip type will result in
     * Excel converting to this format).
     */
    /**
     * Set the expected MacOS blip type
     */
    var blipTypeMacOS: Byte = 0
    /**
     * 16 byte MD4 checksum.
     */
    /**
     * 16 byte MD4 checksum.
     */
    var uid: ByteArray? = null // 16 bytes
    /**
     * unused
     */
    /**
     * unused
     */
    var tag: Short = 0
    /**
     * Blip size in stream.
     */
    /**
     * Blip size in stream.
     */
    var size: Int = 0
    /**
     * The reference count of this blip.
     */
    /**
     * The reference count of this blip.
     */
    var ref: Int = 0
    /**
     * File offset in the delay stream.
     */
    /**
     * File offset in the delay stream.
     */
    var offset: Int = 0
    /**
     * Defines the way this blip is used.
     */
    /**
     * Defines the way this blip is used.
     */
    var usage: Byte = 0
    /**
     * The length in characters of the blip name.
     */
    /**
     * The length in characters of the blip name.
     */
    var name: Byte = 0
    var unused2: Byte = 0
    var unused3: Byte = 0
    var blipRecord: EscherBlipRecord? = null

    /**
     * Any remaining data in this record.
     */
    /**
     * Any remaining data in this record.
     */
    var remainingData: ByteArray? = null

    override fun fillFields(
        data: ByteArray?,
        offset: Int,
        recordFactory: EscherRecordFactory?
    ): Int {
        val data = data!!
        var bytesRemaining = readHeader(data, offset)
        var pos = offset + 8
        this.blipTypeWin32 = data[pos]
        this.blipTypeMacOS = data[pos + 1]
        System.arraycopy(data, pos + 2, ByteArray(16).also { this.uid = it }, 0, 16)
        this.tag = getShort(data, pos + 18)
        this.size = getInt(data, pos + 20)
        this.ref = getInt(data, pos + 24)
        this.offset = getInt(data, pos + 28)
        this.usage = data[pos + 32]
        this.name = data[pos + 33]
        this.unused2 = data[pos + 34]
        this.unused3 = data[pos + 35]
        bytesRemaining -= 36

        var bytesRead = 0
        if (bytesRemaining > 0) {
            // Some older escher formats skip this last record
            val r = recordFactory!!.createRecord(data, pos + 36)
            if (r is EscherBlipRecord) {
                this.blipRecord = r
                bytesRead = blipRecord!!.fillFields(data, pos + 36, recordFactory)
            } else if (r is EscherBSERecord) {
                val eacherBSERecord = r
                return fillFields(data, pos + 36, recordFactory)
            }
        }
        pos += 36 + bytesRead
        bytesRemaining -= bytesRead

        this.remainingData = ByteArray(bytesRemaining)
        System.arraycopy(data, pos, this.remainingData, 0, bytesRemaining)
        return bytesRemaining + 8 + 36 + (if (this.blipRecord == null) 0 else blipRecord!!.recordSize)
    }

    override fun serialize(
        offset: Int,
        data: ByteArray?,
        listener: EscherSerializationListener?
    ): Int {
        val data = data!!
        val listener = listener!!
        listener.beforeRecordSerialize(offset, recordId, this)

        if (this.remainingData == null) this.remainingData = ByteArray(0)

        putShort(data, offset, options)
        putShort(data, offset + 2, recordId)
        if (this.remainingData == null) this.remainingData = ByteArray(0)
        val blipSize = if (this.blipRecord == null) 0 else blipRecord!!.recordSize
        val remainingBytes = remainingData!!.size + 36 + blipSize
        putInt(data, offset + 4, remainingBytes)

        data[offset + 8] = this.blipTypeWin32
        data[offset + 9] = this.blipTypeMacOS
        for (i in 0..15) data[offset + 10 + i] = this.uid!![i]
        putShort(data, offset + 26, this.tag)
        putInt(data, offset + 28, this.size)
        putInt(data, offset + 32, this.ref)
        putInt(data, offset + 36, this.offset)
        data[offset + 40] = this.usage
        data[offset + 41] = this.name
        data[offset + 42] = this.unused2
        data[offset + 43] = this.unused3
        var bytesWritten = 0
        if (this.blipRecord != null) {
            bytesWritten =
                blipRecord!!.serialize(offset + 44, data, NullEscherSerializationListener())
        }
        if (this.remainingData == null) this.remainingData = ByteArray(0)
        System.arraycopy(
            this.remainingData,
            0,
            data,
            offset + 44 + bytesWritten,
            remainingData!!.size
        )
        val pos = offset + 8 + 36 + remainingData!!.size + bytesWritten

        listener.afterRecordSerialize(pos, recordId, pos - offset, this)
        return pos - offset
    }

    override val recordSize: Int
        get() {
        var field_12_size = 0
        if (this.blipRecord != null) {
            field_12_size = blipRecord!!.recordSize
        }
        var remaining_size = 0
        if (this.remainingData != null) {
            remaining_size = remainingData!!.size
        }
        return 8 + 1 + 1 + 16 + 2 + 4 + 4 + 4 + 1 + 1 +
                1 + 1 + field_12_size + remaining_size
    }

    override val recordName: String
        get() {
        return "BSE"
    }

    override fun toString(): String {
        val extraData = if (this.remainingData == null) null else HexDump.toHex(
            this.remainingData!!, 32
        )
        return javaClass.getName() + ":" + '\n' +
                "  RecordId: 0x" + toHex(RECORD_ID) + '\n' +
                "  Options: 0x" + toHex(options) + '\n' +
                "  BlipTypeWin32: " + this.blipTypeWin32 + '\n' +
                "  BlipTypeMacOS: " + this.blipTypeMacOS + '\n' +
                "  SUID: " + (if (this.uid == null) "" else HexDump.toHex(this.uid!!)) + '\n' +
                "  Tag: " + this.tag + '\n' +
                "  Size: " + this.size + '\n' +
                "  Ref: " + this.ref + '\n' +
                "  Offset: " + this.offset + '\n' +
                "  Usage: " + this.usage + '\n' +
                "  Name: " + this.name + '\n' +
                "  Unused2: " + this.unused2 + '\n' +
                "  Unused3: " + this.unused3 + '\n' +
                "  blipRecord: " + this.blipRecord + '\n' +
                "  Extra Data:" + '\n' + extraData
    }

    /**
     * 
     */
    override fun dispose() {
        this.uid = null
        this.remainingData = null
        if (this.blipRecord != null) {
            blipRecord!!.dispose()
            this.blipRecord = null
        }
    }

    companion object {
        @JvmField
        val RECORD_ID: Short = 0xF007.toShort()
        const val RECORD_DESCRIPTION: String = "MsofbtBSE"

        const val BT_ERROR: Byte = 0
        const val BT_UNKNOWN: Byte = 1
        const val BT_EMF: Byte = 2
        const val BT_WMF: Byte = 3
        const val BT_PICT: Byte = 4
        const val BT_JPEG: Byte = 5
        const val BT_PNG: Byte = 6
        const val BT_DIB: Byte = 7

        /**
         * Retrieve the string representation given a blip id.
         */
        fun getBlipType(b: Byte): String {
            when (b) {
                BT_ERROR -> return " ERROR"
                BT_UNKNOWN -> return " UNKNOWN"
                BT_EMF -> return " EMF"
                BT_WMF -> return " WMF"
                BT_PICT -> return " PICT"
                BT_JPEG -> return " JPEG"
                BT_PNG -> return " PNG"
                BT_DIB -> return " DIB"
            }
            if (b < 32) {
                return " NotKnown"
            }
            return " Client"
        }
    }
}
