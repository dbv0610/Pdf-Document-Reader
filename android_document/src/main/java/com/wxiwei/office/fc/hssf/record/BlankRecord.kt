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

import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Title:        Blank cell record (0x0201) <P>
 * Description:  Represents a column in a row with no value but with styling.</P><P>
 * REFERENCE:  PG 287 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
</P> */
class BlankRecord : StandardRecord, CellValueRecordInterface {
    private var field_1_row = 0
    private var field_2_col: Short = 0
    private var field_3_xf: Short = 0

    /** Creates a new instance of BlankRecord  */
    constructor()

    constructor(`in`: RecordInputStream) {
        field_1_row = `in`.readUShort()
        field_2_col = `in`.readShort()
        field_3_xf = `in`.readShort()
    }

    /**
     * the row this cell occurs on
     */
    override var row: Int
        get() = field_1_row
        set(row) {
            field_1_row = row
        }

    /**
     * the column this cell defines within the row
     */
    override var column: Short
        get() = field_2_col
        set(col) {
            field_2_col = col
        }

    /**
     * the index of the extended format record to style this cell with
     * @see ExtendedFormatRecord
     */
    override var xFIndex: Short
        get() = field_3_xf
        set(xf) {
            field_3_xf = xf
        }

    /**
     * return the non static version of the id for this record.
     */
    override fun getSid(): Short {
        return Companion.sid
    }

    override fun toString(): String {
        val sb = StringBuffer()

        sb.append("[BLANK]\n")
        sb.append("    row= ").append(shortToHex(row)).append("\n")
        sb.append("    col= ").append(shortToHex(column.toInt())).append("\n")
        sb.append("    xf = ").append(shortToHex(xFIndex.toInt())).append("\n")
        sb.append("[/BLANK]\n")
        return sb.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(row)
        out.writeShort(column.toInt())
        out.writeShort(xFIndex.toInt())
    }

    override fun getDataSize(): Int {
        return 6
    }

    override fun clone(): Any {
        val rec = BlankRecord()
        rec.field_1_row = field_1_row
        rec.field_2_col = field_2_col
        rec.field_3_xf = field_3_xf
        return rec
    }

    companion object {
        const val sid: Short = 0x0201
    }
}
