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
package com.wxiwei.office.fc.hssf.usermodel

import com.wxiwei.office.fc.hssf.formula.EvaluationCell
import com.wxiwei.office.fc.hssf.formula.EvaluationName
import com.wxiwei.office.fc.hssf.formula.EvaluationSheet
import com.wxiwei.office.fc.hssf.formula.EvaluationWorkbook
import com.wxiwei.office.fc.hssf.formula.EvaluationWorkbook.ExternalName
import com.wxiwei.office.fc.hssf.formula.EvaluationWorkbook.ExternalSheet
import com.wxiwei.office.fc.hssf.formula.FormulaParsingWorkbook
import com.wxiwei.office.fc.hssf.formula.FormulaRenderingWorkbook
import com.wxiwei.office.fc.hssf.formula.ptg.NamePtg
import com.wxiwei.office.fc.hssf.formula.ptg.NameXPtg
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.formula.udf.UDFFinder
import com.wxiwei.office.fc.hssf.model.InternalWorkbook
import com.wxiwei.office.fc.hssf.record.NameRecord
import com.wxiwei.office.fc.hssf.record.aggregates.FormulaRecordAggregate
import com.wxiwei.office.fc.ss.SpreadsheetVersion
import com.wxiwei.office.ss.model.XLSModel.AWorkbook


/**
 * Internal POI use only
 * 
 * @author Josh Micich
 */
class HSSFEvaluationWorkbook private constructor(private val _uBook: AWorkbook) :
    FormulaRenderingWorkbook, EvaluationWorkbook, FormulaParsingWorkbook {
    private val _iBook: InternalWorkbook? = _uBook.getInternalWorkbook()

    override fun getExternalSheetIndex(sheetName: String?): Int {
        val sheetIndex = _uBook.getSheetIndex(sheetName)
        return _iBook!!.checkExternSheet(sheetIndex).toInt()
    }

    override fun getExternalSheetIndex(workbookName: String?, sheetName: String?): Int {
        return _iBook!!.getExternalSheetIndex(workbookName, sheetName)
    }

    override fun getNameXPtg(name: String?): NameXPtg? {
        return _iBook!!.getNameXPtg(name, _uBook.getUDFFinder()!!)
    }

    /**
     * Lookup a named range by its name.
     * 
     * @param name the name to search
     * @param sheetIndex  the 0-based index of the sheet this formula belongs to.
     * The sheet index is required to resolve sheet-level names. `-1` means workbook-global names
     */
    override fun getName(name: String?, sheetIndex: Int): EvaluationName? {
        if (name == null) return null
        for (i in 0..<_iBook!!.numNames) {
            val nr = _iBook.getNameRecord(i)
            if (nr.sheetNumber == sheetIndex + 1 && name.equals(
                    nr.getNameText(),
                    ignoreCase = true
                )
            ) {
                return Name(nr, i)
            }
        }
        return if (sheetIndex == -1) null else getName(name, -1)
    }

    override fun getSheetIndex(sheet: EvaluationSheet?): Int {
        if (sheet == null) return -1
        val sheet = (sheet as HSSFEvaluationSheet).getASheet()
        return _uBook.getSheetIndex(sheet)
    }

    override fun getSheetIndex(sheetName: String?): Int {
        return _uBook.getSheetIndex(sheetName)
    }

    override fun getSheetName(sheetIndex: Int): String? {
        return _uBook.getSheet(sheetIndex)!!.getSheetName()
    }

    override fun getSheet(sheetIndex: Int): EvaluationSheet {
        return HSSFEvaluationSheet(_uBook.getSheetAt(sheetIndex))
    }

    override fun convertFromExternSheetIndex(externSheetIndex: Int): Int {
        return _iBook!!.getSheetIndexFromExternSheetIndex(externSheetIndex)
    }

    override fun getExternalSheet(externSheetIndex: Int): ExternalSheet? {
        return _iBook!!.getExternalSheet(externSheetIndex)
    }

    override fun getExternalName(externSheetIndex: Int, externNameIndex: Int): ExternalName? {
        return _iBook!!.getExternalName(externSheetIndex, externNameIndex)
    }

    override fun resolveNameXText(n: NameXPtg?): String? {
        if (n == null) return null
        return _iBook!!.resolveNameXText(n.sheetRefIndex, n.nameIndex)
    }

    override fun getSheetNameByExternSheet(externSheetIndex: Int): String? {
        return _iBook!!.findSheetNameFromExternSheet(externSheetIndex)
    }

    override fun getNameText(namePtg: NamePtg?): String? {
        if (namePtg == null) return null
        return _iBook!!.getNameRecord(namePtg.index).getNameText()
    }

    override fun getName(namePtg: NamePtg?): EvaluationName? {
        if (namePtg == null) return null
        val ix = namePtg.index
        return Name(_iBook!!.getNameRecord(ix), ix)
    }

    fun getName(nameXPtg: NameXPtg): EvaluationName {
        val ix = nameXPtg.nameIndex
        return Name(_iBook!!.getNameRecord(ix), ix)
    }

    override fun getFormulaTokens(evalCell: EvaluationCell?): Array<Ptg?>? {
        if (evalCell == null) return null
        val cell = (evalCell as HSSFEvaluationCell).getACell()
        val fra = cell.getCellValueRecord() as FormulaRecordAggregate?
        return fra?.formulaTokens as Array<Ptg?>?
    }

    override val uDFFinder: UDFFinder?
        get() = _uBook.getUDFFinder()

    private class Name(private val _nameRecord: NameRecord, private val _index: Int) :
        EvaluationName {
        override val nameDefinition: Array<Ptg?>?
            get() = _nameRecord.getNameDefinition() as Array<Ptg?>?
        override val nameText: String?
            get() = _nameRecord.getNameText()

        override fun hasFormula(): Boolean {
            return _nameRecord.hasFormula()
        }

        override val isFunctionName: Boolean
            get() = _nameRecord.isFunctionName()
        override val isRange: Boolean
            get() = _nameRecord.hasFormula() // TODO - is this right?

        override fun createPtg(): NamePtg {
            return NamePtg(_index)
        }
    }

    override val spreadsheetVersion: SpreadsheetVersion
        get() = SpreadsheetVersion.EXCEL97

    companion object {
        fun create(book: AWorkbook?): HSSFEvaluationWorkbook? {
            if (book == null) {
                return null
            }
            return HSSFEvaluationWorkbook(book)
        }
    }
}
