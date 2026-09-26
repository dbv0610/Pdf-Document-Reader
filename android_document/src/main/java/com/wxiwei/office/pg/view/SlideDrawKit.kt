/*
 * 文件名称:          SlideDrawKit.java
 *
 * 编译器:            android2.2
 * 时间:              下午4:04:42
 */
package com.wxiwei.office.pg.view

import android.graphics.Bitmap
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
import com.wxiwei.office.common.shape.TableCell
import com.wxiwei.office.common.shape.TableShape
import com.wxiwei.office.common.shape.TextBox
import com.wxiwei.office.constant.wp.WPModelConstant
import com.wxiwei.office.fc.hslf.record.OEPlaceholderAtom
import com.wxiwei.office.java.awt.Color
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.java.awt.Rectanglef
import com.wxiwei.office.pg.animate.IAnimation
import com.wxiwei.office.pg.animate.ShapeAnimation
import com.wxiwei.office.pg.control.PGEditor
import com.wxiwei.office.pg.model.PGModel
import com.wxiwei.office.pg.model.PGSlide
import com.wxiwei.office.simpletext.model.IDocument
import com.wxiwei.office.simpletext.model.LeafElement
import com.wxiwei.office.simpletext.model.ParagraphElement
import com.wxiwei.office.simpletext.model.SectionElement
import com.wxiwei.office.simpletext.view.STRoot

/**
 * Slide Draw Kit
 * <p>
 * <p>
 * Read版本:        Read V1.0
 * <p>
 * 作者:            ljj8494
 * <p>
 * 日期:            2013-2-5
 * <p>
 * 负责人:          ljj8494
 * <p>
 * 负责小组:
 * <p>
 * <p>
 */
open class SlideDrawKit {

    /**
     *
     * @param canvas
     * @param pgModel
     * @param editor
     * @param slide
     * @param zoom
     */
    fun drawSlide(canvas: Canvas, pgModel: PGModel, editor: PGEditor?, slide: PGSlide, zoom: Float) {
        drawSlide(canvas, pgModel, editor, slide, zoom, null)
    }

    /**
     *
     * @param canvas
     * @param pgModel
     * @param editor
     * @param slide
     * @param zoom
     * @param isPlaying
     */
    fun drawSlide(
        canvas: Canvas,
        pgModel: PGModel,
        editor: PGEditor?,
        slide: PGSlide,
        zoom: Float,
        shapeVisible: Map<Int, MutableMap<Int, IAnimation>?>?
    ) {
        synchronized(this) {
            val d = pgModel.getPageSize()!!
            brRect.set(0, 0, (d.width * zoom).toInt(), (d.height * zoom).toInt())

            // 绘制背景
            if (!BackgroundDrawer.drawBackground(canvas, editor!!.getControl(), slide.getSlideNo(), slide.getBackgroundAndFill(), brRect, null, zoom)) {
                canvas.drawColor(Color.white.getRGB())
            }
            // draw shapes of master and layout
            val indexs = slide.getMasterIndexs()
            for (i in indexs.indices) {
                drawShapes(canvas, pgModel, editor, pgModel.getSlideMaster(indexs[i]), slide.getSlideNo(), zoom, shapeVisible)
            }
            // 绘制shape
            drawShapes(canvas, pgModel, editor, slide, slide.getSlideNo(), zoom, shapeVisible)
        }
    }

