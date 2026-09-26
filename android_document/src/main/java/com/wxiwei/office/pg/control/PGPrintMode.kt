package com.wxiwei.office.pg.control

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
import com.wxiwei.office.common.shape.AbstractShape
import com.wxiwei.office.common.shape.IShape
import com.wxiwei.office.common.shape.TextBox
import com.wxiwei.office.constant.EventConstant
import com.wxiwei.office.java.awt.Dimension
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.pg.model.PGModel
import com.wxiwei.office.pg.model.PGSlide
import com.wxiwei.office.pg.view.SlideDrawKit
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.IElement
import com.wxiwei.office.simpletext.model.ParagraphElement
import com.wxiwei.office.simpletext.view.STRoot
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.IMainFrame
import com.wxiwei.office.system.OpenTrace
import com.wxiwei.office.system.SysKit
import com.wxiwei.office.system.beans.pagelist.APageListItem
import com.wxiwei.office.system.beans.pagelist.APageListView
import com.wxiwei.office.system.beans.pagelist.IPageListViewListener

class PGPrintMode : FrameLayout, IPageListViewListener {
    private var preShowPageIndex = -1
    private var control: IControl? = null
    private var listView: APageListView? = null
    private var paint = Paint()
    private var pgModel: PGModel? = null
    private var editor: PGEditor? = null
    private var pageSize = Rect()

    constructor(context: Context) : super(context)

    constructor(context: Context, control: IControl, pgModel: PGModel, editor: PGEditor) : super(context) {
        this.control = control
        this.pgModel = pgModel
        this.editor = editor
        listView = APageListView(context, this)
        addView(listView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        paint.setAntiAlias(true)
        paint.setTypeface(Typeface.SANS_SERIF)
        paint.setTextSize(24f)
    }

    fun setVisible(visible: Boolean) { listView?.setVisibility(if (visible) View.VISIBLE else View.GONE) }

    override fun setBackgroundColor(color: Int) {
        super.setBackgroundColor(color)
        listView?.setBackgroundColor(color)
    }

    override fun setBackgroundResource(resid: Int) {
        super.setBackgroundResource(resid)
        listView?.setBackgroundResource(resid)
    }

    @Suppress("DEPRECATION")
    override fun setBackgroundDrawable(d: Drawable?) {
        super.setBackgroundDrawable(d)
        listView?.setBackgroundDrawable(d)
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == VISIBLE) listView?.getCurrentPageView()?.let { exportImage(it, null) }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        drawPageNubmer(canvas)
    }

    fun init() {
        val start = android.os.SystemClock.uptimeMillis()
        OpenTrace.mark("pgPrintMode.init.begin list=${listView != null} zoom=${getZoom()} pageCount=${getPageCount()}")
        if ((getZoom() * 100).toInt() == 100) {
            OpenTrace.mark("pgPrintMode.init.beforeFitZoom")
            val fitZoom = getFitZoom()
            OpenTrace.mark("pgPrintMode.init.fitZoom=$fitZoom")
            setZoom(fitZoom, Int.MIN_VALUE, Int.MIN_VALUE)
        }
        OpenTrace.mark("pgPrintMode.init.end", start)
    }

    fun setZoom(zoom: Float, pointX: Int, pointY: Int) {
        OpenTrace.mark("pgPrintMode.setZoom.begin zoom=$zoom point=$pointX,$pointY")
        listView?.setZoom(zoom, pointX, pointY)
        OpenTrace.mark("pgPrintMode.setZoom.end")
    }
    fun setFitSize(value: Int) { listView?.setFitSize(value) }
    fun getFitSizeState(): Int = listView?.getFitSizeState() ?: 0
    fun getZoom(): Float = listView?.getZoom() ?: 1f
    fun getFitZoom(): Float = listView?.getFitZoom() ?: 1f
    fun getCurrentPageNumber(): Int = listView?.getCurrentPageNumber() ?: 0
    fun getListView(): APageListView? = listView
    fun nextPageView() { listView?.nextPageView() }
    fun previousPageview() { listView?.previousPageview() }
    fun showSlideForIndex(index: Int) { listView?.showPDFPageForIndex(index) }

    fun viewToModel(x: Int, y: Int, isBack: Boolean): Long {
        val pageIndex = getCurrentPageNumber() - 1
        if (pageIndex < 0 || pageIndex >= getPageCount()) return 0
        return editor?.viewToModel(x, y, isBack) ?: 0
    }

