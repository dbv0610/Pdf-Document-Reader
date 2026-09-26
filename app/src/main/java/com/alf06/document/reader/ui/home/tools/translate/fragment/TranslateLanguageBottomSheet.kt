package com.alf06.document.reader.ui.home.tools.translate.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.BottomSheetTranslateLanguageBinding
import com.alf06.document.reader.databinding.ItemTranslateLanguageBinding
import com.ui.baselib.base.BaseBottomSheet

class TranslateLanguageBottomSheet(
    activity: FragmentActivity,
    private val languages: List<Pair<String, String>>,
    private val selected: String?,
    private val onSelect: (String) -> Unit,
) : BaseBottomSheet<BottomSheetTranslateLanguageBinding>(
    activity,
    BottomSheetTranslateLanguageBinding::inflate
) {
    override fun BottomSheetTranslateLanguageBinding.onBind() {
        rvLanguages.adapter = LanguageAdapter()

        val current = languages.indexOfFirst { it.first == selected }
        if (current >= 0) {
            (rvLanguages.layoutManager as? LinearLayoutManager)
                ?.scrollToPositionWithOffset(current, 0)
        }
    }

    private inner class LanguageAdapter : RecyclerView.Adapter<LanguageAdapter.Holder>() {
        inner class Holder(val binding: ItemTranslateLanguageBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
            ItemTranslateLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

        override fun getItemCount() = languages.size

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val (tag, name) = languages[position]
            holder.binding.apply {
                tvName.text = name
                imgRadio.setImageResource(
                    if (tag == selected) R.drawable.ic_app_circle_check
                    else R.drawable.ic_app_circle_uncheck
                )
                root.setOnClickListener {
                    onSelect(tag)
                    dismissSafe()
                }
            }
        }
    }
}
