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
package com.wxiwei.office.fc.hslf.model.textproperties

/**
 * Definition of a special kind of property of some text, or its
 * paragraph. For these properties, a flag in the "contains" header
 * field tells you the data property family will exist. The value
 * of the property is itself a mask, encoding several different
 * (but related) properties
 */
open class BitMaskTextProp(
    sizeOfDataBlock: Int, maskInHeader: Int, overallName: String?,
    subPropNames: Array<String?>
) : TextProp(sizeOfDataBlock, maskInHeader, "bitmask") {
    /** Fetch the list of the names of the sub properties  */
    val subPropNames: Array<String?>?
    private val subPropMasks: IntArray

    /** Fetch the list of if the sub properties match or not  */
    var subPropMatches: BooleanArray
        private set

    init {
        this.subPropNames = subPropNames
        this.name = overallName
        subPropMasks = IntArray(subPropNames.size)
        subPropMatches = BooleanArray(subPropNames.size)

        // Initialise the masks list
        for (i in subPropMasks.indices) {
            subPropMasks[i] = (1 shl i)
        }
    }

    /**
     * As we're purely mask based, just set flags for stuff
     * that is set
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getWriteMaskProperty")
    override val writeMask: Int
        get() = value

    /**
     * Set the value of the text property, and recompute the sub
     * properties based on it
     */
    override fun setValue(`val`: Int) {
        value = `val`

        // Figure out the values of the sub properties
        for (i in subPropMatches.indices) {
            subPropMatches[i] = false
            if ((value and subPropMasks[i]) != 0) {
                subPropMatches[i] = true
            }
        }
    }

    /**
     * Fetch the true/false status of the subproperty with the given index
     */
    fun getSubValue(idx: Int): Boolean {
        return subPropMatches[idx]
    }

    /**
     * Set the true/false status of the subproperty with the given index
     */
    fun setSubValue(value: Boolean, idx: Int) {
        if (subPropMatches[idx] == value) {
            return
        }
        if (value) {
            this.value += subPropMasks[idx]
        } else {
            this.value -= subPropMasks[idx]
        }
        subPropMatches[idx] = value
    }

    override fun clone(): Any {
        val newObj = super.clone() as BitMaskTextProp

        // Don't carry over matches, but keep everything 
        //  else as it was
        newObj.subPropMatches = BooleanArray(subPropMatches.size)

        return newObj
    }
}
