/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
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
 * Title: Object Protect Record<P>
 * Description: Protect embedded object with the lamest "security" ever invented.
 * This record tells  "I want to protect my objects" with lame security.  It
 * appears in conjunction with the PASSWORD and PROTECT records as well as its
 * scenario protect cousin.</P><P>
 * REFERENCE:  PG 368 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
</P> */
class ObjectProtectRecord

    : StandardRecord {
    private var field_1_protect: Short = 0

    constructor()

    constructor(`in`: RecordInputStream) {
        field_1_protect = `in`.readShort()
    }

    /**
     * set whether the sheet is protected or not
     * @param protect whether to protect the sheet or not
     */
    fun setProtect(protect: Boolean) {
        if (protect) {
            field_1_protect = 1
        } else {
            field_1_protect = 0
        }
    }

    /**
     * get whether the sheet is protected or not
     * @return whether to protect the sheet or not
     */
    fun getProtect(): Boolean {
        return (field_1_protect.toInt() == 1)
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[SCENARIOPROTECT]\n")
        buffer.append("    .protect         = ").append(getProtect())
            .append("\n")
        buffer.append("[/SCENARIOPROTECT]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(field_1_protect.toInt())
    }

    override fun getDataSize(): Int {
        return 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = ObjectProtectRecord()
        rec.field_1_protect = field_1_protect
        return rec
    }

    companion object {
        const val sid: Short = 0x63
    }
}
