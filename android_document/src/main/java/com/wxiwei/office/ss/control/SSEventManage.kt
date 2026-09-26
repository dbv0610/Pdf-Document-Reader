package com.wxiwei.office.ss.control

import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import com.wxiwei.office.constant.EventConstant
import com.wxiwei.office.ss.model.CellRangeAddress
import com.wxiwei.office.ss.model.baseModel.Cell
import com.wxiwei.office.ss.model.baseModel.Row
import com.wxiwei.office.ss.model.baseModel.Sheet
import com.wxiwei.office.ss.model.baseModel.Workbook
import com.wxiwei.office.ss.other.DrawingCell
import com.wxiwei.office.ss.other.FocusCell
import com.wxiwei.office.ss.view.SheetView
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.ITimerListener
import com.wxiwei.office.system.OpenTrace
import com.wxiwei.office.system.beans.AEventManage
import com.wxiwei.office.system.beans.ATimer

private var FocusCell.rect: Rect?
    get() = getRect()
    set(value) = setRect(value)
private var FocusCell.type: Int
    get() = getType()
    set(value) = setType(value.toShort())
private var FocusCell.row: Int
    get() = getRow()
    set(value) = setRow(value)
private var FocusCell.column: Int
    get() = getColumn()
    set(value) = setColumn(value)

private var DrawingCell.left: Float
    get() = getLeft()
    set(value) = setLeft(value)
private var DrawingCell.top: Float
    get() = getTop()
    set(value) = setTop(value)
private var DrawingCell.width: Float
    get() = getWidth()
    set(value) = setWidth(value)
private var DrawingCell.height: Float
    get() = getHeight()
    set(value) = setHeight(value)
private var DrawingCell.visibleWidth: Float
    get() = getVisibleWidth()
    set(value) = setVisibleWidth(value)
private var DrawingCell.visibleHeight: Float
    get() = getVisibleHeight()
    set(value) = setVisibleHeight(value)
private var DrawingCell.rowIndex: Int
    get() = getRowIndex()
    set(value) = setRowIndex(value)
private var DrawingCell.columnIndex: Int
    get() = getColumnIndex()
    set(value) = setColumnIndex(value)

private val SheetView.columnHeaderHeight: Int get() = getColumnHeaderHeight()
private val SheetView.rowHeaderWidth: Int get() = getRowHeaderWidth()
private val SheetView.currentSheet: Sheet get() = getCurrentSheet()!!
private val SheetView.minRowAndColumnInformation: com.wxiwei.office.ss.other.SheetScroller get() = getMinRowAndColumnInformation()!!
private val SheetView.zoom: Float get() = getZoom()
private val SheetView.scrollX: Float get() = getScrollX()
private val SheetView.scrollY: Float get() = getScrollY()
private val SheetView.maxScrollY: Int get() = getMaxScrollY()
private val SheetView.maxScrollX: Int get() = getMaxScrollX()
private val SheetView.rowHeader: com.wxiwei.office.ss.view.RowHeader get() = getRowHeader()!!

private val Sheet.workbook: Workbook get() = getWorkbook()!!
private val Sheet.defaultRowHeight: Int get() = getDefaultRowHeight()
private val Sheet.firstRowNum: Int get() = getFirstRowNum()
private val Sheet.lastRowNum: Int get() = getLastRowNum()
private val Row.rowPixelHeight: Float get() = getRowPixelHeight()
private var Row.rowPixelHeightSet: Float
    get() = getRowPixelHeight()
    set(value) = setRowPixelHeight(value)
private val Row.rowNumber: Int get() = getRowNumber()
private val Row.firstCol: Int get() = getFirstCol()
private val Row.lastCol: Int get() = getLastCol()
private val Cell.rangeAddressIndex: Int get() = getRangeAddressIndex()
private val CellRangeAddress.firstRow: Int get() = getFirstRow()
private val CellRangeAddress.firstColumn: Int get() = getFirstColumn()
private val com.wxiwei.office.ss.other.SheetScroller.minRowIndex: Int get() = getMinRowIndex()
private val com.wxiwei.office.ss.other.SheetScroller.minColumnIndex: Int get() = getMinColumnIndex()
private val com.wxiwei.office.ss.other.SheetScroller.isRowAllVisible: Boolean get() = isRowAllVisible()
private val com.wxiwei.office.ss.other.SheetScroller.isColumnAllVisible: Boolean get() = isColumnAllVisible()
private val com.wxiwei.office.ss.other.SheetScroller.visibleRowHeight: Double get() = getVisibleRowHeight()
private val com.wxiwei.office.ss.other.SheetScroller.visibleColumnWidth: Double get() = getVisibleColumnWidth()

