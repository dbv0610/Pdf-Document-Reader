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

import com.wxiwei.office.fc.util.BitFieldFactory.Companion.getInstance
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Title:        Iteration Record (0x0011) 
 *
 *
 * Description:  Tells whether to iterate over forumla calculations or not
 * (if a formula is dependant upon another formula's result)
 * (odd feature for something that can only have 32 elements in
 * a formula!)<P>
 * REFERENCE:  PG 325 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P>
 *
 *
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 */
class IterationRecord : StandardRecord {
    private var _flags: Int

    constructor(iterateOn: Boolean) {
        _flags = iterationOn.setBoolean(0, iterateOn)
    }

    constructor(`in`: RecordInputStream) {
        _flags = `in`.readShort().toInt()
    }

    /**
     * set whether or not to iterate for calculations
     * @param iterate or not
     */
    fun setIteration(iterate: Boolean) {
        _flags = iterationOn.setBoolean(_flags, iterate)
    }

    /**
     * get whether or not to iterate for calculations
     * 
     * @return whether iterative calculations are turned off or on
     */
    fun getIteration(): Boolean {
        return iterationOn.isSet(_flags)
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[ITERATION]\n")
        buffer.append("    .flags      = ").append(shortToHex(_flags)).append("\n")
        buffer.append("[/ITERATION]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(_flags)
    }

    override fun getDataSize(): Int {
        return 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        return IterationRecord(getIteration())
    }

    companion object {
        const val sid: Short = 0x0011

        private val iterationOn = getInstance(0x0001)
    }
}
