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
package com.wxiwei.office.fc.hssf.usermodel

import com.wxiwei.office.fc.hssf.formula.EvaluationCell
import com.wxiwei.office.fc.hssf.formula.EvaluationSheet
import com.wxiwei.office.ss.model.XLSModel.ACell
import com.wxiwei.office.ss.model.XLSModel.ARow
import com.wxiwei.office.ss.model.XLSModel.ASheet

/**
 * HSSF wrapper for a sheet under evaluation
 * 
 * @author Josh Micich
 */
/*final*/
internal class HSSFEvaluationSheet(@get:JvmName("getASheetProperty") @set:JvmName("setASheetProperty") var aSheet: ASheet?) : EvaluationSheet {
    fun getASheet(): ASheet? = aSheet
    fun setASheet(sheet: ASheet?) { this.aSheet = sheet }

    override fun getCell(rowIndex: Int, columnIndex: Int): EvaluationCell? {
        if (aSheet == null) return null
        val row = aSheet!!.getRow(rowIndex) as ARow?
        if (row == null) {
            return null
        }
        val cell = row.getCell(columnIndex) as ACell?
        if (cell == null) {
            return null
        }
        return HSSFEvaluationCell(cell, this)
    }
}