class SSEventManage(spreadsheet: Spreadsheet, control: IControl) : AEventManage(spreadsheet.context, control), ITimerListener {
    private val minDistance = 10
    private var longPress = false
    private var oldX = 0
    private var oldY = 0
    private var spreadsheet: Spreadsheet? = spreadsheet
    private var oldHeaderArea: FocusCell? = null
    private var newHeaderArea: FocusCell? = null
    private var actionDown = false
    private var scrolling = false
    private var timer: ATimer? = ATimer(1000, this, control)
    private var moveCount = 0
    private var gestureStart = 0L

    override fun actionPerformed() {
        timer?.stop()
        control.actionEvent(EventConstant.APP_GENERATED_PICTURE_ID, null)
    }

    private fun changingHeader(event: MotionEvent) {
        val newArea = newHeaderArea ?: return
        scrolling = true
        val x = event.x
        val y = event.y
        val area = newArea.rect ?: return
        when (newArea.type) {
            FocusCell.ROWHEADER.toInt() -> {
                area.bottom = Math.round((oldHeaderArea!!.rect!!.bottom + (y - oldY)))
                if (area.bottom <= area.top + minDistance) area.bottom = area.top + minDistance
            }
            FocusCell.COLUMNHEADER.toInt() -> {
                area.right = Math.round((oldHeaderArea!!.rect!!.right + (x - oldX)))
                if (area.right <= area.left + minDistance) area.right = area.left + minDistance
            }
        }
        spreadsheet?.getSheetView()?.changeHeaderArea(newArea)
    }

    private fun checkClickedCell(event: MotionEvent): Boolean {
        val ss = spreadsheet ?: return false
        val x = event.x
        val y = event.y
        val sheetView = ss.getSheetView()!!
        if (sheetView.columnHeaderHeight > y || sheetView.rowHeaderWidth > x) return false
        val cellInfo = DrawingCell()
        cellInfo.left = sheetView.rowHeaderWidth.toFloat()
        cellInfo.top = sheetView.columnHeaderHeight.toFloat()
        cellInfo.rowIndex = sheetView.minRowAndColumnInformation.minRowIndex
        cellInfo.columnIndex = sheetView.minRowAndColumnInformation.minColumnIndex
        val maxRows = if (sheetView.currentSheet.workbook.isBefore07Version()) Workbook.MAXROW_03 else Workbook.MAXROW_07
        while (cellInfo.top <= y && cellInfo.rowIndex <= maxRows) {
            val row = sheetView.currentSheet.getRow(cellInfo.rowIndex)
            if (row != null && row.isZeroHeight()) {
                cellInfo.rowIndex++
                continue
            }
            cellInfo.height = Math.round((if (row == null) sheetView.currentSheet.defaultRowHeight.toFloat() else row.getRowPixelHeight()) * sheetView.zoom).toFloat()
            cellInfo.visibleHeight = if (cellInfo.rowIndex == sheetView.minRowAndColumnInformation.minRowIndex && !sheetView.minRowAndColumnInformation.isRowAllVisible) {
                Math.round(sheetView.minRowAndColumnInformation.visibleRowHeight * sheetView.zoom).toFloat()
            } else cellInfo.height
            cellInfo.top += cellInfo.visibleHeight
            cellInfo.rowIndex++
        }
        val maxColumns = if (sheetView.currentSheet.workbook.isBefore07Version()) Workbook.MAXCOLUMN_03 else Workbook.MAXCOLUMN_07
        while (cellInfo.left <= x && cellInfo.columnIndex <= maxColumns) {
            if (sheetView.currentSheet.isColumnHidden(cellInfo.columnIndex)) {
                cellInfo.columnIndex++
                continue
            }
            cellInfo.width = Math.round(sheetView.currentSheet.getColumnPixelWidth(cellInfo.columnIndex) * sheetView.zoom).toFloat()
            cellInfo.visibleWidth = if (cellInfo.columnIndex == sheetView.minRowAndColumnInformation.minColumnIndex && !sheetView.minRowAndColumnInformation.isColumnAllVisible) {
                Math.round(sheetView.minRowAndColumnInformation.visibleColumnWidth * sheetView.zoom).toFloat()
            } else cellInfo.width
            cellInfo.left += cellInfo.visibleWidth
            cellInfo.columnIndex++
        }
        sheetView.currentSheet.setActiveCellType(Sheet.ACTIVECELL_SINGLE)
        sheetView.selectedCell(cellInfo.rowIndex - 1, cellInfo.columnIndex - 1)
        ss.getControl().actionEvent(EventConstant.APP_CONTENT_SELECTED, null)
        ss.abortDrawing()
        ss.postInvalidate()
        return true
    }

