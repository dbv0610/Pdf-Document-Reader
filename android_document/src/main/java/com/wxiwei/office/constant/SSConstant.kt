/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          SSConstant.java
 *
 * 编译器:            android2.2
 * 时间:              下午2:20:14
 */
package com.wxiwei.office.constant

import android.graphics.Color

/**
 * excel应用的常量类
 */
object SSConstant {
    // 默认行高（像素值）
    const val DEFAULT_ROW_HEIGHT = 18
    // 默认列宽（像素值）
    const val DEFAULT_COLUMN_WIDTH = 72
    // 行标题默认宽度（像素值）
    const val DEFAULT_ROW_HEADER_WIDTH = 50
    // 列标题默认高度（像素值）
    const val DEFAULT_COLUMN_HEADER_HEIGHT = 30
    // 标题填充颜色
    const val HEADER_FILL_COLOR = -0x2c2c2d
    // 标题字符颜色
    const val HEADER_TEXT_COLOR = Color.BLACK
    // header grid line color
    const val HEADER_GRIDLINE_COLOR = -0x939393
    // 网格线颜色
    const val GRIDLINE_COLOR = -0x382e27

    /** Grid line color of [sheet], transparent when the sheet hides grid lines (showGridLines="0"). */
    @JvmStatic
    fun gridlineColor(sheet: com.wxiwei.office.ss.model.baseModel.Sheet?): Int =
        if (sheet == null || sheet.isShowGridLines()) GRIDLINE_COLOR else android.graphics.Color.TRANSPARENT
    // 标题的字号
    const val HEADER_TEXT_FONTSZIE = 16
    //active color
    const val ACTIVE_COLOR = -0x3c009c
    // 列宽用到的字符宽度
    const val COLUMN_CHAR_WIDTH = 6.0f
    //
    const val SHEET_SPACETOBORDER = 2
    //indent
    const val INDENT_TO_PIXEL = 34
}
