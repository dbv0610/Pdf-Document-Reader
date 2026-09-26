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

import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Date

/**
 * 
 * Provides various static utility methods.
 * 
 * @author Rainer Klute (klute@rainer-klute.de)
 */
object Util {
    /**
     * 
     * Checks whether two byte arrays <var>a</var> and <var>b</var>
     * are equal. They are equal
     * 
     * 
     * 
     *  * 
     *
     *if they have the same length and
     * 
     *  * 
     *
     *if for each <var>i</var> with
     * <var>i</var>&nbsp;&gt;=&nbsp;0 and
     * <var>i</var>&nbsp;&lt;&nbsp;<var>a.length</var> holds
     * <var>a</var>[<var>i</var>]&nbsp;== <var>b</var>[<var>i</var>].
     * 
     * 
     * 
     * @param a The first byte array
     * @param b The first byte array
     * @return `true` if the byte arrays are equal, else
     * `false`
     */
    fun equal(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        for (i in a.indices) if (a[i] != b[i]) return false
        return true
    }


    /**
     * 
     * Copies a part of a byte array into another byte array.
     * 
     * @param src The source byte array.
     * @param srcOffset Offset in the source byte array.
     * @param length The number of bytes to copy.
     * @param dst The destination byte array.
     * @param dstOffset Offset in the destination byte array.
     */
    fun copy(
        src: ByteArray, srcOffset: Int,
        length: Int, dst: ByteArray,
        dstOffset: Int
    ) {
        for (i in 0..<length) dst[dstOffset + i] = src[srcOffset + i]
    }


    /**
     * 
     * Concatenates the contents of several byte arrays into a
     * single one.
     * 
     * @param byteArrays The byte arrays to be concatened.
     * @return A new byte array containing the concatenated byte
     * arrays.
     */
    fun cat(byteArrays: Array<ByteArray?>): ByteArray {
        var capacity = 0
        for (i in byteArrays.indices) capacity += byteArrays[i]!!.size
        val result = ByteArray(capacity)
        var r = 0
        for (i in byteArrays.indices) for (j in byteArrays[i]!!.indices) result[r++] =
            byteArrays[i]!![j]
        return result
    }


    /**
     * 
     * Copies bytes from a source byte array into a new byte
     * array.
     * 
     * @param src Copy from this byte array.
     * @param offset Start copying here.
     * @param length Copy this many bytes.
     * @return The new byte array. Its length is number of copied bytes.
     */
    fun copy(
        src: ByteArray, offset: Int,
        length: Int
    ): ByteArray {
        val result = ByteArray(length)
        copy(src, offset, length, result, 0)
        return result
    }


    /**
     * 
     * The difference between the Windows epoch (1601-01-01
     * 00:00:00) and the Unix epoch (1970-01-01 00:00:00) in
     * milliseconds: 11644473600000L. (Use your favorite spreadsheet
     * program to verify the correctness of this value. By the way,
     * did you notice that you can tell from the epochs which
     * operating system is the modern one? :-))
     */
    const val EPOCH_DIFF: Long = 11644473600000L


    /**
     * 
     * Converts a Windows FILETIME into a [Date]. The Windows
     * FILETIME structure holds a date and time associated with a
     * file. The structure identifies a 64-bit integer specifying the
     * number of 100-nanosecond intervals which have passed since
     * January 1, 1601. This 64-bit value is split into the two double
     * words stored in the structure.
     * 
     * @param high The higher double word of the FILETIME structure.
     * @param low The lower double word of the FILETIME structure.
     * @return The Windows FILETIME as a [Date].
     */
    fun filetimeToDate(high: Int, low: Int): Date {
        val filetime = (high.toLong()) shl 32 or (low.toLong() and 0xffffffffL)
        return filetimeToDate(filetime)
    }

    /**
     * 
     * Converts a Windows FILETIME into a [Date]. The Windows
     * FILETIME structure holds a date and time associated with a
     * file. The structure identifies a 64-bit integer specifying the
     * number of 100-nanosecond intervals which have passed since
     * January 1, 1601.
     * 
     * @param filetime The filetime to convert.
     * @return The Windows FILETIME as a [Date].
     */
    fun filetimeToDate(filetime: Long): Date {
        val ms_since_16010101 = filetime / (1000 * 10)
        val ms_since_19700101 = ms_since_16010101 - EPOCH_DIFF
        return Date(ms_since_19700101)
    }


