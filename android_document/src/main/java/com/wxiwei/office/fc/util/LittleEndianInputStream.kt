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

import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream

/**
 * Wraps an [InputStream] providing [LittleEndianInput]<p/>
 *
 * This class does not buffer any input, so the stream read position maintained
 * by this class is consistent with that of the inner stream.
 *
 * @author Josh Micich
 */
open class LittleEndianInputStream(`is`: InputStream?) : FilterInputStream(`is`), LittleEndianInput {
    override fun available(): Int {
        try {
            return super.available()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    override fun readByte(): Byte {
        return readUByte().toByte()
    }

    override fun readUByte(): Int {
        val ch: Int
        try {
            ch = `in`.read()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        checkEOF(ch)
        return ch
    }

    override fun readDouble(): Double {
        return java.lang.Double.longBitsToDouble(readLong())
    }

    override fun readInt(): Int {
        val ch1: Int
        val ch2: Int
        val ch3: Int
        val ch4: Int
        try {
            ch1 = `in`.read()
            ch2 = `in`.read()
            ch3 = `in`.read()
            ch4 = `in`.read()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        checkEOF(ch1 or ch2 or ch3 or ch4)
        return (ch4 shl 24) + (ch3 shl 16) + (ch2 shl 8) + (ch1 shl 0)
    }

    override fun readLong(): Long {
        val b0: Int
        val b1: Int
        val b2: Int
        val b3: Int
        val b4: Int
        val b5: Int
        val b6: Int
        val b7: Int
        try {
            b0 = `in`.read()
            b1 = `in`.read()
            b2 = `in`.read()
            b3 = `in`.read()
            b4 = `in`.read()
            b5 = `in`.read()
            b6 = `in`.read()
            b7 = `in`.read()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        checkEOF(b0 or b1 or b2 or b3 or b4 or b5 or b6 or b7)
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

    override fun readUShort(): Int {
        val ch1: Int
        val ch2: Int
        try {
            ch1 = `in`.read()
            ch2 = `in`.read()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        checkEOF(ch1 or ch2)
        return (ch2 shl 8) + (ch1 shl 0)
    }

    override fun readFully(buf: ByteArray) {
        readFully(buf, 0, buf.size)
    }

    override fun readFully(buf: ByteArray, off: Int, len: Int) {
        val max = off + len
        for (i in off until max) {
            val ch: Int
            try {
                ch = `in`.read()
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
            checkEOF(ch)
            buf[i] = ch.toByte()
        }
    }

    companion object {
        private fun checkEOF(value: Int) {
            if (value < 0) {
                throw RuntimeException("Unexpected end-of-file")
            }
        }
    }
}
