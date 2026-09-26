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

import java.io.IOException
import java.io.InputStream

/**
 * Implementation of a BlockingInputStream to provide data to
 * RawDataBlock that expects data in 512 byte chunks.  Useful to read
 * data from slow (ie, non FileInputStream) sources, for example when
 * reading an OLE2 Document over a network.
 *
 * Possible extentions: add a timeout. Curently a call to read(byte[]) on this
 *    class is blocking, so use at your own peril if your underlying stream blocks.
 *
 * @author Jens Gerhard
 * @author aviks - documentation cleanups.
 */
open class BlockingInputStream(`is`: InputStream) : InputStream() {
    @JvmField
    protected var `is`: InputStream = `is`

    @Throws(IOException::class)
    override fun available(): Int {
        return `is`.available()
    }

    @Throws(IOException::class)
    override fun close() {
        `is`.close()
    }

    override fun mark(readLimit: Int) {
        `is`.mark(readLimit)
    }

    override fun markSupported(): Boolean {
        return `is`.markSupported()
    }

    @Throws(IOException::class)
    override fun read(): Int {
        return `is`.read()
    }

    /**
     * We had to revert to byte per byte reading to keep
     * with slow network connections on one hand, without
     * missing the end-of-file.
     * This is the only method that does its own thing in this class
     *    everything else is delegated to aggregated stream.
     * THIS IS A BLOCKING BLOCK READ!!!
     */
    @Throws(IOException::class)
    override fun read(bf: ByteArray): Int {
        var i = 0
        var b = 4611
        while (i < bf.size) {
            b = `is`.read()
            if (b == -1) {
                break
            }
            bf[i++] = b.toByte()
        }
        if (i == 0 && b == -1) {
            return -1
        }
        return i
    }

    @Throws(IOException::class)
    override fun read(bf: ByteArray, s: Int, l: Int): Int {
        return `is`.read(bf, s, l)
    }

    @Throws(IOException::class)
    override fun reset() {
        `is`.reset()
    }

    @Throws(IOException::class)
    override fun skip(n: Long): Long {
        return `is`.skip(n)
    }
}
