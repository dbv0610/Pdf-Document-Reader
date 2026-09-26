/*
 * 文件名称:          PageAttr.java
 *
 * 编译器:            android2.2
 * 时间:              下午2:17:24
 */
package com.wxiwei.office.simpletext.view

/**
 * 页面属性集
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2011-11-20
 *
 * 负责人:          ljj8494
 */
class PageAttr {
    // 页面宽度
    @JvmField
    var pageWidth = 0

    // 页面高度
    @JvmField
    var pageHeight = 0

    // 上边距
    @JvmField
    var topMargin = 0

    // 下边距
    @JvmField
    var bottomMargin = 0

    // 左边距
    @JvmField
    var leftMargin = 0

    // 右边距
    @JvmField
    var rightMargin = 0

    // page vertical alignment
    @JvmField
    var verticalAlign: Byte = 0

    //
    @JvmField
    var horizontalAlign: Byte = 0

    //
    @JvmField
    var headerMargin = 0

    //
    @JvmField
    var footerMargin = 0

    //
    @JvmField
    var pageBRColor = 0

    //
    @JvmField
    var pageBorder = 0

    //
    @JvmField
    var pageLinePitch = 0f

    /**
     *
     */
    fun reset() {
        verticalAlign = 0
        horizontalAlign = 0
        pageWidth = 0
        pageHeight = 0
        topMargin = 0
        bottomMargin = 0
        leftMargin = 0
        rightMargin = 0
        headerMargin = 0
        footerMargin = 0
        pageBorder = 0
        pageBRColor = 0xFFFFFFFF.toInt()
        pageLinePitch = 0f
    }

    /**
     *
     */
    fun dispose() {
    }

    companion object {
        const val GRIDTYPE_NONE: Byte = 0
        const val GRIDTYPE_LINE_AND_CHAR: Byte = 1
        const val GRIDTYPE_LINE: Byte = 2
        const val GRIDTYPE_CHAR: Byte = 3
    }
}
