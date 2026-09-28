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
package com.wxiwei.office.fc.hssf.formula

/**
 * Abstracts a cell for the purpose of formula evaluation.  This interface represents both formula
 * and non-formula cells.<br></br>
 * 
 * For POI internal use only
 * 
 * @author Josh Micich
 */
interface EvaluationCell {
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getIdentityKeyProperty")
    val identityKey: Any?
    fun getIdentityKey(): Any? = identityKey

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getSheetProperty")
    val sheet: EvaluationSheet?
    fun getSheet(): EvaluationSheet? = sheet

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getRowIndexProperty")
    val rowIndex: Int
    fun getRowIndex(): Int = rowIndex

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getColumnIndexProperty")
    val columnIndex: Int
    fun getColumnIndex(): Int = columnIndex

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getCellTypeProperty")
    val cellType: Int
    fun getCellType(): Int = cellType

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNumericCellValueProperty")
    val numericCellValue: Double
    fun getNumericCellValue(): Double = numericCellValue

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getStringCellValueProperty")
    val stringCellValue: String?
    fun getStringCellValue(): String? = stringCellValue

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getBooleanCellValueProperty")
    val booleanCellValue: Boolean
    fun getBooleanCellValue(): Boolean = booleanCellValue

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getErrorCellValueProperty")
    val errorCellValue: Int
    fun getErrorCellValue(): Int = errorCellValue
}
