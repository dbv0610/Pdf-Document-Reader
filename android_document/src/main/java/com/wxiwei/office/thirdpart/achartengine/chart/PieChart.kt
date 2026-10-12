/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
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
import android.graphics.Rect
import android.graphics.RectF
import com.wxiwei.office.system.IControl
import com.wxiwei.office.thirdpart.achartengine.model.CategorySeries
import com.wxiwei.office.thirdpart.achartengine.renderers.DefaultRenderer
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * The pie chart rendering class.
 */
class PieChart
/**
 * Builds a new pie chart instance.
 * 
 * @param dataset the series dataset
 * @param renderer the series renderer
 */
    (dataset: CategorySeries?, renderer: DefaultRenderer?) : RoundChart(dataset, renderer!!) {
    /** Outline drawn around each slice (c:dPt/c:spPr/a:ln), 0 for none.  */
    @JvmField
    var sliceBorderColor: Int = 0

    /**
     * The graphical representation of the pie chart.
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
        canvas.save()
        canvas.clipRect(x, y, x + width, y + height)

        paint.setAntiAlias(mRenderer.isAntialiasing)
        paint.setStyle(Paint.Style.FILL)
        paint.setTextSize(mRenderer.labelsTextSize)

        drawBackgroundAndFrame(mRenderer, canvas, control, Rect(x, y, x + width, y + height), paint)

        var legendSize = mRenderer.legendHeight
        if (mRenderer.isShowLegend && legendSize == 0) {
            legendSize = height / 5
        }

        val sLength = mDataset!!.itemCount
        var total = 0.0
        val titles = arrayOfNulls<String>(sLength)
        for (i in 0..<sLength) {
            total += mDataset!!.getValue(i)
            titles[i] = mDataset!!.getCategory(i)
        }

        val titleAreaSize = getTitleTextAreaSize(mRenderer, width, height, paint)
        var legendH = height
        if (titleAreaSize != null) {
            legendH -= titleAreaSize.height
        }
        val legendAreaSize = getLegendAutoSize(mRenderer, titles, width, legendH, paint)

        val margins = mRenderer.margins
        val left = x + (margins[1] * width).toInt()
        var top = y + (margins[0] * height).toInt()
        if (titleAreaSize != null) {
            top += titleAreaSize.height
        }
        var right = x + width - (margins[3] * width).toInt()
        if (legendAreaSize != null && (legendPosition == AbstractChart.Companion.LegendPosition_Left || legendPosition == AbstractChart.Companion.LegendPosition_Right)) {
            right -= legendAreaSize.width
        }

        var bottom = y + height - (margins[2] * height).toInt()
        if (legendAreaSize != null && (legendPosition == AbstractChart.Companion.LegendPosition_Top || legendPosition == AbstractChart.Companion.LegendPosition_Bottom)) {
            bottom -= legendAreaSize.height
        }

        val size = mRenderer.legendTextSize * mRenderer.zoomRate
        paint.setTextSize(size)
        paint.setTextAlign(Paint.Align.CENTER)
        paint.setFakeBoldText(true)


        //draw chart title    
        if (mRenderer.isShowChartTitle) {
            paint.setTextSize(mRenderer.chartTitleTextSize * mRenderer.zoomRate)
            val maxTitleAreaSize = getMaxTitleAreaSize(width, height)
            drawTitle(
                canvas,
                mRenderer.chartTitle!!,
                1.0f,
                (x + width / 2).toFloat(),
                y + mRenderer.chartTitleTextSize * mRenderer.zoomRate * 2,
                maxTitleAreaSize.width.toFloat(),
                maxTitleAreaSize.height.toFloat(),
                paint,
                0f
            )
        }

        paint.setFakeBoldText(false)

        if (mRenderer.isShowLegend && legendAreaSize != null && legendPosition == AbstractChart.Companion.LegendPosition_Top) {
            // legend above the pie: the pie takes the space below it
            top = titleBottom(y) + legendAreaSize.height
            bottom = y + height - (margins[2] * height).toInt()
        } else {
            bottom = y + height - legendSize
        }

        var currentAngle = 0f
        val mRadius = min(abs(right - left), abs(bottom - top))
        val radius = (mRadius * 0.35 * mRenderer.scale).toInt()

        val centerX = (left + margins[1] * width + right - margins[3] * width).toInt() / 2
        val centerY = (bottom - margins[2] * height + top + margins[0] * height).toInt() / 2
        val shortRadius = radius * 0.9f
        val longRadius = radius * 1.1f

        val oval = RectF(
            (centerX - radius).toFloat(),
            (centerY - radius).toFloat(),
            (centerX + radius).toFloat(),
            (centerY + radius).toFloat()
        )
        val prevLabelsBounds: MutableList<RectF> = ArrayList<RectF>()
        for (i in 0..<sLength) {
            paint.setColor(mRenderer.getSeriesRendererAt(i).color)
            val value = mDataset!!.getValue(i).toFloat()
            val angle = (value / total * 360).toFloat()
            canvas.drawArc(oval, currentAngle - 90, angle, true, paint)
            if (sliceBorderColor != 0) {
                val style = paint.getStyle()
                paint.setStyle(Paint.Style.STROKE)
                paint.setStrokeWidth(max(1f, mRenderer.zoomRate))
                paint.setColor(sliceBorderColor)
                canvas.drawArc(oval, currentAngle - 90, angle, true, paint)
                paint.setStyle(style)
            }
            //      drawLabel(canvas, mDataset!!.getCategory(i), mRenderer, prevLabelsBounds, centerX, centerY,
//          shortRadius, longRadius, currentAngle - 90, angle, left, right, paint);
            currentAngle += angle
        }


        prevLabelsBounds.clear()
        if (mRenderer.isShowLegend) {
            val legendWidth = legendAreaSize!!.width
            val legendHeight = min(height, legendAreaSize.height)
            var legendLeft = x
            var legendTop = y
            when (legendPosition) {
                AbstractChart.Companion.LegendPosition_Right, AbstractChart.Companion.LegendPosition_Left -> {
                    legendLeft =
                        x + width - legendWidth - (mRenderer.legendTextSize * mRenderer.zoomRate).toInt()
                    if (titleAreaSize != null) {
                        legendTop = y + (height + titleAreaSize.height) / 2
                    } else {
                        legendTop = y + (height - legendHeight) / 2
                    }
                }

                AbstractChart.Companion.LegendPosition_Top -> {
                    legendLeft = x + (width - legendWidth) / 2
                    legendTop = titleBottom(y)
                }

                AbstractChart.Companion.LegendPosition_Bottom -> {
                    legendLeft = x + (width - legendWidth) / 2
                    legendTop = y + height - legendHeight
                }
            }

            drawLegend(
                canvas,
                mRenderer,
                titles,
                legendLeft,
                legendTop,
                legendWidth,
                legendHeight,
                paint,
                false
            )
        }

        canvas.restore()
    }

    /** Bottom of the chart title (drawn with its baseline two text sizes below the top), or y without one.  */
    private fun titleBottom(y: Int): Int {
        if (!mRenderer.isShowChartTitle || mRenderer.chartTitle == null || mRenderer.chartTitle!!.length == 0) {
            return y
        }
        return y + (mRenderer.chartTitleTextSize * mRenderer.zoomRate * 2.5f).toInt()
    }
}
