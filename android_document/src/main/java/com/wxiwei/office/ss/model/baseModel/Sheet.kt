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
