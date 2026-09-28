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
import com.wxiwei.office.fc.util.LittleEndian.getInt
import com.wxiwei.office.fc.util.LittleEndian.putInt
import com.wxiwei.office.fc.util.LittleEndian.putShort
import com.wxiwei.office.fc.util.RecordFormatException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream


/**
 * The blip record is used to hold details about large binary objects that occur in escher such
 * as JPEG, GIF, PICT and WMF files.  The contents of the stream is usually compressed.  Inflate
 * can be used to decompress the data.
 * 
 * @author Glen Stampoultzis
 * @see java.util.zip.Inflater
 */
class EscherBlipWMFRecord

    : EscherBlipRecord() {
    /**
     * Retrieve the secondary UID
     */
    /**
     * Set the secondary UID
     */
    var secondaryUID: ByteArray = ByteArray(16)
    /**
     * Retrieve the cache of the metafile size
     */
    /**
     * Set the cache of the metafile size
     */
    var cacheOfSize: Int = 0
    /**
     * Retrieve the top boundary of the metafile drawing commands
     */
    /**
     * Set the top boundary of the metafile drawing commands
     */
    var boundaryTop: Int = 0
    /**
     * Retrieve the left boundary of the metafile drawing commands
     */
    /**
     * Set the left boundary of the metafile drawing commands
     */
    var boundaryLeft: Int = 0
    /**
     * Retrieve the boundary width of the metafile drawing commands
     */
    /**
     * Set the boundary width of the metafile drawing commands
     */
    var boundaryWidth: Int = 0
    /**
     * Retrieve the boundary height of the metafile drawing commands
     */
    /**
     * Set the boundary height of the metafile drawing commands
     */
    var boundaryHeight: Int = 0
    /**
     * Retrieve the width of the metafile in EMU's (English Metric Units).
     */
    /**
     * Set the width of the metafile in EMU's (English Metric Units).
     */
    var width: Int = 0
    /**
     * Retrieve the height of the metafile in EMU's (English Metric Units).
     */
    /**
     * Set the height of the metafile in EMU's (English Metric Units).
     */
    var height: Int = 0
    /**
     * Retrieve the cache of the saved size
     */
    /**
     * Set the cache of the saved size
     */
    var cacheOfSavedSize: Int = 0
    /**
     * Is the contents of the blip compressed?
     */
    /**
     * Set whether the contents of the blip is compressed
     */
    var compressionFlag: Byte = 0
    /**
     * Filter should always be 0
     */
    /**
     * Filter should always be 0
     */
    var filter: Byte = 0
    /**
     * The BLIP data
     */
    /**
     * The BLIP data
     */
    var data: ByteArray = ByteArray(0)

    override fun fillFields(
        data: ByteArray?,
        offset: Int,
        recordFactory: EscherRecordFactory?
    ): Int {
        val data = data!!
        val bytesAfterHeader = readHeader(data, offset)
        val pos: Int = offset + HEADER_SIZE

        var size = 0
        this.secondaryUID = ByteArray(16)
        System.arraycopy(data, pos + size, this.secondaryUID, 0, 16)
        size += 16
        this.cacheOfSize = getInt(data, pos + size)
        size += 4
        this.boundaryTop = getInt(data, pos + size)
        size += 4
        this.boundaryLeft = getInt(data, pos + size)
        size += 4
        this.boundaryWidth = getInt(data, pos + size)
        size += 4
        this.boundaryHeight = getInt(data, pos + size)
        size += 4
        this.width = getInt(data, pos + size)
        size += 4
        this.height = getInt(data, pos + size)
        size += 4
        this.cacheOfSavedSize = getInt(data, pos + size)
        size += 4
        this.compressionFlag = data[pos + size]
        size++
        this.filter = data[pos + size]
        size++

        val bytesRemaining = bytesAfterHeader - size
        this.data = ByteArray(bytesRemaining)
        System.arraycopy(data, pos + size, this.data, 0, bytesRemaining)
        size += bytesRemaining

        return HEADER_SIZE + size
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
        val remainingBytes = data.size + 36
        putInt(data, offset + 4, remainingBytes)

        var pos: Int = offset + HEADER_SIZE
        System.arraycopy(this.secondaryUID, 0, data, pos, 16)
        pos += 16
        putInt(data, pos, this.cacheOfSize)
        pos += 4
        putInt(data, pos, this.boundaryTop)
        pos += 4
        putInt(data, pos, this.boundaryLeft)
        pos += 4
        putInt(data, pos, this.boundaryWidth)
        pos += 4
        putInt(data, pos, this.boundaryHeight)
        pos += 4
        putInt(data, pos, this.width)
        pos += 4
        putInt(data, pos, this.height)
        pos += 4
        putInt(data, pos, this.cacheOfSavedSize)
        pos += 4
        data[pos++] = this.compressionFlag
        data[pos++] = this.filter
        System.arraycopy(this.data, 0, data, pos, data.size)
        pos += data.size

        listener.afterRecordSerialize(pos, recordId, pos - offset, this)
        return pos - offset
    }

    override val recordSize: Int
        get() {
        return 58 + data.size
    }

    override val recordName: String
        get() {
        return "Blip"
    }

    /**
     * The string representation of this record.
     * 
     * @return A string
     */
    override fun toString(): String {
        val nl = System.getProperty("line.separator")

        var extraData: String?
        val b = ByteArrayOutputStream()
        try {
            dump(this.data, 0, b, 0)
            extraData = b.toString()
        } catch (e: Exception) {
            extraData = e.toString()
        }
        return javaClass.getName() + ":" + nl +
                "  RecordId: 0x" + toHex(recordId) + nl +
                "  Options: 0x" + toHex(options) + nl +
                "  Secondary UID: " + toHex(this.secondaryUID) + nl +
                "  CacheOfSize: " + this.cacheOfSize + nl +
                "  BoundaryTop: " + this.boundaryTop + nl +
                "  BoundaryLeft: " + this.boundaryLeft + nl +
                "  BoundaryWidth: " + this.boundaryWidth + nl +
                "  BoundaryHeight: " + this.boundaryHeight + nl +
                "  X: " + this.width + nl +
                "  Y: " + this.height + nl +
                "  CacheOfSavedSize: " + this.cacheOfSavedSize + nl +
                "  CompressionFlag: " + this.compressionFlag + nl +
                "  Filter: " + this.filter + nl +
                "  Data:" + nl + extraData
    }

    companion object {
        //    public static final short  RECORD_ID_START    = (short) 0xF018;
        //    public static final short  RECORD_ID_END      = (short) 0xF117;
        const val RECORD_DESCRIPTION: String = "msofbtBlip"
        private const val HEADER_SIZE = 8

        /**
         * Compress the contents of the provided array
         * 
         * @param data An uncompressed byte array
         * @see DeflaterOutputStream.write
         */
        fun compress(data: ByteArray): ByteArray {
            val out = ByteArrayOutputStream()
            val deflaterOutputStream = DeflaterOutputStream(out)
            try {
                for (i in data.indices) deflaterOutputStream.write(data[i].toInt())
            } catch (e: IOException) {
                throw RecordFormatException(e.toString())
            }

            return out.toByteArray()
        }

        /**
         * Decompresses a byte array.
         * 
         * @param data   The compressed byte array
         * @param pos    The starting position into the byte array
         * @param length The number of compressed bytes to decompress
         * @return An uncompressed byte array
         * @see InflaterInputStream.read
         */
        fun decompress(data: ByteArray, pos: Int, length: Int): ByteArray {
            val compressedData = ByteArray(length)
            System.arraycopy(data, pos + 50, compressedData, 0, length)
            val compressedInputStream: InputStream = ByteArrayInputStream(compressedData)
            val inflaterInputStream = InflaterInputStream(compressedInputStream)
            val out = ByteArrayOutputStream()
            var c: Int
            try {
                while ((inflaterInputStream.read().also { c = it }) != -1) out.write(c)
            } catch (e: IOException) {
                throw RecordFormatException(e.toString())
            }
            return out.toByteArray()
        }
    }
}
