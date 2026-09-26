/*
 * 文件名称:          	EcloseCharacterView.java
 *
 * 编译器:            android2.2
 * 时间:             	上午10:54:01
 */
package com.wxiwei.office.wp.view

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.Path
import android.graphics.RectF
import com.wxiwei.office.constant.wp.WPModelConstant
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.simpletext.model.IElement

/**
 * enclose character view
 */
class EncloseCharacterView : LeafView {

    //
    protected var enclosePaint: Paint? = null

    //
    protected var path: Path? = null

    constructor()

    constructor(paraElem: IElement, elem: IElement) : super(paraElem, elem)

    override fun getType(): Short {
        return WPViewConstant.ENCLOSE_CHARACTER_VIEW
    }

    /**
     * 初始化leaf属性
     */
    override fun initProperty(elem: IElement, paraElem: IElement) {
        super.initProperty(elem, paraElem)
        enclosePaint = Paint()
        enclosePaint!!.color = charAttr!!.fontColor
        enclosePaint!!.style = Style.STROKE
        enclosePaint!!.isAntiAlias = true
        path = Path()
    }

    override fun draw(canvas: Canvas, originX: Int, originY: Int, zoom: Float) {
        super.draw(canvas, originX, originY, zoom)
        // 画圈
        drawEnclose(canvas, originX, originY, zoom)
    }

    private fun drawEnclose(canvas: Canvas, originX: Int, originY: Int, zoom: Float) {
        val dX = (x * zoom).toInt() + originX
        val dY = (y * zoom).toInt() + originY
        val w = (getWidth() * zoom).toInt()
        val h = (getHeight() * zoom).toInt()
        val enclosePaint = enclosePaint!!
        val encloseType = charAttr!!.encloseType
        // 圆
        if (encloseType == WPModelConstant.ENCLOSURE_TYPE_ROUND) {
            canvas.drawArc(RectF(dX.toFloat(), dY.toFloat(), (dX + w).toFloat(), (dY + h).toFloat()), 0f, 360f, false, enclosePaint)
        }
        // 正方形
        else if (encloseType == WPModelConstant.ENCLOSURE_TYPE_SQUARE) {
            canvas.drawRect(dX.toFloat(), dY.toFloat(), (dX + w).toFloat(), (dY + h).toFloat(), enclosePaint)
        }
        // 三角形
        else if (encloseType == WPModelConstant.ENCLOSURE_TYPE_TRIANGLE) {
            val path = path!!
            path.reset()
            path.moveTo((dX + w / 2).toFloat(), dY.toFloat())
            path.lineTo(dX.toFloat(), (dY + h).toFloat())
            path.lineTo((dX + w).toFloat(), (dY + h).toFloat())
            path.close()
            canvas.drawPath(path, enclosePaint)
        }
        // 菱形
        else if (encloseType == WPModelConstant.ENCLOSURE_TYPE_RHOMBUS) {
            val path = path!!
            path.reset()
            path.moveTo((dX + w / 2).toFloat(), dY.toFloat())
            path.lineTo(dX.toFloat(), (dY + h / 2).toFloat())
            path.lineTo((dX + w / 2).toFloat(), (dY + h).toFloat())
            path.lineTo((dX + w).toFloat(), (dY + h / 2).toFloat())
            path.close()
            canvas.drawPath(path, enclosePaint)
        }
    }

    /**
     * 放回对象池
     */
    override fun free() {
    }

    override fun dispose() {
        super.dispose()
        paint = null
    }
}
