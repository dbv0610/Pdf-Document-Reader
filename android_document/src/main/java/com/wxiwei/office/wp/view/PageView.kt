/*
 * 文件名称:          PageView.java
 *
 * 编译器:            android2.2
 * 时间:              下午9:42:18
 */
package com.wxiwei.office.wp.view

import com.wxiwei.office.system.*

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.wxiwei.office.common.BackgroundDrawer
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.simpletext.model.IElement
import com.wxiwei.office.simpletext.view.AbstractView
import com.wxiwei.office.simpletext.view.IView
import com.wxiwei.office.wp.model.WPDocument

/**
 * 页面视图
 */
class PageView(elem: IElement) : AbstractView() {

    //
    private var breakTable = false

    //
    private var pageBRColor = 0

    //
    private var pageBorderIndex = -1

    //
    private var paint: Paint? = null

    // 页码
    private var pageNum = 0

    // header
    private var headerView: TitleView? = null

    // footer
    private var footerView: TitleView? = null

    //autoshape view and picture view
    private var shapeViews: MutableList<LeafView>? = null

    init {
        this.elem = elem
        paint = Paint()
        paint!!.strokeWidth = 2f
    }

    override fun getType(): Short {
        return WPViewConstant.PAGE_VIEW
    }

    override fun draw(canvas: Canvas, originX: Int, originY: Int, zoom: Float) {
        canvas.save()
        val dX = (x * zoom).toInt() + originX
        val dY = (y * zoom).toInt() + originY

        canvas.clipRect(dX.toFloat(), dY.toFloat(), dX + getWidth() * zoom, dY + getHeight() * zoom)

        // draw background
        drawBackground(canvas, dX, dY, zoom)
        // draw border
        drawBorder(canvas, dX, dY, zoom)
        // draw paper
        drawPaper(canvas, dX, dY, zoom)
        // Separated
        drawPageSeparated(canvas, dX, dY, zoom)

        headerView?.let {
            it.setParentView(this)
            it.draw(canvas, dX, dY, zoom)
        }
        footerView?.let {
            it.setParentView(this)
            it.draw(canvas, dX, dY, zoom)
        }

        // draw shape
        drawShape(canvas, dX, dY, zoom, true)

        super.draw(canvas, originX, originY, zoom)

        // draw shape
        drawShape(canvas, dX, dY, zoom, false)

        canvas.restore()
    }

    fun drawForPrintMode(canvas: Canvas, originX: Int, originY: Int, zoom: Float) {
        val dX = (x * zoom).toInt() + originX
        val dY = (y * zoom).toInt() + originY
        // draw background
        drawBackground(canvas, dX, dY, zoom)
        //
        drawBorder(canvas, dX, dY, zoom)
        //
        drawPageSeparated(canvas, dX, dY, zoom)
        headerView?.let {
            it.setParentView(this)
            it.draw(canvas, dX, dY, zoom)
        }
        footerView?.let {
            it.setParentView(this)
            it.draw(canvas, dX, dY, zoom)
        }
        // draw shape
        drawShape(canvas, dX, dY, zoom, true)

        super.draw(canvas, originX, originY, zoom)

        // draw shape
        drawShape(canvas, dX, dY, zoom, false)
    }

    fun drawToImage(canvas: Canvas, originX: Int, originY: Int, zoom: Float) {
        val dX = (x * zoom).toInt() + originX
        val dY = (y * zoom).toInt() + originY
        // draw background
        drawBackground(canvas, dX, dY, zoom)
        //
        drawBorder(canvas, dX, dY, zoom)
        headerView?.let {
            it.setParentView(this)
            it.draw(canvas, dX, dY, zoom)
        }
        footerView?.let {
            it.setParentView(this)
            it.draw(canvas, dX, dY, zoom)
        }
        // draw shape
        drawShape(canvas, dX, dY, zoom, true)

        super.draw(canvas, originX, originY, zoom)

        // draw shape
        drawShape(canvas, dX, dY, zoom, false)
    }

    private fun drawBackground(canvas: Canvas, dx: Int, dy: Int, zoom: Float) {
        val w = (getWidth() * zoom).toInt()
        val h = (getHeight() * zoom).toInt()

        val rect = Rect(dx, dy, dx + w, dy + h)
        val pageFill = (getDocument() as WPDocument).getPageBackground()
        if (pageFill != null) {
            BackgroundDrawer.drawBackground(canvas, getControl(), pageNum, pageFill, rect, null, zoom)
        } else {
            paint!!.color = -0x1
            canvas.drawRect(dx.toFloat(), dy.toFloat(), (dx + w).toFloat(), (dy + h).toFloat(), paint!!)
        }
    }

