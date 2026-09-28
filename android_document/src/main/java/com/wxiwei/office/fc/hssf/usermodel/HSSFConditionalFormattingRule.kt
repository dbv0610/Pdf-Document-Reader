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

import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.record.CFRuleRecord
import com.wxiwei.office.fc.hssf.record.cf.BorderFormatting
import com.wxiwei.office.fc.hssf.record.cf.FontFormatting
import com.wxiwei.office.fc.hssf.record.cf.PatternFormatting
import com.wxiwei.office.fc.ss.usermodel.ConditionalFormattingRule


/**
 * 
 * High level representation of Conditional Formatting Rule.
 * It allows to specify formula based conditions for the Conditional Formatting
 * and the formatting settings such as font, border and pattern.
 * 
 * @author Dmitriy Kumshayev
 */
class HSSFConditionalFormattingRule internal constructor(
    pWorkbook: HSSFWorkbook?,
    pRuleRecord: CFRuleRecord?
) : ConditionalFormattingRule {
    val cfRuleRecord: CFRuleRecord
    private val workbook: HSSFWorkbook

    init {
        requireNotNull(pWorkbook) { "pWorkbook must not be null" }
        requireNotNull(pRuleRecord) { "pRuleRecord must not be null" }
        workbook = pWorkbook
        cfRuleRecord = pRuleRecord
    }

    private fun getFontFormatting(create: Boolean): HSSFFontFormatting? {
        var fontFormatting = cfRuleRecord.fontFormatting
        if (fontFormatting != null) {
            cfRuleRecord.fontFormatting = fontFormatting
            return HSSFFontFormatting(cfRuleRecord)
        } else if (create) {
            fontFormatting = FontFormatting()
            cfRuleRecord.fontFormatting = fontFormatting
            return HSSFFontFormatting(cfRuleRecord)
        } else {
            return null
        }
    }

    /**
     * @return - font formatting object  if defined,  `null` otherwise
     */
    override fun getFontFormatting(): HSSFFontFormatting? {
        return getFontFormatting(false)
    }

    /**
     * create a new font formatting structure if it does not exist,
     * otherwise just return existing object.
     * @return - font formatting object, never returns `null`.
     */
    override fun createFontFormatting(): HSSFFontFormatting? {
        return getFontFormatting(true)
    }

    private fun getBorderFormatting(create: Boolean): HSSFBorderFormatting? {
        var borderFormatting = cfRuleRecord.borderFormatting
        if (borderFormatting != null) {
            cfRuleRecord.borderFormatting = borderFormatting
            return HSSFBorderFormatting(cfRuleRecord)
        } else if (create) {
            borderFormatting = BorderFormatting()
            cfRuleRecord.borderFormatting = borderFormatting
            return HSSFBorderFormatting(cfRuleRecord)
        } else {
            return null
        }
    }

    /**
     * @return - border formatting object  if defined,  `null` otherwise
     */
    override fun getBorderFormatting(): HSSFBorderFormatting? {
        return getBorderFormatting(false)
    }

    /**
     * create a new border formatting structure if it does not exist,
     * otherwise just return existing object.
     * @return - border formatting object, never returns `null`.
     */
    override fun createBorderFormatting(): HSSFBorderFormatting? {
        return getBorderFormatting(true)
    }

    private fun getPatternFormatting(create: Boolean): HSSFPatternFormatting? {
        var patternFormatting = cfRuleRecord.patternFormatting
        if (patternFormatting != null) {
            cfRuleRecord.patternFormatting = patternFormatting
            return HSSFPatternFormatting(cfRuleRecord)
        } else if (create) {
            patternFormatting = PatternFormatting()
            cfRuleRecord.patternFormatting = patternFormatting
            return HSSFPatternFormatting(cfRuleRecord)
        } else {
            return null
        }
    }

    /**
     * @return - pattern formatting object  if defined, `null` otherwise
     */
    override fun getPatternFormatting(): HSSFPatternFormatting? {
        return getPatternFormatting(false)
    }

    /**
     * create a new pattern formatting structure if it does not exist,
     * otherwise just return existing object.
     * @return - pattern formatting object, never returns `null`.
     */
    override fun createPatternFormatting(): HSSFPatternFormatting? {
        return getPatternFormatting(true)
    }

    /**
     * @return -  the conditiontype for the cfrule
     */
    override fun getConditionType(): Byte {
        return cfRuleRecord.conditionType
    }

    /**
     * @return - the comparisionoperatation for the cfrule
     */
    override fun getComparisonOperation(): Byte {
        return cfRuleRecord.comparisonOperation
    }

    override fun getFormula1(): String? {
        return toFormulaString(cfRuleRecord.parsedExpression1)
    }

    override fun getFormula2(): String? {
        val conditionType = cfRuleRecord.conditionType
        if (conditionType == CELL_COMPARISON) {
            val comparisonOperation = cfRuleRecord.comparisonOperation
            when (comparisonOperation) {
                CFRuleRecord.ComparisonOperator.BETWEEN, CFRuleRecord.ComparisonOperator.NOT_BETWEEN -> return toFormulaString(
                    cfRuleRecord.parsedExpression2
                )
            }
        }
        return null
    }

    private fun toFormulaString(parsedExpression: Array<Ptg?>?): String? {
//		if(parsedExpression ==null) {
//			return null;
//		}
//		return HSSFFormulaParser.toFormulaString(workbook, parsedExpression);
        return null
    }

    companion object {
        private val CELL_COMPARISON = CFRuleRecord.CONDITION_TYPE_CELL_VALUE_IS
    }
}
