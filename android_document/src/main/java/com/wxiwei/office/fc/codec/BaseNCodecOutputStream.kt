/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wxiwei.office.fc.codec

import java.io.FilterOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * Abstract superclass for Base-N output streams.
 * 
 * @since 1.5
 */
open class BaseNCodecOutputStream(
    out: OutputStream?,
    private val baseNCodec: BaseNCodec,
    private val doEncode: Boolean
) : FilterOutputStream(out) {
    private val singleByte = ByteArray(1)

    /**
     * Writes the specified `byte` to this output stream.
     * 
     * @param i
     * source byte
     * @throws IOException
     * if an I/O error occurs.
     */
    @Throws(IOException::class)
    override fun write(i: Int) {
        singleByte[0] = i.toByte()
        write(singleByte, 0, 1)
    }

    /**
     * Writes `len` bytes from the specified `b` array starting at `offset` to this
     * output stream.
     * 
     * @param b
     * source byte array
     * @param offset
     * where to start reading the bytes
     * @param len
     * maximum number of bytes to write
     * 
     * @throws IOException
     * if an I/O error occurs.
     * @throws NullPointerException
     * if the byte array parameter is null
     * @throws IndexOutOfBoundsException
     * if offset, len or buffer size are invalid
     */
    @Throws(IOException::class)
    override fun write(b: ByteArray?, offset: Int, len: Int) {
        if (b == null) {
            throw NullPointerException()
        } else if (offset < 0 || len < 0) {
            throw IndexOutOfBoundsException()
        } else if (offset > b.size || offset + len > b.size) {
            throw IndexOutOfBoundsException()
        } else if (len > 0) {
            if (doEncode) {
                baseNCodec.encode(b, offset, len)
            } else {
                baseNCodec.decode(b, offset, len)
            }
            flush(false)
        }
    }

    /**
     * Flushes this output stream and forces any buffered output bytes to be written out to the stream. If propogate is
     * true, the wrapped stream will also be flushed.
     * 
     * @param propogate
     * boolean flag to indicate whether the wrapped OutputStream should also be flushed.
     * @throws IOException
     * if an I/O error occurs.
     */
    @Throws(IOException::class)
    private fun flush(propogate: Boolean) {
        val avail = baseNCodec.available()
        if (avail > 0) {
            val buf = ByteArray(avail)
            val c = baseNCodec.readResults(buf, 0, avail)
            if (c > 0) {
                out.write(buf, 0, c)
            }
        }
        if (propogate) {
            out.flush()
        }
    }

    /**
     * Flushes this output stream and forces any buffered output bytes to be written out to the stream.
     * 
     * @throws IOException
     * if an I/O error occurs.
     */
    @Throws(IOException::class)
    override fun flush() {
        flush(true)
    }

    /**
     * Closes this output stream and releases any system resources associated with the stream.
     * 
     * @throws IOException
     * if an I/O error occurs.
     */
    @Throws(IOException::class)
    override fun close() {
        // Notify encoder of EOF (-1).
        if (doEncode) {
            baseNCodec.encode(singleByte, 0, -1)
        } else {
            baseNCodec.decode(singleByte, 0, -1)
        }
        flush()
        out.close()
    }
}
