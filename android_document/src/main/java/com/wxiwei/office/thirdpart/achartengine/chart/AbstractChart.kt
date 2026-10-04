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
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import com.wxiwei.office.common.BackgroundDrawer.drawPathBackground
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.system.IControl
import com.wxiwei.office.thirdpart.achartengine.renderers.DefaultRenderer
import com.wxiwei.office.thirdpart.achartengine.renderers.SimpleSeriesRenderer
import com.wxiwei.office.thirdpart.achartengine.renderers.XYMultipleSeriesRenderer
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * An abstract class to be implemented by the chart rendering classes.
 */
abstract class AbstractChart /* implements Serializable*/ {
    @JvmField
    var categoryAxisTextColor: Int = Color.BLACK
    var legendPosition: Byte = LegendPosition_Right
    private var legendArea: Rectangle? = null

    /**
     * The graphical representation of the chart.
     * 
     * @param canvas the canvas to paint to
     * @param x the top left x value of the view to draw to
     * @param y the top left y value of the view to draw to
     * @param width the width of the view to draw to
     * @param height the height of the view to draw to
     * @param paint the paint
     */
    abstract fun draw(
        canvas: Canvas,
        control: IControl?,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        paint: Paint
    )

    abstract var zoomRate: Float

    /**
     * Draws the chart background.
     * 
     * @param renderer the chart renderer
     * @param canvas the canvas to paint to
     * @param x the top left x value of the view to draw to
     * @param y the top left y value of the view to draw to
     * @param width the width of the view to draw to
     * @param height the height of the view to draw to
     * @param paint the paint used for drawing
     * @param newColor if a new color is to be used
     * @param color the color to be used
     */
    protected fun drawBackground(
        renderer: DefaultRenderer, canvas: Canvas, x: Int, y: Int, width: Int,
        height: Int, paint: Paint, newColor: Boolean, color: Int
    ) {
        if (renderer.isApplyBackgroundColor || newColor) {
            if (newColor) {
                paint.setColor(color)
            } else {
                paint.setColor(renderer.backgroundColor)
            }
            paint.setStyle(Paint.Style.FILL)
            canvas.drawRect(
                x.toFloat(),
                y.toFloat(),
                (x + width).toFloat(),
                (y + height).toFloat(),
                paint
            )
        }
    }

    /**
     * Draws the chart background.
     * 
     * @param renderer the chart renderer
     * @param canvas the canvas to paint to
     * @param x the top left x value of the view to draw to
     * @param y the top left y value of the view to draw to
     * @param width the width of the view to draw to
     * @param height the height of the view to draw to
     * @param paint the paint used for drawing
     * @param newColor if a new color is to be used
     * @param color the color to be used
     */
    protected fun drawBackgroundAndFrame(
        renderer: DefaultRenderer,
        canvas: Canvas,
        control: IControl?,
        rect: Rect,
        paint: Paint
    ) {
        val alpha = paint.getAlpha()
        val path = Path()
        path.addRect(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat(),
            Path.Direction.CCW
        )
        // draw fill
        val fill = renderer.backgroundAndFill
        if (fill != null) {
            paint.setStyle(Paint.Style.FILL)
            drawPathBackground(canvas, control, 1, fill, rect, null, 1.0f, path, paint)
            paint.setAlpha(alpha)
        }


        // draw border
        val frame = renderer.chartFrame
        if (frame != null) {
            paint.setStyle(Paint.Style.STROKE)
            paint.setStrokeWidth(2f)
            if (frame.isDash) {
                val dashPathEffect = DashPathEffect(floatArrayOf(5f, 5f), 10f)
                paint.setPathEffect(dashPathEffect)
            }

            drawPathBackground(
                canvas,
                null,
                1,
                frame.backgroundAndFill,
                rect,
                null,
                1.0f,
                path,
                paint
            )
            paint.setStyle(Paint.Style.FILL)
            paint.setAlpha(alpha)
        }

        paint.reset()
        paint.setAntiAlias(true)
    }

    /**
     * max area size of chart title
     * @param chartWidth
     * @param chartHeight
     * @return
     */
    fun getMaxTitleAreaSize(chartWidth: Int, chartHeight: Int): Rectangle {
        return Rectangle((chartWidth * 0.8f).toInt(), chartHeight / 2)
    }

