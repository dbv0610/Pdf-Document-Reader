/*
 * 文件名称:          MinRowAndColumnInformation.java
 *
 * 编译器:            android2.2
 * 时间:              下午2:22:31
 */
package com.wxiwei.office.ss.other

import com.wxiwei.office.ss.model.baseModel.Row
import com.wxiwei.office.ss.model.baseModel.Sheet
import com.wxiwei.office.ss.model.baseModel.Workbook
import com.wxiwei.office.system.OpenTrace
import kotlin.math.abs

/**
 * min row and column information of current sheet after scrolling
 *
 * Read版本:        Read V1.0
 *
 * 作者:            jqin
 *
 * 日期:            2011-12-27
 *
 * 负责人:           jqin
 *
 * 负责小组:
 */
class SheetScroller {
    //current min row and column index
    private var minRowIndex = 0
    private var minColumnIndex = 0

    //current min row height and column width(zoom = 1)
    private var columnWidth = 0f
    private var rowHeight = 0f

    private var isRowAllVisible = true
    private var isColumnAllVisible = true

    //current min visible row height and visible column width(zoom = 1)
    private var visibleRowHeight = 0.0
    private var visibleColumnWidth = 0.0
    // hidden part of the first row/column; visibleRowHeight/visibleColumnWidth hold the visible part
    private var rowOffset = 0.0
    private var columnOffset = 0.0
    private var lastScrollX = Int.MIN_VALUE
    private var lastScrollY = Int.MIN_VALUE
    private var updateCount = 0
    private var rowsVisited = 0
    private var columnsVisited = 0

    fun reset() {
        setMinRowIndex(0)
        setMinColumnIndex(0)

        setRowHeight(0f)
        setColumnWidth(0f)

        setVisibleRowHeight(0.0)
        setVisibleColumnWidth(0.0)

        setRowAllVisible(true)
        setColumnAllVisible(true)
    }

    fun resetPositionCache() {
        lastScrollX = Int.MIN_VALUE
        lastScrollY = Int.MIN_VALUE
    }

