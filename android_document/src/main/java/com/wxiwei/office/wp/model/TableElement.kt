package com.wxiwei.office.wp.model

import com.wxiwei.office.constant.wp.WPModelConstant
import com.wxiwei.office.simpletext.model.ElementCollectionImpl
import com.wxiwei.office.simpletext.model.IDocument
import com.wxiwei.office.simpletext.model.IElement
import com.wxiwei.office.simpletext.model.LeafElement
import com.wxiwei.office.simpletext.model.ParagraphElement

class TableElement : ParagraphElement() {
    private val rowElement = ElementCollectionImpl(10)

    override fun getType(): Short = WPModelConstant.TABLE_ELEMENT

    fun appendRow(rowElem: RowElement) {
        rowElement.addElement(rowElem)
    }

    fun getRowElement(offset: Long): IElement = rowElement.getElement(offset)!!

    override fun getElementForIndex(index: Int): IElement? = rowElement.getElementForIndex(index)

    override fun getText(doc: IDocument?): String = ""

    override fun appendLeaf(leafElem: LeafElement?) {
    }

    override fun getLeaf(offset: Long): IElement? = null
}
