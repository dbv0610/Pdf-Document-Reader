/*
 * 文件名称:          ParaAttr.java
 *
 * 编译器:            android2.2
 * 时间:              下午2:21:35
 */
package com.wxiwei.office.simpletext.view

/**
 * 段落属性
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2011-11-20
 *
 * 负责人:          ljj8494
 */
class ParaAttr {
    // 左缩进
    @JvmField
    var leftIndent = 0

    // 右缩进
    @JvmField
    var rightIndent = 0

    // 特殊缩进格式， =0 首行缩进， = 1 悬挂缩进
    //public byte specialIndentType;
    // 特殊缩进值
    @JvmField
    var specialIndentValue = 0

    // 行距type
    @JvmField
    var lineSpaceType: Byte = 0

    // 行距值
    @JvmField
    var lineSpaceValue = 0f

    // 段前间距
    @JvmField
    var beforeSpace = 0

    // 段后问题
    @JvmField
    var afterSpace = 0

    // 重直对齐方式
    @JvmField
    var verticalAlignment: Byte = 0

    // 水平对齐方式
    @JvmField
    var horizontalAlignment: Byte = 0

    // list ID
    @JvmField
    var listID = 0

    // list level
    @JvmField
    var listLevel: Byte = 0

    // list 文本缩进
    @JvmField
    var listTextIndent = 0

    //
    @JvmField
    var listAlignIndent = 0

    // pg bullet text ID
    @JvmField
    var pgBulletID = 0

    //
    @JvmField
    var tabClearPosition = 0

    /**
     *
     */
    fun reset() {
        leftIndent = 0
        rightIndent = 0
        //specialIndentType = -1;
        specialIndentValue = 0
        lineSpaceType = -1
        lineSpaceValue = 0f
        beforeSpace = 0
        afterSpace = 0
        verticalAlignment = 0
        horizontalAlignment = 0
        listID = -1
        listLevel = -1
        listTextIndent = 0
        listAlignIndent = 0
        pgBulletID = -1
        tabClearPosition = 0
    }

    /**
     *
     */
    fun dispose() {
    }
}
