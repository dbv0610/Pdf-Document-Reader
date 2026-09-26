/*
 * 文件名称:          ParagraphView.java
 *
 * 编译器:            android2.2
 * 时间:              上午10:27:16
 */
package com.wxiwei.office.wp.view

import android.graphics.Canvas
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.objectpool.IMemObj
import com.wxiwei.office.simpletext.model.IElement
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
        var view: IView? = getChildView()
        if (view != null && vY > view.getY()) {
            while (view != null) {
                if (vY >= view.getY() && vY < view.getY() + view.getLayoutSpan(WPViewConstant.Y_AXIS)) {
                    break
                }
                view = view.getNextView()
            }
        }
        view = view ?: getChildView()
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
        if (bnView != null) {
            bnView!!.dispose()
            bnView = null
        }
    }
}
