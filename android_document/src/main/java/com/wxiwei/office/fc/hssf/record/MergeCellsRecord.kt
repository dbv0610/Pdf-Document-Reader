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

import com.wxiwei.office.fc.ss.util.CellRangeAddressList
import com.wxiwei.office.fc.ss.util.HSSFCellRangeAddress
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * Title: Merged Cells Record (0x00E5)
 * <br></br>
 * Description:  Optional record defining a square area of cells to "merged" into one cell. <br></br>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 */
class MergeCellsRecord : StandardRecord {
    /** sometimes the regions array is shared with other MergedCellsRecords  */
    private val _regions: Array<HSSFCellRangeAddress>
    private val _startIndex: Int
    private val _numberOfRegions: Int

    constructor(regions: Array<HSSFCellRangeAddress>, startIndex: Int, numberOfRegions: Int) {
        _regions = regions
        _startIndex = startIndex
        _numberOfRegions = numberOfRegions
    }

    /**
     * Constructs a MergedCellsRecord and sets its fields appropriately
     * @param in the RecordInputstream to read the record from
     */
    constructor(`in`: RecordInputStream) {
        val nRegions = `in`.readUShort()
        val cras: Array<HSSFCellRangeAddress> = Array(nRegions) { HSSFCellRangeAddress(`in`) }
        _numberOfRegions = nRegions
        _startIndex = 0
        _regions = cras
    }

    /**
     * get the number of merged areas.  If this drops down to 0 you should just go
     * ahead and delete the record.
     * @return number of areas
     */
    fun getNumAreas(): Short {
        return _numberOfRegions.toShort()
    }

    /**
     * @return MergedRegion at the given index representing the area that is Merged (r1,c1 - r2,c2)
     */
    fun getAreaAt(index: Int): HSSFCellRangeAddress {
        return _regions[_startIndex + index]
    }

    override fun getDataSize(): Int {
        return CellRangeAddressList.getEncodedSize(_numberOfRegions)
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    public override fun serialize(out: LittleEndianOutput) {
        val nItems = _numberOfRegions
        out.writeShort(nItems)
        for (i in 0..<_numberOfRegions) {
            _regions[_startIndex + i].serialize(out)
        }
    }

    override fun toString(): String {
        val retval = StringBuffer()

        retval.append("[MERGEDCELLS]").append("\n")
        retval.append("     .numregions =").append(getNumAreas().toInt()).append("\n")
        for (k in 0..<_numberOfRegions) {
            val r = _regions[_startIndex + k]

            retval.append("     .rowfrom =").append(r.getFirstRow()).append("\n")
            retval.append("     .rowto   =").append(r.getLastRow()).append("\n")
            retval.append("     .colfrom =").append(r.getFirstColumn()).append("\n")
            retval.append("     .colto   =").append(r.getLastColumn()).append("\n")
        }
        retval.append("[MERGEDCELLS]").append("\n")
        return retval.toString()
    }

    override fun clone(): Any {
        val nRegions = _numberOfRegions
        val clonedRegions: Array<HSSFCellRangeAddress> =
            Array(nRegions) { i -> _regions[_startIndex + i].copy() }
        return MergeCellsRecord(clonedRegions, 0, nRegions)
    }

    companion object {
        const val sid: Short = 0x00E5
    }
}
