/*
 * 文件名称:          CellView.java
 *
 * 编译器:            android2.2
 * 时间:              上午10:52:17
 */
package com.wxiwei.office.ss.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.Rect
import android.graphics.RectF
import com.wxiwei.office.common.PaintKit
import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.common.bg.Gradient
import com.wxiwei.office.common.bg.LinearGradientShader
import com.wxiwei.office.common.bg.PatternShader
import com.wxiwei.office.common.bg.TileShader
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.constant.SSConstant
import com.wxiwei.office.constant.wp.WPAttrConstant
import com.wxiwei.office.constant.wp.WPModelConstant
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.fc.ppt.attribute.ParaAttr
import com.wxiwei.office.fc.ppt.attribute.RunAttr
import com.wxiwei.office.fc.xls.Reader.SchemeColorUtil
import com.wxiwei.office.simpletext.font.FontKit
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.AttributeSetImpl
import com.wxiwei.office.simpletext.model.CellLeafElement
import com.wxiwei.office.simpletext.model.IAttributeSet
import com.wxiwei.office.simpletext.model.IDocument
import com.wxiwei.office.simpletext.model.LeafElement
import com.wxiwei.office.simpletext.model.ParagraphElement
import com.wxiwei.office.simpletext.model.STDocument
import com.wxiwei.office.simpletext.model.SectionElement
import com.wxiwei.office.simpletext.view.IView
import com.wxiwei.office.simpletext.view.STRoot
import com.wxiwei.office.ss.model.CellRangeAddress
import com.wxiwei.office.ss.model.baseModel.Cell
import com.wxiwei.office.ss.model.baseModel.Workbook
import com.wxiwei.office.ss.model.style.CellStyle
import com.wxiwei.office.ss.model.table.SSTable
import com.wxiwei.office.ss.model.table.SSTableCellStyle
import com.wxiwei.office.ss.model.table.TableStyleKit
import com.wxiwei.office.ss.other.DrawingCell
import com.wxiwei.office.ss.other.MergeCell
import com.wxiwei.office.ss.other.MergeCellMgr
import com.wxiwei.office.ss.util.ModelUtil
import com.wxiwei.office.ss.util.format.NumericFormatter
import com.wxiwei.office.system.IControl
import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * draw cells of sheet
 *
 * Read版本:        Read V1.0
 *
 * 作者:            jqin
 *
 * 日期:            2011-12-2
 *
 * 负责人:           jqin
 *
 * 负责小组:
 */
class CellView(sheetView: SheetView?) {
    //
    private var sheetView: SheetView? = null

    //border
    private var cellBorderView: CellBorderView? = null

    private var left = 0f
    private var top = 0f

    //merged cell
    private var mergedCellSize: MergeCell? = MergeCell()
    private var mergedCellMgr: MergeCellMgr? = MergeCellMgr()

    private var cellInfor: DrawingCell? = null

    private var cellRect: RectF? = null
    private var numericCellAlignRight = false

    ////////////////////////temp
    private var strBuilder: StringBuilder? = StringBuilder()
    private var tableStyleKit: TableStyleKit? = TableStyleKit()

    /**
     *
     * @param sheetView
     */
    init {
        this.sheetView = sheetView
        cellBorderView = CellBorderView(sheetView)

        cellRect = RectF()
    }

    private fun redrawMergedCellBordersAndBackground(canvas: Canvas, cr: CellRangeAddress) {
        val mergedCellSize = this.mergedCellSize!!
        val cellInfor = this.cellInfor!!
        canvas.save()
        canvas.clipRect(
            left,
            top,
            left + mergedCellSize.getWidth() - mergedCellSize.getNovisibleWidth() - (cellInfor.getWidth() - cellInfor.getVisibleWidth()),
            top + mergedCellSize.getHeight() - mergedCellSize.getNoVisibleHeight() - (cellInfor.getHeight() - cellInfor.getVisibleHeight())
        )

        //fill background
        canvas.drawColor(Color.WHITE)

        //redraw merged cell border
        val paint = PaintKit.instance().getPaint()
        //save paint property
        val oldColor = paint.color
        val oldStyle = paint.style

        paint.color = SSConstant.GRIDLINE_COLOR
        paint.style = Style.STROKE
        canvas.drawRect(
            left,
            top,
            left + mergedCellSize.getWidth() - mergedCellSize.getNovisibleWidth(),
            top + mergedCellSize.getHeight() - mergedCellSize.getNoVisibleHeight(), paint
        )

        paint.style = oldStyle

        var minRowIndex = sheetView!!.getMinRowAndColumnInformation()!!.getMinRowIndex()
        var minColumnIndex = sheetView!!.getMinRowAndColumnInformation()!!.getMinColumnIndex()
        if (mergedCellSize.isFrozenRow()) {
            minRowIndex = 0
        }
        if (mergedCellSize.isFrozenColumn()) {
            minColumnIndex = 0
        }
        //redraw header border
        if (minRowIndex >= cr.getFirstRow()) {
            paint.color = SSConstant.HEADER_GRIDLINE_COLOR
            canvas.drawRect(left, top, left + mergedCellSize.getWidth() - mergedCellSize.getNovisibleWidth(), top + 1, paint)
        }

        if (minColumnIndex >= cr.getFirstColumn()) {
            paint.color = SSConstant.HEADER_GRIDLINE_COLOR
            canvas.drawRect(left, top, left + 1, top + mergedCellSize.getHeight() - mergedCellSize.getNoVisibleHeight(), paint)
        }

        paint.color = oldColor

        canvas.restore()
    }

    /**
     *
     * @param canvas
     * @param cell
     * @param row
     * @param col
     * @param left
     * @param top
     * @param w
     * @param h
     * @param visibleWidth
     * @param visibleHeight
     */
    fun draw(canvas: Canvas, cell: Cell?, cellInfor: DrawingCell) {
        var cell = cell
        if (cell == null || sheetView!!.getSpreadsheet()!!.isAbortDrawing()) {
            return
        }

        val sheet = sheetView!!.getCurrentSheet()!!
        run {
            this.cellInfor = cellInfor
            //init data
            this.left = cellInfor.getLeft()
            this.top = cellInfor.getTop()
            this.mergedCellSize!!.setWidth(cellInfor.getWidth())
            this.mergedCellSize!!.setHeight(cellInfor.getHeight())
            mergedCellSize!!.setNovisibleWidth(0f)
            mergedCellSize!!.setNoVisibleHeight(0f)

            if (cell!!.getRangeAddressIndex() >= 0) {
                //find the width and height of not show cell which include in merged cell
                val cr = sheet.getMergeRange(cell!!.getRangeAddressIndex())!!

                //
                if (mergedCellMgr!!.isDrawMergeCell(sheetView!!, cr, cellInfor.getRowIndex(), cellInfor.getColumnIndex())) {
                    mergedCellSize = mergedCellMgr!!.getMergedCellSize(sheetView!!, cr, cellInfor.getRowIndex(), cellInfor.getColumnIndex())

                    cell = sheet.getRow(cr.getFirstRow())!!.getCell(cr.getFirstColumn())

                    //redraw merged cell background and borders
                    redrawMergedCellBordersAndBackground(canvas, cr)
                } else {
                    return
                }
            }
        }

        val mergedCellSize = this.mergedCellSize!!
        cellRect!!.set(
            left, top,
            (left + mergedCellSize.getWidth() - mergedCellSize.getNovisibleWidth() - (cellInfor.getWidth() - cellInfor.getVisibleWidth())),
            (top + mergedCellSize.getHeight() - mergedCellSize.getNoVisibleHeight() - (cellInfor.getHeight() - cellInfor.getVisibleHeight()))
        )

        val table = cell!!.getTableInfo()
        var tableCellStyle: SSTableCellStyle? = null
        if (table != null) {
            tableCellStyle = getTableCellStyle(
                table,
                sheetView!!.getCurrentSheet()!!.getWorkbook()!!,
                cell.getRowNumber(), cell.getColNumber()
            )
        }

        drawCellBackgroundAndBorder(canvas, cell, tableCellStyle)

        drawCellContents(canvas, cell, cellInfor, tableCellStyle)
    }

