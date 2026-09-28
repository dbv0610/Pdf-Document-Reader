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

import com.wxiwei.office.fc.hssf.formula.eval.BlankEval
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval

/**
 * Instances of this class keep track of multiple dependent cell evaluations due
 * to recursive calls to [WorkbookEvaluator.evaluate]}
 * The main purpose of this class is to detect an attempt to evaluate a cell
 * that is already being evaluated. In other words, it detects circular
 * references in spreadsheet formulas.
 * 
 * @author Josh Micich
 */
internal class EvaluationTracker(private val _cache: EvaluationCache) {
    // TODO - consider deleting this class and letting CellEvaluationFrame take care of itself
    private val _evaluationFrames: MutableList<CellEvaluationFrame>
    private val _currentlyEvaluatingCells: MutableSet<CellCacheEntry?>

    init {
        _evaluationFrames = ArrayList<CellEvaluationFrame>()
        _currentlyEvaluatingCells = HashSet<CellCacheEntry?>()
    }

    /**
     * Notifies this evaluation tracker that evaluation of the specified cell is
     * about to start.<br></br>
     * 
     * In the case of a `true` return code, the caller should
     * continue evaluation of the specified cell, and also be sure to call
     * <tt>endEvaluate()</tt> when complete.<br></br>
     * 
     * In the case of a `null` return code, the caller should
     * return an evaluation result of
     * <tt>ErrorEval.CIRCULAR_REF_ERROR<tt>, and not call <tt>endEvaluate()</tt>.
     * <br></br>
     * @return `false` if the specified cell is already being evaluated
    </tt></tt> */
    fun startEvaluate(cce: FormulaCellCacheEntry): Boolean {
        requireNotNull(cce) { "cellLoc must not be null" }
        if (_currentlyEvaluatingCells.contains(cce)) {
            return false
        }
        _currentlyEvaluatingCells.add(cce)
        _evaluationFrames.add(CellEvaluationFrame(cce))
        return true
    }

    fun updateCacheResult(result: ValueEval?) {
        val nFrames = _evaluationFrames.size
        check(nFrames >= 1) { "Call to endEvaluate without matching call to startEvaluate" }
        val frame = _evaluationFrames.get(nFrames - 1)
        if (result === ErrorEval.CIRCULAR_REF_ERROR && nFrames > 1) {
            // Don't cache a circular ref error result if this cell is not the top evaluated cell.
            // A true circular ref error will propagate all the way around the loop.  However, it's
            // possible to have parts of the formula tree (/ parts of the loop) to evaluate to
            // CIRCULAR_REF_ERROR, and that value not get used in the final cell result (see the
            // unit tests for a simple example). Thus, the only CIRCULAR_REF_ERROR result that can
            // safely be cached is that of the top evaluated cell.
            return
        }

        frame.updateFormulaResult(result)
    }

    /**
     * Notifies this evaluation tracker that the evaluation of the specified cell is complete. 
     *
     *
     * 
     * Every successful call to <tt>startEvaluate</tt> must be followed by a call to <tt>endEvaluate</tt> (recommended in a finally block) to enable
     * proper tracking of which cells are being evaluated at any point in time.
     *
     *
     * 
     * Assuming a well behaved client, parameters to this method would not be
     * required. However, they have been included to assert correct behaviour,
     * and form more meaningful error messages.
     */
    fun endEvaluate(cce: CellCacheEntry?) {
        var nFrames = _evaluationFrames.size
        check(nFrames >= 1) { "Call to endEvaluate without matching call to startEvaluate" }

        nFrames--
        val frame = _evaluationFrames.get(nFrames)
        check(cce === frame.cCE) { "Wrong cell specified. " }
        // else - no problems so pop current frame
        _evaluationFrames.removeAt(nFrames)
        _currentlyEvaluatingCells.remove(cce)
    }

    fun acceptFormulaDependency(cce: CellCacheEntry) {
        // Tell the currently evaluating cell frame that it has a dependency on the specified
        val prevFrameIndex = _evaluationFrames.size - 1
        if (prevFrameIndex < 0) {
            // Top level frame, there is no 'cell' above this frame that is using the current cell
        } else {
            val consumingFrame = _evaluationFrames.get(prevFrameIndex)
            consumingFrame.addSensitiveInputCell(cce)
        }
    }

    fun acceptPlainValueDependency(
        bookIndex: Int, sheetIndex: Int,
        rowIndex: Int, columnIndex: Int, value: ValueEval
    ) {
        // Tell the currently evaluating cell frame that it has a dependency on the specified
        val prevFrameIndex = _evaluationFrames.size - 1
        if (prevFrameIndex < 0) {
            // Top level frame, there is no 'cell' above this frame that is using the current cell
        } else {
            val consumingFrame = _evaluationFrames.get(prevFrameIndex)
            if (value === BlankEval.instance) {
                consumingFrame.addUsedBlankCell(bookIndex, sheetIndex, rowIndex, columnIndex)
            } else {
                val cce = _cache.getPlainValueEntry(
                    bookIndex, sheetIndex,
                    rowIndex, columnIndex, value
                )
                consumingFrame.addSensitiveInputCell(cce)
            }
        }
    }
}
