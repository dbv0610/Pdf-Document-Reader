package com.alf06.document.reader.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.ItemPdfThumbBinding
import com.alf06.document.reader.model.DocumentPage
import com.ui.baselib.base.DiffAdapter
import com.ui.baselib.widget.layout.bgColor
import com.ui.baselib.widget.layout.stColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

class DocumentPreviewAdapter(
    private val scope: CoroutineScope? = null,
    private val loadThumbnail: (suspend (Int) -> Bitmap?)? = null,
    private val onPageClick: (DocumentPage) -> Unit,
) : DiffAdapter<DocumentPage, ItemPdfThumbBinding>(
    areItemsTheSame = { old, new -> old.index == new.index }
) {
    private var currentPage = 0
    private val requests = mutableMapOf<ItemPdfThumbBinding, ThumbnailRequest>()

    private class ThumbnailRequest(val index: Int, var job: Job? = null)

    fun setCurrentPage(page: Int) {
        if (page == currentPage) return
        val previous = currentPage
        currentPage = page
        // bind() highlights currentPage, so positions not in the list yet need no notify.
        if (previous in 0 until itemCount) notifyItemChanged(previous, PAYLOAD_CURRENT)
        if (page in 0 until itemCount) notifyItemChanged(page, PAYLOAD_CURRENT)
    }

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemPdfThumbBinding = ItemPdfThumbBinding.inflate(inflater, parent, false).also {
        // The item derives its other side from its 56:78 ratio, so the fixed side follows the scroll direction.
        val manager = (parent as? RecyclerView)?.layoutManager as? LinearLayoutManager
        if (manager?.orientation == RecyclerView.VERTICAL) {
            it.root.updateLayoutParams {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }
    }

    /** Loads every thumbnail again, e.g. after the document was saved; shown ones stay until replaced. */
    fun refreshThumbnails() {
        if (loadThumbnail != null) notifyItemRangeChanged(0, itemCount, PAYLOAD_REFRESH)
    }

    @SuppressLint("SetTextI18n")
    override fun ItemPdfThumbBinding.bind(item: DocumentPage, position: Int) {
        bindCurrent(position)
        pageIndex.text = "${position + 1}"
        requests.remove(this)?.job?.cancel()
        imagePreview.setImageBitmap(null)
        errorText.isVisible = false
        errorText.text = null
        val loader = loadThumbnail
        val coroutineScope = scope

        if (loader != null && coroutineScope != null) {
            progressBar.isVisible = true
            imagePreview.isVisible = false
            load(item, loader, coroutineScope)
        } else {
            root.minimumHeight = 0
            progressBar.isVisible = false
            imagePreview.isVisible = true
            imagePreview.setImageBitmap(item.bitmap)
        }
        root.setOnClickListener { onPageClick(item) }
    }

    override fun ItemPdfThumbBinding.bind(item: DocumentPage, position: Int, payloads: List<Any>) {
        val loader = loadThumbnail
        val coroutineScope = scope
        when {
            payloads.isNotEmpty() && payloads.all { it == PAYLOAD_CURRENT } -> bindCurrent(position)
            // Keep showing the old thumbnail until the new one is there, so a refresh does not flash.
            payloads.isNotEmpty() && PAYLOAD_REFRESH in payloads && loader != null && coroutineScope != null -> {
                bindCurrent(position)
                requests.remove(this)?.job?.cancel()
                load(item, loader, coroutineScope)
            }
            else -> bind(item, position)
        }
    }

    private fun ItemPdfThumbBinding.load(
        item: DocumentPage,
        loader: suspend (Int) -> Bitmap?,
        coroutineScope: CoroutineScope,
    ) {
        val request = ThumbnailRequest(item.index)
        requests[this] = request
        request.job = coroutineScope.launch {
            val bitmap = loader(item.index)
            ensureActive()
            if (requests[this@load] !== request || request.index != item.index) return@launch
            progressBar.isVisible = false
            imagePreview.setImageBitmap(bitmap)
            imagePreview.isVisible = bitmap != null
            if (bitmap != null) root.minimumHeight = 0
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        requests.remove(holder.binding)?.job?.cancel()
        holder.binding.imagePreview.setImageBitmap(null)
        super.onViewRecycled(holder)
    }

    private fun ItemPdfThumbBinding.bindCurrent(position: Int) {
        root.stColor(
            if (position == currentPage) ContextCompat.getColor(root.context, R.color.primary)
            else ContextCompat.getColor(root.context, R.color.color_7c8a97)
        )
        pageIndex.bgColor(
            if (position == currentPage) ContextCompat.getColor(root.context, R.color.primary)
            else ContextCompat.getColor(root.context, R.color.color_7c8a97)
        )
    }

    private companion object {
        const val PAYLOAD_CURRENT = "current"
        const val PAYLOAD_REFRESH = "refresh"
    }
}
