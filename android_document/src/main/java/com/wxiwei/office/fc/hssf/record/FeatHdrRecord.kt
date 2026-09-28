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

import com.wxiwei.office.fc.hssf.record.common.FtrHeader
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * Title: FeatHdr (Feature Header) Record
 * <P>
 * This record specifies common information for Shared Features, and
 * specifies the beginning of a collection of records to define them.
 * The collection of data (Globals Substream ABNF, macro sheet substream
 * ABNF or worksheet substream ABNF) specifies Shared Feature data.
</P> */
class FeatHdrRecord : StandardRecord {
    private val futureHeader: FtrHeader
    private var isf_sharedFeatureType = 0 // See SHAREDFEATURES_
    private var reserved: Byte = 0 // Should always be one

    /**
     * 0x00000000 = rgbHdrData not present
     * 0xffffffff = rgbHdrData present
     */
    private var cbHdrData: Long = 0

    /** We need a BOFRecord to make sense of this...  */
    private var rgbHdrData: ByteArray = byteArrayOf()

    constructor() {
        futureHeader = FtrHeader()
        futureHeader.recordType = Companion.sid
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    constructor(`in`: RecordInputStream) {
        futureHeader = FtrHeader(`in`)

        isf_sharedFeatureType = `in`.readShort().toInt()
        reserved = `in`.readByte()
        cbHdrData = `in`.readInt().toLong()
        // Don't process this just yet, need the BOFRecord
        rgbHdrData = `in`.readRemainder()
    }

    override fun toString(): String {
        val buffer = StringBuffer()
        buffer.append("[FEATURE HEADER]\n")


        // TODO ...
        buffer.append("[/FEATURE HEADER]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        futureHeader.serialize(out)

        out.writeShort(isf_sharedFeatureType)
        out.writeByte(reserved.toInt())
        out.writeInt(cbHdrData.toInt())
        out.write(rgbHdrData)
    }

    override fun getDataSize(): Int {
        return 12 + 2 + 1 + 4 + rgbHdrData.size
    }

    //HACK: do a "cheat" clone, see Record.java for more information
    override fun clone(): Any {
        return cloneViaReserialise()
    }


    companion object {
        /**
         * Specifies the enhanced protection type. Used to protect a
         * shared workbook by restricting access to some areas of it
         */
        const val SHAREDFEATURES_ISFPROTECTION: Int = 0x02

        /**
         * Specifies that formula errors should be ignored
         */
        const val SHAREDFEATURES_ISFFEC2: Int = 0x03

        /**
         * Specifies the smart tag type. Recognises certain
         * types of entries (proper names, dates/times etc) and
         * flags them for action
         */
        const val SHAREDFEATURES_ISFFACTOID: Int = 0x04

        /**
         * Specifies the shared list type. Used for a table
         * within a sheet
         */
        const val SHAREDFEATURES_ISFLIST: Int = 0x05


        const val sid: Short = 0x0867
    }
}
