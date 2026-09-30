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

import com.wxiwei.office.fc.ss.util.AreaReference
import com.wxiwei.office.fc.ss.util.CellReference
import com.wxiwei.office.fc.util.BitFieldFactory.Companion.getInstance
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * Specifies a rectangular area of cells A1:A4 for instance.
 * @author  andy
 * @author Jason Height (jheight at chariot dot net dot au)
 */
abstract class AreaPtgBase : OperandPtg, AreaI {
    /**
     * TODO - (May-2008) fix subclasses of AreaPtg 'AreaN~' which are used in shared formulas.
     * see similar comment in ReferencePtg
     */
    protected fun notImplemented(): RuntimeException {
        return RuntimeException("Coding Error: This method should never be called. This ptg should be converted")
    }

    /** zero based, unsigned 16 bit  */
    private var field_1_first_row = 0

    /** zero based, unsigned 16 bit  */
    private var field_2_last_row = 0

    /** zero based, unsigned 8 bit  */
    private var field_3_first_column = 0

    /** zero based, unsigned 8 bit  */
    private var field_4_last_column = 0

    protected constructor()

    protected constructor(ar: AreaReference) {
        val firstCell = ar.getFirstCell()
        val lastCell = ar.getLastCell()
        setFirstRow(firstCell.getRow())
        setFirstColumn((if (firstCell.getCol().toInt() == -1) 0 else firstCell.getCol()).toInt())
        setLastRow(lastCell.getRow())
        setLastColumn((if (lastCell.getCol().toInt() == -1) 0xFF else lastCell.getCol()).toInt())
        this.isFirstColRelative = !firstCell.isColAbsolute()
        this.isLastColRelative = !lastCell.isColAbsolute()
        this.isFirstRowRelative = !firstCell.isRowAbsolute()
        this.isLastRowRelative = !lastCell.isRowAbsolute()
    }

    protected constructor(
        firstRow: Int,
        lastRow: Int,
        firstColumn: Int,
        lastColumn: Int,
        firstRowRelative: Boolean,
        lastRowRelative: Boolean,
        firstColRelative: Boolean,
        lastColRelative: Boolean
    ) {
        if (lastRow > firstRow) {
            setFirstRow(firstRow)
            setLastRow(lastRow)
            this.isFirstRowRelative = firstRowRelative
            this.isLastRowRelative = lastRowRelative
        } else {
            setFirstRow(lastRow)
            setLastRow(firstRow)
            this.isFirstRowRelative = lastRowRelative
            this.isLastRowRelative = firstRowRelative
        }

        if (lastColumn > firstColumn) {
            setFirstColumn(firstColumn)
            setLastColumn(lastColumn)
            this.isFirstColRelative = firstColRelative
            this.isLastColRelative = lastColRelative
        } else {
            setFirstColumn(lastColumn)
            setLastColumn(firstColumn)
            this.isFirstColRelative = lastColRelative
            this.isLastColRelative = firstColRelative
        }
    }

    protected fun readCoordinates(`in`: LittleEndianInput) {
        field_1_first_row = `in`.readUShort()
        field_2_last_row = `in`.readUShort()
        field_3_first_column = `in`.readUShort()
        field_4_last_column = `in`.readUShort()
    }

    protected fun writeCoordinates(out: LittleEndianOutput) {
        out.writeShort(field_1_first_row)
        out.writeShort(field_2_last_row)
        out.writeShort(field_3_first_column)
        out.writeShort(field_4_last_column)
    }

    /**
     * @return the first row in the area
     */
    override val firstRow: Int get() {
        return field_1_first_row
    }

    /**
     * sets the first row
     * @param rowIx number (0-based)
     */
    fun setFirstRow(rowIx: Int) {
        field_1_first_row = rowIx
    }

    /**
     * @return last row in the range (x2 in x1,y1-x2,y2)
     */
    override val lastRow: Int get() {
        return field_2_last_row
    }

    /**
     * @param rowIx last row number in the area
     */
    fun setLastRow(rowIx: Int) {
        field_2_last_row = rowIx
    }

    /**
     * @return the first column number in the area.
     */
    override val firstColumn: Int get() {
        return columnMask.getValue(field_3_first_column)
    }

    val firstColumnRaw: Short
        /**
         * @return the first column number + the options bit settings unstripped
         */
        get() = field_3_first_column.toShort() // TODO

    var isFirstRowRelative: Boolean
        /**
         * @return whether or not the first row is a relative reference or not.
         */
        get() = rowRelative.isSet(field_3_first_column)
        /**
         * sets the first row to relative or not
         * @param rel is relative or not.
         */
        set(rel) {
            field_3_first_column =
                rowRelative.setBoolean(field_3_first_column, rel)
        }

    var isFirstColRelative: Boolean
        /**
         * @return isrelative first column to relative or not
         */
        get() = colRelative.isSet(field_3_first_column)
        /**
         * set whether the first column is relative
         */
        set(rel) {
            field_3_first_column =
                colRelative.setBoolean(field_3_first_column, rel)
        }

    /**
     * set the first column in the area
     */
    fun setFirstColumn(colIx: Int) {
        field_3_first_column = columnMask.setValue(field_3_first_column, colIx)
    }

    /**
     * set the first column irrespective of the bitmasks
     */
    fun setFirstColumnRaw(column: Int) {
        field_3_first_column = column
    }

    /**
     * @return lastcolumn in the area
     */
    override val lastColumn: Int get() {
        return columnMask.getValue(field_4_last_column)
    }

    var lastColumnRaw: Short
        /**
         * @return last column and bitmask (the raw field)
         */
        get() = field_4_last_column.toShort()
        /**
         * set the last column irrespective of the bitmasks
         */
        set(column) {
            field_4_last_column = column.toInt()
        }

    var isLastRowRelative: Boolean
        /**
         * @return last row relative or not
         */
        get() = rowRelative.isSet(field_4_last_column)
        /**
         * set whether the last row is relative or not
         * @param rel `true` if the last row relative, else
         * `false`
         */
        set(rel) {
            field_4_last_column =
                rowRelative.setBoolean(field_4_last_column, rel)
        }

    var isLastColRelative: Boolean
        /**
         * @return lastcol relative or not
         */
        get() = colRelative.isSet(field_4_last_column)
        /**
         * set whether the last column should be relative or not
         */
        set(rel) {
            field_4_last_column =
                colRelative.setBoolean(field_4_last_column, rel)
        }

    /**
     * set the last column in the area
     */
    fun setLastColumn(colIx: Int) {
        field_4_last_column = columnMask.setValue(field_4_last_column, colIx)
    }

    protected fun formatReferenceAsString(): String? {
        val topLeft = CellReference(
            firstRow,
            firstColumn,
            !this.isFirstRowRelative,
            !this.isFirstColRelative
        )
        val botRight = CellReference(
            lastRow,
            lastColumn,
            !this.isLastRowRelative,
            !this.isLastColRelative
        )

        if (AreaReference.isWholeColumnReference(topLeft, botRight)) {
            return (AreaReference(topLeft, botRight)).formatAsString()
        }
        return topLeft.formatAsString() + ":" + botRight.formatAsString()
    }

    override fun toFormulaString(): String? {
        return formatReferenceAsString()
    }

    override val defaultOperandClass: Byte get() {
        return Ptg.Companion.CLASS_REF
    }

    companion object {
        private val rowRelative = getInstance(0x8000)
        private val colRelative = getInstance(0x4000)
        private val columnMask = getInstance(0x3FFF)
    }
}
