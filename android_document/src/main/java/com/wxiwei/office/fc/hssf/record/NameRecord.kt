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
import com.wxiwei.office.fc.hssf.formula.Formula.Companion.read
import com.wxiwei.office.fc.hssf.formula.ptg.Area3DPtg
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.formula.ptg.Ref3DPtg
import com.wxiwei.office.fc.hssf.record.cont.ContinuableRecord
import com.wxiwei.office.fc.hssf.record.cont.ContinuableRecordOutput
import com.wxiwei.office.fc.util.HexDump.byteToHex
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianByteArrayInputStream
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.StringUtil.hasMultibyte
import com.wxiwei.office.fc.util.StringUtil.putCompressedUnicode
import com.wxiwei.office.fc.util.StringUtil.putUnicodeLE
import com.wxiwei.office.fc.util.StringUtil.readCompressedUnicode
import com.wxiwei.office.fc.util.StringUtil.readUnicodeLE

/**
 * Title:        DEFINEDNAME Record (0x0018) 
 *
 *
 * Description:  Defines a named range within a workbook. <P>
 * REFERENCE:  </P><P>
 * @author Libin Roman (Vista Portal LDT. Developer)
 * @author  Sergei Kozello (sergeikozello at mail.ru)
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Petr Udalau - added method setFunction(boolean)
</P> */
class NameRecord : ContinuableRecord {
    private object Option {
        const val OPT_HIDDEN_NAME: Int = 0x0001
        const val OPT_FUNCTION_NAME: Int = 0x0002
        const val OPT_COMMAND_NAME: Int = 0x0004
        const val OPT_MACRO: Int = 0x0008
        const val OPT_COMPLEX: Int = 0x0010
        const val OPT_BUILTIN: Int = 0x0020
        const val OPT_BINDATA: Int = 0x1000
        fun isFormula(optValue: Int): Boolean {
            return (optValue and 0x0F) == 0
        }
    }

    private var field_1_option_flag: Short = 0
    private var field_2_keyboard_shortcut: Byte = 0

    /** One-based extern index of sheet (resolved via LinkTable). Zero if this is a global name   */
    private var field_5_externSheetIndex_plus1: Short = 0

    /** the one based sheet number.   */
    private var field_6_sheetNumber = 0
    private var field_11_nameIsMultibyte = false
    private var field_12_built_in_code: Byte = 0
    private var field_12_name_text: String? = null
    private var field_13_name_definition: Formula
    private var field_14_custom_menu_text: String
    private var field_15_description_text: String
    private var field_16_help_topic_text: String
    private var field_17_status_bar_text: String


    /** Creates new NameRecord  */
    constructor() {
        field_13_name_definition = Formula.create(Ptg.EMPTY_PTG_ARRAY)!!

        field_12_name_text = ""
        field_14_custom_menu_text = ""
        field_15_description_text = ""
        field_16_help_topic_text = ""
        field_17_status_bar_text = ""
    }

    /**
     * Constructor to create a built-in named region
     * @param builtin Built-in byte representation for the name record, use the public constants
     */
    constructor(builtin: Byte, sheetNumber: Int) : this() {
        field_12_built_in_code = builtin
        setOptionFlag((field_1_option_flag.toInt() or Option.OPT_BUILTIN).toShort())
        field_6_sheetNumber = sheetNumber //the extern sheets are set through references
    }

    /** sets the option flag for the named range
     * @param flag option flag
     */
    fun setOptionFlag(flag: Short) {
        field_1_option_flag = flag
    }


    /** sets the keyboard shortcut
     * @param shortcut keyboard shortcut
     */
    fun setKeyboardShortcut(shortcut: Byte) {
        field_2_keyboard_shortcut = shortcut
    }

    /**
     * For named ranges, and built-in names
     * @return the 1-based sheet number.
     */
    fun getSheetNumber(): Int {
        return field_6_sheetNumber
    }

    /**
     * @return function group
     * @see FnGroupCountRecord
     */
    fun getFnGroup(): Byte {
        val masked = field_1_option_flag.toInt() and 0x0fc0
        return (masked shr 4).toByte()
    }


