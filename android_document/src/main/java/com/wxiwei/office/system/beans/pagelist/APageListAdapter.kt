package com.wxiwei.office.system.beans.pagelist

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

class APageListAdapter(private var listView: APageListView?) : BaseAdapter() {
    override fun getCount(): Int = listView!!.getPageCount()

    override fun getItem(position: Int): Any? = null

    override fun getItemId(position: Int): Long = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var pageItem = convertView as APageListItem?
        val pageSize: Rect = listView!!.getPageListViewListener().getPageSize(position)!!
        if (convertView == null) {
            pageItem = listView!!.getPageListItem(position, convertView, parent)
            if (pageItem == null) {
                pageSize.right = 794
                pageSize.bottom = 1124
                pageItem = APageListItem(listView!!, pageSize.width(), pageSize.height())
            }
        }
        pageItem!!.setPageItemRawData(position, pageSize.width(), pageSize.height())
        return pageItem
    }

    fun dispose() {
        listView = null
    }
}
