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
 * Adapts a plain byte array to [LittleEndianOutput]
 *
 *
 * @author Josh Micich
 */
class LittleEndianByteArrayOutputStream(buf: ByteArray, startOffset: Int, maxWriteLen: Int) : LittleEndianOutput,
    DelayableLittleEndianOutput {
    private val _buf: ByteArray
    private val _endIndex: Int
    private var _writeIndex: Int

    init {
        if (startOffset < 0 || startOffset > buf.size) {
            throw IllegalArgumentException("Specified startOffset (" + startOffset
                + ") is out of allowable range (0.." + buf.size + ")")
        }
        _buf = buf
        _writeIndex = startOffset
        _endIndex = startOffset + maxWriteLen
        if (_endIndex < startOffset || _endIndex > buf.size) {
            throw IllegalArgumentException("calculated end index (" + _endIndex
                + ") is out of allowable range (" + _writeIndex + ".." + buf.size + ")")
        }
    }

    constructor(buf: ByteArray, startOffset: Int) : this(buf, startOffset, buf.size - startOffset)

    private fun checkPosition(i: Int) {
        if (i > _endIndex - _writeIndex) {
            throw RuntimeException("Buffer overrun")
        }
    }

    override fun writeByte(v: Int) {
        checkPosition(1)
        _buf[_writeIndex++] = v.toByte()
    }

    override fun writeDouble(v: Double) {
        writeLong(java.lang.Double.doubleToLongBits(v))
    }

    override fun writeInt(v: Int) {
        checkPosition(4)
        var i = _writeIndex
        _buf[i++] = ((v ushr 0) and 0xFF).toByte()
        _buf[i++] = ((v ushr 8) and 0xFF).toByte()
        _buf[i++] = ((v ushr 16) and 0xFF).toByte()
        _buf[i++] = ((v ushr 24) and 0xFF).toByte()
        _writeIndex = i
    }

    override fun writeLong(v: Long) {
        writeInt((v shr 0).toInt())
        writeInt((v shr 32).toInt())
    }

    override fun writeShort(v: Int) {
        checkPosition(2)
        var i = _writeIndex
        _buf[i++] = ((v ushr 0) and 0xFF).toByte()
        _buf[i++] = ((v ushr 8) and 0xFF).toByte()
        _writeIndex = i
    }

    override fun write(b: ByteArray) {
        val len = b.size
        checkPosition(len)
        System.arraycopy(b, 0, _buf, _writeIndex, len)
        _writeIndex += len
    }

    override fun write(b: ByteArray, offset: Int, len: Int) {
        checkPosition(len)
        System.arraycopy(b, offset, _buf, _writeIndex, len)
        _writeIndex += len
    }

    fun getWriteIndex(): Int {
        return _writeIndex
    }

    override fun createDelayedOutput(size: Int): LittleEndianOutput {
        checkPosition(size)
        val result: LittleEndianOutput = LittleEndianByteArrayOutputStream(_buf, _writeIndex, size)
        _writeIndex += size
        return result
    }
}