    fun update(sheet: Sheet, scrollX: Int, scrollY: Int) {
        val started = android.os.SystemClock.uptimeMillis()
        updateCount++
        rowsVisited = 0
        columnsVisited = 0
        val paneInfo = sheet.getPaneInformation()
        if (paneInfo == null && lastScrollX != Int.MIN_VALUE &&
            abs(scrollX - lastScrollX) <= sheet.getDefaultColWidth().coerceAtLeast(72) * 200 &&
            abs(scrollY - lastScrollY) <= sheet.getDefaultRowHeight().coerceAtLeast(18) * 200) {
            updateRowsIncremental(sheet, scrollY - lastScrollY)
            updateColumnsIncremental(sheet, scrollX - lastScrollX)
            lastScrollX = scrollX
            lastScrollY = scrollY
            traceUpdate(sheet, scrollX, scrollY, "incremental", started)
            return
        }
        reset()

        setVisibleRowHeight(scrollY.toDouble())
        setVisibleColumnWidth(scrollX.toDouble())

        if (paneInfo != null) {
            setMinRowIndex(paneInfo.getHorizontalSplitTopRow().toInt())
            setMinColumnIndex(paneInfo.getVerticalSplitLeftColumn().toInt())
        }

        val maxSheetRows = if (sheet.getWorkbook()!!.isBefore07Version()) Workbook.MAXROW_03 else Workbook.MAXROW_07
        val maxSheetColumns = if (sheet.getWorkbook()!!.isBefore07Version()) Workbook.MAXCOLUMN_03 else Workbook.MAXCOLUMN_07
        var row: Row?
        if (scrollY > 0) {
            val firstRow = sheet.getFirstRowNum()
            val lastRow = sheet.getLastRowNum()
            val defaultRowHeight = sheet.getDefaultRowHeight()

            //update row information
            while (visibleRowHeight >= 1 && minRowIndex <= maxSheetRows) {
                // Rows after the last physically defined row all use the
                // default height. Skip them as a block instead of walking up
                // to Excel's 1,048,576-row limit on every touch/frame.
                if (minRowIndex > lastRow) {
                    val skippedRows = (visibleRowHeight / defaultRowHeight).toInt()
                    if (skippedRows > 0) {
                        minRowIndex += skippedRows
                        visibleRowHeight -= skippedRows * defaultRowHeight
                        rowsVisited += skippedRows
                    }
                    if (visibleRowHeight < 1) break
                }
                if (minRowIndex >= firstRow && minRowIndex <= lastRow) {
                    row = sheet.getRow(minRowIndex)
                } else {
                    row = null
                }

                if (row == null || (row != null && !row.isZeroHeight())) {
                    rowHeight = (if (row == null) defaultRowHeight.toFloat() else row.getRowPixelHeight())
                    visibleRowHeight = visibleRowHeight - rowHeight
                }
                minRowIndex++
                rowsVisited++
            }

            if (minRowIndex != maxSheetRows) {
                minRowIndex--
                setVisibleRowHeight(Math.abs(getVisibleRowHeight()))
                if (getVisibleRowHeight() < 1) {
                    minRowIndex++
                    setVisibleRowHeight(0.0)
                } else {
                    setRowAllVisible(false)
                }
            } else {
                minRowIndex--
                row = sheet.getRow(minRowIndex)
                while (row != null && row.isZeroHeight()) {
                    minRowIndex--
                    row = sheet.getRow(getMinRowIndex())
                }

                setVisibleRowHeight(0.0)
            }
        }

        if (scrollX > 0) {
            //update column information
            while (visibleColumnWidth >= 1 && minColumnIndex <= maxSheetColumns) {
                if (!sheet.isColumnHidden(minColumnIndex)) {
                    columnWidth = sheet.getColumnPixelWidth(minColumnIndex)
                    visibleColumnWidth -= columnWidth.toDouble()
                }
                minColumnIndex++
                columnsVisited++
            }

            if (minColumnIndex != maxSheetColumns) {
                minColumnIndex--
                setVisibleColumnWidth(Math.abs(getVisibleColumnWidth()))
                if (getVisibleColumnWidth() < 1) {
                    minColumnIndex++
                    setVisibleColumnWidth(0.0)
                } else {
                    setColumnAllVisible(false)
                }
            } else {
                minColumnIndex--
                while (sheet.isColumnHidden(minColumnIndex)) {
                    minColumnIndex--
                }
                setVisibleColumnWidth(0.0)
            }
        }
        rowOffset = if (isRowAllVisible) 0.0 else rowHeightAt(sheet, minRowIndex) - visibleRowHeight
        columnOffset = if (isColumnAllVisible) 0.0 else sheet.getColumnPixelWidth(minColumnIndex) - visibleColumnWidth
        lastScrollX = scrollX
        lastScrollY = scrollY
        traceUpdate(sheet, scrollX, scrollY, "full", started)
    }

    private fun traceUpdate(sheet: Sheet, scrollX: Int, scrollY: Int, mode: String, started: Long) {
        val elapsed = android.os.SystemClock.uptimeMillis() - started
        if (elapsed >= 8 || updateCount % 20 == 0) {
            OpenTrace.d(
                "excel.scroll.scroller mode=$mode elapsed=${elapsed}ms " +
                    "scroll=$scrollX,$scrollY visitedRows=$rowsVisited visitedCols=$columnsVisited " +
                    "min=${minRowIndex},${minColumnIndex} sheet=${sheet.getSheetName()}"
            )
        }
    }

    private fun rowHeightAt(sheet: Sheet, index: Int): Float {
        val row = sheet.getRow(index)
        if (row != null && row.isZeroHeight()) return 0f
        return if (row == null) sheet.getDefaultRowHeight().toFloat() else row.getRowPixelHeight()
    }

    private fun updateRowsIncremental(sheet: Sheet, delta: Int) {
        var offset = rowOffset + delta
        if (delta >= 0) {
            while (offset >= 1) {
                val height = rowHeightAt(sheet, minRowIndex)
                if (height < 1) {
                    minRowIndex++
                    rowsVisited++
                    continue
                }
                if (offset < height) break
                offset -= height
                minRowIndex++
                rowsVisited++
            }
        } else {
            while (offset < 0 && minRowIndex > 0) {
                minRowIndex--
                rowsVisited++
                offset += rowHeightAt(sheet, minRowIndex)
            }
            if (offset < 0) offset = 0.0
        }
        // same convention as the full update: visibleRowHeight is the visible part of the row
        rowOffset = if (offset < 1) 0.0 else offset
        isRowAllVisible = offset < 1
        visibleRowHeight = if (isRowAllVisible) 0.0 else rowHeightAt(sheet, minRowIndex) - offset
    }

