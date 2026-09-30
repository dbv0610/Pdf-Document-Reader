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
package com.wxiwei.office.fc.hssf.formula

import com.wxiwei.office.fc.hssf.formula.ptg.AbstractFunctionPtg
import com.wxiwei.office.fc.hssf.formula.ptg.AttrPtg
import com.wxiwei.office.fc.hssf.formula.ptg.ControlPtg
import com.wxiwei.office.fc.hssf.formula.ptg.FuncVarPtg
import com.wxiwei.office.fc.hssf.formula.ptg.MemAreaPtg
import com.wxiwei.office.fc.hssf.formula.ptg.MemFuncPtg
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.formula.ptg.RangePtg
import com.wxiwei.office.fc.hssf.formula.ptg.UnionPtg
import com.wxiwei.office.fc.hssf.formula.ptg.ValueOperatorPtg

/**
 * This class performs 'operand class' transformation. Non-base tokens are classified into three
 * operand classes:
 * 
 *  * reference
 *  * value
 *  * array
 * 
 * 
 * 
 * 
 * The final operand class chosen for each token depends on the formula type and the token's place
 * in the formula. If POI gets the operand class wrong, Excel *may* interpret the formula
 * incorrectly.  This condition is typically manifested as a formula cell that displays as '#VALUE!',
 * but resolves correctly when the user presses F2, enter.
 *
 *
 * 
 * The logic implemented here was partially inspired by the description in
 * "OpenOffice.org's Documentation of the Microsoft Excel File Format".  The model presented there
 * seems to be inconsistent with observed Excel behaviour (These differences have not been fully
 * investigated). The implementation in this class has been heavily modified in order to satisfy
 * concrete examples of how Excel performs the same logic (see TestRVA).
 *
 *
 * 
 * Hopefully, as additional important test cases are identified and added to the test suite,
 * patterns might become more obvious in this code and allow for simplification.
 * 
 * @author Josh Micich
 */
internal class OperandClassTransformer(private val _formulaType: Int) {
    /**
     * Traverses the supplied formula parse tree, calling <tt>Ptg.setClass()</tt> for each non-base
     * token to set its operand class.
     */
    fun transformFormula(rootNode: ParseNode) {
        val rootNodeOperandClass: Byte
        when (_formulaType) {
            FormulaType.CELL -> rootNodeOperandClass = Ptg.CLASS_VALUE
            FormulaType.ARRAY -> rootNodeOperandClass = Ptg.CLASS_ARRAY
            FormulaType.NAMEDRANGE, FormulaType.DATAVALIDATION_LIST -> rootNodeOperandClass =
                Ptg.CLASS_REF

            else -> throw RuntimeException(
                ("Incomplete code - formula type ("
                        + _formulaType + ") not supported yet")
            )

        }
        transformNode(rootNode, rootNodeOperandClass, false)
    }

    /**
     * @param callerForceArrayFlag `true` if one of the current node's parents is a
     * function Ptg which has been changed from default 'V' to 'A' type (due to requirements on
     * the function return value).
     */
    private fun transformNode(
        node: ParseNode, desiredOperandClass: Byte,
        callerForceArrayFlag: Boolean
    ) {
        var token = node.token
        val children = node.children
        val isSimpleValueFunc: Boolean = isSimpleValueFunction(token)

        if (isSimpleValueFunc) {
            val localForceArray = desiredOperandClass == Ptg.CLASS_ARRAY
            for (i in children.indices) {
                transformNode(children[i], desiredOperandClass, localForceArray)
            }
            setSimpleValueFuncClass(
                token as AbstractFunctionPtg,
                desiredOperandClass,
                callerForceArrayFlag
            )
            return
        }

        if (isSingleArgSum(token)) {
            // Need to process the argument of SUM with transformFunctionNode below
            // so make a dummy FuncVarPtg for that call.
            token = FuncVarPtg.SUM
            // Note - the tAttrSum token (node.getToken()) is a base
            // token so does not need to have its operand class set
        }
        if (token is ValueOperatorPtg || token is ControlPtg
            || token is MemFuncPtg
            || token is MemAreaPtg
            || token is UnionPtg
        ) {
            // Value Operator Ptgs and Control are base tokens, so token will be unchanged
            // but any child nodes are processed according to desiredOperandClass and callerForceArrayFlag

            // As per OOO documentation Sec 3.2.4 "Token Class Transformation", "Step 1"
            // All direct operands of value operators that are initially 'R' type will
            // be converted to 'V' type.

            val localDesiredOperandClass =
                if (desiredOperandClass == Ptg.CLASS_REF) Ptg.CLASS_VALUE else desiredOperandClass
            for (i in children.indices) {
                transformNode(children[i], localDesiredOperandClass, callerForceArrayFlag)
            }
            return
        }
        if (token is AbstractFunctionPtg) {
            transformFunctionNode(token, children, desiredOperandClass, callerForceArrayFlag)
            return
        }
        if (children.size > 0) {
            if (token === RangePtg.instance) {
                // TODO is any token transformation required under the various ref operators?
                return
            }
            throw IllegalStateException("Node should not have any children")
        }

        if (token.isBaseToken) {
            // nothing to do
            return
        }
        token.setClass(transformClass(token.ptgClass, desiredOperandClass, callerForceArrayFlag))
    }

