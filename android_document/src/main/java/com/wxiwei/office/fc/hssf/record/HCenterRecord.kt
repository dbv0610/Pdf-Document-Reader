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
 * Title:        HCenter record (0x0083)<P>
 * Description:  whether to center between horizontal margins</P><P>
 * REFERENCE:  PG 320 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
</P> */
class HCenterRecord : StandardRecord {
    private var field_1_hcenter: Short = 0

    constructor()

    constructor(`in`: RecordInputStream) {
        field_1_hcenter = `in`.readShort()
    }

    /**
     * set whether or not to horizonatally center this sheet.
     * @param hc  center - t/f
     */
    fun setHCenter(hc: Boolean) {
        if (hc == true) {
            field_1_hcenter = 1
        } else {
            field_1_hcenter = 0
        }
    }

    /**
     * get whether or not to horizonatally center this sheet.
     * @return center - t/f
     */
    fun getHCenter(): Boolean {
        return (field_1_hcenter.toInt() == 1)
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[HCENTER]\n")
        buffer.append("    .hcenter        = ").append(getHCenter())
            .append("\n")
        buffer.append("[/HCENTER]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(field_1_hcenter.toInt())
    }

    override fun getDataSize(): Int {
        return 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = HCenterRecord()
        rec.field_1_hcenter = field_1_hcenter
        return rec
    }

    companion object {
        const val sid: Short = 0x0083
    }
}