    fun modelToView(offset: Long, rect: Rectangle, isBack: Boolean): Rectangle {
        val pageIndex = getCurrentPageNumber() - 1
        if (pageIndex < 0 || pageIndex >= getPageCount()) return rect
        return editor?.modelToView(offset, rect, isBack) ?: rect
    }

    override fun getPageCount(): Int = maxOf(pgModel?.getSlideCount() ?: 0, 1)

    override fun getPageListItem(position: Int, convertView: View?, parent: ViewGroup?): APageListItem {
        val rect = getPageSize(position)
        return PGPageListItem(listView!!, control!!, editor, rect.width(), rect.height())
    }

    override fun getPageSize(pageIndex: Int): Rect {
        val d: Dimension? = pgModel?.getPageSize()
        if (d == null) pageSize.set(0, 0, width, height) else pageSize.set(0, 0, d.width, d.height)
        return pageSize
    }

    override fun exportImage(pageItem: APageListItem, srcBitmap: Bitmap?) {
        val page = pageItem ?: return
        val ctl = control ?: return
        if (parent !is Presentation) return
        val find = ctl.getFind() as? PGFind
        if (find?.isSetPointToVisible() == true) {
            find.setSetPointToVisible(false)
            val rect = editor?.modelToView(editor?.getHighlight()?.getSelectStart() ?: 0, Rectangle(), false) ?: Rectangle()
            if (listView?.isPointVisibleOnScreen(rect.x, rect.y) == false) {
                listView?.setItemPointVisibleOnScreen(rect.x, rect.y)
                return
            }
        }
        // TODO(coroutine): render the page snapshot on the UI queue; use Dispatchers.Main.
        post {
            try {
                val slide = pgModel?.getSlide(page.getPageIndex()) ?: return@post
                val otp = ctl.getOfficeToPicture()
                if (otp != null && otp.getModeType() == IOfficeToPicture.VIEW_CHANGE_END) {
                    val rW = minOf(width, page.getWidth())
                    val rH = minOf(height, page.getHeight())
                    val dst = otp.getBitmap(rW, rH) ?: return@post
                    val paintZoom: Float
                    val left: Int
                    val top: Int
                    val zoom: Float
                    if (dst.width == rW && dst.height == rH) {
                        paintZoom = 1f
                        left = page.getLeft()
                        top = page.getTop()
                        zoom = getZoom()
                    } else {
                        paintZoom = minOf(dst.width.toFloat() / rW, dst.height.toFloat() / rH)
                        left = (page.getLeft() * paintZoom).toInt()
                        top = (page.getTop() * paintZoom).toInt()
                        zoom = getZoom() * paintZoom
                    }
                    val canvas = Canvas(dst)
                    canvas.drawColor(Color.WHITE)
                    canvas.translate(-(maxOf(left, 0) - left).toFloat(), -(maxOf(top, 0) - top).toFloat())
                    SlideDrawKit.instance().drawSlide(canvas, pgModel!!, editor!!, slide, zoom)
                    ctl.getSysKit().getCalloutManager().drawPath(canvas, page.getPageIndex(), zoom)
                    otp.callBack(dst)
                }
            } catch (_: Exception) { }
        }
    }

    fun getSnapshot(dstBitmap: Bitmap): Bitmap? {
        val ctl = control ?: return null
        if (parent !is Presentation) return null
        val page = listView?.getCurrentPageView() as? PGPageListItem ?: return null
        val slide = pgModel?.getSlide(page.getPageIndex()) ?: return dstBitmap
        val rW = minOf(width, page.getWidth())
        val rH = minOf(height, page.getHeight())
        val paintZoom = if (dstBitmap.width == rW && dstBitmap.height == rH) 1f else minOf(dstBitmap.width.toFloat() / rW, dstBitmap.height.toFloat() / rH)
        val left = if (paintZoom == 1f) page.getLeft() else (page.getLeft() * paintZoom).toInt()
        val top = if (paintZoom == 1f) page.getTop() else (page.getTop() * paintZoom).toInt()
        val canvas = Canvas(dstBitmap)
        canvas.drawColor(Color.WHITE)
        canvas.translate(-(maxOf(left, 0) - left).toFloat(), -(maxOf(top, 0) - top).toFloat())
        SlideDrawKit.instance().drawSlide(canvas, pgModel!!, editor!!, slide, getZoom() * paintZoom)
        return dstBitmap
    }

