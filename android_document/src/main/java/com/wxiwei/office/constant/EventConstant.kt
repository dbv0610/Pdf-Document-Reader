/*
 * 文件名称:          EventConstant.java
 *
 * 编译器:            android2.2
 * 时间:              下午4:32:07
 */
package com.wxiwei.office.constant

/**
 * 事件类型ID
 */
object EventConstant {
    /* =============== 系统 和文件操作 ActionID =============*/
    // 返回
    const val SYS_ONBACK_ID = 0x00000000 // 0
    // 得到打开文件路径
    const val SYS_FILEPAHT_ID = SYS_ONBACK_ID + 1 // 1
    // 最近打开文件
    const val SYS_RECENTLY_FILES_ID = SYS_FILEPAHT_ID + 1 // 2
    // 标星文件
    const val SYS_MARK_FILES_ID = SYS_RECENTLY_FILES_ID + 1 // 3
    // SDCard文件
    const val SYS_MEMORY_CARD_ID = SYS_MARK_FILES_ID + 1 // 4
    // 搜索
    const val SYS_SEARCH_ID = SYS_MEMORY_CARD_ID + 1 // 5
    // 设置
    const val SYS_SETTINGS_ID = SYS_SEARCH_ID + 1 // 6
    // 帐户
    const val SYS_ACCOUNT_ID = SYS_SETTINGS_ID + SYS_SETTINGS_ID // 7
    // 注册
    const val SYS_REGISTER_ID = SYS_ACCOUNT_ID + 1 // 8
    // 更新
    const val SYS_UPDATE_ID = SYS_REGISTER_ID + 1 // 9
    // 帮助
    const val SYS_HELP_ID = SYS_UPDATE_ID + 1 // 10
    // 关于
    const val SYS_ABOUT_ID = SYS_HELP_ID + 1 // 11;
    // 显示tooltip
    const val SYS_SHOW_TOOLTIP = SYS_ABOUT_ID + 1 // 12
    // 关于tooltip
    const val SYS_CLOSE_TOOLTIP = SYS_SHOW_TOOLTIP + 1 // 13
    // 初始化动作
    const val SYS_INIT_ID = SYS_CLOSE_TOOLTIP + 1 // 14
    // 更新toolsbar button状态
    const val SYS_UPDATE_TOOLSBAR_BUTTON_STATUS = SYS_INIT_ID + 1 //15
    // set recently files count
    const val SYS_SET_MAX_RECENT_NUMBER = SYS_UPDATE_TOOLSBAR_BUTTON_STATUS + 1 // 16
    // auto test finish
    const val SYS_AUTO_TEST_FINISH_ID = SYS_SET_MAX_RECENT_NUMBER + 1 // 17;
    // reader file finish
    const val SYS_READER_FINSH_ID = SYS_AUTO_TEST_FINISH_ID + 1 // 18
    // start back reader file
    const val SYS_START_BACK_READER_ID = SYS_READER_FINSH_ID + 1 // 19
    // reset activity title
    const val SYS_RESET_TITLE_ID = SYS_START_BACK_READER_ID + 1 // 20
    // set progress bar visible
    const val SYS_SET_PROGRESS_BAR_ID = SYS_RESET_TITLE_ID + 1 // 21
    // vector graph converting progress
    const val SYS_VECTORGRAPH_PROGRESS = SYS_SET_PROGRESS_BAR_ID + 1 // 22

    /* =============== 文件操作 ActionID ============== */
    // 新建文件夹
    const val FILE_CREATE_FOLDER_ID = 0x10000000 // 0x10000000
    // 重命名
    const val FILE_RENAME_ID = FILE_CREATE_FOLDER_ID + 1 // 0x10000001
    // 复制
    const val FILE_COPY_ID = FILE_RENAME_ID + 1 // 0x10000002
    // 剪切
    const val FILE_CUT_ID = FILE_COPY_ID + 1 // 0x10000003
    // 粘贴
    const val FILE_PASTE_ID = FILE_CUT_ID + 1 // 0x10000004
    // 删除
    const val FILE_DELETE_ID = FILE_PASTE_ID + 1 // 0x10000005
    // 分享
    const val FILE_SHARE_ID = FILE_DELETE_ID + 1 // 0x10000006
    // 排序
    const val FILE_SORT_ID = FILE_SHARE_ID + 1 // 0x10000007
    // 标星
    const val FILE_MARK_STAR_ID = FILE_SORT_ID + 1 // 0x10000008
    // 打印
    const val FILE_PRINT_ID = FILE_MARK_STAR_ID + 1 // 0x10000009
    // 刷新
    const val FILE_REFRESH_ID = FILE_PRINT_ID + 1 // 0x10000010
    // 设置排序类型
    const val FILE_SORT_TYPE_ID = FILE_REFRESH_ID + 1 // 0x10000011
    // new folder failed
    const val FILE_CREATE_FOLDER_FAILED_ID = FILE_SORT_TYPE_ID + 1 // 0x10000012

