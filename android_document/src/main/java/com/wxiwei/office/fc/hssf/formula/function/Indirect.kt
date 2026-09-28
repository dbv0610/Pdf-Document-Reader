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

import com.wxiwei.office.fc.hssf.formula.OperationEvaluationContext
import com.wxiwei.office.fc.hssf.formula.eval.BlankEval
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.EvaluationException
import com.wxiwei.office.fc.hssf.formula.eval.MissingArgEval
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver.getSingleValue
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval


/**
 * Implementation for Excel function INDIRECT
 *
 *
 * 
 * INDIRECT() returns the cell or area reference denoted by the text argument.
 *
 *
 * 
 * **Syntax**:
 * **INDIRECT**(**ref_text**,isA1Style)
 *
 *
 * 
 * **ref_text** a string representation of the desired reference as it would
 * normally be written in a cell formula.<br></br>
 * **isA1Style** (default TRUE) specifies whether the ref_text should be
 * interpreted as A1-style or R1C1-style.
 * 
 * @author Josh Micich
 */
class Indirect private constructor() : FreeRefFunction {
    override fun evaluate(args: Array<ValueEval?>, ec: OperationEvaluationContext): ValueEval? {
        if (args.size < 1) {
            return ErrorEval.VALUE_INVALID
        }

        val isA1style: Boolean
        val text: String?
        try {
            val ve = getSingleValue(
                args[0], ec.rowIndex, ec
                    .columnIndex
            )
            text = OperandResolver.coerceValueToString(ve!!)
            when (args.size) {
                1 -> isA1style = true
                2 -> isA1style = evaluateBooleanArg(args[1], ec)
                else -> return ErrorEval.VALUE_INVALID
            }
        } catch (e: EvaluationException) {
            return e.errorEval
        }

        return Companion.evaluateIndirect(ec, text!!, isA1style)
    }

    companion object {
        @JvmField
        val instance: FreeRefFunction = Indirect()

        @Throws(EvaluationException::class)
        private fun evaluateBooleanArg(arg: ValueEval?, ec: OperationEvaluationContext): Boolean {
            val ve = getSingleValue(arg, ec.rowIndex, ec.columnIndex)

            if (ve === BlankEval.instance || ve === MissingArgEval.instance) {
                return false
            }
            // numeric quantities follow standard boolean conversion rules
            // for strings, only "TRUE" and "FALSE" (case insensitive) are valid
            return OperandResolver.coerceValueToBoolean(ve, false)!!
        }

        private fun evaluateIndirect(
            ec: OperationEvaluationContext, text: String,
            isA1style: Boolean
        ): ValueEval? {
            // Search backwards for '!' because sheet names can contain '!'
            val plingPos = text.lastIndexOf('!')

            val workbookName: String?
            val sheetName: String?
            val refText: String? // whitespace around this gets trimmed OK
            if (plingPos < 0) {
                workbookName = null
                sheetName = null
                refText = text
            } else {
                val parts: Array<String?>? =
                    parseWorkbookAndSheetName(text.subSequence(0, plingPos))
                if (parts == null) {
                    return ErrorEval.REF_INVALID
                }
                workbookName = parts[0]
                sheetName = parts[1]
                refText = text.substring(plingPos + 1)
            }

            val refStrPart1: String?
            val refStrPart2: String?

            val colonPos = refText.indexOf(':')
            if (colonPos < 0) {
                refStrPart1 = refText.trim { it <= ' ' }
                refStrPart2 = null
            } else {
                refStrPart1 = refText.substring(0, colonPos).trim { it <= ' ' }
                refStrPart2 = refText.substring(colonPos + 1).trim { it <= ' ' }
            }
            return ec.getDynamicReference(
                workbookName,
                sheetName,
                refStrPart1,
                refStrPart2,
                isA1style
            )
        }

        /**
         * @return array of length 2: {workbookName, sheetName,}.  Second element will always be
         * present.  First element may be null if sheetName is unqualified.
         * Returns `null` if text cannot be parsed.
         */
        private fun parseWorkbookAndSheetName(text: CharSequence): Array<String?>? {
            val lastIx = text.length - 1
            if (lastIx < 0) {
                return null
            }
            if (canTrim(text)) {
                return null
            }
            var firstChar = text.get(0)
            if (Character.isWhitespace(firstChar)) {
                return null
            }
            if (firstChar == '\'') {
                // workbookName or sheetName needs quoting
                // quotes go around both
                if (text.get(lastIx) != '\'') {
                    return null
                }
                firstChar = text.get(1)
                if (Character.isWhitespace(firstChar)) {
                    return null
                }
                val wbName: String?
                val sheetStartPos: Int
                if (firstChar == '[') {
                    val rbPos = text.toString().lastIndexOf(']')
                    if (rbPos < 0) {
                        return null
                    }
                    wbName = unescapeString(text.subSequence(2, rbPos))
                    if (wbName == null || canTrim(wbName)) {
                        return null
                    }
                    sheetStartPos = rbPos + 1
                } else {
                    wbName = null
                    sheetStartPos = 1
                }

                // else - just sheet name
                val sheetName: String? = unescapeString(text.subSequence(sheetStartPos, lastIx))
                if (sheetName == null) { // note - when quoted, sheetName can
                    // start/end with whitespace
                    return null
                }
                return arrayOf<String?>(wbName, sheetName)
            }

            if (firstChar == '[') {
                val rbPos = text.toString().lastIndexOf(']')
                if (rbPos < 0) {
                    return null
                }
                val wbName = text.subSequence(1, rbPos)
                if (canTrim(wbName)) {
                    return null
                }
                val sheetName = text.subSequence(rbPos + 1, text.length)
                if (canTrim(sheetName)) {
                    return null
                }
                return arrayOf<String?>(wbName.toString(), sheetName.toString())
            }
            // else - just sheet name
            return arrayOf<String?>(null, text.toString())
        }

        /**
         * @return `null` if there is a syntax error in any escape sequence
         * (the typical syntax error is a single quote character not followed by another).
         */
        private fun unescapeString(text: CharSequence): String? {
            val len = text.length
            val sb = StringBuilder(len)
            var i = 0
            while (i < len) {
                var ch = text.get(i)
                if (ch == '\'') {
                    // every quote must be followed by another
                    i++
                    if (i >= len) {
                        return null
                    }
                    ch = text.get(i)
                    if (ch != '\'') {
                        return null
                    }
                }
                sb.append(ch)
                i++
            }
            return sb.toString()
        }

        private fun canTrim(text: CharSequence): Boolean {
            val lastIx = text.length - 1
            if (lastIx < 0) {
                return false
            }
            if (Character.isWhitespace(text.get(0))) {
                return true
            }
            if (Character.isWhitespace(text.get(lastIx))) {
                return true
            }
            return false
        }
    }
}
