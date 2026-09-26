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
 * Title:        RefMode Record<P>
 * Description:  Describes which reference mode to use</P><P>
 * REFERENCE:  PG 376 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
</P> */
class RefModeRecord

    : StandardRecord {
    private var field_1_mode: Short = 0

    constructor()

    constructor(`in`: RecordInputStream) {
        field_1_mode = `in`.readShort()
    }

    /**
     * set the reference mode to use (HSSF uses/assumes A1)
     * @param mode the mode to use
     * @see .USE_A1_MODE
     * 
     * @see .USE_R1C1_MODE
     */
    fun setMode(mode: Short) {
        field_1_mode = mode
    }

    /**
     * get the reference mode to use (HSSF uses/assumes A1)
     * @return mode to use
     * @see .USE_A1_MODE
     * 
     * @see .USE_R1C1_MODE
     */
    fun getMode(): Short {
        return field_1_mode
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[REFMODE]\n")
        buffer.append("    .mode           = ")
            .append(Integer.toHexString(getMode().toInt())).append("\n")
        buffer.append("[/REFMODE]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(getMode().toInt())
    }

    override fun getDataSize(): Int {
        return 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = RefModeRecord()
        rec.field_1_mode = field_1_mode
        return rec
    }

    companion object {
        const val sid: Short = 0xf
        const val USE_A1_MODE: Short = 1
        const val USE_R1C1_MODE: Short = 0
    }
}
