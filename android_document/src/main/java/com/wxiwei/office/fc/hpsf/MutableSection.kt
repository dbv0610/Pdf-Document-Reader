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
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.Collections
import java.util.Date
import java.util.LinkedList

/**
 * 
 * Adds writing capability to the [Section] class.
 * 
 * 
 * Please be aware that this class' functionality will be merged into the
 * [Section] class at a later time, so the API will change.
 */
class MutableSection : Section {
    /**
     * 
     * If the "dirty" flag is true, the section's size must be
     * (re-)calculated before the section is written.
     */
    private var dirty = true


    /**
     * 
     * List to assemble the properties. Unfortunately a wrong
     * decision has been taken when specifying the "properties" field
     * as an Property[]. It should have been a [List].
     */
    private var preprops: MutableList<Property>? = null


    /**
     * 
     * Contains the bytes making out the section. This byte array is
     * established when the section's size is calculated and can be reused
     * later. It is valid only if the "dirty" flag is false.
     */
    private var sectionBytes: ByteArray? = null


    /**
     * 
     * Creates an empty mutable section.
     */
    constructor() {
        dirty = true
        formatID = null
        offset = -1
        preprops = LinkedList<Property>()
    }


    /**
     * 
     * Constructs a `MutableSection` by doing a deep copy of an
     * existing `Section`. All nested `Property`
     * instances, will be their mutable counterparts in the new
     * `MutableSection`.
     * 
     * @param s The section set to copy
     */
    constructor(s: Section) {
        setFormatID(s.getFormatID())
        val pa = s.getProperties()
        val mpa = Array<Property>(pa.size) { i -> MutableProperty(pa[i]) }
        setProperties(mpa)
        setDictionary(s.getDictionary())
    }


    /**
     * 
     * Sets the section's format ID.
     * 
     * @param formatID The section's format ID
     * 
     * @see .setFormatID
     * @see getFormatID
     */
    fun setFormatID(formatID: ClassID?) {
        this.formatID = formatID
    }


    /**
     * 
     * Sets the section's format ID.
     * 
     * @param formatID The section's format ID as a byte array. It components
     * are in big-endian format.
     * 
     * @see .setFormatID
     * @see getFormatID
     */
    fun setFormatID(formatID: ByteArray) {
        var fid = getFormatID()
        if (fid == null) {
            fid = ClassID()
            setFormatID(fid)
        }
        fid.setBytes(formatID)
    }


    /**
     * 
     * Sets this section's properties. Any former values are overwritten.
     * 
     * @param properties This section's new properties.
     */
    fun setProperties(properties: Array<Property>) {
        this.properties = properties
        preprops = LinkedList<Property>()
        for (i in properties.indices) preprops!!.add(properties[i])
        dirty = true
    }


    /**
     * 
     * Sets the string value of the property with the specified ID.
     * 
     * @param id The property's ID
     * @param value The property's value. It will be written as a Unicode
     * string.
     * 
     * @see .setProperty
     * @see .getProperty
     */
    fun setProperty(id: Int, value: String?) {
        setProperty(id, Variant.Companion.VT_LPWSTR.toLong(), value)
        dirty = true
    }


    /**
     * 
     * Sets the int value of the property with the specified ID.
     * 
     * @param id The property's ID
     * @param value The property's value.
     * 
     * @see .setProperty
     * @see .getProperty
     */
    fun setProperty(id: Int, value: Int) {
        setProperty(id, Variant.Companion.VT_I4.toLong(), value)
        dirty = true
    }


    /**
     * 
     * Sets the long value of the property with the specified ID.
     * 
     * @param id The property's ID
     * @param value The property's value.
     * 
     * @see .setProperty
     * @see .getProperty
     */
    fun setProperty(id: Int, value: Long) {
        setProperty(id, Variant.Companion.VT_I8.toLong(), value)
        dirty = true
    }


    /**
     * 
     * Sets the boolean value of the property with the specified ID.
     * 
     * @param id The property's ID
     * @param value The property's value.
     * 
     * @see .setProperty
     * @see .getProperty
     */
    fun setProperty(id: Int, value: Boolean) {
        setProperty(id, Variant.Companion.VT_BOOL.toLong(), value)
        dirty = true
    }


