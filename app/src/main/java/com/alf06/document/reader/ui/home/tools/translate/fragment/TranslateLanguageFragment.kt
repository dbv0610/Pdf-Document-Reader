package com.alf06.document.reader.ui.home.tools.translate.fragment

import android.text.format.Formatter
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.FragmentTranslateLanguageBinding
import com.alf06.document.reader.ui.home.tools.translate.TranslateError
import com.alf06.document.reader.ui.home.tools.translate.TranslateNavigator
import com.alf06.document.reader.ui.home.tools.translate.TranslatePdfViewModel
import com.alf06.document.reader.ui.home.tools.translate.TranslateState
import com.ui.baselib.base.BaseFragment
import com.ui.baselib.extensions.click
import com.ui.baselib.lifecycle.collectFlow
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TranslateLanguageFragment :
    BaseFragment<FragmentTranslateLanguageBinding>(FragmentTranslateLanguageBinding::inflate) {
    private val viewModel: TranslatePdfViewModel by activityViewModel()

    private var languageSheet: TranslateLanguageBottomSheet? = null

    private val loadingDialogLazy = lazy {
        TranslateProgressDialog(requireActivity()) {
            navHost.navigatePopUpTo(
                TranslateNavigator.Language.route,
                TranslateNavigator.SelectPages.route
            )
        }
    }
    private val loadingDialog by loadingDialogLazy

    override fun FragmentTranslateLanguageBinding.initView() {
        val pages = viewModel.selectedPages.value.size
        tvPageCount.text = resources.getQuantityString(R.plurals.translate_page_count, pages, pages)
        viewModel.source.value?.let { document ->
            tvFileName.text = File(document.path).name
            tvFileInfo.text = getString(
                R.string.success_file_info,
                SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    Locale.getDefault()
                ).format(Date(document.lastModified)),
                Formatter.formatShortFileSize(appContext(), document.size)
            )
        }

        collectFlow(viewModel.targetLanguage) { tag ->
            tvLanguage.text = tag?.let(viewModel::languageName)
            renderStartButton()
        }
        collectFlow(viewModel.translateState) { state ->
            renderLoading(state)
            renderStartButton()
            when (state) {
                is TranslateState.Running -> Unit
                TranslateState.Done -> {
                    viewModel.consumeTranslateResult()
                    navigateTo(TranslateNavigator.Result.route)
                }

                is TranslateState.Failed -> {
                    viewModel.consumeTranslateResult()
                    toast(errorText(state.error))
                }

                TranslateState.Idle -> Unit
            }
        }
    }

    override fun FragmentTranslateLanguageBinding.onClick() {
        icBack.click { onBackPressed() }
        layoutLanguage.click { showLanguages() }
        btnStart.click { viewModel.translate() }
    }

    private fun renderStartButton() {
        binding.btnStart.isEnabled =
            viewModel.targetLanguage.value != null && viewModel.translateState.value !is TranslateState.Running
    }

    private fun renderLoading(state: TranslateState) {
        if (state is TranslateState.Running) {
            if (!loadingDialog.isShowing) loadingDialog.show()
            loadingDialog.render(state.step)
        } else if (loadingDialogLazy.isInitialized()) {
            loadingDialog.dismiss()
        }
    }

    private fun showLanguages() {
        val activity = activity ?: return
        if (languageSheet?.isShowing == true) return
        languageSheet = TranslateLanguageBottomSheet(
            activity,
            viewModel.languages,
            viewModel.targetLanguage.value,
            viewModel::setTargetLanguage
        ).apply {
            onDismissListener = { languageSheet = null }
            show()
        }
    }

    private fun errorText(error: TranslateError): Int = when (error) {
        TranslateError.NoText -> R.string.translate_no_text
        TranslateError.UnknownLanguage -> R.string.translate_unknown_language
        TranslateError.ModelDownload -> R.string.translate_download_failed
        TranslateError.Failed -> R.string.translate_failed
    }

    override fun onBackPressed(): Boolean {
        viewModel.cancelTranslate()
        navHost.goBackFragment()
        return true
    }

    override fun onDestroyView() {
        languageSheet?.dismissSafe()
        languageSheet = null
        if (loadingDialogLazy.isInitialized()) loadingDialog.dismiss()
        super.onDestroyView()
    }
}
