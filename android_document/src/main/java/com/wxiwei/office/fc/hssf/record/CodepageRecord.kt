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
 * Title: Codepage Record<P>
 * Description:  the default characterset. for the workbook</P><P>
 * REFERENCE:  PG 293 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
</P> */
class CodepageRecord

    : StandardRecord {
    /**
     * get the codepage for this workbook
     * 
     * @see .CODEPAGE
     * 
     * @return codepage - the codepage to set
     */
    /**
     * set the codepage for this workbook
     * 
     * @see .CODEPAGE
     * 
     * @param cp the codepage to set
     */
    var codepage: Short = 0 // = 0;

    constructor()

    constructor(`in`: RecordInputStream) {
        this.codepage = `in`.readShort()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[CODEPAGE]\n")
        buffer.append("    .codepage        = ")
            .append(Integer.toHexString(this.codepage.toInt())).append("\n")
        buffer.append("[/CODEPAGE]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(this.codepage.toInt())
    }

    override fun getDataSize(): Int {
        return 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    companion object {
        const val sid: Short = 0x42

        /**
         * the likely correct value for CODEPAGE (at least for US versions).  We could use
         * some help with international versions (which we do not have access to documentation
         * for)
         */
        val CODEPAGE: Short = 0x4b0.toShort()
    }
}
