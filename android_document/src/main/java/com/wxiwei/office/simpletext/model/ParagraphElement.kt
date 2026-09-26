/*
 * 文件名称:          ParagraphElement.java
 *
 * 编译器:            android2.2
 * 时间:              下午3:03:17
 */
package com.wxiwei.office.simpletext.model

/**
 * 段落元素
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2011-12-28
 *
 * 负责人:          ljj8494
 */
open class ParagraphElement : AbstractElement() {
    //
    private var leaf: ElementCollectionImpl? = ElementCollectionImpl(10)

    /**
     *
     */
    override fun getText(doc: IDocument?): String? {
        val count = leaf!!.size()
        val text = StringBuilder()
        for (i in 0 until count) {
            text.append(leaf!!.getElementForIndex(i)!!.getText(null))
        }
        return text.toString()
    }

    /**
     *
     */
    open fun appendLeaf(leafElem: LeafElement?) {
        leaf!!.addElement(leafElem)
    }

    /**
     *
     */
    open fun getLeaf(offset: Long): IElement? {
        return leaf!!.getElement(offset)
    }

    /**
     * 得到指定index的Offset
     */
    open fun getElementForIndex(index: Int): IElement? {
        return leaf!!.getElementForIndex(index)
    }

    /**
     *
     */
    override fun dispose() {
        super.dispose()
        if (leaf != null) {
            leaf!!.dispose()
            leaf = null
        }
    }
}
