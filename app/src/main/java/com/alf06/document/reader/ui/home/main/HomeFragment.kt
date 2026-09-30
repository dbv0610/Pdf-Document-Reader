package com.alf06.document.reader.ui.home.main

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.alf06.document.reader.R
import com.alf06.document.reader.base.shareFile
import com.alf06.document.reader.model.RecentUi
import com.alf06.document.reader.ui.dialog.DeleteFileDialog
import com.alf06.document.reader.ui.dialog.FileOptionsBottomSheet
import com.alf06.document.reader.ui.dialog.RenameFileDialog
import com.alf06.document.reader.ui.dialog.SortBottomSheet
import com.alf06.document.reader.ui.home.document.openDocument
import com.alf06.document.reader.ui.home.other.SearchFileActivity
import com.alf06.document.reader.databinding.FragmentHomeBinding
import com.alf06.document.reader.ui.adapter.DocumentAdapter
import com.alf06.document.reader.ui.home.other.SelectedFileActivity
import com.alf06.document.reader.viewmodel.DocumentViewModel
import com.ui.baselib.base.BaseFragment
import com.ui.baselib.extensions.click
import com.ui.baselib.lifecycle.collectFlow
import kotlinx.coroutines.flow.combine
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.io.File
import com.alf06.document.reader.databinding.ItemHomeOptionBinding
import com.alf06.document.reader.ui.home.scanner.DocumentScannerActivity
import com.alf06.document.reader.ui.home.setting.SettingActivity
import com.alf06.document.reader.ui.home.tools.image_to_pdf.ImageToPdfActivity
import com.alf06.document.reader.ui.home.tools.translate.TranslatePdfActivity
import com.alf06.document.reader.ui.home.tools.zip.CreateZipActivity
import com.alf06.document.reader.ui.dialog.DialogProcess
import com.alf06.document.reader.ui.home.document.office.ReadDocumentActivity
import com.wxiwei.office.editor.DocumentCreator

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    override val isFullSc: Boolean
        get() = false
    val viewModel: DocumentViewModel by activityViewModel()
    private val createViewModel: CreateDocumentViewModel by viewModels()
    private var createProgress: DialogProcess? = null
    private var createDialog: android.app.Dialog? = null

    override fun FragmentHomeBinding.initView() {
        btnCreateDocument.click { chooseDocumentType() }
        collectFlow(createViewModel.state) { state ->
            btnCreateDocument.isEnabled = state != CreateDocumentViewModel.State.Creating
            if (state == CreateDocumentViewModel.State.Creating) {
                if (createProgress == null) createProgress = DialogProcess(requireActivity())
                createProgress?.setShowing(true)
            } else {
                createProgress?.dismiss()
                createProgress = null
            }
            when (state) {
                is CreateDocumentViewModel.State.Created -> {
                    createViewModel.consumeResult()
                    viewModel.addToRecent(state.document)
                    viewModel.refresh()
                    startActivity(Intent(requireContext(), ReadDocumentActivity::class.java)
                        .putExtra(ReadDocumentActivity.ARG_DOCUMENT, state.document)
                        .putExtra(ReadDocumentActivity.ARG_START_EDITING, true))
                }
                is CreateDocumentViewModel.State.Failed -> {
                    createViewModel.consumeResult()
                    toast(if (state.exists) R.string.file_name_exists else R.string.create_document_failed)
                    askDocumentName(state.format, state.name)
                }
                else -> Unit
            }
        }
        val tabFilterAdapter = TabFilterAdapter { filter ->
            viewModel.setFileTypeFilter(filter)
        }
        rcvTabMenu.adapter = tabFilterAdapter
        rcvTabMenu.itemAnimator = null
        collectFlow(viewModel.fileTypeFilter) { filter ->
            tabFilterAdapter.setSelected(filter)
        }

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
        collectFlow(
            combine(viewModel.listDocumentSearch, viewModel.isScanning, ::Pair)
        ) { (documents, isScanning) ->
            documentAdapter.submitList(documents){
                rcvItemContent.scrollToPosition(0)
            }
            progressLoading.isVisible = isScanning && documents.isEmpty()
            layoutEmpty.isVisible = !isScanning && documents.isEmpty()
        }
        itemTranslate.setupView(R.drawable.img_home_option_1, R.string.translate_pdf).root.click {
            launchActivity<TranslatePdfActivity>()
        }
        itemScanToPdf.setupView(R.drawable.img_home_option_2, R.string.scan_to_pdf).root.click{
            launchActivity<DocumentScannerActivity>()
        }
        itemImgToPDF.setupView(R.drawable.img_home_option_3, R.string.iamge_to_pdf).root.click {
            launchActivity<ImageToPdfActivity>()
        }
        itemCreateZip.setupView(R.drawable.img_home_option_4, R.string.create_zip_file).root.click {
            launchActivity<CreateZipActivity>()
        }
    }

    fun ItemHomeOptionBinding.setupView(@DrawableRes icon: Int,@StringRes title: Int): ItemHomeOptionBinding  = apply{
        imgContent.setImageResource(icon)
        tvTitle.text = getString(title)
    }

    override fun FragmentHomeBinding.onClick() {
        icSetting.click { launchActivity<SettingActivity>() }
        icAppFilter.click { showSortOptions() }
        icSelectedCheck.click { launchActivity<SelectedFileActivity>() }
        lnSearch.click {
            launchActivity<SearchFileActivity>()
        }
    }

    private fun showSortOptions() {
        val activity = activity ?: return
        SortBottomSheet(
            activity = activity,
            sortBy = viewModel.sortByData.value,
            order = viewModel.sortOrder.value,
        ) { sortBy, order ->
            viewModel.setSort(sortBy, order)
        }.show()
    }

    private fun chooseDocumentType() {
        val formats = DocumentCreator.Format.entries
        createDialog?.dismiss()
        createDialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.create_document)
            .setItems(R.array.new_document_types) { _, index -> askDocumentName(formats[index]) }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun askDocumentName(format: DocumentCreator.Format, name: String? = null) {
        createDialog?.dismiss()
        val defaultName = when (format) {
            DocumentCreator.Format.WORD -> R.string.new_word_name
            DocumentCreator.Format.EXCEL -> R.string.new_excel_name
            DocumentCreator.Format.POWERPOINT -> R.string.new_powerpoint_name
        }
        createDialog = RenameFileDialog(
            context = requireActivity(),
            currentName = name ?: getString(defaultName),
            titleRes = R.string.create_document,
            skipIfUnchanged = false,
        ) { enteredName ->
            createViewModel.create(format, enteredName)
            true
        }.also { it.show() }
    }

    override fun onDestroyView() {
        createDialog?.dismiss()
        createDialog = null
        createProgress?.dismiss()
        createProgress = null
        super.onDestroyView()
    }

    private fun showFileOptions(item: RecentUi) {
        val activity = activity ?: return
        val document = item.document
        FileOptionsBottomSheet(
            activity = activity,
            item = item,
            onRename = {
                RenameFileDialog(activity, File(document.path).nameWithoutExtension) { newName ->
                    viewModel.renameFile(model = document, newBaseName = newName)
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
