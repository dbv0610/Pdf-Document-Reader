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

import com.wxiwei.office.fc.util.BitFieldFactory.Companion.getInstance
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Title:        Refresh All Record (0x01B7) 
 *
 *
 * Description:  Flag whether to refresh all external data when loading a sheet.
 * (which hssf doesn't support anyhow so who really cares?)<P>
 * REFERENCE:  PG 376 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
</P> */
class RefreshAllRecord private constructor(options: Int) : StandardRecord() {
    private var _options: Int

    init {
        _options = options
    }

    constructor(`in`: RecordInputStream) : this(`in`.readUShort())

    constructor(refreshAll: Boolean) : this(0) {
        setRefreshAll(refreshAll)
    }

    /**
     * set whether to refresh all external data when loading a sheet
     * @param refreshAll or not
     */
    fun setRefreshAll(refreshAll: Boolean) {
        _options = refreshFlag.setBoolean(_options, refreshAll)
    }

    /**
     * get whether to refresh all external data when loading a sheet
     * @return refreshall or not
     */
    fun getRefreshAll(): Boolean {
        return refreshFlag.isSet(_options)
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[REFRESHALL]\n")
        buffer.append("    .options      = ").append(shortToHex(_options)).append("\n")
        buffer.append("[/REFRESHALL]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(_options)
    }

    override fun getDataSize(): Int {
        return 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        return RefreshAllRecord(_options)
    }

    companion object {
        const val sid: Short = 0x01B7

        private val refreshFlag = getInstance(0x0001)
    }
}
