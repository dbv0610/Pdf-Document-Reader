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
package com.wxiwei.office.fc.hpsf

import com.wxiwei.office.fc.util.LittleEndianConsts

import com.wxiwei.office.fc.util.LittleEndian
import com.wxiwei.office.fc.util.LittleEndian.putDouble
import com.wxiwei.office.fc.util.LittleEndian.putInt
import com.wxiwei.office.fc.util.LittleEndian.putLong
import com.wxiwei.office.fc.util.LittleEndian.putShort
import java.io.IOException
import java.io.OutputStream

/**
 * 
 * Class for writing little-endian data and more.
 * 
 * @author Rainer Klute [&lt;klute@rainer-klute.de&gt;](mailto:klute@rainer-klute.de)
 */
object TypeWriter {
    /**
     * 
     * Writes a two-byte value (short) to an output stream.
     * 
     * @param out The stream to write to.
     * @param n The value to write.
     * @return The number of bytes that have been written.
     * @exception IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    fun writeToStream(out: OutputStream, n: Short): Int {
        val length: Int = LittleEndianConsts.SHORT_SIZE
        val buffer = ByteArray(length)
        putShort(buffer, 0, n) // FIXME: unsigned
        out.write(buffer, 0, length)
        return length
    }


    /**
     * 
     * Writes a four-byte value to an output stream.
     * 
     * @param out The stream to write to.
     * @param n The value to write.
     * @exception IOException if an I/O error occurs
     * @return The number of bytes written to the output stream.
     */
    @Throws(IOException::class)
    fun writeToStream(out: OutputStream, n: Int): Int {
        val l: Int = LittleEndianConsts.INT_SIZE
        val buffer = ByteArray(l)
        putInt(buffer, 0, n)
        out.write(buffer, 0, l)
        return l
    }


    /**
     * 
     * Writes a eight-byte value to an output stream.
     * 
     * @param out The stream to write to.
     * @param n The value to write.
     * @exception IOException if an I/O error occurs
     * @return The number of bytes written to the output stream.
     */
    @Throws(IOException::class)
    fun writeToStream(out: OutputStream, n: Long): Int {
        val l: Int = LittleEndianConsts.LONG_SIZE
        val buffer = ByteArray(l)
        putLong(buffer, 0, n)
        out.write(buffer, 0, l)
        return l
    }


    /**
     * 
     * Writes an unsigned two-byte value to an output stream.
     * 
     * @param out The stream to write to
     * @param n The value to write
     * @exception IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    fun writeUShortToStream(out: OutputStream, n: Int) {
        val high = n and -0x10000
        if (high != 0) throw IllegalPropertySetDataException("Value " + n + " cannot be represented by 2 bytes.")
        writeToStream(out, n.toShort())
    }


    /**
     * 
     * Writes an unsigned four-byte value to an output stream.
     * 
     * @param out The stream to write to.
     * @param n The value to write.
     * @return The number of bytes that have been written to the output stream.
     * @exception IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    fun writeUIntToStream(out: OutputStream, n: Long): Int {
        val high = n and -0x100000000L
        if (high != 0L && high != -0x100000000L) throw IllegalPropertySetDataException("Value " + n + " cannot be represented by 4 bytes.")
        return writeToStream(out, n.toInt())
    }


    /**
     * 
     * Writes a 16-byte [ClassID] to an output stream.
     * 
     * @param out The stream to write to
     * @param n The value to write
     * @return The number of bytes written
     * @exception IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    fun writeToStream(out: OutputStream, n: ClassID): Int {
        val b = ByteArray(16)
        n.write(b, 0)
        out.write(b, 0, b.size)
        return b.size
    }


    /**
     * 
     * Writes an array of [Property] instances to an output stream
     * according to the Horrible Property Stream Format.
     * 
     * @param out The stream to write to
     * @param properties The array to write to the stream
     * @param codepage The codepage number to use for writing strings
     * @exception IOException if an I/O error occurs
     * @throws UnsupportedVariantTypeException if HPSF does not support some
     * variant type.
     */
    @Throws(IOException::class, UnsupportedVariantTypeException::class)
    fun writeToStream(
        out: OutputStream,
        properties: Array<Property>?,
        codepage: Int
    ) {
        /* If there are no properties don't write anything. */
        if (properties == null) return

        /* Write the property list. This is a list containing pairs of property
         * ID and offset into the stream. */
        for (i in properties.indices) {
            val p = properties[i]
            writeUIntToStream(out, p.getID())
            writeUIntToStream(out, p.size.toLong())
        }

        /* Write the properties themselves. */
        for (i in properties.indices) {
            val p = properties[i]
            val type = p.getType()
            writeUIntToStream(out, type)
            VariantSupport.Companion.write(out, type.toInt().toLong(), p.getValue(), codepage)
        }
    }


    /**
     * 
     * Writes a double value value to an output stream.
     * 
     * @param out The stream to write to.
     * @param n The value to write.
     * @exception IOException if an I/O error occurs
     * @return The number of bytes written to the output stream.
     */
    @Throws(IOException::class)
    fun writeToStream(out: OutputStream, n: Double): Int {
        val l: Int = LittleEndianConsts.DOUBLE_SIZE
        val buffer = ByteArray(l)
        putDouble(buffer, 0, n)
        out.write(buffer, 0, l)
        return l
    }
}
