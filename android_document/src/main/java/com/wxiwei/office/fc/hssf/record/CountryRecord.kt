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
 * Title:        Country Record (aka WIN.INI country)<P>
 * Description:  used for localization.  Currently HSSF always sets this to 1
 * and it seems to work fine even in Germany.  (es geht's auch fuer Deutschland)</P><P>
 * 
 * REFERENCE:  PG 298 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
</P> */
class CountryRecord

    : StandardRecord {
    /**
     * gets the default country
     * 
     * @return country ID (1 = US)
     */
    /**
     * sets the default country
     * 
     * @param country ID to set (1 = US)
     */
    // 1 for US
    var defaultCountry: Short = 0
    /**
     * gets the current country
     * 
     * @return country ID (1 = US)
     */
    /**
     * sets the current country
     * 
     * @param country ID to set (1 = US)
     */
    var currentCountry: Short = 0

    constructor()

    constructor(`in`: RecordInputStream) {
        this.defaultCountry = `in`.readShort()
        this.currentCountry = `in`.readShort()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[COUNTRY]\n")
        buffer.append("    .defaultcountry  = ")
            .append(Integer.toHexString(this.defaultCountry.toInt())).append("\n")
        buffer.append("    .currentcountry  = ")
            .append(Integer.toHexString(this.currentCountry.toInt())).append("\n")
        buffer.append("[/COUNTRY]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(this.defaultCountry.toInt())
        out.writeShort(this.currentCountry.toInt())
    }

    override fun getDataSize(): Int {
        return 4
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    companion object {
        const val sid: Short = 0x8c
    }
}
