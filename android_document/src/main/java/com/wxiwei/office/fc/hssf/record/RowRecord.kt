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

import com.wxiwei.office.fc.util.BitFieldFactory.Companion.getInstance
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Title:        Row Record (0x0208)<P></P>
 * Description:  stores the row information for the sheet. <P></P>
 * REFERENCE:  PG 379 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
</P> */
class RowRecord : StandardRecord {
    private var field_1_row_number: Int
    private var field_2_first_col = 0
    private var field_3_last_col = 0 // plus 1
    private var field_4_height: Short
    private var field_5_optimize: Short // hint field for gui, can/should be set to zero

    // for generated sheets.
    private var field_6_reserved: Short

    /** 16 bit options flags  */
    private var field_7_option_flags: Int
    private var field_8_xf_index: Short // only if isFormatted

    constructor(rowNumber: Int) {
        field_1_row_number = rowNumber
        field_4_height = 0xFF.toShort()
        field_5_optimize = 0.toShort()
        field_6_reserved = 0.toShort()
        field_7_option_flags = OPTION_BITS_ALWAYS_SET // seems necessary for outlining

        field_8_xf_index = 0xf.toShort()
        setEmpty()
    }

    constructor(`in`: RecordInputStream) {
        field_1_row_number = `in`.readUShort()
        field_2_first_col = `in`.readShort().toInt()
        field_3_last_col = `in`.readShort().toInt()
        field_4_height = `in`.readShort()
        field_5_optimize = `in`.readShort()
        field_6_reserved = `in`.readShort()
        field_7_option_flags = `in`.readShort().toInt()
        field_8_xf_index = `in`.readShort()
    }

    /**
     * Updates the firstCol and lastCol fields to the reserved value (-1)
     * to signify that this row is empty
     */
    fun setEmpty() {
        field_2_first_col = 0
        field_3_last_col = 0
    }

    fun isEmpty(): Boolean {
        return (field_2_first_col or field_3_last_col) == 0
    }

    /**
     * set the logical row number for this row (0 based index)
     * @param row - the row number
     */
    fun setRowNumber(row: Int) {
        field_1_row_number = row
    }

    /**
     * set the logical col number for the first cell this row (0 based index)
     * @param col - the col number
     */
    fun setFirstCol(col: Int) {
        field_2_first_col = col
    }

    /**
     * @param col - one past the zero-based index to the last cell in this row
     */
    fun setLastCol(col: Int) {
        field_3_last_col = col
    }

    /**
     * set the height of the row
     * @param height of the row
     */
    fun setHeight(height: Short) {
        field_4_height = height
    }

    /**
     * set whether to optimize or not (set to 0)
     * @param optimize (set to 0)
     */
    fun setOptimize(optimize: Short) {
        field_5_optimize = optimize
    }

    // option bitfields
    /**
     * set the outline level of this row
     * @param ol - the outline level
     */
    fun setOutlineLevel(ol: Short) {
        field_7_option_flags = outlineLevel.setValue(field_7_option_flags, ol.toInt())
    }

    /**
     * set whether or not to collapse this row
     * @param c - collapse or not
     */
    fun setColapsed(c: Boolean) {
        field_7_option_flags = colapsed.setBoolean(field_7_option_flags, c)
    }

    /**
     * set whether or not to display this row with 0 height
     * @param z  height is zero or not.
     */
    fun setZeroHeight(z: Boolean) {
        field_7_option_flags = zeroHeight.setBoolean(field_7_option_flags, z)
    }

    /**
     * set whether the font and row height are not compatible
     * @param  f  true if they aren't compatible (damn not logic)
     */
    fun setBadFontHeight(f: Boolean) {
        field_7_option_flags = badFontHeight.setBoolean(field_7_option_flags, f)
    }

    /**
     * set whether the row has been formatted (even if its got all blank cells)
     * @param f  formatted or not
     */
    fun setFormatted(f: Boolean) {
        field_7_option_flags = formatted.setBoolean(field_7_option_flags, f)
    }

    // end bitfields
    /**
     * if the row is formatted then this is the index to the extended format record
     * @see ExtendedFormatRecord
     * 
     * @param index to the XF record
     */
    fun setXFIndex(index: Short) {
        field_8_xf_index = index
    }

    /**
     * get the logical row number for this row (0 based index)
     * @return row - the row number
     */
    fun getRowNumber(): Int {
        return field_1_row_number
    }

    /**
     * get the logical col number for the first cell this row (0 based index)
     * @return col - the col number
     */
    fun getFirstCol(): Int {
        return field_2_first_col
    }

    /**
     * get the logical col number for the last cell this row (0 based index), plus one
     * @return col - the last col index + 1
     */
    fun getLastCol(): Int {
        return field_3_last_col
    }

    /**
     * get the height of the row
     * @return height of the row
     */
    fun getHeight(): Short {
        return field_4_height
    }