    /**
     * 绘制背景
     *
     * @param canvas
     * @param zoom
     */
    private fun drawShapes(
        canvas: Canvas,
        pgModel: PGModel,
        editor: PGEditor,
        slide: PGSlide?,
        slideNo: Int,
        zoom: Float,
        shapeVisible: Map<Int, MutableMap<Int, IAnimation>?>?
    ) {
        if (slide != null) {
            val count = slide.getShapeCount()
            for (i in 0 until count) {
                val shape = slide.getShape(i)!!
                if (shape.isHidden()) {
                    continue
                }

                val placeHolderID = shape.getPlaceHolderID()
                var draw = false
                if (slide.getSlideType() == PGSlide.Slide_Normal.toInt()) {
                    draw = true
                } else if (placeHolderID == OEPlaceholderAtom.None.toInt()
                    || placeHolderID == OEPlaceholderAtom.Object.toInt()
                    || placeHolderID == OEPlaceholderAtom.Graph.toInt()
                    || placeHolderID == OEPlaceholderAtom.Table.toInt()
                    || placeHolderID == OEPlaceholderAtom.ClipArt.toInt()
                    || placeHolderID == OEPlaceholderAtom.OrganizationChart.toInt()
                    || placeHolderID == OEPlaceholderAtom.MediaClip.toInt()
                ) {
                    draw = true
                }

                if (draw) {
                    drawShape(canvas, pgModel, editor, slideNo, shape, zoom, shapeVisible)
                }
            }
        }
    }

    /**
     *
     * @param shape
     * @param zoom
     * @return
     */
    private fun getShapeRect(shape: IShape, zoom: Float): Rect {
        val shapeRect = shape.getBounds()
        val left = Math.round(shapeRect.x * zoom)
        val top = Math.round(shapeRect.y * zoom)
        val width = Math.round(shapeRect.width * zoom)
        val height = Math.round(shapeRect.height * zoom)
        return Rect(left, top, left + width, top + height)
    }

    private fun drawShape(
        canvas: Canvas,
        pgModel: PGModel,
        editor: PGEditor,
        slideNo: Int,
        shape: IShape,
        zoom: Float,
        shapeVisible: Map<Int, MutableMap<Int, IAnimation>?>?
    ) {
        canvas.save()

        if (shape is GroupShape) {
            val rect = getShapeRect(shape, zoom)

            //flip vertical
            if (shape.getFlipVertical()) {
                canvas.translate(rect.left.toFloat(), rect.bottom.toFloat())
                canvas.scale(1f, -1f)
                canvas.translate(-rect.left.toFloat(), -rect.top.toFloat())
            }
            //flip horizontal
            if (shape.getFlipHorizontal()) {
                canvas.translate(rect.right.toFloat(), rect.top.toFloat())
                canvas.scale(-1f, 1f)
                canvas.translate(-rect.left.toFloat(), -rect.top.toFloat())
            }

            if (shape.getRotation() != 0f) {
                canvas.rotate(shape.getRotation(), rect.exactCenterX(), rect.exactCenterY())
            }

            val shapes = shape.getShapes()
            for (i in shapes.indices) {
                val childShape = shapes[i]
                if (shape.isHidden()) {
                    continue
                }
                drawShape(canvas, pgModel, editor, slideNo, childShape, zoom, shapeVisible)
            }
        } else {
            if (shape.getType() == AbstractShape.SHAPE_SMARTART) {
                val rect = getShapeRect(shape, zoom)

                val smartArt = shape as SmartArt
                BackgroundDrawer.drawLineAndFill(canvas, editor.getControl(), slideNo, smartArt, rect, zoom)

                canvas.translate(rect.left.toFloat(), rect.top.toFloat())

                val shapes = smartArt.getShapes()
                for (item in shapes) {
                    drawShape(canvas, pgModel, editor, slideNo, item, zoom, shapeVisible)
                }
            } else if (shape.getType() == AbstractShape.SHAPE_TEXTBOX) {
                // 文本框
                drawTextShape(canvas, pgModel, editor, slideNo, shape as TextBox, zoom, shapeVisible)
            } else {
                // 自选图型
                if (shape.getType() == AbstractShape.SHAPE_LINE || shape.getType() == AbstractShape.SHAPE_AUTOSHAPE) {
                    AutoShapeKit.instance().drawAutoShape(canvas, editor.getControl(), slideNo, shape as AutoShape, zoom)
                }
                // 图片
                else if (shape.getType() == AbstractShape.SHAPE_PICTURE) {
                    drawPicture(canvas, editor, slideNo, shape as PictureShape, zoom)
                }
                // chart
                else if (shape.getType() == AbstractShape.SHAPE_CHART) {
                    drawChart(canvas, editor, shape as AChart, zoom)
                }
                // table
                else if (shape.getType() == AbstractShape.SHAPE_TABLE) {
                    drawTable(canvas, pgModel, editor, slideNo, shape as TableShape, zoom, shapeVisible)
                }
            }
        }

        canvas.restore()
    }

