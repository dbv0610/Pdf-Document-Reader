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

import com.wxiwei.office.fc.hssf.formula.EvaluationCell
import com.wxiwei.office.fc.hssf.formula.EvaluationSheet
import com.wxiwei.office.fc.ss.usermodel.ICell


/**
 * Represents a cell being used for forked evaluation that has had a value set different from the
 * corresponding cell in the shared master workbook.
 * 
 * @author Josh Micich
 */
internal class ForkedEvaluationCell(sheet: ForkedEvaluationSheet?, masterCell: EvaluationCell) :
    EvaluationCell {
    private val _sheet: EvaluationSheet?

    /** corresponding cell from master workbook  */
    private val _masterCell: EvaluationCell
    private var _booleanValue = false
    private var _cellType = 0
    private var _errorValue = 0
    private var _numberValue = 0.0
    private var _stringValue: String? = null

    init {
        _sheet = sheet
        _masterCell = masterCell
        // start with value blank, but expect construction to be immediately
        setValue(BlankEval.instance) // followed by a proper call to setValue()
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getIdentityKeyProperty")
    override val identityKey: Any?
        get() {
        return _masterCell.identityKey
    }

    fun setValue(value: ValueEval) {
        val cls: Class<out ValueEval?> = value.javaClass

        if (cls == NumberEval::class.java) {
            _cellType = ICell.CELL_TYPE_NUMERIC
            _numberValue = (value as NumberEval).numberValue
            return
        }
        if (cls == StringEval::class.java) {
            _cellType = ICell.CELL_TYPE_STRING
            _stringValue = (value as StringEval).stringValue
            return
        }
        if (cls == BoolEval::class.java) {
            _cellType = ICell.CELL_TYPE_BOOLEAN
            _booleanValue = (value as BoolEval).booleanValue
            return
        }
        if (cls == ErrorEval::class.java) {
            _cellType = ICell.CELL_TYPE_ERROR
            _errorValue = (value as ErrorEval).errorCode
            return
        }
        if (cls == BlankEval::class.java) {
            _cellType = ICell.CELL_TYPE_BLANK
            return
        }
        throw IllegalArgumentException("Unexpected value class (" + cls.getName() + ")")
    }

    fun copyValue(destCell: ICell) {
        when (_cellType) {
            ICell.CELL_TYPE_BLANK -> {
                destCell.setCellType(ICell.CELL_TYPE_BLANK)
                return
            }

            ICell.CELL_TYPE_NUMERIC -> {
                destCell.setCellValue(_numberValue)
                return
            }

            ICell.CELL_TYPE_BOOLEAN -> {
                destCell.setCellValue(_booleanValue)
                return
            }

            ICell.CELL_TYPE_STRING -> {
                destCell.setCellValue(_stringValue)
                return
            }

            ICell.CELL_TYPE_ERROR -> {
                destCell.setCellErrorValue(_errorValue.toByte())
                return
            }
        }
        throw IllegalStateException("Unexpected data type (" + _cellType + ")")
    }

    private fun checkCellType(expectedCellType: Int) {
        if (_cellType != expectedCellType) {
            throw RuntimeException("Wrong data type (" + _cellType + ")")
        }
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getCellTypeProperty")
    override val cellType: Int
        get() {
        return _cellType
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getBooleanCellValueProperty")
    override val booleanCellValue: Boolean
        get() {
        checkCellType(ICell.CELL_TYPE_BOOLEAN)
        return _booleanValue
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getErrorCellValueProperty")
    override val errorCellValue: Int
        get() {
        checkCellType(ICell.CELL_TYPE_ERROR)
        return _errorValue
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNumericCellValueProperty")
    override val numericCellValue: Double
        get() {
        checkCellType(ICell.CELL_TYPE_NUMERIC)
        return _numberValue
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getStringCellValueProperty")
    override val stringCellValue: String?
        get() {
        checkCellType(ICell.CELL_TYPE_STRING)
        return _stringValue
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getSheetProperty")
    override val sheet: EvaluationSheet?
        get() {
        return _sheet
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getRowIndexProperty")
    override val rowIndex: Int
        get() {
        return _masterCell.rowIndex
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getColumnIndexProperty")
    override val columnIndex: Int
        get() {
        return _masterCell.columnIndex
    }
}
