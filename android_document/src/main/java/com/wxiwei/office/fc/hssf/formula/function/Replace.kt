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
 * An implementation of the Excel REPLACE() function:
 *
 *
 * Replaces part of a text string based on the number of characters
 * you specify, with another text string.<br></br>
 * 
 * **Syntax**:<br></br>
 * **REPLACE**(**oldText**, **startNum**, **numChars**, **newText**)
 *
 *
 * 
 * **oldText**  The text string containing characters to replace<br></br>
 * **startNum** The position of the first character to replace (1-based)<br></br>
 * **numChars** The number of characters to replace<br></br>
 * **newText** The new text value to replace the removed section<br></br>
 * 
 * @author Manda Wilson &lt; wilson at c bio dot msk cc dot org &gt;
 */
class Replace : Fixed4ArgFunction() {
    override fun evaluate(
        srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?, arg1: ValueEval?,
        arg2: ValueEval?, arg3: ValueEval?
    ): ValueEval? {
        val oldStr: String
        val startNum: Int
        val numChars: Int
        val newStr: String?
        try {
            oldStr = TextFunction.Companion.evaluateStringArg(arg0, srcRowIndex, srcColumnIndex)
            startNum = TextFunction.Companion.evaluateIntArg(arg1, srcRowIndex, srcColumnIndex)
            numChars = TextFunction.Companion.evaluateIntArg(arg2, srcRowIndex, srcColumnIndex)
            newStr = TextFunction.Companion.evaluateStringArg(arg3, srcRowIndex, srcColumnIndex)
        } catch (e: EvaluationException) {
            return e.errorEval
        }

        if (startNum < 1 || numChars < 0) {
            return ErrorEval.VALUE_INVALID
        }
        val strBuff = StringBuffer(oldStr)
        // remove any characters that should be replaced
        if (startNum <= oldStr.length && numChars != 0) {
            strBuff.delete(startNum - 1, startNum - 1 + numChars)
        }
        // now insert (or append) newStr
        if (startNum > strBuff.length) {
            strBuff.append(newStr)
        } else {
            strBuff.insert(startNum - 1, newStr)
        }
        return StringEval(strBuff.toString())
    }
}
