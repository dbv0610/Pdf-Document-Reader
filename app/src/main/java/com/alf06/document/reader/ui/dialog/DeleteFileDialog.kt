package com.alf06.document.reader.ui.dialog

import android.content.Context
import androidx.annotation.StringRes
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.DialogDeleteFileBinding
import com.ui.baselib.base.BaseDialog
import com.ui.baselib.extensions.click

class DeleteFileDialog(
    context: Context,
    @StringRes private val titleRes: Int = R.string.file_action_delete,
    @StringRes private val messageRes: Int = R.string.file_delete_message,
    private val onConfirm: () -> Unit,
) : BaseDialog<DialogDeleteFileBinding>(context, DialogDeleteFileBinding::inflate, true) {

    override fun DialogDeleteFileBinding.initView() {
        tvTitle.setText(titleRes)
        tvMessage.setText(messageRes)
        btnConfirm.click {
            onConfirm()
            dismiss()
        }
        btnCancel.click { dismiss() }
    }
}
