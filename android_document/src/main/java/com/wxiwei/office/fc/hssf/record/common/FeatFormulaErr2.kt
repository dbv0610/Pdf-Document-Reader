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
package com.wxiwei.office.fc.hssf.record.common

import com.wxiwei.office.fc.hssf.record.RecordInputStream
import com.wxiwei.office.fc.util.BitField
import com.wxiwei.office.fc.util.BitFieldFactory.Companion.getInstance
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Title: FeatFormulaErr2 (Formula Evaluation Shared Feature) common record part
 * <P>
 * This record part specifies Formula Evaluation & Error Ignoring data
 * for a sheet, stored as part of a Shared Feature. It can be found in
 * records such as [FeatRecord].
 * For the full meanings of the flags, see pages 669 and 670
 * of the Excel binary file format documentation.
</P> */
class FeatFormulaErr2 : SharedFeature {
    /**
     * What errors we should ignore
     */
    private var errorCheck = 0


    constructor()

    constructor(`in`: RecordInputStream) {
        errorCheck = `in`.readInt()
    }

    override fun toString(): String {
        val buffer = StringBuffer()
        buffer.append(" [FEATURE FORMULA ERRORS]\n")
        buffer.append("  checkCalculationErrors    = ")
        buffer.append("  checkEmptyCellRef         = ")
        buffer.append("  checkNumbersAsText        = ")
        buffer.append("  checkInconsistentRanges   = ")
        buffer.append("  checkInconsistentFormulas = ")
        buffer.append("  checkDateTimeFormats      = ")
        buffer.append("  checkUnprotectedFormulas  = ")
        buffer.append("  performDataValidation     = ")
        buffer.append(" [/FEATURE FORMULA ERRORS]\n")
        return buffer.toString()
    }

    override fun serialize(out: LittleEndianOutput) {
        out.writeInt(errorCheck)
    }

    override val dataSize: Int
        get() = 4

    fun _getRawErrorCheckValue(): Int {
        return errorCheck
    }

    var checkCalculationErrors: Boolean
        get() = Companion.checkCalculationErrors.isSet(errorCheck)
        set(checkCalculationErrors) {
            Companion.checkCalculationErrors.setBoolean(
                errorCheck, checkCalculationErrors
            )
        }

    var checkEmptyCellRef: Boolean
        get() = Companion.checkEmptyCellRef.isSet(errorCheck)
        set(checkEmptyCellRef) {
            Companion.checkEmptyCellRef.setBoolean(
                errorCheck, checkEmptyCellRef
            )
        }

    var checkNumbersAsText: Boolean
        get() = Companion.checkNumbersAsText.isSet(errorCheck)
        set(checkNumbersAsText) {
            Companion.checkNumbersAsText.setBoolean(
                errorCheck, checkNumbersAsText
            )
        }

    var checkInconsistentRanges: Boolean
        get() = Companion.checkInconsistentRanges.isSet(errorCheck)
        set(checkInconsistentRanges) {
            Companion.checkInconsistentRanges.setBoolean(
                errorCheck, checkInconsistentRanges
            )
        }

    var checkInconsistentFormulas: Boolean
        get() = Companion.checkInconsistentFormulas.isSet(errorCheck)
        set(checkInconsistentFormulas) {
            Companion.checkInconsistentFormulas.setBoolean(
                errorCheck, checkInconsistentFormulas
            )
        }

    var checkDateTimeFormats: Boolean
        get() = Companion.checkDateTimeFormats.isSet(errorCheck)
        set(checkDateTimeFormats) {
            Companion.checkDateTimeFormats.setBoolean(
                errorCheck, checkDateTimeFormats
            )
        }

    var checkUnprotectedFormulas: Boolean
        get() = Companion.checkUnprotectedFormulas.isSet(errorCheck)
        set(checkUnprotectedFormulas) {
            Companion.checkUnprotectedFormulas.setBoolean(
                errorCheck, checkUnprotectedFormulas
            )
        }

    var performDataValidation: Boolean
        get() = Companion.performDataValidation.isSet(errorCheck)
        set(performDataValidation) {
            Companion.performDataValidation.setBoolean(
                errorCheck, performDataValidation
            )
        }

    companion object {
        var checkCalculationErrors: BitField = getInstance(0x01)
        var checkEmptyCellRef: BitField = getInstance(0x02)
        var checkNumbersAsText: BitField = getInstance(0x04)
        var checkInconsistentRanges: BitField = getInstance(0x08)
        var checkInconsistentFormulas: BitField = getInstance(0x10)
        var checkDateTimeFormats: BitField = getInstance(0x20)
        var checkUnprotectedFormulas: BitField = getInstance(0x40)
        var performDataValidation: BitField = getInstance(0x80)
    }
}
