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
package com.wxiwei.office.fc.hssf.record.chart

import com.wxiwei.office.fc.hssf.record.RecordInputStream
import com.wxiwei.office.fc.hssf.record.StandardRecord
import com.wxiwei.office.fc.util.HexDump.byteToHex
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * CHARTFRTINFO - Chart Future Record Type Info (0x0850)<br></br>
 * 
 * @author Patrick Cheng
 */
class ChartFRTInfoRecord(`in`: RecordInputStream) : StandardRecord() {
    private val rt: Short
    private val grbitFrt: Short
    private val verOriginator: Byte
    private val verWriter: Byte
    private val rgCFRTID: Array<CFRTID?>

    private class CFRTID(`in`: LittleEndianInput) {
        private val rtFirst: Int
        private val rtLast: Int

        init {
            rtFirst = `in`.readShort().toInt()
            rtLast = `in`.readShort().toInt()
        }

        fun serialize(out: LittleEndianOutput) {
            out.writeShort(rtFirst)
            out.writeShort(rtLast)
        }

        companion object {
            const val ENCODED_SIZE: Int = 4
        }
    }

    init {
        rt = `in`.readShort()
        grbitFrt = `in`.readShort()
        verOriginator = `in`.readByte()
        verWriter = `in`.readByte()
        val cCFRTID = `in`.readShort().toInt()

        rgCFRTID = arrayOfNulls<CFRTID>(cCFRTID)
        for (i in 0..<cCFRTID) {
            rgCFRTID[i] = CFRTID(`in`)
        }
    }

    override fun getDataSize(): Int {
        return 2 + 2 + 1 + 1 + 2 + rgCFRTID.size * CFRTID.ENCODED_SIZE
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(rt.toInt())
        out.writeShort(grbitFrt.toInt())
        out.writeByte(verOriginator.toInt())
        out.writeByte(verWriter.toInt())
        val nCFRTIDs = rgCFRTID.size
        out.writeShort(nCFRTIDs)

        for (i in 0..<nCFRTIDs) {
            rgCFRTID[i]!!.serialize(out)
        }
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[CHARTFRTINFO]\n")
        buffer.append("    .rt           =").append(shortToHex(rt.toInt())).append('\n')
        buffer.append("    .grbitFrt     =").append(shortToHex(grbitFrt.toInt())).append('\n')
        buffer.append("    .verOriginator=").append(byteToHex(verOriginator.toInt())).append('\n')
        buffer.append("    .verWriter    =").append(byteToHex(verOriginator.toInt())).append('\n')
        buffer.append("    .nCFRTIDs     =").append(shortToHex(rgCFRTID.size)).append('\n')
        buffer.append("[/CHARTFRTINFO]\n")
        return buffer.toString()
    }

    companion object {
        const val sid: Short = 0x850
    }
}
