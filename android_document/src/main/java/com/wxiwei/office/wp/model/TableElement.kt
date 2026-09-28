/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
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

    fun rowCount(): Int = rowElement.size()

    /** Puts [row] in at [index] (its offsets must already be right). */
    fun insertRow(index: Int, row: RowElement) {
        rowElement.insertElementForIndex(row, index)
    }

    /** Takes the row at [index] out. */
    fun detachRowAt(index: Int): IElement? = rowElement.detachElementForIndex(index)

    override fun getText(doc: IDocument?): String = ""

    override fun appendLeaf(leafElem: LeafElement?) {
    }

    override fun getLeaf(offset: Long): IElement? = null
}
