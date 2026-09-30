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
package com.wxiwei.office.fc.hssf.formula.ptg

import com.wxiwei.office.fc.hssf.formula.FormulaRenderingWorkbook
import com.wxiwei.office.fc.hssf.formula.WorkbookDependentFormula
import com.wxiwei.office.fc.ss.usermodel.ErrorConstants
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * Title:        Deleted Area 3D Ptg - 3D referecnce (Sheet + Area)<P>
 * Description:  Defined a area in Extern Sheet. </P><P>
 * REFERENCE:  </P><P>
 * @author Patrick Luby
 * @version 1.0-pre
</P> */
class DeletedArea3DPtg : OperandPtg, WorkbookDependentFormula {
    private val field_1_index_extern_sheet: Int
    private val unused1: Int
    private val unused2: Int

    constructor(externSheetIndex: Int) {
        field_1_index_extern_sheet = externSheetIndex
        unused1 = 0
        unused2 = 0
    }

    constructor(`in`: LittleEndianInput) {
        field_1_index_extern_sheet = `in`.readUShort()
        unused1 = `in`.readInt()
        unused2 = `in`.readInt()
    }

    override fun toFormulaString(book: FormulaRenderingWorkbook): String {
        return ExternSheetNameResolver.prependSheetName(
            book, field_1_index_extern_sheet,
            ErrorConstants.getText(ErrorConstants.ERROR_REF)
        )
    }

    override fun toFormulaString(): String? {
        throw RuntimeException("3D references need a workbook to determine formula text")
    }

    override val defaultOperandClass: Byte get() {
        return Ptg.Companion.CLASS_REF
    }

    override val size: Int get() {
        return 11
    }

    override fun write(out: LittleEndianOutput) {
        out.writeByte(sid + ptgClass)
        out.writeShort(field_1_index_extern_sheet)
        out.writeInt(unused1)
        out.writeInt(unused2)
    }

    companion object {
        const val sid: Byte = 0x3d
    }
}