    fun setSheetNumber(value: Int) {
        field_6_sheetNumber = value
    }

    @get:JvmName("getSheetNumberProperty")
    @set:JvmName("setSheetNumberProperty")
    var sheetNumber: Int get() = getSheetNumber(); set(v) = setSheetNumber(v)
    var commentText: String? = ""
    @get:JvmName("getDescriptionTextProperty")
    @set:JvmName("setDescriptionTextProperty")
    var descriptionText: String? get() = getDescriptionText(); set(v) { field_15_description_text = v ?: "" }


    /** sets the name of the named range
     * @param name named range name
     */
    fun setNameText(name: String) {
        field_12_name_text = name
        field_11_nameIsMultibyte = hasMultibyte(name)
    }

    /** sets the custom menu text
     * @param text custom menu text
     */
    fun setCustomMenuText(text: String) {
        field_14_custom_menu_text = text
    }

    /** sets the description text
     * @param text the description text
     */
    fun setDescriptionText(text: String) {
        field_15_description_text = text
    }

    /** sets the help topic text
     * @param text help topix text
     */
    fun setHelpTopicText(text: String) {
        field_16_help_topic_text = text
    }

    /** sets the status bar text
     * @param text status bar text
     */
    fun setStatusBarText(text: String) {
        field_17_status_bar_text = text
    }

    /** gets the option flag
     * @return option flag
     */
    fun getOptionFlag(): Short {
        return field_1_option_flag
    }

    /** returns the keyboard shortcut
     * @return keyboard shortcut
     */
    fun getKeyboardShortcut(): Byte {
        return field_2_keyboard_shortcut
    }

    /**
     * gets the name length, in characters
     * @return name length
     */
    private fun getNameTextLength(): Int {
        if (isBuiltInName()) {
            return 1
        }
        return field_12_name_text!!.length
    }


    /**
     * @return true if name is hidden
     */
    fun isHiddenName(): Boolean {
        return (field_1_option_flag.toInt() and Option.OPT_HIDDEN_NAME) != 0
    }

    fun setHidden(b: Boolean) {
        if (b) {
            field_1_option_flag = (field_1_option_flag.toInt() or Option.OPT_HIDDEN_NAME).toShort()
        } else {
            field_1_option_flag = (field_1_option_flag.toInt() and (Option.OPT_HIDDEN_NAME.inv())).toShort()
        }
    }

    /**
     * @return `true` if name is a function
     */
    fun isFunctionName(): Boolean {
        return (field_1_option_flag.toInt() and Option.OPT_FUNCTION_NAME) != 0
    }

    /**
     * Indicates that the defined name refers to a user-defined function.
     * This attribute is used when there is an add-in or other code project associated with the file.
     * 
     * @param function `true` indicates the name refers to a function.
     */
    fun setFunction(function: Boolean) {
        if (function) {
            field_1_option_flag = (field_1_option_flag.toInt() or Option.OPT_FUNCTION_NAME).toShort()
        } else {
            field_1_option_flag = (field_1_option_flag.toInt() and (Option.OPT_FUNCTION_NAME.inv())).toShort()
        }
    }

    /**
     * @return `true` if name has a formula (named range or defined value)
     */
    fun hasFormula(): Boolean {
        return Option.isFormula(field_1_option_flag.toInt()) && field_13_name_definition.encodedTokenSize > 0
    }

    /**
     * @return true if name is a command
     */
    fun isCommandName(): Boolean {
        return (field_1_option_flag.toInt() and Option.OPT_COMMAND_NAME) != 0
    }

    /**
     * @return true if function macro or command macro
     */
    fun isMacro(): Boolean {
        return (field_1_option_flag.toInt() and Option.OPT_MACRO) != 0
    }

    /**
     * @return true if array formula or user defined
     */
    fun isComplexFunction(): Boolean {
        return (field_1_option_flag.toInt() and Option.OPT_COMPLEX) != 0
    }

    /**Convenience Function to determine if the name is a built-in name
     */
    fun isBuiltInName(): Boolean {
        return ((field_1_option_flag.toInt() and Option.OPT_BUILTIN) != 0)
    }


