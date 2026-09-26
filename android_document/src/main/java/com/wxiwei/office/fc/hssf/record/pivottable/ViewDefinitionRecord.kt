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
package com.wxiwei.office.fc.hssf.record.pivottable

import com.wxiwei.office.fc.hssf.record.RecordInputStream
import com.wxiwei.office.fc.hssf.record.StandardRecord
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.fc.util.StringUtil.getEncodedSize
import com.wxiwei.office.fc.util.StringUtil.readUnicodeString
import com.wxiwei.office.fc.util.StringUtil.writeUnicodeStringFlagAndData


/**
 * SXVIEW - View Definition (0x00B0)<br></br>
 * 
 * @author Patrick Cheng
 */
class ViewDefinitionRecord(`in`: RecordInputStream) : StandardRecord() {
    private val rwFirst: Int
    private val rwLast: Int
    private val colFirst: Int
    private val colLast: Int
    private val rwFirstHead: Int
    private val rwFirstData: Int
    private val colFirstData: Int
    private val iCache: Int
    private val reserved: Int

    private val sxaxis4Data: Int
    private val ipos4Data: Int
    private val cDim: Int

    private val cDimRw: Int

    private val cDimCol: Int
    private val cDimPg: Int

    private val cDimData: Int
    private val cRw: Int
    private val cCol: Int
    private val grbit: Int
    private val itblAutoFmt: Int

    private val dataField: String
    private val name: String


    init {
        rwFirst = `in`.readUShort()
        rwLast = `in`.readUShort()
        colFirst = `in`.readUShort()
        colLast = `in`.readUShort()
        rwFirstHead = `in`.readUShort()
        rwFirstData = `in`.readUShort()
        colFirstData = `in`.readUShort()
        iCache = `in`.readUShort()
        reserved = `in`.readUShort()
        sxaxis4Data = `in`.readUShort()
        ipos4Data = `in`.readUShort()
        cDim = `in`.readUShort()
        cDimRw = `in`.readUShort()
        cDimCol = `in`.readUShort()
        cDimPg = `in`.readUShort()
        cDimData = `in`.readUShort()
        cRw = `in`.readUShort()
        cCol = `in`.readUShort()
        grbit = `in`.readUShort()
        itblAutoFmt = `in`.readUShort()
        val cchName = `in`.readUShort()
        val cchData = `in`.readUShort()

        name = readUnicodeString(`in`, cchName)
        dataField = readUnicodeString(`in`, cchData)
    }

    override fun serialize(out: LittleEndianOutput) {
        out.writeShort(rwFirst)
        out.writeShort(rwLast)
        out.writeShort(colFirst)
        out.writeShort(colLast)
        out.writeShort(rwFirstHead)
        out.writeShort(rwFirstData)
        out.writeShort(colFirstData)
        out.writeShort(iCache)
        out.writeShort(reserved)
        out.writeShort(sxaxis4Data)
        out.writeShort(ipos4Data)
        out.writeShort(cDim)
        out.writeShort(cDimRw)
        out.writeShort(cDimCol)
        out.writeShort(cDimPg)
        out.writeShort(cDimData)
        out.writeShort(cRw)
        out.writeShort(cCol)
        out.writeShort(grbit)
        out.writeShort(itblAutoFmt)
        out.writeShort(name.length)
        out.writeShort(dataField.length)

        writeUnicodeStringFlagAndData(out, name)
        writeUnicodeStringFlagAndData(out, dataField)
    }

    override fun getDataSize(): Int {
        return 40 +  // 20 short fields (rwFirst ... itblAutoFmt)
                getEncodedSize(name) + getEncodedSize(dataField)
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[SXVIEW]\n")
        buffer.append("    .rwFirst      =").append(shortToHex(rwFirst)).append('\n')
        buffer.append("    .rwLast       =").append(shortToHex(rwLast)).append('\n')
        buffer.append("    .colFirst     =").append(shortToHex(colFirst)).append('\n')
        buffer.append("    .colLast      =").append(shortToHex(colLast)).append('\n')
        buffer.append("    .rwFirstHead  =").append(shortToHex(rwFirstHead)).append('\n')
        buffer.append("    .rwFirstData  =").append(shortToHex(rwFirstData)).append('\n')
        buffer.append("    .colFirstData =").append(shortToHex(colFirstData)).append('\n')
        buffer.append("    .iCache       =").append(shortToHex(iCache)).append('\n')
        buffer.append("    .reserved     =").append(shortToHex(reserved)).append('\n')
        buffer.append("    .sxaxis4Data  =").append(shortToHex(sxaxis4Data)).append('\n')
        buffer.append("    .ipos4Data    =").append(shortToHex(ipos4Data)).append('\n')
        buffer.append("    .cDim         =").append(shortToHex(cDim)).append('\n')
        buffer.append("    .cDimRw       =").append(shortToHex(cDimRw)).append('\n')
        buffer.append("    .cDimCol      =").append(shortToHex(cDimCol)).append('\n')
        buffer.append("    .cDimPg       =").append(shortToHex(cDimPg)).append('\n')
        buffer.append("    .cDimData     =").append(shortToHex(cDimData)).append('\n')
        buffer.append("    .cRw          =").append(shortToHex(cRw)).append('\n')
        buffer.append("    .cCol         =").append(shortToHex(cCol)).append('\n')
        buffer.append("    .grbit        =").append(shortToHex(grbit)).append('\n')
        buffer.append("    .itblAutoFmt  =").append(shortToHex(itblAutoFmt)).append('\n')
        buffer.append("    .name         =").append(name).append('\n')
        buffer.append("    .dataField    =").append(dataField).append('\n')

        buffer.append("[/SXVIEW]\n")
        return buffer.toString()
    }

    companion object {
        const val sid: Short = 0x00B0
    }
}
