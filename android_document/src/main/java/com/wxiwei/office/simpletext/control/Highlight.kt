/*
 * 文件名称:          Highlight.java
 *
 * 编译器:            android2.2
 * 时间:              下午1:27:42
 */
package com.wxiwei.office.simpletext.control

import android.graphics.Canvas
import android.graphics.Paint
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.simpletext.view.IView
import com.wxiwei.office.wp.view.WPViewKit

/**
 * highlight 高亮管理
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2012-2-28
 *
 * 负责人:          ljj8494
 */
class Highlight(word: IWord?) : IHighlight {
    //
    private var isPaintHighlight = true

    // select start offset
    private var selectStart: Long = 0

    // select end offset
    private var selectEnd: Long = 0

    //
    private var word: IWord? = word

    //
    private var paint: Paint? = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        paint!!.setColor(0xA0BBDDFF.toInt())
        //paint.setStyle(Style.FILL);
    }

    /**
     *
     */
    override fun draw(canvas: Canvas, line: IView, originX: Int, originY: Int, start: Long, end: Long, zoom: Float) {
        var start = start
        if (!isSelectText() || end <= selectStart || start > selectEnd || !isPaintHighlight) {
            return
        }
        start = Math.max(start, selectStart)
        var leaf: IView? = line.getView(start, WPViewConstant.LEAF_VIEW.toInt(), false)
        if (leaf == null) {
            return
        }

        val sRect = Rectangle()
        word!!.modelToView(start, sRect, false)

        var leafEnd = leaf.getEndOffset(null)
        val paintEnd = Math.min(end, selectEnd)

        var x = sRect.x
        var y = originY
        var w = leaf.getWidth()
        //
        if (start == selectStart) {
            val leafRect = WPViewKit.instance().getAbsoluteCoordinate(
                leaf,
                WPViewConstant.PAGE_ROOT.toInt(), Rectangle()
            )
            if (word!!.getEditType() == MainConstant.APPLICATION_TYPE_PPT
                && word!!.getTextBox() != null
            ) {
                leafRect.x += word!!.getTextBox()!!.getBounds().x
                leafRect.y += word!!.getTextBox()!!.getBounds().y
            }
            w -= (sRect.x - leafRect.x)
        }

        var h = line.getLayoutSpan(WPViewConstant.Y_AXIS)
        val parent = line.getParentView()
        if (parent != null) {
            // first line
            if (line.getPreView() == null) {
                y = (y - parent.getTopIndent() * zoom).toInt()
                h += parent.getTopIndent()
            }
            // last line
            if (line.getNextView() == null) {
                h += parent.getBottomIndent()
            }
        }

        while (leafEnd <= paintEnd) {
            canvas.drawRect(x * zoom, y.toFloat(), (x + w) * zoom, y + h * zoom, paint!!)
            x += w
            leaf = leaf!!.getNextView()
            if (leaf == null) {
                break
            }
            w = leaf.getWidth()
            leafEnd = leaf.getEndOffset(null)
        }

        // 绘制，选取范围最后部分
        if (end >= selectEnd) {
            val eRect = Rectangle()
            word!!.modelToView(selectEnd, eRect, false)
            if (eRect.x > x) {
                canvas.drawRect(x * zoom, y.toFloat(), eRect.x * zoom, y + h * zoom, paint!!)
            }
        }
    }

    /**
     *
     */
    override fun getSelectText(): String? {
        if (isSelectText()) {
            return word!!.getDocument()!!.getText(selectStart, selectEnd)
        }
        return ""
    }

    /**
     *
     */
    override fun isSelectText(): Boolean {
        return selectStart != selectEnd
    }

    /**
     *
     */
    override fun removeHighlight() {
        this.selectStart = 0
        this.selectEnd = 0
    }

    /**
     *
     */
    override fun addHighlight(start: Long, end: Long) {
        this.selectStart = start
        this.selectEnd = end
    }

    /**
     *
     */
    override fun getSelectStart(): Long {
        return selectStart
    }

    /**
     *
     */
    override fun setSelectStart(selectStart: Long) {
        this.selectStart = selectStart
    }

    /**
     *
     */
    override fun getSelectEnd(): Long {
        return selectEnd
    }

    /**
     *
     */
    override fun setSelectEnd(selectEnd: Long) {
        this.selectEnd = selectEnd
    }

    /**
     * @param isPaintHighlight The isPaintHighlight to set.
     */
    override fun setPaintHighlight(isPaintHighlight: Boolean) {
        this.isPaintHighlight = isPaintHighlight
    }

    /**
     *
     */
    override fun dispose() {
        word = null
        paint = null
    }
}
