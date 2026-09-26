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
import com.wxiwei.office.fc.util.LittleEndian.putInt
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Border Formatting Block of the Conditional Formatting Rule Record.
 * 
 * @author Dmitriy Kumshayev
 */
class BorderFormatting : Cloneable {
    // BORDER FORMATTING BLOCK
    // For Border Line Style codes see HSSFCellStyle.BORDER_XXXXXX
    private var field_13_border_styles1: Int
    private var field_14_border_styles2: Int

    constructor() {
        field_13_border_styles1 = 0
        field_14_border_styles2 = 0
    }

    /** Creates new FontFormatting  */
    constructor(`in`: LittleEndianInput) {
        field_13_border_styles1 = `in`.readInt()
        field_14_border_styles2 = `in`.readInt()
    }


    var borderLeft: Int
        /**
         * get the type of border to use for the left border of the cell
         * @return border type
         * @see .BORDER_NONE
         * 
         * @see .BORDER_THIN
         * 
         * @see .BORDER_MEDIUM
         * 
         * @see .BORDER_DASHED
         * 
         * @see .BORDER_DOTTED
         * 
         * @see .BORDER_THICK
         * 
         * @see .BORDER_DOUBLE
         * 
         * @see .BORDER_HAIR
         * 
         * @see .BORDER_MEDIUM_DASHED
         * 
         * @see .BORDER_DASH_DOT
         * 
         * @see .BORDER_MEDIUM_DASH_DOT
         * 
         * @see .BORDER_DASH_DOT_DOT
         * 
         * @see .BORDER_MEDIUM_DASH_DOT_DOT
         * 
         * @see .BORDER_SLANTED_DASH_DOT
         */
        get() = bordLeftLineStyle.getValue(
            field_13_border_styles1
        )
        /**
         * set the type of border to use for the left border of the cell
         * @param border type
         * @see .BORDER_NONE
         * 
         * @see .BORDER_THIN
         * 
         * @see .BORDER_MEDIUM
         * 
         * @see .BORDER_DASHED
         * 
         * @see .BORDER_DOTTED
         * 
         * @see .BORDER_THICK
         * 
         * @see .BORDER_DOUBLE
         * 
         * @see .BORDER_HAIR
         * 
         * @see .BORDER_MEDIUM_DASHED
         * 
         * @see .BORDER_DASH_DOT
         * 
         * @see .BORDER_MEDIUM_DASH_DOT
         * 
         * @see .BORDER_DASH_DOT_DOT
         * 
         * @see .BORDER_MEDIUM_DASH_DOT_DOT
         * 
         * @see .BORDER_SLANTED_DASH_DOT
         */
        set(border) {
            field_13_border_styles1 =
                bordLeftLineStyle.setValue(
                    field_13_border_styles1,
                    border
                )
        }

    var borderRight: Int
        /**
         * get the type of border to use for the right border of the cell
         * @return border type
         * @see .BORDER_NONE
         * 
         * @see .BORDER_THIN
         * 
         * @see .BORDER_MEDIUM
         * 
         * @see .BORDER_DASHED
         * 
         * @see .BORDER_DOTTED
         * 
         * @see .BORDER_THICK
         * 
         * @see .BORDER_DOUBLE
         * 
         * @see .BORDER_HAIR
         * 
         * @see .BORDER_MEDIUM_DASHED
         * 
         * @see .BORDER_DASH_DOT
         * 
         * @see .BORDER_MEDIUM_DASH_DOT
         * 
         * @see .BORDER_DASH_DOT_DOT
         * 
         * @see .BORDER_MEDIUM_DASH_DOT_DOT
         * 
         * @see .BORDER_SLANTED_DASH_DOT
         */
        get() = bordRightLineStyle.getValue(
            field_13_border_styles1
        )
        /**
         * set the type of border to use for the right border of the cell
         * @param border type
         * @see .BORDER_NONE
         * 
         * @see .BORDER_THIN
         * 
         * @see .BORDER_MEDIUM
         * 
         * @see .BORDER_DASHED
         * 
         * @see .BORDER_DOTTED
         * 
         * @see .BORDER_THICK
         * 
         * @see .BORDER_DOUBLE
         * 
         * @see .BORDER_HAIR
         * 
         * @see .BORDER_MEDIUM_DASHED
         * 
         * @see .BORDER_DASH_DOT
         * 
         * @see .BORDER_MEDIUM_DASH_DOT
         * 
         * @see .BORDER_DASH_DOT_DOT
         * 
         * @see .BORDER_MEDIUM_DASH_DOT_DOT
         * 
         * @see .BORDER_SLANTED_DASH_DOT
         */
        set(border) {
            field_13_border_styles1 =
                bordRightLineStyle.setValue(
                    field_13_border_styles1,
                    border
                )
        }

