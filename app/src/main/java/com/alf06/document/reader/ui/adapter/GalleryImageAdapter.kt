package com.alf06.document.reader.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.alf06.document.reader.databinding.ItemGalleryImageBinding
import com.alf06.document.reader.model.GalleryImage
import com.ui.baselib.base.DiffAdapter
import com.ui.baselib.extensions.click

class GalleryImageAdapter(
    private val onToggle: (GalleryImage) -> Unit,
    private val onOpenItem: (GalleryImage) -> Unit
) : DiffAdapter<GalleryImage, ItemGalleryImageBinding>(
    areItemsTheSame = { old, new -> old.id == new.id }
) {
    private var selectedIds: Set<Long> = emptySet()

    fun setSelectedIds(ids: Set<Long>) {
        if (ids == selectedIds) return
        val changed = (selectedIds - ids) + (ids - selectedIds)
        selectedIds = ids
        currentList.forEachIndexed { index, image ->
            if (image.id in changed) notifyItemChanged(index, PAYLOAD_SELECTION)
        }
    }

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemGalleryImageBinding = ItemGalleryImageBinding.inflate(inflater, parent, false)

    override fun ItemGalleryImageBinding.bind(item: GalleryImage, position: Int) {
        val radius = root.resources.displayMetrics.density * 6
        Glide.with(imgThumb)
            .load(item.uri)
            .transform(CenterCrop(), RoundedCorners(radius.toInt()))
            .into(imgThumb)
        bindSelection(item)
        imgOpenFile.click {
            onOpenItem(item)
        }
        root.click { onToggle(item) }
    }

    override fun ItemGalleryImageBinding.bind(item: GalleryImage, position: Int, payloads: List<Any>) {
        if (PAYLOAD_SELECTION in payloads) bindSelection(item) else bind(item, position)
    }

    private fun ItemGalleryImageBinding.bindSelection(item: GalleryImage) {
        viewSelected.isVisible = item.id in selectedIds
    }

    private companion object {
        const val PAYLOAD_SELECTION = "selection"
    }
}
