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
/*
 * HSSFDataFormat.java
 *
 * Created on December 18, 2001, 12:42 PM
 */
package com.wxiwei.office.fc.hssf.usermodel

import com.wxiwei.office.fc.hssf.model.InternalWorkbook
import com.wxiwei.office.fc.ss.usermodel.BuiltinFormats
import com.wxiwei.office.fc.ss.usermodel.DataFormat
import java.util.Arrays
import java.util.Locale
import java.util.Vector

/**
 * Identifies both built-in and user defined formats within a workbook.
 *
 *
 * See [BuiltinFormats] for a list of supported built-in formats.
 *
 *
 * 
 * **International Formats**<br></br>
 * Since version 2003 Excel has supported international formats.  These are denoted
 * with a prefix "[$-xxx]" (where xxx is a 1-7 digit hexadecimal number).
 * See the Microsoft article
 * [
 * Creating international number formats
](http://office.microsoft.com/assistance/hfws.aspx?AssetID=HA010346351033&CTT=6&Origin=EC010272491033) *  for more details on these codes.
 * 
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author  Shawn M. Laubach (slaubach at apache dot org)
 */
class HSSFDataFormat internal constructor(private val _workbook: InternalWorkbook) : DataFormat {
    private val _formats = Vector<String?>()
    private var _movedBuiltins = false // Flag to see if need to

    // check the built in list
    // or if the regular list
    // has all entries.
    /**
     * Constructs a new data formatter.  It takes a workbook to have
     * access to the workbooks format records.
     * @param workbook the workbook the formats are tied to.
     */
    init {
        val i = _workbook.formats.iterator()
        while (i.hasNext()) {
            val r = i.next()
            ensureFormatsSize(r.getIndexCode())
            _formats.set(r.getIndexCode(), r.getFormatString())
        }
    }

    /**
     * Get the format index that matches the given format
     * string, creating a new format entry if required.
     * Aliases text to the proper format as required.
     * @param pFormat string matching a built in format
     * @return index of format.
     */
    override fun getFormat(pFormat: String): Short {
        // Normalise the format string
        val format: String?
        if (pFormat.uppercase(Locale.getDefault()) == "TEXT") {
            format = "@"
        } else {
            format = pFormat
        }

        // Merge in the built in formats if we haven't already
        if (!_movedBuiltins) {
            for (i in _builtinFormats.indices) {
                ensureFormatsSize(i)
                if (_formats.get(i) == null) {
                    _formats.set(i, _builtinFormats[i])
                } else {
                    // The workbook overrides this default format
                }
            }
            _movedBuiltins = true
        }


        // See if we can find it
        for (i in _formats.indices) {
            if (format == _formats.get(i)) {
                return i.toShort()
            }
        }

        // We can't find it, so add it as a new one
        val index = _workbook.getFormat(format, true)
        ensureFormatsSize(index.toInt())
        _formats.set(index.toInt(), format)
        return index
    }

    /**
     * get the format string that matches the given format index
     * @param index of a format
     * @return string represented at index of format or null if there is not a  format at that index
     */
    override fun getFormat(index: Short): String? {
        if (_movedBuiltins) {
            return _formats.get(index.toInt())
        }

        if (index.toInt() == -1) {
            // YK: formatIndex can be -1, for example, for cell in column Y in test-data/spreadsheet/45322.xls
            // return null for those
            return null
        }

        val fmt = if (_formats.size > index) _formats.get(index.toInt()) else null
        if (_builtinFormats.size > index && _builtinFormats[index.toInt()] != null) {
            // It's in the built in range
            if (fmt != null) {
                // It's been overriden, use that value
                return fmt
            } else {
                // Standard built in format
                return _builtinFormats[index.toInt()]
            }
        }
        return fmt
    }

    /**
     * Ensures that the formats list can hold entries
     * up to and including the entry with this index
     */
    private fun ensureFormatsSize(index: Int) {
        if (_formats.size <= index) {
            _formats.setSize(index + 1)
        }
    }

    companion object {
        private val _builtinFormats: Array<String?> = BuiltinFormats.getAll()

        val builtinFormats: MutableList<String?>
            get() = Arrays.asList<String?>(*_builtinFormats)

        /**
         * get the format index that matches the given format string
         *
         *
         * Automatically converts "text" to excel's format string to represent text.
         * @param format string matching a built in format
         * @return index of format or -1 if undefined.
         */
        fun getBuiltinFormat(format: String): Short {
            return BuiltinFormats.getBuiltinFormat(format).toShort()
        }

        @JvmStatic

        fun getFormatCode(workbook: InternalWorkbook, index: Short): String? {
            if (index.toInt() == -1) {
                // YK: formatIndex can be -1, for example, for cell in column Y in test-data/spreadsheet/45322.xls
                // return null for those
                return null
            }

            val i = workbook.formats.iterator()
            while (i.hasNext()) {
                val r = i.next()
                if (index.toInt() == r.getIndexCode()) {
                    return r.getFormatString()
                }
            }

            if (_builtinFormats.size > index && _builtinFormats[index.toInt()] != null) {
                // Standard built in format
                return _builtinFormats[index.toInt()]
            }

            return null
        }

        /**
         * get the format string that matches the given format index
         * @param index of a built in format
         * @return string represented at index of format or null if there is not a builtin format at that index
         */
        fun getBuiltinFormat(index: Short): String? {
            return BuiltinFormats.getBuiltinFormat(index.toInt())
        }

        val numberOfBuiltinBuiltinFormats: Int
            /**
             * get the number of built-in and reserved builtinFormats
             * @return number of built-in and reserved builtinFormats
             */
            get() = _builtinFormats.size
    }
}
