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
 * End Of File record.
 * <P>
 * Description:  Marks the end of records belonging to a particular object in the
 * HSSF File</P><P>
 * REFERENCE:  PG 307 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
</P> */
class EOFRecord : StandardRecord {
    private constructor()

    /**
     * @param in unused (since this record has no data)
     */
    constructor(`in`: RecordInputStream?)

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[EOF]\n")
        buffer.append("[/EOF]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
    }

    override fun getDataSize(): Int {
        return ENCODED_SIZE - 4
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        return instance
    }

    companion object {
        const val sid: Short = 0x0A
        const val ENCODED_SIZE: Int = 4

        @JvmField
        val instance: EOFRecord = EOFRecord()
    }
}
