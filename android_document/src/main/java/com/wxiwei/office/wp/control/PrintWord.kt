package com.wxiwei.office.wp.control

import com.wxiwei.office.system.*

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.wxiwei.office.common.IOfficeToPicture
import com.wxiwei.office.common.hyperlink.Hyperlink
import com.wxiwei.office.common.picture.PictureKit
import com.wxiwei.office.constant.EventConstant
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.SysKit
import com.wxiwei.office.system.beans.pagelist.APageListItem
import com.wxiwei.office.system.beans.pagelist.APageListView
import com.wxiwei.office.system.beans.pagelist.IPageListViewListener
import com.wxiwei.office.wp.view.PageRoot
import com.wxiwei.office.wp.view.PageView
import kotlin.math.max
import kotlin.math.min

class PrintWord : FrameLayout, IPageListViewListener {
    private var preShowPageIndex = -1
    private var prePageCount = -1
    private var control: IControl? = null
    private lateinit var listView: APageListView
    private lateinit var paint: Paint
    private var pageRoot: PageRoot? = null
    private var pageSize: Rect? = Rect()

    constructor(context: Context) : super(context)

    constructor(context: Context, control: IControl, pageRoot: PageRoot) : super(context) {
        this.control = control
        this.pageRoot = pageRoot
        listView = APageListView(context, this)
        addView(listView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        paint = Paint()
        paint.isAntiAlias = true
        paint.typeface = Typeface.SANS_SERIF
        paint.textSize = 24f
    }

    override fun setBackgroundColor(color: Int) {
        super.setBackgroundColor(color)
        if (::listView.isInitialized) {
            listView.setBackgroundColor(color)
        }
    }

    override fun setBackgroundResource(resid: Int) {
        super.setBackgroundResource(resid)
        if (::listView.isInitialized) {
            listView.setBackgroundResource(resid)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun setBackgroundDrawable(d: Drawable?) {
        super.setBackgroundDrawable(d)
        if (::listView.isInitialized) {
            listView.setBackgroundDrawable(d)
        }
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (::listView.isInitialized) {
            exportImage(listView.currentPageView, null)
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        drawPageNubmer(canvas)
    }

    fun init() {
    }

    fun setZoom(zoom: Float, pointX: Int, pointY: Int) {
        listView.setZoom(zoom, pointX, pointY)
    }

    fun setFitSize(value: Int) {
        listView.setFitSize(value)
    }

    fun getFitSizeState(): Int = listView.fitSizeState

    fun getZoom(): Float = listView.zoom

    fun getFitZoom(): Float = listView.fitZoom

    fun getCurrentPageNumber(): Int = listView.currentPageNumber

    fun getListView(): APageListView = listView

    fun nextPageView() {
        listView.nextPageView()
    }

    fun previousPageview() {
        listView.previousPageview()
    }

    fun showPDFPageForIndex(index: Int) {
        listView.showPDFPageForIndex(index)
    }

    fun viewToModel(x: Int, y: Int, isBack: Boolean): Long {
        val pageIndex = listView.currentPageNumber - 1
        val pageRoot = pageRoot ?: return 0
        if (pageIndex < 0 || pageIndex >= getPageCount()) {
            return 0
        }
        return pageRoot.viewToModel(x, y, isBack)
    }

    fun modelToView(offset: Long, rect: Rectangle, isBack: Boolean): Rectangle {
        val pageIndex = listView.currentPageNumber - 1
        val pageRoot = pageRoot ?: return rect
        if (pageIndex < 0 || pageIndex >= getPageCount()) {
            return rect
        }
        return pageRoot.modelToView(offset, rect, isBack)
    }

    override fun getPageCount(): Int = max(pageRoot?.getChildCount() ?: 0, 1)

    override fun getPageListItem(position: Int, convertView: View?, parent: ViewGroup?): APageListItem {
        val rect = getPageSize(position)
        return WPPageListItem(listView, control!!, rect.width(), rect.height())
    }

    override fun getPageSize(pageIndex: Int): Rect {
        val pageRoot = pageRoot
        val pageSize = pageSize ?: Rect().also { this.pageSize = it }
        val view = pageRoot?.getPageView(pageIndex)
        if (view != null) {
            pageSize.set(0, 0, view.getWidth(), view.getHeight())
        } else if (pageRoot != null) {
            val attr = pageRoot.getDocument()!!.getSection(0)!!.getAttribute()
            val pageWidth = (AttrManage.instance().getPageWidth(attr) * MainConstant.TWIPS_TO_PIXEL).toInt()
            val pageHeight = (AttrManage.instance().getPageHeight(attr) * MainConstant.TWIPS_TO_PIXEL).toInt()
            pageSize.set(0, 0, pageWidth, pageHeight)
        }
        return pageSize
    }

    override fun exportImage(pageItem: APageListItem, srcBitmap: Bitmap?) {
        val control = control ?: return
        val pageRoot = pageRoot ?: return
        if (getControl() == null || parent !is Word) {
            return
        }

        val find = control.find as WPFind
        if (find.isSetPointToVisible()) {
            find.setSetPointToVisible(false)
            val pv = pageRoot.getPageView(pageItem.pageIndex) ?: return
            val rect = modelToView((parent as Word).getHighlight().getSelectStart(), Rectangle(), false)
            rect.x -= pv.getX()
            rect.y -= pv.getY()
            if (!listView.isPointVisibleOnScreen(rect.x, rect.y)) {
                listView.setItemPointVisibleOnScreen(rect.x, rect.y)
                return
            }
        }
        post {
            try {
                val otp: IOfficeToPicture? = getControl()?.officeToPicture
                if (otp != null && otp.modeType == IOfficeToPicture.VIEW_CHANGE_END) {
                    val rW = min(width, pageItem.width)
                    val rH = min(height, pageItem.height)
                    val dstBitmap = otp.getBitmap(rW, rH) ?: return@post
                    if (parent is Word) {
                        (parent as Word).getHighlight().setPaintHighlight(false)
                    }
                    drawPageToBitmap(dstBitmap, pageItem, rW, rH, pageRoot, control)
                    if (parent is Word) {
                        (parent as Word).getHighlight().setPaintHighlight(true)
                    }
                    otp.callBack(dstBitmap)
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun drawPageToBitmap(
        dstBitmap: Bitmap,
        pageItem: APageListItem,
        rW: Int,
        rH: Int,
        pageRoot: PageRoot,
        control: IControl
    ) {
        if (dstBitmap.width == rW && dstBitmap.height == rH) {
            val canvas = Canvas(dstBitmap)
            canvas.drawColor(Color.WHITE)
            val zoom = listView.zoom
            val pv = pageRoot.getPageView(pageItem.pageIndex)
            if (pv != null) {
                canvas.save()
                canvas.translate(-pv.getX() * zoom, -pv.getY() * zoom)
                val left = pageItem.left
                val top = pageItem.top
                pv.drawForPrintMode(canvas, -(max(left, 0) - left), -(max(top, 0) - top), zoom)
                canvas.restore()
                canvas.translate(-(max(left, 0) - left).toFloat(), -(max(top, 0) - top).toFloat())
                control.getSysKit().getCalloutManager().drawPath(canvas, pageItem.pageIndex, zoom)
            }
        } else {
            val pv = pageRoot.getPageView(pageItem.pageIndex)
            if (pv != null) {
                val paintZoom = min(dstBitmap.width / rW.toFloat(), dstBitmap.height / rH.toFloat())
                val zoom = listView.zoom * paintZoom
                val left = (pageItem.left * paintZoom).toInt()
                val top = (pageItem.top * paintZoom).toInt()
                val canvas = Canvas(dstBitmap)
                canvas.save()
                canvas.drawColor(Color.WHITE)
                canvas.translate(-pv.getX() * zoom, -pv.getY() * zoom)
                pv.drawForPrintMode(canvas, -(max(left, 0) - left), -(max(top, 0) - top), zoom)
                canvas.restore()
                canvas.translate(-(max(left, 0) - left).toFloat(), -(max(top, 0) - top).toFloat())
                control.getSysKit().getCalloutManager().drawPath(canvas, pageItem.pageIndex, zoom)
            }
        }
    }

    fun getSnapshot(dstBitmap: Bitmap): Bitmap? {
        val pageRoot = pageRoot ?: return null
        val pageItem = listView.currentPageView ?: return null
        val rW = min(width, pageItem.width)
        val rH = min(height, pageItem.height)

        if (parent is Word) {
            (parent as Word).getHighlight().setPaintHighlight(false)
        }
        if (dstBitmap.width == rW && dstBitmap.height == rH) {
            val canvas = Canvas(dstBitmap)
            canvas.drawColor(Color.WHITE)
            val zoom = listView.zoom
            val pv = pageRoot.getPageView(pageItem.pageIndex)
            if (pv != null) {
                canvas.translate(-pv.getX() * zoom, -pv.getY() * zoom)
                val left = pageItem.left
                val top = pageItem.top
                pv.drawForPrintMode(canvas, -(max(left, 0) - left), -(max(top, 0) - top), zoom)
            }
        } else {
            val pv = pageRoot.getPageView(pageItem.pageIndex)
            if (pv != null) {
                val paintZoom = min(dstBitmap.width / rW.toFloat(), dstBitmap.height / rH.toFloat())
                val zoom = listView.zoom * paintZoom
                val left = (pageItem.left * paintZoom).toInt()
                val top = (pageItem.top * paintZoom).toInt()
                val canvas = Canvas(dstBitmap)
                canvas.drawColor(Color.WHITE)
                canvas.translate(-pv.getX() * zoom, -pv.getY() * zoom)
                pv.drawForPrintMode(canvas, -(max(left, 0) - left), -(max(top, 0) - top), zoom)
            }
        }
        if (parent is Word) {
            (parent as Word).getHighlight().setPaintHighlight(true)
        }
        return dstBitmap
    }

    override fun isInit(): Boolean = true

    override fun isIgnoreOriginalSize(): Boolean = control!!.getMainFrame().isIgnoreOriginalSize()

    override fun getPageListViewMovingPosition(): Byte = control!!.getMainFrame().getPageListViewMovingPosition()

    override fun getModel(): Any? = pageRoot

    fun getControl(): IControl? = control

    override fun onEventMethod(
        v: View?,
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float,
        eventMethodType: Byte
    ): Boolean {
        val control = control ?: return false
        val pageRoot = pageRoot ?: return false
        if (eventMethodType == IPageListViewListener.ON_SINGLE_TAP_UP && e1 != null && e1.action == MotionEvent.ACTION_UP) {
            val item = listView.currentPageView
            if (item != null) {
                val pv = pageRoot.getPageView(item.pageIndex)
                if (pv != null) {
                    val zoom = listView.zoom
                    val x = ((e1.x - item.left) / zoom).toInt() + pv.getX()
                    val y = ((e1.y - item.top) / zoom).toInt() + pv.getY()
                    val offset = pv.viewToModel(x, y, false)
                    if (offset >= 0) {
                        val leaf = pv.getDocument()!!.getLeaf(offset)
                        if (leaf != null) {
                            val hyID = AttrManage.instance().getHperlinkID(leaf.getAttribute())
                            if (hyID >= 0) {
                                val hylink: Hyperlink? = control.getSysKit().getHyperlinkManage().getHyperlink(hyID)
                                if (hylink != null) {
                                    control.actionEvent(EventConstant.APP_HYPERLINK, hylink)
                                }
                            }
                        }
                    }
                }
            }
        }
        return control.getMainFrame().onEventMethod(v, e1, e2, velocityX, velocityY, eventMethodType)
    }

    override fun updateStutus(obj: Any?) {
        control?.actionEvent(EventConstant.SYS_UPDATE_TOOLSBAR_BUTTON_STATUS, obj)
    }

    override fun resetSearchResult(pageItem: APageListItem) {
        if (parent is Word) {
            val word = parent as Word
            if (word.getFind().getPageIndex() != pageItem.pageIndex) {
                word.getHighlight().removeHighlight()
            }
        }
    }

    override fun isTouchZoom(): Boolean = control!!.getMainFrame().isTouchZoom()

    override fun isShowZoomingMsg(): Boolean = control!!.getMainFrame().isShowZoomingMsg()

    override fun changeZoom() {
        control?.getMainFrame()?.changeZoom()
    }

    override fun isChangePage(): Boolean = control!!.getMainFrame().isChangePage()

    override fun setDrawPictrue(isDrawPictrue: Boolean) {
        PictureKit.instance().setDrawPictrue(isDrawPictrue)
    }

    fun getCurrentPageView(): PageView? {
        val item = listView.currentPageView
        return if (item != null) pageRoot?.getPageView(item.pageIndex) else null
    }

    private fun drawPageNubmer(canvas: Canvas) {
        val control = control ?: return
        val pageRoot = pageRoot ?: return
        if (control.getMainFrame().isDrawPageNumber()) {
            val pn = listView.currentPageNumber.toString() + " / " + pageRoot.getChildCount()
            val w = paint.measureText(pn).toInt()
            val h = (paint.descent() - paint.ascent()).toInt()
            val x = (width - w) / 2
            var y = height - h - 20

            val drawable = SysKit.getPageNubmerDrawable()
            drawable.setBounds(x - 10, y - 10, x + w + 10, y + h + 10)
            drawable.draw(canvas)

            y = (y - paint.ascent()).toInt()
            canvas.drawText(pn, x.toFloat(), y.toFloat(), paint)
        }

        val pageNumber = listView.currentPageNumber
        val pageCount = getPageCount()
        if (preShowPageIndex != pageNumber || prePageCount != pageCount) {
            control?.getMainFrame()?.changePage(pageNumber, pageCount)
            preShowPageIndex = pageNumber
            prePageCount = pageCount
        }
    }

    fun dispose() {
        control = null
        if (::listView.isInitialized) {
            listView.dispose()
        }
        pageRoot = null
        pageSize = null
    }
}
