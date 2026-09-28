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

import com.wxiwei.office.fc.poifs.filesystem.DirectoryEntry
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * 
 * Abstract superclass for the convenience classes [ ] and [DocumentSummaryInformation].
 * 
 * 
 * The motivation behind this class is quite nasty if you look
 * behind the scenes, but it serves the application programmer well by
 * providing him with the easy-to-use [SummaryInformation] and
 * [DocumentSummaryInformation] classes. When parsing the data a
 * property set stream consists of (possibly coming from an [ ]) we want to read and process each byte only
 * once. Since we don't know in advance which kind of property set we
 * have, we can expect only the most general [ ]. Creating a special subclass should be as easy as
 * calling the special subclass' constructor and pass the general
 * [PropertySet] in. To make things easy internally, the special
 * class just holds a reference to the general [PropertySet] and
 * delegates all method calls to it.
 * 
 * 
 * A cleaner implementation would have been like this: The [ ] parses the stream data into some internal
 * object first.  Then it finds out whether the stream is a [ ], a [DocumentSummaryInformation] or a
 * general [PropertySet].  However, the current implementation
 * went the other way round historically: the convenience classes came
 * only late to my mind.
 * 
 * @author Rainer Klute [&lt;klute@rainer-klute.de&gt;](mailto:klute@rainer-klute.de)
 */
abstract class SpecialPropertySet : MutablePropertySet {
    /**
     * The id to name mapping of the properties
     * in this set.
     */
    abstract fun getPropertySetIDMap(): PropertyIDMap?

    /**
     * 
     * The "real" property set `SpecialPropertySet`
     * delegates to.
     */
    private val delegate: MutablePropertySet


    /**
     * 
     * Creates a `SpecialPropertySet`.
     * 
     * @param ps The property set to be encapsulated by the
     * `SpecialPropertySet`
     */
    constructor(ps: PropertySet) {
        delegate = MutablePropertySet(ps)
    }


    /**
     * 
     * Creates a `SpecialPropertySet`.
     * 
     * @param ps The mutable property set to be encapsulated by the
     * `SpecialPropertySet`
     */
    constructor(ps: MutablePropertySet) {
        delegate = ps
    }


    /**
     * @see PropertySet.getByteOrder
     */
    override fun getByteOrder(): Int {
        return delegate.getByteOrder()
    }


    /**
     * @see PropertySet.getFormat
     */
    override fun getFormat(): Int {
        return delegate.getFormat()
    }


    /**
     * @see PropertySet.getOSVersion
     */
    override fun getOSVersion(): Int {
        return delegate.getOSVersion()
    }


    /**
     * @see PropertySet.getClassID
     */
    override fun getClassID(): ClassID? {
        return delegate.getClassID()
    }


    /**
     * @see PropertySet.getSectionCount
     */
    override fun getSectionCount(): Int {
        return delegate.getSectionCount()
    }


    /**
     * @see PropertySet.getSections
     */
    override fun getSections(): MutableList<Section>? {
        return delegate.getSections()
    }


    /**
     * @see PropertySet.isSummaryInformation
     */
    override fun isSummaryInformation(): Boolean {
        return delegate.isSummaryInformation()
    }


    /**
     * @see PropertySet.isDocumentSummaryInformation
     */
    override fun isDocumentSummaryInformation(): Boolean {
        return delegate.isDocumentSummaryInformation()
    }


    /**
     * @see getSingleSection
     */
    override fun getFirstSection(): Section? {
        return delegate.getFirstSection()
    }


    /**
     * @see MutablePropertySet.addSection
     */
    override fun addSection(section: Section) {
        delegate.addSection(section)
    }


    /**
     * @see MutablePropertySet.clearSections
     */
    override fun clearSections() {
        delegate.clearSections()
    }


    /**
     * @see MutablePropertySet.setByteOrder
     */
    override fun setByteOrder(byteOrder: Int) {
        delegate.setByteOrder(byteOrder)
    }


    /**
     * @see MutablePropertySet.setClassID
     */
    override fun setClassID(classID: ClassID?) {
        delegate.setClassID(classID)
    }


    /**
     * @see MutablePropertySet.setFormat
     */
    override fun setFormat(format: Int) {
        delegate.setFormat(format)
    }


    /**
     * @see MutablePropertySet.setOSVersion
     */
    override fun setOSVersion(osVersion: Int) {
        delegate.setOSVersion(osVersion)
    }


    /**
     * @see MutablePropertySet.toInputStream
     */
    @Throws(IOException::class, WritingNotSupportedException::class)
    override fun toInputStream(): InputStream? {
        return delegate.toInputStream()
    }


    /**
     * @see MutablePropertySet.write
     */
    @Throws(WritingNotSupportedException::class, IOException::class)
    override fun write(dir: DirectoryEntry, name: String?) {
        delegate.write(dir, name)
    }


    /**
     * @see MutablePropertySet.write
     */
    @Throws(WritingNotSupportedException::class, IOException::class)
    override fun write(out: OutputStream) {
        delegate.write(out)
    }


    /**
     * @see PropertySet.equals
     */
    override fun equals(o: Any?): Boolean {
        return delegate == o
    }


    /**
     * @see PropertySet.getProperties
     */
    @Throws(NoSingleSectionException::class)
    override fun getProperties(): Array<Property>? {
        return delegate.getProperties()
    }


    /**
     * @see PropertySet.getProperty
     */
    @Throws(NoSingleSectionException::class)
    override fun getProperty(id: Int): Any? {
        return delegate.getProperty(id)
    }


    /**
     * @see PropertySet.getPropertyBooleanValue
     */
    @Throws(NoSingleSectionException::class)
    override fun getPropertyBooleanValue(id: Int): Boolean {
        return delegate.getPropertyBooleanValue(id)
    }


    /**
     * @see PropertySet.getPropertyIntValue
     */
    @Throws(NoSingleSectionException::class)
    override fun getPropertyIntValue(id: Int): Int {
        return delegate.getPropertyIntValue(id)
    }


    /**
     * @see PropertySet.hashCode
     */
    override fun hashCode(): Int {
        return delegate.hashCode()
    }


    /**
     * @see PropertySet.toString
     */
    override fun toString(): String {
        return delegate.toString()
    }


    /**
     * @see PropertySet.wasNull
     */
    @Throws(NoSingleSectionException::class)
    override fun wasNull(): Boolean {
        return delegate.wasNull()
    }
}
