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

import com.wxiwei.office.fc.hssf.formula.Formula
import com.wxiwei.office.fc.hssf.formula.Formula.Companion.read
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.util.CellRangeAddress8Bit
import com.wxiwei.office.fc.util.HexDump.intToHex
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * ARRAY (0x0221)
 *
 *
 * 
 * Treated in a similar way to SharedFormulaRecord
 * 
 * @author Josh Micich
 */
class ArrayRecord : SharedValueRecordBase {
    private val _options: Int
    private val _field3notUsed: Int
    private val _formula: Formula

    constructor(`in`: RecordInputStream) : super(`in`) {
        _options = `in`.readUShort()
        _field3notUsed = `in`.readInt()
        val formulaTokenLen = `in`.readUShort()
        val totalFormulaLen = `in`.available()
        _formula = read(formulaTokenLen, `in`, totalFormulaLen)
    }

    constructor(formula: Formula, range: CellRangeAddress8Bit?) : super(range) {
        _options = 0 //YK: Excel 2007 leaves this field unset
        _field3notUsed = 0
        _formula = formula
    }

    val isAlwaysRecalculate: Boolean
        get() = (_options and OPT_ALWAYS_RECALCULATE) != 0
    val isCalculateOnOpen: Boolean
        get() = (_options and OPT_CALCULATE_ON_OPEN) != 0

    val formulaTokens: Array<Ptg?>
        get() = _formula.tokens

    override fun getExtraDataSize(): Int {
        return 2 + 4 + _formula.encodedSize
    }

    override fun serializeExtraData(out: LittleEndianOutput) {
        out.writeShort(_options)
        out.writeInt(_field3notUsed)
        _formula.serialize(out)
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun toString(): String {
        val sb = StringBuffer()
        sb.append(javaClass.getName()).append(" [ARRAY]\n")
        sb.append(" range=").append(getRange().toString()).append("\n")
        sb.append(" options=").append(shortToHex(_options)).append("\n")
        sb.append(" notUsed=").append(intToHex(_field3notUsed)).append("\n")
        sb.append(" formula:").append("\n")
        val ptgs: Array<Ptg> = _formula.tokens.requireNoNulls()
        for (i in ptgs.indices) {
            val ptg = ptgs[i]
            sb.append(ptg.toString()).append(ptg.rVAType).append("\n")
        }
        sb.append("]")
        return sb.toString()
    }

    companion object {
        const val sid: Short = 0x0221
        private const val OPT_ALWAYS_RECALCULATE = 0x0001
        private const val OPT_CALCULATE_ON_OPEN = 0x0002
    }
}
