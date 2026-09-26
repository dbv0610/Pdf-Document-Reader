package com.alf06.document.reader.ui.dialog

import android.content.Context
import com.alf06.document.reader.databinding.DialogNoInternetBinding

import com.ui.baselib.base.BaseDialog
import com.ui.baselib.extensions.click

class DialogNoInternet(context: Context, val onTryAgain: () -> Unit = {}) :
    BaseDialog<DialogNoInternetBinding>(context, DialogNoInternetBinding::inflate) {
    override fun DialogNoInternetBinding.initView() {
        btnExit.click {
            dismiss()
            onTryAgain()
        }
    }
}