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

import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.ss.usermodel.Picture
import com.wxiwei.office.fc.ss.usermodel.Workbook
import com.wxiwei.office.fc.ss.util.ImageUtils
import com.wxiwei.office.java.awt.Dimension
import com.wxiwei.office.ss.model.XLSModel.AWorkbook
import java.io.ByteArrayInputStream

/**
 * Represents a escher picture.  Eg. A GIF, JPEG etc...
 * 
 * @author Glen Stampoultzis
 * @author Yegor Kozlov (yegor at apache.org)
 */
class HSSFPicture : HSSFSimpleShape, Picture {
    var pictureIndex: Int = 0

    /**
     * 
     * @return
     */
    var escherOptRecord: EscherOptRecord? = null
        private set

    /**
     * Constructs a picture object.
     */
    constructor(
        escherContainer: EscherContainerRecord?,
        parent: HSSFShape?,
        anchor: HSSFAnchor?
    ) : super(escherContainer, parent, anchor) {
        shapeType = OBJECT_TYPE_PICTURE.toInt()
    }

    /**
     * Constructs a picture object.
     */
    constructor(
        workbook: AWorkbook?,
        escherContainer: EscherContainerRecord?,
        parent: HSSFShape?,
        anchor: HSSFAnchor?,
        opt: EscherOptRecord?
    ) : super(escherContainer, parent, anchor) {
        shapeType = OBJECT_TYPE_PICTURE.toInt()
        this.escherOptRecord = opt

        processLineWidth()
        processLine(escherContainer, workbook)
        processSimpleBackground(escherContainer, workbook)

        processRotationAndFlip(escherContainer)
    }

    /**
     * Resize the image
     * 
     * 
     * Please note, that this method works correctly only for workbooks
     * with default font size (Arial 10pt for .xls).
     * If the default font is changed the resized image can be streched vertically or horizontally.
     * 
     * 
     * @param scale the amount by which image dimensions are multiplied relative to the original size.
     * `resize(1.0)` sets the original size, `resize(0.5)` resize to 50% of the original,
     * `resize(2.0)` resizes to 200% of the original.
     */
    override fun resize(scale: Double) {
        val anchor = getAnchor() as HSSFClientAnchor
        anchor.anchorType = 2

        val pref = getPreferredSize(scale)

        val row2 = anchor.row1 + (pref.row2 - pref.row1)
        val col2 = anchor.col1 + (pref.col2 - pref.col1)

        anchor.setCol2(col2.toShort())
        anchor.dx1 = 0
        anchor.dx2 = pref.dx2

        anchor.row2 = row2
        anchor.dy1 = 0
        anchor.dy2 = pref.dy2
    }

    /**
     * Reset the image to the original size.
     * 
     * 
     * 
     * Please note, that this method works correctly only for workbooks
     * with default font size (Arial 10pt for .xls).
     * If the default font is changed the resized image can be streched vertically or horizontally.
     * 
     */
    override fun resize() {
        resize(1.0)
    }

    /**
     * Calculate the preferred size for this picture.
     * 
     * @return HSSFClientAnchor with the preferred size for this image
     * @since POI 3.0.2
     */
    override fun getPreferredSize(): HSSFClientAnchor {
        return getPreferredSize(1.0)
    }

    /**
     * Calculate the preferred size for this picture.
     * 
     * @param scale the amount by which image dimensions are multiplied relative to the original size.
     * @return HSSFClientAnchor with the preferred size for this image
     * @since POI 3.0.2
     */
    fun getPreferredSize(scale: Double): HSSFClientAnchor {
        val anchor = getAnchor() as HSSFClientAnchor

        val size = this.imageDimension
        val scaledWidth = size!!.getWidth() * scale
        val scaledHeight = size.getHeight() * scale

        var w = 0f

        //space in the leftmost cell
        w += getColumnWidthInPixels(anchor.col1.toInt()) * (1 - anchor.dx1.toFloat() / 1024)
        var col2 = (anchor.col1 + 1).toShort()
        var dx2 = 0

        while (w < scaledWidth) {
            w += getColumnWidthInPixels((col2++).toInt())
        }

        if (w > scaledWidth) {
            //calculate dx2, offset in the rightmost cell
            col2--
            val cw = getColumnWidthInPixels(col2.toInt()).toDouble()
            val delta = w - scaledWidth
            dx2 = ((cw - delta) / cw * 1024).toInt()
        }
        anchor.setCol2(col2)
        anchor.dx2 = dx2

        var h = 0f
        h += (1 - anchor.dy1.toFloat() / 256) * getRowHeightInPixels(anchor.row1)
        var row2 = anchor.row1 + 1
        var dy2 = 0

        while (h < scaledHeight) {
            h += getRowHeightInPixels(row2++)
        }
        if (h > scaledHeight) {
            row2--
            val ch = getRowHeightInPixels(row2).toDouble()
            val delta = h - scaledHeight
            dy2 = ((ch - delta) / ch * 256).toInt()
        }
        anchor.row2 = row2
        anchor.dy2 = dy2

        return anchor
    }

