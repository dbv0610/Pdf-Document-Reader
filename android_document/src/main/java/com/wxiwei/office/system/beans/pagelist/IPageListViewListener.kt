package com.wxiwei.office.system.beans.pagelist

import android.graphics.Bitmap
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup

interface IPageListViewListener {
    companion object {
        const val ON_TOUCH: Byte = 0
        const val ON_DOWN: Byte = 1
        const val ON_SHOW_PRESS: Byte = 2
        const val ON_SINGLE_TAP_UP: Byte = 3
        const val ON_SCROLL: Byte = 4
        const val ON_LONG_PRESS: Byte = 5
        const val ON_FLING: Byte = 6
        const val ON_SINGLE_TAP_CONFIRMED: Byte = 7
        const val ON_DOUBLE_TAP: Byte = 8
        const val ON_DOUBLE_TAP_EVENT: Byte = 9
        const val ON_CLICK: Byte = 10
        const val Moving_Horizontal: Byte = 0
        const val Moving_Vertical: Byte = 1
    }

    fun getModel(): Any?
    fun getPageListItem(position: Int, convertView: View?, parent: ViewGroup?): APageListItem
    fun exportImage(view: APageListItem, srcBitmap: Bitmap?)
    fun getPageCount(): Int
    fun getPageSize(pageIndex: Int): Rect?
    fun onEventMethod(v: View?, e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float, eventMethodType: Byte): Boolean
    fun updateStutus(obj: Any?)
    fun resetSearchResult(pageItem: APageListItem)
    fun isTouchZoom(): Boolean
    fun isShowZoomingMsg(): Boolean
    fun changeZoom()
    fun isChangePage(): Boolean
    fun setDrawPictrue(isDrawPictrue: Boolean)
    fun isInit(): Boolean
    fun isIgnoreOriginalSize(): Boolean
    fun getPageListViewMovingPosition(): Byte
}
