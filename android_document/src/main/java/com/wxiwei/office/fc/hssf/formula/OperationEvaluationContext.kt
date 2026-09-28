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
package com.wxiwei.office.fc.hssf.formula

import com.wxiwei.office.fc.hssf.formula.CollaboratingWorkbooksEnvironment.WorkbookNotFoundException
import com.wxiwei.office.fc.hssf.formula.eval.AreaEval
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.NameXEval
import com.wxiwei.office.fc.hssf.formula.eval.RefEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.fc.hssf.formula.function.FreeRefFunction
import com.wxiwei.office.fc.hssf.formula.ptg.Area3DPtg
import com.wxiwei.office.fc.hssf.formula.ptg.NameXPtg
import com.wxiwei.office.fc.hssf.formula.ptg.Ref3DPtg
import com.wxiwei.office.fc.hssf.formula.ptg.RefPtgBase
import com.wxiwei.office.fc.ss.SpreadsheetVersion
import com.wxiwei.office.fc.ss.util.CellReference

/**
 * Contains all the contextual information required to evaluate an operation
 * within a formula
 * 
 * For POI internal use only
 * 
 * @author Josh Micich
 */
class OperationEvaluationContext internal constructor(
    private val _bookEvaluator: WorkbookEvaluator,
    val workbook: EvaluationWorkbook,
    private val _sheetIndex: Int,
    val rowIndex: Int,
    val columnIndex: Int,
    private val _tracker: EvaluationTracker
) {
    private var _sre: SheetRefEvaluator?

    init {
        _sre = SheetRefEvaluator(_bookEvaluator, _tracker, _sheetIndex)
    }

    internal fun createExternSheetRefEvaluator(ptg: ExternSheetReferenceToken): SheetRefEvaluator {
        return createExternSheetRefEvaluator(ptg.externSheetIndex)
    }

    internal fun createExternSheetRefEvaluator(externSheetIndex: Int): SheetRefEvaluator {
        val externalSheet = workbook.getExternalSheet(externSheetIndex)
        val targetEvaluator: WorkbookEvaluator?
        val otherSheetIndex: Int
        if (externalSheet == null) {
            // sheet is in same workbook
            otherSheetIndex = workbook.convertFromExternSheetIndex(externSheetIndex)
            targetEvaluator = _bookEvaluator
        } else {
            // look up sheet by name from external workbook
            val workbookName = externalSheet.workbookName
            try {
                targetEvaluator = _bookEvaluator.getOtherWorkbookEvaluator(workbookName)
            } catch (e: WorkbookNotFoundException) {
                throw RuntimeException(e.message)
            }
            otherSheetIndex = targetEvaluator.getSheetIndex(externalSheet.sheetName)
            if (otherSheetIndex < 0) {
                throw RuntimeException(
                    ("Invalid sheet name '" + externalSheet.sheetName
                            + "' in bool '" + workbookName + "'.")
                )
            }
        }
        return SheetRefEvaluator(targetEvaluator, _tracker, otherSheetIndex)
    }

    /**
     * @return `null` if either workbook or sheet is not found
     */
    private fun createExternSheetRefEvaluator(
        workbookName: String?,
        sheetName: String?
    ): SheetRefEvaluator? {
        val targetEvaluator: WorkbookEvaluator
        if (workbookName == null) {
            targetEvaluator = _bookEvaluator
        } else {
            requireNotNull(sheetName) { "sheetName must not be null if workbookName is provided" }
            try {
                targetEvaluator = _bookEvaluator.getOtherWorkbookEvaluator(workbookName)
            } catch (e: WorkbookNotFoundException) {
                return null
            }
        }
        val otherSheetIndex =
            if (sheetName == null) _sheetIndex else targetEvaluator.getSheetIndex(sheetName)
        if (otherSheetIndex < 0) {
            return null
        }
        return SheetRefEvaluator(targetEvaluator, _tracker, otherSheetIndex)
    }

    internal val refEvaluatorForCurrentSheet: SheetRefEvaluator
        get() {
            if (_sre == null) {
                _sre = SheetRefEvaluator(_bookEvaluator, _tracker, _sheetIndex)
            }
            return _sre!!
        }


    /**
     * Resolves a cell or area reference dynamically.
     * @param workbookName the name of the workbook containing the reference.  If `null`
     * the current workbook is assumed.  Note - to evaluate formulas which use multiple workbooks,
     * a [CollaboratingWorkbooksEnvironment] must be set up.
     * @param sheetName the name of the sheet containing the reference.  May be `null`
     * (when <tt>workbookName</tt> is also null) in which case the current workbook and sheet is
     * assumed.
     * @param refStrPart1 the single cell reference or first part of the area reference.  Must not
     * be `null`.
     * @param refStrPart2 the second part of the area reference. For single cell references this
     * parameter must be `null`
     * @param isA1Style specifies the format for <tt>refStrPart1</tt> and <tt>refStrPart2</tt>.
     * Pass `true` for 'A1' style and `false` for 'R1C1' style.
     * TODO - currently POI only supports 'A1' reference style
     * @return a [RefEval] or [AreaEval]
     */
    fun getDynamicReference(
        workbookName: String?, sheetName: String?, refStrPart1: String,
        refStrPart2: String?, isA1Style: Boolean
    ): ValueEval? {
        if (!isA1Style) {
            throw RuntimeException("R1C1 style not supported yet")
        }
        val sre = createExternSheetRefEvaluator(workbookName, sheetName)
        if (sre == null) {
            return ErrorEval.REF_INVALID
        }
        // ugly typecast - TODO - make spreadsheet version more easily accessible
        val ssVersion = (this.workbook as FormulaParsingWorkbook).spreadsheetVersion

        val part1refType: CellReference.NameType = classifyCellReference(refStrPart1, ssVersion)
        when (part1refType) {
            CellReference.NameType.BAD_CELL_OR_NAMED_RANGE -> return ErrorEval.REF_INVALID
            CellReference.NameType.NAMED_RANGE -> {
                val nm = (this.workbook as FormulaParsingWorkbook).getName(refStrPart1, _sheetIndex)!!
                if (!nm.isRange) {
                    throw RuntimeException("Specified name '" + refStrPart1 + "' is not a range as expected.")
                }
                return _bookEvaluator.evaluateNameFormula(nm.nameDefinition!!, this)
            }
            else -> {}
        }
        if (refStrPart2 == null) {
            // no ':'
            when (part1refType) {
                CellReference.NameType.COLUMN, CellReference.NameType.ROW -> return ErrorEval.REF_INVALID
                CellReference.NameType.CELL -> {
                    val cr = CellReference(refStrPart1)
                    return LazyRefEval(cr.getRow(), cr.getCol().toInt(), sre)
                }
                else -> {}
            }
            throw IllegalStateException("Unexpected reference classification of '" + refStrPart1 + "'.")
        }
        val part2refType: CellReference.NameType = classifyCellReference(refStrPart1, ssVersion)
        when (part2refType) {
            CellReference.NameType.BAD_CELL_OR_NAMED_RANGE -> return ErrorEval.REF_INVALID
            CellReference.NameType.NAMED_RANGE -> throw RuntimeException(
                ("Cannot evaluate '" + refStrPart1
                        + "'. Indirect evaluation of defined names not supported yet")
            )
            else -> {}
        }

        if (part2refType != part1refType) {
            // LHS and RHS of ':' must be compatible
            return ErrorEval.REF_INVALID
        }
        val firstRow: Int
        val firstCol: Int
        val lastRow: Int
        val lastCol: Int
        when (part1refType) {
            CellReference.NameType.COLUMN -> {
                firstRow = 0
                lastRow = ssVersion.getLastRowIndex()
                firstCol = parseColRef(refStrPart1)
                lastCol = parseColRef(refStrPart2)
            }

            CellReference.NameType.ROW -> {
                firstCol = 0
                lastCol = ssVersion.getLastColumnIndex()
                firstRow = parseRowRef(refStrPart1)
                lastRow = parseRowRef(refStrPart2)
            }

            CellReference.NameType.CELL -> {
                var cr: CellReference
                cr = CellReference(refStrPart1)
                firstRow = cr.getRow()
                firstCol = cr.getCol().toInt()
                cr = CellReference(refStrPart2)
                lastRow = cr.getRow()
                lastCol = cr.getCol().toInt()
            }

            else -> throw IllegalStateException("Unexpected reference classification of '" + refStrPart1 + "'.")
        }
        return LazyAreaEval(firstRow, firstCol, lastRow, lastCol, sre)
    }

    fun findUserDefinedFunction(functionName: String?): FreeRefFunction? {
        return _bookEvaluator.findUserDefinedFunction(functionName)
    }

    fun getRefEval(rowIndex: Int, columnIndex: Int): ValueEval {
        val sre = this.refEvaluatorForCurrentSheet
        return LazyRefEval(rowIndex, columnIndex, sre)
    }

    fun getRef3DEval(rowIndex: Int, columnIndex: Int, extSheetIndex: Int): ValueEval {
        val sre = createExternSheetRefEvaluator(extSheetIndex)
        return LazyRefEval(rowIndex, columnIndex, sre)
    }

    fun getAreaEval(
        firstRowIndex: Int, firstColumnIndex: Int,
        lastRowIndex: Int, lastColumnIndex: Int
    ): ValueEval {
        val sre = this.refEvaluatorForCurrentSheet
        return LazyAreaEval(firstRowIndex, firstColumnIndex, lastRowIndex, lastColumnIndex, sre)
    }

    fun getArea3DEval(
        firstRowIndex: Int, firstColumnIndex: Int,
        lastRowIndex: Int, lastColumnIndex: Int, extSheetIndex: Int
    ): ValueEval {
        val sre = createExternSheetRefEvaluator(extSheetIndex)
        return LazyAreaEval(firstRowIndex, firstColumnIndex, lastRowIndex, lastColumnIndex, sre)
    }

    fun getNameXEval(nameXPtg: NameXPtg): ValueEval {
        val externSheet = workbook.getExternalSheet(nameXPtg.sheetRefIndex)
        if (externSheet == null) return NameXEval(nameXPtg)
        val workbookName = externSheet.workbookName
        val externName = workbook.getExternalName(
            nameXPtg.sheetRefIndex,
            nameXPtg.nameIndex
        )!!
        try {
            val refWorkbookEvaluator = _bookEvaluator.getOtherWorkbookEvaluator(workbookName)
            val evaluationName =
                refWorkbookEvaluator.getName(externName.name, externName.ix - 1)
            if (evaluationName != null && evaluationName.hasFormula()) {
                val nameDefinition = evaluationName.nameDefinition!!
                if (nameDefinition.size > 1) {
                    throw RuntimeException("Complex name formulas not supported yet")
                }
                val ptg = nameDefinition[0]
                if (ptg is Ref3DPtg) {
                    val ref3D = ptg
                    val sheetIndex =
                        refWorkbookEvaluator.getSheetIndexByExternIndex(ref3D.externSheetIndex)
                    val sheetName = refWorkbookEvaluator.getSheetName(sheetIndex)
                    val sre = createExternSheetRefEvaluator(workbookName, sheetName)
                    return LazyRefEval(ref3D.row, ref3D.column, sre!!)
                } else if (ptg is Area3DPtg) {
                    val area3D = ptg
                    val sheetIndex =
                        refWorkbookEvaluator.getSheetIndexByExternIndex(area3D.externSheetIndex)
                    val sheetName = refWorkbookEvaluator.getSheetName(sheetIndex)
                    val sre = createExternSheetRefEvaluator(workbookName, sheetName)
                    return LazyAreaEval(
                        area3D.firstRow,
                        area3D.firstColumn,
                        area3D.lastRow,
                        area3D.lastColumn,
                        sre!!
                    )
                }
            }
            return ErrorEval.REF_INVALID
        } catch (wnfe: WorkbookNotFoundException) {
            return ErrorEval.REF_INVALID
        }
    }

    companion object {
        val UDF: FreeRefFunction = UserDefinedFunction.Companion.instance
        private fun parseRowRef(refStrPart: String): Int {
            return CellReference.convertColStringToIndex(refStrPart)
        }

        private fun parseColRef(refStrPart: String): Int {
            return refStrPart.toInt() - 1
        }

        private fun classifyCellReference(
            str: String,
            ssVersion: SpreadsheetVersion?
        ): CellReference.NameType {
            val len = str.length
            if (len < 1) {
                return CellReference.NameType.BAD_CELL_OR_NAMED_RANGE
            }
            return CellReference.classifyCellReference(str, ssVersion)
        }
    }
}
