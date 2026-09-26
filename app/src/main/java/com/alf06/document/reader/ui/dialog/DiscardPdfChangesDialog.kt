package com.alf06.document.reader.ui.dialog

import android.content.Context
import com.alf06.document.reader.databinding.DialogDiscardPdfChangesBinding
import com.ui.baselib.base.BaseDialog
import com.ui.baselib.extensions.click

class DiscardPdfChangesDialog(
    context: Context,
    private val onDiscard: () -> Unit,
) : BaseDialog<DialogDiscardPdfChangesBinding>(context, DialogDiscardPdfChangesBinding::inflate, true) {

    override fun DialogDiscardPdfChangesBinding.initView() {
        btnContinueEditing.click { dismiss() }
        btnDiscard.click {
            dismiss()
            onDiscard()
        }
    }
}
