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

import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Integer (unsigned short integer) Stores an unsigned short value (java int) in
 * a formula
 * 
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 */
class IntPtg(value: Int) : ScalarConstantPtg() {
    val value: Int

    constructor(`in`: LittleEndianInput) : this(`in`.readUShort())

    init {
        require(isInRange(value)) { "value is out of range: " + value }
        this.value = value
    }

    override fun write(out: LittleEndianOutput) {
        out.writeByte(sid + ptgClass)
        out.writeShort(this.value)
    }

    override val size: Int get() {
        return SIZE
    }

    override fun toFormulaString(): String {
        return this.value.toString()
    }

    companion object {
        // 16 bit unsigned integer
        private const val MIN_VALUE = 0x0000
        private const val MAX_VALUE = 0xFFFF

        /**
         * Excel represents integers 0..65535 with the tInt token.
         * 
         * @return `true` if the specified value is within the range of values
         * <tt>IntPtg</tt> can represent.
         */
        @JvmStatic
        fun isInRange(i: Int): Boolean {
            return i >= MIN_VALUE && i <= MAX_VALUE
        }

        const val SIZE: Int = 3
        const val sid: Byte = 0x1e
    }
}
