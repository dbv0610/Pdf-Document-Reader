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
     * Plain text leaves overlapping [start, end), after splitting the ones that cross a boundary
     * (the halves keep copies of the attributes). Other leaves (shapes, fields) are never split
     * and are left out. Used by live editing to format part of a run.
     */
    fun leavesFor(start: Long, end: Long): List<LeafElement> {
        val leaves = leaf ?: return emptyList()
        fun splitAt(offset: Long) {
            for (i in 0 until leaves.size()) {
                val l = leaves.getElementForIndex(i) as? LeafElement ?: continue
                if (l.javaClass != LeafElement::class.java) continue
                val ls = l.getStartOffset()
                val le = l.getEndOffset()
                if (offset <= ls || offset >= le) continue
                val text = l.getText(null) ?: return
                val k = (offset - ls).toInt()
                if (k <= 0 || k >= text.length) return
                val left = LeafElement(text.substring(0, k)).apply {
                    setAttribute(l.getAttribute().clone()); setStartOffset(ls); setEndOffset(offset)
                }
                val right = LeafElement(text.substring(k)).apply {
                    setAttribute(l.getAttribute().clone()); setStartOffset(offset); setEndOffset(le)
                }
                leaves.removeElementForIndex(i)
                leaves.insertElementForIndex(left, i)
                leaves.insertElementForIndex(right, i + 1)
                return
            }
        }
        splitAt(start)
        splitAt(end)
        val result = ArrayList<LeafElement>()
        for (i in 0 until leaves.size()) {
            val l = leaves.getElementForIndex(i) as? LeafElement ?: continue
            if (l.javaClass != LeafElement::class.java) continue
            if (l.getStartOffset() >= start && l.getEndOffset() <= end && l.getEndOffset() > l.getStartOffset()) result.add(l)
        }
        return result
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
