package com.alf06.document.reader.ui.home.document.office

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.alf06.document.reader.R
import com.alf06.document.reader.base.shareFile
import com.alf06.document.reader.databinding.ActivityReadDocumentBinding
import com.alf06.document.reader.model.DocumentPage
import com.alf06.document.reader.model.DocumentType
import com.alf06.document.reader.model.PageViewType
import com.alf06.document.reader.model.RecentDocument
import com.alf06.document.reader.model.RecentUi
import com.alf06.document.reader.ui.adapter.DocumentPreviewAdapter
import com.alf06.document.reader.ui.dialog.DeleteFileDialog
import com.alf06.document.reader.ui.dialog.DialogProcess
import com.alf06.document.reader.ui.dialog.DocumentPasswordDialog
import com.alf06.document.reader.ui.dialog.FileOptionsBottomSheet
import com.alf06.document.reader.ui.dialog.OpenFileErrorDialog
import com.alf06.document.reader.ui.dialog.RenameFileDialog
import com.alf06.document.reader.ui.home.document.layoutThumbnailStrip
import com.alf06.document.reader.ui.home.document.office.edit.ExcelEditPanel
import com.alf06.document.reader.ui.home.document.office.edit.OfficeEditPanel
import com.alf06.document.reader.ui.home.document.office.edit.SlideEditPanel
import com.alf06.document.reader.ui.home.document.office.edit.WordEditPanel
import com.alf06.document.reader.viewmodel.DocumentViewModel
import com.ui.baselib.api.parcelable
import com.ui.baselib.base.BaseActivity
import com.ui.baselib.extensions.click
import com.ui.baselib.extensions.gone
import com.ui.baselib.extensions.invisible
import com.ui.baselib.extensions.visible
import com.ui.baselib.lifecycle.collectFlow
import com.wxiwei.office.reader.OfficeDocumentView
import com.wxiwei.office.reader.ReaderState
import com.wxiwei.office.system.DocumentPasswords
import com.wxiwei.office.system.OfficeFileType
import com.wxiwei.office.system.OpenFileException
import com.wxiwei.office.system.OpenTrace
import com.wxiwei.office.system.search.DocumentSearch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import kotlin.math.ceil

