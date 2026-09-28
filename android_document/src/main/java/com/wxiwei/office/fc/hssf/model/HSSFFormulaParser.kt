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
package com.wxiwei.office.fc.hssf.model

import com.wxiwei.office.fc.hssf.formula.FormulaParseException
import com.wxiwei.office.fc.hssf.formula.FormulaParser
import com.wxiwei.office.fc.hssf.formula.FormulaParser.Companion.parse
import com.wxiwei.office.fc.hssf.formula.FormulaParsingWorkbook
import com.wxiwei.office.fc.hssf.formula.FormulaRenderer
import com.wxiwei.office.fc.hssf.formula.FormulaType
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.usermodel.HSSFEvaluationWorkbook
import com.wxiwei.office.ss.model.XLSModel.AWorkbook


/**
 * HSSF wrapper for the [FormulaParser] and [FormulaRenderer]
 * 
 * @author Josh Micich
 */
object HSSFFormulaParser {
    private fun createParsingWorkbook(book: AWorkbook?): FormulaParsingWorkbook? {
        return HSSFEvaluationWorkbook.create(book)
    }

    /**
     * @param formulaType a constant from [FormulaType]
     * @return the parsed formula tokens
     * @throws FormulaParseException if the formula has incorrect syntax or is otherwise invalid
     */
    /**
     * Convenience method for parsing cell formulas. see [.parse]
     */
    @JvmOverloads
    @Throws(FormulaParseException::class)
    fun parse(
        formula: String,
        workbook: AWorkbook?,
        formulaType: Int = FormulaType.CELL
    ): Array<Ptg?>? {
        return parse(formula, workbook, formulaType, -1)
    }

    /**
     * @param formula     the formula to parse
     * @param workbook    the parent workbook
     * @param formulaType a constant from [FormulaType]
     * @param sheetIndex  the 0-based index of the sheet this formula belongs to.
     * The sheet index is required to resolve sheet-level names. `-1` means that
     * the scope of the name will be ignored and  the parser will match named ranges only by name
     * 
     * @return the parsed formula tokens
     * @throws FormulaParseException if the formula has incorrect syntax or is otherwise invalid
     */
    @JvmStatic
    @Throws(FormulaParseException::class)
    fun parse(
        formula: String,
        workbook: AWorkbook?,
        formulaType: Int,
        sheetIndex: Int
    ): Array<Ptg?>? {
        return parse(formula, createParsingWorkbook(workbook), formulaType, sheetIndex)
    }

    /**
     * Static method to convert an array of [Ptg]s in RPN order
     * to a human readable string format in infix mode.
     * @param book  used for defined names and 3D references
     * @param ptgs  must not be `null`
     * @return a human readable String
     */
    fun toFormulaString(book: AWorkbook?, ptgs: Array<Ptg>): String? {
        return FormulaRenderer.toFormulaString(HSSFEvaluationWorkbook.create(book), ptgs)
    }
}