    private fun updateColumnsIncremental(sheet: Sheet, delta: Int) {
        var offset = columnOffset + delta
        if (delta >= 0) {
            while (offset >= 1) {
                if (sheet.isColumnHidden(minColumnIndex)) {
                    minColumnIndex++
                    columnsVisited++
                    continue
                }
                val width = sheet.getColumnPixelWidth(minColumnIndex)
                if (offset < width) break
                offset -= width
                minColumnIndex++
                columnsVisited++
            }
        } else {
            while (offset < 0 && minColumnIndex > 0) {
                minColumnIndex--
                columnsVisited++
                if (!sheet.isColumnHidden(minColumnIndex)) {
                    offset += sheet.getColumnPixelWidth(minColumnIndex)
                }
            }
            if (offset < 0) offset = 0.0
        }
        columnOffset = if (offset < 1) 0.0 else offset
        isColumnAllVisible = offset < 1
        visibleColumnWidth = if (isColumnAllVisible) 0.0 else sheet.getColumnPixelWidth(minColumnIndex) - offset
    }

    /**
     * @return Returns the minRowIndex.
     */
    fun getMinRowIndex(): Int {
        return minRowIndex
    }

    /**
     * @param minRowIndex The minRowIndex to set.
     */
    fun setMinRowIndex(minRowIndex: Int) {
        this.minRowIndex = minRowIndex
    }

    /**
     *
     */
    fun dispose() {
    }

    /**
     * @return Returns the minColumnIndex.
     */
    fun getMinColumnIndex(): Int {
        return minColumnIndex
    }

    /**
     * @param minColumnIndex The minColumnIndex to set.
     */
    fun setMinColumnIndex(minColumnIndex: Int) {
        this.minColumnIndex = minColumnIndex
    }

    /**
     * @return Returns the columnWidth.
     */
    fun getColumnWidth(): Float {
        return columnWidth
    }

    /**
     * @param columnWidth The columnWidth to set.
     */
    fun setColumnWidth(columnWidth: Float) {
        this.columnWidth = columnWidth
    }

    /**
     * @return Returns the rowHeight.
     */
    fun getRowHeight(): Float {
        return rowHeight
    }

    /**
     * @param rowHeight The rowHeight to set.
     */
    fun setRowHeight(rowHeight: Float) {
        this.rowHeight = rowHeight
    }

    /**
     * @return Returns the isRowAllVisible.
     */
    fun isRowAllVisible(): Boolean {
        return isRowAllVisible
    }

    /**
     * @param isRowAllVisible The isRowAllVisible to set.
     */
    fun setRowAllVisible(isRowAllVisible: Boolean) {
        this.isRowAllVisible = isRowAllVisible
    }

    /**
     * @return Returns the isColumnAllVisible.
     */
    fun isColumnAllVisible(): Boolean {
        return isColumnAllVisible
    }

    /**
     * @param isColumnAllVisible The isColumnAllVisible to set.
     */
    fun setColumnAllVisible(isColumnAllVisible: Boolean) {
        this.isColumnAllVisible = isColumnAllVisible
    }

    /**
     * @return Returns the visibleRowHeight.
     */
    fun getVisibleRowHeight(): Double {
        return visibleRowHeight
    }

    /**
     * @param visibleRowHeight The visibleRowHeight to set.
     */
    fun setVisibleRowHeight(visibleRowHeight: Double) {
        this.visibleRowHeight = visibleRowHeight
    }

    /**
     * @return Returns the visibleColumnWidth.
     */
    fun getVisibleColumnWidth(): Double {
        return visibleColumnWidth
    }

    /**
     * @param visibleColumnWidth The visibleColumnWidth to set.
     */
    fun setVisibleColumnWidth(visibleColumnWidth: Double) {
        this.visibleColumnWidth = visibleColumnWidth
    }
}
