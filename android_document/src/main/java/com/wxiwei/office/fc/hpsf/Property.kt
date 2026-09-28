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

import com.wxiwei.office.fc.util.HexDump.dump
import com.wxiwei.office.fc.util.LittleEndian
import com.wxiwei.office.fc.util.LittleEndian.getUInt
import com.wxiwei.office.fc.util.POILogFactory.Companion.getLogger
import com.wxiwei.office.fc.util.POILogger
import java.io.UnsupportedEncodingException

/**
 * 
 * A property in a [Section] of a [PropertySet].
 * 
 * 
 * The property's **ID** gives the property a meaning
 * in the context of its [Section]. Each [Section] spans
 * its own name space of property IDs.
 * 
 * 
 * The property's **type** determines how its
 * **value ** is interpreted. For example, if the type is
 * [Variant.VT_LPSTR] (byte string), the value consists of a
 * DWord telling how many bytes the string contains. The bytes follow
 * immediately, including any null bytes that terminate the
 * string. The type [Variant.VT_I4] denotes a four-byte integer
 * value, [Variant.VT_FILETIME] some date and time (of a
 * file).
 * 
 * 
 * Please note that not all [Variant] types yet. This might change
 * over time but largely depends on your feedback so that the POI team knows
 * which variant types are really needed. So please feel free to submit error
 * reports or patches for the types you need.
 * 
 * 
 * Microsoft documentation: [
 * Property Set Display Name Dictionary](http://msdn.microsoft.com/library/en-us/stg/stg/property_set_display_name_dictionary.asp?frame=true).
 * 
 * @author Rainer Klute [&lt;klute@rainer-klute.de&gt;](mailto:klute@rainer-klute.de)
 * @author Drew Varner (Drew.Varner InAndAround sc.edu)
 * @see Section
 * 
 * @see Variant
 */
open class Property {
    /**
     * 
     * Returns the property's ID.
     * 
     * @return The ID value
     */
    /** 
     *
     *The property's ID.  */
    @JvmField
    protected var id: Long = 0

    /** Returns the property's ID. */
    fun getID(): Long {
        return id
    }

    /** The property's type. */
    @JvmField
    protected var type: Long = 0

    /** Returns the property's type. */
    fun getType(): Long {
        return type
    }

    /** The property's value. */
    @JvmField
    protected var value: Any? = null

    /** Returns the property's value. */
    open fun getValue(): Any? {
        return value
    }


    /**
     * 
     * Creates a property.
     * 
     * @param id the property's ID.
     * @param type the property's type, see [Variant].
     * @param value the property's value. Only certain types are allowed, see
     * [Variant].
     */
    constructor(id: Long, type: Long, value: Any?) {
        this.id = id
        this.type = type
        this.value = value
    }


    /**
     * 
     * Creates a [Property] instance by reading its bytes
     * from the property set stream.
     * 
     * @param id The property's ID.
     * @param src The bytes the property set stream consists of.
     * @param offset The property's type/value pair's offset in the
     * section.
     * @param length The property's type/value pair's length in bytes.
     * @param codepage The section's and thus the property's
     * codepage. It is needed only when reading string values.
     * @exception UnsupportedEncodingException if the specified codepage is not
     * supported.
     */
    constructor(
        id: Long, src: ByteArray, offset: Long,
        length: Int, codepage: Int
    ) {
        this.id = id

        /*
         * ID 0 is a special case since it specifies a dictionary of
         * property IDs and property names.
         */
        if (id == 0L) {
            value = readDictionary(src, offset, length, codepage)
            return
        }

        var o = offset.toInt()
        type = getUInt(src, o)
        o += LittleEndianConsts.INT_SIZE

        try {
            value = VariantSupport.Companion.read(src, o, length, type.toInt().toLong(), codepage)
        } catch (ex: UnsupportedVariantTypeException) {
            VariantSupport.Companion.writeUnsupportedTypeMessage(ex)
            value = ex.value
        }
    }


    /**
     * 
     * Creates an empty property. It must be filled using the set method to
     * be usable.
     */
    protected constructor()


