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
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Title: Beginning Of File (0x0809)<P>
 * Description: Somewhat of a misnomer, its used for the beginning of a set of
 * records that have a particular purpose or subject.
 * Used in sheets and workbooks.</P><P>
 * REFERENCE:  PG 289 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver
 * @author Jason Height (jheight at chariot dot net dot au)
</P> */
class BOFRecord : StandardRecord {
    /**
     * Version number - for BIFF8 should be 0x06
     * @see .VERSION
     * 
     * @return version number of the generator of this file
     */
    /**
     * Version number - for BIFF8 should be 0x06
     * @see .VERSION
     * 
     * @param version version to be set
     */
    var version: Int = 0
    /**
     * type of object this marks
     * @see .TYPE_WORKBOOK
     * 
     * @see .TYPE_VB_MODULE
     * 
     * @see .TYPE_WORKSHEET
     * 
     * @see .TYPE_CHART
     * 
     * @see .TYPE_EXCEL_4_MACRO
     * 
     * @see .TYPE_WORKSPACE_FILE
     * 
     * @return type of object
     */
    /**
     * type of object this marks
     * @see .TYPE_WORKBOOK
     * 
     * @see .TYPE_VB_MODULE
     * 
     * @see .TYPE_WORKSHEET
     * 
     * @see .TYPE_CHART
     * 
     * @see .TYPE_EXCEL_4_MACRO
     * 
     * @see .TYPE_WORKSPACE_FILE
     * 
     * @param type type to be set
     */
    var type: Int = 0
    /**
     * get the build that wrote this file
     * @see .BUILD
     * 
     * @return short build number of the generator of this file
     */
    /**
     * build that wrote this file
     * @see .BUILD
     * 
     * @param build build number to set
     */
    var build: Int = 0
    /**
     * Year of the build that wrote this file
     * @see .BUILD_YEAR
     * 
     * @return short build year of the generator of this file
     */
    /**
     * Year of the build that wrote this file
     * @see .BUILD_YEAR
     * 
     * @param year build year to set
     */
    var buildYear: Int = 0
    /**
     * get the history bit mask (not very useful)
     * @see .HISTORY_MASK
     * 
     * @return int bitmask showing the history of the file (who cares!)
     */
    /**
     * set the history bit mask (not very useful)
     * @see .HISTORY_MASK
     * 
     * @param bitmask bitmask to set for the history
     */
    var historyBitMask: Int = 0
    /**
     * get the minimum version required to read this file
     * 
     * @see .VERSION
     * 
     * @return int least version that can read the file
     */
    /**
     * set the minimum version required to read this file
     * 
     * @see .VERSION
     * 
     * @param version version to set
     */
    var requiredVersion: Int = 0

    /**
     * Constructs an empty BOFRecord with no fields set.
     */
    constructor()

    private constructor(type: Int) {
        this.version = VERSION
        this.type = type
        this.build = BUILD
        this.buildYear = BUILD_YEAR
        this.historyBitMask = 0x01
        this.requiredVersion = VERSION
    }

    constructor(`in`: RecordInputStream) {
        this.version = `in`.readShort().toInt()
        this.type = `in`.readShort().toInt()

        // Some external tools don't generate all of
        //  the remaining fields
        if (`in`.remaining() >= 2) {
            this.build = `in`.readShort().toInt()
        }
        if (`in`.remaining() >= 2) {
            this.buildYear = `in`.readShort().toInt()
        }
        if (`in`.remaining() >= 4) {
            this.historyBitMask = `in`.readInt()
        }
        if (`in`.remaining() >= 4) {
            this.requiredVersion = `in`.readInt()
        }
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[BOF RECORD]\n")
        buffer.append("    .version  = ").append(shortToHex(this.version)).append("\n")
        buffer.append("    .type     = ").append(shortToHex(this.type))
        buffer.append(" (").append(this.typeName).append(")").append("\n")
        buffer.append("    .build    = ").append(shortToHex(this.build)).append("\n")
        buffer.append("    .buildyear= ").append(this.buildYear).append("\n")
        buffer.append("    .history  = ").append(intToHex(this.historyBitMask)).append("\n")
        buffer.append("    .reqver   = ").append(intToHex(this.requiredVersion)).append("\n")
        buffer.append("[/BOF RECORD]\n")
        return buffer.toString()
    }

    private val typeName: String
        get() {
            when (this.type) {
                TYPE_CHART -> return "chart"
                TYPE_EXCEL_4_MACRO -> return "excel 4 macro"
                TYPE_VB_MODULE -> return "vb module"
                TYPE_WORKBOOK -> return "workbook"
                TYPE_WORKSHEET -> return "worksheet"
                TYPE_WORKSPACE_FILE -> return "workspace file"
            }
            return "#error unknown type#"
        }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(this.version)
        out.writeShort(this.type)
        out.writeShort(this.build)
        out.writeShort(this.buildYear)
        out.writeInt(this.historyBitMask)
        out.writeInt(this.requiredVersion)
    }

    override fun getDataSize(): Int {
        return 16
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = BOFRecord()
        rec.version = this.version
        rec.type = this.type
        rec.build = this.build
        rec.buildYear = this.buildYear
        rec.historyBitMask = this.historyBitMask
        rec.requiredVersion = this.requiredVersion
        return rec
    }

    companion object {
        /**
         * for BIFF8 files the BOF is 0x809.  For earlier versions it was 0x09 or 0x(biffversion)09
         */
        const val sid: Short = 0x809

        /** suggested default (0x0600 - BIFF8)  */
        const val VERSION: Int = 0x0600

        /** suggested default 0x10d3  */
        const val BUILD: Int = 0x10d3

        /** suggested default  0x07CC (1996)  */
        const val BUILD_YEAR: Int = 0x07CC // 1996

        /** suggested default for a normal sheet (0x41)  */
        const val HISTORY_MASK: Int = 0x41

        const val TYPE_WORKBOOK: Int = 0x05
        const val TYPE_VB_MODULE: Int = 0x06
        const val TYPE_WORKSHEET: Int = 0x10
        const val TYPE_CHART: Int = 0x20
        const val TYPE_EXCEL_4_MACRO: Int = 0x40
        const val TYPE_WORKSPACE_FILE: Int = 0x100

        fun createSheetBOF(): BOFRecord {
            return BOFRecord(TYPE_WORKSHEET)
        }
    }
}
