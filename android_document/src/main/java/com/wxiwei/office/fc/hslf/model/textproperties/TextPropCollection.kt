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

import com.wxiwei.office.fc.hslf.record.StyleTextPropAtom
import com.wxiwei.office.fc.util.LittleEndian.getInt
import com.wxiwei.office.fc.util.LittleEndian.getShort
import java.util.LinkedList

/**
 * For a given run of characters, holds the properties (which could
 * be paragraph properties or character properties).
 * Used to hold the number of characters affected, the list of active
 * properties, and the random reserved field if required.
 */
class TextPropCollection {
    /** Fetch the TextProp with this name, or null if it isn't present  */
    fun findByName(textPropName: String?): TextProp? {
        for (i in textPropList!!.indices) {
            val prop = textPropList!!.get(i)
            if (prop.getName() == textPropName) {
                return prop
            }
        }
        return null
    }

    /** Add the TextProp with this name to the list  */
    fun addWithName(name: String?): TextProp {
        // Find the base TextProp to base on
        var base: TextProp? = null
        for (i in StyleTextPropAtom.Companion.characterTextPropTypes.indices) {
            if (StyleTextPropAtom.characterTextPropTypes[i]?.getName() == name) {
                base = StyleTextPropAtom.characterTextPropTypes[i]
            }
        }
        for (i in StyleTextPropAtom.paragraphTextPropTypes.indices) {
            if (StyleTextPropAtom.paragraphTextPropTypes[i]?.getName() == name) {
                base = StyleTextPropAtom.Companion.paragraphTextPropTypes[i]
            }
        }
        requireNotNull(base) {
            ("No TextProp with name " + name
                    + " is defined to add from")
        }

        // Add a copy of this property, in the right place to the list
        val textProp = base.clone() as TextProp
        var pos = 0
        for (i in textPropList!!.indices) {
            val curProp = textPropList!!.get(i)
            if (textProp.getMask() > curProp.getMask()) {
                pos++
            }
        }
        textPropList!!.add(pos, textProp)
        return textProp
    }

    /**
     * For an existing set of text properties, build the list of
     * properties coded for in a given run of properties.
     * @return the number of bytes that were used encoding the properties list
     */
    fun buildTextPropList(
        containsField: Int, potentialProperties: Array<TextProp?>, data: ByteArray,
        dataOffset: Int
    ): Int {
        var bytesPassed = 0

        // For each possible entry, see if we match the mask
        // If we do, decode that, save it, and shuffle on
        for (i in potentialProperties.indices) {
            // Check there's still data left to read

            // Check if this property is found in the mask

            if ((containsField and potentialProperties[i]!!.getMask()) != 0) {
                if (dataOffset + bytesPassed >= data.size) {
                    // Out of data, can't be any more properties to go
                    // remember the mask and return
                    this.specialMask = this.specialMask or potentialProperties[i]!!.getMask()
                    return bytesPassed
                }

                // Bingo, data contains this property
                val prop = potentialProperties[i]!!.clone() as TextProp
                var `val` = 0
                if (prop.getSize() == 2) {
                    `val` = getShort(data, dataOffset + bytesPassed).toInt()
                } else if (prop.getSize() == 4) {
                    `val` = getInt(data, dataOffset + bytesPassed)
                } else if (prop.getSize() == 0) {
                    //remember "special" bits.
                    this.specialMask = this.specialMask or potentialProperties[i]!!.getMask()
                    continue
                }
                if (CharFlagsTextProp.NAME == prop.getName() && `val` < 0) {
                    `val` = 0
                }
                prop.setValue(`val`)
                bytesPassed += prop.getSize()
                if ("tabStops" == prop.getName()) {
                    bytesPassed += `val` * 4
                }
                textPropList!!.add(prop)
            }
        }

        // Return how many bytes were used
        return bytesPassed
    }

    /**
     * Create a new collection of text properties (be they paragraph
     * or character) which will be groked via a subsequent call to
     * buildTextPropList().
     */
    constructor(charactersCovered: Int, reservedField: Short) {
        this.charactersCovered = charactersCovered
        this.reservedField = reservedField
        textPropList = LinkedList<TextProp>()
    }

    /**
     * Create a new collection of text properties (be they paragraph
     * or character) for a run of text without any
     */
    constructor(textSize: Int) {
        charactersCovered = textSize
        reservedField = -1
        textPropList = LinkedList<TextProp>()
    }

    /**
     * Update the size of the text that this set of properties
     * applies to
     */
    fun updateTextSize(textSize: Int) {
        charactersCovered = textSize
    }

    /**
     * 
     */
    fun dispose() {
        if (textPropList != null) {
            for (i in textPropList!!.indices) {
                textPropList!!.get(i).dispose()
            }
            textPropList!!.clear()
            textPropList = null
        }
    }

    /** Fetch the number of characters this styling applies to  */
    @get:JvmName("getCharactersCoveredProperty")
    var charactersCovered: Int = 0
    fun getCharactersCovered(): Int = charactersCovered

    @get:JvmName("getReservedFieldProperty")
    var reservedField: Short = 0
    fun getReservedField(): Short = reservedField

    /** Fetch the TextProps that define this styling  */
    @get:JvmName("getTextPropListProperty")
    var textPropList: LinkedList<TextProp>? = null
    fun getTextPropList(): LinkedList<TextProp>? = textPropList

    @get:JvmName("getSpecialMaskProperty")
    var specialMask: Int = 0
    fun getSpecialMask(): Int = specialMask
}
