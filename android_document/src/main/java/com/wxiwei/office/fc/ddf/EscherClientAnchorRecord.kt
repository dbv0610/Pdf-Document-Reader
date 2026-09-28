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

import com.wxiwei.office.fc.util.HexDump
import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndian.getShort
import com.wxiwei.office.fc.util.LittleEndian.putInt
import com.wxiwei.office.fc.util.LittleEndian.putShort
import java.io.ByteArrayOutputStream


/**
 * The escher client anchor specifies which rows and cells the shape is bound to as well as
 * the offsets within those cells.  Each cell is 1024 units wide by 256 units long regardless
 * of the actual size of the cell.  The EscherClientAnchorRecord only applies to the top-most
 * shapes.  Shapes contained in groups are bound using the EscherChildAnchorRecords.
 * 
 * @author Glen Stampoultzis
 * @see EscherChildAnchorRecord
 */
class EscherClientAnchorRecord

    : EscherRecord() {
    /**
     * 0 = Move and size with Cells, 2 = Move but don't size with cells, 3 = Don't move or size with cells.
     */
    /**
     * 0 = Move and size with Cells, 2 = Move but don't size with cells, 3 = Don't move or size with cells.
     */
    var flag: Short = 0
    /**
     * The column number for the top-left position.  0 based.
     */
    /**
     * The column number for the top-left position.  0 based.
     */
    var col1: Short = 0
    /**
     * The x offset within the top-left cell.  Range is from 0 to 1023.
     */
    /**
     * The x offset within the top-left cell.  Range is from 0 to 1023.
     */
    var dx1: Short = 0
    /**
     * The row number for the top-left corner of the shape.
     */
    /**
     * The row number for the top-left corner of the shape.
     */
    var row1: Short = 0
    private var field_5_dy1: Short = 0
    private var field_6_col2: Short = 0
    private var field_7_dx2: Short = 0
    private var field_8_row2: Short = 0
    private var field_9_dy2: Short = 0
    /**
     * Any remaining data in the record
     */
    /**
     * Any remaining data in the record
     */
    var remainingData: ByteArray? = null
    private var shortRecord = false

    override fun fillFields(
        data: ByteArray?,
        offset: Int,
        recordFactory: EscherRecordFactory?
    ): Int {
        val data = data!!
        var bytesRemaining = readHeader(data, offset)
        val pos = offset + 8
        var size = 0

        // Always find 4 two byte entries. Sometimes find 9
        if (bytesRemaining == 4)  // Word format only 4 bytes
        {
            // Not sure exactly what the format is quite yet, likely a reference to a PLC
        } else {
            if (bytesRemaining == 16) {
                this.flag = getShort(data, pos + size)
                size += 4
                this.col1 = getShort(data, pos + size)
                size += 4
                this.dx1 = getShort(data, pos + size)
                size += 4
                this.row1 = getShort(data, pos + size)
                size += 4
                shortRecord = false
            } else {
                this.flag = getShort(data, pos + size)
                size += 2
                this.col1 = getShort(data, pos + size)
                size += 2
                this.dx1 = getShort(data, pos + size)
                size += 2
                this.row1 = getShort(data, pos + size)
                size += 2

                if (bytesRemaining >= 18) {
                    field_5_dy1 = getShort(data, pos + size)
                    size += 2
                    field_6_col2 = getShort(data, pos + size)
                    size += 2
                    field_7_dx2 = getShort(data, pos + size)
                    size += 2
                    field_8_row2 = getShort(data, pos + size)
                    size += 2
                    field_9_dy2 = getShort(data, pos + size)
                    size += 2
                    shortRecord = false
                } else {
                    shortRecord = true
                }
            }
        }
        bytesRemaining -= size
        remainingData = ByteArray(bytesRemaining)
        System.arraycopy(data, pos + size, remainingData, 0, bytesRemaining)
        return 8 + size + bytesRemaining
    }

    override fun serialize(
        offset: Int,
        data: ByteArray?,
        listener: EscherSerializationListener?
    ): Int {
        val data = data!!
        val listener = listener!!
        listener.beforeRecordSerialize(offset, recordId, this)

        if (remainingData == null) remainingData = ByteArray(0)
        putShort(data, offset, options)
        putShort(data, offset + 2, recordId)
        val remainingBytes = remainingData!!.size + (if (shortRecord) 8 else 18)
        putInt(data, offset + 4, remainingBytes)
        putShort(data, offset + 8, this.flag)
        putShort(data, offset + 10, this.col1)
        putShort(data, offset + 12, this.dx1)
        putShort(data, offset + 14, this.row1)
        if (!shortRecord) {
            putShort(data, offset + 16, field_5_dy1)
            putShort(data, offset + 18, field_6_col2)
            putShort(data, offset + 20, field_7_dx2)
            putShort(data, offset + 22, field_8_row2)
            putShort(data, offset + 24, field_9_dy2)
        }
        System.arraycopy(
            remainingData,
            0,
            data,
            offset + (if (shortRecord) 16 else 26),
            remainingData!!.size
        )
        val pos = offset + 8 + (if (shortRecord) 8 else 18) + remainingData!!.size

        listener.afterRecordSerialize(pos, recordId, pos - offset, this)
        return pos - offset
    }

    override val recordSize: Int
        get() {
        return 8 + (if (shortRecord) 8 else 18) + (if (remainingData == null) 0 else remainingData!!.size)
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
        return "ClientAnchor"
    }

    /**
     * Returns the string representation for this record.
     * 
     * @return A string
     */
    override fun toString(): String {
        val nl = System.getProperty("line.separator")

        var extraData: String?
        val b = ByteArrayOutputStream()
        try {
            HexDump.dump(this.remainingData!!, 0, b, 0)
            extraData = b.toString()
        } catch (e: Exception) {
            extraData = "error\n"
        }
        return javaClass.getName() + ":" + nl +
                "  RecordId: 0x" + toHex(RECORD_ID) + nl +
                "  Options: 0x" + toHex(options) + nl +
                "  Flag: " + this.flag + nl +
                "  Col1: " + this.col1 + nl +
                "  DX1: " + this.dx1 + nl +
                "  Row1: " + this.row1 + nl +
                "  DY1: " + field_5_dy1 + nl +
                "  Col2: " + field_6_col2 + nl +
                "  DX2: " + field_7_dx2 + nl +
                "  Row2: " + field_8_row2 + nl +
                "  DY2: " + field_9_dy2 + nl +
                "  Extra Data:" + nl + extraData
    }

    var dy1: Short
        /**
         * The y offset within the top-left corner of the current shape.
         */
        get() = field_5_dy1
        /**
         * The y offset within the top-left corner of the current shape.
         */
        set(field_5_dy1) {
            shortRecord = false
            this.field_5_dy1 = field_5_dy1
        }

    var col2: Short
        /**
         * The column of the bottom right corner of this shape.
         */
        get() = field_6_col2
        /**
         * The column of the bottom right corner of this shape.
         */
        set(field_6_col2) {
            shortRecord = false
            this.field_6_col2 = field_6_col2
        }

    var dx2: Short
        /**
         * The x offset withing the cell for the bottom-right corner of this shape.
         */
        get() = field_7_dx2
        /**
         * The x offset withing the cell for the bottom-right corner of this shape.
         */
        set(field_7_dx2) {
            shortRecord = false
            this.field_7_dx2 = field_7_dx2
        }

    var row2: Short
        /**
         * The row number for the bottom-right corner of the current shape.
         */
        get() = field_8_row2
        /**
         * The row number for the bottom-right corner of the current shape.
         */
        set(field_8_row2) {
            shortRecord = false
            this.field_8_row2 = field_8_row2
        }

    var dy2: Short
        /**
         * The y offset withing the cell for the bottom-right corner of this shape.
         */
        get() = field_9_dy2
        /**
         * The y offset withing the cell for the bottom-right corner of this shape.
         */
        set(field_9_dy2) {
            shortRecord = false
            this.field_9_dy2 = field_9_dy2
        }

    /**
     * 
     * 
     */
    override fun dispose() {
        remainingData = null
    }

    companion object {
        @JvmField
        val RECORD_ID: Short = 0xF010.toShort()
        const val RECORD_DESCRIPTION: String = "MsofbtClientAnchor"
    }
}
