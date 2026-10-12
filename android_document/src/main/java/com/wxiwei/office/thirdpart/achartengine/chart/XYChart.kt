/**
 * Copyright (C) 2009, 2010 SC 4ViewSoft SRL
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wxiwei.office.thirdpart.achartengine.chart

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Paint.Cap
import android.graphics.Paint.Join
import android.graphics.Path
import android.graphics.PathEffect
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.Typeface
import com.wxiwei.office.common.BackgroundDrawer.drawPathBackground
import com.wxiwei.office.constant.SSConstant
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.system.IControl
import com.wxiwei.office.thirdpart.achartengine.model.XYMultipleSeriesDataset
import com.wxiwei.office.thirdpart.achartengine.model.XYSeries
import com.wxiwei.office.thirdpart.achartengine.renderers.SimpleSeriesRenderer
import com.wxiwei.office.thirdpart.achartengine.renderers.XYMultipleSeriesRenderer
import com.wxiwei.office.thirdpart.achartengine.util.MathHelper
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * The XY chart rendering class.
 */
abstract class XYChart : AbstractChart {
    /** The multiple series dataset.  */
    protected lateinit var mDataset: XYMultipleSeriesDataset

    /** The multiple series renderer.  */
    protected lateinit var mRenderer: XYMultipleSeriesRenderer

    /** The current scale value.  */
    private var mScale = 0f

    /** The current translate value.  */
    private var mTranslate = 0f

    /** The canvas center point.  */
    private var mCenter: PointF? = null

    /** The visible chart area, in screen coordinates.  */
    var screenR: Rect? = null

    /** The calculated range.  */
    private val mCalcRange: MutableMap<Int?, DoubleArray> = HashMap<Int?, DoubleArray>()

    protected constructor()

    /**
     * Builds a new XY chart instance.
     * 
     * @param dataset the multiple series dataset
     * @param renderer the multiple series renderer
     */
    constructor(dataset: XYMultipleSeriesDataset, renderer: XYMultipleSeriesRenderer) {
        mDataset = dataset
        mRenderer = renderer
    }

    // TODO: javadoc
    open fun setDatasetRenderer(
        dataset: XYMultipleSeriesDataset,
        renderer: XYMultipleSeriesRenderer
    ) {
        mDataset = dataset
        mRenderer = renderer
    }

    /**
     * 
     * (non-Javadoc)
     * @see AbstractChart.setZoomRate
     */
    override var zoomRate: Float
        get() = mRenderer!!.zoomRate
        set(rate) {
            this.mRenderer!!.zoomRate = rate
        }

    protected fun drawSeriesBackgroundAndFrame(
        renderer: XYMultipleSeriesRenderer,
        canvas: Canvas,
        rect: Rect,
        paint: Paint
    ) {
        val alpha = paint.getAlpha()
        val path = Path()
        path.addRect(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat(),
            Path.Direction.CCW
        )
        // draw fill
        val fill = renderer.seriesBackgroundColor
        if (fill != null) {
            paint.setStyle(Paint.Style.FILL)
            drawPathBackground(canvas, null, 1, fill, rect, null, 1.0f, path, paint)
            paint.setAlpha(alpha)
        }


        // draw border
        val frame = renderer.seriesFrame
        if (frame != null) {
            paint.setStyle(Paint.Style.STROKE)
            paint.setStrokeWidth(2f)
            if (frame.isDash) {
                val dashPathEffect = DashPathEffect(floatArrayOf(5f, 5f), 10f)
                paint.setPathEffect(dashPathEffect)
            }

            drawPathBackground(
                canvas,
                null,
                1,
                frame.backgroundAndFill,
                rect,
                null,
                1.0f,
                path,
                paint
            )
            paint.setStyle(Paint.Style.FILL)
            paint.setAlpha(alpha)
        }

        paint.reset()
        paint.setAntiAlias(true)
    }

