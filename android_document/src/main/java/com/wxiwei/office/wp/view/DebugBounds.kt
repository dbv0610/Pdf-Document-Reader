package com.wxiwei.office.wp.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.simpletext.view.IView

/**
 * Debug aid: outlines the Word layout boxes so misplaced paragraphs, lines and table cells show on
 * screen and in render tests. Off by default; turn on with [enabled] (e.g. from a test or a debug
 * menu) and redraw.
 */
object DebugBounds {
    @JvmField @Volatile var enabled = false

    const val PARAGRAPH = Color.BLUE
    const val LINE = Color.GREEN
    const val CELL = 0xFFFF8C00.toInt()

    private val paint = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 1f }

    /** Outlines [view] ([color]) at its place, for a parent drawn at ([originX], [originY]). */
    fun draw(canvas: Canvas, view: IView, originX: Int, originY: Int, zoom: Float, color: Int) {
        if (!enabled) return
        val left = view.getX() * zoom + originX
        val top = view.getY() * zoom + originY
        synchronized(paint) {
            paint.color = color
            // a cell's box includes its margins
            val w = if (color == CELL) view.getLayoutSpan(WPViewConstant.X_AXIS) else view.getWidth()
            val h = if (color == CELL) view.getLayoutSpan(WPViewConstant.Y_AXIS) else view.getHeight()
            canvas.drawRect(left, top, left + w * zoom, top + h * zoom, paint)
        }
    }
}
