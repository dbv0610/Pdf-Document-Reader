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

import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg.Companion.readTokens
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.fc.util.StringUtil.getEncodedSize
import com.wxiwei.office.fc.util.StringUtil.readUnicodeString
import com.wxiwei.office.fc.util.StringUtil.writeUnicodeString

/**
 * This structure specifies the properties of a list or drop-down list embedded object in a sheet.
 */
class LbsDataSubRecord : SubRecord {
    /**
     * From [MS-XLS].pdf 2.5.147 FtLbsData:
     * 
     * An unsigned integer that indirectly specifies whether
     * some of the data in this structure appear in a subsequent Continue record.
     * If _cbFContinued is 0x00, all of the fields in this structure except sid and _cbFContinued
     * MUST NOT exist. If this entire structure is contained within the same record,
     * then _cbFContinued MUST be greater than or equal to the size, in bytes,
     * of this structure, not including the four bytes for the ft and _cbFContinued fields
     */
    private var _cbFContinued = 0

    /**
     * a formula that specifies the range of cell values that are the items in this list.
     */
    private var _unknownPreFormulaInt = 0
    private var _linkPtg: Ptg? = null
    private var _unknownPostFormulaByte: Byte? = null

    /**
     * An unsigned integer that specifies the number of items in the list.
     */
    private var _cLines = 0

    /**
     * An unsigned integer that specifies the one-based index of the first selected item in this list.
     * A value of 0x00 specifies there is no currently selected item.
     */
    private var _iSel = 0

    /**
     * flags that tell what data follows
     */
    private var _flags = 0

    /**
     * An ObjId that specifies the edit box associated with this list.
     * A value of 0x00 specifies that there is no edit box associated with this list.
     */
    private var _idEdit = 0

    /**
     * An optional LbsDropData that specifies properties for this dropdown control.
     * This field MUST exist if and only if the containing Obj?s cmo.ot is equal to 0x14.
     */
    private var _dropData: LbsDropData? = null

    /**
     * An optional array of strings where each string specifies an item in the list.
     * The number of elements in this array, if it exists, MUST be [._cLines]
     */
    private var _rgLines: Array<String?>? = null

    /**
     * An optional array of booleans that specifies
     * which items in the list are part of a multiple selection
     */
    private var _bsels: BooleanArray? = null

    /**
     * @param in the stream to read data from
     * @param cbFContinued the seconf short in the record header
     * @param cmoOt the containing Obj's [CommonObjectDataSubRecord.field_1_objectType]
     */
    constructor(`in`: LittleEndianInput, cbFContinued: Int, cmoOt: Int) {
        _cbFContinued = cbFContinued

        val encodedTokenLen = `in`.readUShort()
        if (encodedTokenLen > 0) {
            val formulaSize = `in`.readUShort()
            _unknownPreFormulaInt = `in`.readInt()

            val ptgs = readTokens(formulaSize, `in`)
            if (ptgs.size != 1) {
                throw RecordFormatException(
                    ("Read " + ptgs.size
                            + " tokens but expected exactly 1")
                )
            }
            _linkPtg = ptgs[0]
            when (encodedTokenLen - formulaSize - 6) {
                1 -> _unknownPostFormulaByte = `in`.readByte()
                0 -> _unknownPostFormulaByte = null
                else -> throw RecordFormatException("Unexpected leftover bytes")
            }
        }

        _cLines = `in`.readUShort()
        _iSel = `in`.readUShort()
        _flags = `in`.readUShort()
        _idEdit = `in`.readUShort()

        // From [MS-XLS].pdf 2.5.147 FtLbsData:
        // This field MUST exist if and only if the containing Obj?s cmo.ot is equal to 0x14.
        if (cmoOt == 0x14) {
            _dropData = LbsDropData(`in`)
        }

        // From [MS-XLS].pdf 2.5.147 FtLbsData:
        // This array MUST exist if and only if the fValidPlex flag (0x2) is set
        if ((_flags and 0x2) != 0) {
            _rgLines = arrayOfNulls<String>(_cLines)
            for (i in 0..<_cLines) {
                _rgLines!![i] = readUnicodeString(`in`)
            }
        }

        // bits 5-6 in the _flags specify the type
        // of selection behavior this list control is expected to support

        // From [MS-XLS].pdf 2.5.147 FtLbsData:
        // This array MUST exist if and only if the wListType field is not equal to 0.
        if (((_flags shr 4) and 0x2) != 0) {
            _bsels = BooleanArray(_cLines)
            for (i in 0..<_cLines) {
                _bsels!![i] = `in`.readByte().toInt() == 1
            }
        }
    }

