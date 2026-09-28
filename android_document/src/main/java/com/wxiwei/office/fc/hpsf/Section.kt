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
import com.wxiwei.office.fc.util.LittleEndian.getUInt
import com.wxiwei.office.fc.util.LittleEndian.getUShort
import java.util.Collections

/**
 * 
 * Represents a section in a [PropertySet].
 * 
 * @author Rainer Klute [&lt;klute@rainer-klute.de&gt;](mailto:klute@rainer-klute.de)
 * @author Drew Varner (Drew.Varner allUpIn sc.edu)
 */
open class Section {
    /**
     * Maps property IDs to section-private PID strings. These
     * strings can be found in the property with ID 0.
     */
    @JvmField
    protected var dictionary: MutableMap<Long, String>? = null

    /**
     * Returns the dictionary which maps property IDs to property names.
     */
    open fun getDictionary(): MutableMap<Long, String>? {
        return dictionary
    }

    /** The section's format ID, [getFormatID]. */
    @JvmField
    protected var formatID: ClassID? = null

    /**
     * Returns the format ID. The format ID is the "type" of the
     * section.
     */
    open fun getFormatID(): ClassID? {
        return formatID
    }

    /** @see getOffset */
    @JvmField
    protected var offset: Long = 0

    /** Returns the offset of the section in the stream. */
    open fun getOffset(): Long {
        return offset
    }

    /** @see getSize */
    @JvmField
    protected var size: Int = 0

    /** Returns the section's size in bytes. */
    open fun getSize(): Int {
        return size
    }

    /** Returns the number of properties in this section. */
    open fun getPropertyCount(): Int {
        return properties.size
    }

    /** @see getProperties */
    @JvmField
    protected var properties: Array<Property> = emptyArray()

    /**
     * Returns this section's properties.
     */
    open fun getProperties(): Array<Property> {
        return properties
    }

    /**
     * Creates an empty and uninitialized [Section].
     */
    protected constructor()


    /**
     * 
     * Creates a [Section] instance from a byte array.
     * 
     * @param src Contains the complete property set stream.
     * @param offset The position in the stream that points to the
     * section's format ID.
     * 
     * @exception UnsupportedEncodingException if the section's codepage is not
     * supported.
     */
    constructor(src: ByteArray, offset: Int) {
        var o1 = offset

        /*
         * Read the format ID.
         */
        formatID = ClassID(src, o1)
        o1 += ClassID.Companion.LENGTH

        /*
         * Read the offset from the stream's start and positions to
         * the section header.
         */
        this.offset = getUInt(src, o1)
        o1 = this.offset.toInt()

        /*
         * Read the section length.
         */
        size = getUInt(src, o1).toInt()
        o1 += LittleEndianConsts.INT_SIZE

        /*
         * Read the number of properties.
         */
        val propertyCount = getUInt(src, o1).toInt()
        o1 += LittleEndianConsts.INT_SIZE

        /*
         * Read the properties. The offset is positioned at the first
         * entry of the property list. There are two problems:
         *
         * 1. For each property we have to find out its length. In the
         *    property list we find each property's ID and its offset relative
         *    to the section's beginning. Unfortunately the properties in the
         *    property list need not to be in ascending order, so it is not
         *    possible to calculate the length as
         *    (offset of property(i+1) - offset of property(i)). Before we can
         *    that we first have to sort the property list by ascending offsets.
         *
         * 2. We have to read the property with ID 1 before we read other
         *    properties, at least before other properties containing strings.
         *    The reason is that property 1 specifies the codepage. If it is
         *    1200, all strings are in Unicode. In other words: Before we can
         *    read any strings we have to know whether they are in Unicode or
         *    not. Unfortunately property 1 is not guaranteed to be the first in
         *    a section.
         *
         *    The algorithm below reads the properties in two passes: The first
         *    one looks for property ID 1 and extracts the codepage number. The
         *    seconds pass reads the other properties.
         */
        val properties = arrayOfNulls<Property>(propertyCount)

        /* Pass 1: Read the property list. */
        var pass1Offset = o1
        val propertyList: MutableList<PropertyListEntry> =
            ArrayList<PropertyListEntry>(propertyCount)
        var ple: PropertyListEntry
        for (i in properties.indices) {
            ple = PropertyListEntry()

            /* Read the property ID. */
            ple.id = getUInt(src, pass1Offset).toInt()
            pass1Offset += LittleEndianConsts.INT_SIZE

            /* Offset from the section's start. */
            ple.offset = getUInt(src, pass1Offset).toInt()
            pass1Offset += LittleEndianConsts.INT_SIZE

            /* Add the entry to the property list. */
            propertyList.add(ple)
        }

        /* Sort the property list by ascending offsets: */
        propertyList.sort()

        /* Calculate the properties' lengths. */
        for (i in 0..<propertyCount - 1) {
            val ple1 = propertyList.get(i)
            val ple2 = propertyList.get(i + 1)
            ple1.length = ple2.offset - ple1.offset
        }
        if (propertyCount > 0) {
            ple = propertyList.get(propertyCount - 1)
            ple.length = size - ple.offset
        }

        /* Look for the codepage. */
        var codepage = -1
        run {
            val i = propertyList.iterator()
            while (codepage == -1 && i.hasNext()) {
                ple = i.next()

                /* Read the codepage if the property ID is 1. */
                if (ple.id == PropertyIDMap.Companion.PID_CODEPAGE) {
                    /* Read the property's value type. It must be
                 * VT_I2. */
                    var o = (this.offset + ple.offset).toInt()
                    val type = getUInt(src, o)
                    o += LittleEndianConsts.INT_SIZE

                    if (type != Variant.Companion.VT_I2.toLong()) throw HPSFRuntimeException(
                        "Value type of property ID 1 is not VT_I2 but " +
                                type + "."
                    )

                    /* Read the codepage number. */
                    codepage = getUShort(src, o)
                }
            }
        }

        /* Pass 2: Read all properties - including the codepage property,
         * if available. */
        var i1 = 0
        val i = propertyList.iterator()
        while (i.hasNext()) {
            ple = i.next()
            var p = Property(
                ple.id.toLong(), src,
                this.offset + ple.offset,
                ple.length, codepage
            )
            if (p.getID() == PropertyIDMap.Companion.PID_CODEPAGE.toLong()) p =
                Property(p.getID(), p.getType(), codepage)
            properties[i1++] = p
        }

        /*
         * Extract the dictionary (if available).
         */
        this.properties = properties.requireNoNulls()

        /*
         * Extract the dictionary (if available).
         */
        dictionary = getProperty(0) as MutableMap<Long, String>?
    }


