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
package com.wxiwei.office.fc.hslf.record

import com.wxiwei.office.fc.util.LittleEndian
import java.io.UnsupportedEncodingException

/**
 * This atom corresponds exactly to a Windows Logical Font (LOGFONT) structure.
 * It keeps all the information needed to define the attributes of a font,
 * such as height, width, etc. For more information, consult the
 * Windows API Programmer's reference.
 * 
 * @author Yegor Kozlov
 */
class FontEntityAtom : RecordAtom {
    /**
     * record header
     */
    private var _header: ByteArray?

    /**
     * record data
     */
    private var _recdata: ByteArray?

    /**
     * Build an instance of `FontEntityAtom` from on-disk data
     */
    protected constructor(source: ByteArray, start: Int, len: Int) {
        // Get the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Grab the record data
        _recdata = ByteArray(len - 8)
        System.arraycopy(source, start + 8, _recdata, 0, len - 8)
    }

    /**
     * Create a new instance of `FontEntityAtom`
     */
    constructor() {
        _recdata = ByteArray(68)

        _header = ByteArray(8)
        LittleEndian.putShort(_header!!, 2, getRecordType().toShort())
        LittleEndian.putInt(_header!!, 4, _recdata!!.size)
    }

    public override fun getRecordType(): Long {
        return RecordTypes.FontEntityAtom.typeID.toLong()
    }

    var fontName: String?
        /**
         * A null-terminated string that specifies the typeface name of the font.
         * The length of this string must not exceed 32 characters
         * including the null terminator.
         * @return font name
         */
        get() {
            var name: String? = null
            try {
                var i = 0
                while (i < 64) {
                    //loop until find null-terminated end of the font name
                    if (_recdata!![i].toInt() == 0 && _recdata!![i + 1].toInt() == 0) {
                        name = kotlin.text.String(_recdata!!, 0, i, charset("UTF-16LE"))
                        break
                    }
                    i += 2
                }
            } catch (e: UnsupportedEncodingException) {
                throw RuntimeException(e.message, e)
            }
            return name
        }
        /**
         * Set the name of the font.
         * The length of this string must not exceed 32 characters
         * including the null terminator.
         * Will be converted to null-terminated if not already
         * @param name of the font
         */
        set(name) {
            // Add a null termination if required
            var name: String = name!!
            if (!name.endsWith("\u0000")) {
                name = name + "\u0000"
            }

            // Ensure it's not now too long
            if (name.length > 32) {
                throw RuntimeException(
                    "The length of the font name, including null termination, must not exceed 32 characters"
                )
            }

            // Everything's happy, so save the name
            try {
                val bytes = name.toByteArray(charset("UTF-16LE"))
                System.arraycopy(bytes, 0, _recdata, 0, bytes.size)
            } catch (e: UnsupportedEncodingException) {
                throw RuntimeException(e.message, e)
            }
        }

    var fontIndex: Int
        get() = LittleEndian.getShort(_header!!, 0).toInt() shr 4
        set(idx) {
            LittleEndian.putShort(_header!!, 0, idx.toShort())
        }

    var charSet: Int
        /**
         * get the character set
         * 
         * @return charset - characterset
         */
        get() = _recdata!![64].toInt()
        /**
         * set the character set
         * 
         * @param charset - characterset
         */
        set(charset) {
            _recdata!![64] = charset.toByte()
        }

    var fontFlags: Int
        /**
         * get the character set
         * Bit 1: If set, font is subsetted
         * 
         * @return the font flags
         */
        get() = _recdata!![65].toInt()
        /**
         * set the font flags
         * Bit 1: If set, font is subsetted
         * 
         * @param flags - the font flags
         */
        set(flags) {
            _recdata!![65] = flags.toByte()
        }

    var fontType: Int
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
        get() = _recdata!![66].toInt()
        /**
         * set the font type
         * 
         * 
         * Bit 1: Raster Font
         * Bit 2: Device Font
         * Bit 3: TrueType Font
         * 
         * 
         * @param type - the font type
         */
        set(type) {
            _recdata!![66] = type.toByte()
        }

    var pitchAndFamily: Int
        /**
         * get lfPitchAndFamily
         * 
         * @return corresponds to the lfPitchAndFamily field of the Win32 API LOGFONT structure
         */
        get() = _recdata!![67].toInt()
        /**
         * set lfPitchAndFamily
         * 
         * 
         * @param val - Corresponds to the lfPitchAndFamily field of the Win32 API LOGFONT structure
         */
        set(`val`) {
            _recdata!![67] = `val`.toByte()
        }


    /**
     * 
     */
    public override fun dispose() {
        _header = null
        _recdata = null
    }
}
