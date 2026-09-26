/*
 * 文件名称:          DialogConstant.java
 * 版权所有@ 2011-2014 虹软（杭州）科技有限公司
 * 编译器:           JDK1.5.0_01
 * 时间:             上午10:14:58
 */
package com.wxiwei.office.constant

/**
 * Dialog ID
 */
object DialogConstant {
    // Warning dialog ID
    const val MESSAGE_DIALOG_ID = 0 // 0
    // text encoding dialog ID
    const val ENCODING_DIALOG_ID = MESSAGE_DIALOG_ID + 1 // 1
    // create a new folder dialog ID
    const val CREATEFOLDER_DIALOG_ID = ENCODING_DIALOG_ID + 1 // 2
    // rename a file dialog ID
    const val RENAMEFILE_DIALOG_ID = CREATEFOLDER_DIALOG_ID + 1 // 3
    // delete file dialog ID
    const val DELETEFILE_DIALOG_ID = RENAMEFILE_DIALOG_ID + 1 // 4
    // ask overwrite file dialog ID
    const val OVERWRITEFILE_DIALOG_ID = DELETEFILE_DIALOG_ID + 1 // 5
    // sort file dialog ID
    const val FILESORT_DIALOG_ID = OVERWRITEFILE_DIALOG_ID + 1 // 6
    // set maximum number of recently opened documents
    const val SET_MAX_RECENT_NUMBER = FILESORT_DIALOG_ID + 1 // 7
    // ppt show notes
    const val SHOW_PG_NOTE_ID = SET_MAX_RECENT_NUMBER + 1 // 8
}
