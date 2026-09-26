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
/*
 * HSSF Chart Title Format Record Type
 */
package com.wxiwei.office.fc.hssf.record.chart

import com.wxiwei.office.fc.hssf.record.RecordInputStream
import com.wxiwei.office.fc.hssf.record.StandardRecord
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * CHARTTITLEFORMAT (0x1050)
 *
 *
 * Describes the formatting runs associated with a chart title.
 */
class ChartTitleFormatRecord(`in`: RecordInputStream) : StandardRecord() {
    private val _formats: Array<CTFormat?>

    private class CTFormat {
        var offset: Int
        val fontIndex: Int

        protected constructor(offset: Short, fontIdx: Short) {
            this.offset = offset.toInt()
            this.fontIndex = fontIdx.toInt()
        }

        constructor(`in`: RecordInputStream) {
            this.offset = `in`.readShort().toInt()
            this.fontIndex = `in`.readShort().toInt()
        }

        fun serialize(out: LittleEndianOutput) {
            out.writeShort(this.offset)
            out.writeShort(this.fontIndex)
        }

        companion object {
            const val ENCODED_SIZE: Int = 4
        }
    }


    init {
        val nRecs = `in`.readUShort()
        _formats = arrayOfNulls<CTFormat>(nRecs)

        for (i in 0..<nRecs) {
            _formats[i] = CTFormat(`in`)
        }
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(_formats.size)
        for (i in _formats.indices) {
            _formats[i]?.serialize(out)
        }
    }

    override fun getDataSize(): Int {
        return 2 + CTFormat.ENCODED_SIZE * _formats.size
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    val formatCount: Int
        get() = _formats.size

    fun modifyFormatRun(oldPos: Short, newLen: Short) {
        var shift = 0
        for (i in _formats.indices) {
            val ctf = _formats[i] ?: continue
            if (shift != 0) {
                ctf.offset = ctf.offset + shift
            } else if (oldPos.toInt() == ctf.offset && i < _formats.size - 1) {
                val nextCTF = _formats[i + 1]
                if (nextCTF != null) {
                    shift = newLen - (nextCTF.offset - ctf.offset)
                }
            }
        }
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[CHARTTITLEFORMAT]\n")
        buffer.append("    .format_runs       = ").append(_formats.size).append("\n")
        for (i in _formats.indices) {
            val ctf = _formats[i]
            buffer.append("       .char_offset= ").append(ctf?.offset)
            buffer.append(",.fontidx= ").append(ctf?.fontIndex)
            buffer.append("\n")
        }
        buffer.append("[/CHARTTITLEFORMAT]\n")
        return buffer.toString()
    }

    companion object {
        const val sid: Short = 0x1050
    }
}
