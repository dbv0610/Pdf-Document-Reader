/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          WPAbstractShape.java
 *  
 * 编译器:            android2.2
 * 时间:              下午2:08:14
 */
package com.wxiwei.office.common.shape

/**
 * TODO: 文件注释
 * 
 * 
 * 
 * 
 * Read版本:        Read V1.0
 * 
 * 
 * 作者:            jqin
 * 
 * 
 * 日期:            2013-5-29
 * 
 * 
 * 负责人:           jqin
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
open class WPAbstractShape : ArbitraryPolygonShape() {
    val wrap: Int
        /**
         * wrap type
         * @return
         */
        get() = wrapType.toInt()

    /**
     * 
     * @param wrapType
     */
    fun setWrap(wrapType: Short) {
        this.wrapType = wrapType
    }


    /**
     * POSITION_ABSOLUTE or POSITION_RELATIVE
     * @return
     */
    /**
     * POSITION_ABSOLUTE or POSITION_RELATIVE
     * @param horPositionType
     */
    //relative or absolute
    var horPositionType: Byte = 0
    /**
     * @return Returns the horRelative.
     */
    /**
     * @param horRelative The horRelative to set.
     */
    // horizontal relative
    var horizontalRelativeTo: Byte = 0

    /**
     * horizontal relative position value(one in a thousand)
     * @return
     */
    //horizontal relative position(one in a thousand)
    var horRelativeValue: Int = 0
    /**
     * @return Returns the horizontal alignment;
     */
    /**
     * @param horAlignment The horizontal alignment to set.
     */
    // horizontal position
    var horizontalAlignment: Byte = ALIGNMENT_ABSOLUTE

    /**
     * POSITION_ABSOLUTE or POSITION_RELATIVE
     * @return
     */
    /**
     * POSITION_ABSOLUTE or POSITION_RELATIVE
     * @param verPositionType
     */
    //relative or absolute
    var verPositionType: Byte = 0
    /**
     * @return Returns the verRelativeTo.
     */
    /**
     * @param verRelative The verRelativeTo to set.
     */
    // vertical relative
    var verticalRelativeTo: Byte = RELATIVE_PARAGRAPH

    /**
     * vertical relative position value(one in a thousand)
     * @return
     */
    //vertical relative position(one in a thousand)
    var verRelativeValue: Int = 0
    /**
     * @return Returns the vertical Alignment value.
     */
    /**
     * @param verAlignment The vertical Alignment type to set.
     */
    // vertical position type
    var verticalAlignment: Byte = ALIGNMENT_ABSOLUTE

    //default is top of text;
    private var wrapType: Short = 3

    // DOCX wp:anchor@relativeHeight: floating shapes are drawn in increasing order
    var zOrder: Long = 0

    /**
     * @return Returns the elementIndex.
     */
    /**
     * @param elementIndex The elementIndex to set.
     */
    // text box element index
    var elementIndex: Int = -1
    /**
     * @return Returns the isTextWrapLine.
     */
    /**
     * @param isTextWrapLine The isTextWrapLine to set.
     */
    //    
    var isTextWrapLine: Boolean = true

    companion object {
        // 紧密型
        const val WRAP_TIGHT: Short = 0
        // 四周型
        const val WRAP_SQUARE: Short = 1
        // 嵌入型
        const val WRAP_OLE: Short = 2
        // 浮于文字上方
        const val WRAP_TOP: Short = 3
        // 穿越型
        const val WRAP_THROUGH: Short = 4
        // 上下型
        const val WRAP_TOPANDBOTTOM: Short = 5
        // 浮于文字下方
        const val WRAP_BOTTOM: Short = 6
        // 相对于柆
        const val RELATIVE_COLUMN: Byte = 0
        // 相对于页边距
        const val RELATIVE_MARGIN: Byte = 1

        // 相对于页面
        const val RELATIVE_PAGE: Byte = 2

        // 相对于字符
        const val RELATIVE_CHARACTER: Byte = 3

        // 相对左边距
        const val RELATIVE_LEFT: Byte = 4

        // 相对右边距
        const val RELATIVE_RIGHT: Byte = 5

        // 相对上边距
        const val RELATIVE_TOP: Byte = 6

        // 相对下边距
        const val RELATIVE_BOTTOM: Byte = 7

        // 相对内边距
        const val RELATIVE_INNER: Byte = 8

        // 相对外边距
        const val RELATIVE_OUTER: Byte = 9

        // 相对于段落
        const val RELATIVE_PARAGRAPH: Byte = 10

        // 相对于行
        const val RELATIVE_LINE: Byte = 11

        //
        const val ALIGNMENT_ABSOLUTE: Byte = 0
        // 左对齐
        const val ALIGNMENT_LEFT: Byte = 1
        // 居中
        const val ALIGNMENT_CENTER: Byte = 2
        // 右对齐
        const val ALIGNMENT_RIGHT: Byte = 3
        // 顶端对齐
        const val ALIGNMENT_TOP: Byte = 4
        // 底端
        const val ALIGNMENT_BOTTOM: Byte = 5
        // 内部
        const val ALIGNMENT_INSIDE: Byte = 6
        // 外部
        const val ALIGNMENT_OUTSIDE: Byte = 7
        /**
         * position type
         */
        //absolute position
        const val POSITIONTYPE_ABSOLUTE: Byte = 0
        //relative position
        const val POSITIONTYPE_RELATIVE: Byte = 1
    }
}