    /**
     *
     * @param canvas
     * @param zoom
     */
    private fun drawTextShape(
        canvas: Canvas,
        pgModel: PGModel,
        editor: PGEditor,
        slideNo: Int,
        shape: TextBox,
        zoom: Float,
        shapeVisible: Map<Int, MutableMap<Int, IAnimation>?>?
    ) {
        val rect = shape.getBounds()
        // 没有文本就不要绘制
        var elem: SectionElement? = shape.getElement()
        if (elem == null || elem.getEndOffset() - elem.getStartOffset() == 0L) {
            return
        }

        canvas.save()
        var doc: IDocument? = null
        var root: STRoot? = shape.getRootView()
        val pgView = editor.getPGView()
        // for slide number
        if (pgView != null && root == null
            && (shape.getMCType() == TextBox.MC_SlideNumber || shape.getPlaceHolderID() == OEPlaceholderAtom.MasterSlideNumber.toInt())
        ) {
            doc = pgModel.getRenderersDoc()
            doc!!.appendSection(elem)

            var pageNumber = elem.getText(null)
            if (pageNumber != null && pageNumber.contains("*")) {
                pageNumber = pageNumber.replace("*", (slideNo + pgView.getPGModel()!!.getSlideNumberOffset()).toString())

                elem = SectionElement()
                elem.setStartOffset(0)
                elem.setEndOffset(pageNumber.length.toLong())
                elem.setAttribute(shape.getElement().getAttribute()!!.clone())

                // para
                val paraElem = shape.getElement().getParaCollection()!!.getElementForIndex(0) as ParagraphElement

                val paraElemNew = ParagraphElement()
                paraElemNew.setStartOffset(0)
                paraElemNew.setEndOffset(pageNumber.length.toLong())
                paraElemNew.setAttribute(paraElem.getAttribute()!!.clone())
                elem.appendParagraph(paraElemNew, WPModelConstant.MAIN)

                // leaf
                val leafElem = paraElem.getElementForIndex(0) as LeafElement

                val leafElemNew = LeafElement(pageNumber)
                leafElemNew.setStartOffset(0)
                leafElemNew.setEndOffset(pageNumber.length.toLong())
                leafElemNew.setAttribute(leafElem.getAttribute()!!.clone())
                paraElemNew.appendLeaf(leafElemNew)

                shape.setElement(elem)
            }
        }
//        processRotation(canvas, shape, zoom);
        if (root == null) {
            doc = pgModel.getRenderersDoc()
            doc!!.appendSection(elem)
            root = STRoot(editor, doc)
            root.setWrapLine(shape.isWrapLine())
            root.doLayout()
            shape.setRootView(root)
        }
        //
        if (shapeVisible != null) {
            if (shape.getGroupShapeID() >= 0) {
                editor.setShapeAnimation(shapeVisible[shape.getGroupShapeID()])
            } else {
                editor.setShapeAnimation(shapeVisible[shape.getShapeID()])
            }

            root.draw(canvas, (rect.x * zoom).toInt(), (rect.y * zoom).toInt(), zoom)
        } else {
            editor.getHighlight()!!.setPaintHighlight(shape === editor.getEditorTextBox())

            root.draw(canvas, (rect.x * zoom).toInt(), (rect.y * zoom).toInt(), zoom)
            //
            editor.getHighlight()!!.setPaintHighlight(false)
        }

        canvas.restore()
    }

