/*
 * 文件名称:          IDocument.java
 *
 * 编译器:            android2.2
 * 时间:              下午3:49:19
 */
package com.wxiwei.office.simpletext.model

/**
 * 文本model接口
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2011-11-11
 *
 * 负责人:          ljj8494
 */
interface IDocument {

    /**
     * 得到指定的offset的区域
     * @see com.wxiwei.office.constant.wp.WPModelConstant
     */
    fun getArea(offset: Long): Long

    /**
     * 得到区域开始位置
     * @see com.wxiwei.office.constant.wp.WPModelConstant
     */
    fun getAreaStart(offset: Long): Long

    /**
     * 得到区域结束位置
     * @see com.wxiwei.office.constant.wp.WPModelConstant
     */
    fun getAreaEnd(offset: Long): Long

    /**
     * 得到区域文本长度
     */
    fun getLength(offset: Long): Long

    /**
     * 得到章节元素
     */
    fun getSection(offset: Long): IElement?

    /**
     * 得到段落元素
     */
    fun getParagraph(offset: Long): IElement?

    /**
     *
     */
    fun getParagraphForIndex(index: Int, area: Long): IElement?

    /**
     * 添加段落
     * @param sectionElement    章节元素
     * @param paraElement       段落无素
     */
    fun appendParagraph(element: IElement, offset: Long)

    /**
     *
     */
    fun appendSection(elem: IElement)

    /**
     *
     */
    fun appendElement(elem: IElement, offset: Long)

    /**
     * 得到leaf元素
     */
    fun getLeaf(offset: Long): IElement?

    /**
     * 得到页眉、页脚元素
     * @param area 区域
     * @param type Element类型，首页、奇数页、偶数页
     */
    fun getHFElement(area: Long, type: Byte): IElement?

    /**
     * 得到脚注、尾注元素
     * @param area 区域
     */
    fun getFEElement(offset: Long): IElement?

    /**
     * 插入文本
     *
     * @param str
     * @param attr
     * @param offset
     */
    fun insertString(str: String?, attr: IAttributeSet?, offset: Long)

    /**
     * 设置章节属性
     * @param start 开始Offset
     * @param len   长度
     * @param attr  属性集
     */
    fun setSectionAttr(start: Long, len: Int, attr: IAttributeSet?)

    /**
     * 设置段落属性
     *
     * @param start 开始Offset
     * @param len   长度
     * @param attr  属性集
     */
    fun setParagraphAttr(start: Long, len: Int, attr: IAttributeSet?)

    /**
     * 设置leaf属性
     * @param start 开始Offset
     * @param len   长度
     * @param attr  属性集
     */
    fun setLeafAttr(start: Long, len: Int, attr: IAttributeSet?)

    /**
     * get 段落总数
     */
    fun getParaCount(area: Long): Int

    /**
     * get 字符串
     */
    fun getText(start: Long, end: Long): String

    /**
     *
     */
    fun dispose()
}
