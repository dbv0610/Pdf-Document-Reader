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

import com.wxiwei.office.fc.hssf.util.CellRangeAddress8Bit
import com.wxiwei.office.fc.util.HexDump.byteToHex
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * Title:        Selection Record (0x001D)<P>
 * Description:  shows the user's selection on the sheet
 * for write set num refs to 0</P><P>
 * 
 * REFERENCE:  PG 291 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @author Glen Stampoultzis (glens at apache.org)
</P> */
class SelectionRecord : StandardRecord {
    private var field_1_pane: Byte
    private var field_2_row_active_cell: Int
    private var field_3_col_active_cell: Int
    private var field_4_active_cell_ref_index: Int
    private var field_6_refs: Array<CellRangeAddress8Bit?>

    /**
     * Creates a default selection record (cell A1, in pane ID 3)
     */
    constructor(activeCellRow: Int, activeCellCol: Int) {
        field_1_pane = 3 // pane id 3 is always present.  see OOO sec 5.75 'PANE'
        field_2_row_active_cell = activeCellRow
        field_3_col_active_cell = activeCellCol
        field_4_active_cell_ref_index = 0
        field_6_refs = arrayOf<CellRangeAddress8Bit?>(
            CellRangeAddress8Bit(activeCellRow, activeCellRow, activeCellCol, activeCellCol),
        )
    }

    constructor(`in`: RecordInputStream) {
        field_1_pane = `in`.readByte()
        field_2_row_active_cell = `in`.readUShort()
        field_3_col_active_cell = `in`.readShort().toInt()
        field_4_active_cell_ref_index = `in`.readShort().toInt()
        val field_5_num_refs = `in`.readUShort()

        field_6_refs = arrayOfNulls<CellRangeAddress8Bit>(field_5_num_refs)
        for (i in field_6_refs.indices) {
            field_6_refs[i] = CellRangeAddress8Bit(`in`)
        }
    }

    /**
     * set which window pane this is for
     */
    fun setPane(pane: Byte) {
        field_1_pane = pane
    }

    /**
     * set the active cell's row
     * @param row number of active cell
     */
    fun setActiveCellRow(row: Int) {
        field_2_row_active_cell = row
    }

    /**
     * set the active cell's col
     * @param col number of active cell
     */
    fun setActiveCellCol(col: Short) {
        field_3_col_active_cell = col.toInt()
    }

    /**
     * set the active cell's reference number
     * @param ref number of active cell
     */
    fun setActiveCellRef(ref: Short) {
        field_4_active_cell_ref_index = ref.toInt()
    }

    /**
     * @return the pane ID which window pane this is for
     */
    fun getPane(): Byte {
        return field_1_pane
    }

    /**
     * get the active cell's row
     * @return row number of active cell
     */
    fun getActiveCellRow(): Int {
        return field_2_row_active_cell
    }

    /**
     * get the active cell's col
     * @return col number of active cell
     */
    fun getActiveCellCol(): Int {
        return field_3_col_active_cell
    }

    /**
     * get the active cell's reference number
     * @return ref number of active cell
     */
    fun getActiveCellRef(): Int {
        return field_4_active_cell_ref_index
    }

    override fun toString(): String {
        val sb = StringBuffer()

        sb.append("[SELECTION]\n")
        sb.append("    .pane            = ").append(byteToHex(getPane().toInt())).append("\n")
        sb.append("    .activecellrow   = ").append(shortToHex(getActiveCellRow())).append("\n")
        sb.append("    .activecellcol   = ").append(shortToHex(getActiveCellCol())).append("\n")
        sb.append("    .activecellref   = ").append(shortToHex(getActiveCellRef())).append("\n")
        sb.append("    .numrefs         = ").append(shortToHex(field_6_refs.size)).append("\n")
        sb.append("[/SELECTION]\n")
        return sb.toString()
    }

    override fun getDataSize(): Int {
        return (9 // 1 byte + 4 shorts
                + CellRangeAddress8Bit.getEncodedSize(field_6_refs.size))
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeByte(getPane().toInt())
        out.writeShort(getActiveCellRow())
        out.writeShort(getActiveCellCol())
        out.writeShort(getActiveCellRef())
        val nRefs = field_6_refs.size
        out.writeShort(nRefs)
        for (i in field_6_refs.indices) {
            field_6_refs[i]!!.serialize(out)
        }
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = SelectionRecord(field_2_row_active_cell, field_3_col_active_cell)
        rec.field_1_pane = field_1_pane
        rec.field_4_active_cell_ref_index = field_4_active_cell_ref_index
        rec.field_6_refs = field_6_refs
        return rec
    }

    companion object {
        const val sid: Short = 0x001D
    }
}