    fun getTitleTextAreaSize(
        renderer: DefaultRenderer,
        chartWidth: Int,
        chartHeight: Int,
        paint: Paint
    ): Rectangle? {
        if (renderer.isShowChartTitle) {
            val maxWidth = chartWidth * 0.8f
            val maxHeight = chartHeight * 0.5f
            return getTextSize(
                renderer.chartTitle,
                renderer.chartTitleTextSize * renderer.zoomRate,
                maxWidth,
                maxHeight,
                paint
            )
        } else {
            return null
        }
    }

    /**
     * title area height
     * @param renderer
     * @param maxWidth
     * @param maxHeight
     * @param paint
     * @return
     */
    fun getTextSize(
        text: String?,
        fontSize: Float,
        maxWidth: Float,
        maxHeight: Float,
        paint: Paint
    ): Rectangle? {
        if (text == null || text.length <= 0) {
            return null
        }
        var titleHeight = 0f
        paint.setTextSize(fontSize)
        val fm = paint.getFontMetrics()
        val lineHeight = ceil((fm.descent - fm.ascent).toDouble()).toFloat()

        val textWidth = paint.measureText(text)
        if (textWidth < maxWidth) {
            return Rectangle(
                ceil(textWidth.toDouble()).toInt(),
                ceil(lineHeight.toDouble()).toInt()
            )
        } else {
            //multiline
            var title: String? = text
            val charWidth = floatArrayOf()
            while (title!!.length > 0 && titleHeight + lineHeight <= maxHeight) {
                var cnt = paint.breakText(title, true, maxWidth, charWidth)
                if (cnt == 0) {
                    //must has one or more than one char for every line
                    cnt = 1
                }
                val drawedText = title.substring(0, cnt)
                title = title.substring(cnt, title.length)
                if (title.length > 0 && titleHeight + lineHeight * 2 > maxHeight) {
                    titleHeight += lineHeight
                    break
                } else {
                    titleHeight += lineHeight
                }
            }
        }

        return Rectangle(ceil(maxWidth.toDouble()).toInt(), ceil(titleHeight.toDouble()).toInt())
    }

    /**
     * draw chart title
     * @param canvas
     * @param renderer
     * @param maxWidth
     * @param maxHeight
     * @param paint
     * @param extraAngle
     */
    protected fun drawTitle(
        canvas: Canvas,
        title: String,
        zoom: Float,
        x: Float,
        y: Float,
        maxWidth: Float,
        maxHeight: Float,
        paint: Paint,
        angle: Float
    ) {
        var title = title
        var x = x
        var y = y
        var maxWidth = maxWidth
        var maxHeight = maxHeight
        x *= zoom
        y *= zoom
        maxWidth *= zoom
        maxHeight *= zoom

        val initX = x
        val initY = y

        if (angle != 0f) {
            canvas.rotate(angle, initX, initY)
        }

        run {
            val fm = paint.getFontMetrics()
            val lineHeight = ceil((fm.descent - fm.ascent).toDouble()).toFloat()

            val textWidth = paint.measureText(title)
            if (textWidth < maxWidth) {
                canvas.drawText(title, x, y, paint)
            } else {
                //multiline
                val charWidth = floatArrayOf()
                var sum = 0f
                while (title.length > 0 && sum + lineHeight <= maxHeight) {
                    var cnt = paint.breakText(title, true, maxWidth, charWidth)
                    if (cnt == 0) {
                        //must has one or more than one char for every line
                        cnt = 1
                    }
                    var drawedText = title.substring(0, cnt)
                    title = title.substring(cnt, title.length)
                    if (title.length > 0 && sum + lineHeight * 2 > maxHeight) {
                        //the last line, and the rest texts show as "..."
                        drawedText = drawedText.substring(0, drawedText.length - 1) + "..."
                        canvas.drawText(drawedText, x, y + fm.descent, paint)
                        y += lineHeight
                        sum += lineHeight
                        break
                    } else {
                        canvas.drawText(drawedText, x, y + fm.descent, paint)
                        y += lineHeight
                        sum += lineHeight
                    }
                }
            }
        }

        if (angle != 0f) {
            canvas.rotate(-angle, initX, initY)
        }
    }

    /**
     * just for auto layout
     * @return
     */
    fun getMaxLegendWidth(chartWidth: Float): Int {
        if (this.legendPosition == LegendPosition_Left || this.legendPosition == LegendPosition_Right) {
            return Math.round(chartWidth * 0.35f)
        } else {
            return Math.round(chartWidth * 0.9f)
        }
    }

