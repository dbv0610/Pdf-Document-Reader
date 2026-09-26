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

import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Title: File Pass Record (0x002F) 
 *
 *
 * 
 * Description: Indicates that the record after this record are encrypted.
 * 
 * @author Jason Height (jheight at chariot dot net dot au)
 */
class FilePassRecord(`in`: RecordInputStream) : StandardRecord() {
    private val _encryptionType: Int
    private val _encryptionInfo: Int
    private var _minorVersionNo: Int
    private var _docId: ByteArray
    private var _saltData: ByteArray
    private var _saltHash: ByteArray

    init {
        _encryptionType = `in`.readUShort()

        when (_encryptionType) {
            ENCRYPTION_XOR -> throw RecordFormatException("HSSF does not currently support XOR obfuscation")
            ENCRYPTION_OTHER -> {}
            else -> throw RecordFormatException("Unknown encryption type " + _encryptionType)
        }
        _encryptionInfo = `in`.readUShort()
        when (_encryptionInfo) {
            ENCRYPTION_OTHER_RC4 -> {}
            ENCRYPTION_OTHER_CAPI_2, ENCRYPTION_OTHER_CAPI_3 -> throw RecordFormatException(
                "HSSF does not currently support CryptoAPI encryption"
            )

            else -> throw RecordFormatException("Unknown encryption info " + _encryptionInfo)
        }
        _minorVersionNo = `in`.readUShort()
        if (_minorVersionNo != 1) {
            throw RecordFormatException("Unexpected VersionInfo number for RC4Header " + _minorVersionNo)
        }
        _docId = read(`in`, 16)
        _saltData = read(`in`, 16)
        _saltHash = read(`in`, 16)
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(_encryptionType)
        out.writeShort(_encryptionInfo)
        out.writeShort(_minorVersionNo)
        out.write(_docId)
        out.write(_saltData)
        out.write(_saltHash)
    }

    override fun getDataSize(): Int {
        return 54
    }


    fun getDocId(): ByteArray {
        return _docId.clone()
    }

    fun setDocId(docId: ByteArray) {
        _docId = docId.clone()
    }

    fun getSaltData(): ByteArray {
        return _saltData.clone()
    }

    fun setSaltData(saltData: ByteArray) {
        _saltData = saltData.clone()
    }

    fun getSaltHash(): ByteArray {
        return _saltHash.clone()
    }

    fun setSaltHash(saltHash: ByteArray) {
        _saltHash = saltHash.clone()
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        // currently immutable
        return this
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[FILEPASS]\n")
        buffer.append("    .type = ").append(shortToHex(_encryptionType)).append("\n")
        buffer.append("    .info = ").append(shortToHex(_encryptionInfo)).append("\n")
        buffer.append("    .ver  = ").append(shortToHex(_minorVersionNo)).append("\n")
        buffer.append("    .docId= ").append(toHex(_docId)).append("\n")
        buffer.append("    .salt = ").append(toHex(_saltData)).append("\n")
        buffer.append("    .hash = ").append(toHex(_saltHash)).append("\n")
        buffer.append("[/FILEPASS]\n")
        return buffer.toString()
    }

    companion object {
        const val sid: Short = 0x002F
        private const val ENCRYPTION_XOR = 0
        private const val ENCRYPTION_OTHER = 1

        private const val ENCRYPTION_OTHER_RC4 = 1
        private const val ENCRYPTION_OTHER_CAPI_2 = 2
        private const val ENCRYPTION_OTHER_CAPI_3 = 3


        private fun read(`in`: RecordInputStream, size: Int): ByteArray {
            val result = ByteArray(size)
            `in`.readFully(result)
            return result
        }
    }
}
