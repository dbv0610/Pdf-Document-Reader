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
 * 
 * @author  andy
 * @author Jason Height (jheight at chariot dot net dot au)
 * @author dmui (save existing implementation)
 */
class ExpPtg : ControlPtg {
    val row: Int
    val column: Int

    constructor(`in`: LittleEndianInput) {
        this.row = `in`.readShort().toInt()
        this.column = `in`.readShort().toInt()
    }

    constructor(firstRow: Int, firstCol: Int) {
        this.row = firstRow
        this.column = firstCol
    }

    override fun write(out: LittleEndianOutput) {
        out.writeByte(sid + ptgClass)
        out.writeShort(this.row)
        out.writeShort(this.column)
    }

    override val size: Int get() {
        return SIZE
    }

    override fun toFormulaString(): String? {
        throw RuntimeException("Coding Error: Expected ExpPtg to be converted from Shared to Non-Shared Formula by ValueRecordsAggregate, but it wasn't")
    }

    override fun toString(): String {
        val buffer = StringBuffer("[Array Formula or Shared Formula]\n")
        buffer.append("row = ").append(this.row).append("\n")
        buffer.append("col = ").append(this.column).append("\n")
        return buffer.toString()
    }

    companion object {
        private const val SIZE = 5
        const val sid: Short = 0x1
    }
}
