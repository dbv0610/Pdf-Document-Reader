package com.wxiwei.office.wp.model

import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.constant.wp.WPModelConstant
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.ElementCollectionImpl
import com.wxiwei.office.simpletext.model.IElement
import com.wxiwei.office.simpletext.model.STDocument
import com.wxiwei.office.simpletext.model.SectionElement

class WPDocument : STDocument() {
    private var root: Array<ElementCollectionImpl?>? = arrayOfNulls(6)
    private var para: Array<ElementCollectionImpl?>? = arrayOfNulls(6)
    private var table: Array<ElementCollectionImpl?>? = arrayOfNulls(4)
    private var pageBG: BackgroundAndFill? = null

    init {
        initRoot()
    }

    private fun initRoot() {
        val root = root ?: return
        val para = para ?: return
        val table = table ?: return
        root[0] = ElementCollectionImpl(5)
        root[1] = ElementCollectionImpl(3)
        root[2] = ElementCollectionImpl(3)
        root[3] = ElementCollectionImpl(5)
        root[4] = ElementCollectionImpl(5)
        root[5] = ElementCollectionImpl(5)

        para[0] = ElementCollectionImpl(100)
        para[1] = ElementCollectionImpl(3)
        para[2] = ElementCollectionImpl(3)
        para[3] = ElementCollectionImpl(5)
        para[4] = ElementCollectionImpl(5)
        para[5] = ElementCollectionImpl(5)

        table[0] = ElementCollectionImpl(5)
        table[1] = ElementCollectionImpl(5)
        table[2] = ElementCollectionImpl(5)
        table[3] = ElementCollectionImpl(5)
    }

    override fun getSection(offset: Long): IElement? = root?.get(0)?.getElement(offset)

    override fun appendSection(elem: IElement) {
        root?.get(0)?.addElement(elem)
    }

    override fun appendElement(elem: IElement, offset: Long) {
        if (elem.getType() == WPModelConstant.PARAGRAPH_ELEMENT) {
            appendParagraph(elem, offset)
        }
        getRootCollection(offset)?.addElement(elem)
    }

    override fun getHFElement(offset: Long, type: Byte): IElement? = getRootCollection(offset)?.getElement(offset)

    override fun getFEElement(offset: Long): IElement? = null

    override fun getParagraph(offset: Long): IElement? {
        if ((offset and WPModelConstant.AREA_MASK) == WPModelConstant.TEXTBOX) {
            val e = getTextboxSectionElement(offset)
            if (e != null) {
                return (e as SectionElement).getParaCollection()!!.getElement(offset)
            }
        }
        return getParaCollection(offset)?.getElement(offset)
    }

    fun getParagraph0(offset: Long): IElement? {
        val elem = getParagraph(offset) ?: return null
        if (AttrManage.instance().getParaLevel(elem.getAttribute()) >= 0) {
            val collection = getTableCollection(offset)
            if (collection != null) {
                return collection.getElement(offset)
            }
        }
        return elem
    }

    override fun getParagraphForIndex(index: Int, area: Long): IElement? {
        if ((area and WPModelConstant.AREA_MASK) == WPModelConstant.TEXTBOX) {
            val e = getTextboxSectionElement(area)
            if (e != null) {
                return (e as SectionElement).getParaCollection()!!.getElementForIndex(index)
            }
        }
        return getParaCollection(area)?.getElementForIndex(index)
    }

    override fun appendParagraph(element: IElement, offset: Long) {
        if (element.getType() == WPModelConstant.TABLE_ELEMENT) {
            getTableCollection(offset)?.addElement(element)
            return
        }
        if ((offset and WPModelConstant.AREA_MASK) == WPModelConstant.TEXTBOX) {
            val e = getTextboxSectionElement(offset)
            if (e != null) {
                (e as SectionElement).appendParagraph(element, offset)
                return
            }
        }
        getParaCollection(offset)?.addElement(element)
    }

    override fun getParaCount(area: Long): Int {
        if ((area and WPModelConstant.AREA_MASK) == WPModelConstant.TEXTBOX) {
            val e = getTextboxSectionElement(area)
            if (e != null) {
                return (e as SectionElement).getParaCollection()!!.size()
            }
        }
        val collection = getParaCollection(area)
        if (collection != null && collection.size() > 0) {
            return collection.size()
        }
        return 0
    }

    private fun getRootCollection(offset: Long): ElementCollectionImpl? {
        val collections = root ?: return null
        return when (offset and WPModelConstant.AREA_MASK) {
            WPModelConstant.MAIN -> collections[0]
            WPModelConstant.HEADER -> collections[1]
            WPModelConstant.FOOTER -> collections[2]
            WPModelConstant.FOOTNOTE -> collections[3]
            WPModelConstant.ENDNOTE -> collections[4]
            WPModelConstant.TEXTBOX -> collections[5]
            else -> null
        }
    }

    fun getParaCollection(offset: Long): ElementCollectionImpl? {
        val collections = para ?: return null
        return when (offset and WPModelConstant.AREA_MASK) {
            WPModelConstant.MAIN -> collections[0]
            WPModelConstant.HEADER -> collections[1]
            WPModelConstant.FOOTER -> collections[2]
            WPModelConstant.FOOTNOTE -> collections[3]
            WPModelConstant.ENDNOTE -> collections[4]
            WPModelConstant.TEXTBOX -> collections[5]
            else -> null
        }
    }

    fun getTableCollection(offset: Long): ElementCollectionImpl? {
        val collections = table ?: return null
        return when (offset and WPModelConstant.AREA_MASK) {
            WPModelConstant.MAIN -> collections[0]
            WPModelConstant.HEADER -> collections[1]
            WPModelConstant.FOOTER -> collections[2]
            WPModelConstant.TEXTBOX -> collections[3]
            else -> null
        }
    }

    override fun getLength(offset: Long): Long {
        val root = getRootCollection(offset)
        if (root != null) {
            if ((offset and WPModelConstant.AREA_MASK) == WPModelConstant.TEXTBOX) {
                val e = getTextboxSectionElement(offset)
                if (e != null) {
                    return e.getEndOffset() - e.getStartOffset()
                }
            }
            return root.getElementForIndex(root.size() - 1)!!.getEndOffset() -
                root.getElementForIndex(0)!!.getStartOffset()
        }
        return 0
    }

    private fun getTextboxSectionElement(offset: Long): IElement? {
        val collections = root ?: return null
        val index = (offset and WPModelConstant.TEXTBOX_MASK) shr 32
        return collections[5]?.getElementForIndex(index.toInt())
    }

    fun getTextboxSectionElementForIndex(index: Int): IElement? = root?.get(5)?.getElementForIndex(index)

    fun setPageBackground(pageBG: BackgroundAndFill?) {
        this.pageBG = pageBG
    }

    fun getPageBackground(): BackgroundAndFill? = pageBG

    override fun dispose() {
        super.dispose()
        root?.let { roots ->
            for (i in roots.indices) {
                roots[i]?.dispose()
                roots[i] = null
            }
        }
        root = null
        para?.let { paras ->
            for (i in paras.indices) {
                paras[i]?.dispose()
                paras[i] = null
            }
        }
        para = null
        table?.let { tables ->
            for (i in tables.indices) {
                tables[i]?.dispose()
                tables[i] = null
            }
        }
        table = null
    }
}
