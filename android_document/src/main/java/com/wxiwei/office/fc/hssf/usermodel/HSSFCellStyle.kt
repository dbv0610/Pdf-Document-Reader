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

import com.wxiwei.office.fc.hssf.model.InternalWorkbook
import com.wxiwei.office.fc.hssf.record.ExtendedFormatRecord
import com.wxiwei.office.fc.hssf.util.HSSFColor
import com.wxiwei.office.fc.ss.usermodel.ICellStyle
import com.wxiwei.office.fc.ss.usermodel.IFont
import com.wxiwei.office.fc.ss.usermodel.Workbook


/**
 * High level representation of the style of a cell in a sheet of a workbook.
 * 
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @see HSSFWorkbook.createCellStyle
 * @see HSSFWorkbook.getCellStyleAt
 * @see HSSFCell.setCellStyle
 */
class HSSFCellStyle(index: Short, rec: ExtendedFormatRecord?, workbook: InternalWorkbook?) :
    ICellStyle {
    private var _format: ExtendedFormatRecord? = null
    private var _index: Short = 0
    private var _workbook: InternalWorkbook? = null


    /** Creates new HSSFCellStyle why would you want to do this??  */
    constructor(index: Short, rec: ExtendedFormatRecord?, workbook: HSSFWorkbook) : this(
        index,
        rec,
        workbook.getWorkbook()
    )

    init {
        _workbook = workbook
        _index = index
        _format = rec
    }

    /**
     * get the index within the HSSFWorkbook (sequence within the collection of ExtnededFormat objects)
     * @return unique index number of the underlying record this style represents (probably you don't care
     * unless you're comparing which one is which)
     */
    override fun getIndex(): Short {
        return _index
    }

    val parentStyle: HSSFCellStyle?
        /**
         * Return the parent style for this cell style.
         * In most cases this will be null, but in a few
         * cases there'll be a fully defined parent.
         */
        get() {
            val parentIndex = _format!!.getParentIndex()
            // parentIndex equal 0xFFF indicates no inheritance from a cell style XF (See 2.4.353 XF)
            if (parentIndex.toInt() == 0 || parentIndex.toInt() == 0xFFF) {
                return null
            }
            return HSSFCellStyle(
                parentIndex,
                _workbook!!.getExFormatAt(parentIndex.toInt()),
                _workbook
            )
        }

    /**
     * set the data format (must be a valid format)
     * @see HSSFDataFormat
     */
    override fun setDataFormat(fmt: Short) {
        _format!!.setFormatIndex(fmt)
    }

    /**
     * get the index of the format
     * @see HSSFDataFormat
     */
    override fun getDataFormat(): Short {
        return _format!!.getFormatIndex()
    }

    /**
     * Get the contents of the format string, by looking up
     * the DataFormat against the bound workbook
     * @see HSSFDataFormat
     * 
     * @return the format string or "General" if not found
     */
    override fun getDataFormatString(): String? {
        return getDataFormatString(_workbook)
    }

    /**
     * Get the contents of the format string, by looking up
     * the DataFormat against the supplied workbook
     * @see HSSFDataFormat
     * 
     * 
     * @return the format string or "General" if not found
     */
    fun getDataFormatString(workbook: Workbook): String? {
        val format = HSSFDataFormat((workbook as HSSFWorkbook).getWorkbook())

        val idx = getDataFormat().toInt()
        return if (idx == -1) "General" else format.getFormat(getDataFormat())
    }

    /**
     * Get the contents of the format string, by looking up
     * the DataFormat against the supplied low level workbook
     * @see HSSFDataFormat
     */
    fun getDataFormatString(workbook: InternalWorkbook?): String? {
//    	HSSFDataFormat format = new HSSFDataFormat( workbook );
//
//        return format.getFormat(getDataFormat());
        return HSSFDataFormat.getFormatCode(workbook!!, _format!!.getFormatIndex())
    }

    /**
     * set the font for this style
     * @param font  a font object created or retreived from the HSSFWorkbook object
     * @see HSSFWorkbook.createFont
     * @see HSSFWorkbook.getFontAt
     */
    override fun setFont(font: IFont?) {
        setFont(font as HSSFFont?)
    }

    fun setFont(font: HSSFFont) {
        _format!!.setIndentNotParentFont(true)
        val fontindex = font.getIndex()
        _format!!.setFontIndex(fontindex)
    }

    /**
     * gets the index of the font for this style
     * @see HSSFWorkbook.getFontAt
     */
    override fun getFontIndex(): Short {
        return _format!!.getFontIndex()
    }

    /**
     * gets the font for this style
     * @param parentWorkbook The HSSFWorkbook that this style belongs to
     * @see getFontIndex
     * @see HSSFWorkbook.getFontAt
     */
    fun getFont(parentWorkbook: Workbook): HSSFFont? {
        return (parentWorkbook as HSSFWorkbook).getFontAt(getFontIndex())
    }

    /**
     * set the cell's using this style to be hidden
     * @param hidden - whether the cell using this style should be hidden
     */
    override fun setHidden(hidden: Boolean) {
        _format!!.setIndentNotParentCellOptions(true)
        _format!!.setHidden(hidden)
    }

    /**
     * get whether the cell's using this style are to be hidden
     * @return hidden - whether the cell using this style should be hidden
     */
    override fun getHidden(): Boolean {
        return _format!!.isHidden()
    }

    /**
     * set the cell's using this style to be locked
     * @param locked - whether the cell using this style should be locked
     */
    override fun setLocked(locked: Boolean) {
        _format!!.setIndentNotParentCellOptions(true)
        _format!!.setLocked(locked)
    }

    /**
     * get whether the cell's using this style are to be locked
     * @return hidden - whether the cell using this style should be locked
     */
    override fun getLocked(): Boolean {
        return _format!!.isLocked()
    }

    /**
     * set the type of horizontal alignment for the cell
     * @param align - the type of alignment
     * @see .ALIGN_GENERAL
     * 
     * @see .ALIGN_LEFT
     * 
     * @see .ALIGN_CENTER
     * 
     * @see .ALIGN_RIGHT
     * 
     * @see .ALIGN_FILL
     * 
     * @see .ALIGN_JUSTIFY
     * 
     * @see .ALIGN_CENTER_SELECTION
     */
    override fun setAlignment(align: Short) {
        _format!!.setIndentNotParentAlignment(true)
        _format!!.setAlignment(align)
    }

    /**
     * get the type of horizontal alignment for the cell
     * @return align - the type of alignment
     * @see .ALIGN_GENERAL
     * 
     * @see .ALIGN_LEFT
     * 
     * @see .ALIGN_CENTER
     * 
     * @see .ALIGN_RIGHT
     * 
     * @see .ALIGN_FILL
     * 
     * @see .ALIGN_JUSTIFY
     * 
     * @see .ALIGN_CENTER_SELECTION
     */
    override fun getAlignment(): Short {
        return _format!!.getAlignment()
    }

    /**
     * set whether the text should be wrapped
     * @param wrapped  wrap text or not
     */
    override fun setWrapText(wrapped: Boolean) {
        _format!!.setIndentNotParentAlignment(true)
        _format!!.setWrapText(wrapped)
    }

    /**
     * get whether the text should be wrapped
     * @return wrap text or not
     */
    override fun getWrapText(): Boolean {
        return _format!!.getWrapText()
    }

    /**
     * set the type of vertical alignment for the cell
     * @param align the type of alignment
     * @see .VERTICAL_TOP
     * 
     * @see .VERTICAL_CENTER
     * 
     * @see .VERTICAL_BOTTOM
     * 
     * @see .VERTICAL_JUSTIFY
     */
    override fun setVerticalAlignment(align: Short) {
        _format!!.setVerticalAlignment(align)
    }

    /**
     * get the type of vertical alignment for the cell
     * @return align the type of alignment
     * @see .VERTICAL_TOP
     * 
     * @see .VERTICAL_CENTER
     * 
     * @see .VERTICAL_BOTTOM
     * 
     * @see .VERTICAL_JUSTIFY
     */
    override fun getVerticalAlignment(): Short {
        return _format!!.getVerticalAlignment()
    }

    /**
     * set the degree of rotation for the text in the cell
     * @param rotation degrees (between -90 and 90 degrees, of 0xff for vertical)
     */
    override fun setRotation(rotation: Short) {
        var rotation = rotation
        if (rotation.toInt() == 0xff) {
            // Special cases for vertically aligned text
        } else if ((rotation < 0) && (rotation >= -90)) {
            //Take care of the funny 4th quadrant issue
            //The 4th quadrant (-1 to -90) is stored as (91 to 180)
            rotation = (90 - rotation).toShort()
        } else require(!((rotation < -90) || (rotation > 90))) { "The rotation must be between -90 and 90 degrees, or 0xff" }
        _format!!.setRotation(rotation)
    }

    /**
     * get the degree of rotation for the text in the cell
     * @return rotation degrees (between -90 and 90 degrees, or 0xff for vertical)
     */
    override fun getRotation(): Short {
        var rotation = _format!!.getRotation()
        if (rotation.toInt() == 0xff) {
            // Vertical aligned special case
            return rotation
        }
        if (rotation > 90) {
            //This is actually the 4th quadrant
            rotation = (90 - rotation).toShort()
        }
        return rotation
    }

    /**
     * set the number of spaces to indent the text in the cell
     * @param indent - number of spaces
     */
    override fun setIndention(indent: Short) {
        _format!!.setIndent(indent)
    }

    /**
     * get the number of spaces to indent the text in the cell
     * @return indent - number of spaces
     */
    override fun getIndention(): Short {
        return _format!!.getIndent()
    }

    /**
     * set the type of border to use for the left border of the cell
     * @param border type
     * @see .BORDER_NONE
     * 
     * @see .BORDER_THIN
     * 
     * @see .BORDER_MEDIUM
     * 
     * @see .BORDER_DASHED
     * 
     * @see .BORDER_DOTTED
     * 
     * @see .BORDER_THICK
     * 
     * @see .BORDER_DOUBLE
     * 
     * @see .BORDER_HAIR
     * 
     * @see .BORDER_MEDIUM_DASHED
     * 
     * @see .BORDER_DASH_DOT
     * 
     * @see .BORDER_MEDIUM_DASH_DOT
     * 
     * @see .BORDER_DASH_DOT_DOT
     * 
     * @see .BORDER_MEDIUM_DASH_DOT_DOT
     * 
     * @see .BORDER_SLANTED_DASH_DOT
     */
    override fun setBorderLeft(border: Short) {
        _format!!.setIndentNotParentBorder(true)
        _format!!.setBorderLeft(border)
    }

    /**
     * get the type of border to use for the left border of the cell
     * @return border type
     * @see .BORDER_NONE
     * 
     * @see .BORDER_THIN
     * 
     * @see .BORDER_MEDIUM
     * 
     * @see .BORDER_DASHED
     * 
     * @see .BORDER_DOTTED
     * 
     * @see .BORDER_THICK
     * 
     * @see .BORDER_DOUBLE
     * 
     * @see .BORDER_HAIR
     * 
     * @see .BORDER_MEDIUM_DASHED
     * 
     * @see .BORDER_DASH_DOT
     * 
     * @see .BORDER_MEDIUM_DASH_DOT
     * 
     * @see .BORDER_DASH_DOT_DOT
     * 
     * @see .BORDER_MEDIUM_DASH_DOT_DOT
     * 
     * @see .BORDER_SLANTED_DASH_DOT
     */
    override fun getBorderLeft(): Short {
        return _format!!.getBorderLeft()
    }

    /**
     * set the type of border to use for the right border of the cell
     * @param border type
     * @see .BORDER_NONE
     * 
     * @see .BORDER_THIN
     * 
     * @see .BORDER_MEDIUM
     * 
     * @see .BORDER_DASHED
     * 
     * @see .BORDER_DOTTED
     * 
     * @see .BORDER_THICK
     * 
     * @see .BORDER_DOUBLE
     * 
     * @see .BORDER_HAIR
     * 
     * @see .BORDER_MEDIUM_DASHED
     * 
     * @see .BORDER_DASH_DOT
     * 
     * @see .BORDER_MEDIUM_DASH_DOT
     * 
     * @see .BORDER_DASH_DOT_DOT
     * 
     * @see .BORDER_MEDIUM_DASH_DOT_DOT
     * 
     * @see .BORDER_SLANTED_DASH_DOT
     */
    override fun setBorderRight(border: Short) {
        _format!!.setIndentNotParentBorder(true)
        _format!!.setBorderRight(border)
    }

    /**
     * get the type of border to use for the right border of the cell
     * @return border type
     * @see .BORDER_NONE
     * 
     * @see .BORDER_THIN
     * 
     * @see .BORDER_MEDIUM
     * 
     * @see .BORDER_DASHED
     * 
     * @see .BORDER_DOTTED
     * 
     * @see .BORDER_THICK
     * 
     * @see .BORDER_DOUBLE
     * 
     * @see .BORDER_HAIR
     * 
     * @see .BORDER_MEDIUM_DASHED
     * 
     * @see .BORDER_DASH_DOT
     * 
     * @see .BORDER_MEDIUM_DASH_DOT
     * 
     * @see .BORDER_DASH_DOT_DOT
     * 
     * @see .BORDER_MEDIUM_DASH_DOT_DOT
     * 
     * @see .BORDER_SLANTED_DASH_DOT
     */
    override fun getBorderRight(): Short {
        return _format!!.getBorderRight()
    }

    /**
     * set the type of border to use for the top border of the cell
     * @param border type
     * @see .BORDER_NONE
     * 
     * @see .BORDER_THIN
     * 
     * @see .BORDER_MEDIUM
     * 
     * @see .BORDER_DASHED
     * 
     * @see .BORDER_DOTTED
     * 
     * @see .BORDER_THICK
     * 
     * @see .BORDER_DOUBLE
     * 
     * @see .BORDER_HAIR
     * 
     * @see .BORDER_MEDIUM_DASHED
     * 
     * @see .BORDER_DASH_DOT
     * 
     * @see .BORDER_MEDIUM_DASH_DOT
     * 
     * @see .BORDER_DASH_DOT_DOT
     * 
     * @see .BORDER_MEDIUM_DASH_DOT_DOT
     * 
     * @see .BORDER_SLANTED_DASH_DOT
     */
    override fun setBorderTop(border: Short) {
        _format!!.setIndentNotParentBorder(true)
        _format!!.setBorderTop(border)
    }

    /**
     * get the type of border to use for the top border of the cell
     * @return border type
     * @see .BORDER_NONE
     * 
     * @see .BORDER_THIN
     * 
     * @see .BORDER_MEDIUM
     * 
     * @see .BORDER_DASHED
     * 
     * @see .BORDER_DOTTED
     * 
     * @see .BORDER_THICK
     * 
     * @see .BORDER_DOUBLE
     * 
     * @see .BORDER_HAIR
     * 
     * @see .BORDER_MEDIUM_DASHED
     * 
     * @see .BORDER_DASH_DOT
     * 
     * @see .BORDER_MEDIUM_DASH_DOT
     * 
     * @see .BORDER_DASH_DOT_DOT
     * 
     * @see .BORDER_MEDIUM_DASH_DOT_DOT
     * 
     * @see .BORDER_SLANTED_DASH_DOT
     */
    override fun getBorderTop(): Short {
        return _format!!.getBorderTop()
    }

    /**
     * set the type of border to use for the bottom border of the cell
     * @param border type
     * @see .BORDER_NONE
     * 
     * @see .BORDER_THIN
     * 
     * @see .BORDER_MEDIUM
     * 
     * @see .BORDER_DASHED
     * 
     * @see .BORDER_DOTTED
     * 
     * @see .BORDER_THICK
     * 
     * @see .BORDER_DOUBLE
     * 
     * @see .BORDER_HAIR
     * 
     * @see .BORDER_MEDIUM_DASHED
     * 
     * @see .BORDER_DASH_DOT
     * 
     * @see .BORDER_MEDIUM_DASH_DOT
     * 
     * @see .BORDER_DASH_DOT_DOT
     * 
     * @see .BORDER_MEDIUM_DASH_DOT_DOT
     * 
     * @see .BORDER_SLANTED_DASH_DOT
     */
    override fun setBorderBottom(border: Short) {
        _format!!.setIndentNotParentBorder(true)
        _format!!.setBorderBottom(border)
    }

    /**
     * get the type of border to use for the bottom border of the cell
     * @return border type
     * @see .BORDER_NONE
     * 
     * @see .BORDER_THIN
     * 
     * @see .BORDER_MEDIUM
     * 
     * @see .BORDER_DASHED
     * 
     * @see .BORDER_DOTTED
     * 
     * @see .BORDER_THICK
     * 
     * @see .BORDER_DOUBLE
     * 
     * @see .BORDER_HAIR
     * 
     * @see .BORDER_MEDIUM_DASHED
     * 
     * @see .BORDER_DASH_DOT
     * 
     * @see .BORDER_MEDIUM_DASH_DOT
     * 
     * @see .BORDER_DASH_DOT_DOT
     * 
     * @see .BORDER_MEDIUM_DASH_DOT_DOT
     * 
     * @see .BORDER_SLANTED_DASH_DOT
     */
    override fun getBorderBottom(): Short {
        return _format!!.getBorderBottom()
    }

    /**
     * set the color to use for the left border
     * @param color The index of the color definition
     */
    override fun setLeftBorderColor(color: Short) {
        _format!!.setLeftBorderPaletteIdx(color)
    }

    /**
     * get the color to use for the left border
     * @see HSSFPalette.getColor
     * @return The index of the color definition
     */
    override fun getLeftBorderColor(): Short {
        return _format!!.getLeftBorderPaletteIdx()
    }

    /**
     * set the color to use for the right border
     * @param color The index of the color definition
     */
    override fun setRightBorderColor(color: Short) {
        _format!!.setRightBorderPaletteIdx(color)
    }

    /**
     * get the color to use for the left border
     * @see HSSFPalette.getColor
     * @return The index of the color definition
     */
    override fun getRightBorderColor(): Short {
        return _format!!.getRightBorderPaletteIdx()
    }

    /**
     * set the color to use for the top border
     * @param color The index of the color definition
     */
    override fun setTopBorderColor(color: Short) {
        _format!!.setTopBorderPaletteIdx(color)
    }

    /**
     * get the color to use for the top border
     * @see HSSFPalette.getColor
     * @return The index of the color definition
     */
    override fun getTopBorderColor(): Short {
        return _format!!.getTopBorderPaletteIdx()
    }

    /**
     * set the color to use for the bottom border
     * @param color The index of the color definition
     */
    override fun setBottomBorderColor(color: Short) {
        _format!!.setBottomBorderPaletteIdx(color)
    }

    /**
     * get the color to use for the left border
     * @see HSSFPalette.getColor
     * @return The index of the color definition
     */
    override fun getBottomBorderColor(): Short {
        return _format!!.getBottomBorderPaletteIdx()
    }

    /**
     * setting to one fills the cell with the foreground color... No idea about
     * other values
     * 
     * @see .NO_FILL
     * 
     * @see .SOLID_FOREGROUND
     * 
     * @see .FINE_DOTS
     * 
     * @see .ALT_BARS
     * 
     * @see .SPARSE_DOTS
     * 
     * @see .THICK_HORZ_BANDS
     * 
     * @see .THICK_VERT_BANDS
     * 
     * @see .THICK_BACKWARD_DIAG
     * 
     * @see .THICK_FORWARD_DIAG
     * 
     * @see .BIG_SPOTS
     * 
     * @see .BRICKS
     * 
     * @see .THIN_HORZ_BANDS
     * 
     * @see .THIN_VERT_BANDS
     * 
     * @see .THIN_BACKWARD_DIAG
     * 
     * @see .THIN_FORWARD_DIAG
     * 
     * @see .SQUARES
     * 
     * @see .DIAMONDS
     * 
     * 
     * @param fp  fill pattern (set to 1 to fill w/foreground color)
     */
    override fun setFillPattern(fp: Short) {
        _format!!.setAdtlFillPattern(fp)
    }

    /**
     * get the fill pattern (??) - set to 1 to fill with foreground color
     * @return fill pattern
     */
    override fun getFillPattern(): Short {
        return _format!!.getAdtlFillPattern()
    }

    /**
     * Checks if the background and foreground fills are set correctly when one
     * or the other is set to the default color.
     * 
     * Works like the logic table below:
     * 
     * BACKGROUND   FOREGROUND
     * 
     * NONE         AUTOMATIC
     * 
     * 0x41         0x40
     * 
     * NONE         RED/ANYTHING
     * 
     * 0x40         0xSOMETHING
     */
    private fun checkDefaultBackgroundFills() {
        if (_format!!.getFillForeground() == HSSFColor.AUTOMATIC.index) {
            //JMH: Why +1, hell why not. I guess it made some sense to someone at the time. Doesnt
            //to me now.... But experience has shown that when the fore is set to AUTOMATIC then the
            //background needs to be incremented......
            if (_format!!.getFillBackground()
                    .toInt() != (HSSFColor.AUTOMATIC.index + 1)
            ) setFillBackgroundColor((HSSFColor.AUTOMATIC.index + 1).toShort())
        } else if (_format!!.getFillBackground()
                .toInt() == HSSFColor.AUTOMATIC.index + 1
        )  //Now if the forground changes to a non-AUTOMATIC color the background resets itself!!!
            if (_format!!.getFillForeground() != HSSFColor.AUTOMATIC.index) setFillBackgroundColor(
                HSSFColor.AUTOMATIC.index
            )
    }

    /**
     * set the background fill color.
     * 
     * 
     * For example:
     * <pre>
     * cs.setFillPattern(HSSFCellStyle.FINE_DOTS );
     * cs.setFillBackgroundColor(new HSSFColor.RED().getIndex());
    </pre> * 
     * optionally a Foreground and background fill can be applied:
     * *Note: Ensure Foreground color is set prior to background*
     * <pre>
     * cs.setFillPattern(HSSFCellStyle.FINE_DOTS );
     * cs.setFillForegroundColor(new HSSFColor.BLUE().getIndex());
     * cs.setFillBackgroundColor(new HSSFColor.RED().getIndex());
    </pre> * 
     * or, for the special case of SOLID_FILL:
     * <pre>
     * cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND );
     * cs.setFillForegroundColor(new HSSFColor.RED().getIndex());
    </pre> * 
     * It is necessary to set the fill style in order
     * for the color to be shown in the cell.
     * 
     * @param bg  color
     */
    override fun setFillBackgroundColor(bg: Short) {
        _format!!.setFillBackground(bg)
        checkDefaultBackgroundFills()
    }

    /**
     * Get the background fill color.
     * Note - many cells are actually filled with a foreground
     * fill, not a background fill - see [.getFillForegroundColor]
     * @see HSSFPalette.getColor
     * @return fill color
     */
    override fun getFillBackgroundColor(): Short {
        val result = _format!!.getFillBackground()
        //JMH: Do this ridiculous conversion, and let HSSFCellStyle
        //internally migrate back and forth
        if (result.toInt() == (HSSFColor.AUTOMATIC.index + 1)) {
            return HSSFColor.AUTOMATIC.index
        }
        return result
    }

    override fun getFillBackgroundColorColor(): HSSFColor? {
        val pallette = HSSFPalette(
            _workbook!!.customPalette
        )
        return pallette.getColor(
            getFillBackgroundColor()
        )
    }

    /**
     * set the foreground fill color
     * *Note: Ensure Foreground color is set prior to background color.*
     * @param bg  color
     */
    override fun setFillForegroundColor(bg: Short) {
        _format!!.setFillForeground(bg)
        checkDefaultBackgroundFills()
    }

    /**
     * Get the foreground fill color.
     * Many cells are filled with this, instead of a
     * background color ([.getFillBackgroundColor])
     * @see HSSFPalette.getColor
     * @return fill color
     */
    override fun getFillForegroundColor(): Short {
        return _format!!.getFillForeground()
    }

    override fun getFillForegroundColorColor(): HSSFColor? {
        val pallette = HSSFPalette(
            _workbook!!.customPalette
        )
        return pallette.getColor(
            getFillForegroundColor()
        )
    }

    var userStyleName: String?
        /**
         * Gets the name of the user defined style.
         * Returns null for built in styles, and
         * styles where no name has been defined
         */
        get() {
            val sr = _workbook!!.getStyleRecord(_index.toInt())
            if (sr == null) {
                return null
            }
            if (sr.isBuiltin()) {
                return null
            }
            return sr.getName()
        }
        /**
         * Sets the name of the user defined style.
         * Will complain if you try this on a built in style.
         */
        set(styleName) {
            var sr = _workbook!!.getStyleRecord(_index.toInt())
            if (sr == null) {
                sr = _workbook!!.createStyleRecord(_index.toInt())
            }
            // All Style records start as "builtin", but generally
            //  only 20 and below really need to be
            require(!(sr.isBuiltin() && _index <= 20)) { "Unable to set user specified style names for built in styles!" }
            sr.setName(styleName!!)
        }

    /**
     * Verifies that this style belongs to the supplied Workbook.
     * Will throw an exception if it belongs to a different one.
     * This is normally called when trying to assign a style to a
     * cell, to ensure the cell and the style are from the same
     * workbook (if they're not, it won't work)
     * @throws IllegalArgumentException if there's a workbook mis-match
     */
    fun verifyBelongsToWorkbook(wb: HSSFWorkbook) {
        require(wb.getWorkbook() == _workbook) { "This Style does not belong to the supplied Workbook. Are you trying to assign a style from one workbook to the cell of a differnt workbook?" }
    }

    /**
     * Clones all the style information from another
     * HSSFCellStyle, onto this one. This
     * HSSFCellStyle will then have all the same
     * properties as the source, but the two may
     * be edited independently.
     * Any stylings on this HSSFCellStyle will be lost!
     * 
     * The source HSSFCellStyle could be from another
     * HSSFWorkbook if you like. This allows you to
     * copy styles from one HSSFWorkbook to another.
     */
    override fun cloneStyleFrom(source: ICellStyle?) {
        if (source is HSSFCellStyle) {
            this.cloneStyleFrom(source)
        } else {
            throw IllegalArgumentException("Can only clone from one HSSFCellStyle to another, not between HSSFCellStyle and XSSFCellStyle")
        }
    }

    fun cloneStyleFrom(source: HSSFCellStyle) {
        // First we need to clone the extended format
        //  record
        _format!!.cloneStyleFrom(source._format!!)

        // Handle matching things if we cross workbooks
        if (_workbook != source._workbook) {
            // Then we need to clone the format string,
            //  and update the format record for this
            val fmt = _workbook!!.createFormat(source.getDataFormatString()).toShort()
            setDataFormat(fmt)

            // Finally we need to clone the font,
            //  and update the format record for this
            val fr = _workbook!!.createNewFont()
            fr.cloneStyleFrom(
                source._workbook!!.getFontRecordAt(
                    source.getFontIndex().toInt()
                )
            )

            val font = HSSFFont(
                _workbook!!.getFontIndex(fr).toShort(), fr
            )
            setFont(font)
        }
    }


    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + (if (_format == null) 0 else _format.hashCode())
        result = prime * result + _index
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) return true
        if (obj == null) return false
        if (obj is HSSFCellStyle) {
            val other = obj
            if (_format == null) {
                if (other._format != null) return false
            } else if (!_format!!.equals(other._format)) return false
            if (_index != other._index) return false
            return true
        }
        return false
    }
}
