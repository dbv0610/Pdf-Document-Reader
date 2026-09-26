package com.alf06.document.reader.ui.dialog

import android.content.Context
import com.ui.baselib.base.BaseDialog
import com.alf06.document.reader.databinding.DialogQuitAppBinding
import com.ui.baselib.extensions.click

class QuitAppDialog(context: Context, var action: () -> Unit = {}) :
    BaseDialog<DialogQuitAppBinding>(context, DialogQuitAppBinding::inflate, true) {
    override fun DialogQuitAppBinding.initView() {
        btnExit.click {
            action.invoke()
            dismiss()
        }
        btnCancel.click {
            dismiss()
        }
    }
}