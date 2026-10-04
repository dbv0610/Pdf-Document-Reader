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
import android.graphics.Paint
import com.wxiwei.office.thirdpart.achartengine.model.XYMultipleSeriesDataset
import com.wxiwei.office.thirdpart.achartengine.model.XYValueSeries
import com.wxiwei.office.thirdpart.achartengine.renderers.SimpleSeriesRenderer
import com.wxiwei.office.thirdpart.achartengine.renderers.XYMultipleSeriesRenderer
import com.wxiwei.office.thirdpart.achartengine.renderers.XYSeriesRenderer

/**
 * The bubble chart rendering class.
 */
class BubbleChart : XYChart {
    internal constructor()

    /**
     * Builds a new bubble chart instance.
     * 
     * @param dataset the multiple series dataset
     * @param renderer the multiple series renderer
     */
    constructor(dataset: XYMultipleSeriesDataset?, renderer: XYMultipleSeriesRenderer?) : super(
        dataset!!,
        renderer!!
    )

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
        val renderer = seriesRenderer as XYSeriesRenderer
        paint.setColor(renderer.color)
        paint.setStyle(Paint.Style.FILL)
        val length = points.size
        val series = mDataset.getSeriesAt(seriesIndex) as XYValueSeries
        val max = series.maxValue

        val coef: Double = MAX_BUBBLE_SIZE / max
        var i = 0
        while (i < length) {
            val size: Double = series.getValue(i / 2) * coef + MIN_BUBBLE_SIZE
            drawCircle(canvas, paint, points[i], points[i + 1], size.toFloat())
            i += 2
        }
    }

    /**
     * Returns the legend shape width.
     * 
     * @param seriesIndex the series index
     * @return the legend shape width
     */
    override fun getLegendShapeWidth(seriesIndex: Int): Int {
        return SHAPE_WIDTH
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
        paint.setStyle(Paint.Style.FILL)
        drawCircle(canvas, paint, x + SHAPE_WIDTH, y, 3f)
    }

    /**
     * The graphical representation of a circle point shape.
     * 
     * @param canvas the canvas to paint to
     * @param paint the paint to be used for drawing
     * @param x the x value of the point the shape should be drawn at
     * @param y the y value of the point the shape should be drawn at
     * @param radius the bubble radius
     */
    private fun drawCircle(canvas: Canvas, paint: Paint, x: Float, y: Float, radius: Float) {
        canvas.drawCircle(x, y, radius, paint)
    }

    /**
     * Returns the chart type identifier.
     * 
     * @return the chart type
     */
    override val chartType: String
        get() = TYPE

    companion object {
        /** The constant to identify this chart type.  */
        const val TYPE: String = "Bubble"

        /** The legend shape width.  */
        private const val SHAPE_WIDTH = 10

        /** The minimum bubble size.  */
        private const val MIN_BUBBLE_SIZE = 2

        /** The maximum bubble size.  */
        private const val MAX_BUBBLE_SIZE = 20
    }
}