    internal constructor()

    /**
     * @return true as LbsDataSubRecord is always the last sub-record
     */
    override fun isTerminating(): Boolean {
        return true
    }

    override fun getDataSize(): Int {
        var result = 2 // 2 initial shorts

        // optional link formula
        if (_linkPtg != null) {
            result += 2 // encoded Ptg size
            result += 4 // unknown int
            result += _linkPtg!!.size
            if (_unknownPostFormulaByte != null) {
                result += 1
            }
        }

        result += 4 * 2 // 4 shorts
        if (_dropData != null) {
            result += _dropData!!.getDataSize()
        }
        if (_rgLines != null) {
            for (str in _rgLines!!) {
                if (str != null) {
                    result += getEncodedSize(str)
                }
            }
        }
        if (_bsels != null) {
            result += _bsels!!.size
        }
        return result
    }

    override fun serialize(out: LittleEndianOutput) {
        out.writeShort(sid)
        out.writeShort(_cbFContinued)

        if (_linkPtg == null) {
            out.writeShort(0)
        } else {
            val formulaSize = _linkPtg!!.size
            var linkSize = formulaSize + 6
            if (_unknownPostFormulaByte != null) {
                linkSize++
            }
            out.writeShort(linkSize)
            out.writeShort(formulaSize)
            out.writeInt(_unknownPreFormulaInt)
            _linkPtg!!.write(out)
            if (_unknownPostFormulaByte != null) {
                out.writeByte(_unknownPostFormulaByte!!.toInt())
            }
        }

        out.writeShort(_cLines)
        out.writeShort(_iSel)
        out.writeShort(_flags)
        out.writeShort(_idEdit)

        if (_dropData != null) {
            _dropData!!.serialize(out)
        }

        if (_rgLines != null) {
            for (str in _rgLines!!) {
                writeUnicodeString(out, str ?: "")
            }
        }

        if (_bsels != null) {
            for (`val` in _bsels) {
                out.writeByte(if (`val`) 1 else 0)
            }
        }
    }

    override fun clone(): Any {
        return this
    }

    override fun toString(): String {
        val sb = StringBuffer(256)

        sb.append("[ftLbsData]\n")
        sb.append("    .unknownShort1 =").append(shortToHex(_cbFContinued)).append("\n")
        sb.append("    .formula        = ").append('\n')
        if (_linkPtg != null) sb.append(_linkPtg.toString()).append(_linkPtg!!.rVAType).append('\n')
        sb.append("    .nEntryCount   =").append(shortToHex(_cLines)).append("\n")
        sb.append("    .selEntryIx    =").append(shortToHex(_iSel)).append("\n")
        sb.append("    .style         =").append(shortToHex(_flags)).append("\n")
        sb.append("    .unknownShort10=").append(shortToHex(_idEdit)).append("\n")
        if (_dropData != null) sb.append('\n').append(_dropData.toString())
        sb.append("[/ftLbsData]\n")
        return sb.toString()
    }

    /**
     * 
     * @return the formula that specifies the range of cell values that are the items in this list.
     */
    fun getFormula(): Ptg? {
        return _linkPtg
    }

