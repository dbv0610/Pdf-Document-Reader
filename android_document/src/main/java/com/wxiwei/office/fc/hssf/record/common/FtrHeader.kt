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
package com.wxiwei.office.fc.hssf.record.common

import com.wxiwei.office.fc.hssf.record.RecordInputStream
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * Title: FtrHeader (Future Record Header) common record part
 * <P>
 * This record part specifies a header for a Ftr (Future)
 * style record, which includes extra attributes above and
 * beyond those of a traditional record.
</P> */
class FtrHeader {
    /** This MUST match the type on the containing record  */
    @JvmField
    var recordType: Short = 0

    /** This is a FrtFlags  */
    var grbitFrt: Short = 0

    /** MUST be 8 bytes and all zero  */
    var reserved: ByteArray

    constructor() {
        reserved = ByteArray(8)
    }

    constructor(`in`: RecordInputStream) {
        recordType = `in`.readShort()
        grbitFrt = `in`.readShort()

        reserved = ByteArray(8)
        `in`.read(reserved, 0, 8)
    }

    override fun toString(): String {
        val buffer = StringBuffer()
        buffer.append(" [FUTURE HEADER]\n")
        buffer.append("   Type " + recordType)
        buffer.append("   Flags " + grbitFrt)
        buffer.append(" [/FUTURE HEADER]\n")
        return buffer.toString()
    }

    fun serialize(out: LittleEndianOutput) {
        out.writeShort(recordType.toInt())
        out.writeShort(grbitFrt.toInt())
        out.write(reserved)
    }

    companion object {
        val dataSize: Int
            get() = 12
    }
}
