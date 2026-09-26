package com.wxiwei.office.system.beans.pagelist

import android.graphics.Bitmap
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.beans.CalloutView.CalloutView
import com.wxiwei.office.system.beans.CalloutView.IExportListener

open class APageListItem(
    @JvmField var listView: APageListView,
    pageWidth: Int,
    pageHeight: Int
) : ViewGroup(listView!!.context), IExportListener {
    @JvmField var mIsBlank = false
    @JvmField var pageIndex = 0
    @JvmField var pageWidth = pageWidth
    @JvmField var pageHeight = pageHeight
    @JvmField var isInit = true
    lateinit var control: IControl
    @JvmField var callouts: CalloutView? = null

    init {
        setBackgroundColor(Color.WHITE)
    }

    fun initCalloutView() {
        if (callouts == null) {
            callouts = CalloutView(listView!!.context, control, this)
            callouts!!.setIndex(pageIndex)
            addView(callouts, 0)
        }
    }

    override fun exportImage() {
        listView?.postRepaint(listView?.getCurrentPageView())
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) pageWidth else MeasureSpec.getSize(widthMeasureSpec)
        val height = if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) pageHeight else MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        callouts?.let {
            val width = right - left
            val height = bottom - top
            it.setZoom(listView!!.getZoom())
            it.layout(0, 0, width, height)
            it.bringToFront()
        }
    }

    open fun setPageItemRawData(pIndex: Int, pageWidth: Int, pageHeight: Int) {
        mIsBlank = false
        pageIndex = pIndex
        this.pageWidth = pageWidth
        this.pageHeight = pageHeight
        if (callouts == null) {
            if (!control.getSysKit().getCalloutManager().isPathEmpty(pIndex)) initCalloutView()
        } else {
            callouts!!.setIndex(pIndex)
        }
    }

    open fun releaseResources() {
        mIsBlank = true
        pageIndex = 0
        if (pageWidth == 0 || pageHeight == 0) {
            pageWidth = listView!!.width
            pageHeight = listView!!.height
        }
    }

    open fun blank(pageIndex: Int) {
        mIsBlank = true
        this.pageIndex = pageIndex
        if (pageWidth == 0 || pageHeight == 0) {
            pageWidth = listView!!.width
            pageHeight = listView!!.height
        }
    }

    open fun setLinkHighlighting(value: Boolean) {
    }

    open fun addRepaintImageView(bitmap: Bitmap?) {
    }

    open fun removeRepaintImageView() {
    }

    fun getPageIndex(): Int = pageIndex

    override fun isOpaque(): Boolean = true

    fun getPageWidth(): Int = pageWidth

    fun getPageHeight(): Int = pageHeight

    open fun dispose() {
    }
}
