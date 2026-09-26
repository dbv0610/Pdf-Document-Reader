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

import com.wxiwei.office.fc.hssf.formula.ptg.NamePtg
import com.wxiwei.office.fc.hssf.formula.ptg.NameXPtg
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.formula.udf.UDFFinder


/**
 * Abstracts a workbook for the purpose of formula evaluation.<br></br>
 * 
 * For POI internal use only
 * 
 * @author Josh Micich
 */
interface EvaluationWorkbook {
    fun getSheetName(sheetIndex: Int): String?

    /**
     * @return -1 if the specified sheet is from a different book
     */
    fun getSheetIndex(sheet: EvaluationSheet?): Int

    /**
     * Finds a sheet index by case insensitive name.
     * @return the index of the sheet matching the specified name.  -1 if not found
     */
    fun getSheetIndex(sheetName: String?): Int

    fun getSheet(sheetIndex: Int): EvaluationSheet?

    /**
     * @return `null` if externSheetIndex refers to a sheet inside the current workbook
     */
    fun getExternalSheet(externSheetIndex: Int): ExternalSheet?
    fun convertFromExternSheetIndex(externSheetIndex: Int): Int
    fun getExternalName(externSheetIndex: Int, externNameIndex: Int): ExternalName?
    fun getName(namePtg: NamePtg?): EvaluationName?
    fun getName(name: String?, sheetIndex: Int): EvaluationName?
    fun resolveNameXText(ptg: NameXPtg?): String?
    fun getFormulaTokens(cell: EvaluationCell?): Array<Ptg?>?
    val uDFFinder: UDFFinder?

    class ExternalSheet(val workbookName: String?, val sheetName: String?)
    class ExternalName(val name: String?, val number: Int, val ix: Int)
}
