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

import com.wxiwei.office.fc.hssf.formula.eval.BlankEval
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.EvaluationException
import com.wxiwei.office.fc.hssf.formula.eval.MissingArgEval
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver.getSingleValue
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval

/**
 * @author Josh Micich
 */
class Choose : Function {
    override fun evaluate(
        args: Array<ValueEval?>,
        srcRowIndex: Int,
        srcColumnIndex: Int
    ): ValueEval? {
        if (args.size < 2) {
            return ErrorEval.VALUE_INVALID
        }

        try {
            val ix: Int = evaluateFirstArg(args[0], srcRowIndex, srcColumnIndex)
            if (ix < 1 || ix >= args.size) {
                return ErrorEval.VALUE_INVALID
            }
            val result = getSingleValue(args[ix], srcRowIndex, srcColumnIndex)
            if (result === MissingArgEval.instance) {
                return BlankEval.instance
            }
            return result
        } catch (e: EvaluationException) {
            return e.errorEval
        }
    }

    companion object {
        @JvmStatic
        @Throws(EvaluationException::class)
        fun evaluateFirstArg(arg0: ValueEval?, srcRowIndex: Int, srcColumnIndex: Int): Int {
            val ev = getSingleValue(arg0, srcRowIndex, srcColumnIndex)
            return OperandResolver.coerceValueToInt(ev!!)
        }
    }
}
