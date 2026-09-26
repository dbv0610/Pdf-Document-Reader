package com.alf06.document.reader.ui.home.main

import androidx.core.view.isVisible
import com.alf06.document.reader.R
import com.alf06.document.reader.base.shareFile
import com.alf06.document.reader.databinding.FragmentStorageBinding
import com.alf06.document.reader.model.RecentUi
import com.alf06.document.reader.model.StorageTab
import com.alf06.document.reader.ui.adapter.DocumentAdapter
import com.alf06.document.reader.ui.dialog.DeleteFileDialog
import com.alf06.document.reader.ui.dialog.FileOptionsBottomSheet
import com.alf06.document.reader.ui.dialog.RenameFileDialog
import com.alf06.document.reader.ui.home.document.openDocument
import com.alf06.document.reader.viewmodel.DocumentViewModel
import com.ui.baselib.base.BaseFragment
import com.ui.baselib.extensions.click
import com.ui.baselib.lifecycle.collectFlow
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.io.File
import com.alf06.document.reader.ui.home.setting.SettingActivity

class StorageFragment : BaseFragment<FragmentStorageBinding>(FragmentStorageBinding::inflate) {
    override val isFullSc: Boolean
        get() = false
    val viewModel: DocumentViewModel by activityViewModel()
    private var scrollToTopOnSubmit = false

    override fun FragmentStorageBinding.initView() {
        val documentAdapter = DocumentAdapter(
            onItemClick = { item ->
                if (context?.openDocument(item.document) == true) {
                    viewModel.addToRecent(item.document)
                }
            },
            onFavoriteClick = { item -> viewModel.toggleFavoriteRecent(item.document) },
            onMoreClick = ::showFileOptions,
        )
        rcvItemContent.adapter = documentAdapter

        collectFlow(viewModel.storageTab) { tab ->
            tabRecent.isSelected = tab == StorageTab.Recent
            tabFavorites.isSelected = tab == StorageTab.Favorites
        }
        collectFlow(viewModel.storageDocuments) { documents ->
            documentAdapter.submitList(documents) {
                if (scrollToTopOnSubmit) {
                    scrollToTopOnSubmit = false
                    rcvItemContent.scrollToPosition(0)
                }
            }
            layoutEmpty.isVisible = documents.isEmpty()
        }
    }

    override fun FragmentStorageBinding.onClick() {
        icSetting.click { launchActivity<SettingActivity>() }
        tabRecent.click { selectTab(StorageTab.Recent) }
        tabFavorites.click { selectTab(StorageTab.Favorites) }
    }

    private fun selectTab(tab: StorageTab) {
        if (viewModel.storageTab.value == tab) return
        scrollToTopOnSubmit = true
        viewModel.setStorageTab(tab)
    }

    private fun showFileOptions(item: RecentUi) {
        val activity = activity ?: return
        val document = item.document
        FileOptionsBottomSheet(
            activity = activity,
            item = item,
            onRename = {
                RenameFileDialog(activity, File(document.path).nameWithoutExtension) { newName ->
                    viewModel.renameFile(document, newName)
                }.show()
            },
            onFavorite = { viewModel.toggleFavoriteRecent(document) },
            onShare = {
                if (!activity.shareFile(document.path)) toast(R.string.file_share_failed)
            },
            onDelete = {
                DeleteFileDialog(activity) {
                    viewModel.deleteFile(document) { deleted ->
                        if (!deleted) toast(R.string.file_delete_failed)
                    }
                }.show()
            },
        ).show()
    }
}
