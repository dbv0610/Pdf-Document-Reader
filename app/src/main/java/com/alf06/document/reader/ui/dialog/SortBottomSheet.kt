package com.alf06.document.reader.ui.dialog

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.BottomSheetSortBinding
import com.alf06.document.reader.databinding.ItemSortOptionBinding
import com.alf06.document.reader.model.SortByData
import com.alf06.document.reader.model.SortOrder
import com.ui.baselib.base.BaseBottomSheet
import com.ui.baselib.extensions.click

class SortBottomSheet(
    activity: FragmentActivity,
    sortBy: SortByData,
    order: SortOrder,
    private val onSave: (SortByData, SortOrder) -> Unit,
) : BaseBottomSheet<BottomSheetSortBinding>(activity, BottomSheetSortBinding::inflate) {

    // "None" means the list still uses scan order; newest-first by last edit matches it best.
    private var selectedSort = if (sortBy == SortByData.None) SortByData.SortByDate else sortBy
    private var selectedOrder = order

    override fun BottomSheetSortBinding.onBind() {
        val sortOptions = mapOf(
            SortByData.SortByDate to optLastEdit,
            SortByData.SortByName to optName,
            SortByData.SortBySize to optFileSize,
        )
        val orderOptions = mapOf(
            SortOrder.Increase to optIncrease,
            SortOrder.Decrease to optDecrease,
        )

        fun render() {
            sortOptions.forEach { (sort, row) -> row.setChecked(sort == selectedSort) }
            orderOptions.forEach { (order, row) -> row.setChecked(order == selectedOrder) }
        }

        optLastEdit.setup(R.drawable.ic_app_sort_date, R.string.sort_last_edit)
        optName.setup(R.drawable.ic_app_name, R.string.sort_name)
        optFileSize.setup(R.drawable.ic_app_sort_size, R.string.sort_file_size)
        optIncrease.setup(R.drawable.ic_app_sort_increase, R.string.sort_increase)
        optDecrease.setup(R.drawable.ic_app_sort_decrease, R.string.sort_decrease)

        sortOptions.forEach { (sort, row) ->
            row.root.setOnClickListener {
                selectedSort = sort
                render()
            }
        }
        orderOptions.forEach { (order, row) ->
            row.root.setOnClickListener {
                selectedOrder = order
                render()
            }
        }
        render()

        btnSave.click {
            onSave(selectedSort, selectedOrder)
            dismissSafe()
        }
    }

    private fun ItemSortOptionBinding.setup(@DrawableRes icon: Int, @StringRes label: Int) {
        imgIcon.setImageResource(icon)
        tvLabel.setText(label)
    }

    private fun ItemSortOptionBinding.setChecked(checked: Boolean) {
        imgRadio.setImageResource(
            if (checked) R.drawable.ic_app_radio_on else R.drawable.ic_app_circle_uncheck
        )
    }
}
