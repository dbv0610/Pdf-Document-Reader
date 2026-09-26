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

import com.wxiwei.office.fc.hssf.formula.ptg.Area2DPtgBase
import com.wxiwei.office.fc.hssf.formula.ptg.Area3DPtg
import com.wxiwei.office.fc.hssf.formula.ptg.AreaErrPtg
import com.wxiwei.office.fc.hssf.formula.ptg.AreaPtg
import com.wxiwei.office.fc.hssf.formula.ptg.AreaPtgBase
import com.wxiwei.office.fc.hssf.formula.ptg.DeletedArea3DPtg
import com.wxiwei.office.fc.hssf.formula.ptg.DeletedRef3DPtg
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.formula.ptg.Ref3DPtg
import com.wxiwei.office.fc.hssf.formula.ptg.RefErrorPtg
import com.wxiwei.office.fc.hssf.formula.ptg.RefPtg
import com.wxiwei.office.fc.hssf.formula.ptg.RefPtgBase
import kotlin.math.max
import kotlin.math.min

/**
 * @author Josh Micich
 */
class FormulaShifter {
    internal enum class ShiftMode {
        Row,
        Sheet
    }

    /**
     * Extern sheet index of sheet where moving is occurring
     */
    private val _externSheetIndex: Int
    private val _firstMovedIndex: Int
    private val _lastMovedIndex: Int
    private val _amountToMove: Int

    private val _srcSheetIndex: Int
    private val _dstSheetIndex: Int

    private val _mode: ShiftMode

    /**
     * Create an instance for shifting row.
     * 
     * For example, this will be called on [com.wxiwei.office.fc.hssf.usermodel.HSSFSheet.shiftRows] }
     */
    private constructor(
        externSheetIndex: Int,
        firstMovedIndex: Int,
        lastMovedIndex: Int,
        amountToMove: Int
    ) {
        require(amountToMove != 0) { "amountToMove must not be zero" }
        require(firstMovedIndex <= lastMovedIndex) { "firstMovedIndex, lastMovedIndex out of order" }
        _externSheetIndex = externSheetIndex
        _firstMovedIndex = firstMovedIndex
        _lastMovedIndex = lastMovedIndex
        _amountToMove = amountToMove
        _mode = ShiftMode.Row

        _dstSheetIndex = -1
        _srcSheetIndex = _dstSheetIndex
    }

    /**
     * Create an instance for shifting sheets.
     * 
     * For example, this will be called on [com.wxiwei.office.fc.hssf.usermodel.HSSFWorkbook.setSheetOrder]
     */
    private constructor(srcSheetIndex: Int, dstSheetIndex: Int) {
        _amountToMove = -1
        _lastMovedIndex = _amountToMove
        _firstMovedIndex = _lastMovedIndex
        _externSheetIndex = _firstMovedIndex

        _srcSheetIndex = srcSheetIndex
        _dstSheetIndex = dstSheetIndex
        _mode = ShiftMode.Sheet
    }

    override fun toString(): String {
        val sb = StringBuffer()

        sb.append(javaClass.getName())
        sb.append(" [")
        sb.append(_firstMovedIndex)
        sb.append(_lastMovedIndex)
        sb.append(_amountToMove)
        return sb.toString()
    }

    /**
     * @param ptgs - if necessary, will get modified by this method
     * @param currentExternSheetIx - the extern sheet index of the sheet that contains the formula being adjusted
     * @return `true` if a change was made to the formula tokens
     */
    fun adjustFormula(ptgs: Array<Ptg?>, currentExternSheetIx: Int): Boolean {
        var refsWereChanged = false
        for (i in ptgs.indices) {
            val newPtg = adjustPtg(ptgs[i], currentExternSheetIx)
            if (newPtg != null) {
                refsWereChanged = true
                ptgs[i] = newPtg
            }
        }
        return refsWereChanged
    }

    private fun adjustPtg(ptg: Ptg?, currentExternSheetIx: Int): Ptg? {
        when (_mode) {
            ShiftMode.Row -> return adjustPtgDueToRowMove(ptg, currentExternSheetIx)
            ShiftMode.Sheet -> return adjustPtgDueToShiftMove(ptg)
            else -> throw IllegalStateException("Unsupported shift mode: " + _mode)
        }
    }

