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

import com.wxiwei.office.fc.ss.util.AreaReference
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * Common superclass of 2-D area refs
 */
abstract class Area2DPtgBase : AreaPtgBase {
    protected constructor(
        firstRow: Int,
        lastRow: Int,
        firstColumn: Int,
        lastColumn: Int,
        firstRowRelative: Boolean,
        lastRowRelative: Boolean,
        firstColRelative: Boolean,
        lastColRelative: Boolean
    ) : super(
        firstRow,
        lastRow,
        firstColumn,
        lastColumn,
        firstRowRelative,
        lastRowRelative,
        firstColRelative,
        lastColRelative
    )

    protected constructor(ar: AreaReference) : super(ar)

    protected constructor(`in`: LittleEndianInput) {
        readCoordinates(`in`)
    }

    protected abstract val sid: Byte

    override fun write(out: LittleEndianOutput) {
        out.writeByte(this.sid + ptgClass)
        writeCoordinates(out)
    }

    override val size: Int get() {
        return SIZE
    }

    override fun toFormulaString(): String? {
        return formatReferenceAsString()
    }

    override fun toString(): String {
        val sb = StringBuffer()
        sb.append(javaClass.getName())
        sb.append(" [")
        sb.append(formatReferenceAsString())
        sb.append("]")
        return sb.toString()
    }

    companion object {
        private const val SIZE = 9
    }
}
