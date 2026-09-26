package com.alf06.document.reader.ui.home.tools.zip.fragment

import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.FragmentPickFilesBinding
import com.alf06.document.reader.ui.adapter.MultiPickFileAdapter
import com.alf06.document.reader.ui.home.tools.zip.CreateZipViewModel
import com.alf06.document.reader.ui.home.tools.zip.ZipNavigator
import com.alf06.document.reader.viewmodel.DocumentViewModel
import com.ui.baselib.base.BaseFragment
import com.ui.baselib.extensions.afterTextChanged
import com.ui.baselib.extensions.click
import com.ui.baselib.lifecycle.collectFlow
import kotlinx.coroutines.flow.combine
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class SelectZipFilesFragment :
    BaseFragment<FragmentPickFilesBinding>(FragmentPickFilesBinding::inflate) {
    private val documentViewModel: DocumentViewModel by activityViewModel()
    private val viewModel: CreateZipViewModel by activityViewModel()

    private val fileAdapter = MultiPickFileAdapter(onToggle = { item -> viewModel.toggle(item.document) })

    override fun FragmentPickFilesBinding.initView() {
        rcvFiles.adapter = fileAdapter
        tvTitle.setText(R.string.select)

        layoutSearch.isVisible = true

        documentViewModel.setSearchCriteria(key = edtSearch.text.toString(), type = null)

        collectFlow(
            combine(documentViewModel.listDocumentSearch, documentViewModel.isScanning, ::Pair)
        ) { (documents, scanning) ->
            fileAdapter.submitList(documents)
            val empty = documents.isEmpty() && !scanning
            layoutEmpty.isVisible = empty
            rcvFiles.isVisible = !empty
        }
        collectFlow(viewModel.files) { files ->
            fileAdapter.setSelectedPaths(files.mapTo(HashSet()) { it.path })
            btnContinue.isVisible = files.isNotEmpty()
            btnContinue.text = getString(R.string.continue_count, files.size)
        }
    }

    override fun FragmentPickFilesBinding.onClick() {
        icBack.click { activity?.finish() }
        icClear.click { edtSearch.setText("") }
        edtSearch.afterTextChanged { key ->
            icClear.isVisible = key.isNotEmpty()
            documentViewModel.setSearchCriteria(key = key, type = null)
        }
        edtSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId != EditorInfo.IME_ACTION_SEARCH) return@setOnEditorActionListener false
            hideKeyboard()
            true
        }
        btnContinue.click {
            hideKeyboard()
            navigateTo(ZipNavigator.ZipFiles.route)
        }
    }
}
