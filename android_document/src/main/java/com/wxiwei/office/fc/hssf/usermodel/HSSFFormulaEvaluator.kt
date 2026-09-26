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

import com.wxiwei.office.fc.hssf.formula.CollaboratingWorkbooksEnvironment
import com.wxiwei.office.fc.hssf.formula.IStabilityClassifier
import com.wxiwei.office.fc.hssf.formula.WorkbookEvaluator
import com.wxiwei.office.fc.hssf.formula.eval.BoolEval
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.NumberEval
import com.wxiwei.office.fc.hssf.formula.eval.StringEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.fc.hssf.formula.udf.UDFFinder
import com.wxiwei.office.fc.ss.usermodel.CellValue
import com.wxiwei.office.fc.ss.usermodel.FormulaEvaluator
import com.wxiwei.office.fc.ss.usermodel.ICell
import com.wxiwei.office.fc.ss.usermodel.Workbook
import com.wxiwei.office.ss.model.XLSModel.ACell
import com.wxiwei.office.ss.model.XLSModel.ASheet
import com.wxiwei.office.ss.model.XLSModel.AWorkbook

/**
 * Evaluates formula cells.
 *
 *
 * 
 * For performance reasons, this class keeps a cache of all previously calculated intermediate
 * cell values.  Be sure to call [.clearAllCachedResultValues] if any workbook cells are changed between
 * calls to evaluate~ methods on this class.
 * 
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * @author Josh Micich
 */
