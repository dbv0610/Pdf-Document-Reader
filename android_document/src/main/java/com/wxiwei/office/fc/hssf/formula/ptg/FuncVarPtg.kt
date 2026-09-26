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

import com.wxiwei.office.fc.hssf.formula.function.FunctionMetadataRegistry.Companion.getFunctionByIndex
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * @author Jason Height (jheight at chariot dot net dot au)
 */
class FuncVarPtg private constructor(
    functionIndex: Int,
    returnClass: Int,
    paramClasses: ByteArray,
    numArgs: Int
) : AbstractFunctionPtg(functionIndex, returnClass, paramClasses, numArgs) {
    override fun write(out: LittleEndianOutput) {
        out.writeByte(sid + ptgClass)
        out.writeByte(numberOfOperands)
        out.writeShort(functionIndex.toInt())
    }

    override val size: Int get() {
        return SIZE
    }

    companion object {
        const val sid: Byte = 0x22
        private const val SIZE = 4

        /**
         * Single instance of this token for 'sum() taking a single argument'
         */
        @JvmField
        val SUM: OperationPtg = create("SUM", 1)

        /**Creates new function pointer from a byte array
         * usually called while reading an excel file.
         */
        fun create(`in`: LittleEndianInput): FuncVarPtg {
            return create(`in`.readByte().toInt(), `in`.readShort().toInt())
        }

        /**
         * Create a function ptg from a string tokenised by the parser
         */
        @JvmStatic
        fun create(pName: String, numArgs: Int): FuncVarPtg {
            return create(numArgs, AbstractFunctionPtg.Companion.lookupIndex(pName).toInt())
        }

        private fun create(numArgs: Int, functionIndex: Int): FuncVarPtg {
            val fm = getFunctionByIndex(functionIndex)
            if (fm == null) {
                // Happens only as a result of a call to FormulaParser.parse(), with a non-built-in function name
                return FuncVarPtg(
                    functionIndex,
                    Ptg.Companion.CLASS_VALUE.toInt(),
                    byteArrayOf(Ptg.Companion.CLASS_VALUE),
                    numArgs
                )
            }
            return FuncVarPtg(
                functionIndex,
                fm.returnClassCode.toInt(),
                fm.parameterClassCodes!!,
                numArgs
            )
        }
    }
}
