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

import com.wxiwei.office.fc.util.LittleEndian.putByte
import com.wxiwei.office.fc.util.LittleEndian.putUShort
import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.fc.util.StringUtil.hasMultibyte
import com.wxiwei.office.fc.util.StringUtil.putCompressedUnicode
import com.wxiwei.office.fc.util.StringUtil.putUnicodeLE
import com.wxiwei.office.fc.util.StringUtil.readCompressedUnicode
import com.wxiwei.office.fc.util.StringUtil.readUnicodeLE
import java.util.Arrays

/**
 * Title: Write Access Record (0x005C)
 *
 *
 * 
 * Description: Stores the username of that who owns the spreadsheet generator (on unix the user's
 * login, on Windoze its the name you typed when you installed the thing)
 * 
 * 
 * REFERENCE: PG 424 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)
 * 
 * 
 * 
 * @author Andrew C. Oliver (acoliver at apache dot org)
 */
class WriteAccessRecord : StandardRecord {
    private var field_1_username: String? = null

    constructor() {
        setUsername("")
    }

    constructor(`in`: RecordInputStream) {
//		if (in.remaining() > DATA_SIZE) {
//			throw new RecordFormatException("Expected data size (" + DATA_SIZE + ") but got ("
//					+ in.remaining() + ")");
//		}
        // The string is always 112 characters (padded with spaces), therefore
        // this record can not be continued.

        val nChars = `in`.readUShort()
        val is16BitFlag = `in`.readUByte()
        if (nChars > DATA_SIZE || (is16BitFlag and 0xFE) != 0) {
            // String header looks wrong (probably missing)
            // OOO doc says this is optional anyway.
            // reconstruct data
            val data = ByteArray(3 + `in`.remaining())
            putUShort(data, 0, nChars)
            putByte(data, 2, is16BitFlag)
            `in`.readFully(data, 3, data.size - 3)
            val rawValue = String(data)
            setUsername(rawValue.trim { it <= ' ' })
            return
        }

        val rawText: String?
        if ((is16BitFlag and 0x01) == 0x00) {
            rawText = readCompressedUnicode(`in`, nChars)
        } else {
            rawText = readUnicodeLE(`in`, nChars)
        }
        field_1_username = rawText.trim { it <= ' ' }

        // consume padding
        var padSize = `in`.remaining()
        while (padSize > 0) {
            // in some cases this seems to be garbage (non spaces)
            `in`.readUByte()
            padSize--
        }
    }

    /**
     * set the username for the user that created the report. HSSF uses the
     * logged in user.
     * 
     * @param username of the user who is logged in (probably "tomcat" or "apache")
     */
    fun setUsername(username: String) {
        val is16bit = hasMultibyte(username)
        val encodedByteCount = 3 + username.length * (if (is16bit) 2 else 1)
        val paddingSize: Int = DATA_SIZE - encodedByteCount
        require(paddingSize >= 0) { "Name is too long: " + username }

        field_1_username = username
    }

    /**
     * get the username for the user that created the report. HSSF uses the
     * logged in user. On natively created M$ Excel sheet this would be the name
     * you typed in when you installed it in most cases.
     * 
     * @return username of the user who is logged in (probably "tomcat" or "apache")
     */
    fun getUsername(): String {
        return field_1_username!!
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[WRITEACCESS]\n")
        buffer.append("    .name = ").append(field_1_username.toString()).append("\n")
        buffer.append("[/WRITEACCESS]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        val username = getUsername()
        val is16bit = hasMultibyte(username)

        out.writeShort(username.length)
        out.writeByte(if (is16bit) 0x01 else 0x00)
        if (is16bit) {
            putUnicodeLE(username, out)
        } else {
            putCompressedUnicode(username, out)
        }
        val encodedByteCount = 3 + username.length * (if (is16bit) 2 else 1)
        val paddingSize: Int = DATA_SIZE - encodedByteCount
        out.write(PADDING, 0, paddingSize)
    }

    override fun getDataSize(): Int {
        return DATA_SIZE
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    companion object {
        const val sid: Short = 0x005C

        private val PAD_CHAR = ' '.code.toByte()
        private const val DATA_SIZE = 112

        /** this record is always padded to a constant length  */
        private val PADDING = ByteArray(DATA_SIZE)

        init {
            Arrays.fill(PADDING, PAD_CHAR)
        }
    }
}
