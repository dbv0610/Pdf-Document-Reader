package com.alf06.document.reader.ui.home.scanner.fragment

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.FragmentScanFilterBinding
import com.alf06.document.reader.databinding.ItemScanFilterBinding
import com.alf06.document.reader.ui.dialog.DialogProcess
import com.alf06.document.reader.ui.home.scanner.ScanPage
import com.alf06.document.reader.ui.home.scanner.ScanSessionViewModel
import com.alf06.document.reader.ui.home.scanner.detection.DocumentImageProcessor
import com.alf06.document.reader.ui.home.scanner.detection.ScanFilter
import com.ui.baselib.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

class ScanFilterFragment : BaseFragment<FragmentScanFilterBinding>(
    FragmentScanFilterBinding::inflate
) {
    private val viewModel: ScanSessionViewModel by activityViewModel()
    private val loadingDialogLazy = lazy { DialogProcess(requireActivity()) }
    private val loadingDialog by loadingDialogLazy
    private val processor = DocumentImageProcessor()
    private val adapter = FilterAdapter { filter -> select(filter) }

    /** Unfiltered crop of the selected page, shrunk to screen size; filters for the preview run on it. */
    private var preview: Bitmap? = null
    private var selected = ScanFilter.Default
    private var previewJob: Job? = null
    private var applying = false

    override fun FragmentScanFilterBinding.initView() {
        root.updatePadding(bottom = root.paddingBottom + navigationBarHeight)
        rvFilters.adapter = adapter
        val page = currentPage()
        if (page == null) {
            navHost.goBackFragment()
            return
        }
        selected = page.filter
        adapter.select(selected)
        // Show the current result right away; the filterable preview is decoded in the background.
        Glide.with(this@ScanFilterFragment).load(page.output).into(ivPreview)
        loadPreview(page)
    }

    override fun FragmentScanFilterBinding.onClick() {
        btnBack.setOnClickListener { navHost.goBackFragment() }
        btnApply.setOnClickListener { viewModel.selectedIndex.value?.let { apply(listOf(it)) } }
        btnApplyAll.setOnClickListener { apply(viewModel.pageList.indices.toList()) }
    }

    override fun onDestroyView() {
        if (loadingDialogLazy.isInitialized()) loadingDialog.dismiss()
        previewJob = null
        preview = null
        super.onDestroyView()
    }

    private fun currentPage(): ScanPage? =
        viewModel.selectedIndex.value?.let { viewModel.pageList.getOrNull(it) }

    private fun loadPreview(page: ScanPage) {
        viewLifecycleOwner.lifecycleScope.launch {
            val loaded = withContext(Dispatchers.Default) {
                val bitmap = processor.loadPreview(page.cropped, PREVIEW_MAX_SIDE) ?: return@withContext null
                val scale = THUMB_MAX_SIDE.toFloat() / max(bitmap.width, bitmap.height)
                val thumbSource = Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt().coerceAtLeast(1),
                    (bitmap.height * scale).toInt().coerceAtLeast(1),
                    true,
                )
                bitmap to ScanFilter.entries.associateWith { processor.filterBitmap(thumbSource, it) }
            } ?: return@launch
            preview = loaded.first
            adapter.setThumbnails(loaded.second)
            // The user may have picked a filter while the preview was loading.
            if (selected != page.filter) renderPreview()
        }
    }

    private fun select(filter: ScanFilter) {
        if (applying || filter == selected) return
        selected = filter
        adapter.select(filter)
        renderPreview()
    }

    private fun renderPreview() {
        val source = preview ?: return
        val filter = selected
        previewJob?.cancel()
        previewJob = viewLifecycleOwner.lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.Default) { processor.filterBitmap(source, filter) }
            binding.ivPreview.setImageBitmap(bitmap)
        }
    }

    /** Applies [selected] to the full-size pages at [indices], then returns to the pages screen. */
    private fun apply(indices: List<Int>) {
        if (applying) return
        val filter = selected
        val pages = indices.mapNotNull { index -> viewModel.pageList.getOrNull(index)?.let { index to it } }
            .filter { (_, page) -> page.filter != filter }
        if (pages.isEmpty()) {
            navHost.goBackFragment()
            return
        }
        applying = true
        loadingDialog.setShowing(true)
        viewLifecycleOwner.lifecycleScope.launch {
            val results = withContext(Dispatchers.Default) {
                pages.map { (index, page) ->
                    index to processor.applyFilter(page.cropped, filter)?.let { page.copy(output = it, filter = filter) }
                }
            }
            results.forEach { (index, page) -> page?.let { viewModel.updatePage(index, it) } }
            loadingDialog.setShowing(false)
            applying = false
            if (results.any { it.second == null }) {
                Toast.makeText(appContext(), R.string.scanner_capture_failed, Toast.LENGTH_SHORT).show()
            }
            navHost.goBackFragment()
        }
    }

    private class FilterAdapter(
        private val onClick: (ScanFilter) -> Unit,
    ) : RecyclerView.Adapter<FilterAdapter.Holder>() {
        private val filters = ScanFilter.entries
        private var thumbnails: Map<ScanFilter, Bitmap> = emptyMap()
        private var selected = ScanFilter.Default

        @SuppressLint("NotifyDataSetChanged")
        fun setThumbnails(thumbnails: Map<ScanFilter, Bitmap>) {
            this.thumbnails = thumbnails
            notifyDataSetChanged()
        }

        fun select(filter: ScanFilter) {
            val previous = selected
            selected = filter
            notifyItemChanged(filters.indexOf(previous))
            notifyItemChanged(filters.indexOf(filter))
        }

        override fun getItemCount() = filters.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            Holder(ItemScanFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)).apply {
                binding.ivThumb.clipToOutline = true
            }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val filter = filters[position]
            val item = holder.binding
            item.root.isSelected = filter == selected
            item.tvName.setText(filter.label)
            item.ivThumb.setImageBitmap(thumbnails[filter])
            item.root.setOnClickListener { onClick(filter) }
        }

        class Holder(val binding: ItemScanFilterBinding) : RecyclerView.ViewHolder(binding.root)
    }

    private companion object {
        const val PREVIEW_MAX_SIDE = 1600
        const val THUMB_MAX_SIDE = 200
    }
}
