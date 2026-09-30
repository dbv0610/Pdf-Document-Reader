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
package com.wxiwei.office.fc.hssf.record

import com.wxiwei.office.fc.hssf.formula.Formula
import com.wxiwei.office.fc.hssf.formula.Formula.Companion.getTokens
import com.wxiwei.office.fc.hssf.formula.Formula.Companion.read
import com.wxiwei.office.fc.hssf.formula.FormulaType
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.model.HSSFFormulaParser.parse
import com.wxiwei.office.fc.hssf.record.cf.BorderFormatting
import com.wxiwei.office.fc.hssf.record.cf.FontFormatting
import com.wxiwei.office.fc.hssf.record.cf.PatternFormatting
import com.wxiwei.office.fc.util.BitField
import com.wxiwei.office.fc.util.BitFieldFactory.Companion.getInstance
import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.ss.model.XLSModel.ASheet
import com.wxiwei.office.ss.model.XLSModel.AWorkbook


/**
 * Conditional Formatting Rule Record (0x01B1).<br></br>
 * 
 * @author Dmitriy Kumshayev
 */
class CFRuleRecord : StandardRecord {
    object ComparisonOperator {
        const val NO_COMPARISON: Byte = 0
        const val BETWEEN: Byte = 1
        const val NOT_BETWEEN: Byte = 2
        const val EQUAL: Byte = 3
        const val NOT_EQUAL: Byte = 4
        const val GT: Byte = 5
        const val LT: Byte = 6
        const val GE: Byte = 7
        const val LE: Byte = 8
    }

    val conditionType: Byte
    var comparisonOperation: Byte

    /**
     * get the option flags
     * 
     * @return bit mask
     */
    var options: Int = 0
        private set

    private var field_6_not_used: Short

    private var _fontFormatting: FontFormatting? = null

    private var _borderFormatting: BorderFormatting? = null

    private var _patternFormatting: PatternFormatting? = null

    private var field_17_formula1: Formula
    private var field_18_formula2: Formula

    /** Creates new CFRuleRecord  */
    private constructor(conditionType: Byte, comparisonOperation: Byte) {
        this.conditionType = conditionType
        this.comparisonOperation = comparisonOperation

        // Set modification flags to 1: by default options are not modified
        this.options = modificationBits.setValue(this.options, -1)
        // Set formatting block flags to 0 (no formatting blocks)
        this.options = fmtBlockBits.setValue(this.options, 0)
        this.options = undocumented.clear(this.options)

        field_6_not_used =
            0x8002.toShort() // Excel seems to write this value, but it doesn't seem to care what it reads
        _fontFormatting = null
        _borderFormatting = null
        _patternFormatting = null
        field_17_formula1 = Formula.create(Ptg.EMPTY_PTG_ARRAY)!!
        field_18_formula2 = Formula.create(Ptg.EMPTY_PTG_ARRAY)!!
    }

    private constructor(
        conditionType: Byte,
        comparisonOperation: Byte,
        formula1: Array<Ptg?>?,
        formula2: Array<Ptg?>?
    ) : this(conditionType, comparisonOperation) {
        field_17_formula1 = Formula.create(formula1)!!
        field_18_formula2 = Formula.create(formula2)!!
    }

    constructor(`in`: RecordInputStream) {
        this.conditionType = `in`.readByte()
        this.comparisonOperation = `in`.readByte()
        val field_3_formula1_len = `in`.readUShort()
        val field_4_formula2_len = `in`.readUShort()
        this.options = `in`.readInt()
        field_6_not_used = `in`.readShort()

        if (containsFontFormattingBlock()) {
            _fontFormatting = FontFormatting(`in`)
        }

        if (containsBorderFormattingBlock()) {
            _borderFormatting = BorderFormatting(`in`)
        }

        if (containsPatternFormattingBlock()) {
            _patternFormatting = PatternFormatting(`in`)
        }

        // "You may not use unions, intersections or array constants in Conditional Formatting criteria"
        field_17_formula1 = read(field_3_formula1_len, `in`)
        field_18_formula2 = read(field_4_formula2_len, `in`)
    }

