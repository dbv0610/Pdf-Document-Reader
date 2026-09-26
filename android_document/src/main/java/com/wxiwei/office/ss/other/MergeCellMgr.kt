/*
 * 文件名称:          MergedCellMgr.java
 *
 * 编译器:            android2.2
 * 时间:              上午10:51:22
 */
package com.wxiwei.office.ss.other

import com.wxiwei.office.ss.model.CellRangeAddress
import com.wxiwei.office.ss.model.baseModel.Sheet
import com.wxiwei.office.ss.view.SheetView

/**
 * TODO: 文件注释
 *
 * Read版本:        Read V1.0
 *
 * 作者:            jqin
 *
 * 日期:            2012-8-20
 *
 * 负责人:           jqin
 *
 * 负责小组:
 */
class MergeCellMgr {
    //
    private var cellRangeAddress: CellRangeAddress? = CellRangeAddress(0, 0, 0, 0)
    private var mergedCell: MergeCell? = MergeCell()

    /**
     *
     * @param sheet
     * @param cr
     * @return
     */
    fun getVisibleCellRangeAddress(sheet: Sheet, cr: CellRangeAddress): CellRangeAddress? {
        var firstCol = cr.getFirstColumn()
        var lastCol = cr.getLastColumn()
        var firstRow = cr.getFirstRow()
        var lastRow = cr.getLastRow()

        //adjust visible merged cell range address
        while (firstCol <= lastCol && sheet.isColumnHidden(firstCol)) {
            firstCol++
        }

        while (lastCol >= firstCol && sheet.isColumnHidden(lastCol)) {
            lastCol--
        }

        while (firstRow <= lastRow && sheet.getRow(firstRow)!!.isZeroHeight()) {
            firstRow++
        }
        while (lastRow >= firstRow && sheet.getRow(lastRow)!!.isZeroHeight()) {
            lastRow--
        }

        cellRangeAddress!!.setFirstColumn(firstCol)
        cellRangeAddress!!.setFirstRow(firstRow)
        cellRangeAddress!!.setLastColumn(lastCol)
        cellRangeAddress!!.setLastRow(lastRow)

        return cellRangeAddress
    }

    /**
     *
     * @param cr
     * @param row
     * @param col
     * @return true:draw current cell borders  false:not draw
     */
    fun isDrawMergeCell(sheetView: SheetView, cr: CellRangeAddress, row: Int, col: Int): Boolean {
        var isDraw = false
        var minColumn = sheetView.getMinRowAndColumnInformation()!!.getMinColumnIndex()
        var minRow = sheetView.getMinRowAndColumnInformation()!!.getMinRowIndex()
        val paneInfo = sheetView.getCurrentSheet()!!.getPaneInformation()
        //except hiden row and column
        getVisibleCellRangeAddress(sheetView.getCurrentSheet()!!, cr)
        val cellRangeAddress = this.cellRangeAddress!!

        if (paneInfo != null) {
            if (row < paneInfo.getHorizontalSplitTopRow() && cr.getLastRow() >= paneInfo.getHorizontalSplitTopRow()) {
                cellRangeAddress.setLastRow(paneInfo.getHorizontalSplitTopRow() - 1)
                minRow = 0
            } else if (row >= paneInfo.getHorizontalSplitTopRow() && cr.getFirstRow() < paneInfo.getHorizontalSplitTopRow()) {
                cellRangeAddress.setFirstRow(paneInfo.getHorizontalSplitTopRow().toInt())
            }

            if (col < paneInfo.getVerticalSplitLeftColumn() && cr.getLastColumn() >= paneInfo.getVerticalSplitLeftColumn()) {
                cellRangeAddress.setLastColumn(paneInfo.getVerticalSplitLeftColumn() - 1)
                minColumn = 0
            } else if (col >= paneInfo.getVerticalSplitLeftColumn() && cr.getFirstColumn() < paneInfo.getVerticalSplitLeftColumn()) {
                cellRangeAddress.setFirstColumn(paneInfo.getVerticalSplitLeftColumn().toInt())
            }
        }

        // 首行首列，必须绘制
        if (cellRangeAddress.getFirstColumn() == col && cellRangeAddress.getFirstRow() == row) {
            isDraw = true
        }
        // 首行且非首列，只当列号==显示的最小列号才需要绘制
        else if (row == cellRangeAddress.getFirstRow() && col > cellRangeAddress.getFirstColumn()) {
            isDraw = col == minColumn
        }
        // 首列且非首行，只当行号==显示的最小行号才需要绘制
        else if (col == cellRangeAddress.getFirstColumn() && row > cellRangeAddress.getFirstRow()) {
            isDraw = row == minRow
        }
        // 非首行首列，只当列号==显示的最小列号 and 行号==最小行号才需要绘制
        else if (row > cellRangeAddress.getFirstRow() && col > cellRangeAddress.getFirstColumn()) {
            isDraw = col == minColumn && row == minRow
        }
        return isDraw
    }

