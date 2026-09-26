/*
 * 文件名称:          RowHeader.java
 *
 * 编译器:            android2.2
 * 时间:              下午2:59:15
 */
package com.wxiwei.office.ss.view

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.wxiwei.office.common.PaintKit
import com.wxiwei.office.constant.SSConstant
import com.wxiwei.office.ss.model.baseModel.Row
import com.wxiwei.office.ss.model.baseModel.Workbook
import com.wxiwei.office.ss.util.HeaderUtil

/**
 * 行标题
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2011-11-8
 *
 * 负责人:          ljj8494
 *
 * 负责小组:
 */
class RowHeader(sheetView: SheetView?) {
    //
    private var sheetview: SheetView? = sheetView

    // 行标题宽度
    private var rowHeaderWidth = SSConstant.DEFAULT_ROW_HEADER_WIDTH

    //
    private var y = 0f

    private var rect: Rect? = null

    /**
     *
     * @param canvas
     * @param rightBound
     * @param zoom
     */
    fun getRowBottomBound(canvas: Canvas, zoom: Float): Int {
        canvas.save()
        val clip = canvas.clipBounds

        val paint = PaintKit.instance().getPaint()
        paint.textSize = SSConstant.HEADER_TEXT_FONTSZIE * zoom

        y = (SSConstant.DEFAULT_COLUMN_HEADER_HEIGHT * zoom)

        //draw no frozen header
        layoutRowLine(canvas, 0, zoom, paint)

        canvas.restore()

        return Math.min(y.toInt(), clip.bottom)
    }

    private fun layoutRowLine(canvas: Canvas, rowStart: Int, zoom: Float, paint: Paint) {
        val clip = canvas.clipBounds

        var h = 0f
        val sheet = sheetview!!.getCurrentSheet()!!
        var row: Row?
        val minRowAndColumnInformation = sheetview!!.getMinRowAndColumnInformation()!!
        var rowIndex = if (minRowAndColumnInformation.getMinRowIndex() > rowStart) minRowAndColumnInformation.getMinRowIndex() else rowStart
        if (!minRowAndColumnInformation.isRowAllVisible()) {
            rowIndex += 1
            y = (y + minRowAndColumnInformation.getVisibleRowHeight() * zoom).toFloat()
        }

        val maxSheetRows = if (sheet.getWorkbook()!!.isBefore07Version()) Workbook.MAXROW_03 else Workbook.MAXROW_07
        while (y <= clip.bottom && rowIndex < maxSheetRows) {
            row = sheet.getRow(rowIndex)
            if (row != null && row.isZeroHeight()) {
                rowIndex++
                continue
            }

            h = if (row == null) sheetview!!.getCurrentSheet()!!.getDefaultRowHeight().toFloat() else row.getRowPixelHeight()
            h = (h * zoom)
            y += h
            rowIndex++
        }
    }

    /**
     *
     * @param canvas
     */
    fun draw(canvas: Canvas, rightBound: Int, zoom: Float) {
        canvas.save()
        val paint = PaintKit.instance().getPaint()

        //save paint property
        val oldColor = paint.color
        val oldTextSize = paint.textSize

        paint.textSize = SSConstant.HEADER_TEXT_FONTSZIE * zoom

        y = (SSConstant.DEFAULT_COLUMN_HEADER_HEIGHT * zoom)

        rect = canvas.clipBounds
        rect!!.set(0, 0, rowHeaderWidth, rect!!.bottom)
        // 左上角的一块区域
        paint.color = SSConstant.HEADER_FILL_COLOR
        canvas.drawRect(rect!!, paint)

        //draw no frozen header
        drawRowLine(canvas, rightBound, 0, zoom, paint)

        //restore paint
        paint.color = oldColor
        paint.textSize = oldTextSize

        canvas.restore()
    }

    /**
     *
     * @param canvas
     * @param x
     * @param y
     * @param w
     * @param zoom
     * @param paint
     */
    private fun drawFirstVisibleHeader(canvas: Canvas, rightBound: Float, zoom: Float, paint: Paint) {
        val rect = this.rect!!
        val fm = paint.fontMetrics
        val clip = canvas.clipBounds
        var visibleRowHeight = 0f
        val minRowAndColumnInformation = sheetview!!.getMinRowAndColumnInformation()!!
        val rowHeight = (minRowAndColumnInformation.getRowHeight() * zoom)
        visibleRowHeight = (minRowAndColumnInformation.getVisibleRowHeight() * zoom).toFloat()

        // 绘制header
        if (HeaderUtil.instance().isActiveRow(sheetview!!.getCurrentSheet()!!, minRowAndColumnInformation.getMinRowIndex())) {
            paint.color = SSConstant.ACTIVE_COLOR
        } else {
            paint.color = SSConstant.HEADER_FILL_COLOR
        }

        rect.set(0, y.toInt(), rowHeaderWidth, (y + visibleRowHeight).toInt())
        canvas.drawRect(rect, paint)

        // 绘线
        paint.color = SSConstant.GRIDLINE_COLOR
        canvas.drawRect(0f, y, rightBound, y + 1, paint)
        //head line
        paint.color = SSConstant.HEADER_GRIDLINE_COLOR
        canvas.drawRect(0f, y, rowHeaderWidth.toFloat(), y + 1, paint)
        // 绘制文本
        canvas.save()
        canvas.clipRect(rect)
        paint.color = SSConstant.HEADER_TEXT_COLOR

        val rowText = (minRowAndColumnInformation.getMinRowIndex() + 1).toString()
        val textWidth = paint.measureText(rowText).toInt()
        val textX = (rowHeaderWidth - textWidth) / 2
        val textY = (rowHeight - Math.ceil((fm.descent - fm.ascent).toDouble())).toInt()
        canvas.drawText(rowText, textX.toFloat(), y + textY - fm.ascent - (rowHeight - visibleRowHeight), paint)

        canvas.restore()
    }

