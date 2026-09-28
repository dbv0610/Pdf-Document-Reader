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
package com.wxiwei.office.fc.hssf.record

import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * DrawingRecord (0x00EC)
 *
 *
 * 
 */
class DrawingRecord : StandardRecord {
    private var recordData: ByteArray
    private var contd: ByteArray? = null

    constructor() {
        recordData = EMPTY_BYTE_ARRAY
    }

    constructor(`in`: RecordInputStream) {
        recordData = `in`.readRemainder()
    }

    fun processContinueRecord(record: ByteArray) {
        //don't merge continue record with the drawing record, it must be serialized separately
        if (contd == null) {
            contd = record
        } else {
            // DrawingRecord followed by more than one ContinueRecords, 
            //like DrawingRecord, ContinueRecord, ContinueRecord...
            val newBuffer = ByteArray(contd!!.size + record.size)
            System.arraycopy(contd, 0, newBuffer, 0, contd!!.size)
            System.arraycopy(record, 0, newBuffer, contd!!.size, record.size)

            contd = newBuffer
        }
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.write(recordData)
    }

    override fun getDataSize(): Int {
        return recordData.size
    }

    fun getDataLength(): Int {
        if (contd != null) {
            return recordData.size + contd!!.size
        } else {
            return recordData.size
        }
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    fun getData(): ByteArray {
        if (contd != null) {
            val newBuffer = ByteArray(recordData.size + contd!!.size)
            System.arraycopy(recordData, 0, newBuffer, 0, recordData.size)
            System.arraycopy(contd, 0, newBuffer, recordData.size, contd!!.size)
            return newBuffer
        }
        return recordData
    }

    fun setData(thedata: ByteArray) {
        requireNotNull(thedata) { "data must not be null" }
        recordData = thedata
    }

    override fun clone(): Any {
        val rec = DrawingRecord()

        rec.recordData = recordData.clone()
        if (contd != null) {
            // TODO - this code probably never executes
            rec.contd = contd!!.clone()
        }

        return rec
    }

    companion object {
        const val sid: Short = 0x00EC

        private val EMPTY_BYTE_ARRAY = byteArrayOf()
    }
}