    /* ============== 应用公共 ActionID =============*/
    // 查找
    const val APP_FIND_ID = 0x20000000 // 0x20000000
    // 分享
    const val APP_SHARE_ID = APP_FIND_ID + 1 // 0x20000001
    // 联网搜索
    const val APP_INTERNET_SEARCH_ID = APP_SHARE_ID + 1 // 0x20000002
    // 朗读
    const val APP_READ_ID = APP_INTERNET_SEARCH_ID + 1 // 0x20000003
    // 签批
    const val APP_APPROVE_ID = APP_READ_ID + 1 // 0x20000004
    // 设置zoom
    const val APP_ZOOM_ID = APP_APPROVE_ID + 1 // 0x20000005
    // 适用的zoom
    const val APP_FIT_ZOOM_ID = APP_ZOOM_ID + 1 // 0x20000006
    //selected
    const val APP_CONTENT_SELECTED = APP_FIT_ZOOM_ID + 1 // 0x20000007
    //hyperlink
    const val APP_HYPERLINK = APP_CONTENT_SELECTED + 1 // 0x20000008
    //abort reading
    const val APP_ABORTREADING = APP_HYPERLINK + 1 // 0x20000009
    // generated picture
    const val APP_GENERATED_PICTURE_ID = APP_ABORTREADING + 1 // 0x2000000A
    // count pages
    const val APP_COUNT_PAGES_ID = APP_GENERATED_PICTURE_ID + 1 // 0x2000000B
    // current page number
    const val APP_CURRENT_PAGE_NUMBER_ID = APP_COUNT_PAGES_ID + 1 // 0x2000000C
    // page up
    const val APP_PAGE_UP_ID = APP_CURRENT_PAGE_NUMBER_ID + 1 // 0x2000000D
    // page down
    const val APP_PAGE_DOWN_ID = APP_PAGE_UP_ID + 1 // 0x2000000E
    // count pages change
    const val APP_COUNT_PAGES_CHANGE_ID = APP_PAGE_DOWN_ID + 1 // 0x2000001F
    // get thumbnail
    const val APP_THUMBNAIL_ID = APP_COUNT_PAGES_CHANGE_ID + 1 // 0x20000010
    // authenticate
    const val APP_AUTHENTICATE_PASSWORD = APP_THUMBNAIL_ID + 1 // 0x20000011
    //
    const val APP_PASSWORD_OK_INIT = APP_AUTHENTICATE_PASSWORD + 1 // 0x20000012
    //specified area of page to image
    const val APP_PAGEAREA_TO_IMAGE = APP_PASSWORD_OK_INIT + 1 //  0x20000013
    // get hyper link URL
    const val APP_GET_HYPERLINK_URL_ID = APP_PAGEAREA_TO_IMAGE + 1 // 0x20000014
    // set fit size
    const val APP_SET_FIT_SIZE_ID = APP_GET_HYPERLINK_URL_ID + 1 // 0x20000015
    // get fit size state
    const val APP_GET_FIT_SIZE_STATE_ID = APP_SET_FIT_SIZE_ID + 1 // 0x20000016
    // get real page count
    const val APP_GET_REAL_PAGE_COUNT_ID = APP_GET_FIT_SIZE_STATE_ID + 1 // 0x20000017
    // get snapshot
    const val APP_GET_SNAPSHOT_ID = APP_GET_REAL_PAGE_COUNT_ID + 1 // 0x20000018;
    // draw callout
    const val APP_DRAW_ID = APP_GET_SNAPSHOT_ID + 1 // 0x20000019;
    // back
    const val APP_BACK_ID = APP_DRAW_ID + 1 // 0x20000020;
    // pen
    const val APP_PEN_ID = APP_BACK_ID + 1 // 0x20000021;
    // eraser
    const val APP_ERASER_ID = APP_PEN_ID + 1 // 0x20000022;
    // color
    const val APP_COLOR_ID = APP_ERASER_ID + 1 // 0x20000022;
    // init callout view
    const val APP_INIT_CALLOUTVIEW_ID = APP_COLOR_ID + 1 // 0x20000023;

    /* ============== search bar ActionID =============*/
    //find
    const val APP_FINDING = 0x2F000000 // 0x2F000000
    //backward
    const val APP_FIND_BACKWARD = APP_FINDING + 1 // 0x2F000001
    //FORWARD
    const val APP_FIND_FORWARD = APP_FIND_BACKWARD + 1 // 0x2F000002