    /**
     * just for auto layout
     * @return
     */
    fun getMaxLegendHeight(chartHeight: Float): Int {
        if (this.legendPosition == LegendPosition_Left || this.legendPosition == LegendPosition_Right) {
            return Math.round(chartHeight * 0.9f)
        } else {
            return Math.round(chartHeight * 0.35f)
        }
    }

    /**
     * max width and height of legend area
     * @param renderer
     * @param titles
     * @param paint
     * @param chartWidth
     * @return
     */
    fun getLegendAutoSize(
        renderer: DefaultRenderer,
        titles: Array<String?>,
        chartWidth: Int,
        chartHeight: Int,
        paint: Paint
    ): Rectangle? {
        if (!renderer.isShowLegend) {
            return null
        }


        var width = -1f
        var height = -1f

        paint.setTextSize(renderer.legendTextSize * renderer.zoomRate)
        val seriesCnt = min(titles.size, renderer.seriesRendererCount)
        for (i in 0..<seriesCnt) {
            val text = titles[i]!!.replace("\n", " ")
            //the rest cell
            val fm = paint.getFontMetrics()

            // 文本高度
            height = max((ceil((fm.descent - fm.ascent).toDouble())).toFloat(), height)

            // 文本宽度
            width = max((paint.measureText(text)), width)
        }

        val textOffset = getLegendShapeWidth(0) * renderer.zoomRate * 2
        val maxLegendHeight = getMaxLegendHeight(chartHeight.toFloat())
        val maxLegendWidth = getMaxLegendWidth(chartWidth.toFloat())
        val maxLegendTextWidth = maxLegendWidth - textOffset
        val singleWidth = ceil((width + textOffset).toDouble()).toInt()
        val singleHeight = ceil(height.toDouble()).toInt()

        if (width > maxLegendTextWidth) {
            //the max width legend shape layout more than one lines
            val lines = ceil((width / maxLegendTextWidth).toDouble()).toInt()
            legendArea = Rectangle(
                maxLegendWidth,
                min(singleHeight * lines * seriesCnt, maxLegendHeight)
            )
        } else {
            when (this.legendPosition) {
                LegendPosition_Left, LegendPosition_Right -> legendArea = Rectangle(
                    singleWidth,
                    min(singleHeight * seriesCnt, maxLegendHeight)
                )

                LegendPosition_Top, LegendPosition_Bottom -> {
                    //max legend count in one line
                    val maxLineCnt = (maxLegendWidth / singleWidth.toFloat()).toInt()
                    if (seriesCnt > maxLineCnt) {
                        //more than one line
                        //get line count
                        var lines = 2
                        var lineCnt = ceil((seriesCnt / lines.toFloat()).toDouble()).toInt()
                        while (lineCnt * singleWidth > maxLegendWidth) {
                            lines = lines + 1
                            lineCnt = ceil((seriesCnt / lines.toFloat()).toDouble()).toInt()
                        }


                        //get legend count in the last line
                        var lastCnt = seriesCnt - seriesCnt / lineCnt * lineCnt
                        while (lastCnt < lineCnt - 1 && ceil((seriesCnt / (lineCnt - 1).toFloat()).toDouble()).toInt() == lines) {
                            lineCnt = lineCnt - 1
                            lastCnt = lines - 1
                        }
                        legendArea = Rectangle(
                            singleWidth * lineCnt,
                            min(singleHeight * lines, maxLegendHeight)
                        )
                    } else {
                        //just one line
                        legendArea = Rectangle(singleWidth * seriesCnt, singleHeight)
                    }
                }

                else -> return null
            }
        }

        return legendArea
    }

