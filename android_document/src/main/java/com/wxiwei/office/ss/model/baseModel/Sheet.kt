/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          Sheet.java
 *
 * 编译器:            android2.2
 * 时间:              下午3:15:38
 */
package com.wxiwei.office.ss.model.baseModel

import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.common.shape.IShape
import com.wxiwei.office.constant.SSConstant
import com.wxiwei.office.simpletext.view.STRoot
import com.wxiwei.office.ss.model.CellRangeAddress
import com.wxiwei.office.ss.model.interfacePart.IReaderListener
import com.wxiwei.office.ss.model.sheetProperty.ColumnInfo
import com.wxiwei.office.ss.model.sheetProperty.PaneInformation
import com.wxiwei.office.ss.model.table.SSTable
import java.util.concurrent.ConcurrentHashMap

/**
 * Sheet 表
 *
 * Read版本:        Read V1.0
 * 作者:            ljj8494
 * 日期:            2012-2-16
 * 负责人:          ljj8494
 */
open class Sheet {
    @JvmField
    protected var book: Workbook? = null

    //
    private var isGridsPrinted = false

    //
    private var firstRow = 0

    //
    private var lastRow = 0

    //
    private var activeCellRow = 0

    //
    private var activeCellColumn = 0

    //max scroll XY
    private var maxScrollX: Float

    private var maxScrollY: Float

    //current scroll XY
    private var scrollX = 0

    private var scrollY = 0

    //sheet type
    private var type: Short = 0

    private var activeCellType: Short

    //zoom
    private var zoom = 1f

    // sheet name
    private var sheetName: String? = null

    //
    private var activeCell: Cell? = null

    //
    @JvmField
    protected var rows: MutableMap<Int, Row>? = null

    //
    private var merges: MutableList<CellRangeAddress>? = null

    //
    private var paneInformation: PaneInformation? = null

    //column style
    private var columnInfoList: MutableList<ColumnInfo>? = null

    @JvmField
    protected var shapesList: MutableList<IShape>? = null

    private var defaultRowHeight = SSConstant.DEFAULT_ROW_HEIGHT

    private var defaultColWidth = SSConstant.DEFAULT_COLUMN_WIDTH

    private var state: Short = 0

    //
    private var iReaderListener: IReaderListener? = null

    //
    private var rootViewMap: MutableList<STRoot?>? = null

    //table
    private var tableList: MutableList<SSTable>? = null

    /**
     *
     */
    init {
        activeCellType = ACTIVECELL_SINGLE
        rows = ConcurrentHashMap()
        merges = ArrayList()
        maxScrollX = Int.MAX_VALUE.toFloat()
        maxScrollY = Int.MAX_VALUE.toFloat()
        shapesList = ArrayList()
    }

    /**
     *
     * @param book
     */
    fun setWorkbook(book: Workbook?) {
        this.book = book
    }

    /**
     *
     * @return
     */
    fun getWorkbook(): Workbook? = book

    /**
     * Moves the rows at or after [at] by [delta] (insert when > 0); with a negative [delta] the
     * rows at..at-delta-1 are taken out and returned. Merged ranges move along.
     */
    fun shiftRows(at: Int, delta: Int): List<Row> {
        val rows = rows!!
        val removed = ArrayList<Row>()
        if (delta < 0) for (r in at until at - delta) rows.remove(r)?.let { removed.add(it) }
        val from = if (delta < 0) at - delta else at
        val moving = rows.keys.filter { it >= from }.sorted().let { if (delta > 0) it.reversed() else it }
        for (k in moving) {
            val row = rows.remove(k) ?: continue
            val n = k + delta
            row.setRowNumber(n)
            row.cellCollection().forEach { it.setRowNumber(n) }
            rows[n] = row
        }
        recomputeRowBounds()
        for (m in merges!!) shiftRange(m, true, at, delta)
        return removed
    }

    /** Puts rows taken out by [shiftRows] back (after the band was inserted again). */
    fun restoreRows(list: List<Row>) {
        for (row in list) rows!![row.getRowNumber()] = row
        recomputeRowBounds()
    }