    /**
     * 
     * Reads a dictionary.
     * 
     * @param src The byte array containing the bytes making out the dictionary.
     * @param offset At this offset within <var>src </var> the dictionary
     * starts.
     * @param length The dictionary contains at most this many bytes.
     * @param codepage The codepage of the string values.
     * @return The dictonary
     * @throws UnsupportedEncodingException if the dictionary's codepage is not
     * (yet) supported.
     */
    @Throws(UnsupportedEncodingException::class)
    protected fun readDictionary(
        src: ByteArray, offset: Long,
        length: Int, codepage: Int
    ): MutableMap<Long, String> {
        /* Check whether "offset" points into the "src" array". */
        if (offset < 0 || offset > src.size) throw HPSFRuntimeException(
            "Illegal offset " + offset + " while HPSF stream contains " +
                    length + " bytes."
        )
        var o = offset.toInt()

        /*
         * Read the number of dictionary entries.
         */
        val nrEntries = getUInt(src, o)
        o += LittleEndianConsts.INT_SIZE

        val m: MutableMap<Long, String> = HashMap(nrEntries.toInt(), 1.0.toFloat())

        try {
            for (i in 0..<nrEntries) {
                /* The key. */
                val id = getUInt(src, o)
                o += LittleEndianConsts.INT_SIZE

                /* The value (a string). The length is the either the
                 * number of (two-byte) characters if the character set is Unicode
                 * or the number of bytes if the character set is not Unicode.
                 * The length includes terminating 0x00 bytes which we have to strip
                 * off to create a Java string. */
                var sLength = getUInt(src, o)
                o += LittleEndianConsts.INT_SIZE

                /* Read the string. */
                val b = StringBuffer()
                when (codepage) {
                    -1 -> {
                        /* Without a codepage the length is equal to the number of
                                            * bytes. */
                        b.append(String(src, o, sLength.toInt()))
                    }

                    Constants.CP_UNICODE -> {
                        /* The length is the number of characters, i.e. the number
                                                * of bytes is twice the number of the characters. */
                        val nrBytes = (sLength * 2).toInt()
                        val h = ByteArray(nrBytes)
                        var i2 = 0
                        while (i2 < nrBytes) {
                            h[i2] = src[o + i2 + 1]
                            h[i2 + 1] = src[o + i2]
                            i2 += 2
                        }
                        b.append(
                            String(
                                h, 0, nrBytes,
                                charset(VariantSupport.Companion.codepageToEncoding(codepage))
                            )
                        )
                    }

                    else -> {
                        /* For encodings other than Unicode the length is the number
                                                * of bytes. */
                        b.append(
                            String(
                                src, o, sLength.toInt(),
                                charset(VariantSupport.Companion.codepageToEncoding(codepage))
                            )
                        )
                    }
                }

                /* Strip 0x00 characters from the end of the string: */
                while (b.length > 0 && b.get(b.length - 1).code == 0x00) b.setLength(b.length - 1)
                if (codepage == Constants.CP_UNICODE) {
                    if (sLength % 2 == 1L) sLength++
                    o += (sLength + sLength).toInt()
                } else o += sLength.toInt()
                m.put(id, b.toString())
            }
        } catch (ex: RuntimeException) {
            val l = getLogger(javaClass)
            l.log(
                POILogger.WARN,
                ("The property set's dictionary contains bogus data. "
                        + "All dictionary entries starting with the one with ID "
                        + this.id + " will be ignored."), ex
            )
        }
        return m
    }


    @get:Throws(WritingNotSupportedException::class)
    val size: Int
        /**
         * 
         * Returns the property's size in bytes. This is always a multiple of
         * 4.
         * 
         * @return the property's size in bytes
         * 
         * @exception WritingNotSupportedException if HPSF does not yet support the
         * property's variant type.
         */
        get() {
            var length: Int =
                Variant.Companion.getVariantLength(type)
            if (length >= 0) return length /* Fixed length */
            if (length == -2)  /* Unknown length */
                throw WritingNotSupportedException(type, null)

            /* Variable length: */
            val PADDING = 4 /* Pad to multiples of 4. */
            when (type.toInt()) {
                Variant.Companion.VT_LPSTR -> {
                    var l = (value as String).length + 1
                    val r = l % PADDING
                    if (r > 0) l += PADDING - r
                    length += l
                }

                Variant.Companion.VT_EMPTY -> {}
                else -> throw WritingNotSupportedException(type, value)
            }
            return length
        }


    /**
     * 
     * Compares two properties. 
     *
     *Please beware that a property with
     * ID == 0 is a special case: It does not have a type, and its value is the
     * section's dictionary. Another special case are strings: Two properties
     * may have the different types Variant.VT_LPSTR and Variant.VT_LPWSTR;
     * 
     * @see Object.equals
     */
    override fun equals(o: Any?): Boolean {
        if (o !is Property) {
            return false
        }
        val p = o
        val pValue = p.getValue()
        val pId: Long = p.id
        if (this.id != pId || (this.id != 0L && !typesAreEqual(type, p.type))) return false
        val value = this.value
        if (value == null && pValue == null) return true
        if (value == null || pValue == null) return false

        /* It's clear now that both values are non-null. */
        val valueClass: Class<*> = value.javaClass
        val pValueClass: Class<*> = pValue.javaClass
        if (!(valueClass.isAssignableFrom(pValueClass)) &&
            !(pValueClass.isAssignableFrom(valueClass))
        ) return false

        if (value is ByteArray) return Util.equal(value, pValue as ByteArray)

        return value == pValue
    }


    private fun typesAreEqual(t1: Long, t2: Long): Boolean {
        if (t1 == t2 ||
            (t1 == Variant.Companion.VT_LPSTR.toLong() && t2 == Variant.Companion.VT_LPWSTR.toLong()) ||
            (t2 == Variant.Companion.VT_LPSTR.toLong() && t1 == Variant.Companion.VT_LPWSTR.toLong())
        ) {
            return true
        }
        return false
    }


    /**
     * @see Object.hashCode
     */
    override fun hashCode(): Int {
        var hashCode: Long = 0
        hashCode += this.id
        hashCode += type
        val value = this.value
        if (value != null) hashCode += value.hashCode().toLong()
        val returnHashCode = (hashCode and 0x0ffffffffL).toInt()
        return returnHashCode
    }


    /**
     * @see Object.toString
     */
    override fun toString(): String {
        val b = StringBuffer()
        b.append(javaClass.getName())
        b.append('[')
        b.append("id: ")
        b.append(this.id)
        b.append(", type: ")
        b.append(this.type)
        val value = getValue()
        b.append(", value: ")
        b.append(value.toString())
        if (value is String) {
            val s = value
            val l = s.length
            val bytes = ByteArray(l * 2)
            for (i in 0..<l) {
                val c = s.get(i)
                val high = ((c.code and 0x00ff00) shr 8).toByte()
                val low = ((c.code and 0x0000ff) shr 0).toByte()
                bytes[i * 2] = high
                bytes[i * 2 + 1] = low
            }
            val hex = dump(bytes, 0L, 0)
            b.append(" [")
            b.append(hex)
            b.append("]")
        }
        b.append(']')
        return b.toString()
    }
}
