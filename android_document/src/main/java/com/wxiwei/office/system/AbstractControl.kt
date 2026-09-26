package com.wxiwei.office.system

import android.app.Activity
import android.app.Dialog
import android.view.View
import com.wxiwei.office.common.ICustomDialog
import com.wxiwei.office.common.IOfficeToPicture
import com.wxiwei.office.common.ISlideShow

abstract class AbstractControl : IControl {
    override fun layoutView(x: Int, y: Int, w: Int, h: Int) {
    }

    override fun actionEvent(actionID: Int, obj: Any?) {
    }

    override fun getActionValue(actionID: Int, obj: Any?): Any? = null

    override fun getCurrentViewIndex(): Int = -1

    override fun getView(): View = null!!

    override fun getDialog(activity: Activity?, id: Int): Dialog? = null

    override fun getMainFrame(): IMainFrame = null!!

    override fun getActivity(): Activity = null!!

    override fun getFind(): IFind = null!!

    override fun getReader(): IReader = null!!

    override fun getSysKit(): SysKit = null!!

    override fun dispose() {
    }

    override fun isAutoTest(): Boolean = false

    override fun getOfficeToPicture(): IOfficeToPicture? = null

    override fun getCustomDialog(): ICustomDialog? = null

    override fun isSlideShow(): Boolean = false

    override fun getSlideShow(): ISlideShow? = null

    override fun openFile(filePath: String?): Boolean = false

    override fun getApplicationType(): Byte = -1
}
