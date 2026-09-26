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

import com.wxiwei.office.fc.hssf.formula.IEvaluationListener.ICacheEntry
import com.wxiwei.office.fc.hssf.formula.eval.BlankEval
import com.wxiwei.office.fc.hssf.formula.eval.BoolEval
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.NumberEval
import com.wxiwei.office.fc.hssf.formula.eval.StringEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval


/**
 * Stores the parameters that identify the evaluation of one cell.<br></br>
 */
internal abstract class CellCacheEntry protected constructor() : ICacheEntry {
    private val _consumingCells: FormulaCellCacheEntrySet
    private var _value: ValueEval? = null


    init {
        _consumingCells = FormulaCellCacheEntrySet()
    }

    protected fun clearValue() {
        _value = null
    }

    fun updateValue(value: ValueEval?): Boolean {
        requireNotNull(value) { "Did not expect to update to null" }
        val result: Boolean = !areValuesEqual(_value, value)
        _value = value
        return result
    }

    override val value: ValueEval?
        get() = _value

    fun addConsumingCell(cellLoc: FormulaCellCacheEntry) {
        _consumingCells.add(cellLoc)
    }

    val consumingCells: Array<FormulaCellCacheEntry>
        get() = _consumingCells.toArray()

    fun clearConsumingCell(cce: FormulaCellCacheEntry) {
        check(_consumingCells.remove(cce)) { "Specified formula cell is not consumed by this cell" }
    }

    fun recurseClearCachedFormulaResults(listener: IEvaluationListener?) {
        if (listener == null) {
            recurseClearCachedFormulaResults()
        } else {
            listener.onClearCachedValue(this)
            recurseClearCachedFormulaResults(listener, 1)
        }
    }

    /**
     * Calls formulaCell.setFormulaResult(null, null) recursively all the way up the tree of
     * dependencies. Calls usedCell.clearConsumingCell(fc) for each child of a cell that is
     * cleared along the way.
     * @param formulaCells
     */
    protected fun recurseClearCachedFormulaResults() {
        val formulaCells = this.consumingCells

        for (i in formulaCells.indices) {
            val fc = formulaCells[i]
            fc.clearFormulaEntry()
            fc.recurseClearCachedFormulaResults()
        }
    }

    /**
     * Identical to [.recurseClearCachedFormulaResults] except for the listener call-backs
     */
    protected fun recurseClearCachedFormulaResults(listener: IEvaluationListener, depth: Int) {
        val formulaCells = this.consumingCells

        listener.sortDependentCachedValues(formulaCells)
        for (i in formulaCells.indices) {
            val fc = formulaCells[i]
            listener.onClearDependentCachedValue(fc, depth)
            fc.clearFormulaEntry()
            fc.recurseClearCachedFormulaResults(listener, depth + 1)
        }
    }

    companion object {
        val EMPTY_ARRAY: Array<CellCacheEntry> = arrayOf<CellCacheEntry>()

        private fun areValuesEqual(a: ValueEval?, b: ValueEval): Boolean {
            if (a == null) {
                return false
            }
            val cls: Class<out ValueEval?> = a.javaClass
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
    }
}