    /**
     * 
     * Converts a [Date] into a filetime.
     * 
     * @param date The date to be converted
     * @return The filetime
     * 
     * @see .filetimeToDate
     * @see .filetimeToDate
     */
    fun dateToFileTime(date: Date): Long {
        val ms_since_19700101 = date.getTime()
        val ms_since_16010101 = ms_since_19700101 + EPOCH_DIFF
        return ms_since_16010101 * (1000 * 10)
    }


    /**
     * 
     * Checks whether two collections are equal. Two collections
     * C<sub>1</sub> and C<sub>2</sub> are equal, if the following conditions
     * are true:
     * 
     * 
     * 
     *  * 
     *
     *For each c<sub>1*i*</sub> (element of C<sub>1</sub>) there
     * is a c<sub>2*j*</sub> (element of C<sub>2</sub>), and
     * c<sub>1*i*</sub> equals c<sub>2*j*</sub>.
     * 
     *  * 
     *
     *For each c<sub>2*i*</sub> (element of C<sub>2</sub>) there
     * is a c<sub>1*j*</sub> (element of C<sub>1</sub>) and
     * c<sub>2*i*</sub> equals c<sub>1*j*</sub>.
     * 
     * 
     * 
     * @param c1 the first collection
     * @param c2 the second collection
     * @return `true` if the collections are equal, else
     * `false`.
     */
    fun equals(c1: MutableCollection<*>, c2: MutableCollection<*>): Boolean {
        val o1: Array<Any?> = c1.toTypedArray()
        val o2: Array<Any?> = c2.toTypedArray()
        return internalEquals(o1, o2)
    }


    /**
     * 
     * Compares to object arrays with regarding the objects' order. For
     * example, [1, 2, 3] and [2, 1, 3] are equal.
     * 
     * @param c1 The first object array.
     * @param c2 The second object array.
     * @return `true` if the object arrays are equal,
     * `false` if they are not.
     */
    fun equals(c1: Array<out Any?>, c2: Array<out Any?>): Boolean {
        val o1: Array<Any?> = Array(c1.size) { c1[it] }
        val o2: Array<Any?> = Array(c2.size) { c2[it] }
        return internalEquals(o1, o2)
    }

    private fun internalEquals(o1: Array<Any?>, o2: Array<Any?>): Boolean {
        for (i1 in o1.indices) {
            val obj1 = o1[i1]
            var matchFound = false
            var i2 = 0
            while (!matchFound && i2 < o1.size) {
                val obj2: Any? = o2[i2]
                if (obj1 == obj2) {
                    matchFound = true
                    o2[i2] = null
                }
                i2++
            }
            if (!matchFound) return false
        }
        return true
    }


    /**
     * 
     * Pads a byte array with 0x00 bytes so that its length is a multiple of
     * 4.
     * 
     * @param ba The byte array to pad.
     * @return The padded byte array.
     */
    fun pad4(ba: ByteArray): ByteArray {
        val PAD = 4
        val result: ByteArray
        var l = ba.size % PAD
        if (l == 0) result = ba
        else {
            l = PAD - l
            result = ByteArray(ba.size + l)
            System.arraycopy(ba, 0, result, 0, ba.size)
        }
        return result
    }


    /**
     * 
     * Pads a character array with 0x0000 characters so that its length is a
     * multiple of 4.
     * 
     * @param ca The character array to pad.
     * @return The padded character array.
     */
    fun pad4(ca: CharArray): CharArray {
        val PAD = 4
        val result: CharArray
        var l = ca.size % PAD
        if (l == 0) result = ca
        else {
            l = PAD - l
            result = CharArray(ca.size + l)
            System.arraycopy(ca, 0, result, 0, ca.size)
        }
        return result
    }


    /**
     * 
     * Pads a string with 0x0000 characters so that its length is a
     * multiple of 4.
     * 
     * @param s The string to pad.
     * @return The padded string as a character array.
     */
    fun pad4(s: String): CharArray {
        return pad4(s.toCharArray())
    }


    /**
     * 
     * Returns a textual representation of a [Throwable], including a
     * stacktrace.
     * 
     * @param t The [Throwable]
     * 
     * @return a string containing the output of a call to
     * `t.printStacktrace()`.
     */
    fun toString(t: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        t.printStackTrace(pw)
        pw.close()
        try {
            sw.close()
            return sw.toString()
        } catch (e: IOException) {
            val b = StringBuffer(t.message)
            b.append("\n")
            b.append("Could not create a stacktrace. Reason: ")
            b.append(e.message)
            return b.toString()
        }
    }
}
