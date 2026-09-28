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
package com.wxiwei.office.fc.hssf.formula.eval

import com.wxiwei.office.fc.hssf.formula.TwoDEval

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 */
interface AreaEval : TwoDEval {
    /**
     * returns the 0-based index of the first row in
     * this area.
     */
    val firstRow: Int

    /**
     * returns the 0-based index of the last row in
     * this area.
     */
    val lastRow: Int

    /**
     * returns the 0-based index of the first col in
     * this area.
     */
    val firstColumn: Int

    /**
     * returns the 0-based index of the last col in
     * this area.
     */
    val lastColumn: Int

    /**
     * @return the ValueEval from within this area at the specified row and col index. Never
     * `null` (possibly [BlankEval]).  The specified indexes should be absolute
     * indexes in the sheet and not relative indexes within the area.
     */
    fun getAbsoluteValue(row: Int, col: Int): ValueEval?

    /**
     * returns true if the cell at row and col specified
     * as absolute indexes in the sheet is contained in
     * this area.
     * @param row
     * @param col
     */
    fun contains(row: Int, col: Int): Boolean

    /**
     * returns true if the specified col is in range
     * @param col
     */
    fun containsColumn(col: Int): Boolean

    /**
     * returns true if the specified row is in range
     * @param row
     */
    fun containsRow(row: Int): Boolean


    /**
     * @return the ValueEval from within this area at the specified relativeRowIndex and
     * relativeColumnIndex. Never `null` (possibly [BlankEval]). The
     * specified indexes should relative to the top left corner of this area.
     */
    fun getRelativeValue(relativeRowIndex: Int, relativeColumnIndex: Int): ValueEval?

    /**
     * Creates an [AreaEval] offset by a relative amount from from the upper left cell
     * of this area
     */
    fun offset(
        relFirstRowIx: Int,
        relLastRowIx: Int,
        relFirstColIx: Int,
        relLastColIx: Int
    ): AreaEval
}
