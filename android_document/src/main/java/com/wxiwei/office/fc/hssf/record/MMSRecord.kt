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
 * Title: MMS Record<P>
 * Description: defines how many add menu and del menu options are stored
 * in the file. Should always be set to 0 for HSSF workbooks</P><P>
 * REFERENCE:  PG 328 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
</P> */
class MMSRecord

    : StandardRecord {
    private var field_1_addMenuCount: Byte = 0 // = 0;
    private var field_2_delMenuCount: Byte = 0 // = 0;

    constructor()

    constructor(`in`: RecordInputStream) {
        if (`in`.remaining() == 0) {
            return
        }

        field_1_addMenuCount = `in`.readByte()
        field_2_delMenuCount = `in`.readByte()
    }

    /**
     * set number of add menu options (set to 0)
     * @param am  number of add menu options
     */
    fun setAddMenuCount(am: Byte) {
        field_1_addMenuCount = am
    }

    /**
     * set number of del menu options (set to 0)
     * @param dm  number of del menu options
     */
    fun setDelMenuCount(dm: Byte) {
        field_2_delMenuCount = dm
    }

    /**
     * get number of add menu options (should be 0)
     * @return number of add menu options
     */
    fun getAddMenuCount(): Byte {
        return field_1_addMenuCount
    }

    /**
     * get number of add del options (should be 0)
     * @return number of add menu options
     */
    fun getDelMenuCount(): Byte {
        return field_2_delMenuCount
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[MMS]\n")
        buffer.append("    .addMenu        = ")
            .append(Integer.toHexString(getAddMenuCount().toInt())).append("\n")
        buffer.append("    .delMenu        = ")
            .append(Integer.toHexString(getDelMenuCount().toInt())).append("\n")
        buffer.append("[/MMS]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeByte(getAddMenuCount().toInt())
        out.writeByte(getDelMenuCount().toInt())
    }

    override fun getDataSize(): Int {
        return 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    companion object {
        const val sid: Short = 0xC1
    }
}
