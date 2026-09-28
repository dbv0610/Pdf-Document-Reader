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

import com.wxiwei.office.fc.hssf.formula.function.Fixed2ArgFunction
import com.wxiwei.office.fc.hssf.formula.function.Function
import kotlin.math.max
import kotlin.math.min

/**
 * @author Josh Micich
 */
class IntersectionEval private constructor() : Fixed2ArgFunction() {
    override fun evaluate(
        srcRowIndex: Int,
        srcColumnIndex: Int,
        arg0: ValueEval?,
        arg1: ValueEval?
    ): ValueEval? {
        try {
            val reA: AreaEval = evaluateRef(arg0)
            val reB: AreaEval = evaluateRef(arg1)
            val result: AreaEval? = resolveRange(reA, reB)
            if (result == null) {
                return ErrorEval.Companion.NULL_INTERSECTION
            }
            return result
        } catch (e: EvaluationException) {
            return e.errorEval
        }
    }

    companion object {
        val instance: Function = IntersectionEval()

        /**
         * @return simple rectangular [AreaEval] which represents the intersection of areas
         * <tt>aeA</tt> and <tt>aeB</tt>. If the two areas do not intersect, the result is `null`.
         */
        private fun resolveRange(aeA: AreaEval, aeB: AreaEval): AreaEval? {
            val aeAfr = aeA.firstRow
            val aeAfc = aeA.firstColumn
            val aeBlc = aeB.lastColumn
            if (aeAfc > aeBlc) {
                return null
            }
            val aeBfc = aeB.firstColumn
            if (aeBfc > aeA.lastColumn) {
                return null
            }
            val aeBlr = aeB.lastRow
            if (aeAfr > aeBlr) {
                return null
            }
            val aeBfr = aeB.firstRow
            val aeAlr = aeA.lastRow
            if (aeBfr > aeAlr) {
                return null
            }


            val top = max(aeAfr, aeBfr)
            val bottom = min(aeAlr, aeBlr)
            val left = max(aeAfc, aeBfc)
            val right = min(aeA.lastColumn, aeBlc)

            return aeA.offset(top - aeAfr, bottom - aeAfr, left - aeAfc, right - aeAfc)
        }

        @Throws(EvaluationException::class)
        private fun evaluateRef(arg: ValueEval?): AreaEval {
            if (arg is AreaEval) {
                return arg
            }
            if (arg is RefEval) {
                return arg.offset(0, 0, 0, 0)
            }
            if (arg is ErrorEval) {
                throw EvaluationException(arg)
            }
            throw IllegalArgumentException("Unexpected ref arg class (" + arg!!.javaClass.getName() + ")")
        }
    }
}
