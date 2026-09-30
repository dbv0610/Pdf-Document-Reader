/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
package com.wxiwei.office.common

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Shader
import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.common.bg.Gradient
import com.wxiwei.office.common.bg.LinearGradientShader
import com.wxiwei.office.common.bg.PatternShader
import com.wxiwei.office.common.bg.TileShader
import com.wxiwei.office.common.picture.PictureKit
import com.wxiwei.office.common.shape.AbstractShape
import com.wxiwei.office.fc.ppt.reader.ReaderKit.Companion.instance
import com.wxiwei.office.pg.animate.IAnimation
import com.wxiwei.office.pg.control.Presentation
import com.wxiwei.office.system.IControl
import kotlin.math.abs

object BackgroundDrawer {
    /**
     * draw picture shape line and background
     * @param canvas
     * @param control
     * @param shape
     * @param rect
     * @param zoom
     */
    fun drawLineAndFill(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        shape: AbstractShape,
        rect: Rect,
        zoom: Float
    ) {
        if (shape.hasLine()) {
            val paint: Paint = PaintKit.Companion.instance().getPaint()
            paint.setStyle(Paint.Style.STROKE)
            paint.setStrokeWidth(shape.line!!.lineWidth * zoom)
            drawBackground(
                canvas,
                control,
                viewIndex,
                shape.line!!.backgroundAndFill,
                rect,
                null,
                zoom,
                paint
            )
        }

        if (shape.backgroundAndFill != null) {
            drawBackground(
                canvas, control, viewIndex,
                shape.backgroundAndFill, rect, null, zoom
            )
        }
    }

    @JvmStatic
    fun drawPathBackground(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        fill: BackgroundAndFill?,
        rect: Rect,
        animation: IAnimation?,
        zoom: Float,
        path: Path?,
        paint: Paint
    ) {
        if (fill == null || path == null) {
            return
        }

        canvas.save()

        if (fill.isSlideBackgroundFill && control != null && control.getView() is Presentation) {
            canvas.clipRect(rect)
            canvas.rotate(0f)
            val d = (control.getView() as Presentation).getPGModel()!!.getPageSize()
            rect.set(0, 0, (d!!.width * zoom).toInt(), (d.height * zoom).toInt())
        }

        when (fill.fillType) {
            BackgroundAndFill.Companion.FILL_SOLID -> {
                paint.setColor(fill.foregroundColor)
                if (animation != null) {
                    var newAlpha = (fill.foregroundColor shr 24) and 0xff
                    newAlpha = (animation.getCurrentAnimationInfor()!!
                        .getAlpha() / 255f * newAlpha).toInt()
                    paint.setAlpha(newAlpha)
                }
                canvas.drawPath(path, paint)
            }

            BackgroundAndFill.Companion.FILL_PICTURE -> {
                canvas.clipPath(path)
                var x = rect.left.toFloat()
                var y = rect.top.toFloat()
                var w = rect.width().toFloat()
                var h = rect.height().toFloat()
                val stretch = fill.stretch
                if (stretch != null) {
                    x += stretch.leftOffset * w
                    y += stretch.topOffset * h

                    w *= (1 - stretch.leftOffset - stretch.rightOffset)
                    h *= (1 - stretch.topOffset - stretch.bottomOffset)
                }
                PictureKit.Companion.instance().drawPicture(
                    canvas, control, viewIndex, fill.getPicture(control),
                    x, y, zoom, w, h, null, animation
                )
            }

            BackgroundAndFill.Companion.FILL_PATTERN, BackgroundAndFill.Companion.FILL_SHADE_LINEAR, BackgroundAndFill.Companion.FILL_SHADE_RADIAL, BackgroundAndFill.Companion.FILL_SHADE_RECT, BackgroundAndFill.Companion.FILL_SHADE_SHAPE, BackgroundAndFill.Companion.FILL_SHADE_TILE -> drawGradientAndTile(
                canvas, control, viewIndex, fill, rect, animation, zoom,
                path, paint
            )

            else -> {}
        }

        canvas.restore()
    }

