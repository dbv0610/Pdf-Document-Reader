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

import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Title:        Default Row Height Record
 * Description:  Row height for rows with undefined or not explicitly defined
 * heights.
 * REFERENCE:  PG 301 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
</P> */
class DefaultRowHeightRecord

    : StandardRecord {
    private var field_1_option_flags: Short
    private var field_2_row_height: Short

    constructor() {
        field_1_option_flags = 0x0000
        field_2_row_height = DEFAULT_ROW_HEIGHT
    }

    constructor(`in`: RecordInputStream) {
        field_1_option_flags = `in`.readShort()
        field_2_row_height = `in`.readShort()
    }

    /**
     * set the (currently unimportant to HSSF) option flags
     * @param flags the bitmask to set
     */
    fun setOptionFlags(flags: Short) {
        field_1_option_flags = flags
    }

    /**
     * set the default row height
     * @param height    for undefined rows/rows w/undefined height
     */
    fun setRowHeight(height: Short) {
        field_2_row_height = height
    }

    /**
     * get the (currently unimportant to HSSF) option flags
     * @return flags - the current bitmask
     */
    fun getOptionFlags(): Short {
        return field_1_option_flags
    }

    /**
     * get the default row height
     * @return rowheight for undefined rows/rows w/undefined height
     */
    fun getRowHeight(): Short {
        return field_2_row_height
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[DEFAULTROWHEIGHT]\n")
        buffer.append("    .optionflags    = ")
            .append(Integer.toHexString(getOptionFlags().toInt())).append("\n")
        buffer.append("    .rowheight      = ")
            .append(Integer.toHexString(getRowHeight().toInt())).append("\n")
        buffer.append("[/DEFAULTROWHEIGHT]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(getOptionFlags().toInt())
        out.writeShort(getRowHeight().toInt())
    }

    override fun getDataSize(): Int {
        return 4
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = DefaultRowHeightRecord()
        rec.field_1_option_flags = field_1_option_flags
        rec.field_2_row_height = field_2_row_height
        return rec
    }

    companion object {
        const val sid: Short = 0x225

        /**
         * The default row height for empty rows is 255 twips (255 / 20 == 12.75 point)
         */
        const val DEFAULT_ROW_HEIGHT: Short = 0xFF
    }
}
