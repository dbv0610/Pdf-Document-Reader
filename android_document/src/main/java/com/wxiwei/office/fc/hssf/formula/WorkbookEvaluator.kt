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
import com.wxiwei.office.fc.hssf.formula.eval.BlankEval
import com.wxiwei.office.fc.hssf.formula.eval.BoolEval
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.EvaluationException
import com.wxiwei.office.fc.hssf.formula.eval.MissingArgEval
import com.wxiwei.office.fc.hssf.formula.eval.NameEval
import com.wxiwei.office.fc.hssf.formula.eval.NotImplementedException
import com.wxiwei.office.fc.hssf.formula.eval.NumberEval
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver.getSingleValue
import com.wxiwei.office.fc.hssf.formula.eval.RefEval
import com.wxiwei.office.fc.hssf.formula.eval.StringEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.fc.hssf.formula.function.Choose
import com.wxiwei.office.fc.hssf.formula.function.FreeRefFunction
import com.wxiwei.office.fc.hssf.formula.function.IfFunc
import com.wxiwei.office.fc.hssf.formula.ptg.Area3DPtg
import com.wxiwei.office.fc.hssf.formula.ptg.AreaErrPtg
import com.wxiwei.office.fc.hssf.formula.ptg.AreaPtg
import com.wxiwei.office.fc.hssf.formula.ptg.AttrPtg
import com.wxiwei.office.fc.hssf.formula.ptg.BoolPtg
import com.wxiwei.office.fc.hssf.formula.ptg.ControlPtg
import com.wxiwei.office.fc.hssf.formula.ptg.DeletedArea3DPtg
import com.wxiwei.office.fc.hssf.formula.ptg.DeletedRef3DPtg
import com.wxiwei.office.fc.hssf.formula.ptg.ErrPtg
import com.wxiwei.office.fc.hssf.formula.ptg.ExpPtg
import com.wxiwei.office.fc.hssf.formula.ptg.FuncVarPtg
import com.wxiwei.office.fc.hssf.formula.ptg.IntPtg
import com.wxiwei.office.fc.hssf.formula.ptg.MemAreaPtg
import com.wxiwei.office.fc.hssf.formula.ptg.MemErrPtg
import com.wxiwei.office.fc.hssf.formula.ptg.MemFuncPtg
import com.wxiwei.office.fc.hssf.formula.ptg.MissingArgPtg
import com.wxiwei.office.fc.hssf.formula.ptg.NamePtg
import com.wxiwei.office.fc.hssf.formula.ptg.NameXPtg
import com.wxiwei.office.fc.hssf.formula.ptg.NumberPtg
import com.wxiwei.office.fc.hssf.formula.ptg.OperationPtg
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.formula.ptg.Ref3DPtg
import com.wxiwei.office.fc.hssf.formula.ptg.RefErrorPtg
import com.wxiwei.office.fc.hssf.formula.ptg.RefPtg
import com.wxiwei.office.fc.hssf.formula.ptg.RefPtgBase
import com.wxiwei.office.fc.hssf.formula.ptg.StringPtg
import com.wxiwei.office.fc.hssf.formula.ptg.UnionPtg
import com.wxiwei.office.fc.hssf.formula.ptg.UnknownPtg
import com.wxiwei.office.fc.hssf.formula.udf.AggregatingUDFFinder
import com.wxiwei.office.fc.hssf.formula.udf.UDFFinder
import com.wxiwei.office.fc.hssf.usermodel.HSSFEvaluationWorkbook
import com.wxiwei.office.fc.ss.usermodel.ICell
import com.wxiwei.office.fc.ss.util.CellReference
import com.wxiwei.office.ss.model.XLSModel.ACell
import com.wxiwei.office.ss.model.baseModel.Cell
import java.util.IdentityHashMap

/**
 * Evaluates formula cells.
 *
 *
 * 
 * For performance reasons, this class keeps a cache of all previously calculated intermediate
 * cell values.  Be sure to call [.clearAllCachedResultValues] if any workbook cells are changed between
 * calls to evaluate~ methods on this class.<br></br>
 * 
 * For POI internal use only
 * 
 * @author Josh Micich
 */
