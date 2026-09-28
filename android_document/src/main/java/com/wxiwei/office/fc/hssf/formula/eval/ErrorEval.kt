/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.wxiwei.office.fc.hssf.formula.eval

import com.wxiwei.office.fc.ss.usermodel.ErrorConstants

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 */
class ErrorEval
/**
 * @param errorCode an 8-bit value
 */ private constructor(val errorCode: Int) : ValueEval {
    override fun toString(): String {
        val sb = StringBuffer(64)
        sb.append(javaClass.getName()).append(" [")
        sb.append(getText(this.errorCode))
        sb.append("]")
        return sb.toString()
    }

    companion object {
        // convenient access to namespace
        private val EC: ErrorConstants? = null

        /** **#NULL!**  - Intersection of two cell ranges is empty  */
        val NULL_INTERSECTION: ErrorEval = ErrorEval(ErrorConstants.ERROR_NULL)

        /** **#DIV/0!** - Division by zero  */
        val DIV_ZERO: ErrorEval = ErrorEval(ErrorConstants.ERROR_DIV_0)

        /** **#VALUE!** - Wrong type of operand  */
        val VALUE_INVALID: ErrorEval = ErrorEval(ErrorConstants.ERROR_VALUE)

        /** **#REF!** - Illegal or deleted cell reference  */
        val REF_INVALID: ErrorEval = ErrorEval(ErrorConstants.ERROR_REF)

        /** **#NAME?** - Wrong function or range name  */
        val NAME_INVALID: ErrorEval = ErrorEval(ErrorConstants.ERROR_NAME)

        /** **#NUM!** - Value range overflow  */
        val NUM_ERROR: ErrorEval = ErrorEval(ErrorConstants.ERROR_NUM)

        /** **#N/A** - Argument or function not available  */
        val NA: ErrorEval = ErrorEval(ErrorConstants.ERROR_NA)


        // POI internal error codes
        private const val CIRCULAR_REF_ERROR_CODE = -0x3c
        private const val FUNCTION_NOT_IMPLEMENTED_CODE = -0x1e

        // Note - Excel does not seem to represent this condition with an error code
        val CIRCULAR_REF_ERROR: ErrorEval = ErrorEval(CIRCULAR_REF_ERROR_CODE)


        /**
         * Translates an Excel internal error code into the corresponding POI ErrorEval instance
         * @param errorCode
         */
        fun valueOf(errorCode: Int): ErrorEval {
            when (errorCode) {
                ErrorConstants.ERROR_NULL -> return NULL_INTERSECTION
                ErrorConstants.ERROR_DIV_0 -> return DIV_ZERO
                ErrorConstants.ERROR_VALUE -> return VALUE_INVALID
                ErrorConstants.ERROR_REF -> return REF_INVALID
                ErrorConstants.ERROR_NAME -> return NAME_INVALID
                ErrorConstants.ERROR_NUM -> return NUM_ERROR
                ErrorConstants.ERROR_NA -> return NA
                CIRCULAR_REF_ERROR_CODE -> return CIRCULAR_REF_ERROR
            }
            throw RuntimeException("Unexpected error code (" + errorCode + ")")
        }

        /**
         * Converts error codes to text.  Handles non-standard error codes OK.
         * For debug/test purposes (and for formatting error messages).
         * @return the String representation of the specified Excel error code.
         */
        @JvmStatic
        fun getText(errorCode: Int): String {
            if (ErrorConstants.isValidCode(errorCode)) {
                return ErrorConstants.getText(errorCode)
            }
            // It is desirable to make these (arbitrary) strings look clearly different from any other
            // value expression that might appear in a formula.  In addition these error strings should
            // look unlike the standard Excel errors.  Hence tilde ('~') was used.
            when (errorCode) {
                CIRCULAR_REF_ERROR_CODE -> return "~CIRCULAR~REF~"
                FUNCTION_NOT_IMPLEMENTED_CODE -> return "~FUNCTION~NOT~IMPLEMENTED~"
            }
            return "~non~std~err(" + errorCode + ")~"
        }
    }
}
