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

import com.wxiwei.office.constant.fc.ConstantValueParser.encode
import com.wxiwei.office.constant.fc.ConstantValueParser.getEncodedSize
import com.wxiwei.office.constant.fc.ConstantValueParser.parse
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * Title:       CRN(0x005A) 
 *
 *
 * Description: This record stores the contents of an external cell or cell range 
 *
 *
 * REFERENCE:   OOO 5.23
 *
 *
 * 
 * @author josh micich
 */
class CRNRecord : StandardRecord {
    var numberOfCRNs: Int = 0
        private set
    private var field_2_first_column_index = 0
    private var field_3_row_index = 0
    private var field_4_constant_values: Array<Any?> = emptyArray()

    constructor() {
        throw RuntimeException("incomplete code")
    }


    constructor(`in`: RecordInputStream) {
        this.numberOfCRNs = `in`.readUByte()
        field_2_first_column_index = `in`.readUByte()
        field_3_row_index = `in`.readShort().toInt()
        val nValues = this.numberOfCRNs - field_2_first_column_index + 1
        field_4_constant_values = parse(`in`, nValues)
    }


    override fun toString(): String {
        val sb = StringBuffer()
        sb.append(javaClass.getName()).append(" [CRN")
        sb.append(" rowIx=").append(field_3_row_index)
        sb.append(" firstColIx=").append(field_2_first_column_index)
        sb.append(" lastColIx=").append(this.numberOfCRNs)
        sb.append("]")
        return sb.toString()
    }

    override fun getDataSize(): Int {
        return 4 + getEncodedSize(field_4_constant_values)
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeByte(this.numberOfCRNs)
        out.writeByte(field_2_first_column_index)
        out.writeShort(field_3_row_index)
        encode(out, field_4_constant_values)
    }

    /**
     * return the non static version of the id for this record.
     */
    override fun getSid(): Short {
        return Companion.sid
    }

    companion object {
        const val sid: Short = 0x005A
    }
}
