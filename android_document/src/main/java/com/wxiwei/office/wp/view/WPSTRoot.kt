/*
 * 文件名称:          STRoot.java
 *
 * 编译器:            android2.2
 * 时间:              下午5:22:20
 */
package com.wxiwei.office.wp.view

import android.graphics.Canvas
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.constant.wp.AttrIDConstant
import com.wxiwei.office.constant.wp.WPAttrConstant
import com.wxiwei.office.constant.wp.WPModelConstant
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.simpletext.control.IWord
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.IDocument
import com.wxiwei.office.simpletext.view.DocAttr
import com.wxiwei.office.simpletext.view.IRoot
import com.wxiwei.office.simpletext.view.IView
import com.wxiwei.office.simpletext.view.PageAttr
import com.wxiwei.office.simpletext.view.ParaAttr
import com.wxiwei.office.simpletext.view.ViewContainer
import com.wxiwei.office.simpletext.view.ViewKit
import com.wxiwei.office.simpletext.view.AbstractView
import com.wxiwei.office.system.IControl
import com.wxiwei.office.wp.model.WPDocument

/**
 * 文本框的根视图
 */
class WPSTRoot(container: IWord, doc: IDocument, elementIndex: Int) : AbstractView(), IRoot {

    companion object {
        //
        private val tableLayout = TableLayoutKit()
    }

    //
    private var isWrapLine = false

    //
    private val elementIndex: Int

    //
    private var doc: IDocument? = null

    //
    private var pageAttr: PageAttr? = null

    //
    private var paraAttr: ParaAttr? = null

    //
    private var docAttr: DocAttr? = null

    //
    private var container: IWord? = null

    //max paragraph width
    private var maxParaWidth = 0

    init {
        this.doc = doc
        this.container = container
        this.elementIndex = elementIndex
        docAttr = DocAttr()
        paraAttr = ParaAttr()
        pageAttr = PageAttr()
    }

    override fun getType(): Short {
        return WPViewConstant.SIMPLE_ROOT
    }

    override fun getContainer(): IWord? {
        return container
    }

    /**
     * 得到model
     */
    override fun getDocument(): IDocument? {
        return doc
    }

    override fun getControl(): IControl {
        return container!!.getControl()!!
    }

