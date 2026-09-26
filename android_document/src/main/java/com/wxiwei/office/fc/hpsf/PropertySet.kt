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
import com.wxiwei.office.fc.util.LittleEndian.getInt
import com.wxiwei.office.fc.util.LittleEndian.getUInt
import com.wxiwei.office.fc.util.LittleEndian.getUShort
import com.wxiwei.office.fc.util.LittleEndian.putShort
import java.io.IOException
import java.io.InputStream
import java.io.UnsupportedEncodingException
import kotlin.math.min

/**
 * 
 * Represents a property set in the Horrible Property Set Format
 * (HPSF). These are usually metadata of a Microsoft Office
 * document.
 * 
 * 
 * An application that wants to access these metadata should create
 * an instance of this class or one of its subclasses by calling the
 * factory method [PropertySetFactory.create] and then retrieve
 * the information its needs by calling appropriate methods.
 * 
 * 
 * [PropertySetFactory.create] does its work by calling one
 * of the constructors [PropertySet.PropertySet] or
 * [PropertySet.PropertySet]. If the constructor's
 * argument is not in the Horrible Property Set Format, i.e. not a
 * property set stream, or if any other error occurs, an appropriate
 * exception is thrown.
 * 
 * 
 * A [PropertySet] has a list of [Section]s, and each
 * [Section] has a [Property] array. Use [ ][.getSections] to retrieve the [Section]s, then call [ ][Section.getProperties] for each [Section] to get hold of the
 * [Property] arrays. Since the vast majority of [ ]s contains only a single [Section], the
 * convenience method [.getProperties] returns the properties of
 * a [PropertySet]'s [Section] (throwing a [ ] if the [PropertySet] contains more
 * (or less) than exactly one [Section]).
 * 
 * @author Rainer Klute [&lt;klute@rainer-klute.de&gt;](mailto:klute@rainer-klute.de)
 * @author Drew Varner (Drew.Varner hanginIn sc.edu)
 */
open class PropertySet {
    /**
     * Specifies this [PropertySet]'s byte order. See the
     * HPFS documentation for details!
     */
    @JvmField
    protected var byteOrder: Int = 0

    /**
     * Returns the property set stream's low-level "byte order"
     * field. It is always `0xFFFE` .
     */
    open fun getByteOrder(): Int {
        return byteOrder
    }

    /**
     * Specifies this [PropertySet]'s format. See the HPFS
     * documentation for details!
     */
    @JvmField
    protected var format: Int = 0

    /**
     * Returns the property set stream's low-level "format"
     * field. It is always `0x0000` .
     */
    open fun getFormat(): Int {
        return format
    }

    /**
     * Specifies the version of the operating system that created
     * this [PropertySet]. See the HPFS documentation for
     * details!
     */
    @JvmField
    protected var osVersion: Int = 0

    /**
     * Returns the property set stream's low-level "OS version"
     * field.
     */
    open fun getOSVersion(): Int {
        return osVersion
    }

    /**
     * Specifies this [PropertySet]'s "classID" field. See
     * the HPFS documentation for details!
     */
    @JvmField
    protected var classID: ClassID? = null

    /**
     * Returns the property set stream's low-level "class ID"
     * field.
     */
    open fun getClassID(): ClassID? {
        return classID
    }

    /**
     * Returns the number of [Section]s in the property
     * set.
     */
    open fun getSectionCount(): Int {
        return sections!!.size
    }

    /**
     * The sections in this [PropertySet].
     */
    @JvmField
    protected var sections: MutableList<Section>? = null

    /**
     * Returns the [Section]s in the property set.
     */
    open fun getSections(): MutableList<Section>? {
        return sections
    }

    /**
     * Creates an empty (uninitialized) [PropertySet].
     *
     * **Please note:** For the time being this
     * constructor is protected since it is used for internal purposes
     * only, but expect it to become public once the property set's
     * writing functionality is implemented.
     */
    protected constructor()


    /**
     * 
     * Creates a [PropertySet] instance from an [ ] in the Horrible Property Set Format.
     * 
     * 
     * The constructor reads the first few bytes from the stream
     * and determines whether it is really a property set stream. If
     * it is, it parses the rest of the stream. If it is not, it
     * resets the stream to its beginning in order to let other
     * components mess around with the data and throws an
     * exception.
     * 
     * @param stream Holds the data making out the property set
     * stream.
     * @throws MarkUnsupportedException if the stream does not support
     * the [InputStream.markSupported] method.
     * @throws IOException if the [InputStream] cannot not be
     * accessed as needed.
     * @exception NoPropertySetStreamException if the input stream does not
     * contain a property set.
     * @exception UnsupportedEncodingException if a character encoding is not
     * supported.
     */
    constructor(stream: InputStream) {
        if (isPropertySetStream(stream)) {
            val avail = stream.available()
            val buffer = ByteArray(avail)
            stream.read(buffer, 0, buffer.size)
            init(buffer, 0, buffer.size)
        } else throw NoPropertySetStreamException()
    }


