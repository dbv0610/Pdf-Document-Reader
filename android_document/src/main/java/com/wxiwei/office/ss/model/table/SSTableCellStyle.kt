/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          TableCellStyle.java
 *
 * 编译器:            android2.2
 * 时间:              下午1:30:16
 */
package com.wxiwei.office.ss.model.table

/**
 * TODO: 文件注释
 *
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2013-4-18
 * 负责人:           jqin
 */
class SSTableCellStyle(fillColor: Int?) {
    private var fontColor: Int = -0x1000000
    //left, right, top, bottom
    private var borderColor: Int? = null
    //background
    private var fillColor: Int? = fillColor

    fun getFontColor(): Int = fontColor

    fun setFontColor(fontColor: Int) {
        this.fontColor = fontColor
    }

    fun getBorderColor(): Int? = borderColor

    fun setBorderColor(borderColor: Int) {
        this.borderColor = borderColor
    }

    fun getFillColor(): Int? = fillColor

    fun setFillColor(fillColor: Int) {
        this.fillColor = fillColor
    }
}