    private fun changeHeaderEnd(): Boolean {
        val oldArea = oldHeaderArea ?: return false
        val ss = spreadsheet ?: return false
        var ret = false
        scrolling = false
        var off: Float
        val sheet = ss.getSheetView()!!.getCurrentSheet()!!
        var row: Row?
        var cell: Cell?
        var index: Int
        when (oldArea.type) {
            FocusCell.ROWHEADER.toInt() -> {
                ret = true
                index = newHeaderArea!!.row
                row = sheet.getRow(index)
                if (row == null) {
                    row = Row(0)
                    row.setRowNumber(index)
                    row.setSheet(sheet)
                    sheet.addRow(row)
                } else {
                    while (sheet.getRow(index)?.isZeroHeight() == true) index--
                    row = sheet.getRow(index)
                    if (row == null) {
                        row = Row(0)
                        row.setRowNumber(index)
                        row.setSheet(sheet)
                        sheet.addRow(row)
                    }
                }
                off = (newHeaderArea!!.rect!!.bottom - newHeaderArea!!.rect!!.top).toFloat() - (oldArea.rect!!.bottom - oldArea.rect!!.top)
                row!!.setRowPixelHeight(Math.round(row.getRowPixelHeight() + off / ss.getSheetView()!!.getZoom()).toFloat())
                index = row.rowNumber
                while (index <= sheet.lastRowNum) {
                    row = sheet.getRow(index++)
                    if (row == null) continue
                    for (i in row.firstCol..row.lastCol) {
                        cell = row.getCell(i)
                        if (cell != null) {
                            if (cell!!.rangeAddressIndex >= 0) {
                                val range: CellRangeAddress = sheet.getMergeRange(cell!!.rangeAddressIndex)!!
                                cell = sheet.getRow(range.firstRow)!!.getCell(range.firstColumn)
                            }
                            cell!!.removeSTRoot()
                        }
                    }
                    row!!.setInitExpandedRangeAddress(false)
                }
            }
            FocusCell.COLUMNHEADER.toInt() -> {
                ret = true
                off = (newHeaderArea!!.rect!!.right - newHeaderArea!!.rect!!.left).toFloat() - (oldArea.rect!!.right - oldArea.rect!!.left)
                index = newHeaderArea!!.column
                while (sheet.isColumnHidden(index)) index--
                sheet.setColumnPixelWidth(index, Math.round(sheet.getColumnPixelWidth(index) + off / ss.getSheetView()!!.getZoom()).toInt())
                index = sheet.firstRowNum
                while (index <= sheet.lastRowNum) {
                    row = sheet.getRow(index++)
                    if (row == null) continue
                    for (i in maxOf(row.firstCol, oldArea.column)..row.lastCol) {
                        cell = row.getCell(i)
                        if (cell != null) {
                            if (cell!!.rangeAddressIndex >= 0) {
                                val range = sheet.getMergeRange(cell!!.rangeAddressIndex)!!
                                cell = sheet.getRow(range.firstRow)!!.getCell(range.firstColumn)
                            }
                            cell!!.removeSTRoot()
                        }
                    }
                    row!!.setInitExpandedRangeAddress(false)
                }
            }
        }
        ss.getSheetView()!!.updateMinRowAndColumnInfo()
        ss.getSheetView()!!.setDrawMovingHeaderLine(false)
        oldHeaderArea = null
        newHeaderArea = null
        return ret
    }