    private fun drawRowLine(canvas: Canvas, rightBound: Int, rowStart: Int, zoom: Float, paint: Paint) {
        val fm = paint.fontMetrics
        val clip = canvas.clipBounds

        var h = 0f
        val sheet = sheetview!!.getCurrentSheet()!!
        var row: Row?
        val minRowAndColumnInformation = sheetview!!.getMinRowAndColumnInformation()!!
        var rowIndex = if (minRowAndColumnInformation.getMinRowIndex() > rowStart) minRowAndColumnInformation.getMinRowIndex() else rowStart
        if (!minRowAndColumnInformation.isRowAllVisible()) {
            //draw rest part of first row
            drawFirstVisibleHeader(canvas, rightBound.toFloat(), zoom, paint)
            rowIndex += 1
            y = (y + minRowAndColumnInformation.getVisibleRowHeight() * zoom).toFloat()
        }

        val rect = this.rect!!
        val maxSheetRows = if (sheet.getWorkbook()!!.isBefore07Version()) Workbook.MAXROW_03 else Workbook.MAXROW_07
        while (y <= clip.bottom && rowIndex < maxSheetRows) {
            row = sheet.getRow(rowIndex)

            if (row != null && row.isZeroHeight()) {
                // redraw header grid line
                paint.color = SSConstant.HEADER_GRIDLINE_COLOR
                canvas.drawRect(0f, y - 1, rowHeaderWidth.toFloat(), y + 1, paint)
                rowIndex++
                continue
            }

            h = if (row == null) sheetview!!.getCurrentSheet()!!.getDefaultRowHeight().toFloat() else row.getRowPixelHeight()
            h = (h * zoom)

            // 绘制header
            if (HeaderUtil.instance().isActiveRow(sheetview!!.getCurrentSheet()!!, rowIndex)) {
                paint.color = SSConstant.ACTIVE_COLOR
            } else {
                paint.color = SSConstant.HEADER_FILL_COLOR
            }

            rect.set(0, y.toInt(), rowHeaderWidth, (y + h).toInt())
            canvas.drawRect(rect, paint)

            // 绘线
            paint.color = SSConstant.GRIDLINE_COLOR
            canvas.drawRect(0f, y, rightBound.toFloat(), y + 1, paint)
            //head line
            paint.color = SSConstant.HEADER_GRIDLINE_COLOR
            canvas.drawRect(0f, y, rowHeaderWidth.toFloat(), y + 1, paint)
            // 绘制文本
            canvas.save()
            canvas.clipRect(rect)
            paint.color = SSConstant.HEADER_TEXT_COLOR

            val rowText = (rowIndex + 1).toString()
            val textWidth = paint.measureText(rowText).toInt()
            val textX = (rowHeaderWidth - textWidth) / 2
            val textY = (h - Math.ceil((fm.descent - fm.ascent).toDouble())).toInt()
            canvas.drawText(rowText, textX.toFloat(), y + textY - fm.ascent, paint)

            canvas.restore()

            y += h
            rowIndex++
        }

        // 绘线最后一根线
        paint.color = SSConstant.GRIDLINE_COLOR
        canvas.drawRect(0f, y, rightBound.toFloat(), y + 1, paint)
        //head line
        paint.color = SSConstant.HEADER_GRIDLINE_COLOR
        canvas.drawRect(0f, y, rowHeaderWidth.toFloat(), y + 1, paint)

        // 有空白需要填充
        if (y < clip.bottom) {
            paint.color = SSConstant.HEADER_FILL_COLOR
            rect.set(0, (y + 1).toInt(), clip.right, clip.bottom)
            canvas.drawRect(rect, paint)
        }
        //draw  line between row header and sheet body
        paint.color = SSConstant.HEADER_GRIDLINE_COLOR
        canvas.drawRect(rowHeaderWidth.toFloat(), 0f, (rowHeaderWidth + 1).toFloat(), y, paint)
    }

    /**
     * 计算行标题宽度
     */
    fun calculateRowHeaderWidth(zoom: Float) {
        val paint = PaintKit.instance().getPaint()
        paint.textSize = SSConstant.HEADER_TEXT_FONTSZIE.toFloat()
        rowHeaderWidth = Math.round(paint.measureText(sheetview!!.getCurrentMinRow().toString())) + EXTEDES_WIDTH
        rowHeaderWidth = Math.round(Math.max(rowHeaderWidth, SSConstant.DEFAULT_ROW_HEADER_WIDTH) * zoom)
    }

    /**
     * @return Returns the rowHeaderWidth.
     */
    fun getRowHeaderWidth(): Int {
        return rowHeaderWidth
    }

    /**
     * @param rowHeaderWidth The rowHeaderWidth to set.
     */
    fun setRowHeaderWidth(rowHeaderWidth: Int) {
        this.rowHeaderWidth = rowHeaderWidth
    }

    /**
     *
     */
    fun dispose() {
        sheetview = null
        rect = null
    }

    companion object {
        private const val EXTEDES_WIDTH = 10
    }
}
