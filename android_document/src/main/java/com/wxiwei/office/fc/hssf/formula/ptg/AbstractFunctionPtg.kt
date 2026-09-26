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
package com.wxiwei.office.fc.hssf.formula.ptg

import com.wxiwei.office.fc.hssf.formula.function.FunctionMetadataRegistry
import com.wxiwei.office.fc.hssf.formula.function.FunctionMetadataRegistry.Companion.getFunctionByIndex
import com.wxiwei.office.fc.hssf.formula.function.FunctionMetadataRegistry.Companion.lookupIndexByName
import java.util.Locale

/**
 * This class provides the base functionality for Excel sheet functions
 * There are two kinds of function Ptgs - tFunc and tFuncVar
 * Therefore, this class will have ONLY two subclasses
 * @author  Avik Sengupta
 * @author Andrew C. Oliver (acoliver at apache dot org)
 */
abstract class AbstractFunctionPtg protected constructor(
    functionIndex: Int,
    pReturnClass: Int,
    private val paramClass: ByteArray,
    nParams: Int
) : OperationPtg() {
    private val returnClass: Byte

    private val _numberOfArgs: Byte
    val functionIndex: Short

    init {
        _numberOfArgs = nParams.toByte()
        this.functionIndex = functionIndex.toShort()
        returnClass = pReturnClass.toByte()
    }

    override val isBaseToken: Boolean get() {
        return false
    }

    override fun toString(): String {
        val sb = StringBuilder(64)
        sb.append(javaClass.getName()).append(" [")
        sb.append(lookupName(this.functionIndex))
        sb.append(" nArgs=").append(_numberOfArgs.toInt())
        sb.append("]")
        return sb.toString()
    }

    override val numberOfOperands: Int get() {
        return _numberOfArgs.toInt()
    }

    val name: String?
        get() = lookupName(this.functionIndex)
    val isExternalFunction: Boolean
        /**
         * external functions get some special processing
         * @return `true` if this is an external function
         */
        get() = this.functionIndex == FUNCTION_INDEX_EXTERNAL

    override fun toFormulaString(): String? {
        return this.name
    }

    override fun toFormulaString(operands: Array<String?>): String {
        val buf = StringBuilder()

        if (this.isExternalFunction) {
            buf.append(operands[0]) // first operand is actually the function name
            appendArgs(buf, 1, operands)
        } else {
            buf.append(this.name)
            appendArgs(buf, 0, operands)
        }
        return buf.toString()
    }

    abstract override val size: Int


    protected fun lookupName(index: Short): String? {
        if (index == FunctionMetadataRegistry.FUNCTION_INDEX_EXTERNAL) {
            return "#external#"
        }
        val fm = getFunctionByIndex(index.toInt())
        if (fm == null) {
            throw RuntimeException("bad function index (" + index + ")")
        }
        return fm.name
    }

    override val defaultOperandClass: Byte get() {
        return returnClass
    }

    fun getParameterClass(index: Int): Byte {
        if (index >= paramClass.size) {
            // For var-arg (and other?) functions, the metadata does not list all the parameter
            // operand classes.  In these cases, all extra parameters are assumed to have the
            // same operand class as the last one specified.
            return paramClass[paramClass.size - 1]
        }
        return paramClass[index]
    }

    companion object {
        /**
         * The name of the IF function (i.e. "IF").  Extracted as a constant for clarity.
         */
        const val FUNCTION_NAME_IF: String = "IF"

        /** All external functions have function index 255  */
        private const val FUNCTION_INDEX_EXTERNAL: Short = 255

        private fun appendArgs(buf: StringBuilder, firstArgIx: Int, operands: Array<String?>) {
            buf.append('(')
            for (i in firstArgIx..<operands.size) {
                if (i > firstArgIx) {
                    buf.append(',')
                }
                buf.append(operands[i])
            }
            buf.append(")")
        }

        /**
         * Used to detect whether a function name found in a formula is one of the standard excel functions
         * 
         * 
         * The name matching is case insensitive.
         * @return `true` if the name specifies a standard worksheet function,
         * `false` if the name should be assumed to be an external function.
         */
        @JvmStatic
        fun isBuiltInFunctionName(name: String): Boolean {
            val ix = lookupIndexByName(name.uppercase(Locale.getDefault()))
            return ix >= 0
        }

        /**
         * Resolves internal function names into function indexes.
         * 
         * 
         * The name matching is case insensitive.
         * @return the standard worksheet function index if found, otherwise <tt>FUNCTION_INDEX_EXTERNAL</tt>
         */
        @JvmStatic
        protected fun lookupIndex(name: String): Short {
            val ix = lookupIndexByName(name.uppercase(Locale.getDefault()))
            if (ix < 0) {
                return FUNCTION_INDEX_EXTERNAL
            }
            return ix
        }
    }
}
