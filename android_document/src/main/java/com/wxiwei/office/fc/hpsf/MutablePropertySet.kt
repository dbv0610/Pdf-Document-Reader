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

import com.wxiwei.office.fc.poifs.filesystem.DirectoryEntry
import com.wxiwei.office.fc.util.LittleEndian
import com.wxiwei.office.fc.util.LittleEndian.getUShort
import com.wxiwei.office.fc.util.LittleEndianConsts
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.util.LinkedList

/**
 * 
 * Adds writing support to the [PropertySet] class.
 * 
 * 
 * Please be aware that this class' functionality will be merged into the
 * [PropertySet] class at a later time, so the API will change.
 * 
 * @author Rainer Klute [&lt;klute@rainer-klute.de&gt;](mailto:klute@rainer-klute.de)
 */
open class MutablePropertySet : PropertySet {
    /**
     * 
     * Constructs a `MutablePropertySet` instance. Its
     * primary task is to initialize the immutable field with their proper
     * values. It also sets fields that might change to reasonable defaults.
     */
    constructor() {
        /* Initialize the "byteOrder" field. */
        byteOrder = getUShort(PropertySet.Companion.BYTE_ORDER_ASSERTION)

        /* Initialize the "format" field. */
        format = getUShort(PropertySet.Companion.FORMAT_ASSERTION)

        /* Initialize "osVersion" field as if the property has been created on
         * a Win32 platform, whether this is the case or not. */
        osVersion = (PropertySet.Companion.OS_WIN32 shl 16) or 0x0A04

        /* Initailize the "classID" field. */
        classID = ClassID()

        /* Initialize the sections. Since property set must have at least
         * one section it is added right here. */
        val sections = LinkedList<Section>()
        sections.add(MutableSection())
        this.sections = sections
    }


    /**
     * 
     * Constructs a `MutablePropertySet` by doing a deep copy of
     * an existing `PropertySet`. All nested elements, i.e.
     * `Section`s and `Property` instances, will be their
     * mutable counterparts in the new `MutablePropertySet`.
     * 
     * @param ps The property set to copy
     */
    constructor(ps: PropertySet) {
        byteOrder = ps.getByteOrder()
        format = ps.getFormat()
        osVersion = ps.getOSVersion()
        setClassID(ps.getClassID())
        clearSections()
        if (sections == null) sections = LinkedList<Section>()
        val i = ps.getSections()!!.iterator()
        while (i.hasNext()) {
            val s = MutableSection(i.next())
            addSection(s)
        }
    }


    /**
     * 
     * The length of the property set stream header.
     */
    private val OFFSET_HEADER: Int =
        PropertySet.Companion.BYTE_ORDER_ASSERTION.size +  /* Byte order    */
                PropertySet.Companion.FORMAT_ASSERTION.size +  /* Format        */
                LittleEndianConsts.INT_SIZE +  /* OS version    */
                ClassID.Companion.LENGTH +  /* Class ID      */
                LittleEndianConsts.INT_SIZE /* Section count */


    /**
     * 
     * Sets the "byteOrder" property.
     * 
     * @param byteOrder the byteOrder value to set
     */
    open fun setByteOrder(byteOrder: Int) {
        this.byteOrder = byteOrder
    }


    /**
     * 
     * Sets the "format" property.
     * 
     * @param format the format value to set
     */
    open fun setFormat(format: Int) {
        this.format = format
    }


    /**
     * 
     * Sets the "osVersion" property.
     * 
     * @param osVersion the osVersion value to set
     */
    open fun setOSVersion(osVersion: Int) {
        this.osVersion = osVersion
    }


    /**
     * 
     * Sets the property set stream's low-level "class ID"
     * field.
     * 
     * @param classID The property set stream's low-level "class ID" field.
     * 
     * @see getClassID
     */
    open fun setClassID(classID: ClassID?) {
        this.classID = classID
    }


    /**
     * 
     * Removes all sections from this property set.
     */
    open fun clearSections() {
        sections = null
    }


