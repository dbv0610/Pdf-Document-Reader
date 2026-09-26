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
import com.wxiwei.office.fc.hssf.formula.SharedFormula
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.util.CellRangeAddress8Bit
import com.wxiwei.office.fc.ss.SpreadsheetVersion
import com.wxiwei.office.fc.util.HexDump.intToHex
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Title:        SHAREDFMLA (0x04BC) SharedFormulaRecord
 * Description:  Primarily used as an excel optimization so that multiple similar formulas
 * are not written out too many times.  We should recognize this record and
 * serialize as is since this is used when reading templates.
 * 
 * 
 * Note: the documentation says that the SID is BC where biffviewer reports 4BC.  The hex dump shows
 * that the two byte sid representation to be 'BC 04' that is consistent with the other high byte
 * record types.
 * @author Danny Mui at apache dot org
 */
class SharedFormulaRecord : SharedValueRecordBase {
    private var field_5_reserved = 0
    private var field_7_parsed_expr: Formula

    // for testing only
    constructor() : this(CellRangeAddress8Bit(0, 0, 0, 0))
    private constructor(range: CellRangeAddress8Bit?) : super(range) {
        field_7_parsed_expr = Formula.create(Ptg.EMPTY_PTG_ARRAY)!!
    }

    /**
     * @param in the RecordInputstream to read the record from
     */
    constructor(`in`: RecordInputStream) : super(`in`) {
        field_5_reserved = `in`.readShort().toInt()
        val field_6_expression_len = `in`.readShort().toInt()
        val nAvailableBytes = `in`.available()
        field_7_parsed_expr = read(field_6_expression_len, `in`, nAvailableBytes)
    }

    override fun serializeExtraData(out: LittleEndianOutput) {
        out.writeShort(field_5_reserved)
        field_7_parsed_expr.serialize(out)
    }

    override fun getExtraDataSize(): Int {
        return 2 + field_7_parsed_expr.encodedSize
    }

    /**
     * print a sort of string representation ([SHARED FORMULA RECORD] id = x [/SHARED FORMULA RECORD])
     */
    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[SHARED FORMULA (").append(intToHex(Companion.sid.toInt())).append("]\n")
        buffer.append("    .range      = ").append(getRange().toString()).append("\n")
        buffer.append("    .reserved    = ").append(shortToHex(field_5_reserved)).append("\n")

        val ptgs = field_7_parsed_expr.tokens
        for (k in ptgs.indices) {
            buffer.append("Formula[").append(k).append("]")
            val ptg = ptgs[k]
            if (ptg != null) {
                buffer.append(ptg.toString()).append(ptg.rVAType).append("\n")
            }
        }

        buffer.append("[/SHARED FORMULA]\n")
        return buffer.toString()
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    /**
     * @return the equivalent [Ptg] array that the formula would have, were it not shared.
     */
    fun getFormulaTokens(formula: FormulaRecord): Array<Ptg?> {
        val formulaRow = formula.row
        val formulaColumn = formula.column.toInt()
        //Sanity checks
        if (!isInRange(formulaRow, formulaColumn)) {
            throw RuntimeException("Shared Formula Conversion: Coding Error")
        }

        val sf = SharedFormula(SpreadsheetVersion.EXCEL97)
        return sf.convertSharedFormulas(field_7_parsed_expr.tokens, formulaRow, formulaColumn)
    }

    override fun clone(): Any {
        val result = SharedFormulaRecord(getRange())
        result.field_5_reserved = field_5_reserved
        result.field_7_parsed_expr = field_7_parsed_expr.copy()
        return result
    }

    fun isFormulaSame(other: SharedFormulaRecord): Boolean {
        return field_7_parsed_expr.isSame(other.field_7_parsed_expr)
    }

    companion object {
        const val sid: Short = 0x04BC
    }
}
