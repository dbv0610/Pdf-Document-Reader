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
import android.graphics.Rect
import android.graphics.RectF
import com.wxiwei.office.system.IControl
import com.wxiwei.office.thirdpart.achartengine.model.CategorySeries
import com.wxiwei.office.thirdpart.achartengine.renderers.DefaultRenderer
import com.wxiwei.office.thirdpart.achartengine.renderers.SimpleSeriesRenderer
import kotlin.math.abs
import kotlin.math.min

/**
 * The pie chart rendering class.
 */
abstract class RoundChart
/**
 * Builds a new pie chart instance.
 * 
 * @param dataset the series dataset
 * @param renderer the series renderer
 */(
    /** The series dataset.  */
    dataset: CategorySeries?,
    /** The series renderer.  */
    var renderer: DefaultRenderer
) : AbstractChart() {
    /** The series dataset (null in a doughnut chart, which has its own).  */
    protected var mDataset: CategorySeries? = dataset

    protected val mRenderer: DefaultRenderer
        get() = renderer

    /**
     * Returns the renderer.
     * 
     * @return the renderer
     */

    /**
     * 
     * (non-Javadoc)
     * @see AbstractChart.setZoomRate
     */
    override var zoomRate: Float
        get() = renderer.zoomRate
        set(rate) {
            this.renderer.zoomRate = rate
        }

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
        val rect = Rect(x, y, x + width, y + height)
        canvas.save()
        canvas.clipRect(rect)

        paint.setAntiAlias(renderer.isAntialiasing)
        paint.setStyle(Paint.Style.FILL)
        paint.setTextSize(renderer.labelsTextSize)
        var legendSize = renderer.legendHeight
        if (renderer.isShowLegend && legendSize == 0) {
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
        if (renderer.isFitLegend) {
            legendSize = drawLegend(
                canvas, this.renderer, titles, left, y, width, height,
                paint, true
            )
        }
        val bottom = y + height - legendSize
        drawBackground(
            this.renderer,
            canvas,
            x,
            y,
            width,
            height,
            paint,
            false,
            DefaultRenderer.Companion.NO_COLOR
        )

        var currentAngle = 0f
        val mRadius = min(abs(right - left), abs(bottom - top))
        val radius = (mRadius * 0.35 * renderer.scale).toInt()
        val centerX = (left + right) / 2
        val centerY = (bottom + top) / 2
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
            paint.setColor(renderer.getSeriesRendererAt(i).color)
            val value = mDataset!!.getValue(i).toFloat()
            val angle = (value / total * 360).toFloat()
            canvas.drawArc(oval, currentAngle, angle, true, paint)
            drawLabel(
                canvas, mDataset!!.getCategory(i)!!,
                this.renderer, prevLabelsBounds, centerX, centerY,
                shortRadius, longRadius, currentAngle, angle, left, right, paint
            )
            currentAngle += angle
        }
        prevLabelsBounds.clear()
        drawLegend(canvas, this.renderer, titles, left, y, width, height, paint, false)

        canvas.restore()
    }

    /**
     * Returns the legend shape width.
     * 
     * @param seriesIndex the series index
     * @return the legend shape width
     */
    override fun getLegendShapeWidth(seriesIndex: Int): Int {
        return this.renderer.legendTextSize.toInt()
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
        val shapeWidth: Float = getLegendShapeWidth(0) * mRenderer.zoomRate
        val halfShapeWidth = shapeWidth / 2
        x += halfShapeWidth
        canvas.drawRect(x, y - halfShapeWidth, x + shapeWidth, y + halfShapeWidth, paint)
        //draw legend shape frame
        paint.setStyle(Paint.Style.STROKE)
        paint.setColor(Color.BLACK)
        canvas.drawRect(
            Math.round(x).toFloat(),
            y - halfShapeWidth,
            x + shapeWidth,
            y + halfShapeWidth,
            paint
        )
        paint.setStyle(Paint.Style.FILL)
    }

    companion object {
        /** The legend shape width.  */
        protected const val SHAPE_WIDTH: Int = 10
    }
}
