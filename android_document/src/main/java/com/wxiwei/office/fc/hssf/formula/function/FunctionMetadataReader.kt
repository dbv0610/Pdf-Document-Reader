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
package com.wxiwei.office.fc.hssf.formula.function

import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.util.Arrays
import java.util.regex.Pattern

/**
 * Converts the text meta-data file into a <tt>FunctionMetadataRegistry</tt>
 * 
 * @author Josh Micich
 */
object FunctionMetadataReader {
    private const val METADATA_FILE_NAME = "functionMetadata.txt"

    /** plain ASCII text metadata file uses three dots for ellipsis  */
    private const val ELLIPSIS = "..."

    private val TAB_DELIM_PATTERN: Pattern = Pattern.compile("\t")
    private val SPACE_DELIM_PATTERN: Pattern = Pattern.compile(" ")
    private val EMPTY_BYTE_ARRAY = byteArrayOf()

    private val DIGIT_ENDING_FUNCTION_NAMES = arrayOf<String?>(
        // Digits at the end of a function might be due to a left-over footnote marker.
        // except in these cases
        "LOG10", "ATAN2", "DAYS360", "SUMXMY2", "SUMX2MY2", "SUMX2PY2",
    )
    private val DIGIT_ENDING_FUNCTION_NAMES_SET: MutableSet<*> =
        HashSet<Any?>(Arrays.asList<String?>(*DIGIT_ENDING_FUNCTION_NAMES))

    fun createRegistry(): FunctionMetadataRegistry {
        val `is` = FunctionMetadataReader::class.java.getResourceAsStream(METADATA_FILE_NAME)
        if (`is` == null) {
            throw RuntimeException("resource '" + METADATA_FILE_NAME + "' not found")
        }

        val br: BufferedReader?
        try {
            br = BufferedReader(InputStreamReader(`is`, "UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException(e)
        }
        val fdb = FunctionDataBuilder(400)

        try {
            while (true) {
                val line = br.readLine()
                if (line == null) {
                    break
                }
                if (line.length < 1 || line.get(0) == '#') {
                    continue
                }
                val trimLine = line.trim { it <= ' ' }
                if (trimLine.length < 1) {
                    continue
                }
                processLine(fdb, line)
            }
            br.close()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        return fdb.build()
    }

    private fun processLine(fdb: FunctionDataBuilder, line: String) {
        val parts = TAB_DELIM_PATTERN.split(line, -2)
        if (parts.size != 8) {
            throw RuntimeException("Bad line format '" + line + "' - expected 8 data fields")
        }
        val functionIndex = parseInt(parts[0])
        val functionName = parts[1]
        val minParams = parseInt(parts[2])
        val maxParams = parseInt(parts[3])
        val returnClassCode = parseReturnTypeCode(parts[4])
        val parameterClassCodes = parseOperandTypeCodes(parts[5])
        // 6 isVolatile
        val hasNote = parts[7].length > 0

        validateFunctionName(functionName)
        // TODO - make POI use isVolatile
        fdb.add(
            functionIndex, functionName, minParams, maxParams,
            returnClassCode, parameterClassCodes, hasNote
        )
    }


    private fun parseReturnTypeCode(code: String): Byte {
        if (code.length == 0) {
            return Ptg.CLASS_REF // happens for GETPIVOTDATA
        }
        return parseOperandTypeCode(code)
    }

    private fun parseOperandTypeCodes(codes: String): ByteArray {
        if (codes.length < 1) {
            return EMPTY_BYTE_ARRAY // happens for GETPIVOTDATA
        }
        if (isDash(codes)) {
            // '-' means empty:
            return EMPTY_BYTE_ARRAY
        }
        val array = SPACE_DELIM_PATTERN.split(codes)
        var nItems = array.size
        if (ELLIPSIS == array[nItems - 1]) {
            // final ellipsis is optional, and ignored
            // (all unspecified params are assumed to be the same as the last)
            nItems--
        }
        val result = ByteArray(nItems)
        for (i in 0..<nItems) {
            result[i] = FunctionMetadataReader.parseOperandTypeCode(array[i]!!)
        }
        return result
    }

    private fun isDash(codes: String): Boolean {
        if (codes.length == 1) {
            when (codes.get(0)) {
                '-' -> return true
            }
        }
        return false
    }

    private fun parseOperandTypeCode(code: String): Byte {
        if (code.length != 1) {
            throw RuntimeException("Bad operand type code format '" + code + "' expected single char")
        }
        when (code.get(0)) {
            'V' -> return Ptg.CLASS_VALUE
            'R' -> return Ptg.CLASS_REF
            'A' -> return Ptg.CLASS_ARRAY
        }
        throw IllegalArgumentException("Unexpected operand type code '" + code + "' (" + code.get(0).code + ")")
    }

    /**
     * Makes sure that footnote digits from the original OOO document have not been accidentally
     * left behind
     */
    private fun validateFunctionName(functionName: String) {
        val len = functionName.length
        var ix = len - 1
        if (!Character.isDigit(functionName.get(ix))) {
            return
        }
        while (ix >= 0) {
            if (!Character.isDigit(functionName.get(ix))) {
                break
            }
            ix--
        }
        if (DIGIT_ENDING_FUNCTION_NAMES_SET.contains(functionName)) {
            return
        }
        throw RuntimeException(
            ("Invalid function name '" + functionName
                    + "' (is footnote number incorrectly appended)")
        )
    }

    private fun parseInt(valStr: String): Int {
        try {
            return valStr.toInt()
        } catch (e: NumberFormatException) {
            throw RuntimeException("Value '" + valStr + "' could not be parsed as an integer")
        }
    }
}
