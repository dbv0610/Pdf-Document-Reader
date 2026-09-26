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
 * Title:        Dimensions Record<P>
 * Description:  provides the minumum and maximum bounds
 * of a sheet.</P><P>
 * REFERENCE:  PG 303 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
</P> */
class DimensionsRecord

    : StandardRecord {
    private var field_1_first_row = 0
    private var field_2_last_row = 0 // plus 1
    private var field_3_first_col: Short = 0
    private var field_4_last_col: Short = 0
    private var field_5_zero: Short = 0 // must be 0 (reserved)

    constructor()

    constructor(`in`: RecordInputStream) {
        field_1_first_row = `in`.readInt()
        field_2_last_row = `in`.readInt()
        field_3_first_col = `in`.readShort()
        field_4_last_col = `in`.readShort()
        field_5_zero = `in`.readShort()
    }

    /**
     * set the first row number for the sheet
     * @param row - first row on the sheet
     */
    fun setFirstRow(row: Int) {
        field_1_first_row = row
    }

    /**
     * set the last row number for the sheet
     * @param row - last row on the sheet
     */
    fun setLastRow(row: Int) {
        field_2_last_row = row
    }

    /**
     * set the first column number for the sheet
     * @param col  first column on the sheet
     */
    fun setFirstCol(col: Short) {
        field_3_first_col = col
    }

    /**
     * set the last col number for the sheet
     * @param col  last column on the sheet
     */
    fun setLastCol(col: Short) {
        field_4_last_col = col
    }

    /**
     * get the first row number for the sheet
     * @return row - first row on the sheet
     */
    fun getFirstRow(): Int {
        return field_1_first_row
    }

    /**
     * get the last row number for the sheet
     * @return row - last row on the sheet
     */
    fun getLastRow(): Int {
        return field_2_last_row
    }

    /**
     * get the first column number for the sheet
     * @return column - first column on the sheet
     */
    fun getFirstCol(): Short {
        return field_3_first_col
    }

    /**
     * get the last col number for the sheet
     * @return column - last column on the sheet
     */
    fun getLastCol(): Short {
        return field_4_last_col
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[DIMENSIONS]\n")
        buffer.append("    .firstrow       = ")
            .append(Integer.toHexString(getFirstRow())).append("\n")
        buffer.append("    .lastrow        = ")
            .append(Integer.toHexString(getLastRow())).append("\n")
        buffer.append("    .firstcol       = ")
            .append(Integer.toHexString(getFirstCol().toInt())).append("\n")
        buffer.append("    .lastcol        = ")
            .append(Integer.toHexString(getLastCol().toInt())).append("\n")
        buffer.append("    .zero           = ")
            .append(Integer.toHexString(field_5_zero.toInt())).append("\n")
        buffer.append("[/DIMENSIONS]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeInt(getFirstRow())
        out.writeInt(getLastRow())
        out.writeShort(getFirstCol().toInt())
        out.writeShort(getLastCol().toInt())
        out.writeShort(0.toShort().toInt())
    }

    override fun getDataSize(): Int {
        return 14
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = DimensionsRecord()
        rec.field_1_first_row = field_1_first_row
        rec.field_2_last_row = field_2_last_row
        rec.field_3_first_col = field_3_first_col
        rec.field_4_last_col = field_4_last_col
        rec.field_5_zero = field_5_zero
        return rec
    }

    companion object {
        const val sid: Short = 0x200
    }
}
