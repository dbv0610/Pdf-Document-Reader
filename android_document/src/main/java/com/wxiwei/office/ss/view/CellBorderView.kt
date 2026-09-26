/*
 * Filename:        CellBorderView.java
 * LegalCopyright	Copyright (C) wxiwei Inc. 2011-2014
 * Compiler:        JDK1.5.0_01
 * Time:            上午8:40:52
 */
package com.wxiwei.office.ss.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import com.wxiwei.office.common.PaintKit
import com.wxiwei.office.ss.model.baseModel.Cell
import com.wxiwei.office.ss.model.baseModel.Row
import com.wxiwei.office.ss.model.baseModel.Sheet
import com.wxiwei.office.ss.model.style.CellStyle
import com.wxiwei.office.ss.model.table.SSTableCellStyle

/**
 * excel 应用控制
 *
 * Read版本:        Read V1.0
 *
 * 作者:            jqin
 *
 * 日期:            2011-11-3
 *
 * 负责人:          jqin
 *
 * 负责小组:
 */
class CellBorderView(sheetView: SheetView?) {
    // 当前显示Sheet
    private var sheetView: SheetView? = sheetView

    /**
     *
     * @param canvas
     * @param cell
     * @param cellInfor.rowIndex
     * @param cellInfor.columnIndex
     * @param cellInfor.left
     * @param cellInfor.top
     * @param w
     * @param cellInfor.height
     * @return
     */
    fun draw(canvas: Canvas, cell: Cell, rect: RectF, tableCellStyle: SSTableCellStyle?) {
        val sheetView = this.sheetView!!
        val paint = PaintKit.instance().getPaint()
        //save paint property
        val oldColor = paint.color

        val book = sheetView.getSpreadsheet()!!.getWorkbook()!!

        canvas.save()
        var colorIndex: Int
        val color: Int
        // draw left border
        if (rect.left > sheetView.getRowHeaderWidth()) {
            colorIndex = LeftBorder(cell)
            if (colorIndex > -1) {
                paint.color = book.getColor(colorIndex)
                canvas.drawRect(rect.left, rect.top, rect.left + 1, rect.bottom, paint)
            } else if (tableCellStyle != null && tableCellStyle.getBorderColor() != null) {
                paint.color = tableCellStyle.getBorderColor()!!
                canvas.drawRect(rect.left, rect.top, rect.left + 1, rect.bottom, paint)
            }
        }

        // draw top border
        if (rect.top > sheetView.getColumnHeaderHeight()) {
            colorIndex = TopBorder(cell)
            if (colorIndex > -1) {
                paint.color = book.getColor(colorIndex)
                canvas.drawRect(rect.left, rect.top, rect.right, rect.top + 1, paint)
            } else if (tableCellStyle != null && tableCellStyle.getBorderColor() != null) {
                paint.color = tableCellStyle.getBorderColor()!!
                canvas.drawRect(rect.left, rect.top, rect.right, rect.top + 1, paint)
            }
        }

        // draw right border
        if (rect.right > sheetView.getRowHeaderWidth()) {
            colorIndex = RightBorder(cell)
            if (colorIndex > -1) {
                paint.color = book.getColor(colorIndex)
                canvas.drawRect(rect.right, rect.top, rect.right + 1, rect.bottom, paint)
            } else if (tableCellStyle != null && tableCellStyle.getBorderColor() != null) {
                paint.color = tableCellStyle.getBorderColor()!!
                canvas.drawRect(rect.right, rect.top, rect.right + 1, rect.bottom, paint)
            }
        }

        // draw bottom border
        if (rect.bottom > sheetView.getColumnHeaderHeight()) {
            colorIndex = BottomBorder(cell)
            if (colorIndex > -1) {
                paint.color = book.getColor(colorIndex)
                canvas.drawRect(rect.left, rect.bottom, rect.right, rect.bottom + 1, paint)
            } else if (tableCellStyle != null && tableCellStyle.getBorderColor() != null) {
                paint.color = tableCellStyle.getBorderColor()!!
                canvas.drawRect(rect.left, rect.bottom, rect.right, rect.bottom + 1, paint)
            }
        }

        paint.color = oldColor
        canvas.restore()
    }

