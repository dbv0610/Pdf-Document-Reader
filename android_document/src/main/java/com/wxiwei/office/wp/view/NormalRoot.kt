package com.wxiwei.office.wp.view

import com.wxiwei.office.system.*

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.util.Log
import com.wxiwei.office.constant.EventConstant
import com.wxiwei.office.constant.wp.AttrIDConstant
import com.wxiwei.office.constant.wp.WPModelConstant
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.simpletext.control.IWord
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.IDocument
import com.wxiwei.office.simpletext.model.IElement
import com.wxiwei.office.simpletext.view.AbstractView
import com.wxiwei.office.simpletext.view.DocAttr
import com.wxiwei.office.simpletext.view.IRoot
import com.wxiwei.office.simpletext.view.IView
import com.wxiwei.office.simpletext.view.PageAttr
import com.wxiwei.office.simpletext.view.ParaAttr
import com.wxiwei.office.simpletext.view.ViewContainer
import com.wxiwei.office.simpletext.view.ViewKit
import com.wxiwei.office.system.IControl
import com.wxiwei.office.wp.control.Word
import com.wxiwei.office.wp.model.WPDocument

class NormalRoot(private var word: Word?) : AbstractView(), IRoot {
    private val LAYOUT_PARA = 20
    private var doc: IDocument? = word?.getDocument()
    private var layoutThread = LayoutThread(this)
    private var canBackLayoutFlag = true
    private var docAttr = DocAttr().apply { rootType = WPViewConstant.NORMAL_ROOT.toByte() }
    private var pageAttr = PageAttr()
    private var paraAttr = ParaAttr()
    private var viewContainer = ViewContainer()
    private var tableLayout = TableLayoutKit()
    private var relayout = true
    private var prePara: ParagraphView? = null
    private var currentLayoutOffset = 0L
    private var maxParaWidth = 0

    override fun getType(): Short = WPViewConstant.NORMAL_ROOT
    override fun getDocument(): IDocument? = word?.getDocument()
    override fun getContainer(): IWord? = word
    override fun getControl(): IControl? = word?.getControl()

    fun layoutAll(): Int {
        super.dispose()
        tableLayout.clearBreakPages()
        word?.getControl()?.getSysKit()?.getListManage()?.resetForNormalView()
        viewContainer.clear()
        maxParaWidth = 0
        prePara = null
        currentLayoutOffset = 0
        layoutPara()
        if (doc != null && currentLayoutOffset < doc!!.getAreaEnd(WPModelConstant.MAIN)) {
            canBackLayoutFlag = true
            layoutThread.start()
            word?.getControl()?.actionEvent(EventConstant.SYS_SET_PROGRESS_BAR_ID, true)
        }
        layoutRoot()
        if (word?.isExportImageAfterZoom() == true) {
            if (getHeight() * (word?.getZoom() ?: 1f) >= (word?.getScrollY() ?: 0) + (word?.getHeight() ?: 0) || currentLayoutOffset >= doc!!.getAreaEnd(WPModelConstant.MAIN)) {
                word?.setExportImageAfterZoom(false)
                word?.getControl()?.actionEvent(EventConstant.APP_GENERATED_PICTURE_ID, null)
            }
        }
        return WPViewConstant.BREAK_NO.toInt()
    }

    fun doLayout(x: Int, y: Int, w: Int, h: Int, maxEnd: Int, flag: Int): Int {
        Log.e("normal root", "doLayout ")
        val localDoc = getDocument() ?: return WPViewConstant.BREAK_NO.toInt()
        viewContainer.clear()
        layoutPara()
        if (currentLayoutOffset < localDoc.getAreaEnd(WPModelConstant.MAIN)) {
            layoutThread.start()
            word?.getControl()?.actionEvent(EventConstant.SYS_SET_PROGRESS_BAR_ID, true)
        }
        layoutRoot()
        return WPViewConstant.BREAK_NO.toInt()
    }

    private fun layoutPara(): Int {
        relayout = true
        val localWord = word ?: return WPViewConstant.BREAK_NO.toInt()
        var dx = WPViewConstant.PAGE_SPACE.toInt()
        var dy = if (prePara == null) WPViewConstant.PAGE_SPACE.toInt() else prePara!!.getY() + prePara!!.getHeight()
        val spanW = if (localWord.getControl()!!.getMainFrame().isZoomAfterLayoutForWord()) ((localWord.resources.displayMetrics.widthPixels / localWord.getZoom()) - WPViewConstant.PAGE_SPACE * 2).toInt() else localWord.resources.displayMetrics.widthPixels - WPViewConstant.PAGE_SPACE * 2
        var spanH = Int.MAX_VALUE
        var flag = ViewKit.instance().setBitValue(0, WPViewConstant.LAYOUT_FLAG_KEEPONE.toInt(), true)
        var count = 0
        val areaEnd = doc!!.getAreaEnd(WPModelConstant.MAIN)
        while (count < LAYOUT_PARA && currentLayoutOffset < areaEnd && relayout) {
            var elem = doc!!.getParagraph(currentLayoutOffset) ?: break
            var para: ParagraphView = if (AttrManage.instance().hasAttribute(elem.getAttribute(), AttrIDConstant.PARA_LEVEL_ID)) {
                elem = (doc as WPDocument).getParagraph0(currentLayoutOffset)!!
                ViewFactory.createView(localWord.getControl()!!, elem, null, WPViewConstant.TABLE_VIEW.toInt()) as ParagraphView
            } else {
                ViewFactory.createView(localWord.getControl()!!, elem, null, WPViewConstant.PARAGRAPH_VIEW.toInt()) as ParagraphView
            }
            para.setParentView(this)
            val start = elem.getStartOffset()
            para.setStartOffset(start)
            para.setEndOffset(elem.getEndOffset())
            if (prePara == null) setChildView(para) else {
                prePara!!.setNextView(para)
                para.setPreView(prePara)
            }
            para.setLocation(dx, dy)
            if (para.getType() == WPViewConstant.TABLE_VIEW) {
                tableLayout.layoutTable(localWord.getControl()!!, doc!!, this, docAttr, pageAttr, paraAttr, para as TableView, currentLayoutOffset, dx, dy, spanW, spanH, flag, false)
            } else {
                tableLayout.clearBreakPages()
                AttrManage.instance().fillParaAttr(localWord.getControl()!!, paraAttr, elem.getAttribute())
                filteParaAttr(paraAttr)
                LayoutKit.instance().layoutPara(localWord.getControl()!!, doc!!, docAttr, pageAttr, paraAttr, para, currentLayoutOffset, dx, dy, spanW, spanH, flag)
            }
            val paraHeight = para.getLayoutSpan(WPViewConstant.Y_AXIS)
            maxParaWidth = kotlin.math.max(para.getLayoutSpan(WPViewConstant.X_AXIS) + WPViewConstant.PAGE_SPACE, maxParaWidth)
            dy += paraHeight
            spanH -= paraHeight
            currentLayoutOffset = para.getEndOffset(null)
            count++
            prePara = para
            viewContainer.add(para)
        }
        return WPViewConstant.BREAK_NO.toInt()
    }

