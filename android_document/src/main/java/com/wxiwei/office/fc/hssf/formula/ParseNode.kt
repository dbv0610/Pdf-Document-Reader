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

import com.wxiwei.office.fc.hssf.formula.function.FunctionMetadataRegistry
import com.wxiwei.office.fc.hssf.formula.ptg.ArrayPtg
import com.wxiwei.office.fc.hssf.formula.ptg.AttrPtg.Companion.createIf
import com.wxiwei.office.fc.hssf.formula.ptg.AttrPtg.Companion.createSkip
import com.wxiwei.office.fc.hssf.formula.ptg.FuncVarPtg
import com.wxiwei.office.fc.hssf.formula.ptg.MemAreaPtg
import com.wxiwei.office.fc.hssf.formula.ptg.MemFuncPtg
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg


/**
 * Represents a syntactic element from a formula by encapsulating the corresponding <tt>Ptg</tt>
 * token.  Each <tt>ParseNode</tt> may have child <tt>ParseNode</tt>s in the case when the wrapped
 * <tt>Ptg</tt> is non-atomic.
 * 
 * @author Josh Micich
 */
internal class ParseNode @JvmOverloads constructor(
    token: Ptg,
    children: Array<ParseNode> = EMPTY_ARRAY
) {
    val token: Ptg
    val children: Array<ParseNode>
    private var _isIf: Boolean
    private val tokenCount: Int

    init {
        requireNotNull(token) { "token must not be null" }
        this.token = token
        this.children = children
        _isIf = isIf(token)
        var tokenCount = 1
        for (i in children.indices) {
            tokenCount += children[i].tokenCount
        }
        if (_isIf) {
            // there will be 2 or 3 extra tAttr tokens according to whether the false param is present
            tokenCount += children.size
        }
        this.tokenCount = tokenCount
    }

    constructor(token: Ptg, child0: ParseNode) : this(token, arrayOf<ParseNode>(child0))
    constructor(token: Ptg, child0: ParseNode, child1: ParseNode) : this(
        token,
        arrayOf<ParseNode>(child0, child1)
    )

    val encodedSize: Int
        get() {
            var result =
                if (this.token is ArrayPtg) ArrayPtg.PLAIN_TOKEN_SIZE else token.size
            for (i in children.indices) {
                result += this.children[i].encodedSize
            }
            return result
        }

    private fun collectPtgs(temp: TokenCollector) {
        if (isIf(this.token)) {
            collectIfPtgs(temp)
            return
        }
        val isPreFixOperator = this.token is MemFuncPtg || this.token is MemAreaPtg
        if (isPreFixOperator) {
            temp.add(this.token)
        }
        for (i in 0..<this.children.size) {
            this.children[i].collectPtgs(temp)
        }
        if (!isPreFixOperator) {
            temp.add(this.token)
        }
    }

    /**
     * The IF() function gets marked up with two or three tAttr tokens.
     * Similar logic will be required for CHOOSE() when it is supported
     * 
     * See excelfileformat.pdf sec 3.10.5 "tAttr (19H)
     */
    private fun collectIfPtgs(temp: TokenCollector) {
        // condition goes first

        this.children[0].collectPtgs(temp)

        // placeholder for tAttrIf
        val ifAttrIndex = temp.createPlaceholder()

        // true parameter
        this.children[1].collectPtgs(temp)

        // placeholder for first skip attr
        val skipAfterTrueParamIndex = temp.createPlaceholder()
        val trueParamSize = temp.sumTokenSizes(ifAttrIndex + 1, skipAfterTrueParamIndex)

        val attrIf =
            createIf(trueParamSize + 4) // distance to start of false parameter/tFuncVar. +4 for tAttrSkip after true

        if (this.children.size > 2) {
            // false param present

            // false parameter

            this.children[2].collectPtgs(temp)

            val skipAfterFalseParamIndex = temp.createPlaceholder()

            val falseParamSize =
                temp.sumTokenSizes(skipAfterTrueParamIndex + 1, skipAfterFalseParamIndex)

            val attrSkipAfterTrue =
                createSkip(falseParamSize + 4 + 4 - 1) // 1 less than distance to end of if FuncVar(size=4). +4 for attr skip before
            val attrSkipAfterFalse =
                createSkip(4 - 1) // 1 less than distance to end of if FuncVar(size=4).

            temp.setPlaceholder(ifAttrIndex, attrIf)
            temp.setPlaceholder(skipAfterTrueParamIndex, attrSkipAfterTrue)
            temp.setPlaceholder(skipAfterFalseParamIndex, attrSkipAfterFalse)
        } else {
            // false parameter not present
            val attrSkipAfterTrue =
                createSkip(4 - 1) // 1 less than distance to end of if FuncVar(size=4).

            temp.setPlaceholder(ifAttrIndex, attrIf)
            temp.setPlaceholder(skipAfterTrueParamIndex, attrSkipAfterTrue)
        }
        temp.add(this.token)
    }

    private class TokenCollector(tokenCount: Int) {
        val result: Array<Ptg?>
        private var _offset = 0

        init {
            this.result = arrayOfNulls<Ptg>(tokenCount)
        }

        fun sumTokenSizes(fromIx: Int, toIx: Int): Int {
            var result = 0
            for (i in fromIx..<toIx) {
                result += this.result[i]!!.size
            }
            return result
        }

        fun createPlaceholder(): Int {
            return _offset++
        }

        fun add(token: Ptg) {
            requireNotNull(token) { "token must not be null" }
            this.result[_offset] = token
            _offset++
        }

        fun setPlaceholder(index: Int, token: Ptg?) {
            check(this.result[index] == null) { "Invalid placeholder index (" + index + ")" }
            this.result[index] = token
        }
    }

    companion object {
        val EMPTY_ARRAY: Array<ParseNode> = arrayOf<ParseNode>()

        /**
         * Collects the array of <tt>Ptg</tt> tokens for the specified tree.
         */
        fun toTokenArray(rootNode: ParseNode): Array<Ptg?> {
            val temp = TokenCollector(rootNode.tokenCount)
            rootNode.collectPtgs(temp)
            return temp.result
        }

        private fun isIf(token: Ptg?): Boolean {
            if (token is FuncVarPtg) {
                val func = token
                if (FunctionMetadataRegistry.FUNCTION_NAME_IF == func.name) {
                    return true
                }
            }
            return false
        }
    }
}
