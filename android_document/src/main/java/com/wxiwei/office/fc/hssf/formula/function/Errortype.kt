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

import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.EvaluationException
import com.wxiwei.office.fc.hssf.formula.eval.NumberEval
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver.getSingleValue
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.fc.ss.usermodel.ErrorConstants


/**
 * Implementation for the ERROR.TYPE() Excel function.
 * 
 * 
 * **Syntax:**<br></br>
 * **ERROR.TYPE**(**errorValue**)
 * 
 * 
 * Returns a number corresponding to the error type of the supplied argument.
 *
 *
 * 
 * 
 * <table border="1" cellpadding="1" cellspacing="1" summary="Return values for ERROR.TYPE()">
 * <tr><td>errorValue</td><td>Return Value</td></tr>
 * <tr><td>#NULL!</td><td>1</td></tr>
 * <tr><td>#DIV/0!</td><td>2</td></tr>
 * <tr><td>#VALUE!</td><td>3</td></tr>
 * <tr><td>#REF!</td><td>4</td></tr>
 * <tr><td>#NAME?</td><td>5</td></tr>
 * <tr><td>#NUM!</td><td>6</td></tr>
 * <tr><td>#N/A!</td><td>7</td></tr>
 * <tr><td>everything else</td><td>#N/A!</td></tr>
</table> * 
 * 
 * Note - the results of ERROR.TYPE() are different to the constants defined in
 * <tt>ErrorConstants</tt>.
 * 
 * 
 * @author Josh Micich
 */
class Errortype : Fixed1ArgFunction() {
    override fun evaluate(srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?): ValueEval {
        try {
            getSingleValue(arg0, srcRowIndex, srcColumnIndex)
            return ErrorEval.NA
        } catch (e: EvaluationException) {
            val result = translateErrorCodeToErrorTypeValue(e.errorEval!!.errorCode)
            return NumberEval(result.toDouble())
        }
    }

    private fun translateErrorCodeToErrorTypeValue(errorCode: Int): Int {
        when (errorCode) {
            ErrorConstants.ERROR_NULL -> return 1
            ErrorConstants.ERROR_DIV_0 -> return 2
            ErrorConstants.ERROR_VALUE -> return 3
            ErrorConstants.ERROR_REF -> return 4
            ErrorConstants.ERROR_NAME -> return 5
            ErrorConstants.ERROR_NUM -> return 6
            ErrorConstants.ERROR_NA -> return 7
        }
        throw IllegalArgumentException("Invalid error code (" + errorCode + ")")
    }
}
