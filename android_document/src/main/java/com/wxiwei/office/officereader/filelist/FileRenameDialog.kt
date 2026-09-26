/*
 * 文件名称:           FileRenameDialog.java
 *  
 * 编译器:             android2.2
 * 时间:               下午2:41:49
 */
package com.wxiwei.office.officereader.filelist

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.IDialogAction
import java.io.File
import java.util.Vector

/**
 * file rename
 * 
 * 
 * 
 * 
 * Read版本:       Read V1.0
 * 
 * 
 * 作者:           jhy1790
 * 
 * 
 * 日期:           2011-12-14
 * 
 * 
 * 负责人:         jhy1790
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
class FileRenameDialog(
    control: IControl?,
    context: Context,
    action: IDialogAction?,
    model: Vector<Any>?,
    dialogID: Int,
    titleResID: Int
) : FileNameDialog(control, context, action, model, dialogID, titleResID) {
    /**
     * 
     */
    fun initDialog() {
        val editText = editText ?: return
        if (model != null) {
            val file = model!!.get(0) as File
            var name = file.getName()
            if (file.isFile()) {
//                textView.setText(R.string.dialog_file_name);
                name = name.substring(0, name.lastIndexOf('.'))
            } else {
//                textView.setText(R.string.dialog_folder_name);
            }
            editText.setText(name)
            editText.addTextChangedListener(watcher)
        }
    }

    /**
     * 
     */
    override fun onClick(v: View?) {
        if (v === ok) {
            val editText = editText ?: return
            if (model == null) {
                dismiss()
                return
            }
            val file = model!!.get(0) as File
            var exe = ""
            if (file.isFile()) {
                exe = file.getName()
                exe = exe.substring(exe.lastIndexOf('.'))
            }

            val newFile: File?
            val filePath = file.getParent()
            val name = editText.getText().toString().trim { it <= ' ' }
            if (filePath!!.endsWith(File.separator)) {
                newFile = File(filePath + name + exe)
            } else {
                newFile = File(filePath + File.separator + name + exe)
            }
            val vector = Vector<Any>()
            vector.add(file)
            vector.add(newFile)
            if (!newFile.exists()) {
                action!!.doAction(dialogID, vector)
                dismiss()
            } else {
//                CharSequence text = getContext().getResources().getText(R.string.dialog_name_error);
//                String message = text.toString().replace("%s", name);
//                new MessageDialog(control, getContext(), action, null,
//                    DialogConstant.MESSAGE_DIALOG_ID,
//                    R.string.dialog_file_rename_error, message).show();
            }
        } else {
            dismiss()
        }
    }

    /**
     * 监听text
     */
    private val watcher: TextWatcher = object : TextWatcher {
        /**
         * 
         * 
         */
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        /**
         * 
         * 
         */
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            ok!!.setEnabled(checkFileName(s.toString()))
        }

        /**
         * 
         * 
         */
        override fun afterTextChanged(s: Editable?) {
        }
    }

    /**
     * 
     * @param context
     * @param action
     * @param model
     * @param dialogID
     * @param titleResID
     */
    init {
        initDialog()
    }

    /**
     * 判断文件名是否合法
     * @param fileName
     * @return
     */
    fun checkFileName(fileName: String?): Boolean {
        if (model != null && (model!!.get(0) as File).getName() == fileName) {
            return false
        }
        return isFileNameOK(fileName)
    }
}
