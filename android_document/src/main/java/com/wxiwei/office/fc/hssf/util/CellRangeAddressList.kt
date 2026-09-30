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
package com.wxiwei.office.fc.hssf.util

import com.wxiwei.office.fc.hssf.record.RecordInputStream
import com.wxiwei.office.fc.ss.util.CellRangeAddressList

/**
 * Implementation of the cell range address lists,like is described
 * in OpenOffice.org's Excel Documentation: excelfileformat.pdf sec 2.5.14 -
 * 'Cell Range Address List'
 * 
 * In BIFF8 there is a common way to store absolute cell range address lists in
 * several records (not formulas). A cell range address list consists of a field
 * with the number of ranges and the list of the range addresses. Each cell
 * range address (called an ADDR structure) contains 4 16-bit-values.
 * 
 * 
 * @author Dragos Buleandra (dragos.buleandra@trade2b.ro)
 */
@Deprecated(
    """use {@link com.wxiwei.office.fc.ss.util.CellRangeAddressList}
  
  """
)
class CellRangeAddressList : CellRangeAddressList {
    constructor(firstRow: Int, lastRow: Int, firstCol: Int, lastCol: Int) : super(
        firstRow,
        lastRow,
        firstCol,
        lastCol
    )

    constructor() : super()

    /**
     * @param in the RecordInputstream to read the record from
     */
    constructor(`in`: RecordInputStream) : super() {
        val nItems = `in`.readUShort()

        for (k in 0..<nItems) {
            _list.add(CellRangeAddress(`in`))
        }
    }
}
