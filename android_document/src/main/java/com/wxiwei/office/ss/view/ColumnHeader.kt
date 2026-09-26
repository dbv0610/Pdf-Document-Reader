/*
 * 文件名称:          ColumnHeader.java
 *
 * 编译器:            android2.2
 * 时间:              下午3:00:26
 */
package com.wxiwei.office.ss.view

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.wxiwei.office.common.PaintKit
import com.wxiwei.office.constant.SSConstant
import com.wxiwei.office.ss.model.baseModel.Workbook
import com.wxiwei.office.ss.util.HeaderUtil

/**
 * 列标题
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
class ColumnHeader(sheetView: SheetView?) {
    //
    private var sheetview: SheetView? = sheetView

    // 列标题高度
    private var columnHeaderHeight = SSConstant.DEFAULT_COLUMN_HEADER_HEIGHT

    private var x = 0f

    private var rect: Rect? = null

    /**
     *
     * @param sheetView
     */
    init {
        rect = Rect()
    }

    fun getColumnRightBound(canvas: Canvas, zoom: Float): Int {
        canvas.save()
        val clip = canvas.clipBounds
        val paint = PaintKit.instance().getPaint()
        paint.textSize = SSConstant.HEADER_TEXT_FONTSZIE * zoom

        x = sheetview!!.getRowHeaderWidth().toFloat()

        layoutColumnLine(canvas, 0, zoom, paint)

        canvas.restore()

        return Math.min(x.toInt(), clip.right)
    }

    private fun layoutColumnLine(canvas: Canvas, columnStart: Int, zoom: Float, paint: Paint) {
        //
        var w = 0f
        val clip = canvas.clipBounds

        val sheet = sheetview!!.getCurrentSheet()!!
        val minRowAndColumnInformation = sheetview!!.getMinRowAndColumnInformation()!!
        var colIndex = if (minRowAndColumnInformation.getMinColumnIndex() > columnStart) minRowAndColumnInformation.getMinColumnIndex() else columnStart
        if (!minRowAndColumnInformation.isColumnAllVisible()) {
            colIndex += 1
            x = (x + minRowAndColumnInformation.getVisibleColumnWidth() * zoom).toFloat()
        }

        val maxSheetColumns = if (sheet.getWorkbook()!!.isBefore07Version()) Workbook.MAXCOLUMN_03 else Workbook.MAXCOLUMN_07
        while (x <= clip.right && colIndex < maxSheetColumns) {
            if (sheet.isColumnHidden(colIndex)) {
                colIndex++
                continue
            }

            w = (sheet.getColumnPixelWidth(colIndex) * zoom)
            x += w
            colIndex++
        }
    }

    /**
     *
     * @param canvas
     */
    fun draw(canvas: Canvas, bottomBound: Int, zoom: Float) {
        canvas.save()
        val paint = PaintKit.instance().getPaint()

        //save paint property
        val oldColor = paint.color
        val oldTextSize = paint.textSize

        paint.textSize = SSConstant.HEADER_TEXT_FONTSZIE * zoom

        x = sheetview!!.getRowHeaderWidth().toFloat()

        val clip = canvas.clipBounds

        drawColumnLine(canvas, bottomBound, 0, zoom, paint)

        //draw line between column header and sheet body
        paint.color = SSConstant.HEADER_GRIDLINE_COLOR
        canvas.drawRect(0f, columnHeaderHeight.toFloat(), x, (columnHeaderHeight + 1).toFloat(), paint)

        //restore paint
        paint.color = oldColor
        paint.textSize = oldTextSize
        canvas.restore()
    }

    private fun drawFirstVisibleColumn(canvas: Canvas, zoom: Float, paint: Paint) {
        val rect = this.rect!!
        val fm = paint.fontMetrics
        var visibleColumnWidth = 0f
        val minRowAndColumnInformation = sheetview!!.getMinRowAndColumnInformation()!!
        //draw rest part of first column
        val columnWidht = (minRowAndColumnInformation.getColumnWidth() * zoom)
        visibleColumnWidth = (minRowAndColumnInformation.getVisibleColumnWidth() * zoom).toFloat()
        // 绘制header
        if (HeaderUtil.instance().isActiveColumn(sheetview!!.getCurrentSheet()!!, minRowAndColumnInformation.getMinColumnIndex())) {
            paint.color = SSConstant.ACTIVE_COLOR
        } else {
            paint.color = SSConstant.HEADER_FILL_COLOR
        }

        rect.set(x.toInt(), 0, (x + visibleColumnWidth).toInt(), columnHeaderHeight)
        canvas.drawRect(rect, paint)

        //header line
        paint.color = SSConstant.HEADER_GRIDLINE_COLOR
        canvas.drawRect(x, 0f, x + 1, columnHeaderHeight.toFloat(), paint)

        // 绘制文本
        canvas.save()
        canvas.clipRect(rect)
        paint.color = SSConstant.HEADER_TEXT_COLOR

        val rowText = HeaderUtil.instance().getColumnHeaderTextByIndex(minRowAndColumnInformation.getMinColumnIndex())
        val textWidth = paint.measureText(rowText)
        val textX = (columnWidht - textWidth) / 2
        val textY = ((columnHeaderHeight - Math.ceil((fm.descent - fm.ascent).toDouble())).toInt() / 2).toFloat()
        canvas.drawText(rowText, x + textX - (columnWidht - visibleColumnWidth), textY - fm.ascent, paint)

        canvas.restore()
    }

    /**
     *
     * @param canvas
     * @param columnStart
     * @param x
     * @param h
     * @param zoom
     * @param paint
     */
    private fun drawColumnLine(canvas: Canvas, bottomBound: Int, columnStart: Int, zoom: Float, paint: Paint) {
        val rect = this.rect!!
        val fm = paint.fontMetrics
        //
        var w = 0f
        val clip = canvas.clipBounds

        val sheet = sheetview!!.getCurrentSheet()!!
        val minRowAndColumnInformation = sheetview!!.getMinRowAndColumnInformation()!!
        var colIndex = if (minRowAndColumnInformation.getMinColumnIndex() > columnStart) minRowAndColumnInformation.getMinColumnIndex() else columnStart
        if (!minRowAndColumnInformation.isColumnAllVisible()) {
            drawFirstVisibleColumn(canvas, zoom, paint)
            colIndex += 1
            x = (x + minRowAndColumnInformation.getVisibleColumnWidth() * zoom).toFloat()
        }

        val maxSheetColumns = if (sheet.getWorkbook()!!.isBefore07Version()) Workbook.MAXCOLUMN_03 else Workbook.MAXCOLUMN_07
        while (x <= clip.right && colIndex < maxSheetColumns) {
            if (sheet.isColumnHidden(colIndex)) {
                // redraw header grid line
                paint.color = SSConstant.HEADER_GRIDLINE_COLOR
                canvas.drawRect(x - 1, 0f, x + 1, columnHeaderHeight.toFloat(), paint)

                colIndex++
                continue
            }

            w = (sheet.getColumnPixelWidth(colIndex) * zoom)
            // 绘制header
            if (HeaderUtil.instance().isActiveColumn(sheetview!!.getCurrentSheet()!!, colIndex)) {
                paint.color = SSConstant.ACTIVE_COLOR
            } else {
                paint.color = SSConstant.HEADER_FILL_COLOR
            }

            rect.set(x.toInt(), 0, (x + w).toInt(), columnHeaderHeight)
            canvas.drawRect(rect, paint)

            if (colIndex != minRowAndColumnInformation.getMinColumnIndex()) {
                // 绘线
                paint.color = SSConstant.GRIDLINE_COLOR
                canvas.drawRect(x, 0f, x + 1, bottomBound.toFloat(), paint)
            }
            //header line
            paint.color = SSConstant.HEADER_GRIDLINE_COLOR
            canvas.drawRect(x, 0f, x + 1, columnHeaderHeight.toFloat(), paint)

            // 绘制文本
            canvas.save()
            canvas.clipRect(rect)
            paint.color = SSConstant.HEADER_TEXT_COLOR

            val colText = HeaderUtil.instance().getColumnHeaderTextByIndex(colIndex)
            val textWidth = paint.measureText(colText).toInt()
            val textX = (w - textWidth) / 2
            val textY = ((columnHeaderHeight - Math.ceil((fm.descent - fm.ascent).toDouble())).toInt() / 2).toFloat()
            canvas.drawText(colText, x + textX, textY - fm.ascent, paint)

            canvas.restore()
            x += w
            colIndex++
        }

        // 绘线最后一根线
        // 绘线
        paint.color = SSConstant.GRIDLINE_COLOR
        canvas.drawRect(x, 0f, x + 1, bottomBound.toFloat(), paint)
        //header line
        paint.color = SSConstant.HEADER_GRIDLINE_COLOR
        canvas.drawRect(x, 0f, x + 1, columnHeaderHeight.toFloat(), paint)

        // 有空白需要填充
        if (x < clip.right) {
            paint.color = SSConstant.HEADER_FILL_COLOR
            rect.set(x.toInt() + 1, 0, clip.right, clip.bottom)
            canvas.drawRect(rect, paint)
        }
    }

    /**
     * @return Returns the columnHeaderHeight.
     */
    fun getColumnHeaderHeight(): Int {
        return columnHeaderHeight
    }

    /**
     * @param columnHeaderHeight The columnHeaderHeight to set.
     */
    fun setColumnHeaderHeight(columnHeaderHeight: Int) {
        this.columnHeaderHeight = columnHeaderHeight
    }

    /**
     *
     */
    fun calculateColumnHeaderHeight(zoom: Float) {
        columnHeaderHeight = Math.round(SSConstant.DEFAULT_COLUMN_HEADER_HEIGHT * zoom)
    }

    /**
     *
     */
    fun dispose() {
        sheetview = null
        rect = null
    }
}
