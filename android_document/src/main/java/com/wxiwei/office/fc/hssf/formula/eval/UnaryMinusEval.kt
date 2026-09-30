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

import com.wxiwei.office.fc.hssf.formula.function.Fixed1ArgFunction
import com.wxiwei.office.fc.hssf.formula.function.Function

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 */
class UnaryMinusEval private constructor() : Fixed1ArgFunction() {
    override fun evaluate(srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?): ValueEval? {
        val d: Double
        try {
            val ve = OperandResolver.getSingleValue(arg0, srcRowIndex, srcColumnIndex)
            d = OperandResolver.coerceValueToDouble(ve)
        } catch (e: EvaluationException) {
            return e.errorEval
        }
        if (d == 0.0) { // this '==' matches +0.0 and -0.0
            return NumberEval.Companion.ZERO
        }
        return NumberEval(-d)
    }

    companion object {
        val instance: Function = UnaryMinusEval()
    }
}
