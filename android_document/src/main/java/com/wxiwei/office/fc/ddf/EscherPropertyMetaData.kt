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
 * This class stores the type and description of an escher property.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class EscherPropertyMetaData {
    val description: String?
    var type: Byte = 0
        private set


    /**
     * @param description The description of the escher property.
     */
    constructor(description: String?) {
        this.description = description
    }

    /**
     * 
     * @param description   The description of the escher property.
     * @param type          The type of the property.
     */
    constructor(description: String?, type: Byte) {
        this.description = description
        this.type = type
    }

    companion object {
        // Escher property types.
        val TYPE_UNKNOWN: Byte = 0.toByte()
        val TYPE_BOOLEAN: Byte = 1.toByte()
        val TYPE_RGB: Byte = 2.toByte()
        val TYPE_SHAPEPATH: Byte = 3.toByte()
        val TYPE_SIMPLE: Byte = 4.toByte()
        val TYPE_ARRAY: Byte = 5.toByte()
    }
}