class ReadDocumentActivity :
    BaseActivity<ActivityReadDocumentBinding>(ActivityReadDocumentBinding::inflate) {
    private val viewModel: ReadDocumentViewModel by viewModel()
    private val documentViewModel: DocumentViewModel by viewModel()

    private var document: RecentDocument? = null
    private val processDialog by lazy { DialogProcess(this) }

    private val loadingDialogLazy = lazy {
        DialogProcess(this).apply {
            setOnKeyListener { _, keyCode, event ->
                if (keyCode != KeyEvent.KEYCODE_BACK) return@setOnKeyListener false
                if (event.action == KeyEvent.ACTION_UP) {
                    dismiss()
                    reader?.abort()
                    finish()
                }
                true
            }
        }
    }
    private val loadingDialog by loadingDialogLazy
    private var openErrorDialog: OpenFileErrorDialog? = null
    private var passwordDialog: DocumentPasswordDialog? = null

    private var reader: OfficeDocumentView? = null
    private var editPanel: OfficeEditPanel? = null

    private var search: DocumentSearch? = null
    private var searchJob: Job? = null
    private var searchCount = 0
    private var searchIndex = 0

    private val thumbnailAdapter by lazy {
        DocumentPreviewAdapter(lifecycleScope, { index -> reader?.thumbnails?.get(index + 1) }) { page ->
            try {
                if (reader?.jumpToPage(page.index + 1) != true) toast(R.string.error_go_to_page)
            } catch (e: Exception) {
                toast(R.string.error_go_to_page)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        document = savedInstanceState?.parcelable(STATE_DOCUMENT) ?: intent.parcelable(ARG_DOCUMENT)
        super.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        document?.let { outState.putParcelable(STATE_DOCUMENT, it) }
        super.onSaveInstanceState(outState)
    }

    override fun initialize() {
        val type = document?.type ?: DocumentType.Doc
        binding.fileName.setText(type.titleRes())
    }

    /** Keeps the current page's thumbnail on screen; pages without a thumbnail yet are skipped. */
    private fun scrollThumbnailTo(index: Int) {
        if (index in 0 until thumbnailAdapter.itemCount) binding.rcvFrameData.scrollToPosition(index)
    }

    override fun ActivityReadDocumentBinding.setData() {
        val document = document ?: run {
            toast(R.string.error_file_not_found)
            finish()
            return
        }
        fileName.text = File(document.path).name
        txtNumberPage.isVisible = document.type != DocumentType.Excel
        rcvFrameData.adapter = thumbnailAdapter
        main.layoutThumbnailStrip(
            header = lnHeader,
            content = lnDocRead,
            strip = rcvFrameData,
            landscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE,
        )
        val reader = initReader(document.path)

        collectFlow(reader.state.map { it.isLoading }.distinctUntilChanged()) { loading ->
            if (loading && !isFinishing) loadingDialog.show()
            else if (loadingDialogLazy.isInitialized()) loadingDialog.dismiss()
        }
        collectFlow(reader.state.map { it.pageNumber to it.pageCount }.distinctUntilChanged()) { (page, count) ->
            if (count == 0) return@collectFlow
            txtNumberPage.fixWidthFor(count)
            txtNumberPage.text = "$page/$count"
            val index = (page - 1).coerceAtLeast(0)
            thumbnailAdapter.setCurrentPage(index)
            scrollThumbnailTo(index)
        }
        collectFlow(reader.state.map { it.thumbnailCount }.distinctUntilChanged()) { count ->
            val shown = thumbnailAdapter.itemCount
            thumbnailAdapter.submitList(List(count) { DocumentPage(it) }) {
                // Thumbnails arrive after the page number, so follow the current page once its thumbnail exists.
                val index = reader.state.value.pageNumber - 1
                if (index >= shown) scrollThumbnailTo(index)
            }
        }

        lifecycleScope.launch {
            reader.thumbnailInvalidated.collect { page ->
                val index = page - 1
                if (index in 0 until thumbnailAdapter.itemCount) thumbnailAdapter.notifyItemChanged(index)
            }
        }

        lifecycleScope.launch {
            val opened = reader.state.first {
                it.status == ReaderState.Status.Failed ||
                    (it.status == ReaderState.Status.Ready && it.thumbnailCount > 0)
            }
            if (opened.status == ReaderState.Status.Ready && opened.fileType != OfficeFileType.OTHER) {
                viewModel.applyDefaultPageState(PageViewType.Thumbnail)
            }
        }

        collectFlow(
            viewModel.pageViewState.map { it == PageViewType.Thumbnail }.distinctUntilChanged()
        ) { show ->

            TransitionManager.beginDelayedTransition(
                main,
                AutoTransition().excludeTarget(R.id.lnDocRead, true).excludeChildren(R.id.lnDocRead, true)
            )
            rcvFrameData.isVisible = show
        }
        collectFlow(
            combine(
                viewModel.pageViewState,
                reader.state.map { it.status == ReaderState.Status.Opening },
                ::Pair
            )
        ) { (state, opening) ->
            progressLoad.isVisible = state == PageViewType.Thumbnail && opening
        }
        renderSearchState()
        // editing: .docx / .xlsx / .pptx once the document is open
        collectFlow(reader.state.map { it.status == ReaderState.Status.Ready }.distinctUntilChanged()) { ready ->
            icEditApp.isVisible = ready && File(document.path).extension.lowercase() in EDITABLE_EXTENSIONS
            if (!ready) closeEditPanel()
        }
    }

    override fun ActivityReadDocumentBinding.onClick() {
        icEditApp.click { toggleEditPanel() }
        icBackApp.click { backPressed() }
        icSearchApp.click {
            lnHeaderDef.gone()
            lnSearchData.visible()
            edtSearchData.requestFocus()
            edtSearchData.post { showKeyboard(edtSearchData) }
        }
        icBackSearch.click { closeSearch() }
        icAppRotate.click { toggleOrientation() }
        icOpenTools.click { showFileOptions() }
        icSearchData.setOnClickListener { performSearch(edtSearchData.text.toString().trim()) }
        icViewNext.setOnClickListener { focusSearchResult(searchIndex + 1) }
        icViewPrev.setOnClickListener { focusSearchResult(searchIndex - 1) }
        edtSearchData.onActionSearch(::performSearch)
    }

    override fun backPressed() {
        if (editPanel != null) {
            closeEditPanel()
            return
        }
        if (binding.lnSearchData.isVisible) {
            closeSearch()
            return
        }
        finish()
    }

    private fun toggleEditPanel() {
        if (editPanel != null) {
            closeEditPanel()
            return
        }
        val reader = reader ?: return
        val file = File(document?.path ?: return)
        if (reader.state.value.status != ReaderState.Status.Ready || reader.control == null) {
            toast(R.string.edit_not_ready)
            return
        }
        val panel = try {
            when (file.extension.lowercase()) {
                "xlsx", "xlsm" -> ExcelEditPanel(this, reader, file)
                "pptx" -> SlideEditPanel(this, reader, file)
                "docx" -> WordEditPanel(this, reader, file)
                else -> null
            }
        } catch (e: Exception) {
            Log.d(TAG, "Edit error: ${e.message}")
            null
        } ?: run {
            toast(R.string.edit_not_supported)
            return
        }
        editPanel = panel
        binding.editPanel.removeAllViews()
        binding.editPanel.addView(panel.view)
        binding.editPanel.visible()
        binding.icEditApp.alpha = 0.5f
    }

    private fun closeEditPanel() {
        val panel = editPanel ?: return
        panel.close()
        editPanel = null
        hideKeyboard()
        binding.editPanel.removeAllViews()
        binding.editPanel.gone()
        binding.icEditApp.alpha = 1f
    }

    private fun showFileOptions() {
        val document = document ?: return
        lifecycleScope.launch {
            val item = RecentUi(document, documentViewModel.isFavorite(document))
            if (isFinishing || isDestroyed) return@launch
            val pageState = viewModel.pageViewState.value
            val pagedViews = document.type != DocumentType.Excel
            FileOptionsBottomSheet(
                activity = this@ReadDocumentActivity,
                item = item,
                onRename = { renameDocument(document) },
                onFavorite = { documentViewModel.toggleFavoriteRecent(document) },
                onShare = {
                    if (!shareFile(document.path)) toast(R.string.file_share_failed)
                },
                onDelete = {
                    DeleteFileDialog(this@ReadDocumentActivity) {
                        documentViewModel.deleteFile(document) { deleted ->
                            if (deleted) finish() else toast(R.string.file_delete_failed)
                        }
                    }.show()
                },
                onThumbnail = { viewModel.setPageState(PageViewType.Thumbnail) }
                    .takeIf { pagedViews && pageState != PageViewType.Thumbnail },
                onPageByPage = { viewModel.setPageState(PageViewType.PageByPage) }
                    .takeIf { pagedViews && pageState != PageViewType.PageByPage },
                onSlideShow = ::startSlideShow.takeIf { document.type == DocumentType.Ppt },
            ).show()
        }
    }

    private fun startSlideShow() {
        val reader = reader
        val slides = reader?.thumbnails
        val count = reader?.state?.value?.pageCount ?: 0
        if (reader == null || slides == null || count == 0) {
            toast(R.string.can_slide_show_now)
            return
        }
        val metrics = resources.displayMetrics
        val width = maxOf(metrics.widthPixels, metrics.heightPixels).coerceAtMost(MAX_SLIDE_WIDTH)
        SlideShowActivity.source = SlideShowActivity.Source(
            count = count,
            preview = slides::get,
            load = { page -> slides.render(page, width) },
            invalidated = reader.thumbnailInvalidated,
        )
        launchActivity<SlideShowActivity>()
    }

    private fun renameDocument(document: RecentDocument) {
        RenameFileDialog(this, File(document.path).nameWithoutExtension) { newName ->
            documentViewModel.renameFile(document, newName) { renamed ->
                if (renamed == null) {
                    toast(R.string.file_rename_failed)
                    return@renameFile
                }
                if (isFinishing || isDestroyed) return@renameFile
                // The activity reopens the file on rotation, so later opens must use the new path.
                DocumentPasswords.get(document.path)?.let { password ->
                    DocumentPasswords.set(renamed.path, password)
                    DocumentPasswords.clear(document.path)
                }
                this.document = renamed
                intent.putExtra(ARG_DOCUMENT, renamed)
                binding.fileName.text = File(renamed.path).name
            }
        }.show()
    }

    private fun toggleOrientation() {
        requestedOrientation =
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun TextView.fixWidthFor(pageCount: Int) {
        val digits = "8".repeat(pageCount.toString().length)
        val width = ceil(paint.measureText("$digits/$digits")).toInt() + totalPaddingLeft + totalPaddingRight
        if (minWidth != width || maxWidth != width) setWidth(width)
    }

    private fun EditText.onActionSearch(onSearch: (String) -> Unit) {
        setOnEditorActionListener { _, actionId, _ ->
            if (actionId != EditorInfo.IME_ACTION_SEARCH) return@setOnEditorActionListener false
            val query = text.toString().trim()
            if (query.isNotEmpty()) {
                val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(windowToken, 0)
            }
            onSearch(query)
            true
        }
    }

    private fun performSearch(keyword: String) {
        if (keyword.isEmpty()) {
            toast(R.string.please_enter_a_search)
            return
        }
        hideKeyboard()
        searchJob?.cancel()
        clearSearchResults()
        val documentSearch = binding.officeViewer.newSearch() ?: return
        processDialog.show()
        searchJob = lifecycleScope.launch {
            val count = try {
                withContext(Dispatchers.Default) { documentSearch.search(keyword) { isActive } }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.d(TAG, "Search error: ${e.message}")
                toast(R.string.search_error_occurred)
                0
            } finally {
                if (searchJob === coroutineContext[Job]) processDialog.dismiss()
            }
            if (isFinishing || isDestroyed) return@launch
            search = documentSearch
            searchCount = count
            focusSearchResult(0)
            binding.lnResultSearch.visible()
        }
    }

    private fun focusSearchResult(index: Int) {
        if (index in 0 until searchCount) {
            try {
                search?.focus(index)
            } catch (e: Exception) {
                Log.d(TAG, "Focus error: ${e.message}")
            }
            searchIndex = index
        }
        renderSearchState()
    }

    private fun renderSearchState() {
        binding.tvCountSearch.text = if (searchCount == 0) {
            getString(R.string.no_result_found)
        } else {
            "${getString(R.string.result)} ${searchIndex + 1}/$searchCount"
        }
        val canPrev = searchIndex > 0
        val canNext = searchIndex < searchCount - 1
        binding.icViewPrev.isEnabled = canPrev
        binding.icViewPrev.alpha = if (canPrev) 1f else DISABLED_ALPHA
        binding.icViewNext.isEnabled = canNext
        binding.icViewNext.alpha = if (canNext) 1f else DISABLED_ALPHA
    }

    private fun clearSearchResults() {
        search?.clear()
        search = null
        searchCount = 0
        searchIndex = 0
        renderSearchState()
    }

    private fun closeSearch() {
        hideKeyboard()
        binding.lnHeaderDef.visible()
        binding.lnSearchData.invisible()
        binding.lnResultSearch.gone()
        binding.edtSearchData.setText("")
        searchJob?.cancel()
        clearSearchResults()
    }

    private fun onOpenFailure(error: OpenFileException): Boolean {
        OpenTrace.e("host open failed reason=${error.reason} path=${error.filePath}", error)
        if (isFinishing || isDestroyed) return true
        processDialog.dismiss()
        reader?.control?.dismissProgressDialog()
        if (openErrorDialog?.isShowing == true || passwordDialog?.isShowing == true) return true
        if (showPasswordDialog(error)) return true
        openErrorDialog = OpenFileErrorDialog(this, getString(openErrorMessage(error))).apply {
            setOnDismissListener {
                openErrorDialog = null
                if (!isFinishing && !isDestroyed) finish()
            }
            show()
        }
        return true
    }

    private fun showPasswordDialog(error: OpenFileException): Boolean {
        val incorrect = error.reason == OpenFileException.Reason.PASSWORD_INCORRECT
        if (!incorrect && error.reason != OpenFileException.Reason.PASSWORD_REQUIRED) return false
        val path = document?.path ?: return false
        if (File(path).extension.lowercase() !in PASSWORD_EXTENSIONS) return false
        var submitted = false
        val message = getString(
            if (incorrect) R.string.error_file_password_incorrect else R.string.error_file_password
        )
        passwordDialog = DocumentPasswordDialog(this, message) { password ->
            submitted = true
            DocumentPasswords.set(path, password)
            passwordDialog?.dismiss()

            recreate()
        }.apply {
            setOnDismissListener {
                passwordDialog = null
                if (!submitted && !isFinishing && !isDestroyed) finish()
            }
            show()
        }
        return true
    }

    private fun openErrorMessage(error: OpenFileException): Int = when (error.reason) {
        OpenFileException.Reason.BAD_FILE -> R.string.error_file_damaged
        OpenFileException.Reason.RTF_DOCUMENT -> R.string.error_file_rtf
        OpenFileException.Reason.OLD_DOCUMENT -> R.string.error_file_old_format
        OpenFileException.Reason.PASSWORD_REQUIRED -> R.string.error_file_password
        OpenFileException.Reason.PASSWORD_INCORRECT -> R.string.error_file_password_incorrect
        OpenFileException.Reason.OUT_OF_MEMORY -> R.string.error_file_too_large_memory
        OpenFileException.Reason.FILE_NOT_FOUND -> R.string.error_file_not_found
        OpenFileException.Reason.STORAGE -> R.string.error_file_storage
        OpenFileException.Reason.UNKNOWN -> R.string.error_file_open_generic
    }

    private fun initReader(filePath: String): OfficeDocumentView {
        val file = File(filePath)
        OpenTrace.d("file clicked path=${file.absolutePath} exists=${file.exists()} length=${file.length()}")
        return binding.officeViewer.also {
            it.onOpenFailure = ::onOpenFailure
            it.open(file.absolutePath)
            reader = it
        }
    }

    override fun onDestroy() {
        editPanel?.close()
        editPanel = null
        if (loadingDialogLazy.isInitialized()) loadingDialog.dismiss()
        passwordDialog?.setOnDismissListener(null)
        passwordDialog?.dismiss()
        passwordDialog = null
        openErrorDialog?.setOnDismissListener(null)
        openErrorDialog?.dismiss()
        openErrorDialog = null
        if (isFinishing) {
            DocumentPasswords.clear(document?.path)
            File(cacheDir, "decrypted").deleteRecursively()
        }
        processDialog.dismiss()

        SlideShowActivity.source = null
        super.onDestroy()
    }

    companion object {
        const val ARG_DOCUMENT = "arg_document"
        private const val TAG = "ReadDocumentActivity"
        private const val STATE_DOCUMENT = "state_document"
        private const val DISABLED_ALPHA = 0.3f
        private const val MAX_SLIDE_WIDTH = 1920
        private val EDITABLE_EXTENSIONS = setOf("docx", "xlsx", "xlsm", "pptx")
        private val PASSWORD_EXTENSIONS = setOf(
            "docx", "dotx", "dotm", "xlsx", "xltx", "xltm", "xlsm", "xls", "xlt",
            "pptx", "pptm", "potx", "potm"
        )

        private fun DocumentType.titleRes(): Int = when (this) {
            DocumentType.Excel -> R.string.xls
            DocumentType.Ppt -> R.string.pptx
            DocumentType.Txt -> R.string.txt
            else -> R.string.doc
        }
    }
}