    /**
     *
     * @param canvas
     * @param picture
     * @param zoom
     */
    private fun drawPicture(canvas: Canvas, editor: PGEditor, slideNo: Int, pictureShape: PictureShape, zoom: Float) {
        canvas.save()
        processRotation(canvas, pictureShape, zoom)
        val r = pictureShape.getBounds()

        val rect = getShapeRect(pictureShape, zoom)

        BackgroundDrawer.drawLineAndFill(canvas, editor.getControl(), slideNo, pictureShape, rect, zoom)

        PictureKit.instance().drawPicture(
            canvas, editor.getControl(), slideNo, pictureShape.getPicture(editor.getControl()),
            r.x * zoom, r.y * zoom, zoom, r.width * zoom, r.height * zoom, pictureShape.getPictureEffectInfor(), pictureShape.getAnimation()
        )
        canvas.restore()
    }

    /**
     *
     * @param canvas
     * @param chart
     * @param zoom
     */
    private fun drawChart(canvas: Canvas, editor: PGEditor, chart: AChart, zoom: Float) {
        val animation = chart.getAnimation()
        if (animation != null && animation.getCurrentAnimationInfor()!!.getAlpha() == 0) {
            return
        }

        canvas.save()
        var rect = chart.getBounds()
        val paint = PaintKit.instance().getPaint()
        if (animation != null) {
            val shapeAnim = animation.getShapeAnimation()!!
            val paraBegin = shapeAnim.getParagraphBegin()
            val paraEnd = shapeAnim.getParagraphEnd()

            if (paraBegin == ShapeAnimation.Para_All && paraEnd == ShapeAnimation.Para_All
                || (paraBegin == ShapeAnimation.Para_BG && paraEnd == ShapeAnimation.Para_BG)
            ) {
                val a = animation.getCurrentAnimationInfor()!!.getAlpha()
                paint.setAlpha(a)

                val rate = a / 255f * 0.5f
                val centerX = rect.getCenterX()
                val centerY = rect.getCenterY()
                rect = Rectangle(rect)
                rect.x = Math.round((centerX - rect.width * rate).toFloat())
                rect.y = Math.round((centerY - rect.height * rate).toFloat())
                rect.width = (rect.width * (rate * 2)).toInt()
                rect.height = (rect.height * (rate * 2)).toInt()
                val zoomT = zoom * rate * 2
                processRotation(canvas, chart, zoomT)
                chart.getAChart().setZoomRate(zoomT)
                chart.getAChart().draw(
                    canvas, editor.getControl(), (rect.x * zoom).toInt(), (rect.y * zoom).toInt(),
                    (rect.width * zoom).toInt(), (rect.height * zoom).toInt(), paint
                )
                return
            }
        }

        processRotation(canvas, chart, zoom)
        chart.getAChart().setZoomRate(zoom)
        chart.getAChart().draw(
            canvas, editor.getControl(), (rect.x * zoom).toInt(), (rect.y * zoom).toInt(),
            (rect.width * zoom).toInt(), (rect.height * zoom).toInt(), paint
        )

//        PictureKit.instance().drawPicture(canvas, editor.getControl(),
//            editor.getControl().getSysKit().getPictureManage().getPicture(chart.getDrawingPicture(editor.getControl())),
//            (int)(rect.x * zoom), (int)(rect.y * zoom), zoom, (int)(rect.width * zoom), (int)(rect.height * zoom), null, chart.getAnimation());

        canvas.restore()
    }