    /**
     *
     * @param sheetView
     * @param cellRangeAddress
     * @return
     */
    fun getMergedCellSize(sheetView: SheetView, cellRangeAddress: CellRangeAddress, row: Int, col: Int): MergeCell? {
        val mergedCell = this.mergedCell!!
        //MergedCellSize mergedCell = new MergedCellSize();
        mergedCell.reset()

        val minColumn = sheetView.getMinRowAndColumnInformation()!!.getMinColumnIndex()
        val minRow = sheetView.getMinRowAndColumnInformation()!!.getMinRowIndex()
        val paneInfo = sheetView.getCurrentSheet()!!.getPaneInformation()

        if (paneInfo == null) {
            for (i in cellRangeAddress.getFirstColumn()..cellRangeAddress.getLastColumn()) {
                if (!sheetView.getCurrentSheet()!!.isColumnHidden(i)) {
                    val tW = (sheetView.getCurrentSheet()!!.getColumnPixelWidth(i) * sheetView.getZoom())
                    mergedCell.setWidth(mergedCell.getWidth() + tW)
                    if (i < minColumn) {
                        mergedCell.setNovisibleWidth(mergedCell.getNovisibleWidth() + tW)
                    }
                }
            }
            for (i in cellRangeAddress.getFirstRow()..cellRangeAddress.getLastRow()) {
                if (!sheetView.getCurrentSheet()!!.getRow(i)!!.isZeroHeight()) {
                    val tH = (sheetView.getCurrentSheet()!!.getRow(i)!!.getRowPixelHeight() * sheetView.getZoom())
                    mergedCell.setHeight(mergedCell.getHeight() + tH)
                    if (i < minRow) {
                        mergedCell.setNoVisibleHeight(mergedCell.getNoVisibleHeight() + tH)
                    }
                }
            }
        } else {
            ///merged cell width
            if (col >= paneInfo.getVerticalSplitLeftColumn()) {
                //free columns
                for (i in cellRangeAddress.getFirstColumn()..cellRangeAddress.getLastColumn()) {
                    if (!sheetView.getCurrentSheet()!!.isColumnHidden(i)) {
                        val tW = (sheetView.getCurrentSheet()!!.getColumnPixelWidth(i) * sheetView.getZoom())
                        mergedCell.setWidth(mergedCell.getWidth() + tW)
                        if (i < minColumn) {
                            mergedCell.setNovisibleWidth(mergedCell.getNovisibleWidth() + tW)
                        }
                    }
                }
            } else {
                //frozen columns
                mergedCell.setFrozenColumn(true)
                for (i in cellRangeAddress.getFirstColumn()..cellRangeAddress.getLastColumn()) {
                    if (!sheetView.getCurrentSheet()!!.isColumnHidden(i)) {
                        val tW = (sheetView.getCurrentSheet()!!.getColumnPixelWidth(i) * sheetView.getZoom())
                        mergedCell.setWidth(mergedCell.getWidth() + tW)
                        if (i >= paneInfo.getVerticalSplitLeftColumn()) {
                            mergedCell.setNovisibleWidth(mergedCell.getNovisibleWidth() + tW)
                        }
                    }
                }
            }

            //////merged cell height
            if (row >= paneInfo.getHorizontalSplitTopRow()) {
                //free rows
                for (i in cellRangeAddress.getFirstRow()..cellRangeAddress.getLastRow()) {
                    if (!sheetView.getCurrentSheet()!!.getRow(i)!!.isZeroHeight()) {
                        val tH = (sheetView.getCurrentSheet()!!.getRow(i)!!.getRowPixelHeight() * sheetView.getZoom())
                        mergedCell.setHeight(mergedCell.getHeight() + tH)
                        if (i < minRow) {
                            mergedCell.setNoVisibleHeight(mergedCell.getNoVisibleHeight() + tH)
                        }
                    }
                }
            } else {
                //frozen rows
                mergedCell.setFrozenRow(true)
                for (i in cellRangeAddress.getFirstRow()..cellRangeAddress.getLastRow()) {
                    if (!sheetView.getCurrentSheet()!!.getRow(i)!!.isZeroHeight()) {
                        val tH = (sheetView.getCurrentSheet()!!.getRow(i)!!.getRowPixelHeight() * sheetView.getZoom())
                        mergedCell.setHeight(mergedCell.getHeight() + tH)
                        if (i >= paneInfo.getHorizontalSplitTopRow()) {
                            mergedCell.setNoVisibleHeight(mergedCell.getNoVisibleHeight() + tH)
                        }
                    }
                }
            }
        }

        return mergedCell
    }

    fun dispose() {
        if (cellRangeAddress != null) {
            cellRangeAddress!!.dispose()
            cellRangeAddress = null
        }

        if (mergedCell != null) {
            mergedCell!!.dispose()
            mergedCell = null
        }
    }
}
