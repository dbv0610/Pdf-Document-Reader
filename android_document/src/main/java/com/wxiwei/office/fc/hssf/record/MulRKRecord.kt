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

import com.wxiwei.office.fc.hssf.util.RKUtil
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * MULRK (0x00BD) 
 *
 *
 * 
 * Used to store multiple RK numbers on a row.  1 MulRk = Multiple Cell values.
 * HSSF just converts this into multiple NUMBER records.  READ-ONLY SUPPORT!<P>
 * REFERENCE:  PG 330 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
</P> */
class MulRKRecord(`in`: RecordInputStream) : StandardRecord() {
    private val field_1_row: Int
    private val field_2_first_col: Short
    private val field_3_rks: Array<RkRec?>
    private val field_4_last_col: Short

    fun getRow(): Int {
        return field_1_row
    }

    /**
     * starting column (first cell this holds in the row)
     * @return first column number
     */
    fun getFirstColumn(): Short {
        return field_2_first_col
    }

    /**
     * ending column (last cell this holds in the row)
     * @return first column number
     */
    fun getLastColumn(): Short {
        return field_4_last_col
    }

    /**
     * get the number of columns this contains (last-first +1)
     * @return number of columns (last - first +1)
     */
    fun getNumColumns(): Int {
        return field_4_last_col - field_2_first_col + 1
    }

    /**
     * returns the xf index for column (coffset = column - field_2_first_col)
     * @return the XF index for the column
     */
    fun getXFAt(coffset: Int): Short {
        return field_3_rks[coffset]!!.xf
    }

    /**
     * returns the rk number for column (coffset = column - field_2_first_col)
     * @return the value (decoded into a double)
     */
    fun getRKNumberAt(coffset: Int): Double {
        return RKUtil.decodeNumber(field_3_rks[coffset]!!.rk)
    }

    /**
     * @param in the RecordInputstream to read the record from
     */
    init {
        field_1_row = `in`.readUShort()
        field_2_first_col = `in`.readShort()
        field_3_rks = RkRec.parseRKs(`in`)
        field_4_last_col = `in`.readShort()
    }


    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[MULRK]\n")
        buffer.append("	.row	 = ").append(shortToHex(getRow())).append("\n")
        buffer.append("	.firstcol= ").append(shortToHex(getFirstColumn().toInt())).append("\n")
        buffer.append("	.lastcol = ").append(shortToHex(getLastColumn().toInt())).append("\n")

        for (k in 0..<getNumColumns()) {
            buffer.append("	xf[").append(k).append("] = ").append(shortToHex(getXFAt(k).toInt()))
                .append("\n")
            buffer.append("	rk[").append(k).append("] = ").append(getRKNumberAt(k)).append("\n")
        }
        buffer.append("[/MULRK]\n")
        return buffer.toString()
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    public override fun serialize(out: LittleEndianOutput) {
        throw RecordFormatException("Sorry, you can't serialize MulRK in this release")
    }

    override fun getDataSize(): Int {
        throw RecordFormatException("Sorry, you can't serialize MulRK in this release")
    }

    private class RkRec(`in`: RecordInputStream) {
        val xf: Short
        val rk: Int

        init {
            xf = `in`.readShort()
            rk = `in`.readInt()
        }

        companion object {
            const val ENCODED_SIZE: Int = 6
            fun parseRKs(`in`: RecordInputStream): Array<RkRec?> {
                val nItems = (`in`.remaining() - 2) / ENCODED_SIZE
                val retval: Array<RkRec?> = arrayOfNulls<RkRec>(nItems)
                for (i in 0..<nItems) {
                    retval[i] = RkRec(`in`)
                }
                return retval
            }
        }
    }

    companion object {
        const val sid: Short = 0x00BD
    }
}
