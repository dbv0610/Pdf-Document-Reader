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
 * Title: Uncalced Record
 * <P>
 * If this record occurs in the Worksheet Substream, it indicates that the formulas have not
 * been recalculated before the document was saved.
 * 
 * @author Olivier Leprince
</P> */
class UncalcedRecord : StandardRecord {
    private val _reserved: Short

    constructor() {
        _reserved = 0
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    constructor(`in`: RecordInputStream) {
        _reserved = `in`.readShort() // unused
    }

    override fun toString(): String {
        val buffer = StringBuffer()
        buffer.append("[UNCALCED]\n")
        buffer.append("    _reserved: ").append(_reserved.toInt()).append('\n')
        buffer.append("[/UNCALCED]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(_reserved.toInt())
    }

    override fun getDataSize(): Int {
        return 2
    }

    companion object {
        const val sid: Short = 0x005E

        fun getStaticRecordSize(): Int {
            return 6
        }
    }
}
