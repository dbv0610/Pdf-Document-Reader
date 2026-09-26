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
 * Title: COLINFO Record (0x007D)
 *
 *
 * Description:  Defines with width and formatting for a range of columns
 *
 *
 * REFERENCE:  PG 293 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)
 *
 *
 * @author Andrew C. Oliver (acoliver at apache dot org)
 */
class ColumnInfoRecord : StandardRecord {
    /**
     * get the first column this record defines formatting info for
     * @return the first column index (0-based)
     */
    /**
     * set the first column this record defines formatting info for
     * @param fc - the first column index (0-based)
     */
    var firstColumn: Int = 0
    /**
     * get the last column this record defines formatting info for
     * @return the last column index (0-based)
     */
    /**
     * set the last column this record defines formatting info for
     * @param lc - the last column index (0-based)
     */
    var lastColumn: Int = 0
    /**
     * @return column width in units of 1/256 of a character width
     */
    /**
     * set the columns' width in 1/256 of a character width
     * @param cw - column width
     */
    var columnWidth: Int = 0
    /**
     * get the columns' default format info
     * @return the extended format index
     * @see ExtendedFormatRecord
     */
    /**
     * set the columns' default format info
     * @param xfi - the extended format index
     * @see ExtendedFormatRecord
     */
    var xFIndex: Int
    private var _options: Int

    // Excel seems write values 2, 10, and 260, even though spec says "must be zero"
    private var field_6_reserved = 0

    /**
     * 得到像素单元的列宽
     * @return Returns the colPixelWidth.
     */
    /**
     * 像素单位列宽
     * @param colPixelWidth The colPixelWidth to set.
     */
    // 转换像素后的列宽,绘制时用的就是这个，默认72像素
    var colPixelWidth: Int = 74

    /**
     * Creates a column info record with default width and format
     */
    constructor() {
        this.columnWidth = 2275
        _options = 2
        this.xFIndex = 0x0f
        field_6_reserved = 2 // seems to be the most common value
    }

    constructor(`in`: RecordInputStream) {
        this.firstColumn = `in`.readUShort()
        this.lastColumn = `in`.readUShort()
        this.columnWidth = `in`.readUShort()
        this.xFIndex = `in`.readUShort()
        _options = `in`.readUShort()
        when (`in`.remaining()) {
            2 -> field_6_reserved = `in`.readUShort()
            1 ->                 // often COLINFO gets encoded 1 byte short
                // shouldn't matter because this field is unused
                field_6_reserved = `in`.readByte().toInt()

            0 ->                 // According to bugzilla 48332,
                // "SoftArtisans OfficeWriter for Excel" totally skips field 6
                // Excel seems to be OK with this, and assumes zero.
                field_6_reserved = 0

            else -> throw RuntimeException("Unusual record size remaining=(" + `in`.remaining() + ")")
        }
    }

    var hidden: Boolean
        /**
         * @return whether the cells are hidden.
         */
        get() = Companion.hidden.isSet(_options)
        /**
         * set whether or not these cells are hidden
         * @param ishidden - whether the cells are hidden.
         */
        set(ishidden) {
            _options = Companion.hidden.setBoolean(_options, ishidden)
        }

    var outlineLevel: Int
        /**
         * @return outline level for the cells
         */
        get() = outlevel.getValue(_options)
        /**
         * set the outline level for the cells
         * @param olevel -outline level for the cells
         */
        set(olevel) {
            _options = outlevel.setValue(_options, olevel)
        }

    var collapsed: Boolean
        /**
         * @return whether the cells are collapsed
         */
        get() = Companion.collapsed.isSet(_options)
        /**
         * set whether the cells are collapsed
         * @param isCollapsed - whether the cells are collapsed
         */
        set(isCollapsed) {
            _options = Companion.collapsed.setBoolean(_options, isCollapsed)
        }

    fun containsColumn(columnIndex: Int): Boolean {
        return this.firstColumn <= columnIndex && columnIndex <= this.lastColumn
    }

    fun isAdjacentBefore(other: ColumnInfoRecord): Boolean {
        return this.lastColumn == other.firstColumn - 1
    }

    /**
     * @return `true` if the format, options and column width match
     */
    fun formatMatches(other: ColumnInfoRecord): Boolean {
        if (this.xFIndex != other.xFIndex) {
            return false
        }
        if (_options != other._options) {
            return false
        }
        if (this.columnWidth != other.columnWidth) {
            return false
        }
        return true
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(this.firstColumn)
        out.writeShort(this.lastColumn)
        out.writeShort(this.columnWidth)
        out.writeShort(this.xFIndex)
        out.writeShort(_options)
        out.writeShort(field_6_reserved)
    }

    override fun getDataSize(): Int {
        return 12
    }

    override fun toString(): String {
        val sb = StringBuilder()

        sb.append("[COLINFO]\n")
        sb.append("  colfirst = ").append(this.firstColumn).append("\n")
        sb.append("  collast  = ").append(this.lastColumn).append("\n")
        sb.append("  colwidth = ").append(this.columnWidth).append("\n")
        sb.append("  xfindex  = ").append(this.xFIndex).append("\n")
        sb.append("  options  = ").append(shortToHex(_options)).append("\n")
        sb.append("    hidden   = ").append(this.hidden).append("\n")
        sb.append("    olevel   = ").append(this.outlineLevel).append("\n")
        sb.append("    collapsed= ").append(this.collapsed).append("\n")
        sb.append("[/COLINFO]\n")
        return sb.toString()
    }

    override fun clone(): Any {
        val rec = ColumnInfoRecord()
        rec.firstColumn = this.firstColumn
        rec.lastColumn = this.lastColumn
        rec.columnWidth = this.columnWidth
        rec.xFIndex = this.xFIndex
        rec._options = _options
        rec.field_6_reserved = field_6_reserved
        return rec
    }

    companion object {
        const val sid: Short = 0x007D

        private val hidden = getInstance(0x01)
        private val outlevel = getInstance(0x0700)
        private val collapsed = getInstance(0x1000)
    }
}