    /** Moves columns at or after [at] by [delta]; returns the removed cells by row, then column. */
    fun shiftColumns(at: Int, delta: Int): Map<Int, Map<Int, Cell>> {
        val removed = HashMap<Int, Map<Int, Cell>>()
        for ((r, row) in rows!!) row.shiftCells(at, delta).takeIf { it.isNotEmpty() }?.let { removed[r] = it }
        for (m in merges!!) shiftRange(m, false, at, delta)
        columnInfoList?.let { list ->
            val it = list.iterator()
            while (it.hasNext()) {
                val info = it.next()
                val range = CellRangeAddress(0, info.getFirstCol(), 0, info.getLastCol())
                if (!shiftRange(range, false, at, delta)) { it.remove(); continue }
                info.setFirstCol(range.getFirstColumn()); info.setLastCol(range.getLastColumn())
            }
        }
        return removed
    }

    /**
     * Moves [range] for an insert (delta > 0) or delete before/at [at]; a range entirely deleted
     * is parked out of reach and false is returned.
     */
    fun shiftRange(range: CellRangeAddress, rows: Boolean, at: Int, delta: Int): Boolean {
        var lo = if (rows) range.getFirstRow() else range.getFirstColumn()
        var hi = if (rows) range.getLastRow() else range.getLastColumn()
        if (delta > 0) {
            if (lo >= at) lo += delta
            if (hi >= at) hi += delta
        } else {
            val end = at - delta
            lo = when { lo < at -> lo; lo >= end -> lo + delta; else -> at }
            hi = when { hi < at -> hi; hi >= end -> hi + delta; else -> at - 1 }
        }
        val alive = hi >= lo
        if (!alive) { lo = if (rows) PARKED_ROW else PARKED_COLUMN; hi = lo }
        if (rows) { range.setFirstRow(lo); range.setLastRow(hi) } else { range.setFirstColumn(lo); range.setLastColumn(hi) }
        return alive
    }

    private fun recomputeRowBounds() {
        val keys = rows!!.keys
        firstRow = keys.minOrNull() ?: 0
        lastRow = keys.maxOrNull() ?: 0
    }

    /**
     * add a row to the sheet
     */
    fun addRow(row: Row?) {
        if (row == null) {
            return
        }

        rows!![Integer.valueOf(row.getRowNumber())] = row
        if (rows!!.size == 1) {
            firstRow = row.getRowNumber()
            lastRow = row.getRowNumber()
        } else {
            firstRow = Math.min(firstRow, row.getRowNumber())
            lastRow = Math.max(lastRow, row.getRowNumber())
        }
    }

    /**
     *
     * @param range
     * @return current size of list
     */
    fun addMergeRange(range: CellRangeAddress): Int {
        merges!!.add(range)
        return merges!!.size
    }

    // ---- a selected range (edit mode): from the active cell to this corner ----
    private var selectionEndRow = -1
    private var selectionEndColumn = -1

    /** Selects from the active cell to [row], [col]; a new active cell ends the range. */
    fun setSelectionEnd(row: Int, col: Int) {
        selectionEndRow = row
        selectionEndColumn = col
    }

    /**
     * The selected range, grown to cover the merged cells it cuts (like Excel), or null when only
     * the active cell (or its merged range) is selected.
     */
    fun getSelectionRange(): CellRangeAddress? {
        if (selectionEndRow < 0 || selectionEndColumn < 0) return null
        var r1 = minOf(activeCellRow, selectionEndRow); var r2 = maxOf(activeCellRow, selectionEndRow)
        var c1 = minOf(activeCellColumn, selectionEndColumn); var c2 = maxOf(activeCellColumn, selectionEndColumn)
        var grown = true
        while (grown) {
            grown = false
            for (m in merges!!) {
                if (m.getFirstRow() > r2 || m.getLastRow() < r1 || m.getFirstColumn() > c2 || m.getLastColumn() < c1) continue
                if (m.getFirstRow() < r1) { r1 = m.getFirstRow(); grown = true }
                if (m.getLastRow() > r2) { r2 = m.getLastRow(); grown = true }
                if (m.getFirstColumn() < c1) { c1 = m.getFirstColumn(); grown = true }
                if (m.getLastColumn() > c2) { c2 = m.getLastColumn(); grown = true }
            }
        }
        val range = CellRangeAddress(r1, c1, r2, c2)
        // a single cell or exactly one merged range is not a range selection
        if (r1 == r2 && c1 == c2) return null
        if (merges!!.any { it.getFirstRow() == r1 && it.getLastRow() == r2 && it.getFirstColumn() == c1 && it.getLastColumn() == c2 }) return null
        return range
    }

