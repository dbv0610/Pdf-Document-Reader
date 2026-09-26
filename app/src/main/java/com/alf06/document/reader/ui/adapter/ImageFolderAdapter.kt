package com.alf06.document.reader.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.ItemImageFolderBinding
import com.alf06.document.reader.model.ImageFolder
import com.ui.baselib.base.DiffAdapter
import com.ui.baselib.extensions.click

class ImageFolderAdapter(
    private val onClick: (ImageFolder) -> Unit,
) : DiffAdapter<ImageFolder, ItemImageFolderBinding>(
    areItemsTheSame = { old, new -> old.id == new.id }
) {
    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemImageFolderBinding = ItemImageFolderBinding.inflate(inflater, parent, false)

    override fun ItemImageFolderBinding.bind(item: ImageFolder, position: Int) {
        val radius = root.resources.displayMetrics.density * 4
        Glide.with(imgCover)
            .load(item.cover)
            .transform(CenterCrop(), RoundedCorners(radius.toInt()))
            .into(imgCover)
        tvName.text = when {
            item.id == null -> root.context.getString(R.string.all_images)
            item.name.isEmpty() -> root.context.getString(R.string.internal_storage)
            else -> item.name
        }
        tvCount.text = item.count.toString()
        root.click { onClick(item) }
    }
}
