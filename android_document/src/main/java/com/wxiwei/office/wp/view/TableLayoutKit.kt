/*
 * 文件名称:          TableLayoutKit.java
 *
 * 编译器:            android2.2
 * 时间:              上午11:26:53
 */
package com.wxiwei.office.wp.view

import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.constant.wp.WPAttrConstant
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.IDocument
import com.wxiwei.office.simpletext.model.IElement
import com.wxiwei.office.simpletext.view.DocAttr
import com.wxiwei.office.simpletext.view.IRoot
import com.wxiwei.office.simpletext.view.PageAttr
import com.wxiwei.office.simpletext.view.ParaAttr
import com.wxiwei.office.simpletext.view.TableAttr
import com.wxiwei.office.simpletext.view.ViewKit
import com.wxiwei.office.system.IControl
import com.wxiwei.office.wp.model.CellElement
import com.wxiwei.office.wp.model.RowElement
import com.wxiwei.office.wp.model.TableElement
import java.util.LinkedHashMap
import java.util.Vector

/**
 * 表格布局算法
 */
class TableLayoutKit {

    private var isRowBreakPages = false

    //
    private var rowIndex = 0

    // 跨页的row element
    private var breakRowElement: RowElement? = null

    //
    private var breakRowView: RowView? = null

    // 跨页的 cell element
    private val breakPagesCell: MutableMap<Int, BreakPagesCell> = LinkedHashMap()

    //
    private val tableAttr = TableAttr()

    //
    private val mergedCell = Vector<CellView>()

    /**
     * 布局表格
     */
    fun layoutTable(control: IControl, doc: IDocument, root: IRoot, docAttr: DocAttr?, pageAttr: PageAttr?, paraAttr: ParaAttr,
                    tableView: TableView, startOffset: Long, x: Int, y: Int, w: Int, h: Int, flag: Int, isBreakPages: Boolean): Int {
        var startOffset = startOffset
        var flag = flag
        var isBreakPages = isBreakPages
        mergedCell.clear()

        var span = h
        val dx = 0
        var dy = 0
        var breakType = WPViewConstant.BREAK_NO.toInt()
        val tableElem = tableView.getElement() as TableElement
        AttrManage.instance().fillTableAttr(tableAttr, tableElem.getAttribute())
        flag = ViewKit.instance().setBitValue(flag, WPViewConstant.LAYOUT_PARA_IN_TABLE.toInt(), true)
        var keepOne = ViewKit.instance().getBitValue(flag, WPViewConstant.LAYOUT_FLAG_KEEPONE.toInt())
        val maxEnd = tableElem.getEndOffset()
        var rowHeight: Int
        var tableHeight = 0
        var tableWidth = 0
        var rowView: RowView? = null
        while (startOffset < maxEnd && span > 0
            || (breakRowElement != null && isBreakPages)
        ) {
            isRowBreakPages = false
            var rowElem: IElement?
            if (breakRowElement != null && isBreakPages) {
                rowElem = breakRowElement
                breakRowElement = null
            } else {
                rowElem = tableElem.getElementForIndex(rowIndex++)
            }
            if (rowElem == null) {
                break
            }
            if (rowView != null) {
                layoutMergedCell(rowView, rowElem as RowElement, false)
                dy = rowView.getY() + rowView.getLayoutSpan(WPViewConstant.Y_AXIS)
                tableHeight = dy
                span = h - tableHeight
                if (span <= 0) {
                    if (isBreakPages(rowView)) {
                        rowIndex--
                        breakRowElement = rowView.getElement() as RowElement
                    } else {
                        breakRowElement = rowElem
                    }

                    break
                }
            }
            rowView = ViewFactory.createView(control, rowElem, null, WPViewConstant.TABLE_ROW_VIEW.toInt()) as RowView
            tableView.appendChlidView(rowView)
            rowView.setStartOffset(startOffset)
            rowView.setLocation(dx, dy)

            // layout row
            breakType = layoutRow(control, doc, root, docAttr, pageAttr, paraAttr, rowView, startOffset, dx, dy, w, span, flag, isBreakPages)

            rowHeight = rowView.getLayoutSpan(WPViewConstant.Y_AXIS)
            if ((rowHeight == 0 || span - rowHeight < 0) && !keepOne) {
                val preView = rowView.getPreView() as RowView?
                if (preView != null && isBreakPages(preView)) {
                    rowIndex--
                    breakRowElement = preView.getElement() as RowElement
                    // 清除当行中跨行cellElem
                    clearCurrentRowBreakPageCell(rowElem)
                } else {
                    breakRowElement = rowView.getElement() as RowElement
                    // 清除当行中跨行cellElem
                    clearCurrentRowBreakPageCell(rowElem)
                }
                tableView.deleteView(rowView, true)
                rowView = preView
                breakType = WPViewConstant.BREAK_LIMIT.toInt()
                break
            } else if (breakType == WPViewConstant.BREAK_LIMIT.toInt() && breakPagesCell.size > 0
                || isRowBreakPages
            ) {
                breakRowElement = rowView.getElement() as RowElement
            }
            tableWidth = Math.max(tableWidth, rowView.getLayoutSpan(WPViewConstant.X_AXIS))
            dy += rowHeight
            tableHeight += rowHeight
            startOffset = rowView.getEndOffset(null)
            isBreakPages = false
            keepOne = false
        }
        layoutMergedCell(rowView, null, true)
        ///
        tableView.setEndOffset(startOffset)
        //
        tableView.setSize(tableWidth, tableHeight)
        //
        if (docAttr!!.rootType.toInt() == WPViewConstant.PAGE_ROOT.toInt()) {
            // table horizontal alignment
            val hor = AttrManage.instance().getParaHorizontalAlign(tableElem.getAttribute()).toByte()
            var want = w - tableWidth
            if (hor == WPAttrConstant.PARA_HOR_ALIGN_CENTER ||
                hor == WPAttrConstant.PARA_HOR_ALIGN_RIGHT
            ) {
                if (hor == WPAttrConstant.PARA_HOR_ALIGN_CENTER) {
                    want /= 2
                }
                tableView.setX(tableView.getX() + want)
            } else {
                tableView.setX(tableView.getX() - tableAttr.leftMargin
                        + (AttrManage.instance().getParaIndentLeft(tableElem.getAttribute()) * MainConstant.TWIPS_TO_PIXEL).toInt())
            }
        }
        breakRowView = rowView
        return breakType
    }

