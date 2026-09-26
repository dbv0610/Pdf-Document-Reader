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
 * Implementation of the VLOOKUP() function.
 *
 *
 * 
 * VLOOKUP finds a row in a lookup table by the first column value and returns the value from another column.<br></br>
 * 
 * **Syntax**:<br></br>
 * **VLOOKUP**(**lookup_value**, **table_array**, **col_index_num**, range_lookup)
 *
 *
 * 
 * **lookup_value**  The value to be found in the first column of the table array.<br></br>
 * **table_array** An area reference for the lookup data. <br></br>
 * **col_index_num** a 1 based index specifying which column value of the lookup data will be returned.<br></br>
 * **range_lookup** If TRUE (default), VLOOKUP finds the largest value less than or equal to
 * the lookup_value.  If FALSE, only exact matches will be considered<br></br>
 * 
 * @author Josh Micich
 */
class Vlookup : Var3or4ArgFunction() {
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
            // arg0 lookup_value, arg1 table_array, arg3 range_lookup, find lookup value, arg2 col_index, fetch result
            val lookupValue = getSingleValue(arg0, srcRowIndex, srcColumnIndex)
            val tableArray = LookupUtils.resolveTableArrayArg(arg1)
            val isRangeLookup = LookupUtils.resolveRangeLookupArg(arg3, srcRowIndex, srcColumnIndex)
            val rowIndex = LookupUtils.lookupIndexOfValue(
                srcRowIndex,
                srcColumnIndex,
                lookupValue,
                LookupUtils.createColumnVector(tableArray, 0),
                isRangeLookup
            )
            val colIndex = LookupUtils.resolveRowOrColIndexArg(arg2, srcRowIndex, srcColumnIndex)
            val resultCol = createResultColumnVector(tableArray, colIndex)
            return resultCol.getItem(rowIndex)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
    }


    /**
     * Returns one column from an <tt>AreaEval</tt>
     * 
     * @param colIndex assumed to be non-negative
     * 
     * @throws EvaluationException (#REF!) if colIndex is too high
     */
    @Throws(EvaluationException::class)
    private fun createResultColumnVector(tableArray: TwoDEval, colIndex: Int): ValueVector {
        if (colIndex >= tableArray.width) {
            throw EvaluationException.invalidRef()
        }
        return LookupUtils.createColumnVector(tableArray, colIndex)
    }

    companion object {
        private val DEFAULT_ARG3: ValueEval = BoolEval.TRUE
    }
}
