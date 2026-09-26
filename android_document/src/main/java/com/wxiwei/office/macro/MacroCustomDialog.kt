/*
 * 文件名称:          MarcoCustomDialog.java
 *
 * 编译器:            android2.2
 * 时间:              下午12:49:58
 */
package com.wxiwei.office.macro

import com.wxiwei.office.common.ICustomDialog

/**
 *
 */
class MacroCustomDialog internal constructor(private var dailogListener: DialogListener?) : ICustomDialog {

    override fun showDialog(type: Byte) {
        dailogListener?.showDialog(type)
    }

    override fun dismissDialog(type: Byte) {
        dailogListener?.dismissDialog(type)
    }

    fun dispose() {
        dailogListener = null
    }
}
