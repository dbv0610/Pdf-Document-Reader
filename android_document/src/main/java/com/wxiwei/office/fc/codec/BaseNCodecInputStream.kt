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

import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream

/**
 * Abstract superclass for Base-N input streams.
 * 
 * @since 1.5
 */
open class BaseNCodecInputStream protected constructor(
    `in`: InputStream?,
    private val baseNCodec: BaseNCodec,
    private val doEncode: Boolean
) : FilterInputStream(`in`) {
    private val singleByte = ByteArray(1)

    /**
     * Reads one `byte` from this input stream.
     * 
     * @return the byte as an integer in the range 0 to 255. Returns -1 if EOF has been reached.
     * @throws IOException
     * if an I/O error occurs.
     */
    @Throws(IOException::class)
    override fun read(): Int {
        var r = read(singleByte, 0, 1)
        while (r == 0) {
            r = read(singleByte, 0, 1)
        }
        if (r > 0) {
            return if (singleByte[0] < 0) 256 + singleByte[0] else singleByte[0].toInt()
        }
        return -1
    }

    /**
     * Attempts to read `len` bytes into the specified `b` array starting at `offset`
     * from this InputStream.
     * 
     * @param b
     * destination byte array
     * @param offset
     * where to start writing the bytes
     * @param len
     * maximum number of bytes to read
     * 
     * @return number of bytes read
     * @throws IOException
     * if an I/O error occurs.
     * @throws NullPointerException
     * if the byte array parameter is null
     * @throws IndexOutOfBoundsException
     * if offset, len or buffer size are invalid
     */
    @Throws(IOException::class)
    override fun read(b: ByteArray?, offset: Int, len: Int): Int {
        if (b == null) {
            throw NullPointerException()
        } else if (offset < 0 || len < 0) {
            throw IndexOutOfBoundsException()
        } else if (offset > b.size || offset + len > b.size) {
            throw IndexOutOfBoundsException()
        } else if (len == 0) {
            return 0
        } else {
            var readLen = 0
            /*
             Rationale for while-loop on (readLen == 0):
             -----
             Base32.readResults() usually returns > 0 or EOF (-1).  In the
             rare case where it returns 0, we just keep trying.

             This is essentially an undocumented contract for InputStream
             implementors that want their code to work properly with
             java.io.InputStreamReader, since the latter hates it when
             InputStream.read(byte[]) returns a zero.  Unfortunately our
             readResults() call must return 0 if a large amount of the data
             being decoded was non-base32, so this while-loop enables proper
             interop with InputStreamReader for that scenario.
             -----
             This is a fix for CODEC-101
            */
            while (readLen == 0) {
                if (!baseNCodec.hasData()) {
                    val buf = ByteArray(if (doEncode) 4096 else 8192)
                    val c = `in`.read(buf)
                    if (doEncode) {
                        baseNCodec.encode(buf, 0, c)
                    } else {
                        baseNCodec.decode(buf, 0, c)
                    }
                }
                readLen = baseNCodec.readResults(b, offset, len)
            }
            return readLen
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @return false
     */
    override fun markSupported(): Boolean {
        return false // not an easy job to support marks
    }
}
