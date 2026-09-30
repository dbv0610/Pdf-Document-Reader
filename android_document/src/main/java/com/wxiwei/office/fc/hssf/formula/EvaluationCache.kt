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

import com.wxiwei.office.fc.hssf.formula.FormulaCellCache.IEntryOperation
import com.wxiwei.office.fc.hssf.formula.FormulaUsedBlankCellSet.BookSheetKey
import com.wxiwei.office.fc.hssf.formula.PlainCellCache.Loc
import com.wxiwei.office.fc.hssf.formula.eval.BlankEval
import com.wxiwei.office.fc.hssf.formula.eval.BoolEval
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.NumberEval
import com.wxiwei.office.fc.hssf.formula.eval.StringEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.fc.ss.usermodel.ICell


/**
 * Performance optimisation for [com.wxiwei.office.fc.ss.usermodel.com.wxiwei.office.fc.ss.usermodel.FormulaEvaluator].
 * This class stores previously calculated values of already visited cells,
 * to avoid unnecessary re-calculation when the same cells are referenced multiple times
 * 
 * @author Josh Micich
 */
internal class EvaluationCache(
    /** only used for testing. `null` otherwise  */
    val _evaluationListener: IEvaluationListener?
) {
    private val _plainCellCache: PlainCellCache
    private val _formulaCellCache: FormulaCellCache

    /* package */
    init {
        _plainCellCache = PlainCellCache()
        _formulaCellCache = FormulaCellCache()
    }

    fun notifyUpdateCell(bookIndex: Int, sheetIndex: Int, cell: EvaluationCell) {
        var fcce = _formulaCellCache.get(cell)

        val rowIndex = cell.rowIndex
        val columnIndex = cell.columnIndex
        val loc = Loc(bookIndex, sheetIndex, rowIndex, columnIndex)
        var pcce = _plainCellCache.get(loc)

        if (cell.cellType == ICell.CELL_TYPE_FORMULA) {
            if (fcce == null) {
                fcce = FormulaCellCacheEntry()
                if (pcce == null) {
                    if (_evaluationListener != null) {
                        _evaluationListener.onChangeFromBlankValue(
                            sheetIndex, rowIndex,
                            columnIndex, cell, fcce
                        )
                    }
                    updateAnyBlankReferencingFormulas(
                        bookIndex, sheetIndex, rowIndex,
                        columnIndex
                    )
                }
                _formulaCellCache.put(cell, fcce)
            } else {
                fcce.recurseClearCachedFormulaResults(_evaluationListener)
                fcce.clearFormulaEntry()
            }
            if (pcce == null) {
                // was formula cell before - no change of type
            } else {
                // changing from plain cell to formula cell
                pcce.recurseClearCachedFormulaResults(_evaluationListener)
                _plainCellCache.remove(loc)
            }
        } else {
            val value: ValueEval? = WorkbookEvaluator.Companion.getValueFromNonFormulaCell(cell)
            if (pcce == null) {
                if (value !== BlankEval.instance) {
                    // only cache non-blank values in the plain cell cache
                    // (dependencies on blank cells are managed by
                    // FormulaCellCacheEntry._usedBlankCellGroup)
                    pcce = PlainValueCellCacheEntry(value)
                    if (fcce == null) {
                        if (_evaluationListener != null) {
                            _evaluationListener.onChangeFromBlankValue(
                                sheetIndex,
                                rowIndex,
                                columnIndex,
                                cell,
                                pcce
                            )
                        }
                        updateAnyBlankReferencingFormulas(
                            bookIndex, sheetIndex,
                            rowIndex, columnIndex
                        )
                    }
                    _plainCellCache.put(loc, pcce)
                }
            } else {
                if (pcce.updateValue(value)) {
                    pcce.recurseClearCachedFormulaResults(_evaluationListener)
                }
                if (value === BlankEval.instance) {
                    _plainCellCache.remove(loc)
                }
            }
            if (fcce == null) {
                // was plain cell before - no change of type
            } else {
                // was formula cell before - now a plain value
                _formulaCellCache.remove(cell)
                fcce.setSensitiveInputCells(null)
                fcce.recurseClearCachedFormulaResults(_evaluationListener)
            }
        }
    }

    private fun updateAnyBlankReferencingFormulas(
        bookIndex: Int, sheetIndex: Int,
        rowIndex: Int, columnIndex: Int
    ) {
        val bsk = BookSheetKey(bookIndex, sheetIndex)
        _formulaCellCache.applyOperation(object : IEntryOperation {
            override fun processEntry(entry: FormulaCellCacheEntry) {
                entry.notifyUpdatedBlankCell(bsk, rowIndex, columnIndex, _evaluationListener)
            }
        })
    }

    fun getPlainValueEntry(
        bookIndex: Int, sheetIndex: Int,
        rowIndex: Int, columnIndex: Int, value: ValueEval
    ): PlainValueCellCacheEntry {
        val loc = Loc(bookIndex, sheetIndex, rowIndex, columnIndex)
        var result = _plainCellCache.get(loc)
        if (result == null) {
            result = PlainValueCellCacheEntry(value)
            _plainCellCache.put(loc, result)
            if (_evaluationListener != null) {
                _evaluationListener.onReadPlainValue(sheetIndex, rowIndex, columnIndex, result)
            }
        } else {
            // TODO - if we are confident that this sanity check is not required, we can remove 'value' from plain value cache entry
            check(areValuesEqual(result.value, value)) { "value changed" }
            if (_evaluationListener != null) {
                _evaluationListener.onCacheHit(sheetIndex, rowIndex, columnIndex, value)
            }
        }
        return result
    }

    private fun areValuesEqual(a: ValueEval?, b: ValueEval): Boolean {
        if (a == null) {
            return false
        }
        val cls: Class<*> = a.javaClass
        if (cls != b.javaClass) {
            // value type is changing
            return false
        }
        if (a === BlankEval.instance) {
            return b === a
        }
        if (cls == NumberEval::class.java) {
            return (a as NumberEval).numberValue == (b as NumberEval).numberValue
        }
        if (cls == StringEval::class.java) {
            return (a as StringEval).stringValue == (b as StringEval).stringValue
        }
        if (cls == BoolEval::class.java) {
            return (a as BoolEval).booleanValue == (b as BoolEval).booleanValue
        }
        if (cls == ErrorEval::class.java) {
            return (a as ErrorEval).errorCode == (b as ErrorEval).errorCode
        }
        throw IllegalStateException("Unexpected value class (" + cls.getName() + ")")
    }

    fun getOrCreateFormulaCellEntry(cell: EvaluationCell): FormulaCellCacheEntry {
        var result = _formulaCellCache.get(cell)
        if (result == null) {
            result = FormulaCellCacheEntry()
            _formulaCellCache.put(cell, result)
        }
        return result
    }

    /**
     * Should be called whenever there are changes to input cells in the evaluated workbook.
     */
    fun clear() {
        if (_evaluationListener != null) {
            _evaluationListener.onClearWholeCache()
        }
        _plainCellCache.clear()
        _formulaCellCache.clear()
    }

    fun notifyDeleteCell(bookIndex: Int, sheetIndex: Int, cell: EvaluationCell) {
        if (cell.cellType == ICell.CELL_TYPE_FORMULA) {
            val fcce = _formulaCellCache.remove(cell)
            if (fcce == null) {
                // formula cell has not been evaluated yet
            } else {
                fcce.setSensitiveInputCells(null)
                fcce.recurseClearCachedFormulaResults(_evaluationListener)
            }
        } else {
            val loc = Loc(bookIndex, sheetIndex, cell.rowIndex, cell.columnIndex)
            val pcce = _plainCellCache.get(loc)

            if (pcce == null) {
                // cache entry doesn't exist. nothing to do
            } else {
                pcce.recurseClearCachedFormulaResults(_evaluationListener)
            }
        }
    }
}
