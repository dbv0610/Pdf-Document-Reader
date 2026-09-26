/*
 * 文件名称:          STDocument.java
 *
 * 编译器:            android2.2
 * 时间:              下午1:55:26
 */
package com.wxiwei.office.simpletext.model

import com.wxiwei.office.constant.wp.WPModelConstant

/**
 * 简单文本Model
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2011-12-28
 *
 * 负责人:          ljj8494
 */
open class STDocument : IDocument {
    // 简单文本只有一个section
    private var section: SectionElement? = null

    /**
     * 得到指定的offset的区域
     * @see WPModelConstant
     */
    override fun getArea(offset: Long): Long {
        return offset and WPModelConstant.AREA_MASK
    }

    /**
     * 得到区域开始位置
     * @see WPModelConstant
     */
    override fun getAreaStart(offset: Long): Long {
        val area = offset and WPModelConstant.AREA_MASK
        // 文本框需要特殊处理
        if (area == WPModelConstant.TEXTBOX) {
            return offset and WPModelConstant.TEXTBOX_MASK
        }
        return area
    }

    /**
     * 得到区域结束位置
     * @see WPModelConstant
     */
    override fun getAreaEnd(offset: Long): Long {
        return getAreaStart(offset) + getLength(offset)
    }

    /**
     * 得到区域文本长度
     */
    override fun getLength(offset: Long): Long {
        return section!!.getEndOffset() - section!!.getStartOffset()
    }

    /**
     * 得到章节元素
     */
    override fun getSection(offset: Long): IElement? {
        return section
    }

    /**
     *
     */
    override fun appendSection(elem: IElement) {
        section = elem as SectionElement
    }

    /**
     *
     */
    override fun appendElement(elem: IElement, offset: Long) {
    }

    /**
     * 得到页眉、页脚元素
     * @param area 区域
     * @param type Element类型，首页、奇数页、偶数页
     */
    override fun getHFElement(area: Long, type: Byte): IElement? {
        return null
    }

    /**
     * 得到脚注、尾注元素
     * @param area 区域
     */
    override fun getFEElement(offset: Long): IElement? {
        return null
    }

    /**
     * 得到段落元素
     */
    override fun getParagraph(offset: Long): IElement? {
        return section!!.getParaCollection()!!.getElement(offset)
    }

    /**
     *
     */
    override fun getParagraphForIndex(index: Int, area: Long): IElement? {
        return section!!.getParaCollection()!!.getElementForIndex(index)
    }

    /**
     * 添加段落
     * @param sectionElement    章节元素
     * @param paraElement       段落无素
     */
    override fun appendParagraph(element: IElement, offset: Long) {
        section!!.appendParagraph(element, offset)
    }

    /**
     * 插入文本
     *
     * @param str
     * @param attr
     * @param offset
     */
    override fun insertString(str: String?, attr: IAttributeSet?, offset: Long) {
    }

    /**
     *
     */
    override fun getLeaf(offset: Long): IElement? {
        val para = getParagraph(offset)
        if (para != null) {
            // leaf的offset的是相对于段落开始位置的
            return (para as ParagraphElement).getLeaf(offset)
        }
        return null
    }

    /**
     *
     */
    override fun setSectionAttr(start: Long, len: Int, attr: IAttributeSet?) {
        section!!.getAttribute()!!.mergeAttribute(attr)
    }

    /**
     *
     */
    override fun setParagraphAttr(start: Long, len: Int, attr: IAttributeSet?) {
        var start = start
        val end = start + len
        //
        while (start < end) {
            val para = section!!.getParaCollection()!!.getElement(start)
            para!!.getAttribute()!!.mergeAttribute(attr)
            start = para.getEndOffset()
        }
    }

    /**
     *
     */
    override fun setLeafAttr(start: Long, len: Int, attr: IAttributeSet?) {
        var start = start
        val end = start + len
        //
        while (start < end) {
            val leaf = getLeaf(start)
            leaf!!.getAttribute()!!.mergeAttribute(attr)
            start = leaf.getEndOffset()
        }
    }

    /**
     * get 段落总数
     */
    override fun getParaCount(area: Long): Int {
        return section!!.getParaCollection()!!.size()
    }

    /**
     *
     */
    override fun getText(start: Long, end: Long): String {
        var start = start
        var str = ""
        val len = end - start
        if (len == 0L || getArea(start) != getArea(end)) {
            return str
        }
        var leaf = getLeaf(start)
        var t = leaf!!.getText(null)
        val sIndex = (start - leaf.getStartOffset()).toInt()
        var eIndex = (if (end >= leaf.getEndOffset()) t!!.length.toLong() else end - leaf.getStartOffset()).toInt()
        str = t!!.substring(sIndex, eIndex)
        //
        start = leaf.getEndOffset()
        while (start < end) {
            leaf = getLeaf(start)
            t = leaf!!.getText(null)
            eIndex = (if (end >= leaf.getEndOffset()) t!!.length.toLong() else end - leaf.getStartOffset()).toInt()
            str = t!!.substring(0, eIndex)
            start = leaf.getEndOffset()
        }
        return str
    }

    /**
     *
     */
    override fun dispose() {
        if (section != null) {
            section!!.dispose()
            section = null
        }
    }
}
