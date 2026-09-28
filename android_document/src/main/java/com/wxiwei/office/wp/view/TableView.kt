/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          TableView.java
 *
 * 编译器:            android2.2
 * 时间:              下午1:47:18
 */
package com.wxiwei.office.wp.view

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Style
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.IAttributeSet
import com.wxiwei.office.simpletext.model.IElement
import com.wxiwei.office.simpletext.view.IView

/**
 * 表格视图
 */
class TableView(elem: IElement) : ParagraphView(elem) {

    //
    private var breakPages = false

    override fun draw(canvas: Canvas, originX: Int, originY: Int, zoom: Float) {
        val tX = (x * zoom) + originX
        val tY = (y * zoom) + originY
        var row = getChildView() as RowView?
        val clip = canvas.getClipBounds()
        val paint = Paint()
        paint.style = Style.STROKE
        while (row != null) {
            var rX: Float
            var rY = 0f
            val isFirstRow = true
            var rowHeight = 0f
            if (row.intersection(clip, tX.toInt(), tY.toInt(), zoom)) {
                rX = tX + row.getX() * zoom
                if (isFirstRow) {
                    rY = tY + row.getY() * zoom
                } else {
                    rY += rowHeight
                }
                rowHeight = row.getHeight() * zoom
                var cell = row.getChildView() as CellView?
                var cX = 0f
                var cY: Float
                var cW = 0f
                var cH: Float
                var cRight: Float
                var isFirstCell = true
                while (cell != null) {
                    if (cell.intersection(clip, rX.toInt(), rY.toInt(), zoom)) {
                        if (cell.isMergedCell() && !cell.isFirstMergedCell()) {
                            cell = cell.getNextView() as CellView?
                            isFirstCell = true
                            continue
                        }
                        cY = rY + cell.getY() * zoom
                        if (isFirstCell) {
                            cX = rX + cell.getX() * zoom
                            isFirstCell = false
                        } else {
                            cX += cW
                        }
                        cW = cell.getLayoutSpan(WPViewConstant.X_AXIS) * zoom
                        cH = Math.max(cell.getHeight() * zoom, rowHeight)
                        cRight = cX + cW
                        // 最后一个单元格
                        if (cell.isValidLastCell()) {
                            if (Math.abs(cRight - (tX + getWidth() * zoom)) <= 10) {
                                cRight = tX + getWidth() * zoom
                            }
                        }
                        // background
                        if (cell.getBackground() != -1) {
                            val old = paint.color
                            paint.color = cell.getBackground()
                            paint.style = Style.FILL
                            canvas.drawRect(cX, cY, cRight, cY + cH, paint)
                            paint.color = old
                        }
                        val cellAttr = cell.getElement()?.getAttribute()
                        if (cellAttr != null && AttrManage.instance().hasTableCellBorders(cellAttr)) {
                            drawCellBorders(canvas, paint, cellAttr, cX, cY, cRight, cY + cH, zoom)
                        } else {
                            // no border information (DOC, PPT): legacy thin grid
                            paint.style = Style.STROKE
                            canvas.drawRect(cX, cY, cRight, cY + cH, paint)
                        }

                        canvas.save()
                        canvas.clipRect(cX, cY, cRight, cY + cH)
                        cell.draw(canvas, rX.toInt(), rY.toInt(), zoom)
                        canvas.restore()
                    }
                    cell = cell.getNextView() as CellView?
                }
            }
            row = row.getNextView() as RowView?
        }
    }

    /** Draws each side the reader resolved; widths are eighths of a point, 0 = no line. */
    private fun drawCellBorders(canvas: Canvas, paint: Paint, attr: IAttributeSet,
                                left: Float, top: Float, right: Float, bottom: Float, zoom: Float) {
        val am = AttrManage.instance()
        val oldColor = paint.color
        val oldWidth = paint.strokeWidth
        paint.style = Style.STROKE
        fun line(eighths: Int, color: Int, x0: Float, y0: Float, x1: Float, y1: Float) {
            if (eighths <= 0) return
            paint.color = color
            // eighths of a point to pixels; never thinner than one device pixel
            paint.strokeWidth = maxOf(1f, eighths / 8f * MainConstant.POINT_TO_PIXEL * zoom)
            canvas.drawLine(x0, y0, x1, y1, paint)
        }
        line(am.getTableTopBorder(attr), am.getTableTopBorderColor(attr), left, top, right, top)
        line(am.getTableBottomBorder(attr), am.getTableBottomBorderColor(attr), left, bottom, right, bottom)
        line(am.getTableLeftBorder(attr), am.getTableLeftBorderColor(attr), left, top, left, bottom)
        line(am.getTableRightBorder(attr), am.getTableRightBorderColor(attr), right, top, right, bottom)
        paint.color = oldColor
        paint.strokeWidth = oldWidth
    }

    /**
     * model到视图
     * @param offset 指定的offset
     * @param isBack 是否向后取，是为在视图上，上一行的结束位置与下一行开始位置相同
     */
    override fun modelToView(offset: Long, rect: Rectangle, isBack: Boolean): Rectangle {
        val view = getView(offset, WPViewConstant.TABLE_ROW_VIEW.toInt(), isBack)
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
        var view: IView? = getChildView()
        if (view != null && vY > view.getY()) {
            while (view != null) {
                if (vY >= view.getY() && vY < view.getY() + view.getLayoutSpan(WPViewConstant.Y_AXIS)) {
                    break
                }
                view = view.getNextView()
            }
        }
        view = if (view == null) getChildView() else view
        if (view != null) {
            return view.viewToModel(vX, vY, isBack)
        }
        return -1
    }

    override fun getType(): Short {
        return WPViewConstant.TABLE_VIEW
    }

    override fun dispose() {
        super.dispose()
    }

    /**
     * @return Returns the isBreakPages.
     */
    fun isBreakPages(): Boolean {
        return breakPages
    }

    /**
     * @param isBreakPages The isBreakPages to set.
     */
    fun setBreakPages(isBreakPages: Boolean) {
        this.breakPages = isBreakPages
    }
}