    fun containsFontFormattingBlock(): Boolean {
        return getOptionFlag(font)
    }

    var fontFormatting: FontFormatting?
        get() {
            if (containsFontFormattingBlock()) {
                return _fontFormatting
            }
            return null
        }
        set(fontFormatting) {
            _fontFormatting = fontFormatting
            setOptionFlag(fontFormatting != null, font)
        }

    fun containsAlignFormattingBlock(): Boolean {
        return getOptionFlag(align)
    }

    fun setAlignFormattingUnchanged() {
        setOptionFlag(false, align)
    }

    fun containsBorderFormattingBlock(): Boolean {
        return getOptionFlag(bord)
    }

    var borderFormatting: BorderFormatting?
        get() {
            if (containsBorderFormattingBlock()) {
                return _borderFormatting
            }
            return null
        }
        set(borderFormatting) {
            _borderFormatting = borderFormatting
            setOptionFlag(borderFormatting != null, bord)
        }

    fun containsPatternFormattingBlock(): Boolean {
        return getOptionFlag(patt)
    }

    var patternFormatting: PatternFormatting?
        get() {
            if (containsPatternFormattingBlock()) {
                return _patternFormatting
            }
            return null
        }
        set(patternFormatting) {
            _patternFormatting = patternFormatting
            setOptionFlag(patternFormatting != null, patt)
        }

    fun containsProtectionFormattingBlock(): Boolean {
        return getOptionFlag(prot)
    }

    fun setProtectionFormattingUnchanged() {
        setOptionFlag(false, prot)
    }


    private fun isModified(field: BitField): Boolean {
        return !field.isSet(this.options)
    }

    private fun setModified(modified: Boolean, field: BitField) {
        this.options = field.setBoolean(this.options, !modified)
    }

    var isLeftBorderModified: Boolean
        get() = isModified(bordLeft)
        set(modified) {
            setModified(modified, bordLeft)
        }

    var isRightBorderModified: Boolean
        get() = isModified(bordRight)
        set(modified) {
            setModified(modified, bordRight)
        }

    var isTopBorderModified: Boolean
        get() = isModified(bordTop)
        set(modified) {
            setModified(modified, bordTop)
        }

    var isBottomBorderModified: Boolean
        get() = isModified(bordBot)
        set(modified) {
            setModified(modified, bordBot)
        }

    var isTopLeftBottomRightBorderModified: Boolean
        get() = isModified(bordTlBr)
        set(modified) {
            setModified(modified, bordTlBr)
        }

    var isBottomLeftTopRightBorderModified: Boolean
        get() = isModified(bordBlTr)
        set(modified) {
            setModified(modified, bordBlTr)
        }

    var isPatternStyleModified: Boolean
        get() = isModified(pattStyle)
        set(modified) {
            setModified(modified, pattStyle)
        }

    var isPatternColorModified: Boolean
        get() = isModified(pattCol)
        set(modified) {
            setModified(modified, pattCol)
        }

    var isPatternBackgroundColorModified: Boolean
        get() = isModified(pattBgCol)
        set(modified) {
            setModified(modified, pattBgCol)
        }

    private fun getOptionFlag(field: BitField): Boolean {
        return field.isSet(this.options)
    }

    private fun setOptionFlag(flag: Boolean, field: BitField) {
        this.options = field.setBoolean(this.options, flag)
    }

    var parsedExpression1: Array<Ptg?>?
        /**
         * get the stack of the 1st expression as a list
         * 
         * @return list of tokens (casts stack to a list and returns it!)
         * this method can return null is we are unable to create Ptgs from
         * existing excel file
         * callers should check for null!
         */
        get() = field_17_formula1.tokens
        set(ptgs) {
            field_17_formula1 = Formula.create(ptgs)!!
        }

    var parsedExpression2: Array<Ptg?>?
        /**
         * get the stack of the 2nd expression as a list
         * 
         * @return array of [Ptg]s, possibly `null`
         */
        get() = getTokens(field_18_formula2)
        set(ptgs) {
            field_18_formula2 = Formula.create(ptgs)!!
        }

