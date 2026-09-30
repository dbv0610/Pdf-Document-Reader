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
package com.wxiwei.office.fc.hssf.record.chart

import com.wxiwei.office.fc.hssf.formula.Formula
import com.wxiwei.office.fc.hssf.formula.Formula.Companion.read
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.record.RecordInputStream
import com.wxiwei.office.fc.hssf.record.StandardRecord
import com.wxiwei.office.fc.util.BitFieldFactory.Companion.getInstance
import com.wxiwei.office.fc.util.HexDump.byteToHex
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * Describes a linked data record.  This record refers to the series data or text.
 *
 *
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class LinkedDataRecord : StandardRecord {
    /**
     * Get the link type field for the LinkedData record.
     * 
     * @return  One of
     * LINK_TYPE_TITLE_OR_TEXT
     * LINK_TYPE_VALUES
     * LINK_TYPE_CATEGORIES
     */
    /**
     * Set the link type field for the LinkedData record.
     * 
     * @param field_1_linkType
     * One of
     * LINK_TYPE_TITLE_OR_TEXT
     * LINK_TYPE_VALUES
     * LINK_TYPE_CATEGORIES
     */
    var linkType: Byte = 0
    /**
     * Get the reference type field for the LinkedData record.
     * 
     * @return  One of
     * REFERENCE_TYPE_DEFAULT_CATEGORIES
     * REFERENCE_TYPE_DIRECT
     * REFERENCE_TYPE_WORKSHEET
     * REFERENCE_TYPE_NOT_USED
     * REFERENCE_TYPE_ERROR_REPORTED
     */
    /**
     * Set the reference type field for the LinkedData record.
     * 
     * @param field_2_referenceType
     * One of
     * REFERENCE_TYPE_DEFAULT_CATEGORIES
     * REFERENCE_TYPE_DIRECT
     * REFERENCE_TYPE_WORKSHEET
     * REFERENCE_TYPE_NOT_USED
     * REFERENCE_TYPE_ERROR_REPORTED
     */
    var referenceType: Byte = 0
    /**
     * Get the options field for the LinkedData record.
     */
    /**
     * Set the options field for the LinkedData record.
     */
    var options: Short = 0
    /**
     * Get the index number fmt record field for the LinkedData record.
     */
    /**
     * Set the index number fmt record field for the LinkedData record.
     */
    var indexNumberFmtRecord: Short = 0
    private var field_5_formulaOfLink: Formula? = null


    constructor()

    constructor(`in`: RecordInputStream) {
        this.linkType = `in`.readByte()
        this.referenceType = `in`.readByte()
        this.options = `in`.readShort()
        this.indexNumberFmtRecord = `in`.readShort()
        val encodedTokenLen = `in`.readUShort()
        field_5_formulaOfLink = read(encodedTokenLen, `in`)
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[AI]\n")
        buffer.append("    .linkType             = ").append(
            byteToHex(
                this.linkType.toInt()
            )
        ).append('\n')
        buffer.append("    .referenceType        = ").append(
            byteToHex(
                this.referenceType.toInt()
            )
        ).append('\n')
        buffer.append("    .options              = ").append(
            shortToHex(
                this.options.toInt()
            )
        ).append('\n')
        buffer.append("    .customNumberFormat   = ").append(this.isCustomNumberFormat).append('\n')
        buffer.append("    .indexNumberFmtRecord = ").append(
            shortToHex(
                this.indexNumberFmtRecord.toInt()
            )
        ).append('\n')
        buffer.append("    .formulaOfLink        = ").append('\n')
        val ptgs = field_5_formulaOfLink!!.tokens
        for (i in ptgs.indices) {
            val ptg = ptgs[i]
            if (ptg != null) {
                buffer.append(ptg.toString()).append(ptg.rVAType).append('\n')
            }
        }

        buffer.append("[/AI]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeByte(linkType.toInt())
        out.writeByte(referenceType.toInt())
        out.writeShort(options.toInt())
        out.writeShort(indexNumberFmtRecord.toInt())
        field_5_formulaOfLink!!.serialize(out)
    }

    override fun getDataSize(): Int {
        return 1 + 1 + 2 + 2 + field_5_formulaOfLink!!.encodedSize
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = LinkedDataRecord()

        rec.linkType = this.linkType
        rec.referenceType = this.referenceType
        rec.options = this.options
        rec.indexNumberFmtRecord = this.indexNumberFmtRecord
        rec.field_5_formulaOfLink = field_5_formulaOfLink!!.copy()
        return rec
    }


    val formulaOfLink: Array<Ptg?>
        /**
         * Get the formula of link field for the LinkedData record.
         */
        get() = field_5_formulaOfLink!!.tokens

    /**
     * Set the formula of link field for the LinkedData record.
     */
    fun setFormulaOfLink(ptgs: Array<Ptg?>?) {
        this.field_5_formulaOfLink = Formula.create(ptgs)
    }

    var isCustomNumberFormat: Boolean
        /**
         * true if this object has a custom number format
         * @return  the custom number format field value.
         */
        get() = customNumberFormat.isSet(options.toInt())
        /**
         * Sets the custom number format field value.
         * true if this object has a custom number format
         */
        set(value) {
            this.options =
                customNumberFormat.setShortBoolean(this.options, value)
        }

    companion object {
        const val sid: Short = 0x1051

        private val customNumberFormat = getInstance(0x1)

        const val LINK_TYPE_TITLE_OR_TEXT: Byte = 0
        const val LINK_TYPE_VALUES: Byte = 1
        const val LINK_TYPE_CATEGORIES: Byte = 2
        const val REFERENCE_TYPE_DEFAULT_CATEGORIES: Byte = 0
        const val REFERENCE_TYPE_DIRECT: Byte = 1
        const val REFERENCE_TYPE_WORKSHEET: Byte = 2
        const val REFERENCE_TYPE_NOT_USED: Byte = 3
        const val REFERENCE_TYPE_ERROR_REPORTED: Byte = 4
    }
}
