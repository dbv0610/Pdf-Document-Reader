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
    val col1: Short

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
    val col2: Short

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
    /**
     * Returns the row (0 based) of the first cell.
     * 
     * @param row1 0-based row of the first cell.
     */
    var row1: Int

    /**
     * Returns the row (0 based) of the second cell.
     * 
     * @return 0-based row of the second cell.
     */
    /**
     * Returns the row (0 based) of the first cell.
     * 
     * @param row2 0-based row of the first cell.
     */
    var row2: Int

    /**
     * Returns the x coordinate within the first cell.
     * 
     * Note - XSSF and HSSF have a slightly different coordinate
     * system, values in XSSF are larger by a factor of
     * [org.apache.poi.xssf.usermodel.XSSFShape.EMU_PER_PIXEL]
     * 
     * @return the x coordinate within the first cell
     */
    /**
     * Sets the x coordinate within the first cell
     * 
     * Note - XSSF and HSSF have a slightly different coordinate
     * system, values in XSSF are larger by a factor of
     * [org.apache.poi.xssf.usermodel.XSSFShape.EMU_PER_PIXEL]
     * 
     * @param dx1 the x coordinate within the first cell
     */
    var dx1: Int

    /**
     * Returns the y coordinate within the first cell
     * 
     * Note - XSSF and HSSF have a slightly different coordinate
     * system, values in XSSF are larger by a factor of
     * [org.apache.poi.xssf.usermodel.XSSFShape.EMU_PER_PIXEL]
     * 
     * @return the y coordinate within the first cell
     */
    /**
     * Sets the y coordinate within the first cell
     * 
     * Note - XSSF and HSSF have a slightly different coordinate
     * system, values in XSSF are larger by a factor of
     * [org.apache.poi.xssf.usermodel.XSSFShape.EMU_PER_PIXEL]
     * 
     * @param dy1 the y coordinate within the first cell
     */
    var dy1: Int

    /**
     * Sets the y coordinate within the second cell
     * 
     * Note - XSSF and HSSF have a slightly different coordinate
     * system, values in XSSF are larger by a factor of
     * [org.apache.poi.xssf.usermodel.XSSFShape.EMU_PER_PIXEL]
     * 
     * @return the y coordinate within the second cell
     */
    /**
     * Sets the y coordinate within the second cell
     * 
     * Note - XSSF and HSSF have a slightly different coordinate
     * system, values in XSSF are larger by a factor of
     * [org.apache.poi.xssf.usermodel.XSSFShape.EMU_PER_PIXEL]
     * 
     * @param dy2 the y coordinate within the second cell
     */
    var dy2: Int

    /**
     * Returns the x coordinate within the second cell
     * 
     * Note - XSSF and HSSF have a slightly different coordinate
     * system, values in XSSF are larger by a factor of
     * [org.apache.poi.xssf.usermodel.XSSFShape.EMU_PER_PIXEL]
     * 
     * @return the x coordinate within the second cell
     */
    /**
     * Sets the x coordinate within the second cell
     * 
     * Note - XSSF and HSSF have a slightly different coordinate
     * system, values in XSSF are larger by a factor of
     * [org.apache.poi.xssf.usermodel.XSSFShape.EMU_PER_PIXEL]
     * 
     * @param dx2 the x coordinate within the second cell
     */
    var dx2: Int

    /**
     * Gets the anchor type
     * 
     * 
     * 0 = Move and size with Cells, 2 = Move but don't size with cells, 3 = Don't move or size with cells.
     * 
     * @return the anchor type
     * @see .MOVE_AND_RESIZE
     * 
     * @see .MOVE_DONT_RESIZE
     * 
     * @see .DONT_MOVE_AND_RESIZE
     */
    /**
     * Sets the anchor type
     * 
     * 
     * 0 = Move and size with Cells, 2 = Move but don't size with cells, 3 = Don't move or size with cells.
     * 
     * @param anchorType the anchor type
     * @see .MOVE_AND_RESIZE
     * 
     * @see .MOVE_DONT_RESIZE
     * 
     * @see .DONT_MOVE_AND_RESIZE
     */
    var anchorType: Int

    companion object {
        /**
         * Move and Resize With Anchor Cells
         * 
         * 
         * Specifies that the current drawing shall move and
         * resize to maintain its row and column anchors (i.e. the
         * object is anchored to the actual from and to row and column)
         * 
         */
        const val MOVE_AND_RESIZE: Int = 0

        /**
         * Move With Cells but Do Not Resize
         * 
         * 
         * Specifies that the current drawing shall move with its
         * row and column (i.e. the object is anchored to the
         * actual from row and column), but that the size shall remain absolute.
         * 
         * 
         * 
         * If additional rows/columns are added between the from and to locations of the drawing,
         * the drawing shall move its to anchors as needed to maintain this same absolute size.
         * 
         */
        const val MOVE_DONT_RESIZE: Int = 2

        /**
         * Do Not Move or Resize With Underlying Rows/Columns
         * 
         * 
         * Specifies that the current start and end positions shall
         * be maintained with respect to the distances from the
         * absolute start point of the worksheet.
         * 
         * 
         * 
         * If additional rows/columns are added before the
         * drawing, the drawing shall move its anchors as needed
         * to maintain this same absolute position.
         * 
         */
        const val DONT_MOVE_AND_RESIZE: Int = 3
    }
}