    var borderTop: Int
        /**
         * get the type of border to use for the top border of the cell
         * @return border type
         * @see .BORDER_NONE
         * 
         * @see .BORDER_THIN
         * 
         * @see .BORDER_MEDIUM
         * 
         * @see .BORDER_DASHED
         * 
         * @see .BORDER_DOTTED
         * 
         * @see .BORDER_THICK
         * 
         * @see .BORDER_DOUBLE
         * 
         * @see .BORDER_HAIR
         * 
         * @see .BORDER_MEDIUM_DASHED
         * 
         * @see .BORDER_DASH_DOT
         * 
         * @see .BORDER_MEDIUM_DASH_DOT
         * 
         * @see .BORDER_DASH_DOT_DOT
         * 
         * @see .BORDER_MEDIUM_DASH_DOT_DOT
         * 
         * @see .BORDER_SLANTED_DASH_DOT
         */
        get() = bordTopLineStyle.getValue(
            field_13_border_styles1
        )
        /**
         * set the type of border to use for the top border of the cell
         * @param border type
         * @see .BORDER_NONE
         * 
         * @see .BORDER_THIN
         * 
         * @see .BORDER_MEDIUM
         * 
         * @see .BORDER_DASHED
         * 
         * @see .BORDER_DOTTED
         * 
         * @see .BORDER_THICK
         * 
         * @see .BORDER_DOUBLE
         * 
         * @see .BORDER_HAIR
         * 
         * @see .BORDER_MEDIUM_DASHED
         * 
         * @see .BORDER_DASH_DOT
         * 
         * @see .BORDER_MEDIUM_DASH_DOT
         * 
         * @see .BORDER_DASH_DOT_DOT
         * 
         * @see .BORDER_MEDIUM_DASH_DOT_DOT
         * 
         * @see .BORDER_SLANTED_DASH_DOT
         */
        set(border) {
            field_13_border_styles1 =
                bordTopLineStyle.setValue(
                    field_13_border_styles1,
                    border
                )
        }

    var borderBottom: Int
        /**
         * get the type of border to use for the bottom border of the cell
         * @return border type
         * @see .BORDER_NONE
         * 
         * @see .BORDER_THIN
         * 
         * @see .BORDER_MEDIUM
         * 
         * @see .BORDER_DASHED
         * 
         * @see .BORDER_DOTTED
         * 
         * @see .BORDER_THICK
         * 
         * @see .BORDER_DOUBLE
         * 
         * @see .BORDER_HAIR
         * 
         * @see .BORDER_MEDIUM_DASHED
         * 
         * @see .BORDER_DASH_DOT
         * 
         * @see .BORDER_MEDIUM_DASH_DOT
         * 
         * @see .BORDER_DASH_DOT_DOT
         * 
         * @see .BORDER_MEDIUM_DASH_DOT_DOT
         * 
         * @see .BORDER_SLANTED_DASH_DOT
         */
        get() = bordBottomLineStyle.getValue(
            field_13_border_styles1
        )
        /**
         * set the type of border to use for the bottom border of the cell
         * @param border type
         * @see .BORDER_NONE
         * 
         * @see .BORDER_THIN
         * 
         * @see .BORDER_MEDIUM
         * 
         * @see .BORDER_DASHED
         * 
         * @see .BORDER_DOTTED
         * 
         * @see .BORDER_THICK
         * 
         * @see .BORDER_DOUBLE
         * 
         * @see .BORDER_HAIR
         * 
         * @see .BORDER_MEDIUM_DASHED
         * 
         * @see .BORDER_DASH_DOT
         * 
         * @see .BORDER_MEDIUM_DASH_DOT
         * 
         * @see .BORDER_DASH_DOT_DOT
         * 
         * @see .BORDER_MEDIUM_DASH_DOT_DOT
         * 
         * @see .BORDER_SLANTED_DASH_DOT
         */
        set(border) {
            field_13_border_styles1 =
                bordBottomLineStyle.setValue(
                    field_13_border_styles1,
                    border
                )
        }

