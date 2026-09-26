package com.alf06.document.reader.ui.dialog

import android.content.Context
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import com.alf06.document.reader.databinding.DialogDocumentPasswordBinding
import com.ui.baselib.base.BaseDialog
import com.ui.baselib.extensions.click

/** Asks for the password of an encrypted document. */
class DocumentPasswordDialog(
    context: Context,
    private val message: String,
    private val onPassword: (String) -> Unit
) : BaseDialog<DialogDocumentPasswordBinding>(context, DialogDocumentPasswordBinding::inflate, true) {

    override fun DialogDocumentPasswordBinding.initView() {
        txtMessage.text = message
        btnCancel.click { dismiss() }
        btnOk.click { submit() }
        edtPassword.imeOptions = EditorInfo.IME_ACTION_DONE
        edtPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) submit()
            actionId == EditorInfo.IME_ACTION_DONE
        }
        edtPassword.requestFocus()
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }

    private fun DialogDocumentPasswordBinding.submit() {
        val password = edtPassword.text?.toString().orEmpty()
        if (password.isEmpty()) return
        onPassword(password)
    }
}
