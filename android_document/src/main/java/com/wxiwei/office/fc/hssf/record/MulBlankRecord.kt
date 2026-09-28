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
 * Title:        Multiple Blank cell record(0x00BE) <P></P>
 * Description:  Represents a  set of columns in a row with no value but with styling.
 * 
 * 
 * REFERENCE:  PG 329 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P></P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Glen Stampoultzis (glens at apache.org)
 * @see BlankRecord
 */
class MulBlankRecord : StandardRecord {
    private val _row: Int
    private val _firstCol: Int
    private val _xfs: ShortArray
    private val _lastCol: Int

    constructor(row: Int, firstCol: Int, xfs: ShortArray) {
        _row = row
        _firstCol = firstCol
        _xfs = xfs
        _lastCol = firstCol + xfs.size - 1
    }

    /**
     * @return the row number of the cells this represents
     */
    fun getRow(): Int {
        return _row
    }

    /**
     * @return starting column (first cell this holds in the row). Zero based
     */
    fun getFirstColumn(): Int {
        return _firstCol
    }

    /**
     * @return ending column (last cell this holds in the row). Zero based
     */
    fun getLastColumn(): Int {
        return _lastCol
    }

    /**
     * get the number of columns this contains (last-first +1)
     * @return number of columns (last - first +1)
     */
    fun getNumColumns(): Int {
        return _lastCol - _firstCol + 1
    }

    /**
     * returns the xf index for column (coffset = column - field_2_first_col)
     * @param coffset  the column (coffset = column - field_2_first_col)
     * @return the XF index for the column
     */
    fun getXFAt(coffset: Int): Short {
        return _xfs[coffset]
    }

    /**
     * @param in the RecordInputstream to read the record from
     */
    constructor(`in`: RecordInputStream) {
        _row = `in`.readUShort()
        _firstCol = `in`.readShort().toInt()
        _xfs = parseXFs(`in`)
        _lastCol = `in`.readShort().toInt()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[MULBLANK]\n")
        buffer.append("row  = ").append(Integer.toHexString(getRow())).append("\n")
        buffer.append("firstcol  = ").append(Integer.toHexString(getFirstColumn())).append("\n")
        buffer.append(" lastcol  = ").append(Integer.toHexString(_lastCol)).append("\n")
        for (k in 0..<getNumColumns()) {
            buffer.append("xf").append(k).append("		= ").append(
                Integer.toHexString(getXFAt(k).toInt())
            ).append("\n")
        }
        buffer.append("[/MULBLANK]\n")
        return buffer.toString()
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(_row)
        out.writeShort(_firstCol)
        val nItems = _xfs.size
        for (i in 0..<nItems) {
            out.writeShort(_xfs[i].toInt())
        }
        out.writeShort(_lastCol)
    }

    override fun getDataSize(): Int {
        // 3 short fields + array of shorts
        return 6 + _xfs.size * 2
    }

    override fun clone(): Any {
        // immutable - so OK to return this
        return this
    }

    companion object {
        const val sid: Short = 0x00BE

        private fun parseXFs(`in`: RecordInputStream): ShortArray {
            val retval = ShortArray((`in`.remaining() - 2) / 2)

            for (idx in retval.indices) {
                retval[idx] = `in`.readShort()
            }
            return retval
        }
    }
}
