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
 * @author aviks
 */
class NameXPtg private constructor(
    /** index to REF entry in externsheet record  */
    val sheetRefIndex: Int,
    /** index to defined name or externname table(1 based)  */
    private val _nameNumber: Int,
    /** reserved must be 0  */
    private val _reserved: Int
) : OperandPtg(), WorkbookDependentFormula {
    /**
     * @param sheetRefIndex index to REF entry in externsheet record
     * @param nameIndex index to defined name or externname table
     */
    constructor(sheetRefIndex: Int, nameIndex: Int) : this(sheetRefIndex, nameIndex + 1, 0)

    constructor(`in`: LittleEndianInput) : this(
        `in`.readUShort(),
        `in`.readUShort(),
        `in`.readUShort()
    )

    override fun write(out: LittleEndianOutput) {
        out.writeByte(sid + ptgClass)
        out.writeShort(this.sheetRefIndex)
        out.writeShort(_nameNumber)
        out.writeShort(_reserved)
    }

    override val size: Int get() {
        return SIZE
    }

    override fun toFormulaString(book: FormulaRenderingWorkbook): String? {
        // -1 to convert definedNameIndex from 1-based to zero-based
        return book.resolveNameXText(this)
    }

    override fun toFormulaString(): String? {
        throw RuntimeException("3D references need a workbook to determine formula text")
    }

    override fun toString(): String {
        val retValue = "NameXPtg:[sheetRefIndex:" + this.sheetRefIndex +
                " , nameNumber:" + _nameNumber + "]"
        return retValue
    }

    override val defaultOperandClass: Byte get() {
        return Ptg.Companion.CLASS_VALUE
    }

    val nameIndex: Int
        get() = _nameNumber - 1

    companion object {
        const val sid: Short = 0x39
        private const val SIZE = 7
    }
}
