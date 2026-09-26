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
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.record.common.UnicodeString
import com.wxiwei.office.fc.ss.util.CellRangeAddressList
import com.wxiwei.office.fc.util.BitField
import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.fc.util.StringUtil.hasMultibyte
import com.wxiwei.office.fc.util.StringUtil.writeUnicodeString


/**
 * Title:        DATAVALIDATION Record (0x01BE)
 *
 *
 * Description:  This record stores data validation settings and a list of cell ranges
 * which contain these settings. The data validation settings of a sheet
 * are stored in a sequential list of DV records. This list is followed by
 * DVAL record(s)
 * @author Dragos Buleandra (dragos.buleandra@trade2b.ro)
 * @author Josh Micich
 */
class DVRecord : StandardRecord {
    /** Option flags  */
    private val _option_flags: Int

    /** Title of the prompt box  */
    private val _promptTitle: UnicodeString

    /** Title of the error box  */
    private val _errorTitle: UnicodeString

    /** Text of the prompt box  */
    private val _promptText: UnicodeString

    /** Text of the error box  */
    private val _errorText: UnicodeString

    /** Not used - Excel seems to always write 0x3FE0  */
    private var _not_used_1: Short = 0x3FE0

    /** Formula data for first condition (RPN token array without size field)  */
    private val _formula1: Formula

    /** Not used - Excel seems to always write 0x0000  */
    private var _not_used_2: Short = 0x0000

    /** Formula data for second condition (RPN token array without size field)  */
    private val _formula2: Formula

    /** Cell range address list with all affected ranges  */
    private val _regions: CellRangeAddressList

    constructor(
        validationType: Int, operator: Int, errorStyle: Int, emptyCellAllowed: Boolean,
        suppressDropDownArrow: Boolean, isExplicitList: Boolean,
        showPromptBox: Boolean, promptTitle: String?, promptText: String?,
        showErrorBox: Boolean, errorTitle: String?, errorText: String?,
        formula1: Array<Ptg?>?, formula2: Array<Ptg?>?,
        regions: CellRangeAddressList
    ) {
        var flags = 0
        flags = opt_data_type.setValue(flags, validationType)
        flags = opt_condition_operator.setValue(flags, operator)
        flags = opt_error_style.setValue(flags, errorStyle)
        flags = opt_empty_cell_allowed.setBoolean(flags, emptyCellAllowed)
        flags = opt_suppress_dropdown_arrow.setBoolean(flags, suppressDropDownArrow)
        flags = opt_string_list_formula.setBoolean(flags, isExplicitList)
        flags = opt_show_prompt_on_cell_selected.setBoolean(flags, showPromptBox)
        flags = opt_show_error_on_invalid_value.setBoolean(flags, showErrorBox)
        _option_flags = flags
        _promptTitle = resolveTitleText(promptTitle)
        _promptText = resolveTitleText(promptText)
        _errorTitle = resolveTitleText(errorTitle)
        _errorText = resolveTitleText(errorText)
        _formula1 = Formula.create(formula1)!!
        _formula2 = Formula.create(formula2)!!
        _regions = regions
    }

    constructor(`in`: RecordInputStream) {
        _option_flags = `in`.readInt()

        _promptTitle = readUnicodeString(`in`)
        _errorTitle = readUnicodeString(`in`)
        _promptText = readUnicodeString(`in`)
        _errorText = readUnicodeString(`in`)

        val field_size_first_formula = `in`.readUShort()
        _not_used_1 = `in`.readShort()

        // "You may not use unions, intersections or array constants in Data Validation criteria"

        // read first formula data condition
        _formula1 = read(field_size_first_formula, `in`)

        val field_size_sec_formula = `in`.readUShort()
        _not_used_2 = `in`.readShort()

        // read sec formula data condition
        _formula2 = read(field_size_sec_formula, `in`)

        // read cell range address list with all affected ranges
        _regions = CellRangeAddressList(`in`)
    }

    // --> start option flags
    /**
     * @return the condition data type
     * @see com.wxiwei.office.fc.ss.usermodel.com.wxiwei.office.fc.ss.usermodel.com.wxiwei.office.fc.ss.usermodel.DataValidationConstraint.ValidationType
     */
    fun getDataType(): Int {
        return opt_data_type.getValue(_option_flags)
    }

    /**
     * @return the condition error style
     * @see com.wxiwei.office.fc.ss.usermodel.com.wxiwei.office.fc.ss.usermodel.com.wxiwei.office.fc.ss.usermodel.DataValidation.ErrorStyle
     */
    fun getErrorStyle(): Int {
        return opt_error_style.getValue(_option_flags)
    }

    /**
     * @return `true` if in list validations the string list is explicitly given in the
     * formula, `false` otherwise
     */
    fun getListExplicitFormula(): Boolean {
        return (opt_string_list_formula.isSet(_option_flags))
    }

    /**
     * @return `true` if empty values are allowed in cells, `false` otherwise
     */
    fun getEmptyCellAllowed(): Boolean {
        return (opt_empty_cell_allowed.isSet(_option_flags))
    }


    /**
     * @return `true` if drop down arrow should be suppressed when list validation is
     * used, `false` otherwise
     */
    fun getSuppressDropdownArrow(): Boolean {
        return (opt_suppress_dropdown_arrow.isSet(_option_flags))
    }

