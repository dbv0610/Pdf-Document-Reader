/*
 * 文件名称:          BorderStyle.java
 *
 * 编译器:            android2.2
 * 时间:              下午1:51:05
 */
package com.wxiwei.office.ss.model.style

/**
 * TODO: 文件注释
 *
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2012-2-22
 * 负责人:           jqin
 */
class BorderStyle {
    private var style: Short = BORDER_NONE
    private var colorIdx: Short = 0/*Palette.FIRST_COLOR_INDEX*/

    constructor()

    constructor(style: Short, colorIdx: Short) {
        this.style = style
        this.colorIdx = colorIdx
    }

    constructor(style: String?, colorIdx: Short) {
        this.style = getStyle(style)
        this.colorIdx = colorIdx
    }

    /**
     *
     * @param style
     * @return
     */
    private fun getStyle(style: String?): Short {
        if (style == null || style == "none") {
            return BORDER_NONE
        } else if (style == "thin") {
            return BORDER_THIN
        } else if (style == "medium") {
            return BORDER_MEDIUM
        } else if (style == "dashed") {
            return BORDER_DASHED
        } else if (style == "dotted") {
            return BORDER_DOTTED
        } else if (style == "thick") {
            return BORDER_THICK
        } else if (style == "double") {
            return BORDER_DOUBLE
        } else if (style == "hair") {
            return BORDER_HAIR
        } else if (style == "mediumDashed") {
            return BORDER_MEDIUM_DASHED
        } else if (style == "dashDot") {
            return BORDER_DASH_DOT
        } else if (style == "mediumDashDot") {
            return BORDER_MEDIUM_DASH_DOT
        } else if (style == "dashDotDot") {
            return BORDER_DASH_DOT_DOT
        } else if (style == "mediumDashDotDot") {
            return BORDER_MEDIUM_DASH_DOT_DOT
        } else if (style == "slantDashDot") {
            return BORDER_SLANTED_DASH_DOT
        }

        return BORDER_NONE
    }

    /**
     *
     * @param style
     */
    fun setStyle(style: Short) {
        this.style = style
    }

    /**
     *
     * @return
     */
    fun getStyle(): Short = style

    /**
     *
     * @param colorIdx
     */
    fun setColor(colorIdx: Short) {
        this.colorIdx = colorIdx
    }

    /**
     *
     * @return
     */
    fun getColor(): Short = colorIdx

    /**
     *
     */
    fun dispose() {
    }

    companion object {
        /**
         * No border
         */
        const val BORDER_NONE: Short = 0x0
        /**
         * Thin border
         */
        const val BORDER_THIN: Short = 0x1
        /**
         * Medium border
         */
        const val BORDER_MEDIUM: Short = 0x2
        /**
         * dash border
         */
        const val BORDER_DASHED: Short = 0x3
        /**
         * dot border
         */
        const val BORDER_HAIR: Short = 0x4
        /**
         * Thick border
         */
        const val BORDER_THICK: Short = 0x5
        /**
         * double-line border
         */
        const val BORDER_DOUBLE: Short = 0x6
        /**
         * hair-line border
         */
        const val BORDER_DOTTED: Short = 0x7
        /**
         * Medium dashed border
         */
        const val BORDER_MEDIUM_DASHED: Short = 0x8
        /**
         * dash-dot border
         */
        const val BORDER_DASH_DOT: Short = 0x9
        /**
         * medium dash-dot border
         */
        const val BORDER_MEDIUM_DASH_DOT: Short = 0xA
        /**
         * dash-dot-dot border
         */
        const val BORDER_DASH_DOT_DOT: Short = 0xB
        /**
         * medium dash-dot-dot border
         */
        const val BORDER_MEDIUM_DASH_DOT_DOT: Short = 0xC
        /**
         * slanted dash-dot border
         */
        const val BORDER_SLANTED_DASH_DOT: Short = 0xD
    }
}
