/*
 * 文件名称:          MainConstant.java
 *
 * 编译器:            android2.2
 * 时间:              下午2:29:36
 */
package com.wxiwei.office.constant

/**
 * 总控常量
 */
object MainConstant {
    // word应用
    const val APPLICATION_TYPE_WP: Byte = 0
    // excel应用
    const val APPLICATION_TYPE_SS: Byte = 1
    // powerpoint应用
    const val APPLICATION_TYPE_PPT: Byte = 2
    // pdf应用
    // text 应用
    const val APPLICATION_TYPE_TXT: Byte = 4
    // doc文档格式
    const val FILE_TYPE_DOC = "doc"
    // docx文档格式
    const val FILE_TYPE_DOCX = "docx"
    // xls文档格式
    const val FILE_TYPE_XLS = "xls"
    // xlsx文档格式
    const val FILE_TYPE_XLSX = "xlsx"
    // ppt文档格式
    const val FILE_TYPE_PPT = "ppt"
    // pptx文档格式
    const val FILE_TYPE_PPTX = "pptx"
    // txt文档格式
    const val FILE_TYPE_TXT = "txt"
    //
    const val FILE_TYPE_PDF = "pdf"

    const val FILE_TYPE_DOT = "dot"
    const val FILE_TYPE_DOTX = "dotx"
    const val FILE_TYPE_DOTM = "dotm"
    const val FILE_TYPE_XLT = "xlt"
    const val FILE_TYPE_XLTX = "xltx"
    const val FILE_TYPE_XLSM = "xlsm"
    const val FILE_TYPE_XLTM = "xltm"
    const val FILE_TYPE_POT = "pot"
    const val FILE_TYPE_PPTM = "pptm"
    const val FILE_TYPE_POTX = "potx"
    const val FILE_TYPE_POTM = "potm"

    const val INTENT_FILED_FILE = "file"
    const val INTENT_FILED_FILE_URI = "fileUri"
    const val INTENT_FILED_FILE_NAME = "fileName"
    const val INTENT_FILED_FILE_IS_SAMPLE = "isSample"

    /* ============ Activity之间Intent传值字段名称 */
    // 文件路径
    const val INTENT_FILED_FILE_PATH = "filePath"
    // 文件列表类型
    const val INTENT_FILED_FILE_LIST_TYPE = "fileListType"
    // 标星文档
    const val INTENT_FILED_MARK_FILES = "markFiles"
    // 最近打开的文档
    const val INTENT_FILED_RECENT_FILES = "recentFiles"
    // sdcard文档
    const val INTENT_FILED_SDCARD_FILES = "sdcard"
    // 文档标星状态
    const val INTENT_FILED_MARK_STATUS = "markFileStatus"

    /* ======= 以下定义组件的高度 ========== */
    // 组件之前的问隙
    const val GAP = 5
    //
    const val ZOOM_ROUND = 10000000

    /* ======= 以下定义一些视图公用常量 ========= */
    // Points DPI (72 pixels per inch)
    const val POINT_DPI = 72.0f
    //厘米到磅
    const val MM_TO_POINT = 2.835f
    // 缇到磅
    const val TWIPS_TO_POINT = 1 / 20.0f
    // 磅到缇
    const val POINT_TO_TWIPS = 20.0f
    // default tab width, 21磅
    const val DEFAULT_TAB_WIDTH_POINT = 21f
    //
    const val EMU_PER_INCH = 914400
    // Pixels DPI (96 pixels per inch)
    const val PIXEL_DPI = 96f
    // 磅到像素
    const val POINT_TO_PIXEL = PIXEL_DPI / POINT_DPI
    // 像素到磅
    const val PIXEL_TO_POINT = POINT_DPI / PIXEL_DPI
    // 缇到像素
    const val TWIPS_TO_PIXEL = TWIPS_TO_POINT * POINT_TO_PIXEL
    // 像素到缇
    const val PIXEL_TO_TWIPS = PIXEL_TO_POINT * POINT_TO_TWIPS
    //// default tab width, 21磅
    const val DEFAULT_TAB_WIDTH_PIXEL = DEFAULT_TAB_WIDTH_POINT * POINT_TO_PIXEL

    /* ============ 数据库中表名*/
    // recently opened files
    const val TABLE_RECENT = "openedfiles"
    // starred files
    const val TABLE_STAR = "starredfiles"
    // settings
    const val TABLE_SETTING = "settings"

    /* =========== 文件解析过程中message类型 =========*/
    // 文档解析成功+
    const val HANDLER_MESSAGE_SUCCESS = 0
    // 文档解析失败
    const val HANDLER_MESSAGE_ERROR = HANDLER_MESSAGE_SUCCESS + 1
    // 显示进度条对话框
    const val HANDLER_MESSAGE_SHOW_PROGRESS = HANDLER_MESSAGE_ERROR + 1
    // 关闭进度条对话框
    const val HANDLER_MESSAGE_DISMISS_PROGRESS = HANDLER_MESSAGE_SHOW_PROGRESS + 1
    // 释放内存
    const val HANDLER_MESSAGE_DISPOSE = HANDLER_MESSAGE_DISMISS_PROGRESS + 1
    // 传递IReader实例
    const val HANDLER_MESSAGE_SEND_READER_INSTANCE = HANDLER_MESSAGE_DISPOSE

    //zoom
    const val STANDARD_RATE = 10000
    const val MAXZOOM = 30000
    const val MAXZOOM_THUMBNAIL = 5000

    // Drawing mode
    //not callout mode
    const val DRAWMODE_NORMAL = 0
    //draw callout
    const val DRAWMODE_CALLOUTDRAW = 1
    //erase callout
    const val DRAWMODE_CALLOUTERASE = 2
}
