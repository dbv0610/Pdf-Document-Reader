package com.wxiwei.office.wp.control

import com.wxiwei.office.constant.DialogConstant
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.IDialogAction
import java.util.Vector

class WPDialogAction(control: IControl?) : IDialogAction {
    @JvmField
    var control: IControl? = control

    override fun doAction(id: Int, model: Vector<Any>?) {
        when (id) {
            DialogConstant.ENCODING_DIALOG_ID -> {}
            else -> {}
        }
    }

    override fun getControl(): IControl? = control

    override fun dispose() {
        control = null
    }
}
