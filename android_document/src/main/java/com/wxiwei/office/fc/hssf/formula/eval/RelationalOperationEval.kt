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
import com.wxiwei.office.fc.ss.util.NumberComparer


/**
 * Base class for all comparison operator evaluators
 * 
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 */
abstract class RelationalOperationEval : Fixed2ArgFunction() {
    /**
     * Converts a standard compare result (-1, 0, 1) to `true` or `false`
     * according to subclass' comparison type.
     */
    protected abstract fun convertComparisonResult(cmpResult: Int): Boolean

    /**
     * This is a description of how the relational operators apply in MS Excel.
     * Use this as a guideline when testing/implementing the evaluate methods
     * for the relational operators Evals.
     * 
     * <pre>
     * Bool.TRUE > any number.
     * Bool > any string. ALWAYS
     * Bool.TRUE > Bool.FALSE
     * Bool.FALSE == Blank
     * 
     * Strings are never converted to numbers or booleans
     * String > any number. ALWAYS
     * Non-empty String > Blank
     * Empty String == Blank
     * String are sorted dictionary wise
     * 
     * Blank > Negative numbers
     * Blank == 0
     * Blank < Positive numbers
    </pre> * 
     */
    override fun evaluate(
        srcRowIndex: Int,
        srcColumnIndex: Int,
        arg0: ValueEval?,
        arg1: ValueEval?
    ): ValueEval? {
        val vA: ValueEval
        val vB: ValueEval
        try {
            vA = OperandResolver.getSingleValue(arg0, srcRowIndex, srcColumnIndex)
            vB = OperandResolver.getSingleValue(arg1, srcRowIndex, srcColumnIndex)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
        val cmpResult: Int = doCompare(vA, vB)
        val result = convertComparisonResult(cmpResult)
        return BoolEval.Companion.valueOf(result)
    }

    companion object {
        private fun doCompare(va: ValueEval, vb: ValueEval): Int {
            // special cases when one operand is blank
            if (va === BlankEval.instance) {
                return compareBlank(vb)
            }
            if (vb === BlankEval.instance) {
                return -compareBlank(va)
            }

            if (va is BoolEval) {
                if (vb is BoolEval) {
                    val bA = va
                    val bB = vb
                    if (bA.booleanValue == bB.booleanValue) {
                        return 0
                    }
                    return if (bA.booleanValue) 1 else -1
                }
                return 1
            }
            if (vb is BoolEval) {
                return -1
            }
            if (va is StringEval) {
                if (vb is StringEval) {
                    val sA = va
                    val sB = vb
                    return sA.stringValue.compareTo(sB.stringValue, ignoreCase = true)
                }
                return 1
            }
            if (vb is StringEval) {
                return -1
            }
            if (va is NumberEval) {
                if (vb is NumberEval) {
                    val nA = va
                    val nB = vb
                    return NumberComparer.compare(nA.numberValue, nB.numberValue)
                }
            }
            throw IllegalArgumentException(
                ("Bad operand types (" + va.javaClass.getName() + "), ("
                        + vb.javaClass.getName() + ")")
            )
        }

        private fun compareBlank(v: ValueEval): Int {
            if (v === BlankEval.instance) {
                return 0
            }
            if (v is BoolEval) {
                val boolEval = v
                return if (boolEval.booleanValue) -1 else 0
            }
            if (v is NumberEval) {
                val ne = v
                return NumberComparer.compare(0.0, ne.numberValue)
            }
            if (v is StringEval) {
                val se = v
                return if (se.stringValue.length < 1) 0 else -1
            }
            throw IllegalArgumentException("bad value class (" + v.javaClass.getName() + ")")
        }

        val EqualEval: Function = object : RelationalOperationEval() {
            override fun convertComparisonResult(cmpResult: Int): Boolean {
                return cmpResult == 0
            }
        }
        val GreaterEqualEval: Function = object : RelationalOperationEval() {
            override fun convertComparisonResult(cmpResult: Int): Boolean {
                return cmpResult >= 0
            }
        }
        val GreaterThanEval: Function = object : RelationalOperationEval() {
            override fun convertComparisonResult(cmpResult: Int): Boolean {
                return cmpResult > 0
            }
        }
        val LessEqualEval: Function = object : RelationalOperationEval() {
            override fun convertComparisonResult(cmpResult: Int): Boolean {
                return cmpResult <= 0
            }
        }
        val LessThanEval: Function = object : RelationalOperationEval() {
            override fun convertComparisonResult(cmpResult: Int): Boolean {
                return cmpResult < 0
            }
        }
        val NotEqualEval: Function = object : RelationalOperationEval() {
            override fun convertComparisonResult(cmpResult: Int): Boolean {
                return cmpResult != 0
            }
        }
    }
}
