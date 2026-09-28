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

import com.wxiwei.office.fc.ss.usermodel.HeaderFooter
import kotlin.math.min


/**
 * Common class for [HSSFHeader] and [HSSFFooter].
 */
abstract class HeaderFooter protected constructor() : HeaderFooter {
    /**
     * @return the internal text representation (combining center, left and right parts).
     * Possibly empty string if no header or footer is set.  Never `null`.
     */
    protected abstract val rawText: String

    private fun splitParts(): Array<String> {
        var text = this.rawText
        // default values
        var _left = ""
        var _center = ""
        var _right = ""

        outer@ while (text.length > 1) {
            if (text.get(0) != '&') {
                // Mimics the behaviour of Excel, which would put it in the center.
                _center = text
                break
            }
            var pos = text.length
            when (text.get(1)) {
                'L' -> {
                    if (text.indexOf("&C") >= 0) {
                        pos = min(pos, text.indexOf("&C"))
                    }
                    if (text.indexOf("&R") >= 0) {
                        pos = min(pos, text.indexOf("&R"))
                    }
                    _left = text.substring(2, pos)
                    text = text.substring(pos)
                }

                'C' -> {
                    if (text.indexOf("&L") >= 0) {
                        pos = min(pos, text.indexOf("&L"))
                    }
                    if (text.indexOf("&R") >= 0) {
                        pos = min(pos, text.indexOf("&R"))
                    }
                    _center = text.substring(2, pos)
                    text = text.substring(pos)
                }

                'R' -> {
                    if (text.indexOf("&C") >= 0) {
                        pos = min(pos, text.indexOf("&C"))
                    }
                    if (text.indexOf("&L") >= 0) {
                        pos = min(pos, text.indexOf("&L"))
                    }
                    _right = text.substring(2, pos)
                    text = text.substring(pos)
                }

                else -> {
                    // Mimics the behaviour of Excel, which would put it in the center.
                    _center = text
                    break@outer
                }
            }
        }
        return arrayOf<String>(_left, _center, _right)
    }

    /**
     * @return the left side of the header or footer.
     */
    override fun getLeft(): String? {
        return splitParts()[0]
    }

    /**
     * @param newLeft The string to set as the left side.
     */
    override fun setLeft(newLeft: String?) {
        updatePart(0, newLeft)
    }

    /**
     * @return the center of the header or footer.
     */
    override fun getCenter(): String? {
        return splitParts()[1]
    }

    /**
     * @param newCenter The string to set as the center.
     */
    override fun setCenter(newCenter: String?) {
        updatePart(1, newCenter)
    }

    /**
     * @return The right side of the header or footer.
     */
    override fun getRight(): String? {
        return splitParts()[2]
    }

    /**
     * @param newRight The string to set as the right side.
     */
    override fun setRight(newRight: String?) {
        updatePart(2, newRight)
    }

    private fun updatePart(partIndex: Int, newValue: String?) {
        val parts = splitParts()
        parts[partIndex] = if (newValue == null) "" else newValue
        updateHeaderFooterText(parts)
    }

    /**
     * Creates the complete footer string based on the left, center, and middle
     * strings.
     */
    private fun updateHeaderFooterText(parts: Array<String>) {
        val _left = parts[0]
        val _center = parts[1]
        val _right = parts[2]

        if (_center.length < 1 && _left.length < 1 && _right.length < 1) {
            setHeaderFooterText("")
            return
        }
        val sb = StringBuilder(64)
        sb.append("&C")
        sb.append(_center)
        sb.append("&L")
        sb.append(_left)
        sb.append("&R")
        sb.append(_right)
        val text = sb.toString()
        setHeaderFooterText(text)
    }

    /**
     * @param text the new header footer text (contains mark-up tags). Possibly
     * empty string never `null`
     */
    protected abstract fun setHeaderFooterText(text: String?)

