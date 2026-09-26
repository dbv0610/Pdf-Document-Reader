package com.alf06.document.reader.ui.adapter

import android.annotation.SuppressLint
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.ItemMergeFileBinding
import com.alf06.document.reader.model.RecentDocument
import com.ui.baselib.base.BaseAdapter
import com.ui.baselib.extensions.click
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Files of the Merge screen in merge order; [onStartDrag] fires when the move handle is pressed. */
class MergeFileAdapter(
    private val onRemove: (RecentDocument) -> Unit,
    private val onStartDrag: (View) -> Unit,
) : BaseAdapter<RecentDocument, ItemMergeFileBinding>() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemMergeFileBinding = ItemMergeFileBinding.inflate(inflater, parent, false)

    @SuppressLint("ClickableViewAccessibility")
    override fun ItemMergeFileBinding.bind(item: RecentDocument, position: Int) {
        val context = root.context
        imgSource.setImageResource(item.type.iconRes())
        tvFileName.text = File(item.path).name
        tvFileInfo.text = context.getString(
            R.string.success_file_info,
            dateFormat.format(Date(item.lastModified)),
            Formatter.formatShortFileSize(context, item.size)
        )
        imgDelete.click { onRemove(item) }
        imgMove.setOnTouchListener { view, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) onStartDrag(view)
            false
        }
    }
}