    /**
     * 
     * Creates a [PropertySet] instance from a byte array
     * that represents a stream in the Horrible Property Set
     * Format.
     * 
     * @param stream The byte array holding the stream data.
     * @param offset The offset in <var>stream</var> where the stream
     * data begin. If the stream data begin with the first byte in the
     * array, the <var>offset</var> is 0.
     * @param length The length of the stream data.
     * @throws NoPropertySetStreamException if the byte array is not a
     * property set stream.
     * 
     * @exception UnsupportedEncodingException if the codepage is not supported.
     */
    /**
     * 
     * Creates a [PropertySet] instance from a byte array
     * that represents a stream in the Horrible Property Set
     * Format.
     * 
     * @param stream The byte array holding the stream data. The
     * complete byte array contents is the stream data.
     * @throws NoPropertySetStreamException if the byte array is not a
     * property set stream.
     * 
     * @exception UnsupportedEncodingException if the codepage is not supported.
     */
    @JvmOverloads
    constructor(stream: ByteArray, offset: Int = 0, length: Int = stream.size) {
        if (isPropertySetStream(stream, offset, length)) init(stream, offset, length)
        else throw NoPropertySetStreamException()
    }


    /**
     * 
     * Initializes this [PropertySet] instance from a byte
     * array. The method assumes that it has been checked already that
     * the byte array indeed represents a property set stream. It does
     * no more checks on its own.
     * 
     * @param src Byte array containing the property set stream
     * @param offset The property set stream starts at this offset
     * from the beginning of <var>src</var>
     * @param length Length of the property set stream.
     * @throws UnsupportedEncodingException if HPSF does not (yet) support the
     * property set's character encoding.
     */
    @Throws(UnsupportedEncodingException::class)
    private fun init(src: ByteArray, offset: Int, length: Int) {
        /* FIXME (3): Ensure that at most "length" bytes are read. */

        /*
         * Read the stream's header fields.
         */

        var o = offset
        byteOrder = getUShort(src, o)
        o += LittleEndianConsts.SHORT_SIZE
        format = getUShort(src, o)
        o += LittleEndianConsts.SHORT_SIZE
        this.osVersion = getUInt(src, o).toInt()
        o += LittleEndianConsts.INT_SIZE
        classID = ClassID(src, o)
        o += ClassID.Companion.LENGTH
        val sectionCount = getInt(src, o)
        o += LittleEndianConsts.INT_SIZE
        if (sectionCount < 0) throw HPSFRuntimeException(
            "Section count " + sectionCount +
                    " is negative."
        )

        /*
         * Read the sections, which are following the header. They
         * start with an array of section descriptions. Each one
         * consists of a format ID telling what the section contains
         * and an offset telling how many bytes from the start of the
         * stream the section begins.
         */
        /*
         * Most property sets have only one section. The Document
         * Summary Information stream has 2. Everything else is a rare
         * exception and is no longer fostered by Microsoft.
         */
        val sections = ArrayList<Section>(sectionCount)
        this.sections = sections

        /*
         * Loop over the section descriptor array. Each descriptor
         * consists of a ClassID and a DWord, and we have to increment
         * "offset" accordingly.
         */
        for (i in 0..<sectionCount) {
            val s = Section(src, o)
            o += ClassID.Companion.LENGTH + LittleEndianConsts.INT_SIZE
            sections.add(s)
        }
    }


    /**
         * 
         * Checks whether this [PropertySet] represents a Summary
         * Information.
         * 
         * @return `true` if this [PropertySet]
         * represents a Summary Information, else `false`.
         */
    open fun isSummaryInformation(): Boolean {
        val sections = this.sections!!
        if (sections.size <= 0) return false
        return Util.equal(
            sections.get(0).getFormatID()!!.getBytes(),
            SectionIDMap.SUMMARY_INFORMATION_ID
        )
    }


    /**
         * 
         * Checks whether this [PropertySet] is a Document
         * Summary Information.
         * 
         * @return `true` if this [PropertySet]
         * represents a Document Summary Information, else `false`.
         */
    open fun isDocumentSummaryInformation(): Boolean {
        val sections = this.sections!!
        if (sections.size <= 0) return false
        return Util.equal(
            sections.get(0).getFormatID()!!.getBytes(),
            SectionIDMap.DOCUMENT_SUMMARY_INFORMATION_ID[0]
        )
    }