    /**
     * @return `true` if a prompt window should appear when cell is selected, `false` otherwise
     */
    fun getShowPromptOnCellSelected(): Boolean {
        return (opt_show_prompt_on_cell_selected.isSet(_option_flags))
    }

    /**
     * @return `true` if an error window should appear when an invalid value is entered
     * in the cell, `false` otherwise
     */
    fun getShowErrorOnInvalidValue(): Boolean {
        return (opt_show_error_on_invalid_value.isSet(_option_flags))
    }

    /**
     * get the condition operator
     * @return the condition operator
     * @see HSSFDataValidation utility class
     */
    fun getConditionOperator(): Int {
        return opt_condition_operator.getValue(_option_flags)
    }


    // <-- end option flags
    fun getCellRangeAddress(): CellRangeAddressList {
        return this._regions
    }


    override fun toString(): String {
        val sb = StringBuffer()
        sb.append("[DV]\n")
        sb.append(" options=").append(Integer.toHexString(_option_flags))
        sb.append(" title-prompt=").append(formatTextTitle(_promptTitle))
        sb.append(" title-error=").append(formatTextTitle(_errorTitle))
        sb.append(" text-prompt=").append(formatTextTitle(_promptText))
        sb.append(" text-error=").append(formatTextTitle(_errorText))
        sb.append("\n")
        appendFormula(sb, "Formula 1:", _formula1)
        appendFormula(sb, "Formula 2:", _formula2)
        sb.append("Regions: ")
        val nRegions = _regions.countRanges()
        for (i in 0..<nRegions) {
            if (i > 0) {
                sb.append(", ")
            }
            val addr = _regions.getCellRangeAddress(i)
            sb.append('(').append(addr.getFirstRow()).append(',').append(addr.getLastRow())
            sb.append(',').append(addr.getFirstColumn()).append(',').append(addr.getLastColumn())
                .append(')')
        }
        sb.append("\n")
        sb.append("[/DV]")

        return sb.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeInt(_option_flags)

        serializeUnicodeString(_promptTitle, out)
        serializeUnicodeString(_errorTitle, out)
        serializeUnicodeString(_promptText, out)
        serializeUnicodeString(_errorText, out)
        out.writeShort(_formula1.encodedTokenSize)
        out.writeShort(_not_used_1.toInt())
        _formula1.serializeTokens(out)

        out.writeShort(_formula2.encodedTokenSize)
        out.writeShort(_not_used_2.toInt())
        _formula2.serializeTokens(out)

        _regions.serialize(out)
    }

    override fun getDataSize(): Int {
        var size =
            4 + 2 + 2 + 2 + 2 //options_field+first_formula_size+first_unused+sec_formula_size+sec+unused;
        size += getUnicodeStringSize(_promptTitle)
        size += getUnicodeStringSize(_errorTitle)
        size += getUnicodeStringSize(_promptText)
        size += getUnicodeStringSize(_errorText)
        size += _formula1.encodedTokenSize
        size += _formula2.encodedTokenSize
        size += _regions.getSize()
        return size
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    /**
     * Clones the object. Uses serialisation, as the
     * contents are somewhat complex
     */
    override fun clone(): Any {
        return cloneViaReserialise()
    }

    companion object {
        const val sid: Short = 0x01BE

        /** the unicode string used for error/prompt title/text when not present  */
        private val NULL_TEXT_STRING = UnicodeString("\u0000")

        /**
         * Option flags field
         * 
         * @see HSSFDataValidation utility class
         */
        private val opt_data_type = BitField(0x0000000F)
        private val opt_error_style = BitField(0x00000070)
        private val opt_string_list_formula = BitField(0x00000080)
        private val opt_empty_cell_allowed = BitField(0x00000100)
        private val opt_suppress_dropdown_arrow = BitField(0x00000200)
        private val opt_show_prompt_on_cell_selected = BitField(0x00040000)
        private val opt_show_error_on_invalid_value = BitField(0x00080000)
        private val opt_condition_operator = BitField(0x00700000)

        private fun formatTextTitle(us: UnicodeString): String {
            val str = us.string
            if (str.length == 1 && str.get(0) == '\u0000') {
                return "'\\0'"
            }
            return str
        }

        private fun appendFormula(sb: StringBuffer, label: String?, f: Formula?) {
            sb.append(label)

            if (f == null) {
                sb.append("<empty>\n")
                return
            }
            val ptgs = f.tokens
            sb.append('\n')
            for (i in ptgs.indices) {
                sb.append('\t').append(ptgs[i].toString()).append('\n')
            }
        }

        /**
         * When entered via the UI, Excel translates empty string into "\0"
         * While it is possible to encode the title/text as empty string (Excel doesn't exactly crash),
         * the resulting tool-tip text / message box looks wrong.  It is best to do the same as the
         * Excel UI and encode 'not present' as "\0".
         */
        private fun resolveTitleText(str: String?): UnicodeString {
            if (str == null || str.length < 1) {
                return NULL_TEXT_STRING
            }
            return UnicodeString(str)
        }

        private fun readUnicodeString(`in`: RecordInputStream): UnicodeString {
            return UnicodeString(`in`)
        }

        private fun serializeUnicodeString(us: UnicodeString, out: LittleEndianOutput) {
            writeUnicodeString(out, us.string)
        }

        private fun getUnicodeStringSize(us: UnicodeString): Int {
            val str = us.string
            return 3 + str.length * (if (hasMultibyte(str)) 2 else 1)
        }
    }
}
