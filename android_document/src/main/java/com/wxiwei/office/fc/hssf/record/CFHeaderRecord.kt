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

import com.wxiwei.office.fc.hssf.record.cf.CellRangeUtil
import com.wxiwei.office.fc.hssf.record.cf.CellRangeUtil.createEnclosingCellRange
import com.wxiwei.office.fc.ss.util.CellRangeAddressList
import com.wxiwei.office.fc.ss.util.HSSFCellRangeAddress
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * Conditional Formatting Header record CFHEADER (0x01B0)
 * 
 * @author Dmitriy Kumshayev
 */
class CFHeaderRecord : StandardRecord {
    var numberOfConditionalFormats: Int = 0
    private var field_2_need_recalculation = 0
    var enclosingCellRange: HSSFCellRangeAddress? = null
    private var field_4_cell_ranges: CellRangeAddressList? = null

    /** Creates new CFHeaderRecord  */
    constructor() {
        field_4_cell_ranges = CellRangeAddressList()
    }

    constructor(regions: Array<HSSFCellRangeAddress>, nRules: Int) {
        val unmergedRanges = regions
        val mergeCellRanges: Array<HSSFCellRangeAddress> =
            CellRangeUtil.mergeCellRanges(unmergedRanges)
        setCellRanges(mergeCellRanges)
        this.numberOfConditionalFormats = nRules
    }

    constructor(`in`: RecordInputStream) {
        this.numberOfConditionalFormats = `in`.readShort().toInt()
        field_2_need_recalculation = `in`.readShort().toInt()
        this.enclosingCellRange = HSSFCellRangeAddress(`in`)
        field_4_cell_ranges = CellRangeAddressList(`in`)
    }

    var needRecalculation: Boolean
        get() = if (field_2_need_recalculation == 1) true else false
        set(b) {
            field_2_need_recalculation = if (b) 1 else 0
        }

    /**
     * Set cell ranges list to a single cell range and
     * modify the enclosing cell range accordingly.
     * @param cellRanges - list of CellRange objects
     */
    fun setCellRanges(cellRanges: Array<HSSFCellRangeAddress>) {
        requireNotNull(cellRanges) { "cellRanges must not be null" }
        val cral = CellRangeAddressList()
        var enclosingRange: HSSFCellRangeAddress? = null
        for (i in cellRanges.indices) {
            val cr = cellRanges[i]
            enclosingRange = createEnclosingCellRange(cr, enclosingRange)
            cral.addCellRangeAddress(cr)
        }
        this.enclosingCellRange = enclosingRange
        field_4_cell_ranges = cral
    }

    val cellRanges: Array<HSSFCellRangeAddress>
        get() = field_4_cell_ranges!!.getCellRangeAddresses()

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[CFHEADER]\n")
        buffer.append("	.id		= ").append(Integer.toHexString(Companion.sid.toInt()))
            .append("\n")
        buffer.append("	.numCF			= ").append(this.numberOfConditionalFormats)
            .append("\n")
        buffer.append("	.needRecalc	   = ").append(this.needRecalculation).append("\n")
        buffer.append("	.enclosingCellRange= ").append(this.enclosingCellRange).append("\n")
        buffer.append("	.cfranges=[")
        for (i in 0..<field_4_cell_ranges!!.countRanges()) {
            buffer.append(if (i == 0) "" else ",")
                .append(field_4_cell_ranges!!.getCellRangeAddress(i).toString())
        }
        buffer.append("]\n")
        buffer.append("[/CFHEADER]\n")
        return buffer.toString()
    }

    override fun getDataSize(): Int {
        return (4 // 2 short fields
                + HSSFCellRangeAddress.ENCODED_SIZE
                + field_4_cell_ranges!!.getSize())
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(this.numberOfConditionalFormats)
        out.writeShort(field_2_need_recalculation)
        enclosingCellRange!!.serialize(out)
        field_4_cell_ranges!!.serialize(out)
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val result = CFHeaderRecord()
        result.numberOfConditionalFormats = this.numberOfConditionalFormats
        result.field_2_need_recalculation = field_2_need_recalculation
        result.enclosingCellRange = this.enclosingCellRange
        result.field_4_cell_ranges = field_4_cell_ranges!!.copy()
        return result
    }

    companion object {
        const val sid: Short = 0x01B0
    }
}
