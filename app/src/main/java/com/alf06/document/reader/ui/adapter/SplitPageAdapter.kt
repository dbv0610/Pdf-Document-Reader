package com.alf06.document.reader.ui.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.ItemSplitPageBinding
import com.ui.baselib.base.DiffAdapter
import com.ui.baselib.extensions.click
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Page grid of the split flow; items are zero based page indices. */
class SplitPageAdapter(
    private val scope: CoroutineScope,
    private val loadThumbnail: suspend (Int) -> Bitmap?,
    private val onToggle: (Int) -> Unit,
) : DiffAdapter<Int, ItemSplitPageBinding>(
    areItemsTheSame = { old, new -> old == new }
) {
    private var selectedPages: Set<Int> = emptySet()
    private val requests = mutableMapOf<ItemSplitPageBinding, Job>()

    fun setSelectedPages(pages: Set<Int>) {
        if (pages == selectedPages) return
        val changed = (selectedPages - pages) + (pages - selectedPages)
        selectedPages = pages
        // Items are the indices 0 until pageCount, so an index is also its position.
        changed.forEach { if (it in 0 until itemCount) notifyItemChanged(it, PAYLOAD_SELECTION) }
    }

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemSplitPageBinding = ItemSplitPageBinding.inflate(inflater, parent, false)

    override fun ItemSplitPageBinding.bind(item: Int, position: Int) {
        tvPage.text = (item + 1).toString()
        bindSelection(item)
        requests.remove(this)?.cancel()
        imgPage.setImageBitmap(null)
        progressBar.isVisible = true
        requests[this] = scope.launch {
            val bitmap = loadThumbnail(item)
            progressBar.isVisible = false
            imgPage.setImageBitmap(bitmap)
        }
        root.click { onToggle(item) }
    }

    override fun ItemSplitPageBinding.bind(item: Int, position: Int, payloads: List<Any>) {
        if (PAYLOAD_SELECTION in payloads) bindSelection(item) else bind(item, position)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        requests.remove(holder.binding)?.cancel()
        holder.binding.imgPage.setImageBitmap(null)
        super.onViewRecycled(holder)
    }

    private fun ItemSplitPageBinding.bindSelection(item: Int) {
        val selected = item in selectedPages
        viewSelected.isVisible = selected
        imgCheck.setImageResource(if (selected) R.drawable.ic_app_radio_on else R.drawable.ic_app_circle_uncheck)
    }

    private companion object {
        const val PAYLOAD_SELECTION = "selection"
    }
}
