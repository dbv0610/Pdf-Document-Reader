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
 * Title:        WSBOOL (0x0081) (called SHEETPR in OOO doc)
 *
 *
 * Description:  stores workbook settings  (aka its a big "everything we didn't
 * put somewhere else")<P>
 * REFERENCE:  PG 425 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Glen Stampoultzis (gstamp@iprimus.com.au)
 * @author Jason Height (jheight at chariot dot net dot au)
</P> */
class WSBoolRecord : StandardRecord {
    private var field_1_wsbool: Byte =
        0 // crappy names are because this is really one big short field (2byte)
    private var field_2_wsbool: Byte = 0 // but the docs inconsistently use it as 2 separate bytes

    constructor()

    constructor(`in`: RecordInputStream) {
        val data = `in`.readRemainder()
        field_1_wsbool =
            data!![1] // backwards because theoretically this is one short field
        field_2_wsbool =
            data[0] // but it was easier to implement it this way to avoid confusion
    } // because the dev kit shows the masks for it as 2 byte fields

    // why?  Why ask why?  But don't drink bud dry as its a really
    // crappy beer, try the czech "Budvar" beer (which is the real
    // budweiser though its ironically good...its sold in the USs
    // as czechvar  --- odd that they had the name first but can't
    // use it)...
    /**
     * set first byte (see bit setters)
     */
    fun setWSBool1(bool1: Byte) {
        field_1_wsbool = bool1
    }

    // bool1 bitfields
    /**
     * show automatic page breaks or not
     * @param ab  whether to show auto page breaks
     */
    fun setAutobreaks(ab: Boolean) {
        field_1_wsbool = autobreaks.setByteBoolean(field_1_wsbool, ab)
    }

    /**
     * set whether sheet is a dialog sheet or not
     * @param isDialog or not
     */
    fun setDialog(isDialog: Boolean) {
        field_1_wsbool = dialog.setByteBoolean(field_1_wsbool, isDialog)
    }

    /**
     * set if row summaries appear below detail in the outline
     * @param below or not
     */
    fun setRowSumsBelow(below: Boolean) {
        field_1_wsbool = rowsumsbelow.setByteBoolean(field_1_wsbool, below)
    }

    /**
     * set if col summaries appear right of the detail in the outline
     * @param right or not
     */
    fun setRowSumsRight(right: Boolean) {
        field_1_wsbool = rowsumsright.setByteBoolean(field_1_wsbool, right)
    }

    // end bitfields
    /**
     * set the second byte (see bit setters)
     */
    fun setWSBool2(bool2: Byte) {
        field_2_wsbool = bool2
    }

    // bool2 bitfields
    /**
     * fit to page option is on
     * @param fit2page  fit or not
     */
    fun setFitToPage(fit2page: Boolean) {
        field_2_wsbool = fittopage.setByteBoolean(field_2_wsbool, fit2page)
    }

    /**
     * set whether to display the guts or not
     * 
     * @param guts or no guts (or glory)
     */
    fun setDisplayGuts(guts: Boolean) {
        field_2_wsbool = displayguts.setByteBoolean(field_2_wsbool, guts)
    }

    /**
     * whether alternate expression evaluation is on
     * @param altexp  alternative expression evaluation or not
     */
    fun setAlternateExpression(altexp: Boolean) {
        field_2_wsbool = alternateexpression.setByteBoolean(
            field_2_wsbool,
            altexp
        )
    }

    /**
     * whether alternative formula entry is on
     * @param formula  alternative formulas or not
     */
    fun setAlternateFormula(formula: Boolean) {
        field_2_wsbool = alternateformula.setByteBoolean(
            field_2_wsbool,
            formula
        )
    }

    // end bitfields
    /**
     * get first byte (see bit getters)
     */
    fun getWSBool1(): Byte {
        return field_1_wsbool
    }

    // bool1 bitfields
    /**
     * show automatic page breaks or not
     * @return whether to show auto page breaks
     */
    fun getAutobreaks(): Boolean {
        return autobreaks.isSet(field_1_wsbool.toInt())
    }

    /**
     * get whether sheet is a dialog sheet or not
     * @return isDialog or not
     */
    fun getDialog(): Boolean {
        return dialog.isSet(field_1_wsbool.toInt())
    }

    /**
     * get if row summaries appear below detail in the outline
     * @return below or not
     */
    fun getRowSumsBelow(): Boolean {
        return rowsumsbelow.isSet(field_1_wsbool.toInt())
    }

    /**
     * get if col summaries appear right of the detail in the outline
     * @return right or not
     */
    fun getRowSumsRight(): Boolean {
        return rowsumsright.isSet(field_1_wsbool.toInt())
    }

    // end bitfields
    /**
     * get the second byte (see bit getters)
     */
    fun getWSBool2(): Byte {
        return field_2_wsbool
    }

    // bool2 bitfields
    /**
     * fit to page option is on
     * @return fit or not
     */
    fun getFitToPage(): Boolean {
        return fittopage.isSet(field_2_wsbool.toInt())
    }

    /**
     * get whether to display the guts or not
     * 
     * @return guts or no guts (or glory)
     */
    fun getDisplayGuts(): Boolean {
        return displayguts.isSet(field_2_wsbool.toInt())
    }

    /**
     * whether alternate expression evaluation is on
     * @return alternative expression evaluation or not
     */
    fun getAlternateExpression(): Boolean {
        return alternateexpression.isSet(field_2_wsbool.toInt())
    }

    /**
     * whether alternative formula entry is on
     * @return alternative formulas or not
     */
    fun getAlternateFormula(): Boolean {
        return alternateformula.isSet(field_2_wsbool.toInt())
    }

    // end bitfields
    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[WSBOOL]\n")
        buffer.append("    .wsbool1        = ")
            .append(Integer.toHexString(getWSBool1().toInt())).append("\n")
        buffer.append("        .autobreaks = ").append(getAutobreaks())
            .append("\n")
        buffer.append("        .dialog     = ").append(getDialog())
            .append("\n")
        buffer.append("        .rowsumsbelw= ").append(getRowSumsBelow())
            .append("\n")
        buffer.append("        .rowsumsrigt= ").append(getRowSumsRight())
            .append("\n")
        buffer.append("    .wsbool2        = ")
            .append(Integer.toHexString(getWSBool2().toInt())).append("\n")
        buffer.append("        .fittopage  = ").append(getFitToPage())
            .append("\n")
        buffer.append("        .displayguts= ").append(getDisplayGuts())
            .append("\n")
        buffer.append("        .alternateex= ")
            .append(getAlternateExpression()).append("\n")
        buffer.append("        .alternatefo= ").append(getAlternateFormula())
            .append("\n")
        buffer.append("[/WSBOOL]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeByte(getWSBool2().toInt())
        out.writeByte(getWSBool1().toInt())
    }

    override fun getDataSize(): Int {
        return 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = WSBoolRecord()
        rec.field_1_wsbool = field_1_wsbool
        rec.field_2_wsbool = field_2_wsbool
        return rec
    }

    companion object {
        const val sid: Short = 0x0081

        // I decided to be consistent in this way.
        private val autobreaks = getInstance(0x01) // are automatic page breaks visible

        // bits 1 to 3 unused
        private val dialog = getInstance(0x10) // is sheet dialog sheet
        private val applystyles = getInstance(0x20) // whether to apply automatic styles to outlines
        private val rowsumsbelow =
            getInstance(0x40) // whether summary rows will appear below detail in outlines
        private val rowsumsright =
            getInstance(0x80) // whether summary rows will appear right of the detail in outlines
        private val fittopage = getInstance(0x01) // whether to fit stuff to the page

        // bit 2 reserved
        private val displayguts =
            getInstance(0x06) // whether to display outline symbols (in the gutters)

        // bits 4-5 reserved
        private val alternateexpression =
            getInstance(0x40) // whether to use alternate expression eval
        private val alternateformula = getInstance(0x80) // whether to use alternate formula entry
    }
}
