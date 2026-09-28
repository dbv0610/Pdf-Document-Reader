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
 * Title: Sheet Tab Index Array Record (0x013D)
 *
 *
 * Description:  Contains an array of sheet id's.  Sheets always keep their ID
 * regardless of what their name is.
 *
 *
 * REFERENCE:  PG 412 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)
 *
 *
 * @author Andrew C. Oliver (acoliver at apache dot org)
 */
class TabIdRecord : StandardRecord {
    var _tabids: ShortArray

    constructor() {
        _tabids = EMPTY_SHORT_ARRAY
    }

    constructor(`in`: RecordInputStream) {
        val nTabs = `in`.remaining() / 2
        _tabids = ShortArray(nTabs)
        for (i in _tabids.indices) {
            _tabids[i] = `in`.readShort()
        }
    }

    /**
     * set the tab array.  (0,1,2).
     * @param array of tab id's {0,1,2}
     */
    fun setTabIdArray(array: ShortArray) {
        _tabids = array
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[TABID]\n")
        buffer.append("    .elements        = ").append(_tabids.size).append("\n")
        for (i in _tabids.indices) {
            buffer.append("    .element_").append(i).append(" = ").append(_tabids[i].toInt())
                .append("\n")
        }
        buffer.append("[/TABID]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        val tabids = _tabids

        for (i in tabids.indices) {
            out.writeShort(tabids[i].toInt())
        }
    }

    override fun getDataSize(): Int {
        return _tabids.size * 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    companion object {
        const val sid: Short = 0x013D
        private val EMPTY_SHORT_ARRAY = shortArrayOf()
    }
}
