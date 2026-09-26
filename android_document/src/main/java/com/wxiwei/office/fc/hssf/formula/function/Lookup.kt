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
import com.wxiwei.office.fc.hssf.formula.eval.EvaluationException
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver.getSingleValue
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.fc.hssf.formula.function.LookupUtils.ValueVector


/**
 * Implementation of Excel function LOOKUP.
 *
 *
 * 
 * LOOKUP finds an index  row in a lookup table by the first column value and returns the value from another column.
 * 
 * **Syntax**:<br></br>
 * **VLOOKUP**(**lookup_value**, **lookup_vector**, result_vector)
 *
 *
 * 
 * **lookup_value**  The value to be found in the lookup vector.<br></br>
 * **lookup_vector**> An area reference for the lookup data. <br></br>
 * **result_vector** Single row or single column area reference from which the result value is chosen.<br></br>
 * 
 * @author Josh Micich
 */
class Lookup : Var2or3ArgFunction() {
    override fun evaluate(
        srcRowIndex: Int,
        srcColumnIndex: Int,
        arg0: ValueEval?,
        arg1: ValueEval?
    ): ValueEval? {
        // complex rules to choose lookupVector and resultVector from the single area ref
        throw RuntimeException("Two arg version of LOOKUP not supported yet")
    }

    override fun evaluate(
        srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?, arg1: ValueEval?,
        arg2: ValueEval?
    ): ValueEval? {
        try {
            val lookupValue = getSingleValue(arg0, srcRowIndex, srcColumnIndex)
            val aeLookupVector = LookupUtils.resolveTableArrayArg(arg1)
            val aeResultVector = LookupUtils.resolveTableArrayArg(arg2)

            val lookupVector: ValueVector = createVector(aeLookupVector)
            val resultVector: ValueVector = createVector(aeResultVector)
            if (lookupVector.size > resultVector.size) {
                // Excel seems to handle this by accessing past the end of the result vector.
                throw RuntimeException("Lookup vector and result vector of differing sizes not supported yet")
            }
            val index = LookupUtils.lookupIndexOfValue(lookupValue, lookupVector, true)
            if (index >= 0) {
                return resultVector.getItem(index)
            }

            return null
        } catch (e: EvaluationException) {
            return e.errorEval
        }
    }

    companion object {
        private fun createVector(ae: TwoDEval): ValueVector {
            val result = LookupUtils.createVector(ae)
            if (result != null) {
                return result
            }
            // extra complexity required to emulate the way LOOKUP can handles these abnormal cases.
            throw RuntimeException("non-vector lookup or result areas not supported yet")
        }
    }
}