    private fun transformClass(
        currentOperandClass: Byte, desiredOperandClass: Byte,
        callerForceArrayFlag: Boolean
    ): Byte {
        when (desiredOperandClass) {
            Ptg.CLASS_VALUE -> {
                if (!callerForceArrayFlag) {
                    return Ptg.CLASS_VALUE
                }
                return Ptg.CLASS_ARRAY
            }

            Ptg.CLASS_ARRAY -> return Ptg.CLASS_ARRAY
            Ptg.CLASS_REF -> {
                if (!callerForceArrayFlag) {
                    return currentOperandClass
                }
                return Ptg.CLASS_REF
            }
        }
        throw IllegalStateException("Unexpected operand class (" + desiredOperandClass + ")")
    }

    private fun transformFunctionNode(
        afp: AbstractFunctionPtg, children: Array<ParseNode>,
        desiredOperandClass: Byte, callerForceArrayFlag: Boolean
    ) {
        val localForceArrayFlag: Boolean
        val defaultReturnOperandClass = afp.defaultOperandClass

        if (callerForceArrayFlag) {
            when (defaultReturnOperandClass) {
                Ptg.CLASS_REF -> {
                    if (desiredOperandClass == Ptg.CLASS_REF) {
                        afp.setClass(Ptg.CLASS_REF)
                    } else {
                        afp.setClass(Ptg.CLASS_ARRAY)
                    }
                    localForceArrayFlag = false
                }

                Ptg.CLASS_ARRAY -> {
                    afp.setClass(Ptg.CLASS_ARRAY)
                    localForceArrayFlag = false
                }

                Ptg.CLASS_VALUE -> {
                    afp.setClass(Ptg.CLASS_ARRAY)
                    localForceArrayFlag = true
                }

                else -> throw IllegalStateException(
                    ("Unexpected operand class ("
                            + defaultReturnOperandClass + ")")
                )
            }
        } else {
            if (defaultReturnOperandClass == desiredOperandClass) {
                localForceArrayFlag = false
                // an alternative would have been to for non-base Ptgs to set their operand class
                // from their default, but this would require the call in many subclasses because
                // the default OC is not known until the end of the constructor
                afp.setClass(defaultReturnOperandClass)
            } else {
                when (desiredOperandClass) {
                    Ptg.CLASS_VALUE -> {
                        // always OK to set functions to return 'value'
                        afp.setClass(Ptg.CLASS_VALUE)
                        localForceArrayFlag = false
                    }

                    Ptg.CLASS_ARRAY -> {
                        when (defaultReturnOperandClass) {
                            Ptg.CLASS_REF -> afp.setClass(Ptg.CLASS_REF)
                            Ptg.CLASS_VALUE -> afp.setClass(Ptg.CLASS_ARRAY)
                            else -> throw IllegalStateException(
                                ("Unexpected operand class ("
                                        + defaultReturnOperandClass + ")")
                            )
                        }
                        localForceArrayFlag = (defaultReturnOperandClass == Ptg.CLASS_VALUE)
                    }

                    Ptg.CLASS_REF -> {
                        when (defaultReturnOperandClass) {
                            Ptg.CLASS_ARRAY -> afp.setClass(Ptg.CLASS_ARRAY)
                            Ptg.CLASS_VALUE -> afp.setClass(Ptg.CLASS_VALUE)
                            else -> throw IllegalStateException(
                                ("Unexpected operand class ("
                                        + defaultReturnOperandClass + ")")
                            )
                        }
                        localForceArrayFlag = false
                    }

                    else -> throw IllegalStateException(
                        ("Unexpected operand class ("
                                + desiredOperandClass + ")")
                    )
                }
            }
        }

        for (i in children.indices) {
            val child = children[i]
            val paramOperandClass = afp.getParameterClass(i)
            transformNode(child, paramOperandClass, localForceArrayFlag)
        }
    }

    private fun setSimpleValueFuncClass(
        afp: AbstractFunctionPtg,
        desiredOperandClass: Byte, callerForceArrayFlag: Boolean
    ) {
        if (callerForceArrayFlag || desiredOperandClass == Ptg.CLASS_ARRAY) {
            afp.setClass(Ptg.CLASS_ARRAY)
        } else {
            afp.setClass(Ptg.CLASS_VALUE)
        }
    }

    companion object {
        private fun isSingleArgSum(token: Ptg?): Boolean {
            if (token is AttrPtg) {
                val attrPtg = token
                return attrPtg.isSum
            }
            return false
        }

        private fun isSimpleValueFunction(token: Ptg?): Boolean {
            if (token is AbstractFunctionPtg) {
                val aptg = token
                if (aptg.defaultOperandClass != Ptg.CLASS_VALUE) {
                    return false
                }
                val numberOfOperands = aptg.numberOfOperands
                for (i in numberOfOperands - 1 downTo 0) {
                    if (aptg.getParameterClass(i) != Ptg.CLASS_VALUE) {
                        return false
                    }
                }
                return true
            }
            return false
        }
    }
}
