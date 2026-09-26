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

/**
 * Percent PTG.
 * 
 * @author Daniel Noll (daniel at nuix.com.au)
 */
class PercentPtg private constructor() : ValueOperatorPtg() {
    override val sid: Byte get() {
        return Companion.sid
    }

    override val numberOfOperands: Int get() {
        return 1
    }

    override fun toFormulaString(operands: Array<String?>): String {
        val buffer = StringBuffer()

        buffer.append(operands[0])
        buffer.append(PERCENT)
        return buffer.toString()
    }

    companion object {
        const val SIZE: Int = 1
        const val sid: Byte = 0x14

        private const val PERCENT = "%"

        @JvmField
        val instance: ValueOperatorPtg = PercentPtg()
    }
}
