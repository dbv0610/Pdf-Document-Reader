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

import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * ftEnd (0x0000)
 *
 *
 * 
 * The end data record is used to denote the end of the subrecords.
 *
 *
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class EndSubRecord : SubRecord {
    constructor()

    /**
     * @param in unused (since this record has no data)
     * @param size
     */
    constructor(`in`: LittleEndianInput?, size: Int) {
        if ((size and 0xFF) != ENCODED_SIZE) { // mask out random crap in upper byte
            throw RecordFormatException("Unexpected size (" + size + ")")
        }
    }

    override fun isTerminating(): Boolean {
        return true
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[ftEnd]\n")

        buffer.append("[/ftEnd]\n")
        return buffer.toString()
    }

    override fun serialize(out: LittleEndianOutput) {
        out.writeShort(sid.toInt())
        out.writeShort(ENCODED_SIZE)
    }

    override fun getDataSize(): Int {
        return ENCODED_SIZE
    }

    fun getSid(): Short {
        return sid
    }

    override fun clone(): Any {
        val rec = EndSubRecord()

        return rec
    }

    companion object {
        const val sid: Short =
            0x0000 // Note - zero sid is somewhat unusual (compared to plain Records)
        private const val ENCODED_SIZE = 0
    }
}
