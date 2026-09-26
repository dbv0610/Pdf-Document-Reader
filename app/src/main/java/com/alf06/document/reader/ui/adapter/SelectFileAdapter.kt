package com.alf06.document.reader.ui.adapter

import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.ViewGroup
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.ItemSelectFileBinding
import com.alf06.document.reader.model.RecentUi
import com.alf06.document.reader.utils.formatDateByMillis
import com.ui.baselib.base.DiffAdapter
import java.io.File

class SelectFileAdapter(
    private val onSelectionChanged: (selected: List<RecentUi>) -> Unit,
    private val onMoreClick: (RecentUi) -> Unit,
) : DiffAdapter<RecentUi, ItemSelectFileBinding>(
    areItemsTheSame = { old, new -> old.document.mediaId == new.document.mediaId }
) {
    private val selectedIds = linkedSetOf<Long>()

    val selectedItems: List<RecentUi>
        get() = currentList.filter { it.document.mediaId in selectedIds }

    override fun onCurrentListChanged(
        previousList: MutableList<RecentUi>,
        currentList: MutableList<RecentUi>
    ) {
        // Drop selections for files that disappeared (deleted, renamed away, rescanned).
        val ids = currentList.mapTo(HashSet()) { it.document.mediaId }
        if (selectedIds.retainAll(ids)) onSelectionChanged(selectedItems)
    }

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemSelectFileBinding = ItemSelectFileBinding.inflate(inflater, parent, false)

    override fun ItemSelectFileBinding.bind(item: RecentUi, position: Int) {
        val document = item.document
        val context = root.context
        imgSource.setImageResource(document.type.iconRes())
        tvFileName.text = File(document.path).name
        tvFileInfo.text = context.getString(
            R.string.document_info,
            formatDateByMillis(document.lastModified),
            Formatter.formatShortFileSize(context, document.size)
        )
        bindSelection(item)
        root.setOnClickListener { toggle(item) }
        imgTools.setOnClickListener { onMoreClick(item) }
    }

    override fun ItemSelectFileBinding.bind(item: RecentUi, position: Int, payloads: List<Any>) {
        if (PAYLOAD_SELECTION in payloads) bindSelection(item) else bind(item, position)
    }

    private fun ItemSelectFileBinding.bindSelection(item: RecentUi) {
        imgCheck.setImageResource(
            if (item.document.mediaId in selectedIds) R.drawable.ic_app_circle_check
            else R.drawable.ic_app_circle_uncheck
        )
    }

    private fun toggle(item: RecentUi) {
        val id = item.document.mediaId
        if (!selectedIds.remove(id)) selectedIds.add(id)
        val position = currentList.indexOfFirst { it.document.mediaId == id }
        if (position != -1) notifyItemChanged(position, PAYLOAD_SELECTION)
        onSelectionChanged(selectedItems)
    }

    private companion object {
        const val PAYLOAD_SELECTION = "selection"
    }
}