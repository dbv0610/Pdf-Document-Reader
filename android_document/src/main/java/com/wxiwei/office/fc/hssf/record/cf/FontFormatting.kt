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
package com.wxiwei.office.fc.hssf.record.cf

import com.wxiwei.office.fc.hssf.record.RecordInputStream
import com.wxiwei.office.fc.util.BitField
import com.wxiwei.office.fc.util.BitFieldFactory.Companion.getInstance
import com.wxiwei.office.fc.util.LittleEndian.getInt
import com.wxiwei.office.fc.util.LittleEndian.getShort
import com.wxiwei.office.fc.util.LittleEndian.putInt
import com.wxiwei.office.fc.util.LittleEndian.putShort
import java.util.Locale


/**
 * Font Formatting Block of the Conditional Formatting Rule Record.
 * 
 * @author Dmitriy Kumshayev
 */
class FontFormatting
private constructor(val rawRecord: ByteArray) : Cloneable {
    constructor() : this(ByteArray(RAW_DATA_SIZE)) {
        this.fontHeight = -1
        this.isItalic = false
        setFontWieghtModified(false)
        setOutline(false)
        setShadow(false)
        setStrikeout(false)
        this.escapementType = 0.toShort()
        this.underlineType = 0.toByte().toShort()
        this.fontColorIndex = (-1).toShort()

        this.isFontStyleModified = false
        this.isFontOutlineModified = false
        this.isFontShadowModified = false
        this.isFontCancellationModified = false

        this.isEscapementTypeModified = false
        this.isUnderlineTypeModified = false

        setShort(OFFSET_FONT_NAME, 0)
        setInt(OFFSET_NOT_USED1, 0x00000001)
        setInt(OFFSET_NOT_USED2, 0x00000000)
        setInt(
            OFFSET_NOT_USED3,
            0x7FFFFFFF
        ) // for some reason Excel always writes  0x7FFFFFFF at this offset
        setShort(OFFSET_FONT_FORMATING_END, 0x0001)
    }

    /** Creates new FontFormatting  */
    constructor(`in`: RecordInputStream) : this(ByteArray(RAW_DATA_SIZE)) {
        for (i in rawRecord.indices) {
            this.rawRecord[i] = `in`.readByte()
        }
    }

    private fun getShort(offset: Int): Short {
        return getShort(this.rawRecord, offset)
    }

    private fun setShort(offset: Int, value: Int) {
        putShort(this.rawRecord, offset, value.toShort())
    }

    private fun getInt(offset: Int): Int {
        return getInt(this.rawRecord, offset)
    }

    private fun setInt(offset: Int, value: Int) {
        putInt(this.rawRecord, offset, value)
    }

    var fontHeight: Int
        /**
         * gets the height of the font in 1/20th point units
         * 
         * @return fontheight (in points/20); or -1 if not modified
         */
        get() = getInt(OFFSET_FONT_HEIGHT)
        /**
         * sets the height of the font in 1/20th point units
         * 
         * 
         * @param height  fontheight (in points/20); or -1 to preserve the cell font height
         */
        set(height) {
            setInt(
                OFFSET_FONT_HEIGHT,
                height
            )
        }

    private fun setFontOption(option: Boolean, field: BitField) {
        var options = getInt(OFFSET_FONT_OPTIONS)
        options = field.setBoolean(options, option)
        setInt(OFFSET_FONT_OPTIONS, options)
    }

    private fun getFontOption(field: BitField): Boolean {
        val options = getInt(OFFSET_FONT_OPTIONS)
        return field.isSet(options)
    }

    var isItalic: Boolean
        /**
         * get whether the font is to be italics or not
         * 
         * @return italics - whether the font is italics or not
         * @see .getFontOption
         */
        get() = getFontOption(posture)
        /**
         * set the font to be italics or not
         * 
         * @param italic - whether the font is italics or not
         * @see .setFontOption
         */
        set(italic) {
            setFontOption(
                italic,
                posture
            )
        }

    fun setOutline(on: Boolean) {
        setFontOption(on, outline)
    }

    val isOutlineOn: Boolean
        get() = getFontOption(outline)

    fun setShadow(on: Boolean) {
        setFontOption(on, shadow)
    }

    val isShadowOn: Boolean
        get() = getFontOption(shadow)

    /**
     * set the font to be stricken out or not
     * 
     * @param strike - whether the font is stricken out or not
     */
    fun setStrikeout(strike: Boolean) {
        setFontOption(strike, cancellation)
    }

    val isStruckout: Boolean
        /**
         * get whether the font is to be stricken out or not
         * 
         * @return strike - whether the font is stricken out or not
         * @see .getFontOption
         */
        get() = getFontOption(cancellation)

    var fontWeight: Short
        /**
         * get the font weight for this font (100-1000dec or 0x64-0x3e8).  Default is
         * 0x190 for normal and 0x2bc for bold
         * 
         * @return bw - a number between 100-1000 for the fonts "boldness"
         */
        get() = getShort(OFFSET_FONT_WEIGHT)
        /**
         * set the font weight (100-1000dec or 0x64-0x3e8).  Default is
         * 0x190 for normal and 0x2bc for bold
         * 
         * @param bw - a number between 100-1000 for the fonts "boldness"
         */
        private set(pbw) {
            var bw = pbw
            if (bw < 100) {
                bw = 100
            }
            if (bw > 1000) {
                bw = 1000
            }
            setShort(
                OFFSET_FONT_WEIGHT,
                bw.toInt()
            )
        }

    var isBold: Boolean
        /**
         * get whether the font weight is set to bold or not
         * 
         * @return bold - whether the font is bold or not
         */
        get() = this.fontWeight == FONT_WEIGHT_BOLD
        /**
         * set the font weight to bold (weight=700) or to normal(weight=400) boldness.
         * 
         * @param bold - set font weight to bold if true; to normal otherwise
         */
        set(bold) {
            this.fontWeight =
                if (bold) FONT_WEIGHT_BOLD else FONT_WEIGHT_NORMAL
        }

    var escapementType: Short
        /**
         * get the type of super or subscript for the font
         * 
         * @return super or subscript option
         * @see com.wxiwei.office.fc.hssf.usermodel.HSSFFontFormatting.SS_NONE
         * 
         * @see com.wxiwei.office.fc.hssf.usermodel.HSSFFontFormatting.SS_SUPER
         * 
         * @see com.wxiwei.office.fc.hssf.usermodel.HSSFFontFormatting.SS_SUB
         */
        get() = getShort(OFFSET_ESCAPEMENT_TYPE)
        /**
         * set the escapement type for the font
         * 
         * @param escapementType  super or subscript option
         * @see com.wxiwei.office.fc.hssf.usermodel.HSSFFontFormatting.SS_NONE
         * 
         * @see com.wxiwei.office.fc.hssf.usermodel.HSSFFontFormatting.SS_SUPER
         * 
         * @see com.wxiwei.office.fc.hssf.usermodel.HSSFFontFormatting.SS_SUB
         */
        set(escapementType) {
            setShort(
                OFFSET_ESCAPEMENT_TYPE,
                escapementType.toInt()
            )
        }

    var underlineType: Short
        /**
         * get the type of underlining for the font
         * 
         * @return font underlining type
         * 
         * @see com.wxiwei.office.fc.hssf.usermodel.HSSFFontFormatting.U_NONE
         * 
         * @see com.wxiwei.office.fc.hssf.usermodel.HSSFFontFormatting.U_SINGLE
         * 
         * @see com.wxiwei.office.fc.hssf.usermodel.HSSFFontFormatting.U_DOUBLE
         * 
         * @see com.wxiwei.office.fc.hssf.usermodel.HSSFFontFormatting.U_SINGLE_ACCOUNTING
         * 
         * @see com.wxiwei.office.fc.hssf.usermodel.HSSFFontFormatting.U_DOUBLE_ACCOUNTING
         */
        get() = getShort(OFFSET_UNDERLINE_TYPE)
        /**
         * set the type of underlining type for the font
         * 
         * @param underlineType underline option
         * 
         * @see com.wxiwei.office.fc.hssf.usermodel.HSSFFontFormatting.U_NONE
         * 
         * @see com.wxiwei.office.fc.hssf.usermodel.HSSFFontFormatting.U_SINGLE
         * 
         * @see com.wxiwei.office.fc.hssf.usermodel.HSSFFontFormatting.U_DOUBLE
         * 
         * @see com.wxiwei.office.fc.hssf.usermodel.HSSFFontFormatting.U_SINGLE_ACCOUNTING
         * 
         * @see com.wxiwei.office.fc.hssf.usermodel.HSSFFontFormatting.U_DOUBLE_ACCOUNTING
         */
        set(underlineType) {
            setShort(
                OFFSET_UNDERLINE_TYPE,
                underlineType.toInt()
            )
        }


    var fontColorIndex: Short
        get() = getInt(OFFSET_FONT_COLOR_INDEX).toShort()
        set(fci) {
            setInt(
                OFFSET_FONT_COLOR_INDEX,
                fci.toInt()
            )
        }

    private fun getOptionFlag(field: BitField): Boolean {
        val optionFlags = getInt(OFFSET_OPTION_FLAGS)
        val value = field.getValue(optionFlags)
        return if (value == 0) true else false
    }

    private fun setOptionFlag(modified: Boolean, field: BitField) {
        val value = if (modified) 0 else 1
        var optionFlags = getInt(OFFSET_OPTION_FLAGS)
        optionFlags = field.setValue(optionFlags, value)
        setInt(OFFSET_OPTION_FLAGS, optionFlags)
    }


    var isFontStyleModified: Boolean
        get() = getOptionFlag(styleModified)
        set(modified) {
            setOptionFlag(
                modified,
                styleModified
            )
        }


    var isFontOutlineModified: Boolean
        get() = getOptionFlag(outlineModified)
        set(modified) {
            setOptionFlag(
                modified,
                outlineModified
            )
        }

    var isFontShadowModified: Boolean
        get() = getOptionFlag(shadowModified)
        set(modified) {
            setOptionFlag(
                modified,
                shadowModified
            )
        }

    var isFontCancellationModified: Boolean
        get() = getOptionFlag(cancellationModified)
        set(modified) {
            setOptionFlag(
                modified,
                cancellationModified
            )
        }

    var isEscapementTypeModified: Boolean
        get() {
            val escapementModified =
                getInt(OFFSET_ESCAPEMENT_TYPE_MODIFIED)
            return escapementModified == 0
        }
        set(modified) {
            val value = if (modified) 0 else 1
            setInt(
                OFFSET_ESCAPEMENT_TYPE_MODIFIED,
                value
            )
        }

    var isUnderlineTypeModified: Boolean
        get() {
            val underlineModified =
                getInt(OFFSET_UNDERLINE_TYPE_MODIFIED)
            return underlineModified == 0
        }
        set(modified) {
            val value = if (modified) 0 else 1
            setInt(
                OFFSET_UNDERLINE_TYPE_MODIFIED,
                value
            )
        }

    fun setFontWieghtModified(modified: Boolean) {
        val value = if (modified) 0 else 1
        setInt(OFFSET_FONT_WEIGHT_MODIFIED, value)
    }

    val isFontWeightModified: Boolean
        get() {
            val fontStyleModified =
                getInt(OFFSET_FONT_WEIGHT_MODIFIED)
            return fontStyleModified == 0
        }

    override fun toString(): String {
        val buffer = StringBuffer()
        buffer.append("	[Font Formatting]\n")

        buffer.append("	.font height = ").append(this.fontHeight).append(" twips\n")

        if (this.isFontStyleModified) {
            buffer.append("	.font posture = ").append(if (this.isItalic) "Italic" else "Normal")
                .append("\n")
        } else {
            buffer.append("	.font posture = ]not modified]").append("\n")
        }

        if (this.isFontOutlineModified) {
            buffer.append("	.font outline = ").append(this.isOutlineOn).append("\n")
        } else {
            buffer.append("	.font outline is not modified\n")
        }

        if (this.isFontShadowModified) {
            buffer.append("	.font shadow = ").append(this.isShadowOn).append("\n")
        } else {
            buffer.append("	.font shadow is not modified\n")
        }

        if (this.isFontCancellationModified) {
            buffer.append("	.font strikeout = ").append(this.isStruckout).append("\n")
        } else {
            buffer.append("	.font strikeout is not modified\n")
        }

        if (this.isFontStyleModified) {
            buffer.append("	.font weight = ").append(this.fontWeight.toInt())
                .append(
                    if (this.fontWeight == FONT_WEIGHT_NORMAL)
                        "(Normal)"
                    else if (this.fontWeight == FONT_WEIGHT_BOLD)
                        "(Bold)"
                    else
                        "0x" + Integer.toHexString(this.fontWeight.toInt())
                ).append("\n")
        } else {
            buffer.append("	.font weight = ]not modified]").append("\n")
        }

        if (this.isEscapementTypeModified) {
            buffer.append("	.escapement type = ").append(this.escapementType.toInt())
                .append("\n")
        } else {
            buffer.append("	.escapement type is not modified\n")
        }

        if (this.isUnderlineTypeModified) {
            buffer.append("	.underline type = ").append(this.underlineType.toInt()).append("\n")
        } else {
            buffer.append("	.underline type is not modified\n")
        }
        buffer.append("	.color index = ").append(
            "0x" + Integer.toHexString(this.fontColorIndex.toInt()).uppercase(
                Locale.getDefault()
            )
        ).append("\n")

        buffer.append("	[/Font Formatting]\n")
        return buffer.toString()
    }

    public override fun clone(): Any {
        val rawData = rawRecord.clone()
        return FontFormatting(rawData)
    }

    companion object {
        private const val OFFSET_FONT_NAME = 0
        private const val OFFSET_FONT_HEIGHT = 64
        private const val OFFSET_FONT_OPTIONS = 68
        private const val OFFSET_FONT_WEIGHT = 72
        private const val OFFSET_ESCAPEMENT_TYPE = 74
        private const val OFFSET_UNDERLINE_TYPE = 76
        private const val OFFSET_FONT_COLOR_INDEX = 80
        private const val OFFSET_OPTION_FLAGS = 88
        private const val OFFSET_ESCAPEMENT_TYPE_MODIFIED = 92
        private const val OFFSET_UNDERLINE_TYPE_MODIFIED = 96
        private const val OFFSET_FONT_WEIGHT_MODIFIED = 100
        private const val OFFSET_NOT_USED1 = 104
        private const val OFFSET_NOT_USED2 = 108
        private const val OFFSET_NOT_USED3 =
            112 // for some reason Excel always writes  0x7FFFFFFF at this offset
        private const val OFFSET_FONT_FORMATING_END = 116
        private const val RAW_DATA_SIZE = 118


        const val FONT_CELL_HEIGHT_PRESERVED: Int = -0x1

        // FONT OPTIONS MASKS
        private val posture = getInstance(0x00000002)
        private val outline = getInstance(0x00000008)
        private val shadow = getInstance(0x00000010)
        private val cancellation = getInstance(0x00000080)

        // OPTION FLAGS MASKS
        private val styleModified = getInstance(0x00000002)
        private val outlineModified = getInstance(0x00000008)
        private val shadowModified = getInstance(0x00000010)
        private val cancellationModified = getInstance(0x00000080)

        /** Escapement type - None  */
        const val SS_NONE: Short = 0

        /** Escapement type - Superscript  */
        const val SS_SUPER: Short = 1

        /** Escapement type - Subscript  */
        const val SS_SUB: Short = 2

        /** Underline type - None  */
        const val U_NONE: Byte = 0

        /** Underline type - Single  */
        const val U_SINGLE: Byte = 1

        /** Underline type - Double  */
        const val U_DOUBLE: Byte = 2

        /** Underline type - Single Accounting  */
        const val U_SINGLE_ACCOUNTING: Byte = 0x21

        /** Underline type - Double Accounting  */
        const val U_DOUBLE_ACCOUNTING: Byte = 0x22

        /** Normal boldness (not bold)  */
        private const val FONT_WEIGHT_NORMAL: Short = 0x190

        /**
         * Bold boldness (bold)
         */
        private const val FONT_WEIGHT_BOLD: Short = 0x2bc
    }
}
