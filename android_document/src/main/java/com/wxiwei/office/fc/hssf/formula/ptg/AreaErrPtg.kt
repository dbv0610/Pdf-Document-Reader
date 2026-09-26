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

import com.wxiwei.office.fc.ss.usermodel.ErrorConstants
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * AreaErr - handles deleted cell area references.
 * 
 * @author Daniel Noll (daniel at nuix dot com dot au)
 */
class AreaErrPtg : OperandPtg {
    private val unused1: Int
    private val unused2: Int

    constructor() {
        unused1 = 0
        unused2 = 0
    }

    constructor(`in`: LittleEndianInput) {
        // 8 bytes unused:
        unused1 = `in`.readInt()
        unused2 = `in`.readInt()
    }

    override fun write(out: LittleEndianOutput) {
        out.writeByte(sid + ptgClass)
        out.writeInt(unused1)
        out.writeInt(unused2)
    }

    override fun toFormulaString(): String {
        return ErrorConstants.getText(ErrorConstants.ERROR_REF)
    }

    override val defaultOperandClass: Byte get() {
        return Ptg.Companion.CLASS_REF
    }

    override val size: Int get() {
        return 9
    }

    companion object {
        const val sid: Byte = 0x2B
    }
}

