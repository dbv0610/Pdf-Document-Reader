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
package com.wxiwei.office.fc.hssf.formula.eval

import com.wxiwei.office.fc.hssf.formula.function.Fixed2ArgFunction
import com.wxiwei.office.fc.hssf.formula.function.Function

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 */
class ConcatEval private constructor() : Fixed2ArgFunction() {
    override fun evaluate(
        srcRowIndex: Int,
        srcColumnIndex: Int,
        arg0: ValueEval?,
        arg1: ValueEval?
    ): ValueEval? {
        val ve0: ValueEval
        val ve1: ValueEval
        try {
            ve0 = OperandResolver.getSingleValue(arg0, srcRowIndex, srcColumnIndex)
            ve1 = OperandResolver.getSingleValue(arg1, srcRowIndex, srcColumnIndex)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
        val sb = StringBuilder()
        sb.append(getText(ve0))
        sb.append(getText(ve1))
        return StringEval(sb.toString())
    }

    private fun getText(ve: ValueEval): Any? {
        if (ve is StringValueEval) {
            val sve = ve
            return sve.stringValue
        }
        if (ve === BlankEval.instance) {
            return ""
        }
        throw IllegalAccessError(
            ("Unexpected value type ("
                    + ve.javaClass.getName() + ")")
        )
    }

    companion object {
        val instance: Function = ConcatEval()
    }
}
