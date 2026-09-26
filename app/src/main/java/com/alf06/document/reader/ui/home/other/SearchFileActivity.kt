package com.alf06.document.reader.ui.home.other

import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import com.alf06.document.reader.R
import com.alf06.document.reader.base.shareFile
import com.alf06.document.reader.databinding.ActivitySearchFileBinding
import com.alf06.document.reader.model.RecentUi
import com.alf06.document.reader.ui.adapter.DocumentAdapter
import com.alf06.document.reader.ui.dialog.DeleteFileDialog
import com.alf06.document.reader.ui.dialog.FileOptionsBottomSheet
import com.alf06.document.reader.ui.dialog.RenameFileDialog
import com.alf06.document.reader.ui.home.document.openDocument
import com.alf06.document.reader.viewmodel.DocumentViewModel
import com.ui.baselib.base.BaseActivity
import com.ui.baselib.base.showKeyboardOn
import com.ui.baselib.extensions.afterTextChanged
import com.ui.baselib.extensions.click
import com.ui.baselib.lifecycle.collectFlow
import kotlinx.coroutines.flow.combine
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class SearchFileActivity :
    BaseActivity<ActivitySearchFileBinding>(ActivitySearchFileBinding::inflate) {
    private val viewModel: DocumentViewModel by viewModel()

    private val documentAdapter = DocumentAdapter(
        onItemClick = { item ->
            if (openDocument(item.document)) viewModel.addToRecent(item.document)
        },
        onFavoriteClick = { item -> viewModel.toggleFavoriteRecent(item.document) },
        onMoreClick = ::showFileOptions,
    )

    override fun backPressed() {
        finish()
    }

    override fun initialize() {
        viewModel.setSearchCriteria(key = "", type = null)
    }

    override fun ActivitySearchFileBinding.setData() {
        rcvItemContent.adapter = documentAdapter
        rcvItemContent.itemAnimator = null
        edtSearch.afterTextChanged { key ->
            viewModel.setSearchCriteria(key = key, type = null)
            textEmptyState.isVisible = key.isBlank()
        }
        textEmptyState.isVisible = edtSearch.text.toString().isBlank()
        collectFlow(
            combine(viewModel.listDocumentSearch, viewModel.searchKey, ::Pair)
        ) { (documents, key) ->
            val results = if (key.isBlank()) emptyList() else documents
            documentAdapter.submitList(results)
            rcvItemContent.isVisible = results.isNotEmpty()
            layoutEmpty.isVisible = key.isNotBlank() && results.isEmpty()
        }
        edtSearch.requestFocus()
        edtSearch.post { showKeyboardOn(edtSearch) }
    }

    override fun ActivitySearchFileBinding.onClick() {
        icBackApp.click { backPressed() }
        icClear.click { edtSearch.setText("") }
        edtSearch.afterTextChanged { icClear.isVisible = it.isNotEmpty() }
        edtSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId != EditorInfo.IME_ACTION_SEARCH) return@setOnEditorActionListener false
            hideKeyboard()
            true
        }
    }

    private fun showFileOptions(item: RecentUi) {
        val document = item.document
        FileOptionsBottomSheet(
            activity = this,
            item = item,
            onRename = {
                RenameFileDialog(this, File(document.path).nameWithoutExtension) { newName ->
                    viewModel.renameFile(document, newName)
                }.show()
            },
            onFavorite = { viewModel.toggleFavoriteRecent(document) },
            onShare = {
                if (!shareFile(document.path)) toast(R.string.file_share_failed)
            },
            onDelete = {
                DeleteFileDialog(this) {
                    viewModel.deleteFile(document) { deleted ->
                        if (!deleted) toast(R.string.file_delete_failed)
                    }
                }.show()
            },
        ).show()
    }
}
