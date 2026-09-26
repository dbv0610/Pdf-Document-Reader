/*
 * 文件名称:          STRoot.java
 *
 * 编译器:            android2.2
 * 时间:              下午5:22:20
 */
package com.wxiwei.office.simpletext.view

import android.graphics.Canvas
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.constant.wp.WPAttrConstant
import com.wxiwei.office.constant.wp.WPModelConstant
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.pg.animate.ShapeAnimation
import com.wxiwei.office.simpletext.control.IWord
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.IDocument
import com.wxiwei.office.system.IControl
import com.wxiwei.office.wp.view.LayoutKit
import com.wxiwei.office.wp.view.LineView
import com.wxiwei.office.wp.view.ParagraphView
import com.wxiwei.office.wp.view.ViewFactory

/**
 * 文本框的根视图
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2011-11-14
 *
 * 负责人:          ljj8494
 */
open class STRoot(container: IWord?, doc: IDocument?) : AbstractView(), IRoot {
    //
    private var isWrapLine = false

    //
    private var doc: IDocument? = doc

    //
    private var pageAttr: PageAttr? = PageAttr()

    //
    private var paraAttr: ParaAttr? = ParaAttr()

    //
    private var docAttr: DocAttr? = DocAttr()

    //
    private var container: IWord? = container

    /**
     *
     */
    override fun getType(): Short {
        return WPViewConstant.SIMPLE_ROOT
    }

    /**
     *
     */
    override fun getContainer(): IWord? {
        return container
    }

    /**
     * 得到model
     */
    override fun getDocument(): IDocument? {
        return doc
    }

    /**
     *
     */
    override fun getControl(): IControl? {
        return container!!.getControl()
    }

    /**
     *  布局
     */
    fun doLayout() {
        val doc = this.doc!!
        val pageAttr = this.pageAttr!!
        val paraAttr = this.paraAttr!!
        val sec = doc.getSection(WPModelConstant.MAIN)
        AttrManage.instance().fillPageAttr(pageAttr, sec!!.getAttribute())

        val dx = pageAttr.leftMargin
        var dy = pageAttr.topMargin
        setTopIndent(pageAttr.topMargin)
        setLeftIndent(pageAttr.leftMargin)
        var spanW = (if (isWrapLine) pageAttr.pageWidth else Int.MAX_VALUE) - pageAttr.leftMargin - pageAttr.rightMargin
        //make sure the min layout width
        spanW = Math.max(IRoot.MINLAYOUTWIDTH, spanW)

        var spanH = Int.MAX_VALUE //pageAttr.pageHeight - pageAttr.topMargin - pageAttr.bottomMargin;
        // keep
        var flag = ViewKit.instance().setBitValue(0, WPViewConstant.LAYOUT_FLAG_KEEPONE.toInt(), true)
        // 是否布局水平对齐方式，不自动换行和页面水平居中都不需要处理lineView水平对齐
        flag = ViewKit.instance().setBitValue(
            0, WPViewConstant.LAYOUT_NOT_WRAP_LINE.toInt(),
            !isWrapLine || pageAttr.horizontalAlign == WPAttrConstant.PAGE_H_CENTER
        )

        val maxEnd = doc.getAreaEnd(WPModelConstant.MAIN)
        var currentParaIndex = 0
        var currentLayoutOffset: Long = 0

        var elem = doc.getParagraphForIndex(currentParaIndex++, WPModelConstant.MAIN)
        var para = ViewFactory.createView(container!!.getControl()!!, elem, null, WPViewConstant.PARAGRAPH_VIEW.toInt()) as ParagraphView
        appendChlidView(para)
        para.setStartOffset(currentLayoutOffset)
        para.setEndOffset(elem!!.getEndOffset())
        var maxParaWidth = 0
        var contentHeight = 0
        var firstPara = true
        val paraCount = doc.getParaCount(maxEnd)
        while (spanH > 0 && currentLayoutOffset < maxEnd) {
            para.setLocation(dx, dy)
            AttrManage.instance().fillParaAttr(container!!.getControl(), paraAttr, elem!!.getAttribute())
            if (container!!.getEditType() == MainConstant.APPLICATION_TYPE_PPT) {
                if (firstPara) {
                    paraAttr.beforeSpace = 0
                }
                if (paraCount == currentParaIndex) {
                    paraAttr.afterSpace = 0
                }
            }
            LayoutKit.instance().layoutPara(
                container!!.getControl()!!, doc, docAttr!!, pageAttr, paraAttr, para,
                currentLayoutOffset, dx, dy, spanW, Int.MAX_VALUE, flag
            )
            val paraHeight = para.getLayoutSpan(WPViewConstant.Y_AXIS)
            dy += paraHeight
            currentLayoutOffset = para.getEndOffset(null)
            spanH -= paraHeight
            contentHeight += paraHeight
            maxParaWidth = Math.max(maxParaWidth, para.getLayoutSpan(WPViewConstant.X_AXIS))
            if (spanH > 0 && currentLayoutOffset < maxEnd) {
                elem = doc.getParagraphForIndex(currentParaIndex++, WPModelConstant.MAIN)
                if (elem == null) {
                    break
                }
                para = ViewFactory.createView(container!!.getControl()!!, elem, null, WPViewConstant.PARAGRAPH_VIEW.toInt()) as ParagraphView
                para.setStartOffset(currentLayoutOffset)
                appendChlidView(para)
            }
            firstPara = false
        }

        if (pageAttr.horizontalAlign == WPAttrConstant.PAGE_H_LEFT) {
            paraAlign(
                if (pageAttr.horizontalAlign == WPAttrConstant.PAGE_H_CENTER) maxParaWidth
                else pageAttr.pageWidth - pageAttr.leftMargin - pageAttr.rightMargin
            )
        }
        layoutPageAlign(contentHeight, maxParaWidth)
    }