    /** gets the name
     * @return name
     */
    fun getNameText(): String? {
        return if (isBuiltInName()) translateBuiltInName(getBuiltInName()) else field_12_name_text
    }

    @get:JvmName("getNameTextProperty")
    val nameText: String? get() = getNameText()

    /** Gets the Built In Name
     * @return the built in Name
     */
    fun getBuiltInName(): Byte {
        return field_12_built_in_code
    }


    /** gets the definition, reference (Formula)
     * @return the name formula. never `null`
     */
    fun getNameDefinition(): Array<Ptg?> {
        return field_13_name_definition.tokens
    }

    fun setNameDefinition(ptgs: Array<Ptg?>?) {
        field_13_name_definition = Formula.create(ptgs)!!
    }

    /** get the custom menu text
     * @return custom menu text
     */
    fun getCustomMenuText(): String {
        return field_14_custom_menu_text
    }

    /** gets the description text
     * @return description text
     */
    fun getDescriptionText(): String {
        return field_15_description_text
    }

    /** get the help topic text
     * @return gelp topic text
     */
    fun getHelpTopicText(): String {
        return field_16_help_topic_text
    }

    /** gets the status bar text
     * @return status bar text
     */
    fun getStatusBarText(): String {
        return field_17_status_bar_text
    }

    /**
     * NameRecord can span into
     * 
     * @param out a data output stream
     */
    public override fun serialize(out: ContinuableRecordOutput) {
        val field_7_length_custom_menu = field_14_custom_menu_text.length
        val field_8_length_description_text = field_15_description_text.length
        val field_9_length_help_topic_text = field_16_help_topic_text.length
        val field_10_length_status_bar_text = field_17_status_bar_text.length

        // size defined below
        out.writeShort(getOptionFlag().toInt())
        out.writeByte(getKeyboardShortcut().toInt())
        out.writeByte(getNameTextLength())
        // Note - formula size is not immediately before encoded formula, and does not include any array constant data
        out.writeShort(field_13_name_definition.encodedTokenSize)
        out.writeShort(field_5_externSheetIndex_plus1.toInt())
        out.writeShort(field_6_sheetNumber)
        out.writeByte(field_7_length_custom_menu)
        out.writeByte(field_8_length_description_text)
        out.writeByte(field_9_length_help_topic_text)
        out.writeByte(field_10_length_status_bar_text)
        out.writeByte(if (field_11_nameIsMultibyte) 1 else 0)

        if (isBuiltInName()) {
            //can send the builtin name directly in
            out.writeByte(field_12_built_in_code.toInt())
        } else {
            val nameText = field_12_name_text!!
            if (field_11_nameIsMultibyte) {
                putUnicodeLE(nameText, out)
            } else {
                putCompressedUnicode(nameText, out)
            }
        }
        field_13_name_definition.serializeTokens(out)
        field_13_name_definition.serializeArrayConstantData(out)

        putCompressedUnicode(getCustomMenuText(), out)
        putCompressedUnicode(getDescriptionText(), out)
        putCompressedUnicode(getHelpTopicText(), out)
        putCompressedUnicode(getStatusBarText(), out)
    }

    private fun getNameRawSize(): Int {
        if (isBuiltInName()) {
            return 1
        }
        val nChars = field_12_name_text!!.length
        if (field_11_nameIsMultibyte) {
            return 2 * nChars
        }
        return nChars
    }

    protected fun getDataSize(): Int {
        return (13 // 3 shorts + 7 bytes
                + getNameRawSize()
                + field_14_custom_menu_text.length
                + field_15_description_text.length
                + field_16_help_topic_text.length
                + field_17_status_bar_text.length
                + field_13_name_definition.encodedSize)
    }

    /** gets the extern sheet number
     * @return extern sheet index
     */
    fun getExternSheetNumber(): Int {
        if (field_13_name_definition.encodedSize < 1) {
            return 0
        }
        val ptg = field_13_name_definition.tokens[0]

        if (ptg!!.javaClass == Area3DPtg::class.java) {
            return (ptg as Area3DPtg).externSheetIndex
        }
        if (ptg.javaClass == Ref3DPtg::class.java) {
            return (ptg as Ref3DPtg).externSheetIndex
        }
        return 0
    }

