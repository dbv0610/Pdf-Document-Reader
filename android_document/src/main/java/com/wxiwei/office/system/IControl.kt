package com.wxiwei.office.system

import android.app.Activity
import android.app.Dialog
import android.view.View
import com.wxiwei.office.common.ICustomDialog
import com.wxiwei.office.common.IOfficeToPicture
import com.wxiwei.office.common.ISlideShow

interface IControl {
    fun canBackLayout(): Boolean
    fun setLayoutThreadDied(isDied: Boolean)
    fun setStopDraw(isStopDraw: Boolean)
    fun layoutView(x: Int, y: Int, w: Int, h: Int)
    fun actionEvent(actionID: Int, obj: Any?)
    fun getActionValue(actionID: Int, obj: Any?): Any?
    fun getCurrentViewIndex(): Int
    fun getView(): View
    fun getDialog(activity: Activity?, id: Int): Dialog?
    fun getMainFrame(): IMainFrame
    fun getActivity(): Activity
    fun getFind(): IFind
    fun isAutoTest(): Boolean
    fun getOfficeToPicture(): IOfficeToPicture?
    fun getCustomDialog(): ICustomDialog?
    fun isSlideShow(): Boolean
    fun getSlideShow(): ISlideShow?
    fun getReader(): IReader
    fun openFile(filePath: String?): Boolean
    fun getApplicationType(): Byte
    fun getSysKit(): SysKit

    fun dispose()
    fun isEndFile(): Boolean
}

val IControl.view: View
    get() = getView()

val IControl.dialog: Dialog?
    get() = getDialog(null, 0)

val IControl.mainFrame: IMainFrame
    get() = getMainFrame()

val IControl.activity: Activity
    get() = getActivity()

val IControl.find: IFind
    get() = getFind()

val IControl.reader: IReader
    get() = getReader()

val IControl.sysKit: SysKit
    get() = getSysKit()

val IControl.isAutoTest: Boolean
    get() = isAutoTest()

val IControl.officeToPicture: IOfficeToPicture?
    get() = getOfficeToPicture()

val IControl.customDialog: ICustomDialog?
    get() = getCustomDialog()

val IControl.slideShow: ISlideShow?
    get() = getSlideShow()
