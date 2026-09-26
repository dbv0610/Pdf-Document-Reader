package com.wxiwei.office.wp.view

import com.wxiwei.office.wp.model.CellElement

class BreakPagesCell(private val cell: CellElement, private val breakOffset: Long) {
    fun getCell(): CellElement = cell
    fun getBreakOffset(): Long = breakOffset
}
