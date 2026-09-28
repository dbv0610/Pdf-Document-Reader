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
package com.wxiwei.office.fc.hssf.record.chart

import com.wxiwei.office.fc.hssf.record.RecordInputStream
import com.wxiwei.office.fc.hssf.record.StandardRecord
import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * The series record describes the overall data for a series.
 *
 *
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class SeriesRecord : StandardRecord {
    /**
     * Get the category data type field for the Series record.
     * 
     * @return  One of
     * CATEGORY_DATA_TYPE_DATES
     * CATEGORY_DATA_TYPE_NUMERIC
     * CATEGORY_DATA_TYPE_SEQUENCE
     * CATEGORY_DATA_TYPE_TEXT
     */
    /**
     * Set the category data type field for the Series record.
     * 
     * @param field_1_categoryDataType
     * One of
     * CATEGORY_DATA_TYPE_DATES
     * CATEGORY_DATA_TYPE_NUMERIC
     * CATEGORY_DATA_TYPE_SEQUENCE
     * CATEGORY_DATA_TYPE_TEXT
     */
    var categoryDataType: Short = 0
    /**
     * Get the values data type field for the Series record.
     * 
     * @return  One of
     * VALUES_DATA_TYPE_DATES
     * VALUES_DATA_TYPE_NUMERIC
     * VALUES_DATA_TYPE_SEQUENCE
     * VALUES_DATA_TYPE_TEXT
     */
    /**
     * Set the values data type field for the Series record.
     * 
     * @param field_2_valuesDataType
     * One of
     * VALUES_DATA_TYPE_DATES
     * VALUES_DATA_TYPE_NUMERIC
     * VALUES_DATA_TYPE_SEQUENCE
     * VALUES_DATA_TYPE_TEXT
     */
    var valuesDataType: Short = 0
    /**
     * Get the num categories field for the Series record.
     */
    /**
     * Set the num categories field for the Series record.
     */
    var numCategories: Short = 0
    /**
     * Get the num values field for the Series record.
     */
    /**
     * Set the num values field for the Series record.
     */
    var numValues: Short = 0
    /**
     * Get the bubble series type field for the Series record.
     * 
     * @return  One of
     * BUBBLE_SERIES_TYPE_DATES
     * BUBBLE_SERIES_TYPE_NUMERIC
     * BUBBLE_SERIES_TYPE_SEQUENCE
     * BUBBLE_SERIES_TYPE_TEXT
     */
    /**
     * Set the bubble series type field for the Series record.
     * 
     * @param field_5_bubbleSeriesType
     * One of
     * BUBBLE_SERIES_TYPE_DATES
     * BUBBLE_SERIES_TYPE_NUMERIC
     * BUBBLE_SERIES_TYPE_SEQUENCE
     * BUBBLE_SERIES_TYPE_TEXT
     */
    var bubbleSeriesType: Short = 0
    /**
     * Get the num bubble values field for the Series record.
     */
    /**
     * Set the num bubble values field for the Series record.
     */
    var numBubbleValues: Short = 0


    constructor()

    constructor(`in`: RecordInputStream) {
        this.categoryDataType = `in`.readShort()
        this.valuesDataType = `in`.readShort()
        this.numCategories = `in`.readShort()
        this.numValues = `in`.readShort()
        this.bubbleSeriesType = `in`.readShort()
        this.numBubbleValues = `in`.readShort()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[SERIES]\n")
        buffer.append("    .categoryDataType     = ")
            .append("0x").append(toHex(this.categoryDataType))
            .append(" (").append(this.categoryDataType.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .valuesDataType       = ")
            .append("0x").append(toHex(this.valuesDataType))
            .append(" (").append(this.valuesDataType.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .numCategories        = ")
            .append("0x").append(toHex(this.numCategories))
            .append(" (").append(this.numCategories.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .numValues            = ")
            .append("0x").append(toHex(this.numValues))
            .append(" (").append(this.numValues.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .bubbleSeriesType     = ")
            .append("0x").append(toHex(this.bubbleSeriesType))
            .append(" (").append(this.bubbleSeriesType.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .numBubbleValues      = ")
            .append("0x").append(toHex(this.numBubbleValues))
            .append(" (").append(this.numBubbleValues.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))

        buffer.append("[/SERIES]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(categoryDataType.toInt())
        out.writeShort(valuesDataType.toInt())
        out.writeShort(numCategories.toInt())
        out.writeShort(numValues.toInt())
        out.writeShort(bubbleSeriesType.toInt())
        out.writeShort(numBubbleValues.toInt())
    }

    override fun getDataSize(): Int {
        return 2 + 2 + 2 + 2 + 2 + 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = SeriesRecord()

        rec.categoryDataType = this.categoryDataType
        rec.valuesDataType = this.valuesDataType
        rec.numCategories = this.numCategories
        rec.numValues = this.numValues
        rec.bubbleSeriesType = this.bubbleSeriesType
        rec.numBubbleValues = this.numBubbleValues
        return rec
    }


    companion object {
        const val sid: Short = 0x1003
        const val CATEGORY_DATA_TYPE_DATES: Short = 0
        const val CATEGORY_DATA_TYPE_NUMERIC: Short = 1
        const val CATEGORY_DATA_TYPE_SEQUENCE: Short = 2
        const val CATEGORY_DATA_TYPE_TEXT: Short = 3
        const val VALUES_DATA_TYPE_DATES: Short = 0
        const val VALUES_DATA_TYPE_NUMERIC: Short = 1
        const val VALUES_DATA_TYPE_SEQUENCE: Short = 2
        const val VALUES_DATA_TYPE_TEXT: Short = 3
        const val BUBBLE_SERIES_TYPE_DATES: Short = 0
        const val BUBBLE_SERIES_TYPE_NUMERIC: Short = 1
        const val BUBBLE_SERIES_TYPE_SEQUENCE: Short = 2
        const val BUBBLE_SERIES_TYPE_TEXT: Short = 3
    }
}
