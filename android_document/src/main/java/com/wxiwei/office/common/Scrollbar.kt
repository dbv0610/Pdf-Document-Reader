/*
 * 文件名称:          Scrollbar.java
 *  
 * 编译器:            android2.2
 * 时间:              下午2:30:54
 */
package com.wxiwei.office.common

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.wxiwei.office.java.awt.Dimension

/**
 * TODO: 文件注释
 * 
 * 
 * 
 * 
 * Read版本:        Read V1.0
 * 
 * 
 * 作者:            jqin
 * 
 * 
 * 日期:            2011-12-13
 * 
 * 
 * 负责人:           jqin
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
class Scrollbar {
    private val SCROLLBAR_SIZE = 5
    private val SCROLLBAR_OFFBORDER = 2

    private val SCROLLBAR_COLOR_ALPHA = 125

    // scroll bar color
    private val SCROLLBAR_COLOR = -0x70bbbbbc /*0x8fcecfd6;*/

    fun setPageSize(width: Int, height: Int) {
        this.pageSize.setSize(width, height)
    }

    /**
     * call setPageSize to set page size before call draw function
     * @param canvas
     * @param zoom
     */
    fun draw(canvas: Canvas, currentX: Int, currentY: Int, paint: Paint) {
        val clip = canvas.getClipBounds()

        val oldColor = paint.getColor()
        val alpha = paint.getAlpha()

        paint.setColor(SCROLLBAR_COLOR)
        paint.setAlpha(SCROLLBAR_COLOR_ALPHA)

        if (pageSize.width > clip.right) {
            drawHorizontalScrollBar(canvas, currentX, paint)
        }

        if (pageSize.height > clip.bottom) {
            drawVerticalScrollBar(canvas, currentY, paint)
        }

        paint.setColor(oldColor)
        paint.setAlpha(alpha)
    }

    /**
     * 
     * @param canvas
     * @param zoom
     */
    private fun drawHorizontalScrollBar(canvas: Canvas, currentX: Int, paint: Paint) {
        val clip = canvas.getClipBounds()


        //horizontal scroll bar length
        val length = (clip.right * clip.right / pageSize.width).toFloat()
        val pixelSteps = pageSize.width / clip.right.toFloat()


        //find the scroll bar rect
        val left = (pageSize.width / 2 - currentX) / pixelSteps - length / 2
        rect.set(
            left,
            (clip.bottom - SCROLLBAR_SIZE - SCROLLBAR_OFFBORDER).toFloat(),
            left + length,
            (clip.bottom - SCROLLBAR_OFFBORDER).toFloat()
        )
        canvas.drawRoundRect(
            rect,
            (SCROLLBAR_SIZE / 2).toFloat(),
            (SCROLLBAR_SIZE / 2).toFloat(),
            paint
        )
    }

    /**
     * 
     * @param canvas
     * @param zoom
     */
    private fun drawVerticalScrollBar(canvas: Canvas, currentY: Int, paint: Paint) {
        val clip = canvas.getClipBounds()


        //vertical scroll bar length
        val length = (clip.bottom * clip.bottom / pageSize.height).toFloat()
        val pixelSteps = pageSize.height / clip.bottom.toFloat()
        //find the scroll bar rect
        val top = (pageSize.height / 2 - currentY) / pixelSteps - length / 2

        rect.set(
            (clip.right - SCROLLBAR_SIZE - SCROLLBAR_OFFBORDER).toFloat(),
            top,
            (clip.right - SCROLLBAR_OFFBORDER).toFloat(),
            top + length
        )
        canvas.drawRoundRect(
            rect,
            (SCROLLBAR_SIZE / 2).toFloat(),
            (SCROLLBAR_SIZE / 2).toFloat(),
            paint
        )
    }

    //
    private val pageSize = Dimension()

    //
    private val rect = RectF()
}
