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

import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * Describes the frozen and unfozen panes.
 *
 *
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class PaneRecord : StandardRecord {
    private var field_1_x: Short = 0
    private var field_2_y: Short = 0
    private var field_3_topRow: Short = 0
    private var field_4_leftColumn: Short = 0
    private var field_5_activePane: Short = 0

    constructor()

    constructor(`in`: RecordInputStream) {
        field_1_x = `in`.readShort()
        field_2_y = `in`.readShort()
        field_3_topRow = `in`.readShort()
        field_4_leftColumn = `in`.readShort()
        field_5_activePane = `in`.readShort()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[PANE]\n")
        buffer.append("    .x                    = ")
            .append("0x").append(toHex(getX()))
            .append(" (").append(getX().toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .y                    = ")
            .append("0x").append(toHex(getY()))
            .append(" (").append(getY().toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .topRow               = ")
            .append("0x").append(toHex(getTopRow()))
            .append(" (").append(getTopRow().toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .leftColumn           = ")
            .append("0x").append(toHex(getLeftColumn()))
            .append(" (").append(getLeftColumn().toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .activePane           = ")
            .append("0x").append(toHex(getActivePane()))
            .append(" (").append(getActivePane().toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))

        buffer.append("[/PANE]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(field_1_x.toInt())
        out.writeShort(field_2_y.toInt())
        out.writeShort(field_3_topRow.toInt())
        out.writeShort(field_4_leftColumn.toInt())
        out.writeShort(field_5_activePane.toInt())
    }

    override fun getDataSize(): Int {
        return 2 + 2 + 2 + 2 + 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = PaneRecord()

        rec.field_1_x = field_1_x
        rec.field_2_y = field_2_y
        rec.field_3_topRow = field_3_topRow
        rec.field_4_leftColumn = field_4_leftColumn
        rec.field_5_activePane = field_5_activePane
        return rec
    }


    /**
     * Get the x field for the Pane record.
     */
    fun getX(): Short {
        return field_1_x
    }

    /**
     * Set the x field for the Pane record.
     */
    fun setX(field_1_x: Short) {
        this.field_1_x = field_1_x
    }

    /**
     * Get the y field for the Pane record.
     */
    fun getY(): Short {
        return field_2_y
    }

    /**
     * Set the y field for the Pane record.
     */
    fun setY(field_2_y: Short) {
        this.field_2_y = field_2_y
    }

    /**
     * Get the top row field for the Pane record.
     */
    fun getTopRow(): Short {
        return field_3_topRow
    }

    /**
     * Set the top row field for the Pane record.
     */
    fun setTopRow(field_3_topRow: Short) {
        this.field_3_topRow = field_3_topRow
    }

    /**
     * Get the left column field for the Pane record.
     */
    fun getLeftColumn(): Short {
        return field_4_leftColumn
    }

    /**
     * Set the left column field for the Pane record.
     */
    fun setLeftColumn(field_4_leftColumn: Short) {
        this.field_4_leftColumn = field_4_leftColumn
    }

    /**
     * Get the active pane field for the Pane record.
     * 
     * @return  One of
     * ACTIVE_PANE_LOWER_RIGHT
     * ACTIVE_PANE_UPPER_RIGHT
     * ACTIVE_PANE_LOWER_LEFT
     * ACTIVE_PANE_UPPER_LEFT
     */
    fun getActivePane(): Short {
        return field_5_activePane
    }

    /**
     * Set the active pane field for the Pane record.
     * 
     * @param field_5_activePane
     * One of
     * ACTIVE_PANE_LOWER_RIGHT
     * ACTIVE_PANE_UPPER_RIGHT
     * ACTIVE_PANE_LOWER_LEFT
     * ACTIVE_PANE_UPPER_LEFT
     */
    fun setActivePane(field_5_activePane: Short) {
        this.field_5_activePane = field_5_activePane
    }

    companion object {
        const val sid: Short = 0x41
        const val ACTIVE_PANE_LOWER_RIGHT: Short = 0
        const val ACTIVE_PANE_UPPER_RIGHT: Short = 1
        const val ACTIVE_PANE_LOWER_LEFT: Short = 2
        // TODO - remove obsolete field (it was deprecated May-2008 v3.1)

        @Deprecated("use ACTIVE_PANE_UPPER_LEFT ")
        const val ACTIVE_PANE_UPER_LEFT: Short = 3
        const val ACTIVE_PANE_UPPER_LEFT: Short = 3
    }
}