    private fun clearCurrentRowBreakPageCell(currentElem: IElement) {
        val keys = Vector<Int>()
        for (key in breakPagesCell.keys) {
            val bc = breakPagesCell[key]
            if (bc!!.getCell().getStartOffset() >= currentElem.getStartOffset()
                && bc.getCell().getEndOffset() <= currentElem.getEndOffset()
            ) {
                keys.add(key)
            }
        }
        for (key in keys) {
            breakPagesCell.remove(key)
        }
    }

    /**
     * 布局表格行
     */
    fun layoutRow(control: IControl, doc: IDocument, root: IRoot, docAttr: DocAttr?, pageAttr: PageAttr?, paraAttr: ParaAttr,
                  rowView: RowView, startOffset: Long, x: Int, y: Int, w: Int, h: Int, flag: Int, isBreakPages: Boolean): Int {
        var startOffset = startOffset
        var w = w
        var dx = 0
        val dy = 0
        var breakType = WPViewConstant.BREAK_NO.toInt()

        val rowElem = rowView.getElement() as RowElement
        val maxEnd = rowElem.getEndOffset()
        val rowHeight = (AttrManage.instance().getTableRowHeight(rowElem.getAttribute()) * MainConstant.TWIPS_TO_PIXEL).toInt()
        var rowWidth = 0
        var cellWidth: Int
        var cellHeight: Int
        var maxCellHeight = 0
        var maxRowHeight = rowHeight
        var cellIndex = 0
        var isNullCell: Boolean
        var isInvalid = true

        while (cellIndex < rowElem.getCellNumber()) {
            var cellElem: IElement?
            isNullCell = false
            // 表格跨页
            if (isBreakPages && breakPagesCell.size > 0) {
                if (breakPagesCell.containsKey(cellIndex)) {
                    val breakCell = breakPagesCell.remove(cellIndex)
                    cellElem = breakCell!!.getCell()
                    startOffset = breakCell.getBreakOffset()
                } else {
                    cellElem = rowElem.getElementForIndex(cellIndex)
                    isNullCell = true
                }
            } else {
                cellElem = rowElem.getElementForIndex(cellIndex)
                if (cellElem == null) {
                    break
                }
                startOffset = cellElem.getStartOffset()
                isNullCell = startOffset == cellElem.getEndOffset()
                if (!isNullCell && breakRowView != null && isBreakPages) {
                    val temp = breakRowView!!.getCellView(cellIndex.toShort())
                    if (temp != null) {
                        isNullCell = temp.getEndOffset(null) == cellElem.getEndOffset()
                    }
                }
            }

            val cellView = ViewFactory.createView(control, cellElem, null, WPViewConstant.TABLE_CELL_VIEW.toInt()) as CellView
            rowView.appendChlidView(cellView)
            cellView.setStartOffset(startOffset)
            cellView.setLocation(dx, dy)
            cellView.setColumn(cellIndex.toShort())

            // cell跨页，但又没有补切到下一页
            if (isNullCell) {
                cellView.setFirstMergedCell(isBreakPages)
                breakType = layoutCellForNull(doc, root, docAttr, pageAttr, paraAttr, cellView, startOffset, dx, dy, w, h, flag, cellIndex, isBreakPages)
            } else {
                // first mergeCell
                cellView.setFirstMergedCell(isBreakPages || AttrManage.instance().isTableVerFirstMerged(cellElem!!.getAttribute()))
                cellView.setMergedCell(AttrManage.instance().isTableVerMerged(cellElem!!.getAttribute()))
                //
                breakType = layoutCell(control, doc, root, docAttr, pageAttr, paraAttr, cellView, startOffset, dx, dy, w, h, flag, cellIndex, isBreakPages)
            }
            //
            cellWidth = cellView.getLayoutSpan(WPViewConstant.X_AXIS)
            cellHeight = cellView.getLayoutSpan(WPViewConstant.Y_AXIS)
            isInvalid = isInvalid && cellHeight == 0
            dx += cellWidth
            rowWidth += cellWidth
            w -= cellWidth
            if (!cellView.isMergedCell()) {
                maxRowHeight = Math.max(maxRowHeight, cellHeight)
            }
            if (cellView.isFirstMergedCell()) {
                mergedCell.add(cellView)
            }
            //
            maxCellHeight = Math.max(maxCellHeight, cellHeight)
            //
            startOffset = cellView.getEndOffset(null)
            cellIndex++
        }
        //
        var cellView = rowView.getChildView() as CellView?
        while (cellView != null) {
            if (!cellView.isMergedCell()
                && cellView.getLayoutSpan(WPViewConstant.Y_AXIS) < maxRowHeight
            ) {
                cellView.setHeight(maxRowHeight - cellView.getTopIndent() - cellView.getBottomIndent())
                //
                val cellElem = cellView.getElement() as CellElement?
                if (cellElem != null) {
                    tableAttr.cellVerticalAlign = AttrManage.instance().getTableCellVerAlign(cellElem.getAttribute()).toByte()
                }

                layoutCellVerticalAlign(cellView)
            }
            cellView = cellView.getNextView() as CellView?
        }
        rowView.setEndOffset(maxEnd)
        if (isInvalid) {
            maxRowHeight = Integer.MAX_VALUE
        }
        rowView.setSize(rowWidth, maxRowHeight)
        breakRowView = null
        return breakType
    }

