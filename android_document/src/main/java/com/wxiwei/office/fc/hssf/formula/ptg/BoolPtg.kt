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

import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Boolean (boolean) Stores a (java) boolean value in a formula.
 * 
 * @author Paul Krause (pkrause at soundbite dot com)
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 */
class BoolPtg private constructor(val value: Boolean) : ScalarConstantPtg() {
    override fun write(out: LittleEndianOutput) {
        out.writeByte(sid + ptgClass)
        out.writeByte(if (this.value) 1 else 0)
    }

    override val size: Int get() {
        return SIZE
    }

    override fun toFormulaString(): String {
        return if (this.value) "TRUE" else "FALSE"
    }

    companion object {
        const val SIZE: Int = 2
        const val sid: Byte = 0x1D

        private val FALSE = BoolPtg(false)
        private val TRUE = BoolPtg(true)

        @JvmStatic
        fun valueOf(b: Boolean): BoolPtg {
            return if (b) TRUE else FALSE
        }

        fun read(`in`: LittleEndianInput): BoolPtg {
            return valueOf(`in`.readByte().toInt() == 1)
        }
    }
}
