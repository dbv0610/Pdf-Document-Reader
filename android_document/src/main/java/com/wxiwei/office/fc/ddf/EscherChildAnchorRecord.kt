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

/**
 * The escher child achor record is used to specify the position of a shape under an
 * existing group.  The first level of shape records use a EscherClientAnchor record instead.
 * 
 * @author Glen Stampoultzis
 * @see EscherChildAnchorRecord
 */
class EscherChildAnchorRecord

    : EscherRecord() {
    /**
     * Retrieves offset within the parent coordinate space for the top left point.
     */
    /**
     * Sets offset within the parent coordinate space for the top left point.
     */
    var dx1: Int = 0
    /**
     * Gets offset within the parent coordinate space for the top left point.
     */
    /**
     * Sets offset within the parent coordinate space for the top left point.
     */
    var dy1: Int = 0
    /**
     * Retrieves offset within the parent coordinate space for the bottom right point.
     */
    /**
     * Sets offset within the parent coordinate space for the bottom right point.
     */
    var dx2: Int = 0
    /**
     * Gets the offset within the parent coordinate space for the bottom right point.
     */
    /**
     * Sets the offset within the parent coordinate space for the bottom right point.
     */
    var dy2: Int = 0

    override fun fillFields(
        data: ByteArray?,
        offset: Int,
        recordFactory: EscherRecordFactory?
    ): Int {
        val data = data!!
        val bytesRemaining = readHeader(data, offset)
        val pos = offset + 8
        var size = 0
        this.dx1 = getInt(data, pos + size)
        size += 4
        this.dy1 = getInt(data, pos + size)
        size += 4
        this.dx2 = getInt(data, pos + size)
        size += 4
        this.dy2 = getInt(data, pos + size)
        size += 4
        return 8 + size
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
        putInt(data, pos, recordSize - 8)
        pos += 4
        putInt(data, pos, this.dx1)
        pos += 4
        putInt(data, pos, this.dy1)
        pos += 4
        putInt(data, pos, this.dx2)
        pos += 4
        putInt(data, pos, this.dy2)
        pos += 4

        listener.afterRecordSerialize(pos, recordId, pos - offset, this)
        return pos - offset
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
        return "ChildAnchor"
    }


    /**
     * The string representation of this record
     */
    override fun toString(): String {
        val nl = System.getProperty("line.separator")

        return javaClass.getName() + ":" + nl +
                "  RecordId: 0x" + toHex(RECORD_ID) + nl +
                "  Options: 0x" + toHex(options) + nl +
                "  X1: " + this.dx1 + nl +
                "  Y1: " + this.dy1 + nl +
                "  X2: " + this.dx2 + nl +
                "  Y2: " + this.dy2 + nl
    }

    /**
     * 
     * 
     */
    override fun dispose() {
    }

    companion object {
        @JvmField
        val RECORD_ID: Short = 0xF00F.toShort()
        const val RECORD_DESCRIPTION: String = "MsofbtChildAnchor"
    }
}
