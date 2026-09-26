/*
 * 文件名称:           AutoShapeKit.java
 *  
 * 编译器:             android2.2
 * 时间:               下午4:26:09
 */
package com.wxiwei.office.common.autoshape

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import com.wxiwei.office.common.BackgroundDrawer
import com.wxiwei.office.common.PaintKit
import com.wxiwei.office.common.autoshape.pathbuilder.actionButton.ActionButtonPathBuilder
import com.wxiwei.office.common.autoshape.pathbuilder.arrow.ArrowPathBuilder
import com.wxiwei.office.common.autoshape.pathbuilder.baseshape.BaseShapePathBuilder
import com.wxiwei.office.common.autoshape.pathbuilder.flowChart.FlowChartDrawing
import com.wxiwei.office.common.autoshape.pathbuilder.line.LinePathBuilder
import com.wxiwei.office.common.autoshape.pathbuilder.math.MathPathBuilder
import com.wxiwei.office.common.autoshape.pathbuilder.rect.RectPathBuilder
import com.wxiwei.office.common.autoshape.pathbuilder.smartArt.SmartArtPathBuilder
import com.wxiwei.office.common.autoshape.pathbuilder.starAndBanner.BannerPathBuilder
import com.wxiwei.office.common.autoshape.pathbuilder.starAndBanner.star.StarPathBuilder
import com.wxiwei.office.common.autoshape.pathbuilder.wedgecallout.WedgeCalloutDrawing
import com.wxiwei.office.common.shape.ArbitraryPolygonShape
import com.wxiwei.office.common.shape.AutoShape
import com.wxiwei.office.common.shape.LineShape
import com.wxiwei.office.common.shape.ShapeTypes
import com.wxiwei.office.common.shape.WPAutoShape
import com.wxiwei.office.fc.ppt.reader.ReaderKit.Companion.instance
import com.wxiwei.office.pg.animate.IAnimation
import com.wxiwei.office.pg.animate.ShapeAnimation
import com.wxiwei.office.ss.util.ColorUtil.Companion.instance
import com.wxiwei.office.system.IControl