    override fun getSid(): Short {
        return Companion.sid
    }

    /**
     * called by the class that is responsible for writing this sucker.
     * Subclasses should implement this so that their data is passed back in a
     * byte array.
     * 
     * @param out the stream to write to
     */
    public override fun serialize(out: LittleEndianOutput) {
        val formula1Len: Int = getFormulaSize(field_17_formula1)
        val formula2Len: Int = getFormulaSize(field_18_formula2)

        out.writeByte(conditionType.toInt())
        out.writeByte(comparisonOperation.toInt())
        out.writeShort(formula1Len)
        out.writeShort(formula2Len)
        out.writeInt(this.options)
        out.writeShort(field_6_not_used.toInt())

        if (containsFontFormattingBlock()) {
            val fontFormattingRawRecord = _fontFormatting!!.rawRecord
            out.write(fontFormattingRawRecord)
        }

        if (containsBorderFormattingBlock()) {
            _borderFormatting!!.serialize(out)
        }

        if (containsPatternFormattingBlock()) {
            _patternFormatting!!.serialize(out)
        }

        field_17_formula1.serializeTokens(out)
        field_18_formula2.serializeTokens(out)
    }

    override fun getDataSize(): Int {
        return 12 +
                (if (containsFontFormattingBlock()) _fontFormatting!!.rawRecord.size else 0) +
                (if (containsBorderFormattingBlock()) 8 else 0) +
                (if (containsPatternFormattingBlock()) 4 else 0) +
                getFormulaSize(field_17_formula1) +
                getFormulaSize(field_18_formula2)
    }


    override fun toString(): String {
        val buffer = StringBuffer()
        buffer.append("[CFRULE]\n")
        buffer.append("    .condition_type   =" + this.conditionType)
        buffer.append("    OPTION FLAGS=0x" + Integer.toHexString(this.options))
        if (false) {
            if (containsFontFormattingBlock()) {
                buffer.append(_fontFormatting.toString())
            }
            if (containsBorderFormattingBlock()) {
                buffer.append(_borderFormatting.toString())
            }
            if (containsPatternFormattingBlock()) {
                buffer.append(_patternFormatting.toString())
            }
            buffer.append("[/CFRULE]\n")
        }
        return buffer.toString()
    }

    override fun clone(): Any {
        val rec = CFRuleRecord(this.conditionType, this.comparisonOperation)
        rec.options = this.options
        rec.field_6_not_used = field_6_not_used
        if (containsFontFormattingBlock()) {
            rec._fontFormatting = _fontFormatting!!.clone() as FontFormatting
        }
        if (containsBorderFormattingBlock()) {
            rec._borderFormatting = _borderFormatting!!.clone() as BorderFormatting
        }
        if (containsPatternFormattingBlock()) {
            rec._patternFormatting = _patternFormatting!!.clone() as PatternFormatting
        }
        rec.field_17_formula1 = field_17_formula1.copy()
        rec.field_18_formula2 = field_17_formula1.copy()

        return rec
    }

