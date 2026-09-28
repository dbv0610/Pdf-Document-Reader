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
package com.wxiwei.office.fc.hssf.record.aggregates

import com.wxiwei.office.fc.hssf.formula.FormulaShifter
import com.wxiwei.office.fc.hssf.model.RecordStream
import com.wxiwei.office.fc.hssf.record.CFHeaderRecord

/**
 * Holds all the conditional formatting for a workbook sheet.
 *
 *
 * 
 * See OOO exelfileformat.pdf sec 4.12 'Conditional Formatting Table'
 * 
 * @author Josh Micich
 */
class ConditionalFormattingTable : RecordAggregate {
    private val _cfHeaders: MutableList<CFRecordsAggregate?>

    /**
     * Creates an empty ConditionalFormattingTable
     */
    constructor() {
        _cfHeaders = ArrayList<CFRecordsAggregate?>()
    }

    constructor(rs: RecordStream) {
        val temp: MutableList<CFRecordsAggregate?> = ArrayList<CFRecordsAggregate?>()
        while (rs.peekNextClass() == CFHeaderRecord::class.java) {
            temp.add(CFRecordsAggregate.Companion.createCFAggregate(rs))
        }
        _cfHeaders = temp
    }

    override fun visitContainedRecords(rv: RecordVisitor) {
        for (i in _cfHeaders.indices) {
            val subAgg = _cfHeaders.get(i) as CFRecordsAggregate
            subAgg.visitContainedRecords(rv)
        }
    }

    /**
     * @return index of the newly added CF header aggregate
     */
    fun add(cfAggregate: CFRecordsAggregate?): Int {
        _cfHeaders.add(cfAggregate)
        return _cfHeaders.size - 1
    }

    fun size(): Int {
        return _cfHeaders.size
    }

    fun get(index: Int): CFRecordsAggregate? {
        checkIndex(index)
        return _cfHeaders.get(index)
    }

    fun remove(index: Int) {
        checkIndex(index)
        _cfHeaders.removeAt(index)
    }

    private fun checkIndex(index: Int) {
        require(!(index < 0 || index >= _cfHeaders.size)) {
            ("Specified CF index " + index
                    + " is outside the allowable range (0.." + (_cfHeaders.size - 1) + ")")
        }
    }

    fun updateFormulasAfterCellShift(shifter: FormulaShifter?, externSheetIndex: Int) {
        var i = 0
        while (i < _cfHeaders.size) {
            val subAgg = _cfHeaders.get(i) as CFRecordsAggregate
            val shouldKeep = subAgg.updateFormulasAfterCellShift(shifter!!, externSheetIndex)
            if (!shouldKeep) {
                _cfHeaders.removeAt(i)
                i--
            }
            i++
        }
    }
}
