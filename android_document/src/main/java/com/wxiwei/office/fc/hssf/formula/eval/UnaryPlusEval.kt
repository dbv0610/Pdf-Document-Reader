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

import com.wxiwei.office.fc.hssf.formula.function.Fixed1ArgFunction
import com.wxiwei.office.fc.hssf.formula.function.Function

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 */
class UnaryPlusEval private constructor() : Fixed1ArgFunction() {
    override fun evaluate(srcCellRow: Int, srcCellCol: Int, arg0: ValueEval?): ValueEval? {
        val d: Double
        try {
            val ve = OperandResolver.getSingleValue(arg0, srcCellRow, srcCellCol)
            if (ve is StringEval) {
                // Note - asymmetric with UnaryMinus
                // -"hello" evaluates to #VALUE!
                // but +"hello" evaluates to "hello"
                return ve
            }
            d = OperandResolver.coerceValueToDouble(ve)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
        return NumberEval(+d)
    }

    companion object {
        val instance: Function = UnaryPlusEval()
    }
}
