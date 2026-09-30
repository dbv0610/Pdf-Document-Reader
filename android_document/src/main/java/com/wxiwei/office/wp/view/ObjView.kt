/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          ObjView.java
 *
 * 编译器:            android2.2
 * 时间:              上午9:45:54
 */
package com.wxiwei.office.wp.view

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.wxiwei.office.common.BackgroundDrawer
import com.wxiwei.office.common.picture.PictureKit
import com.wxiwei.office.common.shape.PictureShape
import com.wxiwei.office.common.shape.WPAutoShape
import com.wxiwei.office.common.shape.WPPictureShape
import com.wxiwei.office.common.shape.WatermarkShape
import com.wxiwei.office.constant.wp.WPModelConstant
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.simpletext.model.IElement
import com.wxiwei.office.simpletext.view.DocAttr
import com.wxiwei.office.simpletext.view.PageAttr
import com.wxiwei.office.simpletext.view.ParaAttr
import com.wxiwei.office.simpletext.view.ViewKit

/**
 * Embedded picture view
 */
class ObjView : LeafView {

    private var pageAttr: PageAttr? = null

    // 字符属性
    private var picShape: WPAutoShape? = null

    fun getShape(): WPAutoShape? = picShape

    //
    private val rect = Rect()

    //
    private var isInlineFlag = false

    constructor()

    constructor(paraElem: IElement, elem: IElement, shape: WPAutoShape?) : super(paraElem, elem) {
        this.picShape = shape
    }

    override fun getType(): Short {
        return WPViewConstant.OBJ_VIEW
    }

    /**
     * 初始化leaf属性
     */
    override fun initProperty(elem: IElement, paraElem: IElement) {
        this.elem = elem
        paint = Paint()
        paint!!.flags = Paint.ANTI_ALIAS_FLAG
        paint!!.textSize = 20f
    }

    /**
     * 视图布局
     */
    override fun doLayout(docAttr: DocAttr?, pageAttr: PageAttr?, paraAttr: ParaAttr?, x: Int, y: Int, w: Int, h: Int, maxEnd: Long, flag: Int): Int {
        this.pageAttr = pageAttr
        val picShape = picShape!!

        isInlineFlag = docAttr!!.rootType.toInt() == WPViewConstant.NORMAL_ROOT.toInt()
                || (picShape.wrap.toInt() != com.wxiwei.office.common.shape.WPAbstractShape.WRAP_TOP.toInt() && picShape.wrap.toInt() != com.wxiwei.office.common.shape.WPAbstractShape.WRAP_BOTTOM.toInt())

        // Floating shapes of headers/footers stay floating (drawn on every page by PageView);
        // laying them out inline made a page-sized header background push the body off the page.
        if (picShape.isWatermarkShape) {
            isInlineFlag = false
        }
        var width = 0
        val r = requireNotNull(picShape.bounds)
        if (isInlineFlag) {
            width = r.width
            setSize(width, r.height)
        } else if (!picShape.isWatermarkShape) {
            PositionLayoutKit.instance().processShapePosition(this, picShape, pageAttr!!)
        }
        setEndOffset(start + 1)
        val keepOne = ViewKit.instance().getBitValue(flag, WPViewConstant.LAYOUT_FLAG_KEEPONE.toInt())
        var breakType = WPViewConstant.BREAK_NO.toInt()
        if (keepOne) {
            return breakType
        }
        if (width > w) {
            breakType = WPViewConstant.BREAK_LIMIT.toInt()
        }
        return breakType
    }

    /**
     * 得到指定结束位置字符宽度
     */
    override fun getTextWidth(): Float {
        val picShape = picShape!!
        return if (picShape.isWatermarkShape) {
            picShape.bounds!!.width.toFloat()
        } else {
            (if (isInlineFlag) inlineBounds()?.width ?: 0 else 0).toFloat()
        }
    }