    /** Index of the live merged range containing the cell, or -1. */
    fun mergeIndexAt(row: Int, col: Int): Int {
        for (i in merges!!.indices) if (isLiveMerge(merges!![i]) && merges!![i].isInRange(row, col)) return i
        return -1
    }

    /** False for a range parked by [shiftRange] or [parkMerge] (deleted/unmerged, kept to keep indexes). */
    fun isLiveMerge(range: CellRangeAddress): Boolean =
        !(range.getFirstRow() == PARKED_ROW && range.getLastRow() == PARKED_ROW) &&
            !(range.getFirstColumn() == PARKED_COLUMN && range.getLastColumn() == PARKED_COLUMN)

    /** Marks the cells of merged range [index] (they must exist) as merged. */
    fun markMerge(index: Int) {
        val m = merges!![index]
        for (r in m.getFirstRow()..m.getLastRow()) {
            val row = getRow(r) ?: continue
            for (c in m.getFirstColumn()..m.getLastColumn()) row.getCell(c, false)?.setRangeAddressIndex(index)
            row.setInitExpandedRangeAddress(false)
        }
        removeSTRoot()
    }

    /** Unmerges range [index]: its cells become single again, the entry is parked (indexes stay). */
    fun parkMerge(index: Int) {
        val m = merges!![index]
        for (r in m.getFirstRow()..m.getLastRow()) {
            val row = getRow(r) ?: continue
            for (c in m.getFirstColumn()..m.getLastColumn()) row.getCell(c, false)?.let { if (it.getRangeAddressIndex() == index) it.setRangeAddressIndex(-1) }
            row.setInitExpandedRangeAddress(false)
        }
        m.setFirstRow(PARKED_ROW); m.setLastRow(PARKED_ROW)
        removeSTRoot()
    }

    /** Puts a parked range [index] back at [bounds] and marks its cells. */
    fun restoreMerge(index: Int, bounds: CellRangeAddress) {
        val m = merges!![index]
        m.setFirstRow(bounds.getFirstRow()); m.setLastRow(bounds.getLastRow())
        m.setFirstColumn(bounds.getFirstColumn()); m.setLastColumn(bounds.getLastColumn())
        markMerge(index)
    }

    /** Live merged ranges, as the file lists them. */
    fun liveMerges(): List<CellRangeAddress> = merges!!.filter { isLiveMerge(it) }

    /**
     * get merge range count of this sheet
     */
    fun getMergeRangeCount(): Int {
        return merges!!.size
    }

    /**
     * get merge range for index
     */
    fun getMergeRange(index: Int): CellRangeAddress? {
        if (index < 0 || index >= merges!!.size) {
            return null
        }
        return merges!![index]
    }

    /**
     * Returns the logical row (not physical) 0-based.  If you ask for a row that is not
     * defined you get a null.  This is to say row 4 represents the fifth row on a sheet.
     *
     * @param rowIndex  row to get
     * @return Row representing the row number or null if its not defined on the sheet
     */
    fun getRow(rowIndex: Int): Row? {
        return rows!![Integer.valueOf(rowIndex)]
    }

