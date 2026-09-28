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

import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Missing Function Arguments
 * 
 * Avik Sengupta &lt;avik at apache.org&gt;
 * 
 * @author Jason Height (jheight at chariot dot net dot au)
 */
class MissingArgPtg private constructor() : ScalarConstantPtg() {
    override fun write(out: LittleEndianOutput) {
        out.writeByte(sid + ptgClass)
    }

    override val size: Int get() {
        return SIZE
    }

    override fun toFormulaString(): String {
        return " "
    }

    companion object {
        private const val SIZE = 1
        const val sid: Byte = 0x16

        @JvmField
        val instance: Ptg = MissingArgPtg()
    }
}
