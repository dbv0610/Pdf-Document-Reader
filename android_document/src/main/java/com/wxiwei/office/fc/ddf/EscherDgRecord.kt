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
 * This record simply holds the number of shapes in the drawing group and the
 * last shape id used for this drawing group.
 * 
 * @author Glen Stampoultzis
 */
class EscherDgRecord

    : EscherRecord() {
    /**
     * The number of shapes in this drawing group.
     */
    /**
     * The number of shapes in this drawing group.
     */
    var numShapes: Int = 0
    /**
     * The last shape id used in this drawing group.
     */
    /**
     * The last shape id used in this drawing group.
     */
    var lastMSOSPID: Int = 0

    override fun fillFields(
        data: ByteArray?,
        offset: Int,
        recordFactory: EscherRecordFactory?
    ): Int {
        val data = data!!
        val bytesRemaining = readHeader(data, offset)
        val pos = offset + 8
        var size = 0
        this.numShapes = getInt(data, pos + size)
        size += 4
        this.lastMSOSPID = getInt(data, pos + size)
        size += 4
        //        bytesRemaining -= size;
//        remainingData  =  new byte[bytesRemaining];
//        System.arraycopy( data, pos + size, remainingData, 0, bytesRemaining );
        return recordSize
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
        putInt(data, offset + 4, 8)
        putInt(data, offset + 8, this.numShapes)
        putInt(data, offset + 12, this.lastMSOSPID)

        //        System.arraycopy( remainingData, 0, data, offset + 26, remainingData.length );
//        int pos = offset + 8 + 18 + remainingData.length;
        listener.afterRecordSerialize(offset + 16, recordId, recordSize, this)
        return recordSize
    }

    /**
     * Returns the number of bytes that are required to serialize this record.
     * 
     * @return Number of bytes
     */
    override val recordSize: Int
        get() {
        return 8 + 8
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
        return "Dg"
    }

    /**
     * Returns the string representation of this record.
     */
    override fun toString(): String {
        return javaClass.getName() + ":" + '\n' +
                "  RecordId: 0x" + toHex(RECORD_ID) + '\n' +
                "  Options: 0x" + toHex(options) + '\n' +
                "  NumShapes: " + this.numShapes + '\n' +
                "  LastMSOSPID: " + this.lastMSOSPID + '\n'
    }

    val drawingGroupId: Short
        /**
         * Gets the drawing group id for this record.  This is encoded in the
         * instance part of the option record.
         * 
         * @return  a drawing group id.
         */
        get() = (options.toInt() shr 4).toShort()

    fun incrementShapeCount() {
        this.numShapes++
    }

    /**
     * 
     * 
     */
    override fun dispose() {
    }

    companion object {
        @JvmField
        val RECORD_ID: Short = 0xF008.toShort()
        const val RECORD_DESCRIPTION: String = "MsofbtDg"
    }
}
