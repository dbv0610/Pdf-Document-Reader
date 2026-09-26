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
package com.wxiwei.office.fc.hssf.record.crypto

import com.wxiwei.office.fc.hssf.record.BiffHeaderInput
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianInputStream
import java.io.InputStream
import kotlin.Byte
import kotlin.ByteArray
import kotlin.Int
import kotlin.Long
import kotlin.RuntimeException
import kotlin.Short

/**
 * 
 * @author Josh Micich
 */
class Biff8DecryptingStream(`in`: InputStream?, initialOffset: Int, key: Biff8EncryptionKey?) :
    BiffHeaderInput, LittleEndianInput {
    private val _le: LittleEndianInput
    private val _rc4: Biff8RC4

    init {
        _rc4 = Biff8RC4(initialOffset, key!!)

        if (`in` is LittleEndianInput) {
            // accessing directly is an optimisation
            _le = `in` as LittleEndianInput
        } else {
            // less optimal, but should work OK just the same. Often occurs in junit tests.
            _le = LittleEndianInputStream(`in`)
        }
    }

    override fun available(): Int {
        return _le.available()
    }

    /**
     * Reads an unsigned short value without decrypting
     */
    override fun readRecordSID(): Int {
        val sid = _le.readUShort()
        _rc4.skipTwoBytes()
        _rc4.startRecord(sid)
        return sid
    }

    /**
     * Reads an unsigned short value without decrypting
     */
    override fun readDataSize(): Int {
        val dataSize = _le.readUShort()
        _rc4.skipTwoBytes()
        return dataSize
    }

    override fun readDouble(): Double {
        val valueLongBits = readLong()
        val result = java.lang.Double.longBitsToDouble(valueLongBits)
        if (result.isNaN()) {
            throw RuntimeException("Did not expect to read NaN") // (Because Excel typically doesn't write NaN
        }
        return result
    }

    override fun readFully(buf: ByteArray) {
        readFully(buf, 0, buf.size)
    }

    override fun readFully(buf: ByteArray, off: Int, len: Int) {
        _le.readFully(buf, off, len)
        _rc4.xor(buf, off, len)
    }


    override fun readUByte(): Int {
        return _rc4.xorByte(_le.readUByte())
    }

    override fun readByte(): Byte {
        return _rc4.xorByte(_le.readUByte()).toByte()
    }


    override fun readUShort(): Int {
        return _rc4.xorShort(_le.readUShort())
    }

    override fun readShort(): Short {
        return _rc4.xorShort(_le.readUShort()).toShort()
    }

    override fun readInt(): Int {
        return _rc4.xorInt(_le.readInt())
    }

    override fun readLong(): Long {
        return _rc4.xorLong(_le.readLong())
    }
}
