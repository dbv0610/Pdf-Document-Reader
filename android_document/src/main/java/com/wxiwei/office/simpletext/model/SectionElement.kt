/*
 * 文件名称:          SectionElement.java
 *
 * 编译器:            android2.2
 * 时间:              下午4:03:46
 */
package com.wxiwei.office.simpletext.model

import com.wxiwei.office.constant.wp.WPModelConstant

/**
 * 单节元素
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2011-12-29
 *
 * 负责人:          ljj8494
 */
open class SectionElement : AbstractElement() {
    //
    private var paraCollection: IElementCollection? = ElementCollectionImpl(10)

    /**
     *
     */
    override fun getType(): Short {
        return WPModelConstant.SECTION_ELEMENT
    }

    /**
     *
     * @param element
     * @param offset
     */
    fun appendParagraph(element: IElement?, offset: Long) {
        (paraCollection as ElementCollectionImpl).addElement(element)
    }

    /**
     * get paragraph collection of this sectionElement
     */
    fun getParaCollection(): IElementCollection? {
        return this.paraCollection
    }

    /**
     *
     */
    override fun getText(doc: IDocument?): String? {
        val count = paraCollection!!.size()
        var text = ""
        for (i in 0 until count) {
            text += paraCollection!!.getElementForIndex(i)!!.getText(null)
        }
        return text
    }

    /**
     *
     */
    fun getElement(offset: Long): IElement? {
        return paraCollection!!.getElement(offset)
    }

    /**
     *
     */
    override fun dispose() {
        super.dispose()
        if (paraCollection != null) {
            paraCollection!!.dispose()
            paraCollection = null
        }
    }
}
