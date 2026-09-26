/*
 * 文件名称:          LineView.java
 *
 * 编译器:            android2.2
 * 时间:              上午10:35:04
 */
package com.wxiwei.office.wp.view

import android.graphics.Canvas
import android.graphics.Paint
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.constant.wp.WPAttrConstant
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.simpletext.control.IWord
import com.wxiwei.office.simpletext.model.IElement
import com.wxiwei.office.simpletext.view.AbstractView
import com.wxiwei.office.simpletext.view.DocAttr
import com.wxiwei.office.simpletext.view.IView
import com.wxiwei.office.simpletext.view.PageAttr
import com.wxiwei.office.simpletext.view.ParaAttr
import com.wxiwei.office.simpletext.view.ViewKit

/**
 * word 行视图
 */
class LineView : AbstractView {

    /**
     * line height except shape(autoshape and object)
     */
    private var heightExceptShape = 0

    constructor()

    constructor(elem: IElement) {
        this.elem = elem
    }

    override fun getType(): Short {
        return WPViewConstant.LINE_VIEW
    }

    /**
     * 对齐方式布局
     */
    fun layoutAlignment(docAttr: DocAttr?, pageAttr: PageAttr, paraAttr: ParaAttr, bnView: BNView?, span: Int, flag: Int) {
        layoutAlignment(docAttr, pageAttr, paraAttr, bnView, span, flag, true)
    }

    fun setHeightExceptShape(height: Int) {
        this.heightExceptShape = height
    }

    fun getHeightExceptShape(): Int {
        return heightExceptShape
    }

    /**
     * 对齐方式布局
     */
    fun layoutAlignment(docAttr: DocAttr?, pageAttr: PageAttr, paraAttr: ParaAttr, bnView: BNView?, span: Int, flag: Int, isLayoutVertical: Boolean) {
        // 水平
        if (!ViewKit.instance().getBitValue(flag, WPViewConstant.LAYOUT_NOT_WRAP_LINE.toInt())) {
            layoutHorizontal(docAttr, pageAttr, paraAttr, bnView, span, flag)
        }
        // 垂直
        if (isLayoutVertical) {
            layoutVertical(docAttr, pageAttr, paraAttr, bnView, span, flag)
        }
    }

    /**
     * 水平对齐布局
     *
     * @param docAttr   文档属性
     * @param pageAttr  页面属性
     * @param paraAttr  段落属性
     * @param span      行布局宽度
     * @param flag      布局标记
     */
    fun layoutHorizontal(docAttr: DocAttr?, pageAttr: PageAttr?, paraAttr: ParaAttr, bnView: BNView?, span: Int, flag: Int) {
        // 水平对齐
        when (paraAttr.horizontalAlignment) {
            WPAttrConstant.PARA_HOR_ALIGN_CENTER -> // 居中
                x += (span - width) / 2
            WPAttrConstant.PARA_HOR_ALIGN_RIGHT -> // 居右
                x += (span - width)
            else -> {
            }
        }
    }

    /**
     * 垂直对齐布局
     *
     * @param docAttr   文档属性
     * @param pageAttr  页面属性
     * @param paraAttr  段落属性
     * @param span      行布局宽度
     * @param flag      布局标记
     */
    private fun layoutVertical(docAttr: DocAttr?, pageAttr: PageAttr, paraAttr: ParaAttr, bnView: BNView?, span: Int, flag: Int) {
        var leaf = getChildView() as LeafView? ?: return
        var maxBaseline = bnView?.getBaseline() ?: 0
        var temp: LeafView? = leaf
        while (temp != null) {
            maxBaseline = Math.max(maxBaseline, temp.getBaseline())
            temp = temp.getNextView() as LeafView?
        }
        // 基线对齐
        var leaf2 = getChildView() as LeafView?
        while (leaf2 != null) {
            if (leaf2.getType() == WPViewConstant.SHAPE_VIEW) {
                if (!(leaf2 as ShapeView).isInline()) {
                    leaf2 = leaf2.getNextView() as LeafView?
                    continue
                }
            } else if (leaf2.getType() == WPViewConstant.OBJ_VIEW) {
                if (!(leaf2 as ObjView).isInline()) {
                    leaf2 = leaf2.getNextView() as LeafView?
                    continue
                }
            }

            val baseline = maxBaseline - leaf2.getBaseline()
            leaf2.setTopIndent(baseline)
            leaf2.setY(leaf2.getY() + baseline)
            leaf2 = leaf2.getNextView() as LeafView?
        }
        var value = 0f
        var processline = false
        // 行距
        when (paraAttr.lineSpaceType) {
            WPAttrConstant.LINE_SPACE_SINGLE, // 单倍行距
            WPAttrConstant.LINE_SPACE_ONE_HALF, // 1.5倍行距
            WPAttrConstant.LINE_SPACE_DOUBLE, // 2倍行距
            WPAttrConstant.LINE_SAPCE_MULTIPLE -> { // 多倍行距
                processline = true
                if (pageAttr.pageLinePitch > 0) {
                    if (heightExceptShape > pageAttr.pageLinePitch * paraAttr.lineSpaceValue) {
                        value = (Math.ceil((heightExceptShape / pageAttr.pageLinePitch).toDouble()) * pageAttr.pageLinePitch).toFloat()
                    } else {
                        value = pageAttr.pageLinePitch * paraAttr.lineSpaceValue
                    }
                } else {
                    value = paraAttr.lineSpaceValue * heightExceptShape
                }
            }

            WPAttrConstant.LINE_SAPCE_LEAST -> { // 最小行距
                processline = true
                if (paraAttr.lineSpaceValue > heightExceptShape) {
                    processline = true
                    if (pageAttr.pageLinePitch > 0) {
                        value = Math.max(paraAttr.lineSpaceValue, pageAttr.pageLinePitch)
                    } else {
                        value = paraAttr.lineSpaceValue
                    }
                } else {
                    if (pageAttr.pageLinePitch > 0) {
                        processline = true
                        value = (Math.ceil((heightExceptShape / pageAttr.pageLinePitch).toDouble()) * pageAttr.pageLinePitch).toFloat()
                    } else {
                        value = heightExceptShape.toFloat()
                    }
                }
            }

            else -> {
            }
        }

        if (processline) {
            value = (value - heightExceptShape) / 2
            setTopIndent(value.toInt())
            setBottomIndent(value.toInt())
            setY(getY() + value.toInt())
            if (bnView != null) {
                bnView.setTopIndent(value.toInt())
                bnView.setBottomIndent(value.toInt())
                bnView.setY(value.toInt())
            }
        }
    }

