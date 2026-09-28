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

import com.wxiwei.office.fc.util.HexDump.intToHex
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Title:        DBCell Record (0x00D7)
 *
 *
 * Description:  Used by Excel and other MS apps to quickly find rows in the sheets.<P>
 * REFERENCE:  PG 299/440 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height
</P> */
class DBCellRecord : StandardRecord {
    class Builder {
        private var _cellOffsets: ShortArray
        private var _nCellOffsets = 0

        init {
            _cellOffsets = ShortArray(4)
        }

        fun addCellOffset(cellRefOffset: Int) {
            if (_cellOffsets.size <= _nCellOffsets) {
                val temp = ShortArray(_nCellOffsets * 2)
                System.arraycopy(_cellOffsets, 0, temp, 0, _nCellOffsets)
                _cellOffsets = temp
            }
            _cellOffsets[_nCellOffsets] = cellRefOffset.toShort()
            _nCellOffsets++
        }

        fun build(rowOffset: Int): DBCellRecord {
            val cellOffsets = ShortArray(_nCellOffsets)
            System.arraycopy(_cellOffsets, 0, cellOffsets, 0, _nCellOffsets)
            return DBCellRecord(rowOffset, cellOffsets)
        }
    }

    /**
     * offset from the start of this DBCellRecord to the start of the first cell in
     * the next DBCell block.
     */
    private val field_1_row_offset: Int
    private val field_2_cell_offsets: ShortArray

    internal constructor(rowOffset: Int, cellOffsets: ShortArray) {
        field_1_row_offset = rowOffset
        field_2_cell_offsets = cellOffsets
    }

    constructor(`in`: RecordInputStream) {
        field_1_row_offset = `in`.readUShort()
        val size = `in`.remaining()
        field_2_cell_offsets = ShortArray(size / 2)

        for (i in field_2_cell_offsets.indices) {
            field_2_cell_offsets[i] = `in`.readShort()
        }
    }


    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[DBCELL]\n")
        buffer.append("    .rowoffset = ").append(intToHex(field_1_row_offset)).append("\n")
        for (k in field_2_cell_offsets.indices) {
            buffer.append("    .cell_").append(k).append(" = ")
                .append(shortToHex(field_2_cell_offsets[k].toInt())).append("\n")
        }
        buffer.append("[/DBCELL]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeInt(field_1_row_offset)
        for (k in field_2_cell_offsets.indices) {
            out.writeShort(field_2_cell_offsets[k].toInt())
        }
    }

    override fun getDataSize(): Int {
        return 4 + field_2_cell_offsets.size * 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        // safe because immutable
        return this
    }

    companion object {
        const val sid: Short = 0x00D7
        const val BLOCK_SIZE: Int = 32

        /**
         * @return the size of the group of <tt>DBCellRecord</tt>s needed to encode
         * the specified number of blocks and rows
         */
        fun calculateSizeOfRecords(nBlocks: Int, nRows: Int): Int {
            // One DBCell per block.
            // 8 bytes per DBCell (non variable section)
            // 2 bytes per row reference
            return nBlocks * 8 + nRows * 2
        }
    }
}
