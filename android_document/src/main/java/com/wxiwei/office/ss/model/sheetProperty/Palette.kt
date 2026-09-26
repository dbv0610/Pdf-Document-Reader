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

package com.wxiwei.office.ss.model.sheetProperty

/**
 * PaletteRecord (0x0092) - Supports custom palettes.
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Brian Sanders (bsanders at risklabs dot com) - custom palette editing
 *
 */
class Palette {
    private val _colors: MutableList<PColor>

    init {
        val defaultPalette = createDefaultPalette()
        _colors = ArrayList(defaultPalette.size)
        for (i in defaultPalette.indices) {
            _colors.add(defaultPalette[i])
        }
    }

    /**
     * Returns the color value at a given index
     *
     * @return the RGB triplet for the color, or `null` if the specified index
     * does not exist
     */
    fun getColor(byteIndex: Int): ByteArray? {
        val i = byteIndex - FIRST_COLOR_INDEX
        if (i < 0 || i >= _colors.size) {
            return null
        }
        return _colors[i].getTriplet()
    }

    /**
     * Sets the color value at a given index
     *
     * If the given index is greater than the current last color index,
     * then black is inserted at every index required to make the palette continuous.
     *
     * @param byteIndex the index to set; if this index is less than 0x8 or greater than
     * 0x40, then no modification is made
     */
    fun setColor(byteIndex: Short, red: Byte, green: Byte, blue: Byte) {
        val i = byteIndex - FIRST_COLOR_INDEX
        if (i < 0 || i >= STANDARD_PALETTE_SIZE) {
            return
        }
        // may need to grow - fill intervening palette entries with black
        while (_colors.size <= i) {
            _colors.add(PColor(0, 0, 0))
        }
        val custColor = PColor(red.toInt(), green.toInt(), blue.toInt())
        _colors[i] = custColor
    }

    fun dispose() {
        _colors.clear()
    }

    /**
     * PColor - element in the list of colors
     */
    private class PColor(private val _red: Int, private val _green: Int, private val _blue: Int) {
        fun getTriplet(): ByteArray {
            return byteArrayOf(_red.toByte(), _green.toByte(), _blue.toByte())
        }
    }

    companion object {
        /** The standard size of an XLS palette */
        const val STANDARD_PALETTE_SIZE: Byte = 56
        /** The byte index of the first color */
        const val FIRST_COLOR_INDEX: Short = 0x8

        /**
         * Creates the default palette as PaletteRecord binary data
         */
        private fun createDefaultPalette(): Array<PColor> {
            return arrayOf(
                pc(0, 0, 0),
                pc(255, 255, 255),
                pc(255, 0, 0),//10
                pc(0, 255, 0),
                pc(0, 0, 255),
                pc(255, 255, 0),
                pc(255, 0, 255),
                pc(0, 255, 255),
                pc(128, 0, 0),
                pc(0, 128, 0),
                pc(0, 0, 128),
                pc(128, 128, 0),
                pc(128, 0, 128),//20
                pc(0, 128, 128),
                pc(192, 192, 192),
                pc(128, 128, 128),
                pc(153, 153, 255),
                pc(153, 51, 102),
                pc(255, 255, 204),
                pc(204, 255, 255),
                pc(102, 0, 102),
                pc(255, 128, 128),
                pc(0, 102, 204),//30
                pc(204, 204, 255),
                pc(0, 0, 128),
                pc(255, 0, 255),
                pc(255, 255, 0),
                pc(0, 255, 255),
                pc(128, 0, 128),
                pc(128, 0, 0),
                pc(0, 128, 128),
                pc(0, 0, 255),
                pc(0, 204, 255),//40
                pc(204, 255, 255),
                pc(204, 255, 204),
                pc(255, 255, 153),
                pc(153, 204, 255),
                pc(255, 153, 204),
                pc(204, 153, 255),
                pc(255, 204, 153),
                pc(51, 102, 255),
                pc(51, 204, 204),
                pc(153, 204, 0),//50
                pc(255, 204, 0),
                pc(255, 153, 0),
                pc(255, 102, 0),
                pc(102, 102, 153),
                pc(150, 150, 150),
                pc(0, 51, 102),
                pc(51, 153, 102),
                pc(0, 51, 0),
                pc(51, 51, 0),
                pc(153, 51, 0),//60
                pc(153, 51, 102),
                pc(51, 51, 153),
                pc(51, 51, 51)
            )
        }

        private fun pc(r: Int, g: Int, b: Int): PColor {
            return PColor(r, g, b)
        }
    }
}
