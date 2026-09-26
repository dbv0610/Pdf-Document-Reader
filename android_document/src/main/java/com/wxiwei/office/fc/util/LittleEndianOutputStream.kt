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

import java.io.FilterOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 *
 * @author Josh Micich
 */
class LittleEndianOutputStream(out: OutputStream?) : FilterOutputStream(out), LittleEndianOutput {

    override fun writeByte(v: Int) {
        try {
            out.write(v)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    override fun writeDouble(v: Double) {
        writeLong(java.lang.Double.doubleToLongBits(v))
    }

    override fun writeInt(v: Int) {
        val b3 = (v ushr 24) and 0xFF
        val b2 = (v ushr 16) and 0xFF
        val b1 = (v ushr 8) and 0xFF
        val b0 = (v ushr 0) and 0xFF
        try {
            out.write(b0)
            out.write(b1)
            out.write(b2)
            out.write(b3)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    override fun writeLong(v: Long) {
        writeInt((v shr 0).toInt())
        writeInt((v shr 32).toInt())
    }

    override fun writeShort(v: Int) {
        val b1 = (v ushr 8) and 0xFF
        val b0 = (v ushr 0) and 0xFF
        try {
            out.write(b0)
            out.write(b1)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    override fun write(b: ByteArray) {
        // suppress IOException for interface method
        try {
            super.write(b)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        // suppress IOException for interface method
        try {
            super.write(b, off, len)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