    /**
     * single legend width and height
     * @param renderer
     * @param titles
     * @param paint
     * @param chartWidth
     * @return
     */
    fun getSingleAutoLegendSize(
        renderer: DefaultRenderer,
        titles: Array<String?>,
        paint: Paint,
        legendWidth: Int
    ): Rectangle {
        var width = -1f
        var height = -1f

        paint.setTextSize(renderer.legendTextSize * renderer.zoomRate)
        val seriesCnt = min(titles.size, renderer.seriesRendererCount)
        for (i in 0..<seriesCnt) {
            val text = titles[i]!!.replace("\n", " ")
            //the rest cell
            val fm = paint.getFontMetrics()

            // 文本高度
            height = max((ceil((fm.descent - fm.ascent).toDouble())).toFloat(), height)

            // 文本宽度
            width = max((paint.measureText(text)), width)
        }

        val maxLegendTextWidth = legendWidth - getLegendShapeWidth(0) * renderer.zoomRate * 2
        if (width > maxLegendTextWidth) {
            //the max width legend shape layout more than one lines
            val lines = ceil((width / maxLegendTextWidth).toDouble()).toInt()
            return Rectangle(legendWidth, ceil(height.toDouble()).toInt() * lines)
        } else {
            return Rectangle(
                ceil((width + getLegendShapeWidth(0) * renderer.zoomRate * 2).toDouble()).toInt(),
                ceil(height.toDouble()).toInt()
            )
        }
    }

    /**
     * 
     * @param size
     * @return
     */
    private fun getLegendTextOffset(renderer: DefaultRenderer): Float {
        return getLegendShapeWidth(0) * 2 * renderer.zoomRate
    }


    /**
     * Draws the chart legend.
     * 
     * @param canvas the canvas to paint to
     * @param renderer the series renderer
     * @param titles the titles to go to the legend
     * @param left the left X value of the area to draw to
     * @param right the right X value of the area to draw to
     * @param top the y value of the area to draw to
     * @param width the width of the area to draw to
     * @param height the height of the area to draw to
     * @param legendSize the legend size
     * @param paint the paint to be used for drawing
     * @param calculate if only calculating the legend size
     * 
     * @return the legend height
     */
    protected fun drawLegend(
        canvas: Canvas,
        renderer: DefaultRenderer,
        titles: Array<String?>,
        left: Int,
        top: Int,
        width: Int,
        height: Int,
        paint: Paint,
        calculate: Boolean
    ): Int {
        if (renderer.isShowLegend) {
            val singleLegendSize = getSingleAutoLegendSize(renderer, titles, paint, width)

            var currentX = left.toFloat()
            var currentY = top.toFloat()
            val right = (left + width).toFloat()

            paint.setTextAlign(Paint.Align.LEFT)
            paint.setTextSize(renderer.legendTextSize * renderer.zoomRate)
            val fm = paint.getFontMetrics()
            val sLength = min(titles.size, renderer.seriesRendererCount)

            for (i in 0..<sLength) {
                val shapeWidth = getLegendShapeWidth(i) * renderer.zoomRate
                var text = titles[i]!!.replace("\n", " ")

                val sum = paint.measureText(text)
                val textOffset = getLegendTextOffset(renderer)
                val extraSize = textOffset + sum
                when (this.legendPosition) {
                    LegendPosition_Left, LegendPosition_Right -> {
                        //draw legend shape
                        if (titles.size == renderer.seriesRendererCount) {
                            paint.setColor(renderer.getSeriesRendererAt(i)!!.color)
                        } else {
                            paint.setColor(Color.LTGRAY)
                        }
                        drawLegendShape(
                            canvas,
                            renderer.getSeriesRendererAt(i),
                            currentX,
                            currentY,
                            i,
                            paint
                        )


                        //draw legend text
                        paint.setColor(categoryAxisTextColor)

                        if (extraSize > width) {
                            val textWidth = width - textOffset
                            val charWidth = floatArrayOf()
                            while (text.length > 0) {
                                var cnt = paint.breakText(text, true, textWidth, charWidth)
                                if (cnt == 0) {
                                    //must has one or more than one char for every line
                                    cnt = 1
                                }
                                val drawedText = text.substring(0, cnt)
                                text = text.substring(cnt, text.length)

                                canvas.drawText(
                                    drawedText,
                                    currentX + 2 * shapeWidth,
                                    currentY + fm.descent,
                                    paint
                                )
                                currentY += ceil((fm.descent - fm.ascent).toDouble()).toFloat()
                            }
                        } else {
                            canvas.drawText(
                                text,
                                currentX + 2 * shapeWidth,
                                currentY + fm.descent,
                                paint
                            )
                            currentY += singleLegendSize.height.toFloat()
                        }
                    }

                    LegendPosition_Top, LegendPosition_Bottom -> {
                        if (extraSize <= singleLegendSize.width) {
                            if (currentX + singleLegendSize.width > right) {
                                //layout next line
                                currentY += singleLegendSize.height.toFloat()
                                currentX = left * renderer.zoomRate


                                //draw legend shape
                                if (titles.size == renderer.seriesRendererCount) {
                                    paint.setColor(renderer.getSeriesRendererAt(i)!!.color)
                                } else {
                                    paint.setColor(Color.LTGRAY)
                                }
                                drawLegendShape(
                                    canvas,
                                    renderer.getSeriesRendererAt(i),
                                    currentX,
                                    currentY,
                                    i,
                                    paint
                                )


                                //draw legend text
                                paint.setColor(categoryAxisTextColor)
                                canvas.drawText(
                                    text,
                                    currentX + 2 * shapeWidth,
                                    currentY + fm.descent,
                                    paint
                                )

                                currentX += singleLegendSize.width.toFloat()
                            } else {
                                //continue to layout in one line
                                //draw legend shape
                                if (titles.size == renderer.seriesRendererCount) {
                                    paint.setColor(renderer.getSeriesRendererAt(i)!!.color)
                                } else {
                                    paint.setColor(Color.LTGRAY)
                                }
                                drawLegendShape(
                                    canvas,
                                    renderer.getSeriesRendererAt(i),
                                    currentX,
                                    currentY,
                                    i,
                                    paint
                                )


                                //draw legend text
                                paint.setColor(categoryAxisTextColor)
                                canvas.drawText(
                                    text,
                                    currentX + 2 * shapeWidth,
                                    currentY + fm.descent,
                                    paint
                                )
                                currentX += singleLegendSize.width.toFloat()
                            }
                        } else {
                            //multiline, layout new line
                            currentY += singleLegendSize.height.toFloat()
                            currentX = left.toFloat()


                            //draw legend shape
                            if (titles.size == renderer.seriesRendererCount) {
                                paint.setColor(renderer.getSeriesRendererAt(i)!!.color)
                            } else {
                                paint.setColor(Color.LTGRAY)
                            }
                            drawLegendShape(
                                canvas,
                                renderer.getSeriesRendererAt(i),
                                currentX,
                                currentY,
                                i,
                                paint
                            )


                            //draw legend text
                            paint.setColor(categoryAxisTextColor)
                            val textWidth = width - textOffset
                            val charWidth = floatArrayOf()
                            while (text.length > 0) {
                                var cnt = paint.breakText(text, true, textWidth, charWidth)
                                if (cnt == 0) {
                                    //must has one or more than one char for every line
                                    cnt = 1
                                }
                                val drawedText = text.substring(0, cnt)
                                text = text.substring(cnt, text.length)

                                canvas.drawText(
                                    drawedText,
                                    currentX + 2 * shapeWidth,
                                    currentY + fm.descent,
                                    paint
                                )
                                currentY += ceil((fm.descent - fm.ascent).toDouble()).toFloat()
                            }
                        }
                    }
                }
            }
        }
        return Math.round(renderer.legendTextSize * renderer.zoomRate)
    }

