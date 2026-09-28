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
package com.wxiwei.office.fc.hssf.formula.function

import com.wxiwei.office.fc.hssf.formula.TwoDEval
import com.wxiwei.office.fc.hssf.formula.eval.RefEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval


/**
 * Common logic for COUNT, COUNTA and COUNTIF
 * 
 * @author Josh Micich
 */
object CountUtils {
    /**
     * @return the number of evaluated cells in the range that match the specified criteria
     */
    fun countMatchingCellsInArea(areaEval: TwoDEval, criteriaPredicate: I_MatchPredicate): Int {
        var result = 0

        val height = areaEval.height
        val width = areaEval.width
        for (rrIx in 0..<height) {
            for (rcIx in 0..<width) {
                val ve = areaEval.getValue(rrIx, rcIx)

                if (criteriaPredicate is I_MatchAreaPredicate) {
                    val areaPredicate: I_MatchAreaPredicate = criteriaPredicate
                    if (!areaPredicate.matches(areaEval, rrIx, rcIx)) continue
                }

                if (criteriaPredicate.matches(ve)) {
                    result++
                }
            }
        }
        return result
    }

    /**
     * @return 1 if the evaluated cell matches the specified criteria
     */
    fun countMatchingCell(refEval: RefEval, criteriaPredicate: I_MatchPredicate): Int {
        if (criteriaPredicate.matches(refEval.innerValueEval)) {
            return 1
        }
        return 0
    }

    fun countArg(eval: ValueEval?, criteriaPredicate: I_MatchPredicate): Int {
        requireNotNull(eval) { "eval must not be null" }
        if (eval is TwoDEval) {
            return countMatchingCellsInArea(eval, criteriaPredicate)
        }
        if (eval is RefEval) {
            return countMatchingCell(eval, criteriaPredicate)
        }
        return if (criteriaPredicate.matches(eval)) 1 else 0
    }

    /**
     * Common interface for the matching criteria.
     */
    interface I_MatchPredicate {
        fun matches(x: ValueEval?): Boolean
    }

    interface I_MatchAreaPredicate : I_MatchPredicate {
        fun matches(x: TwoDEval, rowIndex: Int, columnIndex: Int): Boolean
    }
}
