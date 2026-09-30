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

import com.wxiwei.office.fc.ss.util.CellReference
import com.wxiwei.office.fc.util.BitFieldFactory.Companion.getInstance
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * ReferencePtgBase - handles references (such as A1, A2, IA4)
 * 
 * @author Andrew C. Oliver (acoliver@apache.org)
 * @author Jason Height (jheight at chariot dot net dot au)
 */
abstract class RefPtgBase : OperandPtg {
    /**
     * @return the row number as an int
     */
    /** The row index - zero based unsigned 16 bit value  */
    var row: Int = 0

    /**
     * Field 2 - lower 8 bits is the zero based unsigned byte column index - bit
     * 16 - isRowRelative - bit 15 - isColumnRelative
     */
    private var field_2_col = 0

    protected constructor()

    protected constructor(c: CellReference) {
        this.row = c.getRow()
        this.column = c.getCol().toInt()
        this.isColRelative = !c.isColAbsolute()
        this.isRowRelative = !c.isRowAbsolute()
    }

    protected fun readCoordinates(`in`: LittleEndianInput) {
        this.row = `in`.readUShort()
        field_2_col = `in`.readUShort()
    }

    protected fun writeCoordinates(out: LittleEndianOutput) {
        out.writeShort(this.row)
        out.writeShort(field_2_col)
    }

    var isRowRelative: Boolean
        get() = rowRelative.isSet(field_2_col)
        set(rel) {
            field_2_col = rowRelative.setBoolean(field_2_col, rel)
        }

    var isColRelative: Boolean
        get() = colRelative.isSet(field_2_col)
        set(rel) {
            field_2_col = colRelative.setBoolean(field_2_col, rel)
        }

    var column: Int
        get() = Companion.column.getValue(field_2_col)
        set(col) {
            field_2_col = Companion.column.setValue(field_2_col, col)
        }

    protected fun formatReferenceAsString(): String? {
        // Only make cell references as needed. Memory is an issue
        val cr = CellReference(
            this.row,
            this.column, !this.isRowRelative, !this.isColRelative
        )
        return cr.formatAsString()
    }

    override val defaultOperandClass: Byte get() {
        return Ptg.Companion.CLASS_REF
    }

    companion object {
        private val rowRelative = getInstance(0x8000)
        private val colRelative = getInstance(0x4000)

        /**
         * YK: subclasses of RefPtgBase are used by the FormulaParser and FormulaEvaluator accross HSSF and XSSF.
         * The bit mask should accomodate the maximum number of avaiable columns, i.e. 0x3FFF.
         * 
         * @see com.wxiwei.office.fc.ss.SpreadsheetVersion
         */
        private val column = getInstance(0x3FFF)
    }
}
