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

package com.wxiwei.office.fc.util

/**
 * Adapts a plain byte array to [LittleEndianInput]
 *
 * @author Josh Micich
 */
class LittleEndianByteArrayInputStream(buf: ByteArray, startOffset: Int, maxReadLen: Int) : LittleEndianInput {
    private val _buf: ByteArray = buf
    private val _endIndex: Int = startOffset + maxReadLen
    private var _readIndex: Int = startOffset

    constructor(buf: ByteArray, startOffset: Int) : this(buf, startOffset, buf.size - startOffset)

    constructor(buf: ByteArray) : this(buf, 0, buf.size)

    override fun available(): Int {
        return _endIndex - _readIndex
    }

    private fun checkPosition(i: Int) {
        if (i > _endIndex - _readIndex) {
            throw RuntimeException("Buffer overrun")
        }
    }

    fun getReadIndex(): Int {
        return _readIndex
    }

    override fun readByte(): Byte {
        checkPosition(1)
        return _buf[_readIndex++]
    }

    override fun readInt(): Int {
        checkPosition(4)
        var i = _readIndex

        val b0 = _buf[i++].toInt() and 0xFF
        val b1 = _buf[i++].toInt() and 0xFF
        val b2 = _buf[i++].toInt() and 0xFF
        val b3 = _buf[i++].toInt() and 0xFF
        _readIndex = i
        return (b3 shl 24) + (b2 shl 16) + (b1 shl 8) + (b0 shl 0)
    }

    override fun readLong(): Long {
        checkPosition(8)
        var i = _readIndex

        val b0 = _buf[i++].toInt() and 0xFF
        val b1 = _buf[i++].toInt() and 0xFF
        val b2 = _buf[i++].toInt() and 0xFF
        val b3 = _buf[i++].toInt() and 0xFF
        val b4 = _buf[i++].toInt() and 0xFF
        val b5 = _buf[i++].toInt() and 0xFF
        val b6 = _buf[i++].toInt() and 0xFF
        val b7 = _buf[i++].toInt() and 0xFF
        _readIndex = i
        return ((b7.toLong() shl 56) +
                (b6.toLong() shl 48) +
                (b5.toLong() shl 40) +
                (b4.toLong() shl 32) +
                (b3.toLong() shl 24) +
                (b2 shl 16) +
                (b1 shl 8) +
                (b0 shl 0))
    }

    override fun readShort(): Short {
        return readUShort().toShort()
    }

    override fun readUByte(): Int {
        checkPosition(1)
        return _buf[_readIndex++].toInt() and 0xFF
    }

    override fun readUShort(): Int {
        checkPosition(2)
        var i = _readIndex

        val b0 = _buf[i++].toInt() and 0xFF
        val b1 = _buf[i++].toInt() and 0xFF
        _readIndex = i
        return (b1 shl 8) + (b0 shl 0)
    }

    override fun readFully(buf: ByteArray, off: Int, len: Int) {
        checkPosition(len)
        System.arraycopy(_buf, _readIndex, buf, off, len)
        _readIndex += len
    }

    override fun readFully(buf: ByteArray) {
        readFully(buf, 0, buf.size)
    }

    override fun readDouble(): Double {
        return java.lang.Double.longBitsToDouble(readLong())
    }
}
