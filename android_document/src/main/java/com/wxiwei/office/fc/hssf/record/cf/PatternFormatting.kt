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
package com.wxiwei.office.fc.hssf.record.cf

import com.wxiwei.office.fc.util.BitFieldFactory.Companion.getInstance
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Pattern Formatting Block of the Conditional Formatting Rule Record.
 * 
 * @author Dmitriy Kumshayev
 */
class PatternFormatting : Cloneable {
    // PATTERN FORMATING BLOCK
    // For Pattern Styles see constants at HSSFCellStyle (from NO_FILL to LEAST_DOTS)
    private var field_15_pattern_style: Int
    private var field_16_pattern_color_indexes: Int

    constructor() {
        field_15_pattern_style = 0
        field_16_pattern_color_indexes = 0
    }

    /** Creates new FontFormatting  */
    constructor(`in`: LittleEndianInput) {
        field_15_pattern_style = `in`.readUShort()
        field_16_pattern_color_indexes = `in`.readUShort()
    }

    var fillPattern: Int
        /**
         * @return fill pattern
         */
        get() = fillPatternStyle.getValue(
            field_15_pattern_style
        )
        /**
         * setting fill pattern
         * 
         * @see .NO_FILL
         * 
         * @see .SOLID_FOREGROUND
         * 
         * @see .FINE_DOTS
         * 
         * @see .ALT_BARS
         * 
         * @see .SPARSE_DOTS
         * 
         * @see .THICK_HORZ_BANDS
         * 
         * @see .THICK_VERT_BANDS
         * 
         * @see .THICK_BACKWARD_DIAG
         * 
         * @see .THICK_FORWARD_DIAG
         * 
         * @see .BIG_SPOTS
         * 
         * @see .BRICKS
         * 
         * @see .THIN_HORZ_BANDS
         * 
         * @see .THIN_VERT_BANDS
         * 
         * @see .THIN_BACKWARD_DIAG
         * 
         * @see .THIN_FORWARD_DIAG
         * 
         * @see .SQUARES
         * 
         * @see .DIAMONDS
         * 
         * 
         * @param fp  fill pattern
         */
        set(fp) {
            field_15_pattern_style =
                fillPatternStyle.setValue(
                    field_15_pattern_style,
                    fp
                )
        }

    var fillBackgroundColor: Int
        /**
         * @see com.wxiwei.office.fc.hssf.usermodel.HSSFPalette.getColor
         * @return get the background fill color
         */
        get() = patternBackgroundColorIndex.getValue(
            field_16_pattern_color_indexes
        )
        /**
         * set the background fill color.
         */
        set(bg) {
            field_16_pattern_color_indexes =
                patternBackgroundColorIndex.setValue(
                    field_16_pattern_color_indexes,
                    bg
                )
        }

    var fillForegroundColor: Int
        /**
         * @see com.wxiwei.office.fc.hssf.usermodel.HSSFPalette.getColor
         * @return get the foreground fill color
         */
        get() = patternColorIndex.getValue(
            field_16_pattern_color_indexes
        )
        /**
         * set the foreground fill color
         */
        set(fg) {
            field_16_pattern_color_indexes =
                patternColorIndex.setValue(
                    field_16_pattern_color_indexes,
                    fg
                )
        }

    override fun toString(): String {
        val buffer = StringBuffer()
        buffer.append("    [Pattern Formatting]\n")
        buffer.append("          .fillpattern= ").append(Integer.toHexString(this.fillPattern))
            .append("\n")
        buffer.append("          .fgcoloridx= ")
            .append(Integer.toHexString(this.fillForegroundColor)).append("\n")
        buffer.append("          .bgcoloridx= ")
            .append(Integer.toHexString(this.fillBackgroundColor)).append("\n")
        buffer.append("    [/Pattern Formatting]\n")
        return buffer.toString()
    }

    public override fun clone(): Any {
        val rec = PatternFormatting()
        rec.field_15_pattern_style = field_15_pattern_style
        rec.field_16_pattern_color_indexes = field_16_pattern_color_indexes
        return rec
    }

    fun serialize(out: LittleEndianOutput) {
        out.writeShort(field_15_pattern_style)
        out.writeShort(field_16_pattern_color_indexes)
    }

    companion object {
        /**  No background  */
        const val NO_FILL: Short = 0

        /**  Solidly filled  */
        const val SOLID_FOREGROUND: Short = 1

        /**  Small fine dots  */
        const val FINE_DOTS: Short = 2

        /**  Wide dots  */
        const val ALT_BARS: Short = 3

        /**  Sparse dots  */
        const val SPARSE_DOTS: Short = 4

        /**  Thick horizontal bands  */
        const val THICK_HORZ_BANDS: Short = 5

        /**  Thick vertical bands  */
        const val THICK_VERT_BANDS: Short = 6

        /**  Thick backward facing diagonals  */
        const val THICK_BACKWARD_DIAG: Short = 7

        /**  Thick forward facing diagonals  */
        const val THICK_FORWARD_DIAG: Short = 8

        /**  Large spots  */
        const val BIG_SPOTS: Short = 9

        /**  Brick-like layout  */
        const val BRICKS: Short = 10

        /**  Thin horizontal bands  */
        const val THIN_HORZ_BANDS: Short = 11

        /**  Thin vertical bands  */
        const val THIN_VERT_BANDS: Short = 12

        /**  Thin backward diagonal  */
        const val THIN_BACKWARD_DIAG: Short = 13

        /**  Thin forward diagonal  */
        const val THIN_FORWARD_DIAG: Short = 14

        /**  Squares  */
        const val SQUARES: Short = 15

        /**  Diamonds  */
        const val DIAMONDS: Short = 16

        /**  Less Dots  */
        const val LESS_DOTS: Short = 17

        /**  Least Dots  */
        const val LEAST_DOTS: Short = 18


        private val fillPatternStyle = getInstance(0xFC00)

        private val patternColorIndex = getInstance(0x007F)
        private val patternBackgroundColorIndex = getInstance(0x3F80)
    }
}
