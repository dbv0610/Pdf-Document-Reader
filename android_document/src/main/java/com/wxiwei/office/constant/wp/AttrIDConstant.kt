/*
 * 文件名称:          AttrIDConstant.java
 *
 * 编译器:            android2.2
 * 时间:              上午11:10:41
 */
package com.wxiwei.office.constant.wp

/**
 * 定义属性ID值
 */
object AttrIDConstant {
    /* ========== 字符属性 =========== */
    // 字符样式
    const val FONT_STYLE_ID: Short = 0x0000
    // 字号
    const val FONT_SIZE_ID: Short = (FONT_STYLE_ID + 1).toShort() // 00001
    // 字体
    const val FONT_NAME_ID: Short = (FONT_SIZE_ID + 1).toShort() //0x0002;
    // 字符颜色
    const val FONT_COLOR_ID: Short = (FONT_NAME_ID + 1).toShort() // 0x0003;
    // 粗体
    const val FONT_BOLD_ID: Short = (FONT_COLOR_ID + 1).toShort() //0x0004;
    // 斜体
    const val FONT_ITALIC_ID: Short = (FONT_BOLD_ID + 1).toShort() //0x0005;
    // 删除线
    const val FONT_STRIKE_ID: Short = (FONT_ITALIC_ID + 1).toShort() //0x0006;
    // 双删除线
    const val FONT_DOUBLESTRIKE_ID: Short = (FONT_STRIKE_ID + 1).toShort() //0x0007;
    // 下划线
    const val FONT_UNDERLINE_ID: Short = (FONT_DOUBLESTRIKE_ID + 1).toShort() //0x0008;
    // 下划线颜色
    const val FONT_UNDERLINE_COLOR_ID: Short = (FONT_UNDERLINE_ID + 1).toShort() // 0x0009
    // 上下标
    const val FONT_SCRIPT_ID: Short = (FONT_UNDERLINE_COLOR_ID + 1).toShort() //0x000A;
    // 高亮
    const val FONT_HIGHLIGHT_ID: Short = (FONT_SCRIPT_ID + 1).toShort() //0x000B;
    // hyperlink
    const val FONT_HYPERLINK_ID: Short = (FONT_HIGHLIGHT_ID + 1).toShort() // 0x000C
    // shape index
    const val FONT_SHAPE_ID: Short = (FONT_HYPERLINK_ID + 1).toShort() // 0x000D
    // font scale
    const val FONT_SCALE_ID: Short = (FONT_SHAPE_ID + 1).toShort() // 0x000E
    // page number type
    const val FONT_PAGE_NUMBER_TYPE_ID: Short = (FONT_SCALE_ID + 1).toShort() // 0x000F
    // enclose character
    const val FONT_ENCLOSE_CHARACTER_TYPE_ID: Short = (FONT_PAGE_NUMBER_TYPE_ID + 1).toShort() // 0x0010;

    /* ========== 段落属性 =========== */
    // 段落样式
    const val PARA_STYLE_ID: Short = 0x1000
    // 段落左缩进
    const val PARA_INDENT_LEFT_ID: Short = (PARA_STYLE_ID + 1).toShort() //0x1001;
    const val PARA_INDENT_INITLEFT_ID: Short = (PARA_INDENT_LEFT_ID + 1).toShort() //0x1001;
    // 段落右缩进
    const val PARA_INDENT_RIGHT_ID: Short = (PARA_INDENT_INITLEFT_ID + 1).toShort() //0x1003;
    // 段前间距
    const val PARA_BEFORE_ID: Short = (PARA_INDENT_RIGHT_ID + 1).toShort() //0x1004;
    // 段后间距
    const val PARA_AFTER_ID: Short = (PARA_BEFORE_ID + 1).toShort() //0x1005;
    // 水平对齐
    const val PARA_HORIZONTAL_ID: Short = (PARA_AFTER_ID + 1).toShort() //0x1006;
    // 垂直对齐
    const val PARA_VERTICAL_ID: Short = (PARA_HORIZONTAL_ID + 1).toShort() // 0x1007;
    // 特殊缩进
    const val PARA_SPECIALINDENT_ID: Short = (PARA_VERTICAL_ID + 1).toShort() //0x1008;
    // 行距
    const val PARA_LINESPACE_ID: Short = (PARA_SPECIALINDENT_ID + 1).toShort() //0x1009;
    // 行距类型
    const val PARA_LINESPACE_TYPE_ID: Short = (PARA_LINESPACE_ID + 1).toShort() //0x100A;
    // 段落级别，用于表格
    const val PARA_LEVEL_ID: Short = (PARA_LINESPACE_TYPE_ID + 1).toShort() //0x100B
    // list level
    const val PARA_LIST_LEVEL_ID: Short = (PARA_LEVEL_ID + 1).toShort() // 0x100C
    // list ID
    const val PARA_LIST_ID: Short = (PARA_LIST_LEVEL_ID + 1).toShort() // 0x100D
    // pg bullet text ID
    const val PARA_PG_BULLET_ID: Short = (PARA_LIST_ID + 1).toShort() // 0x100E
    //
    const val PARA_TABS_CLEAR_POSITION_ID: Short = (PARA_PG_BULLET_ID + 1).toShort() //0x100F

