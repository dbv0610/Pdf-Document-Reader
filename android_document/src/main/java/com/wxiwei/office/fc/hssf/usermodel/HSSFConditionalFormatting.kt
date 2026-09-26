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
import com.wxiwei.office.fc.ss.usermodel.ConditionalFormatting
import com.wxiwei.office.fc.ss.usermodel.ConditionalFormattingRule
import com.wxiwei.office.fc.ss.util.HSSFCellRangeAddress
import com.wxiwei.office.fc.ss.util.Region


/**
 * HSSFConditionalFormatting class encapsulates all settings of Conditional Formatting.
 * 
 * The class can be used
 * 
 * <UL>
 * <LI>
 * to make a copy HSSFConditionalFormatting settings.
</LI> * 
 * 
 * 
 * For example:
 * <PRE>
 * HSSFConditionalFormatting cf = sheet.getConditionalFormattingAt(index);
 * newSheet.addConditionalFormatting(cf);
</PRE> * 
 * 
 * <LI>
 * or to modify existing Conditional Formatting settings (formatting regions and/or rules).
</LI> * 
</UL> * 
 * 
 * Use [HSSFSheet.getSheetConditionalFormatting] to get access to an instance of this class.
 * <P>
 * To create a new Conditional Formatting set use the following approach:
 * 
</P> * <PRE>
 * 
 * // Define a Conditional Formatting rule, which triggers formatting
 * // when cell's value is greater or equal than 100.0 and
 * // applies patternFormatting defined below.
 * HSSFConditionalFormattingRule rule = sheet.createConditionalFormattingRule(
 * ComparisonOperator.GE,
 * "100.0", // 1st formula
 * null     // 2nd formula is not used for comparison operator GE
 * );
 * 
 * // Create pattern with red background
 * HSSFPatternFormatting patternFmt = rule.cretePatternFormatting();
 * patternFormatting.setFillBackgroundColor(HSSFColor.RED.index);
 * 
 * // Define a region containing first column
 * Region [] regions =
 * {
 * new Region(1,(short)1,-1,(short)1)
 * };
 * 
 * // Apply Conditional Formatting rule defined above to the regions
 * sheet.addConditionalFormatting(regions, rule);
</PRE> * 
 * 
 * @author Dmitriy Kumshayev
 */
class HSSFConditionalFormatting internal constructor(
    workbook: HSSFWorkbook,
    cfAggregate: CFRecordsAggregate
) : ConditionalFormatting {
    private val _workbook: HSSFWorkbook
    val cFRecordsAggregate: CFRecordsAggregate

    init {
        requireNotNull(workbook) { "workbook must not be null" }
        requireNotNull(cfAggregate) { "cfAggregate must not be null" }
        _workbook = workbook
        this.cFRecordsAggregate = cfAggregate
    }

    @get:Deprecated("(Aug-2008) use {@link HSSFConditionalFormatting#getFormattingRanges()}")
    val formattingRegions: Array<Region?>
        get() {
            val cellRanges = getFormattingRanges()
            return Region.convertCellRangesToRegions(cellRanges)
        }

    /**
     * @return array of <tt>CellRangeAddress</tt>s. never `null`
     */
    override fun getFormattingRanges(): Array<HSSFCellRangeAddress?> {
        return cFRecordsAggregate.header!!.cellRanges as Array<HSSFCellRangeAddress?>
    }

    /**
     * Replaces an existing Conditional Formatting rule at position idx.
     * Excel allows to create up to 3 Conditional Formatting rules.
     * This method can be useful to modify existing  Conditional Formatting rules.
     * 
     * @param idx position of the rule. Should be between 0 and 2.
     * @param cfRule - Conditional Formatting rule
     */
    fun setRule(idx: Int, cfRule: HSSFConditionalFormattingRule) {
        cFRecordsAggregate.setRule(idx, cfRule.cfRuleRecord)
    }

    override fun setRule(idx: Int, cfRule: ConditionalFormattingRule?) {
        setRule(idx, cfRule as HSSFConditionalFormattingRule?)
    }

    /**
     * add a Conditional Formatting rule.
     * Excel allows to create up to 3 Conditional Formatting rules.
     * @param cfRule - Conditional Formatting rule
     */
    fun addRule(cfRule: HSSFConditionalFormattingRule) {
        cFRecordsAggregate.addRule(cfRule.cfRuleRecord)
    }

    override fun addRule(cfRule: ConditionalFormattingRule?) {
        addRule(cfRule as HSSFConditionalFormattingRule?)
    }

    /**
     * @return the Conditional Formatting rule at position idx.
     */
    override fun getRule(idx: Int): HSSFConditionalFormattingRule {
        val ruleRecord: CFRuleRecord? = cFRecordsAggregate.getRule(idx)
        return HSSFConditionalFormattingRule(_workbook, ruleRecord)
    }

    /**
     * @return number of Conditional Formatting rules.
     */
    override fun getNumberOfRules(): Int {
        return cFRecordsAggregate.numberOfRules
    }

    override fun toString(): String {
        return cFRecordsAggregate.toString()
    }
}
