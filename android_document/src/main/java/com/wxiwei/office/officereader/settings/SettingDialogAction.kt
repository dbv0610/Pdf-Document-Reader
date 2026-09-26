package com.wxiwei.office.officereader.settings

import com.wxiwei.office.constant.DialogConstant
import com.wxiwei.office.constant.EventConstant
import com.wxiwei.office.system.IDialogAction
import com.wxiwei.office.system.IControl
import java.util.Vector

class SettingDialogAction(control: IControl?) : IDialogAction {
    private var controlValue: IControl? = control
    override fun doAction(id: Int, model: Vector<Any>?) {
        when (id) {
            DialogConstant.SET_MAX_RECENT_NUMBER -> if (model != null) {
                controlValue?.actionEvent(EventConstant.SYS_SET_MAX_RECENT_NUMBER, model[0])
            }
        }
    }

    override fun getControl(): IControl? = controlValue

    override fun dispose() {
        controlValue = null
    }
}
