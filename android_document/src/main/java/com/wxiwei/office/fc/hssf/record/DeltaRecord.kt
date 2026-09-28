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
 * Title:        Delta Record (0x0010)
 *
 *
 * Description:  controls the accuracy of the calculations
 *
 *
 * REFERENCE:  PG 303 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)
 *
 *
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 */
class DeltaRecord : StandardRecord {
    // a double is an IEEE 8-byte float...damn IEEE and their goofy standards an
    // ambiguous numeric identifiers
    private val field_1_max_change: Double

    constructor(maxChange: Double) {
        field_1_max_change = maxChange
    }

    constructor(`in`: RecordInputStream) {
        field_1_max_change = `in`.readDouble()
    }

    /**
     * get the maximum change
     * @return maxChange - maximum rounding error
     */
    fun getMaxChange(): Double {
        return field_1_max_change
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[DELTA]\n")
        buffer.append("    .maxchange = ").append(getMaxChange()).append("\n")
        buffer.append("[/DELTA]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeDouble(getMaxChange())
    }

    override fun getDataSize(): Int {
        return 8
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        // immutable
        return this
    }

    companion object {
        const val sid: Short = 0x0010
        const val DEFAULT_VALUE: Double = 0.0010 // should be .001
    }
}