    /**
     * 
     * Sets the value and the variant type of the property with the
     * specified ID. If a property with this ID is not yet present in
     * the section, it will be added. An already present property with
     * the specified ID will be overwritten. A default mapping will be
     * used to choose the property's type.
     * 
     * @param id The property's ID.
     * @param variantType The property's variant type.
     * @param value The property's value.
     * 
     * @see .setProperty
     * @see .getProperty
     * 
     * @see Variant
     */
    fun setProperty(
        id: Int, variantType: Long,
        value: Any?
    ) {
        val p = MutableProperty()
        p.setID(id.toLong())
        p.setType(variantType)
        p.setValue(value)
        setProperty(p)
        dirty = true
    }


    /**
     * 
     * Sets a property.
     * 
     * @param p The property to be set.
     * 
     * @see .setProperty
     * @see .getProperty
     * 
     * @see Variant
     */
    fun setProperty(p: Property) {
        val id = p.getID()
        removeProperty(id)
        preprops!!.add(p)
        dirty = true
    }


    /**
     * 
     * Removes a property.
     * 
     * @param id The ID of the property to be removed
     */
    fun removeProperty(id: Long) {
        val i = preprops!!.iterator()
        while (i.hasNext()) {
            if (i.next().getID() == id) {
                i.remove()
                break
            }
        }
        dirty = true
    }


    /**
     * 
     * Sets the value of the boolean property with the specified
     * ID.
     * 
     * @param id The property's ID
     * @param value The property's value
     * 
     * @see .setProperty
     * @see .getProperty
     * 
     * @see Variant
     */
    protected fun setPropertyBooleanValue(id: Int, value: Boolean) {
        setProperty(id, Variant.Companion.VT_BOOL.toLong(), value)
    }


    /**
     * 
     * Returns the section's size.
     * 
     * @return the section's size.
     */
    override fun getSize(): Int {
        if (dirty) {
            try {
                size = calcSize()
                dirty = false
            } catch (ex: HPSFRuntimeException) {
                throw ex
            } catch (ex: Exception) {
                throw HPSFRuntimeException(ex)
            }
        }
        return size
    }


    /**
     * 
     * Calculates the section's size. It is the sum of the lengths of the
     * section's header (8), the properties list (16 times the number of
     * properties) and the properties themselves.
     * 
     * @return the section's length in bytes.
     * @throws WritingNotSupportedException
     * @throws IOException
     */
    @Throws(WritingNotSupportedException::class, IOException::class)
    private fun calcSize(): Int {
        val out = ByteArrayOutputStream()
        write(out)
        out.close()
        /* Pad to multiple of 4 bytes so that even the Windows shell (explorer)
         * shows custom properties. */
        sectionBytes = Util.pad4(out.toByteArray())
        return sectionBytes!!.size
    }