    /**
     *  布局
     */
    fun doLayout() {
        val doc = doc!!
        val pageAttr = pageAttr!!
        val paraAttr = paraAttr!!
        val docAttr = docAttr

        val sec = (doc as WPDocument).getTextboxSectionElementForIndex(elementIndex)
        AttrManage.instance().fillPageAttr(pageAttr, sec!!.getAttribute())
        val attr = doc.getSection(WPModelConstant.MAIN)!!.getAttribute()
        val bodyWidth =
            ((AttrManage.instance().getPageWidth(attr) - AttrManage.instance().getPageMarginLeft(attr) - AttrManage.instance().getPageMarginRight(attr))
                    * MainConstant.TWIPS_TO_PIXEL).toInt()

        tableLayout.clearBreakPages()
        val dx = pageAttr.leftMargin
        var dy = pageAttr.topMargin
        setTopIndent(pageAttr.topMargin)
        setLeftIndent(pageAttr.leftMargin)

        var spanW = (if (isWrapLine) pageAttr.pageWidth else bodyWidth) - pageAttr.leftMargin - pageAttr.rightMargin
        //make sure the min layout width
        spanW = Math.max(IRoot.MINLAYOUTWIDTH, spanW)

        var spanH = Integer.MAX_VALUE
        // keep
        var flag = ViewKit.instance().setBitValue(0, WPViewConstant.LAYOUT_FLAG_KEEPONE.toInt(), true)
        // 是否布局水平对齐方式，不自动换行和页面水平居中都不需要处理lineView水平对齐
        flag = ViewKit.instance().setBitValue(flag, WPViewConstant.LAYOUT_NOT_WRAP_LINE.toInt(),
            !isWrapLine || pageAttr.horizontalAlign == WPAttrConstant.PAGE_H_CENTER)

        val maxEnd = sec!!.getEndOffset()
        var currentLayoutOffset = sec!!.getStartOffset()
        val paraCount = doc.getParaCount(maxEnd)
        if (paraCount == 0) {
            return
        }
        var elem = doc.getParagraph(currentLayoutOffset)
        var para: ParagraphView
        if (AttrManage.instance().hasAttribute(elem!!.getAttribute(), AttrIDConstant.PARA_LEVEL_ID)) {
            elem = (doc as WPDocument).getParagraph0(currentLayoutOffset)
            para = ViewFactory.createView(getControl(), elem, null, WPViewConstant.TABLE_VIEW.toInt()) as ParagraphView
        } else {
            para = ViewFactory.createView(getControl(), elem, null, WPViewConstant.PARAGRAPH_VIEW.toInt()) as ParagraphView
        }
        appendChlidView(para)
        para.setStartOffset(currentLayoutOffset)
        para.setEndOffset(elem!!.getEndOffset())
        var contentHeight = 0

        var breakType = 0
        while (spanH > 0 && currentLayoutOffset < maxEnd && breakType != WPViewConstant.BREAK_LIMIT.toInt()) {
            para.setLocation(dx, dy)
            // 表格段落
            if (para.getType() == WPViewConstant.TABLE_VIEW) {
                breakType = tableLayout.layoutTable(getControl(), doc, this, docAttr, pageAttr, paraAttr,
                    para as TableView, currentLayoutOffset, dx, dy, spanW, spanH, flag, false)
            } else {
                tableLayout.clearBreakPages()
                AttrManage.instance().fillParaAttr(getControl(), paraAttr, elem!!.getAttribute())
                breakType = LayoutKit.instance().layoutPara(getControl(), doc, docAttr!!, pageAttr, paraAttr,
                    para, currentLayoutOffset, dx, dy, spanW, spanH, flag)
            }
            val paraHeight = para.getLayoutSpan(WPViewConstant.Y_AXIS)
            dy += paraHeight
            currentLayoutOffset = para.getEndOffset(null)
            spanH -= paraHeight
            contentHeight += paraHeight
            maxParaWidth = Math.max(maxParaWidth, para.getLayoutSpan(WPViewConstant.X_AXIS))
            if (spanH > 0 && currentLayoutOffset < maxEnd) {
                elem = doc.getParagraph(currentLayoutOffset)
                if (elem == null) {
                    break
                }
                if (AttrManage.instance().hasAttribute(elem!!.getAttribute(), AttrIDConstant.PARA_LEVEL_ID)) {
                    elem = (doc as WPDocument).getParagraph0(currentLayoutOffset)
                    para = ViewFactory.createView(getControl(), elem, null, WPViewConstant.TABLE_VIEW.toInt()) as ParagraphView
                } else {
                    para = ViewFactory.createView(getControl(), elem, null, WPViewConstant.PARAGRAPH_VIEW.toInt()) as ParagraphView
                }
                para.setStartOffset(currentLayoutOffset)
                appendChlidView(para)
            }
        }

        if (!isWrapLine) {
            paraAlign(maxParaWidth)
        }

        layoutPageAlign(contentHeight, maxParaWidth)

        if (!isWrapLine) {
            pageAttr.pageWidth = bodyWidth
        }
    }

    private fun paraAlign(maxParaWidth: Int) {
        var paraView = getChildView()
        while (paraView != null) {
            paraAttr!!.horizontalAlignment = AttrManage.instance().getParaHorizontalAlign(paraView.getElement()!!.getAttribute()).toByte()
            var line = paraView.getChildView()
            while (line != null) {
                if (line.getType() == WPViewConstant.LINE_VIEW) {
                    (line as LineView).layoutAlignment(docAttr, pageAttr!!, paraAttr!!, (paraView as ParagraphView).getBNView(), maxParaWidth, 0, false)
                }
                line = line.getNextView()
            }
            paraView = paraView.getNextView()
        }
    }

