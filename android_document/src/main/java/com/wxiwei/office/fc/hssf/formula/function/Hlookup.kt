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
import com.wxiwei.office.fc.hssf.formula.eval.BoolEval
import com.wxiwei.office.fc.hssf.formula.eval.EvaluationException
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver.getSingleValue
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.fc.hssf.formula.function.LookupUtils.ValueVector


/**
 * Implementation of the HLOOKUP() function.
 *
 *
 * 
 * HLOOKUP finds a column in a lookup table by the first row value and returns the value from another row.<br></br>
 * 
 * **Syntax**:<br></br>
 * **HLOOKUP**(**lookup_value**, **table_array**, **row_index_num**, range_lookup)
 *
 *
 * 
 * **lookup_value**  The value to be found in the first column of the table array.<br></br>
 * **table_array** An area reference for the lookup data. <br></br>
 * **row_index_num** a 1 based index specifying which row value of the lookup data will be returned.<br></br>
 * **range_lookup** If TRUE (default), HLOOKUP finds the largest value less than or equal to
 * the lookup_value.  If FALSE, only exact matches will be considered<br></br>
 * 
 * @author Josh Micich
 */
class Hlookup : Var3or4ArgFunction() {
    override fun evaluate(
        srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?, arg1: ValueEval?,
        arg2: ValueEval?
    ): ValueEval? {
        return evaluate(srcRowIndex, srcColumnIndex, arg0, arg1, arg2, DEFAULT_ARG3)
    }

    override fun evaluate(
        srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?, arg1: ValueEval?,
        arg2: ValueEval?, arg3: ValueEval?
    ): ValueEval? {
        try {
            // Evaluation order:
            // arg0 lookup_value, arg1 table_array, arg3 range_lookup, find lookup value, arg2 row_index, fetch result
            val lookupValue = getSingleValue(arg0, srcRowIndex, srcColumnIndex)
            val tableArray = LookupUtils.resolveTableArrayArg(arg1)
            val isRangeLookup = LookupUtils.resolveRangeLookupArg(arg3, srcRowIndex, srcColumnIndex)
            val colIndex = LookupUtils.lookupIndexOfValue(
                lookupValue,
                LookupUtils.createRowVector(tableArray, 0),
                isRangeLookup
            )
            val rowIndex = LookupUtils.resolveRowOrColIndexArg(arg2, srcRowIndex, srcColumnIndex)
            val resultCol = createResultColumnVector(tableArray, rowIndex)
            return resultCol.getItem(colIndex)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
    }

    /**
     * Returns one column from an <tt>AreaEval</tt>
     * 
     * @param rowIndex assumed to be non-negative
     * 
     * @throws EvaluationException (#REF!) if colIndex is too high
     */
    @Throws(EvaluationException::class)
    private fun createResultColumnVector(tableArray: TwoDEval, rowIndex: Int): ValueVector {
        if (rowIndex >= tableArray.height) {
            throw EvaluationException.invalidRef()
        }
        return LookupUtils.createRowVector(tableArray, rowIndex)
    }

    companion object {
        private val DEFAULT_ARG3: ValueEval = BoolEval.TRUE
    }
}
