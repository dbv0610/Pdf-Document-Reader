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
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * SERIESLIST (0x1016)
 *
 *
 * 
 * The series list record defines the series displayed as an overlay to the main chart record.<br></br>
 * 
 * (As with all chart related records, documentation is lacking.
 * See [ChartRecord] for more details)
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class SeriesListRecord : StandardRecord {
    /**
     * Get the series numbers field for the SeriesList record.
     */
    val seriesNumbers: ShortArray

    constructor(seriesNumbers: ShortArray) {
        this.seriesNumbers = seriesNumbers
    }

    constructor(`in`: RecordInputStream) {
        val nItems = `in`.readUShort()
        val ss = ShortArray(nItems)
        for (i in 0..<nItems) {
            ss[i] = `in`.readShort()
        }
        this.seriesNumbers = ss
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[SERIESLIST]\n")
        buffer.append("    .seriesNumbers= ").append(" (").append(this.seriesNumbers).append(" )")
        buffer.append("\n")

        buffer.append("[/SERIESLIST]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        val nItems = seriesNumbers.size
        out.writeShort(nItems)
        for (i in 0..<nItems) {
            out.writeShort(this.seriesNumbers[i].toInt())
        }
    }

    override fun getDataSize(): Int {
        return seriesNumbers.size * 2 + 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        return SeriesListRecord(seriesNumbers.clone())
    }

    companion object {
        const val sid: Short = 0x1016
    }
}
