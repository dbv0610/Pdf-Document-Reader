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
 * Title:        Window1 Record<P>
 * Description:  Stores the attributes of the workbook window.  This is basically
 * so the gui knows how big to make the window holding the spreadsheet
 * document.</P><P>
 * REFERENCE:  PG 421 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
</P> */
class WindowOneRecord : StandardRecord {
    // our variable names stolen from old TV sets.
    private var field_1_h_hold: Short = 0 // horizontal position
    private var field_2_v_hold: Short = 0 // vertical position
    private var field_3_width: Short = 0
    private var field_4_height: Short = 0
    private var field_5_options: Short = 0

    // all the rest are "reserved"
    private var field_6_active_sheet = 0
    private var field_7_first_visible_tab = 0
    private var field_8_num_selected_tabs: Short = 0
    private var field_9_tab_width_ratio: Short = 0

    constructor()

    constructor(`in`: RecordInputStream) {
        field_1_h_hold = `in`.readShort()
        field_2_v_hold = `in`.readShort()
        field_3_width = `in`.readShort()
        field_4_height = `in`.readShort()
        field_5_options = `in`.readShort()
        field_6_active_sheet = `in`.readShort().toInt()
        field_7_first_visible_tab = `in`.readShort().toInt()
        field_8_num_selected_tabs = `in`.readShort()
        field_9_tab_width_ratio = `in`.readShort()
    }

    /**
     * set the horizontal position of the window (in 1/20ths of a point)
     * @param h - horizontal location
     */
    fun setHorizontalHold(h: Short) {
        field_1_h_hold = h
    }

    /**
     * set the vertical position of the window (in 1/20ths of a point)
     * @param v - vertical location
     */
    fun setVerticalHold(v: Short) {
        field_2_v_hold = v
    }

    /**
     * set the width of the window
     * @param w  width
     */
    fun setWidth(w: Short) {
        field_3_width = w
    }

    /**
     * set teh height of the window
     * @param h  height
     */
    fun setHeight(h: Short) {
        field_4_height = h
    }

    /**
     * set the options bitmask (see bit setters)
     * 
     * @param o - the bitmask
     */
    fun setOptions(o: Short) {
        field_5_options = o
    }

    // bitfields for options
    /**
     * set whether the window is hidden or not
     * @param ishidden or not
     */
    fun setHidden(ishidden: Boolean) {
        field_5_options = hidden.setShortBoolean(field_5_options, ishidden)
    }

    /**
     * set whether the window has been iconized or not
     * @param isiconic  iconize  or not
     */
    fun setIconic(isiconic: Boolean) {
        field_5_options = iconic.setShortBoolean(field_5_options, isiconic)
    }

    /**
     * set whether to display the horizontal scrollbar or not
     * @param scroll display or not
     */
    fun setDisplayHorizonalScrollbar(scroll: Boolean) {
        field_5_options = hscroll.setShortBoolean(field_5_options, scroll)
    }

    /**
     * set whether to display the vertical scrollbar or not
     * @param scroll  display or not
     */
    fun setDisplayVerticalScrollbar(scroll: Boolean) {
        field_5_options = vscroll.setShortBoolean(field_5_options, scroll)
    }

    /**
     * set whether to display the tabs or not
     * @param disptabs  display or not
     */
    fun setDisplayTabs(disptabs: Boolean) {
        field_5_options = tabs.setShortBoolean(field_5_options, disptabs)
    }

    // end bitfields
    fun setActiveSheetIndex(index: Int) {
        field_6_active_sheet = index
    }

    /**
     * deprecated May 2008
     */
    @Deprecated("- Misleading name - use setActiveSheetIndex() ")
    fun setSelectedTab(s: Short) {
        setActiveSheetIndex(s.toInt())
    }

    /**
     * Sets the first visible sheet in the worksheet tab-bar.  This method does **not**
     * hide, select or focus sheets.  It just sets the scroll position in the tab-bar.
     * @param t the sheet index of the tab that will become the first in the tab-bar
     */
    fun setFirstVisibleTab(t: Int) {
        field_7_first_visible_tab = t
    }

    /**
     * deprecated May 2008
     */
    @Deprecated("- Misleading name - use setFirstVisibleTab() ")
    fun setDisplayedTab(t: Short) {
        setFirstVisibleTab(t.toInt())
    }

    /**
     * set the number of selected tabs
     * @param n  number of tabs
     */
    fun setNumSelectedTabs(n: Short) {
        field_8_num_selected_tabs = n
    }

    /**
     * ratio of the width of the tabs to the horizontal scrollbar
     * @param r  ratio
     */
    fun setTabWidthRatio(r: Short) {
        field_9_tab_width_ratio = r
    }

    /**
     * get the horizontal position of the window (in 1/20ths of a point)
     * @return h - horizontal location
     */
    fun getHorizontalHold(): Short {
        return field_1_h_hold
    }

    /**
     * get the vertical position of the window (in 1/20ths of a point)
     * @return v - vertical location
     */
    fun getVerticalHold(): Short {
        return field_2_v_hold
    }

    /**
     * get the width of the window
     * @return width
     */
    fun getWidth(): Short {
        return field_3_width
    }