    /**
         * 
         * Convenience method returning the [Property] array
         * contained in this property set. It is a shortcut for getting
         * the [PropertySet]'s [Section]s list and then
         * getting the [Property] array from the first [ ].
         * 
         * @return The properties of the only [Section] of this
         * [PropertySet].
         * @throws NoSingleSectionException if the [PropertySet] has
         * more or less than one [Section].
         */
    @Throws(NoSingleSectionException::class)
    open fun getProperties(): Array<Property>? {
        return getFirstSection()!!.getProperties()
    }


    /**
     * 
     * Convenience method returning the value of the property with
     * the specified ID. If the property is not available,
     * `null` is returned and a subsequent call to [ ][.wasNull] will return `true` .
     * 
     * @param id The property ID
     * @return The property value
     * @throws NoSingleSectionException if the [PropertySet] has
     * more or less than one [Section].
     */
    @Throws(NoSingleSectionException::class)
    open fun getProperty(id: Int): Any? {
        return getFirstSection()!!.getProperty(id.toLong())
    }


    /**
     * 
     * Convenience method returning the value of a boolean property
     * with the specified ID. If the property is not available,
     * `false` is returned. A subsequent call to [ ][.wasNull] will return `true` to let the caller
     * distinguish that case from a real property value of
     * `false`.
     * 
     * @param id The property ID
     * @return The property value
     * @throws NoSingleSectionException if the [PropertySet] has
     * more or less than one [Section].
     */
    @Throws(NoSingleSectionException::class)
    open fun getPropertyBooleanValue(id: Int): Boolean {
        return getFirstSection()!!.getPropertyBooleanValue(id)
    }


    /**
     * 
     * Convenience method returning the value of the numeric
     * property with the specified ID. If the property is not
     * available, 0 is returned. A subsequent call to [.wasNull]
     * will return `true` to let the caller distinguish
     * that case from a real property value of 0.
     * 
     * @param id The property ID
     * @return The propertyIntValue value
     * @throws NoSingleSectionException if the [PropertySet] has
     * more or less than one [Section].
     */
    @Throws(NoSingleSectionException::class)
    open fun getPropertyIntValue(id: Int): Int {
        return getFirstSection()!!.getPropertyIntValue(id.toLong())
    }


    /**
     * 
     * Checks whether the property which the last call to [ ][.getPropertyIntValue] or [.getProperty] tried to access
     * was available or not. This information might be important for
     * callers of [.getPropertyIntValue] since the latter
     * returns 0 if the property does not exist. Using [ ][.wasNull], the caller can distiguish this case from a
     * property's real value of 0.
     * 
     * @return `true` if the last call to [ ][.getPropertyIntValue] or [.getProperty] tried to access a
     * property that was not available, else `false`.
     * @throws NoSingleSectionException if the [PropertySet] has
     * more than one [Section].
     */
    @Throws(NoSingleSectionException::class)
    open fun wasNull(): Boolean {
        return getFirstSection()!!.wasNull()
    }


    /**
         * 
         * Gets the [PropertySet]'s first section.
         * 
         * @return The [PropertySet]'s first section.
         */
    open fun getFirstSection(): Section? {
        if (getSectionCount() < 1) throw MissingSectionException("Property set does not contain any sections.")
        return sections!!.get(0)
    }


    /**
         * 
         * If the [PropertySet] has only a single section this
         * method returns it.
         * 
         * @return The singleSection value
         */
    fun getSingleSection(): Section? {
        val sectionCount = getSectionCount()
        if (sectionCount != 1) throw NoSingleSectionException("Property set contains " + sectionCount + " sections.")
        return sections!!.get(0)
    }


    /**
     * 
     * Returns `true` if the `PropertySet` is equal
     * to the specified parameter, else `false`.
     * 
     * @param o the object to compare this `PropertySet` with
     * 
     * @return `true` if the objects are equal, `false`
     * if not
     */
    override fun equals(o: Any?): Boolean {
        if (o == null || o !is PropertySet) return false
        val ps = o
        val byteOrder1 = ps.getByteOrder()
        val byteOrder2 = getByteOrder()
        val classID1 = ps.getClassID()
        val classID2 = getClassID()
        val format1 = ps.getFormat()
        val format2 = getFormat()
        val osVersion1: Int = ps.getOSVersion()
        val osVersion2: Int = getOSVersion()
        val sectionCount1 = ps.getSectionCount()
        val sectionCount2 = getSectionCount()
        if (byteOrder1 != byteOrder2 || (classID1 != classID2) || format1 != format2 || osVersion1 != osVersion2 || sectionCount1 != sectionCount2) return false

        /* Compare the sections: */
        return Util.equals(getSections()!!, ps.getSections()!!)
    }


    /**
     * @see Object.hashCode
     */
    override fun hashCode(): Int {
        throw UnsupportedOperationException("FIXME: Not yet implemented.")
    }