    /**
     * 
     * Writes this section into an output stream.
     * 
     * 
     * Internally this is done by writing into three byte array output
     * streams: one for the properties, one for the property list and one for
     * the section as such. The two former are appended to the latter when they
     * have received all their data.
     * 
     * @param out The stream to write into.
     * 
     * @return The number of bytes written, i.e. the section's size.
     * @exception IOException if an I/O error occurs
     * @exception WritingNotSupportedException if HPSF does not yet support
     * writing a property's variant type.
     */
    @Throws(WritingNotSupportedException::class, IOException::class)
    fun write(out: OutputStream): Int {
        /* Check whether we have already generated the bytes making out the
         * section. */
        val sectionBytes = this.sectionBytes
        if (!dirty && sectionBytes != null) {
            out.write(sectionBytes)
            return sectionBytes.size
        }

        /* The properties are written to this stream. */
        val propertyStream =
            ByteArrayOutputStream()

        /* The property list is established here. After each property that has
         * been written to "propertyStream", a property list entry is written to
         * "propertyListStream". */
        val propertyListStream =
            ByteArrayOutputStream()

        /* Maintain the current position in the list. */
        var position = 0

        /* Increase the position variable by the size of the property list so
         * that it points behind the property list and to the beginning of the
         * properties themselves. */
        position += 2 * LittleEndianConsts.INT_SIZE +
                getPropertyCount() * 2 * LittleEndianConsts.INT_SIZE

        /* Writing the section's dictionary it tricky. If there is a dictionary
         * (property 0) the codepage property (property 1) must be set, too. */
        var codepage = -1
        if (getProperty(PropertyIDMap.Companion.PID_DICTIONARY.toLong()) != null) {
            val p1 = getProperty(PropertyIDMap.Companion.PID_CODEPAGE.toLong())
            if (p1 != null) {
                if (p1 !is Int) throw IllegalPropertySetDataException(
                    "The codepage property (ID = 1) must be an " +
                            "Integer object."
                )
            } else  /* Warning: The codepage property is not set although a
                 * dictionary is present. In order to cope with this problem we
                 * add the codepage property and set it to Unicode. */
                setProperty(
                    PropertyIDMap.Companion.PID_CODEPAGE, Variant.Companion.VT_I2.toLong(),
                    Constants.CP_UNICODE
                )
            codepage = this.codepage
        }

        /* Sort the property list by their property IDs: */
        Collections.sort(preprops!!, object : Comparator<Property> {
            override fun compare(p1: Property, p2: Property): Int {
                if (p1.getID() < p2.getID()) return -1
                else if (p1.getID() == p2.getID()) return 0
                else return 1
            }
        })

        /* Write the properties and the property list into their respective
         * streams: */
        val i = preprops!!.listIterator()
        while (i.hasNext()) {
            val p = i.next() as MutableProperty
            val id = p.getID()

            /* Write the property list entry. */
            TypeWriter.writeUIntToStream(propertyListStream, p.getID())
            TypeWriter.writeUIntToStream(propertyListStream, position.toLong())

            /* If the property ID is not equal 0 we write the property and all
             * is fine. However, if it equals 0 we have to write the section's
             * dictionary which has an implicit type only and an explicit
             * value. */
            if (id != 0L)  /* Write the property and update the position to the next
                 * property. */
                position += p.write(propertyStream, this.codepage)
            else {
                if (codepage == -1) throw IllegalPropertySetDataException("Codepage (property 1) is undefined.")
                position += writeDictionary(
                    propertyStream, dictionary!!,
                    codepage
                )
            }
        }
        propertyStream.close()
        propertyListStream.close()

        /* Write the section: */
        val pb1 = propertyListStream.toByteArray()
        val pb2 = propertyStream.toByteArray()

        /* Write the section's length: */
        TypeWriter.writeToStream(
            out, LittleEndianConsts.INT_SIZE * 2 +
                    pb1.size + pb2.size
        )

        /* Write the section's number of properties: */
        TypeWriter.writeToStream(out, getPropertyCount())

        /* Write the property list: */
        out.write(pb1)

        /* Write the properties: */
        out.write(pb2)

        val streamLength: Int = LittleEndianConsts.INT_SIZE * 2 + pb1.size + pb2.size
        return streamLength
    }


    /**
     * 
     * Overwrites the super class' method to cope with a redundancy:
     * the property count is maintained in a separate member variable, but
     * shouldn't.
     * 
     * @return The number of properties in this section
     */
    override fun getPropertyCount(): Int {
        return preprops!!.size
    }


    /**
     * 
     * Gets this section's properties.
     * 
     * @return this section's properties.
     */
    override fun getProperties(): Array<Property> {
        val props = preprops!!.toTypedArray()
        properties = props
        return props
    }


    /**
     * 
     * Gets a property.
     * 
     * @param id The ID of the property to get
     * @return The property or `null` if there is no such property
     */
    override fun getProperty(id: Long): Any? {
        /* Calling getProperties() ensures that properties and preprops are in
         * sync.</p> */
        getProperties()
        return super.getProperty(id)
    }