    /**
     * Calculates if the current width exceeds the total width.
     * 
     * @param currentWidth the current width
     * @param renderer the renderer
     * @param right the right side pixel value
     * @param width the total width
     * @return if the current width exceeds the total width
     */
    protected fun getExceed(
        currentWidth: Float,
        renderer: DefaultRenderer?,
        right: Int,
        width: Int
    ): Boolean {
        var exceed = currentWidth > right
        if (isVertical(renderer)) {
            exceed = currentWidth > width
        }
        return exceed
    }

    /**
     * Checks if the current chart is rendered as vertical.
     * 
     * @param renderer the renderer
     * @return if the chart is rendered as a vertical one
     */
    protected fun isVertical(renderer: DefaultRenderer?): Boolean {
        return renderer is XYMultipleSeriesRenderer
                && renderer.orientation == XYMultipleSeriesRenderer.Orientation.VERTICAL
    }

    /**
     * The graphical representation of a path.
     * 
     * @param canvas the canvas to paint to
     * @param points the points that are contained in the path to paint
     * @param paint the paint to be used for painting
     * @param circular if the path ends with the start point
     */
    protected fun drawPath(canvas: Canvas, points: FloatArray, paint: Paint, circular: Boolean) {
        val path = Path()
        path.moveTo(points[0], points[1])
        var i = 2
        while (i < points.size) {
            path.lineTo(points[i], points[i + 1])
            i += 2
        }
        if (circular) {
            path.lineTo(points[0], points[1])
        }
        canvas.drawPath(path, paint)
    }

