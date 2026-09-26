package com.wxiwei.office.ss.view

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.wxiwei.office.common.PaintKit
import com.wxiwei.office.ss.model.baseModel.Workbook
import com.wxiwei.office.ss.model.style.BorderStyle
import com.wxiwei.office.ss.model.style.CellStyle
import com.wxiwei.office.ss.model.table.SSTable
import com.wxiwei.office.ss.model.table.TableFormatManager
import com.wxiwei.office.ss.util.ModelUtil

class TableFormatView(sheetView: SheetView?) {
    // 当前显示Sheet
    private var sheetView: SheetView? = sheetView

    fun draw(canvas: Canvas) {
        val paint = PaintKit.instance().getPaint()
        //save paint property
        val oldColor = paint.color
        canvas.save()

        val formatMgr = sheetView!!.getCurrentSheet()!!.getWorkbook()!!.getTableFormatManager()
        val tables = sheetView!!.getCurrentSheet()!!.getTables()
        if (tables != null && formatMgr != null) {
            for (table in tables) {
                //header row
                if (table.isHeaderRowShown() && (table.getHeaderRowDxfId() >= 0 || table.getHeaderRowBorderDxfId() >= 0)) {
                    drawHeaderRowFormat(canvas, formatMgr, table, paint)
                }

                //total row
                if (table.isTotalRowShown() && (table.getTotalsRowDxfId() >= 0 || table.getTotalsRowBorderDxfId() >= 0)) {
                    drawTotalRowFormat(canvas, formatMgr, table, paint)
                }

                //table border
                if (table.getTableBorderDxfId() >= 0) {
                    drawTableBorders(canvas, formatMgr, table, paint)
                }
            }
        }

        paint.color = oldColor
        canvas.restore()
    }

    private fun drawHeaderRowFormat(canvas: Canvas, formatMgr: TableFormatManager, table: SSTable, paint: Paint) {
        val book = sheetView!!.getCurrentSheet()!!.getWorkbook()!!

        val ref = table.getTableReference()!!

        val headerRowDxf = formatMgr.getFormat(table.getHeaderRowDxfId())
        val headerRowBorderDxf = formatMgr.getFormat(table.getHeaderRowBorderDxfId())
        val rect = ModelUtil.instance().getCellAnchor(sheetView!!, ref.getFirstRow(), ref.getFirstColumn(), ref.getLastColumn())

        //border
        if (headerRowDxf != null) {
            drawFormatBorders(canvas, paint, book, headerRowDxf, rect)
        }

        if (headerRowBorderDxf != null) {
            drawFormatBorders(canvas, paint, book, headerRowBorderDxf, rect)
        }
    }

    private fun drawTotalRowFormat(canvas: Canvas, formatMgr: TableFormatManager, table: SSTable, paint: Paint) {
        val book = sheetView!!.getCurrentSheet()!!.getWorkbook()!!

        val ref = table.getTableReference()!!

        val totalsRowDxf = formatMgr.getFormat(table.getTotalsRowDxfId())
        val totalsRowBorderDxf = formatMgr.getFormat(table.getTotalsRowBorderDxfId())
        val rect = ModelUtil.instance().getCellAnchor(sheetView!!, ref.getLastRow(), ref.getFirstColumn(), ref.getLastColumn())

        //border
        if (totalsRowDxf != null) {
            drawFormatBorders(canvas, paint, book, totalsRowDxf, rect)
        }

        if (totalsRowBorderDxf != null) {
            drawFormatBorders(canvas, paint, book, totalsRowBorderDxf, rect)
        }
    }

    private fun drawTableBorders(canvas: Canvas, formatMgr: TableFormatManager, table: SSTable, paint: Paint) {
        val rect = ModelUtil.instance().getCellRangeAddressAnchor(sheetView!!, table.getTableReference()!!)

        drawFormatBorders(
            canvas, paint, sheetView!!.getCurrentSheet()!!.getWorkbook()!!,
            formatMgr.getFormat(table.getTableBorderDxfId())!!, rect
        )
    }

    private fun drawFormatBorders(
        canvas: Canvas, paint: Paint, book: Workbook,
        headerRowDxf: CellStyle, rect: RectF
    ) {
        // draw left border
        if (rect.left > sheetView!!.getRowHeaderWidth() && headerRowDxf.getBorderLeft() != BorderStyle.BORDER_NONE) {
            paint.color = book.getColor(headerRowDxf.getBorderLeftColorIdx().toInt())
            canvas.drawRect(rect.left, rect.top, rect.left + 1, rect.bottom, paint)
        }

        // draw top border
        if (rect.top > sheetView!!.getColumnHeaderHeight() && headerRowDxf.getBorderTop() != BorderStyle.BORDER_NONE) {
            paint.color = book.getColor(headerRowDxf.getBorderTopColorIdx().toInt())
            canvas.drawRect(rect.left, rect.top, rect.right, rect.top + 1, paint)
        }

        // draw right border
        if (rect.right > sheetView!!.getRowHeaderWidth() && headerRowDxf.getBorderRight() != BorderStyle.BORDER_NONE) {
            paint.color = book.getColor(headerRowDxf.getBorderRightColorIdx().toInt())
            canvas.drawRect(rect.right, rect.top, rect.right + 1, rect.bottom, paint)
        }

        // draw bottom border
        if (rect.bottom > sheetView!!.getColumnHeaderHeight() && headerRowDxf.getBorderBottom() != BorderStyle.BORDER_NONE) {
            paint.color = book.getColor(headerRowDxf.getBorderBottomColorIdx().toInt())
            canvas.drawRect(rect.left, rect.bottom, rect.right, rect.bottom + 1, paint)
        }
    }
}
