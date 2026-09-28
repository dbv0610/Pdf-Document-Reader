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

import com.wxiwei.office.fc.hssf.formula.SheetNameFormatter
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.EvaluationException
import com.wxiwei.office.fc.hssf.formula.eval.MissingArgEval
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver.getSingleValue
import com.wxiwei.office.fc.hssf.formula.eval.StringEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.fc.ss.util.CellReference

/**
 * Creates a text reference as text, given specified row and column numbers.
 * 
 * @author Aniket Banerjee (banerjee@google.com)
 */
class Address : Function {
    override fun evaluate(
        args: Array<ValueEval?>, srcRowIndex: Int,
        srcColumnIndex: Int
    ): ValueEval? {
        if (args.size < 2 || args.size > 5) {
            return ErrorEval.VALUE_INVALID
        }
        try {
            val pAbsRow: Boolean
            val pAbsCol: Boolean

            val row = NumericFunction.Companion.singleOperandEvaluate(
                args[0],
                srcRowIndex,
                srcColumnIndex
            ).toInt()
            val col = NumericFunction.Companion.singleOperandEvaluate(
                args[1],
                srcRowIndex,
                srcColumnIndex
            ).toInt()

            val refType: Int
            if (args.size > 2) {
                refType = NumericFunction.Companion.singleOperandEvaluate(
                    args[2],
                    srcRowIndex,
                    srcColumnIndex
                ).toInt()
            } else {
                refType = REF_ABSOLUTE
            }
            when (refType) {
                REF_ABSOLUTE -> {
                    pAbsRow = true
                    pAbsCol = true
                }

                REF_ROW_ABSOLUTE_COLUMN_RELATIVE -> {
                    pAbsRow = true
                    pAbsCol = false
                }

                REF_ROW_RELATIVE_RELATIVE_ABSOLUTE -> {
                    pAbsRow = false
                    pAbsCol = true
                }

                REF_RELATIVE -> {
                    pAbsRow = false
                    pAbsCol = false
                }

                else -> throw EvaluationException(ErrorEval.VALUE_INVALID)
            }

            val a1: Boolean
            if (args.size > 3) {
                val ve = getSingleValue(args[3], srcRowIndex, srcColumnIndex)
                // TODO R1C1 style is not yet supported
                a1 =
                    if (ve === MissingArgEval.instance) true else OperandResolver.coerceValueToBoolean(
                        ve,
                        false
                    )!!
            } else {
                a1 = true
            }

            val sheetName: String?
            if (args.size == 5) {
                val ve = getSingleValue(args[4], srcRowIndex, srcColumnIndex)
                sheetName =
                    if (ve === MissingArgEval.instance) null else OperandResolver.coerceValueToString(
                        ve!!
                    )
            } else {
                sheetName = null
            }

            val ref = CellReference(row - 1, col - 1, pAbsRow, pAbsCol)
            val sb = StringBuffer(32)
            if (sheetName != null) {
                SheetNameFormatter.appendFormat(sb, sheetName)
                sb.append('!')
            }
            sb.append(ref.formatAsString())

            return StringEval(sb.toString())
        } catch (e: EvaluationException) {
            return e.errorEval
        }
    }

    companion object {
        const val REF_ABSOLUTE: Int = 1
        const val REF_ROW_ABSOLUTE_COLUMN_RELATIVE: Int = 2
        const val REF_ROW_RELATIVE_RELATIVE_ABSOLUTE: Int = 3
        const val REF_RELATIVE: Int = 4
    }
}
