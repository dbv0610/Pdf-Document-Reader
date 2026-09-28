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

import com.wxiwei.office.fc.util.BitFieldFactory.Companion.getInstance
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Title:        Window Two Record<P>
 * Description:  sheet window settings</P><P>
 * REFERENCE:  PG 422 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
</P> */
class WindowTwoRecord : StandardRecord {
    // 4-7 reserved
    // end bitfields
    private var field_1_options: Short = 0
    private var field_2_top_row: Short = 0
    private var field_3_left_col: Short = 0
    private var field_4_header_color = 0
    private var field_5_page_break_zoom: Short = 0
    private var field_6_normal_zoom: Short = 0
    private var field_7_reserved = 0

    constructor()

    constructor(`in`: RecordInputStream) {
        val size = `in`.remaining()
        field_1_options = `in`.readShort()
        field_2_top_row = `in`.readShort()
        field_3_left_col = `in`.readShort()
        field_4_header_color = `in`.readInt()
        if (size > 10) {
            field_5_page_break_zoom = `in`.readShort()
            field_6_normal_zoom = `in`.readShort()
        }
        if (size > 14) {   // there is a special case of this record that has only 14 bytes...undocumented!
            field_7_reserved = `in`.readInt()
        }
    }

    /**
     * set the options bitmask or just use the bit setters.
     * @param options
     */
    fun setOptions(options: Short) {
        field_1_options = options
    }

    // option bitfields
    /**
     * set whether the window should display formulas
     * @param formulas or not
     */
    fun setDisplayFormulas(formulas: Boolean) {
        field_1_options = displayFormulas.setShortBoolean(field_1_options, formulas)
    }

    /**
     * set whether the window should display gridlines
     * @param gridlines or not
     */
    fun setDisplayGridlines(gridlines: Boolean) {
        field_1_options = displayGridlines.setShortBoolean(field_1_options, gridlines)
    }

    /**
     * set whether the window should display row and column headings
     * @param headings or not
     */
    fun setDisplayRowColHeadings(headings: Boolean) {
        field_1_options = displayRowColHeadings.setShortBoolean(field_1_options, headings)
    }

    /**
     * set whether the window should freeze panes
     * @param freezepanes  freeze panes or not
     */
    fun setFreezePanes(freezepanes: Boolean) {
        field_1_options = freezePanes.setShortBoolean(field_1_options, freezepanes)
    }

    /**
     * set whether the window should display zero values
     * @param zeros or not
     */
    fun setDisplayZeros(zeros: Boolean) {
        field_1_options = displayZeros.setShortBoolean(field_1_options, zeros)
    }

    /**
     * set whether the window should display a default header
     * @param header or not
     */
    fun setDefaultHeader(header: Boolean) {
        field_1_options = defaultHeader.setShortBoolean(field_1_options, header)
    }

    /**
     * is this arabic?
     * @param isarabic  arabic or not
     */
    fun setArabic(isarabic: Boolean) {
        field_1_options = arabic.setShortBoolean(field_1_options, isarabic)
    }

    /**
     * set whether the outline symbols are displaed
     * @param guts  symbols or not
     */
    fun setDisplayGuts(guts: Boolean) {
        field_1_options = displayGuts.setShortBoolean(field_1_options, guts)
    }

    /**
     * freeze unsplit panes or not
     * @param freeze or not
     */
    fun setFreezePanesNoSplit(freeze: Boolean) {
        field_1_options = freezePanesNoSplit.setShortBoolean(field_1_options, freeze)
    }

    /**
     * sheet tab is selected
     * @param sel  selected or not
     */
    fun setSelected(sel: Boolean) {
        field_1_options = selected.setShortBoolean(field_1_options, sel)
    }

    /**
     * is the sheet currently displayed in the window
     * @param p  displayed or not
     */
    fun setActive(p: Boolean) {
        field_1_options = active.setShortBoolean(field_1_options, p)
    }

    /**
     * deprecated May 2008
     */
    @Deprecated("use setActive()")
    fun setPaged(p: Boolean) {
        setActive(p)
    }

