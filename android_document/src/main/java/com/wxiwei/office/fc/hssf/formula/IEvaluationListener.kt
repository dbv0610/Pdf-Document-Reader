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

import com.wxiwei.office.fc.hssf.formula.eval.ValueEval

/**
 * Tests can implement this class to track the internal working of the [WorkbookEvaluator].<br></br>
 * 
 * For POI internal testing use only
 * 
 * @author Josh Micich
 */
internal interface IEvaluationListener {
    /**
     * A (mostly) opaque interface to allow test clients to trace cache values
     * Each spreadsheet cell gets one unique cache entry instance.  These objects
     * are safe to use as keys in [java.util.HashMap]s
     */
    interface ICacheEntry {
        val value: ValueEval?
    }

    fun onCacheHit(sheetIndex: Int, rowIndex: Int, columnIndex: Int, result: ValueEval?)
    fun onReadPlainValue(sheetIndex: Int, rowIndex: Int, columnIndex: Int, entry: ICacheEntry?)
    fun onStartEvaluate(cell: EvaluationCell?, entry: ICacheEntry?)
    fun onEndEvaluate(entry: ICacheEntry?, result: ValueEval?)
    fun onClearWholeCache()
    fun onClearCachedValue(entry: ICacheEntry?)

    /**
     * Internally, formula [ICacheEntry]s are stored in sets which may change ordering due
     * to seemingly trivial changes.  This method is provided to make the order of call-backs to
     * [.onClearDependentCachedValue] more deterministic.
     */
    fun sortDependentCachedValues(formulaCells: Array<out ICacheEntry?>?)
    fun onClearDependentCachedValue(formulaCell: ICacheEntry?, depth: Int)
    fun onChangeFromBlankValue(
        sheetIndex: Int, rowIndex: Int, columnIndex: Int,
        cell: EvaluationCell?, entry: ICacheEntry?
    )
}
