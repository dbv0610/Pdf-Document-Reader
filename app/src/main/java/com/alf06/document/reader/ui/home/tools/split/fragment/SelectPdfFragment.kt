package com.alf06.document.reader.ui.home.tools.split.fragment

import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.FragmentSplitSelectFileBinding
import com.alf06.document.reader.model.DocumentType
import com.alf06.document.reader.ui.adapter.PickFileAdapter
import com.alf06.document.reader.ui.dialog.DialogProcess
import com.alf06.document.reader.ui.dialog.DocumentPasswordDialog
import com.alf06.document.reader.ui.home.tools.split.OpenState
import com.alf06.document.reader.ui.home.tools.split.SplitNavigator
import com.alf06.document.reader.ui.home.tools.split.SplitPdfViewModel
import com.alf06.document.reader.viewmodel.DocumentViewModel
import com.ui.baselib.base.BaseFragment
import com.ui.baselib.extensions.afterTextChanged
import com.ui.baselib.extensions.click
import com.ui.baselib.lifecycle.collectFlow
import kotlinx.coroutines.flow.combine
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class SelectPdfFragment :
    BaseFragment<FragmentSplitSelectFileBinding>(FragmentSplitSelectFileBinding::inflate) {
    private val loadingDialogLazy = lazy { DialogProcess(requireActivity()) }
    private val loadingDialog by loadingDialogLazy
    private val documentViewModel: DocumentViewModel by activityViewModel()
    private val viewModel: SplitPdfViewModel by activityViewModel()

    private val fileAdapter = PickFileAdapter(onItemClick = { item ->
        hideKeyboard()
        viewModel.open(item.document)
    })

    private var passwordDialog: DocumentPasswordDialog? = null

    override fun FragmentSplitSelectFileBinding.initView() {
        rcvFiles.adapter = fileAdapter
        documentViewModel.setSearchCriteria(key = edtSearch.text.toString(), type = DocumentType.Pdf)

        collectFlow(
            combine(
                documentViewModel.listDocumentSearch,
                documentViewModel.searchKey,
                documentViewModel.isScanning,
                ::Triple
            )
        ) { (documents, key, scanning) ->
            fileAdapter.submitList(documents)
            val empty = documents.isEmpty() && !scanning

            layoutSearch.isVisible = !(empty && key.isBlank())
            layoutEmpty.isVisible = empty
            rcvFiles.isVisible = !empty
        }
        collectFlow(viewModel.openState) { state ->
            loadingDialog.setShowing(state == OpenState.Opening)
            when (state) {
                OpenState.Opened -> {
                    viewModel.consumeOpenResult()
                    navigateTo(SplitNavigator.SelectPages.route)
                }
                is OpenState.PasswordRequired -> {
                    viewModel.consumeOpenResult()
                    askPassword(state)
                }
                OpenState.Failed -> {
                    viewModel.consumeOpenResult()
                    toast(R.string.split_open_failed)
                }
                else -> Unit
            }
        }
    }

    override fun FragmentSplitSelectFileBinding.onClick() {
        icBack.click { activity?.finish() }
        icClear.click { edtSearch.setText("") }
        edtSearch.afterTextChanged { key ->
            icClear.isVisible = key.isNotEmpty()
            documentViewModel.setSearchCriteria(key = key, type = DocumentType.Pdf)
        }
        edtSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId != EditorInfo.IME_ACTION_SEARCH) return@setOnEditorActionListener false
            hideKeyboard()
            true
        }
    }

    private fun askPassword(state: OpenState.PasswordRequired) {
        val activity = activity ?: return
        if (passwordDialog?.isShowing == true) return
        val message = getString(
            if (state.incorrect) R.string.error_file_password_incorrect else R.string.error_file_password
        )
        passwordDialog = DocumentPasswordDialog(activity, message) { password ->
            passwordDialog?.dismiss()
            viewModel.open(state.file, password)
        }.apply {
            setOnDismissListener { passwordDialog = null }
            show()
        }
    }

    override fun onDestroyView() {
        if (loadingDialogLazy.isInitialized()) loadingDialog.dismiss()
        passwordDialog?.dismiss()
        passwordDialog = null
        super.onDestroyView()
    }
}
