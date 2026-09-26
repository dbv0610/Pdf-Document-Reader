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

package com.wxiwei.office.constant.fc

import com.wxiwei.office.fc.ss.usermodel.ErrorConstants

/**
 * Represents a constant error code value as encoded in a constant values array.
 *
 * This class is a type-safe wrapper for a 16-bit int value performing a similar job to
 * <tt>ErrorEval</tt>.
 *
 * @author Josh Micich
 */
class ErrorConstant private constructor(val errorCode: Int) {

    val text: String
        get() {
            if (ErrorConstants.isValidCode(errorCode)) {
                return ErrorConstants.getText(errorCode)
            }
            return "unknown error code ($errorCode)"
        }

    override fun toString(): String {
        val sb = StringBuffer(64)
        sb.append(javaClass.name).append(" [")
        sb.append(text)
        sb.append("]")
        return sb.toString()
    }

    companion object {
        private val NULL = ErrorConstant(ErrorConstants.ERROR_NULL)
        private val DIV_0 = ErrorConstant(ErrorConstants.ERROR_DIV_0)
        private val VALUE = ErrorConstant(ErrorConstants.ERROR_VALUE)
        private val REF = ErrorConstant(ErrorConstants.ERROR_REF)
        private val NAME = ErrorConstant(ErrorConstants.ERROR_NAME)
        private val NUM = ErrorConstant(ErrorConstants.ERROR_NUM)
        private val NA = ErrorConstant(ErrorConstants.ERROR_NA)

        @JvmStatic
        fun valueOf(errorCode: Int): ErrorConstant {
            when (errorCode) {
                ErrorConstants.ERROR_NULL -> return NULL
                ErrorConstants.ERROR_DIV_0 -> return DIV_0
                ErrorConstants.ERROR_VALUE -> return VALUE
                ErrorConstants.ERROR_REF -> return REF
                ErrorConstants.ERROR_NAME -> return NAME
                ErrorConstants.ERROR_NUM -> return NUM
                ErrorConstants.ERROR_NA -> return NA
            }
            System.err.println("Warning - unexpected error code ($errorCode)")
            return ErrorConstant(errorCode)
        }
    }
}
