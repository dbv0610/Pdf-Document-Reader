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

import com.wxiwei.office.fc.util.IntList
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Title:        Index Record (0x020B)
 *
 *
 * Description:  Occurs right after BOF, tells you where the DBCELL records are for a sheet
 * Important for locating cells
 *
 *
 * NOT USED IN THIS RELEASE
 * REFERENCE:  PG 323 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)
 *
 *
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 */
class IndexRecord : StandardRecord {
    private var field_2_first_row = 0 // first row on the sheet
    private var field_3_last_row_add1 = 0 // last row
    private var field_4_zero = 0 // supposed to be zero
    private var field_5_dbcells: IntList? = null // array of offsets to DBCELL records

    constructor()

    constructor(`in`: RecordInputStream) {
        val field_1_zero = `in`.readInt()
        if (field_1_zero != 0) {
            throw RecordFormatException("Expected zero for field 1 but got " + field_1_zero)
        }
        field_2_first_row = `in`.readInt()
        field_3_last_row_add1 = `in`.readInt()
        field_4_zero = `in`.readInt()

        val nCells = `in`.remaining() / 4
        field_5_dbcells = IntList(nCells)
        for (i in 0..<nCells) {
            field_5_dbcells!!.add(`in`.readInt())
        }
    }

    fun setFirstRow(row: Int) {
        field_2_first_row = row
    }

    fun setLastRowAdd1(row: Int) {
        field_3_last_row_add1 = row
    }

    fun addDbcell(cell: Int) {
        if (field_5_dbcells == null) {
            field_5_dbcells = IntList()
        }
        field_5_dbcells!!.add(cell)
    }

    fun setDbcell(cell: Int, value: Int) {
        field_5_dbcells!!.set(cell, value)
    }

    fun getFirstRow(): Int {
        return field_2_first_row
    }

    fun getLastRowAdd1(): Int {
        return field_3_last_row_add1
    }

    fun getNumDbcells(): Int {
        if (field_5_dbcells == null) {
            return 0
        }
        return field_5_dbcells!!.size()
    }

    fun getDbcellAt(cellnum: Int): Int {
        return field_5_dbcells!!.get(cellnum)
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[INDEX]\n")
        buffer.append("    .firstrow       = ")
            .append(Integer.toHexString(getFirstRow())).append("\n")
        buffer.append("    .lastrowadd1    = ")
            .append(Integer.toHexString(getLastRowAdd1())).append("\n")
        for (k in 0..<getNumDbcells()) {
            buffer.append("    .dbcell_").append(k).append(" = ")
                .append(Integer.toHexString(getDbcellAt(k))).append("\n")
        }
        buffer.append("[/INDEX]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeInt(0)
        out.writeInt(getFirstRow())
        out.writeInt(getLastRowAdd1())
        out.writeInt(field_4_zero)
        for (k in 0..<getNumDbcells()) {
            out.writeInt(getDbcellAt(k))
        }
    }

    override fun getDataSize(): Int {
        return (16 // 4 ints
                + getNumDbcells() * 4)
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = IndexRecord()
        rec.field_2_first_row = field_2_first_row
        rec.field_3_last_row_add1 = field_3_last_row_add1
        rec.field_4_zero = field_4_zero
        rec.field_5_dbcells = IntList()
        rec.field_5_dbcells!!.addAll(field_5_dbcells!!)
        return rec
    }

    companion object {
        const val sid: Short = 0x020B

        /**
         * @return the size of an INdexRecord when it needs to index the specified number of blocks
         */
        fun getRecordSizeForBlockCount(blockCount: Int): Int {
            return 20 + 4 * blockCount
        }
    }
}
