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
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import com.wxiwei.office.thirdpart.achartengine.model.XYMultipleSeriesDataset
import com.wxiwei.office.thirdpart.achartengine.model.XYSeries
import com.wxiwei.office.thirdpart.achartengine.renderers.SimpleSeriesRenderer
import com.wxiwei.office.thirdpart.achartengine.renderers.XYMultipleSeriesRenderer
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * The bar chart rendering class.
 */
class BarChart : XYChart {
    /** The chart type.  */
    protected var mType: Type? = Type.DEFAULT

    /**
     * The bar chart type enum.
     */
    enum class Type {
        DEFAULT, STACKED
    }

    internal constructor()

    /**
     * Builds a new bar chart instance.
     * 
     * @param dataset the multiple series dataset
     * @param renderer the multiple series renderer
     * @param type the bar chart type
     */
    constructor(
        dataset: XYMultipleSeriesDataset?,
        renderer: XYMultipleSeriesRenderer?,
        type: Type?
    ) : super(dataset!!, renderer!!) {
        mType = type
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
    override fun drawSeries(
        canvas: Canvas, paint: Paint, points: FloatArray,
        seriesRenderer: SimpleSeriesRenderer, yAxisValue: Float, seriesIndex: Int
    ) {
        val seriesNr = mDataset.seriesCount
        val length = points.size
        paint.setColor(seriesRenderer.color)
        paint.setStyle(Paint.Style.FILL)
        val halfDiffX = getHalfDiffX(points, length, seriesNr)
        var i = 0
        while (i < length) {
            val x = points[i]
            val y = points[i + 1]
            drawBar(canvas, x, yAxisValue, x, y, halfDiffX, seriesNr, seriesIndex, paint)
            i += 2
        }
        paint.setColor(seriesRenderer.color)
    }

    protected fun drawBar(
        canvas: Canvas, xMin: Float, yMin: Float, xMax: Float, yMax: Float,
        halfDiffX: Float, seriesNr: Int, seriesIndex: Int, paint: Paint
    ) {
        val scale = mDataset.getSeriesAt(seriesIndex).scaleNumber
        if (mType == Type.STACKED) {
            drawBar(
                canvas, xMin - halfDiffX, yMax, xMax + halfDiffX, yMin, scale, seriesIndex,
                paint
            )
        } else {
            val startX = xMin - seriesNr * halfDiffX + seriesIndex * 2 * halfDiffX
            drawBar(canvas, startX, yMax, startX + 2 * halfDiffX, yMin, scale, seriesIndex, paint)
        }
    }

    private fun drawBar(
        canvas: Canvas, xMin: Float, yMin: Float, xMax: Float, yMax: Float, scale: Int,
        seriesIndex: Int, paint: Paint
    ) {
        val renderer = mRenderer.getSeriesRendererAt(seriesIndex)
        if (renderer.isGradientEnabled) {
            val minY = toScreenPoint(
                doubleArrayOf(0.0, renderer.gradientStopValue),
                scale
            )[1].toFloat()
            val maxY = toScreenPoint(
                doubleArrayOf(0.0, renderer.gradientStartValue),
                scale
            )[1].toFloat()
            val gradientMinY = max(minY, yMin)
            val gradientMaxY = min(maxY, yMax)
            val gradientMinColor = renderer.gradientStopColor
            val gradientMaxColor = renderer.gradientStartColor
            var gradientStartColor = gradientMinColor
            var gradientStopColor = gradientMaxColor

            if (yMin < minY) {
                paint.setColor(gradientMaxColor)
                canvas.drawRect(
                    Math.round(xMin).toFloat(),
                    Math.round(yMin).toFloat(),
                    Math.round(xMax).toFloat(),
                    Math.round(gradientMinY).toFloat(),
                    paint
                )
            } else {
                gradientStopColor = getGradientPartialColor(
                    gradientMaxColor, gradientMinColor,
                    (maxY - gradientMinY) / (maxY - minY)
                )
            }
            if (yMax > maxY) {
                paint.setColor(gradientMinColor)
                canvas.drawRect(
                    Math.round(xMin).toFloat(),
                    Math.round(gradientMaxY).toFloat(),
                    Math.round(xMax).toFloat(),
                    Math.round(yMax).toFloat(),
                    paint
                )
            } else {
                gradientStartColor = getGradientPartialColor(
                    gradientMinColor, gradientMaxColor,
                    (gradientMaxY - minY) / (maxY - minY)
                )
            }
            val gradient = GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                intArrayOf(gradientStartColor, gradientStopColor)
            )
            gradient.setBounds(
                Math.round(xMin), Math.round(gradientMinY), Math.round(xMax),
                Math.round(gradientMaxY)
            )
            gradient.draw(canvas)
        } else {
            if (abs(yMax - yMin) < 0.0000001f) {
                return
            }

            canvas.drawRect(
                Math.round(xMin).toFloat(),
                Math.round(yMin).toFloat(),
                Math.round(xMax).toFloat(),
                Math.round(yMax).toFloat(),
                paint
            )


            val color = paint.getColor()
            paint.setColor(Color.BLACK)
            paint.setStyle(Paint.Style.STROKE)

            canvas.drawRect(
                Math.round(xMin).toFloat(),
                Math.round(yMin).toFloat(),
                Math.round(xMax).toFloat(),
                Math.round(yMax).toFloat(),
                paint
            )
            paint.setStyle(Paint.Style.FILL)
            paint.setColor(color)
        }
    }

