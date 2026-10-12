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
import com.wxiwei.office.thirdpart.achartengine.model.XYSeries
import com.wxiwei.office.thirdpart.achartengine.renderers.SimpleSeriesRenderer
import com.wxiwei.office.thirdpart.achartengine.renderers.XYMultipleSeriesRenderer

/**
 * The combined XY chart rendering class.
 */
class CombinedXYChart(
    dataset: XYMultipleSeriesDataset, renderer: XYMultipleSeriesRenderer,
    types: Array<String?>
) : XYChart(dataset, renderer) {
    /** The embedded XY charts.  */
    private var mCharts: Array<XYChart?>

    /** The supported charts for being combined.  */
    private val xyChartTypes: Array<Class<*>> = arrayOf<Class<*>>(
        TimeChart::class.java,
        LineChart::class.java,
        ColumnBarChart::class.java,
        BubbleChart::class.java,
        LineChart::class.java,
        ScatterChart::class.java,
        RangeBarChart::class.java
    )

    /**
     * Builds a new combined XY chart instance.
     * 
     * @param dataset the multiple series dataset
     * @param renderer the multiple series renderer
     * @param types the XY chart types
     */
    init {
        val length = types.size
        mCharts = arrayOfNulls<XYChart>(length)
        for (i in 0..<length) {
            try {
                mCharts[i] = getXYChart(types[i]!!)
            } catch (e: Exception) {
                // ignore
            }
            requireNotNull(mCharts[i] != null) { "Unknown chart type " + types[i] }
            val newDataset = XYMultipleSeriesDataset()
            newDataset.addSeries(dataset.getSeriesAt(i))
            val newRenderer = XYMultipleSeriesRenderer()
            // TODO: copy other parameters here
            newRenderer.barSpacing = renderer.barSpacing
            newRenderer.pointSize = renderer.pointSize
            val scale = dataset.getSeriesAt(i).scaleNumber
            if (renderer.isMinXSet(scale)) {
                newRenderer.xAxisMin = renderer.getXAxisMin(scale)
            }
            if (renderer.isMaxXSet(scale)) {
                newRenderer.xAxisMax = renderer.getXAxisMax(scale)
            }
            if (renderer.isMinYSet(scale)) {
                newRenderer.yAxisMin = renderer.getYAxisMin(scale)
            }
            if (renderer.isMaxYSet(scale)) {
                newRenderer.yAxisMax = renderer.getYAxisMax(scale)
            }
            newRenderer.addSeriesRenderer(renderer.getSeriesRendererAt(i))
            mCharts[i]!!.setDatasetRenderer(newDataset, newRenderer)
        }
    }

    @Throws(IllegalAccessException::class, InstantiationException::class)
    private fun getXYChart(type: String): XYChart? {
        var chart: XYChart? = null
        val length = xyChartTypes.size
        var i = 0
        while (i < length && chart == null) {
            val newChart = xyChartTypes[i]!!.newInstance() as XYChart
            if (type == newChart.chartType) {
                chart = newChart
            }
            i++
        }
        return chart
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
        mCharts[seriesIndex]!!.screenR = screenR
        mCharts[seriesIndex]!!.setCalcRange(
            getCalcRange(
                mDataset.getSeriesAt(seriesIndex).scaleNumber
            ), 0
        )
        mCharts[seriesIndex]!!.drawSeries(canvas, paint, points, seriesRenderer, yAxisValue, 0)
    }

    override fun drawSeries(
        series: XYSeries,
        canvas: Canvas,
        paint: Paint,
        pointsList: MutableList<Float>,
        seriesRenderer: SimpleSeriesRenderer,
        yAxisValue: Float,
        seriesIndex: Int,
        or: XYMultipleSeriesRenderer.Orientation?
    ) {
        mCharts[seriesIndex]!!.screenR = screenR
        mCharts[seriesIndex]!!.setCalcRange(
            getCalcRange(
                mDataset.getSeriesAt(seriesIndex).scaleNumber
            ), 0
        )
        mCharts[seriesIndex]!!.drawSeries(
            series, canvas, paint, pointsList, seriesRenderer, yAxisValue,
            0, or
        )
    }

    /**
     * Returns the legend shape width.
     * 
     * @param seriesIndex the series index
     * @return the legend shape width
     */
    override fun getLegendShapeWidth(seriesIndex: Int): Int {
        return mCharts[seriesIndex]!!.getLegendShapeWidth(0)
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
        mCharts[seriesIndex]!!.drawLegendShape(canvas, renderer, x, y, 0, paint)
    }

    /**
     * Returns the chart type identifier.
     * 
     * @return the chart type
     */
    override val chartType: String
        get() = "Combined"
}
