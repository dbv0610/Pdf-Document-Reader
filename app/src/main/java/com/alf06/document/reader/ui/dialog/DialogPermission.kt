package com.alf06.document.reader.ui.dialog

import android.app.Activity
import com.alf06.document.reader.databinding.DialogFilePermissionBinding
import com.ui.baselib.base.BaseDialog
import com.ui.baselib.extensions.click

class DialogPermission(private val context: Activity, private val onAllow: () -> Unit) :
    BaseDialog<DialogFilePermissionBinding>(
        context, DialogFilePermissionBinding::inflate
    ) {
    override fun DialogFilePermissionBinding.initView() {
        btnCancel.click {
            dismiss()
        }
        btnAllow.click {
            dismiss()
            onAllow.invoke()
        }
    }
}
