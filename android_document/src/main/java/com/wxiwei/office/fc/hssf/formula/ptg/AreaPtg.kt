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

import com.wxiwei.office.fc.ss.util.AreaReference
import com.wxiwei.office.fc.util.LittleEndianInput


/**
 * Specifies a rectangular area of cells A1:A4 for instance.
 * @author Jason Height (jheight at chariot dot net dot au)
 */
class AreaPtg : Area2DPtgBase {
    constructor(
        firstRow: Int,
        lastRow: Int,
        firstColumn: Int,
        lastColumn: Int,
        firstRowRelative: Boolean,
        lastRowRelative: Boolean,
        firstColRelative: Boolean,
        lastColRelative: Boolean
    ) : super(
        firstRow,
        lastRow,
        firstColumn,
        lastColumn,
        firstRowRelative,
        lastRowRelative,
        firstColRelative,
        lastColRelative
    )

    constructor(`in`: LittleEndianInput) : super(`in`)
    constructor(arearef: String?) : super(AreaReference(arearef))
    constructor(areaRef: AreaReference) : super(areaRef)

    override val sid: Byte get() {
        return Companion.sid.toByte()
    }

    companion object {
        const val sid: Short = 0x25
    }
}
