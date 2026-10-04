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
import com.wxiwei.office.thirdpart.achartengine.renderers.SimpleSeriesRenderer
import com.wxiwei.office.thirdpart.achartengine.renderers.XYMultipleSeriesRenderer
import com.wxiwei.office.thirdpart.achartengine.renderers.XYSeriesRenderer
import kotlin.math.max

/**
 * The line chart rendering class.
 */
open class LineChart : XYChart {
    /** The scatter chart to be used to draw the data points.  */
    private var mPointsChart: ScatterChart? = null

    internal constructor()

    /**
     * Builds a new line chart instance.
     * 
     * @param dataset the multiple series dataset
     * @param renderer the multiple series renderer
     */
    constructor(dataset: XYMultipleSeriesDataset?, renderer: XYMultipleSeriesRenderer?) : super(
        dataset!!,
        renderer!!
    ) {
        mPointsChart = ScatterChart(dataset, renderer!!)
    }

    /**
     * Sets the series and the renderer.
     * 
     * @param dataset the series dataset
     * @param renderer the series renderer
     */
    override fun setDatasetRenderer(
        dataset: XYMultipleSeriesDataset,
        renderer: XYMultipleSeriesRenderer
    ) {
        super.setDatasetRenderer(dataset, renderer)
        mPointsChart = ScatterChart(dataset, renderer)
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
        val length = points.size
        val renderer = seriesRenderer as XYSeriesRenderer
        val lineWidth = paint.getStrokeWidth()
        paint.setStrokeWidth(max(renderer.lineWidth * mRenderer.zoomRate, 1.0f))
        if (renderer.isFillBelowLine) {
            paint.setColor(renderer.fillBelowLineColor)
            val pLength = points.size
            val fillPoints = FloatArray(pLength + 4)
            System.arraycopy(points, 0, fillPoints, 0, length)
            fillPoints[0] = points[0] + 1
            fillPoints[length] = fillPoints[length - 2]
            fillPoints[length + 1] = yAxisValue
            fillPoints[length + 2] = fillPoints[0]
            fillPoints[length + 3] = fillPoints[length + 1]
            paint.setStyle(Paint.Style.FILL)
            drawPath(canvas, fillPoints, paint, true)
        }
        paint.setColor(seriesRenderer.color)
        paint.setStyle(Paint.Style.STROKE)
        drawPath(canvas, points, paint, false)
        paint.setStyle(Paint.Style.FILL)
        paint.setStrokeWidth(lineWidth)
    }

    /**
     * Returns the legend shape width.
     * 
     * @param seriesIndex the series index
     * @return the legend shape width
     */
    override fun getLegendShapeWidth(seriesIndex: Int): Int {
        return renderer.legendTextSize.toInt()
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
//	  canvas.drawLine(x, y, x + getLegendShapeWidth(0) * mRenderer.getZoomRate(), y, paint);
        if (isRenderPoints(renderer)) {
            mPointsChart!!.setDrawFrameFlag(false)
            mPointsChart!!.drawLegendShape(canvas, renderer, x, y, seriesIndex, paint)
        }
    }

    /**
     * Returns if the chart should display the points as a certain shape.
     * 
     * @param renderer the series renderer
     */
    override fun isRenderPoints(renderer: SimpleSeriesRenderer): Boolean {
        return (renderer as XYSeriesRenderer).pointStyle != PointStyle.POINT
    }

    /**
     * Returns the scatter chart to be used for drawing the data points.
     * 
     * @return the data points scatter chart
     */
    override val pointsChart: ScatterChart
        get() = mPointsChart!!

    /**
     * Returns the chart type identifier.
     * 
     * @return the chart type
     */
    override val chartType: String
        get() = TYPE

    companion object {
        /** The constant to identify this chart type.  */
        const val TYPE: String = "Line"

        /** The legend shape width.  */
        private const val SHAPE_WIDTH = 30
    }
}