    /**
     * 布局单元格
     */
    fun layoutCell(control: IControl, doc: IDocument, root: IRoot, docAttr: DocAttr?, pageAttr: PageAttr?, paraAttr: ParaAttr,
                   cellView: CellView, startOffset: Long, x: Int, y: Int, w: Int, h: Int, flag: Int, cellIndex: Int, isBreakPages: Boolean): Int {
        var startOffset = startOffset
        var flag = flag
        val cellElem = cellView.getElement() as CellElement
        AttrManage.instance().fillTableAttr(tableAttr, cellElem.getAttribute())
        cellView.setBackground(tableAttr.cellBackground)

        cellView.setIndent(tableAttr.leftMargin, tableAttr.topMargin, tableAttr.rightMargin, tableAttr.bottomMargin)
        var dx = tableAttr.leftMargin
        var dy = tableAttr.topMargin

        var breakType = WPViewConstant.BREAK_NO.toInt()

        val maxEnd = cellElem.getEndOffset()
        var cellHeight = 0
        var spanH = h - tableAttr.topMargin - tableAttr.bottomMargin
        val cellWidth = tableAttr.cellWidth - tableAttr.leftMargin - tableAttr.rightMargin

        while (startOffset < maxEnd && spanH > 0 && breakType != WPViewConstant.BREAK_LIMIT.toInt()) {
            val paraElem = doc.getParagraph(startOffset)
            val paraView = ViewFactory.createView(control, paraElem, null, WPViewConstant.PARAGRAPH_VIEW.toInt()) as ParagraphView
            cellView.appendChlidView(paraView)
            paraView.setStartOffset(startOffset)
            paraView.setLocation(dx, dy)
            //
            AttrManage.instance().fillParaAttr(cellView.getControl(), paraAttr, paraElem!!.getAttribute())
            breakType = LayoutKit.instance().layoutPara(control, doc, docAttr!!, pageAttr!!, paraAttr, paraView,
                startOffset, dx, dy, cellWidth, spanH, flag)
            //
            val paraHeight = paraView.getLayoutSpan(WPViewConstant.Y_AXIS)
            if (paraView.getChildView() == null) {
                cellView.deleteView(paraView, true)
                break
            }
            if (root.getViewContainer() != null) {
                root.getViewContainer()!!.add(paraView)
            }

            dy += paraHeight
            cellHeight += paraHeight
            spanH -= paraHeight

            startOffset = paraView.getEndOffset(null)
            paraView.setEndOffset(startOffset)
            flag = ViewKit.instance().setBitValue(flag, WPViewConstant.LAYOUT_FLAG_KEEPONE.toInt(), false)
        }
        if (startOffset < maxEnd) {
            if (!breakPagesCell.containsKey(cellIndex) && cellWidth > 0) {
                breakPagesCell.put(cellIndex, BreakPagesCell(cellElem, startOffset))
                isRowBreakPages = true
            }
        }
        cellView.setEndOffset(startOffset)
        cellView.setSize(cellWidth, cellHeight)
        return breakType
    }

