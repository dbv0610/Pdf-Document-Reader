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
package com.wxiwei.office.fc.hssf.usermodel

import com.wxiwei.office.fc.hssf.record.PaletteRecord
import com.wxiwei.office.fc.hssf.util.HSSFColor
import java.util.Locale
import kotlin.math.abs


/**
 * Represents a workbook color palette.
 * Internally, the XLS format refers to colors using an offset into the palette
 * record.  Thus, the first color in the palette has the index 0x8, the second
 * has the index 0x9, etc. through 0x40
 * 
 * @author Brian Sanders (bsanders at risklabs dot com)
 */
class HSSFPalette(private val _palette: PaletteRecord) {
    /**
     * Retrieves the color at a given index
     * 
     * @param index the palette index, between 0x8 to 0x40 inclusive
     * @return the color, or null if the index is not populated
     */
    fun getColor(index: Short): HSSFColor? {
        //Handle the special AUTOMATIC case
        if (index == HSSFColor.AUTOMATIC.index) {
            return HSSFColor.AUTOMATIC.instance
        }
        val b = _palette.getColor(index.toInt())
        if (b != null) {
            return CustomColor(index, b)
        }
        return null
    }

    /**
     * Retrieves the color at a given index
     * 
     * @param index the palette index, between 0x8 to 0x40 inclusive
     * @return the color, or null if the index is not populated
     */
    fun getColor(index: Int): HSSFColor? {
        return getColor(index.toShort())
    }

    /**
     * Finds the first occurance of a given color
     * 
     * @param red the RGB red component, between 0 and 255 inclusive
     * @param green the RGB green component, between 0 and 255 inclusive
     * @param blue the RGB blue component, between 0 and 255 inclusive
     * @return the color, or null if the color does not exist in this palette
     */
    fun findColor(red: Byte, green: Byte, blue: Byte): HSSFColor? {
        var b = _palette.getColor(PaletteRecord.FIRST_COLOR_INDEX.toInt())
        var i = PaletteRecord.FIRST_COLOR_INDEX
        while (b != null
        ) {
            if (b[0] == red && b[1] == green && b[2] == blue) {
                return CustomColor(i, b)
            }
            b = _palette.getColor((++i).toInt())
        }
        return null
    }

    /**
     * Finds the closest matching color in the custom palette.  The
     * method for finding the distance between the colors is fairly
     * primative.
     * 
     * @param red   The red component of the color to match.
     * @param green The green component of the color to match.
     * @param blue  The blue component of the color to match.
     * @return  The closest color or null if there are no custom
     * colors currently defined.
     */
    fun findSimilarColor(red: Byte, green: Byte, blue: Byte): HSSFColor? {
        return findSimilarColor(unsignedInt(red), unsignedInt(green), unsignedInt(blue))
    }

    /**
     * Finds the closest matching color in the custom palette.  The
     * method for finding the distance between the colors is fairly
     * primative.
     * 
     * @param red   The red component of the color to match.
     * @param green The green component of the color to match.
     * @param blue  The blue component of the color to match.
     * @return  The closest color or null if there are no custom
     * colors currently defined.
     */
    fun findSimilarColor(red: Int, green: Int, blue: Int): HSSFColor? {
        var result: HSSFColor? = null
        var minColorDistance = Int.MAX_VALUE
        var b = _palette.getColor(PaletteRecord.FIRST_COLOR_INDEX.toInt())
        var i = PaletteRecord.FIRST_COLOR_INDEX
        while (b != null
        ) {
            val colorDistance = abs(red - unsignedInt(b[0])) + abs(green - unsignedInt(b[1])) + abs(
                blue - unsignedInt(b[2])
            )
            if (colorDistance < minColorDistance) {
                minColorDistance = colorDistance
                result = getColor(i)
            }
            b = _palette.getColor((++i).toInt())
        }
        return result
    }

    /**
     * Turn a byte of between -127 and 127 into something between
     * 0 and 255, so distance calculations work as expected.
     */
    private fun unsignedInt(b: Byte): Int {
        return 0xFF and (b.toInt())
    }

    /**
     * Sets the color at the given offset
     * 
     * @param index the palette index, between 0x8 to 0x40 inclusive
     * @param red the RGB red component, between 0 and 255 inclusive
     * @param green the RGB green component, between 0 and 255 inclusive
     * @param blue the RGB blue component, between 0 and 255 inclusive
     */
    fun setColorAtIndex(index: Short, red: Byte, green: Byte, blue: Byte) {
        _palette.setColor(index, red, green, blue)
    }

    /**
     * Adds a new color into an empty color slot.
     * @param red       The red component
     * @param green     The green component
     * @param blue      The blue component
     * 
     * @return  The new custom color.
     * 
     * @throws RuntimeException if there are more more free color indexes.
     */
    fun addColor(red: Byte, green: Byte, blue: Byte): HSSFColor? {
        var b = _palette.getColor(PaletteRecord.FIRST_COLOR_INDEX.toInt())
        var i: Short
        i = PaletteRecord.FIRST_COLOR_INDEX
        while (i < PaletteRecord.STANDARD_PALETTE_SIZE + PaletteRecord.FIRST_COLOR_INDEX) {
            if (b == null) {
                setColorAtIndex(i, red, green, blue)
                return getColor(i)
            }
            b = _palette.getColor((++i).toInt())
        }
        throw RuntimeException("Could not find free color index")
    }

    private class CustomColor(
        private val _byteOffset: Short,
        private val _red: Byte,
        private val _green: Byte,
        private val _blue: Byte
    ) : HSSFColor() {
        constructor(byteOffset: Short, colors: ByteArray) : this(
            byteOffset,
            colors[0],
            colors[1],
            colors[2]
        )

        override val index: Short
            get() = _byteOffset

        override val triplet: ShortArray
            get() = shortArrayOf(
                (_red.toInt() and 0xff).toShort(),
                (_green.toInt() and 0xff).toShort(),
                (_blue.toInt() and 0xff).toShort()
            )

        override val hexString: String
            get() {
                val sb = StringBuffer()
                sb.append(getGnumericPart(_red))
                sb.append(':')
                sb.append(getGnumericPart(_green))
                sb.append(':')
                sb.append(getGnumericPart(_blue))
                return sb.toString()
            }

        fun getGnumericPart(color: Byte): String {
            var s: String
            if (color.toInt() == 0) {
                s = "0"
            } else {
                var c = color.toInt() and 0xff //as unsigned
                c = (c shl 8) or c //pad to 16-bit
                s = Integer.toHexString(c).uppercase(Locale.getDefault())
                while (s.length < 4) {
                    s = "0" + s
                }
            }
            return s
        }
    }
}
