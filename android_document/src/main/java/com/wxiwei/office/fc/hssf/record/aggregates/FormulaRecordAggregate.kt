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
package com.wxiwei.office.fc.hssf.record.aggregates

import com.wxiwei.office.fc.hssf.formula.Formula
import com.wxiwei.office.fc.hssf.formula.ptg.ExpPtg
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.record.ArrayRecord
import com.wxiwei.office.fc.hssf.record.CellValueRecordInterface
import com.wxiwei.office.fc.hssf.record.FormulaRecord
import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.hssf.record.RecordFormatException
import com.wxiwei.office.fc.hssf.record.SharedFormulaRecord
import com.wxiwei.office.fc.hssf.record.StringRecord
import com.wxiwei.office.fc.hssf.util.CellRangeAddress8Bit
import com.wxiwei.office.fc.ss.util.HSSFCellRangeAddress

/**
 * The formula record aggregate is used to join together the formula record and it's
 * (optional) string record and (optional) Shared Formula Record (template reads, excel optimization).
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Vladimirs Abramovs(Vladimirs.Abramovs at exigenservices.com) - Array Formula support
 */
class FormulaRecordAggregate(
    formulaRec: FormulaRecord,
    stringRec: StringRecord?,
    svm: SharedValueManager
) : RecordAggregate(), CellValueRecordInterface {
    val formulaRecord: FormulaRecord
    private val _sharedValueManager: SharedValueManager
    /**
     * debug only
     * TODO - encapsulate
     */
    /** caches the calculated result of the formula  */
    var stringRecord: StringRecord? = null
        private set
    private var _sharedFormulaRecord: SharedFormulaRecord? = null

    /**
     * @param stringRec may be `null` if this formula does not have a cached text
     * value.
     * @param svm the [SharedValueManager] for the current sheet
     */
    init {
        requireNotNull(svm) { "sfm must not be null" }
        if (formulaRec.hasCachedResultString()) {
            if (stringRec == null) {
                throw RecordFormatException("Formula record flag is set but String record was not found")
            }
            this.stringRecord = stringRec
        } else {
            // Usually stringRec is null here (in agreement with what the formula rec says).
            // In the case where an extra StringRecord is erroneously present, Excel (2007)
            // ignores it (see bug 46213).
            this.stringRecord = null
        }

        this.formulaRecord = formulaRec
        _sharedValueManager = svm
        if (formulaRec.isSharedFormula()) {
            val firstCell = formulaRec.getFormula().expReference
            if (firstCell == null) {
                handleMissingSharedFormulaRecord(formulaRec)
            } else {
                _sharedFormulaRecord = svm.linkSharedFormulaRecord(firstCell, this)
            }
        }
    }

    override var xFIndex: Short
        get() = formulaRecord.xFIndex
        set(xf) {
            formulaRecord.xFIndex = xf
        }

    override var column: Short
        get() = formulaRecord.column
        set(col) {
            formulaRecord.column = col
        }

    override var row: Int
        get() = formulaRecord.row
        set(row) {
            formulaRecord.row = row
        }

    override fun toString(): String {
        return formulaRecord.toString()
    }

    override fun visitContainedRecords(rv: RecordVisitor) {
        rv.visitRecord(this.formulaRecord)
        val sharedFormulaRecord: Record? = _sharedValueManager.getRecordForFirstCell(this)
        if (sharedFormulaRecord != null) {
            rv.visitRecord(sharedFormulaRecord)
        }
        if (formulaRecord.hasCachedResultString() && this.stringRecord != null) {
            rv.visitRecord(this.stringRecord!!)
        }
    }

    val stringValue: String?
        get() {
            if (this.stringRecord == null) {
                return null
            }
            return stringRecord!!.getString()
        }

    fun setCachedStringResult(value: String) {
        // Save the string into a String Record, creating one if required

        if (this.stringRecord == null) {
            this.stringRecord = StringRecord()
        }
        stringRecord!!.setString(value)
        if (value.length < 1) {
            formulaRecord.setCachedResultTypeEmptyString()
        } else {
            formulaRecord.setCachedResultTypeString()
        }
    }

    fun setCachedBooleanResult(value: Boolean) {
        this.stringRecord = null
        formulaRecord.setCachedResultBoolean(value)
    }

    fun setCachedErrorResult(errorCode: Int) {
        this.stringRecord = null
        formulaRecord.setCachedResultErrorCode(errorCode)
    }

    fun setCachedDoubleResult(value: Double) {
        this.stringRecord = null
        formulaRecord.setValue(value)
    }

    val formulaTokens: Array<Ptg?>
        get() {
            if (_sharedFormulaRecord != null) {
                return _sharedFormulaRecord!!.getFormulaTokens(this.formulaRecord)
            }
            val expRef =
                formulaRecord.getFormula().expReference
            if (expRef != null) {
                val arec =
                    _sharedValueManager.getArrayRecord(expRef.getRow(), expRef.getCol().toInt())
                return arec!!.formulaTokens
            }
            return formulaRecord.getParsedExpression()
        }

    /**
     * Also checks for a related shared formula and unlinks it if found
     */
    fun setParsedExpression(ptgs: Array<Ptg?>?) {
        notifyFormulaChanging()
        formulaRecord.setParsedExpression(ptgs)
    }

    fun unlinkSharedFormula() {
        val sfr = _sharedFormulaRecord
        checkNotNull(sfr) { "Formula not linked to shared formula" }
        val ptgs = sfr.getFormulaTokens(this.formulaRecord)
        formulaRecord.setParsedExpression(ptgs)
        //Now its not shared!
        formulaRecord.setSharedFormula(false)
        _sharedFormulaRecord = null
    }

    /**
     * Should be called by any code which is either deleting this formula cell, or changing
     * its type.  This method gives the aggregate a chance to unlink any shared formula
     * that may be involved with this cell formula.
     */
    fun notifyFormulaChanging() {
        if (_sharedFormulaRecord != null) {
            _sharedValueManager.unlink(_sharedFormulaRecord)
        }
    }

    val isPartOfArrayFormula: Boolean
        get() {
            if (_sharedFormulaRecord != null) {
                return false
            }
            val expRef =
                formulaRecord.getFormula().expReference
            val arec =
                if (expRef == null) null else _sharedValueManager.getArrayRecord(
                    expRef.getRow(),
                    expRef.getCol().toInt()
                )
            return arec != null
        }

    val arrayFormulaRange: HSSFCellRangeAddress
        get() {
            check(_sharedFormulaRecord == null) { "not an array formula cell." }
            val expRef =
                formulaRecord.getFormula().expReference
            checkNotNull(expRef) { "not an array formula cell." }
            val arec =
                _sharedValueManager.getArrayRecord(expRef.getRow(), expRef.getCol().toInt())
            checkNotNull(arec) { "ArrayRecord was not found for the locator " + expRef.formatAsString() }
            val a = arec.getRange()
            return HSSFCellRangeAddress(
                a.getFirstRow(),
                a.getLastRow(),
                a.getFirstColumn(),
                a.getLastColumn()
            )
        }

    fun setArrayFormula(r: HSSFCellRangeAddress, ptgs: Array<Ptg?>?) {
        val arr = ArrayRecord(
            Formula.create(ptgs)!!,
            CellRangeAddress8Bit(
                r.getFirstRow(),
                r.getLastRow(),
                r.getFirstColumn(),
                r.getLastColumn()
            )
        )
        _sharedValueManager.addArrayRecord(arr)
    }

    /**
     * Removes an array formula
     * @return the range of the array formula containing the specified cell. Never `null`
     */
    fun removeArrayFormula(rowIndex: Int, columnIndex: Int): HSSFCellRangeAddress {
        val a = _sharedValueManager.removeArrayFormula(rowIndex, columnIndex)!!
        // at this point FormulaRecordAggregate#isPartOfArrayFormula() should return false
        formulaRecord.setParsedExpression(null)
        return HSSFCellRangeAddress(
            a.getFirstRow(),
            a.getLastRow(),
            a.getFirstColumn(),
            a.getLastColumn()
        )
    }

    companion object {
        /**
         * Sometimes the shared formula flag "seems" to be erroneously set (because the corresponding
         * [SharedFormulaRecord] does not exist). Normally this would leave no way of determining
         * the [Ptg] tokens for the formula.  However as it turns out in these
         * cases, Excel encodes the unshared [Ptg] tokens in the right place (inside the [ ]).  So the the only thing that needs to be done is to ignore the erroneous
         * shared formula flag.<br></br>
         * 
         * This method may also be used for setting breakpoints to help diagnose issues regarding the
         * abnormally-set 'shared formula' flags.
         * (see TestValueRecordsAggregate.testSpuriousSharedFormulaFlag()).
         *
         *
         */
        private fun handleMissingSharedFormulaRecord(formula: FormulaRecord) {
            // make sure 'unshared' formula is actually available
            val firstToken = formula.getParsedExpression()[0]
            if (firstToken is ExpPtg) {
                throw RecordFormatException(
                    "SharedFormulaRecord not found for FormulaRecord with (isSharedFormula=true)"
                )
            }
            // could log an info message here since this is a fairly unusual occurrence.
            formula.setSharedFormula(false) // no point leaving the flag erroneously set
        }
    }
}
