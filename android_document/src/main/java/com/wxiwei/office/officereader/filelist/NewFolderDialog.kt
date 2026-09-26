package com.wxiwei.office.officereader.filelist

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.IDialogAction
import java.io.File
import java.util.Vector

class NewFolderDialog(
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
//        textView.setText(R.string.dialog_folder_name);
        editText.addTextChangedListener(watcher)
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
            val newFolder: File?
            val filePath = model!!.get(0).toString()
            val name = editText.getText().toString().trim { it <= ' ' }
            if (filePath.endsWith(File.separator)) {
                newFolder = File(filePath + name)
            } else {
                newFolder = File(filePath + File.separator + name)
            }
            val vector = Vector<Any>()
            vector.add(newFolder)
            if (!newFolder.exists()) {
                action!!.doAction(dialogID, vector)
                dismiss()
            } else {
//                CharSequence text = getContext().getResources().getText(R.string.dialog_name_error);
//                String message = text.toString().replace("%s", name);
//                new MessageDialog(control, getContext(), action, null,
//                    DialogConstant.MESSAGE_DIALOG_ID,
//                    R.string.dialog_create_folder_error, message).show();
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
            ok!!.setEnabled(isFileNameOK(s.toString()))
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
     */
    init {
        initDialog()
    }
}
