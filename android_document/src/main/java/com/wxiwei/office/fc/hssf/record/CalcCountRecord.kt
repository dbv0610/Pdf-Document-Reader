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
 * Title:        Calc Count Record
 * Description:  Specifies the maximum times the gui should perform a formula
 * recalculation.  For instance: in the case a formula includes
 * cells that are themselves a result of a formula and a value
 * changes.  This is essentially a failsafe against an infinate
 * loop in the event the formulas are not independant. <P>
 * REFERENCE:  PG 292 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 * @see CalcModeRecord
</P> */
class CalcCountRecord

    : StandardRecord {
    /**
     * get the number of iterations to perform
     * @return iterations
     */
    /**
     * set the number of iterations to perform
     * @param iterations to perform
     */
    var iterations: Short = 0

    constructor()

    constructor(`in`: RecordInputStream) {
        this.iterations = `in`.readShort()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[CALCCOUNT]\n")
        buffer.append("    .iterations     = ")
            .append(Integer.toHexString(this.iterations.toInt())).append("\n")
        buffer.append("[/CALCCOUNT]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(this.iterations.toInt())
    }

    override fun getDataSize(): Int {
        return 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = CalcCountRecord()
        rec.iterations = this.iterations
        return rec
    }

    companion object {
        const val sid: Short = 0xC
    }
}
