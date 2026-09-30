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
import java.io.OutputStream

/**
 * 
 * Adds writing capability to the [Property] class.
 * 
 * 
 * Please be aware that this class' functionality will be merged into the
 * [Property] class at a later time, so the API will change.
 * 
 * @author Rainer Klute [&lt;klute@rainer-klute.de&gt;](mailto:klute@rainer-klute.de)
 */
open class MutableProperty : Property {
    /**
     * 
     * Creates an empty property. It must be filled using the set method to
     * be usable.
     */
    constructor()


    /**
     * 
     * Creates a `MutableProperty` as a copy of an existing
     * `Property`.
     * 
     * @param p The property to copy.
     */
    constructor(p: Property) {
        setID(p.getID())
        setType(p.getType())
        setValue(p.getValue())
    }


    /**
     * 
     * Sets the property's ID.
     * 
     * @param id the ID
     */
    fun setID(id: Long) {
        this.id = id
    }


    /**
     * 
     * Sets the property's type.
     * 
     * @param type the property's type
     */
    fun setType(type: Long) {
        this.type = type
    }


    /**
     * 
     * Sets the property's value.
     * 
     * @param value the property's value
     */
    fun setValue(value: Any?) {
        this.value = value
    }


    /**
     * 
     * Writes the property to an output stream.
     * 
     * @param out The output stream to write to.
     * @param codepage The codepage to use for writing non-wide strings
     * @return the number of bytes written to the stream
     * 
     * @exception IOException if an I/O error occurs
     * @exception WritingNotSupportedException if a variant type is to be
     * written that is not yet supported
     */
    @Throws(IOException::class, WritingNotSupportedException::class)
    fun write(out: OutputStream, codepage: Int): Int {
        var length = 0
        var variantType = getType()

        /* Ensure that wide strings are written if the codepage is Unicode. */
        if (codepage == Constants.CP_UNICODE && variantType == Variant.Companion.VT_LPSTR.toLong()) variantType =
            Variant.Companion.VT_LPWSTR.toLong()

        length += TypeWriter.writeUIntToStream(out, variantType)
        length += VariantSupport.Companion.write(out, variantType, getValue(), codepage)
        return length
    }
}
