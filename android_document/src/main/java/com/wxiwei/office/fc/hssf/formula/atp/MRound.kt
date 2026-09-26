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
package com.wxiwei.office.fc.hssf.formula.atp

import com.wxiwei.office.fc.hssf.formula.OperationEvaluationContext
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.EvaluationException
import com.wxiwei.office.fc.hssf.formula.eval.NumberEval
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.fc.hssf.formula.function.FreeRefFunction
import com.wxiwei.office.fc.hssf.formula.function.NumericFunction

/**
 * Implementation of Excel 'Analysis ToolPak' function MROUND()<br></br>
 * 
 * Returns a number rounded to the desired multiple.
 *
 *
 * 
 * **Syntax**<br></br>
 * **MROUND**(**number**, **multiple**)
 * 
 * 
 * 
 * 
 * @author Yegor Kozlov
 */
internal class MRound private constructor() : FreeRefFunction {
    override fun evaluate(args: Array<ValueEval?>, ec: OperationEvaluationContext): ValueEval? {
        val number: Double
        val multiple: Double
        val result: Double

        if (args.size != 2) {
            return ErrorEval.VALUE_INVALID
        }

        try {
            number = OperandResolver.coerceValueToDouble(
                OperandResolver.getSingleValue(
                    args[0],
                    ec.rowIndex, ec.columnIndex
                )
            )
            multiple = OperandResolver.coerceValueToDouble(
                OperandResolver.getSingleValue(
                    args[1],
                    ec.rowIndex, ec.columnIndex
                )
            )

            if (multiple == 0.0) {
                result = 0.0
            } else {
                if (number * multiple < 0) {
                    // Returns #NUM! because the number and the multiple have different signs
                    throw EvaluationException(ErrorEval.NUM_ERROR)
                }
                result = multiple * Math.round(number / multiple)
            }
            NumericFunction.checkValue(result)
            return NumberEval(result)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
    }

    companion object {
        val instance: FreeRefFunction = MRound()
    }
}
