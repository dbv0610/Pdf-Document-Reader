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
package com.wxiwei.office.fc.hssf.model

import com.wxiwei.office.fc.ddf.EscherChildAnchorRecord
import com.wxiwei.office.fc.ddf.EscherClientAnchorRecord
import com.wxiwei.office.fc.ddf.EscherRecord
import com.wxiwei.office.fc.hssf.usermodel.HSSFAnchor
import com.wxiwei.office.fc.hssf.usermodel.HSSFChildAnchor
import com.wxiwei.office.fc.hssf.usermodel.HSSFClientAnchor
import kotlin.math.max
import kotlin.math.min


/**
 * 
 */
object ConvertAnchor {
    @JvmStatic
    fun createAnchor(userAnchor: HSSFAnchor?): EscherRecord {
        if (userAnchor is HSSFClientAnchor) {
            val a = userAnchor

            val anchor = EscherClientAnchorRecord()
            anchor.recordId = EscherClientAnchorRecord.RECORD_ID
            anchor.options = 0x0000.toShort()
            anchor.flag = a.anchorType.toShort()
            anchor.col1 = min(a.col1.toInt(), a.col2.toInt()).toShort()
            anchor.dx1 = a.dx1.toShort()
            anchor.row1 = min(a.row1, a.row2).toShort()
            anchor.dy1 = a.dy1.toShort()

            anchor.col2 = max(a.col1.toInt(), a.col2.toInt()).toShort()
            anchor.dx2 = a.dx2.toShort()
            anchor.row2 = max(a.row1, a.row2).toShort()
            anchor.dy2 = a.dy2.toShort()
            return anchor
        }
        val a = userAnchor as HSSFChildAnchor
        val anchor = EscherChildAnchorRecord()
        anchor.recordId = EscherChildAnchorRecord.RECORD_ID
        anchor.options = 0x0000.toShort()
        anchor.dx1 = min(a.dx1, a.dx2)
        anchor.dy1 = min(a.dy1, a.dy2)
        anchor.dx2 = max(a.dx2, a.dx1)
        anchor.dy2 = max(a.dy2, a.dy1)
        return anchor
    }
}
