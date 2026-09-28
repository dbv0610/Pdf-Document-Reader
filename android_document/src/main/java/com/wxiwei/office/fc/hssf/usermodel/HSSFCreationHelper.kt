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

import com.wxiwei.office.fc.ss.usermodel.CreationHelper

class HSSFCreationHelper internal constructor(private val workbook: HSSFWorkbook) : CreationHelper {
    private val dataFormat: HSSFDataFormat?

    init {
        // Create the things we only ever need one of
        dataFormat = HSSFDataFormat(workbook.getWorkbook())
    }

    override fun createRichTextString(text: String?): HSSFRichTextString {
        return HSSFRichTextString(text)
    }

    override fun createDataFormat(): HSSFDataFormat? {
        return dataFormat
    }

    override fun createHyperlink(type: Int): HSSFHyperlink {
        return HSSFHyperlink(type)
    }

    /**
     * Creates a HSSFFormulaEvaluator, the object that evaluates formula cells.
     * 
     * @return a HSSFFormulaEvaluator instance
     */
    override fun createFormulaEvaluator(): HSSFFormulaEvaluator? {
        //return new HSSFFormulaEvaluator(workbook);
        return null
    }

    /**
     * Creates a HSSFClientAnchor. Use this object to position drawing object in a sheet
     * 
     * @return a HSSFClientAnchor instance
     * @see com.wxiwei.office.fc.ss.usermodel.com.wxiwei.office.fc.ss.usermodel.com.wxiwei.office.fc.ss.usermodel.Drawing
     */
    override fun createClientAnchor(): HSSFClientAnchor {
        return HSSFClientAnchor()
    }
}