    /**
     * The graphical representation of the XY chart.
     * 
     * @param canvas the canvas to paint to
     * @param x the top left x value of the view to draw to
     * @param y the top left y value of the view to draw to
     * @param width the width of the view to draw to
     * @param height the height of the view to draw to
     * @param paint the paint
     */
    override fun draw(
        canvas: Canvas,
        control: IControl?,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        paint: Paint
    ) {
        val rect = Rect(x, y, x + width, y + height)
        canvas.save()
        canvas.clipRect(rect)

        paint.setAntiAlias(mRenderer!!.isAntialiasing)
        val preColor = paint.getColor()
        val preSize = paint.getStrokeWidth()

        drawBackgroundAndFrame(mRenderer, canvas, control, rect, paint)
        var legendHeight = mRenderer!!.legendHeight

        if (mRenderer!!.isShowLegend && legendHeight == 0) {
            legendHeight = height / 5
        }

        val sLength = mDataset!!.seriesCount
        val titles = arrayOfNulls<String>(sLength)
        for (i in 0..<sLength) {
            titles[i] = mDataset!!.getSeriesAt(i).title
        }

        val titleAreaSize = getTitleTextAreaSize(mRenderer, width, height, paint)
        val xTitleAreaSize = getXTitleTextAreaSize(width, height, paint)
        val yTitleAreaSize = getYTitleTextAreaSize(width, height, paint)

        var legendH = height
        if (titleAreaSize != null) {
            legendH -= titleAreaSize.height
        }
        val legendSize = getLegendAutoSize(mRenderer, titles, width, legendH, paint)

        val margins = mRenderer!!.margins

        var left =
            x + (margins[1] * width + mRenderer!!.yTitleTextSize * mRenderer!!.zoomRate).toInt()
        if (yTitleAreaSize != null) {
            left += yTitleAreaSize.width
        }

        var top = y + (margins[0] * height).toInt()
        if (titleAreaSize != null) {
            top += titleAreaSize.height
        }

        var right = x + width - (margins[3] * width).toInt()
        if (legendSize != null && (legendPosition == AbstractChart.Companion.LegendPosition_Left || legendPosition == AbstractChart.Companion.LegendPosition_Right)) {
            right -= legendSize.width
        }

        var bottom = y + height - (margins[2] * height).toInt()
        if (legendSize != null && (legendPosition == AbstractChart.Companion.LegendPosition_Top || legendPosition == AbstractChart.Companion.LegendPosition_Bottom)) {
            bottom -= legendSize.height
        }
        if (xTitleAreaSize != null) {
            bottom -= xTitleAreaSize.height
        }


        //
        paint.setTextSize(mRenderer!!.labelsTextSize * mRenderer!!.zoomRate)
        var fm = paint.getFontMetrics()
        bottom = (bottom - (fm.descent - fm.ascent)).toInt()

        if (paint.getTypeface() == null || (paint.getTypeface()
                .toString() != mRenderer!!.textTypefaceName) || paint.getTypeface()
                .getStyle() != mRenderer!!.textTypefaceStyle
        ) {
            paint.setTypeface(
                Typeface.create(
                    mRenderer!!.textTypefaceName,
                    mRenderer!!.textTypefaceStyle
                )
            )
        }
        val or = mRenderer!!.orientation
        if (or == XYMultipleSeriesRenderer.Orientation.VERTICAL) {
            right -= legendHeight
            bottom += legendHeight - 20
        }
        val angle = or.angle
        val rotate = angle == 90
        mScale = (height).toFloat() / width
        mTranslate = (abs(width - height) / 2).toFloat()
        if (mScale < 1) {
            mTranslate *= -1f
        }
        mCenter = PointF(((x + width) / 2).toFloat(), ((y + height) / 2).toFloat())
        if (rotate) {
            transform(canvas, angle.toFloat(), false)
        }

        var maxScaleNumber = -Int.MAX_VALUE
        for (i in 0..<sLength) {
            maxScaleNumber = max(maxScaleNumber, mDataset!!.getSeriesAt(i).scaleNumber)
        }
        maxScaleNumber++
        if (maxScaleNumber < 0) {
            canvas.restore()
            return
        }
        val minX = DoubleArray(maxScaleNumber)
        val maxX = DoubleArray(maxScaleNumber)
        val minY = DoubleArray(maxScaleNumber)
        val maxY = DoubleArray(maxScaleNumber)
        val isMinXSet = BooleanArray(maxScaleNumber)
        val isMaxXSet = BooleanArray(maxScaleNumber)
        val isMinYSet = BooleanArray(maxScaleNumber)
        val isMaxYSet = BooleanArray(maxScaleNumber)

        for (i in 0..<maxScaleNumber) {
            minX[i] = mRenderer!!.getXAxisMin(i)
            maxX[i] = mRenderer!!.getXAxisMax(i)
            minY[i] = mRenderer!!.getYAxisMin(i)
            maxY[i] = mRenderer!!.getYAxisMax(i)
            isMinXSet[i] = mRenderer!!.isMinXSet(i)
            isMaxXSet[i] = mRenderer!!.isMaxXSet(i)
            isMinYSet[i] = mRenderer!!.isMinYSet(i)
            isMaxYSet[i] = mRenderer!!.isMaxYSet(i)
            if (mCalcRange.get(i) == null) {
                mCalcRange.put(i, DoubleArray(4))
            }
        }

        val allYLabels: MutableMap<Int?, MutableList<Double>?> =
            HashMap<Int?, MutableList<Double>?>()
        for (i in 0..<maxScaleNumber) {
            paint.setTextSize(mRenderer!!.labelsTextSize * mRenderer!!.zoomRate)
            fm = paint.getFontMetrics()
            val yLabelHeight = fm.descent - fm.ascent
            val lines = ((bottom - top) / yLabelHeight).toInt() / 2
            val approxNumLabels = min(mRenderer!!.yLabels, lines)

            allYLabels.put(
                i,
                getValidLabels(MathHelper.getLabels(minY[i], maxY[i], approxNumLabels))
            )
        }

        for (i in 0..<maxScaleNumber) {
            if (abs(minY[i]) > 0.001) {
                //minY[i] != 0
                val yLabels = allYLabels.get(i)
                val miny = yLabels!!.get(0)!! - (yLabels.get(1)!! - yLabels.get(0)!!)
                if (minY[i] > 0 && miny > 0) {
                    minY[i] = miny
                }
            }
        }

        var yLabelMaxWidth = 0f
        paint.setTextSize(mRenderer!!.labelsTextSize * mRenderer!!.zoomRate)
        for (i in 0..<maxScaleNumber) {
            val yLabels = allYLabels.get(i)
            val length = yLabels!!.size
            for (j in 0..<length) {
                val label: Double = yLabels.get(j)!!
                minY[i] = min(minY[i], label)
                maxY[i] = max(maxY[i], label)
                yLabelMaxWidth = max(yLabelMaxWidth, paint.measureText(getLabel(label)))
            }
        }


        left = (left + yLabelMaxWidth).toInt()

        if (this.screenR == null) {
            this.screenR = Rect()
        }
        screenR!!.set(left, top, right, bottom)


        //draw series background and frame
        drawSeriesBackgroundAndFrame(mRenderer!!, canvas, this.screenR!!, paint)

        //    paint.setColor(mRenderer.getSeriesBackgroundColor());
//    canvas.drawRect(mScreenR, paint);
        val xPixelsPerUnit = DoubleArray(maxScaleNumber)
        val yPixelsPerUnit = DoubleArray(maxScaleNumber)
        for (i in 0..<sLength) {
            val series = mDataset!!.getSeriesAt(i)
            val scale = series.scaleNumber
            if (series.itemCount == 0) {
                continue
            }
            if (!isMinXSet[scale]) {
                val minimumX = series.minX
                minX[scale] = min(minX[scale], minimumX)
                mCalcRange.get(scale)!![0] = minX[scale]
            }
            if (!isMaxXSet[scale]) {
                val maximumX = series.maxX
                maxX[scale] = max(maxX[scale], maximumX)
                mCalcRange.get(scale)!![1] = maxX[scale]
            }
            if (!isMinYSet[scale]) {
                val minimumY = series.minY
                minY[scale] = min(minY[scale], minimumY.toFloat().toDouble())
                mCalcRange.get(scale)!![2] = minY[scale]
            }
            if (!isMaxYSet[scale]) {
                val maximumY = series.maxY
                maxY[scale] = max(maxY[scale], maximumY.toFloat().toDouble())
                mCalcRange.get(scale)!![3] = maxY[scale]
            }
        }

        for (i in 0..<maxScaleNumber) {
            if (maxX[i] - minX[i] != 0.0) {
                xPixelsPerUnit[i] = (right - left) / (maxX[i] - minX[i])
            }
            if (maxY[i] - minY[i] != 0.0) {
                yPixelsPerUnit[i] = ((bottom - top) / (maxY[i] - minY[i])).toFloat().toDouble()
            }
        }


        //draw title, label, grid
        val off = max(mRenderer!!.zoomRate / 2, 0.5f)
        var hasValues = false
        for (i in 0..<sLength) {
            if (mDataset!!.getSeriesAt(i).itemCount > 0) {
                hasValues = true
                break
            }
        }
        val showLabels = mRenderer!!.isShowLabels && hasValues
        val showGrid = mRenderer!!.isShowGridH
        val showCustomTextGrid = mRenderer!!.isShowCustomTextGrid
        if (showLabels || showGrid) {
            val xLabels: MutableList<Double>?
            if (this.chartType != ScatterChart.Companion.TYPE) {
                xLabels = ArrayList<Double>()
                var xLabel = minX[0] + 1
                while (xLabel <= maxX[0]) {
                    xLabels.add(floor(xLabel))
                    xLabel += 1.0
                }
            } else {
                xLabels =
                    getValidLabels(MathHelper.getLabels(minX[0], maxX[0], mRenderer!!.xLabels))
                minX[0] = xLabels.get(0)!!
                maxX[0] = xLabels.get(xLabels.size - 1)!!
                xPixelsPerUnit[0] = (right - left) / (maxX[0] - minX[0])
            }


            //draw x text label
            var xLabelsLeft = left
            if (showLabels) {
                paint.setColor(mRenderer!!.labelsColor)
                paint.setTextSize(mRenderer!!.labelsTextSize * mRenderer!!.zoomRate)
                paint.setTextAlign(mRenderer!!.xLabelsAlign)
                if (mRenderer!!.xLabelsAlign == Paint.Align.LEFT) {
                    xLabelsLeft = (xLabelsLeft + mRenderer!!.labelsTextSize / 4).toInt()
                }
            }

            var yAxeX = bottom.toFloat()
            if (minY[0] < 0) {
                yAxeX = (bottom + yPixelsPerUnit[0] * minY[0]).toFloat()
            }

            if (this.chartType != ScatterChart.Companion.TYPE) {
                drawXLabels(
                    xLabels, mRenderer!!.xTextLabelLocations, canvas, paint, xLabelsLeft, top,
                    yAxeX, xPixelsPerUnit[0], minX[0]
                )
            } else {
                drawXLabels(
                    xLabels, null, canvas, paint, xLabelsLeft, top,
                    yAxeX, xPixelsPerUnit[0], minX[0]
                )
            }


            //draw y text label      
            for (i in 0..<maxScaleNumber) {
                paint.setTextAlign(mRenderer!!.getYLabelsAlign(i))
                val yLabels = allYLabels.get(i)
                if (abs(yLabels!!.get(0)!! - minY[0]) > 0.000001f) {
                    yLabels.add(minY[0])
                }

                val length = yLabels.size
                for (j in 0..<length) {
                    val label: Double = yLabels.get(j)!!
                    val axisAlign = mRenderer!!.getYAxisAlign(i)
                    val textLabel = mRenderer!!.getYTextLabel(label, i) != null
                    val yLabel = (bottom - yPixelsPerUnit[i] * (label - minY[i])).toFloat()
                    if (or == XYMultipleSeriesRenderer.Orientation.HORIZONTAL) {
                        if (showLabels && !textLabel) {
                            paint.setColor(mRenderer!!.labelsColor)
                            if (axisAlign == Paint.Align.LEFT) {
                                drawText(
                                    canvas,
                                    getLabel(label),
                                    left - paint.measureText(getLabel(label)),
                                    yLabel,
                                    paint,
                                    mRenderer!!.yLabelsAngle
                                )
                            } else {
                                drawText(
                                    canvas,
                                    getLabel(label),
                                    right.toFloat(),
                                    yLabel - 2,
                                    paint,
                                    mRenderer!!.yLabelsAngle
                                )
                            }
                        }
                        if (showGrid) {
                            paint.setColor(mRenderer!!.gridColor)
                            canvas.drawRect(
                                left.toFloat(),
                                yLabel - off,
                                right.toFloat(),
                                yLabel + off,
                                paint
                            )
                        }
                    } else if (or == XYMultipleSeriesRenderer.Orientation.VERTICAL) {
                        if (showLabels && !textLabel) {
                            paint.setColor(mRenderer!!.labelsColor)
                            //canvas.drawLine(right - getLabelLinePos(axisAlign), yLabel, right, yLabel, paint);
                            drawText(
                                canvas,
                                getLabel(label),
                                (right + 10).toFloat(),
                                yLabel - 2,
                                paint,
                                mRenderer!!.yLabelsAngle
                            )
                        }
                        if (showGrid) {
                            paint.setColor(mRenderer!!.gridColor)
                            canvas.drawRect(
                                right.toFloat(),
                                (Math.round(yLabel) - 1).toFloat(),
                                left.toFloat(),
                                Math.round(yLabel).toFloat(),
                                paint
                            )
                        }
                    }
                }
            }

            if (showLabels) {
                paint.setColor(mRenderer!!.labelsColor)
                for (i in 0..<maxScaleNumber) {
                    val axisAlign = mRenderer!!.getYAxisAlign(i)
                    val yTextLabelLocations = mRenderer!!.getYTextLabelLocations(i)
                    for (location in yTextLabelLocations) {
                        if (minY[i] <= location && location <= maxY[i]) {
                            val yLabel = (bottom - yPixelsPerUnit[i]
                                    * (location - minY[i])).toFloat()
                            val label = mRenderer!!.getYTextLabel(location, i)
                            paint.setColor(mRenderer!!.labelsColor)
                            if (or == XYMultipleSeriesRenderer.Orientation.HORIZONTAL) {
                                if (axisAlign == Paint.Align.LEFT) {
                                    canvas.drawLine(
                                        (left + getLabelLinePos(axisAlign)).toFloat(),
                                        yLabel,
                                        left.toFloat(),
                                        yLabel,
                                        paint
                                    )
                                    drawText(
                                        canvas,
                                        label!!,
                                        left.toFloat(),
                                        yLabel - 2,
                                        paint,
                                        mRenderer!!.yLabelsAngle
                                    )
                                } else {
                                    canvas.drawLine(
                                        right.toFloat(),
                                        yLabel,
                                        (right + getLabelLinePos(axisAlign)).toFloat(),
                                        yLabel,
                                        paint
                                    )
                                    drawText(
                                        canvas,
                                        label!!,
                                        right.toFloat(),
                                        yLabel - 2,
                                        paint,
                                        mRenderer!!.yLabelsAngle
                                    )
                                }
                                if (showCustomTextGrid) {
                                    paint.setColor(mRenderer!!.gridColor)
                                    canvas.drawLine(
                                        left.toFloat(),
                                        yLabel,
                                        right.toFloat(),
                                        yLabel,
                                        paint
                                    )
                                }
                            } else {
                                canvas.drawLine(
                                    (right - getLabelLinePos(axisAlign)).toFloat(),
                                    yLabel,
                                    right.toFloat(),
                                    yLabel,
                                    paint
                                )
                                drawText(
                                    canvas,
                                    label!!,
                                    (right + 10).toFloat(),
                                    yLabel - 2,
                                    paint,
                                    mRenderer!!.yLabelsAngle
                                )
                                if (showCustomTextGrid) {
                                    paint.setColor(mRenderer!!.gridColor)
                                    canvas.drawLine(
                                        right.toFloat(),
                                        yLabel,
                                        left.toFloat(),
                                        yLabel,
                                        paint
                                    )
                                }
                            }
                        }
                    }
                }
            }


            //draw title
            if (showLabels) {
                paint.setColor(mRenderer!!.labelsColor)
                paint.setTextAlign(Paint.Align.CENTER)
                paint.setFakeBoldText(true)

                if (or == XYMultipleSeriesRenderer.Orientation.HORIZONTAL) {
                    //draw chart title
                    if (mRenderer!!.isShowChartTitle) {
                        paint.setTextSize(mRenderer!!.chartTitleTextSize * mRenderer!!.zoomRate)
                        val maxTitleAreaSize = getMaxTitleAreaSize(width, height)
                        drawTitle(
                            canvas,
                            mRenderer!!.chartTitle!!,
                            1.0f,
                            (x + width / 2).toFloat(),
                            y + mRenderer!!.chartTitleTextSize * mRenderer!!.zoomRate * 2,
                            maxTitleAreaSize.width.toFloat(),
                            maxTitleAreaSize.height.toFloat(),
                            paint,
                            0f
                        )
                    }


                    //draw y title
                    if (yTitleAreaSize != null) {
                        paint.setTextSize(mRenderer!!.yTitleTextSize * mRenderer!!.zoomRate)
                        val maxWidth = height * 0.8f
                        val maxHeight = width * 0.2f
                        var yTitleTop = y.toFloat()
                        if (titleAreaSize != null && yTitleAreaSize.height == maxWidth.toInt()) {
                            yTitleTop = (y + titleAreaSize.height + height / 2).toFloat()
                        } else {
                            yTitleTop = (y + height / 2).toFloat()
                        }
                        drawTitle(
                            canvas,
                            mRenderer!!.yTitle!!,
                            1.0f,
                            x + mRenderer!!.yTitleTextSize * mRenderer!!.zoomRate * 1.5f,
                            yTitleTop,
                            maxWidth,
                            maxHeight,
                            paint,
                            -90f
                        )
                    }


                    //draw x title
                    if (xTitleAreaSize != null) {
                        val maxWidth = width * 0.8f
                        val maxHeight = height * 0.2f
                        paint.setTextSize(mRenderer!!.xTitleTextSize * mRenderer!!.zoomRate)
                        fm = paint.getFontMetrics()
                        var xTitleLeft = x.toFloat()
                        var yTitleTop = (y + height - xTitleAreaSize.height).toFloat()
                        if (yTitleAreaSize != null) {
                            xTitleLeft = (x + (width + yTitleAreaSize.width) / 2).toFloat()
                        } else {
                            xTitleLeft = (x + width / 2).toFloat()
                        }

                        if (legendSize != null && (legendPosition == AbstractChart.Companion.LegendPosition_Top || legendPosition == AbstractChart.Companion.LegendPosition_Bottom)) {
                            yTitleTop =
                                (y + height - legendSize.height - xTitleAreaSize.height).toFloat()
                        }
                        drawTitle(
                            canvas,
                            mRenderer!!.xTitle!!,
                            1.0f,
                            xTitleLeft,
                            yTitleTop + fm.descent,
                            maxWidth,
                            maxHeight,
                            paint,
                            0f
                        )
                    }
                } else if (or == XYMultipleSeriesRenderer.Orientation.VERTICAL) {
                    drawText(
                        canvas,
                        mRenderer!!.xTitle!!,
                        (x + width / 2).toFloat(),
                        (y + height).toFloat(),
                        paint,
                        -90f
                    )
                    drawText(
                        canvas,
                        mRenderer!!.yTitle!!,
                        right + 20 * mRenderer!!.zoomRate,
                        (y + height / 2).toFloat(),
                        paint,
                        0f
                    )
                    paint.setTextSize(mRenderer!!.chartTitleTextSize * mRenderer!!.zoomRate)
                    drawText(
                        canvas,
                        mRenderer!!.chartTitle!!,
                        x.toFloat(),
                        (top + height / 2).toFloat(),
                        paint,
                        0f
                    )
                }

                paint.setFakeBoldText(false)
            }
        }


        //draw series    
        for (i in 0..<sLength) {
            val series = mDataset!!.getSeriesAt(i)
            val scale = series.scaleNumber
            if (series.itemCount == 0) {
                continue
            }
            val seriesRenderer = mRenderer!!.getSeriesRendererAt(i)
            val originalValuesLength = series.itemCount
            val valuesLength = originalValuesLength
            val length = valuesLength * 2
            val points: MutableList<Float> = ArrayList<Float>()
            var j = 0
            while (j < length) {
                val index = j / 2
                val yValue = series.getY(index)
                if (yValue != MathHelper.NULL_VALUE) {
                    points.add((left + xPixelsPerUnit[scale] * (series.getX(index) - minX[scale])).toFloat())
                    points.add((bottom - yPixelsPerUnit[scale] * (yValue - minY[scale])).toFloat())
                } else {
                    if (points.size > 0) {
                        drawSeries(
                            series, canvas, paint, points, seriesRenderer, min(
                                bottom.toFloat(),
                                (bottom + yPixelsPerUnit[scale] * minY[scale]).toFloat()
                            ), i, or
                        )
                        points.clear()
                    }
                }
                j += 2
            }

            if (points.size > 0) {
                drawSeries(
                    series, canvas, paint, points, seriesRenderer, min(
                        bottom.toFloat(),
                        (bottom + yPixelsPerUnit[scale] * minY[scale]).toFloat()
                    ), i, or
                )

                paint.setStyle(Paint.Style.FILL)
            }
        }

        // draw stuff over the margins such as data doesn't render on these areas
        drawBackground(
            mRenderer, canvas, x, bottom, width, height - bottom, paint, true, mRenderer!!
                .marginsColor
        )
        drawBackground(
            mRenderer, canvas, x, y, width, (margins[0] * height).toInt(), paint, true, mRenderer!!
                .marginsColor
        )
        if (or == XYMultipleSeriesRenderer.Orientation.HORIZONTAL) {
            drawBackground(
                mRenderer, canvas, x, y, left - x, height - y, paint, true, mRenderer!!
                    .marginsColor
            )
            drawBackground(
                mRenderer,
                canvas,
                right,
                y,
                (margins[3] * width).toInt(),
                height - y,
                paint,
                true,
                mRenderer!!
                    .marginsColor
            )
        } else if (or == XYMultipleSeriesRenderer.Orientation.VERTICAL) {
            drawBackground(
                mRenderer, canvas, right, y, width - right, height - y, paint, true, mRenderer!!
                    .marginsColor
            )
            drawBackground(
                mRenderer, canvas, x, y, left - x, height - y, paint, true, mRenderer!!
                    .marginsColor
            )
        }


        //draw legend(series color and name)    
        if (or == XYMultipleSeriesRenderer.Orientation.HORIZONTAL) {
            if (mRenderer!!.isShowLegend) {
                val legendWidth = legendSize!!.width
                val legendHeight2 = min(height, legendSize.height)
                var legendLeft = x
                var legendTop = y
                when (legendPosition) {
                    AbstractChart.Companion.LegendPosition_Right, AbstractChart.Companion.LegendPosition_Left -> {
                        legendLeft =
                            x + width - legendWidth - (mRenderer!!.legendTextSize * mRenderer!!.zoomRate).toInt()
                        if (titleAreaSize != null) {
                            legendTop = y + (height + titleAreaSize.height) / 2
                        } else {
                            legendTop = y + (height - legendHeight2) / 2
                        }
                    }

                    AbstractChart.Companion.LegendPosition_Top, AbstractChart.Companion.LegendPosition_Bottom -> {
                        legendLeft = x + (width - legendWidth) / 2
                        legendTop = y + height - legendHeight2
                    }
                }

                drawLegend(
                    canvas,
                    mRenderer,
                    titles,
                    legendLeft,
                    legendTop,
                    legendWidth,
                    legendHeight2,
                    paint,
                    false
                )
            }
        } else if (or == XYMultipleSeriesRenderer.Orientation.VERTICAL) {
            transform(canvas, angle.toFloat(), true)
            drawLegend(
                canvas,
                mRenderer,
                titles,
                x + SSConstant.SHEET_SPACETOBORDER,
                y,
                width,
                height,
                paint,
                false
            )
            transform(canvas, angle.toFloat(), false)
        }


        //chart axes
        if (mRenderer!!.isShowAxes) {
            paint.setColor(mRenderer!!.axesColor)
            paint.setFakeBoldText(true)


            //x axe
            var yAxeX = bottom.toFloat()
            if (minY[0] < 0) {
                yAxeX = (bottom + yPixelsPerUnit[0] * minY[0]).toFloat()
            }

            canvas.drawRect(
                left.toFloat(),
                Math.round(bottom.toFloat()) - off,
                right.toFloat(),
                Math.round(bottom.toFloat()) + off,
                paint
            )
            var rightAxis = false
            var i = 0
            while (i < maxScaleNumber && !rightAxis) {
                rightAxis = mRenderer!!.getYAxisAlign(i) == Paint.Align.RIGHT
                i++
            }


            //y axe
            if (or == XYMultipleSeriesRenderer.Orientation.HORIZONTAL) {
                canvas.drawRect(left - off, top.toFloat(), left + off, bottom.toFloat(), paint)
                if (rightAxis) {
                    canvas.drawRect(
                        right - off,
                        top.toFloat(),
                        right + off,
                        bottom.toFloat(),
                        paint
                    )
                }
            } else if (or == XYMultipleSeriesRenderer.Orientation.VERTICAL) {
                canvas.drawRect(right - off, top.toFloat(), right + off, bottom.toFloat(), paint)
            }

            paint.setFakeBoldText(false)
        }

        if (rotate) {
            transform(canvas, angle.toFloat(), true)
        }

        paint.setColor(preColor)
        paint.setStrokeWidth(preSize)

        canvas.restore()
    }