    /**
     * @return `true` if this Ptg needed to be changed
     */
    private fun adjustPtgDueToRowMove(ptg: Ptg?, currentExternSheetIx: Int): Ptg? {
        if (ptg is RefPtg) {
            if (currentExternSheetIx != _externSheetIndex) {
                // local refs on other sheets are unaffected
                return null
            }
            val rptg = ptg
            return rowMoveRefPtg(rptg)
        }
        if (ptg is Ref3DPtg) {
            val rptg = ptg
            if (_externSheetIndex != rptg.externSheetIndex) {
                // only move 3D refs that refer to the sheet with cells being moved
                // (currentExternSheetIx is irrelevant)
                return null
            }
            return rowMoveRefPtg(rptg)
        }
        if (ptg is Area2DPtgBase) {
            if (currentExternSheetIx != _externSheetIndex) {
                // local refs on other sheets are unaffected
                return ptg
            }
            return rowMoveAreaPtg(ptg)
        }
        if (ptg is Area3DPtg) {
            val aptg = ptg
            if (_externSheetIndex != aptg.externSheetIndex) {
                // only move 3D refs that refer to the sheet with cells being moved
                // (currentExternSheetIx is irrelevant)
                return null
            }
            return rowMoveAreaPtg(aptg)
        }
        return null
    }

    private fun adjustPtgDueToShiftMove(ptg: Ptg?): Ptg? {
        var updatedPtg: Ptg? = null
        if (ptg is Ref3DPtg) {
            val ref = ptg
            if (ref.externSheetIndex == _srcSheetIndex) {
                ref.setExternSheetIndex(_dstSheetIndex)
                updatedPtg = ref
            } else if (ref.externSheetIndex == _dstSheetIndex) {
                ref.setExternSheetIndex(_srcSheetIndex)
                updatedPtg = ref
            }
        }
        return updatedPtg
    }

    private fun rowMoveRefPtg(rptg: RefPtgBase): Ptg? {
        val refRow = rptg.row
        if (_firstMovedIndex <= refRow && refRow <= _lastMovedIndex) {
            // Rows being moved completely enclose the ref.
            // - move the area ref along with the rows regardless of destination
            rptg.row = refRow + _amountToMove
            return rptg
        }

        // else rules for adjusting area may also depend on the destination of the moved rows
        val destFirstRowIndex = _firstMovedIndex + _amountToMove
        val destLastRowIndex = _lastMovedIndex + _amountToMove

        // ref is outside source rows
        // check for clashes with destination
        if (destLastRowIndex < refRow || refRow < destFirstRowIndex) {
            // destination rows are completely outside ref
            return null
        }

        if (destFirstRowIndex <= refRow && refRow <= destLastRowIndex) {
            // destination rows enclose the area (possibly exactly)
            return createDeletedRef(rptg)
        }
        throw IllegalStateException(
            "Situation not covered: (" + _firstMovedIndex + ", " +
                    _lastMovedIndex + ", " + _amountToMove + ", " + refRow + ", " + refRow + ")"
        )
    }

