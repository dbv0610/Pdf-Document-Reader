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
package com.wxiwei.office.fc.hssf.record.crypto

import com.wxiwei.office.fc.hssf.record.BOFRecord
import com.wxiwei.office.fc.hssf.record.FilePassRecord
import com.wxiwei.office.fc.hssf.record.InterfaceHdrRecord

/**
 * Used for both encrypting and decrypting BIFF8 streams. The internal
 * [RC4] instance is renewed (re-keyed) every 1024 bytes.
 * 
 * @author Josh Micich
 */
internal class Biff8RC4(initialOffset: Int, key: Biff8EncryptionKey) {
    private var _rc4: RC4? = null

    /**
     * This field is used to keep track of when to change the [RC4]
     * instance. The change occurs every 1024 bytes. Every byte passed over is
     * counted.
     */
    private var _streamPos: Int
    private var _nextRC4BlockStart = 0
    private var _currentKeyIndex = 0
    private var _shouldSkipEncryptionOnCurrentRecord: Boolean

    private val _key: Biff8EncryptionKey

    init {
        if (initialOffset >= RC4_REKEYING_INTERVAL) {
            throw RuntimeException(
                ("initialOffset (" + initialOffset + ")>"
                        + RC4_REKEYING_INTERVAL + " not supported yet")
            )
        }
        _key = key
        _streamPos = 0
        rekeyForNextBlock()
        _streamPos = initialOffset
        for (i in initialOffset downTo 1) {
            _rc4!!.output()
        }
        _shouldSkipEncryptionOnCurrentRecord = false
    }

    private fun rekeyForNextBlock() {
        _currentKeyIndex = _streamPos / RC4_REKEYING_INTERVAL
        _rc4 = _key.createRC4(_currentKeyIndex)
        _nextRC4BlockStart = (_currentKeyIndex + 1) * RC4_REKEYING_INTERVAL
    }

    private val nextRC4Byte: Int
        get() {
            if (_streamPos >= _nextRC4BlockStart) {
                rekeyForNextBlock()
            }
            val mask = _rc4!!.output()
            _streamPos++
            if (_shouldSkipEncryptionOnCurrentRecord) {
                return 0
            }
            return mask.toInt() and 0xFF
        }

    fun startRecord(currentSid: Int) {
        _shouldSkipEncryptionOnCurrentRecord = isNeverEncryptedRecord(currentSid)
    }

    /**
     * Used when BIFF header fields (sid, size) are being read. The internal
     * [RC4] instance must step even when unencrypted bytes are read
     */
    fun skipTwoBytes() {
        this.nextRC4Byte
        this.nextRC4Byte
    }

    fun xor(buf: ByteArray?, pOffset: Int, pLen: Int) {
        val nLeftInBlock: Int
        nLeftInBlock = _nextRC4BlockStart - _streamPos
        if (pLen <= nLeftInBlock) {
            // simple case - this read does not cross key blocks
            _rc4!!.encrypt(buf!!, pOffset, pLen)
            _streamPos += pLen
            return
        }

        var offset = pOffset
        var len = pLen

        // start by using the rest of the current block
        if (len > nLeftInBlock) {
            if (nLeftInBlock > 0) {
                _rc4!!.encrypt(buf!!, offset, nLeftInBlock)
                _streamPos += nLeftInBlock
                offset += nLeftInBlock
                len -= nLeftInBlock
            }
            rekeyForNextBlock()
        }
        // all full blocks following
        while (len > RC4_REKEYING_INTERVAL) {
            _rc4!!.encrypt(buf!!, offset, RC4_REKEYING_INTERVAL)
            _streamPos += RC4_REKEYING_INTERVAL
            offset += RC4_REKEYING_INTERVAL
            len -= RC4_REKEYING_INTERVAL
            rekeyForNextBlock()
        }
        // finish with incomplete block
        _rc4!!.encrypt(buf!!, offset, len)
        _streamPos += len
    }

    fun xorByte(rawVal: Int): Int {
        val mask = this.nextRC4Byte
        return (rawVal xor mask).toByte().toInt()
    }

    fun xorShort(rawVal: Int): Int {
        val b0 = this.nextRC4Byte
        val b1 = this.nextRC4Byte
        val mask = (b1 shl 8) + (b0 shl 0)
        return rawVal xor mask
    }

    fun xorInt(rawVal: Int): Int {
        val b0 = this.nextRC4Byte
        val b1 = this.nextRC4Byte
        val b2 = this.nextRC4Byte
        val b3 = this.nextRC4Byte
        val mask = (b3 shl 24) + (b2 shl 16) + (b1 shl 8) + (b0 shl 0)
        return rawVal xor mask
    }

    fun xorLong(rawVal: Long): Long {
        val b0 = this.nextRC4Byte
        val b1 = this.nextRC4Byte
        val b2 = this.nextRC4Byte
        val b3 = this.nextRC4Byte
        val b4 = this.nextRC4Byte
        val b5 = this.nextRC4Byte
        val b6 = this.nextRC4Byte
        val b7 = this.nextRC4Byte
        val mask =
            (((b7.toLong()) shl 56)
                    + ((b6.toLong()) shl 48)
                    + ((b5.toLong()) shl 40)
                    + ((b4.toLong()) shl 32)
                    + ((b3.toLong()) shl 24)
                    + (b2 shl 16)
                    + (b1 shl 8)
                    + (b0 shl 0))
        return rawVal xor mask
    }

    companion object {
        private const val RC4_REKEYING_INTERVAL = 1024

        /**
         * TODO: Additionally, the lbPlyPos (position_of_BOF) field of the BoundSheet8 record MUST NOT be encrypted.
         * 
         * @return `true` if record type specified by <tt>sid</tt> is never encrypted
         */
        private fun isNeverEncryptedRecord(sid: Int): Boolean {
            when (sid.toShort()) {
                BOFRecord.sid, InterfaceHdrRecord.sid, FilePassRecord.sid ->                // this only really counts when writing because FILEPASS is read early

                    // UsrExcl(0x0194)
                    // FileLock
                    // RRDInfo(0x0196)
                    // RRDHead(0x0138)
                    return true
            }
            return false
        }
    }
}