    /* ========= 章节属性 ========= */
    // 页面宽度
    const val PAGE_WIDTH_ID: Short = 0x2000
    // 页面高度
    const val PAGE_HEIGHT_ID: Short = (PAGE_WIDTH_ID + 1).toShort() //0x2001;
    // 页面左边距
    const val PAGE_LEFT_ID: Short = (PAGE_HEIGHT_ID + 1).toShort() //0x2002;
    // 页面右边距
    const val PAGE_RIGHT_ID: Short = (PAGE_LEFT_ID + 1).toShort() //0x2003;
    // 页面上边距
    const val PAGE_TOP_ID: Short = (PAGE_RIGHT_ID + 1).toShort() //0x2004;
    // 页面下边距
    const val PAGE_BOTTOM_ID: Short = (PAGE_TOP_ID + 1).toShort() //0x2005;
    // 垂直对齐
    const val PAGE_VERTICAL_ID: Short = (PAGE_BOTTOM_ID + 1).toShort() // 0x2006;
    // 页眉到上边界的距离
    const val PAGE_HEADER_ID: Short = (PAGE_VERTICAL_ID + 1).toShort() // 0x2007
    // 页脚到下边界的距离
    const val PAGE_FOOTER_ID: Short = (PAGE_HEADER_ID + 1).toShort() // 0x2008
    // 页面水平对齐
    const val PAGE_HORIZONTAL_ID: Short = (PAGE_FOOTER_ID + 1).toShort() // 0x2009;
    // page background color
    const val PAGE_BACKGROUND_COLOR_ID: Short = (PAGE_HORIZONTAL_ID + 1).toShort() // 0x200A
    // page border
    const val PAGE_BORDER_ID: Short = (PAGE_BACKGROUND_COLOR_ID + 1).toShort() // 0x200B
    //line pitch
    const val PAGE_LINEPITCH_ID: Short = (PAGE_BORDER_ID + 1).toShort() // 0x200C

    /* ======== 表格属性 ======== */
    // 上边框
    const val TABLE_TOP_BORDER_ID: Short = 0x3000
    // 上边框颜色
    const val TABLE_TOP_BORDER_COLOR_ID: Short = (TABLE_TOP_BORDER_ID + 1).toShort() // 0x3001
    // 下边框
    const val TABLE_BOTTOM_BORDER_ID: Short = (TABLE_TOP_BORDER_COLOR_ID + 1).toShort() // 0x3002
    // 下边框颜色
    const val TABLE_BOTTOM_BORDER_COLOR_ID: Short = (TABLE_BOTTOM_BORDER_ID + 1).toShort() // 0x3003
    // 左边框
    const val TABLE_LEFT_BORDER_ID: Short = (TABLE_BOTTOM_BORDER_COLOR_ID + 1).toShort() // 0x3004
    // 左边框颜色
    const val TABLE_LEFT_BORDER_COLOR_ID: Short = (TABLE_LEFT_BORDER_ID + 1).toShort() // 0x3005
    // 左边框
    const val TABLE_RIGHT_BORDER_ID: Short = (TABLE_LEFT_BORDER_COLOR_ID + 1).toShort() // 0x3006
    // 左边框颜色
    const val TABLE_RIGHT_BORDER_COLOR_ID: Short = (TABLE_RIGHT_BORDER_ID + 1).toShort() // 0x3007
    // 行高
    const val TABLE_ROW_HEIGHT_ID: Short = (TABLE_RIGHT_BORDER_COLOR_ID + 1).toShort() // 0x3008
    // 列宽
    const val TABLE_CELL_WIDTH_ID: Short = (TABLE_ROW_HEIGHT_ID + 1).toShort() // 0x3009
    // 行标题
    const val TABLE_ROW_HEADER_ID: Short = (TABLE_CELL_WIDTH_ID + 1).toShort() // 0x300A
    // 行跨页
    const val TABLE_ROW_SPLIT_ID: Short = (TABLE_ROW_HEADER_ID + 1).toShort() // 0x300B
    // 第一个水平合并单元格
    const val TABLE_CELL_HOR_FIRST_MERGED_ID: Short = (TABLE_ROW_SPLIT_ID + 1).toShort() // 0x300C
    // 水平合并单元格
    const val TABLE_CELL_HORIZONTAL_MERGED_ID: Short = (TABLE_CELL_HOR_FIRST_MERGED_ID + 1).toShort() // 0x300D
    // 第一个垂直合并单元格
    const val TABLE_CELL_VER_FIRST_MERGED_ID: Short = (TABLE_CELL_HORIZONTAL_MERGED_ID + 1).toShort() // 0x300E
    // 垂直合并单元格
    const val TABLE_CELL_VERTICAL_MERGED_ID: Short = (TABLE_CELL_VER_FIRST_MERGED_ID + 1).toShort() // 0x300F
    // 垂直对齐方式
    const val TABLE_CELL_VERTICAL_ALIGN_ID: Short = (TABLE_CELL_VERTICAL_MERGED_ID + 1).toShort() // 0x3010
    // 上边距
    const val TABLE_TOP_MARGIN_ID: Short = (TABLE_CELL_VERTICAL_ALIGN_ID + 1).toShort() // 0x3011
    // 下边距
    const val TABLE_BOTTOM_MARGIN_ID: Short = (TABLE_TOP_MARGIN_ID + 1).toShort() // 0x3012
    // 左边距
    const val TABLE_LEFT_MARGIN_ID: Short = (TABLE_BOTTOM_MARGIN_ID + 1).toShort() // 0x3013
    // 右边距
    const val TABLE_RIGHT_MARGIN_ID: Short = (TABLE_LEFT_MARGIN_ID + 1).toShort() // 03014
}
