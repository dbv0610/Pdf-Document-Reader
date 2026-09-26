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

package com.wxiwei.office.ss.model.interfacePart

/**
 * A client anchor is attached to an excel worksheet.  It anchors against a
 * top-left and bottom-right cell.
 *
 * @author Yegor Kozlov
 */
interface IClientAnchor {
    /**
     * Returns the column (0 based) of the first cell.
     *
     * @return 0-based column of the first cell.
     */
    fun getCol1(): Short

    /**
     * Sets the column (0 based) of the first cell.
     *
     * @param col1 0-based column of the first cell.
     */
    fun setCol1(col1: Int)

    /**
     * Returns the column (0 based) of the second cell.
     *
     * @return 0-based column of the second cell.
     */
    fun getCol2(): Short

    /**
     * Returns the column (0 based) of the second cell.
     *
     * @param col2 0-based column of the second cell.
     */
    fun setCol2(col2: Int)

    /**
     * Returns the row (0 based) of the first cell.
     *
     * @return 0-based row of the first cell.
     */
    fun getRow1(): Int

    /**
     * Returns the row (0 based) of the first cell.
     *
     * @param row1 0-based row of the first cell.
     */
    fun setRow1(row1: Int)

    /**
     * Returns the row (0 based) of the second cell.
     *
     * @return 0-based row of the second cell.
     */
    fun getRow2(): Int

    /**
     * Returns the row (0 based) of the first cell.
     *
     * @param row2 0-based row of the first cell.
     */
    fun setRow2(row2: Int)

    /**
     * Returns the x coordinate within the first cell.
     *
     * Note - XSSF and HSSF have a slightly different coordinate
     *  system, values in XSSF are larger by a factor of
     *  EMU_PER_PIXEL
     *
     * @return the x coordinate within the first cell
     */
    fun getDx1(): Int

    /**
     * Sets the x coordinate within the first cell
     *
     * @param dx1 the x coordinate within the first cell
     */
    fun setDx1(dx1: Int)

    /**
     * Returns the y coordinate within the first cell
     *
     * @return the y coordinate within the first cell
     */
    fun getDy1(): Int

    /**
     * Sets the y coordinate within the first cell
     *
     * @param dy1 the y coordinate within the first cell
     */
    fun setDy1(dy1: Int)

    /**
     * Sets the y coordinate within the second cell
     *
     * @return the y coordinate within the second cell
     */
    fun getDy2(): Int

    /**
     * Sets the y coordinate within the second cell
     *
     * @param dy2 the y coordinate within the second cell
     */
    fun setDy2(dy2: Int)

    /**
     * Returns the x coordinate within the second cell
     *
     * @return the x coordinate within the second cell
     */
    fun getDx2(): Int

    /**
     * Sets the x coordinate within the second cell
     *
     * @param dx2 the x coordinate within the second cell
     */
    fun setDx2(dx2: Int)

    /**
     * Sets the anchor type
     *
     * 0 = Move and size with Cells, 2 = Move but don't size with cells, 3 = Don't move or size with cells.
     *
     * @param anchorType the anchor type
     * @see MOVE_AND_RESIZE
     * @see MOVE_DONT_RESIZE
     * @see DONT_MOVE_AND_RESIZE
     */
    fun setAnchorType(anchorType: Int)

    /**
     * Gets the anchor type
     *
     * 0 = Move and size with Cells, 2 = Move but don't size with cells, 3 = Don't move or size with cells.
     *
     * @return the anchor type
     * @see MOVE_AND_RESIZE
     * @see MOVE_DONT_RESIZE
     * @see DONT_MOVE_AND_RESIZE
     */
    fun getAnchorType(): Int

    companion object {
        /**
         * Move and Resize With Anchor Cells
         *
         * Specifies that the current drawing shall move and
         * resize to maintain its row and column anchors (i.e. the
         * object is anchored to the actual from and to row and column)
         */
        const val MOVE_AND_RESIZE = 0

        /**
         * Move With Cells but Do Not Resize
         *
         * Specifies that the current drawing shall move with its
         * row and column (i.e. the object is anchored to the
         * actual from row and column), but that the size shall remain absolute.
         *
         * If additional rows/columns are added between the from and to locations of the drawing,
         * the drawing shall move its to anchors as needed to maintain this same absolute size.
         */
        const val MOVE_DONT_RESIZE = 2

        /**
         * Do Not Move or Resize With Underlying Rows/Columns
         *
         * Specifies that the current start and end positions shall
         * be maintained with respect to the distances from the
         * absolute start point of the worksheet.
         *
         * If additional rows/columns are added before the
         * drawing, the drawing shall move its anchors as needed
         * to maintain this same absolute position.
         */
        const val DONT_MOVE_AND_RESIZE = 3
    }
}
