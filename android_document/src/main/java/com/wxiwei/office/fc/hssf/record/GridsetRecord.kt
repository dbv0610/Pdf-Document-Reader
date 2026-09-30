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
 * Title:        Gridset Record.<P>
 * Description:  flag denoting whether the user specified that gridlines are used when
 * printing.</P><P>
 * REFERENCE:  PG 320 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * 
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author  Glen Stampoultzis (glens at apache.org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * 
 * @version 2.0-pre
</P> */
class GridsetRecord

    : StandardRecord {
    var field_1_gridset_flag: Short = 0

    constructor()

    constructor(`in`: RecordInputStream) {
        field_1_gridset_flag = `in`.readShort()
    }

    /**
     * set whether gridlines are visible when printing
     * 
     * @param gridset - **true** if no gridlines are print, **false** if gridlines are not print.
     */
    fun setGridset(gridset: Boolean) {
        if (gridset == true) {
            field_1_gridset_flag = 1
        } else {
            field_1_gridset_flag = 0
        }
    }

    /**
     * get whether the gridlines are shown during printing.
     * 
     * @return gridset - true if gridlines are NOT printed, false if they are.
     */
    fun getGridset(): Boolean {
        return (field_1_gridset_flag.toInt() == 1)
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[GRIDSET]\n")
        buffer.append("    .gridset        = ").append(getGridset())
            .append("\n")
        buffer.append("[/GRIDSET]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(field_1_gridset_flag.toInt())
    }

    override fun getDataSize(): Int {
        return 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = GridsetRecord()
        rec.field_1_gridset_flag = field_1_gridset_flag
        return rec
    }

    companion object {
        const val sid: Short = 0x82
    }
}