    /**
     *
     * @param cell
     * @return left border color index
     */
    private fun LeftBorder(cell: Cell): Int {
        var cell = cell
        var style = cell.getCellStyle()
        val sheet = sheetView!!.getCurrentSheet()!!
        if (cell.getRangeAddressIndex() >= 0) {
            val cr = sheet.getMergeRange(cell.getRangeAddressIndex())!!
            val tempCell = sheet.getRow(cr.getFirstRow())!!.getCell(cr.getFirstColumn())
            if (tempCell != null) {
                style = tempCell.getCellStyle()
                cell = tempCell
            }
        }

        var hasLeft = false
        var color = -1
        if (style != null && style.getBorderLeft() > CellStyle.BORDER_NONE) {
            hasLeft = true
            color = style.getBorderLeftColorIdx().toInt()
        } else {
            var tempCell = sheet.getRowByColumnsStyle(cell.getRowNumber())!!.getCell(cell.getColNumber() - 1)
            if (tempCell != null) {
                style = tempCell.getCellStyle()
                if (tempCell.getRangeAddressIndex() >= 0) {
                    val cr = sheet.getMergeRange(tempCell.getRangeAddressIndex())!!
                    tempCell = sheet.getRow(cr.getLastRow())!!.getCell(cr.getLastColumn())
                    if (tempCell != null) {
                        style = tempCell.getCellStyle()
                    }
                }
                if (style != null && style.getBorderRight() > CellStyle.BORDER_NONE) {
                    hasLeft = true
                    color = style.getBorderRightColorIdx().toInt()
                }
            }
        }

        if (hasLeft && cell.getExpandedRangeAddressIndex() >= 0) {
            val cr = sheet.getRow(cell.getRowNumber())!!.getExpandedRangeAddress(cell.getExpandedRangeAddressIndex())!!.getRangedAddress()!!
            if (cell.getColNumber() != cr.getFirstColumn()) {
                hasLeft = false
            }
        }

        if (hasLeft) {
            return color
        } else {
            return -1
        }
    }

    /**
     *
     * @param cell
     * @return right border color index
     */
    private fun RightBorder(cell: Cell): Int {
        var cell = cell
        var style = cell.getCellStyle()
        val sheet = sheetView!!.getCurrentSheet()!!
        if (cell.getRangeAddressIndex() >= 0) {
            val cr = sheet.getMergeRange(cell.getRangeAddressIndex())!!
            val tempCell = sheet.getRow(cr.getLastRow())!!.getCell(cr.getLastColumn())
            if (tempCell != null) {
                style = tempCell.getCellStyle()
                cell = tempCell
            }
        }
        var hasRight = false
        var color = -1
        if (style != null && style.getBorderRight() > CellStyle.BORDER_NONE) {
            hasRight = true
            color = style.getBorderRightColorIdx().toInt()
        } else {
            var tempCell = sheet.getRowByColumnsStyle(cell.getRowNumber())!!.getCell(cell.getColNumber() + 1)
            if (tempCell != null) {
                style = tempCell.getCellStyle()
                if (tempCell.getRangeAddressIndex() >= 0) {
                    val cr = sheet.getMergeRange(tempCell.getRangeAddressIndex())!!
                    tempCell = sheet.getRow(cr.getFirstRow())!!.getCell(cr.getFirstColumn())
                    if (tempCell != null) {
                        style = tempCell.getCellStyle()
                    }
                }
                if (style != null && style.getBorderLeft() > CellStyle.BORDER_NONE) {
                    hasRight = true
                    color = style.getBorderLeftColorIdx().toInt()
                }
            }
        }

        if (hasRight && cell.getExpandedRangeAddressIndex() >= 0) {
            val cr = sheet.getRow(cell.getRowNumber())!!.getExpandedRangeAddress(cell.getExpandedRangeAddressIndex())!!.getRangedAddress()!!
            if (cell.getColNumber() != cr.getLastColumn()) {
                hasRight = false
            }
        }

        if (hasRight) {
            return color
        } else {
            return -1
        }
    }

    /**
     *
     * @param cell
     * @return top border color index
     */
    private fun TopBorder(cell: Cell): Int {
        var cell: Cell? = cell
        var style = cell!!.getCellStyle()
        val sheet = sheetView!!.getCurrentSheet()!!
        if (cell.getRangeAddressIndex() >= 0) {
            val cr = sheet.getMergeRange(cell.getRangeAddressIndex())!!
            val tempCell = sheet.getRow(cr.getFirstRow())!!.getCell(cr.getFirstColumn())
            if (tempCell != null) {
                style = tempCell.getCellStyle()
                cell = tempCell
            }
        }

        if (style != null && style.getBorderTop() > CellStyle.BORDER_NONE) {
            return style.getBorderTopColorIdx().toInt()
        }

        val topRow: Row? = sheet.getRowByColumnsStyle(cell!!.getRowNumber() - 1)
        if (topRow != null) {
            cell = topRow.getCell(cell.getColNumber())
            if (cell != null) {
                style = cell.getCellStyle()
                if (cell.getRangeAddressIndex() >= 0) {
                    val cr = sheet.getMergeRange(cell.getRangeAddressIndex())!!
                    cell = sheet.getRow(cr.getLastRow())!!.getCell(cr.getLastColumn())
                    if (cell != null) {
                        style = cell.getCellStyle()
                    }
                }
                if (style != null && style.getBorderBottom() > CellStyle.BORDER_NONE) {
                    return style.getBorderBottomColorIdx().toInt()
                }
            }
        }

        return -1
    }