    private fun getGradientPartialColor(minColor: Int, maxColor: Int, fraction: Float): Int {
        val alpha = Math.round(
            fraction * Color.alpha(minColor) + (1 - fraction)
                    * Color.alpha(maxColor)
        )
        val r = Math.round(fraction * Color.red(minColor) + (1 - fraction) * Color.red(maxColor))
        val g = Math.round(
            fraction * Color.green(minColor) + (1 - fraction)
                    * Color.green(maxColor)
        )
        val b = Math.round(
            fraction * Color.blue(minColor) + (1 - fraction)
                    * Color.blue((maxColor))
        )
        return Color.argb(alpha, r, g, b)
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
    override fun drawChartValuesText(
        canvas: Canvas, series: XYSeries, paint: Paint, points: FloatArray,
        seriesIndex: Int
    ) {
        val seriesNr = mDataset.seriesCount
        val halfDiffX = getHalfDiffX(points, points.size, seriesNr)
        var k = 0
        while (k < points.size) {
            var x = points[k]
            if (mType == Type.DEFAULT) {
                x += seriesIndex * 2 * halfDiffX - (seriesNr - 1.5f) * halfDiffX
            }
            drawText(canvas, getLabel(series.getY(k / 2)), x, points[k + 1] - 3.5f, paint, 0f)
            k += 2
        }
    }

    /**
     * Returns the legend shape width.
     * 
     * @param seriesIndex the series index
     * @return the legend shape width
     */
    override fun getLegendShapeWidth(seriesIndex: Int): Int {
        return renderer.legendTextSize.toInt() /*SHAPE_WIDTH*/
    }

    /**
     * The graphical representation of the legend shape.
     * 
     * @param canvas the canvas to paint to
     * @param renderer the series renderer
     * @param x the x value of the point the shape should be drawn at
     * @param y the y value of the point the shape should be drawn at
     * @param seriesIndex the series index
     * @param paint the paint to be used for drawing
     */
    override fun drawLegendShape(
        canvas: Canvas, renderer: SimpleSeriesRenderer, x: Float, y: Float,
        seriesIndex: Int, paint: Paint
    ) {
        var x = x
        val shapeWidth = mRenderer.legendTextSize * mRenderer.zoomRate
        val halfShapeWidth = shapeWidth / 2
        x += halfShapeWidth

        canvas.drawRect(x, y - halfShapeWidth, x + shapeWidth, y + halfShapeWidth, paint)
        //draw legend shape frame
        paint.setStyle(Paint.Style.STROKE)
        paint.setColor(Color.BLACK)
        canvas.drawRect(x, y - halfShapeWidth, x + shapeWidth, y + halfShapeWidth, paint)
        paint.setStyle(Paint.Style.FILL)
    }

    /**
     * Calculates and returns the half-distance in the graphical representation of
     * 2 consecutive points.
     * 
     * @param points the points
     * @param length the points length
     * @param seriesNr the series number
     * @return the calculated half-distance value
     */
    protected fun getHalfDiffX(points: FloatArray, length: Int, seriesNr: Int): Float {
        var div = length
        if (length > 2) {
            div = length - 2
        }
        var halfDiffX = (points[length - 2] - points[0]) / div
        if (halfDiffX == 0f) {
            halfDiffX = (screenR!!.width() / 2).toFloat()
        }

        if (mType != Type.STACKED) {
            halfDiffX /= (seriesNr + 1).toFloat()
        }
        return (halfDiffX / (this.coeficient * (1 + mRenderer.barSpacing))).toFloat()
    }

    protected open val coeficient: Float
        /**
         * Returns the value of a constant used to calculate the half-distance.
         * 
         * @return the constant value
         */
        get() = 1f

    /**
     * Returns the default axis minimum.
     * 
     * @return the default axis minimum
     */
    override val defaultMinimum: Double
        get() = 0.0

    /**
     * Returns the chart type identifier.
     * 
     * @return the chart type
     */
    override val chartType: String
        get() = TYPE

    companion object {
        /** The constant to identify this chart type.  */
        const val TYPE: String = "Bar"
    }
}