class WorkbookEvaluator internal constructor(
    private val _workbook: EvaluationWorkbook?, /* package */
    internal val evaluationListener: IEvaluationListener?,
    stabilityClassifier: IStabilityClassifier?, udfFinder: UDFFinder?
) {
    private var _cache: EvaluationCache

    /** part of cache entry key (useful when evaluating multiple workbooks)  */
    private var _workbookIx: Int

    private val _sheetIndexesBySheet: MutableMap<EvaluationSheet?, Int?>
    private val _sheetIndexesByName: MutableMap<String?, Int?>

    /* package */
    var environment: CollaboratingWorkbooksEnvironment
        private set
    private val _stabilityClassifier: IStabilityClassifier?
    private val _udfFinder: AggregatingUDFFinder?
    private var tracker: EvaluationTracker? = null

    /**
     * @param udfFinder pass `null` for default (AnalysisToolPak only)
     */
    constructor(
        workbook: EvaluationWorkbook,
        stabilityClassifier: IStabilityClassifier?,
        udfFinder: UDFFinder?
    ) : this(workbook, null, stabilityClassifier, udfFinder)

    /* package */
    init {
        _cache = EvaluationCache(evaluationListener)
        _sheetIndexesBySheet = IdentityHashMap<EvaluationSheet?, Int?>()
        _sheetIndexesByName = IdentityHashMap<String?, Int?>()
        this.environment = CollaboratingWorkbooksEnvironment.Companion.EMPTY
        _workbookIx = 0
        _stabilityClassifier = stabilityClassifier

        val defaultToolkit =  // workbook can be null in unit tests
            if (_workbook == null) null else _workbook.uDFFinder as AggregatingUDFFinder?
        if (defaultToolkit != null && udfFinder != null) {
            defaultToolkit.add(udfFinder)
        }
        _udfFinder = defaultToolkit
    }

    /**
     * also for debug use. Used in toString methods
     */
    /* package */
    fun getSheetName(sheetIndex: Int): String? {
        return _workbook!!.getSheetName(sheetIndex)
    }

    /* package */
    fun getSheet(sheetIndex: Int): EvaluationSheet? {
        val iter = _sheetIndexesBySheet.keys.iterator()
        while (iter.hasNext()) {
            val sheet = iter.next()
            if (_sheetIndexesBySheet.get(sheet) == sheetIndex) {
                return sheet
            }
        }
        val sheet = _workbook!!.getSheet(sheetIndex)
        _sheetIndexesBySheet.put(sheet, sheetIndex)
        return sheet
    }

    val workbook: EvaluationWorkbook
        /* package */
        get() = _workbook!!

    /* package */
    fun getName(name: String?, sheetIndex: Int): EvaluationName? {
        val namePtg = _workbook!!.getName(name, sheetIndex)!!.createPtg()

        if (namePtg == null) {
            return null
        } else {
            return _workbook.getName(namePtg)
        }
    }

    /* package */
    internal fun attachToEnvironment(
        collaboratingWorkbooksEnvironment: CollaboratingWorkbooksEnvironment,
        cache: EvaluationCache,
        workbookIx: Int
    ) {
        this.environment = collaboratingWorkbooksEnvironment
        _cache = cache
        _workbookIx = workbookIx
    }

    /**
     * Discards the current workbook environment and attaches to the default 'empty' environment.
     * Also resets evaluation cache.
     */
    /* package */
    fun detachFromEnvironment() {
        this.environment = CollaboratingWorkbooksEnvironment.Companion.EMPTY
        _cache = EvaluationCache(this.evaluationListener)
        _workbookIx = 0
    }

    /**
     * @return the evaluator for another workbook which is part of the same [CollaboratingWorkbooksEnvironment]
     */
    /* package */
    @Throws(WorkbookNotFoundException::class)
    fun getOtherWorkbookEvaluator(workbookName: String?): WorkbookEvaluator {
        return environment.getWorkbookEvaluator(workbookName)
    }

    /**
     * Should be called whenever there are changes to input cells in the evaluated workbook.
     * Failure to call this method after changing cell values will cause incorrect behaviour
     * of the evaluate~ methods of this class
     */
    fun clearAllCachedResultValues() {
        _cache.clear()
        _sheetIndexesBySheet.clear()
    }

    /**
     * Should be called to tell the cell value cache that the specified (value or formula) cell
     * has changed.
     */
    fun notifyUpdateCell(cell: EvaluationCell) {
        val sheetIndex = getSheetIndex(cell.sheet)
        _cache.notifyUpdateCell(_workbookIx, sheetIndex, cell)
    }

    /**
     * Should be called to tell the cell value cache that the specified cell has just been
     * deleted.
     */
    fun notifyDeleteCell(cell: EvaluationCell) {
        val sheetIndex = getSheetIndex(cell.sheet)
        _cache.notifyDeleteCell(_workbookIx, sheetIndex, cell)
    }

    private fun getSheetIndex(sheet: EvaluationSheet?): Int {
        var result = _sheetIndexesBySheet.get(sheet)
        if (result == null) {
            val sheetIndex = _workbook!!.getSheetIndex(sheet)
            if (sheetIndex < 0) {
                throw RuntimeException("Specified sheet from a different book")
            }
            result = sheetIndex
            _sheetIndexesBySheet.put(sheet, result)
        }
        return result
    }


    fun evaluate(srcCell: EvaluationCell): ValueEval? {
        val sheetIndex = getSheetIndex(srcCell.sheet)
        if (tracker == null) {
            tracker = EvaluationTracker(_cache)
        }
        return evaluateAny(
            srcCell,
            sheetIndex,
            srcCell.rowIndex,
            srcCell.columnIndex,
            tracker!! /*new EvaluationTracker(_cache)*/
        )
    }

    /**
     * Case-insensitive.
     * @return -1 if sheet with specified name does not exist
     */
    /* package */
    fun getSheetIndex(sheetName: String?): Int {
        var result = _sheetIndexesByName.get(sheetName)
        if (result == null) {
            val sheetIndex = _workbook!!.getSheetIndex(sheetName)
            if (sheetIndex < 0) {
                return -1
            }
            result = sheetIndex
            _sheetIndexesByName.put(sheetName, result)
        }
        return result
    }

    /* package */
    fun getSheetIndexByExternIndex(externSheetIndex: Int): Int {
        return _workbook!!.convertFromExternSheetIndex(externSheetIndex)
    }


    /**
     * @return never `null`, never [BlankEval]
     */
    private fun evaluateAny(
        srcCell: EvaluationCell?, sheetIndex: Int,
        rowIndex: Int, columnIndex: Int, tracker: EvaluationTracker
    ): ValueEval? {
        // avoid tracking dependencies to cells that have constant definition

        val shouldCellDependencyBeRecorded = if (_stabilityClassifier == null)
            true
        else
            !_stabilityClassifier.isCellFinal(sheetIndex, rowIndex, columnIndex)
        if (srcCell == null || srcCell.cellType != ICell.CELL_TYPE_FORMULA) {
            val result: ValueEval = getValueFromNonFormulaCell(srcCell)
            if (shouldCellDependencyBeRecorded) {
                tracker.acceptPlainValueDependency(
                    _workbookIx,
                    sheetIndex,
                    rowIndex,
                    columnIndex,
                    result
                )
            }
            return result
        }

        val cce = _cache.getOrCreateFormulaCellEntry(srcCell)
        if (shouldCellDependencyBeRecorded || cce.isInputSensitive) {
            tracker.acceptFormulaDependency(cce)
        }
        val evalListener = this.evaluationListener
        var result: ValueEval?
        if (cce.value == null) {
            if (!tracker.startEvaluate(cce)) {
                return ErrorEval.CIRCULAR_REF_ERROR
            }
            val ec = OperationEvaluationContext(
                this,
                this.workbook,
                sheetIndex,
                rowIndex,
                columnIndex,
                tracker
            )

            try {
                val ptgs = _workbook!!.getFormulaTokens(srcCell)!!
                if (evalListener == null) {
                    result = evaluateFormula(ec, ptgs)
                    if (result == null) {
                        return null
                    }
                } else {
                    evalListener.onStartEvaluate(srcCell, cce)
                    result = evaluateFormula(ec, ptgs)
                    evalListener.onEndEvaluate(cce, result)
                }

                tracker.updateCacheResult(result)
            } catch (e: NotImplementedException) {
                throw addExceptionInfo(e, sheetIndex, rowIndex, columnIndex)
            } catch (e: Exception) {
                // Other models (XLSX live editing) store results themselves
                if (srcCell.identityKey !is ACell) {
                    // Let the caller keep the saved value instead of showing a made-up #N/A
                    throw RuntimeException(e)
                }
                val cell = srcCell.identityKey as ACell
                cell.setCellType(Cell.CELL_TYPE_ERROR.toInt(), false)
                cell.setCellErrorValue(ErrorEval.NA.errorCode.toByte())
                return null
            } finally {
                tracker.endEvaluate(cce)
            }
        } else {
            if (evalListener != null) {
                evalListener.onCacheHit(sheetIndex, rowIndex, columnIndex, cce.value)
            }
            return cce.value
        }
        if (isDebugLogEnabled) {
            val sheetName = getSheetName(sheetIndex)
            val cr = CellReference(rowIndex, columnIndex)
            logDebug("Evaluated " + sheetName + "!" + cr.formatAsString() + " to " + result.toString())
        }

        // Usually (result === cce.getValue())
        // But sometimes: (result==ErrorEval.CIRCULAR_REF_ERROR, cce.getValue()==null)
        // When circular references are detected, the cache entry is only updated for
        // the top evaluation frame
        if (srcCell.identityKey !is ACell) {
            return result
        }
        val cell = srcCell.identityKey as ACell
        if (result is NumberEval) {
            val ne = result
            cell.setCellType(Cell.CELL_TYPE_NUMERIC.toInt(), false)
            cell.setCellValue(ne.numberValue)
        } else if (result is BoolEval) {
            val be = result
            cell.setCellType(Cell.CELL_TYPE_BOOLEAN.toInt(), false)
            cell.setCellValue(be.booleanValue)
        } else if (result is StringEval) {
            val ne = result
            cell.setCellType(Cell.CELL_TYPE_STRING.toInt(), false)
            cell.setCellValue(ne.stringValue)
        } else if (result is ErrorEval) {
            cell.setCellType(Cell.CELL_TYPE_ERROR.toInt(), false)
            cell.setCellErrorValue(result.errorCode.toByte())
        }
        return result
    }

    /**
     * Adds the current cell reference to the exception for easier debugging.
     * Would be nice to get the formula text as well, but that seems to require
     * too much digging around and casting to get the FormulaRenderingWorkbook.
     */
    private fun addExceptionInfo(
        inner: NotImplementedException,
        sheetIndex: Int,
        rowIndex: Int,
        columnIndex: Int
    ): NotImplementedException {
        try {
            val sheetName = _workbook!!.getSheetName(sheetIndex)
            val cr = CellReference(sheetName, rowIndex, columnIndex, false, false)
            val msg = "Error evaluating cell " + cr.formatAsString()
            return NotImplementedException(msg, inner)
        } catch (e: Exception) {
            // avoid bombing out during exception handling
            e.printStackTrace()
            return inner // preserve original exception
        }
    }

    // visibility raised for testing
    /* package */
    fun evaluateFormula(ec: OperationEvaluationContext, ptgs: Array<out Ptg?>): ValueEval? {
        val stack: MutableList<ValueEval?> = ArrayList<ValueEval?>()

        var i = 0
        val iSize = ptgs.size
        while (i < iSize) {
            // since we don't know how to handle these yet :(
            var ptg = ptgs[i]!!
            if (ptg is AttrPtg) {
                var attrPtg = ptg
                if (attrPtg.isSum) {
                    // Excel prefers to encode 'SUM()' as a tAttr token, but this evaluator
                    // expects the equivalent function token
                    ptg = FuncVarPtg.SUM
                }
                if (attrPtg.isOptimizedChoose) {
                    val arg0 = stack.removeAt(stack.size - 1)
                    val jumpTable = attrPtg.jumpTable
                    var dist: Int
                    val nChoices = jumpTable!!.size
                    try {
                        val switchIndex =
                            Choose.evaluateFirstArg(arg0, ec.rowIndex, ec.columnIndex)
                        if (switchIndex < 1 || switchIndex > nChoices) {
                            stack.add(ErrorEval.VALUE_INVALID)
                            dist = attrPtg.chooseFuncOffset + 4 // +4 for tFuncFar(CHOOSE)
                        } else {
                            dist = jumpTable[switchIndex - 1]
                        }
                    } catch (e: EvaluationException) {
                        stack.add(e.errorEval)
                        dist = attrPtg.chooseFuncOffset + 4 // +4 for tFuncFar(CHOOSE)
                    }
                    // Encoded dist for tAttrChoose includes size of jump table, but
                    // countTokensToBeSkipped() does not (it counts whole tokens).
                    dist -= nChoices * 2 + 2 // subtract jump table size
                    i += countTokensToBeSkipped(ptgs, i, dist)
                    i++
                    continue
                }
                if (attrPtg.isOptimizedIf) {
                    val arg0 = stack.removeAt(stack.size - 1)
                    val evaluatedPredicate: Boolean
                    try {
                        evaluatedPredicate =
                            IfFunc.evaluateFirstArg(arg0, ec.rowIndex, ec.columnIndex)
                    } catch (e: EvaluationException) {
                        stack.add(e.errorEval)
                        var dist = attrPtg.data.toInt()
                        i += countTokensToBeSkipped(ptgs, i, dist)
                        attrPtg = ptgs[i] as AttrPtg
                        dist = attrPtg.data + 1
                        i += countTokensToBeSkipped(ptgs, i, dist)
                        i++
                        continue
                    }
                    if (evaluatedPredicate) {
                        // nothing to skip - true param folows
                    } else {
                        val dist = attrPtg.data.toInt()
                        i += countTokensToBeSkipped(ptgs, i, dist)
                        val nextPtg: Ptg? = ptgs[i + 1]
                        if (ptgs[i] is AttrPtg && nextPtg is FuncVarPtg) {
                            // this is an if statement without a false param (as opposed to MissingArgPtg as the false param)
                            i++
                            stack.add(BoolEval.FALSE)
                        }
                    }
                    i++
                    continue
                }
                if (attrPtg.isSkip) {
                    val dist = attrPtg.data + 1
                    i += countTokensToBeSkipped(ptgs, i, dist)
                    if (stack.get(stack.size - 1) === MissingArgEval.instance) {
                        stack.removeAt(stack.size - 1)
                        stack.add(BlankEval.instance)
                    }
                    i++
                    continue
                }
            }
            if (ptg is ControlPtg) {
                // skip Parentheses, Attr, etc
                i++
                continue
            }
            if (ptg is MemFuncPtg || ptg is MemAreaPtg) {
                // can ignore, rest of tokens for this expression are in OK RPN order
                i++
                continue
            }
            if (ptg is MemErrPtg) {
                i++
                continue
            }

            val opResult: ValueEval?
            if (ptg is OperationPtg) {
                val optg = ptg

                if (optg is UnionPtg) {
                    i++
                    continue
                }


                val numops = optg.numberOfOperands
                val ops = arrayOfNulls<ValueEval>(numops)

                // storing the ops in reverse order since they are popping
                for (j in numops - 1 downTo 0) {
                    val p = stack.removeAt(stack.size - 1)

                    ops[j] = p
                }
                //				logDebug("invoke " + operation + " (nAgs=" + numops + ")");
                opResult = OperationEvaluatorFactory.evaluate(optg, ops, ec)
            } else {
                opResult = getEvalForPtg(ptg, ec)
            }
            if (opResult == null) {
                return null
                //throw new RuntimeException("Evaluation result must not be null");
            }
            //			logDebug("push " + opResult);
            stack.add(opResult)
            i++
        }

        val value = stack.removeAt(stack.size - 1)

        check(stack.isEmpty()) { "evaluation stack not empty" }

        if (value is AreaEval || value is RefEval) {
            return value
        }

        return dereferenceResult(value, ec.rowIndex, ec.columnIndex)
    }

    /**
     * returns an appropriate Eval impl instance for the Ptg. The Ptg must be
     * one of: Area3DPtg, AreaPtg, ReferencePtg, Ref3DPtg, IntPtg, NumberPtg,
     * StringPtg, BoolPtg <br></br>special Note: OperationPtg subtypes cannot be
     * passed here!
     */
    private fun getEvalForPtg(ptg: Ptg, ec: OperationEvaluationContext): ValueEval? {
        //  consider converting all these (ptg instanceof XxxPtg) expressions to (ptg.getClass() == XxxPtg.class)

        if (ptg is NamePtg) {
            // named ranges, macro functions
            val namePtg = ptg
            val nameRecord = _workbook!!.getName(namePtg)!!
            if (nameRecord.isFunctionName) {
                return NameEval(nameRecord.nameText)
            }
            if (nameRecord.hasFormula()) {
                return evaluateNameFormula(nameRecord.nameDefinition!!, ec)
            }

            throw RuntimeException("Don't now how to evalate name '" + nameRecord.nameText + "'")
        }
        if (ptg is NameXPtg) {
            val nameXPtg = ptg
            val nameRecord = (_workbook as HSSFEvaluationWorkbook).getName(nameXPtg)
            if (nameRecord.isFunctionName) {
                return NameEval(nameRecord.nameText)
            }
            if (nameRecord.hasFormula()) {
                return evaluateNameFormula(nameRecord.nameDefinition!!, ec)
            }

            throw RuntimeException("Don't now how to evalate name '" + nameRecord.nameText + "'")


            //return ec.getNameXEval(((NameXPtg) ptg));
        }

        if (ptg is IntPtg) {
            return NumberEval(ptg.value.toDouble())
        }
        if (ptg is NumberPtg) {
            return NumberEval(ptg.value)
        }
        if (ptg is StringPtg) {
            return StringEval(ptg.value)
        }
        if (ptg is BoolPtg) {
            return BoolEval.valueOf(ptg.value)
        }
        if (ptg is ErrPtg) {
            return ErrorEval.valueOf(ptg.errorCode)
        }
        if (ptg is MissingArgPtg) {
            return MissingArgEval.instance
        }
        if (ptg is AreaErrPtg || ptg is RefErrorPtg
            || ptg is DeletedArea3DPtg || ptg is DeletedRef3DPtg
        ) {
            return ErrorEval.REF_INVALID
        }
        if (ptg is Ref3DPtg) {
            val rptg = ptg
            return ec.getRef3DEval(
                rptg.row,
                rptg.column,
                rptg.externSheetIndex
            )
        }
        if (ptg is Area3DPtg) {
            val aptg = ptg
            return ec.getArea3DEval(
                aptg.firstRow,
                aptg.firstColumn,
                aptg.lastRow,
                aptg.lastColumn,
                aptg.externSheetIndex
            )
        }
        if (ptg is RefPtg) {
            val rptg = ptg
            return ec.getRefEval(rptg.row, rptg.column)
        }
        if (ptg is AreaPtg) {
            val aptg = ptg
            return ec.getAreaEval(
                aptg.firstRow,
                aptg.firstColumn,
                aptg.lastRow,
                aptg.lastColumn
            )
        }

        if (ptg is UnknownPtg) {
            // POI uses UnknownPtg when the encoded Ptg array seems to be corrupted.
            // This seems to occur in very rare cases (e.g. unused name formulas in bug 44774, attachment 21790)
            // In any case, formulas are re-parsed before execution, so UnknownPtg should not get here
            throw RuntimeException("UnknownPtg not allowed")
        }
        if (ptg is ExpPtg) {
            // ExpPtg is used for array formulas and shared formulas.
            // it is currently unsupported, and may not even get implemented here
            throw RuntimeException("ExpPtg currently not supported")
        }

        throw RuntimeException("Unexpected ptg class (" + ptg.javaClass.getName() + ")")
    }

    /**
     * YK: Used by OperationEvaluationContext to resolve indirect names.
     */
    /*package*/
    fun evaluateNameFormula(ptgs: Array<out Ptg?>, ec: OperationEvaluationContext): ValueEval? {
//		if (ptgs.length > 1) {
//			throw new RuntimeException("Complex name formulas not supported yet");
//		}
//		return getEvalForPtg(ptgs[0], ec);
        if (ptgs.size == 1) {
            return getEvalForPtg(ptgs[0]!!, ec)
        } else {
            return evaluateFormula(ec, ptgs)
        }
    }

    /**
     * Used by the lazy ref evals whenever they need to get the value of a contained cell.
     */
    /* package */
    internal fun evaluateReference(
        sheet: EvaluationSheet, sheetIndex: Int, rowIndex: Int,
        columnIndex: Int, tracker: EvaluationTracker
    ): ValueEval? {
        val cell = sheet.getCell(rowIndex, columnIndex)
        return evaluateAny(cell, sheetIndex, rowIndex, columnIndex, tracker)
    }

    fun findUserDefinedFunction(functionName: String?): FreeRefFunction? {
        return _udfFinder!!.findFunction(functionName)
    }

    companion object {
        private val isDebugLogEnabled: Boolean
            get() = false

        private fun logDebug(s: String?) {
            if (isDebugLogEnabled) {
                println(s)
            }
        }

        /**
         * Gets the value from a non-formula cell.
         * @param cell may be `null`
         * @return [BlankEval] if cell is `null` or blank, never `null`
         */
        /* package */
        fun getValueFromNonFormulaCell(cell: EvaluationCell?): ValueEval {
            if (cell == null) {
                return BlankEval.instance
            }
            val cellType = cell.cellType
            when (cellType) {
                ICell.CELL_TYPE_NUMERIC -> return NumberEval(cell.numericCellValue)
                ICell.CELL_TYPE_STRING -> return StringEval(cell.stringCellValue!!)
                ICell.CELL_TYPE_BOOLEAN -> return BoolEval.valueOf(cell.booleanCellValue)!!
                ICell.CELL_TYPE_BLANK -> return BlankEval.instance
                ICell.CELL_TYPE_ERROR -> return ErrorEval.valueOf(cell.errorCellValue)
            }
            throw RuntimeException("Unexpected cell type (" + cellType + ")")
        }


        /**
         * Calculates the number of tokens that the evaluator should skip upon reaching a tAttrSkip.
         * 
         * @return the number of tokens (starting from <tt>startIndex+1</tt>) that need to be skipped
         * to achieve the specified <tt>distInBytes</tt> skip distance.
         */
        private fun countTokensToBeSkipped(
            ptgs: Array<out Ptg?>,
            startIndex: Int,
            distInBytes: Int
        ): Int {
            var remBytes = distInBytes
            var index = startIndex
            while (remBytes != 0) {
                index++
                remBytes -= ptgs[index]!!.size
                if (remBytes < 0) {
                    throw RuntimeException("Bad skip distance (wrong token size calculation).")
                }
                if (index >= ptgs.size) {
                    throw RuntimeException("Skip distance too far (ran out of formula tokens).")
                }
            }
            return index - startIndex
        }

        /**
         * Dereferences a single value from any AreaEval or RefEval evaluation
         * result. If the supplied evaluationResult is just a plain value, it is
         * returned as-is.
         * 
         * @return a [NumberEval], [StringEval], [BoolEval], or
         * [ErrorEval]. Never `null`. [BlankEval] is
         * converted to [NumberEval.ZERO]
         */
        fun dereferenceResult(
            evaluationResult: ValueEval?,
            srcRowNum: Int,
            srcColNum: Int
        ): ValueEval? {
            val value: ValueEval?
            try {
                value = getSingleValue(evaluationResult, srcRowNum, srcColNum)
            } catch (e: EvaluationException) {
                return e.errorEval
            }
            if (value === BlankEval.instance) {
                // Note Excel behaviour here. A blank final final value is converted to zero.
                return NumberEval.ZERO
                // Formulas _never_ evaluate to blank.  If a formula appears to have evaluated to
                // blank, the actual value is empty string. This can be verified with ISBLANK().
            }
            return value
        }
    }
}
