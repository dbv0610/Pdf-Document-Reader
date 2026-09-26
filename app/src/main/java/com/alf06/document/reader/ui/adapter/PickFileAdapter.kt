package com.alf06.document.reader.ui.adapter

import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.ViewGroup
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.ItemPickFileBinding
import com.alf06.document.reader.model.RecentUi
import com.ui.baselib.base.DiffAdapter
import com.ui.baselib.extensions.click
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Plain file row for tool flows that pick a single file. */
class PickFileAdapter(
    private val onItemClick: (RecentUi) -> Unit,
) : DiffAdapter<RecentUi, ItemPickFileBinding>(
    areItemsTheSame = { old, new -> old.document.mediaId == new.document.mediaId }
) {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemPickFileBinding = ItemPickFileBinding.inflate(inflater, parent, false)

    override fun ItemPickFileBinding.bind(item: RecentUi, position: Int) {
        val document = item.document
        val context = root.context
        imgSource.setImageResource(document.type.iconRes())
        tvFileName.text = File(document.path).name
        tvFileInfo.text = context.getString(
            R.string.success_file_info,
            dateFormat.format(Date(document.lastModified)),
            Formatter.formatShortFileSize(context, document.size)
        )
        root.click { onItemClick(item) }
    }
}
