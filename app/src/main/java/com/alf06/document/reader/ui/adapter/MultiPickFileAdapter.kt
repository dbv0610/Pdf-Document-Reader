package com.alf06.document.reader.ui.adapter

import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.ViewGroup
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.ItemMultiPickFileBinding
import com.alf06.document.reader.model.RecentUi
import com.ui.baselib.base.DiffAdapter
import com.ui.baselib.extensions.click
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** File row with a check for tool flows that pick several files; the selection is owned by the caller. */
class MultiPickFileAdapter(
    private val onToggle: (RecentUi) -> Unit,
) : DiffAdapter<RecentUi, ItemMultiPickFileBinding>(
    areItemsTheSame = { old, new -> old.document.mediaId == new.document.mediaId }
) {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private var selectedPaths: Set<String> = emptySet()

    fun setSelectedPaths(paths: Set<String>) {
        if (paths == selectedPaths) return
        val changed = (selectedPaths - paths) + (paths - selectedPaths)
        selectedPaths = paths
        currentList.forEachIndexed { position, item ->
            if (item.document.path in changed) notifyItemChanged(position, PAYLOAD_SELECTION)
        }
    }

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemMultiPickFileBinding = ItemMultiPickFileBinding.inflate(inflater, parent, false)

    override fun ItemMultiPickFileBinding.bind(item: RecentUi, position: Int) {
        val document = item.document
        val context = root.context
        imgSource.setImageResource(document.type.iconRes())
        tvFileName.text = File(document.path).name
        tvFileInfo.text = context.getString(
            R.string.success_file_info,
            dateFormat.format(Date(document.lastModified)),
            Formatter.formatShortFileSize(context, document.size)
        )
        bindSelection(item)
        root.click { onToggle(item) }
    }

    override fun ItemMultiPickFileBinding.bind(item: RecentUi, position: Int, payloads: List<Any>) {
        if (PAYLOAD_SELECTION in payloads) bindSelection(item) else bind(item, position)
    }

    private fun ItemMultiPickFileBinding.bindSelection(item: RecentUi) {
        imgCheck.setImageResource(
            if (item.document.path in selectedPaths) R.drawable.ic_app_circle_check
            else R.drawable.ic_app_circle_uncheck
        )
    }

    private companion object {
        const val PAYLOAD_SELECTION = "selection"
    }
}