    /**
     * 
     * Adds a section to this property set.
     * 
     * @param section The [Section] to add. It will be appended
     * after any sections that are already present in the property set
     * and thus become the last section.
     */
    open fun addSection(section: Section) {
        val sections = this.sections ?: LinkedList<Section>().also { this.sections = it }
        sections.add(section)
    }


    /**
     * 
     * Writes the property set to an output stream.
     * 
     * @param out the output stream to write the section to
     * @exception IOException if an error when writing to the output stream
     * occurs
     * @exception WritingNotSupportedException if HPSF does not yet support
     * writing a property's variant type.
     */
    @Throws(WritingNotSupportedException::class, IOException::class)
    open fun write(out: OutputStream) {
        /* Write the number of sections in this property set stream. */
        val sections = this.sections!!
        val nrSections = sections.size
        var length = 0

        /* Write the property set's header. */
        length += TypeWriter.writeToStream(out, getByteOrder().toShort())
        length += TypeWriter.writeToStream(out, getFormat().toShort())
        length += TypeWriter.writeToStream(out, getOSVersion())
        length += TypeWriter.writeToStream(out, getClassID()!!)
        length += TypeWriter.writeToStream(out, nrSections)
        var offset = OFFSET_HEADER

        /* Write the section list, i.e. the references to the sections. Each
         * entry in the section list consist of the section's class ID and the
         * section's offset relative to the beginning of the stream. */
        offset += nrSections * (ClassID.Companion.LENGTH + LittleEndianConsts.INT_SIZE)
        val sectionsBegin = offset
        run {
            val i = sections.listIterator()
            while (i.hasNext()) {
                val s = i.next() as MutableSection
                val formatID = s.getFormatID()
                if (formatID == null) throw NoFormatIDException()
                length += TypeWriter.writeToStream(out, formatID)
                length += TypeWriter.writeUIntToStream(out, offset.toLong())
                try {
                    offset += s.getSize()
                } catch (ex: HPSFRuntimeException) {
                    val cause = ex.reason
                    if (cause is UnsupportedEncodingException) {
                        throw IllegalPropertySetDataException(cause)
                    }
                    throw ex
                }
            }
        }

        /* Write the sections themselves. */
        offset = sectionsBegin
        val i = sections.listIterator()
        while (i.hasNext()) {
            val s = i.next() as MutableSection
            offset += s.write(out)
        }
    }


    /**
     * 
     * Returns the contents of this property set stream as an input stream.
     * The latter can be used for example to write the property set into a POIFS
     * document. The input stream represents a snapshot of the property set.
     * If the latter is modified while the input stream is still being
     * read, the modifications will not be reflected in the input stream but in
     * the [MutablePropertySet] only.
     * 
     * @return the contents of this property set stream
     * 
     * @throws WritingNotSupportedException if HPSF does not yet support writing
     * of a property's variant type.
     * @throws IOException if an I/O exception occurs.
     */
    @Throws(IOException::class, WritingNotSupportedException::class)
    open fun toInputStream(): InputStream? {
        val psStream = ByteArrayOutputStream()
        write(psStream)
        psStream.close()
        val streamData = psStream.toByteArray()
        return ByteArrayInputStream(streamData)
    }

    /**
     * 
     * Writes a property set to a document in a POI filesystem directory.
     * 
     * @param dir The directory in the POI filesystem to write the document to.
     * @param name The document's name. If there is already a document with the
     * same name in the directory the latter will be overwritten.
     * 
     * @throws WritingNotSupportedException
     * @throws IOException
     */
    @Throws(WritingNotSupportedException::class, IOException::class)
    open fun write(dir: DirectoryEntry, name: String?) {
        /* If there is already an entry with the same name, remove it. */
        try {
            val e = dir.getEntry(name)
            e.delete()
        } catch (ex: FileNotFoundException) {
            /* Entry not found, no need to remove it. */
        }
        /* Create the new entry. */
        dir.createDocument(name, toInputStream())
    }
}