    private fun filteParaAttr(paraAttr: ParaAttr) {
        paraAttr.rightIndent = if (paraAttr.rightIndent < 0) 0 else paraAttr.rightIndent
        paraAttr.leftIndent = if (paraAttr.leftIndent < 0) 0 else paraAttr.leftIndent
    }

    override fun draw(canvas: Canvas, originX: Int, originY: Int, zoom: Float) {
        canvas.drawColor(Color.WHITE)
        val dX = (x * zoom).toInt() + originX
        val dY = (y * zoom).toInt() + originY
        var view = getChildView()
        val clip = canvas.clipBounds
        var b = false
        while (view != null) {
            if (view.intersection(clip, dX, dY, zoom)) {
                view.draw(canvas, dX, dY, zoom)
                b = true
            } else if (b) {
                break
            }
            view = view.getNextView()
        }
    }

    override fun modelToView(offset: Long, rect: Rectangle, isBack: Boolean): Rectangle {
        val view = viewContainer.getParagraph(offset, isBack)
        if (view != null) {
            view.modelToView(offset, rect, isBack)
            var p = view.getParentView()
            while (p != null && p.getType() != WPViewConstant.NORMAL_ROOT) {
                rect.x += p.getX()
                rect.y += p.getY()
                p = p.getParentView()
            }
        }
        rect.x += getX()
        rect.y += getY()
        return rect
    }

    override fun viewToModel(x: Int, y: Int, isBack: Boolean): Long {
        var xx = x - getX()
        var yy = y - getY()
        var view: IView? = getChildView() ?: return -1
        if (yy > view!!.getY()) {
            while (view != null) {
                if (yy >= view.getY() && yy < view.getY() + view.getLayoutSpan(WPViewConstant.Y_AXIS)) break
                view = view.getNextView()
            }
        }
        view = view ?: getChildView()
        return view?.viewToModel(xx, yy, isBack) ?: -1
    }

    override fun canBackLayout(): Boolean = canBackLayoutFlag && currentLayoutOffset < doc!!.getAreaEnd(WPModelConstant.MAIN)
    override fun backLayout() {
        layoutPara()
        layoutRoot()
        if (currentLayoutOffset >= doc!!.getAreaEnd(WPModelConstant.MAIN)) {
            word?.getControl()?.actionEvent(EventConstant.SYS_AUTO_TEST_FINISH_ID, true)
            word?.getControl()?.actionEvent(EventConstant.SYS_SET_PROGRESS_BAR_ID, false)
            val r = word!!.getVisibleRect()
            var sX = r.x
            var sY = r.y
            val wW = (getWidth() * word!!.getZoom()).toInt()
            val wH = (getHeight() * word!!.getZoom()).toInt()
            if (r.x + r.width > wW) sX = wW - r.width
            if (r.y + r.height > wH) sY = wH - r.height
            val sTx = sX
            val sTy = sY
            if (sX != r.x || sY != r.y) {
                word?.post { word?.scrollTo(kotlin.math.max(0, sTx), kotlin.math.max(0, sTy)) }
            }
        }
        word?.postInvalidate()
        if (word?.isExportImageAfterZoom() == true) {
            if (getHeight() * word!!.getZoom() >= word!!.getScrollY() + word!!.getHeight() || currentLayoutOffset >= doc!!.getAreaEnd(WPModelConstant.MAIN)) {
                word?.setExportImageAfterZoom(false)
                word?.getControl()?.actionEvent(EventConstant.APP_GENERATED_PICTURE_ID, null)
            }
        }
    }

    fun layoutRoot() {
        if (prePara != null) setSize(kotlin.math.max(word!!.getWidth(), maxParaWidth), prePara!!.getY() + prePara!!.getHeight())
    }

    fun stopBackLayout() { canBackLayoutFlag = false; relayout = false }
    override fun getViewContainer(): ViewContainer = viewContainer
    fun setLayoutThreadDied(isDied: Boolean) { layoutThread.setDied(isDied) }

    override fun dispose() {
        super.dispose()
        canBackLayoutFlag = false
        layoutThread.dispose()
        word = null
        docAttr.dispose()
        pageAttr.dispose()
        paraAttr.dispose()
        prePara = null
        doc = null
    }
}