    /**
     *
     * @param canvas
     * @param table
     * @param zoom
     */
    private fun drawTable(
        canvas: Canvas,
        pgModel: PGModel,
        editor: PGEditor,
        slideNo: Int,
        table: TableShape,
        zoom: Float,
        shapeVisible: Map<Int, MutableMap<Int, IAnimation>?>?
    ) {
        canvas.save()
        processRotation(canvas, table, zoom)
        var alpha = 255
        if (table.getAnimation() != null) {
            alpha = table.getAnimation().getCurrentAnimationInfor()!!.getAlpha()
        }
        if (table.getAnimation() != null && alpha != 255) {
//            int LAYERS_FLAGS = Canvas.MATRIX_SAVE_FLAG |
//                Canvas.CLIP_SAVE_FLAG
//                | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
//                | Canvas.FULL_COLOR_LAYER_SAVE_FLAG
//                | Canvas.CLIP_TO_LAYER_SAVE_FLAG;
//            int LAYERS_FLAGS = 31;
            val tableRect = table.getBounds()
            if (tableRect != null) {
                @Suppress("DEPRECATION")
                canvas.saveLayerAlpha(
                    tableRect.x * zoom, tableRect.y * zoom,
                    (tableRect.x + tableRect.width + 1) * zoom, (tableRect.height + tableRect.y + 1) * zoom,
                    alpha, 31
                )
            }
        }
        val count = table.getCellCount()
        for (i in 0 until count) {
            val cell = table.getCell(i)
            if (cell != null) {
                val rect = cell.getBounds()

                brRect.set(
                    Math.round(rect.x * zoom), Math.round(rect.y * zoom),
                    Math.round((rect.x + rect.width) * zoom), Math.round((rect.y + rect.height) * zoom)
                )
                // background
                BackgroundDrawer.drawBackground(canvas, editor.getControl(), slideNo, cell.getBackgroundAndFill(), brRect, null, zoom)

                // border
//                if(table.isTable07())
                run {
                    drawCellBorder(canvas, cell, rect, zoom)
                }

                // text
                val tb = cell.getText()
                if (tb != null) {
                    drawTextShape(canvas, pgModel, editor, slideNo, cell.getText(), zoom, shapeVisible)
                }
            }
        }

        if (alpha != 255) {
            canvas.restore()
        }

        canvas.restore()
    }

    /**
     *
     * @param canvas
     * @param rect
     */
    private fun drawCellBorder(canvas: Canvas, cell: TableCell, rect: Rectanglef, zoom: Float) {
        drawCellBorder(canvas, cell, rect, zoom, null)
    }

    /**
     *
     * @param canvas
     * @param rect
     */
    private fun drawCellBorder(canvas: Canvas, cell: TableCell, rect: Rectanglef, zoom: Float, animation: IAnimation?) {
        val paint = PaintKit.instance().getPaint()
        val oldColor = paint.getColor()

        canvas.save()
        val addExd = Math.max(1f, zoom)
        // left
        var line = cell.getLeftLine()
        if (line != null) {
            paint.setColor(line.getBackgroundAndFill().getForegroundColor())
            paint.setStrokeWidth(line.getLineWidth() * zoom)
            if (animation != null) {
                paint.setAlpha(animation.getCurrentAnimationInfor()!!.getAlpha())
            }
            canvas.drawRect(rect.x * zoom, rect.y * zoom, rect.x * zoom + addExd, (rect.y + rect.height) * zoom, paint)
        }

        // top
        line = cell.getTopLine()
        if (line != null) {
            paint.setColor(line.getBackgroundAndFill().getForegroundColor())
            paint.setStrokeWidth(line.getLineWidth() * zoom)
            if (animation != null) {
                paint.setAlpha(animation.getCurrentAnimationInfor()!!.getAlpha())
            }
            canvas.drawRect(rect.x * zoom, rect.y * zoom, (rect.x + rect.width) * zoom, rect.y * zoom + addExd, paint)
        }

        // right
        line = cell.getRightLine()
        if (line != null) {
            paint.setColor(line.getBackgroundAndFill().getForegroundColor())
            paint.setStrokeWidth(line.getLineWidth() * zoom)
            if (animation != null) {
                paint.setAlpha(animation.getCurrentAnimationInfor()!!.getAlpha())
            }
            canvas.drawRect(
                (rect.x + rect.width) * zoom, rect.y * zoom, (rect.x + rect.width) * zoom + addExd,
                (rect.y + rect.height) * zoom, paint
            )
        }

        // bottom
        line = cell.getBottomLine()
        if (line != null) {
            paint.setColor(line.getBackgroundAndFill().getForegroundColor())
            paint.setStrokeWidth(line.getLineWidth() * zoom)
            if (animation != null) {
                paint.setAlpha(animation.getCurrentAnimationInfor()!!.getAlpha())
            }
            canvas.drawRect(
                rect.x * zoom, (rect.y + rect.height) * zoom, (rect.x + rect.width) * zoom,
                (rect.y + rect.height) * zoom + addExd, paint
            )
        }

        paint.setColor(oldColor)
        canvas.restore()
    }

