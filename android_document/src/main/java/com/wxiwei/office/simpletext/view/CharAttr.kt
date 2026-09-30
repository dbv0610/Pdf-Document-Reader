/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          CharAttr.java
 *
 * 编译器:            android2.2
 * 时间:              下午2:38:58
 */
package com.wxiwei.office.simpletext.view

/**
 * 字符属性
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2011-12-15
 *
 * 负责人:          ljj8494
 */
class CharAttr {
    // 字号
    @JvmField
    var fontSize = 0

    // exact size in points (fontSize is rounded)
    @JvmField
    var fontSizeF = 0f

    // character spacing in points (0 = normal)
    @JvmField
    var spacingPt = 0f

    // 0 none, 1 all caps, 2 small caps (shown as caps)
    @JvmField
    var caps = 0

    // raised (+) or lowered (-) by this many points
    @JvmField
    var positionPt = 0f

    // hidden text: takes no room and is not drawn
    @JvmField
    var isHidden = false

    // 字体
    @JvmField
    var fontIndex = 0

    // font scale(of percent)
    @JvmField
    var fontScale = 0

    //
    @JvmField
    var fontColor = 0

    // 粗体
    @JvmField
    var isBold = false

    // 斜体
    @JvmField
    var isItalic = false

    // 下划线类型
    @JvmField
    var underlineType = 0

    // 下划线颜色
    @JvmField
    var underlineColor = 0

    // 是否有删除线
    @JvmField
    var isStrikeThrough = false

    // 是否有双删除线
    @JvmField
    var isDoubleStrikeThrough = false

    /*
     * 上下标类型 = 0 正常文本， =1 上标，=2 下标
     */
    @JvmField
    var subSuperScriptType: Short = 0

    // 高亮颜色
    @JvmField
    var highlightedColor = 0

    //
    @JvmField
    var encloseType: Byte = 0

    //field code type
    @JvmField
    var pageNumberType: Byte = 0

    /**
     *
     */
    fun reset() {
        underlineType = 0
        fontColor = -1
        underlineColor = -1
        isStrikeThrough = false
        isDoubleStrikeThrough = false
        subSuperScriptType = 0
        fontIndex = -1
        encloseType = -1
        pageNumberType = -1
    }
}
