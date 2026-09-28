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
import com.wxiwei.office.fc.hssf.formula.ptg.AreaErrPtg
import com.wxiwei.office.fc.hssf.formula.ptg.AreaPtg
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.model.RecordStream
import com.wxiwei.office.fc.hssf.record.CFHeaderRecord
import com.wxiwei.office.fc.hssf.record.CFRuleRecord
import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.ss.util.HSSFCellRangeAddress

/**
 * CFRecordsAggregate - aggregates Conditional Formatting records CFHeaderRecord
 * and number of up to three CFRuleRecord records together to simplify
 * access to them.
 * 
 * @author Dmitriy Kumshayev
 */
class CFRecordsAggregate private constructor(
    pHeader: CFHeaderRecord,
    pRules: Array<CFRuleRecord?>
) : RecordAggregate() {
    /**
     * @return the header. Never `null`.
     */
    @JvmField
    val header: CFHeaderRecord

    /** List of CFRuleRecord objects  */
    private val rules: MutableList<CFRuleRecord?>

    init {
        requireNotNull(pHeader) { "header must not be null" }
        requireNotNull(pRules) { "rules must not be null" }
        require(pRules.size <= MAX_CONDTIONAL_FORMAT_RULES) {
            ("No more than "
                    + MAX_CONDTIONAL_FORMAT_RULES + " rules may be specified")
        }
        if (pRules.size != pHeader.numberOfConditionalFormats) {
            throw RuntimeException("Mismatch number of rules")
        }
        header = pHeader
        rules = ArrayList<CFRuleRecord?>(3)
        for (i in pRules.indices) {
            rules.add(pRules[i])
        }
    }

    constructor(regions: Array<HSSFCellRangeAddress>, rules: Array<CFRuleRecord?>) : this(
        CFHeaderRecord(regions, rules.size),
        rules
    )

    /**
     * Create a deep clone of the record
     */
    fun cloneCFAggregate(): CFRecordsAggregate {
        val newRecs = arrayOfNulls<CFRuleRecord>(rules.size)
        for (i in newRecs.indices) {
            newRecs[i] = getRule(i)!!.clone() as CFRuleRecord
        }
        return CFRecordsAggregate(header.clone() as CFHeaderRecord, newRecs)
    }

    private fun checkRuleIndex(idx: Int) {
        require(!(idx < 0 || idx >= rules.size)) {
            ("Bad rule record index (" + idx
                    + ") nRules=" + rules.size)
        }
    }

    fun getRule(idx: Int): CFRuleRecord? {
        checkRuleIndex(idx)
        return rules.get(idx)
    }

    fun setRule(idx: Int, r: CFRuleRecord) {
        requireNotNull(r) { "r must not be null" }
        checkRuleIndex(idx)
        rules.set(idx, r)
    }

    fun addRule(r: CFRuleRecord) {
        requireNotNull(r) { "r must not be null" }
        check(rules.size < MAX_CONDTIONAL_FORMAT_RULES) {
            ("Cannot have more than "
                    + MAX_CONDTIONAL_FORMAT_RULES + " conditional format rules")
        }
        rules.add(r)
        header.numberOfConditionalFormats = rules.size
    }

    val numberOfRules: Int
        get() = rules.size

    /**
     * String representation of CFRecordsAggregate
     */
    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[CF]\n")
        buffer.append(header.toString())
        for (i in rules.indices) {
            val cfRule = rules.get(i) as CFRuleRecord
            buffer.append(cfRule.toString())
        }
        buffer.append("[/CF]\n")
        return buffer.toString()
    }

    override fun visitContainedRecords(rv: RecordVisitor) {
        rv.visitRecord(header)
        for (i in rules.indices) {
            val rule = rules.get(i) as CFRuleRecord
            rv.visitRecord(rule)
        }
    }

    /**
     * @return `false` if this whole [CFHeaderRecord] / [CFRuleRecord]s should be deleted
     */
    fun updateFormulasAfterCellShift(shifter: FormulaShifter, currentExternSheetIx: Int): Boolean {
        val cellRanges = header.cellRanges
        var changed = false
        val temp: MutableList<HSSFCellRangeAddress> = ArrayList<HSSFCellRangeAddress>()
        for (i in cellRanges.indices) {
            val craOld = cellRanges[i]
            val craNew: HSSFCellRangeAddress? = shiftRange(shifter, craOld, currentExternSheetIx)
            if (craNew == null) {
                changed = true
                continue
            }
            temp.add(craNew)
            if (craNew !== craOld) {
                changed = true
            }
        }

        if (changed) {
            val nRanges = temp.size
            if (nRanges == 0) {
                return false
            }
            val newRanges = temp.toTypedArray()
            header.setCellRanges(newRanges)
        }

        for (i in rules.indices) {
            val rule = rules.get(i) as CFRuleRecord
            var ptgs: Array<Ptg?>?
            ptgs = rule.parsedExpression1
            if (ptgs != null && shifter.adjustFormula(ptgs, currentExternSheetIx)) {
                rule.parsedExpression1 = ptgs
            }
            ptgs = rule.parsedExpression2
            if (ptgs != null && shifter.adjustFormula(ptgs, currentExternSheetIx)) {
                rule.parsedExpression2 = ptgs
            }
        }
        return true
    }

    companion object {
        /** Excel allows up to 3 conditional formating rules  */
        private const val MAX_CONDTIONAL_FORMAT_RULES = 3

        /**
         * Create CFRecordsAggregate from a list of CF Records
         * @param rs - the stream to read from
         * @return CFRecordsAggregate object
         */
        fun createCFAggregate(rs: RecordStream): CFRecordsAggregate {
            val rec: Record? = rs.next
            check(rec!!.getSid() == CFHeaderRecord.sid) {
                ("next record sid was " + rec.getSid()
                        + " instead of " + CFHeaderRecord.sid + " as expected")
            }

            val header = rec as CFHeaderRecord
            val nRules = header.numberOfConditionalFormats

            val rules = arrayOfNulls<CFRuleRecord>(nRules)
            for (i in rules.indices) {
                rules[i] = rs.next as CFRuleRecord?
            }

            return CFRecordsAggregate(header, rules)
        }

        private fun shiftRange(
            shifter: FormulaShifter,
            cra: HSSFCellRangeAddress,
            currentExternSheetIx: Int
        ): HSSFCellRangeAddress? {
            // FormulaShifter works well in terms of Ptgs - so convert CellRangeAddress to AreaPtg (and back) here
            val aptg = AreaPtg(
                cra.getFirstRow(),
                cra.getLastRow(),
                cra.getFirstColumn(),
                cra.getLastColumn(),
                false,
                false,
                false,
                false
            )
            val ptgs = arrayOf<Ptg?>(aptg)

            if (!shifter.adjustFormula(ptgs, currentExternSheetIx)) {
                return cra
            }
            val ptg0 = ptgs[0]
            if (ptg0 is AreaPtg) {
                val bptg = ptg0
                return HSSFCellRangeAddress(
                    bptg.firstRow,
                    bptg.lastRow,
                    bptg.firstColumn,
                    bptg.lastColumn
                )
            }
            if (ptg0 is AreaErrPtg) {
                return null
            }
            throw IllegalStateException("Unexpected shifted ptg class (" + ptg0!!.javaClass.getName() + ")")
        }
    }
}