    private fun drawGradientAndTile(
        canvas: Canvas?, control: IControl?, viewIndex: Int,
        fill: BackgroundAndFill?, rect: Rect?, animation: IAnimation?,
        zoom: Float, path: Path?, paint: Paint?
    ) {
        if (fill == null || paint == null || rect == null || canvas == null) {
            return
        }

        val aShader = fill.shader
        if (aShader != null) {
            if (aShader is LinearGradientShader) {
                val lineWidth = paint.getStrokeWidth()
                //vertical or horizontal direct line
                if (abs(rect.left - rect.right) <= lineWidth) {
                    rect.set(
                        Math.round(rect.left - lineWidth / 2),
                        Math.round(rect.top.toFloat()),
                        Math.round(rect.right + lineWidth / 2),
                        Math.round(rect.bottom.toFloat())
                    )
                } else if (abs(rect.top - rect.bottom) <= lineWidth) {
                    rect.set(
                        Math.round(rect.left.toFloat()),
                        Math.round(rect.top - lineWidth / 2),
                        Math.round(rect.right.toFloat()),
                        Math.round(rect.bottom + lineWidth / 2)
                    )
                }
            }

            // Clear any existing shader to prevent memory leaks
            paint.setShader(null)

            // For BitmapShader, we need to be extra careful
            if (aShader is TileShader) {
                // TileShader uses BitmapShader internally
                try {
                    // Force recreation of the shader to avoid using recycled bitmaps
                    val r = 1 / zoom
                    val scaledRect = Rect(
                        Math.round(rect.left * r),
                        Math.round(rect.top * r),
                        Math.round(rect.right * r),
                        Math.round(rect.bottom * r)
                    )

                    // Check if the rect is valid
                    if (scaledRect.width() <= 0 || scaledRect.height() <= 0) {
                        return
                    }

                    // Create a new shader with the scaled rect
                    val shader = aShader.createShader(control, viewIndex, scaledRect)

                    // If shader creation failed, return early
                    if (shader == null) {
                        return
                    }

                    // Set up the matrix for the shader
                    val m = Matrix()
                    val tileShader = aShader
                    val offX = rect.left + tileShader.offsetX * zoom
                    val offY = rect.top + tileShader.offsetY * zoom

                    m.postScale(zoom, zoom)
                    m.postScale(
                        rect.width() / Gradient.Companion.COORDINATE_LENGTH.toFloat(),
                        rect.height() / Gradient.Companion.COORDINATE_LENGTH.toFloat()
                    )
                    m.postTranslate(offX, offY)

                    // Apply the shader
                    try {
                        shader.setLocalMatrix(m)
                        paint.setShader(shader)

                        var newAlpha = aShader.alpha
                        if (animation != null) {
                            newAlpha = (animation.getCurrentAnimationInfor()!!
                                .getAlpha() / 255f * newAlpha).toInt()
                        }
                        paint.setAlpha(newAlpha)

                        if (path != null) {
                            canvas.drawPath(path, paint)
                        } else {
                            canvas.drawRect(rect, paint)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        // Always clear the shader when done
                        paint.setShader(null)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    paint.setShader(null)
                }
                return
            }

            // For other shader types
            var shader: Shader? = null
            try {
                shader = aShader.shader
                if (shader == null) {
                    val r = 1 / zoom
                    val scaledRect = Rect(
                        Math.round(rect.left * r),
                        Math.round(rect.top * r),
                        Math.round(rect.right * r),
                        Math.round(rect.bottom * r)
                    )

                    // Check if the rect is valid
                    if (scaledRect.width() <= 0 || scaledRect.height() <= 0) {
                        return
                    }

                    shader = aShader.createShader(control, viewIndex, scaledRect)
                }

                // If we still don't have a valid shader, return early
                if (shader == null) {
                    return
                }

                val m = Matrix()
                var offX = rect.left.toFloat()
                var offY = rect.top.toFloat()

                if (aShader is PatternShader) {
                    // Pattern shader specific code if needed
                } else if (aShader is LinearGradientShader) {
                    val gradient = aShader
                    var focusX = 1f
                    var focusY = 1f

                    if (gradient.getAngle() == 90) {
                        when (gradient.focus) {
                            100 -> {
                                focusX = 0f
                                focusY = 0f
                            }

                            0 -> focusX = 1f
                            -50 -> {
                                focusX = 0.5f
                                focusY = 0.5f
                            }

                            50 -> {
                                focusX = -0.5f
                                focusY = -0.5f
                            }
                        }
                    } else {
                        when (gradient.focus) {
                            100 -> {
                                focusX = 0f
                                focusY = 0f
                            }

                            0 -> focusX = 1f
                            50 -> {
                                focusX = 0.5f
                                focusY = 0.5f
                            }

                            -50 -> {
                                focusX = -0.5f
                                focusY = -0.5f
                            }
                        }
                    }

                    offX += focusX * rect.width()
                    offY += focusY * rect.height()
                }

                // Check if the rect dimensions are valid before scaling
                if (rect.width() <= 0 || rect.height() <= 0) {
                    return
                }

                m.postScale(
                    rect.width() / Gradient.Companion.COORDINATE_LENGTH.toFloat(),
                    rect.height() / Gradient.Companion.COORDINATE_LENGTH.toFloat()
                )
                m.postTranslate(offX, offY)

                try {
                    shader.setLocalMatrix(m)
                    paint.setShader(shader)

                    var newAlpha = aShader.alpha
                    if (animation != null) {
                        newAlpha = (animation.getCurrentAnimationInfor()!!
                            .getAlpha() / 255f * newAlpha).toInt()
                    }
                    paint.setAlpha(newAlpha)

                    if (path != null) {
                        canvas.drawPath(path, paint)
                    } else {
                        canvas.drawRect(rect, paint)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    paint.setShader(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                paint.setShader(null)
            }
        }
    }

    /**
     * 绘制背景
     * @param canvas
     * @param br
     * @param rect
     * @return
     */
    /**
     * 
     * @param canvas
     * @param control
     * @param br
     * @param rect
     * @param animation
     * @param zoom
     * @return
     */
    @JvmOverloads
    fun drawBackground(
        canvas: Canvas,
        control: IControl?,
        viewIndex: Int,
        br: BackgroundAndFill?,
        rect: Rect,
        animation: IAnimation?,
        zoom: Float,
        paint: Paint = PaintKit.Companion.instance().getPaint()
    ): Boolean {
        if (br != null) {
            canvas.save()

            if (br.isSlideBackgroundFill && control != null && control.getView() is Presentation) {
                canvas.clipRect(rect)
                canvas.rotate(0f)

                val d = (control.getView() as Presentation).getPGModel()!!.getPageSize()
                rect.set(0, 0, (d!!.width * zoom).toInt(), (d.height * zoom).toInt())
            }

            when (br.fillType) {
                BackgroundAndFill.Companion.FILL_SOLID -> {
                    val color = paint.getColor()
                    paint.setColor(br.foregroundColor)
                    if (animation != null) {
                        paint.setAlpha(animation.getCurrentAnimationInfor()!!.getAlpha())
                    }
                    canvas.drawRect(rect, paint)

                    //restore
                    paint.setColor(color)
                    canvas.restore()
                    return true
                }

                BackgroundAndFill.Companion.FILL_PICTURE -> {
                    var x = rect.left.toFloat()
                    var y = rect.top.toFloat()
                    var w = rect.width().toFloat()
                    var h = rect.height().toFloat()
                    val stretch = br.stretch
                    if (stretch != null) {
                        x += stretch.leftOffset * w
                        y += stretch.topOffset * h

                        w *= (1 - stretch.leftOffset - stretch.rightOffset)
                        h *= (1 - stretch.topOffset - stretch.bottomOffset)
                    }
                    PictureKit.Companion.instance().drawPicture(
                        canvas, control, viewIndex, br.getPicture(control),
                        x, y, zoom, w, h, null, animation
                    )
                    canvas.restore()
                    return true
                }

                BackgroundAndFill.Companion.FILL_PATTERN, BackgroundAndFill.Companion.FILL_SHADE_LINEAR, BackgroundAndFill.Companion.FILL_SHADE_RADIAL, BackgroundAndFill.Companion.FILL_SHADE_RECT, BackgroundAndFill.Companion.FILL_SHADE_SHAPE, BackgroundAndFill.Companion.FILL_SHADE_TILE -> {
                    drawGradientAndTile(
                        canvas,
                        control,
                        viewIndex,
                        br,
                        rect,
                        animation,
                        zoom,
                        null,
                        paint
                    )
                    canvas.restore()
                    return true
                }

                else -> {}
            }

            canvas.restore()
        }
        return false
    }
}