    private fun getValidLabels(labels: MutableList<Double>): MutableList<Double> {
        val result: MutableList<Double> = ArrayList<Double>(labels)
        for (label in labels) {
            if (label.isNaN()) {
                result.remove(label)
            }
        }
        return result
    }

    open fun drawSeries(
        series: XYSeries,
        canvas: Canvas,
        paint: Paint,
        pointsList: MutableList<Float>,
        seriesRenderer: SimpleSeriesRenderer,
        yAxisValue: Float,
        seriesIndex: Int,
        or: XYMultipleSeriesRenderer.Orientation?
    ) {
        val stroke = seriesRenderer.stroke
        val cap = paint.getStrokeCap()
        val join = paint.getStrokeJoin()
        val miter = paint.getStrokeMiter()
        val pathEffect = paint.getPathEffect()
        val style = paint.getStyle()
        if (stroke != null) {
            var effect: PathEffect? = null
            if (stroke.intervals != null) {
                effect = DashPathEffect(stroke.intervals, stroke.phase)
            }
            setStroke(
                stroke.cap,
                stroke.join,
                stroke.miter,
                Paint.Style.FILL_AND_STROKE,
                effect,
                paint
            )
        }

        val points = MathHelper.getFloats(pointsList)
        drawSeries(canvas, paint, points, seriesRenderer, yAxisValue, seriesIndex)

        if (isRenderPoints(seriesRenderer)) {
            val pointsChart = this.pointsChart
            if (pointsChart != null) {
                pointsChart.drawSeries(
                    canvas,
                    paint,
                    points,
                    seriesRenderer,
                    yAxisValue,
                    seriesIndex
                )
            }
        }

        paint.setTextSize(seriesRenderer.chartValuesTextSize)
        if (or == XYMultipleSeriesRenderer.Orientation.HORIZONTAL) {
            paint.setTextAlign(Paint.Align.CENTER)
        } else {
            paint.setTextAlign(Paint.Align.LEFT)
        }

        if (seriesRenderer.isDisplayChartValues) {
            drawChartValuesText(canvas, series, paint, points, seriesIndex)
        }

        if (stroke != null) {
            setStroke(cap, join, miter, style, pathEffect, paint)
        }
    }