    /**
     *
     * @param rowIndex
     * @return
     */
    fun getRowByColumnsStyle(rowIndex: Int): Row? {
        var row = rows!![Integer.valueOf(rowIndex)]
        if (row != null) {
            return row
        }

        val columnInfoList = columnInfoList
        if (columnInfoList == null || columnInfoList.size == 0) {
            return null
        }

        var columnInfo: ColumnInfo
        var index = 0
        while (index < columnInfoList.size) {
            columnInfo = columnInfoList[index++]
            val cellStyle = book!!.getCellStyle(columnInfo.getStyle())
            if (cellStyle != null
                && ((cellStyle.getFillPatternType() == BackgroundAndFill.FILL_SOLID && (cellStyle.getFgColor() and 0xFFFFFF) != 0xFFFFFF)
                        || cellStyle.getBorderLeft() > 0
                        || cellStyle.getBorderTop() > 0
                        || cellStyle.getBorderRight() > 0
                        || cellStyle.getBorderBottom() > 0)
            ) {
                row = Row(1)
                row.setRowNumber(rowIndex)
                row.setRowPixelHeight(defaultRowHeight.toFloat())
                row.setSheet(this)
                row.completed()

                rows!![rowIndex] = row
                return row
            }
        }

        return null
    }

    /**
     * Returns the number of physically defined rows (NOT the number of rows in the sheet)
     */
    fun getPhysicalNumberOfRows(): Int {
        return rows!!.size
    }

    /**
     * @return Returns the sheetName.
     */
    fun getSheetName(): String? = sheetName

    /**
     * @param sheetName The sheetName to set.
     */
    fun setSheetName(sheetName: String?) {
        this.sheetName = sheetName
    }

    /**
     * @return Returns the zoom.
     */
    fun getZoom(): Float = zoom

    /**
     * @param zoom The zoom to set.
     */
    fun setZoom(zoom: Float) {
        this.zoom = zoom
    }

    /**
     *
     */
    fun getMaxScrollX(): Float = maxScrollX

    /**
     *
     * @return
     */
    fun getMaxScrollY(): Float = maxScrollY

    /**
     * @return Returns the scrollX.
     */
    fun getScrollX(): Int = scrollX

    /**
     * @param scrollX The scrollX to set.
     */
    fun setScrollX(scrollX: Int) {
        this.scrollX = scrollX
    }

    /**
     * @return Returns the scrollY.
     */
    fun getScrollY(): Int = scrollY

    /**
     * @param scrollY The scrollY to set.
     */
    fun setScrollY(scrollY: Int) {
        this.scrollY = scrollY
    }

    /**
     *
     * @param scrollX
     * @param scrollY
     */
    fun setScroll(scrollX: Int, scrollY: Int) {
        this.scrollX = scrollX
        this.scrollY = scrollY
    }

    /**
     * @return Returns the firstRow.
     */
    fun getFirstRowNum(): Int = firstRow

    /**
     * @param firstRow The firstRow to set.
     */
    fun setFirstRowNum(firstRow: Int) {
        this.firstRow = firstRow
    }

    /**
     * @return Returns the lastRow.
     */
    fun getLastRowNum(): Int = lastRow

    /**
     * @param lastRow The lastRow to set.
     */
    fun setLastRowNum(lastRow: Int) {
        this.lastRow = lastRow
    }

    /**
     *
     * @param columnInfo
     */
    fun addColumnInfo(columnInfo: ColumnInfo) {
        if (columnInfoList == null) {
            columnInfoList = ArrayList()
        }
        columnInfoList!!.add(columnInfo)
    }

    /**
     * Returns the CellStyle index that applies to the given
     *  (0 based) column, or 0 if no style has been
     *  set for that column
     */
    fun getColumnStyle(column: Int): Int {
        val columnInfoList = columnInfoList
        if (columnInfoList != null) {
            var columnInfo: ColumnInfo
            var index = 0
            while (index < columnInfoList.size) {
                columnInfo = columnInfoList[index++]
                if (columnInfo.getFirstCol() <= column && columnInfo.getLastCol() >= column) {
                    return columnInfo.getStyle()
                }
            }
        }
        return 0
    }

