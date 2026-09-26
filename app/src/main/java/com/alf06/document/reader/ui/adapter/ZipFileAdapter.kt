package com.alf06.document.reader.ui.adapter

import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.ViewGroup
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.ItemZipFileBinding
import com.alf06.document.reader.model.RecentDocument
import com.ui.baselib.base.DiffAdapter
import com.ui.baselib.extensions.click
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Files of the Create ZIP screen, each with a remove button. */
class ZipFileAdapter(
    private val onRemove: (RecentDocument) -> Unit,
) : DiffAdapter<RecentDocument, ItemZipFileBinding>(
    areItemsTheSame = { old, new -> old.path == new.path }
) {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemZipFileBinding = ItemZipFileBinding.inflate(inflater, parent, false)

    override fun ItemZipFileBinding.bind(item: RecentDocument, position: Int) {
        val context = root.context
        imgSource.setImageResource(item.type.iconRes())
        tvFileName.text = File(item.path).name
        tvFileInfo.text = context.getString(
            R.string.success_file_info,
            dateFormat.format(Date(item.lastModified)),
            Formatter.formatShortFileSize(context, item.size)
        )
        imgRemove.click { onRemove(item) }
    }
}
