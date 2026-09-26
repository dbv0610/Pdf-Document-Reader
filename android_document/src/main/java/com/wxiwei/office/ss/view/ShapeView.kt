/*
 * 文件名称:          ShapeView.java
 *
 * 编译器:            android2.2
 * 时间:              上午11:09:58
 */
package com.wxiwei.office.ss.view

import android.graphics.Canvas
import android.graphics.Rect
import com.wxiwei.office.common.BackgroundDrawer
import com.wxiwei.office.common.PaintKit
import com.wxiwei.office.common.autoshape.AutoShapeKit
import com.wxiwei.office.common.picture.PictureKit
import com.wxiwei.office.common.shape.AChart
import com.wxiwei.office.common.shape.AbstractShape
import com.wxiwei.office.common.shape.AutoShape
import com.wxiwei.office.common.shape.GroupShape
import com.wxiwei.office.common.shape.IShape
import com.wxiwei.office.common.shape.PictureShape
import com.wxiwei.office.common.shape.SmartArt
import com.wxiwei.office.common.shape.TextBox
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.IDocument
import com.wxiwei.office.simpletext.model.STDocument
import com.wxiwei.office.simpletext.view.STRoot
import com.wxiwei.office.system.IControl

/**
 * draw  shapes of sheet
 *
 * Read版本:        Read V1.0
 *
 * 作者:            jqin
 *
 * 日期:            2011-12-2
 *
 * 负责人:           jqin
 *
 * 负责小组:
 */
class ShapeView(sheetView: SheetView?) {
    //
    private var sheetView: SheetView? = sheetView

    //temp
    private var shapeRect: Rect? = null
    private var temRect: Rect? = null

    /**
     *
     * @param sheetView
     */
    init {
        //init temp rect
        shapeRect = Rect()
        temRect = Rect()
    }

    /**
     *
     * @param shapePostion
     * @return
     */
    fun panzoomViewRect(shapePostion: Rectangle, parent: IShape?) {
        val sheetView = this.sheetView!!
        val shapeRect = this.shapeRect!!
        val zoom = sheetView.getZoom()
        if (parent != null && parent is SmartArt) {
            shapeRect.left = Math.round((shapePostion.x) * zoom)
            shapeRect.right = Math.round((shapePostion.x + shapePostion.width) * zoom)
            shapeRect.top = Math.round((shapePostion.y) * zoom)
            shapeRect.bottom = Math.round((shapePostion.y + shapePostion.height) * zoom)
        } else {
            val x = sheetView.getRowHeaderWidth()
            val y = sheetView.getColumnHeaderHeight()
            val scrollX = sheetView.getScrollX()
            val scrollY = sheetView.getScrollY()

            shapeRect.left = x + Math.round((shapePostion.x - scrollX) * zoom)
            shapeRect.right = x + Math.round((shapePostion.x + shapePostion.width - scrollX) * zoom)
            shapeRect.top = y + Math.round((shapePostion.y - scrollY) * zoom)
            shapeRect.bottom = y + Math.round((shapePostion.y + shapePostion.height - scrollY) * zoom)
        }

        //same  to tempRect
        temRect!!.set(shapeRect.left, shapeRect.top, shapeRect.right, shapeRect.bottom)
    }

    /**
     * draw shapes of current sheet
     * @param canvas
     */
    fun draw(canvas: Canvas) {
        val sheetView = this.sheetView!!
        val clip = canvas.clipBounds
        clip.left = sheetView.getRowHeaderWidth()
        clip.top = sheetView.getColumnHeaderHeight()

        val cnt = sheetView.getCurrentSheet()!!.getShapeCount()
        val control = sheetView.getSpreadsheet()!!.getControl()
        var i = 0
        while (i < cnt && !sheetView.getSpreadsheet()!!.isAbortDrawing()) {
            val shape = sheetView.getCurrentSheet()!!.getShape(i)
            drawShape(canvas, clip, control, null, shape!!)
            i++
        }
    }