    private fun drawBorder(canvas: Canvas, dx: Int, dy: Int, zoom: Float) {
        // draw page border
        if (pageBorderIndex >= 0) {
            val paint = paint!!
            val w = (getWidth() * zoom).toInt()
            val h = (getHeight() * zoom).toInt()
            val bs = getControl()!!.getSysKit().getBordersManage().getBorders(pageBorderIndex)
            val old = paint.color
            if (bs != null) {
                val left = bs.getLeftBorder()
                val top = bs.getTopBorder()
                val right = bs.getRightBorder()
                val bottom = bs.getBottomBorder()
                var sX: Int
                var sY: Int
                var eX: Int
                var eY: Int
                // left
                if (left != null) {
                    paint.color = left.getColor()
                    sX = (zoom * left.getSpace()).toInt() + dx
                    eX = sX
                    sY = (if (top == null) 0 else (top.getSpace() * zoom).toInt()) + dy
                    eY = (if (bottom == null) h.toFloat() else (h - bottom.getSpace() * zoom)).toInt() + dy
                    canvas.drawLine(sX.toFloat(), sY.toFloat(), eX.toFloat(), eY.toFloat(), paint)
                }
                // top
                if (top != null) {
                    paint.color = top.getColor()
                    sY = (zoom * top.getSpace()).toInt() + dy
                    eY = sY
                    sX = (if (left == null) 0 else (left.getSpace() * zoom).toInt()) + dx - 1
                    eX = (if (right == null) w.toFloat() else (w - right.getSpace() * zoom)).toInt() + dx + 1
                    canvas.drawLine(sX.toFloat(), sY.toFloat(), eX.toFloat(), eY.toFloat(), paint)
                }
                // right
                if (right != null) {
                    paint.color = right.getColor()
                    sX = (w - right.getSpace() * zoom).toInt() + dx
                    eX = sX
                    sY = (if (top == null) 0f else (top.getSpace() * zoom)).toInt() + dy
                    eY = (if (bottom == null) h.toFloat() else (h - bottom.getSpace() * zoom)).toInt() + dy
                    canvas.drawLine(sX.toFloat(), sY.toFloat(), eX.toFloat(), eY.toFloat(), paint)
                }
                // bottom
                if (bottom != null) {
                    paint.color = bottom.getColor()
                    sY = (h - zoom * top!!.getSpace()).toInt() + dy
                    eY = sY
                    sX = (if (left == null) 0 else (left.getSpace() * zoom).toInt()) + dx - 1
                    eX = (if (right == null) w.toFloat() else (w - right.getSpace() * zoom)).toInt() + dx + 1
                    canvas.drawLine(sX.toFloat(), sY.toFloat(), eX.toFloat(), eY.toFloat(), paint)
                }
            }
            paint.color = old
        }
    }

    private fun drawPaper(canvas: Canvas, dx: Int, dy: Int, zoom: Float) {
        canvas.save()
        val paint = paint!!
        val w = (getWidth() * zoom).toInt()
        val h = (getHeight() * zoom).toInt()
        canvas.clipRect(dx, dy, dx + w + 5, dy + h + 5)
        // 绘黑色边框
        paint.color = Color.BLACK
        // 上
        canvas.drawLine(dx.toFloat(), dy.toFloat(), (dx + w).toFloat(), dy.toFloat(), paint)
        // 左
        canvas.drawLine(dx.toFloat(), dy.toFloat(), dx.toFloat(), (dy + h).toFloat(), paint)
        // 右
        canvas.drawLine((dx + w).toFloat(), dy.toFloat(), (dx + w).toFloat(), (dy + h).toFloat(), paint)
        // 下
        canvas.drawLine(dx.toFloat(), (dy + h).toFloat(), (dx + w).toFloat(), (dy + h).toFloat(), paint)

        canvas.restore()
    }

    private fun drawPageSeparated(canvas: Canvas, dx: Int, dy: Int, zoom: Float) {
        val paint = paint!!
        val bm = 30
        val left = getLeftIndent() * zoom + dx
        val top = getTopIndent() * zoom + dy
        paint.color = Color.GRAY
        // 左上角
        canvas.drawRect(left - 1, top - bm, left, top, paint)
        canvas.drawRect(left - bm, top - 1, left, top, paint)

        // 右上角
        val right = (getWidth() - getRightIndent()) * zoom + dx
        canvas.drawRect(right, top - bm, right + 1, top, paint)
        canvas.drawRect(right, top - 1, right + bm, top, paint)

        // 左下角
        val bottom = (getHeight() - getBottomIndent()) * zoom + dy
        canvas.drawRect(left - 1, bottom, left, bottom + bm, paint)
        canvas.drawRect(left - bm, bottom, left, bottom + 1, paint)

        // 右下角
        canvas.drawRect(right, bottom, right + 1, bottom + bm, paint)
        canvas.drawRect(right, bottom, right + bm, bottom + 1, paint)
    }