    /**
     * 
     * Represents an entry in the property list and holds a property's ID and
     * its offset from the section's beginning.
     */
    internal class PropertyListEntry : Comparable<PropertyListEntry> {
        var id: Int = 0
        var offset: Int = 0
        var length: Int = 0

        /**
         * 
         * Compares this [PropertyListEntry] with another one by their
         * offsets. A [PropertyListEntry] is "smaller" than another one if
         * its offset from the section's begin is smaller.
         * 
         * @see Comparable.compareTo
         */
        override fun compareTo(o: PropertyListEntry): Int {
            val otherOffset = o.offset
            if (offset < otherOffset) return -1
            else if (offset == otherOffset) return 0
            else return 1
        }

        override fun toString(): String {
            val b = StringBuffer()
            b.append(javaClass.getName())
            b.append("[id=")
            b.append(id)
            b.append(", offset=")
            b.append(offset)
            b.append(", length=")
            b.append(length)
            b.append(']')
            return b.toString()
        }
    }


    /**
     * 
     * Returns the value of the property with the specified ID. If
     * the property is not available, `null` is returned
     * and a subsequent call to [.wasNull] will return
     * `true`.
     * 
     * @param id The property's ID
     * 
     * @return The property's value
     */
    open fun getProperty(id: Long): Any? {
        wasNull = false
        for (i in properties.indices) if (id == properties[i].getID()) return properties[i].getValue()
        wasNull = true
        return null
    }


    /**
     * 
     * Returns the value of the numeric property with the specified
     * ID. If the property is not available, 0 is returned. A
     * subsequent call to [.wasNull] will return
     * `true` to let the caller distinguish that case from
     * a real property value of 0.
     * 
     * @param id The property's ID
     * 
     * @return The property's value
     */
    fun getPropertyIntValue(id: Long): Int {
        val i: Number
        val o = getProperty(id)
        if (o == null) return 0
        if (!(o is Long || o is Int)) throw HPSFRuntimeException(
            "This property is not an integer type, but " +
                    o.javaClass.getName() + "."
        )
        i = o as Number
        return i.toInt()
    }


    /**
     * 
     * Returns the value of the boolean property with the specified
     * ID. If the property is not available, `false` is
     * returned. A subsequent call to [.wasNull] will return
     * `true` to let the caller distinguish that case from
     * a real property value of `false`.
     * 
     * @param id The property's ID
     * 
     * @return The property's value
     */
    fun getPropertyBooleanValue(id: Int): Boolean {
        val b = getProperty(id.toLong()) as Boolean?
        if (b == null) {
            return false
        }
        return b
    }


    /**
     * 
     * This member is `true` if the last call to [ ][.getPropertyIntValue] or [.getProperty] tried to access a
     * property that was not available, else `false`.
     */
    private var wasNull = false


    /**
     * 
     * Checks whether the property which the last call to [ ][.getPropertyIntValue] or [.getProperty] tried to access
     * was available or not. This information might be important for
     * callers of [.getPropertyIntValue] since the latter
     * returns 0 if the property does not exist. Using [ ][.wasNull] the caller can distiguish this case from a property's
     * real value of 0.
     * 
     * @return `true` if the last call to [ ][.getPropertyIntValue] or [.getProperty] tried to access a
     * property that was not available, else `false`.
     */
    fun wasNull(): Boolean {
        return wasNull
    }