    /**
     *
     */
    private fun paraAlign(maxParaWidth: Int) {
        if (isWrapLine) {
            return
        }
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

    /**
     *
     */
    private fun layoutPageAlign(contentHeight: Int, maxParaWidth: Int) {
        val pageAttr = this.pageAttr!!
        val spanHeight = pageAttr.pageHeight - pageAttr.topMargin - pageAttr.bottomMargin
        var addSpan = 0
        when (pageAttr.verticalAlign) {
            WPAttrConstant.PAGE_V_CENTER -> addSpan = (spanHeight - contentHeight) / 2
            WPAttrConstant.PAGE_V_BOTTOM -> addSpan = spanHeight - contentHeight
            else -> {
            }
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

    /**
     *
     * @param canvas
     * @param originX
     * @param originY
     * @param zoom
     * @param xls
     */
    override fun draw(canvas: Canvas, originX: Int, originY: Int, zoom: Float) {
        val linesHeight: MutableList<Int> = ArrayList(10)
        if (container != null /*&& container.getEditType() == MainConstant.APPLICATION_TYPE_PPT*/) {
            val dX = (x * zoom).toInt() + originX
            val dY = (y * zoom).toInt() + originY
            var view = getChildView()
            val clip = canvas.getClipBounds()
            var paraID = 0
            while (view != null) {
                if (view.intersection(clip, dX, dY, zoom)) {
                    val animation = container!!.getParagraphAnimation(paraID)
                    if (animation != null) {
                        val shapeAnim = animation.getShapeAnimation()
                        val paraBegin = shapeAnim!!.getParagraphBegin()
                        val paraEnd = shapeAnim.getParagraphEnd()
                        if (paraBegin == ShapeAnimation.Para_All && paraEnd == ShapeAnimation.Para_All
                            || (paraBegin == ShapeAnimation.Para_BG && paraEnd == ShapeAnimation.Para_BG)
                            || (paraBegin >= 0 && paraEnd >= 0 && paraID >= paraBegin && paraID <= paraEnd)
                        ) {
                            val area = view.getViewRect(dX, dY, zoom)
                            val frameCnt = (animation.getFPS() * animation.getDuration() / 1000f).toInt()
                            val progress = animation.getCurrentAnimationInfor()!!.getProgress()
                            if (shapeAnim.getAnimationType() == ShapeAnimation.SA_ENTR) {
                                val a = (area.bottom + MainConstant.GAP) * 2 / Math.pow(frameCnt.toDouble(), 2.0)
                                val y = (dY - (area.bottom + MainConstant.GAP) + 0.5f * a * Math.pow((frameCnt * progress).toDouble(), 2.0)).toInt()
                                if (view.intersection(clip, dX, y, zoom)) {
                                    if (view.intersection(clip, dX, y, zoom)) {
                                        view.draw(canvas, dX, y, zoom)
                                    }
                                }
                            } else if (shapeAnim.getAnimationType() == ShapeAnimation.SA_EMPH) {
                                canvas.save()
                                canvas.rotate(
                                    animation.getCurrentAnimationInfor()!!.getAngle().toFloat(),
                                    area.centerX().toFloat(),
                                    area.centerY().toFloat()
                                )
                                view.draw(canvas, dX, dY, zoom)
                                canvas.restore()
                            } else if (shapeAnim.getAnimationType() == ShapeAnimation.SA_EXIT) {
                                val a = (clip.bottom - area.top + MainConstant.GAP) * 2 / Math.pow(frameCnt.toDouble(), 2.0)
                                val y = (dY + (clip.bottom - area.top + MainConstant.GAP) - 0.5f * a * Math.pow((frameCnt * (1 - progress)).toDouble(), 2.0)).toInt()
                                if (view.intersection(clip, dX, y, zoom)) {
                                    view.draw(canvas, dX, y, zoom)
                                }
                            } else {
                                view.draw(canvas, dX, dY, zoom)
                            }
                        }
                    } else {
                        view.draw(canvas, dX, dY, zoom)
                    }
                }
                paraID++
                view = view.getNextView()
            }
        } else {
            super.draw(canvas, originX, originY, zoom)
        }
    }

    /**
     *
     */
    override fun canBackLayout(): Boolean {
        return false
    }

    /**
     *
     */
    override fun backLayout() {
    }

    /**
     * model到视图
     * @param offset 指定的offset
     * @param isBack 是否向后取，是为在视图上，上一行的结束位置与下一行开始位置相同
     */
    override fun modelToView(offset: Long, rect: Rectangle, isBack: Boolean): Rectangle? {
        val view = getView(offset, WPViewConstant.PARAGRAPH_VIEW.toInt(), isBack)
        if (view != null) {
            view.modelToView(offset, rect, isBack)
        }
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
        var x = x
        var y = y
        x -= getX()
        y -= getY()
        var view = getChildView()
        while (view != null) {
            if (y >= view.getY() && y < view.getY() + view.getLayoutSpan(WPViewConstant.Y_AXIS)) {
                break
            }
            view = view.getNextView()
        }
        if (view != null) {
            return view.viewToModel(x, y, isBack)
        }
        return -1
    }

    /**
     *
     */
    override fun getViewContainer(): ViewContainer? {
        return null
    }

    /**
     *
     */
    fun setWrapLine(b: Boolean) {
        this.isWrapLine = b
    }

    /**
     *
     */
    override fun dispose() {
        super.dispose()
        doc = null
        container = null
        pageAttr = null
        paraAttr = null
        docAttr = null
    }
}
