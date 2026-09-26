package com.wxiwei.office.officereader.filelist

import com.wxiwei.office.constant.DialogConstant
import com.wxiwei.office.constant.EventConstant
import com.wxiwei.office.system.FileKit
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.IDialogAction
import java.io.File
import java.util.Vector

class FileDialogAction(initialControl: IControl?) : IDialogAction {
    @JvmField
    var control: IControl? = initialControl
    override fun doAction(id: Int, model: Vector<Any>?) {
        if (model == null) return
        when (id) {
            DialogConstant.CREATEFOLDER_DIALOG_ID -> {
                if ((model[0] as File).mkdir()) control?.actionEvent(EventConstant.FILE_REFRESH_ID, null)
                else control?.actionEvent(EventConstant.FILE_CREATE_FOLDER_FAILED_ID, null)
            }
            DialogConstant.RENAMEFILE_DIALOG_ID -> {
                (model[0] as File).renameTo(model[1] as File)
                control?.actionEvent(EventConstant.FILE_REFRESH_ID, true)
            }
            DialogConstant.DELETEFILE_DIALOG_ID -> {
                for (item in model) FileKit.instance().deleteFile(item as File)
                control?.actionEvent(EventConstant.FILE_REFRESH_ID, null)
            }
            DialogConstant.OVERWRITEFILE_DIALOG_ID -> {
                val fromFile = model[1] as File
                val toFile = model[2] as File
                if (fromFile != toFile) {
                    FileKit.instance().pasteFile(fromFile, toFile)
                    if (model[0] as Boolean) FileKit.instance().deleteFile(fromFile)
                }
            }
            DialogConstant.FILESORT_DIALOG_ID -> control?.actionEvent(EventConstant.FILE_SORT_TYPE_ID, model)
        }
    }
    override fun getControl(): IControl? = control
    override fun dispose() { control = null }
}
