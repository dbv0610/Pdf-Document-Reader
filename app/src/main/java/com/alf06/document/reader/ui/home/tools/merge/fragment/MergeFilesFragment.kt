package com.alf06.document.reader.ui.home.tools.merge.fragment

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.FragmentMergeFilesBinding
import com.alf06.document.reader.ui.adapter.MergeFileAdapter
import com.alf06.document.reader.ui.dialog.DialogProcess
import com.alf06.document.reader.ui.dialog.DocumentPasswordDialog
import com.alf06.document.reader.ui.dialog.RenameFileDialog
import com.alf06.document.reader.ui.home.tools.SuccessFragment
import com.alf06.document.reader.ui.home.tools.merge.MergeNavigator
import com.alf06.document.reader.ui.home.tools.merge.MergePdfViewModel
import com.alf06.document.reader.ui.home.tools.merge.MergeState
import com.alf06.document.reader.utils.AppUtils
import com.ui.baselib.base.BaseFragment
import com.ui.baselib.extensions.click
import com.ui.baselib.lifecycle.collectFlow
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.io.File

class MergeFilesFragment :
    BaseFragment<FragmentMergeFilesBinding>(FragmentMergeFilesBinding::inflate) {
    private val loadingDialogLazy = lazy { DialogProcess(requireActivity()) }
    private val loadingDialog by loadingDialogLazy
    private val viewModel: MergePdfViewModel by activityViewModel()

    private val adapter: MergeFileAdapter = MergeFileAdapter(
        onRemove = { viewModel.remove(it) },
        onStartDrag = { handle -> binding.rcvFiles.findContainingViewHolder(handle)?.let(touchHelper::startDrag) },
    )

    private val touchHelper: ItemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            adapter.onMove(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

        override fun isLongPressDragEnabled(): Boolean = false

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            viewModel.reorder(adapter.currentItems)
        }
    })

    private var passwordDialog: DocumentPasswordDialog? = null

    override fun FragmentMergeFilesBinding.initView() {
        rcvFiles.adapter = adapter
        touchHelper.attachToRecyclerView(rcvFiles)

        collectFlow(viewModel.files) { files ->

            if (files.isEmpty()) {
                navHost.goBackFragment()
                return@collectFlow
            }

            if (files != adapter.currentItems) adapter.submitList(files)
            btnMerge.text = getString(R.string.merge_count, files.size)
            renderMergeButton()
        }
        collectFlow(viewModel.mergeState) { state ->
            loadingDialog.setShowing(state == MergeState.Running)
            renderMergeButton()
            when (state) {
                is MergeState.Done -> {
                    viewModel.consumeMergeResult()
                    navigateTo(
                        MergeNavigator.Success.route,
                        SuccessFragment.ARG_PATH to state.path,
                        SuccessFragment.ARG_TITLE to R.string.merge_successfully,
                    )
                }
                is MergeState.PasswordRequired -> {
                    viewModel.consumeMergeResult()
                    askPassword(state)
                }
                MergeState.Failed -> {
                    viewModel.consumeMergeResult()
                    toast(R.string.merge_failed)
                }
                else -> Unit
            }
        }
    }

    override fun FragmentMergeFilesBinding.onClick() {
        icBack.click { navHost.goBackFragment() }
        btnAdd.click { navHost.goBackFragment() }
        btnMerge.click {
            if (viewModel.files.value.size < 2) toast(R.string.merge_need_two_files) else askFileName()
        }
    }

    private fun renderMergeButton() {
        binding.btnMerge.isEnabled = viewModel.mergeState.value != MergeState.Running
    }

    private fun askFileName() {
        val activity = activity ?: return
        launchMain {
            val dir = File(AppUtils.ensureDocumentDirectory())
            RenameFileDialog(
                context = activity,
                currentName = defaultFileName(dir),
                titleRes = R.string.file_name,
                failedRes = R.string.file_name_exists,
                skipIfUnchanged = false,
            ) { name ->
                val output = File(dir, "$name.pdf")

                if (output.exists()) return@RenameFileDialog false
                viewModel.merge(output)
                true
            }.show()
        }
    }

    private fun defaultFileName(dir: File): String {
        val first = viewModel.files.value.firstOrNull()?.path?.let { File(it).nameWithoutExtension } ?: "PDF"
        val base = "${first}_merged"
        return generateSequence(1) { it + 1 }
            .map { if (it == 1) base else "${base}_$it" }
            .first { !File(dir, "$it.pdf").exists() }
    }

    private fun askPassword(state: MergeState.PasswordRequired) {
        val activity = activity ?: return
        if (passwordDialog?.isShowing == true) return
        val name = File(state.path).name
        val message = getString(
            if (state.incorrect) R.string.error_file_password_incorrect else R.string.error_file_password
        )
        passwordDialog = DocumentPasswordDialog(activity, "$name\n$message") { password ->
            passwordDialog?.dismiss()
            viewModel.setPassword(state.path, password)
            viewModel.merge(state.output)
        }.apply {
            setOnDismissListener { passwordDialog = null }
            show()
        }
    }

    override fun onDestroyView() {
        if (loadingDialogLazy.isInitialized()) loadingDialog.dismiss()
        passwordDialog?.dismiss()
        passwordDialog = null
        super.onDestroyView()
    }
}
