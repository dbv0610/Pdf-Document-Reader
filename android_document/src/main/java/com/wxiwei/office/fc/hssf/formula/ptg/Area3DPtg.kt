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

import com.wxiwei.office.fc.hssf.formula.ExternSheetReferenceToken
import com.wxiwei.office.fc.hssf.formula.FormulaRenderingWorkbook
import com.wxiwei.office.fc.hssf.formula.WorkbookDependentFormula
import com.wxiwei.office.fc.ss.util.AreaReference
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * Title:        Area 3D Ptg - 3D reference (Sheet + Area)<P>
 * Description:  Defined a area in Extern Sheet. </P><P>
 * REFERENCE:  </P><P>
 * @author Libin Roman (Vista Portal LDT. Developer)
 * @author avik
 * @author Jason Height (jheight at chariot dot net dot au)
</P> */
class Area3DPtg : AreaPtgBase, WorkbookDependentFormula, ExternSheetReferenceToken {
    private var field_1_index_extern_sheet = 0


    constructor(arearef: String?, externIdx: Int) : super(AreaReference(arearef)) {
        setExternSheetIndex(externIdx)
    }

    constructor(`in`: LittleEndianInput) {
        field_1_index_extern_sheet = `in`.readShort().toInt()
        readCoordinates(`in`)
    }

    constructor(
        firstRow: Int,
        lastRow: Int,
        firstColumn: Int,
        lastColumn: Int,
        firstRowRelative: Boolean,
        lastRowRelative: Boolean,
        firstColRelative: Boolean,
        lastColRelative: Boolean,
        externalSheetIndex: Int
    ) : super(
        firstRow,
        lastRow,
        firstColumn,
        lastColumn,
        firstRowRelative,
        lastRowRelative,
        firstColRelative,
        lastColRelative
    ) {
        setExternSheetIndex(externalSheetIndex)
    }

    constructor(arearef: AreaReference, externIdx: Int) : super(arearef) {
        setExternSheetIndex(externIdx)
    }

    override fun toString(): String {
        val sb = StringBuffer()
        sb.append(javaClass.getName())
        sb.append(" [")
        sb.append("sheetIx=").append(externSheetIndex)
        sb.append(" ! ")
        sb.append(formatReferenceAsString())
        sb.append("]")
        return sb.toString()
    }

    override fun write(out: LittleEndianOutput) {
        out.writeByte(sid + ptgClass)
        out.writeShort(field_1_index_extern_sheet)
        writeCoordinates(out)
    }

    override val size: Int get() {
        return SIZE
    }

    override val externSheetIndex: Int get() {
        return field_1_index_extern_sheet
    }

    fun setExternSheetIndex(index: Int) {
        field_1_index_extern_sheet = index
    }

    override fun format2DRefAsString(): String? {
        return formatReferenceAsString()
    }

    /**
     * @return text representation of this area reference that can be used in text
     * formulas. The sheet name will get properly delimited if required.
     */
    override fun toFormulaString(book: FormulaRenderingWorkbook): String {
        return ExternSheetNameResolver.prependSheetName(
            book,
            field_1_index_extern_sheet,
            formatReferenceAsString()
        )
    }

    override fun toFormulaString(): String? {
        throw RuntimeException("3D references need a workbook to determine formula text")
    }

    companion object {
        const val sid: Byte = 0x3b
        private const val SIZE = 11 // 10 + 1 for Ptg
    }
}
