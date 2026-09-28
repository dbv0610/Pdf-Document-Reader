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
package com.wxiwei.office.fc.ddf

/**
 * This is the abstract base class for all escher properties.
 * 
 * @see EscherOptRecord
 * 
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
abstract class EscherProperty {
    val id: Short

    /**
     * The id is distinct from the actual property number.  The id includes the property number the blip id
     * flag and an indicator whether the property is complex or not.
     */
    constructor(id: Short) {
        this.id = id
    }

    /**
     * Constructs a new escher property.  The three parameters are combined to form a property
     * id.
     */
    constructor(propertyNumber: Short, isComplex: Boolean, isBlipId: Boolean) {
        this.id = (propertyNumber +
                (if (isComplex) 0x8000 else 0x0) +
                (if (isBlipId) 0x4000 else 0x0)).toShort()
    }

    @get:JvmName("getPropertyNumberProperty")
    val propertyNumber: Short
        get() = (id.toInt() and 0x3FFF.toShort().toInt()).toShort()

    fun getPropertyNumber(): Short = propertyNumber

    val isComplex: Boolean
        get() = (id.toInt() and 0x8000.toShort().toInt()) != 0

    val isBlipId: Boolean
        get() = (id.toInt() and 0x4000.toShort().toInt()) != 0

    val name: String?
        get() = EscherProperties.getPropertyName(this.propertyNumber)

    open val propertySize: Int
        /**
         * Most properties are just 6 bytes in length.  Override this if we're
         * dealing with complex properties.
         */
        get() = 6

    /**
     * Escher properties consist of a simple fixed length part and a complex variable length part.
     * The fixed length part is serialized first.
     */
    abstract fun serializeSimplePart(data: ByteArray?, pos: Int): Int

    /**
     * Escher properties consist of a simple fixed length part and a complex variable length part.
     * The fixed length part is serialized first.
     */
    abstract fun serializeComplexPart(data: ByteArray?, pos: Int): Int
}