    var borderDiagonal: Int
        /**
         * get the type of border to use for the diagonal border of the cell
         * @return border type
         * @see .BORDER_NONE
         * 
         * @see .BORDER_THIN
         * 
         * @see .BORDER_MEDIUM
         * 
         * @see .BORDER_DASHED
         * 
         * @see .BORDER_DOTTED
         * 
         * @see .BORDER_THICK
         * 
         * @see .BORDER_DOUBLE
         * 
         * @see .BORDER_HAIR
         * 
         * @see .BORDER_MEDIUM_DASHED
         * 
         * @see .BORDER_DASH_DOT
         * 
         * @see .BORDER_MEDIUM_DASH_DOT
         * 
         * @see .BORDER_DASH_DOT_DOT
         * 
         * @see .BORDER_MEDIUM_DASH_DOT_DOT
         * 
         * @see .BORDER_SLANTED_DASH_DOT
         */
        get() = bordDiagLineStyle.getValue(
            field_14_border_styles2
        )
        /**
         * set the type of border to use for the diagonal border of the cell
         * @param border type
         * @see .BORDER_NONE
         * 
         * @see .BORDER_THIN
         * 
         * @see .BORDER_MEDIUM
         * 
         * @see .BORDER_DASHED
         * 
         * @see .BORDER_DOTTED
         * 
         * @see .BORDER_THICK
         * 
         * @see .BORDER_DOUBLE
         * 
         * @see .BORDER_HAIR
         * 
         * @see .BORDER_MEDIUM_DASHED
         * 
         * @see .BORDER_DASH_DOT
         * 
         * @see .BORDER_MEDIUM_DASH_DOT
         * 
         * @see .BORDER_DASH_DOT_DOT
         * 
         * @see .BORDER_MEDIUM_DASH_DOT_DOT
         * 
         * @see .BORDER_SLANTED_DASH_DOT
         */
        set(border) {
            field_14_border_styles2 =
                bordDiagLineStyle.setValue(
                    field_14_border_styles2,
                    border
                )
        }

    var leftBorderColor: Int
        /**
         * get the color to use for the left border
         * @see com.wxiwei.office.fc.hssf.usermodel.HSSFPalette.getColor
         * @return  The index of the color definition
         */
        get() = bordLeftLineColor.getValue(
            field_13_border_styles1
        )
        /**
         * set the color to use for the left border
         * @param color The index of the color definition
         */
        set(color) {
            field_13_border_styles1 =
                bordLeftLineColor.setValue(
                    field_13_border_styles1,
                    color
                )
        }

    var rightBorderColor: Int
        /**
         * get the color to use for the right border
         * @see com.wxiwei.office.fc.hssf.usermodel.HSSFPalette.getColor
         * @return The index of the color definition
         */
        get() = bordRightLineColor.getValue(
            field_13_border_styles1
        )
        /**
         * set the color to use for the right border
         * @param color The index of the color definition
         */
        set(color) {
            field_13_border_styles1 =
                bordRightLineColor.setValue(
                    field_13_border_styles1,
                    color
                )
        }

    var topBorderColor: Int
        /**
         * get the color to use for the top border
         * @see com.wxiwei.office.fc.hssf.usermodel.HSSFPalette.getColor
         * @return The index of the color definition
         */
        get() = bordTopLineColor.getValue(
            field_14_border_styles2
        )
        /**
         * set the color to use for the top border
         * @param color The index of the color definition
         */
        set(color) {
            field_14_border_styles2 =
                bordTopLineColor.setValue(
                    field_14_border_styles2,
                    color
                )
        }

    var bottomBorderColor: Int
        /**
         * get the color to use for the bottom border
         * @see com.wxiwei.office.fc.hssf.usermodel.HSSFPalette.getColor
         * @return The index of the color definition
         */
        get() = bordBottomLineColor.getValue(
            field_14_border_styles2
        )
        /**
         * set the color to use for the bottom border
         * @param color The index of the color definition
         */
        set(color) {
            field_14_border_styles2 =
                bordBottomLineColor.setValue(
                    field_14_border_styles2,
                    color
                )
        }

    var diagonalBorderColor: Int
        /**
         * get the color to use for the diagonal border
         * @see com.wxiwei.office.fc.hssf.usermodel.HSSFPalette.getColor
         * @return The index of the color definition
         */
        get() = bordDiagLineColor.getValue(
            field_14_border_styles2
        )
        /**
         * set the color to use for the diagonal borders
         * @param color The index of the color definition
         */
        set(color) {
            field_14_border_styles2 =
                bordDiagLineColor.setValue(
                    field_14_border_styles2,
                    color
                )
        }

    var isForwardDiagonalOn: Boolean
        /**
         * @return `true` if forward diagonal is on
         */
        get() = bordBlTrtLineOnOff.isSet(
            field_13_border_styles1
        )
        /**
         * Of/off bottom left to top right line
         * 
         * @param on - if `true` - on, otherwise off
         */
        set(on) {
            field_13_border_styles1 =
                bordBlTrtLineOnOff.setBoolean(
                    field_13_border_styles1,
                    on
                )
        }

