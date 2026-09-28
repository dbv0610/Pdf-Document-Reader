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

import java.io.IOException
import java.io.InputStream
import java.io.UnsupportedEncodingException

/**
 * 
 * Factory class to create instances of [SummaryInformation],
 * [DocumentSummaryInformation] and [PropertySet].
 * 
 * @author Rainer Klute [&lt;klute@rainer-klute.de&gt;](mailto:klute@rainer-klute.de)
 */
object PropertySetFactory {
    /**
     * 
     * Creates the most specific [PropertySet] from an [ ]. This is preferrably a [ ] or a [SummaryInformation]. If
     * the specified [InputStream] does not contain a property
     * set stream, an exception is thrown and the [InputStream]
     * is repositioned at its beginning.
     * 
     * @param stream Contains the property set stream's data.
     * @return The created [PropertySet].
     * @throws NoPropertySetStreamException if the stream does not
     * contain a property set.
     * @throws MarkUnsupportedException if the stream does not support
     * the `mark` operation.
     * @throws IOException if some I/O problem occurs.
     * @exception UnsupportedEncodingException if the specified codepage is not
     * supported.
     */
    @JvmStatic
    @Throws(
        NoPropertySetStreamException::class,
        MarkUnsupportedException::class,
        UnsupportedEncodingException::class,
        IOException::class
    )
    fun create(stream: InputStream): PropertySet {
        val ps = PropertySet(stream)
        try {
            if (ps.isSummaryInformation()) return SummaryInformation(ps)
            else if (ps.isDocumentSummaryInformation()) return DocumentSummaryInformation(ps)
            else return ps
        } catch (ex: UnexpectedPropertySetTypeException) {
            /* This exception will never be throws because we already checked
             * explicitly for this case above. */
            throw Error(ex.toString())
        }
    }


    /**
     * 
     * Creates a new summary information.
     * 
     * @return the new summary information.
     */
    @JvmStatic
    fun newSummaryInformation(): SummaryInformation {
        val ps = MutablePropertySet()
        val s = ps.getFirstSection() as MutableSection
        s.setFormatID(SectionIDMap.Companion.SUMMARY_INFORMATION_ID)
        try {
            return SummaryInformation(ps)
        } catch (ex: UnexpectedPropertySetTypeException) {
            /* This should never happen. */
            throw HPSFRuntimeException(ex)
        }
    }


    /**
     * 
     * Creates a new document summary information.
     * 
     * @return the new document summary information.
     */
    @JvmStatic
    fun newDocumentSummaryInformation(): DocumentSummaryInformation {
        val ps = MutablePropertySet()
        val s = ps.getFirstSection() as MutableSection
        s.setFormatID(SectionIDMap.Companion.DOCUMENT_SUMMARY_INFORMATION_ID[0])
        try {
            return DocumentSummaryInformation(ps)
        } catch (ex: UnexpectedPropertySetTypeException) {
            /* This should never happen. */
            throw HPSFRuntimeException(ex)
        }
    }
}
