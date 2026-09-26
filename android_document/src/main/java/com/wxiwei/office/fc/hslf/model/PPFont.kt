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
package com.wxiwei.office.fc.hslf.model

import com.wxiwei.office.fc.hslf.record.FontEntityAtom

/**
 * Represents a Font used in a presenation.
 * 
 * 
 * In PowerPoint Font is a shared resource and can be shared among text object in the presentation.
 * 
 * Some commonly used fonts are predefined in static constants.
 * 
 * @author Yegor Kozlov
 */
class PPFont {
    /**
     * get the character set
     * 
     * @return charset - characterset
     */
    var charSet: Int = 0
        /**
         * set the character set
         * 
         * @param val - characterset
         */
        set(`val`) {
            field = `val`
        }

    /**
     * get the font type
     * 
     * 
     * Bit 1: Raster Font
     * Bit 2: Device Font
     * Bit 3: TrueType Font
     * 
     * 
     * @return the font type
     */
    var fontType: Int = 0
        /**
         * set the font type
         * 
         * 
         * Bit 1: Raster Font
         * Bit 2: Device Font
         * Bit 3: TrueType Font
         * 
         * 
         * @param val - the font type
         */
        set(`val`) {
            field = `val`
        }

    /**
     * get the character set
     * Bit 1: If set, font is subsetted
     * 
     * @return the font flags
     */
    var fontFlags: Int = 0
        /**
         * set the font flags
         * Bit 1: If set, font is subsetted
         * 
         * @param val - the font flags
         */
        set(`val`) {
            field = `val`
        }

    /**
     * get lfPitchAndFamily
     * 
     * @return corresponds to the lfPitchAndFamily field of the Win32 API LOGFONT structure
     */
    var pitchAndFamily: Int = 0
        /**
         * set lfPitchAndFamily
         * 
         * 
         * @param val - Corresponds to the lfPitchAndFamily field of the Win32 API LOGFONT structure
         */
        set(`val`) {
            field = `val`
        }

    /**
     * get the name for the font (i.e. Arial)
     * 
     * @return String representing the name of the font to use
     */
    var fontName: String? = null
        /**
         * set the name for the font (i.e. Arial)
         * 
         * @param val  String representing the name of the font to use
         */
        set(`val`) {
            field = `val`
        }

    /**
     * Creates a new instance of PPFont
     */
    constructor()

    /**
     * Creates a new instance of PPFont and initialize it from the supplied font atom
     */
    constructor(fontAtom: FontEntityAtom) {
        this.fontName = fontAtom.fontName
        this.charSet = fontAtom.charSet
        this.fontType = fontAtom.fontType
        this.fontFlags = fontAtom.fontFlags
        this.pitchAndFamily = fontAtom.pitchAndFamily
    }

    companion object {
        /**
         * ANSI character set
         */
        const val ANSI_CHARSET: Byte = 0

        /**
         * Default character set.
         */
        const val DEFAULT_CHARSET: Byte = 1

        /**
         * Symbol character set
         */
        const val SYMBOL_CHARSET: Byte = 2

        /**
         * Constants for the pitch and family of the font.
         * The two low-order bits specify the pitch of the font and can be one of the following values
         */
        const val DEFAULT_PITCH: Byte = 0
        const val FIXED_PITCH: Byte = 1
        const val VARIABLE_PITCH: Byte = 2

        /**
         * Don't care or don't know.
         */
        const val FF_DONTCARE: Byte = 0

        /**
         * Fonts with variable stroke width (proportional) and with serifs. Times New Roman is an example.
         */
        const val FF_ROMAN: Byte = 16

        /**
         * Fonts with variable stroke width (proportional) and without serifs. Arial is an example.
         */
        const val FF_SWISS: Byte = 32

        /**
         * Fonts designed to look like handwriting. Script and Cursive are examples.
         */
        const val FF_SCRIPT: Byte = 64

        /**
         * Fonts with constant stroke width (monospace), with or without serifs.
         * Monospace fonts are usually modern. CourierNew is an example
         */
        const val FF_MODERN: Byte = 48

        /**
         * Novelty fonts. Old English is an example
         */
        const val FF_DECORATIVE: Byte = 80

        val ARIAL: PPFont
        val TIMES_NEW_ROMAN: PPFont
        val COURIER_NEW: PPFont
        val WINGDINGS: PPFont

        init {
            ARIAL = PPFont()
            ARIAL.fontName = "Arial"
            ARIAL.charSet = ANSI_CHARSET.toInt()
            ARIAL.fontType = 4
            ARIAL.fontFlags = 0
            ARIAL.pitchAndFamily =
                VARIABLE_PITCH.toInt() or FF_SWISS.toInt()

            TIMES_NEW_ROMAN = PPFont()
            TIMES_NEW_ROMAN.fontName = "Times New Roman"
            TIMES_NEW_ROMAN.charSet = ANSI_CHARSET.toInt()
            TIMES_NEW_ROMAN.fontType = 4
            TIMES_NEW_ROMAN.fontFlags = 0
            TIMES_NEW_ROMAN.pitchAndFamily =
                VARIABLE_PITCH.toInt() or FF_ROMAN.toInt()

            COURIER_NEW = PPFont()
            COURIER_NEW.fontName = "Courier New"
            COURIER_NEW.charSet = ANSI_CHARSET.toInt()
            COURIER_NEW.fontType = 4
            COURIER_NEW.fontFlags = 0
            COURIER_NEW.pitchAndFamily =
                FIXED_PITCH.toInt() or FF_MODERN.toInt()

            WINGDINGS = PPFont()
            WINGDINGS.fontName = "Wingdings"
            WINGDINGS.charSet = SYMBOL_CHARSET.toInt()
            WINGDINGS.fontType = 4
            WINGDINGS.fontFlags = 0
            WINGDINGS.pitchAndFamily =
                VARIABLE_PITCH.toInt() or FF_DONTCARE.toInt()
        }
    }
}
