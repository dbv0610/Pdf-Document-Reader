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

import com.wxiwei.office.fc.ss.usermodel.ErrorConstants
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * @author Daniel Noll (daniel at nuix dot com dot au)
 */
class ErrPtg private constructor(errorCode: Int) : ScalarConstantPtg() {
    val errorCode: Int

    /** Creates new ErrPtg  */
    init {
        require(ErrorConstants.isValidCode(errorCode)) { "Invalid error code (" + errorCode + ")" }
        this.errorCode = errorCode
    }

    override fun write(out: LittleEndianOutput) {
        out.writeByte(sid + ptgClass)
        out.writeByte(this.errorCode)
    }

    override fun toFormulaString(): String {
        return ErrorConstants.getText(this.errorCode)
    }

    override val size: Int get() {
        return SIZE
    }

    companion object {
        // convenient access to namespace
        private val EC: ErrorConstants? = null

        /** **#NULL!**  - Intersection of two cell ranges is empty  */
        val NULL_INTERSECTION: ErrPtg = ErrPtg(ErrorConstants.ERROR_NULL)

        /** **#DIV/0!** - Division by zero  */
        val DIV_ZERO: ErrPtg = ErrPtg(ErrorConstants.ERROR_DIV_0)

        /** **#VALUE!** - Wrong type of operand  */
        val VALUE_INVALID: ErrPtg = ErrPtg(ErrorConstants.ERROR_VALUE)

        /** **#REF!** - Illegal or deleted cell reference  */
        @JvmField
        val REF_INVALID: ErrPtg = ErrPtg(ErrorConstants.ERROR_REF)

        /** **#NAME?** - Wrong function or range name  */
        val NAME_INVALID: ErrPtg = ErrPtg(ErrorConstants.ERROR_NAME)

        /** **#NUM!** - Value range overflow  */
        val NUM_ERROR: ErrPtg = ErrPtg(ErrorConstants.ERROR_NUM)

        /** **#N/A** - Argument or function not available  */
        val N_A: ErrPtg = ErrPtg(ErrorConstants.ERROR_NA)


        const val sid: Short = 0x1c
        private const val SIZE = 2
        fun read(`in`: LittleEndianInput): ErrPtg {
            return valueOf(`in`.readByte().toInt())
        }

        @JvmStatic
        fun valueOf(code: Int): ErrPtg {
            when (code) {
                ErrorConstants.ERROR_DIV_0 -> return DIV_ZERO
                ErrorConstants.ERROR_NA -> return N_A
                ErrorConstants.ERROR_NAME -> return NAME_INVALID
                ErrorConstants.ERROR_NULL -> return NULL_INTERSECTION
                ErrorConstants.ERROR_NUM -> return NUM_ERROR
                ErrorConstants.ERROR_REF -> return REF_INVALID
                ErrorConstants.ERROR_VALUE -> return VALUE_INVALID
            }
            throw RuntimeException("Unexpected error code (" + code + ")")
        }
    }
}
