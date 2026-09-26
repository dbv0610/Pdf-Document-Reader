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

package com.wxiwei.office.ss.model

import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.ss.model.baseModel.Sheet
import com.wxiwei.office.ss.model.interfacePart.IClientAnchor

/**
 * A client anchor is attached to an excel worksheet.  It anchors against a
 * top-left and buttom-right cell.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
class ClientAnchor : Anchor, IClientAnchor {
    //
    private var col1: Short = 0
    //
    private var row1: Int = 0
    //
    private var col2: Short = 0
    //
    private var row2: Int = 0
    //
    private var anchorType: Int = 0

    /**
     * Creates a new client anchor and defaults all the anchor positions to 0.
     */
    constructor()

    /**
     * Creates a new client anchor and sets the top-left and bottom-right
     * coordinates of the anchor.
     *
     * @param dx1   the x coordinate within the first cell.
     * @param dy1   the y coordinate within the first cell.
     * @param dx2   the x coordinate within the second cell.
     * @param dy2   the y coordinate within the second cell.
     * @param col1  the column (0 based) of the first cell.
     * @param row1  the row (0 based) of the first cell.
     * @param col2  the column (0 based) of the second cell.
     * @param row2  the row (0 based) of the second cell.
     */
    constructor(dx1: Int, dy1: Int, dx2: Int, dy2: Int, col1: Short, row1: Int, col2: Short, row2: Int) :
            super(dx1, dy1, dx2, dy2) {
        checkRange(dx1, 0, 1023, "dx1")
        checkRange(dx2, 0, 1023, "dx2")
        checkRange(dy1, 0, 255, "dy1")
        checkRange(dy2, 0, 255, "dy2")
        checkRange(col1.toInt(), 0, 255, "col1")
        checkRange(col2.toInt(), 0, 255, "col2")
        checkRange(row1, 0, 255 * 256, "row1")
        checkRange(row2, 0, 255 * 256, "row2")

        this.col1 = col1
        this.row1 = row1
        this.col2 = col2
        this.row2 = row2
    }

    /**
     * Calculates the height of a client anchor in points.
     *
     * @param sheet     the sheet the anchor will be attached to
     * @return          the shape height.
     */
    fun getAnchorHeightInPoints(sheet: Sheet): Float {
        val y1 = getDy1()
        val y2 = getDy2()
        val row1 = Math.min(getRow1(), getRow2())
        val row2 = Math.max(getRow1(), getRow2())

        var points = 0f
        if (row1 == row2) {
            points = ((y2 - y1) / 256.0f) * getRowHeightInPoints(sheet, row2)
        } else {
            points += ((256.0f - y1) / 256.0f) * getRowHeightInPoints(sheet, row1)
            for (i in row1 + 1 until row2) {
                points += getRowHeightInPoints(sheet, i)
            }
            points += (y2 / 256.0f) * getRowHeightInPoints(sheet, row2)
        }

        return points
    }

    /**
     *
     * @param sheet
     * @param rowNum
     * @return
     */
    private fun getRowHeightInPoints(sheet: Sheet, rowNum: Int): Float {
        val row = sheet.getRow(rowNum)
        if (row == null) {
            return 18 * MainConstant.PIXEL_TO_POINT
        }
        return row.getRowPixelHeight() * MainConstant.PIXEL_TO_POINT
    }

    /**
     * (non-Javadoc)
     * @see IClientAnchor.getCol1
     */
    override fun getCol1(): Short = col1

    /**
     *
     * @param col1
     */
    fun setCol1(col1: Short) {
        checkRange(col1.toInt(), 0, 255, "col1")
        this.col1 = col1
    }

    /**
     * (non-Javadoc)
     * @see IClientAnchor.setCol1
     */
    override fun setCol1(col1: Int) {
        setCol1(col1.toShort())
    }

    /**
     * (non-Javadoc)
     * @see IClientAnchor.getCol2
     */
    override fun getCol2(): Short = col2

    /**
     *
     * @param col2
     */
    fun setCol2(col2: Short) {
        checkRange(col2.toInt(), 0, 255, "col2")
        this.col2 = col2
    }

    /**
     * (non-Javadoc)
     * @see IClientAnchor.setCol2
     */
    override fun setCol2(col2: Int) {
        setCol2(col2.toShort())
    }

    /**
     * (non-Javadoc)
     * @see IClientAnchor.getRow1
     */
    override fun getRow1(): Int = row1

    /**
     * (non-Javadoc)
     * @see IClientAnchor.setRow1
     */
    override fun setRow1(row1: Int) {
        checkRange(row1, 0, 256 * 256, "row1")
        this.row1 = row1
    }

    /**
     * (non-Javadoc)
     * @see IClientAnchor.getRow2
     */
    override fun getRow2(): Int = row2

    /**
     * (non-Javadoc)
     * @see IClientAnchor.setRow2
     */
    override fun setRow2(row2: Int) {
        checkRange(row2, 0, 256 * 256, "row2")
        this.row2 = row2
    }

    /**
     * Dets the top-left and bottom-right
     * coordinates of the anchor.
     *
     * @param x1   the x coordinate within the first cell.
     * @param y1   the y coordinate within the first cell.
     * @param x2   the x coordinate within the second cell.
     * @param y2   the y coordinate within the second cell.
     * @param col1  the column (0 based) of the first cell.
     * @param row1  the row (0 based) of the first cell.
     * @param col2  the column (0 based) of the second cell.
     * @param row2  the row (0 based) of the second cell.
     */
    fun setAnchor(col1: Short, row1: Int, x1: Int, y1: Int, col2: Short, row2: Int, x2: Int, y2: Int) {
        checkRange(dx1, 0, 1023, "dx1")
        checkRange(dx2, 0, 1023, "dx2")
        checkRange(dy1, 0, 255, "dy1")
        checkRange(dy2, 0, 255, "dy2")
        checkRange(col1.toInt(), 0, 255, "col1")
        checkRange(col2.toInt(), 0, 255, "col2")
        checkRange(row1, 0, 255 * 256, "row1")
        checkRange(row2, 0, 255 * 256, "row2")

        this.col1 = col1
        this.row1 = row1
        this.dx1 = x1
        this.dy1 = y1
        this.col2 = col2
        this.row2 = row2
        this.dx2 = x2
        this.dy2 = y2
    }

    /**
     * @return  true if the anchor goes from right to left.
     */
    override fun isHorizontallyFlipped(): Boolean {
        if (col1 == col2) {
            return dx1 > dx2
        }
        return col1 > col2
    }

    /**
     * @return  true if the anchor goes from bottom to top.
     */
    override fun isVerticallyFlipped(): Boolean {
        if (row1 == row2) {
            return dy1 > dy2
        }
        return row1 > row2
    }

    /**
     * Gets the anchor type
     *
     * 0 = Move and size with Cells, 2 = Move but don't size with cells, 3 = Don't move or size with cells.
     */
    override fun getAnchorType(): Int = anchorType

    /**
     * Sets the anchor type
     *
     * 0 = Move and size with Cells, 2 = Move but don't size with cells, 3 = Don't move or size with cells.
     */
    override fun setAnchorType(anchorType: Int) {
        this.anchorType = anchorType
    }

    private fun checkRange(value: Int, minRange: Int, maxRange: Int, varName: String) {
        if (value < minRange || value > maxRange)
            throw IllegalArgumentException(
                varName + " must be between " + minRange + " and "
                        + maxRange
            )
    }
}
