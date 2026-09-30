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
 * Record for the top margin.
 *
 *
 * 
 * @author Shawn Laubach (slaubach at apache dot org)
 */
class TopMarginRecord : StandardRecord, Margin {
    private var field_1_margin = 0.0

    constructor()

    /**
     * @param in the RecordInputstream to read the record from
     */
    constructor(`in`: RecordInputStream) {
        field_1_margin = `in`.readDouble()
    }

    override fun toString(): String {
        val buffer = StringBuffer()
        buffer.append("[TopMargin]\n")
        buffer.append("    .margin               = ").append(" (").append(getMargin())
            .append(" )\n")
        buffer.append("[/TopMargin]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeDouble(field_1_margin)
    }

    override fun getDataSize(): Int {
        return 8
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    /**
     * Get the margin field for the TopMargin record.
     */
    override fun getMargin(): Double {
        return field_1_margin
    }

    /**
     * Set the margin field for the TopMargin record.
     */
    override fun setMargin(field_1_margin: Double) {
        this.field_1_margin = field_1_margin
    }

    override fun clone(): Any {
        val rec = TopMarginRecord()
        rec.field_1_margin = this.field_1_margin
        return rec
    }

    companion object {
        const val sid: Short = 0x28
    }
} // END OF 