    private fun rowMoveAreaPtg(aptg: AreaPtgBase): Ptg? {
        val aFirstRow = aptg.firstRow
        val aLastRow = aptg.lastRow
        if (_firstMovedIndex <= aFirstRow && aLastRow <= _lastMovedIndex) {
            // Rows being moved completely enclose the area ref.
            // - move the area ref along with the rows regardless of destination
            aptg.setFirstRow(aFirstRow + _amountToMove)
            aptg.setLastRow(aLastRow + _amountToMove)
            return aptg
        }

        // else rules for adjusting area may also depend on the destination of the moved rows
        val destFirstRowIndex = _firstMovedIndex + _amountToMove
        val destLastRowIndex = _lastMovedIndex + _amountToMove

        if (aFirstRow < _firstMovedIndex && _lastMovedIndex < aLastRow) {
            // Rows moved were originally *completely* within the area ref

            // If the destination of the rows overlaps either the top
            // or bottom of the area ref there will be a change

            if (destFirstRowIndex < aFirstRow && aFirstRow <= destLastRowIndex) {
                // truncate the top of the area by the moved rows
                aptg.setFirstRow(destLastRowIndex + 1)
                return aptg
            } else if (destFirstRowIndex <= aLastRow && aLastRow < destLastRowIndex) {
                // truncate the bottom of the area by the moved rows
                aptg.setLastRow(destFirstRowIndex - 1)
                return aptg
            }
            // else - rows have moved completely outside the area ref,
            // or still remain completely within the area ref
            return null // - no change to the area
        }
        if (_firstMovedIndex <= aFirstRow && aFirstRow <= _lastMovedIndex) {
            // Rows moved include the first row of the area ref, but not the last row
            // btw: (aLastRow > _lastMovedIndex)
            if (_amountToMove < 0) {
                // simple case - expand area by shifting top upward
                aptg.setFirstRow(aFirstRow + _amountToMove)
                return aptg
            }
            if (destFirstRowIndex > aLastRow) {
                // in this case, excel ignores the row move
                return null
            }
            var newFirstRowIx = aFirstRow + _amountToMove
            if (destLastRowIndex < aLastRow) {
                // end of area is preserved (will remain exact same row)
                // the top area row is moved simply
                aptg.setFirstRow(newFirstRowIx)
                return aptg
            }
            // else - bottom area row has been replaced - both area top and bottom may move now
            val areaRemainingTopRowIx = _lastMovedIndex + 1
            if (destFirstRowIndex > areaRemainingTopRowIx) {
                // old top row of area has moved deep within the area, and exposed a new top row
                newFirstRowIx = areaRemainingTopRowIx
            }
            aptg.setFirstRow(newFirstRowIx)
            aptg.setLastRow(max(aLastRow, destLastRowIndex))
            return aptg
        }
        if (_firstMovedIndex <= aLastRow && aLastRow <= _lastMovedIndex) {
            // Rows moved include the last row of the area ref, but not the first
            // btw: (aFirstRow < _firstMovedIndex)
            if (_amountToMove > 0) {
                // simple case - expand area by shifting bottom downward
                aptg.setLastRow(aLastRow + _amountToMove)
                return aptg
            }
            if (destLastRowIndex < aFirstRow) {
                // in this case, excel ignores the row move
                return null
            }
            var newLastRowIx = aLastRow + _amountToMove
            if (destFirstRowIndex > aFirstRow) {
                // top of area is preserved (will remain exact same row)
                // the bottom area row is moved simply
                aptg.setLastRow(newLastRowIx)
                return aptg
            }
            // else - top area row has been replaced - both area top and bottom may move now
            val areaRemainingBottomRowIx = _firstMovedIndex - 1
            if (destLastRowIndex < areaRemainingBottomRowIx) {
                // old bottom row of area has moved up deep within the area, and exposed a new bottom row
                newLastRowIx = areaRemainingBottomRowIx
            }
            aptg.setFirstRow(min(aFirstRow, destFirstRowIndex))
            aptg.setLastRow(newLastRowIx)
            return aptg
        }

        // else source rows include none of the rows of the area ref
        // check for clashes with destination
        if (destLastRowIndex < aFirstRow || aLastRow < destFirstRowIndex) {
            // destination rows are completely outside area ref
            return null
        }

        if (destFirstRowIndex <= aFirstRow && aLastRow <= destLastRowIndex) {
            // destination rows enclose the area (possibly exactly)
            return createDeletedRef(aptg)
        }

        if (aFirstRow <= destFirstRowIndex && destLastRowIndex <= aLastRow) {
            // destination rows are within area ref (possibly exact on top or bottom, but not both)
            return null // - no change to area
        }

        if (destFirstRowIndex < aFirstRow && aFirstRow <= destLastRowIndex) {
            // dest rows overlap top of area
            // - truncate the top
            aptg.setFirstRow(destLastRowIndex + 1)
            return aptg
        }
        if (destFirstRowIndex < aLastRow && aLastRow <= destLastRowIndex) {
            // dest rows overlap bottom of area
            // - truncate the bottom
            aptg.setLastRow(destFirstRowIndex - 1)
            return aptg
        }
        throw IllegalStateException(
            "Situation not covered: (" + _firstMovedIndex + ", " +
                    _lastMovedIndex + ", " + _amountToMove + ", " + aFirstRow + ", " + aLastRow + ")"
        )
    }

    companion object {
        @JvmStatic
        fun createForRowShift(
            externSheetIndex: Int,
            firstMovedRowIndex: Int,
            lastMovedRowIndex: Int,
            numberOfRowsToMove: Int
        ): FormulaShifter {
            return FormulaShifter(
                externSheetIndex,
                firstMovedRowIndex,
                lastMovedRowIndex,
                numberOfRowsToMove
            )
        }

        @JvmStatic
        fun createForSheetShift(srcSheetIndex: Int, dstSheetIndex: Int): FormulaShifter {
            return FormulaShifter(srcSheetIndex, dstSheetIndex)
        }

        private fun createDeletedRef(ptg: Ptg): Ptg {
            if (ptg is RefPtg) {
                return RefErrorPtg()
            }
            if (ptg is Ref3DPtg) {
                val rptg = ptg
                return DeletedRef3DPtg(rptg.externSheetIndex)
            }
            if (ptg is AreaPtg) {
                return AreaErrPtg()
            }
            if (ptg is Area3DPtg) {
                val area3DPtg = ptg
                return DeletedArea3DPtg(area3DPtg.externSheetIndex)
            }

            throw IllegalArgumentException("Unexpected ref ptg class (" + ptg.javaClass.getName() + ")")
        }
    }
}
