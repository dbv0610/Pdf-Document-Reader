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
package com.wxiwei.office.fc.hssf.record.chart

import com.wxiwei.office.fc.hssf.record.RecordInputStream
import com.wxiwei.office.fc.hssf.record.StandardRecord
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * CHART (0x1002) 
 *
 *
 * 
 * The chart record is used to define the location and size of a chart.
 *
 *
 * 
 * Chart related records don't seem to be covered in either the
 * <A HREF="http://sc.openoffice.org/excelfileformat.pdf">OOO</A>
 * or the
 * <A HREF="http://download.microsoft.com/download/0/B/E/0BE8BDD7-E5E8-422A-ABFD-4342ED7AD886/Excel97-2007BinaryFileFormat(xls)Specification.pdf">MS</A>
 * documentation.
 * 
 * The book "Microsoft Excel 97 Developer's Kit" ISBN: (1-57231-498-2) seems to have an entire
 * chapter (10) devoted to Chart records.  One
 * <A HREF="http://ooxmlisdefectivebydesign.blogspot.com/2008/03/bad-surprise-in-microsoft-office-binary.html">blog</A>
 * suggests that some documentation for these records is available in "MSDN Library, Feb 1998",
 * but no later.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class ChartRecord : StandardRecord {
    /**
     * Get the x field for the Chart record.
     */
    /**
     * Set the x field for the Chart record.
     */
    var x: Int = 0
    /**
     * Get the y field for the Chart record.
     */
    /**
     * Set the y field for the Chart record.
     */
    var y: Int = 0
    /**
     * Get the width field for the Chart record.
     */
    /**
     * Set the width field for the Chart record.
     */
    var width: Int = 0
    /**
     * Get the height field for the Chart record.
     */
    /**
     * Set the height field for the Chart record.
     */
    var height: Int = 0


    constructor()

    constructor(`in`: RecordInputStream) {
        this.x = `in`.readInt()
        this.y = `in`.readInt()
        this.width = `in`.readInt()
        this.height = `in`.readInt()
    }

    override fun toString(): String {
        val sb = StringBuffer()

        sb.append("[CHART]\n")
        sb.append("    .x     = ").append(this.x).append('\n')
        sb.append("    .y     = ").append(this.y).append('\n')
        sb.append("    .width = ").append(this.width).append('\n')
        sb.append("    .height= ").append(this.height).append('\n')
        sb.append("[/CHART]\n")
        return sb.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeInt(this.x)
        out.writeInt(this.y)
        out.writeInt(this.width)
        out.writeInt(this.height)
    }

    override fun getDataSize(): Int {
        return 4 + 4 + 4 + 4
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = ChartRecord()

        rec.x = this.x
        rec.y = this.y
        rec.width = this.width
        rec.height = this.height
        return rec
    }


    companion object {
        const val sid: Short = 0x1002
    }
}
