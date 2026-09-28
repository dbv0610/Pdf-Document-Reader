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
package com.wxiwei.office.fc.hssf.formula.ptg

import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * This ptg indicates a data table.
 * It only occurs in a FORMULA record, never in an
 * ARRAY or NAME record.  When ptgTbl occurs in a
 * formula, it is the only token in the formula.
 * 
 * This indicates that the cell containing the
 * formula is an interior cell in a data table;
 * the table description is found in a TABLE
 * record. Rows and columns which contain input
 * values to be substituted in the table do
 * not contain ptgTbl.
 * See page 811 of the june 08 binary docs.
 */
class TblPtg(`in`: LittleEndianInput) : ControlPtg() {
    /** The row number of the upper left corner  */
    val row: Int

    /** The column number of the upper left corner  */
    val column: Int

    init {
        this.row = `in`.readUShort()
        this.column = `in`.readUShort()
    }

    override fun write(out: LittleEndianOutput) {
        out.writeByte(sid + ptgClass)
        out.writeShort(this.row)
        out.writeShort(this.column)
    }

    override val size: Int get() {
        return SIZE
    }

    override fun toFormulaString(): String? {
        // table(....)[][]
        throw RuntimeException("Table and Arrays are not yet supported")
    }

    override fun toString(): String {
        val buffer =
            StringBuffer("[Data Table - Parent cell is an interior cell in a data table]\n")
        buffer.append("top left row = ").append(this.row).append("\n")
        buffer.append("top left col = ").append(this.column).append("\n")
        return buffer.toString()
    }

    companion object {
        private const val SIZE = 5
        const val sid: Short = 0x02
    }
}