    /**
     *
     * @param canvas
     * @param rect
     * @param activeCellType
     */
    fun drawActiveCellBorder(canvas: Canvas, rect: RectF, activeCellType: Short) {
        cellBorderView!!.drawActiveCellBorder(canvas, rect, activeCellType)
    }

    /**
     *
     * @param canvas
     * @param cellInfor
     */
    fun drawCellBackgroundAndBorder(canvas: Canvas, cell: Cell, tableCellStyle: SSTableCellStyle?) {
        val cellRect = this.cellRect!!
        val sheetView = this.sheetView!!
        val paint = PaintKit.instance().getPaint()
        //save paint text size
        val color = paint.color

        //cell background
        val style = cell.getCellStyle()
        if (style != null && style.getFillPatternType() == BackgroundAndFill.FILL_SOLID) {
            paint.color = style.getFgColor()
            if (Math.abs(cellRect.left - sheetView.getRowHeaderWidth()) < 1) {
                canvas.drawRect(cellRect.left + 1, cellRect.top, cellRect.right, cellRect.bottom, paint)
            } else {
                canvas.drawRect(cellRect, paint)
            }
        } else if (style != null &&
            (style.getFillPatternType() == BackgroundAndFill.FILL_SHADE_LINEAR || style.getFillPatternType() == BackgroundAndFill.FILL_SHADE_RADIAL)
        ) {
            drawGradientAndTile(
                canvas, sheetView.getSpreadsheet()!!.getControl(), sheetView.getSheetIndex(),
                style.getFillPattern()!!, cellRect, sheetView.getZoom(), paint
            )
        } else {
            if (tableCellStyle != null && tableCellStyle.getFillColor() != null) {
                val bg = tableCellStyle.getFillColor()!!
                paint.color = bg
                if (Math.abs(cellRect.left - sheetView.getRowHeaderWidth()) < 1) {
                    canvas.drawRect(cellRect.left + 1, cellRect.top, cellRect.right, cellRect.bottom, paint)
                } else {
                    canvas.drawRect(cellRect, paint)
                }
            }
        }

        paint.color = color

        //draw cell borders
        cellBorderView!!.draw(canvas, cell, cellRect, tableCellStyle)
    }

