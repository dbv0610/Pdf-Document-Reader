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
 * The range bar chart rendering class.
 */
class RangeBarChart : ColumnBarChart {
    internal constructor()

    /**
     * Builds a new range bar chart instance.
     * 
     * @param dataset the multiple series dataset
     * @param renderer the multiple series renderer
     * @param type the range bar chart type
     */
    constructor(
        dataset: XYMultipleSeriesDataset?,
        renderer: XYMultipleSeriesRenderer?,
        type: Type?
    ) : super(dataset, renderer, type)

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
            val xMin = points[i]
            val yMin = points[i + 1]
            // xMin = xMax
            val xMax = points[i + 2]
            val yMax = points[i + 3]
            drawBar(canvas, xMin, yMin, xMax, yMax, halfDiffX, seriesNr, seriesIndex, paint)
            i += 4
        }
        paint.setColor(seriesRenderer.color)
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
            // draw the maximum value
            drawText(canvas, getLabel(series.getY(k / 2 + 1)), x, points[k + 3] - 3f, paint, 0f)
            // draw the minimum value
            drawText(canvas, getLabel(series.getY(k / 2)), x, points[k + 1] + 7.5f, paint, 0f)
            k += 4
        }
    }

    /**
     * Returns the value of a constant used to calculate the half-distance.
     * 
     * @return the constant value
     */
    override val coeficient: Float
        get() = 0.5f

    /**
     * Returns the chart type identifier.
     * 
     * @return the chart type
     */
    override val chartType: String
        get() = TYPE

    companion object {
        /** The chart type.  */
        const val TYPE: String = "RangeBar"
    }
}