    /**
     * get the height of the window
     * @return height
     */
    fun getHeight(): Short {
        return field_4_height
    }

    /**
     * get the options bitmask (see bit setters)
     * 
     * @return o - the bitmask
     */
    fun getOptions(): Short {
        return field_5_options
    }

    // bitfields for options
    /**
     * get whether the window is hidden or not
     * @return ishidden or not
     */
    fun getHidden(): Boolean {
        return hidden.isSet(field_5_options.toInt())
    }

    /**
     * get whether the window has been iconized or not
     * @return iconize  or not
     */
    fun getIconic(): Boolean {
        return iconic.isSet(field_5_options.toInt())
    }

    /**
     * get whether to display the horizontal scrollbar or not
     * @return display or not
     */
    fun getDisplayHorizontalScrollbar(): Boolean {
        return hscroll.isSet(field_5_options.toInt())
    }

    /**
     * get whether to display the vertical scrollbar or not
     * @return display or not
     */
    fun getDisplayVerticalScrollbar(): Boolean {
        return vscroll.isSet(field_5_options.toInt())
    }

    /**
     * get whether to display the tabs or not
     * @return display or not
     */
    fun getDisplayTabs(): Boolean {
        return tabs.isSet(field_5_options.toInt())
    }


    // end options bitfields
    /**
     * @return the index of the currently displayed sheet
     */
    fun getActiveSheetIndex(): Int {
        return field_6_active_sheet
    }

    /**
     * deprecated May 2008
     */
    @Deprecated("- Misleading name - use getActiveSheetIndex() ")
    fun getSelectedTab(): Short {
        return getActiveSheetIndex().toShort()
    }

    /**
     * @return the first visible sheet in the worksheet tab-bar.
     * I.E. the scroll position of the tab-bar.
     */
    fun getFirstVisibleTab(): Int {
        return field_7_first_visible_tab
    }

    /**
     * deprecated May 2008
     */
    @Deprecated("- Misleading name - use getFirstVisibleTab() ")
    fun getDisplayedTab(): Short {
        return getFirstVisibleTab().toShort()
    }

    /**
     * get the number of selected tabs
     * @return number of tabs
     */
    fun getNumSelectedTabs(): Short {
        return field_8_num_selected_tabs
    }

    /**
     * ratio of the width of the tabs to the horizontal scrollbar
     * @return ratio
     */
    fun getTabWidthRatio(): Short {
        return field_9_tab_width_ratio
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[WINDOW1]\n")
        buffer.append("    .h_hold          = ")
            .append(Integer.toHexString(getHorizontalHold().toInt())).append("\n")
        buffer.append("    .v_hold          = ")
            .append(Integer.toHexString(getVerticalHold().toInt())).append("\n")
        buffer.append("    .width           = ")
            .append(Integer.toHexString(getWidth().toInt())).append("\n")
        buffer.append("    .height          = ")
            .append(Integer.toHexString(getHeight().toInt())).append("\n")
        buffer.append("    .options         = ")
            .append(Integer.toHexString(getOptions().toInt())).append("\n")
        buffer.append("        .hidden      = ").append(getHidden())
            .append("\n")
        buffer.append("        .iconic      = ").append(getIconic())
            .append("\n")
        buffer.append("        .hscroll     = ")
            .append(getDisplayHorizontalScrollbar()).append("\n")
        buffer.append("        .vscroll     = ")
            .append(getDisplayVerticalScrollbar()).append("\n")
        buffer.append("        .tabs        = ").append(getDisplayTabs())
            .append("\n")
        buffer.append("    .activeSheet     = ")
            .append(Integer.toHexString(getActiveSheetIndex())).append("\n")
        buffer.append("    .firstVisibleTab    = ")
            .append(Integer.toHexString(getFirstVisibleTab())).append("\n")
        buffer.append("    .numselectedtabs = ")
            .append(Integer.toHexString(getNumSelectedTabs().toInt())).append("\n")
        buffer.append("    .tabwidthratio   = ")
            .append(Integer.toHexString(getTabWidthRatio().toInt())).append("\n")
        buffer.append("[/WINDOW1]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(getHorizontalHold().toInt())
        out.writeShort(getVerticalHold().toInt())
        out.writeShort(getWidth().toInt())
        out.writeShort(getHeight().toInt())
        out.writeShort(getOptions().toInt())
        out.writeShort(getActiveSheetIndex())
        out.writeShort(getFirstVisibleTab())
        out.writeShort(getNumSelectedTabs().toInt())
        out.writeShort(getTabWidthRatio().toInt())
    }

    override fun getDataSize(): Int {
        return 18
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    companion object {
        const val sid: Short = 0x3d

        private val hidden = getInstance(0x01) // is this window is hidden
        private val iconic = getInstance(0x02) // is this window is an icon
        private val reserved = getInstance(0x04) // reserved
        private val hscroll = getInstance(0x08) // display horizontal scrollbar
        private val vscroll = getInstance(0x10) // display vertical scrollbar
        private val tabs = getInstance(0x20) // display tabs at the bottom
    }
}