    companion object {
        const val sid: Short = 0x01B1

        const val CONDITION_TYPE_CELL_VALUE_IS: Byte = 1
        const val CONDITION_TYPE_FORMULA: Byte = 2

        private val modificationBits: BitField = bf(0x003FFFFF) // Bits: font,align,bord,patt,prot
        private val alignHor: BitField = bf(0x00000001) // 0 = Horizontal alignment modified
        private val alignVer: BitField = bf(0x00000002) // 0 = Vertical alignment modified
        private val alignWrap: BitField = bf(0x00000004) // 0 = Text wrapped flag modified
        private val alignRot: BitField = bf(0x00000008) // 0 = Text rotation modified
        private val alignJustLast: BitField = bf(0x00000010) // 0 = Justify last line flag modified
        private val alignIndent: BitField = bf(0x00000020) // 0 = Indentation modified
        private val alignShrin: BitField = bf(0x00000040) // 0 = Shrink to fit flag modified
        private val notUsed1: BitField = bf(0x00000080) // Always 1
        private val protLocked: BitField = bf(0x00000100) // 0 = Cell locked flag modified
        private val protHidden: BitField = bf(0x00000200) // 0 = Cell hidden flag modified
        private val bordLeft: BitField = bf(0x00000400) // 0 = Left border style and colour modified
        private val bordRight: BitField =
            bf(0x00000800) // 0 = Right border style and colour modified
        private val bordTop: BitField = bf(0x00001000) // 0 = Top border style and colour modified
        private val bordBot: BitField =
            bf(0x00002000) // 0 = Bottom border style and colour modified
        private val bordTlBr: BitField =
            bf(0x00004000) // 0 = Top-left to bottom-right border flag modified
        private val bordBlTr: BitField =
            bf(0x00008000) // 0 = Bottom-left to top-right border flag modified
        private val pattStyle: BitField = bf(0x00010000) // 0 = Pattern style modified
        private val pattCol: BitField = bf(0x00020000) // 0 = Pattern colour modified
        private val pattBgCol: BitField = bf(0x00040000) // 0 = Pattern background colour modified
        private val notUsed2: BitField = bf(0x00380000) // Always 111
        private val undocumented: BitField = bf(0x03C00000) // Undocumented bits
        private val fmtBlockBits: BitField = bf(0x7C000000) // Bits: font,align,bord,patt,prot
        private val font: BitField = bf(0x04000000) // 1 = Record contains font formatting block
        private val align: BitField =
            bf(0x08000000) // 1 = Record contains alignment formatting block
        private val bord: BitField = bf(0x10000000) // 1 = Record contains border formatting block
        private val patt: BitField = bf(0x20000000) // 1 = Record contains pattern formatting block
        private val prot: BitField =
            bf(0x40000000) // 1 = Record contains protection formatting block
        private val alignTextDir: BitField = bf(-0x80000000) // 0 = Text direction modified


        private fun bf(i: Int): BitField {
            return getInstance(i)
        }

        /**
         * Creates a new comparison operation rule
         */
        fun create(sheet: ASheet, formulaText: String?): CFRuleRecord {
            val formula1: Array<Ptg?>? = parseFormula(formulaText, sheet)
            return CFRuleRecord(
                CONDITION_TYPE_FORMULA, ComparisonOperator.NO_COMPARISON,
                formula1, null
            )
        }

        /**
         * Creates a new comparison operation rule
         */
        fun create(
            sheet: ASheet, comparisonOperation: Byte,
            formulaText1: String?, formulaText2: String?
        ): CFRuleRecord {
            val formula1: Array<Ptg?>? = parseFormula(formulaText1, sheet)
            val formula2: Array<Ptg?>? = parseFormula(formulaText2, sheet)
            return CFRuleRecord(
                CONDITION_TYPE_CELL_VALUE_IS,
                comparisonOperation,
                formula1,
                formula2
            )
        }

        /**
         * @param ptgs must not be `null`
         * @return encoded size of the formula tokens (does not include 2 bytes for ushort length)
         */
        private fun getFormulaSize(formula: Formula): Int {
            return formula.encodedTokenSize
        }

        /**
         * TODO - parse conditional format formulas properly i.e. produce tRefN and tAreaN instead of tRef and tArea
         * this call will produce the wrong results if the formula contains any cell references
         * One approach might be to apply the inverse of SharedFormulaRecord.convertSharedFormulas(Stack, int, int)
         * Note - two extra parameters (rowIx & colIx) will be required. They probably come from one of the Region objects.
         * 
         * @return `null` if <tt>formula</tt> was null.
         */
        private fun parseFormula(formula: String?, sheet: ASheet): Array<Ptg?>? {
            if (formula == null) {
                return null
            }
            //        int sheetIndex = sheet.getWorkbook().getSheetIndex(sheet);
//        return HSSFFormulaParser.parse(formula, sheet.getWorkbook(), FormulaType.CELL, sheetIndex);
            val sheetIndex = (sheet.getWorkbook() as AWorkbook).getSheetIndex(sheet)
            return parse(formula, sheet.getWorkbook() as AWorkbook?, FormulaType.CELL, sheetIndex)
        }
    }
}
