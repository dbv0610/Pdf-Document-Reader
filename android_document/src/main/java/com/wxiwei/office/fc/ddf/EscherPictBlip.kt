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
import com.wxiwei.office.fc.util.POILogFactory.Companion.getLogger
import com.wxiwei.office.fc.util.POILogger
import com.wxiwei.office.java.awt.Dimension
import com.wxiwei.office.java.awt.Rectangle
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.InflaterInputStream

/**
 * @author Daniel Noll
 */
class EscherPictBlip : EscherBlipRecord() {
    var uID: ByteArray? = null
    var uncompressedSize: Int = 0
    private var field_3_rcBounds_x1 = 0
    private var field_3_rcBounds_y1 = 0
    private var field_3_rcBounds_x2 = 0
    private var field_3_rcBounds_y2 = 0
    private var field_4_ptSize_w = 0
    private var field_4_ptSize_h = 0
    var compressedSize: Int = 0
    private var field_6_fCompression: Byte = 0
    private var field_7_fFilter: Byte = 0

    private lateinit var raw_pictureData: ByteArray

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
        this.uncompressedSize = getInt(data, pos)
        pos += 4
        field_3_rcBounds_x1 = getInt(data, pos)
        pos += 4
        field_3_rcBounds_y1 = getInt(data, pos)
        pos += 4
        field_3_rcBounds_x2 = getInt(data, pos)
        pos += 4
        field_3_rcBounds_y2 = getInt(data, pos)
        pos += 4
        field_4_ptSize_w = getInt(data, pos)
        pos += 4
        field_4_ptSize_h = getInt(data, pos)
        pos += 4
        this.compressedSize = getInt(data, pos)
        pos += 4
        field_6_fCompression = data[pos]
        pos++
        field_7_fFilter = data[pos]
        pos++

        raw_pictureData = ByteArray(this.compressedSize)
        System.arraycopy(data, pos, raw_pictureData, 0, this.compressedSize)

        // 0 means DEFLATE compression
        // 0xFE means no compression
        if (field_6_fCompression.toInt() == 0) {
            picturedata = inflatePictureData(raw_pictureData)
        } else {
            picturedata = raw_pictureData
        }

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

        var pos = offset
        putShort(data, pos, options)
        pos += 2
        putShort(data, pos, recordId)
        pos += 2
        putInt(data, recordSize - HEADER_SIZE)
        pos += 4

        System.arraycopy(this.uID, 0, data, pos, 16)
        pos += 16
        putInt(data, pos, this.uncompressedSize)
        pos += 4
        putInt(data, pos, field_3_rcBounds_x1)
        pos += 4
        putInt(data, pos, field_3_rcBounds_y1)
        pos += 4
        putInt(data, pos, field_3_rcBounds_x2)
        pos += 4
        putInt(data, pos, field_3_rcBounds_y2)
        pos += 4
        putInt(data, pos, field_4_ptSize_w)
        pos += 4
        putInt(data, pos, field_4_ptSize_h)
        pos += 4
        putInt(data, pos, this.compressedSize)
        pos += 4
        data[pos] = field_6_fCompression
        pos++
        data[pos] = field_7_fFilter
        pos++

        System.arraycopy(raw_pictureData, 0, data, pos, raw_pictureData.size)

        listener.afterRecordSerialize(
            offset + recordSize,
            recordId,
            recordSize,
            this
        )
        return HEADER_SIZE + 16 + 1 + raw_pictureData.size
    }

    override val recordSize: Int
        get() {
        return 8 + 50 + raw_pictureData.size
    }

    var bounds: Rectangle
        get() = Rectangle(
            field_3_rcBounds_x1,
            field_3_rcBounds_y1,
            field_3_rcBounds_x2 - field_3_rcBounds_x1,
            field_3_rcBounds_y2 - field_3_rcBounds_y1
        )
        set(bounds) {
            field_3_rcBounds_x1 = bounds.x
            field_3_rcBounds_y1 = bounds.y
            field_3_rcBounds_x2 = bounds.x + bounds.width
            field_3_rcBounds_y2 = bounds.y + bounds.height
        }

    var sizeEMU: Dimension
        get() = Dimension(field_4_ptSize_w, field_4_ptSize_h)
        set(sizeEMU) {
            field_4_ptSize_w = sizeEMU.width
            field_4_ptSize_h = sizeEMU.height
        }

    var isCompressed: Boolean
        get() = (field_6_fCompression.toInt() == 0)
        set(compressed) {
            field_6_fCompression = if (compressed) 0 else 0xFE.toByte()
        }

    // filtering is always 254 according to available docs, so no point giving it a setter method.
    override fun toString(): String {
        val extraData = toHex(picturedata!!, 32)
        return javaClass.getName() + ":" + '\n' +
                "  RecordId: 0x" + toHex(recordId) + '\n' +
                "  Options: 0x" + toHex(options) + '\n' +
                "  UID: 0x" + toHex(this.uID!!) + '\n' +
                "  Uncompressed Size: " + toHex(this.uncompressedSize) + '\n' +
                "  Bounds: " + this.bounds + '\n' +
                "  Size in EMU: " + this.sizeEMU + '\n' +
                "  Compressed Size: " + toHex(this.compressedSize) + '\n' +
                "  Compression: " + toHex(field_6_fCompression) + '\n' +
                "  Filter: " + toHex(field_7_fFilter) + '\n' +
                "  Extra Data:" + '\n' + extraData
    }

    companion object {
        private val log = getLogger(EscherPictBlip::class.java)

        val RECORD_ID_EMF: Short = (0xF018.toShort() + 2).toShort()
        val RECORD_ID_WMF: Short = (0xF018.toShort() + 3).toShort()
        val RECORD_ID_PICT: Short = (0xF018.toShort() + 4).toShort()

        private const val HEADER_SIZE = 8

        /**
         * Decompresses the provided data, returning the inflated result.
         * 
         * @param data the deflated picture data.
         * @return the inflated picture data.
         */
        private fun inflatePictureData(data: ByteArray?): ByteArray? {
            try {
                val `in` = InflaterInputStream(ByteArrayInputStream(data))
                val out = ByteArrayOutputStream()
                val buf = ByteArray(4096)
                var readBytes: Int
                while ((`in`.read(buf).also { readBytes = it }) > 0) {
                    out.write(buf, 0, readBytes)
                }
                return out.toByteArray()
            } catch (e: IOException) {
                log.log(POILogger.INFO, "Possibly corrupt compression or non-compressed data", e)
                return data
            }
        }
    }
}
