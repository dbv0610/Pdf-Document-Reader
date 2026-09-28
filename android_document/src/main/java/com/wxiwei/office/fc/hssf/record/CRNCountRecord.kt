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

/**
 * XCT - CRN Count <P>
 * 
 * REFERENCE:  5.114</P><P>
 * 
 * @author Josh Micich
</P> */
class CRNCountRecord : StandardRecord {
    var numberOfCRNs: Int = 0
        private set
    private var field_2_sheet_table_index = 0

    constructor() {
        throw RuntimeException("incomplete code")
    }


    constructor(`in`: RecordInputStream) {
        this.numberOfCRNs = `in`.readShort().toInt()
        if (this.numberOfCRNs < 0) {
            // TODO - seems like the sign bit of this field might be used for some other purpose
            // see example file for test case "TestBugs.test19599()"
            this.numberOfCRNs = (-numberOfCRNs.toShort()).toInt()
        }
        field_2_sheet_table_index = `in`.readShort().toInt()
    }


    override fun toString(): String {
        val sb = StringBuffer()
        sb.append(javaClass.getName()).append(" [XCT")
        sb.append(" nCRNs=").append(this.numberOfCRNs)
        sb.append(" sheetIx=").append(field_2_sheet_table_index)
        sb.append("]")
        return sb.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(numberOfCRNs.toShort().toInt())
        out.writeShort(field_2_sheet_table_index.toShort().toInt())
    }

    override fun getDataSize(): Int {
        return DATA_SIZE.toInt()
    }

    /**
     * return the non static version of the id for this record.
     */
    override fun getSid(): Short {
        return Companion.sid
    }

    companion object {
        const val sid: Short = 0x59

        private const val DATA_SIZE: Short = 4
    }
}
