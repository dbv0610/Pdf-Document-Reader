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

/**
 * The scatter chart rendering class.
 */
class ScatterChart : XYChart {
    /** The point shape size.  */
    private var size: Float = SIZE

    var isDrawFrame: Boolean = true
        private set

    internal constructor()

    /**
     * Builds a new scatter chart instance.
     * 
     * @param dataset the multiple series dataset
     * @param renderer the multiple series renderer
     */
    constructor(dataset: XYMultipleSeriesDataset?, renderer: XYMultipleSeriesRenderer) : super(
        dataset!!,
        renderer
    ) {
        size = renderer.pointSize
    }

    // TODO: javadoc
    override fun setDatasetRenderer(
        dataset: XYMultipleSeriesDataset,
        renderer: XYMultipleSeriesRenderer
    ) {
        super.setDatasetRenderer(dataset, renderer)
        size = renderer.pointSize
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
        val renderer = seriesRenderer as XYSeriesRenderer
        paint.setColor(renderer.color)
        if (renderer.isFillPoints) {
            paint.setStyle(Paint.Style.FILL)
        } else {
            paint.setStyle(Paint.Style.STROKE)
        }
        val length = points.size
        when (renderer.pointStyle) {
            PointStyle.X -> {
                var i = 0
                while (i < length) {
                    drawX(canvas, paint, points[i], points[i + 1])
                    i += 2
                }
            }

            PointStyle.CIRCLE -> {
                var i = 0
                while (i < length) {
                    drawCircle(canvas, paint, points[i], points[i + 1])
                    i += 2
                }
            }

            PointStyle.TRIANGLE -> {
                val path = FloatArray(6)
                var i = 0
                while (i < length) {
                    drawTriangle(canvas, paint, path, points[i], points[i + 1])
                    i += 2
                }
            }

            PointStyle.SQUARE -> {
                var i = 0
                while (i < length) {
                    drawSquare(canvas, paint, points[i], points[i + 1])
                    i += 2
                }
            }

            PointStyle.DIAMOND -> {
                val path = FloatArray(8)
                var i = 0
                while (i < length) {
                    drawDiamond(canvas, paint, path, points[i], points[i + 1])
                    i += 2
                }
            }

            PointStyle.POINT -> canvas.drawPoints(points, paint)
            null -> {}
        }
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
        var x = x
        if ((renderer as XYSeriesRenderer).isFillPoints) {
            paint.setStyle(Paint.Style.FILL)
        } else {
            paint.setStyle(Paint.Style.STROKE)
        }

        val shapeWidth = mRenderer.legendTextSize.toInt() * mRenderer.zoomRate
        x += shapeWidth / 2

        when (renderer.pointStyle) {
            PointStyle.X -> drawX(canvas, paint, x, y)
            PointStyle.CIRCLE -> drawCircle(canvas, paint, x, y)
            PointStyle.TRIANGLE -> drawTriangle(canvas, paint, FloatArray(6), x, y)
            PointStyle.SQUARE -> drawSquare(canvas, paint, x, y)
            PointStyle.DIAMOND -> drawDiamond(canvas, paint, FloatArray(8), x, y)
            PointStyle.POINT -> canvas.drawPoint(x, y, paint)
            null -> {}
        }
        //    if(drawFrame)
//    {
//        float halfShapeWidth =  shapeWidth/ 2;
//        x -= halfShapeWidth;
//        //draw legend shape frame
//        paint.setStyle(Style.STROKE);
//        paint.setColor(Color.BLACK);
//        paint.setAlpha(255);
//        canvas.drawRect(Math.round(x), y - halfShapeWidth, x + shapeWidth, y + halfShapeWidth, paint);
//        paint.setStyle(Style.FILL);
//    }    
    }

    /**
     * The graphical representation of an X point shape.
     * 
     * @param canvas the canvas to paint to
     * @param paint the paint to be used for drawing
     * @param x the x value of the point the shape should be drawn at
     * @param y the y value of the point the shape should be drawn at
     */
    private fun drawX(canvas: Canvas, paint: Paint, x: Float, y: Float) {
        val temSize = size * mRenderer.zoomRate
        canvas.drawLine(x - temSize, y - temSize, x + temSize, y + temSize, paint)
        canvas.drawLine(x + temSize, y - temSize, x - temSize, y + temSize, paint)
    }

    /**
     * The graphical representation of a circle point shape.
     * 
     * @param canvas the canvas to paint to
     * @param paint the paint to be used for drawing
     * @param x the x value of the point the shape should be drawn at
     * @param y the y value of the point the shape should be drawn at
     */
    private fun drawCircle(canvas: Canvas, paint: Paint, x: Float, y: Float) {
        val temSize = size * mRenderer.zoomRate
        canvas.drawCircle(x, y, temSize, paint)
    }

    /**
     * The graphical representation of a triangle point shape.
     * 
     * @param canvas the canvas to paint to
     * @param paint the paint to be used for drawing
     * @param path the triangle path
     * @param x the x value of the point the shape should be drawn at
     * @param y the y value of the point the shape should be drawn at
     */
    private fun drawTriangle(canvas: Canvas?, paint: Paint?, path: FloatArray, x: Float, y: Float) {
        val temSize = size * mRenderer.zoomRate
        path[0] = x
        path[1] = y - temSize - temSize / 2
        path[2] = x - temSize
        path[3] = y + temSize
        path[4] = x + temSize
        path[5] = path[3]
        drawPath(canvas!!, path, paint!!, true)
    }

    /**
     * The graphical representation of a square point shape.
     * 
     * @param canvas the canvas to paint to
     * @param paint the paint to be used for drawing
     * @param x the x value of the point the shape should be drawn at
     * @param y the y value of the point the shape should be drawn at
     */
    private fun drawSquare(canvas: Canvas, paint: Paint, x: Float, y: Float) {
        val temSize = size * mRenderer.zoomRate
        canvas.drawRect(x - temSize, y - temSize, x + temSize, y + temSize, paint)
    }

    /**
     * The graphical representation of a diamond point shape.
     * 
     * @param canvas the canvas to paint to
     * @param paint the paint to be used for drawing
     * @param path the diamond path
     * @param x the x value of the point the shape should be drawn at
     * @param y the y value of the point the shape should be drawn at
     */
    private fun drawDiamond(canvas: Canvas?, paint: Paint?, path: FloatArray, x: Float, y: Float) {
        val temSize = size * mRenderer.zoomRate
        path[0] = x
        path[1] = y - temSize
        path[2] = x - temSize
        path[3] = y
        path[4] = x
        path[5] = y + temSize
        path[6] = x + temSize
        path[7] = y
        drawPath(canvas!!, path, paint!!, true)
    }

    /**
     * Returns the chart type identifier.
     * 
     * @return the chart type
     */
    override val chartType: String
        get() = TYPE

    fun setDrawFrameFlag(drawFrame: Boolean) {
        this.isDrawFrame = drawFrame
    }

    companion object {
        /** The constant to identify this chart type.  */
        const val TYPE: String = "Scatter"

        /** The default point shape size.  */
        private const val SIZE = 3f
    }
}
