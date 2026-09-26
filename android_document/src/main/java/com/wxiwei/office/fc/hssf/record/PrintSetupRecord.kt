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
 * Title:        PAGESETUP (0x00A1)
 *
 *
 * Description:  Stores print setup options -- bogus for HSSF (and marked as such)
 *
 *
 * REFERENCE:  PG 385 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)
 *
 *
 * REFERENCE:  PG 412 Microsoft Excel Binary File Format Structure v20091214
 *
 *
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 */
class PrintSetupRecord : StandardRecord {
    /** Constants for this are held in [PrintSetup]  */
    private var field_1_paper_size: Short = 0
    private var field_2_scale: Short = 0
    private var field_3_page_start: Short = 0
    private var field_4_fit_width: Short = 0
    private var field_5_fit_height: Short = 0
    private var field_6_options: Short = 0
    private var field_7_hresolution: Short = 0
    private var field_8_vresolution: Short = 0
    private var field_9_headermargin = 0.0
    private var field_10_footermargin = 0.0
    private var field_11_copies: Short = 0

    constructor()

    constructor(`in`: RecordInputStream) {
        field_1_paper_size = `in`.readShort()
        field_2_scale = `in`.readShort()
        field_3_page_start = `in`.readShort()
        field_4_fit_width = `in`.readShort()
        field_5_fit_height = `in`.readShort()
        field_6_options = `in`.readShort()
        field_7_hresolution = `in`.readShort()
        field_8_vresolution = `in`.readShort()
        field_9_headermargin = `in`.readDouble()
        field_10_footermargin = `in`.readDouble()
        field_11_copies = `in`.readShort()
    }

    fun setPaperSize(size: Short) {
        field_1_paper_size = size
    }

    fun setScale(scale: Short) {
        field_2_scale = scale
    }

    fun setPageStart(start: Short) {
        field_3_page_start = start
    }

    fun setFitWidth(width: Short) {
        field_4_fit_width = width
    }

    fun setFitHeight(height: Short) {
        field_5_fit_height = height
    }

    fun setOptions(options: Short) {
        field_6_options = options
    }

    // option bitfields
    fun setLeftToRight(ltor: Boolean) {
        field_6_options = lefttoright.setShortBoolean(field_6_options, ltor)
    }

    fun setLandscape(ls: Boolean) {
        field_6_options = landscape.setShortBoolean(field_6_options, ls)
    }

    fun setValidSettings(valid: Boolean) {
        field_6_options = validsettings.setShortBoolean(field_6_options, valid)
    }

    fun setNoColor(mono: Boolean) {
        field_6_options = nocolor.setShortBoolean(field_6_options, mono)
    }

    fun setDraft(d: Boolean) {
        field_6_options = draft.setShortBoolean(field_6_options, d)
    }

    fun setNotes(printnotes: Boolean) {
        field_6_options = notes.setShortBoolean(field_6_options, printnotes)
    }

    fun setNoOrientation(orientation: Boolean) {
        field_6_options = noOrientation.setShortBoolean(field_6_options, orientation)
    }

    fun setUsePage(page: Boolean) {
        field_6_options = usepage.setShortBoolean(field_6_options, page)
    }

    // end option bitfields
    fun setHResolution(resolution: Short) {
        field_7_hresolution = resolution
    }

    fun setVResolution(resolution: Short) {
        field_8_vresolution = resolution
    }

    fun setHeaderMargin(headermargin: Double) {
        field_9_headermargin = headermargin
    }

    fun setFooterMargin(footermargin: Double) {
        field_10_footermargin = footermargin
    }

    fun setCopies(copies: Short) {
        field_11_copies = copies
    }

    fun getPaperSize(): Short {
        return field_1_paper_size
    }

    fun getScale(): Short {
        return field_2_scale
    }

    fun getPageStart(): Short {
        return field_3_page_start
    }

    fun getFitWidth(): Short {
        return field_4_fit_width
    }

    fun getFitHeight(): Short {
        return field_5_fit_height
    }

    fun getOptions(): Short {
        return field_6_options
    }

    // option bitfields
    fun getLeftToRight(): Boolean {
        return lefttoright.isSet(field_6_options.toInt())
    }

    fun getLandscape(): Boolean {
        return landscape.isSet(field_6_options.toInt())
    }

    fun getValidSettings(): Boolean {
        return validsettings.isSet(field_6_options.toInt())
    }

    fun getNoColor(): Boolean {
        return nocolor.isSet(field_6_options.toInt())
    }

