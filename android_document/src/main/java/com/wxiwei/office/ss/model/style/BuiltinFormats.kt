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

package com.wxiwei.office.ss.model.style

/**
 * Utility to identify built-in formats.  The following is a list of the formats as
 * returned by this class.
 *
 *       0, "General"
 *       1, "0"
 *       2, "0.00"
 *       3, "#,##0"
 *       4, "#,##0.00"
 *       5, "$#,##0_);($#,##0)"
 *       6, "$#,##0_);[Red]($#,##0)"
 *       7, "$#,##0.00);($#,##0.00)"
 *       8, "$#,##0.00_);[Red]($#,##0.00)"
 *       9, "0%"
 *       0xa, "0.00%"
 *       0xb, "0.00E+00"
 *       0xc, "# ?/?"
 *       0xd, "# ??/??"
 *       0xe, "m/d/yy"
 *       0xf, "d-mmm-yy"
 *       0x10, "d-mmm"
 *       0x11, "mmm-yy"
 *       0x12, "h:mm AM/PM"
 *       0x13, "h:mm:ss AM/PM"
 *       0x14, "h:mm"
 *       0x15, "h:mm:ss"
 *       0x16, "m/d/yy h:mm"
 *
 *       // 0x17 - 0x24 reserved for international and undocumented
 *       0x25, "#,##0_);(#,##0)"
 *       0x26, "#,##0_);[Red](#,##0)"
 *       0x27, "#,##0.00_);(#,##0.00)"
 *       0x28, "#,##0.00_);[Red](#,##0.00)"
 *       0x29, "_(*#,##0_);_(*(#,##0);_(* \"-\"_);_(@_)"
 *       0x2a, "_($*#,##0_);_($*(#,##0);_($* \"-\"_);_(@_)"
 *       0x2b, "_(*#,##0.00_);_(*(#,##0.00);_(*\"-\"??_);_(@_)"
 *       0x2c, "_($*#,##0.00_);_($*(#,##0.00);_($*\"-\"??_);_(@_)"
 *       0x2d, "mm:ss"
 *       0x2e, "[h]:mm:ss"
 *       0x2f, "mm:ss.0"
 *       0x30, "##0.0E+0"
 *       0x31, "@" - This is text format.
 *       0x31  "text" - Alias for "@"
 *
 * @author Yegor Kozlov
 *
 * Modified 6/17/09 by Stanislav Shor - positive formats don't need starting '('
 */
object BuiltinFormats {
    /**
     * The first user-defined format starts at 164.
     */
    const val FIRST_USER_DEFINED_FORMAT_INDEX = 164

    private val _formats: Array<String>

