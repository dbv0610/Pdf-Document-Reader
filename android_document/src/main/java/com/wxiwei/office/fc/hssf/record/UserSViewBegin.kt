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

import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndianOutput
import java.util.Locale


/**
 * The UserSViewBegin record specifies settings for a custom view associated with the sheet.
 * This record also marks the start of custom view records, which save custom view settings.
 * Records between [UserSViewBegin] and [UserSViewEnd] contain settings for the custom view,
 * not settings for the sheet itself.
 * 
 * @author Yegor Kozlov
 */
class UserSViewBegin : StandardRecord {
    private val _rawData: ByteArray

    constructor(data: ByteArray) {
        _rawData = data
    }

    /**
     * construct an UserSViewBegin record.  No fields are interpreted and the record will
     * be serialized in its original form more or less
     * @param in the RecordInputstream to read the record from
     */
    constructor(`in`: RecordInputStream) {
        _rawData = `in`.readRemainder()
    }

    /**
     * spit the record out AS IS. no interpretation or identification
     */
    public override fun serialize(out: LittleEndianOutput) {
        out.write(_rawData)
    }

    override fun getDataSize(): Int {
        return _rawData.size
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    /**
     * @return Globally unique identifier for the custom view
     */
    fun getGuid(): ByteArray {
        val guid = ByteArray(16)
        System.arraycopy(_rawData, 0, guid, 0, guid.size)
        return guid
    }

    override fun toString(): String {
        val sb = StringBuffer()

        sb.append("[").append("USERSVIEWBEGIN").append("] (0x")
        sb.append(Integer.toHexString(Companion.sid.toInt()).uppercase(Locale.getDefault()) + ")\n")
        sb.append("  rawData=").append(toHex(_rawData)).append("\n")
        sb.append("[/").append("USERSVIEWBEGIN").append("]\n")
        return sb.toString()
    }

    //HACK: do a "cheat" clone, see Record.java for more information
    override fun clone(): Any {
        return cloneViaReserialise()
    }

    companion object {
        const val sid: Short = 0x01AA
    }
}
