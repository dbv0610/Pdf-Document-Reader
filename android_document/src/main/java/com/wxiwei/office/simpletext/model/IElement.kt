/*
 * 文件名称:          IElement.java
 *
 * 编译器:            android2.2
 * 时间:              下午4:06:29
 */
package com.wxiwei.office.simpletext.model

/**
 * Model的元素，主要是章节、段落、Leaf
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2011-11-11
 *
 * 负责人:          ljj8494
 */
interface IElement {
    /**
     *
     */
    fun getType(): Short

    /**
     * 开始Offset
     */
    fun setStartOffset(start: Long)

    fun getStartOffset(): Long

    /**
     * 结束Offset
     */
    fun setEndOffset(end: Long)

    fun getEndOffset(): Long

    /**
     *
     */
    fun setAttribute(attrSet: IAttributeSet?)

    /**
     * 得到属性集
     */
    fun getAttribute(): IAttributeSet?

    /**
     * 得到文本
     */
    fun getText(doc: IDocument?): String?

    /**
     *
     */
    fun dispose()
}