    /**
     * called by the constructor, should set class level fields.  Should throw
     * runtime exception for bad/icomplete data.
     * 
     * @param ris the RecordInputstream to read the record from
     */
    constructor(ris: RecordInputStream) {
        // YK: Formula data can span into continue records, for example,
        // when containing a large array of strings. See Bugzilla 50244

        // read all remaining bytes and wrap into a LittleEndianInput

        val remainder = ris.readAllContinuedRemainder()
        val `in`: LittleEndianInput = LittleEndianByteArrayInputStream(remainder)

        field_1_option_flag = `in`.readShort()
        field_2_keyboard_shortcut = `in`.readByte()
        val field_3_length_name_text = `in`.readUByte()
        val field_4_length_name_definition = `in`.readShort().toInt()
        field_5_externSheetIndex_plus1 = `in`.readShort()
        field_6_sheetNumber = `in`.readUShort()
        val f7_customMenuLen = `in`.readUByte()
        val f8_descriptionTextLen = `in`.readUByte()
        val f9_helpTopicTextLen = `in`.readUByte()
        val f10_statusBarTextLen = `in`.readUByte()

        //store the name in byte form if it's a built-in name
        field_11_nameIsMultibyte = (`in`.readByte().toInt() != 0)
        if (isBuiltInName()) {
            field_12_built_in_code = `in`.readByte()
        } else {
            if (field_11_nameIsMultibyte) {
                field_12_name_text = readUnicodeLE(`in`, field_3_length_name_text)
            } else {
                field_12_name_text = readCompressedUnicode(`in`, field_3_length_name_text)
            }
        }

        val nBytesAvailable = `in`.available() - ((f7_customMenuLen
                + f8_descriptionTextLen + f9_helpTopicTextLen + f10_statusBarTextLen))
        field_13_name_definition = read(field_4_length_name_definition, `in`, nBytesAvailable)

        //Who says that this can only ever be compressed unicode???
        field_14_custom_menu_text = readCompressedUnicode(`in`, f7_customMenuLen)
        field_15_description_text = readCompressedUnicode(`in`, f8_descriptionTextLen)
        field_16_help_topic_text = readCompressedUnicode(`in`, f9_helpTopicTextLen)
        field_17_status_bar_text = readCompressedUnicode(`in`, f10_statusBarTextLen)
    }

    /**
     * return the non static version of the id for this record.
     */
    override fun getSid(): Short {
        return Companion.sid
    }

