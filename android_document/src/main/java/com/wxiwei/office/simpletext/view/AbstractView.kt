/*
 * 文件名称:          AbstractView.java
 *
 * 编译器:            android2.2
 * 时间:              下午4:36:32
 */
package com.wxiwei.office.simpletext.view

import android.graphics.Canvas
import android.graphics.Rect
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.simpletext.control.IWord
import com.wxiwei.office.simpletext.model.IDocument
import com.wxiwei.office.simpletext.model.IElement
import com.wxiwei.office.system.IControl

/**
 * 视图实现的抽象类
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2011-11-11
 *
 * 负责人:          ljj8494
 */
abstract class AbstractView : IView {
    // Model的Element
    @JvmField
    protected var elem: IElement? = null

    // x值
    @JvmField
    protected var x = 0

    // y值
    @JvmField
    protected var y = 0

    // 高度
    @JvmField
    protected var width = 0

    // 宽度
    @JvmField
    protected var height = 0

    // 上边距
    @JvmField
    protected var topIndent = 0

    // 下边距
    @JvmField
    protected var bottomIndent = 0

    // 左边距
    @JvmField
    protected var leftIndent = 0

    // 右边距
    @JvmField
    protected var rightIndent = 0

    // 视图开始Offset，相对model的Element
    @JvmField
    protected var start: Long = 0

    // 视图结束Offset，相对Model的Element
    @JvmField
    protected var end: Long = 0

    // 父视图
    @JvmField
    protected var parent: IView? = null

    // 子视图
    @JvmField
    protected var child: IView? = null

    // 前一个视图
    @JvmField
    protected var preView: IView? = null

    // 后一个视图
    @JvmField
    protected var nextView: IView? = null

    /**
     *
     */
    override fun getElement(): IElement? {
        return this.elem
    }

    /**
     *
     */
    override fun setElement(elem: IElement?) {
        this.elem = elem
    }

    /**
     * 视图类型，页、栏、段落、行、leaf等
     */
    override fun getType(): Short {
        return -1
    }

    /**
     * @return Returns the x.
     */
    override fun getX(): Int {
        return x
    }

    /**
     * @param x The x to set.
     */
    override fun setX(x: Int) {
        this.x = x
    }

    /**
     * @return Returns the y.
     */
    override fun getY(): Int {
        return y
    }

    /**
     * @param y The y to set.
     */
    override fun setY(y: Int) {
        this.y = y
    }

    /**
     * @return Returns the width.
     */
    override fun getWidth(): Int {
        return width
    }

    /**
     * @param width The width to set.
     */
    override fun setWidth(width: Int) {
        this.width = width
    }

    /**
     * @return Returns the height.
     */
    override fun getHeight(): Int {
        return height
    }

    /**
     * @param height The height to set.
     */
    override fun setHeight(height: Int) {
        this.height = height
    }

    /**
     * @return Returns the start.
     */
    override fun getStartOffset(doc: IDocument?): Long {
        return start
    }

    /**
     * @param start The start to set.
     */
    override fun setStartOffset(start: Long) {
        this.start = start
    }

    /**
     *
     */
    override fun getElemStart(doc: IDocument?): Long {
        return elem!!.getStartOffset()
    }

    /**
     * @return Returns the end.
     */
    override fun getEndOffset(doc: IDocument?): Long {
        return end
    }

    /**
     *
     */
    override fun getElemEnd(doc: IDocument?): Long {
        return elem!!.getEndOffset()
    }

    /**
     * @return Returns the parent.
     */
    override fun getParentView(): IView? {
        return parent
    }

    /**
     * @param parent The parent to set.
     */
    override fun setParentView(parent: IView?) {
        this.parent = parent
    }

    /**
     * @return Returns the child.
     */
    override fun getChildView(): IView? {
        return child
    }

    /**
     * @param child The child to set.
     */
    override fun setChildView(child: IView?) {
        this.child = child
    }

    /**
     * @return Returns the preView.
     */
    override fun getPreView(): IView? {
        return preView
    }

    /**
     * @param preView The preView to set.
     */
    override fun setPreView(preView: IView?) {
        this.preView = preView
    }

    /**
     * @return Returns the nextView.
     */
    override fun getNextView(): IView? {
        return nextView
    }

    /**
     * @param nextView The nextView to set.
     */
    override fun setNextView(nextView: IView?) {
        this.nextView = nextView
    }

    /**
     * 设置大小
     */
    override fun setSize(w: Int, h: Int) {
        this.width = w
        this.height = h
    }

