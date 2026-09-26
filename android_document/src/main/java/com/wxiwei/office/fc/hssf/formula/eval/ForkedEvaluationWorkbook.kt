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
package com.wxiwei.office.fc.hssf.formula.eval

import com.wxiwei.office.fc.hssf.formula.EvaluationCell
import com.wxiwei.office.fc.hssf.formula.EvaluationName
import com.wxiwei.office.fc.hssf.formula.EvaluationSheet
import com.wxiwei.office.fc.hssf.formula.EvaluationWorkbook
import com.wxiwei.office.fc.hssf.formula.EvaluationWorkbook.ExternalName
import com.wxiwei.office.fc.hssf.formula.EvaluationWorkbook.ExternalSheet
import com.wxiwei.office.fc.hssf.formula.ptg.NamePtg
import com.wxiwei.office.fc.hssf.formula.ptg.NameXPtg
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.formula.udf.UDFFinder
import com.wxiwei.office.fc.ss.usermodel.Workbook

/**
 * Represents a workbook being used for forked evaluation. Most operations are delegated to the
 * shared master workbook, except those that potentially involve cell values that may have been
 * updated after a call to [.getOrCreateUpdatableCell].
 * 
 * @author Josh Micich
 */
internal class ForkedEvaluationWorkbook(private val _masterBook: EvaluationWorkbook) :
    EvaluationWorkbook {
    private val _sharedSheetsByName: MutableMap<String?, ForkedEvaluationSheet?>

    init {
        _sharedSheetsByName = HashMap<String?, ForkedEvaluationSheet?>()
    }

    fun getOrCreateUpdatableCell(
        sheetName: String?, rowIndex: Int,
        columnIndex: Int
    ): ForkedEvaluationCell {
        val sheet = getSharedSheet(sheetName)
        return sheet.getOrCreateUpdatableCell(rowIndex, columnIndex)
    }

    fun getEvaluationCell(sheetName: String?, rowIndex: Int, columnIndex: Int): EvaluationCell? {
        val sheet = getSharedSheet(sheetName)
        return sheet.getCell(rowIndex, columnIndex)
    }

    private fun getSharedSheet(sheetName: String?): ForkedEvaluationSheet {
        var result = _sharedSheetsByName.get(sheetName)
        if (result == null) {
            result = ForkedEvaluationSheet(
                _masterBook.getSheet(
                    _masterBook
                        .getSheetIndex(sheetName)
                )!!
            )
            _sharedSheetsByName.put(sheetName, result)
        }
        return result
    }

    fun copyUpdatedCells(workbook: Workbook?) {
//		String[] sheetNames = new String[_sharedSheetsByName.size()];
//		_sharedSheetsByName.keySet().toArray(sheetNames);
//		OrderedSheet[] oss = new OrderedSheet[sheetNames.length];
//		for (int i = 0; i < sheetNames.length; i++) {
//			String sheetName = sheetNames[i];
//			oss[i] = new OrderedSheet(sheetName, _masterBook.getSheetIndex(sheetName));
//		}
//		for (int i = 0; i < oss.length; i++) {
//			String sheetName = oss[i].getSheetName();
//			ForkedEvaluationSheet sheet = _sharedSheetsByName.get(sheetName);
//			sheet.copyUpdatedCells(workbook.getSheet(sheetName));
//		}
    }

    override fun convertFromExternSheetIndex(externSheetIndex: Int): Int {
        return _masterBook.convertFromExternSheetIndex(externSheetIndex)
    }

    override fun getExternalSheet(externSheetIndex: Int): ExternalSheet? {
        return _masterBook.getExternalSheet(externSheetIndex)
    }

    override fun getFormulaTokens(cell: EvaluationCell?): Array<Ptg?>? {
        if (cell is ForkedEvaluationCell) {
            // doesn't happen yet because formulas cannot be modified from the master workbook
            throw RuntimeException("Updated formulas not supported yet")
        }
        return _masterBook.getFormulaTokens(cell)
    }

    override fun getName(namePtg: NamePtg?): EvaluationName? {
        return _masterBook.getName(namePtg)
    }

    override fun getName(name: String?, sheetIndex: Int): EvaluationName? {
        return _masterBook.getName(name, sheetIndex)
    }

    override fun getSheet(sheetIndex: Int): EvaluationSheet {
        return getSharedSheet(getSheetName(sheetIndex))
    }

    override fun getExternalName(externSheetIndex: Int, externNameIndex: Int): ExternalName? {
        return _masterBook.getExternalName(externSheetIndex, externNameIndex)
    }

    override fun getSheetIndex(sheet: EvaluationSheet?): Int {
        if (sheet is ForkedEvaluationSheet) {
            val mes = sheet
            return mes.getSheetIndex(_masterBook)
        }
        return _masterBook.getSheetIndex(sheet)
    }

    override fun getSheetIndex(sheetName: String?): Int {
        return _masterBook.getSheetIndex(sheetName)
    }

    override fun getSheetName(sheetIndex: Int): String? {
        return _masterBook.getSheetName(sheetIndex)
    }

    override fun resolveNameXText(ptg: NameXPtg?): String? {
        return _masterBook.resolveNameXText(ptg)
    }

    override val uDFFinder: UDFFinder?
        get() = _masterBook.uDFFinder

    private class OrderedSheet(val sheetName: String?, private val _index: Int) :
        Comparable<OrderedSheet> {
        override fun compareTo(o: OrderedSheet): Int {
            return _index - o._index
        }
    }
}
