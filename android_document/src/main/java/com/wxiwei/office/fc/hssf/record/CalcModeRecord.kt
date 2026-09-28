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
 * Title:        Calc Mode Record<P>
 * Description:  Tells the gui whether to calculate formulas
 * automatically, manually or automatically
 * except for tables.</P><P>
 * REFERENCE:  PG 292 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 * @see CalcCountRecord
</P> */
class CalcModeRecord

    : StandardRecord {
    /**
     * get the calc mode flag for formulas
     * 
     * @see .MANUAL
     * 
     * @see .AUTOMATIC
     * 
     * @see .AUTOMATIC_EXCEPT_TABLES
     * 
     * 
     * @return calcmode one of the three flags above
     */
    /**
     * set the calc mode flag for formulas
     * 
     * @see .MANUAL
     * 
     * @see .AUTOMATIC
     * 
     * @see .AUTOMATIC_EXCEPT_TABLES
     * 
     * 
     * @param calcmode one of the three flags above
     */
    var calcMode: Short = 0

    constructor()

    constructor(`in`: RecordInputStream) {
        this.calcMode = `in`.readShort()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[CALCMODE]\n")
        buffer.append("    .calcmode       = ")
            .append(Integer.toHexString(this.calcMode.toInt())).append("\n")
        buffer.append("[/CALCMODE]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(this.calcMode.toInt())
    }

    override fun getDataSize(): Int {
        return 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = CalcModeRecord()
        rec.calcMode = this.calcMode
        return rec
    }

    companion object {
        const val sid: Short = 0xD

        /**
         * manually calculate formulas (0)
         */
        const val MANUAL: Short = 0

        /**
         * automatically calculate formulas (1)
         */
        const val AUTOMATIC: Short = 1

        /**
         * automatically calculate formulas except for tables (-1)
         */
        val AUTOMATIC_EXCEPT_TABLES: Short = -1
    }
}
