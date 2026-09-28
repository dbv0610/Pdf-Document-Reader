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

import com.wxiwei.office.fc.hssf.formula.TwoDEval
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.MissingArgEval
import com.wxiwei.office.fc.hssf.formula.eval.NumberEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.fc.hssf.formula.function.CountUtils.I_MatchAreaPredicate
import com.wxiwei.office.fc.hssf.formula.function.CountUtils.I_MatchPredicate


/**
 * Counts the number of cells that contain numeric data within
 * the list of arguments.
 * 
 * Excel Syntax
 * COUNT(value1,value2,...)
 * Value1, value2, ...   are 1 to 30 arguments representing the values or ranges to be counted.
 * 
 * TODO: Check this properly matches excel on edge cases
 * like formula cells, error cells etc
 */
class Count : Function {
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
                if (valueEval is NumberEval) {
                    // only numbers are counted
                    return true
                }
                if (valueEval === MissingArgEval.instance) {
                    // oh yeah, and missing arguments
                    return true
                }

                // error values and string values not counted
                return false
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

        /**
         * Create an instance of Count to use in [Subtotal]
         * 
         * 
         * If there are other subtotals within argument refs (or nested subtotals),
         * these nested subtotals are ignored to avoid double counting.
         * 
         * 
         * @see Subtotal
         */
        fun subtotalInstance(): Count {
            return Count(subtotalPredicate)
        }
    }
}
