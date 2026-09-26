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
import com.wxiwei.office.fc.util.LittleEndian.putInt
import com.wxiwei.office.fc.util.LittleEndian.putShort
import java.io.ByteArrayOutputStream


/**
 * The EscherClientDataRecord is used to store client specific data about the position of a
 * shape within a container.
 * 
 * @author Glen Stampoultzis
 */
class EscherClientDataRecord

    : EscherRecord() {
    /**
     * Any data recording this record.
     */
    /**
     * Any data recording this record.
     */
    @JvmField
    var remainingData: ByteArray? = null

    override fun fillFields(
        data: ByteArray?,
        offset: Int,
        recordFactory: EscherRecordFactory?
    ): Int {
        val data = data!!
        val bytesRemaining = readHeader(data, offset)
        val pos = offset + 8
        remainingData = ByteArray(bytesRemaining)
        System.arraycopy(data, pos, remainingData, 0, bytesRemaining)
        return 8 + bytesRemaining
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
        putInt(data, offset + 4, remainingData!!.size)
        System.arraycopy(remainingData, 0, data, offset + 8, remainingData!!.size)
        val pos = offset + 8 + remainingData!!.size

        listener.afterRecordSerialize(pos, recordId, pos - offset, this)
        return pos - offset
    }

    override val recordSize: Int
        get() {
        return 8 + (if (remainingData == null) 0 else remainingData!!.size)
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
        return "ClientData"
    }

    /**
     * Returns the string representation of this record.
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
                "  Extra Data:" + nl +
                extraData
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
        val RECORD_ID: Short = 0xF011.toShort()
        const val RECORD_DESCRIPTION: String = "MsofbtClientData"
    }
}
