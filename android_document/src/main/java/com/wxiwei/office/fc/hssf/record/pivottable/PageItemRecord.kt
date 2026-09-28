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
package com.wxiwei.office.fc.hssf.record.pivottable

import com.wxiwei.office.fc.hssf.record.RecordFormatException
import com.wxiwei.office.fc.hssf.record.RecordInputStream
import com.wxiwei.office.fc.hssf.record.StandardRecord
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * SXPI - Page Item (0x00B6)<br></br>
 * 
 * @author Patrick Cheng
 */
class PageItemRecord(`in`: RecordInputStream) : StandardRecord() {
    private class FieldInfo(`in`: RecordInputStream) {
        /** Index to the View Item SXVI(0x00B2) record  */
        private val _isxvi: Int

        /** Index to the [ViewFieldsRecord] SXVD(0x00B1) record  */
        private val _isxvd: Int

        /** Object ID for the drop-down arrow  */
        private val _idObj: Int

        init {
            _isxvi = `in`.readShort().toInt()
            _isxvd = `in`.readShort().toInt()
            _idObj = `in`.readShort().toInt()
        }

        fun serialize(out: LittleEndianOutput) {
            out.writeShort(_isxvi)
            out.writeShort(_isxvd)
            out.writeShort(_idObj)
        }

        fun appendDebugInfo(sb: StringBuffer) {
            sb.append('(')
            sb.append("isxvi=").append(shortToHex(_isxvi))
            sb.append(" isxvd=").append(shortToHex(_isxvd))
            sb.append(" idObj=").append(shortToHex(_idObj))
            sb.append(')')
        }

        companion object {
            const val ENCODED_SIZE: Int = 6
        }
    }

    private val _fieldInfos: Array<FieldInfo?>

    init {
        val dataSize = `in`.remaining()
        if (dataSize % FieldInfo.Companion.ENCODED_SIZE != 0) {
            throw RecordFormatException("Bad data size " + dataSize)
        }

        val nItems: Int = dataSize / FieldInfo.Companion.ENCODED_SIZE

        val fis = arrayOfNulls<FieldInfo>(nItems)
        for (i in fis.indices) {
            fis[i] = FieldInfo(`in`)
        }
        _fieldInfos = fis
    }

    override fun serialize(out: LittleEndianOutput) {
        for (i in _fieldInfos.indices) {
            _fieldInfos[i]!!.serialize(out)
        }
    }

    override fun getDataSize(): Int {
        return _fieldInfos.size * FieldInfo.Companion.ENCODED_SIZE
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun toString(): String {
        val sb = StringBuffer()

        sb.append("[SXPI]\n")
        for (i in _fieldInfos.indices) {
            sb.append("    item[").append(i).append("]=")
            _fieldInfos[i]!!.appendDebugInfo(sb)
            sb.append('\n')
        }
        sb.append("[/SXPI]\n")
        return sb.toString()
    }

    companion object {
        const val sid: Short = 0x00B6
    }
}