    fun setColumnPixelWidth(column: Int, width: Int) {
        val columnInfoList = columnInfoList
        if (columnInfoList != null) {
            var columnInfo: ColumnInfo
            var index = 0
            while (index < columnInfoList.size) {
                columnInfo = columnInfoList[index++]
                if (columnInfo.getFirstCol() == column && columnInfo.getLastCol() == column) {
                    columnInfo.setColWidth(width.toFloat())
                    return
                } else if (columnInfo.getFirstCol() == column) {
                    val columnInfo3 = ColumnInfo(
                        column + 1, columnInfo.getLastCol(), columnInfo.getColWidth(),
                        columnInfo.getStyle(), columnInfo.isHidden()
                    )

                    columnInfo.setColWidth(width.toFloat())
                    columnInfo.setLastCol(column)

                    columnInfoList.add(columnInfo3)
                    return
                } else if (columnInfo.getLastCol() == column) {
                    val columnInfo1 = ColumnInfo(
                        columnInfo.getFirstCol(), column - 1, columnInfo.getColWidth(),
                        columnInfo.getStyle(), columnInfo.isHidden()
                    )

                    columnInfo.setColWidth(width.toFloat())
                    columnInfo.setFirstCol(column)

                    columnInfoList.add(columnInfo1)
                    return
                } else if (columnInfo.getFirstCol() < column && columnInfo.getLastCol() > column) {
                    val columnInfo1 = ColumnInfo(
                        columnInfo.getFirstCol(), column - 1, columnInfo.getColWidth(),
                        columnInfo.getStyle(), columnInfo.isHidden()
                    )

                    val columnInfo2 = ColumnInfo(
                        column + 1, columnInfo.getLastCol(), columnInfo.getColWidth(),
                        columnInfo.getStyle(), columnInfo.isHidden()
                    )

                    columnInfo.setFirstCol(column)
                    columnInfo.setLastCol(column)
                    columnInfo.setColWidth(width.toFloat())

                    columnInfoList.add(columnInfo1)
                    columnInfoList.add(columnInfo2)
                    return
                }
            }

            columnInfoList.add(ColumnInfo(column, column, width.toFloat(), 0, false))
        } else {
            this.columnInfoList = ArrayList()
            this.columnInfoList!!.add(ColumnInfo(column, column, width.toFloat(), 0, false))
        }
    }

    /**
     * @return Returns the columnPixelWidth.
     */
    fun getColumnPixelWidth(column: Int): Float {
        val columnInfoList = columnInfoList
        if (columnInfoList != null) {
            var columnInfo: ColumnInfo
            var index = 0
            while (index < columnInfoList.size) {
                columnInfo = columnInfoList[index++]
                if (columnInfo.getFirstCol() <= column && columnInfo.getLastCol() >= column) {
                    return columnInfo.getColWidth()
                }
            }
        }

        return defaultColWidth.toFloat()
    }

    fun getColumnInfo(column: Int): ColumnInfo? {
        val columnInfoList = columnInfoList
        if (columnInfoList != null) {
            var columnInfo: ColumnInfo
            var index = 0
            while (index < columnInfoList.size) {
                columnInfo = columnInfoList[index++]
                if (columnInfo.getFirstCol() <= column && columnInfo.getLastCol() >= column) {
                    return columnInfo
                }
            }
        }

        return null
    }

    /**
     * @return Returns the isGridsPrinted.
     */
    fun isGridsPrinted(): Boolean = isGridsPrinted

    /**
     * @param isGridsPrinted The isGridsPrinted to set.
     */
    fun setGridsPrinted(isGridsPrinted: Boolean) {
        this.isGridsPrinted = isGridsPrinted
    }

    /**
     * @return Returns the paneInformation.
     */
    fun getPaneInformation(): PaneInformation? {
        return null/*paneInformation*/
    }

    /**
     * @param paneInformation The paneInformation to set.
     */
    fun setPaneInformation(paneInformation: PaneInformation?) {
        this.paneInformation = paneInformation
    }

    /**
     * The frozen pane, if any. [getPaneInformation] stays null on purpose: the scroller would
     * skip the frozen rows; SheetView draws them over the scrolled area instead.
     */
    fun getFrozenPane(): PaneInformation? = paneInformation?.takeIf {
        it.isFreezePane() && (it.getHorizontalSplitTopRow() > 0 || it.getVerticalSplitLeftColumn() > 0)
    }

    // sheetView@showGridLines
    private var showGridLines = true

    /** An AutoFilter range (sheet or table); [filtered] holds the absolute columns being filtered. */
    class AutoFilter(val range: com.wxiwei.office.ss.model.CellRangeAddress, val filtered: MutableSet<Int> = HashSet())

