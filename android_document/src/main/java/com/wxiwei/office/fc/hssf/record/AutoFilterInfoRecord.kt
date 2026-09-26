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
 * The AutoFilterInfo record specifies the number of columns that have AutoFilter enabled
 * and indicates the beginning of the collection of AutoFilter records.
 * 
 * @author Yegor Kozlov
 */
class AutoFilterInfoRecord

    : StandardRecord {
    /**
     * get the number of AutoFilter drop-down arrows on the sheet
     * 
     * @return the number of AutoFilter drop-down arrows on the sheet
     */
    /**
     * set the number of AutoFilter drop-down arrows on the sheet
     * 
     * @param num  the number of AutoFilter drop-down arrows on the sheet
     */
    /**
     * Number of AutoFilter drop-down arrows on the sheet
     */
    var numEntries: Short = 0 // = 0;

    constructor()

    constructor(`in`: RecordInputStream) {
        this.numEntries = `in`.readShort()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[AUTOFILTERINFO]\n")
        buffer.append("    .numEntries          = ")
            .append(numEntries.toInt()).append("\n")
        buffer.append("[/AUTOFILTERINFO]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(numEntries.toInt())
    }

    override fun getDataSize(): Int {
        return 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        return cloneViaReserialise()
    }

    companion object {
        const val sid: Short = 0x9D
    }
}