    @Synchronized
    override fun draw(canvas: Canvas, originX: Int, originY: Int, zoom: Float) {
        if (isInlineFlag) {
            val picShape = picShape!!
            val control = getControl()
            val left = Math.round((x * zoom) + originX)
            val top = Math.round((y * zoom) + originY)
            val right = Math.round((x * zoom) + originX + getWidth() * zoom)
            val bottom = Math.round((y * zoom) + originY + getHeight() * zoom)

            rect.set(left, top, right, bottom)

            val picture = (picShape as? WPPictureShape)?.getPictureShape()
            if (!picShape.isWatermarkShape && picture != null) {
                BackgroundDrawer.drawLineAndFill(canvas, control, getPageNumber(), picture, rect, zoom)

                PictureKit.instance().drawPicture(canvas, control, getPageNumber(), picture.getPicture(getControl()),
                    left.toFloat(), top.toFloat(), zoom, getWidth() * zoom, getHeight() * zoom, picture.pictureEffectInfor)
            }
        }
    }

    @Synchronized
    fun drawForWrap(canvas: Canvas, originX: Int, originY: Int, zoom: Float) {
        val picShape = picShape!!
        val r = requireNotNull(picShape.bounds)
        val control = getControl()

        var left = Math.round((x * zoom) + originX)
        var top = Math.round((y * zoom) + originY)
        val right = Math.round((x * zoom) + originX + r.getWidth() * zoom).toInt()
        val bottom = Math.round((y * zoom) + originY + r.getHeight() * zoom).toInt()

        rect.set(left, top, right, bottom)

        if (picShape.isWatermarkShape) {
            val pageAttr = pageAttr!!
            val mainBodyWidth = pageAttr.pageWidth - pageAttr.leftMargin - pageAttr.rightMargin
            val mainBodyHeight = pageAttr.pageHeight - pageAttr.topMargin - pageAttr.bottomMargin

            val centerX = originX + (pageAttr.leftMargin + mainBodyWidth / 2f) * zoom
            val centerY = originY + (pageAttr.topMargin + mainBodyHeight / 2f) * zoom

            left = Math.round(centerX - r.width * zoom / 2f)
            top = Math.round(centerY - r.height * zoom / 2f)
            PictureKit.instance().drawPicture(canvas, control, getPageNumber(),
                PictureShape.getPicture(control, (picShape as WatermarkShape).pictureIndex),
                left.toFloat(),
                top.toFloat(),
                zoom,
                Math.round(r.getWidth() * zoom).toFloat(),
                Math.round(r.getHeight() * zoom).toFloat(),
                (picShape as WatermarkShape).effectInfor)
        } else {
            val picture = (picShape as? WPPictureShape)?.getPictureShape() ?: return
            BackgroundDrawer.drawLineAndFill(canvas, control, getPageNumber(), picture, rect, zoom)

            PictureKit.instance().drawPicture(canvas, control, getPageNumber(),
                picture.getPicture(getControl()),
                left.toFloat(),
                top.toFloat(),
                zoom,
                Math.round(r.getWidth() * zoom).toFloat(),
                Math.round(r.getHeight() * zoom).toFloat(),
                picture.pictureEffectInfor)
        }
    }

    /**
     * model到视图
     */
    override fun modelToView(offset: Long, rect: Rectangle, isBack: Boolean): Rectangle {
        rect.x += getX()
        rect.y += getY()
        return rect
    }

    override fun viewToModel(x: Int, y: Int, isBack: Boolean): Long {
        return start
    }

    fun isBehindDoc(): Boolean {
        return picShape!!.wrap.toInt() == com.wxiwei.office.common.shape.WPAbstractShape.WRAP_BOTTOM.toInt()
    }

    /** Size of an in-line object: its picture's, else the shape's own (a shape read without its picture). */
    private fun inlineBounds() = (picShape as? WPPictureShape)?.getPictureShape()?.bounds ?: picShape?.bounds

    /**
     * 得到基线
     */
    override fun getBaseline(): Int {
        if (!picShape!!.isWatermarkShape) {
            return if (isInlineFlag) inlineBounds()?.height ?: 0 else 0
        }

        return 0
    }

    fun isInline(): Boolean {
        return isInlineFlag
    }

    /**
     * 放回对象池
     */
    override fun free() {
    }

    override fun dispose() {
        super.dispose()
        picShape = null
    }
}
