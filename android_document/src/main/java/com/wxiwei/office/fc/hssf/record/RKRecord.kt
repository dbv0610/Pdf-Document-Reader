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

import com.wxiwei.office.fc.hssf.util.RKUtil
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * Title:        RK Record (0x027E)
 *
 *
 * Description:  An internal 32 bit number with the two most significant bits
 * storing the type.  This is part of a bizarre scheme to save disk
 * space and memory (gee look at all the other whole records that
 * are in the file just "cause"..,far better to waste processor
 * cycles on this then leave on of those "valuable" records out).
 *
 *
 * We support this in READ-ONLY mode.  HSSF converts these to NUMBER records
 *
 *
 * 
 * 
 * 
 * REFERENCE:  PG 376 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @see NumberRecord
</P> */
class RKRecord : CellRecord {
    private var field_4_rk_number = 0

    private constructor()

    constructor(`in`: RecordInputStream) : super(`in`) {
        field_4_rk_number = `in`.readInt()
    }

    /**
     * Extract the value of the number
     * <P>
     * The mechanism for determining the value is dependent on the two
     * low order bits of the raw number. If bit 1 is set, the number
     * is an integer and can be cast directly as a double, otherwise,
     * it's apparently the exponent and mantissa of a double (and the
     * remaining low-order bits of the double's mantissa are 0's).
    </P> * <P>
     * If bit 0 is set, the result of the conversion to a double is
     * divided by 100; otherwise, the value is left alone.
    </P> * <P>
     * [insert picture of Screwy Squirrel in full Napoleonic regalia]
     * 
     * @return the value as a proper double (hey, it <B>could</B>
     * happen)
    </P> */
    fun getRKNumber(): Double {
        return RKUtil.decodeNumber(field_4_rk_number)
    }

    override fun getRecordName(): String {
        return "RK"
    }

    override fun appendValueText(sb: StringBuilder) {
        sb.append("  .value= ").append(getRKNumber())
    }

    override fun serializeValue(out: LittleEndianOutput) {
        out.writeInt(field_4_rk_number)
    }

    override fun getValueDataSize(): Int {
        return 4
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = RKRecord()
        copyBaseFields(rec)
        rec.field_4_rk_number = field_4_rk_number
        return rec
    }

    companion object {
        const val sid: Short = 0x027E
        const val RK_IEEE_NUMBER: Short = 0
        const val RK_IEEE_NUMBER_TIMES_100: Short = 1
        const val RK_INTEGER: Short = 2
        const val RK_INTEGER_TIMES_100: Short = 3
    }
}
