/*
 * 文件名称:          TableAttr.java
 *
 * 编译器:            android2.2
 * 时间:              下午4:38:31
 */
package com.wxiwei.office.simpletext.view

/**
 * table attribute
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2012-5-21
 *
 * 负责人:          ljj8494
 */
class TableAttr {

    // 上边距
    @JvmField
    var topMargin = 0

    // 左边距
    @JvmField
    var leftMargin = 0

    // 右边距
    @JvmField
    var rightMargin = 0

    // 下边距
    @JvmField
    var bottomMargin = 0

    // cell宽度
    @JvmField
    var cellWidth = 0

    // cell vertical align
    @JvmField
    var cellVerticalAlign: Byte = 0

    // cell background
    @JvmField
    var cellBackground = 0

    /**
     *
     */
    fun reset() {
        topMargin = 0
        leftMargin = 0
        rightMargin = 0
        bottomMargin = 0
        cellVerticalAlign = 0
        cellBackground = -1
    }
}
