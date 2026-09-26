package com.wxiwei.office.officereader.filelist

import android.content.Context
import android.util.AttributeSet
import android.widget.CheckBox
import com.wxiwei.office.officereader.FileListActivity
import com.wxiwei.office.system.IControl

class FileCheckBox : CheckBox {
    private var fileItem: FileItem? = null
    private var control: IControl? = null
    constructor(context: Context, control: IControl, fileItem: FileItem) : super(context) {
        this.fileItem = fileItem
        this.control = control
        isChecked = fileItem.isCheck()
        isFocusable = false
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    override fun performClick(): Boolean {
        val result = super.performClick()
        val item = fileItem ?: return result
        item.setCheck(isChecked)
        val activity = control?.getActivity() as? FileListActivity ?: return result
        if (isChecked) activity.addSelectFileItem(item) else activity.removeSelectFileItem(item)
        return result
    }
    fun setFileItem(fileItem: FileItem?) {
        this.fileItem = fileItem
        isChecked = fileItem?.isCheck() ?: false
    }
    fun dispose() {
        fileItem = null
        control = null
    }
}
