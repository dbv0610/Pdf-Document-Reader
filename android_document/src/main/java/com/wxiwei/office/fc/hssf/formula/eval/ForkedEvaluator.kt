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
package com.wxiwei.office.fc.hssf.formula.eval

import com.wxiwei.office.fc.hssf.formula.CollaboratingWorkbooksEnvironment
import com.wxiwei.office.fc.hssf.formula.EvaluationWorkbook
import com.wxiwei.office.fc.hssf.formula.IStabilityClassifier
import com.wxiwei.office.fc.hssf.formula.WorkbookEvaluator
import com.wxiwei.office.fc.hssf.formula.udf.UDFFinder
import com.wxiwei.office.fc.hssf.usermodel.HSSFEvaluationWorkbook
import com.wxiwei.office.fc.ss.usermodel.ICell
import com.wxiwei.office.fc.ss.usermodel.Workbook
import com.wxiwei.office.ss.model.XLSModel.AWorkbook


/**
 * An alternative workbook evaluator that saves memory in situations where a single workbook is
 * concurrently and independently evaluated many times.  With standard formula evaluation, around
 * 90% of memory consumption is due to loading of the [HSSFWorkbook] or [org.apache.poi.xssf.usermodel.XSSFWorkbook].
 * This class enables a 'master workbook' to be loaded just once and shared between many evaluation
 * clients.  Each evaluation client creates its own [ForkedEvaluator] and can set cell values
 * that will be used for local evaluations (and don't disturb evaluations on other evaluators).
 * 
 * @author Josh Micich
 */
class ForkedEvaluator private constructor(
    masterWorkbook: EvaluationWorkbook,
    stabilityClassifier: IStabilityClassifier?,
    udfFinder: UDFFinder?
) {
    private val _evaluator: WorkbookEvaluator
    private val _sewb: ForkedEvaluationWorkbook

    init {
        _sewb = ForkedEvaluationWorkbook(masterWorkbook)
        _evaluator = WorkbookEvaluator(_sewb, stabilityClassifier, udfFinder)
    }

    /**
     * Sets the specified cell to the supplied <tt>value</tt>
     * @param sheetName the name of the sheet containing the cell
     * @param rowIndex zero based
     * @param columnIndex zero based
     */
    fun updateCell(sheetName: String?, rowIndex: Int, columnIndex: Int, value: ValueEval) {
        val cell = _sewb.getOrCreateUpdatableCell(sheetName, rowIndex, columnIndex)
        cell.setValue(value)
        _evaluator.notifyUpdateCell(cell)
    }

    /**
     * Copies the values of all updated cells (modified by calls to [ ][.updateCell]) to the supplied <tt>workbook</tt>.<br></br>
     * Typically, the supplied <tt>workbook</tt> is a writable copy of the 'master workbook',
     * but at the very least it must contain sheets with the same names.
     */
    fun copyUpdatedCells(workbook: Workbook?) {
        _sewb.copyUpdatedCells(workbook)
    }

    /**
     * If cell contains a formula, the formula is evaluated and returned,
     * else the CellValue simply copies the appropriate cell value from
     * the cell and also its cell type. This method should be preferred over
     * evaluateInCell() when the call should not modify the contents of the
     * original cell.
     * 
     * @param sheetName the name of the sheet containing the cell
     * @param rowIndex zero based
     * @param columnIndex zero based
     * @return `null` if the supplied cell is `null` or blank
     */
    fun evaluate(sheetName: String?, rowIndex: Int, columnIndex: Int): ValueEval? {
        val cell = _sewb.getEvaluationCell(sheetName, rowIndex, columnIndex)!!

        when (cell.cellType) {
            ICell.CELL_TYPE_BOOLEAN -> return BoolEval.Companion.valueOf(cell.booleanCellValue)
            ICell.CELL_TYPE_ERROR -> return ErrorEval.Companion.valueOf(cell.errorCellValue)
            ICell.CELL_TYPE_FORMULA -> return _evaluator.evaluate(cell)
            ICell.CELL_TYPE_NUMERIC -> return NumberEval(cell.numericCellValue)
            ICell.CELL_TYPE_STRING -> return StringEval(cell.stringCellValue!!)
            ICell.CELL_TYPE_BLANK -> return null
        }
        throw IllegalStateException("Bad cell type (" + cell.cellType + ")")
    }

    companion object {
        private fun createEvaluationWorkbook(wb: Workbook): EvaluationWorkbook {
            if (wb is AWorkbook) {
                return HSSFEvaluationWorkbook.create(wb)!!
            }
            // TODO rearrange POI build to allow this
//		if (wb instanceof XSSFWorkbook) {
//			return XSSFEvaluationWorkbook.create((XSSFWorkbook) wb);
//		}
            throw IllegalArgumentException("Unexpected workbook type (" + wb.javaClass.getName() + ")")
        }

        @Deprecated("(Sep 2009) (reduce overloading) use {@link #create(Workbook, IStabilityClassifier, UDFFinder)}")
        fun create(wb: Workbook, stabilityClassifier: IStabilityClassifier?): ForkedEvaluator {
            return create(wb, stabilityClassifier, null)
        }

        /**
         * @param udfFinder pass `null` for default (AnalysisToolPak only)
         */
        fun create(
            wb: Workbook,
            stabilityClassifier: IStabilityClassifier?,
            udfFinder: UDFFinder?
        ): ForkedEvaluator {
            return ForkedEvaluator(createEvaluationWorkbook(wb), stabilityClassifier, udfFinder)
        }

        /**
         * Coordinates several formula evaluators together so that formulas that involve external
         * references can be evaluated.
         * @param workbookNames the simple file names used to identify the workbooks in formulas
         * with external links (for example "MyData.xls" as used in a formula "[MyData.xls]Sheet1!A1")
         * @param evaluators all evaluators for the full set of workbooks required by the formulas.
         */
        fun setupEnvironment(workbookNames: Array<String?>, evaluators: Array<ForkedEvaluator?>) {
            val wbEvals = arrayOfNulls<WorkbookEvaluator>(evaluators.size)
            for (i in wbEvals.indices) {
                wbEvals[i] = evaluators[i]!!._evaluator
            }
            CollaboratingWorkbooksEnvironment.setup(workbookNames, wbEvals)
        }
    }
}
