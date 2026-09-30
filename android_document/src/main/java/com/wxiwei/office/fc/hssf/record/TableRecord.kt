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

import com.wxiwei.office.fc.hssf.util.CellRangeAddress8Bit
import com.wxiwei.office.fc.hssf.util.CellReference
import com.wxiwei.office.fc.util.BitFieldFactory.Companion.getInstance
import com.wxiwei.office.fc.util.HexDump.byteToHex
import com.wxiwei.office.fc.util.HexDump.intToHex
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * DATATABLE (0x0236)
 *
 *
 * 
 * TableRecord - The record specifies a data table.
 * This record is preceded by a single Formula record that
 * defines the first cell in the data table, which should
 * only contain a single Ptg, [TblPtg].
 * 
 * See p536 of the June 08 binary docs
 */
class TableRecord : SharedValueRecordBase {
    private var field_5_flags = 0
    private val field_6_res: Int
    private var field_7_rowInputRow = 0
    private var field_8_colInputRow = 0
    private var field_9_rowInputCol = 0
    private var field_10_colInputCol = 0

    constructor(`in`: RecordInputStream) : super(`in`) {
        field_5_flags = `in`.readByte().toInt()
        field_6_res = `in`.readByte().toInt()
        field_7_rowInputRow = `in`.readShort().toInt()
        field_8_colInputRow = `in`.readShort().toInt()
        field_9_rowInputCol = `in`.readShort().toInt()
        field_10_colInputCol = `in`.readShort().toInt()
    }

    constructor(range: CellRangeAddress8Bit?) : super(range) {
        field_6_res = 0
    }

    fun getFlags(): Int {
        return field_5_flags
    }

    fun setFlags(flags: Int) {
        field_5_flags = flags
    }

    fun getRowInputRow(): Int {
        return field_7_rowInputRow
    }

    fun setRowInputRow(rowInputRow: Int) {
        field_7_rowInputRow = rowInputRow
    }

    fun getColInputRow(): Int {
        return field_8_colInputRow
    }

    fun setColInputRow(colInputRow: Int) {
        field_8_colInputRow = colInputRow
    }

    fun getRowInputCol(): Int {
        return field_9_rowInputCol
    }

    fun setRowInputCol(rowInputCol: Int) {
        field_9_rowInputCol = rowInputCol
    }

    fun getColInputCol(): Int {
        return field_10_colInputCol
    }

    fun setColInputCol(colInputCol: Int) {
        field_10_colInputCol = colInputCol
    }


    fun isAlwaysCalc(): Boolean {
        return alwaysCalc.isSet(field_5_flags)
    }

    fun setAlwaysCalc(flag: Boolean) {
        field_5_flags = alwaysCalc.setBoolean(field_5_flags, flag)
    }

    fun isRowOrColInpCell(): Boolean {
        return rowOrColInpCell.isSet(field_5_flags)
    }

    fun setRowOrColInpCell(flag: Boolean) {
        field_5_flags = rowOrColInpCell.setBoolean(field_5_flags, flag)
    }

    fun isOneNotTwoVar(): Boolean {
        return oneOrTwoVar.isSet(field_5_flags)
    }

    fun setOneNotTwoVar(flag: Boolean) {
        field_5_flags = oneOrTwoVar.setBoolean(field_5_flags, flag)
    }

    fun isColDeleted(): Boolean {
        return colDeleted.isSet(field_5_flags)
    }

    fun setColDeleted(flag: Boolean) {
        field_5_flags = colDeleted.setBoolean(field_5_flags, flag)
    }

    fun isRowDeleted(): Boolean {
        return rowDeleted.isSet(field_5_flags)
    }

    fun setRowDeleted(flag: Boolean) {
        field_5_flags = rowDeleted.setBoolean(field_5_flags, flag)
    }


    override fun getSid(): Short {
        return Companion.sid
    }

    override fun getExtraDataSize(): Int {
        return (2 // 2 byte fields
                + 8) // 4 short fields
    }

    override fun serializeExtraData(out: LittleEndianOutput) {
        out.writeByte(field_5_flags)
        out.writeByte(field_6_res)
        out.writeShort(field_7_rowInputRow)
        out.writeShort(field_8_colInputRow)
        out.writeShort(field_9_rowInputCol)
        out.writeShort(field_10_colInputCol)
    }

    override fun toString(): String {
        val buffer = StringBuffer()
        buffer.append("[TABLE]\n")
        buffer.append("    .range    = ").append(getRange().toString()).append("\n")
        buffer.append("    .flags    = ").append(byteToHex(field_5_flags)).append("\n")
        buffer.append("    .alwaysClc= ").append(isAlwaysCalc()).append("\n")
        buffer.append("    .reserved = ").append(intToHex(field_6_res)).append("\n")
        val crRowInput: CellReference = cr(field_7_rowInputRow, field_8_colInputRow)
        val crColInput: CellReference = cr(field_9_rowInputCol, field_10_colInputCol)
        buffer.append("    .rowInput = ").append(crRowInput.formatAsString()).append("\n")
        buffer.append("    .colInput = ").append(crColInput.formatAsString()).append("\n")
        buffer.append("[/TABLE]\n")
        return buffer.toString()
    }

    companion object {
        const val sid: Short = 0x0236

        private val alwaysCalc = getInstance(0x0001)
        private val calcOnOpen = getInstance(0x0002)
        private val rowOrColInpCell = getInstance(0x0004)
        private val oneOrTwoVar = getInstance(0x0008)
        private val rowDeleted = getInstance(0x0010)
        private val colDeleted = getInstance(0x0020)

        private fun cr(rowIx: Int, colIxAndFlags: Int): CellReference {
            val colIx = colIxAndFlags and 0x00FF
            val isRowAbs = (colIxAndFlags and 0x8000) == 0
            val isColAbs = (colIxAndFlags and 0x4000) == 0
            return CellReference(rowIx, colIx, isRowAbs, isColAbs)
        }
    }
}
