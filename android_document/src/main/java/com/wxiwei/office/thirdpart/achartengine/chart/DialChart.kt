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
import com.wxiwei.office.system.IControl
import com.wxiwei.office.thirdpart.achartengine.model.CategorySeries
import com.wxiwei.office.thirdpart.achartengine.renderers.DefaultRenderer
import com.wxiwei.office.thirdpart.achartengine.renderers.DialRenderer
import com.wxiwei.office.thirdpart.achartengine.util.MathHelper
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * The dial chart rendering class.
 */
class DialChart
/**
 * Builds a new pie chart instance.
 * 
 * @param dataset the series dataset
 * @param renderer the dial renderer
 */(
    dataset: CategorySeries?,
    /** The series renderer.  */
    private val mDialRenderer: DialRenderer
) : RoundChart(dataset, mDialRenderer) {
    /**
     * The graphical representation of the dial chart.
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
        paint.setAntiAlias(mDialRenderer.isAntialiasing)
        paint.setStyle(Paint.Style.FILL)
        paint.setTextSize(mDialRenderer.labelsTextSize)
        var legendSize = mDialRenderer.legendHeight
        if (mDialRenderer.isShowLegend && legendSize == 0) {
            legendSize = height / 5
        }
        val left = x
        val top = y
        val right = x + width

        val sLength = mDataset!!.itemCount
        var total = 0.0
        val titles = arrayOfNulls<String>(sLength)
        for (i in 0..<sLength) {
            total += mDataset!!.getValue(i)
            titles[i] = mDataset!!.getCategory(i)
        }

        if (mDialRenderer.isFitLegend) {
            legendSize = drawLegend(
                canvas, mDialRenderer, titles, left, y, width, height,
                paint, true
            )
        }
        val bottom = y + height - legendSize
        drawBackground(
            mDialRenderer,
            canvas,
            x,
            y,
            width,
            height,
            paint,
            false,
            DefaultRenderer.Companion.NO_COLOR
        )

        val mRadius = min(abs(right - left), abs(bottom - top))
        val radius = (mRadius * 0.35 * mDialRenderer.scale).toInt()
        val centerX = (left + right) / 2
        val centerY = (bottom + top) / 2
        val shortRadius = radius * 0.9f
        val longRadius = radius * 1.1f
        var min = mDialRenderer.minValue
        var max = mDialRenderer.maxValue
        val angleMin = mDialRenderer.angleMin
        val angleMax = mDialRenderer.angleMax
        if (!mDialRenderer.isMinValueSet || !mDialRenderer.isMaxValueSet) {
            val count = mDialRenderer.seriesRendererCount
            for (i in 0..<count) {
                val value = mDataset!!.getValue(i)
                if (!mDialRenderer.isMinValueSet) {
                    min = min(min, value)
                }
                if (!mDialRenderer.isMaxValueSet) {
                    max = max(max, value)
                }
            }
        }
        if (min == max) {
            min = min * 0.5
            max = max * 1.5
        }

        paint.setColor(mDialRenderer.labelsColor)
        var minorTicks = mDialRenderer.minorTicksSpacing
        var majorTicks = mDialRenderer.majorTicksSpacing
        if (minorTicks == MathHelper.NULL_VALUE) {
            minorTicks = (max - min) / 30
        }
        if (majorTicks == MathHelper.NULL_VALUE) {
            majorTicks = (max - min) / 10
        }
        drawTicks(
            canvas,
            min,
            max,
            angleMin,
            angleMax,
            centerX,
            centerY,
            longRadius.toDouble(),
            radius.toDouble(),
            minorTicks,
            paint,
            false
        )
        drawTicks(
            canvas,
            min,
            max,
            angleMin,
            angleMax,
            centerX,
            centerY,
            longRadius.toDouble(),
            shortRadius.toDouble(),
            majorTicks,
            paint,
            true
        )

        val count = mDialRenderer.seriesRendererCount
        for (i in 0..<count) {
            val angle = getAngleForValue(mDataset!!.getValue(i), angleMin, angleMax, min, max)
            paint.setColor(mDialRenderer.getSeriesRendererAt(i).color)
            val type = mDialRenderer.getVisualTypeForIndex(i) == DialRenderer.Type.ARROW
            drawNeedle(canvas, angle, centerX, centerY, shortRadius.toDouble(), type, paint)
        }
        drawLegend(canvas, mDialRenderer, titles, left, y, width, height, paint, false)
    }

    /**
     * Returns the angle for a specific chart value.
     * 
     * @param value the chart value
     * @param minAngle the minimum chart angle value
     * @param maxAngle the maximum chart angle value
     * @param min the minimum chart value
     * @param max the maximum chart value
     * @return the angle
     */
    private fun getAngleForValue(
        value: Double, minAngle: Double, maxAngle: Double, min: Double,
        max: Double
    ): Double {
        val angleDiff = maxAngle - minAngle
        val diff = max - min
        return Math.toRadians(minAngle + (value - min) * angleDiff / diff)
    }

    /**
     * Draws the chart tick lines.
     * 
     * @param canvas the canvas
     * @param min the minimum chart value
     * @param max the maximum chart value
     * @param minAngle the minimum chart angle value
     * @param maxAngle the maximum chart angle value
     * @param centerX the center x value
     * @param centerY the center y value
     * @param longRadius the long radius
     * @param shortRadius the short radius
     * @param ticks the tick spacing
     * @param paint the paint settings
     * @param labels paint the labels
     * @return the angle
     */
    private fun drawTicks(
        canvas: Canvas,
        min: Double,
        max: Double,
        minAngle: Double,
        maxAngle: Double,
        centerX: Int,
        centerY: Int,
        longRadius: Double,
        shortRadius: Double,
        ticks: Double,
        paint: Paint,
        labels: Boolean
    ) {
        var i = min
        while (i <= max) {
            val angle = getAngleForValue(i, minAngle, maxAngle, min, max)
            val sinValue = sin(angle)
            val cosValue = cos(angle)
            val x1 = Math.round(centerX + (shortRadius * sinValue).toFloat())
            val y1 = Math.round(centerY + (shortRadius * cosValue).toFloat())
            val x2 = Math.round(centerX + (longRadius * sinValue).toFloat())
            val y2 = Math.round(centerY + (longRadius * cosValue).toFloat())
            canvas.drawLine(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat(), paint)
            if (labels) {
                paint.setTextAlign(Paint.Align.LEFT)
                if (x1 <= x2) {
                    paint.setTextAlign(Paint.Align.RIGHT)
                }
                var text = i.toString() + ""
                if (Math.round(i) == i.toLong()) {
                    text = i.toLong().toString() + ""
                }
                canvas.drawText(text, x1.toFloat(), y1.toFloat(), paint)
            }
            i += ticks
        }
    }

    /**
     * Returns the angle for a specific chart value.
     * 
     * @param canvas the canvas
     * @param angle the needle angle value
     * @param centerX the center x value
     * @param centerY the center y value
     * @param radius the radius
     * @param arrow if a needle or an arrow to be painted
     * @param paint the paint settings
     * @return the angle
     */
    private fun drawNeedle(
        canvas: Canvas, angle: Double, centerX: Int, centerY: Int, radius: Double,
        arrow: Boolean, paint: Paint
    ) {
        val diff = Math.toRadians(90.0)
        val needleSinValue = (NEEDLE_RADIUS * sin(angle - diff)).toInt()
        val needleCosValue = (NEEDLE_RADIUS * cos(angle - diff)).toInt()
        val needleX = (radius * sin(angle)).toInt()
        val needleY = (radius * cos(angle)).toInt()
        val needleCenterX = centerX + needleX
        val needleCenterY = centerY + needleY
        val points: FloatArray?
        if (arrow) {
            val arrowBaseX = centerX + (radius * 0.85 * sin(angle)).toInt()
            val arrowBaseY = centerY + (radius * 0.85 * cos(angle)).toInt()
            points = floatArrayOf(
                (arrowBaseX - needleSinValue).toFloat(),
                (arrowBaseY - needleCosValue).toFloat(),
                needleCenterX.toFloat(),
                needleCenterY.toFloat(),
                (arrowBaseX + needleSinValue).toFloat(),
                (arrowBaseY + needleCosValue).toFloat()
            )
            val width = paint.getStrokeWidth()
            paint.setStrokeWidth(5f)
            canvas.drawLine(
                centerX.toFloat(),
                centerY.toFloat(),
                needleCenterX.toFloat(),
                needleCenterY.toFloat(),
                paint
            )
            paint.setStrokeWidth(width)
        } else {
            points = floatArrayOf(
                (centerX - needleSinValue).toFloat(),
                (centerY - needleCosValue).toFloat(),
                needleCenterX.toFloat(),
                needleCenterY.toFloat(),
                (centerX + needleSinValue).toFloat(),
                (centerY + needleCosValue).toFloat()
            )
        }
        drawPath(canvas, points, paint, true)
    }

    companion object {
        /** The radius of the needle.  */
        private const val NEEDLE_RADIUS = 10
    }
}
