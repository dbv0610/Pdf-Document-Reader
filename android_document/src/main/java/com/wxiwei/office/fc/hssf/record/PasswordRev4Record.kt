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

import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Title:        Protection Revision 4 password Record (0x01BC) 
 *
 *
 * Description:  Stores the (2 byte??!!) encrypted password for a shared workbook
 *
 *
 * REFERENCE:  PG 374 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)
 *
 *
 * @author Andrew C. Oliver (acoliver at apache dot org)
 */
class PasswordRev4Record : StandardRecord {
    private var field_1_password: Int

    constructor(pw: Int) {
        field_1_password = pw
    }

    constructor(`in`: RecordInputStream) {
        field_1_password = `in`.readShort().toInt()
    }

    /**
     * set the password
     * 
     * @param pw  representing the password
     */
    fun setPassword(pw: Short) {
        field_1_password = pw.toInt()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[PROT4REVPASSWORD]\n")
        buffer.append("    .password = ").append(shortToHex(field_1_password)).append("\n")
        buffer.append("[/PROT4REVPASSWORD]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(field_1_password)
    }

    override fun getDataSize(): Int {
        return 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    companion object {
        const val sid: Short = 0x01BC
    }
}
