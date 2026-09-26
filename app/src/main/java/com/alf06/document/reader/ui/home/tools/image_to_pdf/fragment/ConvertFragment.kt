package com.alf06.document.reader.ui.home.tools.image_to_pdf.fragment

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.FragmentConvertBinding
import com.alf06.document.reader.ui.adapter.ConvertImageAdapter
import com.alf06.document.reader.ui.dialog.DialogProcess
import com.alf06.document.reader.ui.dialog.RenameFileDialog
import com.alf06.document.reader.ui.home.tools.SuccessFragment
import com.alf06.document.reader.ui.home.tools.image_to_pdf.ConvertState
import com.alf06.document.reader.ui.home.tools.image_to_pdf.ImagePdfNavigator
import com.alf06.document.reader.ui.home.tools.image_to_pdf.SelectImageViewModel
import com.alf06.document.reader.utils.AppUtils
import com.ui.baselib.base.BaseFragment
import com.ui.baselib.extensions.click
import com.ui.baselib.lifecycle.collectFlow
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConvertFragment :
    BaseFragment<FragmentConvertBinding>(FragmentConvertBinding::inflate) {
    private val loadingDialogLazy = lazy { DialogProcess(requireActivity()) }
    private val loadingDialog by loadingDialogLazy
    private val viewModel: SelectImageViewModel by activityViewModel()

    private val adapter: ConvertImageAdapter = ConvertImageAdapter(onStartDrag = { handle ->
        binding.rcvImages.findContainingViewHolder(handle)?.let(touchHelper::startDrag)
    })

    private val touchHelper: ItemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, 0
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

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            viewModel.reorder(adapter.currentItems)
        }
    })

    override fun FragmentConvertBinding.initView() {
        rcvImages.adapter = adapter
        touchHelper.attachToRecyclerView(rcvImages)

        collectFlow(viewModel.selected) { selected ->

            if (selected != adapter.currentItems) adapter.submitList(selected)
            btnConvert.text = getString(R.string.convert_count, selected.size)
            renderConvertButton()
        }
        collectFlow(viewModel.convertState) { state ->
            loadingDialog.setShowing(state == ConvertState.Running)
            renderConvertButton()
            when (state) {
                is ConvertState.Done -> {
                    viewModel.consumeConvertResult()
                    navigateTo(ImagePdfNavigator.Success.route, SuccessFragment.ARG_PATH to state.path)
                }
                ConvertState.Failed -> {
                    viewModel.consumeConvertResult()
                    toast(R.string.convert_failed)
                }
                else -> Unit
            }
        }
    }

    override fun FragmentConvertBinding.onClick() {
        icBack.click { navHost.goBackFragment() }
        btnAdd.click { navHost.goBackFragment() }
        btnConvert.click { askFileName() }
    }

    private fun renderConvertButton() {
        binding.btnConvert.isEnabled =
            viewModel.selected.value.isNotEmpty() && viewModel.convertState.value != ConvertState.Running
    }

    private fun askFileName() {
        val activity = activity ?: return
        launchMain {
            val dir = File(AppUtils.ensureDocumentDirectory())
            RenameFileDialog(
                context = activity,
                currentName = defaultFileName(),
                failedRes = R.string.file_name_exists,
                skipIfUnchanged = false,
            ) { name ->
                val output = File(dir, "$name.pdf")

                if (output.exists()) return@RenameFileDialog false
                viewModel.convert(output)
                true
            }.show()
        }
    }

    private fun defaultFileName() =
        "PDF_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

    override fun onDestroyView() {
        if (loadingDialogLazy.isInitialized()) loadingDialog.dismiss()
        super.onDestroyView()
    }
}
