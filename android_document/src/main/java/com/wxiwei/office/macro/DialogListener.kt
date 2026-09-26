/*
 * 文件名称:          DialogListener.java
 *
 * 编译器:            android2.2
 * 时间:              上午10:35:58
 */
package com.wxiwei.office.macro

import com.wxiwei.office.common.ICustomDialog

/**
 *
 */
interface DialogListener {

    /**
     * @param type dialog type
     */
    fun showDialog(type: Byte)

    /**
     * @param type
     */
    fun dismissDialog(type: Byte)

    companion object {
        //password dialog
        const val DIALOGTYPE_PASSWORD = ICustomDialog.DIALOGTYPE_PASSWORD
        //txt encode dialog
        const val DIALOGTYPE_ENCODE = ICustomDialog.DIALOGTYPE_ENCODE
        //loading dialog
        const val DIALOGTYPE_LOADING = ICustomDialog.DIALOGTYPE_LOADING
        //error dialog
        const val DIALOGTYPE_ERROR = ICustomDialog.DIALOGTYPE_ERROR
        //
        const val DIALOGTYPE_FIND = ICustomDialog.DIALOGTYPE_FIND
    }
}