    /**
     * was the sheet saved in page break view
     * @param p  pagebreaksaved or not
     */
    fun setSavedInPageBreakPreview(p: Boolean) {
        field_1_options = savedInPageBreakPreview.setShortBoolean(field_1_options, p)
    }

    // end of bitfields.
    /**
     * set the top row visible in the window
     * @param topRow  top row visible
     */
    fun setTopRow(topRow: Short) {
        field_2_top_row = topRow
    }

    /**
     * set the leftmost column displayed in the window
     * @param leftCol  leftmost column
     */
    fun setLeftCol(leftCol: Short) {
        field_3_left_col = leftCol
    }

    /**
     * set the palette index for the header color
     * @param color
     */
    fun setHeaderColor(color: Int) {
        field_4_header_color = color
    }

    /**
     * zoom magification in page break view
     * @param zoom
     */
    fun setPageBreakZoom(zoom: Short) {
        field_5_page_break_zoom = zoom
    }

    /**
     * set the zoom magnification in normal view
     * @param zoom
     */
    fun setNormalZoom(zoom: Short) {
        field_6_normal_zoom = zoom
    }

    /**
     * set the reserved (don't do this) value
     */
    fun setReserved(reserved: Int) {
        field_7_reserved = reserved
    }

    /**
     * get the options bitmask or just use the bit setters.
     * @return options
     */
    fun getOptions(): Short {
        return field_1_options
    }

    // option bitfields
    /**
     * get whether the window should display formulas
     * @return formulas or not
     */
    fun getDisplayFormulas(): Boolean {
        return displayFormulas.isSet(field_1_options.toInt())
    }

    /**
     * get whether the window should display gridlines
     * @return gridlines or not
     */
    fun getDisplayGridlines(): Boolean {
        return displayGridlines.isSet(field_1_options.toInt())
    }

    /**
     * get whether the window should display row and column headings
     * @return headings or not
     */
    fun getDisplayRowColHeadings(): Boolean {
        return displayRowColHeadings.isSet(field_1_options.toInt())
    }

    /**
     * get whether the window should freeze panes
     * @return freeze panes or not
     */
    fun getFreezePanes(): Boolean {
        return freezePanes.isSet(field_1_options.toInt())
    }

    /**
     * get whether the window should display zero values
     * @return zeros or not
     */
    fun getDisplayZeros(): Boolean {
        return displayZeros.isSet(field_1_options.toInt())
    }

    /**
     * get whether the window should display a default header
     * @return header or not
     */
    fun getDefaultHeader(): Boolean {
        return defaultHeader.isSet(field_1_options.toInt())
    }

    /**
     * is this arabic?
     * @return arabic or not
     */
    fun getArabic(): Boolean {
        return arabic.isSet(field_1_options.toInt())
    }

    /**
     * get whether the outline symbols are displaed
     * @return symbols or not
     */
    fun getDisplayGuts(): Boolean {
        return displayGuts.isSet(field_1_options.toInt())
    }

    /**
     * freeze unsplit panes or not
     * @return freeze or not
     */
    fun getFreezePanesNoSplit(): Boolean {
        return freezePanesNoSplit.isSet(field_1_options.toInt())
    }

    /**
     * sheet tab is selected
     * @return selected or not
     */
    fun getSelected(): Boolean {
        return selected.isSet(field_1_options.toInt())
    }

    /**
     * is the sheet currently displayed in the window
     * @return displayed or not
     */
    fun isActive(): Boolean {
        return active.isSet(field_1_options.toInt())
    }

    /**
     * deprecated May 2008
     */
    @Deprecated("use isActive()")
    fun getPaged(): Boolean {
        return isActive()
    }

    /**
     * was the sheet saved in page break view
     * @return pagebreaksaved or not
     */
    fun getSavedInPageBreakPreview(): Boolean {
        return savedInPageBreakPreview.isSet(field_1_options.toInt())
    }

    // end of bitfields.
    /**
     * get the top row visible in the window
     * @return toprow
     */
    fun getTopRow(): Short {
        return field_2_top_row
    }

    /**
     * get the leftmost column displayed in the window
     * @return leftmost
     */
    fun getLeftCol(): Short {
        return field_3_left_col
    }