    /*
	  20 00
	  00
	  01
	  1A 00 // sz = 0x1A = 26
	  00 00
	  01 00
	  00
	  00
	  00
	  00
	  00 // unicode flag
	  07 // name

	  29 17 00 3B 00 00 00 00 FF FF 00 00 02 00 3B 00 //{ 26
	  00 07 00 07 00 00 00 FF 00 10                   //  }



	  20 00
	  00
	  01
	  0B 00 // sz = 0xB = 11
	  00 00
	  01 00
	  00
	  00
	  00
	  00
	  00 // unicode flag
	  07 // name

	  3B 00 00 07 00 07 00 00 00 FF 00   // { 11 }
  */
    /*
	  18, 00,
	  1B, 00,

	  20, 00,
	  00,
	  01,
	  0B, 00,
	  00,
	  00,
	  00,
	  00,
	  00,
	  07,
	  3B 00 00 07 00 07 00 00 00 FF 00 ]
	 */
    override fun toString(): String {
        val sb = StringBuffer()

        sb.append("[NAME]\n")
        sb.append("    .option flags           = ").append(shortToHex(field_1_option_flag.toInt()))
            .append("\n")
        sb.append("    .keyboard shortcut      = ")
            .append(byteToHex(field_2_keyboard_shortcut.toInt())).append("\n")
        sb.append("    .length of the name     = ").append(getNameTextLength()).append("\n")
        sb.append("    .extSheetIx(1-based, 0=Global)= ")
            .append(field_5_externSheetIndex_plus1.toInt()).append("\n")
        sb.append("    .sheetTabIx             = ").append(field_6_sheetNumber).append("\n")
        sb.append("    .Menu text length       = ").append(field_14_custom_menu_text.length)
            .append("\n")
        sb.append("    .Description text length= ").append(field_15_description_text.length)
            .append("\n")
        sb.append("    .Help topic text length = ").append(field_16_help_topic_text.length)
            .append("\n")
        sb.append("    .Status bar text length = ").append(field_17_status_bar_text.length)
            .append("\n")
        sb.append("    .NameIsMultibyte        = ").append(field_11_nameIsMultibyte).append("\n")
        sb.append("    .Name (Unicode text)    = ").append(getNameText()).append("\n")
        val ptgs = field_13_name_definition.tokens
        sb.append("    .Formula (nTokens=").append(ptgs.size).append("):").append("\n")
        for (i in ptgs.indices) {
            val ptg = ptgs[i]
            if (ptg != null) {
                sb.append("       " + ptg.toString()).append(ptg.rVAType).append("\n")
            }
        }

        sb.append("    .Menu text       = ").append(field_14_custom_menu_text).append("\n")
        sb.append("    .Description text= ").append(field_15_description_text).append("\n")
        sb.append("    .Help topic text = ").append(field_16_help_topic_text).append("\n")
        sb.append("    .Status bar text = ").append(field_17_status_bar_text).append("\n")
        sb.append("[/NAME]\n")

        return sb.toString()
    }

    companion object {
        const val sid: Short = 0x0018

        /**Included for completeness sake, not implemented  */
        const val BUILTIN_CONSOLIDATE_AREA: Byte = 1

        /**Included for completeness sake, not implemented  */
        const val BUILTIN_AUTO_OPEN: Byte = 2

        /**Included for completeness sake, not implemented  */
        const val BUILTIN_AUTO_CLOSE: Byte = 3

        /**Included for completeness sake, not implemented  */
        const val BUILTIN_DATABASE: Byte = 4

        /**Included for completeness sake, not implemented  */
        const val BUILTIN_CRITERIA: Byte = 5

        const val BUILTIN_PRINT_AREA: Byte = 6
        const val BUILTIN_PRINT_TITLE: Byte = 7

        /**Included for completeness sake, not implemented  */
        const val BUILTIN_RECORDER: Byte = 8

        /**Included for completeness sake, not implemented  */
        const val BUILTIN_DATA_FORM: Byte = 9

        /**Included for completeness sake, not implemented  */
        const val BUILTIN_AUTO_ACTIVATE: Byte = 10

        /**Included for completeness sake, not implemented  */
        const val BUILTIN_AUTO_DEACTIVATE: Byte = 11

        /**Included for completeness sake, not implemented  */
        const val BUILTIN_SHEET_TITLE: Byte = 12

        const val BUILTIN_FILTER_DB: Byte = 13

        /**Creates a human readable name for built in types
         * @return Unknown if the built-in name cannot be translated
         */
        private fun translateBuiltInName(name: Byte): String {
            when (name) {
                BUILTIN_AUTO_ACTIVATE -> return "Auto_Activate"
                BUILTIN_AUTO_CLOSE -> return "Auto_Close"
                BUILTIN_AUTO_DEACTIVATE -> return "Auto_Deactivate"
                BUILTIN_AUTO_OPEN -> return "Auto_Open"
                BUILTIN_CONSOLIDATE_AREA -> return "Consolidate_Area"
                BUILTIN_CRITERIA -> return "Criteria"
                BUILTIN_DATABASE -> return "Database"
                BUILTIN_DATA_FORM -> return "Data_Form"
                BUILTIN_PRINT_AREA -> return "Print_Area"
                BUILTIN_PRINT_TITLE -> return "Print_Titles"
                BUILTIN_RECORDER -> return "Recorder"
                BUILTIN_SHEET_TITLE -> return "Sheet_Title"
                BUILTIN_FILTER_DB -> return "_FilterDatabase"

            }

            return "Unknown"
        }
    }
}
