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
package com.wxiwei.office.fc.hssf.usermodel

import com.wxiwei.office.fc.hssf.record.PrintSetupRecord
import com.wxiwei.office.fc.ss.usermodel.PrintSetup


/**
 * Used to modify the print setup.
 * <P>
 * Paper size constants have been added for the ones I have access
 * to.  They follow as:<br></br>
 * public static final short LETTER_PAPERSIZE 	          = 1;<br></br>
 * public static final short LEGAL_PAPERSIZE 		  = 5;<br></br>
 * public static final short EXECUTIVE_PAPERSIZE 	  = 7;<br></br>
 * public static final short A4_PAPERSIZE 	  	  = 9;<br></br>
 * public static final short A5_PAPERSIZE 		  = 11;<br></br>
 * public static final short ENVELOPE_10_PAPERSIZE 	  = 20;<br></br>
 * public static final short ENVELOPE_DL_PAPERSIZE 	  = 27;<br></br>
 * public static final short ENVELOPE_CS_PAPERSIZE 	  = 28;<br></br>
 * public static final short ENVELOPE_MONARCH_PAPERSIZE  = 37;<br></br>
</P> * <P>
 * @author Shawn Laubach (slaubach at apache dot org)
</P> */
class HSSFPrintSetup
/**
 * Constructor.  Takes the low level print setup record.
 * @param printSetupRecord the low level print setup record
 */(var printSetupRecord: PrintSetupRecord) : PrintSetup {
    /**
     * Set the paper size.
     * @param size the paper size.
     */
    override fun setPaperSize(size: Short) {
        printSetupRecord.setPaperSize(size)
    }

    /**
     * Set the scale.
     * @param scale the scale to use
     */
    override fun setScale(scale: Short) {
        printSetupRecord.setScale(scale)
    }

    /**
     * Set the page numbering start.
     * @param start the page numbering start
     */
    override fun setPageStart(start: Short) {
        printSetupRecord.setPageStart(start)
    }

    /**
     * Set the number of pages wide to fit the sheet in
     * @param width the number of pages
     */
    override fun setFitWidth(width: Short) {
        printSetupRecord.setFitWidth(width)
    }

    /**
     * Set the number of pages high to fit the sheet in
     * @param height the number of pages
     */
    override fun setFitHeight(height: Short) {
        printSetupRecord.setFitHeight(height)
    }

    /**
     * Set whether to go left to right or top down in ordering
     * @param ltor left to right
     */
    override fun setLeftToRight(ltor: Boolean) {
        printSetupRecord.setLeftToRight(ltor)
    }

    /**
     * Set whether to print in landscape
     * @param ls landscape
     */
    override fun setLandscape(ls: Boolean) {
        printSetupRecord.setLandscape(!ls)
    }

    /**
     * Valid settings.  I'm not for sure.
     * @param valid Valid
     */
    override fun setValidSettings(valid: Boolean) {
        printSetupRecord.setValidSettings(valid)
    }

    /**
     * Set whether it is black and white
     * @param mono Black and white
     */
    override fun setNoColor(mono: Boolean) {
        printSetupRecord.setNoColor(mono)
    }

    /**
     * Set whether it is in draft mode
     * @param d draft
     */
    override fun setDraft(d: Boolean) {
        printSetupRecord.setDraft(d)
    }

    /**
     * Print the include notes
     * @param printnotes print the notes
     */
    override fun setNotes(printnotes: Boolean) {
        printSetupRecord.setNotes(printnotes)
    }

    /**
     * Set no orientation. ?
     * @param orientation Orientation.
     */
    override fun setNoOrientation(orientation: Boolean) {
        printSetupRecord.setNoOrientation(orientation)
    }

    /**
     * Set whether to use page start
     * @param page Use page start
     */
    override fun setUsePage(page: Boolean) {
        printSetupRecord.setUsePage(page)
    }

    /**
     * Sets the horizontal resolution.
     * @param resolution horizontal resolution
     */
    override fun setHResolution(resolution: Short) {
        printSetupRecord.setHResolution(resolution)
    }

    /**
     * Sets the vertical resolution.
     * @param resolution vertical resolution
     */
    override fun setVResolution(resolution: Short) {
        printSetupRecord.setVResolution(resolution)
    }

    /**
     * Sets the header margin.
     * @param headermargin header margin
     */
    override fun setHeaderMargin(headermargin: Double) {
        printSetupRecord.setHeaderMargin(headermargin)
    }

    /**
     * Sets the footer margin.
     * @param footermargin footer margin
     */
    override fun setFooterMargin(footermargin: Double) {
        printSetupRecord.setFooterMargin(footermargin)
    }

    /**
     * Sets the number of copies.
     * @param copies number of copies
     */
    override fun setCopies(copies: Short) {
        printSetupRecord.setCopies(copies)
    }

    /**
     * Returns the paper size.
     * @return paper size
     */
    override fun getPaperSize(): Short {
        return printSetupRecord.getPaperSize()
    }

    /**
     * Returns the scale.
     * @return scale
     */
    override fun getScale(): Short {
        return printSetupRecord.getScale()
    }

    /**
     * Returns the page start.
     * @return page start
     */
    override fun getPageStart(): Short {
        return printSetupRecord.getPageStart()
    }

    /**
     * Returns the number of pages wide to fit sheet in.
     * @return number of pages wide to fit sheet in
     */
    override fun getFitWidth(): Short {
        return printSetupRecord.getFitWidth()
    }

    /**
     * Returns the number of pages high to fit the sheet in.
     * @return number of pages high to fit the sheet in
     */
    override fun getFitHeight(): Short {
        return printSetupRecord.getFitHeight()
    }

    var options: Short
        /**
         * Returns the bit flags for the options.
         * @return bit flags for the options
         */
        get() = printSetupRecord.getOptions()
        /**
         * Sets the options flags.  Not advisable to do it directly.
         * @param options The bit flags for the options
         */
        set(options) {
            printSetupRecord.setOptions(options)
        }

    /**
     * Returns the left to right print order.
     * @return left to right print order
     */
    override fun getLeftToRight(): Boolean {
        return printSetupRecord.getLeftToRight()
    }

    /**
     * Returns the landscape mode.
     * @return landscape mode
     */
    override fun getLandscape(): Boolean {
        return !printSetupRecord.getLandscape()
    }

    /**
     * Returns the valid settings.
     * @return valid settings
     */
    override fun getValidSettings(): Boolean {
        return printSetupRecord.getValidSettings()
    }

    /**
     * Returns the black and white setting.
     * @return black and white setting
     */
    override fun getNoColor(): Boolean {
        return printSetupRecord.getNoColor()
    }

    /**
     * Returns the draft mode.
     * @return draft mode
     */
    override fun getDraft(): Boolean {
        return printSetupRecord.getDraft()
    }

    /**
     * Returns the print notes.
     * @return print notes
     */
    override fun getNotes(): Boolean {
        return printSetupRecord.getNotes()
    }

    /**
     * Returns the no orientation.
     * @return no orientation
     */
    override fun getNoOrientation(): Boolean {
        return printSetupRecord.getNoOrientation()
    }

    /**
     * Returns the use page numbers.
     * @return use page numbers
     */
    override fun getUsePage(): Boolean {
        return printSetupRecord.getUsePage()
    }

    /**
     * Returns the horizontal resolution.
     * @return horizontal resolution
     */
    override fun getHResolution(): Short {
        return printSetupRecord.getHResolution()
    }

    /**
     * Returns the vertical resolution.
     * @return vertical resolution
     */
    override fun getVResolution(): Short {
        return printSetupRecord.getVResolution()
    }

    /**
     * Returns the header margin.
     * @return header margin
     */
    override fun getHeaderMargin(): Double {
        return printSetupRecord.getHeaderMargin()
    }

    /**
     * Returns the footer margin.
     * @return footer margin
     */
    override fun getFooterMargin(): Double {
        return printSetupRecord.getFooterMargin()
    }

    /**
     * Returns the number of copies.
     * @return number of copies
     */
    override fun getCopies(): Short {
        return printSetupRecord.getCopies()
    }
}
