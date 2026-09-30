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

import com.wxiwei.office.fc.ss.util.NumberToTextConverter
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * NUMBER (0x0203) Contains a numeric cell value. <P>
 * REFERENCE:  PG 334 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
</P> */
class NumberRecord : CellRecord {
    private var field_4_value = 0.0

    /** Creates new NumberRecord  */
    constructor()

    /**
     * @param in the RecordInputstream to read the record from
     */
    constructor(`in`: RecordInputStream) : super(`in`) {
        field_4_value = `in`.readDouble()
    }

    /**
     * set the value for the cell
     * 
     * @param value  double representing the value
     */
    fun setValue(value: Double) {
        field_4_value = value
    }

    /**
     * get the value for the cell
     * 
     * @return double representing the value
     */
    fun getValue(): Double {
        return field_4_value
    }

    override fun getRecordName(): String {
        return "NUMBER"
    }

    override fun appendValueText(sb: StringBuilder) {
        sb.append("  .value= ").append(NumberToTextConverter.toText(field_4_value))
    }

    override fun serializeValue(out: LittleEndianOutput) {
        out.writeDouble(getValue())
    }

    override fun getValueDataSize(): Int {
        return 8
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = NumberRecord()
        copyBaseFields(rec)
        rec.field_4_value = field_4_value
        return rec
    }

    companion object {
        const val sid: Short = 0x0203
    }
}
