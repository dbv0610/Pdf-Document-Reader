package com.wxiwei.office.wp.control

import android.graphics.Bitmap
import android.graphics.Canvas
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.beans.pagelist.APageListItem
import com.wxiwei.office.system.beans.pagelist.APageListView
import com.wxiwei.office.wp.view.PageRoot

class WPPageListItem(listView: APageListView, control: IControl, pageWidth: Int, pageHeight: Int) :
    APageListItem(listView, pageWidth, pageHeight) {

    private var pageRoot: PageRoot? = null

    init {
        this.control = control
        this.pageRoot = listView.model as PageRoot
        setBackgroundColor(BACKGROUND_COLOR)
    }

    public override fun onDraw(canvas: Canvas) {
        val pv = pageRoot?.getPageView(pageIndex)
        if (pv != null) {
            val zoom = listView.zoom
            canvas.save()
            canvas.translate(-pv.getX() * zoom, -pv.getY() * zoom)
            pv.drawForPrintMode(canvas, 0, 0, zoom)
            canvas.restore()
        }
    }

    override fun setPageItemRawData(pIndex: Int, pageWidth: Int, pageHeight: Int) {
        super.setPageItemRawData(pIndex, pageWidth, pageHeight)
        if ((listView.zoom * 100).toInt() == 100 || isInit && pIndex == 0) {
            listView.exportImage(this, null)
        }
        isInit = false
    }

    public override fun addRepaintImageView(bmp: Bitmap?) {
        postInvalidate()
        listView.exportImage(this, null)
    }

    override fun removeRepaintImageView() {
    }

    override fun dispose() {
        super.dispose()
        pageRoot = null
    }

    companion object {
        private const val BACKGROUND_COLOR = -0x1
    }
}
