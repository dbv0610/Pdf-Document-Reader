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
import com.wxiwei.office.fc.hssf.record.cf.BorderFormatting


/**
 * High level representation for Border Formatting component
 * of Conditional Formatting settings
 * 
 * @author Dmitriy Kumshayev
 */
class HSSFBorderFormatting(private val cfRuleRecord: CFRuleRecord) :
    com.wxiwei.office.fc.ss.usermodel.BorderFormatting {
    protected val borderFormattingBlock: BorderFormatting?

    init {
        this.borderFormattingBlock = cfRuleRecord.borderFormatting
    }

    override fun getBorderBottom(): Short {
        return borderFormattingBlock!!.borderBottom.toShort()
    }

    override fun getBorderDiagonal(): Short {
        return borderFormattingBlock!!.borderDiagonal.toShort()
    }

    override fun getBorderLeft(): Short {
        return borderFormattingBlock!!.borderLeft.toShort()
    }

    override fun getBorderRight(): Short {
        return borderFormattingBlock!!.borderRight.toShort()
    }

    override fun getBorderTop(): Short {
        return borderFormattingBlock!!.borderTop.toShort()
    }

    override fun getBottomBorderColor(): Short {
        return borderFormattingBlock!!.bottomBorderColor.toShort()
    }

    override fun getDiagonalBorderColor(): Short {
        return borderFormattingBlock!!.diagonalBorderColor.toShort()
    }

    override fun getLeftBorderColor(): Short {
        return borderFormattingBlock!!.leftBorderColor.toShort()
    }

    override fun getRightBorderColor(): Short {
        return borderFormattingBlock!!.rightBorderColor.toShort()
    }

    override fun getTopBorderColor(): Short {
        return borderFormattingBlock!!.topBorderColor.toShort()
    }

    var isBackwardDiagonalOn: Boolean
        get() = borderFormattingBlock!!.isBackwardDiagonalOn
        set(on) {
            borderFormattingBlock!!.isBackwardDiagonalOn = on
            if (on) {
                cfRuleRecord.isTopLeftBottomRightBorderModified = on
            }
        }

    var isForwardDiagonalOn: Boolean
        get() = borderFormattingBlock!!.isForwardDiagonalOn
        set(on) {
            borderFormattingBlock!!.isForwardDiagonalOn = on
            if (on) {
                cfRuleRecord.isBottomLeftTopRightBorderModified = on
            }
        }

    override fun setBorderBottom(border: Short) {
        borderFormattingBlock!!.borderBottom = border.toInt()
        if (border.toInt() != 0) {
            cfRuleRecord.isBottomBorderModified = true
        }
    }

    override fun setBorderDiagonal(border: Short) {
        borderFormattingBlock!!.borderDiagonal = border.toInt()
        if (border.toInt() != 0) {
            cfRuleRecord.isBottomLeftTopRightBorderModified = true
            cfRuleRecord.isTopLeftBottomRightBorderModified = true
        }
    }

    override fun setBorderLeft(border: Short) {
        borderFormattingBlock!!.borderLeft = border.toInt()
        if (border.toInt() != 0) {
            cfRuleRecord.isLeftBorderModified = true
        }
    }

    override fun setBorderRight(border: Short) {
        borderFormattingBlock!!.borderRight = border.toInt()
        if (border.toInt() != 0) {
            cfRuleRecord.isRightBorderModified = true
        }
    }

    override fun setBorderTop(border: Short) {
        borderFormattingBlock!!.borderTop = border.toInt()
        if (border.toInt() != 0) {
            cfRuleRecord.isTopBorderModified = true
        }
    }

    override fun setBottomBorderColor(color: Short) {
        borderFormattingBlock!!.bottomBorderColor = color.toInt()
        if (color.toInt() != 0) {
            cfRuleRecord.isBottomBorderModified = true
        }
    }

    override fun setDiagonalBorderColor(color: Short) {
        borderFormattingBlock!!.diagonalBorderColor = color.toInt()
        if (color.toInt() != 0) {
            cfRuleRecord.isBottomLeftTopRightBorderModified = true
            cfRuleRecord.isTopLeftBottomRightBorderModified = true
        }
    }

    override fun setLeftBorderColor(color: Short) {
        borderFormattingBlock!!.leftBorderColor = color.toInt()
        if (color.toInt() != 0) {
            cfRuleRecord.isLeftBorderModified = true
        }
    }

    override fun setRightBorderColor(color: Short) {
        borderFormattingBlock!!.rightBorderColor = color.toInt()
        if (color.toInt() != 0) {
            cfRuleRecord.isRightBorderModified = true
        }
    }

    override fun setTopBorderColor(color: Short) {
        borderFormattingBlock!!.topBorderColor = color.toInt()
        if (color.toInt() != 0) {
            cfRuleRecord.isTopBorderModified = true
        }
    }
}