    /**
     * model到视图
     * @param offset 指定的offset
     * @param isBack 是否向后取，是为在视图上，上一行的结束位置与下一行开始位置相同
     */
    override fun modelToView(offset: Long, rect: Rectangle, isBack: Boolean): Rectangle {
        val view = getView(offset, WPViewConstant.LEAF_VIEW.toInt(), isBack)
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
        var view: IView? = getView(vX, vY, WPViewConstant.LEAF_VIEW.toInt(), isBack)
        if (view == null) {
            view = if (vX > getWidth()) {
                getLastView()
            } else {
                getChildView()
            }
        }
        if (view != null) {
            return view.viewToModel(vX, vY, isBack)
        }
        return -1
    }

    override fun draw(canvas: Canvas, originX: Int, originY: Int, zoom: Float) {
        canvas.save()
        val word = getContainer() as IWord?
        val dX = (x * zoom).toInt() + originX
        val dY = (y * zoom).toInt() + originY
        var view = getChildView()
        val clip = canvas.getClipBounds()
        if (getTopIndent() < 0
            && word != null && word.getEditType() == MainConstant.APPLICATION_TYPE_WP
        ) {
            canvas.clipRect(dX.toFloat(), dY - getTopIndent() * zoom, dX + getLayoutSpan(WPViewConstant.X_AXIS) * zoom,
                dY - getTopIndent() * zoom + getLayoutSpan(WPViewConstant.Y_AXIS) * zoom)
        }
        while (view != null) {
            if (view.intersection(clip, dX, dY, zoom)) {
                view.draw(canvas, dX, dY, zoom)
            }
            view = view.getNextView()
        }
        canvas.restore()
        // draw underline
        drawUnderline(canvas, originX, originY, zoom)
        // 绘制高亮
        if (word != null && word.getHighlight() != null) {
            word.getHighlight()!!.draw(canvas, this, dX, dY, getStartOffset(null), getEndOffset(null), zoom)
        }
    }

    /**
     * draw underline
     */
    private fun drawUnderline(canvas: Canvas, originX: Int, originY: Int, zoom: Float) {
        var underlinePaint: Paint? = null
        var dX = (x * zoom).toInt() + originX
        val dY = (y * zoom + originY + getTopIndent() * zoom).toInt()
        var leaf = getChildView() as LeafView?
        var w = 0
        var color = Integer.MAX_VALUE
        var baseline = 0
        while (leaf != null) {
            val charAttr = leaf.getCharAttr()
            if (charAttr == null) {
                leaf = leaf.getNextView() as LeafView?
                continue
            }
            if (charAttr.underlineType > 0) {
                // difference color
                if (color != Integer.MAX_VALUE && color != charAttr.underlineColor) {
                    //draw last underline
                    val paint = underlinePaint ?: Paint().also { underlinePaint = it }
                    paint.color = color
                    canvas.drawRect(dX.toFloat(), (dY + baseline + 1).toFloat(), (dX + w).toFloat(), (dY + baseline + 2).toFloat(), paint)
                    dX += w

                    //new underline begin
                    color = charAttr.underlineColor
                    w = 0
                    baseline = 0
                } else if (color == Integer.MAX_VALUE) {
                    color = charAttr.underlineColor
                }
                w += (leaf.getWidth() * zoom).toInt()
                baseline = Math.max(baseline, (leaf.getUnderlinePosition() * zoom).toInt())
            } else {
                if (color != Integer.MAX_VALUE) {
                    //draw last underline
                    val paint = underlinePaint ?: Paint().also { underlinePaint = it }
                    paint.color = color
                    canvas.drawRect(dX.toFloat(), (dY + baseline + 1).toFloat(), (dX + w).toFloat(), (dY + baseline + 2).toFloat(), paint)

                    dX += w
                    w = 0
                    baseline = 0
                }
                dX += (leaf.getWidth() * zoom).toInt()
                color = Integer.MAX_VALUE
            }
            leaf = leaf.getNextView() as LeafView?
        }
        if (color != Integer.MAX_VALUE) {
            val paint = underlinePaint ?: Paint()
            paint.color = color
            canvas.drawRect(dX.toFloat(), (dY + baseline + 1).toFloat(), (dX + w).toFloat(), (dY + baseline + 2).toFloat(), paint)
        }
    }

    /**
     * 放回对象池
     */
    override fun free() {
    }

    override fun dispose() {
        super.dispose()
    }
}
