package com.alf06.document.reader.ui.home.tools.zip.fragment

import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.FragmentZipFilesBinding
import com.alf06.document.reader.ui.adapter.ZipFileAdapter
import com.alf06.document.reader.ui.dialog.DialogProcess
import com.alf06.document.reader.ui.dialog.RenameFileDialog
import com.alf06.document.reader.ui.home.tools.SuccessFragment
import com.alf06.document.reader.ui.home.tools.zip.CreateZipViewModel
import com.alf06.document.reader.ui.home.tools.zip.ZipNavigator
import com.alf06.document.reader.ui.home.tools.zip.ZipState
import com.alf06.document.reader.utils.AppUtils
import com.ui.baselib.base.BaseFragment
import com.ui.baselib.extensions.click
import com.ui.baselib.lifecycle.collectFlow
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.io.File

class ZipFilesFragment :
    BaseFragment<FragmentZipFilesBinding>(FragmentZipFilesBinding::inflate) {
    private val loadingDialogLazy = lazy { DialogProcess(requireActivity()) }
    private val loadingDialog by loadingDialogLazy
    private val viewModel: CreateZipViewModel by activityViewModel()

    private val adapter = ZipFileAdapter(onRemove = { viewModel.remove(it) })

    override fun FragmentZipFilesBinding.initView() {
        rcvFiles.adapter = adapter

        collectFlow(viewModel.files) { files ->

            if (files.isEmpty()) {
                navHost.goBackFragment()
                return@collectFlow
            }
            adapter.submitList(files)
            renderCreateButton()
        }
        collectFlow(viewModel.zipState) { state ->
            loadingDialog.setShowing(state == ZipState.Running)
            renderCreateButton()
            when (state) {
                is ZipState.Done -> {
                    viewModel.consumeZipResult()
                    navigateTo(
                        ZipNavigator.Success.route,
                        SuccessFragment.ARG_PATH to state.path,
                        SuccessFragment.ARG_TITLE to R.string.zip_successfully,
                    )
                }
                ZipState.Failed -> {
                    viewModel.consumeZipResult()
                    toast(R.string.zip_failed)
                }
                else -> Unit
            }
        }
    }

    override fun FragmentZipFilesBinding.onClick() {
        icBack.click { navHost.goBackFragment() }
        btnAdd.click { navHost.goBackFragment() }
        btnCreateZip.click { askFileName() }
    }

    private fun renderCreateButton() {
        binding.btnCreateZip.isEnabled =
            viewModel.files.value.isNotEmpty() && viewModel.zipState.value != ZipState.Running
    }

    private fun askFileName() {
        val activity = activity ?: return
        if (viewModel.files.value.isEmpty()) return
        launchMain {
            val dir = File(AppUtils.ensureDocumentDirectory())
            RenameFileDialog(
                context = activity,
                currentName = defaultFileName(dir),
                failedRes = R.string.file_name_exists,
                skipIfUnchanged = false,
            ) { name ->
                val output = File(dir, "$name.zip")

                if (output.exists()) return@RenameFileDialog false
                viewModel.createZip(output)
                true
            }.show()
        }
    }

    private fun defaultFileName(dir: File): String {
        val base = viewModel.files.value.firstOrNull()?.path?.let { File(it).nameWithoutExtension } ?: "Archive"
        return generateSequence(1) { it + 1 }
            .map { if (it == 1) base else "${base}_$it" }
            .first { !File(dir, "$it.zip").exists() }
    }

    override fun onDestroyView() {
        if (loadingDialogLazy.isInitialized()) loadingDialog.dismiss()
        super.onDestroyView()
    }
}
