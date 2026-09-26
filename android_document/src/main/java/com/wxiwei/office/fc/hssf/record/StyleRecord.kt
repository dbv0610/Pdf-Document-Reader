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

import com.wxiwei.office.fc.util.BitFieldFactory.Companion.getInstance
import com.wxiwei.office.fc.util.HexDump.byteToHex
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.fc.util.StringUtil.hasMultibyte
import com.wxiwei.office.fc.util.StringUtil.putCompressedUnicode
import com.wxiwei.office.fc.util.StringUtil.putUnicodeLE
import com.wxiwei.office.fc.util.StringUtil.readCompressedUnicode
import com.wxiwei.office.fc.util.StringUtil.readUnicodeLE

/**
 * Title:        Style Record (0x0293)
 *
 *
 * Description:  Describes a builtin to the gui or user defined style<P>
 * REFERENCE:  PG 390 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author aviks : string fixes for UserDefined Style
</P> */
class StyleRecord : StandardRecord {
    /** shared by both user defined and built-in styles  */
    private var field_1_xf_index: Int = 0

    // only for built in styles
    private var field_2_builtin_style = 0
    private var field_3_outline_style_level = 0

    // only for user defined styles
    private var field_3_stringHasMultibyte = false
    private var field_4_name: String? = null

    /**
     * creates a new style record, initially set to 'built-in'
     */
    constructor() {
        field_1_xf_index = isBuiltinFlag.set(field_1_xf_index)
    }

    constructor(`in`: RecordInputStream) {
        field_1_xf_index = `in`.readShort().toInt()
        if (isBuiltin()) {
            field_2_builtin_style = `in`.readByte().toInt()
            field_3_outline_style_level = `in`.readByte().toInt()
        } else {
            val field_2_name_length = `in`.readShort().toInt()

            if (`in`.remaining() < 1) {
                // Some files from Crystal Reports lack the is16BitUnicode byte
                //  the remaining fields, which is naughty
                if (field_2_name_length != 0) {
                    throw RecordFormatException("Ran out of data reading style record")
                }
                // guess this is OK if the string length is zero
                field_4_name = ""
            } else {
                field_3_stringHasMultibyte = `in`.readByte().toInt() != 0x00
                if (field_3_stringHasMultibyte) {
                    field_4_name = readUnicodeLE(`in`, field_2_name_length)
                } else {
                    field_4_name = readCompressedUnicode(`in`, field_2_name_length)
                }
            }
        }
    }

    /**
     * set the actual index of the style extended format record
     * @param xfIndex of the xf record
     */
    fun setXFIndex(xfIndex: Int) {
        field_1_xf_index = styleIndexMask.setValue(field_1_xf_index, xfIndex)
    }

    /**
     * get the actual index of the style extended format record
     * @see .getXFIndex
     * @return index of the xf record
     */
    fun getXFIndex(): Int {
        return styleIndexMask.getValue(field_1_xf_index)
    }

    /**
     * set the style's name
     * @param name of the style
     */
    fun setName(name: String) {
        field_4_name = name
        field_3_stringHasMultibyte = hasMultibyte(name)
        field_1_xf_index = isBuiltinFlag.clear(field_1_xf_index)
    }

    /**
     * if this is a builtin style set the number of the built in style
     * @param  builtinStyleId style number (0-7)
     */
    fun setBuiltinStyle(builtinStyleId: Int) {
        field_1_xf_index = isBuiltinFlag.set(field_1_xf_index)
        field_2_builtin_style = builtinStyleId
    }

    /**
     * set the row or column level of the style (if builtin 1||2)
     */
    fun setOutlineStyleLevel(level: Int) {
        field_3_outline_style_level = level and 0x00FF
    }

    fun isBuiltin(): Boolean {
        return isBuiltinFlag.isSet(field_1_xf_index)
    }

    /**
     * get the style's name
     * @return name of the style
     */
    fun getName(): String {
        return field_4_name!!
    }

    override fun toString(): String {
        val sb = StringBuffer()

        sb.append("[STYLE]\n")
        sb.append("    .xf_index_raw =").append(shortToHex(field_1_xf_index)).append("\n")
        sb.append("        .type     =").append(if (isBuiltin()) "built-in" else "user-defined")
            .append("\n")
        sb.append("        .xf_index =").append(shortToHex(getXFIndex())).append("\n")
        if (isBuiltin()) {
            sb.append("    .builtin_style=").append(byteToHex(field_2_builtin_style)).append("\n")
            sb.append("    .outline_level=").append(byteToHex(field_3_outline_style_level))
                .append("\n")
        } else {
            sb.append("    .name        =").append(getName()).append("\n")
        }
        sb.append("[/STYLE]\n")
        return sb.toString()
    }


    override fun getDataSize(): Int {
        if (isBuiltin()) {
            return 4 // short, byte, byte
        }
        return (2 // short xf index 
                + 3 // str len + flag 
                + field_4_name!!.length * (if (field_3_stringHasMultibyte) 2 else 1))
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(field_1_xf_index)
        if (isBuiltin()) {
            out.writeByte(field_2_builtin_style)
            out.writeByte(field_3_outline_style_level)
        } else {
            out.writeShort(field_4_name!!.length)
            out.writeByte(if (field_3_stringHasMultibyte) 0x01 else 0x00)
            if (field_3_stringHasMultibyte) {
                putUnicodeLE(getName(), out)
            } else {
                putCompressedUnicode(getName(), out)
            }
        }
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    companion object {
        const val sid: Short = 0x0293

        private val styleIndexMask = getInstance(0x0FFF)
        private val isBuiltinFlag = getInstance(0x8000)
    }
}
