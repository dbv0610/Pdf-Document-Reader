package com.alf06.document.reader.ui.dialog

import android.content.Context
import com.alf06.document.reader.databinding.DialogOpenFileErrorBinding
import com.ui.baselib.base.BaseDialog
import com.ui.baselib.extensions.click

class OpenFileErrorDialog(context: Context, private val message: String) :
    BaseDialog<DialogOpenFileErrorBinding>(context, DialogOpenFileErrorBinding::inflate, true) {

    override fun DialogOpenFileErrorBinding.initView() {
        txtMessage.text = message
        btnAgree.click { dismiss() }
    }
}
