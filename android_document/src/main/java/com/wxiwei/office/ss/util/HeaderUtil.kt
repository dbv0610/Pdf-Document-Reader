/*
 * 文件名称:          ColumnUtil.java
 *
 * 编译器:            android2.2
 * 时间:              上午9:02:37
 */
package com.wxiwei.office.ss.util

import com.wxiwei.office.ss.model.baseModel.Sheet

/**
 * TODO: 文件注释
 *
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2012-2-27
 * 负责人:           jqin
 */
class HeaderUtil {
    /**
     * 得到列标题显示文本
     */
    fun getColumnHeaderTextByIndex(index: Int): String {
        var index = index
        var result = ""
        while (index >= 0) {
            result = ('A' + index % 26).toString() + result
            index = index / 26 - 1
        }
        return result
    }

    fun getColumnHeaderIndexByText(text: String): Int {
        var index = 0
        for (i in 0 until text.length) {
            index = index * 26 + (text[i] - 'A') + 1
        }

        return index - 1
    }

    fun isActiveRow(sheet: Sheet, row: Int): Boolean {
        if (sheet.getActiveCellType() == Sheet.ACTIVECELL_COLUMN) {
            return true
        } else if (sheet.getActiveCellType() == Sheet.ACTIVECELL_ROW) {
            return sheet.getActiveCellRow() == row
        } else {
            var active = false
            val cell = sheet.getActiveCell()
            if (cell != null && cell.getRangeAddressIndex() >= 0) {
                val cr = sheet.getMergeRange(cell.getRangeAddressIndex())!!
                if (cr.getFirstRow() <= row && cr.getLastRow() >= row) {
                    active = true
                }
            } else if (sheet.getActiveCellRow() == row) {
                active = true
            }

            return active
        }
    }

    fun isActiveColumn(sheet: Sheet, col: Int): Boolean {
        if (sheet.getActiveCellType() == Sheet.ACTIVECELL_ROW) {
            return true
        } else if (sheet.getActiveCellType() == Sheet.ACTIVECELL_COLUMN) {
            return sheet.getActiveCellColumn() == col
        } else {
            var active = false
            val cell = sheet.getActiveCell()
            if (cell != null && cell.getRangeAddressIndex() >= 0) {
                val cr = sheet.getMergeRange(cell.getRangeAddressIndex())!!
                if (cr.getFirstColumn() <= col && cr.getLastColumn() >= col) {
                    active = true
                }
            } else if (sheet.getActiveCellColumn() == col) {
                active = true
            }

            return active
        }
    }

    companion object {
        //
        private val util = HeaderUtil()

        //
        @JvmStatic
        fun instance(): HeaderUtil {
            return util
        }
    }
}
