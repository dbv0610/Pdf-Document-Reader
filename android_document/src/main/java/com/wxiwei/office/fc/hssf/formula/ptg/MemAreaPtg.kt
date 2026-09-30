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
 * @author Daniel Noll (daniel at nuix dot com dot au)
 */
class MemAreaPtg : OperandPtg {
    private val field_1_reserved: Int
    val lenRefSubexpression: Int

    /** Creates new MemAreaPtg  */
    constructor(subexLen: Int) {
        field_1_reserved = 0
        this.lenRefSubexpression = subexLen
    }

    constructor(`in`: LittleEndianInput) {
        field_1_reserved = `in`.readInt()
        this.lenRefSubexpression = `in`.readShort().toInt()
    }

    override fun write(out: LittleEndianOutput) {
        out.writeByte(sid + ptgClass)
        out.writeInt(field_1_reserved)
        out.writeShort(this.lenRefSubexpression)
    }

    override val size: Int get() {
        return SIZE
    }

    override fun toFormulaString(): String {
        return "" // TODO: Not sure how to format this. -- DN
    }

    override val defaultOperandClass: Byte get() {
        return Ptg.Companion.CLASS_VALUE
    }

    override fun toString(): String {
        val sb = StringBuffer(64)
        sb.append(javaClass.getName()).append(" [len=")
        sb.append(this.lenRefSubexpression)
        sb.append("]")
        return sb.toString()
    }

    companion object {
        const val sid: Short = 0x26
        private const val SIZE = 7
    }
}