    private fun getColumnWidthInPixels(column: Int): Float {
        //        int cw = _patriarch!!._sheet!!.getColumnWidth(column);
//        float px = getPixelWidth(column);
//
//        return cw / px;

        if (checkPatriarch()) {
            return _patriarch!!._sheet!!.getColumnPixelWidth(column)
        }
        return 0f
    }

    private fun getRowHeightInPixels(i: Int): Float {
        //        HSSFRow row = _patriarch!!._sheet!!.getRow(i);
//        float height;
//        if (row != null)
//            height = row.getHeight();
//        else
//            height = _patriarch!!._sheet!!.getDefaultRowHeight();
//
//        return height / PX_ROW;

        if (checkPatriarch()) {
            val row = _patriarch!!._sheet!!.getRow(i)
            if (row != null) {
                return row.getRowPixelHeight()
            }
        }

        run {
            return 18f
        }
    }

    private fun getPixelWidth(column: Int): Float {
        //        int def = _patriarch!!._sheet!!.getDefaultColumnWidth() * 256;
//        int cw = _patriarch!!._sheet!!.getColumnWidth(column);
//
//        return cw == def ? PX_DEFAULT : PX_MODIFIED;

        return PX_DEFAULT
    }

    val imageDimension: Dimension?
        /**
         * Return the dimension of this image
         * 
         * @return image dimension
         */
        get() {
            if (checkPatriarch()) {
                val bse = _patriarch!!._sheet!!.getAWorkbook()!!.getInternalWorkbook()!!
                    .getBSERecord(this.pictureIndex)
                val data = bse!!.blipRecord!!.picturedata
                val type = bse.blipTypeWin32.toInt()
                return ImageUtils.getImageDimension(
                    ByteArrayInputStream(
                        data
                    ), type
                )
            }
            return null
        }

    /**
     * Return picture data for this shape
     * 
     * @return picture data for this shape
     */
    override fun getPictureData(): HSSFPictureData? {
        if (checkPatriarch() && this.pictureIndex > 0) {
            val iwb = _patriarch!!._sheet!!.getAWorkbook()!!.getInternalWorkbook()
            val blipRecord = iwb!!.getBSERecord(this.pictureIndex)!!.blipRecord
            return HSSFPictureData(blipRecord!!)
        }

        return null
    }

    companion object {
        val PICTURE_TYPE_EMF: Int = Workbook.PICTURE_TYPE_EMF // Windows Enhanced Metafile
        val PICTURE_TYPE_WMF: Int = Workbook.PICTURE_TYPE_WMF // Windows Metafile
        val PICTURE_TYPE_PICT: Int = Workbook.PICTURE_TYPE_PICT // Macintosh PICT
        val PICTURE_TYPE_JPEG: Int = Workbook.PICTURE_TYPE_JPEG // JFIF
        val PICTURE_TYPE_PNG: Int = Workbook.PICTURE_TYPE_PNG // PNG
        val PICTURE_TYPE_DIB: Int = Workbook.PICTURE_TYPE_DIB // Windows DIB

        /**
         * width of 1px in columns with default width in units of 1/256 of a character width
         */
        private const val PX_DEFAULT = 32.00f

        /**
         * width of 1px in columns with overridden width in units of 1/256 of a character width
         */
        private const val PX_MODIFIED = 36.56f

        /**
         * Height of 1px of a row
         */
        private const val PX_ROW = 15
    }
}
