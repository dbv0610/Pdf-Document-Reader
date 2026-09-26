/*
 * 文件名称:          WPConstant.java
 *
 * 编译器:            android2.2
 * 时间:              下午3:15:30
 */
package com.wxiwei.office.constant.wp

/**
 * Word用到常量
 */
object WPViewConstant {
    // 布局断行标记，没有断行
    const val BREAK_NO: Byte = 0
    // 受限断行
    const val BREAK_LIMIT: Byte = 1
    // 换行符断行
    const val BREAK_ENTER: Byte = 2
    // 分节符
    const val BREAK_PAGE: Byte = 3

    // 页与页的间距
    const val PAGE_SPACE: Short = 5

    /* ============ 布局标记位 ============= */
    // 孤行控制，控制View是否必须有一个子View
    const val LAYOUT_FLAG_KEEPONE: Byte = 0
    // 最后不完全可见的一行是否删除
    const val LAYOUT_FLAG_DELLELINEVIEW: Byte = (LAYOUT_FLAG_KEEPONE + 1).toByte() // 1
    // 布局表格中段落
    const val LAYOUT_PARA_IN_TABLE: Byte = (LAYOUT_FLAG_DELLELINEVIEW + 1).toByte() // 2
    // 是布局水平对式
    const val LAYOUT_NOT_WRAP_LINE: Byte = (LAYOUT_PARA_IN_TABLE + 1).toByte() // 3

    /* ============ 定义视图类型 ============ */
    // page root
    const val PAGE_ROOT: Short = 0 // 0
    // normal root
    const val NORMAL_ROOT: Short = (PAGE_ROOT + 1).toShort() // 1
    //
    const val PRINT_ROOT: Short = (NORMAL_ROOT + 1).toShort() // 2
    // simple root
    const val SIMPLE_ROOT: Short = (PRINT_ROOT + 1).toShort() // 3
    // page view
    const val PAGE_VIEW: Short = (SIMPLE_ROOT + 1).toShort() // 4
    // paragraph  view
    const val PARAGRAPH_VIEW: Short = (PAGE_VIEW + 1).toShort() // 5
    // line view
    const val LINE_VIEW: Short = (PARAGRAPH_VIEW + 1).toShort() // 6
    // leaf view
    const val LEAF_VIEW: Short = (LINE_VIEW + 1).toShort() // 7
    // objv iew
    const val OBJ_VIEW: Short = (LEAF_VIEW + 1).toShort() // 8
    // table view
    const val TABLE_VIEW: Short = (OBJ_VIEW + 1).toShort() // 9
    // table row view
    const val TABLE_ROW_VIEW: Short = (TABLE_VIEW + 1).toShort() // 10
    // table cell view
    const val TABLE_CELL_VIEW: Short = (TABLE_ROW_VIEW + 1).toShort() // 11
    // title view (for header and footer)
    const val TITLE_VIEW: Short = (TABLE_CELL_VIEW + 1).toShort() // 12
    // bullet and number view
    const val BN_VIEW: Short = (TITLE_VIEW + 1).toShort() // 13
    // shape view
    const val SHAPE_VIEW: Short = (TITLE_VIEW + 1).toShort() // 14
    // enclose character view
    const val ENCLOSE_CHARACTER_VIEW: Short = (SHAPE_VIEW + 1).toShort() //15

    // 视图坐标X方向
    const val X_AXIS: Byte = 0
    // 视图坐标Y方向
    const val Y_AXIS: Byte = (X_AXIS + 1).toByte()
}
