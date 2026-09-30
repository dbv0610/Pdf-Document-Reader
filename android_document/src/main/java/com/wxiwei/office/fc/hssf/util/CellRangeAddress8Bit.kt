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
package com.wxiwei.office.fc.hssf.util

import com.wxiwei.office.fc.ss.util.CellRangeAddressBase
import com.wxiwei.office.fc.util.LittleEndianByteArrayOutputStream
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * See OOO documentation: excelfileformat.pdf sec 2.5.14 - 'Cell Range Address'
 *
 *
 * 
 * Like [CellRangeAddress] except column fields are 8-bit.
 * 
 * @author Josh Micich
 */
class CellRangeAddress8Bit : CellRangeAddressBase {
    constructor(firstRow: Int, lastRow: Int, firstCol: Int, lastCol: Int) : super(
        firstRow,
        lastRow,
        firstCol,
        lastCol
    )

    constructor(`in`: LittleEndianInput) : super(
        readUShortAndCheck(`in`),
        `in`.readUShort(),
        `in`.readUByte(),
        `in`.readUByte()
    )

    @Deprecated("use {@link #serialize(LittleEndianOutput)}")
    fun serialize(offset: Int, data: ByteArray): Int {
        serialize(LittleEndianByteArrayOutputStream(data, offset, ENCODED_SIZE))
        return ENCODED_SIZE
    }

    fun serialize(out: LittleEndianOutput) {
        out.writeShort(getFirstRow())
        out.writeShort(getLastRow())
        out.writeByte(getFirstColumn())
        out.writeByte(getLastColumn())
    }

    fun copy(): CellRangeAddress8Bit {
        return CellRangeAddress8Bit(getFirstRow(), getLastRow(), getFirstColumn(), getLastColumn())
    }

    companion object {
        const val ENCODED_SIZE: Int = 6

        private fun readUShortAndCheck(`in`: LittleEndianInput): Int {
            if (`in`.available() < ENCODED_SIZE) {
                // Ran out of data
                throw RuntimeException("Ran out of data reading CellRangeAddress")
            }
            return `in`.readUShort()
        }

        fun getEncodedSize(numberOfItems: Int): Int {
            return numberOfItems * ENCODED_SIZE
        }
    }
}