    /**
     *
     * @param cell
     * @return bottom border color index
     */
    private fun BottomBorder(cell: Cell): Int {
        var cell: Cell? = cell
        var style = cell!!.getCellStyle()
        val sheet = sheetView!!.getCurrentSheet()!!
        if (cell.getRangeAddressIndex() >= 0) {
            val cr = sheet.getMergeRange(cell.getRangeAddressIndex())!!
            val tempCell = sheet.getRow(cr.getLastRow())!!.getCell(cr.getLastColumn())
            if (tempCell != null) {
                style = tempCell.getCellStyle()
                cell = tempCell
            }
        }

        if (style != null && style.getBorderBottom() > CellStyle.BORDER_NONE) {
            return style.getBorderBottomColorIdx().toInt()
        }

        val topRow: Row? = sheet.getRowByColumnsStyle(cell!!.getRowNumber() + 1)
        if (topRow != null) {
            cell = topRow.getCell(cell.getColNumber())
            if (cell != null) {
                style = cell.getCellStyle()
                if (cell.getRangeAddressIndex() >= 0) {
                    val cr = sheet.getMergeRange(cell.getRangeAddressIndex())!!
                    cell = sheet.getRow(cr.getFirstRow())!!.getCell(cr.getFirstColumn())
                    if (cell != null) {
                        style = cell.getCellStyle()
                    }
                }
                if (style != null && style.getBorderTop() > CellStyle.BORDER_NONE) {
                    return style.getBorderTopColorIdx().toInt()
                }
            }
        }

        return -1
    }

    /**
     *
     * @param canvas
     * @param rect
     * @param activeCellType
     */
    fun drawActiveCellBorder(canvas: Canvas, rect: RectF, activeCellType: Short) {
        val clipBounds = canvas.clipBounds
        clipBounds.left = sheetView!!.getRowHeaderWidth()
        clipBounds.top = sheetView!!.getColumnHeaderHeight()

        canvas.save()
        canvas.clipRect(clipBounds)

        val paint = PaintKit.instance().getPaint()
        //save paint property
        val oldColor = paint.color

        paint.color = Color.BLACK
        if (activeCellType == Sheet.ACTIVECELL_SINGLE && rect.left != rect.right && rect.top != rect.bottom) {
            //left frame
            //if(rect.left > sheetView.getRowHeaderWidth())
            run {
                canvas.drawRect(rect.left - 2, rect.top - 2, rect.left + 1, rect.bottom + 2, paint)
            }
            //top
            //if(rect.top > sheetView.getColumnHeaderHeight())
            run {
                canvas.drawRect(rect.left - 2, rect.top - 2, rect.right + 2, rect.top + 1, paint)
            }
            //right frame
            //if(rect.right > sheetView.getRowHeaderWidth())
            run {
                canvas.drawRect(rect.right - 1, rect.top - 2, rect.right + 2, rect.bottom + 2, paint)
            }
            //bottom
            //if(rect.bottom > sheetView.getColumnHeaderHeight())
            run {
                canvas.drawRect(rect.left - 2, rect.bottom - 1, rect.right + 2, rect.bottom + 2, paint)
            }
        } else if (activeCellType == Sheet.ACTIVECELL_ROW && rect.top != rect.bottom) {
            //top
            canvas.drawRect((clipBounds.left - 2).toFloat(), rect.top - 2, (clipBounds.right + 10).toFloat(), rect.top + 1, paint)

            //bottom
            canvas.drawRect((clipBounds.left - 2).toFloat(), rect.bottom - 1, (clipBounds.right + 10).toFloat(), rect.bottom + 2, paint)
        } else if (activeCellType == Sheet.ACTIVECELL_COLUMN && rect.left != rect.right) {
            //left frame
            canvas.drawRect(rect.left - 2, (clipBounds.top - 2).toFloat(), rect.left + 1, (clipBounds.bottom + 2).toFloat(), paint)

            //right frame
            canvas.drawRect(rect.right - 1, (clipBounds.top - 2).toFloat(), rect.right + 2, (clipBounds.bottom + 2).toFloat(), paint)
        }

        paint.color = oldColor
        canvas.restore()
    }

    fun dispose() {
        sheetView = null
    }
}
