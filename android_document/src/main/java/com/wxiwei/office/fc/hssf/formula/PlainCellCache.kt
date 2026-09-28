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

/**
 * 
 * @author Josh Micich
 */
internal class PlainCellCache {
    class Loc {
        private val _bookSheetColumn: Long

        val rowIndex: Int

        constructor(bookIndex: Int, sheetIndex: Int, rowIndex: Int, columnIndex: Int) {
            _bookSheetColumn = toBookSheetColumn(bookIndex, sheetIndex, columnIndex)
            this.rowIndex = rowIndex
        }

        constructor(bookSheetColumn: Long, rowIndex: Int) {
            _bookSheetColumn = bookSheetColumn
            this.rowIndex = rowIndex
        }

        override fun hashCode(): Int {
            return (_bookSheetColumn xor (_bookSheetColumn ushr 32)).toInt() + 17 * this.rowIndex
        }

        override fun equals(obj: Any?): Boolean {
            assert(obj is Loc) { "these package-private cache key instances are only compared to themselves" }
            val other: Loc = obj as Loc
            return _bookSheetColumn == other._bookSheetColumn && this.rowIndex == other.rowIndex
        }

        val columnIndex: Int
            get() = (_bookSheetColumn and 0x000FFFFL).toInt()

        val sheetIndex: Int
            get() = ((_bookSheetColumn shr 32) and 0xFFFFL).toInt()

        val bookIndex: Int
            get() = ((_bookSheetColumn shr 48) and 0xFFFFL).toInt()

        companion object {
            fun toBookSheetColumn(bookIndex: Int, sheetIndex: Int, columnIndex: Int): Long {
                return ((bookIndex.toLong() and 0xFFFFL) shl 48) +
                        ((sheetIndex.toLong() and 0xFFFFL) shl 32) +
                        ((columnIndex.toLong() and 0xFFFFL) shl 0)
            }
        }
    }

    private val _plainValueEntriesByLoc: MutableMap<Loc?, PlainValueCellCacheEntry?>

    init {
        _plainValueEntriesByLoc = HashMap<Loc?, PlainValueCellCacheEntry?>()
    }

    fun put(key: Loc?, cce: PlainValueCellCacheEntry?) {
        _plainValueEntriesByLoc.put(key, cce)
    }

    fun clear() {
        _plainValueEntriesByLoc.clear()
    }

    fun get(key: Loc?): PlainValueCellCacheEntry? {
        return _plainValueEntriesByLoc.get(key)
    }

    fun remove(key: Loc?) {
        _plainValueEntriesByLoc.remove(key)
    }
}