    /**
     * 设置位置
     */
    override fun setLocation(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    /**
     * 设置范围
     */
    override fun setBound(x: Int, y: Int, w: Int, h: Int) {
        this.x = x
        this.y = y
        this.width = w
        this.height = y
    }

    /**
     * 得到组件
     */
    override fun getContainer(): IWord? {
        val parent = getParentView()
        if (parent != null) {
            return parent.getContainer()
        }
        return null
    }

    /**
     * 得到组件
     */
    override fun getControl(): IControl? {
        val parent = getParentView()
        if (parent != null) {
            return parent.getControl()
        }
        return null
    }

    /**
     * 得到model
     */
    override fun getDocument(): IDocument? {
        val parent = getParentView()
        if (parent != null) {
            return parent.getDocument()
        }
        return null
    }

    /**
     * 得到最后一个子视图，此方法尽量少用，非常耗时
     */
    override fun getLastView(): IView? {
        var temp = getChildView()
        if (temp == null) {
            return null
        }
        while (temp!!.getNextView() != null) {
            temp = temp.getNextView()
        }
        return temp
    }

    /**
     * 得到子视图数量，此方法尽量少调用，非常耗时
     */
    override fun getChildCount(): Int {
        var count = 0
        var temp = getChildView()
        while (temp != null) {
            count++
            temp = temp.getNextView()
        }
        return count
    }

    /**
     * 追加一个子视图
     */
    override fun appendChlidView(view: IView?) {
        view!!.setParentView(this)
        if (child == null) {
            child = view
            return
        }
        val lastView = getLastView()
        view.setPreView(lastView)
        lastView!!.setNextView(view)
    }

    /**
     * 指定视图后面插入一个子视图
     *
     * @param view 指定视图，如果 = null, 则把newView设置成第一个子视图
     * @param newView 要插入的视图
     */
    override fun insertView(view: IView?, newView: IView?) {
        newView!!.setParentView(this)
        // newView设置第一个子图
        if (view == null) {
            if (child == null) {
                child = newView
            } else {
                newView.setNextView(child)
                child!!.setPreView(newView)
                child = newView
            }
        }
    }

    /**
     * 删除一个指定视图
     *
     * @param idDeleteChlid = ture，则连子视图也删除。
     */
    override fun deleteView(view: IView?, isDeleteChild: Boolean) {
        view!!.setParentView(null)
        if (view === child) {
            child = null
        } else {
            val pre = view.getPreView()
            val next = view.getNextView()
            pre!!.setNextView(next)
            if (next != null) {
                next.setPreView(pre)
            }
        }
        if (isDeleteChild) {
            view.dispose()
        }
    }

    /**
     * 视图结束位置
     * @param end 相对于model的element开始位置的
     */
    override fun setEndOffset(end: Long) {
        this.end = end
    }

    /**
     * get view rect
     * @param originX
     * @param originY
     * @param zoom
     * @return
     */
    override fun getViewRect(originX: Int, originY: Int, zoom: Float): Rect {
        val tw = (getLayoutSpan(WPViewConstant.X_AXIS) * zoom).toInt()
        val th = (getLayoutSpan(WPViewConstant.Y_AXIS) * zoom).toInt()

        val tx = (x * zoom).toInt() + originX
        val ty = (y * zoom).toInt() + originY
        return Rect(tx, ty, tx + tw, ty + th)
    }

    /**
     * 视图是否在指定区相交
     *
     * @param rect
     */
    override fun intersection(rect: Rect, originX: Int, originY: Int, zoom: Float): Boolean {
        var tw = (getLayoutSpan(WPViewConstant.X_AXIS) * zoom).toInt()
        var th = (getLayoutSpan(WPViewConstant.Y_AXIS) * zoom).toInt()
        var rw = rect.right - rect.left
        var rh = rect.bottom - rect.top
        if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0) {
            return false
        }
        val tx = (x * zoom).toInt() + originX
        val ty = (y * zoom).toInt() + originY
        val rx = rect.left
        val ry = rect.top
        rw += rx
        rh += ry
        tw += tx
        th += ty
        //      overflow || intersect
        return ((rw < rx || rw > tx)
                && (rh < ry || rh > ty)
                && (tw < tx || tw > rx)
                && (th < ty || th > ry))
    }

    /**
     * 视图是否包含指定 offset
     *
     * @param offset 指定的offset
     * @param isBack 是否向后取，是为在视图上，上一行的结束位置与下一行开始位置相同
     */
    override fun contains(offset: Long, isBack: Boolean): Boolean {
        val doc = getDocument()
        val start = getStartOffset(doc)
        val end = getEndOffset(doc)
        return offset >= start && (offset < end || (offset == end && isBack))
    }

    /**
     * 视图是否包含指定 x，y值
     * @param x
     * @param y
     * @param isBack
     * @return
     */
    override fun contains(x: Int, y: Int, isBack: Boolean): Boolean {
        return x >= this.x && x < this.x + this.width
                && y >= this.y && y < this.y + getHeight()
    }

    /**
     * model到视图
     * @param offset 指定的offset
     * @param isBack 是否向后取，是为在视图上，上一行的结束位置与下一行开始位置相同
     */
    override fun modelToView(offset: Long, rect: Rectangle, isBack: Boolean): Rectangle? {
        return null
    }

    /**
     * @param x
     * @param y
     * @param isBack 是否向后取，是为在视图上，上一行的结束位置与下一行开始位置相同
     */
    override fun viewToModel(x: Int, y: Int, isBack: Boolean): Long {
        return 0
    }

