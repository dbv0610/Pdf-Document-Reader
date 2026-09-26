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
package com.wxiwei.office.fc.hssf.util

import com.wxiwei.office.fc.ss.util.CellReference

/**
 * Common conversion functions between Excel style A1, C27 style
 * cell references, and POI usermodel style row=0, column=0
 * style references.
 * @author  Avik Sengupta
 * @author  Dennis Doubleday (patch to seperateRowColumns())
 */
class CellReference : CellReference {
    /**
     * Create an cell ref from a string representation.  Sheet names containing special characters should be
     * delimited and escaped as per normal syntax rules for formulas.
     */
    constructor(cellRef: String) : super(cellRef)

    constructor(pRow: Int, pCol: Int) : super(pRow, pCol, true, true)

    constructor(pRow: Int, pCol: Int, pAbsRow: Boolean, pAbsCol: Boolean) : super(
        null,
        pRow,
        pCol,
        pAbsRow,
        pAbsCol
    )

    constructor(
        pSheetName: String?,
        pRow: Int,
        pCol: Int,
        pAbsRow: Boolean,
        pAbsCol: Boolean
    ) : super(pSheetName, pRow, pCol, pAbsRow, pAbsCol)
}
