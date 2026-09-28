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

import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.EvaluationException
import com.wxiwei.office.fc.hssf.formula.eval.StringEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval

/**
 * An implementation of the SUBSTITUTE function:<P></P>
 * Substitutes text in a text string with new text, some number of times.
 * @author Manda Wilson &lt; wilson at c bio dot msk cc dot org &gt;
 */
class Substitute : Var3or4ArgFunction() {
    override fun evaluate(
        srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?, arg1: ValueEval?,
        arg2: ValueEval?
    ): ValueEval? {
        val result: String?
        try {
            val oldStr: String =
                TextFunction.Companion.evaluateStringArg(arg0, srcRowIndex, srcColumnIndex)
            val searchStr: String =
                TextFunction.Companion.evaluateStringArg(arg1, srcRowIndex, srcColumnIndex)
            val newStr: String =
                TextFunction.Companion.evaluateStringArg(arg2, srcRowIndex, srcColumnIndex)

            result = replaceAllOccurrences(oldStr, searchStr, newStr)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
        return StringEval(result)
    }

    override fun evaluate(
        srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?, arg1: ValueEval?,
        arg2: ValueEval?, arg3: ValueEval?
    ): ValueEval? {
        val result: String
        try {
            val oldStr: String =
                TextFunction.Companion.evaluateStringArg(arg0, srcRowIndex, srcColumnIndex)
            val searchStr: String =
                TextFunction.Companion.evaluateStringArg(arg1, srcRowIndex, srcColumnIndex)
            val newStr: String =
                TextFunction.Companion.evaluateStringArg(arg2, srcRowIndex, srcColumnIndex)

            val instanceNumber: Int =
                TextFunction.Companion.evaluateIntArg(arg3, srcRowIndex, srcColumnIndex)
            if (instanceNumber < 1) {
                return ErrorEval.VALUE_INVALID
            }
            result = replaceOneOccurrence(oldStr, searchStr, newStr, instanceNumber)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
        return StringEval(result)
    }

    companion object {
        private fun replaceAllOccurrences(
            oldStr: String,
            searchStr: String,
            newStr: String?
        ): String {
            val sb = StringBuffer()
            var startIndex = 0
            var nextMatch = -1
            while (true) {
                nextMatch = oldStr.indexOf(searchStr, startIndex)
                if (nextMatch < 0) {
                    // store everything from end of last match to end of string
                    sb.append(oldStr.substring(startIndex))
                    return sb.toString()
                }
                // store everything from end of last match to start of this match
                sb.append(oldStr.substring(startIndex, nextMatch))
                sb.append(newStr)
                startIndex = nextMatch + searchStr.length
            }
        }

        private fun replaceOneOccurrence(
            oldStr: String,
            searchStr: String,
            newStr: String,
            instanceNumber: Int
        ): String {
            if (searchStr.length < 1) {
                return oldStr
            }
            var startIndex = 0
            var nextMatch = -1
            var count = 0
            while (true) {
                nextMatch = oldStr.indexOf(searchStr, startIndex)
                if (nextMatch < 0) {
                    // not enough occurrences found - leave unchanged
                    return oldStr
                }
                count++
                if (count == instanceNumber) {
                    val sb = StringBuffer(oldStr.length + newStr.length)
                    sb.append(oldStr.substring(0, nextMatch))
                    sb.append(newStr)
                    sb.append(oldStr.substring(nextMatch + searchStr.length))
                    return sb.toString()
                }
                startIndex = nextMatch + searchStr.length
            }
        }
    }
}
