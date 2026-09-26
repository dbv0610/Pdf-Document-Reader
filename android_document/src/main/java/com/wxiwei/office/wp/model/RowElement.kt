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
}