class HSSFFormulaEvaluator private constructor(
    workbook: AWorkbook?, stabilityClassifier: IStabilityClassifier?,
    udfFinder: UDFFinder?
) : FormulaEvaluator {
    private val _bookEvaluator: WorkbookEvaluator
    private var _book: AWorkbook? = null

    @Deprecated("(Sep 2008) HSSFSheet parameter is ignored")
    constructor(sheet: ASheet, workbook: AWorkbook?) : this(workbook) {
        if (false) {
            sheet.toString() // suppress unused parameter compiler warning
        }
        this._book = workbook
    }

    constructor(workbook: AWorkbook?) : this(workbook, null) {
        this._book = workbook
    }

    /**
     * @param stabilityClassifier used to optimise caching performance. Pass `null`
     * for the (conservative) assumption that any cell may have its definition changed after
     * evaluation begins.
     */
    constructor(workbook: AWorkbook?, stabilityClassifier: IStabilityClassifier?) : this(
        workbook,
        stabilityClassifier,
        null
    )

    /**
     * Does nothing
     */
    @Deprecated("(Aug 2008) - not needed, since the current row can be derived from the cell")
    fun setCurrentRow(row: HSSFRow) {
        // do nothing
        if (false) {
            row.javaClass // suppress unused parameter compiler warning
        }
    }

    /**
     * Should be called whenever there are major changes (e.g. moving sheets) to input cells
     * in the evaluated workbook.  If performance is not critical, a single call to this method
     * may be used instead of many specific calls to the notify~ methods.
     * 
     * Failure to call this method after changing cell values will cause incorrect behaviour
     * of the evaluate~ methods of this class
     */
    override fun clearAllCachedResultValues() {
        _bookEvaluator.clearAllCachedResultValues()
    }

    /**
     * Should be called to tell the cell value cache that the specified (value or formula) cell
     * has changed.
     * Failure to call this method after changing cell values will cause incorrect behaviour
     * of the evaluate~ methods of this class
     */
    fun notifyUpdateCell(cell: ACell?) {
        if (cell != null) _bookEvaluator.notifyUpdateCell(HSSFEvaluationCell(cell))
    }

    override fun notifyUpdateCell(cell: ICell?) {
        if (cell != null) _bookEvaluator.notifyUpdateCell(HSSFEvaluationCell(cell as ACell))
    }

    /**
     * Should be called to tell the cell value cache that the specified cell has just been
     * deleted.
     * Failure to call this method after changing cell values will cause incorrect behaviour
     * of the evaluate~ methods of this class
     */
    fun notifyDeleteCell(cell: ACell?) {
        if (cell != null) _bookEvaluator.notifyDeleteCell(HSSFEvaluationCell(cell))
    }

    override fun notifyDeleteCell(cell: ICell?) {
        if (cell != null) _bookEvaluator.notifyDeleteCell(HSSFEvaluationCell(cell as ACell))
    }

    /**
     * Should be called to tell the cell value cache that the specified (value or formula) cell
     * has changed.
     * Failure to call this method after changing cell values will cause incorrect behaviour
     * of the evaluate~ methods of this class
     */
    override fun notifySetFormula(cell: ICell?) {
        if (cell != null) _bookEvaluator.notifyUpdateCell(HSSFEvaluationCell(cell as ACell))
    }

    /**
     * If cell contains a formula, the formula is evaluated and returned,
     * else the CellValue simply copies the appropriate cell value from
     * the cell and also its cell type. This method should be preferred over
     * evaluateInCell() when the call should not modify the contents of the
     * original cell.
     * 
     * @param cell may be `null` signifying that the cell is not present (or blank)
     * @return `null` if the supplied cell is `null` or blank
     */
    override fun evaluate(cell: ICell?): CellValue? {
        if (cell == null) {
            return null
        }

        when (cell.getCellType()) {
            ICell.CELL_TYPE_BOOLEAN -> return CellValue.valueOf(cell.getBooleanCellValue())
            ICell.CELL_TYPE_ERROR -> return CellValue.getError(cell.getErrorCellValue().toInt())
            ICell.CELL_TYPE_FORMULA -> return evaluateFormulaCellValue(cell)
            ICell.CELL_TYPE_NUMERIC -> return CellValue(cell.getNumericCellValue())
            ICell.CELL_TYPE_STRING -> return CellValue(cell.getRichStringCellValue().getString())
            ICell.CELL_TYPE_BLANK -> return null
        }
        throw IllegalStateException("Bad cell type (" + cell.getCellType() + ")")
    }

    /**
     * If cell contains formula, it evaluates the formula, and saves the result of the formula. The
     * cell remains as a formula cell. If the cell does not contain formula, this method returns -1
     * and leaves the cell unchanged.
     * 
     * Note that the type of the *formula result* is returned, so you know what kind of
     * cached formula result is also stored with  the formula.
     * <pre>
     * int evaluatedCellType = evaluator.evaluateFormulaCell(cell);
    </pre> * 
     * Be aware that your cell will hold both the formula, and the result. If you want the cell
     * replaced with the result of the formula, use [.evaluateInCell]
     * @param cell The cell to evaluate
     * @return -1 for non-formula cells, or the type of the *formula result*
     */
    override fun evaluateFormulaCell(cell: ICell?): Int {
        if (cell == null || cell.getCellType() != ICell.CELL_TYPE_FORMULA) {
            return -1
        }
        val cv = evaluateFormulaCellValue(cell)
        // cell remains a formula cell, but the cached value is changed
        setCellValue(cell, cv)
        return cv.getCellType()
    }

    /**
     * If cell contains formula, it evaluates the formula, and
     * puts the formula result back into the cell, in place
     * of the old formula.
     * Else if cell does not contain formula, this method leaves
     * the cell unchanged.
     * Note that the same instance of HSSFCell is returned to
     * allow chained calls like:
     * <pre>
     * int evaluatedCellType = evaluator.evaluateInCell(cell).getCellType();
    </pre> * 
     * Be aware that your cell value will be changed to hold the
     * result of the formula. If you simply want the formula
     * value computed for you, use [.evaluateFormulaCell]}
     */
    override fun evaluateInCell(cell: ICell?): HSSFCell? {
        if (cell == null) {
            return null
        }
        val result = cell as HSSFCell
        if (cell.getCellType() == ICell.CELL_TYPE_FORMULA) {
            val cv = evaluateFormulaCellValue(cell)
            setCellValue(cell, cv)
            setCellType(cell, cv) // cell will no longer be a formula cell
        }
        return result
    }

    /**
     * Loops over all cells in all sheets of the supplied
     * workbook.
     * For cells that contain formulas, their formulas are
     * evaluated, and the results are saved. These cells
     * remain as formula cells.
     * For cells that do not contain formulas, no changes
     * are made.
     * This is a helpful wrapper around looping over all
     * cells, and calling evaluateFormulaCell on each one.
     */
    override fun evaluateAll() {
        evaluateAllFormulaCells(_book, this)
    }

    /**
     * Returns a CellValue wrapper around the supplied ValueEval instance.
     * @param eval
     */
    fun evaluateFormulaCellValue(cell: ICell?): CellValue {
        val aCell = (cell as ACell?) ?: return CellValue.getError(0)
        if (hssfEvaluationCell != null) {
            hssfEvaluationCell!!.setHSSFCell(aCell)
        } else {
            hssfEvaluationCell = HSSFEvaluationCell(aCell)
        }

        _bookEvaluator.clearAllCachedResultValues()

        val eval = _bookEvaluator.evaluate(hssfEvaluationCell!!)
        if (eval is NumberEval) {
            val ne = eval
            return CellValue(ne.getNumberValue())
        }
        if (eval is BoolEval) {
            val be = eval
            return CellValue.valueOf(be.booleanValue)
        }
        if (eval is StringEval) {
            val ne = eval
            return CellValue(ne.getStringValue())
        }
        if (eval is ErrorEval) {
            return CellValue.getError(eval.errorCode)
        }
        if (eval == null) {
            return CellValue.getError(0)
        }
        throw RuntimeException("Unexpected eval class (" + eval.javaClass.getName() + ")")
    }

    private var hssfEvaluationCell: HSSFEvaluationCell? = null

    /**
     * @param udfFinder pass `null` for default (AnalysisToolPak only)
     */
    init {
        _bookEvaluator = WorkbookEvaluator(
            HSSFEvaluationWorkbook.create(workbook)!!,
            stabilityClassifier, udfFinder
        )
    }

    /**
     * 
     * @param cell
     * @return
     */
    fun evaluateFormulaValueEval(cell: ACell?): ValueEval? {
        if (cell == null) return null
        if (hssfEvaluationCell != null) {
            hssfEvaluationCell!!.setHSSFCell(cell)
        } else {
            hssfEvaluationCell = HSSFEvaluationCell(cell)
        }

        _bookEvaluator.clearAllCachedResultValues()

        return _bookEvaluator.evaluate(hssfEvaluationCell!!)
    }

    /**
     * 
     * @param cell
     * @return
     */
    fun evaluateFormulaValueEval(name: HSSFName?): ValueEval {
        return BoolEval.FALSE
    }

    companion object {
        /**
         * @param stabilityClassifier used to optimise caching performance. Pass `null`
         * for the (conservative) assumption that any cell may have its definition changed after
         * evaluation begins.
         * @param udfFinder pass `null` for default (AnalysisToolPak only)
         */
        fun create(
            workbook: AWorkbook?,
            stabilityClassifier: IStabilityClassifier?, udfFinder: UDFFinder?
        ): HSSFFormulaEvaluator {
            return HSSFFormulaEvaluator(workbook, stabilityClassifier, udfFinder)
        }

        /**
         * Coordinates several formula evaluators together so that formulas that involve external
         * references can be evaluated.
         * @param workbookNames the simple file names used to identify the workbooks in formulas
         * with external links (for example "MyData.xls" as used in a formula "[MyData.xls]Sheet1!A1")
         * @param evaluators all evaluators for the full set of workbooks required by the formulas.
         */
        fun setupEnvironment(
            workbookNames: Array<String?>?,
            evaluators: Array<HSSFFormulaEvaluator?>
        ) {
            val wbEvals = arrayOfNulls<WorkbookEvaluator>(evaluators.size)
            for (i in wbEvals.indices) {
                wbEvals[i] = evaluators[i]!!._bookEvaluator
            }
            CollaboratingWorkbooksEnvironment.setup(workbookNames!!, wbEvals)
        }

        private fun setCellType(cell: ICell, cv: CellValue) {
            val cellType = cv.getCellType()
            when (cellType) {
                ICell.CELL_TYPE_BOOLEAN, ICell.CELL_TYPE_ERROR, ICell.CELL_TYPE_NUMERIC, ICell.CELL_TYPE_STRING -> {
                    cell.setCellType(cellType)
                    return
                }

                ICell.CELL_TYPE_BLANK, ICell.CELL_TYPE_FORMULA -> {}
            }
            throw IllegalStateException("Unexpected cell value type (" + cellType + ")")
        }

        private fun setCellValue(cell: ICell, cv: CellValue) {
            val cellType = cv.getCellType()
            when (cellType) {
                ICell.CELL_TYPE_BOOLEAN -> cell.setCellValue(cv.getBooleanValue())
                ICell.CELL_TYPE_ERROR -> cell.setCellErrorValue(cv.getErrorValue())
                ICell.CELL_TYPE_NUMERIC -> cell.setCellValue(cv.getNumberValue())
                ICell.CELL_TYPE_STRING -> cell.setCellValue(HSSFRichTextString(cv.getStringValue()))
                ICell.CELL_TYPE_BLANK, ICell.CELL_TYPE_FORMULA -> throw IllegalStateException("Unexpected cell value type (" + cellType + ")")
                else -> throw IllegalStateException("Unexpected cell value type (" + cellType + ")")
            }
        }

        /**
         * Loops over all cells in all sheets of the supplied
         * workbook.
         * For cells that contain formulas, their formulas are
         * evaluated, and the results are saved. These cells
         * remain as formula cells.
         * For cells that do not contain formulas, no changes
         * are made.
         * This is a helpful wrapper around looping over all
         * cells, and calling evaluateFormulaCell on each one.
         */
        fun evaluateAllFormulaCells(wb: AWorkbook?) {
            evaluateAllFormulaCells(wb, HSSFFormulaEvaluator(wb))
        }

        /**
         * Loops over all cells in all sheets of the supplied
         * workbook.
         * For cells that contain formulas, their formulas are
         * evaluated, and the results are saved. These cells
         * remain as formula cells.
         * For cells that do not contain formulas, no changes
         * are made.
         * This is a helpful wrapper around looping over all
         * cells, and calling evaluateFormulaCell on each one.
         */
        fun evaluateAllFormulaCells(wb: Workbook?) {
//        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
//        evaluateAllFormulaCells(wb, evaluator);
        }

        private fun evaluateAllFormulaCells(wb: Workbook?, evaluator: FormulaEvaluator?) {
//        for (int i = 0; i < wb.getNumberOfSheets(); i++)
//        {
//            Sheet sheet = wb.getSheetAt(i);
//
//            for (IRow r : sheet)
//            {
//                for (ICell c : r)
//                {
//                    if (c.getCellType() == HSSFCell.CELL_TYPE_FORMULA)
//                    {
//                        evaluator.evaluateFormulaCell(c);
//                    }
//                }
//            }
//        }
        }
    }
}
