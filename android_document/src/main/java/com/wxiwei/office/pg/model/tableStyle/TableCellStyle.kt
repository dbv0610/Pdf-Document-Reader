package com.wxiwei.office.pg.model.tableStyle

import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.simpletext.model.IAttributeSet

class TableCellStyle {
    private var cellBorders: TableCellBorders? = null
    private var bgFill: Element? = null
    private var fontAttr: IAttributeSet? = null
    fun getTableCellBorders(): TableCellBorders? = cellBorders
    fun setTableCellBorders(value: TableCellBorders?) { cellBorders = value }
    fun getTableCellBgFill(): Element? = bgFill
    fun setTableCellBgFill(value: Element?) { bgFill = value }
    fun setFontAttributeSet(value: IAttributeSet?) { fontAttr = value }
    fun getFontAttributeSet(): IAttributeSet? = fontAttr
    fun dispose() { cellBorders = null; bgFill = null; fontAttr = null }
}
