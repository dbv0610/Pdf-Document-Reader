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
package com.wxiwei.office.fc.hssf.formula.ptg

import com.wxiwei.office.fc.ss.util.NumberToTextConverter
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * Number Stores a floating point value in a formula value stored in a 8 byte
 * field using IEEE notation
 * 
 * @author Avik Sengupta
 * @author Jason Height (jheight at chariot dot net dot au)
 */
class NumberPtg(val value: Double) : ScalarConstantPtg() {
    constructor(`in`: LittleEndianInput) : this(`in`.readDouble())

    /**
     * Create a NumberPtg from a string representation of the number Number
     * format is not checked, it is expected to be validated in the parser that
     * calls this method.
     * 
     * @param value String representation of a floating point number
     */
    constructor(value: String) : this(value.toDouble())

    override fun write(out: LittleEndianOutput) {
        out.writeByte(sid + ptgClass)
        out.writeDouble(this.value)
    }

    override val size: Int get() {
        return SIZE
    }

    override fun toFormulaString(): String {
        return NumberToTextConverter.toText(this.value)
    }

    companion object {
        const val SIZE: Int = 9
        const val sid: Byte = 0x1f
    }
}
