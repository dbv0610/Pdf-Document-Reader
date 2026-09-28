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

import com.wxiwei.office.fc.ss.util.CellReference

/**
 * Optimisation - compacts many blank cell references used by a single formula.
 * 
 * @author Josh Micich
 */
internal class FormulaUsedBlankCellSet {
    class BookSheetKey(private val _bookIndex: Int, private val _sheetIndex: Int) {
        override fun hashCode(): Int {
            return _bookIndex * 17 + _sheetIndex
        }

        override fun equals(obj: Any?): Boolean {
            assert(obj is BookSheetKey) { "these private cache key instances are only compared to themselves" }
            val other: BookSheetKey = obj as BookSheetKey
            return _bookIndex == other._bookIndex && _sheetIndex == other._sheetIndex
        }
    }

    private class BlankCellSheetGroup {
        private val _rectangleGroups: MutableList<BlankCellRectangleGroup>
        private var _currentRowIndex: Int
        private var _firstColumnIndex = 0
        private var _lastColumnIndex = 0
        private var _currentRectangleGroup: BlankCellRectangleGroup? = null

        init {
            _rectangleGroups = ArrayList<BlankCellRectangleGroup>()
            _currentRowIndex = -1
        }

        fun addCell(rowIndex: Int, columnIndex: Int) {
            if (_currentRowIndex == -1) {
                _currentRowIndex = rowIndex
                _firstColumnIndex = columnIndex
                _lastColumnIndex = columnIndex
            } else {
                if (_currentRowIndex == rowIndex && _lastColumnIndex + 1 == columnIndex) {
                    _lastColumnIndex = columnIndex
                } else {
                    // cell does not fit on end of current row
                    if (_currentRectangleGroup == null) {
                        _currentRectangleGroup = BlankCellRectangleGroup(
                            _currentRowIndex,
                            _firstColumnIndex,
                            _lastColumnIndex
                        )
                    } else {
                        if (!_currentRectangleGroup!!.acceptRow(
                                _currentRowIndex,
                                _firstColumnIndex,
                                _lastColumnIndex
                            )
                        ) {
                            _rectangleGroups.add(_currentRectangleGroup!!)
                            _currentRectangleGroup = BlankCellRectangleGroup(
                                _currentRowIndex,
                                _firstColumnIndex,
                                _lastColumnIndex
                            )
                        }
                    }
                    _currentRowIndex = rowIndex
                    _firstColumnIndex = columnIndex
                    _lastColumnIndex = columnIndex
                }
            }
        }

        fun containsCell(rowIndex: Int, columnIndex: Int): Boolean {
            for (i in _rectangleGroups.indices.reversed()) {
                val bcrg = _rectangleGroups.get(i)
                if (bcrg.containsCell(rowIndex, columnIndex)) {
                    return true
                }
            }
            if (_currentRectangleGroup != null && _currentRectangleGroup!!.containsCell(
                    rowIndex,
                    columnIndex
                )
            ) {
                return true
            }
            if (_currentRowIndex != -1 && _currentRowIndex == rowIndex) {
                if (_firstColumnIndex <= columnIndex && columnIndex <= _lastColumnIndex) {
                    return true
                }
            }
            return false
        }
    }

    private class BlankCellRectangleGroup(
        private val _firstRowIndex: Int,
        private val _firstColumnIndex: Int,
        private val _lastColumnIndex: Int
    ) {
        private var _lastRowIndex: Int

        init {
            _lastRowIndex = _firstRowIndex
        }

        fun containsCell(rowIndex: Int, columnIndex: Int): Boolean {
            if (columnIndex < _firstColumnIndex) {
                return false
            }
            if (columnIndex > _lastColumnIndex) {
                return false
            }
            if (rowIndex < _firstRowIndex) {
                return false
            }
            if (rowIndex > _lastRowIndex) {
                return false
            }
            return true
        }

        fun acceptRow(rowIndex: Int, firstColumnIndex: Int, lastColumnIndex: Int): Boolean {
            if (firstColumnIndex != _firstColumnIndex) {
                return false
            }
            if (lastColumnIndex != _lastColumnIndex) {
                return false
            }
            if (rowIndex != _lastRowIndex + 1) {
                return false
            }
            _lastRowIndex = rowIndex
            return true
        }

        override fun toString(): String {
            val sb = StringBuffer(64)
            val crA = CellReference(_firstRowIndex, _firstColumnIndex, false, false)
            val crB = CellReference(_lastRowIndex, _lastColumnIndex, false, false)
            sb.append(javaClass.getName())
            sb.append(" [").append(crA.formatAsString()).append(':').append(crB.formatAsString())
                .append("]")
            return sb.toString()
        }
    }

    private val _sheetGroupsByBookSheet: MutableMap<BookSheetKey?, BlankCellSheetGroup?>

    init {
        _sheetGroupsByBookSheet = HashMap<BookSheetKey?, BlankCellSheetGroup?>()
    }

    fun addCell(bookIndex: Int, sheetIndex: Int, rowIndex: Int, columnIndex: Int) {
        val sbcg = getSheetGroup(bookIndex, sheetIndex)
        sbcg.addCell(rowIndex, columnIndex)
    }

    private fun getSheetGroup(bookIndex: Int, sheetIndex: Int): BlankCellSheetGroup {
        val key = BookSheetKey(bookIndex, sheetIndex)

        var result = _sheetGroupsByBookSheet.get(key)
        if (result == null) {
            result = BlankCellSheetGroup()
            _sheetGroupsByBookSheet.put(key, result)
        }
        return result
    }

    fun containsCell(key: BookSheetKey?, rowIndex: Int, columnIndex: Int): Boolean {
        val bcsg = _sheetGroupsByBookSheet.get(key)
        if (bcsg == null) {
            return false
        }
        return bcsg.containsCell(rowIndex, columnIndex)
    }

    val isEmpty: Boolean
        get() = _sheetGroupsByBookSheet.isEmpty()
}
