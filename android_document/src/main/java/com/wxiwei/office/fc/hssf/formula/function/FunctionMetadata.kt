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

/**
 * Holds information about Excel built-in functions.
 * 
 * @author Josh Micich
 */
class FunctionMetadata /* package */ internal constructor(
    val index: Int, val name: String?, val minParams: Int, val maxParams: Int,
    val returnClassCode: Byte, private val _parameterClassCodes: ByteArray
) {
    fun hasFixedArgsLength(): Boolean {
        return this.minParams == this.maxParams
    }

    val parameterClassCodes: ByteArray?
        get() = _parameterClassCodes.clone()

    /**
     * Some varags functions (like VLOOKUP) have a specific limit to the number of arguments that
     * can be passed.  Other functions (like SUM) don't have such a limit.  For those functions,
     * the spreadsheet version determines the maximum number of arguments that can be passed.
     * @return `true` if this function can the maximum number of arguments allowable by
     * the [com.wxiwei.office.fc.ss.SpreadsheetVersion]
     */
    fun hasUnlimitedVarags(): Boolean {
        return FUNCTION_MAX_PARAMS.toInt() == this.maxParams
    }

    override fun toString(): String {
        val sb = StringBuffer(64)
        sb.append(javaClass.getName()).append(" [")
        sb.append(this.index).append(" ").append(this.name)
        sb.append("]")
        return sb.toString()
    }

    companion object {
        /**
         * maxParams=30 in functionMetadata.txt means the maximum number arguments supported
         * by the given version of Excel. Validation routines should take the actual limit (Excel 97 or 2007)
         * from the SpreadsheetVersion enum.
         * Perhaps a value like 'M' should be used instead of '30' in functionMetadata.txt
         * to make that file more version neutral.
         * @see com.wxiwei.office.fc.hssf.formula.FormulaParser.validateNumArgs
         */
        private const val FUNCTION_MAX_PARAMS: Short = 30
    }
}