/**
 * draw autoShape
 * 
 * 
 * 
 * 
 * Read版本:       Read V1.0
 * 
 * 
 * 作者:           jhy1790
 * 
 * 
 * 日期:           2012-8-27
 * 
 * 
 * 负责人:         jhy1790
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
class AutoShapeKit

/**
 * 
 */
{
    /**
     * 
     * @param shape all shapes except textbox
     * @return
     */
    private fun getShapeAnimation(shape: AutoShape): IAnimation? {
        val animation = shape.getAnimation()
        if (animation != null) {
            val shapeAnim = animation.getShapeAnimation()
            val paraBegin = shapeAnim!!.getParagraphBegin()
            val paraEnd = shapeAnim.getParagraphEnd()

            if ((paraBegin == ShapeAnimation.Para_All && paraEnd == ShapeAnimation.Para_All)
                || (paraBegin == ShapeAnimation.Para_BG && paraEnd == ShapeAnimation.Para_BG)
            ) {
                return animation
            }
        }

        return null
    }

    private fun processShapeRect(rect: Rect, animation: IAnimation?) {
        if (animation != null) {
            val width = rect.width()
            val height = rect.height()

            val alpha = animation.getCurrentAnimationInfor()!!.getAlpha()
            val rate = alpha / 255f * 0.5f

            val centerX = rect.centerX()
            val centerY = rect.centerY()
            rect.set(
                (centerX - width * rate).toInt(),
                (centerY - height * rate).toInt(),
                (centerX + width * rate).toInt(),
                (centerY + height * rate).toInt()
            )
        }
    }

    /**
     * 
     * @param canvas
     * @param shape
     * @param zoom
     */
    fun drawAutoShape(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        zoom: Float
    ) {
        val shapeRect = shape.getBounds()
        val left = Math.round(shapeRect.x * zoom)
        val top = Math.round(shapeRect.y * zoom)
        val width = Math.round(shapeRect.width * zoom)
        val height = Math.round(shapeRect.height * zoom)
        rect.set(left, top, left + width, top + height)
        drawAutoShape(canvas, control, viewIndex, shape, rect, zoom)
    }

    /**
     * 
     * @param canvas
     * @param shape
     * @param rect
     * @param zoom
     */
    fun drawAutoShape(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        rect: Rect,
        zoom: Float
    ) {
        val animation = getShapeAnimation(shape)


        //zoom by animation
        processShapeRect(rect, animation)
        val type = shape.getShapeType()
        when (type) {
            ShapeTypes.Line, ShapeTypes.StraightConnector1, ShapeTypes.BentConnector2, ShapeTypes.BentConnector3, ShapeTypes.CurvedConnector2, ShapeTypes.CurvedConnector3, ShapeTypes.CurvedConnector4, ShapeTypes.CurvedConnector5 -> {
                if (shape is LineShape) {
                    val pathList = LinePathBuilder.getLinePath(shape, rect, zoom)
                    var i = 0
                    while (i < pathList!!.size) {
                        val extendPath = ExtendPath(pathList.get(i)!!)
                        drawShape(
                            canvas,
                            control,
                            viewIndex,
                            shape,
                            extendPath,
                            rect,
                            animation,
                            zoom
                        )
                        i++
                    }
                }
            }

            ShapeTypes.ArbitraryPolygon -> {
                m.reset()
                m.postScale(zoom, zoom)
                val pathList = (shape as ArbitraryPolygonShape).getPaths()
                var i = 0
                while (i < pathList.size) {
                    val extendPath = ExtendPath(pathList.get(i)!!)
                    extendPath.path!!.transform(m)
                    extendPath.path!!.offset(rect.left.toFloat(), rect.top.toFloat())

                    drawShape(canvas, control, viewIndex, shape, extendPath, rect, animation, zoom)
                    i++
                }
            }

            ShapeTypes.WP_Line, ShapeTypes.Curve, ShapeTypes.DirectPolygon -> {
                m.reset()
                m.postScale(zoom, zoom)
                val pathList = (shape as WPAutoShape).getPaths()

                val r = Rect(rect)
                if (rect.width() == 0 || rect.height() == 0) {
                    //wp line, curve, polygon rect adjust
                    val bounds = RectF()
                    pathList.get(0)!!.path!!.computeBounds(bounds, true)
                    r.set(
                        bounds.left.toInt(),
                        bounds.top.toInt(),
                        bounds.right.toInt(),
                        bounds.bottom.toInt()
                    )
                }

                var i = 0
                while (i < pathList.size) {
                    val extendPath = ExtendPath(pathList.get(i)!!)
                    extendPath.path!!.transform(m)
                    extendPath.path!!.offset(rect.left.toFloat(), rect.top.toFloat())

                    drawShape(canvas, control, viewIndex, shape, extendPath, r, animation, zoom)
                    i++
                }
            }

            ShapeTypes.Rectangle, ShapeTypes.TextBox, ShapeTypes.RoundRectangle, ShapeTypes.Round1Rect, ShapeTypes.Round2SameRect, ShapeTypes.Round2DiagRect, ShapeTypes.Snip1Rect, ShapeTypes.Snip2SameRect, ShapeTypes.Snip2DiagRect, ShapeTypes.SnipRoundRect, ShapeTypes.TextPlainText -> drawShape(
                canvas,
                control,
                viewIndex,
                shape,
                RectPathBuilder.getRectPath(shape, rect),
                rect,
                animation,
                zoom
            )

            ShapeTypes.Ellipse, ShapeTypes.Triangle, ShapeTypes.RtTriangle, ShapeTypes.Parallelogram, ShapeTypes.Trapezoid, ShapeTypes.Diamond, ShapeTypes.Pentagon, ShapeTypes.Hexagon, ShapeTypes.Heptagon, ShapeTypes.Octagon, ShapeTypes.Decagon, ShapeTypes.Dodecagon, ShapeTypes.Pie, ShapeTypes.Chord, ShapeTypes.Teardrop, ShapeTypes.Frame, ShapeTypes.HalfFrame, ShapeTypes.Corner, ShapeTypes.DiagStripe, ShapeTypes.Plus, ShapeTypes.Plaque, ShapeTypes.Can, ShapeTypes.Cube, ShapeTypes.Bevel, ShapeTypes.Donut, ShapeTypes.NoSmoking, ShapeTypes.BlockArc, ShapeTypes.FoldedCorner, ShapeTypes.SmileyFace, ShapeTypes.Sun, ShapeTypes.Heart, ShapeTypes.LightningBolt, ShapeTypes.Moon, ShapeTypes.Cloud, ShapeTypes.Arc, ShapeTypes.BracketPair, ShapeTypes.BracePair, ShapeTypes.LeftBracket, ShapeTypes.RightBracket, ShapeTypes.LeftBrace, ShapeTypes.RightBrace -> {
                val obj = BaseShapePathBuilder.getBaseShapePath(shape, rect)
                if (obj is Path) {
                    drawShape(canvas, control, viewIndex, shape, obj, rect, animation, zoom)
                } else {
                    val pathList = obj as MutableList<ExtendPath?>?
                    var i = 0
                    while (i < pathList!!.size) {
                        val extendPath = ExtendPath(pathList.get(i)!!)
                        drawShape(
                            canvas,
                            control,
                            viewIndex,
                            shape,
                            extendPath,
                            rect,
                            animation,
                            zoom
                        )
                        i++
                    }
                }
            }

            ShapeTypes.MathPlus, ShapeTypes.MathMinus, ShapeTypes.MathMultiply, ShapeTypes.MathDivide, ShapeTypes.MathEqual, ShapeTypes.MathNotEqual -> drawShape(
                canvas,
                control,
                viewIndex,
                shape,
                MathPathBuilder.getMathPath(shape, rect),
                rect,
                animation,
                zoom
            )

            ShapeTypes.RightArrow, ShapeTypes.LeftArrow, ShapeTypes.UpArrow, ShapeTypes.DownArrow, ShapeTypes.LeftRightArrow, ShapeTypes.UpDownArrow, ShapeTypes.QuadArrow, ShapeTypes.LeftRightUpArrow, ShapeTypes.BentArrow, ShapeTypes.UturnArrow, ShapeTypes.LeftUpArrow, ShapeTypes.BentUpArrow, ShapeTypes.StripedRightArrow, ShapeTypes.NotchedRightArrow, ShapeTypes.HomePlate, ShapeTypes.Chevron, ShapeTypes.RightArrowCallout, ShapeTypes.LeftArrowCallout, ShapeTypes.DownArrowCallout, ShapeTypes.UpArrowCallout, ShapeTypes.LeftRightArrowCallout, ShapeTypes.UpDownArrowCallout, ShapeTypes.QuadArrowCallout, ShapeTypes.CircularArrow, ShapeTypes.CurvedRightArrow, ShapeTypes.CurvedLeftArrow, ShapeTypes.CurvedUpArrow, ShapeTypes.CurvedDownArrow -> {
                val obj: Any? = ArrowPathBuilder.Companion.getArrowPath(shape, rect)
                if (obj is Path) {
                    drawShape(canvas, control, viewIndex, shape, obj, rect, animation, zoom)
                } else {
                    val list = obj as MutableList<Path?>
                    val cnt = list.size
                    var i = 0
                    while (i < cnt) {
                        drawShape(
                            canvas,
                            control,
                            viewIndex,
                            shape,
                            list.get(i),
                            rect,
                            animation,
                            zoom
                        )
                        i++
                    }
                }
            }

            ShapeTypes.FlowChartProcess, ShapeTypes.FlowChartAlternateProcess, ShapeTypes.FlowChartDecision, ShapeTypes.FlowChartInputOutput, ShapeTypes.FlowChartPredefinedProcess, ShapeTypes.FlowChartInternalStorage, ShapeTypes.FlowChartDocument, ShapeTypes.FlowChartMultidocument, ShapeTypes.FlowChartTerminator, ShapeTypes.FlowChartPreparation, ShapeTypes.FlowChartManualInput, ShapeTypes.FlowChartManualOperation, ShapeTypes.FlowChartConnector, ShapeTypes.FlowChartOffpageConnector, ShapeTypes.FlowChartPunchedCard, ShapeTypes.FlowChartPunchedTape, ShapeTypes.FlowChartSummingJunction, ShapeTypes.FlowChartOr, ShapeTypes.FlowChartCollate, ShapeTypes.FlowChartSort, ShapeTypes.FlowChartExtract, ShapeTypes.FlowChartMerge, ShapeTypes.FlowChartOnlineStorage, ShapeTypes.FlowChartDelay, ShapeTypes.FlowChartMagneticTape, ShapeTypes.FlowChartMagneticDisk, ShapeTypes.FlowChartMagneticDrum, ShapeTypes.FlowChartDisplay -> FlowChartDrawing.Companion.instance()
                .drawFlowChart(canvas, control, viewIndex, shape, rect, zoom)

            ShapeTypes.WedgeRectCallout, ShapeTypes.WedgeRoundRectCallout, ShapeTypes.WedgeEllipseCallout, ShapeTypes.CloudCallout, ShapeTypes.BorderCallout1, ShapeTypes.BorderCallout2, ShapeTypes.BorderCallout3, ShapeTypes.BorderCallout4, ShapeTypes.AccentCallout1, ShapeTypes.AccentCallout2, ShapeTypes.AccentCallout3, ShapeTypes.AccentCallout4, ShapeTypes.Callout1, ShapeTypes.Callout2, ShapeTypes.Callout3, ShapeTypes.Callout4, ShapeTypes.AccentBorderCallout1, ShapeTypes.AccentBorderCallout2, ShapeTypes.AccentBorderCallout3, ShapeTypes.AccentBorderCallout4 -> {
                val obj: Any? =
                    WedgeCalloutDrawing.Companion.instance().getWedgeCalloutPath(shape, rect)
                if (obj is Path) {
                    drawShape(canvas, control, viewIndex, shape, obj, rect, animation, zoom)
                } else {
                    val list = obj as MutableList<ExtendPath?>
                    val cnt = list.size
                    var i = 0
                    while (i < cnt) {
                        drawShape(
                            canvas,
                            control,
                            viewIndex,
                            shape,
                            list.get(i)!!,
                            rect,
                            animation,
                            zoom
                        )
                        i++
                    }
                }
            }

            ShapeTypes.ActionButtonBackPrevious, ShapeTypes.ActionButtonForwardNext, ShapeTypes.ActionButtonBeginning, ShapeTypes.ActionButtonEnd, ShapeTypes.ActionButtonHome, ShapeTypes.ActionButtonInformation, ShapeTypes.ActionButtonReturn, ShapeTypes.ActionButtonMovie, ShapeTypes.ActionButtonDocument, ShapeTypes.ActionButtonSound, ShapeTypes.ActionButtonHelp, ShapeTypes.ActionButtonBlank -> {
                val pathList = ActionButtonPathBuilder.getActionButtonExtendPath(shape, rect)
                if (pathList == null) {
                    return
                }
                val cnt = pathList.size
                var i = 0
                while (i < cnt) {
                    drawShape(
                        canvas,
                        control,
                        viewIndex,
                        shape,
                        pathList.get(i)!!,
                        rect,
                        animation,
                        zoom
                    )
                    i++
                }
            }

            ShapeTypes.IrregularSeal1, ShapeTypes.IrregularSeal2, ShapeTypes.Star, ShapeTypes.Star4, ShapeTypes.Star5, ShapeTypes.Star6, ShapeTypes.Star7, ShapeTypes.Star8, ShapeTypes.Star10, ShapeTypes.Star12, ShapeTypes.Star16, ShapeTypes.Star24, ShapeTypes.Star32 -> {
                val path = StarPathBuilder.getStarPath(shape, rect)
                if (path != null) {
                    drawShape(canvas, control, viewIndex, shape, path, rect, animation, zoom)
                }
            }

            ShapeTypes.Ribbon, ShapeTypes.Ribbon2, ShapeTypes.EllipseRibbon, ShapeTypes.EllipseRibbon2, ShapeTypes.VerticalScroll, ShapeTypes.HorizontalScroll, ShapeTypes.Wave, ShapeTypes.DoubleWave, ShapeTypes.LeftRightRibbon -> {
                val pathList = BannerPathBuilder.getFlagExtendPath(shape, rect)
                if (pathList == null) {
                    return
                }
                val cnt = pathList.size
                var i = 0
                while (i < cnt) {
                    drawShape(
                        canvas,
                        control,
                        viewIndex,
                        shape,
                        pathList.get(i)!!,
                        rect,
                        animation,
                        zoom
                    )
                    i++
                }
            }

            ShapeTypes.Funnel, ShapeTypes.Gear6, ShapeTypes.Gear9, ShapeTypes.LeftCircularArrow, ShapeTypes.PieWedge, ShapeTypes.SwooshArrow -> {
                val path = SmartArtPathBuilder.getStarPath(shape, rect)
                if (path != null) {
                    drawShape(canvas, control, viewIndex, shape, path, rect, animation, zoom)
                }
            }

            else -> {}
        }
    }

    private fun processCanvas(
        canvas: Canvas,
        rect: Rect,
        angle: Float,
        flipH: Boolean,
        flipV: Boolean,
        animation: IAnimation?
    ) {
        var angle = angle
        if (animation != null) {
            angle += animation.getCurrentAnimationInfor()!!.getAngle().toFloat()
        }


        //flip vertical
        if (flipV) {
            canvas.translate(rect.left.toFloat(), rect.bottom.toFloat())
            canvas.scale(1f, -1f)
            canvas.translate(-rect.left.toFloat(), -rect.top.toFloat())

            angle = -angle
        }
        //flip horizontal
        if (flipH) {
            canvas.translate(rect.right.toFloat(), rect.top.toFloat())
            canvas.scale(-1f, 1f)
            canvas.translate(-rect.left.toFloat(), -rect.top.toFloat())

            angle = -angle
        }

        if (angle != 0f) {
            canvas.rotate(angle, rect.centerX().toFloat(), rect.centerY().toFloat())
        }
    }

    fun drawShape(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        pathExtend: ExtendPath,
        rect: Rect,
        animation: IAnimation?,
        zoom: Float
    ) {
        // save
        canvas.save()
        val paint = PaintKit.instance().getPaint()

        processCanvas(
            canvas,
            rect,
            shape.getRotation(),
            shape.getFlipHorizontal(),
            shape.getFlipVertical(),
            animation
        )

        val alpha = paint.getAlpha()
        // draw fill
        val fill = pathExtend.backgroundAndFill
        if (fill != null) {
            paint.setStyle(Paint.Style.FILL)
            BackgroundDrawer.drawPathBackground(
                canvas,
                control,
                viewIndex,
                fill,
                rect,
                animation,
                zoom,
                pathExtend.path,
                paint
            )
            paint.setAlpha(alpha)
        }


        // draw border
        if (pathExtend.hasLine()) {
            paint.setStyle(Paint.Style.STROKE)
            paint.setStrokeWidth(pathExtend.line!!.getLineWidth() * zoom)
            if (pathExtend.line!!.isDash() && !pathExtend.isArrowPath) {
                val dashPathEffect = DashPathEffect(floatArrayOf(5 * zoom, 5 * zoom), 10f)
                paint.setPathEffect(dashPathEffect)
            }

            BackgroundDrawer.drawPathBackground(
                canvas,
                control,
                viewIndex,
                pathExtend.line!!.getBackgroundAndFill(),
                rect,
                animation,
                zoom,
                pathExtend.path,
                paint
            )
            paint.setAlpha(alpha)
        }


        //restore
        canvas.restore()
    }

    /**
     * 
     * @param canvas
     * @param shape
     * @param path
     * @param rect
     */
    fun drawShape(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        path: Path?,
        rect: Rect,
        zoom: Float
    ) {
        this.drawShape(
            canvas,
            control,
            viewIndex,
            shape,
            path,
            rect,
            getShapeAnimation(shape),
            zoom
        )
    }

    /**
     * 
     * @param canvas
     * @param shape
     * @param path
     * @param rect
     */
    fun drawShape(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AutoShape,
        path: Path?,
        rect: Rect,
        animation: IAnimation?,
        zoom: Float
    ) {
        if (path == null) {
            return
        }


        // save
        canvas.save()
        val paint = PaintKit.instance().getPaint()
        val color = paint.getColor()
        val style = paint.getStyle()
        val alpha = paint.getAlpha()

        processCanvas(
            canvas,
            rect,
            shape.getRotation(),
            shape.getFlipHorizontal(),
            shape.getFlipVertical(),
            animation
        )


        // draw fill
        val fill = shape.getBackgroundAndFill()
        if (fill != null) {
            paint.setStyle(Paint.Style.FILL)
            BackgroundDrawer.drawPathBackground(
                canvas,
                control,
                viewIndex,
                fill,
                rect,
                animation,
                zoom,
                path,
                paint
            )
            paint.setAlpha(alpha)
        }
        // draw border
        if (shape.hasLine()) {
            paint.setStyle(Paint.Style.STROKE)

            paint.setStrokeWidth(shape.getLine().getLineWidth() * zoom)
            if (shape.getLine().isDash()) {
                val dashPathEffect = DashPathEffect(floatArrayOf(5 * zoom, 5 * zoom), 10f)
                paint.setPathEffect(dashPathEffect)
            }
            BackgroundDrawer.drawPathBackground(
                canvas,
                control,
                viewIndex,
                shape.getLine().getBackgroundAndFill(),
                rect,
                animation,
                zoom,
                path,
                paint
            )
            paint.setAlpha(alpha)
        }


        //restore
        paint.setAlpha(alpha)
        paint.setColor(color)
        paint.setStyle(style)
        canvas.restore()
    }

    companion object {
        //
        var ARROW_WIDTH: Int = 10

        private val rect = Rect()

        private val m = Matrix()

        //
        private val kit = AutoShapeKit()

        /**
         * 
         * @return
         */
        fun instance(): AutoShapeKit {
            return kit
        }
    }
}
