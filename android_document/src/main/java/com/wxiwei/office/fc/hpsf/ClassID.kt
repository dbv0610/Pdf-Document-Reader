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

import com.wxiwei.office.fc.util.HexDump.toHex

/**
 * 
 * Represents a class ID (16 bytes). Unlike other little-endian
 * type the [ClassID] is not just 16 bytes stored in the wrong
 * order. Instead, it is a double word (4 bytes) followed by two
 * words (2 bytes each) followed by 8 bytes.
 * 
 * @author Rainer Klute [&lt;klute@rainer-klute.de&gt;](mailto:klute@rainer-klute.de)
 */
class ClassID {
    /**
     * 
     * The bytes making out the class ID in correct order,
     * i.e. big-endian.
     */
    private lateinit var bytes: ByteArray


    /**
     * 
     * Creates a [ClassID] and reads its value from a byte
     * array.
     * 
     * @param src The byte array to read from.
     * @param offset The offset of the first byte to read.
     */
    constructor(src: ByteArray, offset: Int) {
        read(src, offset)
    }


    /**
     * 
     * Creates a [ClassID] and initializes its value with
     * 0x00 bytes.
     */
    constructor() {
        bytes = ByteArray(LENGTH)
        for (i in 0..<LENGTH) bytes[i] = 0x00
    }


    /**
     * @return The number of bytes occupied by this object in the byte
     * stream.
     */
    fun length(): Int {
        return LENGTH
    }


    /**
     * 
     * Gets the bytes making out the class ID. They are returned in
     * correct order, i.e. big-endian.
     * 
     * @return the bytes making out the class ID.
     */
    fun getBytes(): ByteArray {
        return bytes
    }


    /**
     * 
     * Sets the bytes making out the class ID.
     * 
     * @param bytes The bytes making out the class ID in big-endian format. They
     * are copied without their order being changed.
     */
    fun setBytes(bytes: ByteArray) {
        for (i in this.bytes.indices) this.bytes[i] = bytes[i]
    }


    /**
     * 
     * Reads the class ID's value from a byte array by turning
     * little-endian into big-endian.
     * 
     * @param src The byte array to read from
     * 
     * @param offset The offset within the <var>src</var> byte array
     * 
     * @return A byte array containing the class ID.
     */
    fun read(src: ByteArray, offset: Int): ByteArray {
        bytes = ByteArray(16)

        /* Read double word. */
        bytes[0] = src[3 + offset]
        bytes[1] = src[2 + offset]
        bytes[2] = src[1 + offset]
        bytes[3] = src[0 + offset]

        /* Read first word. */
        bytes[4] = src[5 + offset]
        bytes[5] = src[4 + offset]

        /* Read second word. */
        bytes[6] = src[7 + offset]
        bytes[7] = src[6 + offset]

        /* Read 8 bytes. */
        for (i in 8..15) bytes[i] = src[i + offset]

        return bytes
    }


    /**
     * 
     * Writes the class ID to a byte array in the
     * little-endian format.
     * 
     * @param dst The byte array to write to.
     * 
     * @param offset The offset within the <var>dst</var> byte array.
     * 
     * @exception ArrayStoreException if there is not enough room for the class
     * ID 16 bytes in the byte array after the <var>offset</var> position.
     */
    @Throws(ArrayStoreException::class)
    fun write(dst: ByteArray, offset: Int) {
        /* Check array size: */
        if (dst.size < 16) throw ArrayStoreException(
            "Destination byte[] must have room for at least 16 bytes, " +
                    "but has a length of only " + dst.size + "."
        )
        /* Write double word. */
        dst[0 + offset] = bytes[3]
        dst[1 + offset] = bytes[2]
        dst[2 + offset] = bytes[1]
        dst[3 + offset] = bytes[0]

        /* Write first word. */
        dst[4 + offset] = bytes[5]
        dst[5 + offset] = bytes[4]

        /* Write second word. */
        dst[6 + offset] = bytes[7]
        dst[7 + offset] = bytes[6]

        /* Write 8 bytes. */
        for (i in 8..15) dst[i + offset] = bytes[i]
    }


    /**
     * 
     * Checks whether this `ClassID` is equal to another
     * object.
     * 
     * @param o the object to compare this `PropertySet` with
     * @return `true` if the objects are equal, else
     * `false`.
     */
    override fun equals(o: Any?): Boolean {
        if (o == null || o !is ClassID) return false
        val cid = o
        if (bytes.size != cid.bytes.size) return false
        for (i in bytes.indices) if (bytes[i] != cid.bytes[i]) return false
        return true
    }


    /**
     * @see Object.hashCode
     */
    override fun hashCode(): Int {
        return String(bytes).hashCode()
    }


    /**
     * 
     * Returns a human-readable representation of the Class ID in standard
     * format `"{xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx}"`.
     * 
     * @return String representation of the Class ID represented by this object.
     */
    override fun toString(): String {
        val sbClassId = StringBuffer(38)
        sbClassId.append('{')
        for (i in 0..15) {
            sbClassId.append(toHex(bytes[i]))
            if (i == 3 || i == 5 || i == 7 || i == 9) sbClassId.append('-')
        }
        sbClassId.append('}')
        return sbClassId.toString()
    }

    companion object {
        /** 
         *
         *The number of bytes occupied by this object in the byte
         * stream.  */
        const val LENGTH: Int = 16
    }
}