    /**
     * @see Object.toString
     */
    override fun toString(): String {
        val b = StringBuffer()
        val sectionCount = getSectionCount()
        b.append(javaClass.getName())
        b.append('[')
        b.append("byteOrder: ")
        b.append(getByteOrder())
        b.append(", classID: ")
        b.append(getClassID())
        b.append(", format: ")
        b.append(getFormat())
        b.append(", OSVersion: ")
        b.append(getOSVersion())
        b.append(", sectionCount: ")
        b.append(sectionCount)
        b.append(", sections: [\n")
        val sections = getSections()!!
        for (i in 0..<sectionCount) b.append(sections.get(i).toString())
        b.append(']')
        b.append(']')
        return b.toString()
    }

    companion object {
        /**
         * 
         * The "byteOrder" field must equal this value.
         */
        val BYTE_ORDER_ASSERTION: ByteArray = byteArrayOf(0xFE.toByte(), 0xFF.toByte())

        /**
         * 
         * The "format" field must equal this value.
         */
        val FORMAT_ASSERTION: ByteArray = byteArrayOf(0x00.toByte(), 0x00.toByte())

        /**
         * 
         * If the OS version field holds this value the property set stream was
         * created on a 16-bit Windows system.
         */
        const val OS_WIN16: Int = 0x0000

        /**
         * 
         * If the OS version field holds this value the property set stream was
         * created on a Macintosh system.
         */
        const val OS_MACINTOSH: Int = 0x0001

        /**
         * 
         * If the OS version field holds this value the property set stream was
         * created on a 32-bit Windows system.
         */
        const val OS_WIN32: Int = 0x0002

        /**
         * 
         * Checks whether an [InputStream] is in the Horrible
         * Property Set Format.
         * 
         * @param stream The [InputStream] to check. In order to
         * perform the check, the method reads the first bytes from the
         * stream. After reading, the stream is reset to the position it
         * had before reading. The [InputStream] must support the
         * [InputStream.mark] method.
         * @return `true` if the stream is a property set
         * stream, else `false`.
         * @throws MarkUnsupportedException if the [InputStream]
         * does not support the [InputStream.mark] method.
         * @exception IOException if an I/O error occurs
         */
        @Throws(MarkUnsupportedException::class, IOException::class)
        fun isPropertySetStream(stream: InputStream): Boolean {
            /*
         * Read at most this many bytes.
         */
            val BUFFER_SIZE = 50

            /*
         * Mark the current position in the stream so that we can
         * reset to this position if the stream does not contain a
         * property set.
         */
            if (!stream.markSupported()) throw MarkUnsupportedException(stream.javaClass.getName())
            stream.mark(BUFFER_SIZE)

            /*
         * Read a couple of bytes from the stream.
         */
            val buffer = ByteArray(BUFFER_SIZE)
            val bytes =
                stream.read(
                    buffer, 0,
                    min(buffer.size, stream.available())
                )
            val isPropertySetStream: Boolean =
                isPropertySetStream(buffer, 0, bytes)
            stream.reset()
            return isPropertySetStream
        }


        /**
         * 
         * Checks whether a byte array is in the Horrible Property Set
         * Format.
         * 
         * @param src The byte array to check.
         * @param offset The offset in the byte array.
         * @param length The significant number of bytes in the byte
         * array. Only this number of bytes will be checked.
         * @return `true` if the byte array is a property set
         * stream, `false` if not.
         */
        fun isPropertySetStream(
            src: ByteArray,
            offset: Int,
            length: Int
        ): Boolean {
            /* FIXME (3): Ensure that at most "length" bytes are read. */

            /*
         * Read the header fields of the stream. They must always be
         * there.
         */

            var o = offset
            val byteOrder = getUShort(src, o)
            o += LittleEndianConsts.SHORT_SIZE
            var temp = ByteArray(LittleEndianConsts.SHORT_SIZE)
            putShort(temp, byteOrder.toShort())
            if (!Util.equal(temp, BYTE_ORDER_ASSERTION)) return false
            val format = getUShort(src, o)
            o += LittleEndianConsts.SHORT_SIZE
            temp = ByteArray(LittleEndianConsts.SHORT_SIZE)
            putShort(temp, format.toShort())
            if (!Util.equal(temp, FORMAT_ASSERTION)) return false
            // final long osVersion = LittleEndian.getUInt(src, offset);
            o += LittleEndianConsts.INT_SIZE
            // final ClassID classID = new ClassID(src, offset);
            o += ClassID.Companion.LENGTH
            val sectionCount = getUInt(src, o)
            o += LittleEndianConsts.INT_SIZE
            if (sectionCount < 0) return false
            return true
        }
    }
}
