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
import android.graphics.RectF
import com.wxiwei.office.system.IControl
import com.wxiwei.office.thirdpart.achartengine.model.MultipleCategorySeries
import com.wxiwei.office.thirdpart.achartengine.renderers.DefaultRenderer
import com.wxiwei.office.thirdpart.achartengine.renderers.SimpleSeriesRenderer
import kotlin.math.abs
import kotlin.math.min

/**
 * The doughnut chart rendering class.
 */
class DoughnutChart
/**
 * Builds a new pie chart instance.
 * 
 * @param dataset the series dataset
 * @param renderer the series renderer
 */(
    /** The series dataset.  */
    private val mMultipleDataset: MultipleCategorySeries, renderer: DefaultRenderer?
) : RoundChart(null, renderer!!) {
    /** A step variable to control the size of the legend shape.  */
    private var mStep = 0

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
        paint.setAntiAlias(mRenderer.isAntialiasing)
        paint.setStyle(Paint.Style.FILL)
        paint.setTextSize(mRenderer.labelsTextSize)
        var legendSize = mRenderer.legendHeight
        if (mRenderer.isShowLegend && legendSize == 0) {
            legendSize = height / 5
        }
        val left = x
        val top = y
        val right = x + width
        val cLength = mMultipleDataset.categoriesCount
        val categories = arrayOfNulls<String>(cLength)
        for (category in 0..<cLength) {
            categories[category] = mMultipleDataset.getCategory(category)
        }
        if (mRenderer.isFitLegend) {
            legendSize = drawLegend(
                canvas, mRenderer, categories, left, y, width, height,
                paint, true
            )
        }

        val bottom = y + height - legendSize
        drawBackground(
            mRenderer,
            canvas,
            x,
            y,
            width,
            height,
            paint,
            false,
            DefaultRenderer.Companion.NO_COLOR
        )
        mStep = RoundChart.Companion.SHAPE_WIDTH * 3 / 4

        val mRadius = min(abs(right - left), abs(bottom - top))
        val rCoef = 0.35 * mRenderer.scale
        val decCoef = 0.2 / cLength
        var radius = (mRadius * rCoef).toInt()
        val centerX = (left + right) / 2
        val centerY = (bottom + top) / 2
        var shortRadius = radius * 0.9f
        val longRadius = radius * 1.1f
        val prevLabelsBounds: MutableList<RectF> = ArrayList<RectF>()
        for (category in 0..<cLength) {
            val sLength = mMultipleDataset.getItemCount(category)
            var total = 0.0
            val titles = arrayOfNulls<String>(sLength)
            for (i in 0..<sLength) {
                total += mMultipleDataset.getValues(category)[i]
                titles[i] = mMultipleDataset.getTitles(category)[i]
            }
            var currentAngle = 0f
            var oval = RectF(
                (centerX - radius).toFloat(),
                (centerY - radius).toFloat(),
                (centerX + radius).toFloat(),
                (centerY + radius).toFloat()
            )
            for (i in 0..<sLength) {
                paint.setColor(mRenderer.getSeriesRendererAt(i).color)
                val value = mMultipleDataset.getValues(category)[i].toFloat()
                val angle = (value / total * 360).toFloat()
                canvas.drawArc(oval, currentAngle, angle, true, paint)
                drawLabel(
                    canvas, mMultipleDataset.getTitles(category)[i]!!, mRenderer, prevLabelsBounds, centerX,
                    centerY, shortRadius, longRadius, currentAngle, angle, left, right, paint
                )
                currentAngle += angle
            }
            radius = (radius - mRadius * decCoef).toInt()
            shortRadius -= (mRadius * decCoef - 2).toFloat()
            if (mRenderer.backgroundColor != 0) {
                paint.setColor(mRenderer.backgroundColor)
            } else {
                paint.setColor(Color.WHITE)
            }
            paint.setStyle(Paint.Style.FILL)
            oval = RectF(
                (centerX - radius).toFloat(),
                (centerY - radius).toFloat(),
                (centerX + radius).toFloat(),
                (centerY + radius).toFloat()
            )
            canvas.drawArc(oval, 0f, 360f, true, paint)
            radius -= 1
        }
        prevLabelsBounds.clear()
        drawLegend(
            canvas, mRenderer, categories, left, y, width, height, paint,
            false
        )
    }

    /**
     * Returns the legend shape width.
     * 
     * @param seriesIndex the series index
     * @return the legend shape width
     */
    override fun getLegendShapeWidth(seriesIndex: Int): Int {
        return RoundChart.Companion.SHAPE_WIDTH
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
        mStep--
        canvas.drawCircle(x + RoundChart.Companion.SHAPE_WIDTH - mStep, y, mStep.toFloat(), paint)
    }
}