    private fun setStroke(
        cap: Cap?,
        join: Join?,
        miter: Float,
        style: Paint.Style?,
        pathEffect: PathEffect?,
        paint: Paint
    ) {
        paint.setStrokeCap(cap)
        paint.setStrokeJoin(join)
        paint.setStrokeMiter(miter)
        paint.setPathEffect(pathEffect)
        paint.setStyle(style)
    }

    /**
     * The graphical representation of the series values as text.
     * 
     * @param canvas the canvas to paint to
     * @param series the series to be painted
     * @param paint the paint to be used for drawing
     * @param points the array of points to be used for drawing the series
     * @param seriesIndex the index of the series currently being drawn
     */
    protected open fun drawChartValuesText(
        canvas: Canvas, series: XYSeries, paint: Paint, points: FloatArray,
        seriesIndex: Int
    ) {
        var k = 0
        while (k < points.size) {
            drawText(
                canvas,
                getLabel(series.getY(k / 2)),
                points[k],
                points[k + 1] - 3.5f,
                paint,
                0f
            )
            k += 2
        }
    }

    /**
     * The graphical representation of a text, to handle both HORIZONTAL and
     * VERTICAL orientations and extra rotation angles.
     * 
     * @param canvas the canvas to paint to
     * @param text the text to be rendered
     * @param x the X axis location of the text
     * @param y the Y axis location of the text
     * @param paint the paint to be used for drawing
     * @param extraAngle the text angle
     */
    protected fun drawText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        paint: Paint,
        extraAngle: Float
    ) {
        val angle = -mRenderer!!.orientation.angle + extraAngle
        if (angle != 0f) {
            // canvas.scale(1 / mScale, mScale);
            canvas.rotate(angle, x, y)
        }
        canvas.drawText(text, x, y, paint)
        if (angle != 0f) {
            canvas.rotate(-angle, x, y)
            // canvas.scale(mScale, 1 / mScale);
        }
    }

    private fun getXTitleTextAreaSize(
        chartWidth: Int,
        chartHeight: Int,
        paint: Paint?
    ): Rectangle? {
        if (mRenderer!!.xTitle!!.length > 0) {
            val maxWidth = chartWidth * 0.8f
            val maxHeight = chartHeight * 0.2f
            return getTextSize(
                mRenderer!!.xTitle,
                mRenderer!!.xTitleTextSize * mRenderer!!.zoomRate,
                maxWidth,
                maxHeight,
                paint!!
            )
        } else {
            return null
        }
    }

    private fun getYTitleTextAreaSize(
        chartWidth: Int,
        chartHeight: Int,
        paint: Paint?
    ): Rectangle? {
        if (mRenderer!!.yTitle!!.length > 0) {
            //rotate 90
            val maxWidth = chartHeight * 0.8f
            val maxHeight = chartWidth * 0.2f
            val size = getTextSize(
                mRenderer!!.yTitle,
                mRenderer!!.xTitleTextSize * mRenderer!!.zoomRate,
                maxWidth,
                maxHeight,
                paint!!
            )

            val w = size!!.width
            size!!.width = size!!.height
            size!!.height = w

            return size
        } else {
            return null
        }
    }

    /**
     * Transform the canvas such as it can handle both HORIZONTAL and VERTICAL
     * orientations.
     * 
     * @param canvas the canvas to paint to
     * @param angle the angle of rotation
     * @param inverse if the inverse transform needs to be applied
     */
    private fun transform(canvas: Canvas, angle: Float, inverse: Boolean) {
        if (inverse) {
            canvas.scale(1 / mScale, mScale)
            canvas.translate(mTranslate, -mTranslate)
            canvas.rotate(-angle, mCenter!!.x, mCenter!!.y)
        } else {
            canvas.rotate(angle, mCenter!!.x, mCenter!!.y)
            canvas.translate(-mTranslate, mTranslate)
            canvas.scale(mScale, 1 / mScale)
        }
    }

    /**
     * Makes sure the fraction digit is not displayed, if not needed.
     * 
     * @param label the input label value
     * @return the label without the useless fraction digit
     */
    protected fun getLabel(label: Double): String {
        var text = ""
        if (label == Math.round(label).toDouble()) {
            text = Math.round(label).toString() + ""
        } else {
            text = label.toString() + ""
        }
        return text
    }

    /**
     * The graphical representation of the labels on the X axis.
     * 
     * @param xLabels the X labels values
     * @param xTextLabelLocations the X text label locations
     * @param canvas the canvas to paint to
     * @param paint the paint to be used for drawing
     * @param left the left value of the labels area
     * @param top the top value of the labels area
     * @param bottom the bottom value of the labels area
     * @param xPixelsPerUnit the amount of pixels per one unit in the chart labels
     * @param minX the minimum value on the X axis in the chart
     */
    protected open fun drawXLabels(
        xLabels: MutableList<Double>, xTextLabelLocations: Array<Double>?, canvas: Canvas,
        paint: Paint, left: Int, top: Int, bottom: Float, xPixelsPerUnit: Double, minX: Double
    ) {
        val length = xLabels.size
        val showLabels = mRenderer!!.isShowLabels
        val showGrid = mRenderer!!.isShowGridV
        val showCustomTextGrid = mRenderer!!.isShowCustomTextGrid
        val off = max(mRenderer!!.zoomRate / 2, 0.5f)
        if (xTextLabelLocations == null || xTextLabelLocations.size == 0) {
            //for scatter
            for (i in 0..<length) {
                val label: Double = xLabels.get(i)!!
                val xLabel = (left + xPixelsPerUnit * (label - minX)).toFloat()

                if (showGrid) {
                    canvas.drawRect(
                        xLabel - off,
                        top.toFloat(),
                        xLabel + off,
                        bottom + 4 * mRenderer!!.zoomRate,
                        paint
                    )
                } else {
                    canvas.drawRect(
                        xLabel - off,
                        bottom,
                        xLabel + off,
                        bottom + 4 * mRenderer!!.zoomRate,
                        paint
                    )
                }

                drawText(
                    canvas,
                    getLabel(label),
                    xLabel,
                    bottom + mRenderer!!.labelsTextSize * 4 / 3 * mRenderer!!.zoomRate,
                    paint,
                    mRenderer!!.xLabelsAngle
                )

                if (showCustomTextGrid) {
                    paint.setColor(mRenderer!!.gridColor)
                    canvas.drawRect(
                        xLabel + xPixelsPerUnit.toFloat() / 2 - off,
                        bottom,
                        xLabel + xPixelsPerUnit.toFloat() / 2 + off,
                        top.toFloat(),
                        paint
                    )
                }
            }
        } else if (showLabels) {
            paint.setColor(mRenderer!!.labelsColor)
            for (location in xTextLabelLocations) {
                val xLabel = (left + xPixelsPerUnit * (location - minX)).toFloat()
                paint.setColor(mRenderer!!.labelsColor)
                if (showGrid) {
                    canvas.drawRect(
                        xLabel + xPixelsPerUnit.toFloat() / 2 - off,
                        top.toFloat(),
                        xLabel + xPixelsPerUnit.toFloat() / 2 + off,
                        bottom + 4 * mRenderer!!.zoomRate,
                        paint
                    )
                } else {
                    canvas.drawRect(
                        xLabel + xPixelsPerUnit.toFloat() / 2 - off,
                        bottom,
                        xLabel + xPixelsPerUnit.toFloat() / 2 + off,
                        bottom + 4 * mRenderer!!.zoomRate,
                        paint
                    )
                }

                drawText(
                    canvas,
                    mRenderer!!.getXTextLabel(location)!!,
                    xLabel,
                    bottom
                            + mRenderer!!.labelsTextSize * mRenderer!!.zoomRate,
                    paint,
                    mRenderer!!.xLabelsAngle
                )
                if (showCustomTextGrid) {
                    paint.setColor(mRenderer!!.gridColor)
                    //canvas.drawLine(xLabel, bottom, xLabel, top, paint);
                    canvas.drawRect(
                        xLabel + xPixelsPerUnit.toFloat() / 2 - off,
                        bottom,
                        xLabel + xPixelsPerUnit.toFloat() / 2 + off,
                        top.toFloat(),
                        paint
                    )
                }
            }
        }
    }

    val renderer: XYMultipleSeriesRenderer
        // TODO: docs
        get() = mRenderer!!

    val dataset: XYMultipleSeriesDataset?
        get() = if (::mDataset.isInitialized) mDataset else null

    fun getCalcRange(scale: Int): DoubleArray? {
        return mCalcRange.get(scale)
    }

    fun setCalcRange(range: DoubleArray?, scale: Int) {
        mCalcRange.put(scale, range!!)
    }

    private fun getLabelLinePos(align: Paint.Align?): Int {
        var pos = 4
        if (align == Paint.Align.LEFT) {
            pos = -pos
        }
        return pos
    }

    /**
     * Transforms a screen point to a real coordinates point.
     * 
     * @param screenX the screen x axis value
     * @param screenY the screen y axis value
     * @return the real coordinates point
     */
    @JvmOverloads
    fun toRealPoint(screenX: Float, screenY: Float, scale: Int = 0): DoubleArray {
        val realMinX = mRenderer!!.getXAxisMin(scale)
        val realMaxX = mRenderer!!.getXAxisMax(scale)
        val realMinY = mRenderer!!.getYAxisMin(scale)
        val realMaxY = mRenderer!!.getYAxisMax(scale)
        return doubleArrayOf(
            (screenX - screenR!!.left) * (realMaxX - realMinX) / screenR!!.width() + realMinX,
            (screenR!!.top + screenR!!.height() - screenY) * (realMaxY - realMinY) / screenR!!.height()
                    + realMinY
        )
    }

    @JvmOverloads
    fun toScreenPoint(realPoint: DoubleArray, scale: Int = 0): DoubleArray {
        var realMinX = mRenderer!!.getXAxisMin(scale)
        var realMaxX = mRenderer!!.getXAxisMax(scale)
        var realMinY = mRenderer!!.getYAxisMin(scale)
        var realMaxY = mRenderer!!.getYAxisMax(scale)
        if (!mRenderer!!.isMinXSet(scale) || !mRenderer!!.isMaxXSet(scale) || !mRenderer!!.isMinXSet(
                scale
            ) || !mRenderer!!.isMaxYSet(scale)
        ) {
            val calcRange = getCalcRange(scale)
            realMinX = calcRange!![0]
            realMaxX = calcRange[1]
            realMinY = calcRange[2]
            realMaxY = calcRange[3]
        }
        return doubleArrayOf(
            (realPoint[0] - realMinX) * screenR!!.width() / (realMaxX - realMinX) + screenR!!.left,
            (realMaxY - realPoint[1]) * screenR!!.height() / (realMaxY - realMinY) + screenR!!.top
        )
    }

    /**
     * The graphical representation of a series.
     * 
     * @param canvas the canvas to paint to
     * @param paint the paint to be used for drawing
     * @param points the array of points to be used for drawing the series
     * @param seriesRenderer the series renderer
     * @param yAxisValue the minimum value of the y axis
     * @param seriesIndex the index of the series currently being drawn
     */
    abstract fun drawSeries(
        canvas: Canvas, paint: Paint, points: FloatArray,
        seriesRenderer: SimpleSeriesRenderer, yAxisValue: Float, seriesIndex: Int
    )

    /**
     * Returns if the chart should display the points as a certain shape.
     * 
     * @param renderer the series renderer
     */
    open fun isRenderPoints(renderer: SimpleSeriesRenderer): Boolean {
        return false
    }

    open val defaultMinimum: Double
        /**
         * Returns the default axis minimum.
         * 
         * @return the default axis minimum
         */
        get() = MathHelper.NULL_VALUE

    open val pointsChart: ScatterChart?
        /**
         * Returns the scatter chart to be used for drawing the data points.
         * 
         * @return the data points scatter chart
         */
        get() = null

    /**
     * Returns the chart type identifier.
     * 
     * @return the chart type
     */
    abstract val chartType: String?

    companion object {
        /** The legend shape width.  */
        protected const val SHAPE_WIDTH: Int = 12
    }
}
