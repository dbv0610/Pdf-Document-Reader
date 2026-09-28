/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
package com.wxiwei.office.wp.model

import com.wxiwei.office.constant.wp.WPModelConstant
import com.wxiwei.office.simpletext.model.AbstractElement
import com.wxiwei.office.simpletext.model.ElementCollectionImpl
import com.wxiwei.office.simpletext.model.IElement

class RowElement : AbstractElement() {
    private val cellElement = ElementCollectionImpl(10)

    override fun getType(): Short = WPModelConstant.TABLE_ROW_ELEMENT

    fun appendCell(cellElem: CellElement) {
        cellElement.addElement(cellElem)
    }

    fun getCellElement(offset: Long): IElement = cellElement.getElement(offset)!!

    fun getElementForIndex(index: Int): IElement? = cellElement.getElementForIndex(index)

    fun insertElementForIndex(element: IElement, index: Int) {
        cellElement.insertElementForIndex(element, index)
    }

    fun getCellNumber(): Int = cellElement.size()

    /** Takes the cell at [index] out. */
    fun detachCellAt(index: Int): IElement? = cellElement.detachElementForIndex(index)
}
