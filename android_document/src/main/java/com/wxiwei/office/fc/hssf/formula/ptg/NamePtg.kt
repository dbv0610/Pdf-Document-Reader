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

import com.wxiwei.office.fc.hssf.formula.FormulaRenderingWorkbook
import com.wxiwei.office.fc.hssf.formula.WorkbookDependentFormula
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * 
 * @author andy
 * @author Jason Height (jheight at chariot dot net dot au)
 */
class NamePtg : OperandPtg, WorkbookDependentFormula {
    /** one-based index to defined name record  */
    private val field_1_label_index: Int
    private var field_2_zero: Short = 0 // reserved must be 0

    /**
     * @param nameIndex zero-based index to name within workbook
     */
    constructor(nameIndex: Int) {
        field_1_label_index = 1 + nameIndex // convert to 1-based
    }

    /** Creates new NamePtg  */
    constructor(`in`: LittleEndianInput) {
        field_1_label_index = `in`.readShort().toInt()
        field_2_zero = `in`.readShort()
    }

    val index: Int
        /**
         * @return zero based index to a defined name record in the LinkTable
         */
        get() = field_1_label_index - 1 // convert to zero based

    override fun write(out: LittleEndianOutput) {
        out.writeByte(sid + ptgClass)
        out.writeShort(field_1_label_index)
        out.writeShort(field_2_zero.toInt())
    }

    override val size: Int get() {
        return SIZE
    }

    override fun toFormulaString(book: FormulaRenderingWorkbook): String? {
        return book.getNameText(this)
    }

    override fun toFormulaString(): String? {
        throw RuntimeException("3D references need a workbook to determine formula text")
    }

    override val defaultOperandClass: Byte get() {
        return Ptg.Companion.CLASS_REF
    }

    companion object {
        const val sid: Short = 0x23
        private const val SIZE = 5
    }
}
