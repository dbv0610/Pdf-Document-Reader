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

import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * While formula tokens are stored in RPN order and thus do not need parenthesis
 * for precedence reasons, Parenthesis tokens ARE written to ensure that user
 * entered parenthesis are displayed as-is on reading back
 * 
 * Avik Sengupta &lt;lists@aviksengupta.com&gt; Andrew C. Oliver (acoliver at
 * apache dot org)
 * 
 * @author Jason Height (jheight at chariot dot net dot au)
 */
class ParenthesisPtg private constructor() : ControlPtg() {
    override fun write(out: LittleEndianOutput) {
        out.writeByte(sid + ptgClass)
    }

    override val size: Int get() {
        return SIZE
    }

    override fun toFormulaString(): String {
        return "()"
    }

    fun toFormulaString(operands: Array<String>): String {
        return "(" + operands[0] + ")"
    }

    companion object {
        private const val SIZE = 1
        const val sid: Byte = 0x15

        @JvmField
        val instance: ControlPtg = ParenthesisPtg()
    }
}
