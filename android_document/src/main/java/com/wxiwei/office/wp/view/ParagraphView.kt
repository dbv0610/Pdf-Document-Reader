/*
 * 文件名称:          ParagraphView.java
 *
 * 编译器:            android2.2
 * 时间:              上午10:27:16
 */
package com.wxiwei.office.wp.view

import android.graphics.Canvas
import android.graphics.Paint
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.objectpool.IMemObj
import com.wxiwei.office.simpletext.model.IElement
import com.wxiwei.office.simpletext.model.ParaDecoration
import com.wxiwei.office.simpletext.view.AbstractView
import com.wxiwei.office.simpletext.view.IView

/**
 * word 段落视图
 */
open class ParagraphView(elem: IElement) : AbstractView(), IMemObj {

    /**
     *
     */
    private var bnView: BNView? = null

    // shading/borders and the text column they span (x offset and width, px at zoom 1)
    private var decoration: ParaDecoration? = null
    private var columnLeft = 0
    private var columnWidth = 0

    fun setDecoration(decoration: ParaDecoration?, columnLeft: Int, columnWidth: Int) {
        this.decoration = decoration
        this.columnLeft = columnLeft
        this.columnWidth = columnWidth
    }

    private fun sameDecoration(view: IView?): Boolean =
        view is ParagraphView && view.decoration == decoration && view.columnLeft == columnLeft && view.columnWidth == columnWidth

    /**
     * Word draws equal borders of consecutive paragraphs as one box: no line between them, and
     * shading fills the gap. [space] of each side is the padding between text and border.
     */
    private fun drawDecoration(canvas: Canvas, originX: Int, originY: Int, zoom: Float, d: ParaDecoration) {
        val pt = MainConstant.POINT_TO_PIXEL * zoom
        val joinPrev = sameDecoration(getPreView())
        val joinNext = sameDecoration(getNextView())
        val left = originX + (x + columnLeft) * zoom - (d.left?.space ?: 0) * pt
        val right = originX + (x + columnLeft + columnWidth) * zoom + (d.right?.space ?: 0) * pt
        var top = originY + y * zoom
        var bottom = originY + (y + height) * zoom
        top -= if (joinPrev) topIndent * zoom else (d.top?.space ?: 0) * pt
        bottom += if (joinNext) bottomIndent * zoom else (d.bottom?.space ?: 0) * pt
        val paint = decorationPaint
        d.shading?.let {
            paint.style = Paint.Style.FILL
            paint.color = it
            canvas.drawRect(left, top, right, bottom, paint)
        }
        paint.style = Paint.Style.STROKE
        fun line(side: ParaDecoration.Side?, x0: Float, y0: Float, x1: Float, y1: Float) {
            if (side == null) return
            paint.color = side.color
            paint.strokeWidth = maxOf(1f, side.eighths / 8f * pt)
            canvas.drawLine(x0, y0, x1, y1, paint)
        }
        if (!joinPrev) line(d.top, left, top, right, top)
        if (!joinNext) line(d.bottom, left, bottom, right, bottom)
        line(d.left, left, top, left, bottom)
        line(d.right, right, top, right, bottom)
    }

    init {
        this.elem = elem
    }

    fun getText(): String {
        return elem!!.getText(null)!!
    }

    override fun getType(): Short {
        return WPViewConstant.PARAGRAPH_VIEW
    }

    /**
     * model到视图
     * @param offset 指定的offset
     * @param isBack 是否向后取，是为在视图上，上一行的结束位置与下一行开始位置相同
     */
    override fun modelToView(offset: Long, rect: Rectangle, isBack: Boolean): Rectangle {
        if (getChildView() == null) {
            buildLine()
        }
        val view = getView(offset, WPViewConstant.LINE_VIEW.toInt(), isBack)
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
        if (getChildView() == null) {
            buildLine()
        }
        val vX = x - getX()
        val vY = y - getY()
        //IView view = getView(x, y, WPViewConstant.LINE_VIEW, isBack);
        val view = WPViewKit.instance().nearestChild(getChildView(), vY) { it.getLayoutSpan(WPViewConstant.Y_AXIS) }
        if (view != null) {
            return view.viewToModel(vX, vY, isBack)
        }
        return -1
    }

    override fun draw(canvas: Canvas, originX: Int, originY: Int, zoom: Float) {
        if (getChildView() == null) {
            buildLine()
        }
        val dX = (x * zoom).toInt() + originX
        val dY = (y * zoom).toInt() + originY
        decoration?.let { drawDecoration(canvas, originX, originY, zoom, it) }
        bnView?.draw(canvas, dX, dY, zoom)
        super.draw(canvas, originX, originY, zoom)
    }

    fun setBNView(bnView: BNView?) {
        this.bnView = bnView
    }

    fun getBNView(): BNView? {
        return this.bnView
    }

    private fun buildLine() {
        val doc = getDocument()
        if (doc != null) {
            LayoutKit.instance().buildLine(doc, this)
        }
    }

    override fun free() {
        /*IView temp = child;
        IView next;
        while (temp != null)
        {
            next = temp.getNextView();
            temp.free();
            temp = next;
        }
        child = null;
        if (bnView != null)
        {
            bnView.dispose();
            bnView = null;
        }*/
    }

    override fun getCopy(): IMemObj? {
        return null
    }

    override fun dispose() {
        super.dispose()
        decoration = null
        if (bnView != null) {
            bnView!!.dispose()
            bnView = null
        }
    }

    private companion object {
        // drawing happens on the UI and thumbnail threads
        private val decorationPaint: Paint get() = paints.get()!!
        private val paints = ThreadLocal.withInitial { Paint(Paint.ANTI_ALIAS_FLAG) }
    }
}
