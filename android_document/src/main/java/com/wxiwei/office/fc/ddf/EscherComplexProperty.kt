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
package com.wxiwei.office.fc.ddf

import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndian.putInt
import com.wxiwei.office.fc.util.LittleEndian.putShort

/**
 * A complex property differs from a simple property in that the data can not fit inside a 32 bit
 * integer.  See the specification for more detailed information regarding exactly what is
 * stored here.
 * 
 * @author Glen Stampoultzis
 */
open class EscherComplexProperty : EscherProperty {
    /**
     * Get the complex data value.
     */
    // TODO - make private and final
    var complexData: ByteArray
        protected set

    /**
     * Create a complex property using the property id and a byte array containing the complex
     * data value.
     * 
     * @param id          The id consists of the property number, a flag indicating whether this is a blip id and a flag
     * indicating that this is a complex property.
     * @param complexData The value of this property.
     */
    constructor(id: Short, complexData: ByteArray) : super(id) {
        this.complexData = complexData
    }

    /**
     * Create a complex property using the property number, a flag to indicate whether this is a
     * blip reference and the complex property data.
     * 
     * @param propertyNumber The property number
     * @param isBlipId       Whether this is a blip id.  Should be false.
     * @param complexData    The value of this complex property.
     */
    constructor(propertyNumber: Short, isBlipId: Boolean, complexData: ByteArray) : super(
        propertyNumber,
        true,
        isBlipId
    ) {
        this.complexData = complexData
    }

    /**
     * Serializes the simple part of this property.  i.e. the first 6 bytes.
     */
    override fun serializeSimplePart(data: ByteArray?, pos: Int): Int {
        val data = data!!
        putShort(data, pos, id)
        putInt(data, pos + 2, complexData.size)
        return 6
    }

    /**
     * Serializes the complex part of this property
     * 
     * @param data The data array to serialize to
     * @param pos  The offset within data to start serializing to.
     * @return The number of bytes serialized.
     */
    override fun serializeComplexPart(data: ByteArray?, pos: Int): Int {
        val data = data!!
        System.arraycopy(this.complexData, 0, data, pos, complexData.size)
        return complexData.size
    }

    /**
     * Determine whether this property is equal to another property.
     * 
     * @param o The object to compare to.
     * @return True if the objects are equal.
     */
    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o !is EscherComplexProperty) {
            return false
        }

        val escherComplexProperty = o

        if (!complexData.contentEquals(escherComplexProperty.complexData)) return false

        return true
    }

    /**
     * Calculates the number of bytes required to serialize this property.
     * 
     * @return Number of bytes
     */
    override val propertySize: Int
        get() {
        return 6 + complexData.size
    }

    override fun hashCode(): Int {
        return id * 11
    }

    /**
     * Retrieves the string representation for this property.
     */
    override fun toString(): String {
        val dataStr = toHex(this.complexData, 32)

        return ("propNum: " + propertyNumber
                + ", propName: " + EscherProperties.getPropertyName(propertyNumber)
                + ", complex: " + isComplex
                + ", blipId: " + isBlipId
                + ", data: " + System.getProperty("line.separator") + dataStr)
    }
}