    /* ============== word Action ID ============ */
    // 文本选择
    const val WP_SELECT_TEXT_ID = 0x30000000 //
    // 视图切换
    const val WP_SWITCH_VIEW = WP_SELECT_TEXT_ID + 1 // 0x30000001;
    // show page
    const val WP_SHOW_PAGE = WP_SWITCH_VIEW + 1 // 0x30000002
    // page to image
    const val WP_PAGE_TO_IMAGE = WP_SHOW_PAGE + 1 // 0x30000003
    // get page size
    const val WP_GET_PAGE_SIZE = WP_PAGE_TO_IMAGE + 1 // 0x30000004
    // layout normal view
    const val WP_LAYOUT_NORMAL_VIEW = WP_GET_PAGE_SIZE + 1 // 0x30000005
    //  get view mode
    const val WP_GET_VIEW_MODE = WP_LAYOUT_NORMAL_VIEW + 1 // 0x30000006;
    // print mode
    const val WP_PRINT_MODE = WP_GET_VIEW_MODE + 1 // 0x0x30000007
    //layout completed
    const val WP_LAYOUT_COMPLETED = WP_PRINT_MODE + 1 // 0x0x30000008

    /* ============= excel Action ID =========== */
    // sheet切换
    const val SS_SHEET_CHANGE = 0x40000000 // 0x40000000
    // show sheet
    const val SS_SHOW_SHEET = SS_SHEET_CHANGE + 1 // 0x40000001
    // get all sheet name
    const val SS_GET_ALL_SHEET_NAME = SS_SHOW_SHEET + 1 // 0x40000002
    // get sheet name
    const val SS_GET_SHEET_NAME = SS_GET_ALL_SHEET_NAME + 1 // 0x40000003
    // change current sheet
    const val SS_CHANGE_SHEET = SS_GET_SHEET_NAME + 1 // 0x40000004
    // remove sheet bar
    const val SS_REMOVE_SHEET_BAR = SS_CHANGE_SHEET + 1 //  0x40000005

    /* ============= ppt Action ID =========== */
    // 备注
    const val PG_NOTE_ID = 0x50000000 // 0x50000000
    //
    const val PG_REPAINT_ID = PG_NOTE_ID + 1 // 0x50000001
    //
    const val PG_SHOW_SLIDE_ID = PG_REPAINT_ID + 1 // 0x50000002
    // slide to image
    const val PG_SLIDE_TO_IMAGE = PG_SHOW_SLIDE_ID + 1 // 0x50000003
    // get slide note
    const val PG_GET_SLIDE_NOTE = PG_SLIDE_TO_IMAGE + 1 // 0x50000004
    // get slide size
    const val PG_GET_SLIDE_SIZE = PG_GET_SLIDE_NOTE + 1 // 0x50000005
    //slideshow
    const val PG_SLIDESHOW = 0x51000000 // 0x51000000
    const val PG_SLIDESHOW_GEGIN = PG_SLIDESHOW + 1 // 0x51000001
    const val PG_SLIDESHOW_END = PG_SLIDESHOW_GEGIN + 1 // 0x51000002
    const val PG_SLIDESHOW_PREVIOUS = PG_SLIDESHOW_END + 1 // 0x51000003
    const val PG_SLIDESHOW_NEXT = PG_SLIDESHOW_PREVIOUS + 1 // 0x51000004
    const val PG_SLIDESHOW_HASPREVIOUSACTION = PG_SLIDESHOW_NEXT + 1 // 0x51000005
    const val PG_SLIDESHOW_HASNEXTACTION = PG_SLIDESHOW_HASPREVIOUSACTION + 1 // 0x51000006
    const val PG_SLIDESHOW_DURATION = PG_SLIDESHOW_HASNEXTACTION + 1 // 0x51000007
    const val PG_SLIDESHOW_SLIDEEXIST = PG_SLIDESHOW_DURATION + 1 // 0x51000008
    const val PG_SLIDESHOW_ANIMATIONSTEPS = PG_SLIDESHOW_SLIDEEXIST + 1 // 0x51000009
    const val PG_SLIDESHOW_SLIDESHOWTOIMAGE = PG_SLIDESHOW_ANIMATIONSTEPS + 1 // 0x5100000A

    /* ===========  以下是用于测试的action 后期可能会删除 =========*/

    //
    const val TXT_DIALOG_FINISH_ID = 0x7000000 // 0x70000000
    // reopen with TXT document
    const val TXT_REOPNE_ID = TXT_DIALOG_FINISH_ID + 1 // 0x70000001

    // 重绘
    const val TEST_REPAINT_ID = -0x10000000
}
