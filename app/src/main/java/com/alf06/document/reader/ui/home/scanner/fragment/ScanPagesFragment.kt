package com.alf06.document.reader.ui.home.scanner.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.view.TransformExperimental
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.FragmentScanPagesBinding
import com.alf06.document.reader.databinding.ItemScanPageBinding
import com.alf06.document.reader.ui.home.scanner.ScanPage
import com.alf06.document.reader.ui.home.scanner.ScanSessionViewModel
import com.alf06.document.reader.ui.home.scanner.ScannerNavigator
import com.alf06.document.reader.ui.home.scanner.detection.DocumentImageProcessor
import com.ui.baselib.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanPagesFragment : BaseFragment<FragmentScanPagesBinding>(
    FragmentScanPagesBinding::inflate
) {
    private val viewModel: ScanSessionViewModel by activityViewModel()
    private val processor = DocumentImageProcessor()
    private val adapter = PageAdapter { index -> viewModel.select(index) }
    private var rotating = false

    override fun FragmentScanPagesBinding.initView() {
        root.updatePadding(bottom = root.paddingBottom + navigationBarHeight)
        rvPages.adapter = adapter

        pageArea.doOnLayout {
            ivPage.maxWidth = it.width
            ivPage.maxHeight = it.height
        }
        viewModel.pages.observe(viewLifecycleOwner) { pages ->
            if (pages.isEmpty()) {
                navHost.goBackFragment()
                return@observe
            }
            render()
        }
        viewModel.selectedIndex.observe(viewLifecycleOwner) { render() }
    }

    override fun FragmentScanPagesBinding.onClick() {
        btnBack.setOnClickListener { backToCamera() }
        btnAddPage.setOnClickListener { backToCamera() }
        btnRetakePage.setOnClickListener {
            viewModel.retakeIndex = viewModel.selectedIndex.value
            navHost.goBackFragment()
        }
        btnDeletePage.setOnClickListener { viewModel.selectedIndex.value?.let(viewModel::delete) }
        btnRotatePage.setOnClickListener { rotateSelected() }

        btnFilter.setOnClickListener { navigateTo(ScannerNavigator.Filter.route) }
        btnEraser.setOnClickListener { }
        btnCancel.setOnClickListener {
            viewModel.clear()
            activity?.finish()
        }
        btnContinue.setOnClickListener { finishWithPages() }
    }

    override fun onBackPressed(): Boolean {
        backToCamera()
        return true
    }

    private fun backToCamera() {
        viewModel.retakeIndex = null
        navHost.goBackFragment()
    }

    private fun render() {
        val pages = viewModel.pageList
        val selected = viewModel.selectedIndex.value ?: 0
        adapter.submit(pages, selected)
        val page = pages.getOrNull(selected) ?: return
        Glide.with(this).load(page.output).into(binding.ivPage)
        binding.rvPages.scrollToPosition(selected)
    }

    private fun rotateSelected() {
        if (rotating) return
        val index = viewModel.selectedIndex.value ?: return
        val page = viewModel.pageList.getOrNull(index) ?: return
        rotating = true
        viewLifecycleOwner.lifecycleScope.launch {
            // Rotate the unfiltered crop too, so a filter picked later keeps the rotation.
            val rotated = withContext(Dispatchers.Default) {
                val cropped = processor.rotateClockwise(page.cropped) ?: return@withContext null
                if (page.output == page.cropped) return@withContext page.copy(cropped = cropped, output = cropped)
                val output = processor.rotateClockwise(page.output)
                if (output == null) {
                    cropped.delete()
                    return@withContext null
                }
                page.copy(cropped = cropped, output = output)
            }
            rotating = false
            if (rotated == null) {
                Toast.makeText(appContext(), R.string.scanner_capture_failed, Toast.LENGTH_SHORT).show()
            } else {
                viewModel.updatePage(index, rotated)
            }
        }
    }

    @OptIn(TransformExperimental::class)
    private fun finishWithPages() {
        val pages = viewModel.pageList
        if (pages.isEmpty()) return
        activity?.setResult(RESULT_OK, Intent().apply {
            putExtra(DocumentScannerFragment.EXTRA_ORIGINAL_PATH, pages.first().original.absolutePath)
            putExtra(DocumentScannerFragment.EXTRA_OUTPUT_PATH, pages.first().output.absolutePath)
            putStringArrayListExtra(
                DocumentScannerFragment.EXTRA_OUTPUT_PATHS,
                ArrayList(pages.map { it.output.absolutePath })
            )
        })
        activity?.finish()
    }

    private class PageAdapter(
        private val onClick: (Int) -> Unit,
    ) : RecyclerView.Adapter<PageAdapter.Holder>() {
        private var pages: List<ScanPage> = emptyList()
        private var selected = 0

        fun submit(pages: List<ScanPage>, selected: Int) {
            this.pages = pages
            this.selected = selected
            notifyDataSetChanged()
        }

        override fun getItemCount() = pages.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            Holder(ItemScanPageBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val item = holder.binding
            val isSelected = position == selected
            item.root.isSelected = isSelected
            item.tvNumber.isSelected = isSelected
            item.tvNumber.text = (position + 1).toString()
            Glide.with(item.ivPage).load(pages[position].output).centerCrop().into(item.ivPage)
            item.root.setOnClickListener { onClick(holder.bindingAdapterPosition) }
        }

        class Holder(val binding: ItemScanPageBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