    private fun drawGradientAndTile(
        canvas: Canvas, control: IControl, viewIndex: Int,
        fill: BackgroundAndFill, rect: RectF, zoom: Float, paint: Paint
    ) {
        val aShader = fill.getShader()
        if (aShader != null) {
            var shader = aShader.getShader()
            if (shader == null) {
                val r = 1 / zoom
                shader = aShader.createShader(
                    control, viewIndex,
                    Rect(
                        (rect.left * r).roundToInt(),
                        (rect.top * r).roundToInt(),
                        (rect.right * r).roundToInt(),
                        (rect.bottom * r).roundToInt()
                    )
                )
                if (shader == null) {
                    return
                }
            }

            val m = Matrix()
            var offX = rect.left
            var offY = rect.top
            when (aShader) {
                is TileShader -> {
                    offX += aShader.offsetX * zoom
                    offY += aShader.offsetY * zoom

                    m.postScale(zoom, zoom)
                }

                is PatternShader -> {
                }

                else -> {
                    if (aShader is LinearGradientShader) {
                        var focusX = 1f
                        var focusY = 1f

                        if (aShader.getAngle() == 90) {
                            when (aShader.getFocus()) {
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
                            when (aShader.getFocus()) {
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

                    m.postScale(
                        rect.width() / Gradient.COORDINATE_LENGTH.toFloat(),
                        rect.height() / Gradient.COORDINATE_LENGTH.toFloat()
                    )
                }
            }
            m.postTranslate(offX, offY)
            shader.setLocalMatrix(m)
            paint.shader = shader
            canvas.drawRect(rect, paint)
            paint.shader = null
        }
    }

    fun getTableCellStyle(table: SSTable, book: Workbook, row: Int, col: Int): SSTableCellStyle? {
        val rangeAddr = table.getTableReference()!!
        val tableStyle =
            tableStyleKit!!.getTableStyle(table.getName(), SchemeColorUtil.getSchemeColor(book))
                ?: return null

        if (table.isHeaderRowShown()) {
            if (row == rangeAddr.getFirstRow()) {
                //first row
                return tableStyle.getFirstRow()
            } else if (table.isTotalRowShown() && row == rangeAddr.getLastRow()) {
                return if (tableStyle.getLastRow() != null) {
                    tableStyle.getLastRow()
                } else if (table.isShowFirstColumn() && col == rangeAddr.getFirstColumn() && tableStyle.getFirstCol() != null) {
                    tableStyle.getFirstCol()
                } else if (table.isShowLastColumn() && col == rangeAddr.getLastColumn() && tableStyle.getLastCol() != null) {
                    tableStyle.getLastCol()
                } else {
                    tableStyle.getBand2H()
                }
            } else if (table.isShowFirstColumn() && col == rangeAddr.getFirstColumn() && tableStyle.getFirstCol() != null) {
                return tableStyle.getFirstCol()
            } else if (table.isShowLastColumn() && col == rangeAddr.getLastColumn() && tableStyle.getLastCol() != null) {
                return tableStyle.getLastCol()
            } else if (table.isShowRowStripes()) {
                return if ((row - rangeAddr.getFirstRow()) % 2 == 1) {
                    //band1v
                    tableStyle.getBand1V()
                } else if (table.isShowColumnStripes() && (col - rangeAddr.getFirstColumn()) % 2 == 0) {
                    //band1H
                    tableStyle.getBand1H()
                } else {
                    tableStyle.getBand2V()
                }
            } else if (table.isShowColumnStripes()) {
                return if ((col - rangeAddr.getFirstColumn()) % 2 == 0) {
                    //band1H
                    tableStyle.getBand1H()
                } else {
                    tableStyle.getBand2H()
                }
            }

            return tableStyle.getBand2H()
        } else {
            if (table.isTotalRowShown() && row == rangeAddr.getLastRow()) {
                if (tableStyle.getLastRow() != null) {
                    return tableStyle.getLastRow()
                } else if (table.isShowFirstColumn() && col == rangeAddr.getFirstColumn() && tableStyle.getFirstCol() != null) {
                    return tableStyle.getFirstCol()
                } else if (table.isShowLastColumn() && col == rangeAddr.getLastColumn() && tableStyle.getLastCol() != null) {
                    return tableStyle.getLastCol()
                } else {
                    return tableStyle.getBand2H()
                }
            } else if (table.isShowFirstColumn() && col == rangeAddr.getFirstColumn() && tableStyle.getFirstCol() != null) {
                return tableStyle.getFirstCol()
            } else if (table.isShowLastColumn() && col == rangeAddr.getLastColumn() && tableStyle.getLastCol() != null) {
                return tableStyle.getLastCol()
            } else if (table.isShowRowStripes()) {
                return if ((row - rangeAddr.getFirstRow()) % 2 == 0) {
                    //band1v
                    tableStyle.getBand1V()
                } else if (table.isShowColumnStripes() && (col - rangeAddr.getFirstColumn()) % 2 == 0) {
                    //band1H
                    tableStyle.getBand1H()
                } else {
                    tableStyle.getBand2V()
                }
            } else if (table.isShowColumnStripes()) {
                return if ((col - rangeAddr.getFirstColumn()) % 2 == 0) {
                    //band1H
                    tableStyle.getBand1H()
                } else {
                    tableStyle.getBand2H()
                }
            }

            return tableStyle.getBand2H()
        }
    }

    /**
     * canvas rotate, old coordinate system convert to new coordinate system
     * x1 = sina * y0 + cosa * x0
     * y1 = cosa * y0 - sina * x0
     */
    private fun getRotatedX(x: Int, y: Int, degree: Int): Int {
        val d = degree / 180f * Math.PI
        return (Math.sin(d) * y + Math.cos(d) * x).toInt()
    }

    private fun getRotatedY(x: Int, y: Int, degree: Int): Int {
        val d = degree / 180f * Math.PI
        return (Math.cos(d) * y - Math.sin(d) * x).toInt()
    }

    /**
     *
     * @param canvas
     * @param cellInfor
     */
    private fun drawCellContents(canvas: Canvas, cell: Cell, cellInfor: DrawingCell, tableCellStyle: SSTableCellStyle?) {
        var cell = cell
        val sheet = sheetView!!.getCurrentSheet()!!

        //expanded cell
        if (cell.getExpandedRangeAddressIndex() >= 0) {
            val row = sheet.getRow(cellInfor.getRowIndex())!!
            val cr = row.getExpandedRangeAddress(cell.getExpandedRangeAddressIndex())!!.getRangedAddress()!!
            if (mergedCellMgr!!.isDrawMergeCell(sheetView!!, cr, cellInfor.getRowIndex(), cellInfor.getColumnIndex())) {
                mergedCellSize = mergedCellMgr!!.getMergedCellSize(sheetView!!, cr, cellInfor.getRowIndex(), cellInfor.getColumnIndex())
                cell = row.getExpandedRangeAddress(cell.getExpandedRangeAddressIndex())!!.getExpandedCell()!!

                cellRect!!.set(
                    left, top,
                    (left + mergedCellSize!!.getWidth() - mergedCellSize!!.getNovisibleWidth() - (cellInfor.getWidth() - cellInfor.getVisibleWidth())),
                    (top + mergedCellSize!!.getHeight() - mergedCellSize!!.getNoVisibleHeight() - (cellInfor.getHeight() - cellInfor.getVisibleHeight()))
                )
            } else {
                return
            }
        }

        val content = ModelUtil.instance().getFormatContents(sheet.getWorkbook()!!, cell)
        if (content.isNullOrEmpty()) {
            return
        }

        val paint = FontKit.instance().getCellPaint(cell, sheet.getWorkbook(), tableCellStyle)

        //save paint text size
        val textSize = paint.textSize
        paint.textSize = textSize * sheetView!!.getZoom()

        numericCellAlignRight = false
        if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN
            || (cell.getCellType() == Cell.CELL_TYPE_NUMERIC && cell.getCellNumericType() != Cell.CELL_TYPE_NUMERIC_STRING)
        ) {
            numericCellAlignRight = true
        }

        if (numericCellAlignRight) {
            drawNumericCell(canvas, cell, content, paint)
        } else {
            drawNonNumericCell(canvas, cell, content, paint)
        }
        //restore paint style
        paint.textSize = textSize

//      //canvas rotate
//      Paint paint = PaintKit.instance().getPaint();
//      int color = paint.getColor();
//      paint.setColor(Color.RED);
//      canvas.rotate(45);
//
//      canvas.drawText("hello world -45", getRotatedX(100, 200, 45), getRotatedY(100, 200, 45),
//              paint);
//
//      canvas.rotate(-45);
//      paint.setColor(color);
    }

    /**
     * draw non-numeric cell
     * @param canvas
     * @param currentRow
     * @param currentColumn
     * @param style
     * @param contents
     * @param paint
     */
    private fun drawNonNumericCell(canvas: Canvas, cell: Cell, contents: String, paint: Paint) {
        var contents = contents
        if (isComplexText(cell)) {
            drawComplexTextCell(canvas, cell)
        } else if (cell.getCellStyle()!!.isWrapText()
            && (contents.contains("\n") || paint.measureText(contents) + 2 * SSConstant.SHEET_SPACETOBORDER > mergedCellSize!!.getWidth())
        ) {
            //more than one lines
            canvas.save()
            canvas.clipRect(cellRect!!)

            //drawWrapText(canvas, style, contents, paint);
            drawWrapText(canvas, cell, contents)

            canvas.restore()
        } else {
            //only one line(cannot have more than 1024 characters in one line)
            if (contents.length > 1024) {
                contents = contents.substring(0, 1024)
            }

            drawNonWrapText(canvas, cell, contents, paint)
        }
    }

    /**
     * draw complex text
     * @param canvas
     * @param cell
     */
    private fun drawComplexTextCell(canvas: Canvas, cell: Cell) {
        if (cell.getCellStyle()!!.isWrapText()) {
            drawWrapComplexTextCell(canvas, cell)
        } else {
            drawNotWrapComplexTextCell(canvas, cell)
        }
    }

    /**
     *
     * @param canvas
     * @param cell
     */
    private fun drawWrapComplexTextCell(canvas: Canvas, cell: Cell) {
        val mergedCellSize = this.mergedCellSize!!
        val cellInfor = this.cellInfor!!
        canvas.save()
        canvas.clipRect(cellRect!!)

        var root = cell.getSTRoot()
        if (root == null) {
            val rect = ModelUtil.instance().getCellAnchor(
                sheetView!!.getCurrentSheet()!!, cell.getRowNumber(), cell.getColNumber()
            )

            val elem = (cell.getSheet()!!.getWorkbook()!!.getSharedItem(cell.getStringCellValueIndex())) as SectionElement?

            if (elem == null || elem.getEndOffset() - elem.getStartOffset() == 0L) {
                canvas.restore()
                return
            }

            val attr = elem.getAttribute()
            // 宽度
            AttrManage.instance().setPageWidth(attr,
                (rect.width() * MainConstant.PIXEL_TO_TWIPS).roundToInt()
            )
            // 高度
            AttrManage.instance().setPageHeight(attr,
                (rect.height() * MainConstant.PIXEL_TO_TWIPS).roundToInt()
            )

            val doc: IDocument = STDocument()
            doc.appendSection(elem)
            root = STRoot(sheetView!!.getSpreadsheet()!!.getEditor(), doc)
            root.setWrapLine(true)
            root.doLayout()

            cell.setSTRoot(root)
        }

        root.draw(
            canvas,
            (left - mergedCellSize.getNovisibleWidth() - (cellInfor.getWidth() - cellInfor.getVisibleWidth())).roundToInt(),
            (top - mergedCellSize.getNoVisibleHeight() - (cellInfor.getHeight() - cellInfor.getVisibleHeight())).roundToInt(),
            sheetView!!.getZoom()
        )

        canvas.restore()
    }

    /**
     *
     * @param canvas
     * @param cell
     */
    private fun drawNotWrapComplexTextCell(canvas: Canvas, cell: Cell) {
        val mergedCellSize = this.mergedCellSize!!
        val cellInfor = this.cellInfor!!
        canvas.save()
        canvas.clipRect(cellRect!!)
        val style = cell.getCellStyle()!!
        val root = cell.getSTRoot()
        if (root == null) {
            canvas.restore()
            return
        }

        var view: IView = root.getChildView() ?: return
        view = view.getChildView() ?: return
        val lineWidth = (view.getLayoutSpan(WPViewConstant.X_AXIS) * sheetView!!.getZoom()).toInt()
        val textHeight = (view.getHeight() * sheetView!!.getZoom()).toInt()

        //horizontal, indent
        val indent = sheetView!!.getIndentWidthWithZoom(style.getIndent().toInt())
        if (indent + 2 * SSConstant.SHEET_SPACETOBORDER >= mergedCellSize.getWidth()) {
            canvas.restore()
            return
        } else {
            when (style.getHorizontalAlign()) {
                CellStyle.ALIGN_GENERAL,
                CellStyle.ALIGN_LEFT,
                CellStyle.ALIGN_FILL,
                CellStyle.ALIGN_JUSTIFY,
                CellStyle.ALIGN_CENTER_SELECTION -> {
                    left += SSConstant.SHEET_SPACETOBORDER
                    mergedCellSize.setWidth(mergedCellSize.getWidth() - indent)
                }

                CellStyle.ALIGN_RIGHT -> {
                    mergedCellSize.setWidth(mergedCellSize.getWidth() - indent)
                    left += mergedCellSize.getWidth() - lineWidth - SSConstant.SHEET_SPACETOBORDER
                }

                CellStyle.ALIGN_CENTER -> left += (mergedCellSize.getWidth() - lineWidth) / 2

                else -> {
                }
            }

            // 垂直对齐
            when (style.getVerticalAlign()) {
                CellStyle.VERTICAL_TOP -> top += SSConstant.SHEET_SPACETOBORDER

                CellStyle.VERTICAL_CENTER,
                CellStyle.VERTICAL_JUSTIFY -> top += SSConstant.SHEET_SPACETOBORDER/*(mergedCellSize.getHeight() - textHeight) / 2*/

                CellStyle.VERTICAL_BOTTOM -> top += SSConstant.SHEET_SPACETOBORDER/*mergedCellSize.getHeight() - textHeight*/

                else -> {
                }
            }
            val x = left - mergedCellSize.getNovisibleWidth() - (cellInfor.getWidth() - cellInfor.getVisibleWidth())
            val y = top - mergedCellSize.getNoVisibleHeight() - (cellInfor.getHeight() - cellInfor.getVisibleHeight())

            if (cell.getExpandedRangeAddressIndex() >= 0) {
                /**
                 * not draw content here, it may be covered by next blank cell's background
                 */
                //canvas.drawText(contents, x, y, paint);
                sheetView!!.addExtendCell(cell, RectF(cellRect), x, y, root)
            } else {
                root.draw(canvas, x.toInt(), y.toInt(), sheetView!!.getZoom())
            }

            canvas.restore()
        }
    }

    /**
     *
     * @param canvas
     * @param contents
     * @param paint
     */
    private fun drawAccountCell(canvas: Canvas, cell: Cell, contents: String, paint: Paint) {
        var contents = contents
        val mergedCellSize = this.mergedCellSize!!
        val cellInfor = this.cellInfor!!
        val strBuilder = this.strBuilder!!
        val fm = paint.fontMetrics
        // 文本宽度
        var textWidth = (paint.measureText(contents)).toInt()
        // 文本高度
        val textHeight = (Math.ceil((fm.descent - fm.ascent).toDouble())).toInt()

        left += SSConstant.SHEET_SPACETOBORDER
        //indention
        val style = cell.getCellStyle()!!
        val indent = (sheetView!!.getIndentWidthWithZoom(style.getIndent().toInt()))
        //
        canvas.save()
        //draw area
        canvas.clipRect(cellRect!!)

        //accounting cell, align currency symbol
        if (textWidth + indent + SSConstant.SHEET_SPACETOBORDER > mergedCellSize.getWidth()) {
            //text width large than cell width, show string "#####"
            val charCnt = ((mergedCellSize.getWidth() - 2 * SSConstant.SHEET_SPACETOBORDER) / paint.measureText("#")).toInt()

            strBuilder.delete(0, strBuilder.length)
            for (i in 0 until charCnt) {
                strBuilder.append('#')
            }

            contents = strBuilder.toString()

            textWidth = (paint.measureText(contents)).toInt()
        } else {
            when (style.getHorizontalAlign()) {
                CellStyle.ALIGN_GENERAL,
                CellStyle.ALIGN_LEFT -> {
                    left += indent
                    mergedCellSize.setWidth(mergedCellSize.getWidth() - indent)
                }

                CellStyle.ALIGN_RIGHT -> mergedCellSize.setWidth(mergedCellSize.getWidth() - indent)
            }

            val index = contents.indexOf("*")
            if (index > -1) {
                val first = contents.substring(0, index)
                val end = contents.substring(index + 1, contents.length)

                val blackCnt = ((mergedCellSize.getWidth() - paint.measureText(first + end) - 2 * SSConstant.SHEET_SPACETOBORDER) / paint.measureText(" ")).toInt()

                strBuilder.delete(0, strBuilder.length)
                strBuilder.append(first)
                for (i in 0 until blackCnt) {
                    strBuilder.append(' ')
                }

                strBuilder.append(end)
                contents = strBuilder.toString()
            }
        }

        // 垂直对齐
        when (cell.getCellStyle()!!.getVerticalAlign()) {
            CellStyle.VERTICAL_TOP -> top += SSConstant.SHEET_SPACETOBORDER

            CellStyle.VERTICAL_CENTER,
            CellStyle.VERTICAL_JUSTIFY -> top += (mergedCellSize.getHeight() - textHeight) / 2

            CellStyle.VERTICAL_BOTTOM -> top += mergedCellSize.getHeight() - textHeight

            else -> {
            }
        }

        val x = left - mergedCellSize.getNovisibleWidth() - (cellInfor.getWidth() - cellInfor.getVisibleWidth())
        val y = top - fm.ascent - mergedCellSize.getNoVisibleHeight() - (cellInfor.getHeight() - cellInfor.getVisibleHeight())

        canvas.drawText(contents, x, y, paint)
        canvas.restore()
    }

    /**
     * contents contain 'E'
     * @param contents
     * @param paint
     * @return
     */
    private fun getScientificGeneralContents(contents: String, paint: Paint): String {
        var contents = contents
        val mergedCellSize = this.mergedCellSize!!
        var firstPart = ""
        var endPart = ""
        var lastChar = 0.toChar()

        var replacedByChar = false

        val index = contents.indexOf('E')

        //first part of contents
        firstPart = contents.substring(0, index)
        //end part of contents
        endPart = contents.substring(index + 1)
        val e = Integer.parseInt(endPart)
        endPart = if (e > 0) {
            if (e < 10) {
                "E+0$endPart"
            } else {
                "E+$endPart"
            }
        } else {
            if (e > -10) {
                "E-0" + (-e).toString()
            } else {
                "E$endPart"
            }
        }

        if (paint.measureText(endPart) + 2 * SSConstant.SHEET_SPACETOBORDER >= mergedCellSize.getWidth()) {
            replacedByChar = true
        } else {
            while (paint.measureText(firstPart + endPart).toInt() + 2 * SSConstant.SHEET_SPACETOBORDER > mergedCellSize.getWidth()) {
                if (firstPart.length >= 1) {
                    lastChar = firstPart[firstPart.length - 1]
                    firstPart = firstPart.substring(0, firstPart.length - 1)
                } else {
                    replacedByChar = true
                    break
                }
            }
        }

        if (replacedByChar || firstPart.isEmpty() || firstPart == "-") {
            contents = ""
            while (paint.measureText(contents + "#") + 2 * SSConstant.SHEET_SPACETOBORDER < mergedCellSize.getWidth()) {
                contents += "#"
            }
        } else if (firstPart[firstPart.length - 1] == '.') {
            //remove '.'
            contents = firstPart.substring(0, firstPart.length - 1) + endPart
        } else {
            //round value
            if (lastChar <= '9' && lastChar >= '5') {
                firstPart = ceilNumeric(firstPart)
            }
            contents = firstPart + endPart
        }

        return contents
    }

    private fun ceilNumeric(contents: String): String {
        var contents = contents
        val index = contents.indexOf('.')
        if (index > 0) {
            val chars = contents.toCharArray()
            var i = chars.size - 1
            while (i > index && chars[i] == '9') {
                i--
            }

            if (i > index) {
                chars[i] = chars[i] + 1
                contents = String(chars, 0, i + 1)
            } else {
                contents = (contents.toDouble().toInt() + 1).toString()
            }
        } else if (contents.length == 1) {
            contents = (contents.toDouble().toInt() + 1).toString()
        }

        return contents
    }

    /**
     * contents not contain 'E'
     * @param contents
     * @param paint
     * @return
     */
    private fun getNonScientificGeneralContents(contents: String, paint: Paint): String {
        var contents = contents
        val mergedCellSize = this.mergedCellSize!!
        var firstPart = ""
        var endPart = ""
        var value: Double
        var step = 1
        var replacedByChar = false

        // 文本宽度
        // 文本宽度
        val textWidth: Int = (paint.measureText(contents)).toInt()
        var lastChar = 0.toChar()

        //accounting cell, align currency symbol
        if (textWidth + 2 * SSConstant.SHEET_SPACETOBORDER > mergedCellSize.getWidth()) {
            if (contents.length == 1) {
                return ""
            }
            value = contents.toDouble()
            //
            if (value.toInt() == 0 || (paint.measureText(value.toInt().toString())).toInt() + 2 * SSConstant.SHEET_SPACETOBORDER > mergedCellSize.getWidth()) {
                //scientific cell value
                var integerCnt = 0
                step = 10
                while (abs(value / step).toInt() > 0) {
                    value /= step
                    integerCnt++
                }

                //two part of contents
                if (integerCnt > 0) {
                    endPart = "E+"
                    if (integerCnt < 10) {
                        endPart = endPart + "0" + integerCnt.toString()
                    } else {
                        endPart += integerCnt.toString()
                    }

                    firstPart = value.toString()
                } else {
                    contents = value.toString()
                    val index = contents.indexOf('E')
                    if (index > 0) {
                        firstPart = contents.substring(0, index)
                        endPart = contents.substring(index)
                    } else {
                        integerCnt = 0
                        while (abs(value) < 1 && abs(value * Int.MAX_VALUE) > 0) {
                            value *= 10.0
                            integerCnt++
                        }
                        firstPart = value.toString()
                        endPart = "E-$integerCnt"
                    }
                }

                if (paint.measureText(endPart) + 2 * SSConstant.SHEET_SPACETOBORDER >= mergedCellSize.getWidth()) {
                    replacedByChar = true
                } else {
                    while (paint.measureText(firstPart + endPart).toInt() + 2 * SSConstant.SHEET_SPACETOBORDER > mergedCellSize.getWidth()) {
                        if (firstPart.isNotEmpty()) {
                            lastChar = firstPart[firstPart.length - 1]
                            firstPart = firstPart.substring(0, firstPart.length - 1)
                        } else {
                            replacedByChar = true
                            break
                        }
                    }
                }

                if (replacedByChar || firstPart.isEmpty() || firstPart == "-") {
                    contents = ""
                    while (paint.measureText("$contents#") + 2 * SSConstant.SHEET_SPACETOBORDER < mergedCellSize.getWidth()) {
                        contents = "$contents#"
                    }
                } else if (firstPart[firstPart.length - 1] == '.') {
                    //remove '.'
                    contents = firstPart.substring(0, firstPart.length - 1) + endPart
                } else {
                    //round value
                    if (lastChar in '5'..'9') {
                        firstPart = ceilNumeric(firstPart)
                    }
                    contents = firstPart + endPart
                }
            } else {
                //add decimal part
                while (paint.measureText(contents).toInt() + 2 * SSConstant.SHEET_SPACETOBORDER > mergedCellSize.getWidth()) {
                    lastChar = contents[contents.length - 1]
                    contents = contents.substring(0, contents.length - 1)
                }

                if (contents[contents.length - 1] == '.') {
                    contents = contents.substring(0, contents.length - 1)
                } else {
                    //round value
                    if (lastChar in '5'..'9') {
                        contents = ceilNumeric(contents)
                    }
                }

                contents = trimInvalidateZero(contents)!!
            }
        }

        return contents
    }

    private fun trimInvalidateZero(contents: String?): String? {
        if (contents == null || contents.length == 0) {
            return contents
        }
        var index = contents.indexOf('.')
        if (index > 0) {
            val chars = contents.toCharArray()

            val i = chars.size - 1
            while (i > index && chars[index] == '0') {
                index--
            }
            if (chars[i] == '.') {
                index--
            }

            return String(chars, 0, i)
        }
        return contents
    }

    private fun adjustGeneralCellContent(contents: String, paint: Paint): String {
        var contents = contents
        //General cell
        contents = scientificToNormal(contents)

        val index = contents.indexOf('E')
        if (index > -1) {
            return getScientificGeneralContents(contents, paint)
        } else {
            return getNonScientificGeneralContents(contents, paint)
        }
    }

    private fun isInteger(str: String): Boolean {
        try {
            Integer.parseInt(str)
            return true
        } catch (e: NumberFormatException) {
            return false
        }
    }

    private fun isDouble(str: String): Boolean {
        try {
            java.lang.Double.parseDouble(str)
            return true
        } catch (e: NumberFormatException) {
            return false
        }
    }

    private fun isNumeric(str: String): Boolean {
        return isInteger(str) || isDouble(str)
    }

    /**
     *
     * @param canvas
     * @param style
     * @param contents
     * @param paint
     */
    private fun drawGeneralCell(canvas: Canvas, cell: Cell, contents: String, paint: Paint) {
        var contents = contents
        contents = adjustGeneralCellContent(contents, paint)

        val mergedCellSize = this.mergedCellSize!!
        val cellInfor = this.cellInfor!!
        val strBuilder = this.strBuilder!!
        val fm = paint.fontMetrics
        // 文本高度
        val textHeight = (Math.ceil((fm.descent - fm.ascent).toDouble())).toInt()
        //
        canvas.save()
        //draw area
        canvas.clipRect(cellRect!!)

        // 垂直对齐
        val cellStyle = cell.getCellStyle()!!
        when (cellStyle.getVerticalAlign()) {
            CellStyle.VERTICAL_TOP -> top += SSConstant.SHEET_SPACETOBORDER

            CellStyle.VERTICAL_CENTER,
            CellStyle.VERTICAL_JUSTIFY -> top += (mergedCellSize.getHeight() - textHeight) / 2

            CellStyle.VERTICAL_BOTTOM -> top += mergedCellSize.getHeight() - textHeight

            else -> {
            }
        }

        //indention
        val style = cell.getCellStyle()!!
        val indent = (sheetView!!.getIndentWidthWithZoom(style.getIndent().toInt()))
        if (indent + 2 * SSConstant.SHEET_SPACETOBORDER >= mergedCellSize.getWidth() && !isNumeric(contents)) {
            canvas.restore()
            return
        }

        //
        var textWidth = (paint.measureText(contents)).toInt()
        if (textWidth + indent + 2 * SSConstant.SHEET_SPACETOBORDER > mergedCellSize.getWidth()) {
            left += SSConstant.SHEET_SPACETOBORDER
            //text width large than cell width, show string "##.."
            val charCnt = ((mergedCellSize.getWidth() - 2 * SSConstant.SHEET_SPACETOBORDER) / paint.measureText("#")).toInt()

            strBuilder.delete(0, strBuilder.length)
            for (i in 0 until charCnt) {
                strBuilder.append('#')
            }

            contents = strBuilder.toString()

            textWidth = (paint.measureText(contents)).toInt()
        } else {
            when (style.getHorizontalAlign()) {
                CellStyle.ALIGN_LEFT,
                CellStyle.ALIGN_FILL,
                CellStyle.ALIGN_JUSTIFY,
                CellStyle.ALIGN_CENTER_SELECTION -> {
                    left += indent + SSConstant.SHEET_SPACETOBORDER
                    mergedCellSize.setWidth(mergedCellSize.getWidth() - indent)
                }

                CellStyle.ALIGN_CENTER -> left += (mergedCellSize.getWidth() - textWidth) / 2

                CellStyle.ALIGN_GENERAL,
                CellStyle.ALIGN_RIGHT -> {
                    mergedCellSize.setWidth(mergedCellSize.getWidth() - indent)
                    left += mergedCellSize.getWidth() - (paint.measureText(contents)).toInt() - SSConstant.SHEET_SPACETOBORDER
                }
            }
        }
        val x = left - mergedCellSize.getNovisibleWidth() - (cellInfor.getWidth() - cellInfor.getVisibleWidth())
        val y = top - fm.ascent - mergedCellSize.getNoVisibleHeight() - (cellInfor.getHeight() - cellInfor.getVisibleHeight())

        canvas.drawText(contents, x, y, paint)
        canvas.restore()
    }

    /**
     *
     * @param contents
     * @return
     */
    private fun scientificToNormal(contents: String): String {
        val strBuilder = this.strBuilder!!
        var index = contents.indexOf('E')
        if (index < 0) {
            return contents
        }

        //first part of contents
        var firstPart = contents.substring(0, index)
        //end part of contents
        var endPart = Integer.parseInt(contents.substring(index + 1))
        val negative = firstPart.toDouble() < 0
        if (Math.abs(endPart) > 10) {
            return contents
        } else if (endPart < 0) {
            //remove char '0' in the end, eg"3.20"
            if (firstPart[firstPart.length - 1] == '0') {
                firstPart = firstPart.substring(0, firstPart.length - 1)
            }
            firstPart = firstPart.replace(".", "")

            //cons '0' string
            strBuilder.delete(0, strBuilder.length)
            endPart++
            while (endPart < 0) {
                strBuilder.append("0")
                endPart++
            }
            //cat
            if (!negative) {
                firstPart = "0." + strBuilder.toString() + firstPart
            } else {
                firstPart = "-0." + strBuilder.toString() + firstPart.replace("-", "")
            }
        } else if (endPart <= 10) {
            index = firstPart.indexOf('.')

            var decimalPartLen = firstPart.length - 2
            if (negative) {
                decimalPartLen = firstPart.length - 3
            }
            if (decimalPartLen <= endPart) {
                firstPart = firstPart.replace(".", "")
                endPart = endPart - decimalPartLen
                while (endPart > 0) {
                    firstPart += "0"
                    endPart--
                }
            } else {
                val chars = firstPart.toCharArray()
                var i = index
                index += endPart
                while (i < index) {
                    chars[i] = chars[i + 1]
                    i++
                }
                chars[i] = '.'
                firstPart = String(chars)
            }
        }

        return firstPart
    }

    /**
     *
     * @param canvas
     * @param style
     * @param value
     * @param paint
     */
    private fun drawNumericCell(canvas: Canvas, cell: Cell, content: String, paint: Paint) {
        var content = content
        val oldColor = paint.color
        if (content.length > 0 && cell.getNumberValue() < 0) {
            //color of Negative number
            paint.color = NumericFormatter.getNegativeColor(cell)
        }

        if (cell.getCellNumericType() == Cell.CELL_TYPE_NUMERIC_ACCOUNTING) {
            //accounting cell
            drawAccountCell(canvas, cell, content, paint)
            return
        } else if (cell.getCellNumericType() == Cell.CELL_TYPE_NUMERIC_GENERAL) {
            drawGeneralCell(canvas, cell, content, paint)
            return
        }

        val mergedCellSize = this.mergedCellSize!!
        val cellInfor = this.cellInfor!!
        val strBuilder = this.strBuilder!!
        //the rest cell
        val fm = paint.fontMetrics
        // 文本高度
        val textHeight = (Math.ceil((fm.descent - fm.ascent).toDouble())).toInt()
        // 文本宽度
        var textWidth = (paint.measureText(content)).toInt()

        //
        canvas.save()
        //draw area
        canvas.clipRect(cellRect!!)

        // 垂直对齐
        when (cell.getCellStyle()!!.getVerticalAlign()) {
            CellStyle.VERTICAL_TOP -> top += SSConstant.SHEET_SPACETOBORDER

            CellStyle.VERTICAL_CENTER,
            CellStyle.VERTICAL_JUSTIFY -> top += (mergedCellSize.getHeight() - textHeight) / 2

            CellStyle.VERTICAL_BOTTOM -> top += mergedCellSize.getHeight() - textHeight

            else -> {
            }
        }

        //
        val indent = (sheetView!!.getIndentWidthWithZoom(cell.getCellStyle()!!.getIndent().toInt()))

        if (textWidth + indent + 2 * SSConstant.SHEET_SPACETOBORDER > mergedCellSize.getWidth()) {
            //text width large than cell width, show string "##.."
            val charCnt = ((mergedCellSize.getWidth() - 2 * SSConstant.SHEET_SPACETOBORDER) / paint.measureText("#")).toInt()

            strBuilder.delete(0, strBuilder.length)
            for (i in 0 until charCnt) {
                strBuilder.append('#')
            }

            content = strBuilder.toString()

            textWidth = (paint.measureText(content)).toInt()
            left += SSConstant.SHEET_SPACETOBORDER
        } else {
            var hori = cell.getCellStyle()!!.getHorizontalAlign()
            if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN && hori == CellStyle.ALIGN_GENERAL) {
                hori = CellStyle.ALIGN_CENTER
            }
            when (hori) {
                CellStyle.ALIGN_LEFT,
                CellStyle.ALIGN_FILL,
                CellStyle.ALIGN_JUSTIFY,
                CellStyle.ALIGN_CENTER_SELECTION -> {
                    left += indent + SSConstant.SHEET_SPACETOBORDER
                    mergedCellSize.setWidth(mergedCellSize.getWidth() - indent)
                }

                CellStyle.ALIGN_RIGHT,
                CellStyle.ALIGN_GENERAL -> {
                    mergedCellSize.setWidth(mergedCellSize.getWidth() - indent)
                    left += mergedCellSize.getWidth() - textWidth - SSConstant.SHEET_SPACETOBORDER
                }

                CellStyle.ALIGN_CENTER -> left += (mergedCellSize.getWidth() - textWidth) / 2

                else -> {
                }
            }
        }

        var x = left - mergedCellSize.getNovisibleWidth() - (cellInfor.getWidth() - cellInfor.getVisibleWidth())
        var y = top - fm.ascent - mergedCellSize.getNoVisibleHeight() - (cellInfor.getHeight() - cellInfor.getVisibleHeight())

        if (mergedCellSize.isFrozenColumn()) {
            x = left - (cellInfor.getWidth() - cellInfor.getVisibleWidth())
        }
        if (mergedCellSize.isFrozenRow()) {
            y = top - fm.ascent - (cellInfor.getHeight() - cellInfor.getVisibleHeight())
        }

        canvas.drawText(content, x, y, paint)
        canvas.restore()
        //
        paint.color = oldColor
    }

    private fun initPageProp(attr: IAttributeSet, style: CellStyle, width: Int, height: Int) {
        var verAlign = WPAttrConstant.PAGE_V_TOP
        when (style.getVerticalAlign()) {
            CellStyle.VERTICAL_TOP -> verAlign = WPAttrConstant.PAGE_V_TOP

            CellStyle.VERTICAL_CENTER,
            CellStyle.VERTICAL_JUSTIFY -> verAlign = WPAttrConstant.PAGE_V_CENTER

            CellStyle.VERTICAL_BOTTOM -> verAlign = WPAttrConstant.PAGE_V_BOTTOM
        }

        //vertical alignment
        AttrManage.instance().setPageVerticalAlign(attr, verAlign)
        // 宽度
        AttrManage.instance().setPageWidth(attr, Math.round(width * MainConstant.PIXEL_TO_TWIPS))
        // 高度
        AttrManage.instance().setPageHeight(attr, Math.round(height * MainConstant.PIXEL_TO_TWIPS))
        // 左边距
        AttrManage.instance().setPageMarginLeft(attr, Math.round(SSConstant.SHEET_SPACETOBORDER * MainConstant.PIXEL_TO_TWIPS))
        // 右边距
        AttrManage.instance().setPageMarginRight(attr, Math.round(SSConstant.SHEET_SPACETOBORDER * MainConstant.PIXEL_TO_TWIPS))
        // 上边距
        AttrManage.instance().setPageMarginTop(attr, 0)
        // 下边框
        AttrManage.instance().setPageMarginBottom(attr, 0)
    }

    private fun convertToSectionElement(cell: Cell, content: String, width: Int, height: Int): SectionElement {
        // ======== 处理文本 ========
        // 建立章节
        val secElem = SectionElement()
        // 开始Offset
        secElem.setStartOffset(0)
        // 属性
        val attr = secElem.getAttribute()

        //init page property
        initPageProp(attr, cell.getCellStyle()!!, width, height)

        val pos = processParagraph(secElem, cell, content)
        secElem.setEndOffset(pos.toLong())

        return secElem
    }

    private fun processParagraph(secElem: SectionElement, cell: Cell, text: String): Int {
        var offset = 0
        val ps = Pattern.compile("\n").split(text)
        for (p in ps) {
            val paraElem = ParagraphElement()
            paraElem.setStartOffset(offset.toLong())
            val attrLayout: IAttributeSet = AttributeSetImpl()
            ParaAttr.instance().setParaAttribute(cell.getCellStyle(), paraElem.getAttribute(), attrLayout)

            offset = processRun(paraElem, cell, p, offset, attrLayout)
            paraElem.setEndOffset(offset.toLong())
            secElem.appendParagraph(paraElem, WPModelConstant.MAIN)
        }

        return offset
    }

    private fun processRun(
        paraElem: ParagraphElement, cell: Cell, p: String,
        offset: Int, attrLayout: IAttributeSet
    ): Int {
        var offset = offset
        var leaf: LeafElement? = null
        // 只有一个回车符的段落
        if (p.length == 0) {
            leaf = CellLeafElement(cell, 0, 0)
            leaf.appendNewlineFlag()
            // 属性
            RunAttr.instance().setRunAttribute(sheetView!!.getCurrentSheet()!!, cell, leaf.getAttribute(), attrLayout)
            // 开始 offset
            leaf.setStartOffset(offset.toLong())
            offset++
            leaf.setEndOffset(offset.toLong())
            paraElem.appendLeaf(leaf)
            return offset
        }

        val len = p.length
        if (len > 0) {
            val content = cell.getSheet()!!.getWorkbook()!!.getSharedString(cell.getStringCellValueIndex())
            if (content == null) {
                //string number
                leaf = LeafElement(p)
            } else {
                val start = content.indexOf(p)
                leaf = CellLeafElement(cell, start, start + p.length)
            }

            // 属性
            RunAttr.instance().setRunAttribute(sheetView!!.getCurrentSheet()!!, cell, leaf.getAttribute(), attrLayout)
            // 开始 offset
            leaf.setStartOffset(offset.toLong())
            offset += len
            // 结束 offset
            leaf.setEndOffset(offset.toLong())
            paraElem.appendLeaf(leaf)
        }

        if (leaf != null) {
            if (leaf is CellLeafElement) {
                leaf.appendNewlineFlag()
                offset++
                leaf.setEndOffset(offset.toLong())
            } else {
                leaf.setText(leaf.getText(null) + "\n")
            }
        }
        return offset
    }

    private fun drawWrapText(canvas: Canvas, cell: Cell, content: String) {
        val mergedCellSize = this.mergedCellSize!!
        val cellInfor = this.cellInfor!!
        var root = cell.getSTRoot()
        if (root == null) {
            val rect = ModelUtil.instance().getCellAnchor(
                sheetView!!.getCurrentSheet()!!, cell.getRowNumber(), cell.getColNumber()
            )

            val elem = convertToSectionElement(cell, content, rect.width(), rect.height())

            if (elem.getEndOffset() - elem.getStartOffset() == 0L) {
                return
            }

            val doc: IDocument = STDocument()
            doc.appendSection(elem)
            root = STRoot(sheetView!!.getSpreadsheet()!!.getEditor(), doc)
            root.setWrapLine(true)
            root.doLayout()

            cell.setSTRoot(root)
        }

        root.draw(
            canvas,
            Math.round(left - mergedCellSize.getNovisibleWidth() - (cellInfor.getWidth() - cellInfor.getVisibleWidth())),
            Math.round(top - mergedCellSize.getNoVisibleHeight() - (cellInfor.getHeight() - cellInfor.getVisibleHeight())),
            sheetView!!.getZoom()
        )
    }

    /**
     *
     * @param canvas
     * @param row
     * @param col
     * @param style
     * @param contents
     * @param paint
     */
    private fun drawNonWrapText(canvas: Canvas, cell: Cell, contents: String, paint: Paint) {
        var contents = contents
        val mergedCellSize = this.mergedCellSize!!
        val cellInfor = this.cellInfor!!
        val strBuilder = this.strBuilder!!
        val fm = paint.fontMetrics
        // 文本宽度
        var textWidth = (paint.measureText(contents)).toInt()
        // 文本高度
        val textHeight = (Math.ceil((fm.descent - fm.ascent).toDouble())).toInt()

        val style = cell.getCellStyle()!!
        //horizontal, indent
        var indent = (sheetView!!.getIndentWidthWithZoom(style.getIndent().toInt()))
        if (indent + 2 * SSConstant.SHEET_SPACETOBORDER >= mergedCellSize.getWidth()) {
            canvas.restore()
            return
        } else {
            if (cell.getCellNumericType() == Cell.CELL_TYPE_NUMERIC_SIMPLEDATE
                && textWidth + indent + 2 * SSConstant.SHEET_SPACETOBORDER > mergedCellSize.getWidth()
            ) {
                //date&time text width large than cell width, show string "##.."
                val charCnt = ((mergedCellSize.getWidth() - 2 * SSConstant.SHEET_SPACETOBORDER) / paint.measureText("#")).toInt()

                strBuilder.delete(0, strBuilder.length)
                for (i in 0 until charCnt) {
                    strBuilder.append('#')
                }

                contents = strBuilder.toString()

                textWidth = (paint.measureText(contents)).toInt()
                indent = 0
            }

            canvas.save()
            //draw area
            canvas.clipRect(cellRect!!)

            var horiAlign = style.getHorizontalAlign()
            if (horiAlign == CellStyle.ALIGN_GENERAL
                && cell.getCellNumericType() == Cell.CELL_TYPE_NUMERIC_SIMPLEDATE
            ) {
                horiAlign = CellStyle.ALIGN_RIGHT
            }

            when (horiAlign) {
                CellStyle.ALIGN_GENERAL,
                CellStyle.ALIGN_LEFT,
                CellStyle.ALIGN_FILL,
                CellStyle.ALIGN_JUSTIFY,
                CellStyle.ALIGN_CENTER_SELECTION -> {
                    left += indent + SSConstant.SHEET_SPACETOBORDER
                    mergedCellSize.setWidth(mergedCellSize.getWidth() - indent)

                    if (textWidth + SSConstant.SHEET_SPACETOBORDER > mergedCellSize.getWidth()) {
                        val widths = FloatArray(contents.length)
                        paint.getTextWidths(contents, 0, contents.length, widths)
                        var s = 0
                        var sum = widths[0]
                        while (sum < mergedCellSize.getWidth() - SSConstant.SHEET_SPACETOBORDER) {
                            s++
                            sum += widths[s]
                        }

                        contents = contents.substring(0, s)
                    }
                }

                CellStyle.ALIGN_RIGHT -> {
                    mergedCellSize.setWidth(mergedCellSize.getWidth() - indent)
                    if (textWidth + SSConstant.SHEET_SPACETOBORDER > mergedCellSize.getWidth()) {
                        val widths = FloatArray(contents.length)
                        paint.getTextWidths(contents, 0, contents.length, widths)

                        var s = contents.length
                        var sum = 0f
                        while (sum < mergedCellSize.getWidth() - SSConstant.SHEET_SPACETOBORDER) {
                            s--
                            sum += widths[s]
                        }

                        contents = contents.substring(s + 1, contents.length)
                        textWidth = (paint.measureText(contents)).toInt()
                    }
                    left += mergedCellSize.getWidth() - textWidth - SSConstant.SHEET_SPACETOBORDER
                }

                CellStyle.ALIGN_CENTER -> left += (mergedCellSize.getWidth() - textWidth) / 2

                else -> {
                }
            }

            // 垂直对齐
            when (style.getVerticalAlign()) {
                CellStyle.VERTICAL_TOP -> top += SSConstant.SHEET_SPACETOBORDER

                CellStyle.VERTICAL_CENTER,
                CellStyle.VERTICAL_JUSTIFY -> top += (mergedCellSize.getHeight() - textHeight) / 2

                CellStyle.VERTICAL_BOTTOM -> top += mergedCellSize.getHeight() - textHeight

                else -> {
                }
            }
            val x = left - mergedCellSize.getNovisibleWidth() - (cellInfor.getWidth() - cellInfor.getVisibleWidth())
            val y = top - fm.ascent - mergedCellSize.getNoVisibleHeight() - (cellInfor.getHeight() - cellInfor.getVisibleHeight())

            if (cell.getExpandedRangeAddressIndex() >= 0) {
                /**
                 * not draw content here, it may be covered by next blank cell's background
                 */
                //canvas.drawText(contents, x, y, paint);
                sheetView!!.addExtendCell(cell, RectF(cellRect), x, y, contents)
            } else {
                canvas.drawText(contents, x, y, paint)
            }

            canvas.restore()
        }
    }

    /**
     *
     */
    fun dispose() {
        sheetView = null

        if (cellBorderView != null) {
            cellBorderView!!.dispose()
            cellBorderView = null
        }

        cellRect = null

        if (mergedCellSize != null) {
            mergedCellSize!!.dispose()
            mergedCellSize = null
        }

        if (mergedCellMgr != null) {
            mergedCellMgr!!.dispose()
            mergedCellMgr = null
        }

        cellInfor = null

        strBuilder = null
        if (tableStyleKit != null) {
            tableStyleKit!!.dispose()
            tableStyleKit = null
        }
    }

    companion object {
        @JvmStatic
        fun isComplexText(cell: Cell): Boolean {
            if (cell.getCellType() == Cell.CELL_TYPE_STRING && cell.getStringCellValueIndex() >= 0) {
                val value = cell.getSheet()!!.getWorkbook()!!.getSharedItem(cell.getStringCellValueIndex())
                return (value is SectionElement)
            }

            return false
        }
    }
}