    /**
     * @return the number of items in the list
     */
    fun getNumberOfItems(): Int {
        return _cLines
    }

    /**
     * This structure specifies properties of the dropdown list control
     */
    class LbsDropData {
        /**
         * An unsigned integer that specifies the style of this dropdown.
         */
        var _wStyle = 0

        /**
         * An unsigned integer that specifies the number of lines to be displayed in the dropdown.
         */
        var _cLine = 0

        /**
         * An unsigned integer that specifies the smallest width in pixels allowed for the dropdown window
         */
        private var _dxMin = 0

        /**
         * a string that specifies the current string value in the dropdown
         */
        private var _str: String

        /**
         * Optional, undefined and MUST be ignored.
         * This field MUST exist if and only if the size of str in bytes is an odd number
         */
        private var _unused: Byte? = null

        constructor() {
            _str = ""
            _unused = 0
        }

        constructor(`in`: LittleEndianInput) {
            _wStyle = `in`.readUShort()
            _cLine = `in`.readUShort()
            _dxMin = `in`.readUShort()
            _str = readUnicodeString(`in`)
            if (getEncodedSize(_str) % 2 != 0) {
                _unused = `in`.readByte()
            }
        }

        /**
         * Set the style of this dropdown.
         * 
         * Possible values:
         * 
         * 
         * 0  Combo dropdown control
         * 1  Combo Edit dropdown control
         * 2  Simple dropdown control (just the dropdown button)
         * 
         */
        fun setStyle(style: Int) {
            _wStyle = style
        }

        /**
         * Set the number of lines to be displayed in the dropdown.
         */
        fun setNumLines(num: Int) {
            _cLine = num
        }

        fun serialize(out: LittleEndianOutput) {
            out.writeShort(_wStyle)
            out.writeShort(_cLine)
            out.writeShort(_dxMin)
            writeUnicodeString(out, _str)
            if (_unused != null) out.writeByte(_unused!!.toInt())
        }

        fun getDataSize(): Int {
            var size = 6
            size += getEncodedSize(_str)
            if (_unused != null) size++
            return size
        }

        override fun toString(): String {
            val sb = StringBuffer()
            sb.append("[LbsDropData]\n")
            sb.append("  ._wStyle:  ").append(_wStyle).append('\n')
            sb.append("  ._cLine:  ").append(_cLine).append('\n')
            sb.append("  ._dxMin:  ").append(_dxMin).append('\n')
            sb.append("  ._str:  ").append(_str).append('\n')
            if (_unused != null) sb.append("  ._unused:  ").append(_unused).append('\n')
            sb.append("[/LbsDropData]\n")

            return sb.toString()
        }

        companion object {
            /**
             * Combo dropdown control
             */
            var STYLE_COMBO_DROPDOWN: Int = 0

            /**
             * Combo Edit dropdown control
             */
            var STYLE_COMBO_EDIT_DROPDOWN: Int = 1

            /**
             * Simple dropdown control (just the dropdown button)
             */
            var STYLE_COMBO_SIMPLE_DROPDOWN: Int = 2
        }
    }

    companion object {
        const val sid: Int = 0x0013

        /**
         * 
         * @return a new instance of LbsDataSubRecord to construct auto-filters
         * @see com.wxiwei.office.fc.hssf.model.ComboboxShape.createObjRecord
         */
        fun newAutoFilterInstance(): LbsDataSubRecord {
            val lbs = LbsDataSubRecord()
            lbs._cbFContinued = 0x1FEE //autofilters seem to alway have this magic number
            lbs._iSel = 0x000

            lbs._flags = 0x0301
            lbs._dropData = LbsDropData()
            lbs._dropData!!._wStyle = LbsDropData.STYLE_COMBO_SIMPLE_DROPDOWN

            // the number of lines to be displayed in the dropdown
            lbs._dropData!!._cLine = 8
            return lbs
        }
    }
}
