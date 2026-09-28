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
package com.wxiwei.office.fc.hssf.formula

import com.wxiwei.office.fc.hssf.formula.eval.AreaEval
import com.wxiwei.office.fc.hssf.formula.eval.AreaEvalBase
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval

/**
 * Common interface of [AreaEval] and [AreaEvalBase]
 * 
 * @author Josh Micich
 */
interface TwoDEval : ValueEval {
    /**
     * @param rowIndex relative row index (zero based)
     * @param columnIndex relative column index (zero based)
     * @return element at the specified row and column position
     */
    fun getValue(rowIndex: Int, columnIndex: Int): ValueEval?

    val width: Int
    val height: Int

    /**
     * @return `true` if the area has just a single row, this also includes
     * the trivial case when the area has just a single cell.
     */
    val isRow: Boolean

    /**
     * @return `true` if the area has just a single column, this also includes
     * the trivial case when the area has just a single cell.
     */
    val isColumn: Boolean

    /**
     * @param rowIndex relative row index (zero based)
     * @return a single row [TwoDEval]
     */
    fun getRow(rowIndex: Int): TwoDEval?

    /**
     * @param columnIndex relative column index (zero based)
     * @return a single column [TwoDEval]
     */
    fun getColumn(columnIndex: Int): TwoDEval?


    /**
     * @return true if the  cell at row and col is a subtotal
     */
    fun isSubTotal(rowIndex: Int, columnIndex: Int): Boolean
}
