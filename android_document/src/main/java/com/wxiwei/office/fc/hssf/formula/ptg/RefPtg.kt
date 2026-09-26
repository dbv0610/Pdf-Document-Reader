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
package com.wxiwei.office.fc.hssf.formula.ptg

import com.wxiwei.office.fc.ss.util.CellReference
import com.wxiwei.office.fc.util.LittleEndianInput


/**
 * ReferencePtg - handles references (such as A1, A2, IA4)
 * @author  Andrew C. Oliver (acoliver@apache.org)
 * @author Jason Height (jheight at chariot dot net dot au)
 */
class RefPtg : Ref2DPtgBase {
    /**
     * Takes in a String representation of a cell reference and fills out the
     * numeric fields.
     */
    constructor(cellref: String) : super(CellReference(cellref))

    constructor(row: Int, column: Int, isRowRelative: Boolean, isColumnRelative: Boolean) : super(
        row,
        column,
        isRowRelative,
        isColumnRelative
    )

    constructor(`in`: LittleEndianInput) : super(`in`)

    constructor(cr: CellReference) : super(cr)

    override val sid: Byte get() {
        return Companion.sid
    }

    companion object {
        const val sid: Byte = 0x24
    }
}
