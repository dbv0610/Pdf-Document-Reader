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
 * Title:        Extended Format Record
 * Description:  Probably one of the more complex records.  There are two breeds:
 * Style and Cell.
 * <P>
 * It should be noted that fields in the extended format record are
 * somewhat arbitrary.  Almost all of the fields are bit-level, but
 * we name them as best as possible by functional group.  In some
 * places this is better than others.
</P> * <P>
 * 
 * REFERENCE:  PG 426 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
</P> */
class ExtendedFormatRecord

    : StandardRecord {
    // fields in BOTH style and Cell XF records
    private var field_1_font_index: Short = 0 // not bit-mapped
    private var field_2_format_index: Short = 0 // not bit-mapped

    private var field_3_cell_options: Short = 0

    private var field_4_alignment_options: Short = 0

    private var field_5_indention_options: Short = 0

    private var field_6_border_options: Short = 0

    private var field_7_palette_options: Short = 0

    private var field_8_adtl_palette_options = 0 // additional to avoid 2

    // apparently bits 15 and 14 are unused
    private var field_9_fill_palette_options: Short = 0

    /**
     * Constructor ExtendedFormatRecord
     * 
     * 
     */
    constructor()

    constructor(`in`: RecordInputStream) {
        field_1_font_index = `in`.readShort()
        field_2_format_index = `in`.readShort()
        field_3_cell_options = `in`.readShort()
        field_4_alignment_options = `in`.readShort()
        field_5_indention_options = `in`.readShort()
        field_6_border_options = `in`.readShort()
        field_7_palette_options = `in`.readShort()
        field_8_adtl_palette_options = `in`.readInt()
        field_9_fill_palette_options = `in`.readShort()
    }

    /**
     * set the index to the FONT record (which font to use 0 based)
     * 
     * 
     * @param index to the font
     * @see FontRecord
     */
    fun setFontIndex(index: Short) {
        field_1_font_index = index
    }

    fun setFormatIndex(index: Short) {
        field_2_format_index = index
    }

    @get:JvmName("getFontIndexProperty")
    @set:JvmName("setFontIndexProperty")
    var fontIndex: Short get() = getFontIndex(); set(v) = setFontIndex(v)
    @get:JvmName("getFormatIndexProperty")
    @set:JvmName("setFormatIndexProperty")
    var formatIndex: Short get() = getFormatIndex(); set(v) = setFormatIndex(v)

    /**
     * sets the options bitmask - you can also use corresponding option bit setters
     * (see other methods that reference this one)
     * 
     * 
     * @param options bitmask to set
     */
    fun setCellOptions(options: Short) {
        field_3_cell_options = options
    }

    // These are the bit fields in cell options
    /**
     * set whether the cell is locked or not
     * 
     * 
     * @param locked - if the cell is locked
     * @see .setCellOptions
     */
    fun setLocked(locked: Boolean) {
        field_3_cell_options = _locked.setShortBoolean(
            field_3_cell_options,
            locked
        )
    }

    /**
     * set whether the cell is hidden or not
     * 
     * 
     * @param hidden - if the cell is hidden
     * @see .setCellOptions
     */
    fun setHidden(hidden: Boolean) {
        field_3_cell_options = _hidden.setShortBoolean(
            field_3_cell_options,
            hidden
        )
    }

    /**
     * set whether the cell is a cell or style XFRecord
     * 
     * 
     * @param type - cell or style (0/1)
     * @see .XF_STYLE
     * 
     * @see .XF_CELL
     * 
     * @see .setCellOptions
     */
    fun setXFType(type: Short) {
        field_3_cell_options = _xf_type.setShortValue(
            field_3_cell_options,
            type
        )
    }

    /**
     * set some old holdover from lotus 123.  Who cares, its all over for Lotus.
     * RIP Lotus.
     * 
     * @param prefix - the lotus thing to set.
     * @see .setCellOptions
     */
    fun set123Prefix(prefix: Boolean) {
        field_3_cell_options =
            _123_prefix.setShortBoolean(field_3_cell_options, prefix)
    }

    // present in both but NULL except in cell records
    /**
     * for cell XF types this is the parent style (usually 0/normal).  For
     * style this should be NULL.
     * 
     * @param parent  index of parent XF
     * @see .NULL
     * 
     * @see .setCellOptions
     */
    fun setParentIndex(parent: Short) {
        field_3_cell_options =
            _parent_index.setShortValue(field_3_cell_options, parent)
    }

    // end bitfields in cell options
    /**
     * set the alignment options bitmask.  See corresponding bitsetter methods
     * that reference this one.
     * 
     * 
     * @param options     - the bitmask to set
     */
    fun setAlignmentOptions(options: Short) {
        field_4_alignment_options = options
    }

    /**
     * set the horizontal alignment of the cell.
     * 
     * 
     * @param align - how to align the cell (see constants)
     * @see .GENERAL
     * 
     * @see .LEFT
     * 
     * @see .CENTER
     * 
     * @see .RIGHT
     * 
     * @see .FILL
     * 
     * @see .JUSTIFY
     * 
     * @see .CENTER_SELECTION
     * 
     * @see .setAlignmentOptions
     */
    fun setAlignment(align: Short) {
        field_4_alignment_options =
            _alignment.setShortValue(field_4_alignment_options, align)
    }

    /**
     * set whether to wrap the text in the cell
     * 
     * 
     * @param wrapped - whether or not to wrap the cell text
     * @see .setAlignmentOptions
     */
    fun setWrapText(wrapped: Boolean) {
        field_4_alignment_options =
            _wrap_text.setShortBoolean(field_4_alignment_options, wrapped)
    }

    /**
     * set the vertical alignment of text in the cell
     * 
     * 
     * @param align     where to align the text
     * @see .VERTICAL_TOP
     * 
     * @see .VERTICAL_CENTER
     * 
     * @see .VERTICAL_BOTTOM
     * 
     * @see .VERTICAL_JUSTIFY
     * 
     * 
     * @see .setAlignmentOptions
     */
    fun setVerticalAlignment(align: Short) {
        field_4_alignment_options =
            _vertical_alignment.setShortValue(
                field_4_alignment_options,
                align
            )
    }

    /**
     * Dunno.  Docs just say this is for far east versions..  (I'm guessing it
     * justifies for right-to-left read languages)
     * 
     * 
     * @param justify
     * @see .setAlignmentOptions
     */
    fun setJustifyLast(justify: Short) {   // for far east languages supported only for format always 0 for US
        field_4_alignment_options =
            _justify_last.setShortValue(field_4_alignment_options, justify)
    }

    /**
     * set the degree of rotation.  (I've not actually seen this used anywhere)
     * 
     * 
     * @param rotation the degree of rotation
     * @see .setAlignmentOptions
     */
    fun setRotation(rotation: Short) {
        field_4_alignment_options =
            _rotation.setShortValue(field_4_alignment_options, rotation)
    }

    /**
     * set the indent options bitmask  (see corresponding bitmask setters that reference
     * this field)
     * 
     * 
     * @param options bitmask to set.
     */
    fun setIndentionOptions(options: Short) {
        field_5_indention_options = options
    }

    // set bitfields for indention options
    /**
     * set indention (not sure of the units, think its spaces)
     * 
     * @param indent - how far to indent the cell
     * @see .setIndentionOptions
     */
    fun setIndent(indent: Short) {
        field_5_indention_options =
            _indent.setShortValue(field_5_indention_options, indent)
    }

    /**
     * set whether to shrink the text to fit
     * 
     * 
     * @param shrink - shrink to fit or not
     * @see .setIndentionOptions
     */
    fun setShrinkToFit(shrink: Boolean) {
        field_5_indention_options =
            _shrink_to_fit.setShortBoolean(field_5_indention_options, shrink)
    }

    /**
     * set whether to merge cells
     * 
     * 
     * @param merge - merge cells or not
     * @see .setIndentionOptions
     */
    fun setMergeCells(merge: Boolean) {
        field_5_indention_options =
            _merge_cells.setShortBoolean(field_5_indention_options, merge)
    }

    /**
     * set the reading order for far east versions (0 - Context, 1 - Left to right,
     * 2 - right to left) - We could use some help with support for the far east.
     * 
     * @param order - the reading order (0,1,2)
     * @see .setIndentionOptions
     */
    fun setReadingOrder(order: Short) {   // only for far east  always 0 in US
        field_5_indention_options =
            _reading_order.setShortValue(field_5_indention_options, order)
    }

    /**
     * set whether or not to use the format in this XF instead of the parent XF.
     * 
     * 
     * @param parent - true if this XF has a different format value than its parent,
     * false otherwise.
     * @see .setIndentionOptions
     */
    fun setIndentNotParentFormat(parent: Boolean) {
        field_5_indention_options =
            _indent_not_parent_format
                .setShortBoolean(field_5_indention_options, parent)
    }

    /**
     * set whether or not to use the font in this XF instead of the parent XF.
     * 
     * 
     * @param font   - true if this XF has a different font value than its parent,
     * false otherwise.
     * @see .setIndentionOptions
     */
    fun setIndentNotParentFont(font: Boolean) {
        field_5_indention_options =
            _indent_not_parent_font.setShortBoolean(
                field_5_indention_options,
                font
            )
    }

    /**
     * set whether or not to use the alignment in this XF instead of the parent XF.
     * 
     * 
     * @param alignment true if this XF has a different alignment value than its parent,
     * false otherwise.
     * @see .setIndentionOptions
     */
    fun setIndentNotParentAlignment(alignment: Boolean) {
        field_5_indention_options =
            _indent_not_parent_alignment
                .setShortBoolean(field_5_indention_options, alignment)
    }

    /**
     * set whether or not to use the border in this XF instead of the parent XF.
     * 
     * 
     * @param border - true if this XF has a different border value than its parent,
     * false otherwise.
     * @see .setIndentionOptions
     */
    fun setIndentNotParentBorder(border: Boolean) {
        field_5_indention_options =
            _indent_not_parent_border
                .setShortBoolean(field_5_indention_options, border)
    }

    /**
     * 
     * Sets whether or not to use the pattern in this XF instead of the
     * parent XF (foreground/background).
     * 
     * @param pattern `true` if this XF has a different pattern
     * value than its parent, false otherwise.
     * @see .setIndentionOptions
     */
    fun setIndentNotParentPattern(pattern: Boolean) {
        field_5_indention_options =
            _indent_not_parent_pattern
                .setShortBoolean(field_5_indention_options, pattern)
    }

    /**
     * set whether or not to use the locking/hidden in this XF instead of the parent XF.
     * 
     * 
     * @param options true if this XF has a different locking or hidden value than its parent,
     * false otherwise.
     * @see .setIndentionOptions
     */
    fun setIndentNotParentCellOptions(options: Boolean) {
        field_5_indention_options =
            _indent_not_parent_cell_options
                .setShortBoolean(field_5_indention_options, options)
    }

    // end indention options bitmask sets
    /**
     * set the border options bitmask (see the corresponding bitsetter methods
     * that reference back to this one)
     * 
     * @param options - the bit mask to set
     */
    fun setBorderOptions(options: Short) {
        field_6_border_options = options
    }

    // border options bitfields
    /**
     * set the borderline style for the left border
     * 
     * 
     * @param border - type of border for the left side of the cell
     * @see .NONE
     * 
     * @see .THIN
     * 
     * @see .MEDIUM
     * 
     * @see .DASHED
     * 
     * @see .DOTTED
     * 
     * @see .THICK
     * 
     * @see .DOUBLE
     * 
     * @see .HAIR
     * 
     * @see .MEDIUM_DASHED
     * 
     * @see .DASH_DOT
     * 
     * @see .MEDIUM_DASH_DOT
     * 
     * @see .DASH_DOT_DOT
     * 
     * @see .MEDIUM_DASH_DOT_DOT
     * 
     * @see .SLANTED_DASH_DOT
     * 
     * @see .setBorderOptions
     */
    fun setBorderLeft(border: Short) {
        field_6_border_options =
            _border_left.setShortValue(field_6_border_options, border)
    }

    /**
     * set the border line style for the right border
     * 
     * 
     * @param border - type of border for the right side of the cell
     * @see .NONE
     * 
     * @see .THIN
     * 
     * @see .MEDIUM
     * 
     * @see .DASHED
     * 
     * @see .DOTTED
     * 
     * @see .THICK
     * 
     * @see .DOUBLE
     * 
     * @see .HAIR
     * 
     * @see .MEDIUM_DASHED
     * 
     * @see .DASH_DOT
     * 
     * @see .MEDIUM_DASH_DOT
     * 
     * @see .DASH_DOT_DOT
     * 
     * @see .MEDIUM_DASH_DOT_DOT
     * 
     * @see .SLANTED_DASH_DOT
     * 
     * @see .setBorderOptions
     */
    fun setBorderRight(border: Short) {
        field_6_border_options =
            _border_right.setShortValue(field_6_border_options, border)
    }

    /**
     * set the border line style for the top border
     * 
     * 
     * @param border - type of border for the top of the cell
     * @see .NONE
     * 
     * @see .THIN
     * 
     * @see .MEDIUM
     * 
     * @see .DASHED
     * 
     * @see .DOTTED
     * 
     * @see .THICK
     * 
     * @see .DOUBLE
     * 
     * @see .HAIR
     * 
     * @see .MEDIUM_DASHED
     * 
     * @see .DASH_DOT
     * 
     * @see .MEDIUM_DASH_DOT
     * 
     * @see .DASH_DOT_DOT
     * 
     * @see .MEDIUM_DASH_DOT_DOT
     * 
     * @see .SLANTED_DASH_DOT
     * 
     * @see .setBorderOptions
     */
    fun setBorderTop(border: Short) {
        field_6_border_options =
            _border_top.setShortValue(field_6_border_options, border)
    }

    /**
     * set the border line style for the bottom border
     * 
     * 
     * @param border - type of border for the bottom of the cell
     * @see .NONE
     * 
     * @see .THIN
     * 
     * @see .MEDIUM
     * 
     * @see .DASHED
     * 
     * @see .DOTTED
     * 
     * @see .THICK
     * 
     * @see .DOUBLE
     * 
     * @see .HAIR
     * 
     * @see .MEDIUM_DASHED
     * 
     * @see .DASH_DOT
     * 
     * @see .MEDIUM_DASH_DOT
     * 
     * @see .DASH_DOT_DOT
     * 
     * @see .MEDIUM_DASH_DOT_DOT
     * 
     * @see .SLANTED_DASH_DOT
     * 
     * @see .setBorderOptions
     */
    fun setBorderBottom(border: Short) {
        field_6_border_options =
            _border_bottom.setShortValue(field_6_border_options, border)
    }

    // end border option bitfields
    /**
     * set the palette options bitmask (see the individual bitsetter methods that
     * reference this one)
     * 
     * 
     * @param options - the bitmask to set
     */
    fun setPaletteOptions(options: Short) {
        field_7_palette_options = options
    }

    // bitfields for palette options
    /**
     * set the palette index for the left border color
     * 
     * 
     * @param border - palette index
     * @see .setPaletteOptions
     */
    fun setLeftBorderPaletteIdx(border: Short) {
        field_7_palette_options =
            _left_border_palette_idx.setShortValue(
                field_7_palette_options,
                border
            )
    }

    /**
     * set the palette index for the right border color
     * 
     * 
     * @param border - palette index
     * @see .setPaletteOptions
     */
    fun setRightBorderPaletteIdx(border: Short) {
        field_7_palette_options =
            _right_border_palette_idx.setShortValue(
                field_7_palette_options,
                border
            )
    }

    // i've no idea.. possible values are 1 for down, 2 for up and 3 for both...0 for none..
    // maybe a diagnal line?
    /**
     * Not sure what this is for (maybe fill lines?) 1 = down, 2 = up, 3 = both, 0 for none..
     * 
     * 
     * @param diag - set whatever it is that this is.
     * @see .setPaletteOptions
     */
    fun setDiag(diag: Short) {
        field_7_palette_options = _diag.setShortValue(
            field_7_palette_options,
            diag
        )
    }

    // end of palette options
    /**
     * set the additional palette options bitmask (see individual bitsetter methods
     * that reference this method)
     * 
     * 
     * @param options - bitmask to set
     */
    fun setAdtlPaletteOptions(options: Short) {
        field_8_adtl_palette_options = options.toInt()
    }

    // bitfields for additional palette options
    /**
     * set the palette index for the top border
     * 
     * 
     * @param border - palette index
     * @see .setAdtlPaletteOptions
     */
    fun setTopBorderPaletteIdx(border: Short) {
        field_8_adtl_palette_options =
            _top_border_palette_idx.setValue(
                field_8_adtl_palette_options,
                border.toInt()
            )
    }

    /**
     * set the palette index for the bottom border
     * 
     * 
     * @param border - palette index
     * @see .setAdtlPaletteOptions
     */
    fun setBottomBorderPaletteIdx(border: Short) {
        field_8_adtl_palette_options =
            _bottom_border_palette_idx.setValue(
                field_8_adtl_palette_options,
                border.toInt()
            )
    }

    /**
     * set for diagonal borders?  No idea (its a palette color for the other function
     * we didn't know what was?)
     * 
     * 
     * @param diag - the palette index?
     * @see .setAdtlPaletteOptions
     */
    fun setAdtlDiag(diag: Short) {
        field_8_adtl_palette_options =
            _adtl_diag.setValue(field_8_adtl_palette_options, diag.toInt())
    }

    /**
     * set the diagonal border line style?  Who the heck ever heard of a diagonal border?
     * 
     * 
     * @param diag - the line style
     * @see .NONE
     * 
     * @see .THIN
     * 
     * @see .MEDIUM
     * 
     * @see .DASHED
     * 
     * @see .DOTTED
     * 
     * @see .THICK
     * 
     * @see .DOUBLE
     * 
     * @see .HAIR
     * 
     * @see .MEDIUM_DASHED
     * 
     * @see .DASH_DOT
     * 
     * @see .MEDIUM_DASH_DOT
     * 
     * @see .DASH_DOT_DOT
     * 
     * @see .MEDIUM_DASH_DOT_DOT
     * 
     * @see .SLANTED_DASH_DOT
     * 
     * @see .setAdtlPaletteOptions
     */
    fun setAdtlDiagLineStyle(diag: Short) {
        field_8_adtl_palette_options =
            _adtl_diag_line_style.setValue(
                field_8_adtl_palette_options,
                diag.toInt()
            )
    }

    /**
     * set the fill pattern
     * 
     * @see .NO_FILL
     * 
     * @see .SOLID_FILL
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
     * @param fill - fill pattern??
     * @see .setAdtlPaletteOptions
     */
    fun setAdtlFillPattern(fill: Short) {
        field_8_adtl_palette_options =
            _adtl_fill_pattern.setValue(field_8_adtl_palette_options, fill.toInt())
    }

    // end bitfields for additional palette options
    /**
     * set the fill palette options bitmask (see
     * 
     * 
     * @param options
     */
    fun setFillPaletteOptions(options: Short) {
        field_9_fill_palette_options = options
    }

    /**
     * set the foreground palette color index
     * 
     * 
     * @param color - palette index
     * @see .setFillPaletteOptions
     */
    fun setFillForeground(color: Short) {
        field_9_fill_palette_options =
            _fill_foreground.setShortValue(
                field_9_fill_palette_options,
                color
            )
    }

    /**
     * set the background palette color index
     * 
     * 
     * @param color - palette index
     * @see .setFillPaletteOptions
     */
    fun setFillBackground(color: Short) {
        field_9_fill_palette_options =
            _fill_background.setShortValue(
                field_9_fill_palette_options,
                color
            )
    }

    /**
     * get the index to the FONT record (which font to use 0 based)
     * 
     * 
     * @return index to the font
     * @see FontRecord
     */
    fun getFontIndex(): Short {
        return field_1_font_index
    }

    /**
     * get the index to the Format record (which FORMAT to use 0-based)
     * 
     * 
     * @return index to the format record
     * @see FormatRecord
     */
    fun getFormatIndex(): Short {
        return field_2_format_index
    }

    /**
     * gets the options bitmask - you can also use corresponding option bit getters
     * (see other methods that reference this one)
     * 
     * 
     * @return options bitmask
     */
    fun getCellOptions(): Short {
        return field_3_cell_options
    }

    // These are the bit fields in cell options
    /**
     * get whether the cell is locked or not
     * 
     * 
     * @return locked - if the cell is locked
     * @see .getCellOptions
     */
    fun isLocked(): Boolean {
        return _locked.isSet(field_3_cell_options.toInt())
    }

    /**
     * get whether the cell is hidden or not
     * 
     * 
     * @return hidden - if the cell is hidden
     * @see .getCellOptions
     */
    fun isHidden(): Boolean {
        return _hidden.isSet(field_3_cell_options.toInt())
    }

    /**
     * get whether the cell is a cell or style XFRecord
     * 
     * 
     * @return type - cell or style (0/1)
     * @see .XF_STYLE
     * 
     * @see .XF_CELL
     * 
     * @see .getCellOptions
     */
    fun getXFType(): Short {
        return _xf_type.getShortValue(field_3_cell_options)
    }

    /**
     * get some old holdover from lotus 123.  Who cares, its all over for Lotus.
     * RIP Lotus.
     * 
     * @return prefix - the lotus thing
     * @see .getCellOptions
     */
    fun get123Prefix(): Boolean {
        return _123_prefix.isSet(field_3_cell_options.toInt())
    }

    /**
     * for cell XF types this is the parent style (usually 0/normal).  For
     * style this should be NULL.
     * 
     * @return index of parent XF
     * @see .NULL
     * 
     * @see .getCellOptions
     */
    fun getParentIndex(): Short {
        return _parent_index.getShortValue(field_3_cell_options)
    }

    // end bitfields in cell options
    /**
     * get the alignment options bitmask.  See corresponding bitgetter methods
     * that reference this one.
     * 
     * 
     * @return options     - the bitmask
     */
    fun getAlignmentOptions(): Short {
        return field_4_alignment_options
    }

    // bitfields in alignment options
    /**
     * get the horizontal alignment of the cell.
     * 
     * 
     * @return align - how to align the cell (see constants)
     * @see .GENERAL
     * 
     * @see .LEFT
     * 
     * @see .CENTER
     * 
     * @see .RIGHT
     * 
     * @see .FILL
     * 
     * @see .JUSTIFY
     * 
     * @see .CENTER_SELECTION
     * 
     * @see .getAlignmentOptions
     */
    fun getAlignment(): Short {
        return _alignment.getShortValue(field_4_alignment_options)
    }

    /**
     * get whether to wrap the text in the cell
     * 
     * 
     * @return wrapped - whether or not to wrap the cell text
     * @see .getAlignmentOptions
     */
    fun getWrapText(): Boolean {
        return _wrap_text.isSet(field_4_alignment_options.toInt())
    }

    /**
     * get the vertical alignment of text in the cell
     * 
     * 
     * @return where to align the text
     * @see .VERTICAL_TOP
     * 
     * @see .VERTICAL_CENTER
     * 
     * @see .VERTICAL_BOTTOM
     * 
     * @see .VERTICAL_JUSTIFY
     * 
     * 
     * @see .getAlignmentOptions
     */
    fun getVerticalAlignment(): Short {
        return _vertical_alignment.getShortValue(field_4_alignment_options)
    }

    /**
     * Dunno.  Docs just say this is for far east versions..  (I'm guessing it
     * justifies for right-to-left read languages)
     * 
     * 
     * @return justify
     * @see .getAlignmentOptions
     */
    fun getJustifyLast(): Short {   // for far east languages supported only for format always 0 for US
        return _justify_last.getShortValue(field_4_alignment_options)
    }

    /**
     * get the degree of rotation.  (I've not actually seen this used anywhere)
     * 
     * 
     * @return rotation - the degree of rotation
     * @see .getAlignmentOptions
     */
    fun getRotation(): Short {
        return _rotation.getShortValue(field_4_alignment_options)
    }

    // end alignment options bitfields
    /**
     * get the indent options bitmask  (see corresponding bit getters that reference
     * this field)
     * 
     * 
     * @return options bitmask
     */
    fun getIndentionOptions(): Short {
        return field_5_indention_options
    }

    // bitfields for indention options
    /**
     * get indention (not sure of the units, think its spaces)
     * 
     * @return indent - how far to indent the cell
     * @see .getIndentionOptions
     */
    fun getIndent(): Short {
        return _indent.getShortValue(field_5_indention_options)
    }

    /**
     * get whether to shrink the text to fit
     * 
     * 
     * @return shrink - shrink to fit or not
     * @see .getIndentionOptions
     */
    fun getShrinkToFit(): Boolean {
        return _shrink_to_fit.isSet(field_5_indention_options.toInt())
    }

    /**
     * get whether to merge cells
     * 
     * 
     * @return merge - merge cells or not
     * @see .getIndentionOptions
     */
    fun getMergeCells(): Boolean {
        return _merge_cells.isSet(field_5_indention_options.toInt())
    }

    /**
     * get the reading order for far east versions (0 - Context, 1 - Left to right,
     * 2 - right to left) - We could use some help with support for the far east.
     * 
     * @return order - the reading order (0,1,2)
     * @see .getIndentionOptions
     */
    fun getReadingOrder(): Short {   // only for far east  always 0 in US
        return _reading_order.getShortValue(field_5_indention_options)
    }

    /**
     * get whether or not to use the format in this XF instead of the parent XF.
     * 
     * 
     * @return parent - true if this XF has a different format value than its parent,
     * false otherwise.
     * @see .getIndentionOptions
     */
    fun isIndentNotParentFormat(): Boolean {
        return _indent_not_parent_format.isSet(field_5_indention_options.toInt())
    }

    /**
     * get whether or not to use the font in this XF instead of the parent XF.
     * 
     * 
     * @return font   - true if this XF has a different font value than its parent,
     * false otherwise.
     * @see .getIndentionOptions
     */
    fun isIndentNotParentFont(): Boolean {
        return _indent_not_parent_font.isSet(field_5_indention_options.toInt())
    }

    /**
     * get whether or not to use the alignment in this XF instead of the parent XF.
     * 
     * 
     * @return alignment true if this XF has a different alignment value than its parent,
     * false otherwise.
     * @see .getIndentionOptions
     */
    fun isIndentNotParentAlignment(): Boolean {
        return _indent_not_parent_alignment.isSet(field_5_indention_options.toInt())
    }

    /**
     * get whether or not to use the border in this XF instead of the parent XF.
     * 
     * 
     * @return border - true if this XF has a different border value than its parent,
     * false otherwise.
     * @see .getIndentionOptions
     */
    fun isIndentNotParentBorder(): Boolean {
        return _indent_not_parent_border.isSet(field_5_indention_options.toInt())
    }

    /**
     * get whether or not to use the pattern in this XF instead of the parent XF.
     * (foregrount/background)
     * 
     * @return pattern- true if this XF has a different pattern value than its parent,
     * false otherwise.
     * @see .getIndentionOptions
     */
    fun isIndentNotParentPattern(): Boolean {
        return _indent_not_parent_pattern.isSet(field_5_indention_options.toInt())
    }

    /**
     * get whether or not to use the locking/hidden in this XF instead of the parent XF.
     * 
     * 
     * @return options- true if this XF has a different locking or hidden value than its parent,
     * false otherwise.
     * @see .getIndentionOptions
     */
    fun isIndentNotParentCellOptions(): Boolean {
        return _indent_not_parent_cell_options
            .isSet(field_5_indention_options.toInt())
    }

    // end of bitfields for indention options
    // border options
    /**
     * get the border options bitmask (see the corresponding bit getter methods
     * that reference back to this one)
     * 
     * @return options - the bit mask to set
     */
    fun getBorderOptions(): Short {
        return field_6_border_options
    }

    // bitfields for border options
    /**
     * get the borderline style for the left border
     * 
     * 
     * @return border - type of border for the left side of the cell
     * @see .NONE
     * 
     * @see .THIN
     * 
     * @see .MEDIUM
     * 
     * @see .DASHED
     * 
     * @see .DOTTED
     * 
     * @see .THICK
     * 
     * @see .DOUBLE
     * 
     * @see .HAIR
     * 
     * @see .MEDIUM_DASHED
     * 
     * @see .DASH_DOT
     * 
     * @see .MEDIUM_DASH_DOT
     * 
     * @see .DASH_DOT_DOT
     * 
     * @see .MEDIUM_DASH_DOT_DOT
     * 
     * @see .SLANTED_DASH_DOT
     * 
     * @see .getBorderOptions
     */
    fun getBorderLeft(): Short {
        return _border_left.getShortValue(field_6_border_options)
    }

    /**
     * get the borderline style for the right border
     * 
     * 
     * @return  border - type of border for the right side of the cell
     * @see .NONE
     * 
     * @see .THIN
     * 
     * @see .MEDIUM
     * 
     * @see .DASHED
     * 
     * @see .DOTTED
     * 
     * @see .THICK
     * 
     * @see .DOUBLE
     * 
     * @see .HAIR
     * 
     * @see .MEDIUM_DASHED
     * 
     * @see .DASH_DOT
     * 
     * @see .MEDIUM_DASH_DOT
     * 
     * @see .DASH_DOT_DOT
     * 
     * @see .MEDIUM_DASH_DOT_DOT
     * 
     * @see .SLANTED_DASH_DOT
     * 
     * @see .getBorderOptions
     */
    fun getBorderRight(): Short {
        return _border_right.getShortValue(field_6_border_options)
    }

    /**
     * get the borderline style for the top border
     * 
     * 
     * @return border - type of border for the top of the cell
     * @see .NONE
     * 
     * @see .THIN
     * 
     * @see .MEDIUM
     * 
     * @see .DASHED
     * 
     * @see .DOTTED
     * 
     * @see .THICK
     * 
     * @see .DOUBLE
     * 
     * @see .HAIR
     * 
     * @see .MEDIUM_DASHED
     * 
     * @see .DASH_DOT
     * 
     * @see .MEDIUM_DASH_DOT
     * 
     * @see .DASH_DOT_DOT
     * 
     * @see .MEDIUM_DASH_DOT_DOT
     * 
     * @see .SLANTED_DASH_DOT
     * 
     * @see .getBorderOptions
     */
    fun getBorderTop(): Short {
        return _border_top.getShortValue(field_6_border_options)
    }

    /**
     * get the borderline style for the bottom border
     * 
     * 
     * @return border - type of border for the bottom of the cell
     * @see .NONE
     * 
     * @see .THIN
     * 
     * @see .MEDIUM
     * 
     * @see .DASHED
     * 
     * @see .DOTTED
     * 
     * @see .THICK
     * 
     * @see .DOUBLE
     * 
     * @see .HAIR
     * 
     * @see .MEDIUM_DASHED
     * 
     * @see .DASH_DOT
     * 
     * @see .MEDIUM_DASH_DOT
     * 
     * @see .DASH_DOT_DOT
     * 
     * @see .MEDIUM_DASH_DOT_DOT
     * 
     * @see .SLANTED_DASH_DOT
     * 
     * @see .getBorderOptions
     */
    fun getBorderBottom(): Short {
        return _border_bottom.getShortValue(field_6_border_options)
    }

    // record types -- palette options
    /**
     * get the palette options bitmask (see the individual bit getter methods that
     * reference this one)
     * 
     * 
     * @return options - the bitmask
     */
    fun getPaletteOptions(): Short {
        return field_7_palette_options
    }

    // bitfields for palette options
    /**
     * get the palette index for the left border color
     * 
     * 
     * @return border - palette index
     * @see .getPaletteOptions
     */
    fun getLeftBorderPaletteIdx(): Short {
        return _left_border_palette_idx
            .getShortValue(field_7_palette_options)
    }

    /**
     * get the palette index for the right border color
     * 
     * 
     * @return border - palette index
     * @see .getPaletteOptions
     */
    fun getRightBorderPaletteIdx(): Short {
        return _right_border_palette_idx
            .getShortValue(field_7_palette_options)
    }

    // i've no idea.. possible values are 1 for down, 2 for up and 3 for both...0 for none..
    // maybe a diagnal line?
    /**
     * Not sure what this is for (maybe fill lines?) 1 = down, 2 = up, 3 = both, 0 for none..
     * 
     * 
     * @return diag - whatever it is that this is.
     * @see .getPaletteOptions
     */
    fun getDiag(): Short {
        return _diag.getShortValue(field_7_palette_options)
    }

    // end of style palette options
    // additional palette options
    /**
     * get the additional palette options bitmask (see individual bit getter methods
     * that reference this method)
     * 
     * 
     * @return options - bitmask to set
     */
    fun getAdtlPaletteOptions(): Int {
        return field_8_adtl_palette_options
    }

    // bitfields for additional palette options
    /**
     * get the palette index for the top border
     * 
     * 
     * @return border - palette index
     * @see .getAdtlPaletteOptions
     */
    fun getTopBorderPaletteIdx(): Short {
        return _top_border_palette_idx
            .getValue(field_8_adtl_palette_options).toShort()
    }

    /**
     * get the palette index for the bottom border
     * 
     * 
     * @return border - palette index
     * @see .getAdtlPaletteOptions
     */
    fun getBottomBorderPaletteIdx(): Short {
        return _bottom_border_palette_idx
            .getValue(field_8_adtl_palette_options).toShort()
    }

    /**
     * get for diagonal borders?  No idea (its a palette color for the other function
     * we didn't know what was?)
     * 
     * 
     * @return diag - the palette index?
     * @see .getAdtlPaletteOptions
     */
    fun getAdtlDiag(): Short {
        return _adtl_diag.getValue(field_8_adtl_palette_options).toShort()
    }

    /**
     * get the diagonal border line style?  Who the heck ever heard of a diagonal border?
     * 
     * 
     * @return diag - the line style
     * @see .NONE
     * 
     * @see .THIN
     * 
     * @see .MEDIUM
     * 
     * @see .DASHED
     * 
     * @see .DOTTED
     * 
     * @see .THICK
     * 
     * @see .DOUBLE
     * 
     * @see .HAIR
     * 
     * @see .MEDIUM_DASHED
     * 
     * @see .DASH_DOT
     * 
     * @see .MEDIUM_DASH_DOT
     * 
     * @see .DASH_DOT_DOT
     * 
     * @see .MEDIUM_DASH_DOT_DOT
     * 
     * @see .SLANTED_DASH_DOT
     * 
     * @see .getAdtlPaletteOptions
     */
    fun getAdtlDiagLineStyle(): Short {
        return _adtl_diag_line_style
            .getValue(field_8_adtl_palette_options).toShort()
    }

    /**
     * get the additional fill pattern
     * 
     * @see .NO_FILL
     * 
     * @see .SOLID_FILL
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
     * @return fill - fill pattern??
     * @see .getAdtlPaletteOptions
     */
    fun getAdtlFillPattern(): Short {
        return _adtl_fill_pattern
            .getValue(field_8_adtl_palette_options).toShort()
    }

    // end bitfields for additional palette options
    // fill palette options
    /**
     * get the fill palette options bitmask (see indivdual bit getters that
     * reference this method)
     * 
     * @return options
     */
    fun getFillPaletteOptions(): Short {
        return field_9_fill_palette_options
    }

    // bitfields for fill palette options
    /**
     * get the foreground palette color index
     * 
     * 
     * @return color - palette index
     * @see .getFillPaletteOptions
     */
    fun getFillForeground(): Short {
        return _fill_foreground.getShortValue(field_9_fill_palette_options)
    }

    /**
     * get the background palette color index
     * 
     * @return color palette index
     * @see .getFillPaletteOptions
     */
    fun getFillBackground(): Short {
        return _fill_background.getShortValue(field_9_fill_palette_options)
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[EXTENDEDFORMAT]\n")
        if (getXFType() == XF_STYLE) {
            buffer.append(" STYLE_RECORD_TYPE\n")
        } else if (getXFType() == XF_CELL) {
            buffer.append(" CELL_RECORD_TYPE\n")
        }
        buffer.append("    .fontindex       = ")
            .append(Integer.toHexString(getFontIndex().toInt())).append("\n")
        buffer.append("    .formatindex     = ")
            .append(Integer.toHexString(getFormatIndex().toInt())).append("\n")
        buffer.append("    .celloptions     = ")
            .append(Integer.toHexString(getCellOptions().toInt())).append("\n")
        buffer.append("          .islocked  = ").append(isLocked())
            .append("\n")
        buffer.append("          .ishidden  = ").append(isHidden())
            .append("\n")
        buffer.append("          .recordtype= ")
            .append(Integer.toHexString(getXFType().toInt())).append("\n")
        buffer.append("          .parentidx = ")
            .append(Integer.toHexString(getParentIndex().toInt())).append("\n")
        buffer.append("    .alignmentoptions= ")
            .append(Integer.toHexString(getAlignmentOptions().toInt())).append("\n")
        buffer.append("          .alignment = ").append(getAlignment().toInt())
            .append("\n")
        buffer.append("          .wraptext  = ").append(getWrapText())
            .append("\n")
        buffer.append("          .valignment= ")
            .append(Integer.toHexString(getVerticalAlignment().toInt())).append("\n")
        buffer.append("          .justlast  = ")
            .append(Integer.toHexString(getJustifyLast().toInt())).append("\n")
        buffer.append("          .rotation  = ")
            .append(Integer.toHexString(getRotation().toInt())).append("\n")
        buffer.append("    .indentionoptions= ")
            .append(Integer.toHexString(getIndentionOptions().toInt())).append("\n")
        buffer.append("          .indent    = ")
            .append(Integer.toHexString(getIndent().toInt())).append("\n")
        buffer.append("          .shrinktoft= ").append(getShrinkToFit())
            .append("\n")
        buffer.append("          .mergecells= ").append(getMergeCells())
            .append("\n")
        buffer.append("          .readngordr= ")
            .append(Integer.toHexString(getReadingOrder().toInt())).append("\n")
        buffer.append("          .formatflag= ")
            .append(isIndentNotParentFormat()).append("\n")
        buffer.append("          .fontflag  = ")
            .append(isIndentNotParentFont()).append("\n")
        buffer.append("          .prntalgnmt= ")
            .append(isIndentNotParentAlignment()).append("\n")
        buffer.append("          .borderflag= ")
            .append(isIndentNotParentBorder()).append("\n")
        buffer.append("          .paternflag= ")
            .append(isIndentNotParentPattern()).append("\n")
        buffer.append("          .celloption= ")
            .append(isIndentNotParentCellOptions()).append("\n")
        buffer.append("    .borderoptns     = ")
            .append(Integer.toHexString(getBorderOptions().toInt())).append("\n")
        buffer.append("          .lftln     = ")
            .append(Integer.toHexString(getBorderLeft().toInt())).append("\n")
        buffer.append("          .rgtln     = ")
            .append(Integer.toHexString(getBorderRight().toInt())).append("\n")
        buffer.append("          .topln     = ")
            .append(Integer.toHexString(getBorderTop().toInt())).append("\n")
        buffer.append("          .btmln     = ")
            .append(Integer.toHexString(getBorderBottom().toInt())).append("\n")
        buffer.append("    .paleteoptns     = ")
            .append(Integer.toHexString(getPaletteOptions().toInt())).append("\n")
        buffer.append("          .leftborder= ")
            .append(Integer.toHexString(getLeftBorderPaletteIdx().toInt()))
            .append("\n")
        buffer.append("          .rghtborder= ")
            .append(Integer.toHexString(getRightBorderPaletteIdx().toInt()))
            .append("\n")
        buffer.append("          .diag      = ")
            .append(Integer.toHexString(getDiag().toInt())).append("\n")
        buffer.append("    .paleteoptn2     = ")
            .append(Integer.toHexString(getAdtlPaletteOptions()))
            .append("\n")
        buffer.append("          .topborder = ")
            .append(Integer.toHexString(getTopBorderPaletteIdx().toInt()))
            .append("\n")
        buffer.append("          .botmborder= ")
            .append(Integer.toHexString(getBottomBorderPaletteIdx().toInt()))
            .append("\n")
        buffer.append("          .adtldiag  = ")
            .append(Integer.toHexString(getAdtlDiag().toInt())).append("\n")
        buffer.append("          .diaglnstyl= ")
            .append(Integer.toHexString(getAdtlDiagLineStyle().toInt())).append("\n")
        buffer.append("          .fillpattrn= ")
            .append(Integer.toHexString(getAdtlFillPattern().toInt())).append("\n")
        buffer.append("    .fillpaloptn     = ")
            .append(Integer.toHexString(getFillPaletteOptions().toInt()))
            .append("\n")
        buffer.append("          .foreground= ")
            .append(Integer.toHexString(getFillForeground().toInt())).append("\n")
        buffer.append("          .background= ")
            .append(Integer.toHexString(getFillBackground().toInt())).append("\n")
        buffer.append("[/EXTENDEDFORMAT]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(getFontIndex().toInt())
        out.writeShort(getFormatIndex().toInt())
        out.writeShort(getCellOptions().toInt())
        out.writeShort(getAlignmentOptions().toInt())
        out.writeShort(getIndentionOptions().toInt())
        out.writeShort(getBorderOptions().toInt())
        out.writeShort(getPaletteOptions().toInt())
        out.writeInt(getAdtlPaletteOptions())
        out.writeShort(getFillPaletteOptions().toInt())
    }

    override fun getDataSize(): Int {
        return 20
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    /**
     * Clones all the style information from another
     * ExtendedFormatRecord, onto this one. This
     * will then hold all the same style options.
     * 
     * If The source ExtendedFormatRecord comes from
     * a different Workbook, you will need to sort
     * out the font and format indicies yourself!
     */
    fun cloneStyleFrom(source: ExtendedFormatRecord) {
        field_1_font_index = source.field_1_font_index
        field_2_format_index = source.field_2_format_index
        field_3_cell_options = source.field_3_cell_options
        field_4_alignment_options = source.field_4_alignment_options
        field_5_indention_options = source.field_5_indention_options
        field_6_border_options = source.field_6_border_options
        field_7_palette_options = source.field_7_palette_options
        field_8_adtl_palette_options = source.field_8_adtl_palette_options
        field_9_fill_palette_options = source.field_9_fill_palette_options
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + field_1_font_index
        result = prime * result + field_2_format_index
        result = prime * result + field_3_cell_options
        result = prime * result + field_4_alignment_options
        result = prime * result + field_5_indention_options
        result = prime * result + field_6_border_options
        result = prime * result + field_7_palette_options
        result = prime * result + field_8_adtl_palette_options
        result = prime * result + field_9_fill_palette_options
        return result
    }

    /**
     * Will consider two different records with the same
     * contents as equals, as the various indexes
     * that matter are embedded in the records
     */
    override fun equals(obj: Any?): Boolean {
        if (this === obj) return true
        if (obj == null) return false
        if (obj is ExtendedFormatRecord) {
            val other = obj
            if (field_1_font_index != other.field_1_font_index) return false
            if (field_2_format_index != other.field_2_format_index) return false
            if (field_3_cell_options != other.field_3_cell_options) return false
            if (field_4_alignment_options != other.field_4_alignment_options) return false
            if (field_5_indention_options != other.field_5_indention_options) return false
            if (field_6_border_options != other.field_6_border_options) return false
            if (field_7_palette_options != other.field_7_palette_options) return false
            if (field_8_adtl_palette_options != other.field_8_adtl_palette_options) return false
            if (field_9_fill_palette_options != other.field_9_fill_palette_options) return false
            return true
        }
        return false
    }


    companion object {
        const val sid: Short = 0xE0

        // null constant
        val NULL: Short = 0xfff0.toShort()

        // xf type
        const val XF_STYLE: Short = 1
        const val XF_CELL: Short = 0

        // borders
        const val NONE: Short = 0x0
        const val THIN: Short = 0x1
        const val MEDIUM: Short = 0x2
        const val DASHED: Short = 0x3
        const val DOTTED: Short = 0x4
        const val THICK: Short = 0x5
        const val DOUBLE: Short = 0x6
        const val HAIR: Short = 0x7
        const val MEDIUM_DASHED: Short = 0x8
        const val DASH_DOT: Short = 0x9
        const val MEDIUM_DASH_DOT: Short = 0xA
        const val DASH_DOT_DOT: Short = 0xB
        const val MEDIUM_DASH_DOT_DOT: Short = 0xC
        const val SLANTED_DASH_DOT: Short = 0xD

        // alignment
        const val GENERAL: Short = 0x0
        const val LEFT: Short = 0x1
        const val CENTER: Short = 0x2
        const val RIGHT: Short = 0x3
        const val FILL: Short = 0x4
        const val JUSTIFY: Short = 0x5
        const val CENTER_SELECTION: Short = 0x6

        // vertical alignment
        const val VERTICAL_TOP: Short = 0x0
        const val VERTICAL_CENTER: Short = 0x1
        const val VERTICAL_BOTTOM: Short = 0x2
        const val VERTICAL_JUSTIFY: Short = 0x3

        // fill
        const val NO_FILL: Short = 0
        const val SOLID_FILL: Short = 1
        const val FINE_DOTS: Short = 2
        const val ALT_BARS: Short = 3
        const val SPARSE_DOTS: Short = 4
        const val THICK_HORZ_BANDS: Short = 5
        const val THICK_VERT_BANDS: Short = 6
        const val THICK_BACKWARD_DIAG: Short = 7
        const val THICK_FORWARD_DIAG: Short = 8
        const val BIG_SPOTS: Short = 9
        const val BRICKS: Short = 10
        const val THIN_HORZ_BANDS: Short = 11
        const val THIN_VERT_BANDS: Short = 12
        const val THIN_BACKWARD_DIAG: Short = 13
        const val THIN_FORWARD_DIAG: Short = 14
        const val SQUARES: Short = 15
        const val DIAMONDS: Short = 16

        // field_3_cell_options bit map
        private val _locked = getInstance(0x0001)
        private val _hidden = getInstance(0x0002)
        private val _xf_type = getInstance(0x0004)
        private val _123_prefix = getInstance(0x0008)
        private val _parent_index = getInstance(0xFFF0)

        // field_4_alignment_options bit map
        private val _alignment = getInstance(0x0007)
        private val _wrap_text = getInstance(0x0008)
        private val _vertical_alignment = getInstance(0x0070)
        private val _justify_last = getInstance(0x0080)
        private val _rotation = getInstance(0xFF00)

        // field_5_indention_options
        private val _indent = getInstance(0x000F)
        private val _shrink_to_fit = getInstance(0x0010)
        private val _merge_cells = getInstance(0x0020)
        private val _reading_order = getInstance(0x00C0)

        // apparently bits 8 and 9 are unused
        private val _indent_not_parent_format = getInstance(0x0400)
        private val _indent_not_parent_font = getInstance(0x0800)
        private val _indent_not_parent_alignment = getInstance(0x1000)
        private val _indent_not_parent_border = getInstance(0x2000)
        private val _indent_not_parent_pattern = getInstance(0x4000)
        private val _indent_not_parent_cell_options = getInstance(0x8000)

        // field_6_border_options bit map
        private val _border_left = getInstance(0x000F)
        private val _border_right = getInstance(0x00F0)
        private val _border_top = getInstance(0x0F00)
        private val _border_bottom = getInstance(0xF000)

        // all three of the following attributes are palette options
        // field_7_palette_options bit map
        private val _left_border_palette_idx = getInstance(0x007F)
        private val _right_border_palette_idx = getInstance(0x3F80)
        private val _diag = getInstance(0xC000)

        // field_8_adtl_palette_options bit map
        private val _top_border_palette_idx = getInstance(0x0000007F)
        private val _bottom_border_palette_idx = getInstance(0x00003F80)
        private val _adtl_diag = getInstance(0x001fc000)
        private val _adtl_diag_line_style = getInstance(0x01e00000)

        // apparently bit 25 is unused
        private val _adtl_fill_pattern = getInstance(-0x4000000)

        // field_9_fill_palette_options bit map
        private val _fill_foreground = getInstance(0x007F)
        private val _fill_background = getInstance(0x3f80)
    }
}
