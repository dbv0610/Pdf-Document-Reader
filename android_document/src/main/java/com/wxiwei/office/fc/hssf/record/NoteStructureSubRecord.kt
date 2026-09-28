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

import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * ftNts (0x000D)
 *
 *
 * Represents a NoteStructure sub record.
 * 
 * 
 * 
 * The docs say nothing about it. The length of this record is always 26 bytes.
 * 
 * 
 * @author Yegor Kozlov
 */
class NoteStructureSubRecord : SubRecord {
    private var reserved: ByteArray

    /**
     * Construct a new `NoteStructureSubRecord` and
     * fill its data with the default values
     */
    constructor() {
        //all we know is that the the length of <code>NoteStructureSubRecord</code> is always 22 bytes
        reserved = ByteArray(ENCODED_SIZE)
    }

    /**
     * Read the record data from the supplied `RecordInputStream`
     */
    constructor(`in`: LittleEndianInput, size: Int) {
        if (size != ENCODED_SIZE) {
            throw RecordFormatException("Unexpected size (" + size + ")")
        }
        //just grab the raw data
        val buf = ByteArray(size)
        `in`.readFully(buf)
        reserved = buf
    }

    /**
     * Convert this record to string.
     * Used by BiffViewer and other utilities.
     */
    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[ftNts ]").append("\n")
        buffer.append("  size     = ").append(getDataSize()).append("\n")
        buffer.append("  reserved = ").append(toHex(reserved)).append("\n")
        buffer.append("[/ftNts ]").append("\n")
        return buffer.toString()
    }

    /**
     * Serialize the record data into the supplied array of bytes
     * 
     * @param out the stream to serialize into
     */
    override fun serialize(out: LittleEndianOutput) {
        out.writeShort(sid.toInt())
        out.writeShort(reserved.size)
        out.write(reserved)
    }

    override fun getDataSize(): Int {
        return reserved.size
    }

    /**
     * @return id of this record.
     */
    fun getSid(): Short {
        return sid
    }

    override fun clone(): Any {
        val rec = NoteStructureSubRecord()
        val recdata = ByteArray(reserved.size)
        System.arraycopy(reserved, 0, recdata, 0, recdata.size)
        rec.reserved = recdata
        return rec
    }

    companion object {
        const val sid: Short = 0x0D
        private const val ENCODED_SIZE = 22
    }
}


