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
 * Title:        Guts Record <P>
 * Description:  Row/column gutter sizes </P><P>
 * REFERENCE:  PG 320 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
</P> */
class GutsRecord

    : StandardRecord {
    private var field_1_left_row_gutter: Short = 0 // size of the row gutter to the left of the rows
    private var field_2_top_col_gutter: Short = 0 // size of the column gutter above the columns
    private var field_3_row_level_max: Short = 0 // maximum outline level for row gutters
    private var field_4_col_level_max: Short = 0 // maximum outline level for column gutters

    constructor()

    constructor(`in`: RecordInputStream) {
        field_1_left_row_gutter = `in`.readShort()
        field_2_top_col_gutter = `in`.readShort()
        field_3_row_level_max = `in`.readShort()
        field_4_col_level_max = `in`.readShort()
    }

    /**
     * set the size of the gutter that appears at the left of the rows
     * 
     * @param gut  gutter size in screen units
     */
    fun setLeftRowGutter(gut: Short) {
        field_1_left_row_gutter = gut
    }

    /**
     * set the size of the gutter that appears at the above the columns
     * 
     * @param gut  gutter size in screen units
     */
    fun setTopColGutter(gut: Short) {
        field_2_top_col_gutter = gut
    }

    /**
     * set the maximum outline level for the row gutter.
     * 
     * @param max  maximum outline level
     */
    fun setRowLevelMax(max: Short) {
        field_3_row_level_max = max
    }

    /**
     * set the maximum outline level for the col gutter.
     * 
     * @param max  maximum outline level
     */
    fun setColLevelMax(max: Short) {
        field_4_col_level_max = max
    }

    /**
     * get the size of the gutter that appears at the left of the rows
     * 
     * @return gutter size in screen units
     */
    fun getLeftRowGutter(): Short {
        return field_1_left_row_gutter
    }

    /**
     * get the size of the gutter that appears at the above the columns
     * 
     * @return gutter size in screen units
     */
    fun getTopColGutter(): Short {
        return field_2_top_col_gutter
    }

    /**
     * get the maximum outline level for the row gutter.
     * 
     * @return maximum outline level
     */
    fun getRowLevelMax(): Short {
        return field_3_row_level_max
    }

    /**
     * get the maximum outline level for the col gutter.
     * 
     * @return maximum outline level
     */
    fun getColLevelMax(): Short {
        return field_4_col_level_max
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[GUTS]\n")
        buffer.append("    .leftgutter     = ")
            .append(Integer.toHexString(getLeftRowGutter().toInt())).append("\n")
        buffer.append("    .topgutter      = ")
            .append(Integer.toHexString(getTopColGutter().toInt())).append("\n")
        buffer.append("    .rowlevelmax    = ")
            .append(Integer.toHexString(getRowLevelMax().toInt())).append("\n")
        buffer.append("    .collevelmax    = ")
            .append(Integer.toHexString(getColLevelMax().toInt())).append("\n")
        buffer.append("[/GUTS]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(getLeftRowGutter().toInt())
        out.writeShort(getTopColGutter().toInt())
        out.writeShort(getRowLevelMax().toInt())
        out.writeShort(getColLevelMax().toInt())
    }

    override fun getDataSize(): Int {
        return 8
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = GutsRecord()
        rec.field_1_left_row_gutter = field_1_left_row_gutter
        rec.field_2_top_col_gutter = field_2_top_col_gutter
        rec.field_3_row_level_max = field_3_row_level_max
        rec.field_4_col_level_max = field_4_col_level_max
        return rec
    }

    companion object {
        const val sid: Short = 0x80
    }
}