    private val autoFilters = ArrayList<AutoFilter>()

    fun addAutoFilter(filter: AutoFilter) {
        autoFilters.add(filter)
    }

    fun getAutoFilters(): List<AutoFilter> = autoFilters

    fun isShowGridLines(): Boolean = showGridLines

    fun setShowGridLines(show: Boolean) {
        showGridLines = show
    }

    /**
     * @return Returns the ColumnHidden.
     */
    fun isColumnHidden(column: Int): Boolean {
        val columnInfoList = columnInfoList
        if (columnInfoList != null) {
            var columnInfo: ColumnInfo
            var index = 0
            while (index < columnInfoList.size) {
                columnInfo = columnInfoList[index++]
                if (columnInfo.getFirstCol() <= column && columnInfo.getLastCol() >= column) {
                    return columnInfo.isHidden()
                }
            }
        }

        return false
    }

    /**
     * @param isColumnHidden The ColumnHidden to set.
     */
    fun setColumnHidden(columnNumber: Int, isColumnHidden: Boolean) {
    }

    /**
     * ACTIVECELL_SINGLE
     * ACTIVECELL_ROW
     * ACTIVECELL_COLUMN;
     * @param type
     */
    fun setActiveCellType(type: Short) {
        this.activeCellType = type
    }

    fun getActiveCellType(): Short = activeCellType

    /**
     *
     */
    private fun checkActiveRowAndColumnBounds() {
        if (book!!.isBefore07Version()) {
            //03 and before version
            activeCellRow = Math.min(activeCellRow, Workbook.MAXROW_03 - 1)
            activeCellColumn = Math.min(activeCellColumn, Workbook.MAXCOLUMN_03 - 1)
        } else {
            //07,10 and later version
            activeCellRow = Math.min(activeCellRow, Workbook.MAXROW_07 - 1)
            activeCellColumn = Math.min(activeCellColumn, Workbook.MAXCOLUMN_07 - 1)
        }
    }

    fun setActiveCellRow(activeCellRow: Int) {
        this.activeCellRow = activeCellRow
        checkActiveRowAndColumnBounds()
    }

    /**
     * @return Returns the activeCellRow.
     */
    fun getActiveCellRow(): Int = activeCellRow

    fun setActiveCellColumn(activeCellColumn: Int) {
        this.activeCellColumn = activeCellColumn
        checkActiveRowAndColumnBounds()
    }

    /**
     * @return Returns the activeCellColumn.
     */
    fun getActiveCellColumn(): Int = activeCellColumn

    /**
     *
     */
    fun setActiveCellRowCol(row: Int, col: Int) {
        activeCellType = ACTIVECELL_SINGLE
        selectionEndRow = -1
        selectionEndColumn = -1
        activeCellRow = row
        activeCellColumn = col
        checkActiveRowAndColumnBounds()

        var cellRangeAddress: CellRangeAddress
        var index = 0
        while (index < merges!!.size) {
            cellRangeAddress = merges!![index++]
            if (cellRangeAddress.isInRange(row, col)) {
                activeCellRow = cellRangeAddress.getFirstRow()
                activeCellColumn = cellRangeAddress.getFirstColumn()
            }
        }

        if (getRow(row) != null) {
            activeCell = getRow(row)!!.getCell(col)
        } else {
            activeCell = null
        }
    }

    /**
     * @return Returns the activeCell.
     */
    fun getActiveCell(): Cell? = activeCell

    /**
     * @param activeCell The activeCell to set.
     */
    fun setActiveCell(activeCell: Cell?) {
        this.activeCell = activeCell
        if (activeCell != null) {
            activeCellRow = activeCell.getRowNumber()
            activeCellColumn = activeCell.getColNumber()
        } else {
            activeCellRow = -1
            activeCellColumn = -1
        }
    }

    /**
     * append shape of this sheet
     */
    fun appendShapes(shape: IShape) {
        this.shapesList!!.add(shape)
    }

    /** Takes [shape] off the sheet (a picture added in edit mode, undone). */
    fun removeShape(shape: IShape) {
        this.shapesList!!.remove(shape)
    }

