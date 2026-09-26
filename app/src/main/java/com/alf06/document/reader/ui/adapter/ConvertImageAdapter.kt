package com.alf06.document.reader.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.alf06.document.reader.databinding.ItemConvertImageBinding
import com.alf06.document.reader.model.GalleryImage
import com.ui.baselib.base.BaseAdapter

/** Page grid of the Convert screen; [onStartDrag] fires when the move handle is pressed. */
class ConvertImageAdapter(
    private val onStartDrag: (View) -> Unit
) : BaseAdapter<GalleryImage, ItemConvertImageBinding>() {

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemConvertImageBinding = ItemConvertImageBinding.inflate(inflater, parent, false)

    @SuppressLint("ClickableViewAccessibility")
    override fun ItemConvertImageBinding.bind(item: GalleryImage, position: Int) {
        val radius = root.resources.displayMetrics.density * 6
        Glide.with(imgThumb)
            .load(item.uri)
            .transform(CenterCrop(), RoundedCorners(radius.toInt()))
            .into(imgThumb)
        imgMove.setOnTouchListener { view, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) onStartDrag(view)
            false
        }
    }
}