    /**
     *
     * @param canvas
     * @param shape
     * @param zoom
     */
    private fun processRotation(canvas: Canvas, shape: IShape, zoom: Float) {
        val rect = shape.getBounds()
        var angle = shape.getRotation()
        //flip vertical
        if (shape.getFlipVertical()) {
            angle += 180f
        }
        val anim = shape.getAnimation()
        if (anim != null) {
            val shapeAnim = anim.getShapeAnimation()!!
            if (shapeAnim.getAnimationType() == ShapeAnimation.SA_EMPH) {
                angle += anim.getCurrentAnimationInfor()!!.getAngle()
            }
        }

        //rotate transform
        if (angle != 0f) {
            val px = rect.x + rect.width.toFloat() / 2
            val py = rect.y + rect.height.toFloat() / 2
            canvas.rotate(angle, px * zoom, py * zoom)
        }
    }

    /**
     * page to image for page number (base 1)
     *
     * @return bitmap raw data
     */
    fun slideToImage(pgModel: PGModel, editor: PGEditor?, slide: PGSlide?): Bitmap? {
        return slideToImage(pgModel, editor, slide, null)
    }

    /**
     *
     * @param pgModel
     * @param editor
     * @param slide
     * @param shapeVisible
     * @return
     */
    fun slideToImage(
        pgModel: PGModel,
        editor: PGEditor?,
        slide: PGSlide?,
        shapeVisible: Map<Int, MutableMap<Int, IAnimation>?>?
    ): Bitmap? {
        synchronized(this) {
            if (slide == null) {
                return null
            }

            val b = PictureKit.instance().isDrawPictrue()
            PictureKit.instance().setDrawPictrue(true)

            val d = pgModel.getPageSize()!!
            val bitmap = Bitmap.createBitmap(d.width, d.height, Bitmap.Config.ARGB_8888)
            brRect.set(0, 0, d.width, d.height)
            val canvas = Canvas(bitmap)
            // 绘制背景
            if (!BackgroundDrawer.drawBackground(canvas, editor!!.getControl(), slide.getSlideNo(), slide.getBackgroundAndFill(), brRect, null, 1.0f)) {
                canvas.drawColor(Color.white.getRGB())
            }
            // draw shapes of master and layout
            val indexs = slide.getMasterIndexs()
            for (i in indexs.indices) {
                drawShapes(canvas, pgModel, editor, pgModel.getSlideMaster(indexs[i]), slide.getSlideNo(), 1f, null)
            }
            // 绘制shape
            drawShapes(canvas, pgModel, editor, slide, slide.getSlideNo(), 1f, shapeVisible)

            PictureKit.instance().setDrawPictrue(b)

            return bitmap
        }
    }

    /**
     * page to image for page number (base 1)
     *
     * @return bitmap raw data
     */
    fun slideAreaToImage(
        pgModel: PGModel,
        editor: PGEditor?,
        slide: PGSlide?,
        srcLeft: Int,
        srcTop: Int,
        srcWidth: Int,
        srcHeight: Int,
        desWidth: Int,
        desHeight: Int
    ): Bitmap? {
        synchronized(this) {
            if (slide == null) {
                return null
            }
            val b = PictureKit.instance().isDrawPictrue()
            PictureKit.instance().setDrawPictrue(true)
            //
            val paintZoom = Math.min(desWidth / srcWidth.toFloat(), desHeight / srcHeight.toFloat())
            var bitmap: Bitmap? = null
            try {
                bitmap = Bitmap.createBitmap((srcWidth * paintZoom).toInt(), (srcHeight * paintZoom).toInt(), Bitmap.Config.ARGB_8888)
            } catch (e: OutOfMemoryError) {
                return null
            }
            if (bitmap == null) {
                return null
            }
            val d = pgModel.getPageSize()!!
            val canvas = Canvas(bitmap)
            brRect.set(0, 0, (d.getWidth() * paintZoom).toInt(), (d.getHeight() * paintZoom).toInt())
            canvas.translate(-srcLeft * paintZoom, -srcTop * paintZoom)
            //
            canvas.drawColor(Color.white.getRGB())
            // 绘制背景
            if (!BackgroundDrawer.drawBackground(canvas, editor!!.getControl(), slide.getSlideNo(), slide.getBackgroundAndFill(), brRect, null, 1f)) {
                //canvas.drawColor(Color.white.getRGB());
            }
            // draw shapes of master and layout
            val indexs = slide.getMasterIndexs()
            for (i in indexs.indices) {
                //drawShape(presentation.getSlideMaster(indexs[i]), canvas, paintZoom);
                drawShapes(canvas, pgModel, editor, pgModel.getSlideMaster(indexs[i]), slide.getSlideNo(), paintZoom, null)
            }
            // 绘制shape
            drawShapes(canvas, pgModel, editor, slide, slide.getSlideNo(), paintZoom, null)

            PictureKit.instance().setDrawPictrue(b)

            return bitmap
        }
    }

