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
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * Common base class for [SharedFormulaRecord], [ArrayRecord] and
 * [TableRecord] which are have similarities.
 * 
 * @author Josh Micich
 */
abstract class SharedValueRecordBase : StandardRecord {
    private val _range: CellRangeAddress8Bit

    protected constructor(range: CellRangeAddress8Bit?) {
        requireNotNull(range) { "range must be supplied." }
        _range = range
    }

    protected constructor() : this(CellRangeAddress8Bit(0, 0, 0, 0))

    /**
     * reads only the range (1 [CellRangeAddress8Bit]) from the stream
     */
    constructor(`in`: LittleEndianInput) {
        _range = CellRangeAddress8Bit(`in`)
    }

    /**
     * @return the range of cells that this record is shared across.  Never `null`.
     */
    fun getRange(): CellRangeAddress8Bit {
        return _range
    }

    fun getFirstRow(): Int {
        return _range.getFirstRow()
    }

    fun getLastRow(): Int {
        return _range.getLastRow()
    }

    fun getFirstColumn(): Int {
        return _range.getFirstColumn().toShort().toInt()
    }

    fun getLastColumn(): Int {
        return _range.getLastColumn().toShort().toInt()
    }

    override fun getDataSize(): Int {
        return CellRangeAddress8Bit.ENCODED_SIZE + getExtraDataSize()
    }

    protected abstract fun getExtraDataSize(): Int

    protected abstract fun serializeExtraData(out: LittleEndianOutput)

    public override fun serialize(out: LittleEndianOutput) {
        _range.serialize(out)
        serializeExtraData(out)
    }

    /**
     * @return `true` if (rowIx, colIx) is within the range ([.getRange])
     * of this shared value object.
     */
    fun isInRange(rowIx: Int, colIx: Int): Boolean {
        val r = _range
        return r.getFirstRow() <= rowIx && r.getLastRow() >= rowIx && r.getFirstColumn() <= colIx && r.getLastColumn() >= colIx
    }

    /**
     * @return `true` if (rowIx, colIx) describes the first cell in this shared value
     * object's range ([.getRange])
     */
    fun isFirstCell(rowIx: Int, colIx: Int): Boolean {
        val r = getRange()
        return r.getFirstRow() == rowIx && r.getFirstColumn() == colIx
    }
}