    private fun layoutPageAlign(contentHeight: Int, maxParaWidth: Int) {
        val pageAttr = pageAttr!!
        val spanHeight = pageAttr.pageHeight - pageAttr.topMargin - pageAttr.bottomMargin
        var addSpan = 0
        when (pageAttr.verticalAlign) {
            WPAttrConstant.PAGE_V_CENTER -> addSpan = (spanHeight - contentHeight) / 2
            WPAttrConstant.PAGE_V_BOTTOM -> addSpan = spanHeight - contentHeight
            else -> {
            }
        }
        // word 中text只有当文本高度小于shape高度才需要做垂直布局
        if (addSpan < 0) {
            return
        }
        setY(addSpan)
        setTopIndent(addSpan)
        //
        if (pageAttr.horizontalAlign == WPAttrConstant.PAGE_H_CENTER) {
            val pageWidth = pageAttr.pageWidth - pageAttr.leftMargin - pageAttr.rightMargin
            addSpan = (pageWidth - maxParaWidth) / 2
            var view = getChildView()
            while (view != null) {
                paraAttr!!.horizontalAlignment = AttrManage.instance().getParaHorizontalAlign(view.getElement()!!.getAttribute()).toByte()
                var line = view.getChildView()
                while (line != null && line.getType() == WPViewConstant.LINE_VIEW) {
                    (line as LineView).layoutAlignment(docAttr, pageAttr, paraAttr!!, (view as ParagraphView).getBNView(), maxParaWidth, 0, false)
                    line.setX(line.getX() + addSpan)
                    line = line.getNextView()
                }
                view = view.getNextView()
            }
        }
    }

    fun getText(): String {
        var text = ""
        var view = getChildView()
        while (view != null) {
            text += (view as ParagraphView).getText()
            view = view.getNextView()
        }

        return text
    }

    override fun draw(canvas: Canvas, originX: Int, originY: Int, zoom: Float) {
        val pageAttr = pageAttr!!
        canvas.save()
        canvas.clipRect(originX.toFloat(), originY.toFloat(), originX + pageAttr.pageWidth * zoom, originY + (pageAttr.pageHeight - pageAttr.bottomMargin) * zoom)
        super.draw(canvas, originX, originY, zoom)
        canvas.restore()
    }

    override fun canBackLayout(): Boolean {
        return false
    }

    override fun backLayout() {
    }

    /**
     * model到视图
     * @param offset 指定的offset
     * @param isBack 是否向后取，是为在视图上，上一行的结束位置与下一行开始位置相同
     */
    override fun modelToView(offset: Long, rect: Rectangle, isBack: Boolean): Rectangle {
        val view = getView(offset, WPViewConstant.PARAGRAPH_VIEW.toInt(), isBack)
        view?.modelToView(offset, rect, isBack)
        rect.x += getX()
        rect.y += getY()
        return rect
    }

    /**
     * @param x
     * @param y
     * @param isBack 是否向后取，是为在视图上，上一行的结束位置与下一行开始位置相同
     */
    override fun viewToModel(x: Int, y: Int, isBack: Boolean): Long {
        var vX = x
        var vY = y
        vX -= getX()
        vY -= getY()
        var view = getChildView()
        while (view != null) {
            if (vY >= view.getY() && vY < view.getY() + view.getLayoutSpan(WPViewConstant.Y_AXIS)) {
                break
            }
            view = view.getNextView()
        }
        if (view != null) {
            return view.viewToModel(vX, vY, isBack)
        }
        return -1
    }

    override fun getViewContainer(): ViewContainer? {
        return null
    }

    fun getAdjustTextboxWidth(): Int {
        return maxParaWidth + pageAttr!!.leftMargin + pageAttr!!.rightMargin
    }

    fun setWrapLine(b: Boolean) {
        this.isWrapLine = b
    }

    override fun dispose() {
        super.dispose()
        doc = null
        container = null
        pageAttr = null
        paraAttr = null
        docAttr = null
    }
}
