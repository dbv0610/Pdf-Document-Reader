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
package com.wxiwei.office.fc.hssf.record

import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Title:        Write Protect Record<P>
 * Description:  Indicated that the sheet/workbook is write protected.
 * REFERENCE:  PG 425 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @version 3.0-pre
</P> */
class WriteProtectRecord : StandardRecord {
    constructor()

    /**
     * @param in unused (since this record has no data)
     */
    constructor(`in`: RecordInputStream?)

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[WRITEPROTECT]\n")
        buffer.append("[/WRITEPROTECT]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
    }

    override fun getDataSize(): Int {
        return 0
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    companion object {
        const val sid: Short = 0x86
    }
}
