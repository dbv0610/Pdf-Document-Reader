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
package com.wxiwei.office.fc.hssf.usermodel

import com.wxiwei.office.fc.hssf.formula.EvaluationCell
import com.wxiwei.office.fc.hssf.formula.EvaluationSheet
import com.wxiwei.office.ss.model.XLSModel.ACell
import com.wxiwei.office.ss.model.XLSModel.ASheet

/**
 * HSSF wrapper for a cell under evaluation
 * 
 * @author Josh Micich
 */
internal class HSSFEvaluationCell @JvmOverloads constructor(
    @get:JvmName("getACellProperty")
    var aCell: ACell,
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getSheetProperty")
    override var sheet: EvaluationSheet? = HSSFEvaluationSheet(aCell.getSheet() as ASheet?)
) : EvaluationCell {
    fun getACell(): ACell = aCell

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getIdentityKeyProperty")
    override val identityKey: Any
        get() = this.aCell

    fun setHSSFCell(cell: ACell) {
        this.aCell = cell
        if (this.sheet != null) {
            (this.sheet as HSSFEvaluationSheet).setASheet(cell.getSheet() as ASheet?)
        } else {
            this.sheet = HSSFEvaluationSheet(cell.getSheet() as ASheet?)
        }
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getBooleanCellValueProperty")
    override val booleanCellValue: Boolean
        get() = aCell.getBooleanCellValue()
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getCellTypeProperty")
    override val cellType: Int
        get() = aCell.getCellType().toInt()
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getColumnIndexProperty")
    override val columnIndex: Int
        get() = aCell.getColNumber()
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getErrorCellValueProperty")
    override val errorCellValue: Int
        get() = aCell.getErrorCellValue().toInt()
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNumericCellValueProperty")
    override val numericCellValue: Double
        get() = aCell.getNumericCellValue()
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getRowIndexProperty")
    override val rowIndex: Int
        get() = aCell.getRowNumber()
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getStringCellValueProperty")
    override val stringCellValue: String?
        get() = aCell.getStringCellValue()
}
