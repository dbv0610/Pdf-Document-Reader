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
 * The opt record is used to store property values for a shape. It is the key to
 * determining the attributes of a shape. Properties can be of two types: simple
 * or complex. Simple types are fixed length. Complex properties are variable
 * length.
 * 
 * @author Glen Stampoultzis
 */
class EscherOptRecord : AbstractEscherOptRecord() {
    /**
     * Automatically recalculate the correct option
     */
    override var options: Short
        get() {
            super.options = ((escherProperties.size shl 4) or 0x3).toShort()
            return super.options
        }
        set(value) {
            super.options = value
        }

    override val recordName: String
        get() {
        return "Opt"
    }

    /**
     * 
     * 
     */
    override fun dispose() {
    }

    companion object {
        @JvmField
        val RECORD_ID: Short = 0xF00B.toShort()
        const val RECORD_DESCRIPTION: String = "msofbtOPT"
    }
}