    /**
     * 布局空单元格
     */
    fun layoutCellForNull(doc: IDocument, root: IRoot, docAttr: DocAttr?, pageAttr: PageAttr?, paraAttr: ParaAttr,
                          cellView: CellView, startOffset: Long, x: Int, y: Int, w: Int, h: Int, flag: Int, cellIndex: Int, isBreakPages: Boolean): Int {
        val cellElem = cellView.getElement() as CellElement
        AttrManage.instance().fillTableAttr(tableAttr, cellElem.getAttribute())
        cellView.setIndent(tableAttr.leftMargin, tableAttr.topMargin, tableAttr.rightMargin, tableAttr.bottomMargin)
        val cellHeight = 0
        val cellWidth = tableAttr.cellWidth - tableAttr.leftMargin - tableAttr.rightMargin
        cellView.setSize(cellWidth, cellHeight)
        return WPViewConstant.BREAK_NO.toInt()
    }

    private fun layoutMergedCell(row: RowView?, nextRowElem: RowElement?, isLastRow: Boolean) {
        if (row == null) {
            return
        }
        var maxY = row.getY() + row.getLayoutSpan(WPViewConstant.Y_AXIS)
        if (isLastRow) {
            for (cell in mergedCell) {
                if (cell.getParentView() != null) {
                    cell.setHeight(maxY - cell.getParentView()!!.getY())
                    //
                    layoutCellVerticalAlign(cell)
                }
            }
            mergedCell.clear()
            return
        }
        for (cell in mergedCell) {
            maxY = Math.max(maxY, cell.getParentView()!!.getY() + cell.getLayoutSpan(WPViewConstant.Y_AXIS))
        }
        val vector = Vector<CellView>()
        for (cell in mergedCell) {
            val cellElem = nextRowElem!!.getElementForIndex(cell.getColumn().toInt())
            if (cellElem == null) {
                continue
            }
            // 如果下一个单元格式不是合并单元格
            if (!AttrManage.instance().isTableVerMerged(cellElem.getAttribute())
                || AttrManage.instance().isTableVerFirstMerged(cellElem.getAttribute())
            ) {
                val cellHeight = cell.getLayoutSpan(WPViewConstant.Y_AXIS)
                if (cell.getParentView()!!.getY() + cellHeight < maxY) {
                    cell.setHeight(maxY - cell.getParentView()!!.getY())
                    //
                    layoutCellVerticalAlign(cell)
                } else {
                    row.setHeight(maxY - row.getY())
                    var cellView = row.getChildView() as CellView?
                    while (cellView != null) {
                        if (!cellView.isMergedCell()) {
                            val oldHeight = cellView.getHeight()
                            cellView.setHeight(maxY - cellView.getParentView()!!.getY())
                            //
                            if (oldHeight != cellView.getHeight()) {
                                layoutCellVerticalAlign(cellView)
                            }
                        }
                        cellView = cellView.getNextView() as CellView?
                    }
                }
                vector.add(cell)
            }
        }
        //
        for (cell in vector) {
            maxY = cell.getParentView()!!.getY() + cell.getLayoutSpan(WPViewConstant.Y_AXIS)
            if (maxY > row.getY() + row.getLayoutSpan(WPViewConstant.Y_AXIS)) {
                cell.setHeight(row.getY() + row.getLayoutSpan(WPViewConstant.Y_AXIS) - cell.getY())
            }
            mergedCell.remove(cell)
        }
    }