    /** Puts [shape] back at [index] of the drawing order (a deleted picture, undone). */
    fun insertShape(index: Int, shape: IShape) {
        this.shapesList!!.add(index.coerceIn(0, shapesList!!.size), shape)
    }

    fun indexOfShape(shape: IShape): Int = shapesList!!.indexOf(shape)

    /** Pictures of the sheet's drawing by their cNvPr id (unique in the drawing): edit mode can move and delete them. */
    private val pictureIds = HashMap<IShape, Int>()

    /** The highest cNvPr id of the drawing: a new picture takes the next one. */
    var maxDrawingId = 1
        private set

    fun pictureId(shape: IShape): Int? = pictureIds[shape]

    fun setPictureId(shape: IShape, id: Int) {
        pictureIds[shape] = id
        noteDrawingId(id)
    }

    fun noteDrawingId(id: Int) {
        if (id > maxDrawingId) maxDrawingId = id
    }

    /**
     * get all shapes of this sheet
     */
    fun getShapes(): Array<IShape> {
        return shapesList!!.toTypedArray()
    }

    /**
     * get shape count of this sheet
     */
    fun getShapeCount(): Int {
        return shapesList!!.size
    }

    /**
     * get shape with index
     */
    fun getShape(index: Int): IShape? {
        if (index < 0 || index >= shapesList!!.size) {
            return null
        }
        return shapesList!![index]
    }

    /**
     *
     * @param defaultRowHeight
     */
    fun setDefaultRowHeight(defaultRowHeight: Int) {
        this.defaultRowHeight = defaultRowHeight
    }

    /**
     *
     * @return
     */
    fun getDefaultRowHeight(): Int = defaultRowHeight

    /**
     *
     * @param defaultColWidth
     */
    fun setDefaultColWidth(defaultColWidth: Int) {
        this.defaultColWidth = defaultColWidth
    }

    /**
     *
     * @return
     */
    fun getDefaultColWidth(): Int = defaultColWidth

    /**
     * TYPE_WORKSHEET
     * TYPE_CHARTSHEET
     * @param type
     */
    fun setSheetType(type: Short) {
        this.type = type
    }

    /**
     * TYPE_WORKSHEET
     * TYPE_CHARTSHEET
     * @return
     */
    fun getSheetType(): Short = type

    /**
     *
     * @param state
     */
    fun setState(state: Short) {
        this.state = state
        if (state == State_Accomplished && iReaderListener != null) {
            iReaderListener!!.OnReadingFinished()
        }

        maxScrollX = 0f
        maxScrollY = 0f
        var columnsCnt = 0
        if (columnInfoList != null) {
            val iter = columnInfoList!!.iterator()
            var info: ColumnInfo
            while (iter.hasNext()) {
                info = iter.next()
                columnsCnt += info.getLastCol() - info.getFirstCol() + 1
                if (info.isHidden()) {
                    continue
                }

                maxScrollX += info.getColWidth() * (info.getLastCol() - info.getFirstCol() + 1)
            }
        }

        val rowCnt = rows!!.size
        val iter = rows!!.values.iterator()
        while (iter.hasNext()) {
            maxScrollY += iter.next().getRowPixelHeight()
        }

        if (!book!!.isBefore07Version()) {
            //version after 2007
            maxScrollX += ((Workbook.MAXCOLUMN_07 - columnsCnt) * defaultColWidth).toFloat()
            maxScrollY += ((Workbook.MAXROW_07 - rowCnt) * defaultRowHeight).toFloat()
        } else {
            //version before 2007 (97/2000/XP/2003)
            maxScrollX += ((Workbook.MAXCOLUMN_03 - columnsCnt) * defaultColWidth).toFloat()
            maxScrollY += ((Workbook.MAXROW_03 - rowCnt) * defaultRowHeight).toFloat()
        }
    }

    fun notifyReadingProgress() {
        if (state != State_Accomplished) iReaderListener?.OnReadingProgress()
    }

    /**
     *
     * @return
     */
    @Synchronized
    fun getState(): Short {
        return state
    }

