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

import com.wxiwei.office.fc.hssf.formula.ptg.OperandPtg
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg.Companion.readTokens
import com.wxiwei.office.fc.hssf.record.cont.ContinuableRecord
import com.wxiwei.office.fc.hssf.record.cont.ContinuableRecordOutput
import com.wxiwei.office.fc.hssf.usermodel.HSSFRichTextString
import com.wxiwei.office.fc.util.BitFieldFactory.Companion.getInstance
import com.wxiwei.office.fc.util.HexDump.intToHex
import com.wxiwei.office.fc.util.HexDump.shortToHex


/**
 * The TXO record (0x01B6) is used to define the properties of a text box. It is
 * followed by two or more continue records unless there is no actual text. The
 * first continue records contain the text data and the last continue record
 * contains the formatting runs.
 *
 *
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class TextObjectRecord : ContinuableRecord {
    private var field_1_options = 0
    private var field_2_textOrientation = 0
    private var field_3_reserved4 = 0
    private var field_4_reserved5 = 0
    private var field_5_reserved6 = 0
    private var field_8_reserved7 = 0

    private var _text: HSSFRichTextString? = null

    /*
	 * Note - the next three fields are very similar to those on
	 * EmbededObjectRefSubRecord(ftPictFmla 0x0009)
	 *
	 * some observed values for the 4 bytes preceding the formula: C0 5E 86 03
	 * C0 11 AC 02 80 F1 8A 03 D4 F0 8A 03
	 */
    private var _unknownPreFormulaInt = 0

    /** expect tRef, tRef3D, tArea, tArea3D or tName  */
    private var _linkRefPtg: OperandPtg? = null

    /**
     * Not clear if needed .  Excel seems to be OK if this byte is not present.
     * Value is often the same as the earlier firstColumn byte.  */
    private var _unknownPostFormulaByte: Byte? = null

    constructor()

    constructor(`in`: RecordInputStream) {
        field_1_options = `in`.readUShort()
        field_2_textOrientation = `in`.readUShort()
        field_3_reserved4 = `in`.readUShort()
        field_4_reserved5 = `in`.readUShort()
        field_5_reserved6 = `in`.readUShort()
        val field_6_textLength = `in`.readUShort()
        val field_7_formattingDataLength = `in`.readUShort()
        field_8_reserved7 = `in`.readInt()

        if (`in`.remaining() > 0) {
            // Text Objects can have simple reference formulas
            // (This bit not mentioned in the MS document)
            if (`in`.remaining() < 11) {
                throw RecordFormatException("Not enough remaining data for a link formula")
            }
            val formulaSize = `in`.readUShort()
            _unknownPreFormulaInt = `in`.readInt()
            val ptgs = readTokens(formulaSize, `in`)
            if (ptgs.size != 1) {
                throw RecordFormatException(
                    ("Read " + ptgs.size
                            + " tokens but expected exactly 1")
                )
            }
            _linkRefPtg = ptgs[0] as OperandPtg?
            if (`in`.remaining() > 0) {
                _unknownPostFormulaByte = `in`.readByte()
            } else {
                _unknownPostFormulaByte = null
            }
        } else {
            _linkRefPtg = null
        }
        if (`in`.remaining() > 0) {
            throw RecordFormatException("Unused " + `in`.remaining() + " bytes at end of record")
        }

        val text: String?
        if (field_6_textLength > 0) {
            text = readRawString(`in`, field_6_textLength)
        } else {
            text = ""
        }
        _text = HSSFRichTextString(text)

        if (field_7_formattingDataLength > 0) {
            Companion.processFontRuns(`in`, _text!!, field_7_formattingDataLength)
        }
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    private fun serializeTXORecord(out: ContinuableRecordOutput) {
        out.writeShort(field_1_options)
        out.writeShort(field_2_textOrientation)
        out.writeShort(field_3_reserved4)
        out.writeShort(field_4_reserved5)
        out.writeShort(field_5_reserved6)
        out.writeShort(_text!!.length())
        out.writeShort(getFormattingDataLength())
        out.writeInt(field_8_reserved7)

        if (_linkRefPtg != null) {
            val formulaSize = _linkRefPtg!!.size
            out.writeShort(formulaSize)
            out.writeInt(_unknownPreFormulaInt)
            _linkRefPtg!!.write(out)
            if (_unknownPostFormulaByte != null) {
                out.writeByte(_unknownPostFormulaByte!!.toInt())
            }
        }
    }

    private fun serializeTrailingRecords(out: ContinuableRecordOutput) {
        out.writeContinue()
        out.writeStringData(_text!!.getString())
        out.writeContinue()
        Companion.writeFormatData(out, _text!!)
    }

    override fun serialize(out: ContinuableRecordOutput) {
        serializeTXORecord(out)
        if (_text!!.getString().length > 0) {
            serializeTrailingRecords(out)
        }
    }

    private fun getFormattingDataLength(): Int {
        if (_text!!.length() < 1) {
            // important - no formatting data if text is empty
            return 0
        }
        return (_text!!.numFormattingRuns() + 1) * FORMAT_RUN_ENCODED_SIZE
    }

    /**
     * Sets the Horizontal text alignment field value.
     */
    fun setHorizontalTextAlignment(value: Int) {
        field_1_options = HorizontalTextAlignment.setValue(field_1_options, value)
    }

    /**
     * @return the Horizontal text alignment field value.
     */
    fun getHorizontalTextAlignment(): Int {
        return HorizontalTextAlignment.getValue(field_1_options)
    }

    /**
     * Sets the Vertical text alignment field value.
     */
    fun setVerticalTextAlignment(value: Int) {
        field_1_options = VerticalTextAlignment.setValue(field_1_options, value)
    }

    /**
     * @return the Vertical text alignment field value.
     */
    fun getVerticalTextAlignment(): Int {
        return VerticalTextAlignment.getValue(field_1_options)
    }

    /**
     * Sets the text locked field value.
     */
    fun setTextLocked(value: Boolean) {
        field_1_options = textLocked.setBoolean(field_1_options, value)
    }

    /**
     * @return the text locked field value.
     */
    fun isTextLocked(): Boolean {
        return textLocked.isSet(field_1_options)
    }

    /**
     * Get the text orientation field for the TextObjectBase record.
     * 
     * @return One of TEXT_ORIENTATION_NONE TEXT_ORIENTATION_TOP_TO_BOTTOM
     * TEXT_ORIENTATION_ROT_RIGHT TEXT_ORIENTATION_ROT_LEFT
     */
    fun getTextOrientation(): Int {
        return field_2_textOrientation
    }

    /**
     * Set the text orientation field for the TextObjectBase record.
     * 
     * @param textOrientation
     * One of TEXT_ORIENTATION_NONE TEXT_ORIENTATION_TOP_TO_BOTTOM
     * TEXT_ORIENTATION_ROT_RIGHT TEXT_ORIENTATION_ROT_LEFT
     */
    fun setTextOrientation(textOrientation: Int) {
        this.field_2_textOrientation = textOrientation
    }

    fun getStr(): HSSFRichTextString {
        return _text!!
    }

    fun setStr(str: HSSFRichTextString) {
        _text = str
    }

    fun getLinkRefPtg(): Ptg? {
        return _linkRefPtg
    }

    override fun toString(): String {
        val sb = StringBuffer()

        sb.append("[TXO]\n")
        sb.append("    .options        = ").append(shortToHex(field_1_options)).append("\n")
        sb.append("         .isHorizontal = ").append(getHorizontalTextAlignment()).append('\n')
        sb.append("         .isVertical   = ").append(getVerticalTextAlignment()).append('\n')
        sb.append("         .textLocked   = ").append(isTextLocked()).append('\n')
        sb.append("    .textOrientation= ").append(shortToHex(getTextOrientation())).append("\n")
        sb.append("    .reserved4      = ").append(shortToHex(field_3_reserved4)).append("\n")
        sb.append("    .reserved5      = ").append(shortToHex(field_4_reserved5)).append("\n")
        sb.append("    .reserved6      = ").append(shortToHex(field_5_reserved6)).append("\n")
        sb.append("    .textLength     = ").append(shortToHex(_text!!.length())).append("\n")
        sb.append("    .reserved7      = ").append(intToHex(field_8_reserved7)).append("\n")

        sb.append("    .string = ").append(_text).append('\n')

        for (i in 0..<_text!!.numFormattingRuns()) {
            sb.append("    .textrun = ").append(_text!!.getFontOfFormattingRun(i).toInt())
                .append('\n')
        }
        sb.append("[/TXO]\n")
        return sb.toString()
    }

    override fun clone(): Any {
        val rec = TextObjectRecord()
        rec._text = _text

        rec.field_1_options = field_1_options
        rec.field_2_textOrientation = field_2_textOrientation
        rec.field_3_reserved4 = field_3_reserved4
        rec.field_4_reserved5 = field_4_reserved5
        rec.field_5_reserved6 = field_5_reserved6
        rec.field_8_reserved7 = field_8_reserved7

        rec._text = _text // clone needed?

        if (_linkRefPtg != null) {
            rec._unknownPreFormulaInt = _unknownPreFormulaInt
            rec._linkRefPtg = _linkRefPtg!!.copy()
            rec._unknownPostFormulaByte = _unknownPostFormulaByte
        }
        return rec
    }

    companion object {
        const val sid: Short = 0x01B6

        private const val FORMAT_RUN_ENCODED_SIZE = 8 // 2 shorts and 4 bytes reserved

        private val HorizontalTextAlignment = getInstance(0x000E)
        private val VerticalTextAlignment = getInstance(0x0070)
        private val textLocked = getInstance(0x0200)

        const val HORIZONTAL_TEXT_ALIGNMENT_LEFT_ALIGNED: Short = 1
        const val HORIZONTAL_TEXT_ALIGNMENT_CENTERED: Short = 2
        const val HORIZONTAL_TEXT_ALIGNMENT_RIGHT_ALIGNED: Short = 3
        const val HORIZONTAL_TEXT_ALIGNMENT_JUSTIFIED: Short = 4
        const val VERTICAL_TEXT_ALIGNMENT_TOP: Short = 1
        const val VERTICAL_TEXT_ALIGNMENT_CENTER: Short = 2
        const val VERTICAL_TEXT_ALIGNMENT_BOTTOM: Short = 3
        const val VERTICAL_TEXT_ALIGNMENT_JUSTIFY: Short = 4

        const val TEXT_ORIENTATION_NONE: Short = 0
        const val TEXT_ORIENTATION_TOP_TO_BOTTOM: Short = 1
        const val TEXT_ORIENTATION_ROT_RIGHT: Short = 2
        const val TEXT_ORIENTATION_ROT_LEFT: Short = 3

        private fun readRawString(`in`: RecordInputStream, textLength: Int): String {
            val compressByte = `in`.readByte()
            val isCompressed = (compressByte.toInt() and 0x01) == 0
            if (isCompressed) {
                return `in`.readCompressedUnicode(textLength)
            }
            return `in`.readUnicodeLEString(textLength)
        }

        private fun processFontRuns(
            `in`: RecordInputStream, str: HSSFRichTextString,
            formattingRunDataLength: Int
        ) {
            if (formattingRunDataLength % FORMAT_RUN_ENCODED_SIZE != 0) {
                throw RecordFormatException(
                    ("Bad format run data length " + formattingRunDataLength
                            + ")")
                )
            }
            val nRuns: Int = formattingRunDataLength / FORMAT_RUN_ENCODED_SIZE
            for (i in 0..<nRuns) {
                val index = `in`.readShort()
                val iFont = `in`.readShort()
                `in`.readInt() // skip reserved.
                str.applyFont(index.toInt(), str.length(), iFont)
            }
        }

        private fun writeFormatData(out: ContinuableRecordOutput, str: HSSFRichTextString) {
            val nRuns = str.numFormattingRuns()
            for (i in 0..<nRuns) {
                out.writeShort(str.getIndexOfFormattingRun(i))
                val fontIndex = str.getFontOfFormattingRun(i).toInt()
                out.writeShort(if (fontIndex == HSSFRichTextString.NO_FONT.toInt()) 0 else fontIndex)
                out.writeInt(0) // skip reserved
            }
            out.writeShort(str.length())
            out.writeShort(0)
            out.writeInt(0) // skip reserved
        }
    }
}
