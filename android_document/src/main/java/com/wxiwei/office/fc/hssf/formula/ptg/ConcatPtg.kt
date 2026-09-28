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

/**
 * 
 * @author  andy
 * @author Jason Height (jheight at chariot dot net dot au)
 */
class ConcatPtg private constructor() : ValueOperatorPtg() {
    override val sid: Byte get() {
        return Companion.sid
    }

    override val numberOfOperands: Int get() {
        return 2
    }

    override fun toFormulaString(operands: Array<String?>): String {
        val buffer = StringBuffer()

        buffer.append(operands[0])
        buffer.append(CONCAT)
        buffer.append(operands[1])
        return buffer.toString()
    }

    companion object {
        const val sid: Byte = 0x08

        private const val CONCAT = "&"

        @JvmField
        val instance: ValueOperatorPtg = ConcatPtg()
    }
}
