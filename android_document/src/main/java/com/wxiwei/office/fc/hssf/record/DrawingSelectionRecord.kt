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

import com.wxiwei.office.fc.util.HexDump.intToHex
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * MsoDrawingSelection (0x00ED)
 *
 *
 * Reference:
 * [MS-OGRAPH].pdf sec 2.4.69
 * 
 * @author Josh Micich
 */
class DrawingSelectionRecord(`in`: RecordInputStream) : StandardRecord() {
    /**
     * From [MS-ODRAW].pdf sec 2.2.1<br></br>
     * TODO - make EscherRecordHeader [LittleEndianInput] aware and refactor with this
     */
    private class OfficeArtRecordHeader(`in`: LittleEndianInput) {
        /**
         * lower 4 bits is 'version' usually 0x01 or 0x0F (for containers)<br></br>
         * upper 12 bits is 'instance'
         */
        private val _verAndInstance: Int

        /** value should be between 0xF000 and 0xFFFF  */
        private val _type: Int
        private val _length: Int

        init {
            _verAndInstance = `in`.readUShort()
            _type = `in`.readUShort()
            _length = `in`.readInt()
        }

        fun serialize(out: LittleEndianOutput) {
            out.writeShort(_verAndInstance)
            out.writeShort(_type)
            out.writeInt(_length)
        }

        fun debugFormatAsString(): String {
            val sb = StringBuffer(32)
            sb.append("ver+inst=").append(shortToHex(_verAndInstance))
            sb.append(" type=").append(shortToHex(_type))
            sb.append(" len=").append(intToHex(_length))
            return sb.toString()
        }

        companion object {
            const val ENCODED_SIZE: Int = 8
        }
    }

    // [MS-OGRAPH].pdf says that the data of this record is an OfficeArtFDGSL structure
    // as described in[MS-ODRAW].pdf sec 2.2.33
    private val _header: OfficeArtRecordHeader
    private val _cpsp: Int

    /** a MSODGSLK enum value for the current selection mode  */
    private val _dgslk: Int
    private val _spidFocus: Int

    /** selected shape IDs (e.g. from EscherSpRecord.ShapeId)  */
    private val _shapeIds: IntArray

    init {
        _header = OfficeArtRecordHeader(`in`)
        _cpsp = `in`.readInt()
        _dgslk = `in`.readInt()
        _spidFocus = `in`.readInt()
        val nShapes = `in`.available() / 4
        val shapeIds = IntArray(nShapes)
        for (i in 0..<nShapes) {
            shapeIds[i] = `in`.readInt()
        }
        _shapeIds = shapeIds
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun getDataSize(): Int {
        return (OfficeArtRecordHeader.Companion.ENCODED_SIZE
                + 12 // 3 int fields
                + _shapeIds.size * 4)
    }

    public override fun serialize(out: LittleEndianOutput) {
        _header.serialize(out)
        out.writeInt(_cpsp)
        out.writeInt(_dgslk)
        out.writeInt(_spidFocus)
        for (i in _shapeIds.indices) {
            out.writeInt(_shapeIds[i])
        }
    }

    override fun clone(): Any {
        // currently immutable
        return this
    }

    override fun toString(): String {
        val sb = StringBuffer()

        sb.append("[MSODRAWINGSELECTION]\n")
        sb.append("    .rh       =(").append(_header.debugFormatAsString()).append(")\n")
        sb.append("    .cpsp     =").append(intToHex(_cpsp)).append('\n')
        sb.append("    .dgslk    =").append(intToHex(_dgslk)).append('\n')
        sb.append("    .spidFocus=").append(intToHex(_spidFocus)).append('\n')
        sb.append("    .shapeIds =(")
        for (i in _shapeIds.indices) {
            if (i > 0) {
                sb.append(", ")
            }
            sb.append(intToHex(_shapeIds[i]))
        }
        sb.append(")\n")

        sb.append("[/MSODRAWINGSELECTION]\n")
        return sb.toString()
    }

    companion object {
        const val sid: Short = 0x00ED
    }
}
