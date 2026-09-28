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
import com.wxiwei.office.fc.hssf.formula.SheetNameFormatter

/**
 * @author Josh Micich
 */
internal object ExternSheetNameResolver {
    fun prependSheetName(
        book: FormulaRenderingWorkbook,
        field_1_index_extern_sheet: Int,
        cellRefText: String?
    ): String {
        val externalSheet = book.getExternalSheet(field_1_index_extern_sheet)
        val sb: StringBuffer
        if (externalSheet != null) {
            val wbName = externalSheet.workbookName!!
            val sheetName = externalSheet.sheetName!!
            sb = StringBuffer(wbName.length + sheetName.length + (cellRefText?.length ?: 4) + 4)
            SheetNameFormatter.appendFormat(sb, wbName, sheetName)
        } else {
            val sheetName = book.getSheetNameByExternSheet(field_1_index_extern_sheet)!!
            sb = StringBuffer(sheetName.length + (cellRefText?.length ?: 4) + 4)
            if (sheetName.length < 1) {
                // What excel does if sheet has been deleted
                sb.append("#REF") // note - '!' added just once below
            } else {
                SheetNameFormatter.appendFormat(sb, sheetName)
            }
        }
        sb.append('!')
        sb.append(cellRefText)
        return sb.toString()
    }
}