    private fun drawShape(canvas: Canvas, originX: Int, originY: Int, zoom: Float, drawBehindDocShape: Boolean) {
        val shapeViews = shapeViews
        if (shapeViews == null || shapeViews.size == 0) {
            return
        }

        if (drawBehindDocShape) {
            //behind doc
            for (shape in shapeViews) {
                if (shape is ShapeView && shape.isBehindDoc()) {
                    shape.drawForWrap(canvas, originX, originY, zoom)
                } else if (shape is ObjView && shape.isBehindDoc()) {
                    shape.drawForWrap(canvas, originX, originY, zoom)
                }
            }
        } else {
            for (shape in shapeViews) {
                if (shape is ShapeView && !shape.isBehindDoc()) {
                    shape.drawForWrap(canvas, originX, originY, zoom)
                } else if (shape is ObjView && !shape.isBehindDoc()) {
                    shape.drawForWrap(canvas, originX, originY, zoom)
                }
            }
        }
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
     * 得到包括offset的指定视图
     */
    override fun getView(offset: Long, type: Int, isBack: Boolean): IView? {
        var view: IView? = child
        while (view != null && !view.contains(offset, isBack)) {
            view = view.getNextView()
        }
        if (view != null && view.getType().toInt() != type
            && view.getType() != WPViewConstant.TABLE_VIEW
        ) {
            return view.getView(offset, type, isBack)
        }
        return view
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
                if (vY >= view.getY() && vY < view.getY() + view.getHeight()) {
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

    /**
     * @return Returns the pageNumber.
     */
    fun getPageNumber(): Int {
        return pageNum
    }

    /**
     * @param pageNumber The pageNumber to set.
     */
    fun setPageNumber(pageNumber: Int) {
        this.pageNum = pageNumber
    }

    /**
     * @return Returns the header.
     */
    fun getHeader(): TitleView? {
        return headerView
    }

    /**
     * @param header The header to set.
     */
    fun setHeader(header: TitleView?) {
        this.headerView = header
    }

    /**
     * @return Returns the footer.
     */
    fun getFooter(): TitleView? {
        return footerView
    }

    /**
     * @param footer The footer to set.
     */
    fun setFooter(footer: TitleView?) {
        this.footerView = footer
    }

    /**
     * @return Returns the hasBreakTable.
     */
    fun isHasBreakTable(): Boolean {
        return breakTable
    }

    /**
     * @param hasBreakTable The hasBreakTable to set.
     */
    fun setHasBreakTable(hasBreakTable: Boolean) {
        this.breakTable = hasBreakTable
    }

    fun setPageBackgroundColor(color: Int) {
        this.pageBRColor = color
    }

    fun setPageBorder(border: Int) {
        this.pageBorderIndex = border
    }

    fun addShapeView(view: LeafView) {
        if (shapeViews == null) {
            shapeViews = ArrayList()
        }
        shapeViews!!.add(view)
    }

    /**
     * check header/footer has field text which need to be updated(eg.Total Page). if has , update it
     */
    fun checkUpdateHeaderFooterFieldText(totalPages: Int): Boolean {
        val hasTotalPageCode = checkUpdateHeaderFooterFieldText(headerView, totalPages)

        return hasTotalPageCode || checkUpdateHeaderFooterFieldText(footerView, totalPages)
    }

    /**
     * @return header/footer has total page number field code or not
     */
    private fun checkUpdateHeaderFooterFieldText(titleView: TitleView?, totalPages: Int): Boolean {
        var hasTotalPageCode = false
        if (titleView != null) {
            var paraView = titleView.getChildView()
            while (paraView != null) {
                var lineView = paraView.getChildView()
                while (lineView != null) {
                    var leafView = lineView.getChildView()
                    while (leafView != null) {
                        if (leafView is LeafView) {
                            if (leafView.hasUpdatedFieldText()) {
                                hasTotalPageCode = true
                                leafView.setNumPages(totalPages)
                            }
                        }

                        leafView = leafView.getNextView()
                    }

                    lineView = lineView.getNextView()
                }

                paraView = paraView.getNextView()
            }
        }

        return hasTotalPageCode
    }

    override fun dispose() {
        super.dispose()
        headerView?.dispose()
        headerView = null
        footerView?.dispose()
        footerView = null
        shapeViews?.clear()
        shapeViews = null
        paint = null
    }
}