    private fun findClickedRowHeader(event: MotionEvent): Int {
        val sheetView = spreadsheet!!.getSheetView()!!
        val info = DrawingCell()
        info.top = sheetView.columnHeaderHeight.toFloat()
        info.rowIndex = sheetView.minRowAndColumnInformation.minRowIndex
        val maxRows = if (sheetView.currentSheet.workbook.isBefore07Version()) Workbook.MAXROW_03 else Workbook.MAXROW_07
        while (info.top <= event.y && info.rowIndex <= maxRows) {
            val row = sheetView.currentSheet.getRow(info.rowIndex)
            if (row != null && row.isZeroHeight()) { info.rowIndex++; continue }
            info.height = Math.round((if (row == null) sheetView.currentSheet.defaultRowHeight.toFloat() else row.getRowPixelHeight()) * sheetView.zoom).toFloat()
            info.visibleHeight = if (info.rowIndex == sheetView.minRowAndColumnInformation.minRowIndex && !sheetView.minRowAndColumnInformation.isRowAllVisible) Math.round(sheetView.minRowAndColumnInformation.visibleRowHeight * sheetView.zoom).toFloat() else info.height
            info.top += info.visibleHeight
            info.rowIndex++
        }
        return info.rowIndex - 1
    }

    private fun findClickedColumnHeader(event: MotionEvent): Int {
        val sheetView = spreadsheet!!.getSheetView()!!
        val info = DrawingCell()
        info.left = sheetView.rowHeaderWidth.toFloat()
        info.columnIndex = sheetView.minRowAndColumnInformation.minColumnIndex
        val maxColumns = if (sheetView.currentSheet.workbook.isBefore07Version()) Workbook.MAXCOLUMN_03 else Workbook.MAXCOLUMN_07
        while (info.left <= event.x && info.columnIndex <= maxColumns) {
            if (sheetView.currentSheet.isColumnHidden(info.columnIndex)) { info.columnIndex++; continue }
            info.width = Math.round(sheetView.currentSheet.getColumnPixelWidth(info.columnIndex) * sheetView.zoom).toFloat()
            info.visibleWidth = if (info.columnIndex == sheetView.minRowAndColumnInformation.minColumnIndex && !sheetView.minRowAndColumnInformation.isColumnAllVisible) Math.round(sheetView.minRowAndColumnInformation.visibleColumnWidth * sheetView.zoom).toFloat() else info.width
            info.left += info.visibleWidth
            info.columnIndex++
        }
        return info.columnIndex - 1
    }

    private fun checkClickedHeader(event: MotionEvent): Boolean {
        val ss = spreadsheet ?: return false
        val sheetView = ss.getSheetView()!!
        val x = event.x
        val y = event.y
        var ret = false
        if (sheetView.rowHeaderWidth > x && sheetView.columnHeaderHeight < y) {
            ret = true
            sheetView.currentSheet.setActiveCellType(Sheet.ACTIVECELL_ROW)
            sheetView.currentSheet.setActiveCellRow(findClickedRowHeader(event))
        } else if (sheetView.rowHeaderWidth < x && sheetView.columnHeaderHeight > y) {
            ret = true
            sheetView.currentSheet.setActiveCellType(Sheet.ACTIVECELL_COLUMN)
            sheetView.currentSheet.setActiveCellColumn(findClickedColumnHeader(event))
        }
        ss.getControl().actionEvent(EventConstant.APP_CONTENT_SELECTED, null)
        return ret
    }

