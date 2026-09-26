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

import com.wxiwei.office.fc.hssf.formula.ptg.NameXPtg
import com.wxiwei.office.fc.ss.SpreadsheetVersion


/**
 * Abstracts a workbook for the purpose of formula parsing.<br></br>
 * 
 * For POI internal use only
 * 
 * @author Josh Micich
 */
interface FormulaParsingWorkbook {
    /**
     * named range name matching is case insensitive
     */
    fun getName(name: String?, sheetIndex: Int): EvaluationName?

    fun getNameXPtg(name: String?): NameXPtg?

    /**
     * gets the externSheet index for a sheet from this workbook
     */
    fun getExternalSheetIndex(sheetName: String?): Int

    /**
     * gets the externSheet index for a sheet from an external workbook
     * @param workbookName e.g. "Budget.xls"
     * @param sheetName a name of a sheet in that workbook
     */
    fun getExternalSheetIndex(workbookName: String?, sheetName: String?): Int

    /**
     * Returns an enum holding spreadhseet properties specific to an Excel version (
     * max column and row numbers, max arguments to a function, etc.)
     */
    val spreadsheetVersion: SpreadsheetVersion
}
