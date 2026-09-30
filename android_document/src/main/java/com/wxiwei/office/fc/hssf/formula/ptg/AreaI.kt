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
package com.wxiwei.office.fc.hssf.formula.ptg

import kotlin.math.max
import kotlin.math.min

/**
 * Common interface for AreaPtg and Area3DPtg, and their child classes.
 */
interface AreaI {
    /**
     * @return the first row in the area
     */
    val firstRow: Int

    /**
     * @return last row in the range (x2 in x1,y1-x2,y2)
     */
    val lastRow: Int

    /**
     * @return the first column number in the area.
     */
    val firstColumn: Int

    /**
     * @return lastcolumn in the area
     */
    val lastColumn: Int

    class OffsetArea(
        baseRow: Int, baseColumn: Int, relFirstRowIx: Int, relLastRowIx: Int,
        relFirstColIx: Int, relLastColIx: Int
    ) : AreaI {
        private val _firstColumn: Int
        private val _firstRow: Int
        private val _lastColumn: Int
        private val _lastRow: Int

        init {
            _firstRow = baseRow + min(relFirstRowIx, relLastRowIx)
            _lastRow = baseRow + max(relFirstRowIx, relLastRowIx)
            _firstColumn = baseColumn + min(relFirstColIx, relLastColIx)
            _lastColumn = baseColumn + max(relFirstColIx, relLastColIx)
        }

        override val firstColumn: Int get() {
            return _firstColumn
        }

        override val firstRow: Int get() {
            return _firstRow
        }

        override val lastColumn: Int get() {
            return _lastColumn
        }

        override val lastRow: Int get() {
            return _lastRow
        }
    }
}
