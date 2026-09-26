/*
 * 文件名称:          CellStyle.java
 *
 * 编译器:            android2.2
 * 时间:              下午3:56:43
 */
package com.wxiwei.office.ss.model.style

import com.wxiwei.office.common.bg.BackgroundAndFill

/**
 * Cell style
 *
 * Read版本:        Read V1.0
 * 作者:            ljj8494
 * 日期:            2012-2-16
 * 负责人:          ljj8494
 */
class CellStyle
/**
 * 构造器
 */
{
    // style index
    private var index: Short = 0

    private var numFmt: NumberFormat? = null

    // font index of this cell
    private var fontIndex: Short = 0

    // hidden of this cell
    private var isHidden = false
    // locked of this cell
    private var isLocked = false

    private var alignment: Alignment? = null

    private var cellBorder: CellBorder? = null

    private var fill: BackgroundAndFill? = null

    /**
     * @return Returns the index.
     */
    fun getIndex(): Short = index

    /**
     * @param index The index to set.
     */
    fun setIndex(index: Short) {
        this.index = index
    }

    private fun checkDataFormat() {
        if (numFmt == null) {
            numFmt = NumberFormat()
        }
    }

    /**
     *
     * @param numFmt
     */
    fun setNumberFormat(numFmt: NumberFormat?) {
        this.numFmt = numFmt
    }

    /**
     *
     * @param id
     */
    fun setNumberFormatID(id: Short) {
        checkDataFormat()
        numFmt!!.setNumberFormatID(id)
    }

    /**
     * @return Returns the dataFormatIndex.
     */
    fun getNumberFormatID(): Short {
        checkDataFormat()
        return numFmt!!.getNumberFormatID()
    }

    /**
     *
     * @param formatCode
     */
    fun setFormatCode(formatCode: String?) {
        checkDataFormat()
        numFmt!!.setFormatCode(formatCode)
    }

    /**
     * @return Returns the dataFormatString.
     */
    fun getFormatCode(): String? {
        checkDataFormat()
        return numFmt!!.getFormatCode()
    }

    /**
     * @return Returns the fontIndex.
     */
    fun getFontIndex(): Short = fontIndex

    /**
     * @param fontIndex The fontIndex to set.
     */
    fun setFontIndex(fontIndex: Short) {
        this.fontIndex = fontIndex
    }

    /**
     * @return Returns the isHidden.
     */
    fun isHidden(): Boolean = isHidden

    /**
     * @param isHidden The isHidden to set.
     */
    fun setHidden(isHidden: Boolean) {
        this.isHidden = isHidden
    }

    /**
     * @return Returns the isLocked.
     */
    fun isLocked(): Boolean = isLocked

    /**
     * @param isLocked The isLocked to set.
     */
    fun setLocked(isLocked: Boolean) {
        this.isLocked = isLocked
    }

    private fun checkAlignmeng() {
        if (alignment == null) {
            alignment = Alignment()
        }
    }

    /**
     * @return Returns the isWrapText.
     */
    fun isWrapText(): Boolean {
        checkAlignmeng()
        return alignment!!.isWrapText()
                || alignment!!.getHorizontalAlign() == ALIGN_JUSTIFY
                || alignment!!.getVerticalAlign() == VERTICAL_JUSTIFY
    }

    /**
     * @param isWrapText The isWrapText to set.
     */
    fun setWrapText(isWrapText: Boolean) {
        checkAlignmeng()
        alignment!!.setWrapText(isWrapText)
    }

    /**
     * @return Returns the horAlign.
     */
    fun getHorizontalAlign(): Short {
        checkAlignmeng()
        return alignment!!.getHorizontalAlign()
    }

    /**
     * @param horAlign The horAlign to set.
     */
    fun setHorizontalAlign(horAlign: String?) {
        checkAlignmeng()
        if (horAlign == null || horAlign.equals("general", ignoreCase = true)) {
            alignment!!.setHorizontalAlign(ALIGN_GENERAL)
        } else if (horAlign.equals("left", ignoreCase = true)) {
            alignment!!.setHorizontalAlign(ALIGN_LEFT)
        } else if (horAlign.equals("center", ignoreCase = true)) {
            alignment!!.setHorizontalAlign(ALIGN_CENTER)
        } else if (horAlign.equals("right", ignoreCase = true)) {
            alignment!!.setHorizontalAlign(ALIGN_RIGHT)
        } else if (horAlign.equals("fill", ignoreCase = true)) {
            alignment!!.setHorizontalAlign(ALIGN_FILL)
        } else if (horAlign.equals("justify", ignoreCase = true)) {
            alignment!!.setHorizontalAlign(ALIGN_JUSTIFY)
        } else if (horAlign.equals("distributed", ignoreCase = true)) {
            alignment!!.setHorizontalAlign(ALIGN_JUSTIFY)
        }
    }

    /**
     * @param horAlign The horAlign to set.
     */
    fun setHorizontalAlign(horAlign: Short) {
        checkAlignmeng()
        alignment!!.setHorizontalAlign(horAlign)
    }

    /**
     * @return Returns the verAlign.
     */
    fun getVerticalAlign(): Short {
        checkAlignmeng()
        return alignment!!.getVerticalAlign()
    }

    /**
     * @param verAlign The verAlign to set.
     */
    fun setVerticalAlign(verAlign: String?) {
        checkAlignmeng()
        // NOTE: original Java dereferenced verAlign before its null check (NPE on null)
        if (verAlign!!.equals("top", ignoreCase = true)) {
            alignment!!.setVerticalAlign(VERTICAL_TOP)
        } else if (verAlign.equals("center", ignoreCase = true)) {
            alignment!!.setVerticalAlign(VERTICAL_CENTER)
        } else if (verAlign.equals("bottom", ignoreCase = true)) {
            alignment!!.setVerticalAlign(VERTICAL_BOTTOM)
        } else if (verAlign.equals("justify", ignoreCase = true)) {
            alignment!!.setVerticalAlign(VERTICAL_JUSTIFY)
        } else if (verAlign.equals("distributed", ignoreCase = true)) {
            alignment!!.setVerticalAlign(VERTICAL_JUSTIFY)
        }
    }

    /**
     * @param verAlign The verAlign to set.
     */
    fun setVerticalAlign(verAlign: Short) {
        checkAlignmeng()
        alignment!!.setVerticalAlign(verAlign)
    }

    /**
     * @return Returns the rotation.
     */
    fun getRotation(): Short {
        checkAlignmeng()
        return alignment!!.getRotaion()
    }

    /**
     * @param rotation The rotation to set.
     */
    fun setRotation(rotation: Short) {
        checkAlignmeng()
        alignment!!.setRotation(rotation)
    }

    /**
     * @return Returns the indent.
     */
    fun getIndent(): Short {
        checkAlignmeng()
        return alignment!!.getIndent()
    }

    /**
     * @param indent The indent to set.
     */
    fun setIndent(indent: Short) {
        checkAlignmeng()
        alignment!!.setIndent(indent)
    }

    private fun checkBorder() {
        if (cellBorder == null) {
            cellBorder = CellBorder()
        }
    }

    /**
     *
     * @param cellBorder
     */
    fun setBorder(cellBorder: CellBorder?) {
        this.cellBorder = cellBorder
    }

    /**
     * @return Returns the borderLeft.
     */
    fun getBorderLeft(): Short {
        checkBorder()
        return cellBorder!!.getLeftBorder()!!.getStyle()
    }

    /**
     * @param borderLeft The borderLeft to set.
     */
    fun setBorderLeft(borderLeft: Short) {
        checkBorder()
        cellBorder!!.getLeftBorder()!!.setStyle(borderLeft)
    }

    /**
     * @return Returns the borderLeftColorIdx.
     */
    fun getBorderLeftColorIdx(): Short {
        checkBorder()
        return cellBorder!!.getLeftBorder()!!.getColor()
    }

    /**
     * @param borderLeftColorIdx The borderLeftColorIdx to set.
     */
    fun setBorderLeftColorIdx(borderLeftColorIdx: Short) {
        checkBorder()
        cellBorder!!.getLeftBorder()!!.setColor(borderLeftColorIdx)
    }

    /**
     * @return Returns the borderRight.
     */
    fun getBorderRight(): Short {
        checkBorder()
        return cellBorder!!.getRightBorder()!!.getStyle()
    }

    /**
     * @param borderRight The borderRight to set.
     */
    fun setBorderRight(borderRight: Short) {
        checkBorder()
        cellBorder!!.getRightBorder()!!.setStyle(borderRight)
    }

    /**
     * @return Returns the borderRightColorIdx.
     */
    fun getBorderRightColorIdx(): Short {
        checkBorder()
        return cellBorder!!.getRightBorder()!!.getColor()
    }

    /**
     * @param borderRightColorIdx The borderRightColorIdx to set.
     */
    fun setBorderRightColorIdx(borderRightColorIdx: Short) {
        checkBorder()
        cellBorder!!.getRightBorder()!!.setColor(borderRightColorIdx)
    }

    /**
     * @return Returns the borderTop.
     */
    fun getBorderTop(): Short {
        checkBorder()
        return cellBorder!!.getTopBorder()!!.getStyle()
    }

    /**
     * @param borderTop The borderTop to set.
     */
    fun setBorderTop(borderTop: Short) {
        checkBorder()
        cellBorder!!.getTopBorder()!!.setStyle(borderTop)
    }

    /**
     * @return Returns the borderTopColorIdx.
     */
    fun getBorderTopColorIdx(): Short {
        checkBorder()
        return cellBorder!!.getTopBorder()!!.getColor()
    }

    /**
     * @param borderTopColorIdx The borderTopColorIdx to set.
     */
    fun setBorderTopColorIdx(borderTopColorIdx: Short) {
        checkBorder()
        cellBorder!!.getTopBorder()!!.setColor(borderTopColorIdx)
    }

    /**
     * @return Returns the borderBottom.
     */
    fun getBorderBottom(): Short {
        checkBorder()
        return cellBorder!!.getBottomBorder()!!.getStyle()
    }

    /**
     * @param borderBottom The borderBottom to set.
     */
    fun setBorderBottom(borderBottom: Short) {
        checkBorder()
        cellBorder!!.getBottomBorder()!!.setStyle(borderBottom)
    }

    /**
     * @return Returns the borderBottomColorIdx.
     */
    fun getBorderBottomColorIdx(): Short {
        checkBorder()
        return cellBorder!!.getBottomBorder()!!.getColor()
    }

    /**
     * @param borderBottomColorIdx The borderBottomColorIdx to set.
     */
    fun setBorderBottomColorIdx(borderBottomColorIdx: Short) {
        checkBorder()
        cellBorder!!.getBottomBorder()!!.setColor(borderBottomColorIdx)
    }

    private fun checkFillPattern() {
        if (fill == null) {
            fill = BackgroundAndFill()
            fill!!.setFillType(BackgroundAndFill.FILL_NO)
        }
    }

    /**
     * @param fill
     */
    fun setFillPattern(fill: BackgroundAndFill?) {
        this.fill = fill
    }

    fun getFillPattern(): BackgroundAndFill? = fill

    /**
     *
     * @param type
     */
    fun setFillPatternType(type: Byte) {
        checkFillPattern()
        fill!!.setFillType(type)
    }

    /**
     * @return Returns the filePattern.
     */
    fun getFillPatternType(): Byte {
        checkFillPattern()
        return fill!!.getFillType()
    }

    /**
     *
     * @param color
     */
    fun setBgColor(color: Int) {
        checkFillPattern()
        fill!!.setBackgoundColor(color)
    }

    /**
     * @return Returns the bgColorIndex.
     */
    fun getBgColor(): Int {
        checkFillPattern()
        return fill!!.getBackgoundColor()
    }

    fun setFgColor(color: Int) {
        checkFillPattern()
        fill!!.setForegroundColor(color)
    }

    /**
     * @return Returns the fgColorIndex.
     */
    fun getFgColor(): Int {
        checkFillPattern()
        return fill!!.getForegroundColor()
    }

    /**
     *
     */
    fun dispose() {
        numFmt = null
        fill = null

        if (cellBorder != null) {
            cellBorder!!.dispose()
            cellBorder = null
        }

        if (alignment != null) {
            alignment!!.dispose()
            alignment = null
        }
    }

    companion object {
        // general (normal) horizontal alignment
        const val ALIGN_GENERAL: Short = 0x00
        // left-justified horizontal alignment
        const val ALIGN_LEFT: Short = 0x01
        // center horizontal alignment
        const val ALIGN_CENTER: Short = 0x02
        // right-justified horizontal alignment
        const val ALIGN_RIGHT: Short = 0x03
        // fill? horizontal alignment
        const val ALIGN_FILL: Short = 0x04
        // justified horizontal alignment
        const val ALIGN_JUSTIFY: Short = 0x05
        // center-selection? horizontal alignment
        const val ALIGN_CENTER_SELECTION: Short = 0x6
        // top-aligned vertical alignment
        const val VERTICAL_TOP: Short = 0x0
        // center-aligned vertical alignment
        const val VERTICAL_CENTER: Short = 0x1
        // bottom-aligned vertical alignment
        const val VERTICAL_BOTTOM: Short = 0x2
        // vertically justified vertical alignment
        const val VERTICAL_JUSTIFY: Short = 0x3
        // No border
        const val BORDER_NONE: Short = 0x0
        // Thin border
        const val BORDER_THIN: Short = 0x1
        // Medium border
        const val BORDER_MEDIUM: Short = 0x2
        // dash border
        const val BORDER_DASHED: Short = 0x3
        // dot border
        const val BORDER_HAIR: Short = 0x4
        // Thick border
        const val BORDER_THICK: Short = 0x5
        // double-line border
        const val BORDER_DOUBLE: Short = 0x6
        // hair-line border
        const val BORDER_DOTTED: Short = 0x7
        // Medium dashed border
        const val BORDER_MEDIUM_DASHED: Short = 0x8
        // dash-dot border
        const val BORDER_DASH_DOT: Short = 0x9
        // medium dash-dot border
        const val BORDER_MEDIUM_DASH_DOT: Short = 0xA
        // dash-dot-dot border
        const val BORDER_DASH_DOT_DOT: Short = 0xB
        // medium dash-dot-dot border
        const val BORDER_MEDIUM_DASH_DOT_DOT: Short = 0xC
        // slanted dash-dot border
        const val BORDER_SLANTED_DASH_DOT: Short = 0xD
        //  No background
        const val NO_FILL: Short = 0
        // Solidly filled
        const val SOLID_FOREGROUND: Short = 1
        // Small fine dots
        const val FINE_DOTS: Short = 2
        // Wide dots
        const val ALT_BARS: Short = 3
        // Sparse dots
        const val SPARSE_DOTS: Short = 4
        // Thick horizontal bands
        const val THICK_HORZ_BANDS: Short = 5
        // Thick vertical bands
        const val THICK_VERT_BANDS: Short = 6
        // Thick backward facing diagonals
        const val THICK_BACKWARD_DIAG: Short = 7
        // Thick forward facing diagonals
        const val THICK_FORWARD_DIAG: Short = 8
        // Large spots
        const val BIG_SPOTS: Short = 9
        // Brick-like layout
        const val BRICKS: Short = 10
        // Thin horizontal bands
        const val THIN_HORZ_BANDS: Short = 11
        // Thin vertical bands
        const val THIN_VERT_BANDS: Short = 12
        // Thin backward diagonal
        const val THIN_BACKWARD_DIAG: Short = 13
        // Thin forward diagonal
        const val THIN_FORWARD_DIAG: Short = 14
        // Squares
        const val SQUARES: Short = 15
        // Diamonds
        const val DIAMONDS: Short = 16
        // Less Dots
        const val LESS_DOTS: Short = 17
        // Least Dots
        const val LEAST_DOTS: Short = 18
    }
}
