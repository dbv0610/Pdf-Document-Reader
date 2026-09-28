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

import com.wxiwei.office.fc.util.HexDump
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Title:        Continue Record(0x003C) - Helper class used primarily for SST Records <P>
 * Description:  handles overflow for prior record in the input
 * stream; content is tailored to that prior record</P><P>
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Csaba Nagy (ncsaba at yahoo dot com)
</P> */
class ContinueRecord : StandardRecord {
    /**
     * get the data for continuation
     * @return byte array containing all of the continued data
     */
    var data: ByteArray?
        private set

    constructor(data: ByteArray?) {
        this.data = data
    }

    public override fun getDataSize(): Int {
        if (this.data != null) {
            return data!!.size
        }

        return 0
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.write(this.data!!)
    }

    fun resetData() {
        this.data = null
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[CONTINUE RECORD]\n")
        buffer.append("    .data = ").append(HexDump.toHex(this.data!!)).append("\n")
        buffer.append("[/CONTINUE RECORD]\n")
        return buffer.toString()
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    constructor(`in`: RecordInputStream) {
        this.data = `in`.readRemainder()
    }

    override fun clone(): Any {
        return ContinueRecord(this.data)
    }

    companion object {
        const val sid: Short = 0x003C
    }
}
