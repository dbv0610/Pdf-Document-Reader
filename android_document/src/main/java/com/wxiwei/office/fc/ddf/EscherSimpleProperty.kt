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
 * A simple property is of fixed length and as a property number in addition
 * to a 32-bit value.  Properties that can't be stored in only 32-bits are
 * stored as EscherComplexProperty objects.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
open class EscherSimpleProperty : EscherProperty {
    /**
     * @return  Return the 32 bit value of this property.
     */
    @get:JvmName("getPropertyValueProperty")
    var propertyValue: Int
        protected set

    fun getPropertyValue(): Int = propertyValue

    /**
     * The id is distinct from the actual property number.  The id includes the property number the blip id
     * flag and an indicator whether the property is complex or not.
     */
    constructor(id: Short, propertyValue: Int) : super(id) {
        this.propertyValue = propertyValue
    }

    /**
     * Constructs a new escher property.  The three parameters are combined to form a property
     * id.
     */
    constructor(
        propertyNumber: Short,
        isComplex: Boolean,
        isBlipId: Boolean,
        propertyValue: Int
    ) : super(propertyNumber, isComplex, isBlipId) {
        this.propertyValue = propertyValue
    }

    /**
     * Serialize the simple part of the escher record.
     * 
     * @return the number of bytes serialized.
     */
    override fun serializeSimplePart(data: ByteArray?, offset: Int): Int {
        val data = data!!
        putShort(data, offset, id)
        putInt(data, offset + 2, propertyValue)
        return 6
    }

    /**
     * Escher properties consist of a simple fixed length part and a complex variable length part.
     * The fixed length part is serialized first.
     */
    override fun serializeComplexPart(data: ByteArray?, pos: Int): Int {
        return 0
    }

    /**
     * Returns true if one escher property is equal to another.
     */
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is EscherSimpleProperty) return false

        val escherSimpleProperty = o

        if (propertyValue != escherSimpleProperty.propertyValue) return false
        if (id != escherSimpleProperty.id) return false

        return true
    }

    /**
     * Returns a hashcode so that this object can be stored in collections that
     * require the use of such things.
     */
    override fun hashCode(): Int {
        return propertyValue
    }

    /**
     * @return the string representation of this property.
     */
    override fun toString(): String {
        return ("propNum: " + propertyNumber
                + ", RAW: 0x" + toHex(id)
                + ", propName: " + EscherProperties.getPropertyName(propertyNumber)
                + ", complex: " + isComplex
                + ", blipId: " + isBlipId
                + ", value: " + propertyValue + " (0x" + toHex(propertyValue) + ")")
    }
}
