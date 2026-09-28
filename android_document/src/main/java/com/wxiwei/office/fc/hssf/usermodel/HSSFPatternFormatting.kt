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
import com.wxiwei.office.fc.hssf.record.cf.PatternFormatting


/**
 * High level representation for Conditional Formatting settings
 * 
 * @author Dmitriy Kumshayev
 */
class HSSFPatternFormatting(private val cfRuleRecord: CFRuleRecord) :
    com.wxiwei.office.fc.ss.usermodel.PatternFormatting {
    protected val patternFormattingBlock: PatternFormatting?

    init {
        this.patternFormattingBlock = cfRuleRecord.patternFormatting
    }

    /**
     * @see PatternFormatting.getFillBackgroundColor
     */
    override fun getFillBackgroundColor(): Short {
        return patternFormattingBlock!!.fillBackgroundColor.toShort()
    }

    /**
     * @see PatternFormatting.getFillForegroundColor
     */
    override fun getFillForegroundColor(): Short {
        return patternFormattingBlock!!.fillForegroundColor.toShort()
    }

    /**
     * @see PatternFormatting.getFillPattern
     */
    override fun getFillPattern(): Short {
        return patternFormattingBlock!!.fillPattern.toShort()
    }

    /**
     * @param bg
     * @see PatternFormatting.setFillBackgroundColor
     */
    override fun setFillBackgroundColor(bg: Short) {
        patternFormattingBlock!!.fillBackgroundColor = bg.toInt()
        if (bg.toInt() != 0) {
            cfRuleRecord.isPatternBackgroundColorModified = true
        }
    }

    /**
     * @param fg
     * @see PatternFormatting.setFillForegroundColor
     */
    override fun setFillForegroundColor(fg: Short) {
        patternFormattingBlock!!.fillForegroundColor = fg.toInt()
        if (fg.toInt() != 0) {
            cfRuleRecord.isPatternColorModified = true
        }
    }

    /**
     * @param fp
     * @see PatternFormatting.setFillPattern
     */
    override fun setFillPattern(fp: Short) {
        patternFormattingBlock!!.fillPattern = fp.toInt()
        if (fp.toInt() != 0) {
            cfRuleRecord.isPatternStyleModified = true
        }
    }
}
