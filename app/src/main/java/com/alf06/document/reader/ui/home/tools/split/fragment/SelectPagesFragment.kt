package com.alf06.document.reader.ui.home.tools.split.fragment

import androidx.lifecycle.lifecycleScope
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.FragmentSplitSelectPagesBinding
import com.alf06.document.reader.ui.adapter.SplitPageAdapter
import com.alf06.document.reader.ui.dialog.DialogProcess
import com.alf06.document.reader.ui.dialog.RenameFileDialog
import com.alf06.document.reader.ui.home.tools.SuccessFragment
import com.alf06.document.reader.ui.home.tools.split.SplitNavigator
import com.alf06.document.reader.ui.home.tools.split.SplitPdfViewModel
import com.alf06.document.reader.ui.home.tools.split.SplitState
import com.alf06.document.reader.utils.AppUtils
import com.ui.baselib.base.BaseFragment
import com.ui.baselib.extensions.click
import com.ui.baselib.lifecycle.collectFlow
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.io.File
import java.util.Locale

class SelectPagesFragment :
    BaseFragment<FragmentSplitSelectPagesBinding>(FragmentSplitSelectPagesBinding::inflate) {
    private val loadingDialogLazy = lazy { DialogProcess(requireActivity()) }
    private val loadingDialog by loadingDialogLazy
    private val viewModel: SplitPdfViewModel by activityViewModel()

    override fun FragmentSplitSelectPagesBinding.initView() {
        val thumbnailWidth = (resources.displayMetrics.widthPixels / 3).coerceIn(1, MAX_THUMBNAIL_WIDTH)
        val adapter = SplitPageAdapter(
            scope = viewLifecycleOwner.lifecycleScope,
            loadThumbnail = { index -> viewModel.thumbnail(index, thumbnailWidth) },
            onToggle = viewModel::togglePage,
        )
        rcvPages.adapter = adapter

        collectFlow(viewModel.pageCount) { count -> adapter.submitList(List(count) { it }) }
        collectFlow(viewModel.selectedPages) { selected ->
            adapter.setSelectedPages(selected)
            tvTitle.text = getString(R.string.split_select_pages, String.format(Locale.US, "%02d", selected.size))
            btnContinue.text = getString(R.string.continue_count, selected.size)
            renderContinueButton()
        }
        collectFlow(viewModel.splitState) { state ->
            loadingDialog.setShowing(state == SplitState.Running)
            renderContinueButton()
            when (state) {
                is SplitState.Done -> {
                    viewModel.consumeSplitResult()
                    navigateTo(
                        SplitNavigator.Success.route,
                        SuccessFragment.ARG_PATH to state.path,
                        SuccessFragment.ARG_TITLE to R.string.split_successfully,
                    )
                }
                SplitState.Failed -> {
                    viewModel.consumeSplitResult()
                    toast(R.string.split_failed)
                }
                else -> Unit
            }
        }
    }

    override fun FragmentSplitSelectPagesBinding.onClick() {
        icBack.click { navHost.goBackFragment() }
        btnContinue.click { askFileName() }
    }

    private fun renderContinueButton() {
        binding.btnContinue.isEnabled =
            viewModel.selectedPages.value.isNotEmpty() && viewModel.splitState.value != SplitState.Running
    }

    private fun askFileName() {
        val activity = activity ?: return
        if (viewModel.selectedPages.value.isEmpty()) return
        launchMain {
            val dir = File(AppUtils.ensureDocumentDirectory())
            RenameFileDialog(
                context = activity,
                currentName = defaultFileName(dir),
                failedRes = R.string.file_name_exists,
                skipIfUnchanged = false,
            ) { name ->
                val output = File(dir, "$name.pdf")

                if (output.exists()) return@RenameFileDialog false
                viewModel.split(output)
                true
            }.show()
        }
    }

    private fun defaultFileName(dir: File): String {
        val source = viewModel.source.value?.path?.let { File(it).nameWithoutExtension } ?: "PDF"
        val base = "${source}_split"
        return generateSequence(1) { it + 1 }
            .map { if (it == 1) base else "${base}_$it" }
            .first { !File(dir, "$it.pdf").exists() }
    }

    private companion object {
        const val MAX_THUMBNAIL_WIDTH = 400
    }

    override fun onDestroyView() {
        if (loadingDialogLazy.isInitialized()) loadingDialog.dismiss()
        super.onDestroyView()
    }
}
