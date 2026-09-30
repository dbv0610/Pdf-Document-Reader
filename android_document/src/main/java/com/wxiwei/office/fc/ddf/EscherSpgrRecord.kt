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
 * The spgr record defines information about a shape group.  Groups in escher
 * are simply another form of shape that you can't physically see.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class EscherSpgrRecord

    : EscherRecord() {
    /**
     * The starting top-left coordinate of child records.
     */
    /**
     * The starting top-left coordinate of child records.
     */
    var rectX1: Int = 0
    /**
     * The starting top-left coordinate of child records.
     */
    /**
     * The starting top-left coordinate of child records.
     */
    var rectY1: Int = 0
    /**
     * The starting bottom-right coordinate of child records.
     */
    /**
     * The starting bottom-right coordinate of child records.
     */
    var rectX2: Int = 0
    /**
     * The starting bottom-right coordinate of child records.
     */
    /**
     * The starting bottom-right coordinate of child records.
     */
    var rectY2: Int = 0

    override fun fillFields(
        data: ByteArray?,
        offset: Int,
        recordFactory: EscherRecordFactory?
    ): Int {
        val data = data!!
        var bytesRemaining = readHeader(data, offset)
        val pos = offset + 8
        var size = 0
        this.rectX1 = getInt(data, pos + size)
        size += 4
        this.rectY1 = getInt(data, pos + size)
        size += 4
        this.rectX2 = getInt(data, pos + size)
        size += 4
        this.rectY2 = getInt(data, pos + size)
        size += 4
        bytesRemaining -= size
        if (bytesRemaining != 0) throw RecordFormatException("Expected no remaining bytes but got " + bytesRemaining)
        //        remainingData  =  new byte[bytesRemaining];
//        System.arraycopy( data, pos + size, remainingData, 0, bytesRemaining );
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
        putShort(data, offset, options)
        putShort(data, offset + 2, recordId)
        val remainingBytes = 16
        putInt(data, offset + 4, remainingBytes)
        putInt(data, offset + 8, this.rectX1)
        putInt(data, offset + 12, this.rectY1)
        putInt(data, offset + 16, this.rectX2)
        putInt(data, offset + 20, this.rectY2)
        //        System.arraycopy( remainingData, 0, data, offset + 26, remainingData.length );
//        int pos = offset + 8 + 18 + remainingData.length;
        listener.afterRecordSerialize(
            offset + recordSize,
            recordId,
            offset + recordSize,
            this
        )
        return 8 + 16
    }

    override val recordSize: Int
        get() {
        return 8 + 16
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
        return "Spgr"
    }

    /**
     * @return  the string representation of this record.
     */
    override fun toString(): String {
        return javaClass.getName() + ":" + '\n' +
                "  RecordId: 0x" + toHex(RECORD_ID) + '\n' +
                "  Options: 0x" + toHex(options) + '\n' +
                "  RectX: " + this.rectX1 + '\n' +
                "  RectY: " + this.rectY1 + '\n' +
                "  RectWidth: " + this.rectX2 + '\n' +
                "  RectHeight: " + this.rectY2 + '\n'
    }

    /**
     * 
     * 
     */
    override fun dispose() {
    }

    companion object {
        @JvmField
        val RECORD_ID: Short = 0xF009.toShort()
        const val RECORD_DESCRIPTION: String = "MsofbtSpgr"
    }
}
