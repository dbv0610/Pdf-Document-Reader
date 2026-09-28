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
internal class FormulaCellCache {
    internal interface IEntryOperation {
        fun processEntry(entry: FormulaCellCacheEntry)
    }

    private val _formulaEntriesByCell: MutableMap<Any?, FormulaCellCacheEntry?>

    init {
        // assumes the object returned by EvaluationCell.getIdentityKey() has a well behaved hashCode+equals
        _formulaEntriesByCell = HashMap<Any?, FormulaCellCacheEntry?>()
    }

    val cacheEntries: Array<CellCacheEntry?>
        get() {
            return _formulaEntriesByCell.values.toTypedArray<CellCacheEntry?>()
        }

    fun clear() {
        _formulaEntriesByCell.clear()
    }

    /**
     * @return `null` if not found
     */
    fun get(cell: EvaluationCell): FormulaCellCacheEntry? {
        return _formulaEntriesByCell.get(cell.identityKey)
    }

    fun put(cell: EvaluationCell, entry: FormulaCellCacheEntry?) {
        _formulaEntriesByCell.put(cell.identityKey, entry)
    }

    fun remove(cell: EvaluationCell): FormulaCellCacheEntry? {
        return _formulaEntriesByCell.remove(cell.identityKey)
    }

    fun applyOperation(operation: IEntryOperation) {
        val i = _formulaEntriesByCell.values.iterator()
        while (i.hasNext()) {
            operation.processEntry(i.next()!!)
        }
    }
}
