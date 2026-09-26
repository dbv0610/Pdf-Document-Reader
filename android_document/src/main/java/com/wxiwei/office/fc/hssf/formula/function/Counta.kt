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

import com.wxiwei.office.fc.hssf.formula.TwoDEval
import com.wxiwei.office.fc.hssf.formula.eval.BlankEval
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.NumberEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.fc.hssf.formula.function.CountUtils.I_MatchAreaPredicate
import com.wxiwei.office.fc.hssf.formula.function.CountUtils.I_MatchPredicate


/**
 * Counts the number of cells that contain data within the list of arguments.
 * 
 * Excel Syntax
 * COUNTA(value1,value2,...)
 * Value1, value2, ...   are 1 to 30 arguments representing the values or ranges to be counted.
 * 
 * @author Josh Micich
 */
class Counta : Function {
    private val _predicate: I_MatchPredicate

    constructor() {
        _predicate = defaultPredicate
    }

    private constructor(criteriaPredicate: I_MatchPredicate) {
        _predicate = criteriaPredicate
    }

    override fun evaluate(args: Array<ValueEval?>, srcCellRow: Int, srcCellCol: Int): ValueEval {
        val nArgs = args.size
        if (nArgs < 1) {
            // too few arguments
            return ErrorEval.VALUE_INVALID
        }

        if (nArgs > 30) {
            // too many arguments
            return ErrorEval.VALUE_INVALID
        }

        var temp = 0

        for (i in 0..<nArgs) {
            temp += CountUtils.countArg(args[i], _predicate)
        }
        return NumberEval(temp.toDouble())
    }

    companion object {
        private val defaultPredicate: I_MatchPredicate = object : I_MatchPredicate {
            override fun matches(valueEval: ValueEval?): Boolean {
                // Note - observed behavior of Excel:
                // Error values like #VALUE!, #REF!, #DIV/0!, #NAME? etc don't cause this COUNTA to return an error
                // in fact, they seem to get counted

                if (valueEval === BlankEval.instance) {
                    return false
                }
                // Note - everything but BlankEval counts
                return true
            }
        }
        private val subtotalPredicate: I_MatchPredicate = object : I_MatchAreaPredicate {
            override fun matches(valueEval: ValueEval?): Boolean {
                return defaultPredicate.matches(valueEval)
            }

            /**
             * don't count cells that are subtotals
             */
            override fun matches(areEval: TwoDEval, rowIndex: Int, columnIndex: Int): Boolean {
                return !areEval.isSubTotal(rowIndex, columnIndex)
            }
        }

        fun subtotalInstance(): Counta {
            return Counta(subtotalPredicate)
        }
    }
}
