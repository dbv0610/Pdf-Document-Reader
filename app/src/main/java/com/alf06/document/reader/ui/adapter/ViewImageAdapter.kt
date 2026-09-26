package com.alf06.document.reader.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.alf06.document.reader.databinding.ItemViewImageBinding
import com.alf06.document.reader.model.GalleryImage
import com.ui.baselib.base.DiffAdapter

class ViewImageAdapter : DiffAdapter<GalleryImage, ItemViewImageBinding>(
    areItemsTheSame = { old, new -> old.id == new.id }
) {
    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemViewImageBinding = ItemViewImageBinding.inflate(inflater, parent, false)

    override fun ItemViewImageBinding.bind(item: GalleryImage, position: Int) {
        Glide.with(imgPhoto).load(item.uri).into(imgPhoto)
    }
}
