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
 * Title: Protect Record (0x0012) 
 *
 *
 * Description:  defines whether a sheet or workbook is protected (HSSF DOES NOT SUPPORT ENCRYPTION)
 *
 *
 * HSSF now supports the simple "protected" sheets (where they are not encrypted and open office et al
 * ignore the password record entirely).
 * REFERENCE:  PG 373 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)
 *
 *
 * @author Andrew C. Oliver (acoliver at apache dot org)
 */
class ProtectRecord private constructor(options: Int) : StandardRecord() {
    private var _options: Int

    init {
        _options = options
    }

    constructor(isProtected: Boolean) : this(0) {
        setProtect(isProtected)
    }

    constructor(`in`: RecordInputStream) : this(`in`.readShort().toInt())

    /**
     * set whether the sheet is protected or not
     * @param protect whether to protect the sheet or not
     */
    fun setProtect(protect: Boolean) {
        _options = protectFlag.setBoolean(_options, protect)
    }

    /**
     * get whether the sheet is protected or not
     * @return whether to protect the sheet or not
     */
    fun getProtect(): Boolean {
        return protectFlag.isSet(_options)
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[PROTECT]\n")
        buffer.append("    .options = ").append(shortToHex(_options)).append("\n")
        buffer.append("[/PROTECT]\n")
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
        return ProtectRecord(_options)
    }

    companion object {
        const val sid: Short = 0x0012

        private val protectFlag = getInstance(0x0001)
    }
}
