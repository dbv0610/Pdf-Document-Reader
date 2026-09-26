package com.alf06.document.reader.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.alf06.document.reader.databinding.ItemGallerySelectedBinding
import com.alf06.document.reader.model.GalleryImage
import com.ui.baselib.base.DiffAdapter
import com.ui.baselib.extensions.click
import com.ui.baselib.extensions.load

class SelectedImageAdapter(
    private val onRemove: (GalleryImage) -> Unit,
) : DiffAdapter<GalleryImage, ItemGallerySelectedBinding>(
    areItemsTheSame = { old, new -> old.id == new.id }
) {
    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemGallerySelectedBinding = ItemGallerySelectedBinding.inflate(inflater, parent, false)

    override fun ItemGallerySelectedBinding.bind(item: GalleryImage, position: Int) {
        imgThumb.load(item.uri)
        imgRemove.click { onRemove(item) }
    }
}
