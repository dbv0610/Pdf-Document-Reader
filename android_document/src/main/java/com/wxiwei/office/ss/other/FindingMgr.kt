/*
 * 文件名称:          SearchCell.java
 *
 * 编译器:            android2.2
 * 时间:              下午7:08:32
 */
package com.wxiwei.office.ss.other

import com.wxiwei.office.ss.model.baseModel.Cell
import com.wxiwei.office.ss.model.baseModel.Row
import com.wxiwei.office.ss.model.baseModel.Sheet
import com.wxiwei.office.ss.util.ModelUtil
import com.wxiwei.office.system.search.SearchText

/**
 * TODO: find data which you're searching for
 *
 * Read版本:        Read V1.0
 *
 * 作者:            jqin
 *
 * 日期:            2012-3-12
 *
 * 负责人:           jqin
 *
 * 负责小组:
 */
class FindingMgr {
    private var sheet: Sheet? = null

    //finding value
    private var value: String? = null

    //finded cell
    private var findedCell: Cell? = null

    private fun isVisible(sheet: Sheet, row: Row, cell: Cell): Boolean =
        !row.isZeroHeight() && !sheet.isColumnHidden(cell.getColNumber())

    /**
     * find cells which contain interesting contents
     * @param value
     * @return
     */
    fun findCell(sheet: Sheet?, value: String?): Cell? {
        if (value == null || sheet == null) {
            return null
        }
        this.sheet = sheet
        this.value = value

        var cellContent: String?
        if (value != null && value.length > 0) {
            var row: Row?
            //search from current active cell
            var i = sheet.getActiveCellRow()
            while (i <= sheet.getLastRowNum()) {
                row = sheet.getRow(i)
                if (row == null) {
                    i++
                    continue
                }

                var j = if (i == sheet.getActiveCellRow()) sheet.getActiveCellColumn() else row.getFirstCol()
                while (j <= row.getLastCol()) {
                    findedCell = row.getCell(j)
                    if (findedCell == null || !isVisible(sheet, row, findedCell!!)) {
                        j++
                        continue
                    }

                    cellContent = ModelUtil.instance().getFormatContents(sheet.getWorkbook()!!, findedCell!!)
                    if (cellContent != null && cellContent.contains(value, ignoreCase = true)) {
                        return findedCell
                    }
                    j++
                }
                i++
            }

            //reached to the end of document, then search from the begin of the document
            i = sheet.getFirstRowNum()
            while (i <= sheet.getActiveCellRow()) {
                row = sheet.getRow(i)
                if (row == null) {
                    i++
                    continue
                }

                var j = row.getFirstCol()
                while (j <= row.getLastCol()) {
                    findedCell = row.getCell(j)
                    if (findedCell == null || !isVisible(sheet, row, findedCell!!)) {
                        j++
                        continue
                    }

                    cellContent = ModelUtil.instance().getFormatContents(sheet.getWorkbook()!!, findedCell!!)
                    if (cellContent != null && cellContent.contains(value, ignoreCase = true)) {
                        return findedCell
                    }
                    j++
                }
                i++
            }
        }

        return null
    }

    fun findBackward(): Cell? {
        val sheet = this.sheet
        val value = this.value
        if (findedCell == null || value == null || sheet == null) {
            return null
        }

        var cellContent: String?
        var row: Row?
        var cell: Cell?
        var i = findedCell!!.getRowNumber()
        while (i >= sheet.getFirstRowNum()) {
            row = sheet.getRow(i)
            if (row == null) {
                i--
                continue
            }

            var j = if (i == findedCell!!.getRowNumber()) findedCell!!.getColNumber() - 1 else row.getLastCol()

            while (j >= 0) {
                cell = row.getCell(j)
                if (cell == null || !isVisible(sheet, row, cell)) {
                    j--
                    continue
                }

                cellContent = ModelUtil.instance().getFormatContents(sheet.getWorkbook()!!, cell)
                if (cellContent != null && cellContent.contains(value, ignoreCase = true)) {
                    findedCell = cell
                    return findedCell
                }
                j--
            }
            i--
        }

        return null
    }

    fun findForward(): Cell? {
        val sheet = this.sheet
        val value = this.value
        if (findedCell == null || value == null || sheet == null) {
            return null
        }

        var cellContent: String?
        var row: Row?
        var cell: Cell?
        var i = findedCell!!.getRowNumber()
        while (i <= sheet.getLastRowNum()) {
            row = sheet.getRow(i)
            if (row == null) {
                i++
                continue
            }

            var j = if (i == findedCell!!.getRowNumber()) findedCell!!.getColNumber() + 1 else row.getFirstCol()

            while (j <= row.getLastCol()) {
                cell = row.getCell(j)
                if (cell == null || !isVisible(sheet, row, cell)) {
                    j++
                    continue
                }

                cellContent = ModelUtil.instance().getFormatContents(sheet.getWorkbook()!!, cell)
                if (cellContent != null && cellContent.contains(value, ignoreCase = true)) {
                    findedCell = cell
                    return findedCell
                }
                j++
            }
            i++
        }

        return null
    }

    fun findAll(sheet: Sheet?, value: String?): MutableList<Cell> {
        val results = findAll(sheet, SearchText.queries(value))
        this.sheet = sheet
        this.value = value
        findedCell = results.firstOrNull()
        return results
    }

    fun findAll(sheet: Sheet?, queries: List<String>, isActive: () -> Boolean = { true }): MutableList<Cell> {
        val results: MutableList<Cell> = ArrayList()
        if (sheet == null || queries.isEmpty()) {
            return results
        }

        val firstRow = sheet.getFirstRowNum()
        val lastRow = sheet.getLastRowNum()
        for (r in firstRow..lastRow) {
            if (!isActive()) return results
            val row = sheet.getRow(r) ?: continue

            val firstCol = row.getFirstCol()
            val lastCol = row.getLastCol()
            for (c in firstCol..lastCol) {
                if (!isActive()) return results
                val cell = row.getCell(c) ?: continue
                if (!isVisible(sheet, row, cell)) continue

                val text = ModelUtil.instance()
                    .getFormatContents(sheet.getWorkbook()!!, cell)
                if (text != null && SearchText.contains(text, queries)) {
                    results.add(cell)
                }
            }
        }
        return results
    }

    /**
     *
     */
    fun dispose() {
        sheet = null
        value = null
        findedCell = null
    }
}
