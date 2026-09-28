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
import com.wxiwei.office.fc.util.StringUtil.putCompressedUnicode

/**
 * Title:        FILESHARING (0x005B) 
 *
 *
 * Description:  stores the encrypted readonly for a workbook (write protect)
 * This functionality is accessed from the options dialog box available when performing 'Save As'.
 *
 *
 * REFERENCE:  PG 314 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)
 *
 *
 * @author Andrew C. Oliver (acoliver at apache dot org)
 */
class FileSharingRecord : StandardRecord {
    private var field_1_readonly: Short = 0
    private var field_2_password: Short = 0
    private var field_3_username_unicode_options: Byte = 0
    private var field_3_username_value: String? = null

    constructor()

    constructor(`in`: RecordInputStream) {
        field_1_readonly = `in`.readShort()
        field_2_password = `in`.readShort()

        val nameLen = `in`.readShort().toInt()

        if (nameLen > 0) {
            // TODO - Current examples(3) from junits only have zero length username. 
            field_3_username_unicode_options = `in`.readByte()
            field_3_username_value = `in`.readCompressedUnicode(nameLen)
        } else {
            field_3_username_value = ""
        }
    }

    /**
     * set the readonly flag
     * 
     * @param readonly 1 for true, not 1 for false
     */
    fun setReadOnly(readonly: Short) {
        field_1_readonly = readonly
    }

    /**
     * get the readonly
     * 
     * @return short  representing if this is read only (1 = true)
     */
    fun getReadOnly(): Short {
        return field_1_readonly
    }

    /**
     * @param password hashed password
     */
    fun setPassword(password: Short) {
        field_2_password = password
    }

    /**
     * @return password hashed with hashPassword() (very lame)
     */
    fun getPassword(): Short {
        return field_2_password
    }


    /**
     * @return username of the user that created the file
     */
    fun getUsername(): String {
        return field_3_username_value!!
    }

    /**
     * @param username of the user that created the file
     */
    fun setUsername(username: String) {
        field_3_username_value = username
    }


    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[FILESHARING]\n")
        buffer.append("    .readonly       = ")
            .append(if (getReadOnly().toInt() == 1) "true" else "false").append("\n")
        buffer.append("    .password       = ")
            .append(Integer.toHexString(getPassword().toInt())).append("\n")
        buffer.append("    .username       = ")
            .append(getUsername()).append("\n")
        buffer.append("[/FILESHARING]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        // TODO - junit
        out.writeShort(getReadOnly().toInt())
        out.writeShort(getPassword().toInt())
        out.writeShort(field_3_username_value!!.length)
        if (field_3_username_value!!.length > 0) {
            out.writeByte(field_3_username_unicode_options.toInt())
            putCompressedUnicode(getUsername(), out)
        }
    }

    override fun getDataSize(): Int {
        val nameLen = field_3_username_value!!.length
        if (nameLen < 1) {
            return 6
        }
        return 7 + nameLen
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    /**
     * Clone this record.
     */
    override fun clone(): Any {
        val clone = FileSharingRecord()
        clone.setReadOnly(field_1_readonly)
        clone.setPassword(field_2_password)
        clone.setUsername(field_3_username_value!!)
        return clone
    }

    companion object {
        const val sid: Short = 0x005B

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
