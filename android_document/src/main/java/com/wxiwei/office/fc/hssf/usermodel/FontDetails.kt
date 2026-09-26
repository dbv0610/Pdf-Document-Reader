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

import java.util.Properties
import java.util.StringTokenizer

/**
 * Stores width and height details about a font.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class FontDetails

/**
 * Construct the font details with the given name and height.
 * 
 * @param fontName  The font name.
 * @param height    The height of the font.
 */(val fontName: String?, val height: Int) {
    private val charWidths: MutableMap<Char?, Int?> = HashMap<Char?, Int?>()

    fun addChar(c: Char, width: Int) {
        charWidths.put(Character.valueOf(c), width)
    }

    /**
     * Retrieves the width of the specified character.  If the metrics for
     * a particular character are not available it defaults to returning the
     * width for the 'W' character.
     */
    fun getCharWidth(c: Char): Int {
        val widthInteger = charWidths.get(Character.valueOf(c))
        if (widthInteger == null && c != 'W') {
            return getCharWidth('W')
        }
        return widthInteger!!
    }

    fun addChars(characters: CharArray, widths: IntArray) {
        for (i in characters.indices) {
            charWidths.put(Character.valueOf(characters[i]), widths[i])
        }
    }

    /**
     * Gets the width of all characters in a string.
     * 
     * @param str   The string to measure.
     * @return      The width of the string for a 10 point font.
     */
    fun getStringWidth(str: String): Int {
        var width = 0
        for (i in 0..<str.length) {
            width += getCharWidth(str.get(i))
        }
        return width
    }

    companion object {
        protected fun buildFontHeightProperty(fontName: String): String {
            return "font." + fontName + ".height"
        }

        protected fun buildFontWidthsProperty(fontName: String): String {
            return "font." + fontName + ".widths"
        }

        protected fun buildFontCharactersProperty(fontName: String): String {
            return "font." + fontName + ".characters"
        }

        /**
         * Create an instance of `FontDetails` by loading them from the
         * provided property object.
         * @param fontName          the font name
         * @param fontMetricsProps  the property object holding the details of this
         * particular font.
         * @return  a new FontDetails instance.
         */
        fun create(fontName: String, fontMetricsProps: Properties): FontDetails {
            val heightStr = fontMetricsProps.getProperty(buildFontHeightProperty(fontName))
            val widthsStr = fontMetricsProps.getProperty(buildFontWidthsProperty(fontName))
            val charactersStr = fontMetricsProps.getProperty(buildFontCharactersProperty(fontName))

            // Ensure that this is a font we know about
            require(!(heightStr == null || widthsStr == null || charactersStr == null)) { "The supplied FontMetrics doesn't know about the font '" + fontName + "', so we can't use it. Please add it to your font metrics file (see StaticFontMetrics.getFontDetails" }

            val height = heightStr.toInt()
            val d = FontDetails(fontName, height)
            val charactersStrArray: Array<String?> = split(charactersStr, ",", -1)
            val widthsStrArray: Array<String?> = split(widthsStr, ",", -1)
            if (charactersStrArray.size != widthsStrArray.size) throw RuntimeException("Number of characters does not number of widths for font " + fontName)
            for (i in widthsStrArray.indices) {
                if (charactersStrArray[i]!!.length != 0) d.addChar(
                    charactersStrArray[i]!!.get(0),
                    widthsStrArray[i]!!.toInt()
                )
            }
            return d
        }

        /**
         * Split the given string into an array of strings using the given
         * delimiter.
         */
        private fun split(text: String, separator: String?, max: Int): Array<String?> {
            val tok = StringTokenizer(text, separator)
            var listSize = tok.countTokens()
            if (max != -1 && listSize > max) listSize = max
            val list: Array<String?>? = arrayOfNulls<String>(listSize)
            var i = 0
            while (tok.hasMoreTokens()) {
                if (max != -1 && i == listSize - 1) {
                    val buf = StringBuffer((text.length * (listSize - i)) / listSize)
                    while (tok.hasMoreTokens()) {
                        buf.append(tok.nextToken())
                        if (tok.hasMoreTokens()) buf.append(separator)
                    }
                    list!![i] = buf.toString().trim { it <= ' ' }
                    break
                }
                list!![i] = tok.nextToken().trim { it <= ' ' }
                i++
            }

            return list!!
        }
    }
}
