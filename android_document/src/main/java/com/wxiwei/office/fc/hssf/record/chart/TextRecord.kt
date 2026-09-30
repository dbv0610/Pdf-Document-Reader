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
import com.wxiwei.office.fc.hssf.record.UnknownRecord
import com.wxiwei.office.fc.util.BitFieldFactory.Companion.getInstance
import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndian.getInt
import com.wxiwei.office.fc.util.LittleEndian.getShort
import com.wxiwei.office.fc.util.LittleEndian.getUnsignedByte
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * The text record is used to define text stored on a chart.
 *
 *
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class TextRecord : StandardRecord {
    /**
     * Get the horizontal alignment field for the Text record.
     * 
     * @return  One of
     * HORIZONTAL_ALIGNMENT_LEFT
     * HORIZONTAL_ALIGNMENT_CENTER
     * HORIZONTAL_ALIGNMENT_BOTTOM
     * HORIZONTAL_ALIGNMENT_JUSTIFY
     */
    /**
     * Set the horizontal alignment field for the Text record.
     * 
     * @param field_1_horizontalAlignment
     * One of
     * HORIZONTAL_ALIGNMENT_LEFT
     * HORIZONTAL_ALIGNMENT_CENTER
     * HORIZONTAL_ALIGNMENT_BOTTOM
     * HORIZONTAL_ALIGNMENT_JUSTIFY
     */
    var horizontalAlignment: Byte = 0
    /**
     * Get the vertical alignment field for the Text record.
     * 
     * @return  One of
     * VERTICAL_ALIGNMENT_TOP
     * VERTICAL_ALIGNMENT_CENTER
     * VERTICAL_ALIGNMENT_BOTTOM
     * VERTICAL_ALIGNMENT_JUSTIFY
     */
    /**
     * Set the vertical alignment field for the Text record.
     * 
     * @param field_2_verticalAlignment
     * One of
     * VERTICAL_ALIGNMENT_TOP
     * VERTICAL_ALIGNMENT_CENTER
     * VERTICAL_ALIGNMENT_BOTTOM
     * VERTICAL_ALIGNMENT_JUSTIFY
     */
    var verticalAlignment: Byte = 0
    /**
     * Get the display mode field for the Text record.
     * 
     * @return  One of
     * DISPLAY_MODE_TRANSPARENT
     * DISPLAY_MODE_OPAQUE
     */
    /**
     * Set the display mode field for the Text record.
     * 
     * @param field_3_displayMode
     * One of
     * DISPLAY_MODE_TRANSPARENT
     * DISPLAY_MODE_OPAQUE
     */
    var displayMode: Short = 0
    /**
     * Get the rgbColor field for the Text record.
     */
    /**
     * Set the rgbColor field for the Text record.
     */
    var rgbColor: Int = 0
    /**
     * Get the x field for the Text record.
     */
    /**
     * Set the x field for the Text record.
     */
    var x: Int = 0
    /**
     * Get the y field for the Text record.
     */
    /**
     * Set the y field for the Text record.
     */
    var y: Int = 0
    /**
     * Get the width field for the Text record.
     */
    /**
     * Set the width field for the Text record.
     */
    var width: Int = 0
    /**
     * Get the height field for the Text record.
     */
    /**
     * Set the height field for the Text record.
     */
    var height: Int = 0
    /**
     * Get the options1 field for the Text record.
     */
    /**
     * Set the options1 field for the Text record.
     */
    var options1: Short = 0
    /**
     * Get the index of color value field for the Text record.
     */
    /**
     * Set the index of color value field for the Text record.
     */
    var indexOfColorValue: Short = 0
    /**
     * Get the options2 field for the Text record.
     */
    /**
     * Set the options2 field for the Text record.
     */
    var options2: Short = 0
    /**
     * Get the text rotation field for the Text record.
     */
    /**
     * Set the text rotation field for the Text record.
     */
    var textRotation: Short = 0


    constructor()

    constructor(unknownRecord: UnknownRecord) {
        if (unknownRecord.getSid() == Companion.sid && unknownRecord.data.size == getDataSize()) {
            val data = unknownRecord.data

            this.horizontalAlignment = getUnsignedByte(data, 0).toByte()
            this.verticalAlignment = getUnsignedByte(data, 1).toByte()
            this.displayMode = getShort(data, 2)
            this.rgbColor = getInt(data, 4)
            this.x = getInt(data, 8)
            this.y = getInt(data, 12)
            this.width = getInt(data, 16)
            this.height = getInt(data, 20)
            this.options1 = getShort(data, 24)
            this.indexOfColorValue = getShort(data, 26)
            this.options2 = getShort(data, 28)
            this.textRotation = getShort(data, 30)
        }
    }

    constructor(`in`: RecordInputStream) {
        this.horizontalAlignment = `in`.readByte()
        this.verticalAlignment = `in`.readByte()
        this.displayMode = `in`.readShort()
        this.rgbColor = `in`.readInt()
        this.x = `in`.readInt()
        this.y = `in`.readInt()
        this.width = `in`.readInt()
        this.height = `in`.readInt()
        this.options1 = `in`.readShort()
        this.indexOfColorValue = `in`.readShort()
        this.options2 = `in`.readShort()
        this.textRotation = `in`.readShort()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[TEXT]\n")
        buffer.append("    .horizontalAlignment  = ")
            .append("0x").append(toHex(this.horizontalAlignment))
            .append(" (").append(this.horizontalAlignment.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .verticalAlignment    = ")
            .append("0x").append(toHex(this.verticalAlignment))
            .append(" (").append(this.verticalAlignment.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .displayMode          = ")
            .append("0x").append(toHex(this.displayMode))
            .append(" (").append(this.displayMode.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .rgbColor             = ")
            .append("0x").append(toHex(this.rgbColor))
            .append(" (").append(this.rgbColor).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .x                    = ")
            .append("0x").append(toHex(this.x))
            .append(" (").append(this.x).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .y                    = ")
            .append("0x").append(toHex(this.y))
            .append(" (").append(this.y).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .width                = ")
            .append("0x").append(toHex(this.width))
            .append(" (").append(this.width).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .height               = ")
            .append("0x").append(toHex(this.height))
            .append(" (").append(this.height).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .options1             = ")
            .append("0x").append(toHex(this.options1))
            .append(" (").append(this.options1.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("         .autoColor                = ").append(this.isAutoColor).append('\n')
        buffer.append("         .showKey                  = ").append(this.isShowKey).append('\n')
        buffer.append("         .showValue                = ").append(this.isShowValue).append('\n')
        buffer.append("         .vertical                 = ").append(this.isVertical).append('\n')
        buffer.append("         .autoGeneratedText        = ").append(this.isAutoGeneratedText)
            .append('\n')
        buffer.append("         .generated                = ").append(this.isGenerated).append('\n')
        buffer.append("         .autoLabelDeleted         = ").append(this.isAutoLabelDeleted)
            .append('\n')
        buffer.append("         .autoBackground           = ").append(this.isAutoBackground)
            .append('\n')
        buffer.append("         .rotation                 = ").append(this.rotation.toInt())
            .append('\n')
        buffer.append("         .showCategoryLabelAsPercentage     = ")
            .append(this.isShowCategoryLabelAsPercentage).append('\n')
        buffer.append("         .showValueAsPercentage     = ").append(this.isShowValueAsPercentage)
            .append('\n')
        buffer.append("         .showBubbleSizes          = ").append(this.isShowBubbleSizes)
            .append('\n')
        buffer.append("         .showLabel                = ").append(this.isShowLabel).append('\n')
        buffer.append("    .indexOfColorValue    = ")
            .append("0x").append(toHex(this.indexOfColorValue))
            .append(" (").append(this.indexOfColorValue.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .options2             = ")
            .append("0x").append(toHex(this.options2))
            .append(" (").append(this.options2.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("         .dataLabelPlacement       = ")
            .append(this.dataLabelPlacement.toInt()).append('\n')
        buffer.append("    .textRotation         = ")
            .append("0x").append(toHex(this.textRotation))
            .append(" (").append(this.textRotation.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))

        buffer.append("[/TEXT]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeByte(horizontalAlignment.toInt())
        out.writeByte(verticalAlignment.toInt())
        out.writeShort(displayMode.toInt())
        out.writeInt(this.rgbColor)
        out.writeInt(this.x)
        out.writeInt(this.y)
        out.writeInt(this.width)
        out.writeInt(this.height)
        out.writeShort(options1.toInt())
        out.writeShort(indexOfColorValue.toInt())
        out.writeShort(options2.toInt())
        out.writeShort(textRotation.toInt())
    }

    override fun getDataSize(): Int {
        return 1 + 1 + 2 + 4 + 4 + 4 + 4 + 4 + 2 + 2 + 2 + 2
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val rec = TextRecord()

        rec.horizontalAlignment = this.horizontalAlignment
        rec.verticalAlignment = this.verticalAlignment
        rec.displayMode = this.displayMode
        rec.rgbColor = this.rgbColor
        rec.x = this.x
        rec.y = this.y
        rec.width = this.width
        rec.height = this.height
        rec.options1 = this.options1
        rec.indexOfColorValue = this.indexOfColorValue
        rec.options2 = this.options2
        rec.textRotation = this.textRotation
        return rec
    }


    var isAutoColor: Boolean
        /**
         * true = automaticly selected colour, false = user-selected
         * @return  the auto color field value.
         */
        get() = autoColor.isSet(
            options1.toInt()
        )
        /**
         * Sets the auto color field value.
         * true = automaticly selected colour, false = user-selected
         */
        set(value) {
            this.options1 =
                autoColor.setShortBoolean(
                    this.options1,
                    value
                )
        }

    var isShowKey: Boolean
        /**
         * true = draw legend
         * @return  the show key field value.
         */
        get() = showKey.isSet(options1.toInt())
        /**
         * Sets the show key field value.
         * true = draw legend
         */
        set(value) {
            this.options1 =
                showKey.setShortBoolean(
                    this.options1,
                    value
                )
        }

    var isShowValue: Boolean
        /**
         * false = text is category label
         * @return  the show value field value.
         */
        get() = showValue.isSet(
            options1.toInt()
        )
        /**
         * Sets the show value field value.
         * false = text is category label
         */
        set(value) {
            this.options1 =
                showValue.setShortBoolean(
                    this.options1,
                    value
                )
        }

    var isVertical: Boolean
        /**
         * true = text is vertical
         * @return  the vertical field value.
         */
        get() = vertical.isSet(options1.toInt())
        /**
         * Sets the vertical field value.
         * true = text is vertical
         */
        set(value) {
            this.options1 =
                vertical.setShortBoolean(
                    this.options1,
                    value
                )
        }

    var isAutoGeneratedText: Boolean
        /**
         * 
         * @return  the auto generated text field value.
         */
        get() = autoGeneratedText.isSet(
            options1.toInt()
        )
        /**
         * Sets the auto generated text field value.
         * 
         */
        set(value) {
            this.options1 =
                autoGeneratedText.setShortBoolean(
                    this.options1,
                    value
                )
        }

    var isGenerated: Boolean
        /**
         * 
         * @return  the generated field value.
         */
        get() = generated.isSet(
            options1.toInt()
        )
        /**
         * Sets the generated field value.
         * 
         */
        set(value) {
            this.options1 =
                generated.setShortBoolean(
                    this.options1,
                    value
                )
        }

    var isAutoLabelDeleted: Boolean
        /**
         * 
         * @return  the auto label deleted field value.
         */
        get() = autoLabelDeleted.isSet(
            options1.toInt()
        )
        /**
         * Sets the auto label deleted field value.
         * 
         */
        set(value) {
            this.options1 =
                autoLabelDeleted.setShortBoolean(
                    this.options1,
                    value
                )
        }

    var isAutoBackground: Boolean
        /**
         * 
         * @return  the auto background field value.
         */
        get() = autoBackground.isSet(
            options1.toInt()
        )
        /**
         * Sets the auto background field value.
         * 
         */
        set(value) {
            this.options1 =
                autoBackground.setShortBoolean(
                    this.options1,
                    value
                )
        }

    var rotation: Short
        /**
         * 
         * @return  the rotation field value.
         */
        get() = Companion.rotation.getShortValue(
            this.options1
        )
        /**
         * Sets the rotation field value.
         * 
         */
        set(value) {
            this.options1 =
                Companion.rotation.setShortValue(
                    this.options1,
                    value
                )
        }

    var isShowCategoryLabelAsPercentage: Boolean
        /**
         * 
         * @return  the show category label as percentage field value.
         */
        get() = showCategoryLabelAsPercentage.isSet(
            options1.toInt()
        )
        /**
         * Sets the show category label as percentage field value.
         * 
         */
        set(value) {
            this.options1 =
                showCategoryLabelAsPercentage.setShortBoolean(
                    this.options1,
                    value
                )
        }

    var isShowValueAsPercentage: Boolean
        /**
         * 
         * @return  the show value as percentage field value.
         */
        get() = showValueAsPercentage.isSet(
            options1.toInt()
        )
        /**
         * Sets the show value as percentage field value.
         * 
         */
        set(value) {
            this.options1 =
                showValueAsPercentage.setShortBoolean(
                    this.options1,
                    value
                )
        }

    var isShowBubbleSizes: Boolean
        /**
         * 
         * @return  the show bubble sizes field value.
         */
        get() = showBubbleSizes.isSet(
            options1.toInt()
        )
        /**
         * Sets the show bubble sizes field value.
         * 
         */
        set(value) {
            this.options1 =
                showBubbleSizes.setShortBoolean(
                    this.options1,
                    value
                )
        }

    var isShowLabel: Boolean
        /**
         * 
         * @return  the show label field value.
         */
        get() = showLabel.isSet(
            options1.toInt()
        )
        /**
         * Sets the show label field value.
         * 
         */
        set(value) {
            this.options1 =
                showLabel.setShortBoolean(
                    this.options1,
                    value
                )
        }

    var dataLabelPlacement: Short
        /**
         * 
         * @return  the data label placement field value.
         */
        get() = Companion.dataLabelPlacement.getShortValue(
            this.options2
        )
        /**
         * Sets the data label placement field value.
         * 
         */
        set(value) {
            this.options2 =
                Companion.dataLabelPlacement.setShortValue(
                    this.options2,
                    value
                )
        }

    companion object {
        const val sid: Short = 0x1025

        private val dataLabelPlacement = getInstance(0x000F)
        private val autoColor = getInstance(0x0001)
        private val showKey = getInstance(0x0002)
        private val showValue = getInstance(0x0004)
        private val vertical = getInstance(0x0008)
        private val autoGeneratedText = getInstance(0x0010)
        private val generated = getInstance(0x0020)
        private val autoLabelDeleted = getInstance(0x0040)
        private val autoBackground = getInstance(0x0080)
        private val rotation = getInstance(0x0700)

        private val showCategoryLabelAsPercentage = getInstance(0x0800)
        private val showValueAsPercentage = getInstance(0x1000)
        private val showBubbleSizes = getInstance(0x2000)
        private val showLabel = getInstance(0x4000)


        const val HORIZONTAL_ALIGNMENT_LEFT: Byte = 1
        const val HORIZONTAL_ALIGNMENT_CENTER: Byte = 2
        const val HORIZONTAL_ALIGNMENT_BOTTOM: Byte = 3
        const val HORIZONTAL_ALIGNMENT_JUSTIFY: Byte = 4
        const val VERTICAL_ALIGNMENT_TOP: Byte = 1
        const val VERTICAL_ALIGNMENT_CENTER: Byte = 2
        const val VERTICAL_ALIGNMENT_BOTTOM: Byte = 3
        const val VERTICAL_ALIGNMENT_JUSTIFY: Byte = 4
        const val DISPLAY_MODE_TRANSPARENT: Short = 1
        const val DISPLAY_MODE_OPAQUE: Short = 2
        const val ROTATION_NONE: Short = 0
        const val ROTATION_TOP_TO_BOTTOM: Short = 1
        const val ROTATION_ROTATED_90_DEGREES: Short = 2
        const val ROTATION_ROTATED_90_DEGREES_CLOCKWISE: Short = 3
        const val DATA_LABEL_PLACEMENT_CHART_DEPENDENT: Short = 0
        const val DATA_LABEL_PLACEMENT_OUTSIDE: Short = 1
        const val DATA_LABEL_PLACEMENT_INSIDE: Short = 2
        const val DATA_LABEL_PLACEMENT_CENTER: Short = 3
        const val DATA_LABEL_PLACEMENT_AXIS: Short = 4
        const val DATA_LABEL_PLACEMENT_ABOVE: Short = 5
        const val DATA_LABEL_PLACEMENT_BELOW: Short = 6
        const val DATA_LABEL_PLACEMENT_LEFT: Short = 7
        const val DATA_LABEL_PLACEMENT_RIGHT: Short = 8
        const val DATA_LABEL_PLACEMENT_AUTO: Short = 9
        const val DATA_LABEL_PLACEMENT_USER_MOVED: Short = 10
    }
}
