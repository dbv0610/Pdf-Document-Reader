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
 * Title:        VCenter record<P>
 * Description:  tells whether to center the sheet between vertical margins</P><P>
 * REFERENCE:  PG 420 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
</P> */
class VCenterRecord : StandardRecord {
    private var field_1_vcenter = 0

    constructor()

    constructor(`in`: RecordInputStream) {
        field_1_vcenter = `in`.readShort().toInt()
    }

    /**
     * set whether to center vertically or not
     * @param hc  vcenter or not
     */
    fun setVCenter(hc: Boolean) {
        field_1_vcenter = if (hc) 1 else 0
    }

    /**
     * get whether to center vertically or not
     * @return vcenter or not
     */
    fun getVCenter(): Boolean {
        return (field_1_vcenter == 1)
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[VCENTER]\n")
        buffer.append("    .vcenter        = ").append(getVCenter())
            .append("\n")
        buffer.append("[/VCENTER]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(field_1_vcenter)
    }

    override fun getDataSize(): Int {
        return 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = VCenterRecord()
        rec.field_1_vcenter = field_1_vcenter
        return rec
    }

    companion object {
        const val sid: Short = 0x84
    }
}