    private enum class MarkupTag(
        /**
         * @return The character sequence that marks this field
         */
        val representation: String, private val _occursInPairs: Boolean
    ) {
        SHEET_NAME_FIELD("&A", false),
        DATE_FIELD("&D", false),
        FILE_FIELD("&F", false),
        FULL_FILE_FIELD("&Z", false),
        PAGE_FIELD("&P", false),
        TIME_FIELD("&T", false),
        NUM_PAGES_FIELD("&N", false),

        PICTURE_FIELD("&G", false),

        BOLD_FIELD("&B", true),
        ITALIC_FIELD("&I", true),
        STRIKETHROUGH_FIELD("&S", true),
        SUBSCRIPT_FIELD("&Y", true),
        SUPERSCRIPT_FIELD("&X", true),
        UNDERLINE_FIELD("&U", true),
        DOUBLE_UNDERLINE_FIELD("&E", true),
        ;

        /**
         * @return true if this markup tag normally comes in a pair, eg turn on
         * underline / turn off underline
         */
        fun occursPairs(): Boolean {
            return _occursInPairs
        }
    }

    companion object {
        /**
         * @param size
         * the new font size
         * @return The mark-up tag representing a new font size
         */
        fun fontSize(size: Short): String {
            return "&" + size
        }

        /**
         * @param font
         * the new font
         * @param style
         * the fonts style, one of regular, italic, bold, italic bold or
         * bold italic
         * @return The mark-up tag representing a new font size
         */
        fun font(font: String, style: String?): String {
            return "&\"" + font + "," + style + "\""
        }

        /**
         * @return The mark-up tag representing the current page number
         */
        fun page(): String {
            return MarkupTag.PAGE_FIELD.representation
        }

        /**
         * @return The mark-up tag representing the number of pages
         */
        fun numPages(): String {
            return MarkupTag.NUM_PAGES_FIELD.representation
        }

        /**
         * @return The mark-up tag representing the current date date
         */
        fun date(): String {
            return MarkupTag.DATE_FIELD.representation
        }

        /**
         * @return The mark-up tag representing current time
         */
        fun time(): String {
            return MarkupTag.TIME_FIELD.representation
        }

        /**
         * @return The mark-up tag representing the current file name
         */
        fun file(): String {
            return MarkupTag.FILE_FIELD.representation
        }

        /**
         * @return The mark-up tag representing the current tab (sheet) name
         */
        fun tab(): String {
            return MarkupTag.SHEET_NAME_FIELD.representation
        }

        /**
         * @return The mark-up tag for start bold
         */
        fun startBold(): String {
            return MarkupTag.BOLD_FIELD.representation
        }

        /**
         * @return The mark-up tag for end bold
         */
        fun endBold(): String {
            return MarkupTag.BOLD_FIELD.representation
        }

        /**
         * @return The mark-up tag for start underline
         */
        fun startUnderline(): String {
            return MarkupTag.UNDERLINE_FIELD.representation
        }

        /**
         * @return The mark-up tag for end underline
         */
        fun endUnderline(): String {
            return MarkupTag.UNDERLINE_FIELD.representation
        }

        /**
         * @return The mark-up tag for start double underline
         */
        fun startDoubleUnderline(): String {
            return MarkupTag.DOUBLE_UNDERLINE_FIELD.representation
        }

        /**
         * @return The mark-up tag for end double underline
         */
        fun endDoubleUnderline(): String {
            return MarkupTag.DOUBLE_UNDERLINE_FIELD.representation
        }

        /**
         * Removes any fields (eg macros, page markers etc) from the string.
         * Normally used to make some text suitable for showing to humans, and the
         * resultant text should not normally be saved back into the document!
         */
        fun stripFields(pText: String?): String? {
            var pos: Int

            // Check we really got something to work on
            if (pText == null || pText.length == 0) {
                return pText
            }

            var text: String? = pText

            // Firstly, do the easy ones which are static
            for (mt in MarkupTag.entries) {
                val seq = mt.representation
                while ((text!!.indexOf(seq).also { pos = it }) > -1) {
                    text = text.substring(0, pos) + text.substring(pos + seq.length)
                }
            }

            // Now do the tricky, dynamic ones
            // These are things like font sizes and font names
            text = text.replace("\\&\\d+".toRegex(), "")
            text = text.replace("\\&\".*?,.*?\"".toRegex(), "")

            // All done
            return text
        }
    }
}