    override fun isInit(): Boolean = true
    override fun isIgnoreOriginalSize(): Boolean = control?.getMainFrame()?.isIgnoreOriginalSize() ?: false
    override fun getPageListViewMovingPosition(): Byte = control?.getMainFrame()?.getPageListViewMovingPosition() ?: 0
    override fun getModel(): Any? = pgModel
    fun getControl(): IControl? = control

    override fun onEventMethod(v: View?, e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float, eventMethodType: Byte): Boolean {
        if (eventMethodType == IMainFrame.ON_SINGLE_TAP_UP && e1 != null && e1.action == MotionEvent.ACTION_UP) {
            val item = listView?.getCurrentPageView()
            if (item != null) {
                val zoom = getZoom()
                val x = ((e1.x - item.getLeft()) / zoom).toInt()
                val y = ((e1.y - item.getTop()) / zoom).toInt()
                val shape = pgModel?.getSlide(item.getPageIndex())?.getTextboxShape(x, y)
                if (shape != null && shape.getType() == AbstractShape.SHAPE_TEXTBOX) {
                    val root: STRoot? = (shape as TextBox).getRootView()
                    if (root != null) {
                        val offset = root.viewToModel(x - shape.getBounds().x, y - shape.getBounds().y, false)
                        if (offset >= 0) {
                            val para = (shape as TextBox).getElement()?.getElement(offset) as? ParagraphElement
                            val leaf: IElement? = para?.getLeaf(offset)
                            if (leaf != null) {
                                val id = AttrManage.instance().getHperlinkID(leaf.getAttribute())
                                if (id >= 0) {
                                    val link: Hyperlink? = control?.getSysKit()?.getHyperlinkManage()?.getHyperlink(id)
                                    if (link != null) { control?.actionEvent(EventConstant.APP_HYPERLINK, link); return true }
                                }
                            }
                        }
                    }
                }
            }
        }
        return control?.getMainFrame()?.onEventMethod(v, e1, e2, velocityX, velocityY, eventMethodType) ?: false
    }

    override fun updateStutus(obj: Any?) { control?.actionEvent(EventConstant.SYS_UPDATE_TOOLSBAR_BUTTON_STATUS, obj) }
    override fun resetSearchResult(pageItem: APageListItem) {
        val pg = parent as? Presentation ?: return
        if (pg.getFind()!!.getPageIndex() != pageItem.getPageIndex()) pg.getEditor().getHighlight()?.removeHighlight()
    }
    override fun isTouchZoom(): Boolean = control?.getMainFrame()?.isTouchZoom() ?: false
    override fun isShowZoomingMsg(): Boolean = control?.getMainFrame()?.isShowZoomingMsg() ?: false
    override fun changeZoom() { control?.getMainFrame()?.changeZoom() }
    override fun setDrawPictrue(value: Boolean) { PictureKit.instance().setDrawPictrue(value) }
    fun getCurrentPGSlide(): PGSlide? = (listView?.getCurrentPageView() as? PGPageListItem)?.let { pgModel?.getSlide(it.getPageIndex()) } ?: pgModel?.getSlide(0)

    private fun drawPageNubmer(canvas: Canvas) {
        val frame = control?.getMainFrame() ?: return
        if (frame.isDrawPageNumber()) {
            val pn = "${getCurrentPageNumber()} / ${pgModel?.getSlideCount()}"
            val w = paint.measureText(pn).toInt()
            val h = (paint.descent() - paint.ascent()).toInt()
            val x = (width - w) / 2
            var y = height - h - 20
            val drawable = SysKit.getPageNubmerDrawable()
            drawable.setBounds(x - 10, y - 10, x + w + 10, y + h + 10)
            drawable.draw(canvas)
            y -= paint.ascent().toInt()
            canvas.drawText(pn, x.toFloat(), y.toFloat(), paint)
        }
        val pageNumber = getCurrentPageNumber()
        if (preShowPageIndex != pageNumber) {
            control?.getMainFrame()?.changePage(pageNumber, pgModel?.getRealSlideCount() ?: 0)
            preShowPageIndex = pageNumber
        }
    }
    override fun isChangePage(): Boolean = control?.getMainFrame()?.isChangePage() ?: false
    fun dispose() { control = null; listView?.dispose(); listView = null; pgModel = null; pageSize = Rect() }
}
