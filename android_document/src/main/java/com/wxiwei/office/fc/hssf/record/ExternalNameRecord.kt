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

import com.wxiwei.office.constant.fc.ConstantValueParser.encode
import com.wxiwei.office.constant.fc.ConstantValueParser.getEncodedSize
import com.wxiwei.office.constant.fc.ConstantValueParser.parse
import com.wxiwei.office.fc.hssf.formula.Formula
import com.wxiwei.office.fc.hssf.formula.Formula.Companion.getTokens
import com.wxiwei.office.fc.hssf.formula.Formula.Companion.read
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.fc.util.StringUtil
import com.wxiwei.office.fc.util.StringUtil.readUnicodeString


/**
 * EXTERNALNAME (0x0023)
 *
 *
 * 
 * @author Josh Micich
 */
class ExternalNameRecord : StandardRecord {
    private var field_1_option_flag: Short = 0
    private var field_2_ixals: Short
    private var field_3_not_used: Short = 0
    private var field_4_name: String? = null
    private var field_5_name_definition: Formula? = null

    /**
     * 'rgoper' / 'Last received results of the DDE link'
     * (seems to be only applicable to DDE links)<br></br>
     * Logically this is a 2-D array, which has been flattened into 1-D array here.
     */
    private var _ddeValues: Array<Any?> = emptyArray()

    /**
     * (logical) number of columns in the [._ddeValues] array
     */
    private var _nColumns = 0

    /**
     * (logical) number of rows in the [._ddeValues] array
     */
    private var _nRows = 0

    /**
     * Convenience Function to determine if the name is a built-in name
     */
    fun isBuiltInName(): Boolean {
        return (field_1_option_flag.toInt() and OPT_BUILTIN_NAME) != 0
    }

    /**
     * For OLE and DDE, links can be either 'automatic' or 'manual'
     */
    fun isAutomaticLink(): Boolean {
        return (field_1_option_flag.toInt() and OPT_AUTOMATIC_LINK) != 0
    }

    /**
     * only for OLE and DDE
     */
    fun isPicureLink(): Boolean {
        return (field_1_option_flag.toInt() and OPT_PICTURE_LINK) != 0
    }

    /**
     * DDE links only. If `true`, this denotes the 'StdDocumentName'
     */
    fun isStdDocumentNameIdentifier(): Boolean {
        return (field_1_option_flag.toInt() and OPT_STD_DOCUMENT_NAME) != 0
    }

    fun isOLELink(): Boolean {
        return (field_1_option_flag.toInt() and OPT_OLE_LINK) != 0
    }

    fun isIconifiedPictureLink(): Boolean {
        return (field_1_option_flag.toInt() and OPT_ICONIFIED_PICTURE_LINK) != 0
    }

    /**
     * @return the standard String representation of this name
     */
    fun getText(): String {
        return field_4_name!!
    }

    fun setText(str: String) {
        field_4_name = str
    }

    /**
     * If this is a local name, then this is the (1 based)
     * index of the name of the Sheet this refers to, as
     * defined in the preceeding [SupBookRecord].
     * If it isn't a local name, then it must be zero.
     */
    fun getIx(): Short {
        return field_2_ixals
    }

    fun setIx(ix: Short) {
        field_2_ixals = ix
    }

    fun getParsedExpression(): Array<Ptg?>? {
        return getTokens(field_5_name_definition)
    }

    fun setParsedExpression(ptgs: Array<Ptg?>?) {
        field_5_name_definition = Formula.create(ptgs)
    }


    override fun getDataSize(): Int {
        var result = 2 + 4 // short and int
        result += StringUtil.getEncodedSize(field_4_name!!) - 1 //size is byte, not short 

        if (!isOLELink() && !isStdDocumentNameIdentifier()) {
            if (isAutomaticLink()) {
                result += 3 // byte, short
                result += getEncodedSize(_ddeValues)
            } else {
                result += field_5_name_definition!!.encodedSize
            }
        }
        return result
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(field_1_option_flag.toInt())
        out.writeShort(field_2_ixals.toInt())
        out.writeShort(field_3_not_used.toInt())

        out.writeByte(field_4_name!!.length)
        StringUtil.writeUnicodeStringFlagAndData(out, field_4_name!!)

        if (!isOLELink() && !isStdDocumentNameIdentifier()) {
            if (isAutomaticLink()) {
                out.writeByte(_nColumns - 1)
                out.writeShort(_nRows - 1)
                encode(out, _ddeValues)
            } else {
                field_5_name_definition!!.serialize(out)
            }
        }
    }

    constructor() {
        field_2_ixals = 0
    }

    constructor(`in`: RecordInputStream) {
        field_1_option_flag = `in`.readShort()
        field_2_ixals = `in`.readShort()
        field_3_not_used = `in`.readShort()

        val numChars = `in`.readUByte()
        field_4_name = readUnicodeString(`in`, numChars)

        // the record body can take different forms.
        // The form is dictated by the values of 3-th and 4-th bits in field_1_option_flag
        if (!isOLELink() && !isStdDocumentNameIdentifier()) {
            // another switch: the fWantAdvise bit specifies whether the body describes
            // an external defined name or a DDE data item
            if (isAutomaticLink()) {
                if (`in`.available() > 0) {
                    //body specifies DDE data item
                    val nColumns = `in`.readUByte() + 1
                    val nRows = `in`.readShort() + 1

                    val totalCount = nRows * nColumns
                    _ddeValues = parse(`in`, totalCount)
                    _nColumns = nColumns
                    _nRows = nRows
                }
            } else {
                //body specifies an external defined name
                val formulaLen = `in`.readUShort()
                field_5_name_definition = read(formulaLen, `in`)
            }
        }
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun toString(): String {
        val sb = StringBuffer()
        sb.append("[EXTERNALNAME]\n")
        sb.append("    .options      = ").append(field_1_option_flag.toInt()).append("\n")
        sb.append("    .ix      = ").append(field_2_ixals.toInt()).append("\n")
        sb.append("    .name    = ").append(field_4_name).append("\n")
        if (field_5_name_definition != null) {
            val ptgs = field_5_name_definition!!.tokens
            for (i in ptgs.indices) {
                val ptg = ptgs[i]
                if (ptg != null) {
                    sb.append(ptg.toString()).append(ptg.rVAType).append("\n")
                }
            }
        }
        sb.append("[/EXTERNALNAME]\n")
        return sb.toString()
    }

    companion object {
        const val sid: Short = 0x0023 // as per BIFF8. (some old versions used 0x223)

        private const val OPT_BUILTIN_NAME = 0x0001
        private const val OPT_AUTOMATIC_LINK = 0x0002 // m$ doc calls this fWantAdvise
        private const val OPT_PICTURE_LINK = 0x0004
        private const val OPT_STD_DOCUMENT_NAME = 0x0008 //fOle
        private const val OPT_OLE_LINK = 0x0010 //fOleLink

        //	private static final int OPT_CLIP_FORMAT_MASK      = 0x7FE0;
        private const val OPT_ICONIFIED_PICTURE_LINK = 0x8000
    }
}
