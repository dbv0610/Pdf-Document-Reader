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
 * Title: Window Protect Record (0x0019) 
 *
 *
 * Description:  flags whether workbook windows are protected
 *
 *
 * REFERENCE:  PG 424 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)
 *
 *
 * @author Andrew C. Oliver (acoliver at apache dot org)
 */
class WindowProtectRecord(options: Int) : StandardRecord() {
    private var _options: Int

    init {
        _options = options
    }

    constructor(`in`: RecordInputStream) : this(`in`.readUShort())

    constructor(protect: Boolean) : this(0) {
        setProtect(protect)
    }

    /**
     * set whether this window should be protected or not
     * @param protect or not
     */
    fun setProtect(protect: Boolean) {
        _options = settingsProtectedFlag.setBoolean(_options, protect)
    }

    /**
     * is this window protected or not
     * 
     * @return protected or not
     */
    fun getProtect(): Boolean {
        return settingsProtectedFlag.isSet(_options)
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[WINDOWPROTECT]\n")
        buffer.append("    .options = ").append(shortToHex(_options)).append("\n")
        buffer.append("[/WINDOWPROTECT]\n")
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
        return WindowProtectRecord(_options)
    }

    companion object {
        const val sid: Short = 0x0019

        private val settingsProtectedFlag = getInstance(0x0001)
    }
}
