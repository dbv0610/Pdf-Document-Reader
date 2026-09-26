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
 * Title:        Label SST Record<P>
 * Description:  Refers to a string in the shared string table and is a column
 * value.  </P><P>
 * REFERENCE:  PG 325 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
</P> */
class LabelSSTRecord : CellRecord {
    private var field_4_sst_index = 0

    constructor()

    constructor(`in`: RecordInputStream) : super(`in`) {
        field_4_sst_index = `in`.readInt()
    }

    /**
     * set the index to the string in the SSTRecord
     * 
     * @param index - of string in the SST Table
     * @see SSTRecord
     */
    fun setSSTIndex(index: Int) {
        field_4_sst_index = index
    }


    /**
     * get the index to the string in the SSTRecord
     * 
     * @return index of string in the SST Table
     * @see SSTRecord
     */
    fun getSSTIndex(): Int {
        return field_4_sst_index
    }

    var sstIndex: Int get() = getSSTIndex(); set(v) = setSSTIndex(v)

    override fun getRecordName(): String {
        return "LABELSST"
    }

    override fun appendValueText(sb: StringBuilder) {
        sb.append("  .sstIndex = ")
        sb.append(shortToHex(xFIndex.toInt()))
    }

    override fun serializeValue(out: LittleEndianOutput) {
        out.writeInt(getSSTIndex())
    }

    override fun getValueDataSize(): Int {
        return 4
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = LabelSSTRecord()
        copyBaseFields(rec)
        rec.field_4_sst_index = field_4_sst_index
        return rec
    }

    companion object {
        const val sid: Short = 0xfd
    }
}