    /**
     * 
     * Returns the PID string associated with a property ID. The ID
     * is first looked up in the [Section]'s private
     * dictionary. If it is not found there, the method calls [ ][SectionIDMap.getPIDString].
     * 
     * @param pid The property ID
     * 
     * @return The property ID's string value
     */
    fun getPIDString(pid: Long): String {
        var s: String? = null
        val dictionary = this.dictionary
        if (dictionary != null) s = dictionary.get(pid)
        if (s == null) s = SectionIDMap.getPIDString(getFormatID()!!.getBytes(), pid)
        if (s == null) s = SectionIDMap.Companion.UNDEFINED
        return s!!
    }


    /**
     * 
     * Checks whether this section is equal to another object. The result is
     * `false` if one of the the following conditions holds:
     * 
     * 
     * 
     *  * 
     *
     *The other object is not a [Section].
     * 
     *  * 
     *
     *The format IDs of the two sections are not equal.
     * 
     *  * 
     *
     *The sections have a different number of properties. However,
     * properties with ID 1 (codepage) are not counted.
     * 
     *  * 
     *
     *The other object is not a [Section].
     * 
     *  * 
     *
     *The properties have different values. The order of the properties
     * is irrelevant.
     * 
     * 
     * 
     * @param o The object to compare this section with
     * @return `true` if the objects are equal, `false` if
     * not
     */
    override fun equals(o: Any?): Boolean {
        if (o == null || o !is Section) return false
        val s = o
        if (s.getFormatID() != getFormatID()) return false

        /* Compare all properties except 0 and 1 as they must be handled
         * specially. */
        var pa1 = arrayOfNulls<Property>(
            getProperties().size
        )
        var pa2 = arrayOfNulls<Property>(
            s.getProperties().size
        )
        System.arraycopy(getProperties(), 0, pa1, 0, pa1.size)
        System.arraycopy(s.getProperties(), 0, pa2, 0, pa2.size)

        /* Extract properties 0 and 1 and remove them from the copy of the
         * arrays. */
        var p10: Property? = null
        var p20: Property? = null
        run {
            var i = 0
            while (i < pa1.size) {
                val id = pa1[i]!!.getID()
                if (id == 0L) {
                    p10 = pa1[i]
                    pa1 = remove(pa1, i)
                    i--
                }
                if (id == 1L) {
                    // p11 = pa1[i];
                    pa1 = remove(pa1, i)
                    i--
                }
                i++
            }
        }
        var i = 0
        while (i < pa2.size) {
            val id = pa2[i]!!.getID()
            if (id == 0L) {
                p20 = pa2[i]
                pa2 = remove(pa2, i)
                i--
            }
            if (id == 1L) {
                // p21 = pa2[i];
                pa2 = remove(pa2, i)
                i--
            }
            i++
        }

        /* If the number of properties (not counting property 1) is unequal the
         * sections are unequal. */
        if (pa1.size != pa2.size) return false

        /* If the dictionaries are unequal the sections are unequal. */
        var dictionaryEqual = true
        if (p10 != null && p20 != null) dictionaryEqual = p10.getValue() == p20.getValue()
        else if (p10 != null || p20 != null) dictionaryEqual = false
        if (dictionaryEqual) {
            return Util.equals(pa1, pa2)
        }
        return false
    }


    /**
     * 
     * Removes a field from a property array. The resulting array is
     * compactified and returned.
     * 
     * @param pa The property array.
     * @param i The index of the field to be removed.
     * @return the compactified array.
     */
    private fun remove(pa: Array<Property?>, i: Int): Array<Property?> {
        val h = arrayOfNulls<Property>(pa.size - 1)
        if (i > 0) System.arraycopy(pa, 0, h, 0, i)
        System.arraycopy(pa, i + 1, h, i, h.size - i)
        return h
    }


    /**
     * @see Object.hashCode
     */
    override fun hashCode(): Int {
        var hashCode: Long = 0
        hashCode += getFormatID().hashCode().toLong()
        val pa = getProperties()
        for (i in pa.indices) hashCode += pa[i].hashCode().toLong()
        val returnHashCode = (hashCode and 0x0ffffffffL).toInt()
        return returnHashCode
    }


    /**
     * @see Object.toString
     */
    override fun toString(): String {
        val b = StringBuffer()
        val pa = getProperties()
        b.append(javaClass.getName())
        b.append('[')
        b.append("formatID: ")
        b.append(getFormatID())
        b.append(", offset: ")
        b.append(getOffset())
        b.append(", propertyCount: ")
        b.append(getPropertyCount())
        b.append(", size: ")
        b.append(getSize())
        b.append(", properties: [\n")
        for (i in pa.indices) {
            b.append(pa[i].toString())
            b.append(",\n")
        }
        b.append(']')
        b.append(']')
        return b.toString()
    }


    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getCodepageProperty")
    open val codepage: Int
        /**
         * 
         * Gets the section's codepage, if any.
         * 
         * @return The section's codepage if one is defined, else -1.
         */
        get() {
            val codepage =
                getProperty(PropertyIDMap.Companion.PID_CODEPAGE.toLong()) as Int?
            if (codepage == null) return -1
            val cp = codepage
            return cp
        }

    open fun getCodepage(): Int = codepage
}
