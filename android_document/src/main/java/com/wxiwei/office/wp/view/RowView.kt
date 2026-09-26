/*
 * 文件名称:          RowView.java
 *
 * 编译器:            android2.2
 * 时间:              下午2:09:35
 */
package com.wxiwei.office.wp.view

import android.graphics.Rect
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.simpletext.model.IElement
import com.wxiwei.office.simpletext.view.AbstractView
import com.wxiwei.office.simpletext.view.IView

/**
 * table row view
 */
class RowView(elem: IElement) : AbstractView() {

    //
    private var isExactlyHeight = false

    init {
        this.elem = elem
    }

    override fun getType(): Short {
        return WPViewConstant.TABLE_ROW_VIEW
    }

    /**
     * 视图是否在指定区相交
     *
     * @param rect
     */
    override fun intersection(rect: Rect, originX: Int, originY: Int, zoom: Float): Boolean {
        return true
    }

    /**
     * @return Returns the isExactlyHeight.
     */
    fun isExactlyHeight(): Boolean {
        return isExactlyHeight
    }

    /**
     * @param isExactlyHeight The isExactlyHeight to set.
     */
    fun setExactlyHeight(isExactlyHeight: Boolean) {
        this.isExactlyHeight = isExactlyHeight
    }

    /**
     * get cell view for index this is row
     */
    fun getCellView(index: Short): CellView? {
        var t = 0
        var cellView = getChildView() as CellView?
        while (cellView != null) {
            if (t == index.toInt()) {
                break
            }
            t++
            cellView = cellView.getNextView() as CellView?
        }
        return cellView
    }

    /**
     * model到视图
     * @param offset 指定的offset
     * @param isBack 是否向后取，是为在视图上，上一行的结束位置与下一行开始位置相同
     */
    override fun modelToView(offset: Long, rect: Rectangle, isBack: Boolean): Rectangle {
        val view = getView(offset, WPViewConstant.TABLE_CELL_VIEW.toInt(), isBack)
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
        val vX = x - getX()
        val vY = y - getY()
        //IView view = getView(x, y, WPViewConstant.LINE_VIEW, isBack);
        var view: IView? = getChildView()
        if (view != null && vY > view.getY()) {
            while (view != null) {
                if (vY >= view.getY() && vY < view.getY() + view.getLayoutSpan(WPViewConstant.Y_AXIS)
                    && vX >= view.getX() && vX <= view.getX() + view.getLayoutSpan(WPViewConstant.X_AXIS)
                ) {
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

    override fun dispose() {
        super.dispose()
    }
}
