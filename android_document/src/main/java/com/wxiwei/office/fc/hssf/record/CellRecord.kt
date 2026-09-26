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
 * Base class for all cell value records (implementors of [CellValueRecordInterface]).
 * Subclasses are expected to manage the cell data values (of various types).
 * 
 * @author Josh Micich
 */
abstract class CellRecord : StandardRecord, CellValueRecordInterface {
    private var _rowIndex = 0
    private var _columnIndex = 0
    private var _formatIndex = 0

    protected constructor()

    protected constructor(`in`: RecordInputStream) {
        _rowIndex = `in`.readUShort()
        _columnIndex = `in`.readUShort()
        _formatIndex = `in`.readUShort()
    }

    override var row: Int
        get() = _rowIndex
        set(row) {
            _rowIndex = row
        }

    override var column: Short
        get() = _columnIndex.toShort()
        set(col) {
            _columnIndex = col.toInt()
        }

    /**
     * the index to the ExtendedFormat
     *
     * @see ExtendedFormatRecord
     */
    override var xFIndex: Short
        get() = _formatIndex.toShort()
        set(xf) {
            _formatIndex = xf.toInt()
        }

    override fun toString(): String {
        val sb = StringBuilder()
        val recordName = getRecordName()

        sb.append("[").append(recordName).append("]\n")
        sb.append("    .row    = ").append(shortToHex(row)).append("\n")
        sb.append("    .col    = ").append(shortToHex(column.toInt())).append("\n")
        sb.append("    .xfindex= ").append(shortToHex(xFIndex.toInt())).append("\n")
        appendValueText(sb)
        sb.append("\n")
        sb.append("[/").append(recordName).append("]\n")
        return sb.toString()
    }

    /**
     * Append specific debug info (used by [.toString] for the value
     * contained in this record. Trailing new-line should not be appended
     * (superclass does that).
     */
    protected abstract fun appendValueText(sb: StringBuilder)

    /**
     * Gets the debug info BIFF record type name (used by [.toString].
     */
    protected abstract fun getRecordName(): String

    /**
     * writes out the value data for this cell record
     */
    protected abstract fun serializeValue(out: LittleEndianOutput)

    /**
     * @return the size (in bytes) of the value data for this cell record
     */
    protected abstract fun getValueDataSize(): Int

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(row)
        out.writeShort(column.toInt())
        out.writeShort(xFIndex.toInt())
        serializeValue(out)
    }

    override fun getDataSize(): Int {
        return 6 + getValueDataSize()
    }

    protected fun copyBaseFields(rec: CellRecord) {
        rec._rowIndex = _rowIndex
        rec._columnIndex = _columnIndex
        rec._formatIndex = _formatIndex
    }
}
