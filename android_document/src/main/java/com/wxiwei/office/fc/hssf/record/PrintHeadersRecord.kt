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
 * Title:        Print Headers Record<P>
 * Description:  Whether or not to print the row/column headers when you
 * enjoy your spreadsheet in the physical form.</P><P>
 * REFERENCE:  PG 373 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
</P> */
class PrintHeadersRecord

    : StandardRecord {
    private var field_1_print_headers: Short = 0

    constructor()

    constructor(`in`: RecordInputStream) {
        field_1_print_headers = `in`.readShort()
    }

    /**
     * set to print the headers - y/n
     * @param p printheaders or not
     */
    fun setPrintHeaders(p: Boolean) {
        if (p == true) {
            field_1_print_headers = 1
        } else {
            field_1_print_headers = 0
        }
    }

    /**
     * get whether to print the headers - y/n
     * @return printheaders or not
     */
    fun getPrintHeaders(): Boolean {
        return (field_1_print_headers.toInt() == 1)
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[PRINTHEADERS]\n")
        buffer.append("    .printheaders   = ").append(getPrintHeaders())
            .append("\n")
        buffer.append("[/PRINTHEADERS]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(field_1_print_headers.toInt())
    }

    override fun getDataSize(): Int {
        return 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = PrintHeadersRecord()
        rec.field_1_print_headers = field_1_print_headers
        return rec
    }

    companion object {
        const val sid: Short = 0x2a
    }
}