    /**
     *
     * @param canvas
     * @param clip
     * @param control
     * @param parent
     * @param shape
     */
    private fun drawShape(canvas: Canvas, clip: Rect, control: IControl, parent: IShape?, shape: IShape) {
        val sheetView = this.sheetView!!
        val shapeRect = this.shapeRect!!
        canvas.save()

        var bounds: Rectangle? = shape.getBounds()

        //chart sheet
        if (bounds == null && shape.getType() == AbstractShape.SHAPE_CHART) {
            val display = sheetView.getSpreadsheet()!!.getControl().getMainFrame()
                .getActivity().resources.displayMetrics
            val width = Math.max(display.widthPixels, display.heightPixels)
            val height = Math.min(display.widthPixels, display.heightPixels)
            bounds = Rectangle(0, 0, Math.round(width.toFloat()), Math.round(height.toFloat()))
            shape.setBounds(bounds)
        }

        //shape rect
        panzoomViewRect(bounds!!, parent)

        if (!temRect!!.intersect(clip) && parent == null) {
            canvas.restore()
            return
        }
        if (shape is GroupShape) {
            //flip vertical
            if (shape.getFlipVertical()) {
                canvas.translate(shapeRect.left.toFloat(), shapeRect.bottom.toFloat())
                canvas.scale(1f, -1f)
                canvas.translate(-shapeRect.left.toFloat(), -shapeRect.top.toFloat())
            }
            //flip horizontal
            if (shape.getFlipHorizontal()) {
                canvas.translate(shapeRect.right.toFloat(), shapeRect.top.toFloat())
                canvas.scale(-1f, 1f)
                canvas.translate(-shapeRect.left.toFloat(), -shapeRect.top.toFloat())
            }

            val shapes = shape.getShapes()
            for (i in shapes.indices) {
                val childShape = shapes[i]
                if (shape.isHidden()) {
                    continue
                }
                drawShape(canvas, clip, control, shape, childShape)
            }
        } else {
            when (shape.getType()) {
                AbstractShape.SHAPE_PICTURE -> {
                    val pictureShape = shape as PictureShape
                    processRotation(canvas, pictureShape, shapeRect)

                    BackgroundDrawer.drawLineAndFill(canvas, control, sheetView.getSheetIndex(), pictureShape, shapeRect, sheetView.getZoom())

                    val pic = control.getSysKit().getPictureManage().getPicture(shape.getPictureIndex())
                    PictureKit.instance().drawPicture(
                        canvas, sheetView.getSpreadsheet()!!.getControl(), sheetView.getSheetIndex(), pic, shapeRect.left.toFloat(), shapeRect.top.toFloat(),
                        sheetView.getZoom(), shapeRect.width().toFloat(), shapeRect.height().toFloat(), shape.getPictureEffectInfor()
                    )
                }

                AbstractShape.SHAPE_TEXTBOX -> drawTextbox(canvas, shapeRect, shape as TextBox)

                AbstractShape.SHAPE_CHART -> {
                    val achart = shape as AChart
                    if (achart.getAChart() != null) {
                        processRotation(canvas, shape, shapeRect)
                        achart.getAChart().setZoomRate(sheetView.getZoom()) //PictureKit.WMFZOOM
                        achart.getAChart().draw(canvas, control, shapeRect.left, shapeRect.top, shapeRect.width(), shapeRect.height(), PaintKit.instance().getPaint())
//                        PictureKit.instance().drawPicture(canvas, control,
//                            control.getSysKit().getPictureManage().getPicture(achart.getDrawingPicture(control)),
//                            shapeRect.left, shapeRect.top , sheetView.getZoom(), shapeRect.width(), shapeRect.height(), null);
                    }
                }

                AbstractShape.SHAPE_LINE, AbstractShape.SHAPE_AUTOSHAPE -> AutoShapeKit.instance().drawAutoShape(canvas, control, sheetView.getSheetIndex(), shape as AutoShape, shapeRect, sheetView.getZoom())

                AbstractShape.SHAPE_SMARTART -> {
                    val smartArt = shape as SmartArt
                    BackgroundDrawer.drawLineAndFill(canvas, control, sheetView.getSheetIndex(), smartArt, shapeRect, sheetView.getZoom())

                    canvas.translate(shapeRect.left.toFloat(), shapeRect.top.toFloat())

                    val shapes = smartArt.getShapes()
                    for (item in shapes) {
                        drawShape(canvas, clip, control, smartArt, item)
                    }
                }
            }
        }

        canvas.restore()
    }

    /**
     *
     * @param canvas
     * @param shapeRect
     * @param textboxData
     */
    private fun drawTextbox(canvas: Canvas, shapeRect: Rect, textbox: TextBox) {
        val elem = textbox.getElement()
        if (elem.getEndOffset() - elem.getStartOffset() == 0L) {
            return
        }
        if (textbox.isEditor()) {
            /*int left = (int)(rect.x * zoom);
            int top =  (int)(rect.y * zoom);
            int right = left + (int)(rect.width * zoom);
            int bottom = top + (int)(rect.height * zoom);
            Style s = paint.getStyle();
            paint.setStyle(Style.STROKE);
            canvas.drawRect(left, top, right, bottom, paint);
            paint.setStyle(s);*/
            return
        }

        processRotation(canvas, textbox, shapeRect)
        var root: STRoot? = textbox.getRootView()
        if (root == null) {
            val doc: IDocument = STDocument()
            doc.appendSection(elem)

            val attr = elem.getAttribute()
            // 宽度
            AttrManage.instance().setPageWidth(attr, Math.round(textbox.getBounds().getWidth() * MainConstant.PIXEL_TO_TWIPS).toInt())
            // 高度
            AttrManage.instance().setPageHeight(attr, Math.round(textbox.getBounds().getHeight() * MainConstant.PIXEL_TO_TWIPS).toInt())

            root = STRoot(sheetView!!.getSpreadsheet()!!.getEditor(), doc)
            root.setWrapLine(textbox.isWrapLine())
            root.doLayout()

            textbox.setRootView(root)
        }

        if (root != null) {
            root.draw(canvas, shapeRect.left, shapeRect.top, sheetView!!.getZoom())
        }
    }

    /**
     *
     * @param canvas
     * @param shape
     * @param zoom
     */
    private fun processRotation(canvas: Canvas, shape: IShape, shapeRect: Rect) {
        var angle = shape.getRotation()
        //flip vertical
        if (shape.getFlipVertical()) {
            angle += 180f
        }

        //rotate transform
        if (angle != 0f) {
            canvas.rotate(angle, shapeRect.centerX().toFloat(), shapeRect.centerY().toFloat())
        }
    }

    /**
     *
     */
    fun dispose() {
        sheetView = null
        shapeRect = null
        temRect = null
    }
}
