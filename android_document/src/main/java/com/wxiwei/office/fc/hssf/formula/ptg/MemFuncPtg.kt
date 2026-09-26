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

import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * @author Glen Stampoultzis (glens at apache.org)
 */
class MemFuncPtg(val lenRefSubexpression: Int) : OperandPtg() {
    /**
     * Creates new function pointer from a byte array usually called while
     * reading an excel file.
     */
    constructor(`in`: LittleEndianInput) : this(`in`.readUShort())

    override val size: Int get() {
        return 3
    }

    override fun write(out: LittleEndianOutput) {
        out.writeByte(sid + ptgClass)
        out.writeShort(this.lenRefSubexpression)
    }

    override fun toFormulaString(): String {
        return ""
    }

    override val defaultOperandClass: Byte get() {
        return Ptg.Companion.CLASS_REF
    }

    override fun toString(): String {
        val sb = StringBuffer(64)
        sb.append(javaClass.getName()).append(" [len=")
        sb.append(this.lenRefSubexpression)
        sb.append("]")
        return sb.toString()
    }

    companion object {
        const val sid: Byte = 0x29
    }
}