    /**
     * 判断给行是否跨行
     */
    private fun isBreakPages(rowView: RowView): Boolean {
        var view = rowView.getChildView()
        while (view != null) {
            val elem = view.getElement()
            if (view.getEndOffset(null) != elem!!.getEndOffset()
                && view.getWidth() > 0
            ) {
                return true
            }
            view = view.getNextView()
        }
        return false
    }

    private fun layoutCellVerticalAlign(cellView: CellView) {
        if (tableAttr.cellVerticalAlign == WPAttrConstant.PARA_VER_ALIGN_TOP) {
            return
        }
        var textHeight = 0
        var para = cellView.getChildView()
        while (para != null) {
            textHeight += para.getLayoutSpan(WPViewConstant.Y_AXIS)
            para = para.getNextView()
        }
        var want = cellView.getLayoutSpan(WPViewConstant.Y_AXIS) - textHeight
        val verAlignmnet = AttrManage.instance().getTableCellVerAlign(cellView.getElement()!!.getAttribute())
        // vertical center alignment
        if (verAlignmnet == WPAttrConstant.PARA_VER_ALIGN_CENTER.toInt()
            || verAlignmnet == WPAttrConstant.PARA_VER_ALIGN_BOTTOM.toInt()
        ) {
            if (verAlignmnet == WPAttrConstant.PARA_VER_ALIGN_CENTER.toInt()) {
                want /= 2
            }
            para = cellView.getChildView()
            while (para != null) {
                para.setY(para.getY() + want)
                para = para.getNextView()
            }
        }
    }

    /**
     * 表格是否跨页
     */
    fun isTableBreakPages(): Boolean {
        return breakPagesCell.size > 0
                || breakRowElement != null
    }

    fun clearBreakPages() {
        rowIndex = 0
        breakRowElement = null
        breakPagesCell.clear()
        breakRowView = null
    }

    fun dispose() {
        breakRowElement = null
        breakPagesCell.clear()
        breakRowView = null
    }
}
