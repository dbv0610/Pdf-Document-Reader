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
 * This structure appears as part of an Obj record that represents a checkbox or radio button.
 * 
 * @author Yegor Kozlov
 */
class FtCblsSubRecord : SubRecord {
    private var reserved: ByteArray

    /**
     * Construct a new `FtCblsSubRecord` and
     * fill its data with the default values
     */
    constructor() {
        reserved = ByteArray(ENCODED_SIZE)
    }

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

        buffer.append("[FtCbls ]").append("\n")
        buffer.append("  size     = ").append(getDataSize()).append("\n")
        buffer.append("  reserved = ").append(toHex(reserved)).append("\n")
        buffer.append("[/FtCbls ]").append("\n")
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
        val rec = FtCblsSubRecord()
        val recdata = ByteArray(reserved.size)
        System.arraycopy(reserved, 0, recdata, 0, recdata.size)
        rec.reserved = recdata
        return rec
    }

    companion object {
        const val sid: Short = 0x0C
        private const val ENCODED_SIZE = 20
    }
}
