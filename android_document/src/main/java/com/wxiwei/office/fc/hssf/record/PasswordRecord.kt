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

import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Title:        Password Record (0x0013)
 *
 *
 * Description:  stores the encrypted password for a sheet or workbook (HSSF doesn't support encryption)
 * REFERENCE:  PG 371 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)
 *
 *
 * @author Andrew C. Oliver (acoliver at apache dot org)
 */
class PasswordRecord : StandardRecord {
    private var field_1_password: Int // not sure why this is only 2 bytes, but it is... go figure

    constructor(password: Int) {
        field_1_password = password
    }

    constructor(`in`: RecordInputStream) {
        field_1_password = `in`.readShort().toInt()
    }

    /**
     * set the password
     * 
     * @param password  representing the password
     */
    fun setPassword(password: Int) {
        field_1_password = password
    }

    /**
     * get the password
     * 
     * @return short  representing the password
     */
    fun getPassword(): Int {
        return field_1_password
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[PASSWORD]\n")
        buffer.append("    .password = ").append(shortToHex(field_1_password)).append("\n")
        buffer.append("[/PASSWORD]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(field_1_password)
    }

    override fun getDataSize(): Int {
        return 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    /**
     * Clone this record.
     */
    override fun clone(): Any {
        return PasswordRecord(field_1_password)
    }

    companion object {
        const val sid: Short = 0x0013

        //this is the world's lamest "security".  thanks to Wouter van Vugt for making me
        //not have to try real hard.  -ACO
        fun hashPassword(password: String): Short {
            val passwordCharacters = password.toByteArray()
            var hash = 0
            if (passwordCharacters.size > 0) {
                var charIndex = passwordCharacters.size
                while (charIndex-- > 0) {
                    hash = ((hash shr 14) and 0x01) or ((hash shl 1) and 0x7fff)
                    hash = hash xor passwordCharacters[charIndex].toInt()
                }
                // also hash with charcount
                hash = ((hash shr 14) and 0x01) or ((hash shl 1) and 0x7fff)
                hash = hash xor passwordCharacters.size
                hash = hash xor (0x8000 or ('N'.code shl 8) or 'K'.code)
            }
            return hash.toShort()
        }
    }
}
