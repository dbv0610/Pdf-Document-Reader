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
package com.wxiwei.office.fc.hssf.formula

import com.wxiwei.office.fc.hssf.formula.ptg.AttrPtg
import com.wxiwei.office.fc.hssf.formula.ptg.MemAreaPtg
import com.wxiwei.office.fc.hssf.formula.ptg.MemErrPtg
import com.wxiwei.office.fc.hssf.formula.ptg.MemFuncPtg
import com.wxiwei.office.fc.hssf.formula.ptg.OperationPtg
import com.wxiwei.office.fc.hssf.formula.ptg.ParenthesisPtg
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import java.util.Stack

/**
 * Common logic for rendering formulas.<br></br>
 * 
 * For POI internal use only
 * 
 * @author Josh Micich
 */
object FormulaRenderer {
    /**
     * Static method to convert an array of [Ptg]s in RPN order
     * to a human readable string format in infix mode.
     * @param book  used for defined names and 3D references
     * @param ptgs  must not be `null`
     * @return a human readable String
     */
    fun toFormulaString(book: FormulaRenderingWorkbook?, ptgs: Array<Ptg>): String? {
        require(!(ptgs == null || ptgs.size == 0)) { "ptgs must not be null" }
        val stack = Stack<String?>()

        for (i in ptgs.indices) {
            val ptg = ptgs[i]
            // TODO - what about MemNoMemPtg?
            if (ptg is MemAreaPtg || ptg is MemFuncPtg || ptg is MemErrPtg) {
                // marks the start of a list of area expressions which will be naturally combined
                // by their trailing operators (e.g. UnionPtg)
                // TODO - put comment and throw exception in toFormulaString() of these classes
                continue
            }
            if (ptg is ParenthesisPtg) {
                val contents = stack.pop()
                stack.push("(" + contents + ")")
                continue
            }
            if (ptg is AttrPtg) {
                val attrPtg = ptg
                if (attrPtg.isOptimizedIf || attrPtg.isOptimizedChoose || attrPtg.isSkip) {
                    continue
                }
                if (attrPtg.isSpace) {
                    // POI currently doesn't render spaces in formulas
                    continue
                    // but if it ever did, care must be taken:
                    // tAttrSpace comes *before* the operand it applies to, which may be consistent
                    // with how the formula text appears but is against the RPN ordering assumed here
                }
                if (attrPtg.isSemiVolatile) {
                    // similar to tAttrSpace - RPN is violated
                    continue
                }
                if (attrPtg.isSum) {
                    val operands = getOperands(stack, attrPtg.numberOfOperands)
                    stack.push(attrPtg.toFormulaString(operands))
                    continue
                }
                throw RuntimeException("Unexpected tAttr: " + attrPtg.toString())
            }

            if (ptg is WorkbookDependentFormula) {
                val optg = ptg as WorkbookDependentFormula
                stack.push(optg.toFormulaString(book!!))
                continue
            }
            if (ptg !is OperationPtg) {
                stack.push(ptg.toFormulaString())
                continue
            }

            val o = ptg
            val operands = getOperands(stack, o.numberOfOperands)
            stack.push(o.toFormulaString(operands))
        }
        check(!stack.isEmpty()) { "Stack underflow" }
        val result = stack.pop()
        check(stack.isEmpty()) { "too much stuff left on the stack" }
        return result
    }

    private fun getOperands(stack: Stack<String?>, nOperands: Int): Array<String?> {
        val operands = arrayOfNulls<String>(nOperands)

        for (j in nOperands - 1 downTo 0) { // reverse iteration because args were pushed in-order
            if (stack.isEmpty()) {
                val msg = ("Too few arguments supplied to operation. Expected (" + nOperands
                        + ") operands but got (" + (nOperands - j - 1) + ")")
                throw IllegalStateException(msg)
            }
            operands[j] = stack.pop()
        }
        return operands
    }
}
