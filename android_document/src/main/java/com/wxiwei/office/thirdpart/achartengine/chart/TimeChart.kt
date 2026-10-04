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
import com.wxiwei.office.thirdpart.achartengine.renderers.XYMultipleSeriesRenderer
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

/**
 * The time chart rendering class.
 */
class TimeChart : LineChart {
    /**
     * Returns the date format pattern to be used for formatting the X axis
     * labels.
     * 
     * @return the date format pattern for the X axis labels
     */
    /**
     * Sets the date format pattern to be used for formatting the X axis labels.
     * 
     * @param format the date format pattern for the X axis labels. If null, an
     * appropriate default format will be used.
     */
    /** The date format pattern to be used in formatting the X axis labels.  */
    var dateFormat: String? = null

    internal constructor()

    /**
     * Builds a new time chart instance.
     * 
     * @param dataset the multiple series dataset
     * @param renderer the multiple series renderer
     */
    constructor(dataset: XYMultipleSeriesDataset?, renderer: XYMultipleSeriesRenderer?) : super(
        dataset,
        renderer
    )

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
    override fun drawXLabels(
        xLabels: MutableList<Double>, xTextLabelLocations: Array<Double>?, canvas: Canvas,
        paint: Paint, left: Int, top: Int, bottom: Float, xPixelsPerUnit: Double, minX: Double
    ) {
        val length = xLabels.size
        if (length > 0) {
            val showLabels = mRenderer.isShowLabels
            val showGrid = mRenderer.isShowGridH
            val format = getDateFormat(xLabels.get(0)!!, xLabels.get(length - 1)!!)
            for (i in 0..<length) {
                val label = Math.round(xLabels.get(i)!!)
                val xLabel = (left + xPixelsPerUnit * (label - minX)).toFloat()
                if (showLabels) {
                    paint.setColor(mRenderer.labelsColor)
                    canvas
                        .drawLine(
                            xLabel,
                            bottom,
                            xLabel,
                            bottom + mRenderer.labelsTextSize / 3,
                            paint
                        )
                    drawText(
                        canvas,
                        format.format(Date(label)),
                        xLabel,
                        bottom
                                + mRenderer.labelsTextSize * 4 / 3,
                        paint,
                        mRenderer.xLabelsAngle
                    )
                }
                if (showGrid) {
                    paint.setColor(mRenderer.gridColor)
                    canvas.drawLine(xLabel, bottom, xLabel, top.toFloat(), paint)
                }
            }
        }
    }

    /**
     * Returns the date format pattern to be used, based on the date range.
     * 
     * @param start the start date in milliseconds
     * @param end the end date in milliseconds
     * @return the date format
     */
    private fun getDateFormat(start: Double, end: Double): DateFormat {
        if (this.dateFormat != null) {
            var format: SimpleDateFormat? = null
            try {
                format = SimpleDateFormat(this.dateFormat)
                return format
            } catch (e: Exception) {
                // do nothing here
            }
        }
        var format = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM)
        val diff = end - start
        if (diff > DAY && diff < 5 * DAY) {
            format =
                SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
        } else if (diff < DAY) {
            format = SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM)
        }
        return format
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
        const val TYPE: String = "Time"

        /** The number of milliseconds in a day.  */
        val DAY: Long = (24 * 60 * 60 * 1000).toLong()
    }
}