    private fun actionUp(event: MotionEvent) {
        if (!actionDown) return
        actionDown = false
        val ret = if (scrolling) changeHeaderEnd() else if (!longPress && checkClickedCell(event)) true else checkClickedHeader(event)
        longPress = false
        if (ret) {
            if (timer?.isRunning == false) timer?.start() else timer?.restart()
        }
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        val ss = spreadsheet ?: return false
        val touchStarted = android.os.SystemClock.uptimeMillis()
        super.onTouch(v, event)
        if (event.pointerCount == 2) {
            scrolling = true
            actionDown = false
            if (newHeaderArea != null) {
                ss.getSheetView()!!.setDrawMovingHeaderLine(false)
                oldHeaderArea = null
                newHeaderArea = null
            }
            return true
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                actionDown = true
                moveCount = 0
                gestureStart = touchStarted
                spreadsheet?.getSheetView()?.onGestureStart()
                OpenTrace.d("excel.scroll.touch action=DOWN x=${event.x} y=${event.y}")
            }
            MotionEvent.ACTION_MOVE -> {
                moveCount++
                changingHeader(event)
                ss.abortDrawing()
                ss.postInvalidateOnAnimation()
                val elapsed = android.os.SystemClock.uptimeMillis() - touchStarted
                if (elapsed >= 8 || moveCount % 20 == 0) {
                    OpenTrace.d(
                        "excel.scroll.touch action=MOVE elapsed=${elapsed}ms moves=$moveCount " +
                            "x=${event.x} y=${event.y} scroll=${ss.getSheetView()?.getScrollX()},${ss.getSheetView()?.getScrollY()}"
                    )
                }
            }
            MotionEvent.ACTION_UP -> {
                actionUp(event)
                scrolling = false
                actionDown = false
                ss.postInvalidateOnAnimation()
                OpenTrace.d(
                    "excel.scroll.touch action=UP gesture=${android.os.SystemClock.uptimeMillis() - gestureStart}ms " +
                        "moves=$moveCount totalHandler=${android.os.SystemClock.uptimeMillis() - touchStarted}ms"
                )
            }
        }
        return false
    }

    private fun findChangingRowHeader(event: MotionEvent) {
        val sheetView = spreadsheet!!.getSheetView()!!
        val info = DrawingCell()
        info.top = sheetView.columnHeaderHeight.toFloat()
        info.rowIndex = sheetView.minRowAndColumnInformation.minRowIndex
        var oldTop = Math.round(info.top)
        val rect = Rect()
        rect.top = oldTop
        rect.bottom = oldTop
        val maxRows = if (sheetView.currentSheet.workbook.isBefore07Version()) Workbook.MAXROW_03 else Workbook.MAXROW_07
        while (info.top <= event.y && info.rowIndex <= maxRows) {
            val row = sheetView.currentSheet.getRow(info.rowIndex)
            if (row != null && row.isZeroHeight()) { info.rowIndex++; continue }
            info.height = Math.round((if (row == null) sheetView.currentSheet.defaultRowHeight.toFloat() else row.getRowPixelHeight()) * sheetView.zoom).toFloat()
            info.visibleHeight = if (info.rowIndex == sheetView.minRowAndColumnInformation.minRowIndex && !sheetView.minRowAndColumnInformation.isRowAllVisible) Math.round(sheetView.minRowAndColumnInformation.visibleRowHeight * sheetView.zoom).toFloat() else info.height
            rect.top = rect.bottom
            rect.bottom = Math.round(info.top)
            oldTop = Math.round(info.top)
            info.top += info.visibleHeight
            info.rowIndex++
        }
        if (oldHeaderArea == null) oldHeaderArea = FocusCell()
        oldHeaderArea!!.type = FocusCell.ROWHEADER.toInt()
        if (event.y > (oldTop + info.top) / 2) {
            oldHeaderArea!!.row = info.rowIndex - 1
            rect.top = rect.bottom
            rect.bottom = Math.round(info.top)
        } else {
            oldHeaderArea!!.row = maxOf(info.rowIndex - 2, 0)
        }
        oldHeaderArea!!.rect = rect
    }

    private fun findChangingColumnHeader(event: MotionEvent) {
        val sheetView = spreadsheet!!.getSheetView()!!
        val info = DrawingCell()
        info.left = sheetView.rowHeaderWidth.toFloat()
        info.columnIndex = sheetView.minRowAndColumnInformation.minColumnIndex
        var oldLeft = Math.round(info.left)
        val rect = Rect()
        rect.left = oldLeft
        rect.right = oldLeft
        val maxColumns = if (sheetView.currentSheet.workbook.isBefore07Version()) Workbook.MAXCOLUMN_03 else Workbook.MAXCOLUMN_07
        while (info.left <= event.x && info.columnIndex <= maxColumns) {
            if (sheetView.currentSheet.isColumnHidden(info.columnIndex)) { info.columnIndex++; continue }
            info.width = Math.round(sheetView.currentSheet.getColumnPixelWidth(info.columnIndex) * sheetView.zoom).toFloat()
            info.visibleWidth = if (info.columnIndex == sheetView.minRowAndColumnInformation.minColumnIndex && !sheetView.minRowAndColumnInformation.isColumnAllVisible) Math.round(sheetView.minRowAndColumnInformation.visibleColumnWidth * sheetView.zoom).toFloat() else info.width
            rect.left = rect.right
            rect.right = Math.round(info.left)
            oldLeft = Math.round(info.left)
            info.left += info.visibleWidth
            info.columnIndex++
        }
        if (oldHeaderArea == null) oldHeaderArea = FocusCell()
        oldHeaderArea!!.type = FocusCell.COLUMNHEADER.toInt()
        if (event.x > (oldLeft + info.left) / 2) {
            oldHeaderArea!!.column = info.columnIndex - 1
            rect.left = rect.right
            rect.right = Math.round(info.left)
        } else {
            oldHeaderArea!!.column = maxOf(info.columnIndex - 2, 0)
        }
        oldHeaderArea!!.rect = rect
    }

    override fun onLongPress(e: MotionEvent) {
        super.onLongPress(e)
        longPress = true
        oldY = Math.round(e.y)
        oldX = Math.round(e.x)
        val sheetView = spreadsheet!!.getSheetView()!!
        if (sheetView.rowHeaderWidth > e.x && sheetView.columnHeaderHeight < e.y) findChangingRowHeader(e)
        else if (sheetView.rowHeaderWidth < e.x && sheetView.columnHeaderHeight > e.y) findChangingColumnHeader(e)
        if (oldHeaderArea != null) {
            newHeaderArea = oldHeaderArea!!.clone()
            spreadsheet!!.getSheetView()!!.changeHeaderArea(newHeaderArea!!)
            spreadsheet!!.getSheetView()!!.setDrawMovingHeaderLine(true)
            spreadsheet!!.abortDrawing()
            spreadsheet!!.postInvalidateOnAnimation()
        }
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        super.onScroll(e1, e2, distanceX, distanceY)
        val sheetView = spreadsheet!!.getSheetView()!!
        var dx = distanceX
        var dy = distanceY
        var change = false
        if (kotlin.math.abs(dx) > 2) change = true else dx = 0f
        if (kotlin.math.abs(dy) > 2) change = true else dy = 0f
        if (change) {
            isScroll = true
            scrolling = true
            sheetView.rowHeader.calculateRowHeaderWidth(sheetView.zoom)
            sheetView.scrollBy(Math.round(dx).toFloat(), Math.round(dy).toFloat(), true)
            spreadsheet!!.abortDrawing()
            spreadsheet!!.postInvalidateOnAnimation()
        }
        return true
    }

    override fun fling(velocityX: Int, velocityY: Int) {
        super.fling(velocityX, velocityY)
        val sheetView = spreadsheet!!.getSheetView()!!
        val zoom = sheetView.zoom
        val scrollX = Math.round(sheetView.scrollX * zoom)
        val scrollY = Math.round(sheetView.scrollY * zoom)
        oldY = 0
        oldX = 0
        if (kotlin.math.abs(velocityY) > kotlin.math.abs(velocityX)) {
            oldY = scrollY
            mScroller.fling(scrollX, scrollY, 0, velocityY, 0, 0, 0, sheetView.maxScrollY)
        } else {
            oldX = scrollX
            mScroller.fling(scrollX, scrollY, velocityX, 0, 0, sheetView.maxScrollX, 0, 0)
        }
        spreadsheet!!.abortDrawing()
        spreadsheet!!.postInvalidateOnAnimation()
    }

    override fun computeScroll() {
        super.computeScroll()
        if (!mScroller.computeScrollOffset()) return
        isFling = true
        val x = mScroller.currX
        val y = mScroller.currY
        if (x == oldX && y == oldY) {
            mScroller.abortAnimation()
            spreadsheet!!.abortDrawing()
            spreadsheet!!.postInvalidateOnAnimation()
            return
        }
        val sheetView = spreadsheet!!.getSheetView()!!
        var draw = false
        if (x != oldX && oldY == 0) {
            if (kotlin.math.abs(x - oldX) > 2) draw = true else oldX = x
        }
        if (y != oldY && oldX == 0) {
            if (kotlin.math.abs(oldY - y) > 2) draw = true else oldY = y
        }
        if (draw) {
            scrolling = true
            sheetView.rowHeader.calculateRowHeaderWidth(sheetView.zoom)
            sheetView.scrollBy(Math.round((x - oldX).toFloat()).toFloat(), Math.round((y - oldY).toFloat()).toFloat())
        }
        spreadsheet!!.abortDrawing()
        spreadsheet!!.postInvalidateOnAnimation()
        oldX = x
        oldY = y
    }

    override fun dispose() {
        super.dispose()
        spreadsheet = null
        oldHeaderArea?.dispose()
        oldHeaderArea = null
        newHeaderArea?.dispose()
        newHeaderArea = null
        timer?.dispose()
        timer = null
    }
}