    /*
    0 General General 18 Time h:mm AM/PM
    1 Decimal 0 19 Time h:mm:ss AM/PM
    2 Decimal 0.00 20 Time h:mm
    3 Decimal #,##0 21 Time h:mm:ss
    4 Decimal #,##0.00 2232 Date/Time M/D/YY h:mm
    531 Currency "$"#,##0_);("$"#,##0) 37 Account. _(#,##0_);(#,##0)
    631 Currency "$"#,##0_);[Red]("$"#,##0) 38 Account. _(#,##0_);[Red](#,##0)
    731 Currency "$"#,##0.00_);("$"#,##0.00) 39 Account. _(#,##0.00_);(#,##0.00)
    831 Currency "$"#,##0.00_);[Red]("$"#,##0.00) 40 Account. _(#,##0.00_);[Red](#,##0.00)
    9 Percent 0% 4131 Currency _("$"* #,##0_);_("$"* (#,##0);_("$"* "-"_);_(@_)
    10 Percent 0.00% 4231 33 Currency _(* #,##0_);_(* (#,##0);_(* "-"_);_(@_)
    11 Scientific 0.00E+00 4331 Currency _("$"* #,##0.00_);_("$"* (#,##0.00);_("$"* "-"??_);_(@_)
    12 Fraction # ?/? 4431 33 Currency _(* #,##0.00_);_(* (#,##0.00);_(* "-"??_);_(@_)
    13 Fraction # ??/?? 45 Time mm:ss
    1432 Date M/D/YY 46 Time [h]:mm:ss
    15 Date D-MMM-YY 47 Time mm:ss.0
    16 Date D-MMM 48 Scientific ##0.0E+0
    17 Date MMM-YY 49 Text @
    * */
    init {
        val m: MutableList<String> = ArrayList()
        putFormat(m, 0, "General")
        putFormat(m, 1, "0")
        putFormat(m, 2, "0.00")
        putFormat(m, 3, "#,##0")
        putFormat(m, 4, "#,##0.00")
        putFormat(m, 5, "\"$\"#,##0_);(\"$\"#,##0)")
        putFormat(m, 6, "\"$\"#,##0_);[Red](\"$\"#,##0)")
        putFormat(m, 7, "\"$\"#,##0.00_);(\"$\"#,##0.00)")
        putFormat(m, 8, "\"$\"#,##0.00_);[Red](\"$\"#,##0.00)")
        putFormat(m, 9, "0%")
        putFormat(m, 0xa, "0.00%")
        putFormat(m, 0xb, "0.00E+00")
        putFormat(m, 0xc, "# ?/?")
        putFormat(m, 0xd, "# ??/??")
        putFormat(m, 0xe, "m/d/yy")
        putFormat(m, 0xf, "d-mmm-yy")
        putFormat(m, 0x10, "d-mmm")
        putFormat(m, 0x11, "mmm-yy")
        putFormat(m, 0x12, "h:mm AM/PM")
        putFormat(m, 0x13, "h:mm:ss AM/PM")
        putFormat(m, 0x14, "h:mm")
        putFormat(m, 0x15, "h:mm:ss")
        putFormat(m, 0x16, "m/d/yy h:mm")

        // 0x17 - 0x1a reserved for international and undocumented
        for (i in 0x17..0x1a) {
            putFormat(m, i, "reserved-0x" + Integer.toHexString(i))
        }
        // 27-36: East Asian dates/times. Excel and WPS write these ids without a formatCode and
        // show them per locale (58 is "m月d日" in Chinese); outside those locales a short date reads best.
        val date = localeShortDate()
        for (i in 27..31) putFormat(m, i, date)
        putFormat(m, 32, "h:mm")
        putFormat(m, 33, "h:mm:ss")
        putFormat(m, 34, "h:mm")
        putFormat(m, 35, "h:mm:ss")
        putFormat(m, 36, date)

        putFormat(m, 0x25, "#,##0_);(#,##0)")
        putFormat(m, 0x26, "#,##0_);[Red](#,##0)")
        putFormat(m, 0x27, "#,##0.00_);(#,##0.00)")
        putFormat(m, 0x28, "#,##0.00_);[Red](#,##0.00)")
        putFormat(m, 0x29, "_(\"$\"* #,##0_);_(\"$\"* (#,##0);_(\"$\"* \"-\"_);_(@_)")
        putFormat(m, 0x2a, "_(* #,##0_);_(* (#,##0);_(* \"-\"_);_(@_)")
        putFormat(m, 0x2b, "_(\"$\"* #,##0.00_);_(\"$\"* (#,##0.00);_(\"$\"* \"-\"??_);_(@_)")
        putFormat(m, 0x2c, "_(* #,##0.00_);_(* (#,##0.00);_(* \"-\"??_);_(@_)")
        putFormat(m, 0x2d, "mm:ss")
        putFormat(m, 0x2e, "[h]:mm:ss")
        putFormat(m, 0x2f, "mm:ss.0")
        putFormat(m, 0x30, "##0.0E+0")
        putFormat(m, 0x31, "@")

        // 50-58: more East Asian dates/times, see 27-36
        for (i in 50..54) putFormat(m, i, date)
        putFormat(m, 55, "h:mm")
        putFormat(m, 56, "h:mm:ss")
        putFormat(m, 0x39, "yyyy\"年\"m\"月\"")
        putFormat(m, 58, date)

        _formats = m.toTypedArray()
    }

    /** The device's short date as an Excel format, e.g. "d/m/yyyy" for Vietnamese. */
    private fun localeShortDate(): String {
        val pattern = (java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT) as? java.text.SimpleDateFormat)
            ?.toPattern() ?: return "m/d/yyyy"
        val out = StringBuilder()
        var i = 0
        while (i < pattern.length) {
            val c = pattern[i]
            var n = 1
            while (i + n < pattern.length && pattern[i + n] == c) n++
            when (c) {
                'y' -> out.append("yyyy")
                'M' -> out.append(if (n >= 2) "mm" else "m")
                'd' -> out.append(if (n >= 2) "dd" else "d")
                '/', '-', '.', ' ' -> repeat(n) { out.append(c) }
                else -> {}
            }
            i += n
        }
        val result = out.toString().trim()
        return if (result.contains('y') && result.contains('m') && result.contains('d')) result else "m/d/yyyy"
    }

    private fun putFormat(m: MutableList<String>, index: Int, value: String) {
        if (m.size != index) {
            throw IllegalStateException("index $index is wrong")
        }
        m.add(value)
    }

    /**
     * @deprecated (May 2009) use [getAll]
     */
    @JvmStatic
    fun getBuiltinFormats(): Map<Int, String> {
        val result: MutableMap<Int, String> = LinkedHashMap()
        for (i in _formats.indices) {
            result[Integer.valueOf(i)] = _formats[i]
        }
        return result
    }

    /**
     * @return array of built-in data formats
     */
    @JvmStatic
    fun getAll(): Array<String> {
        return _formats.clone()
    }

    /**
     * Get the format string that matches the given format index
     *
     * @param index of a built in format
     * @return string represented at index of format or `null` if there is not a built-in format at that index
     */
    @JvmStatic
    fun getBuiltinFormat(index: Int): String? {
        if (index < 0 || index >= _formats.size) {
            return null
        }
        return _formats[index]
    }

    /**
     * Get the format index that matches the given format string
     *
     * Automatically converts "text" to excel's format string to represent text.
     *
     * @param pFmt string matching a built-in format
     * @return index of format or -1 if undefined.
     */
    @JvmStatic
    fun getBuiltinFormat(pFmt: String): Int {
        val fmt: String
        if (pFmt.equals("TEXT", ignoreCase = true)) {
            fmt = "@"
        } else {
            fmt = pFmt
        }

        for (i in _formats.indices) {
            if (fmt == _formats[i]) {
                return i
            }
        }
        return -1
    }
}