    /**
     * get the palette index for the header color
     * @return color
     */
    fun getHeaderColor(): Int {
        return field_4_header_color
    }

    /**
     * zoom magification in page break view
     * @return zoom
     */
    fun getPageBreakZoom(): Short {
        return field_5_page_break_zoom
    }

    /**
     * get the zoom magnification in normal view
     * @return zoom
     */
    fun getNormalZoom(): Short {
        return field_6_normal_zoom
    }

    /**
     * get the reserved bits - why would you do this?
     * @return reserved stuff -probably garbage
     */
    fun getReserved(): Int {
        return field_7_reserved
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[WINDOW2]\n")
        buffer.append("    .options        = ")
            .append(Integer.toHexString(getOptions().toInt())).append("\n")
        buffer.append("       .dispformulas= ").append(getDisplayFormulas())
            .append("\n")
        buffer.append("       .dispgridlins= ").append(getDisplayGridlines())
            .append("\n")
        buffer.append("       .disprcheadin= ")
            .append(getDisplayRowColHeadings()).append("\n")
        buffer.append("       .freezepanes = ").append(getFreezePanes())
            .append("\n")
        buffer.append("       .displayzeros= ").append(getDisplayZeros())
            .append("\n")
        buffer.append("       .defaultheadr= ").append(getDefaultHeader())
            .append("\n")
        buffer.append("       .arabic      = ").append(getArabic())
            .append("\n")
        buffer.append("       .displayguts = ").append(getDisplayGuts())
            .append("\n")
        buffer.append("       .frzpnsnosplt= ")
            .append(getFreezePanesNoSplit()).append("\n")
        buffer.append("       .selected    = ").append(getSelected())
            .append("\n")
        buffer.append("       .active       = ").append(isActive())
            .append("\n")
        buffer.append("       .svdinpgbrkpv= ")
            .append(getSavedInPageBreakPreview()).append("\n")
        buffer.append("    .toprow         = ")
            .append(Integer.toHexString(getTopRow().toInt())).append("\n")
        buffer.append("    .leftcol        = ")
            .append(Integer.toHexString(getLeftCol().toInt())).append("\n")
        buffer.append("    .headercolor    = ")
            .append(Integer.toHexString(getHeaderColor())).append("\n")
        buffer.append("    .pagebreakzoom  = ")
            .append(Integer.toHexString(getPageBreakZoom().toInt())).append("\n")
        buffer.append("    .normalzoom     = ")
            .append(Integer.toHexString(getNormalZoom().toInt())).append("\n")
        buffer.append("    .reserved       = ")
            .append(Integer.toHexString(getReserved())).append("\n")
        buffer.append("[/WINDOW2]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(getOptions().toInt())
        out.writeShort(getTopRow().toInt())
        out.writeShort(getLeftCol().toInt())
        out.writeInt(getHeaderColor())
        out.writeShort(getPageBreakZoom().toInt())
        out.writeShort(getNormalZoom().toInt())
        out.writeInt(getReserved())
    }

    override fun getDataSize(): Int {
        return 18
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = WindowTwoRecord()
        rec.field_1_options = field_1_options
        rec.field_2_top_row = field_2_top_row
        rec.field_3_left_col = field_3_left_col
        rec.field_4_header_color = field_4_header_color
        rec.field_5_page_break_zoom = field_5_page_break_zoom
        rec.field_6_normal_zoom = field_6_normal_zoom
        rec.field_7_reserved = field_7_reserved
        return rec
    }

    companion object {
        const val sid: Short = 0x023E

        // bitfields
        private val displayFormulas = getInstance(0x01)
        private val displayGridlines = getInstance(0x02)
        private val displayRowColHeadings = getInstance(0x04)
        private val freezePanes = getInstance(0x08)
        private val displayZeros = getInstance(0x10)

        /**  if false use color in field 4 if true use default foreground for headers  */
        private val defaultHeader = getInstance(0x20)
        private val arabic = getInstance(0x040)
        private val displayGuts = getInstance(0x080)
        private val freezePanesNoSplit = getInstance(0x100)
        private val selected = getInstance(0x200)
        private val active = getInstance(0x400)
        private val savedInPageBreakPreview = getInstance(0x800)
    }
}
