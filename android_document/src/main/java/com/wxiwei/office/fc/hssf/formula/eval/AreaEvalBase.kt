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

import com.wxiwei.office.fc.hssf.formula.ptg.AreaI

/**
 * @author Josh Micich
 */
abstract class AreaEvalBase : AreaEval {
    private val _firstColumn: Int
    private val _firstRow: Int
    private val _lastColumn: Int
    private val _lastRow: Int
    private val _nColumns: Int
    private val _nRows: Int

    protected constructor(firstRow: Int, firstColumn: Int, lastRow: Int, lastColumn: Int) {
        _firstColumn = firstColumn
        _firstRow = firstRow
        _lastColumn = lastColumn
        _lastRow = lastRow

        _nColumns = _lastColumn - _firstColumn + 1
        _nRows = _lastRow - _firstRow + 1
    }

    protected constructor(ptg: AreaI) {
        _firstRow = ptg.firstRow
        _firstColumn = ptg.firstColumn
        _lastRow = ptg.lastRow
        _lastColumn = ptg.lastColumn

        _nColumns = _lastColumn - _firstColumn + 1
        _nRows = _lastRow - _firstRow + 1
    }

    override val firstColumn: Int
        get() = _firstColumn

    override val firstRow: Int
        get() = _firstRow

    override val lastColumn: Int
        get() = _lastColumn

    override val lastRow: Int
        get() = _lastRow

    override fun getAbsoluteValue(row: Int, col: Int): ValueEval? {
        val rowOffsetIx = row - _firstRow
        val colOffsetIx = col - _firstColumn

        require(!(rowOffsetIx < 0 || rowOffsetIx >= _nRows)) {
            ("Specified row index (" + row
                    + ") is outside the allowed range (" + _firstRow + ".." + _lastRow + ")")
        }
        require(!(colOffsetIx < 0 || colOffsetIx >= _nColumns)) {
            ("Specified column index (" + col
                    + ") is outside the allowed range (" + _firstColumn + ".." + col + ")")
        }
        return getRelativeValue(rowOffsetIx, colOffsetIx)
    }

    override fun contains(row: Int, col: Int): Boolean {
        return _firstRow <= row && _lastRow >= row && _firstColumn <= col && _lastColumn >= col
    }

    override fun containsRow(row: Int): Boolean {
        return _firstRow <= row && _lastRow >= row
    }

    override fun containsColumn(col: Int): Boolean {
        return _firstColumn <= col && _lastColumn >= col
    }

    override val isColumn: Boolean
        get() = _firstColumn == _lastColumn

    override val isRow: Boolean
        get() = _firstRow == _lastRow

    override val height: Int
        get() = _lastRow - _firstRow + 1

    override fun getValue(row: Int, col: Int): ValueEval? {
        return getRelativeValue(row, col)
    }

    abstract override fun getRelativeValue(
        relativeRowIndex: Int,
        relativeColumnIndex: Int
    ): ValueEval?

    override val width: Int
        get() = _lastColumn - _firstColumn + 1

    /**
     * @return  whether cell at rowIndex and columnIndex is a subtotal.
     * By default return false which means 'don't care about subtotals'
     */
    override fun isSubTotal(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }
}
