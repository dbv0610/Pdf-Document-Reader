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

import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Title:        DATAVALIDATIONS Record (0x01B2)
 *
 *
 * Description:  used in data validation ;
 * This record is the list header of all data validation records (0x01BE) in the current sheet.
 * @author Dragos Buleandra (dragos.buleandra@trade2b.ro)
 */
class DVALRecord : StandardRecord {
    /** Options of the DVAL  */
    private var field_1_options: Short = 0

    /** Horizontal position of the dialog  */
    private var field_2_horiz_pos = 0

    /** Vertical position of the dialog  */
    private var field_3_vert_pos = 0

    /** Object ID of the drop down arrow object for list boxes ;
     * in our case this will be always FFFF , until
     * MSODrawingGroup and MSODrawing records are implemented  */
    private var field_cbo_id: Int

    /** Number of following DV Records  */
    private var field_5_dv_no: Int

    constructor() {
        field_cbo_id = -0x1
        field_5_dv_no = 0x00000000
    }

    constructor(`in`: RecordInputStream) {
        field_1_options = `in`.readShort()
        field_2_horiz_pos = `in`.readInt()
        field_3_vert_pos = `in`.readInt()
        field_cbo_id = `in`.readInt()
        field_5_dv_no = `in`.readInt()
    }

    /**
     * @param options the options of the dialog
     */
    fun setOptions(options: Short) {
        field_1_options = options
    }

    /**
     * @param horiz_pos the Horizontal position of the dialog
     */
    fun setHorizontalPos(horiz_pos: Int) {
        field_2_horiz_pos = horiz_pos
    }

    /**
     * @param vert_pos the Vertical position of the dialog
     */
    fun setVerticalPos(vert_pos: Int) {
        field_3_vert_pos = vert_pos
    }

    /**
     * set the object ID of the drop down arrow object for list boxes
     * @param cboID - Object ID
     */
    fun setObjectID(cboID: Int) {
        field_cbo_id = cboID
    }

    /**
     * Set the number of following DV records
     * @param dvNo - the DV records number
     */
    fun setDVRecNo(dvNo: Int) {
        field_5_dv_no = dvNo
    }

    /**
     * @return the field_1_options
     */
    fun getOptions(): Short {
        return field_1_options
    }

    /**
     * @return the Horizontal position of the dialog
     */
    fun getHorizontalPos(): Int {
        return field_2_horiz_pos
    }

    /**
     * @return the the Vertical position of the dialog
     */
    fun getVerticalPos(): Int {
        return field_3_vert_pos
    }

    /**
     * get Object ID of the drop down arrow object for list boxes
     */
    fun getObjectID(): Int {
        return field_cbo_id
    }

    /**
     * Get number of following DV records
     */
    fun getDVRecNo(): Int {
        return field_5_dv_no
    }


    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[DVAL]\n")
        buffer.append("    .options      = ").append(getOptions().toInt()).append('\n')
        buffer.append("    .horizPos     = ").append(getHorizontalPos()).append('\n')
        buffer.append("    .vertPos      = ").append(getVerticalPos()).append('\n')
        buffer.append("    .comboObjectID   = ").append(Integer.toHexString(getObjectID()))
            .append("\n")
        buffer.append("    .DVRecordsNumber = ").append(Integer.toHexString(getDVRecNo()))
            .append("\n")
        buffer.append("[/DVAL]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(getOptions().toInt())
        out.writeInt(getHorizontalPos())
        out.writeInt(getVerticalPos())
        out.writeInt(getObjectID())
        out.writeInt(getDVRecNo())
    }

    override fun getDataSize(): Int {
        return 18
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = DVALRecord()
        rec.field_1_options = field_1_options
        rec.field_2_horiz_pos = field_2_horiz_pos
        rec.field_3_vert_pos = field_3_vert_pos
        rec.field_cbo_id = field_cbo_id
        rec.field_5_dv_no = field_5_dv_no
        return rec
    }

    companion object {
        const val sid: Short = 0x01B2
    }
}
