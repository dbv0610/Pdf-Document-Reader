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
 * Title: Recalc Id Record (0x01C1)
 *
 *
 * Description:  This record contains an ID that marks when a worksheet was last
 * recalculated. It's an optimization Excel uses to determine if it
 * needs to  recalculate the spreadsheet when it's opened. So far, only
 * the two engine ids `0x80 0x38 0x01 0x00`
 * and `0x60 0x69 0x01 0x00` have been seen.
 * A value of `0x00` will cause Excel to recalculate
 * all formulas on the next load.
 *
 *
 * REFERENCE:  http://chicago.sourceforge.net/devel/docs/excel/biff8.html
 *
 *
 * @author Luc Girardin (luc dot girardin at macrofocus dot com)
 */
class RecalcIdRecord : StandardRecord {
    private val _reserved0: Int

    /**
     * An unsigned integer that specifies the recalculation engine identifier
     * of the recalculation engine that performed the last recalculation.
     * If the value is less than the recalculation engine identifier associated with the application,
     * the application will recalculate the results of all formulas on
     * this workbook immediately after loading the file
     */
    private var _engineId: Int

    constructor() {
        _reserved0 = 0
        _engineId = 0
    }

    constructor(`in`: RecordInputStream) {
        `in`.readUShort() // field 'rt' should have value 0x01C1, but Excel doesn't care during reading
        _reserved0 = `in`.readUShort()
        _engineId = `in`.readInt()
    }

    fun isNeeded(): Boolean {
        return true
    }

    fun setEngineId(`val`: Int) {
        _engineId = `val`
    }

    fun getEngineId(): Int {
        return _engineId
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[RECALCID]\n")
        buffer.append("    .reserved = ").append(shortToHex(_reserved0)).append("\n")
        buffer.append("    .engineId = ").append(intToHex(_engineId)).append("\n")
        buffer.append("[/RECALCID]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(Companion.sid.toInt()) // always write 'rt' field as 0x01C1
        out.writeShort(_reserved0)
        out.writeInt(_engineId)
    }

    override fun getDataSize(): Int {
        return 8
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    companion object {
        const val sid: Short = 0x01C1
    }
}
