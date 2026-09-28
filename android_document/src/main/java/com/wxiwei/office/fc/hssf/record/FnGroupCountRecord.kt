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
 * Title: Function Group Count Record<P>
 * Description:  Number of built in function groups in the current version of the
 * Spreadsheet (probably only used on Windoze)</P><P>
 * REFERENCE:  PG 315 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
</P> */
class FnGroupCountRecord

    : StandardRecord {
    private var field_1_count: Short = 0

    constructor()

    constructor(`in`: RecordInputStream) {
        field_1_count = `in`.readShort()
    }

    /**
     * set the number of built-in functions
     * 
     * @param count - number of functions
     */
    fun setCount(count: Short) {
        field_1_count = count
    }

    /**
     * get the number of built-in functions
     * 
     * @return number of built-in functions
     */
    fun getCount(): Short {
        return field_1_count
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[FNGROUPCOUNT]\n")
        buffer.append("    .count            = ").append(getCount().toInt())
            .append("\n")
        buffer.append("[/FNGROUPCOUNT]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(getCount().toInt())
    }

    override fun getDataSize(): Int {
        return 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    companion object {
        const val sid: Short = 0x9c

        /**
         * suggested default (14 dec)
         */
        const val COUNT: Short = 14
    }
}
