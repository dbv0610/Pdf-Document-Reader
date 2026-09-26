package com.alf06.document.reader.ui.home.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.ItemTabFilterBinding
import com.alf06.document.reader.model.FileTypeFilter

class TabFilterAdapter(
    private val onSelected: (FileTypeFilter) -> Unit,
) : RecyclerView.Adapter<TabFilterAdapter.TabViewHolder>() {
    private val items = FileTypeFilter.entries

    var selected: FileTypeFilter = FileTypeFilter.All
        private set

    fun setSelected(filter: FileTypeFilter) {
        if (filter == selected) return
        val old = selected
        selected = filter
        notifyItemChanged(items.indexOf(old))
        notifyItemChanged(items.indexOf(filter))
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        return TabViewHolder(
            ItemTabFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        holder.bindView(items[position])
    }

    inner class TabViewHolder(private val binding: ItemTabFilterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindView(item: FileTypeFilter) {
            val isSelected = item == selected
            val context = binding.root.context
            binding.tvContent.setText(item.title)
            binding.tvContent.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (isSelected) R.color.primary else R.color.text_primary
                )
            )
            binding.viewSelected.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
            itemView.setOnClickListener {
                if (item == selected) return@setOnClickListener
                setSelected(item)
                onSelected(item)
            }
        }
    }
}
