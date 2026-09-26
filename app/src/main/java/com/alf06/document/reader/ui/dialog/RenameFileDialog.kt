package com.alf06.document.reader.ui.dialog

import android.content.Context
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.DialogRenameFileBinding
import com.ui.baselib.base.BaseDialog
import com.ui.baselib.extensions.click

/**
 * [onSave] receives the new name without extension and returns whether it succeeded;
 * on failure the dialog stays open and shows [failedRes]. With [skipIfUnchanged] (renaming)
 * keeping the same name just closes; turn it off when the name is picked for a new file.
 */
class RenameFileDialog(
    context: Context,
    private val currentName: String,
    @StringRes private val titleRes: Int = R.string.file_action_rename,
    @StringRes private val failedRes: Int = R.string.file_rename_failed,
    private val skipIfUnchanged: Boolean = true,
    private val onSave: (String) -> Boolean,
) : BaseDialog<DialogRenameFileBinding>(context, DialogRenameFileBinding::inflate, true) {

    override fun DialogRenameFileBinding.initView() {
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        tvTitle.setText(titleRes)
        edtName.setText(currentName)
        edtName.setSelection(currentName.length)
        edtName.requestFocus()
        icClear.isVisible = currentName.isNotEmpty()
        edtName.doAfterTextChanged { icClear.isVisible = !it.isNullOrEmpty() }
        icClear.click { edtName.text?.clear() }
        edtName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit()
                true
            } else false
        }
        btnConfirm.click { submit() }
        btnCancel.click {
            hideKeyboard()
            dismiss()
        }
    }

    private fun submit() {
        val newName = binding.edtName.text?.toString()?.trim().orEmpty()
        val error = when {
            newName.isEmpty() -> R.string.file_rename_empty
            newName.any { it in INVALID_CHARS } -> R.string.file_rename_invalid
            else -> null
        }
        if (error != null) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            return
        }
        if (skipIfUnchanged && newName == currentName) {
            hideKeyboard()
            dismiss()
            return
        }
        if (onSave(newName)) {
            hideKeyboard()
            dismiss()
        } else {
            Toast.makeText(context, failedRes, Toast.LENGTH_SHORT).show()
        }
    }

    private companion object {
        const val INVALID_CHARS = "/\\:*?\"<>|"
    }
}