    /**
     * Returns the legend shape width.
     * 
     * @param seriesIndex the series index
     * @return the legend shape width
     */
    abstract fun getLegendShapeWidth(seriesIndex: Int): Int

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
    abstract fun drawLegendShape(
        canvas: Canvas, renderer: SimpleSeriesRenderer, x: Float,
        y: Float, seriesIndex: Int, paint: Paint
    )

    /**
     * Calculates the best text to fit into the available space.
     * 
     * @param text the entire text
     * @param width the width to fit the text into
     * @param paint the paint
     * @return the text to fit into the space
     */
    private fun getFitText(text: String, width: Float, paint: Paint): String {
        var newText = text
        val length = text.length
        var diff = 0
        while (paint.measureText(newText) > width && diff < length) {
            diff++
            newText = text.substring(0, length - diff) + "..."
        }
        if (diff == length) {
            newText = "..."
        }
        return newText
    }

    protected fun drawLabel(
        canvas: Canvas,
        labelText: String,
        renderer: DefaultRenderer,
        prevLabelsBounds: MutableList<RectF>,
        centerX: Int,
        centerY: Int,
        shortRadius: Float,
        longRadius: Float,
        currentAngle: Float,
        angle: Float,
        left: Int,
        right: Int,
        paint: Paint
    ) {
        var labelText = labelText
        if (renderer.isShowLabels) {
            paint.setColor(renderer.labelsColor)
            val rAngle = Math.toRadians((90 - (currentAngle + angle / 2)).toDouble())
            val sinValue = sin(rAngle)
            val cosValue = cos(rAngle)
            val x1 = Math.round(centerX + (shortRadius * sinValue).toFloat())
            val y1 = Math.round(centerY + (shortRadius * cosValue).toFloat())
            val x2 = Math.round(centerX + (longRadius * sinValue).toFloat())
            var y2 = Math.round(centerY + (longRadius * cosValue).toFloat())

            val size = renderer.labelsTextSize
            var extra = max(size / 2, 10f)
            paint.setTextAlign(Paint.Align.LEFT)
            if (x1 > x2) {
                extra = -extra
                paint.setTextAlign(Paint.Align.RIGHT)
            }
            val xLabel = x2 + extra
            var yLabel = y2.toFloat()
            var width = right - xLabel
            if (x1 > x2) {
                width = xLabel - left
            }
            labelText = getFitText(labelText, width, paint)
            val widthLabel = paint.measureText(labelText)
            var okBounds = false
            while (!okBounds) {
                var intersects = false
                val length = prevLabelsBounds.size
                var j = 0
                while (j < length && !intersects) {
                    val prevLabelBounds = prevLabelsBounds.get(j)
                    if (prevLabelBounds.intersects(
                            xLabel,
                            yLabel,
                            xLabel + widthLabel,
                            yLabel + size
                        )
                    ) {
                        intersects = true
                        yLabel = max(yLabel, prevLabelBounds.bottom)
                    }
                    j++
                }
                okBounds = !intersects
            }

            y2 = (yLabel - size / 2).toInt()
            canvas.drawLine(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat(), paint)
            canvas.drawLine(x2.toFloat(), y2.toFloat(), x2 + extra, y2.toFloat(), paint)
            canvas.drawText(labelText, xLabel, yLabel, paint)
            prevLabelsBounds.add(RectF(xLabel, yLabel, xLabel + widthLabel, yLabel + size))
        }
    }

    companion object {
        const val CHART_AREA: Short = 0
        const val CHART_BAR: Short = 1
        const val CHART_LINE: Short = 2
        const val CHART_PIE: Short = 3
        const val CHART_SCATTER: Short = 4
        const val CHART_STOCK: Short = 5
        const val CHART_SURFACE: Short = 6
        const val CHART_DOUGHNUT: Short = 7
        const val CHART_BUBBLE: Short = 8
        const val CHART_RADAR: Short = 9
        const val CHART_UNKOWN: Short = 10

        //legend position
        const val LegendPosition_Left: Byte = 0
        const val LegendPosition_Top: Byte = 1
        const val LegendPosition_Right: Byte = 2
        const val LegendPosition_Bottom: Byte = 3
    }
}
