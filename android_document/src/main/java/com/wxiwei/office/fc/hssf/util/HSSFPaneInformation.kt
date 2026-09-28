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
package com.wxiwei.office.fc.hssf.util

/**
 * Holds information regarding a split plane or freeze plane for a sheet.
 * 
 */
class HSSFPaneInformation
    (
    /**
     * Returns the vertical position of the split.
     * @return 0 if there is no vertical spilt,
     * or for a freeze pane the number of columns in the TOP pane,
     * or for a split plane the position of the split in 1/20th of a point.
     */
    val verticalSplitPosition: Short,
    /**
     * Returns the horizontal position of the split.
     * @return 0 if there is no horizontal spilt,
     * or for a freeze pane the number of rows in the LEFT pane,
     * or for a split plane the position of the split in 1/20th of a point.
     */
    val horizontalSplitPosition: Short,
    /**
     * For a horizontal split returns the top row in the BOTTOM pane.
     * @return 0 if there is no horizontal split, or the top row of the bottom pane.
     */
    val horizontalSplitTopRow: Short,
    /**
     * For a vertical split returns the left column in the RIGHT pane.
     * @return 0 if there is no vertical split, or the left column in the RIGHT pane.
     */
    val verticalSplitLeftColumn: Short,
    /**
     * Returns the active pane
     * @see .PANE_LOWER_RIGHT
     * 
     * @see .PANE_UPPER_RIGHT
     * 
     * @see .PANE_LOWER_LEFT
     * 
     * @see .PANE_UPPER_LEFT
     * 
     * @return the active pane.
     */
    val activePane: Byte, frozen: Boolean
) {
    /** Returns true if this is a Freeze pane, false if it is a split pane.
     */
    var isFreezePane: Boolean = false
        private set

    init {
        this.isFreezePane = frozen
    }


    companion object {
        /** Constant for active pane being the lower right */
        val PANE_LOWER_RIGHT: Byte = 0.toByte()

        /** Constant for active pane being the upper right */
        val PANE_UPPER_RIGHT: Byte = 1.toByte()

        /** Constant for active pane being the lower left */
        val PANE_LOWER_LEFT: Byte = 2.toByte()

        /** Constant for active pane being the upper left */
        val PANE_UPPER_LEFT: Byte = 3.toByte()
    }
}
