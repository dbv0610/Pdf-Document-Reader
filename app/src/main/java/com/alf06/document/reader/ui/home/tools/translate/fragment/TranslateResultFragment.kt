package com.alf06.document.reader.ui.home.tools.translate.fragment

import android.util.Log
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.FragmentTranslateResultBinding
import com.alf06.document.reader.ui.dialog.DialogProcess
import com.alf06.document.reader.ui.dialog.RenameFileDialog
import com.alf06.document.reader.ui.home.tools.SuccessFragment
import com.alf06.document.reader.ui.home.tools.translate.SaveState
import com.alf06.document.reader.ui.home.tools.translate.TranslateNavigator
import com.alf06.document.reader.ui.home.tools.translate.TranslatePdfViewModel
import com.alf06.document.reader.utils.AppUtils
import com.ui.baselib.base.BaseFragment
import com.ui.baselib.extensions.click
import com.ui.baselib.lifecycle.collectFlow
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.io.File

class TranslateResultFragment :
    BaseFragment<FragmentTranslateResultBinding>(FragmentTranslateResultBinding::inflate) {
    private val loadingDialogLazy = lazy { DialogProcess(requireActivity()) }
    private val loadingDialog by loadingDialogLazy
    private val viewModel: TranslatePdfViewModel by activityViewModel()

    override fun FragmentTranslateResultBinding.initView() {
        val file = viewModel.translatedFile.value
        if (file == null || !file.isFile) {
            navHost.goBackFragment()
            return
        }
        pdfResult.fromFile(file)
            .pageSeparatorSpacing(8)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .onLoad { pageCount -> bindingOrNull?.txtNumberPage?.text = "1/$pageCount" }
            .onPageChange { page, pageCount -> bindingOrNull?.txtNumberPage?.text = "${page + 1}/$pageCount" }
            .onError { error ->
                Log.e(TAG, "Preview failed", error)
                toast(R.string.translate_failed)
            }
            .load()

        collectFlow(viewModel.saveState) { state ->
            loadingDialog.setShowing(state == SaveState.Running)
            when (state) {
                is SaveState.Done -> {
                    viewModel.consumeSaveResult()
                    navigateTo(
                        TranslateNavigator.Success.route,
                        SuccessFragment.ARG_PATH to state.path,
                        SuccessFragment.ARG_TITLE to R.string.downloaded_successfully,
                    )
                }
                SaveState.Failed -> {
                    viewModel.consumeSaveResult()
                    toast(R.string.translate_save_failed)
                }
                else -> Unit
            }
        }
    }

    override fun FragmentTranslateResultBinding.onClick() {
        icBack.click { navHost.goBackFragment() }
        icHome.click { activity?.finish() }
        btnDownload.click { askFileName() }
    }

    private fun askFileName() {
        val activity = activity ?: return
        if (viewModel.saveState.value == SaveState.Running) return
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
                viewModel.save(output)
                true
            }.show()
        }
    }
    private fun defaultFileName(dir: File): String {
        val base = viewModel.translatedFile.value?.nameWithoutExtension ?: "PDF"
        return generateSequence(1) { it + 1 }
            .map { if (it == 1) base else "${base}_$it" }
            .first { !File(dir, "$it.pdf").exists() }
    }

    override fun onDestroyView() {
        if (loadingDialogLazy.isInitialized()) loadingDialog.dismiss()
        bindingOrNull?.pdfResult?.recycle()
        super.onDestroyView()
    }

    private companion object {
        const val TAG = "TranslateResult"
    }
}
