/*
 * 文件名称:          CellView.java
 *
 * 编译器:            android2.2
 * 时间:              下午2:11:50
 */
package com.wxiwei.office.wp.view

import android.graphics.Canvas
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.simpletext.model.IElement
import com.wxiwei.office.simpletext.view.AbstractView
import com.wxiwei.office.simpletext.view.IView

/**
 * table cell view
 */
class CellView(elem: IElement) : AbstractView() {

    // first merge cell
    private var isFirstMergedCell = false

    // merge cell
    private var isMergedCell = false

    // index in row
    private var column: Short = 0

    //
    private var background = -1

    init {
        this.elem = elem
    }

    override fun draw(canvas: Canvas, originX: Int, originY: Int, zoom: Float) {
        super.draw(canvas, originX, originY, zoom)
    }

    /**
     * model到视图
     * @param offset 指定的offset
     * @param isBack 是否向后取，是为在视图上，上一行的结束位置与下一行开始位置相同
     */
    override fun modelToView(offset: Long, rect: Rectangle, isBack: Boolean): Rectangle {
        val view = getView(offset, WPViewConstant.LINE_VIEW.toInt(), isBack)
        view?.modelToView(offset, rect, isBack)
        rect.x += getX() + getLeftIndent()
        rect.y += getY() + getTopIndent()
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

    override fun getType(): Short {
        return WPViewConstant.TABLE_CELL_VIEW
    }

    /**
     * @return Returns the isFirstMergedCell.
     */
    fun isFirstMergedCell(): Boolean {
        return isFirstMergedCell
    }

    /**
     * @param isFirstMergedCell The isFirstMergedCell to set.
     */
    fun setFirstMergedCell(isFirstMergedCell: Boolean) {
        this.isFirstMergedCell = isFirstMergedCell
    }

    /**
     * @return Returns the isMergedCell.
     */
    fun isMergedCell(): Boolean {
        return isMergedCell
    }

    /**
     * @param isMergedCell The isMergedCell to set.
     */
    fun setMergedCell(isMergedCell: Boolean) {
        this.isMergedCell = isMergedCell
    }

    fun isValidLastCell(): Boolean {
        if (getNextView() == null) {
            return true
        }
        val nextCell = getNextView() as CellView
        if (isMergedCell()) {
            return nextCell.isValidLastCell()
        }
        if (nextCell.getStartOffset(null) == 0L && nextCell.getEndOffset(null) == 0L) {
            return nextCell.isValidLastCell()
        }

        return false
    }

    /**
     * @return Returns the index.
     */
    fun getColumn(): Short {
        return column
    }

    /**
     * @param column The index to set.
     */
    fun setColumn(column: Short) {
        this.column = column
    }

    fun setBackground(color: Int) {
        this.background = color
    }

    fun getBackground(): Int {
        return this.background
    }

    override fun dispose() {
        super.dispose()
    }
}