    /**
     * 
     * Sets the section's dictionary. All keys in the dictionary must be
     * [Long] instances, all values must be
     * [String]s. This method overwrites the properties with IDs
     * 0 and 1 since they are reserved for the dictionary and the dictionary's
     * codepage. Setting these properties explicitly might have surprising
     * effects. An application should never do this but always use this
     * method.
     * 
     * @param dictionary The dictionary
     * 
     * @exception IllegalPropertySetDataException if the dictionary's key and
     * value types are not correct.
     * 
     * @see getDictionary
     */
    @Throws(IllegalPropertySetDataException::class)
    fun setDictionary(dictionary: MutableMap<Long, String>?) {
        if (dictionary != null) {
            this.dictionary = dictionary

            /* Set the dictionary property (ID 0). Please note that the second
             * parameter in the method call below is unused because dictionaries
             * don't have a type. */
            setProperty(PropertyIDMap.Companion.PID_DICTIONARY, -1L, dictionary)

            /* If the codepage property (ID 1) for the strings (keys and
             * values) used in the dictionary is not yet defined, set it to
             * Unicode. */
            val codepage =
                getProperty(PropertyIDMap.Companion.PID_CODEPAGE.toLong()) as Int?
            if (codepage == null) setProperty(
                PropertyIDMap.Companion.PID_CODEPAGE, Variant.Companion.VT_I2.toLong(),
                Constants.CP_UNICODE
            )
        } else  /* Setting the dictionary to null means to remove property 0.
             * However, it does not mean to remove property 1 (codepage). */
            removeProperty(PropertyIDMap.Companion.PID_DICTIONARY.toLong())
    }


    /**
     * 
     * Sets a property.
     * 
     * @param id The property ID.
     * @param value The property's value. The value's class must be one of those
     * supported by HPSF.
     */
    fun setProperty(id: Int, value: Any) {
        if (value is String) setProperty(id, value as String?)
        else if (value is Long) setProperty(id, value)
        else if (value is Int) setProperty(id, value)
        else if (value is Short) setProperty(id, value.toInt())
        else if (value is Boolean) setProperty(id, value)
        else if (value is Date) setProperty(id, Variant.Companion.VT_FILETIME.toLong(), value)
        else throw HPSFRuntimeException(
            "HPSF does not support properties of type " +
                    value.javaClass.getName() + "."
        )
    }


    /**
     * 
     * Removes all properties from the section including 0 (dictionary) and
     * 1 (codepage).
     */
    fun clear() {
        val properties = getProperties()
        for (i in properties.indices) {
            val p = properties[i]
            removeProperty(p.getID())
        }
    }

    /**
     * 
     * Sets the codepage.
     * 
     * @param codepage the codepage
     */
    fun setCodepage(codepage: Int) {
        setProperty(
            PropertyIDMap.Companion.PID_CODEPAGE, Variant.Companion.VT_I2.toLong(),
            codepage
        )
    }

    companion object {
        /**
         * 
         * Writes the section's dictionary.
         * 
         * @param out The output stream to write to.
         * @param dictionary The dictionary.
         * @param codepage The codepage to be used to write the dictionary items.
         * @return The number of bytes written
         * @exception IOException if an I/O exception occurs.
         */
        @Throws(IOException::class)
        private fun writeDictionary(
            out: OutputStream,
            dictionary: MutableMap<Long, String>, codepage: Int
        ): Int {
            var length = TypeWriter.writeUIntToStream(out, dictionary.size.toLong())
            val i: MutableIterator<Long> = dictionary.keys.iterator()
            while (i.hasNext()) {
                val key = i.next()
                val value = dictionary.get(key)

                if (codepage == Constants.CP_UNICODE) {
                    /* Write the dictionary item in Unicode. */
                    var sLength = value!!.length + 1
                    if (sLength % 2 == 1) sLength++
                    length += TypeWriter.writeUIntToStream(out, key)
                    length += TypeWriter.writeUIntToStream(out, sLength.toLong())
                    val ca =
                        value.toByteArray(
                            charset(
                                VariantSupport.Companion.codepageToEncoding(
                                    codepage
                                )
                            )
                        )
                    var j = 2
                    while (j < ca.size) {
                        out.write(ca[j + 1].toInt())
                        out.write(ca[j].toInt())
                        length += 2
                        j += 2
                    }
                    sLength -= value.length
                    while (sLength > 0) {
                        out.write(0x00)
                        out.write(0x00)
                        length += 2
                        sLength--
                    }
                } else {
                    /* Write the dictionary item in another codepage than
                 * Unicode. */
                    length += TypeWriter.writeUIntToStream(out, key)
                    length += TypeWriter.writeUIntToStream(out, (value!!.length + 1).toLong())
                    val ba =
                        value.toByteArray(
                            charset(
                                VariantSupport.Companion.codepageToEncoding(
                                    codepage
                                )
                            )
                        )
                    for (j in ba.indices) {
                        out.write(ba[j].toInt())
                        length++
                    }
                    out.write(0x00)
                    length++
                }
            }
            return length
        }
    }
}
