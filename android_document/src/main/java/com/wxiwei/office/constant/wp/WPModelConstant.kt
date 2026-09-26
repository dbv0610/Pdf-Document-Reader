/*
 * 文件名称:          WPModelConstant.java
 *
 * 编译器:            android2.2
 * 时间:              上午9:25:50
 */
package com.wxiwei.office.constant.wp

/**
 * word model定义的一些常量
 */
object WPModelConstant {

    /* ============ 文本区域 ================ */
    // 正文
    const val MAIN = 0x0000000000000000L
    // 页眉
    const val HEADER = 0x1000000000000000L
    // 页脚
    const val FOOTER = 0x2000000000000000L
    // 脚注
    const val FOOTNOTE = 0x3000000000000000L
    // 尾注
    const val ENDNOTE = 0x4000000000000000L
    // 文本框，高32位区分不同的文本框
    const val TEXTBOX = 0x5000000000000000L
    // 区域Mask (0xF000000000000000L)
    const val AREA_MASK = -0x1000000000000000L
    // 文本框mask
    const val TEXTBOX_MASK = 0x0FFFFFFF00000000L

    /* ============ model的Element类型 ========= */
    // 章节Element
    const val SECTION_ELEMENT: Short = 0 // 0
    // 段落Element
    const val PARAGRAPH_ELEMENT: Short = (SECTION_ELEMENT + 1).toShort() // 1
    // Leaf Element
    const val LEAF_ELEMENT: Short = (SECTION_ELEMENT + 1).toShort() // 2
    // table element
    const val TABLE_ELEMENT: Short = (LEAF_ELEMENT + 1).toShort() // 3
    // table row element
    const val TABLE_ROW_ELEMENT: Short = (TABLE_ELEMENT + 1).toShort() // 4
    // table cell element
    const val TABLE_CELL_ELEMENT: Short = (TABLE_ROW_ELEMENT + 1).toShort() // 5
    // header element
    const val HEADER_ELEMENT: Short = (TABLE_CELL_ELEMENT + 1).toShort() // 6
    // footer element
    const val FOOTER_ELEMENT: Short = (HEADER_ELEMENT + 1).toShort() // 7

    /* =========== element的Collection类型 ============ */
    // 章节集合
    const val SECTION_COLLECTION: Short = 0
    // 页眉
    const val HEADER_COLLECTION: Short = (SECTION_COLLECTION + 1).toShort() // 1
    // 页脚
    const val FOOTER_COLLECTION: Short = (HEADER_COLLECTION + 1).toShort() // 2
    // 脚注
    const val FOOTNOTE_COLLECTION: Short = (FOOTER_COLLECTION + 1).toShort() // 3
    // 尾注
    const val ENDNOTE_COLLECTION: Short = (FOOTNOTE_COLLECTION + 1).toShort() // 4
    // 文本框
    const val TEXTBOX_COLLECTION: Short = (ENDNOTE_COLLECTION + 1).toShort() // 5

    /* =========== 页眉页脚类型 ==========  */
    // 首页
    const val HF_FIRST: Byte = 0
    // 奇数页
    const val HF_ODD: Byte = (HF_FIRST + 1).toByte() // 1
    // 偶数页
    const val HF_EVEN: Byte = (HF_ODD + 1).toByte() // 2

    /* =========== 页码类型 ============ */
    // page number
    const val PN_PAGE_NUMBER: Byte = 1
    // total pages
    const val PN_TOTAL_PAGES: Byte = 2

    /* =========== 圈号类型 =========== */
    // 圆
    const val ENCLOSURE_TYPE_ROUND: Byte = 0
    // 正方
    const val ENCLOSURE_TYPE_SQUARE: Byte = (ENCLOSURE_TYPE_ROUND + 1).toByte()
    // 三角
    const val ENCLOSURE_TYPE_TRIANGLE: Byte = (ENCLOSURE_TYPE_SQUARE + 1).toByte()
    // 菱形
    const val ENCLOSURE_TYPE_RHOMBUS: Byte = (ENCLOSURE_TYPE_TRIANGLE + 1).toByte()
}