    var isBackwardDiagonalOn: Boolean
        /**
         * @return `true` if backward diagonal is on
         */
        get() = bordTlBrLineOnOff.isSet(
            field_13_border_styles1
        )
        /**
         * Of/off top left to bottom right line
         * 
         * @param on - if `true` - on, otherwise off
         */
        set(on) {
            field_13_border_styles1 =
                bordTlBrLineOnOff.setBoolean(
                    field_13_border_styles1,
                    on
                )
        }


    override fun toString(): String {
        val buffer = StringBuffer()
        buffer.append("    [Border Formatting]\n")
        buffer.append("          .lftln     = ").append(Integer.toHexString(this.borderLeft))
            .append("\n")
        buffer.append("          .rgtln     = ").append(Integer.toHexString(this.borderRight))
            .append("\n")
        buffer.append("          .topln     = ").append(Integer.toHexString(this.borderTop))
            .append("\n")
        buffer.append("          .btmln     = ").append(Integer.toHexString(this.borderBottom))
            .append("\n")
        buffer.append("          .leftborder= ").append(Integer.toHexString(this.leftBorderColor))
            .append("\n")
        buffer.append("          .rghtborder= ").append(Integer.toHexString(this.rightBorderColor))
            .append("\n")
        buffer.append("          .topborder= ").append(Integer.toHexString(this.topBorderColor))
            .append("\n")
        buffer.append("          .bottomborder= ")
            .append(Integer.toHexString(this.bottomBorderColor)).append("\n")
        buffer.append("          .fwdiag= ").append(this.isForwardDiagonalOn).append("\n")
        buffer.append("          .bwdiag= ").append(this.isBackwardDiagonalOn).append("\n")
        buffer.append("    [/Border Formatting]\n")
        return buffer.toString()
    }

    public override fun clone(): Any {
        val rec = BorderFormatting()
        rec.field_13_border_styles1 = field_13_border_styles1
        rec.field_14_border_styles2 = field_14_border_styles2
        return rec
    }

    fun serialize(offset: Int, data: ByteArray): Int {
        putInt(data, offset + 0, field_13_border_styles1)
        putInt(data, offset + 4, field_14_border_styles2)
        return 8
    }

    fun serialize(out: LittleEndianOutput) {
        out.writeInt(field_13_border_styles1)
        out.writeInt(field_14_border_styles2)
    }

    companion object {
        /** No border  */
        const val BORDER_NONE: Short = 0x0

        /** Thin border  */
        const val BORDER_THIN: Short = 0x1

        /** Medium border  */
        const val BORDER_MEDIUM: Short = 0x2

        /** dash border  */
        const val BORDER_DASHED: Short = 0x3

        /** dot border  */
        const val BORDER_HAIR: Short = 0x4

        /** Thick border  */
        const val BORDER_THICK: Short = 0x5

        /** double-line border  */
        const val BORDER_DOUBLE: Short = 0x6

        /** hair-line border  */
        const val BORDER_DOTTED: Short = 0x7

        /** Medium dashed border  */
        const val BORDER_MEDIUM_DASHED: Short = 0x8

        /** dash-dot border  */
        const val BORDER_DASH_DOT: Short = 0x9

        /** medium dash-dot border  */
        const val BORDER_MEDIUM_DASH_DOT: Short = 0xA

        /** dash-dot-dot border  */
        const val BORDER_DASH_DOT_DOT: Short = 0xB

        /** medium dash-dot-dot border  */
        const val BORDER_MEDIUM_DASH_DOT_DOT: Short = 0xC

        /** slanted dash-dot border  */
        const val BORDER_SLANTED_DASH_DOT: Short = 0xD

        private val bordLeftLineStyle = getInstance(0x0000000F)
        private val bordRightLineStyle = getInstance(0x000000F0)
        private val bordTopLineStyle = getInstance(0x00000F00)
        private val bordBottomLineStyle = getInstance(0x0000F000)
        private val bordLeftLineColor = getInstance(0x007F0000)
        private val bordRightLineColor = getInstance(0x3F800000)
        private val bordTlBrLineOnOff = getInstance(0x40000000)
        private val bordBlTrtLineOnOff = getInstance(-0x80000000)

        private val bordTopLineColor = getInstance(0x0000007F)
        private val bordBottomLineColor = getInstance(0x00003f80)
        private val bordDiagLineColor = getInstance(0x001FC000)
        private val bordDiagLineStyle = getInstance(0x01E00000)
    }
}
