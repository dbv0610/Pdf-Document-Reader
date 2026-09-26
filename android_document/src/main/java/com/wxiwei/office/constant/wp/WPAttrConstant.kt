/*
 * 文件名称:          WPAttrConstant.java
 *
 * 编译器:            android2.2
 * 时间:              下午5:38:10
 */
package com.wxiwei.office.constant.wp

/**
 * 属性常
 */
object WPAttrConstant {
    /* ============ page vertical alignment ================ */
    //
    const val PAGE_V_TOP: Byte = 0
    //
    const val PAGE_V_CENTER: Byte = 1
    //
    const val PAGE_V_BOTTOM: Byte = 2
    //
    const val PAGE_V_JUSTIFIED: Byte = 3
    //
    const val PAGE_V_DISTRIBUTED: Byte = 4
    //
    const val PAGE_H_LEFT: Byte = 0
    //
    const val PAGE_H_CENTER: Byte = 1
    //
    const val PAGE_H_RIGHT: Byte = 2

    /* ============ 段落属性 ================ */
    // horizontal left alignment
    const val PARA_HOR_ALIGN_LEFT: Byte = 0
    // horizontal center alignment
    const val PARA_HOR_ALIGN_CENTER: Byte = 1
    // horizontal right alignment
    const val PARA_HOR_ALIGN_RIGHT: Byte = 2
    // horizontal justified alignment
    const val PARA_HOR_ALIGN_JUSTIFIED: Byte = 3
    // horizontal distributed alignment
    const val PARA_HOR_ALIGN_DISTRIBUTED: Byte = 4
    // vertical top alignment
    const val PARA_VER_ALIGN_TOP: Byte = 0
    // vertical center alignment
    const val PARA_VER_ALIGN_CENTER: Byte = 1
    // vertical bottom alignment
    const val PARA_VER_ALIGN_BOTTOM: Byte = 2

    // ========== 行距 =============== */
    // 单倍行距
    const val LINE_SPACE_SINGLE: Byte = 0
    // 1.5倍行距
    const val LINE_SPACE_ONE_HALF: Byte = 1
    // 2倍行距
    const val LINE_SPACE_DOUBLE: Byte = 2
    // 最小值
    const val LINE_SAPCE_LEAST: Byte = 3
    // 固定值
    const val LINE_SPACE_EXACTLY: Byte = 4
    // 多倍行距
    const val LINE_SAPCE_MULTIPLE: Byte = 5
}
