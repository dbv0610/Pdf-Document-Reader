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
import com.wxiwei.office.fc.hssf.formula.eval.NotImplementedException
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver.getSingleValue
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval

/**
 * Implementation for the Excel function SUBTOTAL
 *
 *
 * 
 * **Syntax :** <br></br>
 * SUBTOTAL ( **functionCode**, **ref1**, ref2 ... ) <br></br>
 * <table border="1" cellpadding="1" cellspacing="0" summary="Parameter descriptions">
 * <tr><td>**functionCode**</td><td>(1-11) Selects the underlying aggregate function to be used (see table below)</td></tr>
 * <tr><td>**ref1**, ref2 ...</td><td>Arguments to be passed to the underlying aggregate function</td></tr>
</table> * <br></br>
 * 
 * 
 * <table border="1" cellpadding="1" cellspacing="0" summary="Parameter descriptions">
 * <tr><th>functionCode</th><th>Aggregate Function</th></tr>
 * <tr align='center'><td>1</td><td>AVERAGE</td></tr>
 * <tr align='center'><td>2</td><td>COUNT</td></tr>
 * <tr align='center'><td>3</td><td>COUNTA</td></tr>
 * <tr align='center'><td>4</td><td>MAX</td></tr>
 * <tr align='center'><td>5</td><td>MIN</td></tr>
 * <tr align='center'><td>6</td><td>PRODUCT</td></tr>
 * <tr align='center'><td>7</td><td>STDEV</td></tr>
 * <tr align='center'><td>8</td><td>STDEVP *</td></tr>
 * <tr align='center'><td>9</td><td>SUM</td></tr>
 * <tr align='center'><td>10</td><td>VAR *</td></tr>
 * <tr align='center'><td>11</td><td>VARP *</td></tr>
 * <tr align='center'><td>101-111</td><td>*</td></tr>
</table> * <br></br>
 * * Not implemented in POI yet. Functions 101-111 are the same as functions 1-11 but with
 * the option 'ignore hidden values'.
 * 
 * 
 * 
 * @author Paul Tomlin &lt; pault at bulk sms dot com &gt;
 */
class Subtotal : Function {
    override fun evaluate(
        args: Array<ValueEval?>,
        srcRowIndex: Int,
        srcColumnIndex: Int
    ): ValueEval? {
        val nInnerArgs =
            args.size - 1 // -1: first arg is used to select from a basic aggregate function
        if (nInnerArgs < 1) {
            return ErrorEval.VALUE_INVALID
        }

        val innerFunc: Function?
        try {
            val ve = getSingleValue(args[0], srcRowIndex, srcColumnIndex)
            val functionCode = OperandResolver.coerceValueToInt(ve!!)
            innerFunc = findFunction(functionCode)
        } catch (e: EvaluationException) {
            return e.errorEval
        }

        val innerArgs = arrayOfNulls<ValueEval>(nInnerArgs)
        System.arraycopy(args, 1, innerArgs, 0, nInnerArgs)

        return innerFunc.evaluate(innerArgs, srcRowIndex, srcColumnIndex)
    }

    companion object {
        @Throws(EvaluationException::class)
        private fun findFunction(functionCode: Int): Function {
            var func: Function?
            when (functionCode) {
                1 -> return AggregateFunction.Companion.subtotalInstance(AggregateFunction.Companion.AVERAGE)
                2 -> return Count.Companion.subtotalInstance()
                3 -> return Counta.Companion.subtotalInstance()
                4 -> return AggregateFunction.Companion.subtotalInstance(AggregateFunction.Companion.MAX)
                5 -> return AggregateFunction.Companion.subtotalInstance(AggregateFunction.Companion.MIN)
                6 -> return AggregateFunction.Companion.subtotalInstance(AggregateFunction.Companion.PRODUCT)
                7 -> return AggregateFunction.Companion.subtotalInstance(AggregateFunction.Companion.STDEV)
                8 -> throw NotImplementedException("STDEVP")
                9 -> return AggregateFunction.Companion.subtotalInstance(AggregateFunction.Companion.SUM)
                10 -> throw NotImplementedException("VAR")
                11 -> throw NotImplementedException("VARP")
            }
            if (functionCode > 100 && functionCode < 112) {
                throw NotImplementedException("SUBTOTAL - with 'exclude hidden values' option")
            }
            throw EvaluationException.invalidValue()
        }
    }
}
