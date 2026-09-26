package com.wxiwei.office.pg.model.tableStyle

import com.wxiwei.office.fc.dom4j.Element

class TableCellBorders {
    private var left: Element? = null
    private var top: Element? = null
    private var right: Element? = null
    private var bottom: Element? = null

    fun getLeftBorder(): Element? = left
    fun setLeftBorder(left: Element?) { this.left = left }
    fun getTopBorder(): Element? = top
    fun setTopBorder(top: Element?) { this.top = top }
    fun getRightBorder(): Element? = right
    fun setRightBorder(right: Element?) { this.right = right }
    fun getBottomBorder(): Element? = bottom
    fun setBottomBorder(bottom: Element?) { this.bottom = bottom }
}
