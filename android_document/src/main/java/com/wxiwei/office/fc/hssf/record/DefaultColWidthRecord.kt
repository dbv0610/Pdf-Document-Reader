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
 * Title:        Default Column Width Record (0x0055) <P>
 * Description:  Specifies the default width for columns that have no specific
 * width set.</P><P>
 * REFERENCE:  PG 302 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
</P> */
class DefaultColWidthRecord : StandardRecord {
    private var field_1_col_width: Int

    constructor() {
        field_1_col_width = DEFAULT_COLUMN_WIDTH
    }

    constructor(`in`: RecordInputStream) {
        field_1_col_width = `in`.readUShort()
    }

    /**
     * set the default column width
     * @param width defaultwidth for columns
     */
    fun setColWidth(width: Int) {
        field_1_col_width = width
    }

    /**
     * get the default column width
     * @return defaultwidth for columns
     */
    fun getColWidth(): Int {
        return field_1_col_width
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[DEFAULTCOLWIDTH]\n")
        buffer.append("    .colwidth      = ")
            .append(Integer.toHexString(getColWidth())).append("\n")
        buffer.append("[/DEFAULTCOLWIDTH]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(getColWidth())
    }

    override fun getDataSize(): Int {
        return 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = DefaultColWidthRecord()
        rec.field_1_col_width = field_1_col_width
        return rec
    }

    companion object {
        const val sid: Short = 0x0055

        /**
         * The default column width is 8 characters
         */
        const val DEFAULT_COLUMN_WIDTH: Int = 0x0008
    }
}