    /**
     * page to image for page number (base 1)
     *
     * @return bitmap raw data
     */
    fun getThumbnail(pgModel: PGModel, editor: PGEditor?, slide: PGSlide?, zoom: Float): Bitmap? {
        synchronized(this) {
            if (slide == null) {
                return null
            }
            val b = PictureKit.instance().isDrawPictrue()
            PictureKit.instance().setDrawPictrue(true)

            val d = pgModel.getPageSize()!!
            val w = (d.width * zoom).toInt()
            val h = (d.height * zoom).toInt()
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            brRect.set(0, 0, w, h)
            canvas.drawColor(Color.white.getRGB())
            // 绘制背景
            if (!BackgroundDrawer.drawBackground(canvas, editor!!.getControl(), slide.getSlideNo(), slide.getBackgroundAndFill(), brRect, null, 1f)) {
                //canvas.drawColor(Color.white.getRGB());
            }
            // draw shapes of master and layout
            val indexs = slide.getMasterIndexs()
            for (i in indexs.indices) {
                //drawShape(presentation.getSlideMaster(indexs[i]), canvas, zoom);
                drawShapes(canvas, pgModel, editor, pgModel.getSlideMaster(indexs[i]), slide.getSlideNo(), zoom, null)
            }
            // 绘制shape
            drawShapes(canvas, pgModel, editor, slide, slide.getSlideNo(), zoom, null)

            PictureKit.instance().setDrawPictrue(b)
            return bitmap
        }
    }

    /**
     *
     * @param slide
     */
    fun disposeOldSlideView(pgModel: PGModel?, slide: PGSlide?) {
        if (slide != null) {
            val count = slide.getShapeCount()
            for (i in 0 until count) {
                val shape = slide.getShape(i)!!
                if (shape.getType() == AbstractShape.SHAPE_TEXTBOX) // 文本框
                {
                    val root = (shape as TextBox).getRootView()
                    if (root != null) {
                        root.dispose()
                        shape.setRootView(null)
                    }
                }
                // table
                else if (shape.getType() == AbstractShape.SHAPE_TABLE) {
                    val cellCount = (shape as TableShape).getCellCount()
                    for (j in 0 until cellCount) {
                        val cell = shape.getCell(j)
                        if (cell != null) {
                            val tb = cell.getText()
                            if (tb != null) {
                                val root = tb.getRootView()
                                if (root != null) {
                                    root.dispose()
                                    tb.setRootView(null)
                                }
                            }
                        }
                    }
                }
            }
            /*int[] indexs = slide.getMasterIndexs();
            for (int i = 0; i < indexs.length; i++)
            {
                disposeOldSlideView(pgModel.getSlideMaster(indexs[i]));
            }*/
        }
    }

    //
    private val brRect = Rect()

    companion object {
        private var kit: SlideDrawKit? = null

        @JvmStatic
        fun instance(): SlideDrawKit {
            if (kit == null) {
                kit = SlideDrawKit()
            }
            return kit!!
        }
    }
}