    fun getDraft(): Boolean {
        return draft.isSet(field_6_options.toInt())
    }

    fun getNotes(): Boolean {
        return notes.isSet(field_6_options.toInt())
    }

    fun getNoOrientation(): Boolean {
        return noOrientation.isSet(field_6_options.toInt())
    }

    fun getUsePage(): Boolean {
        return usepage.isSet(field_6_options.toInt())
    }

    // end option bitfields
    fun getHResolution(): Short {
        return field_7_hresolution
    }

    fun getVResolution(): Short {
        return field_8_vresolution
    }

    fun getHeaderMargin(): Double {
        return field_9_headermargin
    }

    fun getFooterMargin(): Double {
        return field_10_footermargin
    }

    fun getCopies(): Short {
        return field_11_copies
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[PRINTSETUP]\n")
        buffer.append("    .papersize      = ").append(getPaperSize().toInt())
            .append("\n")
        buffer.append("    .scale          = ").append(getScale().toInt())
            .append("\n")
        buffer.append("    .pagestart      = ").append(getPageStart().toInt())
            .append("\n")
        buffer.append("    .fitwidth       = ").append(getFitWidth().toInt())
            .append("\n")
        buffer.append("    .fitheight      = ").append(getFitHeight().toInt())
            .append("\n")
        buffer.append("    .options        = ").append(getOptions().toInt())
            .append("\n")
        buffer.append("        .ltor       = ").append(getLeftToRight())
            .append("\n")
        buffer.append("        .landscape  = ").append(getLandscape())
            .append("\n")
        buffer.append("        .valid      = ").append(getValidSettings())
            .append("\n")
        buffer.append("        .mono       = ").append(getNoColor())
            .append("\n")
        buffer.append("        .draft      = ").append(getDraft())
            .append("\n")
        buffer.append("        .notes      = ").append(getNotes())
            .append("\n")
        buffer.append("        .noOrientat = ").append(getNoOrientation())
            .append("\n")
        buffer.append("        .usepage    = ").append(getUsePage())
            .append("\n")
        buffer.append("    .hresolution    = ").append(getHResolution().toInt())
            .append("\n")
        buffer.append("    .vresolution    = ").append(getVResolution().toInt())
            .append("\n")
        buffer.append("    .headermargin   = ").append(getHeaderMargin())
            .append("\n")
        buffer.append("    .footermargin   = ").append(getFooterMargin())
            .append("\n")
        buffer.append("    .copies         = ").append(getCopies().toInt())
            .append("\n")
        buffer.append("[/PRINTSETUP]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(getPaperSize().toInt())
        out.writeShort(getScale().toInt())
        out.writeShort(getPageStart().toInt())
        out.writeShort(getFitWidth().toInt())
        out.writeShort(getFitHeight().toInt())
        out.writeShort(getOptions().toInt())
        out.writeShort(getHResolution().toInt())
        out.writeShort(getVResolution().toInt())
        out.writeDouble(getHeaderMargin())
        out.writeDouble(getFooterMargin())
        out.writeShort(getCopies().toInt())
    }

    override fun getDataSize(): Int {
        return 34
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = PrintSetupRecord()
        rec.field_1_paper_size = field_1_paper_size
        rec.field_2_scale = field_2_scale
        rec.field_3_page_start = field_3_page_start
        rec.field_4_fit_width = field_4_fit_width
        rec.field_5_fit_height = field_5_fit_height
        rec.field_6_options = field_6_options
        rec.field_7_hresolution = field_7_hresolution
        rec.field_8_vresolution = field_8_vresolution
        rec.field_9_headermargin = field_9_headermargin
        rec.field_10_footermargin = field_10_footermargin
        rec.field_11_copies = field_11_copies
        return rec
    }

    companion object {
        const val sid: Short = 0x00A1
        private val lefttoright = getInstance(0x01) // print over then down
        private val landscape = getInstance(0x02) // landscape mode
        private val validsettings = getInstance(
            0x04
        ) // if papersize, scale, resolution, copies, landscape

        // weren't obtained from the print consider them
        // mere bunk
        private val nocolor = getInstance(0x08) // print mono/b&w, colorless
        private val draft = getInstance(0x10) // print draft quality
        private val notes = getInstance(0x20) // print the notes
        private val noOrientation = getInstance(0x40) // the orientation is not set
        private val usepage = getInstance(0x80) // use a user set page no, instead of auto
    }
}
