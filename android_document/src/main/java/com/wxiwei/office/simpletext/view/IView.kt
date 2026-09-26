/*
 * 文件名称:          IView.java
 *
 * 编译器:            android2.2
 * 时间:              下午3:16:53
 */
package com.wxiwei.office.simpletext.view

import android.graphics.Canvas
import android.graphics.Rect
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.simpletext.control.IWord
import com.wxiwei.office.simpletext.model.IDocument
import com.wxiwei.office.simpletext.model.IElement
import com.wxiwei.office.system.IControl

/**
 * 视图接口
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2011-11-11
 *
 * 负责人:          ljj8494
 */
interface IView {
    /**
     * 得到视图对应Model的Element
     */
    fun getElement(): IElement?

    /**
     *
     */
    fun setElement(elem: IElement?)

    /**
     * 视图类型，页、栏、段落、行、leaf等
     */
    fun getType(): Short

    /**
     * x值，相对父视图
     */
    fun setX(x: Int)

    fun getX(): Int

    /**
     * y值, 相对父视图
     */
    fun setY(y: Int)

    fun getY(): Int

    /**
     * 宽度
     */
    fun setWidth(w: Int)

    fun getWidth(): Int

    /**
     * 高度
     */
    fun setHeight(h: Int)

    fun getHeight(): Int

    /**
     * 设置大小
     */
    fun setSize(w: Int, h: Int)

    /**
     * 设置位置
     */
    fun setLocation(x: Int, y: Int)

    /**
     * 设置范围
     */
    fun setBound(x: Int, y: Int, w: Int, h: Int)

    /**
     * 上边距
     */
    fun setTopIndent(top: Int)

    fun getTopIndent(): Int

    /**
     * 下边距
     */
    fun setBottomIndent(bottom: Int)

    fun getBottomIndent(): Int

    /**
     * 左边距
     */
    fun setLeftIndent(left: Int)

    fun getLeftIndent(): Int

    /**
     * 左边距
     */
    fun setRightIndent(right: Int)

    fun getRightIndent(): Int

    /**
     * 设置边距
     */
    fun setIndent(left: Int, top: Int, right: Int, bottom: Int)

    /**
     * 得到组件
     */
    fun getContainer(): IWord?

    /**
     *
     */
    fun getControl(): IControl?

    /**
     * 得到model
     */
    fun getDocument(): IDocument?

    /**
     * 第一个子视图
     */
    fun setChildView(view: IView?)

    fun getChildView(): IView?

    /**
     * 父视图
     */
    fun setParentView(view: IView?)

    fun getParentView(): IView?

    /**
     * 前一个视图
     */
    fun setPreView(view: IView?)

    fun getPreView(): IView?

    /**
     * 后一个视图
     */
    fun setNextView(view: IView?)

    fun getNextView(): IView?

    /**
     * 得到最后一个子视图，此方法尽量少用，非常耗时
     */
    fun getLastView(): IView?

    /**
     * 得到子视图数量，此方法尽量少调用，非常耗时
     */
    fun getChildCount(): Int

    /**
     * 追加一个子视图
     */
    fun appendChlidView(view: IView?)

    /**
     * 指定视图后面插入一个子视图
     *
     * @param view 指定视图，如果 = null, 则把newView设置成第一个视图
     * @param newView 要插入的视图
     */
    fun insertView(view: IView?, newView: IView?)

    /**
     * 删除一个指定视图
     *
     * @param idDeleteChlid = ture，则连子视图也删除。
     */
    fun deleteView(view: IView?, isDeleteChild: Boolean)

    /**
     * 视图开始位置
     */
    fun setStartOffset(start: Long)

    fun getStartOffset(doc: IDocument?): Long

    /**
     * 视图结束位置
     * @param end 相对于model的element开始位置的
     */
    fun setEndOffset(end: Long)

    fun getEndOffset(doc: IDocument?): Long

    /**
     * 得到Element开始位置
     */
    fun getElemStart(doc: IDocument?): Long

    /**
     * 得到Element结束位置
     */
    fun getElemEnd(doc: IDocument?): Long

    /**
     * get view rect
     * @param originX
     * @param originY
     * @param zoom
     * @return
     */
    fun getViewRect(originX: Int, originY: Int, zoom: Float): Rect

    /**
     * 视图是否在指定区间内
     *
     * @param rect
     */
    fun intersection(rect: Rect, originX: Int, originY: Int, zoom: Float): Boolean

    /**
     * 视图是否包含指定 offset
     *
     * @param offset 指定的offset
     * @param isBack 是否向后取，是为在视图上，上一行的结束位置与下一行开始位置相同
     */
    fun contains(offset: Long, isBack: Boolean): Boolean

    /**
     * 视图是否包含指定 x，y值
     * @param x
     * @param y
     * @param isBack
     * @return
     */
    fun contains(x: Int, y: Int, isBack: Boolean): Boolean

    /**
     * model到视图
     * @param offset 指定的offset
     * @param isBack 是否向后取，是为在视图上，上一行的结束位置与下一行开始位置相同
     */
    fun modelToView(offset: Long, rect: Rectangle, isBack: Boolean): Rectangle?

    /**
     * @param x
     * @param y
     * @param isBack 是否向后取，是为在视图上，上一行的结束位置与下一行开始位置相同
     */
    fun viewToModel(x: Int, y: Int, isBack: Boolean): Long

    /**
     * 得到下一个Offset位置，根据坐标
     *
     * @param offset 当前Offset
     * @param dir 方向，上、下、左、右
     * @param x
     * @param y
     */
    fun getNextForCoordinate(offset: Long, dir: Int, x: Int, y: Int): Long

    /**
     * 得到下一个Offset位置，根据Offset
     * @param offset 当前Offset
     * @param dir 方向，上、下、左、右
     * @param x
     * @param y
     */
    fun getNextForOffset(offset: Long, dir: Int, x: Int, y: Int): Long

    /**
     *
     * @param canvas
     * @param originX
     * @param originY
     * @param zoom
     */
    fun draw(canvas: Canvas, originX: Int, originY: Int, zoom: Float)

    /**
     * 视图布局
     * @param x
     * @param y
     * @param w
     * @param h
     * @param maxEnd
     * @param flag 布局标记，传递一些布尔值，位操作
     */
    fun doLayout(x: Int, y: Int, w: Int, h: Int, maxEnd: Long, flag: Int): Int

    /**
     * 得视图布局大小，包括上下左右Indent
     *
     * @param axis 方向
     * @see WPViewConstant.X_AXIS
     * @see WPViewConstant.Y_AXIS
     */
    fun getLayoutSpan(axis: Byte): Int

    /**
     * 得到包括offset的指定视图
     *
     * @param offset
     * @param type
     * @param isBack
     * @return
     */
    fun getView(offset: Long, type: Int, isBack: Boolean): IView?

    /**
     * 得到包括x, y值的指定视图
     */
    fun getView(x: Int, y: Int, type: Int, isBack: Boolean): IView?

    /**
     * 释放内存
     */
    fun dispose()

    /**
     *
     */
    fun free()
}