    /**
     *
     * @return
     */
    fun isAccomplished(): Boolean {
        return state == State_Accomplished
    }

    /**
     * send notifications to caller
     * @param iReaderListener
     */
    fun setReaderListener(iReaderListener: IReaderListener?) {
        this.iReaderListener = iReaderListener
    }

    /**
     *
     * @param root
     * @return root position
     */
    fun addSTRoot(root: STRoot?): Int {
        if (rootViewMap == null) {
            rootViewMap = ArrayList()
        }

        val id = rootViewMap!!.size
        rootViewMap!!.add(id, root)
        return id
    }

    /**
     *
     * @param id
     * @return
     */
    fun getSTRoot(id: Int): STRoot? {
        if (id < 0 || id >= rootViewMap!!.size) {
            return null
        }

        return rootViewMap!![id]
    }

    /**
     * called when changed sheet
     */
    fun removeSTRoot() {
        if (rootViewMap != null) {
            val cnt = rootViewMap!!.size
            var index = 0
            while (index < cnt) {
                val root = rootViewMap!![index++]
                if (root != null) {
                    root.dispose()
                }
            }
            rootViewMap!!.clear()
        }

        var rowIndex = firstRow
        while (rowIndex <= lastRow) {
            val row = getRow(rowIndex++)
            if (row == null || (row != null && row.isZeroHeight())) {
                continue
            }

            row.setInitExpandedRangeAddress(false)
            val iter = row.cellCollection().iterator()
            while (iter.hasNext()) {
                iter.next().removeSTRoot()
            }
        }
    }

    /**
     *
     * @param table
     */
    fun addTable(table: SSTable) {
        if (tableList == null) {
            tableList = ArrayList()
        }

        tableList!!.add(table)
    }

    /**
     *
     * @return
     */
    fun getTables(): Array<SSTable>? {
        if (tableList != null) {
            return tableList!!.toTypedArray()
        }

        return null
    }

    /**
     *
     */
    open fun dispose() {
        book = null
        sheetName = null
        paneInformation = null
        iReaderListener = null

        if (activeCell != null) {
            activeCell!!.dispose()
            activeCell = null
        }

        if (rows != null) {
            val rowCollection: Collection<Row> = rows!!.values
            for (row in rowCollection) {
                row.dispose()
            }
            rows!!.clear()
            rows = null
        }

        if (merges != null) {
            val iter = merges!!.iterator()
            while (iter.hasNext()) {
                (iter.next()).dispose()
            }
            merges!!.clear()
            merges = null
        }

        if (columnInfoList != null) {
            columnInfoList!!.clear()
            columnInfoList = null
        }

        if (shapesList != null) {
            val iter = shapesList!!.iterator()
            while (iter.hasNext()) {
                (iter.next()).dispose()
            }
            shapesList!!.clear()
            shapesList = null
        }

        if (rootViewMap != null) {
            removeSTRoot()
            rootViewMap = null
        }

        if (tableList != null) {
            tableList!!.clear()
            tableList = null
        }
    }

    companion object {
        /** Where deleted or unmerged merged ranges are parked (their list index must not change). */
        const val PARKED_ROW = 1048575
        const val PARKED_COLUMN = 16383
        /**
         * normal sheet
         */
        const val TYPE_WORKSHEET: Short = 0

        /**
         * chart sheet
         */
        const val TYPE_CHARTSHEET: Short = 1

        /**
         * Used for compile-time optimization.  This is the initial size for the collection of
         * rows.  It is currently set to 20.  If you generate larger sheets you may benefit
         * by setting this to a higher number and recompiling a custom edition of Sheet.
         */
        const val INITIAL_CAPACITY = 20

        //current active cell is single
        const val ACTIVECELL_SINGLE: Short = 0

        //current active cells are all cells of a row
        const val ACTIVECELL_ROW: Short = 1

        //current active cells are all cells of a column
        const val ACTIVECELL_COLUMN: Short = 2

        /**
         * not initialize
         */
        const val State_NotAccomplished: Short = 0

        /**
         *
         */
        const val State_Reading: Short = 1

        /**
         * initialized
         */
        const val State_Accomplished: Short = 2
    }
}