    /**
     * get whether to optimize or not (set to 0)
     * @return optimize (set to 0)
     */
    fun getOptimize(): Short {
        return field_5_optimize
    }

    /**
     * gets the option bitmask.  (use the individual bit setters that refer to this
     * method)
     * @return options - the bitmask
     */
    fun getOptionFlags(): Short {
        return field_7_option_flags.toShort()
    }

    // option bitfields
    /**
     * get the outline level of this row
     * @return ol - the outline level
     * @see .getOptionFlags
     */
    fun getOutlineLevel(): Short {
        return outlineLevel.getValue(field_7_option_flags).toShort()
    }

    /**
     * get whether or not to colapse this row
     * @return c - colapse or not
     * @see .getOptionFlags
     */
    fun getColapsed(): Boolean {
        return (colapsed.isSet(field_7_option_flags))
    }

    /**
     * get whether or not to display this row with 0 height
     * @return - z height is zero or not.
     * @see .getOptionFlags
     */
    fun getZeroHeight(): Boolean {
        return zeroHeight.isSet(field_7_option_flags)
    }

    /**
     * get whether the font and row height are not compatible
     * @return - f -true if they aren't compatible (damn not logic)
     * @see .getOptionFlags
     */
    fun getBadFontHeight(): Boolean {
        return badFontHeight.isSet(field_7_option_flags)
    }

    /**
     * get whether the row has been formatted (even if its got all blank cells)
     * @return formatted or not
     * @see .getOptionFlags
     */
    fun getFormatted(): Boolean {
        return formatted.isSet(field_7_option_flags)
    }

    // end bitfields
    /**
     * if the row is formatted then this is the index to the extended format record
     * @see ExtendedFormatRecord
     * 
     * @return index to the XF record or bogus value (undefined) if isn't formatted
     */
    fun getXFIndex(): Short {
        return field_8_xf_index
    }

    override fun toString(): String {
        val sb = StringBuffer()

        sb.append("[ROW]\n")
        sb.append("    .rownumber      = ").append(Integer.toHexString(getRowNumber()))
            .append("\n")
        sb.append("    .firstcol       = ").append(shortToHex(getFirstCol())).append("\n")
        sb.append("    .lastcol        = ").append(shortToHex(getLastCol())).append("\n")
        sb.append("    .height         = ").append(shortToHex(getHeight().toInt())).append("\n")
        sb.append("    .optimize       = ").append(shortToHex(getOptimize().toInt())).append("\n")
        sb.append("    .reserved       = ").append(shortToHex(field_6_reserved.toInt()))
            .append("\n")
        sb.append("    .optionflags    = ").append(shortToHex(getOptionFlags().toInt()))
            .append("\n")
        sb.append("        .outlinelvl = ").append(Integer.toHexString(getOutlineLevel().toInt()))
            .append("\n")
        sb.append("        .colapsed   = ").append(getColapsed()).append("\n")
        sb.append("        .zeroheight = ").append(getZeroHeight()).append("\n")
        sb.append("        .badfontheig= ").append(getBadFontHeight()).append("\n")
        sb.append("        .formatted  = ").append(getFormatted()).append("\n")
        sb.append("    .xfindex        = ").append(Integer.toHexString(getXFIndex().toInt()))
            .append("\n")
        sb.append("[/ROW]\n")
        return sb.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(getRowNumber())
        out.writeShort(if (getFirstCol() == -1) 0.toShort().toInt() else getFirstCol())
        out.writeShort(if (getLastCol() == -1) 0.toShort().toInt() else getLastCol())
        out.writeShort(getHeight().toInt())
        out.writeShort(getOptimize().toInt())
        out.writeShort(field_6_reserved.toInt())
        out.writeShort(getOptionFlags().toInt())
        out.writeShort(getXFIndex().toInt())
    }

    override fun getDataSize(): Int {
        return ENCODED_SIZE - 4
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = RowRecord(field_1_row_number)
        rec.field_2_first_col = field_2_first_col
        rec.field_3_last_col = field_3_last_col
        rec.field_4_height = field_4_height
        rec.field_5_optimize = field_5_optimize
        rec.field_6_reserved = field_6_reserved
        rec.field_7_option_flags = field_7_option_flags
        rec.field_8_xf_index = field_8_xf_index
        return rec
    }

    companion object {
        const val sid: Short = 0x0208

        const val ENCODED_SIZE: Int = 20

        private const val OPTION_BITS_ALWAYS_SET = 0x0100
        private const val DEFAULT_HEIGHT_BIT = 0x8000

        private val outlineLevel = getInstance(0x07)

        // bit 3 reserved
        private val colapsed = getInstance(0x10)
        private val zeroHeight = getInstance(0x20)
        private val badFontHeight = getInstance(0x40)
        private val formatted = getInstance(0x80)
    }
}
