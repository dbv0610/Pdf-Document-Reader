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
package com.wxiwei.office.fc.hssf.usermodel

import com.wxiwei.office.fc.hssf.record.CFRuleRecord
import com.wxiwei.office.fc.hssf.record.aggregates.CFRecordsAggregate
import com.wxiwei.office.fc.hssf.record.aggregates.ConditionalFormattingTable
import com.wxiwei.office.fc.ss.SpreadsheetVersion
import com.wxiwei.office.fc.ss.usermodel.ConditionalFormatting
import com.wxiwei.office.fc.ss.usermodel.ConditionalFormattingRule
import com.wxiwei.office.fc.ss.usermodel.SheetConditionalFormatting
import com.wxiwei.office.fc.ss.util.HSSFCellRangeAddress
import com.wxiwei.office.fc.ss.util.Region


/**
 * The 'Conditional Formatting' facet of <tt>HSSFSheet</tt>
 * 
 * @author Dmitriy Kumshayev
 */
class HSSFSheetConditionalFormatting internal constructor(private val _sheet: HSSFSheet) :
    SheetConditionalFormatting {
    private val _conditionalFormattingTable: ConditionalFormattingTable?

    /* package */
    init {
        _conditionalFormattingTable = _sheet.sheet.conditionalFormattingTable
    }

    /**
     * A factory method allowing to create a conditional formatting rule
     * with a cell comparison operator
     *
     *
     * TODO - formulas containing cell references are currently not parsed properly
     * 
     * @param comparisonOperation - a constant value from
     * <tt>[CFRuleRecord.ComparisonOperator]</tt>: 
     *
     *
     * 
     *  * BETWEEN
     *  * NOT_BETWEEN
     *  * EQUAL
     *  * NOT_EQUAL
     *  * GT
     *  * LT
     *  * GE
     *  * LE
     * 
     * 
     * @param formula1 - formula for the valued, compared with the cell
     * @param formula2 - second formula (only used with
     * [CFRuleRecord.ComparisonOperator.BETWEEN]) and
     * [CFRuleRecord.ComparisonOperator.NOT_BETWEEN] operations)
     */
    override fun createConditionalFormattingRule(
        comparisonOperation: Byte,
        formula1: String?,
        formula2: String?
    ): HSSFConditionalFormattingRule? {
        //		HSSFWorkbook wb = _sheet.getWorkbook();
//		CFRuleRecord rr = CFRuleRecord.create(_sheet, comparisonOperation, formula1, formula2);
//		return new HSSFConditionalFormattingRule(wb, rr);

        return null
    }

    override fun createConditionalFormattingRule(
        comparisonOperation: Byte,
        formula1: String?
    ): HSSFConditionalFormattingRule? {
        //        HSSFWorkbook wb = _sheet.getWorkbook();
//        CFRuleRecord rr = CFRuleRecord.create(_sheet, comparisonOperation, formula1, null);
//        return new HSSFConditionalFormattingRule(wb, rr);

        return null
    }

    /**
     * A factory method allowing to create a conditional formatting rule with a formula.<br></br>
     * 
     * The formatting rules are applied by Excel when the value of the formula not equal to 0.
     *
     *
     * TODO - formulas containing cell references are currently not parsed properly
     * @param formula - formula for the valued, compared with the cell
     */
    override fun createConditionalFormattingRule(formula: String?): HSSFConditionalFormattingRule? {
        val wb = _sheet.workbook
        //		CFRuleRecord rr = CFRuleRecord.create(_sheet, formula);
//		return new HSSFConditionalFormattingRule(wb, rr);
        return null
    }

    /**
     * Adds a copy of HSSFConditionalFormatting object to the sheet
     * 
     * This method could be used to copy HSSFConditionalFormatting object
     * from one sheet to another. For example:
     * <pre>
     * HSSFConditionalFormatting cf = sheet.getConditionalFormattingAt(index);
     * newSheet.addConditionalFormatting(cf);
    </pre> * 
     * 
     * @param cf HSSFConditionalFormatting object
     * @return index of the new Conditional Formatting object
     */
    fun addConditionalFormatting(cf: HSSFConditionalFormatting): Int {
        val cfraClone = cf.cFRecordsAggregate.cloneCFAggregate()

        return _conditionalFormattingTable!!.add(cfraClone)
    }

    override fun addConditionalFormatting(cf: ConditionalFormatting?): Int {
        return addConditionalFormatting(cf as HSSFConditionalFormatting)
    }

    @Deprecated("use <tt>CellRangeAddress</tt> instead of <tt>Region</tt>")
    fun addConditionalFormatting(
        regions: Array<Region?>,
        cfRules: Array<HSSFConditionalFormattingRule?>
    ): Int {
        return addConditionalFormatting(Region.convertRegionsToCellRanges(regions), cfRules)
    }

    /**
     * Allows to add a new Conditional Formatting set to the sheet.
     * 
     * @param regions - list of rectangular regions to apply conditional formatting rules
     * @param cfRules - set of up to three conditional formatting rules
     * 
     * @return index of the newly created Conditional Formatting object
     */
    fun addConditionalFormatting(
        regions: Array<HSSFCellRangeAddress>,
        cfRules: Array<HSSFConditionalFormattingRule?>
    ): Int {
        requireNotNull(regions) { "regions must not be null" }
        for (range in regions) range.validate(SpreadsheetVersion.EXCEL97)

        requireNotNull(cfRules) { "cfRules must not be null" }
        require(cfRules.size != 0) { "cfRules must not be empty" }
        require(cfRules.size <= 3) { "Number of rules must not exceed 3" }

        val rules = arrayOfNulls<CFRuleRecord>(cfRules.size)
        for (i in cfRules.indices) {
            rules[i] = cfRules[i]!!.cfRuleRecord
        }
        val cfra: CFRecordsAggregate = CFRecordsAggregate(regions, rules)
        return _conditionalFormattingTable!!.add(cfra)
    }

    override fun addConditionalFormatting(
        regions: Array<HSSFCellRangeAddress>,
        cfRules: Array<ConditionalFormattingRule?>
    ): Int {
        val hfRules = Array<HSSFConditionalFormattingRule?>(cfRules.size) { cfRules[it] as? HSSFConditionalFormattingRule }
        return addConditionalFormatting(regions, hfRules)
    }

    fun addConditionalFormatting(
        regions: Array<HSSFCellRangeAddress>,
        rule1: HSSFConditionalFormattingRule?
    ): Int {
        return addConditionalFormatting(
            regions,
            if (rule1 == null) arrayOfNulls<HSSFConditionalFormattingRule>(0) else arrayOf<HSSFConditionalFormattingRule?>(
                rule1
            )
        )
    }

    override fun addConditionalFormatting(
        regions: Array<HSSFCellRangeAddress>,
        rule1: ConditionalFormattingRule?
    ): Int {
        return addConditionalFormatting(regions, rule1 as HSSFConditionalFormattingRule?)
    }

    fun addConditionalFormatting(
        regions: Array<HSSFCellRangeAddress>,
        rule1: HSSFConditionalFormattingRule?,
        rule2: HSSFConditionalFormattingRule?
    ): Int {
        return addConditionalFormatting(
            regions,
            arrayOf<HSSFConditionalFormattingRule?>(
                rule1, rule2
            )
        )
    }

    override fun addConditionalFormatting(
        regions: Array<HSSFCellRangeAddress>,
        rule1: ConditionalFormattingRule?,
        rule2: ConditionalFormattingRule?
    ): Int {
        return addConditionalFormatting(
            regions,
            rule1 as HSSFConditionalFormattingRule?,
            rule2 as HSSFConditionalFormattingRule?
        )
    }

    /**
     * gets Conditional Formatting object at a particular index
     * 
     * @param index
     * of the Conditional Formatting object to fetch
     * @return Conditional Formatting object
     */
    override fun getConditionalFormattingAt(index: Int): HSSFConditionalFormatting? {
        val cf = _conditionalFormattingTable!!.get(index)
        if (cf == null) {
            return null
        }
        return HSSFConditionalFormatting(_sheet.workbook, cf)
    }

    /**
     * @return number of Conditional Formatting objects of the sheet
     */
    override fun getNumConditionalFormattings(): Int {
        return _conditionalFormattingTable!!.size()
    }

    /**
     * removes a Conditional Formatting object by index
     * @param index of a Conditional Formatting object to remove
     */
    override fun removeConditionalFormatting(index: Int) {
        _conditionalFormattingTable!!.remove(index)
    }
}
