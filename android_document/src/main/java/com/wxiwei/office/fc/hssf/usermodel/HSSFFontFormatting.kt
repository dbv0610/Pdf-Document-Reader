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
import com.wxiwei.office.fc.hssf.record.cf.FontFormatting


/**
 * High level representation for Font Formatting component
 * of Conditional Formatting settings
 * 
 * @author Dmitriy Kumshayev
 */
class HSSFFontFormatting(cfRuleRecord: CFRuleRecord) :
    com.wxiwei.office.fc.ss.usermodel.FontFormatting {
    protected val fontFormattingBlock: FontFormatting?

    init {
        this.fontFormattingBlock = cfRuleRecord.fontFormatting
    }

    /**
     * get the type of super or subscript for the font
     * 
     * @return super or subscript option
     * @see .SS_NONE
     * 
     * @see .SS_SUPER
     * 
     * @see .SS_SUB
     */
    override fun getEscapementType(): Short {
        return fontFormattingBlock!!.escapementType
    }

    /**
     * @return font color index
     */
    override fun getFontColorIndex(): Short {
        return fontFormattingBlock!!.fontColorIndex
    }

    /**
     * gets the height of the font in 1/20th point units
     * 
     * @return fontheight (in points/20); or -1 if not modified
     */
    override fun getFontHeight(): Int {
        return fontFormattingBlock!!.fontHeight
    }

    val fontWeight: Short
        /**
         * get the font weight for this font (100-1000dec or 0x64-0x3e8).  Default is
         * 0x190 for normal and 0x2bc for bold
         * 
         * @return bw - a number between 100-1000 for the fonts "boldness"
         */
        get() = fontFormattingBlock!!.fontWeight

    protected val rawRecord: ByteArray
        /**
         * @see FontFormatting.getRawRecord
         */
        get() = fontFormattingBlock!!.rawRecord

    /**
     * get the type of underlining for the font
     * 
     * @return font underlining type
     * 
     * @see .U_NONE
     * 
     * @see .U_SINGLE
     * 
     * @see .U_DOUBLE
     * 
     * @see .U_SINGLE_ACCOUNTING
     * 
     * @see .U_DOUBLE_ACCOUNTING
     */
    override fun getUnderlineType(): Short {
        return fontFormattingBlock!!.underlineType
    }

    /**
     * get whether the font weight is set to bold or not
     * 
     * @return bold - whether the font is bold or not
     */
    override fun isBold(): Boolean {
        return fontFormattingBlock!!.isFontWeightModified && fontFormattingBlock.isBold
    }

    var isEscapementTypeModified: Boolean
        /**
         * @return true if escapement type was modified from default
         */
        get() = fontFormattingBlock!!.isEscapementTypeModified
        /**
         * @param modified
         * @see FontFormatting.setEscapementTypeModified
         */
        set(modified) {
            fontFormattingBlock!!.isEscapementTypeModified = modified
        }

    var isFontCancellationModified: Boolean
        /**
         * @return true if font cancellation was modified from default
         */
        get() = fontFormattingBlock!!.isFontCancellationModified
        /**
         * @param modified
         * @see FontFormatting.setFontCancellationModified
         */
        set(modified) {
            fontFormattingBlock!!.isFontCancellationModified = modified
        }

    var isFontOutlineModified: Boolean
        /**
         * @return true if font outline type was modified from default
         */
        get() = fontFormattingBlock!!.isFontOutlineModified
        /**
         * @param modified
         * @see FontFormatting.setFontOutlineModified
         */
        set(modified) {
            fontFormattingBlock!!.isFontOutlineModified = modified
        }

    var isFontShadowModified: Boolean
        /**
         * @return true if font shadow type was modified from default
         */
        get() = fontFormattingBlock!!.isFontShadowModified
        /**
         * @param modified
         * @see FontFormatting.setFontShadowModified
         */
        set(modified) {
            fontFormattingBlock!!.isFontShadowModified = modified
        }

    var isFontStyleModified: Boolean
        /**
         * @return true if font style was modified from default
         */
        get() = fontFormattingBlock!!.isFontStyleModified
        /**
         * @param modified
         * @see FontFormatting.setFontStyleModified
         */
        set(modified) {
            fontFormattingBlock!!.isFontStyleModified = modified
        }

    /**
     * @return true if font style was set to *italic*
     */
    override fun isItalic(): Boolean {
        return fontFormattingBlock!!.isFontStyleModified && fontFormattingBlock.isItalic
    }

    val isOutlineOn: Boolean
        /**
         * @return true if font outline is on
         */
        get() = fontFormattingBlock!!.isFontOutlineModified && fontFormattingBlock.isOutlineOn

    val isShadowOn: Boolean
        /**
         * @return true if font shadow is on
         */
        get() = fontFormattingBlock!!.isFontOutlineModified && fontFormattingBlock.isShadowOn

    val isStruckout: Boolean
        /**
         * @return true if font strikeout is on
         */
        get() = fontFormattingBlock!!.isFontCancellationModified && fontFormattingBlock.isStruckout

    var isUnderlineTypeModified: Boolean
        /**
         * @return true if font underline type was modified from default
         */
        get() = fontFormattingBlock!!.isUnderlineTypeModified
        /**
         * @param modified
         * @see FontFormatting.setUnderlineTypeModified
         */
        set(modified) {
            fontFormattingBlock!!.isUnderlineTypeModified = modified
        }

    val isFontWeightModified: Boolean
        /**
         * @return true if font weight was modified from default
         */
        get() = fontFormattingBlock!!.isFontWeightModified

    /**
     * set font style options.
     * 
     * @param italic - if true, set posture style to italic, otherwise to normal
     * @param bold if true, set font weight to bold, otherwise to normal
     */
    override fun setFontStyle(italic: Boolean, bold: Boolean) {
        val modified = italic || bold
        fontFormattingBlock!!.isItalic = italic
        fontFormattingBlock.isBold = bold
        fontFormattingBlock.isFontStyleModified = modified
        fontFormattingBlock.setFontWieghtModified(modified)
    }

    /**
     * set font style options to default values (non-italic, non-bold)
     */
    override fun resetFontStyle() {
        setFontStyle(false, false)
    }

    /**
     * set the escapement type for the font
     * 
     * @param escapementType  super or subscript option
     * @see .SS_NONE
     * 
     * @see .SS_SUPER
     * 
     * @see .SS_SUB
     */
    override fun setEscapementType(escapementType: Short) {
        when (escapementType) {
            com.wxiwei.office.fc.ss.usermodel.FontFormatting.SS_SUB, com.wxiwei.office.fc.ss.usermodel.FontFormatting.SS_SUPER -> {
                fontFormattingBlock!!.escapementType = escapementType
                fontFormattingBlock.isEscapementTypeModified = true
            }

            com.wxiwei.office.fc.ss.usermodel.FontFormatting.SS_NONE -> {
                fontFormattingBlock!!.escapementType = escapementType
                fontFormattingBlock.isEscapementTypeModified = false
            }

            else -> {}
        }
    }

    /**
     * @param fci
     * @see FontFormatting.setFontColorIndex
     */
    override fun setFontColorIndex(fci: Short) {
        fontFormattingBlock!!.fontColorIndex = fci
    }

    /**
     * @param height
     * @see FontFormatting.setFontHeight
     */
    override fun setFontHeight(height: Int) {
        fontFormattingBlock!!.fontHeight = height
    }

    /**
     * @param on
     * @see FontFormatting.setOutline
     */
    fun setOutline(on: Boolean) {
        fontFormattingBlock!!.setOutline(on)
        fontFormattingBlock.isFontOutlineModified = on
    }

    /**
     * @param on
     * @see FontFormatting.setShadow
     */
    fun setShadow(on: Boolean) {
        fontFormattingBlock!!.setShadow(on)
        fontFormattingBlock.isFontShadowModified = on
    }

    /**
     * @param strike
     * @see FontFormatting.setStrikeout
     */
    fun setStrikeout(strike: Boolean) {
        fontFormattingBlock!!.setStrikeout(strike)
        fontFormattingBlock.isFontCancellationModified = strike
    }

    /**
     * set the type of underlining type for the font
     * 
     * @param underlineType  super or subscript option
     * 
     * @see .U_NONE
     * 
     * @see .U_SINGLE
     * 
     * @see .U_DOUBLE
     * 
     * @see .U_SINGLE_ACCOUNTING
     * 
     * @see .U_DOUBLE_ACCOUNTING
     */
    override fun setUnderlineType(underlineType: Short) {
        when (underlineType.toByte()) {
            U_SINGLE, U_DOUBLE, U_SINGLE_ACCOUNTING, U_DOUBLE_ACCOUNTING -> {
                fontFormattingBlock!!.underlineType = underlineType
                this.isUnderlineTypeModified = true
            }

            U_NONE -> {
                fontFormattingBlock!!.underlineType = underlineType
                this.isUnderlineTypeModified = false
            }

            else -> {}
        }
    }

    companion object {
        /** Underline type - None  */
        val U_NONE: Byte = FontFormatting.U_NONE

        /** Underline type - Single  */
        val U_SINGLE: Byte = FontFormatting.U_SINGLE

        /** Underline type - Double  */
        val U_DOUBLE: Byte = FontFormatting.U_DOUBLE

        /**  Underline type - Single Accounting  */
        val U_SINGLE_ACCOUNTING: Byte = FontFormatting.U_SINGLE_ACCOUNTING

        /** Underline type - Double Accounting  */
        val U_DOUBLE_ACCOUNTING: Byte = FontFormatting.U_DOUBLE_ACCOUNTING
    }
}