    /**
     * 得到下一个Offset位置，根据坐标
     *
     * @param offset 当前Offset
     * @param dir 方向，上、下、左、右
     * @param x
     * @param y
     */
    override fun getNextForCoordinate(offset: Long, dir: Int, x: Int, y: Int): Long {
        return 0
    }

    /**
     * 得到下一个Offset位置，根据Offset
     * @param offset 当前Offset
     * @param dir 方向，上、下、左、右
     * @param x
     * @param y
     */
    override fun getNextForOffset(offset: Long, dir: Int, x: Int, y: Int): Long {
        return 0
    }

    /**
     * 视图布局
     * @param x
     * @param y
     * @param w
     * @param h
     * @param maxEnd
     * @param flag 布局标记，传递一些布尔值，位操作
     */
    override fun doLayout(x: Int, y: Int, w: Int, h: Int, maxEnd: Long, flag: Int): Int {
        return WPViewConstant.BREAK_NO.toInt()
    }

    /**
     *
     * @param canvas
     * @param x
     * @param y
     * @param zoom
     */
    override fun draw(canvas: Canvas, originX: Int, originY: Int, zoom: Float) {
        val dX = (x * zoom).toInt() + originX
        val dY = (y * zoom).toInt() + originY
        var view = getChildView()
        val clip = canvas.getClipBounds()
        while (view != null) {
            if (view.intersection(clip, dX, dY, zoom)) {
                view.draw(canvas, dX, dY, zoom)
            }
            view = view.getNextView()
        }
    }

    open fun getLineHeight(linesHeight: MutableList<Int>) {
        var view = getChildView()
        if (view != null) {
            while (view != null) {
                linesHeight.add(view.getHeight())
                view = view.getNextView()
            }
        }
    }

    /**
     * @return Returns the topIndent.
     */
    override fun getTopIndent(): Int {
        return topIndent
    }

    /**
     * @param topIndent The topIndent to set.
     */
    override fun setTopIndent(topIndent: Int) {
        this.topIndent = topIndent
    }

    /**
     * @return Returns the bottomIndent.
     */
    override fun getBottomIndent(): Int {
        return bottomIndent
    }

    /**
     * @param bottomIndent The bottomIndent to set.
     */
    override fun setBottomIndent(bottomIndent: Int) {
        this.bottomIndent = bottomIndent
    }

    /**
     * @return Returns the leftIndentt.
     */
    override fun getLeftIndent(): Int {
        return leftIndent
    }

    /**
     * @param leftIndentt The leftIndentt to set.
     */
    override fun setLeftIndent(leftIndent: Int) {
        this.leftIndent = leftIndent
    }

    /**
     * @return Returns the rightIndent.
     */
    override fun getRightIndent(): Int {
        return rightIndent
    }

    /**
     * @param rightIndent The rightIndent to set.
     */
    override fun setRightIndent(rightIndent: Int) {
        this.rightIndent = rightIndent
    }

    /**
     * 得视图布局大小，包括上下左右Indent
     *
     * @param axis 方向
     * @see WPViewConstant.X_AXIS
     * @see WPViewConstant.Y_AXIS
     */
    override fun getLayoutSpan(axis: Byte): Int {
        // 宽度
        if (axis == WPViewConstant.X_AXIS) {
            return rightIndent + width + leftIndent
        }
        //
        else {
            return topIndent + getHeight() + bottomIndent
        }
    }

    /**
     * 设置边距
     */
    override fun setIndent(left: Int, top: Int, right: Int, bottom: Int) {
        this.leftIndent = left
        this.topIndent = top
        this.rightIndent = right
        this.bottomIndent = bottom
    }

    /**
     * 得到包括offset的指定视图
     *
     * @param offset
     * @param type
     * @param isBack
     * @return
     */
    override fun getView(offset: Long, type: Int, isBack: Boolean): IView? {
        var view = child
        while (view != null && !view.contains(offset, isBack)) {
            view = view.getNextView()
        }
        if (view != null && view.getType().toInt() != type) {
            return view.getView(offset, type, isBack)
        }
        return view
    }

    /**
     * 得到包括x, y值的指定视图
     */
    override fun getView(x: Int, y: Int, type: Int, isBack: Boolean): IView? {
        var x = x
        var y = y
        var view = child
        while (view != null && !view.contains(x, y, isBack)) {
            view = view.getNextView()
        }
        if (view != null && view.getType().toInt() != type) {
            x -= this.x
            y -= this.y
            return view.getView(x, y, type, isBack)
        }
        return view
    }

    /**
     * 释放内存
     */
    override fun dispose() {
        this.parent = null
        elem = null
        var temp = child
        var next: IView?
        while (temp != null) {
            next = temp.getNextView()
            temp.dispose()
            temp = next
        }
        this.preView = null
        this.nextView = null
        this.child = null
    }

    /**
     *
     */
    override fun free() {
    }
}
