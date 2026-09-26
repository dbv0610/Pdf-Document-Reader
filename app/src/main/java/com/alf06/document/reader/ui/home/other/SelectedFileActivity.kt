package com.alf06.document.reader.ui.home.other

import androidx.core.view.isVisible
import com.alf06.document.reader.R
import com.alf06.document.reader.base.shareFile
import com.alf06.document.reader.databinding.ActivitySelectedFileBinding
import com.alf06.document.reader.model.RecentUi
import com.alf06.document.reader.ui.adapter.SelectFileAdapter
import com.alf06.document.reader.ui.dialog.DeleteFileDialog
import com.alf06.document.reader.ui.dialog.FileOptionsBottomSheet
import com.alf06.document.reader.ui.dialog.RenameFileDialog
import com.alf06.document.reader.viewmodel.DocumentViewModel
import com.ui.baselib.base.BaseActivity
import com.ui.baselib.extensions.click
import com.ui.baselib.lifecycle.collectFlow
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class SelectedFileActivity :
    BaseActivity<ActivitySelectedFileBinding>(ActivitySelectedFileBinding::inflate) {
    private val viewModel: DocumentViewModel by viewModel()

    private val selectAdapter = SelectFileAdapter(
        onSelectionChanged = ::renderSelection,
        onMoreClick = ::showFileOptions,
    )

    override fun backPressed() {
        finish()
    }

    override fun initialize() {
    }

    override fun ActivitySelectedFileBinding.setData() {
        rcvItemSelect.adapter = selectAdapter
        renderSelection(emptyList())

        collectFlow(viewModel.listDocumentSearch) { documents ->
            selectAdapter.submitList(documents)
            layoutEmpty.isVisible = documents.isEmpty()
            rcvItemSelect.isVisible = documents.isNotEmpty()
        }
    }

    override fun ActivitySelectedFileBinding.onClick() {
        icBackApp.click { backPressed() }
        btnDelete.click { confirmDeleteSelected() }
    }

    private fun renderSelection(selected: List<RecentUi>) {
        binding.tvSelected.text = getString(R.string.selected, selected.size.toString())
        binding.btnDelete.isVisible = selected.isNotEmpty()
    }

    private fun confirmDeleteSelected() {
        val selected = selectAdapter.selectedItems
        if (selected.isEmpty()) return
        DeleteFileDialog(
            context = this,
            titleRes = R.string.delete_selected_title,
            messageRes = R.string.delete_selected_message,
        ) {
            viewModel.deleteFiles(selected.map { it.document }) { failedCount ->
                if (failedCount > 0) {
                    toast(getString(R.string.delete_selected_failed, failedCount))
                }
            }
        }.show()
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
