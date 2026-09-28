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

import com.wxiwei.office.fc.ss.usermodel.ErrorConstants
import com.wxiwei.office.fc.util.HexDump.byteToHex
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * Creates new BoolErrRecord. (0x0205) <P>
 * REFERENCE:  PG ??? Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Michael P. Harhen
 * @author Jason Height (jheight at chariot dot net dot au)
</P> */
class BoolErrRecord : CellRecord {
    private var _value = 0
    /**
     * Indicates whether the call holds an error value
     * 
     * @return boolean true if the cell holds an error value
     */
    /**
     * If `true`, this record represents an error cell value, otherwise this record represents a boolean cell value
     */
    var isError: Boolean = false
        private set

    /** Creates new BoolErrRecord  */
    constructor()

    /**
     * @param in the RecordInputstream to read the record from
     */
    constructor(`in`: RecordInputStream) : super(`in`) {
        when (`in`.remaining()) {
            2 -> _value = `in`.readByte().toInt()
            3 -> _value = `in`.readUShort()
            else -> throw RecordFormatException(
                ("Unexpected size ("
                        + `in`.remaining() + ") for BOOLERR record.")
            )
        }
        val flag = `in`.readUByte()
        when (flag) {
            0 -> this.isError = false
            1 -> this.isError = true
            else -> throw RecordFormatException(
                ("Unexpected isError flag ("
                        + flag + ") for BOOLERR record.")
            )
        }
    }

    /**
     * set the boolean value for the cell
     * 
     * @param value   representing the boolean value
     */
    fun setValue(value: Boolean) {
        _value = if (value) 1 else 0
        this.isError = false
    }

    /**
     * set the error value for the cell
     * 
     * @param value     error representing the error value
     * this value can only be 0,7,15,23,29,36 or 42
     * see bugzilla bug 16560 for an explanation
     */
    fun setValue(value: Byte) {
        when (value.toInt()) {
            ErrorConstants.ERROR_NULL, ErrorConstants.ERROR_DIV_0, ErrorConstants.ERROR_VALUE, ErrorConstants.ERROR_REF, ErrorConstants.ERROR_NAME, ErrorConstants.ERROR_NUM, ErrorConstants.ERROR_NA -> {
                _value = value.toInt()
                this.isError = true
                return
            }
        }
        throw IllegalArgumentException("Error Value can only be 0,7,15,23,29,36 or 42. It cannot be " + value)
    }

    val booleanValue: Boolean
        /**
         * get the value for the cell
         * 
         * @return boolean representing the boolean value
         */
        get() = _value != 0

    val errorValue: Byte
        /**
         * get the error value for the cell
         * 
         * @return byte representing the error value
         */
        get() = _value.toByte()

    val isBoolean: Boolean
        /**
         * Indicates whether the call holds a boolean value
         * 
         * @return boolean true if the cell holds a boolean value
         */
        get() = !this.isError

    override fun getRecordName(): String {
        return "BOOLERR"
    }

    override fun appendValueText(sb: StringBuilder) {
        if (this.isBoolean) {
            sb.append("  .boolVal = ")
            sb.append(this.booleanValue)
        } else {
            sb.append("  .errCode = ")
            sb.append(ErrorConstants.getText(this.errorValue.toInt()))
            sb.append(" (").append(byteToHex(this.errorValue.toInt())).append(")")
        }
    }

    override fun serializeValue(out: LittleEndianOutput) {
        out.writeByte(_value)
        out.writeByte(if (this.isError) 1 else 0)
    }

    override fun getValueDataSize(): Int {
        return 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = BoolErrRecord()
        copyBaseFields(rec)
        rec._value = _value
        rec.isError = this.isError
        return rec
    }

    companion object {
        const val sid: Short = 0x0205
    }
}
